ALTER TABLE public.sesion
ALTER COLUMN ip_origen TYPE inet
USING NULLIF(ip_origen, '')::inet;

ALTER TABLE public.pago
ALTER COLUMN payload_respuesta TYPE jsonb
USING CASE
    WHEN payload_respuesta IS NULL OR btrim(payload_respuesta) = '' THEN NULL
    ELSE payload_respuesta::jsonb
END;
