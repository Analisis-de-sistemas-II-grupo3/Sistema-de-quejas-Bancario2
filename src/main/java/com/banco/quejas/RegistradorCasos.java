package com.banco.quejas;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Lógica local del CU02 V1.4. CU01 proporcionará la sesión real. */
public class RegistradorCasos {
    private static final long MAX_ADJUNTO_BYTES = 2L * 1024 * 1024;
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("pdf", "jpg", "jpeg", "png");
    private final Map<String, ProductoServicio> catalogo = new HashMap<>();
    private final List<AgenteAtencion> agentes;
    private final List<Caso> casos = new ArrayList<>();
    private final List<EventoBitacora> bitacora = new ArrayList<>();
    private final List<Notificacion> notificaciones = new ArrayList<>();
    private final Map<String, AtomicInteger> correlativos = new HashMap<>();
    private final Random aleatorio = new Random();
    private final RepositorioCasosPostgres repositorioPostgres;

    public RegistradorCasos(Collection<ProductoServicio> catalogo, List<AgenteAtencion> agentes) {
        catalogo.forEach(producto -> this.catalogo.put(producto.id(), producto));
        this.agentes = new ArrayList<>(agentes);
        this.repositorioPostgres = null;
    }
    /** Constructor de producción: catálogo y casos se leen/escriben en PostgreSQL. */
    public RegistradorCasos(RepositorioCasosPostgres repositorioPostgres) {
        this.agentes = new ArrayList<>();
        this.repositorioPostgres = repositorioPostgres;
    }

    public Caso registrar(Cliente cliente, SesionUsuario sesion, SolicitudRegistroCaso solicitud) {
        validarClienteAutenticado(cliente, sesion);
        validarSolicitud(solicitud);
        if (repositorioPostgres != null) return repositorioPostgres.guardarCaso(cliente, sesion, solicitud);
        LocalDateTime ahora = LocalDateTime.now();
        String folio = siguienteFolio(solicitud.tipo(), ahora.getYear());
        AgenteAtencion agente = reservarAgenteDisponible();
        EstadoCaso estado = agente == null ? EstadoCaso.RECIBIDO : EstadoCaso.ASIGNADO;
        Caso caso = new Caso(folio, cliente.id(), cliente.nombreCompleto(), solicitud.tipo(), catalogo.get(solicitud.productoId()),
                solicitud.sucursalCanal().trim(), solicitud.fechaHecho(), solicitud.descripcion().trim(), solicitud.adjunto(), estado, agente, ahora);
        casos.add(caso);
        bitacora.add(new EventoBitacora(ahora, folio, "CREACIÓN DE CASO", null, EstadoCaso.RECIBIDO.name(), sesion.rol(),
                sesion.usuario(), sesion.ip(), "El cliente registró el caso con folio " + folio + "."));
        if (agente != null) registrarAsignacion(caso, agente, ahora);
        notificaciones.add(new Notificacion(ahora, cliente.correo(), "Caso registrado: " + folio,
                "Su caso fue registrado con éxito. Folio: " + folio + "."));
        return caso;
    }

    public List<ProductoServicio> consultarCatalogo() { return repositorioPostgres != null ? repositorioPostgres.catalogo() : catalogo.values().stream().sorted(Comparator.comparing(ProductoServicio::nombre)).toList(); }
    public List<Caso> consultarCasos() { return List.copyOf(casos); }
    /** CU03 usa este método para comprobar que el folio pertenece a un caso existente. */
    public Optional<Caso> buscarPorFolio(String folio) {
        if (folio == null || folio.isBlank()) return Optional.empty();
        return repositorioPostgres != null ? repositorioPostgres.buscar(folio.trim()) : casos.stream().filter(caso -> caso.folio().equalsIgnoreCase(folio.trim())).findFirst();
    }
    public List<EventoBitacora> consultarBitacora() { return List.copyOf(bitacora); }
    public List<Notificacion> consultarNotificaciones() { return List.copyOf(notificaciones); }

    /** CU03 registra aquí la carga de evidencia mientras CU de Auditoría se integra. */
    public void registrarCargaEvidencia(String folio, SesionUsuario sesion, String descripcion) {
        bitacora.add(new EventoBitacora(LocalDateTime.now(), folio, "CARGA DE EVIDENCIA", null, null,
                sesion.rol(), sesion.usuario(), sesion.ip(), descripcion));
    }
    /** Guarda los datos de CU03 en documento_adjunto cuando se usa PostgreSQL. */
    public void guardarEvidencia(String folio, EvidenciaCaso evidencia, SesionUsuario sesion) {
        if (repositorioPostgres != null) repositorioPostgres.guardarDocumento(folio, evidencia, sesion);
    }
    public void guardarEvidencias(String folio, List<EvidenciaCaso> evidencias, SesionUsuario sesion) {
        if (repositorioPostgres != null) repositorioPostgres.guardarDocumentos(folio, evidencias, sesion);
    }
    public List<EvidenciaCaso> consultarEvidencias(String folio) {
        return repositorioPostgres != null ? repositorioPostgres.documentos(folio) : List.of();
    }
    public ArchivoEvidencia descargarEvidencia(String folio, long documentoId) {
        if (repositorioPostgres == null) throw new ErrorEvidenciaException("La descarga requiere la conexión a PostgreSQL.");
        return repositorioPostgres.obtenerArchivo(folio, documentoId);
    }

    private void validarClienteAutenticado(Cliente cliente, SesionUsuario sesion) {
        if (cliente == null || sesion == null || !"Cliente".equalsIgnoreCase(sesion.rol()))
            throw new RegistroCasoException("Debe iniciar sesión como Cliente para registrar un caso.");
        if (cliente.cuentas() == null || cliente.cuentas().stream().noneMatch(CuentaBancaria::activa))
            throw new RegistroCasoException("El registro de casos está reservado a clientes con al menos una cuenta activa.");
    }
    private void validarSolicitud(SolicitudRegistroCaso solicitud) {
        if (solicitud == null) throw new RegistroCasoException("La solicitud es obligatoria.");
        obligatorio(solicitud.tipo(), "Debe seleccionar un tipo de caso.");
        obligatorio(solicitud.productoId(), "Debe seleccionar un producto o servicio bancario.");
        if (repositorioPostgres != null ? !repositorioPostgres.existeProducto(solicitud.productoId()) : !catalogo.containsKey(solicitud.productoId())) throw new RegistroCasoException("El sistema solo atiende productos o servicios bancarios del catálogo institucional.");
        obligatorio(solicitud.sucursalCanal(), "Debe indicar la sucursal o canal donde ocurrió el hecho.");
        if (solicitud.fechaHecho() == null) throw new RegistroCasoException("Debe indicar la fecha del hecho.");
        obligatorio(solicitud.descripcion(), "Debe ingresar la descripción del caso.");
        if (solicitud.descripcion().trim().length() > 2000) throw new RegistroCasoException("La descripción no puede exceder 2,000 caracteres.");
        if (solicitud.adjunto() != null) validarAdjunto(solicitud.adjunto());
    }
    private void registrarAsignacion(Caso caso, AgenteAtencion agente, LocalDateTime fecha) {
        bitacora.add(new EventoBitacora(fecha, caso.folio(), "ASIGNACIÓN AUTOMÁTICA", EstadoCaso.RECIBIDO.name(), EstadoCaso.ASIGNADO.name(),
                "Proceso automático", "SISTEMA", "127.0.0.1", "Caso asignado aleatoriamente a " + agente.nombre() + "."));
        notificaciones.add(new Notificacion(fecha, agente.id(), "Nuevo caso asignado: " + caso.folio(),
                "Se le asignó automáticamente un caso de tipo " + caso.tipo() + "."));
    }
    private AgenteAtencion reservarAgenteDisponible() {
        List<AgenteAtencion> disponibles = agentes.stream().filter(AgenteAtencion::disponible).toList();
        if (disponibles.isEmpty()) return null;
        AgenteAtencion agente = disponibles.get(aleatorio.nextInt(disponibles.size()));
        int indice = agentes.indexOf(agente);
        agentes.set(indice, new AgenteAtencion(agente.id(), agente.nombre(), agente.sesionActiva(), agente.casosActivos() + 1, agente.limiteCasosActivos()));
        return agente;
    }
    private String siguienteFolio(TipoCaso tipo, int anio) { int correlativo = correlativos.computeIfAbsent(tipo.name() + '-' + anio, k -> new AtomicInteger()).incrementAndGet(); return "%s-%05d-%d".formatted(tipo.prefijo(), correlativo, anio); }
    private void validarAdjunto(DocumentoAdjunto adjunto) {
        if (adjunto.tamanioBytes() > MAX_ADJUNTO_BYTES) throw new RegistroCasoException("El documento adjunto no puede exceder 2 MB.");
        if (!EXTENSIONES_PERMITIDAS.contains(adjunto.extension().toLowerCase(Locale.ROOT))) throw new RegistroCasoException("El documento debe ser PDF, JPG, JPEG o PNG.");
    }
    private void obligatorio(Object valor, String mensaje) { if (valor == null || valor instanceof String texto && texto.isBlank()) throw new RegistroCasoException(mensaje); }
}
