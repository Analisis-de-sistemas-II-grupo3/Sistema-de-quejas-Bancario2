package com.umg.quejasbancario.service;

import com.umg.quejasbancario.dto.request.CatalogoSimpleRequest;
import com.umg.quejasbancario.dto.request.ParametroRequest;
import com.umg.quejasbancario.dto.request.TipoCasoRequest;
import com.umg.quejasbancario.dto.response.CatalogoItemResponse;
import com.umg.quejasbancario.entity.*;
import com.umg.quejasbancario.exception.BusinessRuleException;
import com.umg.quejasbancario.exception.ResourceNotFoundException;
import com.umg.quejasbancario.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CU-15 Gestionar Catalogos del Sistema (Tipo de Caso, Categoria,
 * Producto/Servicio) y CU-16 Configurar Parametros del Sistema.
 */
@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final TipoCasoRepository tipoCasoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoServicioRepository productoServicioRepository;
    private final ParametroSistemaRepository parametroSistemaRepository;
    private final RolRepository rolRepository;
    private final BitacoraRegistroService bitacoraRegistroService;

    // ---------------- Tipo de Caso ----------------

    @Transactional(readOnly = true)
    public List<CatalogoItemResponse> listarTiposCaso() {
        return tipoCasoRepository.findAll().stream()
                .map(t -> CatalogoItemResponse.builder().id(t.getIdTipoCaso()).nombre(t.getNombre()).extra(t.getPrefijo()).build())
                .toList();
    }

    @Transactional
    public CatalogoItemResponse crearTipoCaso(TipoCasoRequest request, Usuario administrador, String ip) {
        if (tipoCasoRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessRuleException("Ya existe un tipo de caso con ese nombre.");
        }
        TipoCaso tipoCaso = TipoCaso.builder().nombre(request.getNombre()).prefijo(request.getPrefijo().toUpperCase()).build();
        tipoCaso = tipoCasoRepository.save(tipoCaso);
        registrarCambioCatalogo(administrador, ip, "Tipo de Caso", "creó", tipoCaso.getNombre());
        return CatalogoItemResponse.builder().id(tipoCaso.getIdTipoCaso()).nombre(tipoCaso.getNombre()).extra(tipoCaso.getPrefijo()).build();
    }

    @Transactional
    public CatalogoItemResponse actualizarTipoCaso(Integer id, TipoCasoRequest request, Usuario administrador, String ip) {
        TipoCaso tipoCaso = tipoCasoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El tipo de caso indicado no existe."));
        tipoCaso.setNombre(request.getNombre());
        tipoCaso.setPrefijo(request.getPrefijo().toUpperCase());
        tipoCaso = tipoCasoRepository.save(tipoCaso);
        registrarCambioCatalogo(administrador, ip, "Tipo de Caso", "actualizó", tipoCaso.getNombre());
        return CatalogoItemResponse.builder().id(tipoCaso.getIdTipoCaso()).nombre(tipoCaso.getNombre()).extra(tipoCaso.getPrefijo()).build();
    }

    // ---------------- Categoria ----------------

    @Transactional(readOnly = true)
    public List<CatalogoItemResponse> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(c -> CatalogoItemResponse.builder().id(c.getIdCategoria()).nombre(c.getNombre()).build())
                .toList();
    }

    @Transactional
    public CatalogoItemResponse crearCategoria(CatalogoSimpleRequest request, Usuario administrador, String ip) {
        if (categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessRuleException("Ya existe una categoría con ese nombre.");
        }
        Categoria categoria = categoriaRepository.save(Categoria.builder().nombre(request.getNombre()).build());
        registrarCambioCatalogo(administrador, ip, "Categoría", "creó", categoria.getNombre());
        return CatalogoItemResponse.builder().id(categoria.getIdCategoria()).nombre(categoria.getNombre()).build();
    }

    // ---------------- Producto / Servicio ----------------

    @Transactional(readOnly = true)
    public List<CatalogoItemResponse> listarProductos() {
        return productoServicioRepository.findAll().stream()
                .map(p -> CatalogoItemResponse.builder().id(p.getIdProducto()).nombre(p.getNombre()).build())
                .toList();
    }

    @Transactional
    public CatalogoItemResponse crearProducto(CatalogoSimpleRequest request, Usuario administrador, String ip) {
        if (productoServicioRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessRuleException("Ya existe un producto/servicio con ese nombre.");
        }
        ProductoServicio producto = productoServicioRepository.save(ProductoServicio.builder().nombre(request.getNombre()).build());
        registrarCambioCatalogo(administrador, ip, "Producto/Servicio", "creó", producto.getNombre());
        return CatalogoItemResponse.builder().id(producto.getIdProducto()).nombre(producto.getNombre()).build();
    }

    // ---------------- Roles (solo lectura, catalogo fijo RN01) ----------------

    @Transactional(readOnly = true)
    public List<CatalogoItemResponse> listarRoles() {
        return rolRepository.findAll().stream()
                .map(r -> CatalogoItemResponse.builder().id(r.getIdRol()).nombre(r.getNombreRol()).build())
                .toList();
    }

    // ---------------- Parametros del Sistema (CU-16) ----------------

    @Transactional(readOnly = true)
    public List<ParametroSistema> listarParametros() {
        return parametroSistemaRepository.findAll();
    }

    @Transactional
    public ParametroSistema actualizarParametro(Integer id, ParametroRequest request, Usuario administrador, String ip) {
        ParametroSistema parametro = parametroSistemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El parámetro indicado no existe."));
        String valorAnterior = parametro.getValor();
        parametro.setValor(request.getValor());
        parametro = parametroSistemaRepository.save(parametro);

        registrarCambioCatalogo(administrador, ip, "Parámetro del Sistema",
                "cambió el valor de '" + parametro.getNombreParametro() + "' de '" + valorAnterior + "' a '" + request.getValor() + "' de",
                "");
        return parametro;
    }

    private void registrarCambioCatalogo(Usuario administrador, String ip, String tipoCatalogo, String accion, String nombre) {
        bitacoraRegistroService.registrarEventoUsuario(administrador, administrador, "Mantenimiento de catálogo",
                "El Administrador '" + administrador.getNombreCompleto() + "' " + accion + " el elemento de catálogo [" + tipoCatalogo + "] '" + nombre + "'.");
    }
}
