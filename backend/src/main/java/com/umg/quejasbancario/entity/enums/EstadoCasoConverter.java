package com.umg.quejasbancario.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoCasoConverter implements AttributeConverter<EstadoCaso, String> {
    @Override
    public String convertToDatabaseColumn(EstadoCaso attribute) {
        return attribute == null ? null : attribute.getValor();
    }
    @Override
    public EstadoCaso convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EstadoCaso.fromValor(dbData);
    }
}
