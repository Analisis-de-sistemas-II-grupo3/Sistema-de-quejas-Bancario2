function escaparHtml(texto) {
    return String(texto ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

/**
 * Dibuja la barra superior y el encabezado del panel con la misma identidad
 * visual de la pagina de inicio (logo, nombre y colores vienen de Identidad).
 * @param {string} idContenedor  id del <div> donde se dibuja
 * @param {string} titulo        nombre del panel, ej. "Portal del Cliente"
 * @param {Array}  enlaces       enlaces de navegacion [{texto, href, activo}]
 * @param {string} descripcion   frase corta que explica para que sirve el panel
 */
function renderNavbar(idContenedor, titulo, enlaces, descripcion) {
    const usuario = Sesion.usuario();
    const nombreCompleto = usuario && usuario.nombreCompleto ? usuario.nombreCompleto.trim() : "";
    const partes = nombreCompleto.split(/\s+/).filter(Boolean);
    const primerNombre = partes[0] || "";
    const iniciales = partes.length > 1
        ? (partes[0][0] + partes[1][0]).toUpperCase()
        : (partes[0] || "?").slice(0, 2).toUpperCase();

    // Con un solo enlace el encabezado ya indica donde esta la persona.
    const links = (enlaces || []).length > 1
        ? enlaces.map(l =>
            `<li class="nav-item"><a class="nav-link ${l.activo ? "activo" : ""}" href="${l.href}">${l.texto}</a></li>`
          ).join("")
        : "";

    document.getElementById(idContenedor).innerHTML = `
    <nav class="navbar navbar-expand-lg portal-nav panel-nav">
      <div class="container">
        <span class="navbar-brand">
          <span class="brand-mark">
            <img data-id="logo" alt="" hidden>
            <i class="bi bi-bank2" data-id="logo-fallback"></i>
          </span>
          <span data-id="nombre">Sistema de Quejas Bancario</span>
        </span>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent" aria-label="Abrir menú">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarContent">
          <ul class="navbar-nav me-auto ms-lg-3">${links}</ul>
          <div class="panel-user">
            <span class="panel-avatar" aria-hidden="true">${escaparHtml(iniciales)}</span>
            <span class="panel-user-name">${escaparHtml(nombreCompleto)}</span>
          </div>
          <button class="btn btn-ghost btn-sm" id="btnLogout"><i class="bi bi-box-arrow-right me-1"></i>Salir</button>
        </div>
      </div>
    </nav>

    <header class="panel-hero">
      <div class="container">
        <span class="hero-eyebrow">${escaparHtml(titulo)}</span>
        <h1>${primerNombre ? "Hola, " + escaparHtml(primerNombre) : escaparHtml(titulo)}</h1>
        ${descripcion ? `<p>${escaparHtml(descripcion)}</p>` : ""}
      </div>
      <div class="panel-sun" aria-hidden="true"></div>
      <svg class="panel-waves" viewBox="0 0 1440 110" preserveAspectRatio="none" aria-hidden="true">
        <path class="pw1" d="M0 55c120-40 240-40 360 0s240 40 360 0 240-40 360 0 240 40 360 0V110H0Z"/>
        <path class="pw2" d="M0 80c120-30 240-30 360 0s240 30 360 0 240-30 360 0 240 30 360 0V110H0Z"/>
      </svg>
    </header>`;

    document.getElementById("btnLogout").addEventListener("click", cerrarSesion);

    // Logo, nombre y colores del banco (igual que en la pagina de inicio).
    if (typeof Identidad !== "undefined") Identidad.cargar();
}

async function cerrarSesion() {
    try {
        await apiFetch("/auth/logout", { method: "POST", flash: true });   // AN01 #11 se muestra en la pagina de inicio
    } catch (e) {
        // Continuamos con el cierre local aunque falle la llamada al backend.
    } finally {
        Sesion.limpiar();
        window.location.href = "../index.html";
    }
}
