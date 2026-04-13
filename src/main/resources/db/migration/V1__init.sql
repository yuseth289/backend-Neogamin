CREATE TABLE IF NOT EXISTS usuario (
    id_usuario BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(30),
    rol VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS sesion (
    id_sesion BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_usuario BIGINT NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    ip_origen VARCHAR(50),
    user_agent VARCHAR(512),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    expira_en TIMESTAMPTZ NOT NULL,
    creada_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revocada_en TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS direccion (
    id_direccion BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_usuario BIGINT NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL,
    es_principal BOOLEAN NOT NULL DEFAULT FALSE,
    pais VARCHAR(80) NOT NULL,
    departamento VARCHAR(100),
    ciudad VARCHAR(100) NOT NULL,
    comuna VARCHAR(100),
    codigo_postal VARCHAR(20),
    calle VARCHAR(150) NOT NULL,
    numero VARCHAR(30) NOT NULL,
    referencia VARCHAR(255),
    estado VARCHAR(20) NOT NULL,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS categoria (
    id_categoria BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_categoria_padre BIGINT REFERENCES categoria(id_categoria) ON DELETE SET NULL,
    nombre VARCHAR(120) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    estado VARCHAR(20) NOT NULL,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS producto (
    id_producto BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_categoria BIGINT NOT NULL REFERENCES categoria(id_categoria),
    fk_vendedor BIGINT NOT NULL REFERENCES usuario(id_usuario),
    sku VARCHAR(80) NOT NULL UNIQUE,
    slug VARCHAR(180) NOT NULL UNIQUE,
    nombre VARCHAR(180) NOT NULL,
    descripcion TEXT,
    moneda CHAR(3) NOT NULL,
    precio_lista NUMERIC(14,2) NOT NULL,
    precio_vigente_cache NUMERIC(14,2),
    stock_fisico INT NOT NULL DEFAULT 0,
    stock_reservado INT NOT NULL DEFAULT 0,
    needs_recalc BOOLEAN NOT NULL DEFAULT FALSE,
    condicion VARCHAR(50),
    estado VARCHAR(20) NOT NULL,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS producto_imagen (
    id_imagen BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_producto BIGINT NOT NULL REFERENCES producto(id_producto) ON DELETE CASCADE,
    url_imagen TEXT NOT NULL,
    alt_text VARCHAR(180),
    orden INT NOT NULL DEFAULT 1,
    es_principal BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS oferta (
    id_oferta BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_producto BIGINT NOT NULL REFERENCES producto(id_producto) ON DELETE CASCADE,
    titulo VARCHAR(150) NOT NULL,
    descripcion VARCHAR(255),
    porcentaje_desc NUMERIC(5,2),
    precio_oferta NUMERIC(14,2),
    fecha_inicio TIMESTAMPTZ NOT NULL,
    fecha_fin TIMESTAMPTZ NOT NULL,
    estado VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pedido (
    id_pedido BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_usuario BIGINT NOT NULL REFERENCES usuario(id_usuario),
    moneda CHAR(3) NOT NULL,
    fk_direccion_envio BIGINT REFERENCES direccion(id_direccion),
    fk_direccion_factura BIGINT REFERENCES direccion(id_direccion),
    subtotal_productos NUMERIC(14,2) NOT NULL DEFAULT 0,
    descuento NUMERIC(14,2) NOT NULL DEFAULT 0,
    impuesto NUMERIC(14,2) NOT NULL DEFAULT 0,
    costo_envio NUMERIC(14,2) NOT NULL DEFAULT 0,
    total NUMERIC(14,2) NOT NULL DEFAULT 0,
    needs_recalc BOOLEAN NOT NULL DEFAULT FALSE,
    estado VARCHAR(30) NOT NULL,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_pago TIMESTAMPTZ,
    fecha_entrega TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pedido_detalle (
    id_detalle BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_pedido BIGINT NOT NULL REFERENCES pedido(id_pedido) ON DELETE CASCADE,
    fk_producto BIGINT NOT NULL REFERENCES producto(id_producto),
    producto_sku VARCHAR(80) NOT NULL,
    producto_nombre VARCHAR(180) NOT NULL,
    moneda CHAR(3) NOT NULL,
    cantidad INT NOT NULL,
    precio_lista_unitario NUMERIC(14,2) NOT NULL,
    descuento_unitario NUMERIC(14,2) NOT NULL DEFAULT 0,
    precio_final_unitario NUMERIC(14,2) NOT NULL,
    impuesto_unitario NUMERIC(14,2) NOT NULL DEFAULT 0,
    subtotal_linea NUMERIC(14,2) NOT NULL,
    total_linea NUMERIC(14,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (fk_pedido, fk_producto)
);

CREATE TABLE IF NOT EXISTS producto_stock_movimiento (
    id_movimiento BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_producto BIGINT NOT NULL REFERENCES producto(id_producto) ON DELETE CASCADE,
    fk_pedido BIGINT REFERENCES pedido(id_pedido) ON DELETE SET NULL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    cantidad INT NOT NULL,
    stock_fisico_anterior INT NOT NULL,
    stock_fisico_nuevo INT NOT NULL,
    stock_reservado_anterior INT NOT NULL,
    stock_reservado_nuevo INT NOT NULL,
    motivo VARCHAR(255),
    fecha_movimiento TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pago (
    id_pago BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_pedido BIGINT NOT NULL REFERENCES pedido(id_pedido),
    fk_usuario BIGINT NOT NULL REFERENCES usuario(id_usuario),
    proveedor_pago VARCHAR(80),
    referencia_interna VARCHAR(120) UNIQUE,
    referencia_externa VARCHAR(160),
    idempotency_key VARCHAR(120),
    monto NUMERIC(14,2) NOT NULL,
    moneda CHAR(3) NOT NULL,
    tipo_pago VARCHAR(20) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    payload_respuesta TEXT,
    fecha_evento TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS factura (
    id_factura BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_pedido BIGINT NOT NULL UNIQUE REFERENCES pedido(id_pedido),
    fk_pago BIGINT REFERENCES pago(id_pago),
    numero_factura VARCHAR(60) NOT NULL UNIQUE,
    moneda CHAR(3) NOT NULL,
    subtotal NUMERIC(14,2) NOT NULL DEFAULT 0,
    descuento NUMERIC(14,2) NOT NULL DEFAULT 0,
    impuesto NUMERIC(14,2) NOT NULL DEFAULT 0,
    costo_envio NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_neto NUMERIC(14,2) NOT NULL DEFAULT 0,
    metodo_pago VARCHAR(20),
    fecha_emision TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    estado_factura VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS resena (
    id_resena BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_usuario BIGINT NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    fk_producto BIGINT NOT NULL REFERENCES producto(id_producto) ON DELETE CASCADE,
    fk_pedido BIGINT REFERENCES pedido(id_pedido) ON DELETE SET NULL,
    compra_verificada BOOLEAN NOT NULL DEFAULT FALSE,
    calificacion SMALLINT NOT NULL,
    comentario TEXT,
    fecha TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (fk_usuario, fk_producto)
);

CREATE TABLE IF NOT EXISTS producto_like (
    id_like BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_usuario BIGINT NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    fk_producto BIGINT NOT NULL REFERENCES producto(id_producto) ON DELETE CASCADE,
    fecha_like TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (fk_usuario, fk_producto)
);

CREATE TABLE IF NOT EXISTS producto_deseado (
    id_deseado BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_usuario BIGINT NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    fk_producto BIGINT NOT NULL REFERENCES producto(id_producto) ON DELETE CASCADE,
    fecha_agregado TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (fk_usuario, fk_producto)
);

CREATE TABLE IF NOT EXISTS auditoria_log (
    id_auditoria BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tabla VARCHAR(100) NOT NULL,
    operacion VARCHAR(20) NOT NULL,
    id_registro TEXT,
    datos_anteriores TEXT,
    datos_nuevos TEXT,
    fecha_evento TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    usuario_db TEXT,
    app_user_id BIGINT,
    observacion TEXT
);
