const usuario = Sesion.exigirRol("AGENTE");
let idCasoActual = null;

renderNavbar("navbar", "Portal del Agente", [
    { texto: "Mi Bandeja", href: "#", activo: true }
], "Atienda los casos que le asignaron y mantenga su estado al día.");

async function cargarBandeja() {
    const tbody = document.getElementById("tablaBandeja");
    tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Cargando...</td></tr>`;
    const estado = document.getElementById("filtroEstado").value;
    try {
        let casos = await apiFetch("/casos/bandeja");
        if (estado) casos = casos.filter(c => c.estado === estado);

        if (casos.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No tiene casos en su bandeja.</td></tr>`;
            return;
        }
        tbody.innerHTML = casos.map(c => `
            <tr class="card-caso" onclick="verDetalle(${c.idCaso})">
                <td><strong>${c.numeroCaso}</strong></td>
                <td>${formatearFecha(c.fechaRegistro)}</td>
                <td>${c.tipoCaso}</td>
                <td>${c.nombreCliente}</td>
                <td>${badgeEstado(c.estado)}</td>
                <td><i class="bi bi-chevron-right"></i></td>
            </tr>
        `).join("");
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    }
}

async function verDetalle(idCaso) {
    idCasoActual = idCaso;
    const modal = new bootstrap.Modal(document.getElementById("modalDetalle"));
    document.getElementById("cuerpoDetalle").innerHTML = "Cargando...";
    document.getElementById("pieDetalle").innerHTML = "";
    modal.show();
    try {
        const c = await apiFetch(`/casos/${idCaso}`);
        document.getElementById("cuerpoDetalle").innerHTML = `
            <div class="row g-2 mb-3">
                <div class="col-6"><strong>No. Caso:</strong> ${c.numeroCaso}</div>
                <div class="col-6"><strong>Estado:</strong> ${badgeEstado(c.estado)}</div>
                <div class="col-6"><strong>Tipo:</strong> ${c.tipoCaso}</div>
                <div class="col-6"><strong>Cuenta:</strong> ${c.numeroCuenta}</div>
                <div class="col-6"><strong>Cliente:</strong> ${c.nombreCliente}</div>
                <div class="col-6"><strong>Identificación:</strong> ${c.identificacionCliente || "-"}</div>
                <div class="col-6"><strong>Correo:</strong> ${c.correoContacto || "-"}</div>
                <div class="col-6"><strong>Teléfono:</strong> ${c.telefonoContacto || "-"}</div>
                <div class="col-6"><strong>Fecha registro:</strong> ${formatearFecha(c.fechaRegistro)}</div>
                <div class="col-6"><strong>Solicitudes de reasignación:</strong> ${c.solicitudesReasignacionUsadas} / 2</div>
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

        // Botones de accion segun el estado actual del caso.
        const pie = document.getElementById("pieDetalle");
        let botones = "";
        if (c.estado === "Asignado" || c.estado === "En espera") {
            botones += `<button class="btn btn-primary" onclick="accionCaso('iniciar-atencion')"><i class="bi bi-play-circle me-1"></i>Iniciar Atención</button>`;
        }
        if (c.estado === "En atención") {
            botones += `<button class="btn btn-warning" onclick="accionCaso('poner-en-espera')"><i class="bi bi-pause-circle me-1"></i>Poner en Espera</button>`;
            botones += `<button class="btn btn-success" onclick="abrirModalResolver()"><i class="bi bi-check-circle me-1"></i>Resolver</button>`;
        }
        if (c.estado === "En espera") {
            botones += `<button class="btn btn-success" onclick="abrirModalResolver()"><i class="bi bi-check-circle me-1"></i>Resolver</button>`;
        }
        if (c.estado === "Resuelto") {
            botones += `<button class="btn btn-dark" onclick="accionCaso('cerrar')"><i class="bi bi-lock me-1"></i>Cerrar Caso</button>`;
        }
        if (c.estado !== "Resuelto" && c.estado !== "Cerrado" && c.solicitudesReasignacionUsadas < 2) {
            botones += `<button class="btn btn-outline-secondary" onclick="abrirModalReasignar()"><i class="bi bi-arrow-repeat me-1"></i>Solicitar Reasignación</button>`;
        }
        pie.innerHTML = botones;
    } catch (err) {
        document.getElementById("cuerpoDetalle").innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

async function accionCaso(accion) {
    mostrarCargando(true);
    try {
        const resp = await apiFetch(`/casos/${idCasoActual}/${accion}`, { method: "POST" });
        bootstrap.Modal.getInstance(document.getElementById("modalDetalle")).hide();
        mostrarAlerta("alertas", resp.mensaje, "success");
        cargarBandeja();
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    } finally {
        mostrarCargando(false);
    }
}

function abrirModalResolver() {
    document.getElementById("detalleResolucion").value = "";
    document.getElementById("alertasResolver").innerHTML = "";
    new bootstrap.Modal(document.getElementById("modalResolver")).show();
}

async function confirmarResolver() {
    const detalleResolucion = document.getElementById("detalleResolucion").value.trim();
    if (!detalleResolucion) {
        mostrarAlerta("alertasResolver", "Por favor ingrese el detalle de la resolución.");
        return;
    }
    mostrarCargando(true);
    try {
        const resp = await apiFetch(`/casos/${idCasoActual}/resolver`, { method: "POST", body: { detalleResolucion } });
        bootstrap.Modal.getInstance(document.getElementById("modalResolver")).hide();
        bootstrap.Modal.getInstance(document.getElementById("modalDetalle"))?.hide();
        mostrarAlerta("alertas", resp.mensaje, "success");
        cargarBandeja();
    } catch (err) {
        mostrarAlerta("alertasResolver", err.message);
    } finally {
        mostrarCargando(false);
    }
}

function abrirModalReasignar() {
    document.getElementById("motivoReasignacion").value = "";
    document.getElementById("alertasReasignar").innerHTML = "";
    new bootstrap.Modal(document.getElementById("modalReasignar")).show();
}

async function confirmarReasignacion() {
    const motivo = document.getElementById("motivoReasignacion").value.trim();
    if (!motivo) {
        mostrarAlerta("alertasReasignar", "Por favor ingrese el motivo de la reasignación.");
        return;
    }
    mostrarCargando(true);
    try {
        await apiFetch(`/reasignaciones/casos/${idCasoActual}/solicitar`, { method: "POST", body: { motivo } });
        bootstrap.Modal.getInstance(document.getElementById("modalReasignar")).hide();
        bootstrap.Modal.getInstance(document.getElementById("modalDetalle"))?.hide();
        mostrarAlerta("alertas", "Su solicitud de reasignación fue enviada al Supervisor.", "success");
        cargarBandeja();
    } catch (err) {
        mostrarAlerta("alertasReasignar", err.message);
    } finally {
        mostrarCargando(false);
    }
}

cargarBandeja();
