package com.umg.quejasbancario.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoUsuarioConverter implements AttributeConverter<EstadoUsuario, String> {
    @Override
    public String convertToDatabaseColumn(EstadoUsuario attribute) {
        return attribute == null ? null : attribute.getValor();
    }
    @Override
    public EstadoUsuario convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EstadoUsuario.fromValor(dbData);
    }
}
