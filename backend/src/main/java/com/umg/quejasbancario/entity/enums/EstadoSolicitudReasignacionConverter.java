package com.umg.quejasbancario.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoSolicitudReasignacionConverter implements AttributeConverter<EstadoSolicitudReasignacion, String> {
    @Override
    public String convertToDatabaseColumn(EstadoSolicitudReasignacion attribute) {
        return attribute == null ? null : attribute.getValor();
    }
    @Override
    public EstadoSolicitudReasignacion convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EstadoSolicitudReasignacion.fromValor(dbData);
    }
}
