package com.umg.quejasbancario.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoEventoAccesoConverter implements AttributeConverter<TipoEventoAcceso, String> {
    @Override
    public String convertToDatabaseColumn(TipoEventoAcceso attribute) {
        return attribute == null ? null : attribute.getValor();
    }
    @Override
    public TipoEventoAcceso convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TipoEventoAcceso.fromValor(dbData);
    }
}
