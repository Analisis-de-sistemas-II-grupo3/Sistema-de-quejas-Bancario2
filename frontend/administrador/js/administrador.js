const usuario = Sesion.exigirRol("ADMINISTRADOR");

renderNavbar("navbar", "Panel de Administración", [
    { texto: "Administración", href: "#", activo: true }
]);

// ---------------- Usuarios (CU-14) ----------------

async function cargarUsuarios() {
    const tbody = document.getElementById("tablaUsuarios");
    tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Cargando...</td></tr>`;
    try {
        const usuarios = await apiFetch("/usuarios");
        tbody.innerHTML = usuarios.map(u => `
            <tr>
                <td>${u.nombreUsuario}</td>
                <td>${u.nombreCompleto}</td>
                <td>${u.correoElectronico}</td>
                <td><span class="badge bg-secondary">${u.rol}</span></td>
                <td>${u.estado === "Activo"
                    ? `<span class="badge bg-success">Activo</span>`
                    : `<span class="badge bg-danger">Inactivo</span>`}</td>
                <td class="text-end">
                    ${u.estado === "Activo"
                        ? `<button class="btn btn-sm btn-outline-danger" onclick="cambiarEstado(${u.idUsuario}, 'Inactivo')"><i class="bi bi-slash-circle"></i> Inactivar</button>`
                        : `<button class="btn btn-sm btn-outline-success" onclick="cambiarEstado(${u.idUsuario}, 'Activo')"><i class="bi bi-check-circle"></i> Activar</button>`}
                </td>
            </tr>
        `).join("");
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    }
}

async function cargarRoles() {
    try {
        const roles = await apiFetch("/catalogos/roles");
        document.getElementById("uRol").innerHTML = roles.map(r => `<option value="${r.nombre}">${r.nombre}</option>`).join("");
    } catch (err) { /* silencioso */ }
}

async function crearUsuario() {
    const request = {
        nombreUsuario: document.getElementById("uNombreUsuario").value.trim(),
        nombreCompleto: document.getElementById("uNombreCompleto").value.trim(),
        correoElectronico: document.getElementById("uCorreo").value.trim(),
        rol: document.getElementById("uRol").value,
        contrasenaInicial: document.getElementById("uContrasena").value || null
    };
    mostrarCargando(true);
    try {
        await apiFetch("/usuarios", { method: "POST", body: request });
        bootstrap.Modal.getInstance(document.getElementById("modalUsuario")).hide();
        document.getElementById("formUsuario").reset();
        mostrarAlerta("alertas", "Usuario creado correctamente. Se envió un correo con sus credenciales.", "success");
        cargarUsuarios();
    } catch (err) {
        mostrarAlerta("alertasUsuario", err.message);
    } finally {
        mostrarCargando(false);
    }
}

async function cambiarEstado(idUsuario, estado) {
    mostrarCargando(true);
    try {
        await apiFetch(`/usuarios/${idUsuario}/estado`, { method: "PATCH", body: { estado } });
        cargarUsuarios();
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    } finally {
        mostrarCargando(false);
    }
}

// ---------------- Catalogos (CU-15) ----------------

async function cargarCatalogos() {
    try {
        const [tipos, categorias, productos] = await Promise.all([
            apiFetch("/catalogos/tipos-caso"),
            apiFetch("/catalogos/categorias"),
            apiFetch("/catalogos/productos")
        ]);
        document.getElementById("listaTiposCaso").innerHTML = tipos.map(t =>
            `<li class="list-group-item d-flex justify-content-between"><span>${t.nombre}</span><span class="badge bg-secondary">${t.extra}</span></li>`).join("");
        document.getElementById("listaCategorias").innerHTML = categorias.map(c =>
            `<li class="list-group-item">${c.nombre}</li>`).join("") || `<li class="list-group-item text-muted">Sin categorías</li>`;
        document.getElementById("listaProductos").innerHTML = productos.map(p =>
            `<li class="list-group-item">${p.nombre}</li>`).join("") || `<li class="list-group-item text-muted">Sin productos</li>`;
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    }
}

document.getElementById("formTipoCaso").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
        await apiFetch("/catalogos/tipos-caso", { method: "POST", body: {
            nombre: document.getElementById("nuevoTipoNombre").value.trim(),
            prefijo: document.getElementById("nuevoTipoPrefijo").value.trim().toUpperCase()
        }});
        e.target.reset();
        cargarCatalogos();
    } catch (err) { mostrarAlerta("alertas", err.message); }
});

document.getElementById("formCategoria").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
        await apiFetch("/catalogos/categorias", { method: "POST", body: { nombre: document.getElementById("nuevaCategoriaNombre").value.trim() }});
        e.target.reset();
        cargarCatalogos();
    } catch (err) { mostrarAlerta("alertas", err.message); }
});

document.getElementById("formProducto").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
        await apiFetch("/catalogos/productos", { method: "POST", body: { nombre: document.getElementById("nuevoProductoNombre").value.trim() }});
        e.target.reset();
        cargarCatalogos();
    } catch (err) { mostrarAlerta("alertas", err.message); }
});

// ---------------- Parametros (CU-16) ----------------

async function cargarParametros() {
    const tbody = document.getElementById("tablaParametros");
    try {
        const parametros = await apiFetch("/parametros");
        tbody.innerHTML = parametros.map(p => `
            <tr>
                <td>${p.nombreParametro}</td>
                <td><input class="form-control form-control-sm" style="max-width:120px" id="param-${p.idParametro}" value="${p.valor}"></td>
                <td class="text-end"><button class="btn btn-sm btn-primary" onclick="guardarParametro(${p.idParametro})">Guardar</button></td>
            </tr>
        `).join("");
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    }
}

async function guardarParametro(idParametro) {
    const valor = document.getElementById(`param-${idParametro}`).value;
    mostrarCargando(true);
    try {
        await apiFetch(`/parametros/${idParametro}`, { method: "PUT", body: { valor } });
        mostrarAlerta("alertas", "Parámetro actualizado correctamente.", "success");
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    } finally {
        mostrarCargando(false);
    }
}

// ---------------- Reportes (CU-12) ----------------

async function descargarReporte(formato) {
    mostrarCargando(true);
    try {
        const params = new URLSearchParams({ formato });
        const estado = document.getElementById("rEstado").value;
        if (estado) params.set("estado", estado);
        await descargarArchivo("/reportes/casos?" + params.toString(), `reporte_casos.${formato === "pdf" ? "pdf" : "xlsx"}`);
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    } finally {
        mostrarCargando(false);
    }
}

cargarUsuarios();
cargarRoles();
cargarCatalogos();
cargarParametros();
