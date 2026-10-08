/**
 * Identidad institucional (RN17, CU-00).
 * Carga nombre, eslogan, logo y colores desde GET /api/publico/institucion y
 * los aplica a la pagina. Nada de esto vive en el codigo del frontend: para
 * otro banco solo cambia la configuracion del backend.
 * Requiere js/api.js (API_BASE_URL).
 */
const Identidad = {
    /** Respaldo neutro si el backend no responde (no contiene ninguna marca). */
    respaldo: { nombre: "Sistema de Quejas Bancario", eslogan: "", logoUrl: null, colores: null },
    datos: null,

    async cargar() {
        let datos = null;
        try {
            const controlador = new AbortController();
            const temporizador = setTimeout(() => controlador.abort(), 5000);
            const resp = await fetch(API_BASE_URL + "/publico/institucion", { signal: controlador.signal });
            clearTimeout(temporizador);
            if (resp.ok) datos = await resp.json();
        } catch (e) {
            datos = null; // se usa el respaldo neutro
        }
        this.datos = datos;
        this.aplicar(datos || this.respaldo);
        document.body.classList.remove("identidad-cargando");
        return datos;
    },

    aplicar(d) {
        const raiz = document.documentElement.style;
        const esColor = c => typeof c === "string" && /^#[0-9a-f]{3,8}$/i.test(c.trim());
        if (d.colores) {
            if (esColor(d.colores.primario)) raiz.setProperty("--brand-primary", d.colores.primario.trim());
            if (esColor(d.colores.secundario)) raiz.setProperty("--brand-secondary", d.colores.secundario.trim());
            if (esColor(d.colores.acento)) raiz.setProperty("--brand-accent", d.colores.acento.trim());
        }

        document.querySelectorAll("[data-id='nombre']").forEach(el => { el.textContent = d.nombre; });
        document.querySelectorAll("[data-id='eslogan']").forEach(el => {
            if (!d.eslogan && el.hasAttribute("data-conservar")) return; // conserva su texto genérico
            el.textContent = d.eslogan || "";
            el.hidden = !d.eslogan;
        });

        document.querySelectorAll("[data-id='logo']").forEach(img => {
            const fallback = img.parentElement.querySelector("[data-id='logo-fallback']");
            if (d.logoUrl) {
                img.onerror = () => { img.hidden = true; if (fallback) fallback.hidden = false; };
                img.src = d.logoUrl;
                img.alt = "Logotipo de " + d.nombre;
                img.hidden = false;
                if (fallback) fallback.hidden = true;
            } else {
                img.hidden = true;
                if (fallback) fallback.hidden = false;
            }
        });

        if (d.logoUrl) {
            let icono = document.querySelector("link[rel='icon']");
            if (!icono) {
                icono = document.createElement("link");
                icono.rel = "icon";
                document.head.appendChild(icono);
            }
            icono.href = d.logoUrl;
        }

        const seccion = document.body.dataset.titulo;
        document.title = seccion ? seccion + " · " + d.nombre : d.nombre;
    }
};

/** Si ya hay sesion, los botones "Iniciar sesion" pasan a "Ir a mi panel". */
function ajustarAccesoSegunSesion() {
    if (!Sesion.estaAutenticado()) return;
    const usuario = Sesion.usuario();
    if (!usuario) return;
    document.querySelectorAll("[data-accion='login']").forEach(a => {
        a.href = rutaPanelPorRol(usuario.rol);
        const icono = a.querySelector("i");
        a.textContent = " Ir a mi panel";
        if (icono) a.prepend(icono);
    });
}
