CREATE OR REPLACE FUNCTION public.trg_set_updated_at_carrito()
RETURNS trigger
LANGUAGE plpgsql
AS $function$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$function$;

CREATE TABLE IF NOT EXISTS public.carrito (
    id_carrito BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_usuario BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_carrito_usuario FOREIGN KEY (fk_usuario)
        REFERENCES public.usuario (id_usuario)
        ON DELETE CASCADE,
    CONSTRAINT chk_carrito_estado
        CHECK (estado IN ('ACTIVO', 'CONVERTIDO', 'ABANDONADO'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_carrito_usuario_activo
ON public.carrito (fk_usuario)
WHERE estado = 'ACTIVO';

CREATE INDEX IF NOT EXISTS idx_carrito_usuario
ON public.carrito (fk_usuario);

CREATE TABLE IF NOT EXISTS public.carrito_item (
    id_carrito_item BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fk_carrito BIGINT NOT NULL,
    fk_producto BIGINT NOT NULL,
    cantidad INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_carrito_item_carrito FOREIGN KEY (fk_carrito)
        REFERENCES public.carrito (id_carrito)
        ON DELETE CASCADE,
    CONSTRAINT fk_carrito_item_producto FOREIGN KEY (fk_producto)
        REFERENCES public.producto (id_producto)
        ON DELETE RESTRICT,
    CONSTRAINT uq_carrito_item_producto UNIQUE (fk_carrito, fk_producto),
    CONSTRAINT chk_carrito_item_cantidad CHECK (cantidad > 0)
);

CREATE INDEX IF NOT EXISTS idx_carrito_item_carrito
ON public.carrito_item (fk_carrito);

CREATE INDEX IF NOT EXISTS idx_carrito_item_producto
ON public.carrito_item (fk_producto);

DROP TRIGGER IF EXISTS trg_carrito_updated_at ON public.carrito;
CREATE TRIGGER trg_carrito_updated_at
BEFORE UPDATE ON public.carrito
FOR EACH ROW
EXECUTE FUNCTION public.trg_set_updated_at_carrito();

DROP TRIGGER IF EXISTS trg_carrito_item_updated_at ON public.carrito_item;
CREATE TRIGGER trg_carrito_item_updated_at
BEFORE UPDATE ON public.carrito_item
FOR EACH ROW
EXECUTE FUNCTION public.trg_set_updated_at_carrito();
