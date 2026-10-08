/**
 * Configuracion central del cliente API.
 * Cambia API_BASE_URL si tu backend corre en otro host/puerto.
 */
const API_BASE_URL = window.API_BASE_URL || "http://localhost:8080/api";

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

    if (respuesta.status === 401) {
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

function mostrarAlerta(contenedorId, mensaje, tipo = "danger") {
    const contenedor = document.getElementById(contenedorId);
    if (!contenedor) { alert(mensaje); return; }
    contenedor.innerHTML = `<div class="alert alert-${tipo} alert-dismissible fade show" role="alert">
        ${mensaje}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>`;
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
