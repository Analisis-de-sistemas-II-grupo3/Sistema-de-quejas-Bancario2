package com.umg.quejasbancario.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoCuentaConverter implements AttributeConverter<EstadoCuenta, String> {
    @Override
    public String convertToDatabaseColumn(EstadoCuenta attribute) {
        return attribute == null ? null : attribute.getValor();
    }
    @Override
    public EstadoCuenta convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EstadoCuenta.fromValor(dbData);
    }
}
