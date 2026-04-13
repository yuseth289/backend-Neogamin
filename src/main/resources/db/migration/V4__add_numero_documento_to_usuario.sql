ALTER TABLE usuario
ADD COLUMN IF NOT EXISTS numero_documento VARCHAR(30);

CREATE UNIQUE INDEX IF NOT EXISTS uq_usuario_numero_documento
ON usuario (numero_documento)
WHERE numero_documento IS NOT NULL;
