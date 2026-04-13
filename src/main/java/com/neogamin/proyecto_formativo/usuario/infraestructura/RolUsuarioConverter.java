package com.neogamin.proyecto_formativo.usuario.infraestructura;

import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = true)
public class RolUsuarioConverter implements AttributeConverter<RolUsuario, String> {

    @Override
    public String convertToDatabaseColumn(RolUsuario attribute) {
        return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public RolUsuario convertToEntityAttribute(String dbData) {
        return dbData == null ? null : RolUsuario.valueOf(dbData.toUpperCase(Locale.ROOT));
    }
}
