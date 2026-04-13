package com.neogamin.proyecto_formativo.compartido.infraestructura;

import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = true)
public class EstadoGenericoConverter implements AttributeConverter<EstadoGenerico, String> {

    @Override
    public String convertToDatabaseColumn(EstadoGenerico attribute) {
        return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public EstadoGenerico convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EstadoGenerico.valueOf(dbData.toUpperCase(Locale.ROOT));
    }
}
