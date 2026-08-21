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

    public RegistradorCasos(Collection<ProductoServicio> catalogo, List<AgenteAtencion> agentes) {
        catalogo.forEach(producto -> this.catalogo.put(producto.id(), producto));
        this.agentes = new ArrayList<>(agentes);
    }

    public Caso registrar(Cliente cliente, SesionUsuario sesion, SolicitudRegistroCaso solicitud) {
        validarClienteAutenticado(cliente, sesion);
        validarSolicitud(solicitud);
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

    public List<ProductoServicio> consultarCatalogo() { return catalogo.values().stream().sorted(Comparator.comparing(ProductoServicio::nombre)).toList(); }
    public List<Caso> consultarCasos() { return List.copyOf(casos); }
    public List<EventoBitacora> consultarBitacora() { return List.copyOf(bitacora); }
    public List<Notificacion> consultarNotificaciones() { return List.copyOf(notificaciones); }

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
        if (!catalogo.containsKey(solicitud.productoId())) throw new RegistroCasoException("El sistema solo atiende productos o servicios bancarios del catálogo institucional.");
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
