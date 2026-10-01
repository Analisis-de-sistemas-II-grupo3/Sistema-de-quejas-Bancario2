package com.umg.quejasbancario.service;

import com.umg.quejasbancario.dto.request.RegistrarCasoRequest;
import com.umg.quejasbancario.dto.response.*;
import com.umg.quejasbancario.entity.*;
import com.umg.quejasbancario.entity.enums.EstadoCaso;
import com.umg.quejasbancario.entity.enums.EstadoCuenta;
import com.umg.quejasbancario.entity.enums.RolNombre;
import com.umg.quejasbancario.exception.AccesoDenegadoException;
import com.umg.quejasbancario.exception.BusinessRuleException;
import com.umg.quejasbancario.exception.ResourceNotFoundException;
import com.umg.quejasbancario.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/** CU-02 Registrar Caso, CU-03 Consultar Estado de Caso, CU-10 Buscar y Filtrar Casos. */
@Service
@RequiredArgsConstructor
public class CasoService {

    private final CasoRepository casoRepository;
    private final TipoCasoRepository tipoCasoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoServicioRepository productoServicioRepository;
    private final CuentaBancariaRepository cuentaBancariaRepository;
    private final DocumentoAdjuntoRepository documentoAdjuntoRepository;
    private final BitacoraCasoRepository bitacoraCasoRepository;
    private final NumeroCasoService numeroCasoService;
    private final AsignacionService asignacionService;
    private final DocumentoService documentoService;
    private final BitacoraRegistroService bitacoraRegistroService;
    private final NotificacionService notificacionService;

    /** CU-02 - Flujo Normal Basico completo. */
    @Transactional
    public CasoDetalleResponse registrarCaso(RegistrarCasoRequest request, MultipartFile archivo, Usuario cliente, String ip) {
        TipoCaso tipoCaso = tipoCasoRepository.findById(request.getIdTipoCaso())
                .orElseThrow(() -> new BusinessRuleException("Por favor ingrese los campos obligatorios."));

        // RN16: la cuenta debe ser una cuenta activa DEL CLIENTE autenticado.
        CuentaBancaria cuenta = cuentaBancariaRepository
                .findByNumeroCuentaAndEstado(request.getNumeroCuenta(), EstadoCuenta.ACTIVA)
                .filter(c -> c.getUsuario().getIdUsuario().equals(cliente.getIdUsuario()))
                .orElseThrow(() -> new BusinessRuleException(
                        "Por favor verifique, el número de cuenta ingresado no corresponde a una cuenta activa. Solo clientes con cuenta activa pueden registrar casos."));

        ProductoServicio producto = null;
        if (request.getIdProducto() != null) {
            producto = productoServicioRepository.findById(request.getIdProducto()).orElse(null);
        }

        Caso caso = Caso.builder()
                .tipoCaso(tipoCaso)
                .producto(producto)
                .cliente(cliente)
                .cuenta(cuenta)
                .descripcion(request.getDescripcion())
                .nombreClienteCaso(request.getNombreCliente())
                .identificacionCliente(request.getIdentificacionCliente())
                .correoContacto(request.getCorreoContacto())
                .telefonoContacto(request.getTelefonoContacto())
                .estado(EstadoCaso.REGISTRADO)
                .build();

        // RN05: numero correlativo por tipo/anio.
        caso.setNumeroCaso(numeroCasoService.generarNumeroCaso(tipoCaso));
        caso = casoRepository.save(caso);

        // Documento adjunto opcional (RN10, hasta 2MB para Cliente).
        if (archivo != null && !archivo.isEmpty()) {
            documentoService.guardarDocumento(archivo, caso, cliente);
        }

        bitacoraRegistroService.registrarEventoCaso(
                caso, cliente, RolNombre.CLIENTE.getValor(), ip,
                null, EstadoCaso.REGISTRADO.getValor(),
                "El Cliente registró el caso '" + caso.getNumeroCaso() + "'.");

        // RN06/CU-04: asignacion automatica y aleatoria. Si no hay agentes, el
        // caso permanece en "Registrado" (FA04) y el scheduler reintentara (RF17).
        asignacionService.asignarCaso(caso, null, ip);

        notificacionService.notificarCasoRegistrado(caso, caso.getCorreoContacto());

        return mapearDetalle(caso);
    }

    /** CU-03 - "Mis Casos": listado del Cliente autenticado. */
    @Transactional(readOnly = true)
    public List<CasoResumenResponse> misCasos(Usuario cliente) {
        return casoRepository.findByCliente_IdUsuario(cliente.getIdUsuario()).stream()
                .map(this::mapearResumen)
                .toList();
    }

    /** CU-03 - Detalle de un caso propio del Cliente. */
    @Transactional(readOnly = true)
    public CasoDetalleResponse detalleCasoCliente(Integer idCaso, Usuario cliente) {
        Caso caso = casoRepository.findById(idCaso)
                .orElseThrow(() -> new ResourceNotFoundException("Por favor verifique, el número de caso no existe en el sistema."));

        if (!caso.getCliente().getIdUsuario().equals(cliente.getIdUsuario())) {
            throw new AccesoDenegadoException("No tiene autorización para consultar este caso.");
        }
        return mapearDetalle(caso);
    }

    /** Detalle de caso para roles internos (Agente, Administrador, Supervisor, Auditor). */
    @Transactional(readOnly = true)
    public CasoDetalleResponse detalleCasoInterno(Integer idCaso) {
        Caso caso = casoRepository.findById(idCaso)
                .orElseThrow(() -> new ResourceNotFoundException("Por favor verifique, el número de caso no existe en el sistema."));
        return mapearDetalle(caso);
    }

    /** CU-10 Buscar y Filtrar Casos, respetando el alcance de visibilidad del rol (RN14). */
    @Transactional(readOnly = true)
    public List<CasoResumenResponse> buscarCasos(String numeroCaso, Integer idTipoCaso, String estado,
                                                  String nombreCliente, Integer idAgente,
                                                  LocalDateTime desde, LocalDateTime hasta,
                                                  Usuario usuarioAutenticado) {
        EstadoCaso estadoEnum = null;
        if (estado != null && !estado.isBlank()) {
            estadoEnum = EstadoCaso.fromValor(estado);
        }

        Specification<Caso> spec = Specification.where(CasoSpecifications.numeroCaso(numeroCaso))
                .and(CasoSpecifications.tipoCaso(idTipoCaso))
                .and(CasoSpecifications.estado(estadoEnum))
                .and(CasoSpecifications.cliente(nombreCliente))
                .and(CasoSpecifications.agente(idAgente))
                .and(CasoSpecifications.fechaDesde(desde))
                .and(CasoSpecifications.fechaHasta(hasta));

        // Alcance de visibilidad por rol: un Agente solo ve sus propios casos asignados.
        String rol = usuarioAutenticado.getRol().getNombreRol();
        if (RolNombre.AGENTE.getValor().equalsIgnoreCase(rol)) {
            spec = spec.and(CasoSpecifications.agente(usuarioAutenticado.getIdUsuario()));
        }

        return casoRepository.findAll(spec).stream().map(this::mapearResumen).toList();
    }

    // ---------------------------------------------------------------
    // Mapeo
    // ---------------------------------------------------------------

    CasoResumenResponse mapearResumen(Caso c) {
        return CasoResumenResponse.builder()
                .idCaso(c.getIdCaso())
                .numeroCaso(c.getNumeroCaso())
                .fechaRegistro(c.getFechaRegistro())
                .tipoCaso(c.getTipoCaso().getNombre())
                .nombreCliente(c.getNombreClienteCaso())
                .agenteAsignado(c.getAgenteAsignado() != null ? c.getAgenteAsignado().getNombreCompleto() : null)
                .estado(c.getEstado().getValor())
                .build();
    }

    CasoDetalleResponse mapearDetalle(Caso c) {
        List<DocumentoAdjuntoResponse> documentos = documentoAdjuntoRepository.findByCaso_IdCaso(c.getIdCaso()).stream()
                .map(d -> DocumentoAdjuntoResponse.builder()
                        .idDocumento(d.getIdDocumento())
                        .nombreArchivo(d.getNombreArchivo())
                        .url("/api/documentos/" + d.getIdDocumento() + "/descargar")
                        .tamano(d.getTamano())
                        .usuarioCarga(d.getUsuarioCarga().getNombreCompleto())
                        .fechaCarga(d.getFechaCarga())
                        .build())
                .toList();

        List<HistorialCasoResponse> historial = bitacoraCasoRepository.findByCaso_IdCasoOrderByFechaHoraAsc(c.getIdCaso()).stream()
                .map(h -> HistorialCasoResponse.builder()
                        .rolEjecuta(h.getRolEjecuta())
                        .usuario(h.getUsuario() != null ? h.getUsuario().getNombreCompleto() : "Sistema")
                        .ip(h.getIp())
                        .estadoAnterior(h.getEstadoAnterior())
                        .estadoNuevo(h.getEstadoNuevo())
                        .descripcionEvento(h.getDescripcionEvento())
                        .fechaHora(h.getFechaHora())
                        .build())
                .toList();

        return CasoDetalleResponse.builder()
                .idCaso(c.getIdCaso())
                .numeroCaso(c.getNumeroCaso())
                .tipoCaso(c.getTipoCaso().getNombre())
                .categoria(c.getCategoria() != null ? c.getCategoria().getNombre() : null)
                .producto(c.getProducto() != null ? c.getProducto().getNombre() : null)
                .descripcion(c.getDescripcion())
                .estado(c.getEstado().getValor())
                .numeroCuenta(c.getCuenta().getNumeroCuenta())
                .nombreCliente(c.getNombreClienteCaso())
                .identificacionCliente(c.getIdentificacionCliente())
                .correoContacto(c.getCorreoContacto())
                .telefonoContacto(c.getTelefonoContacto())
                .agenteAsignado(c.getAgenteAsignado() != null ? c.getAgenteAsignado().getNombreCompleto() : null)
                .detalleResolucion(c.getDetalleResolucion())
                .fechaRegistro(c.getFechaRegistro())
                .fechaCierre(c.getFechaCierre())
                .solicitudesReasignacionUsadas(c.getSolicitudesReasignacionUsadas())
                .documentos(documentos)
                .historial(historial)
                .build();
    }
}
