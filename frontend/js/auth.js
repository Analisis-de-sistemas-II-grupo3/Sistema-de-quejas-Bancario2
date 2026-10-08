// Si ya hay sesion activa, redirige directo a su panel.
(function redirigirSiYaHaySesion() {
    if (Sesion.estaAutenticado()) {
        const usuario = Sesion.usuario();
        if (usuario) window.location.href = rutaPanelPorRol(usuario.rol);
    }
})();

document.getElementById("formLogin").addEventListener("submit", async (e) => {
    e.preventDefault();
    const nombreUsuario = document.getElementById("nombreUsuario").value.trim();
    const contrasena = document.getElementById("contrasena").value;

    mostrarCargando(true);
    try {
        const login = await apiFetch("/auth/login", {
            method: "POST",
            body: { nombreUsuario, contrasena }
        });
        Sesion.guardar(login);
        window.location.href = rutaPanelPorRol(login.rol);
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    } finally {
        mostrarCargando(false);
    }
});
