UPDATE public.usuario
SET rol = lower(rol)
WHERE rol IS NOT NULL;

UPDATE public.usuario
SET estado = lower(estado)
WHERE estado IS NOT NULL;

UPDATE public.direccion
SET estado = lower(estado)
WHERE estado IS NOT NULL;

UPDATE public.categoria
SET estado = lower(estado)
WHERE estado IS NOT NULL;

UPDATE public.producto
SET estado = lower(estado)
WHERE estado IS NOT NULL;

UPDATE public.oferta
SET estado = lower(estado)
WHERE estado IS NOT NULL;

ALTER TABLE public.usuario
DROP CONSTRAINT IF EXISTS usuario_rol_check;

ALTER TABLE public.usuario
DROP CONSTRAINT IF EXISTS usuario_estado_check;

ALTER TABLE public.direccion
DROP CONSTRAINT IF EXISTS direccion_estado_check;

ALTER TABLE public.categoria
DROP CONSTRAINT IF EXISTS categoria_estado_check;

ALTER TABLE public.producto
DROP CONSTRAINT IF EXISTS producto_estado_check;

ALTER TABLE public.oferta
DROP CONSTRAINT IF EXISTS oferta_estado_check;

ALTER TABLE public.usuario
ADD CONSTRAINT usuario_rol_check
CHECK (rol IN ('cliente', 'vendedor', 'admin'));

ALTER TABLE public.usuario
ADD CONSTRAINT usuario_estado_check
CHECK (estado IN ('activo', 'inactivo'));

ALTER TABLE public.direccion
ADD CONSTRAINT direccion_estado_check
CHECK (estado IN ('activo', 'inactivo'));

ALTER TABLE public.categoria
ADD CONSTRAINT categoria_estado_check
CHECK (estado IN ('activo', 'inactivo'));

ALTER TABLE public.producto
ADD CONSTRAINT producto_estado_check
CHECK (estado IN ('activo', 'inactivo'));

ALTER TABLE public.oferta
ADD CONSTRAINT oferta_estado_check
CHECK (estado IN ('activo', 'inactivo'));
