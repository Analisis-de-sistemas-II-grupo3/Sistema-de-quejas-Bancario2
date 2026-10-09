const usuario = Sesion.exigirRol("CLIENTE");

renderNavbar("navbar", "Portal del Cliente", [
    { texto: "Mis Casos", href: "#", activo: true }
], "Registre un caso y siga su avance hasta que se resuelva.");

function mostrarSeccion(idSeccion) {
    document.getElementById("seccionMisCasos").style.display = idSeccion === "seccionMisCasos" ? "block" : "none";
    document.getElementById("seccionRegistrar").style.display = idSeccion === "seccionRegistrar" ? "block" : "none";
    if (idSeccion === "seccionMisCasos") cargarMisCasos();
}

async function cargarCatalogos() {
    try {
        const [tipos, cuentas, productos] = await Promise.all([
            apiFetch("/catalogos/tipos-caso"),
            apiFetch("/cuentas/mis-cuentas"),
            apiFetch("/catalogos/productos")
        ]);

        const selTipo = document.getElementById("idTipoCaso");
        selTipo.innerHTML = tipos.map(t => `<option value="${t.id}">${t.nombre}</option>`).join("");

        const selCuenta = document.getElementById("numeroCuenta");
        if (cuentas.length === 0) {
            selCuenta.innerHTML = `<option value="">No tiene cuentas activas registradas</option>`;
        } else {
            selCuenta.innerHTML = cuentas.map(c => `<option value="${c.numeroCuenta}">${c.numeroCuenta}</option>`).join("");
        }

        const selProducto = document.getElementById("idProducto");
        selProducto.innerHTML = `<option value="">-- No aplica --</option>` +
            productos.map(p => `<option value="${p.id}">${p.nombre}</option>`).join("");
    } catch (err) {
        mostrarAlerta("alertasRegistrar", err.message);
    }
}

async function cargarMisCasos() {
    const tbody = document.getElementById("tablaMisCasos");
    tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Cargando...</td></tr>`;
    try {
        const casos = await apiFetch("/casos/mis-casos");
        if (casos.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Aún no ha registrado ningún caso.</td></tr>`;
            return;
        }
        tbody.innerHTML = casos.map(c => `
            <tr class="card-caso" onclick="verDetalle(${c.idCaso})">
                <td><strong>${c.numeroCaso}</strong></td>
                <td>${formatearFecha(c.fechaRegistro)}</td>
                <td>${c.tipoCaso}</td>
                <td>${c.agenteAsignado || "Pendiente de asignación"}</td>
                <td>${badgeEstado(c.estado)}</td>
                <td><i class="bi bi-chevron-right"></i></td>
            </tr>
        `).join("");
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    }
}

async function verDetalle(idCaso) {
    const modal = new bootstrap.Modal(document.getElementById("modalDetalle"));
    document.getElementById("cuerpoDetalle").innerHTML = "Cargando...";
    modal.show();
    try {
        const c = await apiFetch(`/casos/mis-casos/${idCaso}`);
        document.getElementById("cuerpoDetalle").innerHTML = `
            <div class="row g-2 mb-3">
                <div class="col-6"><strong>No. Caso:</strong> ${c.numeroCaso}</div>
                <div class="col-6"><strong>Estado:</strong> ${badgeEstado(c.estado)}</div>
                <div class="col-6"><strong>Tipo:</strong> ${c.tipoCaso}</div>
                <div class="col-6"><strong>Agente:</strong> ${c.agenteAsignado || "Pendiente de asignación"}</div>
                <div class="col-6"><strong>Cuenta:</strong> ${c.numeroCuenta}</div>
                <div class="col-6"><strong>Fecha registro:</strong> ${formatearFecha(c.fechaRegistro)}</div>
                ${c.fechaCierre ? `<div class="col-6"><strong>Fecha cierre:</strong> ${formatearFecha(c.fechaCierre)}</div>` : ""}
            </div>
            <p><strong>Descripción:</strong><br>${c.descripcion}</p>
            ${c.detalleResolucion ? `<div class="alert alert-success"><strong>Resolución:</strong><br>${c.detalleResolucion}</div>` : ""}

            ${c.documentos.length ? `<h6 class="mt-3">Documentos adjuntos</h6>
                <ul class="list-group mb-3">
                    ${c.documentos.map(d => `<li class="list-group-item d-flex justify-content-between align-items-center">
                        <span><i class="bi bi-paperclip me-1"></i>${d.nombreArchivo}</span>
                        <a class="btn btn-sm btn-outline-primary" href="${API_BASE_URL}${d.url}" target="_blank">Ver</a>
                    </li>`).join("")}
                </ul>` : ""}

            <h6 class="mt-3">Historial del caso</h6>
            ${c.historial.map(h => `
                <div class="timeline-item">
                    <div class="small text-muted">${formatearFecha(h.fechaHora)} — ${h.usuario} (${h.rolEjecuta})</div>
                    <div>${h.descripcionEvento}</div>
                </div>
            `).join("")}
        `;
    } catch (err) {
        document.getElementById("cuerpoDetalle").innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

document.getElementById("formRegistrarCaso").addEventListener("submit", async (e) => {
    e.preventDefault();
    const formData = new FormData();
    formData.append("idTipoCaso", document.getElementById("idTipoCaso").value);
    formData.append("numeroCuenta", document.getElementById("numeroCuenta").value);
    const idProducto = document.getElementById("idProducto").value;
    if (idProducto) formData.append("idProducto", idProducto);
    formData.append("nombreCliente", document.getElementById("nombreCliente").value);
    formData.append("identificacionCliente", document.getElementById("identificacionCliente").value);
    formData.append("correoContacto", document.getElementById("correoContacto").value);
    formData.append("telefonoContacto", document.getElementById("telefonoContacto").value);
    formData.append("descripcion", document.getElementById("descripcion").value);
    const archivo = document.getElementById("archivo").files[0];
    if (archivo) formData.append("archivo", archivo);

    mostrarCargando(true);
    try {
        const caso = await apiFetch("/casos/registrar", { method: "POST", body: formData });
        document.getElementById("formRegistrarCaso").reset();
        mostrarSeccion("seccionMisCasos");
        mostrarAlerta("alertas", `Su caso fue registrado con éxito. Número de caso: <strong>${caso.numeroCaso}</strong>.`, "success");
    } catch (err) {
        mostrarAlerta("alertasRegistrar", err.message);
    } finally {
        mostrarCargando(false);
    }
});

cargarCatalogos();
cargarMisCasos();
