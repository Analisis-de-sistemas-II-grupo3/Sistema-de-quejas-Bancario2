/**
 * Configuracion central del cliente API.
 * Cambia API_BASE_URL si tu backend corre en otro host/puerto.
 */
const API_BASE_URL = window.API_BASE_URL || "http://localhost:8080/api";

/* =====================================================================
 * Notificaciones (mensajes AN01 de exito / AN02 de error de las Reglas de Negocio)
 *  - Notificar.mostrar(): notificacion flotante SIEMPRE visible (queda por encima de
 *    cualquier modal, no depende de la posicion del scroll).
 *  - Flash: guarda mensajes en sessionStorage para mostrarlos en la pagina siguiente
 *    (p.ej. "Inicio de sesion exitoso" justo antes de redirigir al panel).
 *  - apiFetch muestra automaticamente los mensajes de exito que envia el backend
 *    en la cabecera X-Mensaje (ver util/Mensajes.java).
 * ===================================================================== */
const Notificar = (() => {
    const ICONOS = {
        success: "bi-check-circle-fill",
        danger: "bi-exclamation-octagon-fill",
        warning: "bi-exclamation-triangle-fill",
        info: "bi-info-circle-fill"
    };
    let contenedor = null;

    function inyectarEstilos() {
        if (document.getElementById("qbToastEstilos")) return;
        const st = document.createElement("style");
        st.id = "qbToastEstilos";
        st.textContent = `
#qbToasts{position:fixed;top:1rem;left:50%;transform:translateX(-50%);z-index:2000;display:flex;flex-direction:column;gap:.6rem;width:min(540px,92vw);pointer-events:none}
.qb-toast{pointer-events:auto;display:flex;align-items:flex-start;gap:.7rem;padding:.8rem 1rem;border-radius:.8rem;border-left:6px solid;box-shadow:0 10px 30px rgba(0,0,0,.18);font-size:.95rem;line-height:1.35;animation:qbIn .25s ease-out}
.qb-toast i{font-size:1.2rem;margin-top:.05rem}
.qb-toast .qb-txt{flex:1;word-break:break-word}
.qb-toast button{border:0;background:transparent;font-size:1.3rem;line-height:1;cursor:pointer;opacity:.6;color:inherit;padding:0 .2rem}
.qb-toast button:hover{opacity:1}
.qb-toast.success{background:#dff3ea;color:#14744f;border-color:#14744f}
.qb-toast.danger{background:#fbe3e7;color:#8f2438;border-color:#c0334d}
.qb-toast.warning{background:#fff3cd;color:#664d03;border-color:#e0a800}
.qb-toast.info{background:#dbeafe;color:#1e40af;border-color:#3b82f6}
@keyframes qbIn{from{opacity:0;transform:translateY(-8px)}to{opacity:1;transform:none}}`;
        document.head.appendChild(st);
    }

    function asegurarContenedor() {
        if (contenedor && document.body.contains(contenedor)) return contenedor;
        inyectarEstilos();
        contenedor = document.createElement("div");
        contenedor.id = "qbToasts";
        contenedor.setAttribute("aria-live", "polite");
        document.body.appendChild(contenedor);
        return contenedor;
    }

    function mostrar(mensaje, tipo = "success", ms) {
        if (!mensaje) return;
        if (!document.body) {
            document.addEventListener("DOMContentLoaded", () => mostrar(mensaje, tipo, ms));
            return;
        }
        const lista = asegurarContenedor();
        while (lista.children.length >= 5) lista.removeChild(lista.firstChild);

        const toast = document.createElement("div");
        toast.className = "qb-toast " + (ICONOS[tipo] ? tipo : "info");
        toast.setAttribute("role", tipo === "danger" ? "alert" : "status");

        const icono = document.createElement("i");
        icono.className = "bi " + (ICONOS[tipo] || ICONOS.info);
        const texto = document.createElement("span");
        texto.className = "qb-txt";
        texto.textContent = mensaje;               // textContent: nunca interpreta HTML
        const cerrar = document.createElement("button");
        cerrar.type = "button";
        cerrar.setAttribute("aria-label", "Cerrar");
        cerrar.innerHTML = "&times;";
        cerrar.addEventListener("click", () => toast.remove());

        toast.append(icono, texto, cerrar);
        lista.appendChild(toast);
        setTimeout(() => toast.remove(), ms || (tipo === "success" ? 6000 : 9000));
    }

    return { mostrar };
})();

const Flash = {
    guardar(mensajes, tipo = "success") {
        if (!mensajes || !mensajes.length) return;
        try { sessionStorage.setItem("qb_flash", JSON.stringify({ mensajes, tipo })); } catch (e) { /* sin storage */ }
    },
    mostrarPendiente() {
        try {
            const raw = sessionStorage.getItem("qb_flash");
            if (!raw) return;
            sessionStorage.removeItem("qb_flash");
            const f = JSON.parse(raw);
            (f.mensajes || []).forEach(m => Notificar.mostrar(m, f.tipo || "success"));
        } catch (e) { /* ignorar */ }
    }
};
if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", () => Flash.mostrarPendiente());
} else {
    Flash.mostrarPendiente();
}

/** Lee los mensajes AN01 que envia el backend en la cabecera X-Mensaje. */
function mensajesDeRespuesta(respuesta) {
    const crudo = respuesta.headers.get("X-Mensaje");
    if (!crudo) return [];
    return crudo.split(",").map(m => {
        try { return decodeURIComponent(m); } catch (e) { return m; }
    }).filter(Boolean);
}

const Sesion = {
    guardar(login) {
        localStorage.setItem("qb_token", login.token);
        localStorage.setItem("qb_usuario", JSON.stringify({
            idUsuario: login.idUsuario,
            nombreUsuario: login.nombreUsuario,
            nombreCompleto: login.nombreCompleto,
            rol: login.rol
        }));
    },
    token() {
        return localStorage.getItem("qb_token");
    },
    usuario() {
        const raw = localStorage.getItem("qb_usuario");
        return raw ? JSON.parse(raw) : null;
    },
    limpiar() {
        localStorage.removeItem("qb_token");
        localStorage.removeItem("qb_usuario");
    },
    estaAutenticado() {
        return !!this.token();
    },
    /** Redirige a la persona si no tiene sesion, o si su rol no coincide con el esperado. */
    exigirRol(rolEsperado) {
        const usuario = this.usuario();
        if (!this.estaAutenticado() || !usuario) {
            window.location.href = rutaLogin();
            return null;
        }
        if (rolEsperado && usuario.rol.toUpperCase() !== rolEsperado.toUpperCase()) {
            window.location.href = rutaLogin();
            return null;
        }
        return usuario;
    }
};

function rutaLogin() {
    // Calcula la ruta relativa al login segun la profundidad de carpetas.
    const profundidad = window.location.pathname.split("/").filter(Boolean).length;
    return window.location.pathname.includes("/cliente/") ||
           window.location.pathname.includes("/agente/") ||
           window.location.pathname.includes("/supervisor/") ||
           window.location.pathname.includes("/administrador/") ||
           window.location.pathname.includes("/auditor/")
        ? "../login.html" : "login.html";
}

/** Panel de destino segun el rol (usado por el login y por el Portal). */
function rutaPanelPorRol(rol) {
    switch (String(rol).toUpperCase()) {
        case "CLIENTE": return "cliente/dashboard.html";
        case "AGENTE": return "agente/dashboard.html";
        case "SUPERVISOR": return "supervisor/dashboard.html";
        case "ADMINISTRADOR": return "administrador/dashboard.html";
        case "AUDITOR": return "auditor/dashboard.html";
        default: return "login.html";
    }
}

/**
 * Wrapper de fetch: agrega el token JWT, maneja JSON, y estandariza errores.
 * @param {string} path - ruta relativa a API_BASE_URL, ej. "/casos/mis-casos"
 * @param {object} opciones - opciones estilo fetch (method, body, headers, isFormData)
 */
async function apiFetch(path, opciones = {}) {
    const headers = opciones.headers ? { ...opciones.headers } : {};
    const token = Sesion.token();
    if (token) headers["Authorization"] = "Bearer " + token;

    let body = opciones.body;
    if (body && !(body instanceof FormData)) {
        headers["Content-Type"] = "application/json";
        body = JSON.stringify(body);
    }

    let respuesta;
    try {
        respuesta = await fetch(API_BASE_URL + path, { ...opciones, headers, body });
    } catch (e) {
        throw new Error("No fue posible conectar con el servidor. Verifique su conexión o que el backend esté en ejecución.");
    }

    if (respuesta.status === 401 && !path.startsWith("/auth/login")) {
        Sesion.limpiar();
        window.location.href = rutaLogin();
        throw new Error("Su sesión expiró. Por favor inicie sesión nuevamente.");
    }

    const contentType = respuesta.headers.get("content-type") || "";

    if (!respuesta.ok) {
        let mensaje = "Ocurrió un error inesperado.";
        if (contentType.includes("application/json")) {
            const data = await respuesta.json().catch(() => null);
            if (data && data.mensaje) mensaje = data.mensaje;
        }
        throw new Error(mensaje);
    }

    if (!opciones.raw) {
        // AN01: el backend informa el resultado de la accion en la cabecera X-Mensaje.
        // opciones.flash = true -> se muestra en la pagina siguiente (cuando se redirige justo despues).
        const mensajes = mensajesDeRespuesta(respuesta);
        if (opciones.flash) Flash.guardar(mensajes, "success");
        else mensajes.forEach(m => Notificar.mostrar(m, "success"));
    }

    if (opciones.raw) return respuesta; // para descargas binarias (blob)
    if (respuesta.status === 204) return null;
    if (contentType.includes("application/json")) return respuesta.json();
    return respuesta.text();
}

/** Descarga un archivo binario (reportes PDF/Excel) generando un enlace temporal. */
async function descargarArchivo(path, nombreSugerido) {
    const respuesta = await apiFetch(path, { raw: true });
    const blob = await respuesta.blob();
    const disposition = respuesta.headers.get("content-disposition") || "";
    let nombre = nombreSugerido;
    const match = disposition.match(/filename="?([^"]+)"?/);
    if (match) nombre = match[1];

    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = nombre;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
}

function mostrarCargando(activo) {
    const overlay = document.getElementById("spinnerOverlay");
    if (overlay) overlay.classList.toggle("activo", activo);
}

/**
 * Muestra un mensaje (normalmente AN02 de error). Si el contenedor esta DENTRO de un modal el
 * mensaje se muestra ahi mismo; en cualquier otro caso se muestra como notificacion flotante,
 * visible aunque haya un modal abierto o la pagina este desplazada.
 */
function mostrarAlerta(contenedorId, mensaje, tipo = "danger") {
    const contenedor = contenedorId ? document.getElementById(contenedorId) : null;
    if (contenedor && contenedor.closest(".modal")) {
        const alerta = document.createElement("div");
        alerta.className = `alert alert-${tipo} alert-dismissible fade show`;
        alerta.setAttribute("role", "alert");
        alerta.appendChild(document.createTextNode(mensaje));
        const cerrar = document.createElement("button");
        cerrar.type = "button";
        cerrar.className = "btn-close";
        cerrar.setAttribute("data-bs-dismiss", "alert");
        alerta.appendChild(cerrar);
        contenedor.replaceChildren(alerta);
        return;
    }
    Notificar.mostrar(mensaje, tipo);
}

function badgeEstado(estado) {
    const clase = "badge-" + String(estado).replace(/\s+/g, "-");
    return `<span class="badge badge-estado ${clase}">${estado}</span>`;
}

function formatearFecha(fechaIso) {
    if (!fechaIso) return "-";
    const f = new Date(fechaIso);
    return f.toLocaleString("es-GT", { dateStyle: "short", timeStyle: "short" });
}
