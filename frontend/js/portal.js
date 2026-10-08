/** CU-00 Portal / Página de Inicio. */
(async function iniciarPortal() {
    const datos = await Identidad.cargar();   // RN17: identidad desde la configuración
    ajustarAccesoSegunSesion();

    const seccion = document.getElementById("nosotros");
    const enlace = document.getElementById("navNosotros");
    const hayIdentidad = datos && (datos.mision || datos.vision || (datos.valores && datos.valores.length));
    if (!hayIdentidad) {
        enlace.hidden = true;     // sin configuración disponible no se muestra la sección
        return;
    }

    document.getElementById("textoMision").textContent = datos.mision || "";
    document.getElementById("textoVision").textContent = datos.vision || "";

    const lista = document.getElementById("listaValores");
    (datos.valores || []).forEach(v => {
        const col = document.createElement("div");
        col.className = "col-sm-6 col-lg-3";
        const tarjeta = document.createElement("div");
        tarjeta.className = "valor-card";
        const titulo = document.createElement("h4");
        titulo.textContent = v.nombre;
        const texto = document.createElement("p");
        texto.textContent = v.descripcion;
        tarjeta.append(titulo, texto);
        col.appendChild(tarjeta);
        lista.appendChild(col);
    });
    seccion.hidden = false;
})();
