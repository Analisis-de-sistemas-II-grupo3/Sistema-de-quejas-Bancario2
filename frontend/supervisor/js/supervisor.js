const usuario = Sesion.exigirRol("SUPERVISOR");
let idSolicitudActual = null;

renderNavbar("navbar", "Panel del Supervisor", [
    { texto: "Solicitudes y Reportes", href: "#", activo: true }
]);

async function cargarSolicitudes() {
    const tbody = document.getElementById("tablaSolicitudes");
    tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">Cargando...</td></tr>`;
    try {
        const solicitudes = await apiFetch("/reasignaciones/pendientes");
        if (solicitudes.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">No hay solicitudes pendientes.</td></tr>`;
            return;
        }
        tbody.innerHTML = solicitudes.map(s => `
            <tr>
                <td><strong>${s.numeroCaso}</strong></td>
                <td>${s.agenteSolicita}</td>
                <td>${s.motivo}</td>
                <td>${formatearFecha(s.fechaSolicitud)}</td>
                <td class="text-end">
                    <button class="btn btn-sm btn-success" onclick="aprobar(${s.idSolicitud})"><i class="bi bi-check-lg"></i> Aprobar</button>
                    <button class="btn btn-sm btn-outline-danger" onclick="abrirRechazo(${s.idSolicitud})"><i class="bi bi-x-lg"></i> Rechazar</button>
                </td>
            </tr>
        `).join("");
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    }
}

async function aprobar(idSolicitud) {
    mostrarCargando(true);
    try {
        await apiFetch(`/reasignaciones/${idSolicitud}/aprobar`, { method: "POST" });
        mostrarAlerta("alertas", "La solicitud fue aprobada y el caso fue reasignado automáticamente.", "success");
        cargarSolicitudes();
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    } finally {
        mostrarCargando(false);
    }
}

function abrirRechazo(idSolicitud) {
    idSolicitudActual = idSolicitud;
    document.getElementById("motivoRechazo").value = "";
    new bootstrap.Modal(document.getElementById("modalRechazar")).show();
}

async function confirmarRechazo() {
    const motivoRechazo = document.getElementById("motivoRechazo").value.trim();
    mostrarCargando(true);
    try {
        await apiFetch(`/reasignaciones/${idSolicitudActual}/rechazar`, { method: "POST", body: { motivoRechazo } });
        bootstrap.Modal.getInstance(document.getElementById("modalRechazar")).hide();
        mostrarAlerta("alertas", "La solicitud fue rechazada.", "success");
        cargarSolicitudes();
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    } finally {
        mostrarCargando(false);
    }
}

async function cargarTiposCaso() {
    try {
        const tipos = await apiFetch("/catalogos/tipos-caso");
        document.getElementById("fTipoCaso").innerHTML += tipos.map(t => `<option value="${t.id}">${t.nombre}</option>`).join("");
    } catch (err) { /* silencioso */ }
}

function construirQuery() {
    const params = new URLSearchParams();
    const numeroCaso = document.getElementById("fNumeroCaso").value.trim();
    const estado = document.getElementById("fEstado").value;
    const idTipoCaso = document.getElementById("fTipoCaso").value;
    if (numeroCaso) params.set("numeroCaso", numeroCaso);
    if (estado) params.set("estado", estado);
    if (idTipoCaso) params.set("idTipoCaso", idTipoCaso);
    return params;
}

async function buscarCasos() {
    const tbody = document.getElementById("tablaCasos");
    tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Buscando...</td></tr>`;
    try {
        const casos = await apiFetch("/casos/buscar?" + construirQuery().toString());
        if (casos.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No se encontraron casos con los filtros indicados.</td></tr>`;
            return;
        }
        tbody.innerHTML = casos.map(c => `
            <tr>
                <td><strong>${c.numeroCaso}</strong></td>
                <td>${formatearFecha(c.fechaRegistro)}</td>
                <td>${c.tipoCaso}</td>
                <td>${c.nombreCliente}</td>
                <td>${c.agenteAsignado || "Sin asignar"}</td>
                <td>${badgeEstado(c.estado)}</td>
            </tr>
        `).join("");
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    }
}

async function descargarReporte(formato) {
    mostrarCargando(true);
    try {
        const params = construirQuery();
        params.set("formato", formato);
        await descargarArchivo("/reportes/casos?" + params.toString(), `reporte_casos.${formato === "pdf" ? "pdf" : "xlsx"}`);
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    } finally {
        mostrarCargando(false);
    }
}

cargarSolicitudes();
cargarTiposCaso();
