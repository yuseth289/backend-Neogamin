CREATE TABLE IF NOT EXISTS public.moneda_referencia (
    codigo CHAR(3) PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    simbolo VARCHAR(10),
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.moneda_referencia (codigo, nombre, simbolo, activa)
VALUES
    ('COP', 'Peso colombiano', '$', TRUE),
    ('USD', 'Dolar estadounidense', '$', TRUE),
    ('EUR', 'Euro', 'EUR', TRUE)
ON CONFLICT (codigo) DO NOTHING;

CREATE TABLE IF NOT EXISTS public.producto_precio_historial (
    id_historial BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_producto BIGINT NOT NULL REFERENCES public.producto (id_producto) ON DELETE CASCADE,
    moneda CHAR(3) NOT NULL,
    precio_anterior NUMERIC(14,2) NOT NULL,
    precio_nuevo NUMERIC(14,2) NOT NULL,
    fecha_cambio TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fk_usuario_cambio BIGINT REFERENCES public.usuario (id_usuario) ON DELETE SET NULL,
    motivo VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_producto_precio_historial_producto
ON public.producto_precio_historial (fk_producto, fecha_cambio DESC);
