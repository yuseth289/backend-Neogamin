CREATE TABLE IF NOT EXISTS vendedor (
    id_vendedor BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_usuario BIGINT NOT NULL UNIQUE REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    nombre_completo_o_razon_social VARCHAR(150) NOT NULL,
    tipo_documento VARCHAR(30) NOT NULL,
    numero_documento VARCHAR(30) NOT NULL UNIQUE,
    pais VARCHAR(80) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    correo VARCHAR(190) NOT NULL,
    nombre_comercial VARCHAR(150) NOT NULL,
    acepta_terminos BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS datos_pago_vendedor (
    id_datos_pago BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_vendedor BIGINT NOT NULL UNIQUE REFERENCES vendedor(id_vendedor) ON DELETE CASCADE,
    tipo_cuenta VARCHAR(20) NOT NULL,
    numero_cuenta VARCHAR(40) NOT NULL,
    banco VARCHAR(120) NOT NULL,
    titular_cuenta VARCHAR(150) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
