CREATE OR REPLACE FUNCTION public.trg_fn_validar_resena_verificada()
RETURNS trigger
LANGUAGE plpgsql
AS $function$
DECLARE
  v_pedido BIGINT;
  v_rol rol_usuario;
BEGIN
  SELECT u.rol INTO v_rol
  FROM usuario u
  WHERE u.id_usuario = NEW.fk_usuario;

  IF v_rol = 'admin'::rol_usuario THEN
    NEW.fk_pedido := NULL;
    NEW.compra_verificada := FALSE;
    RETURN NEW;
  END IF;

  SELECT p.id_pedido INTO v_pedido
  FROM pedido p
  JOIN pedido_detalle pd ON pd.fk_pedido = p.id_pedido
  WHERE p.fk_usuario   = NEW.fk_usuario
    AND pd.fk_producto = NEW.fk_producto
    AND p.estado IN (
      'entregado'::estado_pedido,
      'pagado'::estado_pedido,
      'enviado'::estado_pedido
    )
  ORDER BY p.fecha_creacion DESC
  LIMIT 1;

  IF v_pedido IS NULL THEN
    RAISE EXCEPTION 'El usuario % no puede reseñar el producto % sin compra válida',
      NEW.fk_usuario, NEW.fk_producto;
  END IF;

  NEW.fk_pedido := COALESCE(NEW.fk_pedido, v_pedido);
  NEW.compra_verificada := TRUE;
  RETURN NEW;
END;
$function$;
