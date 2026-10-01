const usuario = Sesion.exigirRol("AUDITOR");
let tabActiva = "casos";

renderNavbar("navbar", "Panel del Auditor", [
    { texto: "Bitácoras del Sistema", href: "#", activo: true }
]);

function filtrosBase() {
    const params = new URLSearchParams();
    const desde = document.getElementById("fDesde").value;
    const hasta = document.getElementById("fHasta").value;
    if (desde) params.set("desde", desde + ":00");
    if (hasta) params.set("hasta", hasta + ":00");
    return params;
}

function buscarBitacoraActiva() {
    const activo = document.querySelector(".nav-link.active").textContent.trim();
    if (activo.includes("Casos")) cargarBitacoraCasos();
    else if (activo.includes("Accesos")) cargarBitacoraAccesos();
    else if (activo.includes("Usuarios")) cargarBitacoraUsuarios();
    else cargarBitacoraCorreos();
}

async function cargarBitacoraCasos() {
    tabActiva = "casos";
    const tbody = document.getElementById("tablaBitacoraCasos");
    tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">Cargando...</td></tr>`;
    try {
        const params = filtrosBase();
        const numeroCaso = document.getElementById("fNumeroCaso").value.trim();
        if (numeroCaso) params.set("numeroCaso", numeroCaso);
        const eventos = await apiFetch("/bitacoras/casos?" + params.toString());
        tbody.innerHTML = eventos.length ? eventos.map(e => `
            <tr>
                <td>${formatearFecha(e.fechaHora)}</td>
                <td>${e.numeroCaso}</td>
                <td>${e.usuario}</td>
                <td>${e.rolEjecuta}</td>
                <td>${e.ip}</td>
                <td>${e.estadoAnterior || "-"}</td>
                <td>${e.estadoNuevo}</td>
                <td>${e.descripcionEvento}</td>
            </tr>
        `).join("") : `<tr><td colspan="8" class="text-center text-muted">Sin resultados.</td></tr>`;
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    }
}

async function cargarBitacoraAccesos() {
    tabActiva = "accesos";
    const tbody = document.getElementById("tablaBitacoraAccesos");
    tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">Cargando...</td></tr>`;
    try {
        const eventos = await apiFetch("/bitacoras/accesos?" + filtrosBase().toString());
        tbody.innerHTML = eventos.length ? eventos.map(e => `
            <tr>
                <td>${formatearFecha(e.fechaHora)}</td>
                <td>${e.usuario}</td>
                <td>${e.ip}</td>
                <td>${e.tipoEvento}</td>
                <td>${e.descripcionEvento}</td>
            </tr>
        `).join("") : `<tr><td colspan="5" class="text-center text-muted">Sin resultados.</td></tr>`;
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    }
}

async function cargarBitacoraUsuarios() {
    tabActiva = "usuarios";
    const tbody = document.getElementById("tablaBitacoraUsuarios");
    tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">Cargando...</td></tr>`;
    try {
        const eventos = await apiFetch("/bitacoras/usuarios?" + filtrosBase().toString());
        tbody.innerHTML = eventos.length ? eventos.map(e => `
            <tr>
                <td>${formatearFecha(e.fechaHora)}</td>
                <td>${e.usuarioAfectado}</td>
                <td>${e.usuarioEjecuta}</td>
                <td>${e.motivo}</td>
                <td>${e.descripcionEvento}</td>
            </tr>
        `).join("") : `<tr><td colspan="5" class="text-center text-muted">Sin resultados.</td></tr>`;
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    }
}

async function cargarBitacoraCorreos() {
    tabActiva = "correos";
    const tbody = document.getElementById("tablaBitacoraCorreos");
    tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">Cargando...</td></tr>`;
    try {
        const params = filtrosBase();
        const numeroCaso = document.getElementById("fNumeroCaso").value.trim();
        if (numeroCaso) params.set("numeroCaso", numeroCaso);
        const eventos = await apiFetch("/bitacoras/correos?" + params.toString());
        tbody.innerHTML = eventos.length ? eventos.map(e => `
            <tr>
                <td>${formatearFecha(e.fechaHora)}</td>
                <td>${e.numeroCaso || "-"}</td>
                <td>${e.destinatario}</td>
                <td>${e.tipoNotificacion}</td>
                <td>${e.descripcionEvento}</td>
            </tr>
        `).join("") : `<tr><td colspan="5" class="text-center text-muted">Sin resultados.</td></tr>`;
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    }
}

async function descargarReporteAuditoria(formato) {
    mostrarCargando(true);
    try {
        const params = filtrosBase();
        params.set("formato", formato);
        const numeroCaso = document.getElementById("fNumeroCaso").value.trim();
        if (numeroCaso) params.set("numeroCaso", numeroCaso);
        await descargarArchivo("/reportes/auditoria?" + params.toString(), `reporte_auditoria.${formato === "pdf" ? "pdf" : "xlsx"}`);
    } catch (err) {
        mostrarAlerta("alertas", err.message);
    } finally {
        mostrarCargando(false);
    }
}

cargarBitacoraCasos();
