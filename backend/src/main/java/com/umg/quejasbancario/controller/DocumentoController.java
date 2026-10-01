package com.umg.quejasbancario.controller;

import com.umg.quejasbancario.entity.DocumentoAdjunto;
import com.umg.quejasbancario.security.CustomUserDetails;
import com.umg.quejasbancario.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @GetMapping("/{id}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails principal) {
        DocumentoAdjunto documento = documentoService.obtenerParaDescarga(id, principal.getUsuario());
        File archivo = new File(documento.getRutaArchivo());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(documento.getNombreArchivo()).build());

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(archivo));
    }
}
