function renderNavbar(idContenedor, titulo, enlaces) {
    const usuario = Sesion.usuario();
    const links = enlaces.map(l =>
        `<li class="nav-item"><a class="nav-link ${l.activo ? "fw-bold text-decoration-underline" : ""}" href="${l.href}">${l.texto}</a></li>`
    ).join("");

    document.getElementById(idContenedor).innerHTML = `
    <nav class="navbar navbar-expand-lg navbar-umg mb-4">
      <div class="container-fluid">
        <span class="navbar-brand"><i class="bi bi-bank2 me-2"></i>${titulo}</span>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarContent">
          <ul class="navbar-nav me-auto">${links}</ul>
          <span class="navbar-text text-white me-3">
            <i class="bi bi-person-circle me-1"></i>${usuario ? usuario.nombreCompleto : ""}
          </span>
          <button class="btn btn-outline-light btn-sm" id="btnLogout"><i class="bi bi-box-arrow-right me-1"></i>Salir</button>
        </div>
      </div>
    </nav>`;

    document.getElementById("btnLogout").addEventListener("click", cerrarSesion);
}

async function cerrarSesion() {
    try {
        await apiFetch("/auth/logout", { method: "POST" });
    } catch (e) {
        // Continuamos con el cierre local aunque falle la llamada al backend.
    } finally {
        Sesion.limpiar();
        window.location.href = "../index.html";
    }
}
