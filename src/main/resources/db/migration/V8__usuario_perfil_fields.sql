ALTER TABLE public.usuario
ADD COLUMN IF NOT EXISTS sobre_mi VARCHAR(500);

ALTER TABLE public.usuario
ADD COLUMN IF NOT EXISTS foto_perfil_url VARCHAR(500);

ALTER TABLE public.usuario
ADD COLUMN IF NOT EXISTS prefiere_noticias BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE public.usuario
ADD COLUMN IF NOT EXISTS prefiere_ofertas BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_direccion_usuario_estado
ON public.direccion (fk_usuario, estado, es_principal);
