/**
 * CU-00 FA01 / CU-03: consulta pública del estado de un caso (sin sesión).
 * Usa GET /api/publico/casos/{numeroCaso}/estado.
 */
const ETAPAS_CICLO = ["Registrado", "Asignado", "En atención", "Resuelto", "Cerrado"]; // RN08 ("En espera" es una pausa de "En atención")

function crear(etiqueta, clase, texto) {
    const el = document.createElement(etiqueta);
    if (clase) el.className = clase;
    if (texto !== undefined) el.textContent = texto;
    return el;
}

function dibujarProgreso(estado) {
    const contenedor = document.getElementById("stepper");
    contenedor.replaceChildren();
    const enEspera = estado === "En espera";
    const referencia = enEspera ? "En atención" : estado;
    const actual = ETAPAS_CICLO.indexOf(referencia);

    ETAPAS_CICLO.forEach((nombre, i) => {
        const cerrado = estado === "Cerrado";
        const clase = i < actual || (cerrado && i === actual) ? "done" : (i === actual ? "active" : "");
        const paso = crear("div", "step " + clase);
        const punto = crear("div", "dot");
        punto.append(clase === "done" ? crear("i", "bi bi-check-lg") : document.createTextNode(String(i + 1)));
        paso.append(punto, document.createTextNode(nombre));
        contenedor.appendChild(paso);
    });
    document.getElementById("notaEspera").hidden = !enEspera;
}

function dibujarHistorial(etapas) {
    const lista = document.getElementById("etapas");
    lista.replaceChildren();
    (etapas || []).forEach(e => {
        const li = document.createElement("li");
        const titulo = e.estadoAnterior
            ? e.estadoAnterior + " → " + e.estadoNuevo
            : "Caso registrado (" + e.estadoNuevo + ")";
        li.append(crear("div", "fw-semibold", titulo), crear("div", "cuando", formatearFecha(e.fechaHora)));
        lista.appendChild(li);
    });
}

function mostrarResultado(caso) {
    document.getElementById("resNumero").textContent = caso.numeroCaso;
    document.getElementById("resTipo").textContent = caso.tipoCaso;
    document.getElementById("resRegistro").textContent = formatearFecha(caso.fechaRegistro);
    document.getElementById("resActualizacion").textContent = formatearFecha(caso.fechaUltimaActualizacion);
    const pill = document.getElementById("resEstado");
    pill.textContent = caso.estado;
    pill.dataset.estado = caso.estado;
    dibujarProgreso(caso.estado);
    dibujarHistorial(caso.etapas);
    document.getElementById("resultado").hidden = false;
}

async function consultar(numero) {
    document.getElementById("alertas").replaceChildren();
    document.getElementById("resultado").hidden = true;

    if (!numero) {
        mostrarAlerta("alertas", "Por favor ingrese los campos obligatorios.");   // AN02 #1
        return;
    }
    mostrarCargando(true);
    try {
        const caso = await apiFetch("/publico/casos/" + encodeURIComponent(numero) + "/estado");
        mostrarResultado(caso);
    } catch (err) {
        mostrarAlerta("alertas", err.message);   // AN02 #5 si el número no existe
    } finally {
        mostrarCargando(false);
    }
}

(async function iniciar() {
    await Identidad.cargar();
    ajustarAccesoSegunSesion();

    const campo = document.getElementById("numeroCaso");
    document.getElementById("formConsulta").addEventListener("submit", e => {
        e.preventDefault();
        consultar(campo.value.trim().toUpperCase());
    });

    // Permite enlaces directos: consultar-caso.html?numero=Q-00001-2026
    const inicial = new URLSearchParams(window.location.search).get("numero");
    if (inicial) {
        campo.value = inicial.trim().toUpperCase();
        consultar(campo.value);
    }
})();
