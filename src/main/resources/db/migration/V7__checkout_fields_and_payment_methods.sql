ALTER TABLE public.pedido
ADD COLUMN IF NOT EXISTS numero_pedido VARCHAR(60);

ALTER TABLE public.pedido
ADD COLUMN IF NOT EXISTS direccion_envio_snapshot JSONB;

ALTER TABLE public.pedido
ADD COLUMN IF NOT EXISTS direccion_factura_snapshot JSONB;

ALTER TABLE public.pedido
ADD COLUMN IF NOT EXISTS fecha_estimada_entrega TIMESTAMPTZ;

CREATE UNIQUE INDEX IF NOT EXISTS uq_pedido_numero_pedido
ON public.pedido (numero_pedido)
WHERE numero_pedido IS NOT NULL;

ALTER TYPE tipo_pago ADD VALUE IF NOT EXISTS 'PAYPAL';
ALTER TYPE tipo_pago ADD VALUE IF NOT EXISTS 'EFECTY';
ALTER TYPE tipo_pago ADD VALUE IF NOT EXISTS 'NEQUI';
