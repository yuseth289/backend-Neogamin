# NeoGaming Backend

Backend Spring Boot del MVP de NeoGaming. Expone autenticacion JWT, catalogo, carrito, checkout, pedidos, pagos y modulos secundarios del marketplace.

## Estado actual

- JWT con sesiones revocables.
- `POST /api/auth/logout` disponible.
- CORS configurable para frontend local.
- `401` consistente para JWT invalido o expirado.
- Swagger y healthcheck activos en desarrollo.
- PostgreSQL local estandarizado via Docker en `localhost:5433`.
- Flyway configurado como fuente de verdad del esquema.

## Requisitos

- Java 21
- Maven Wrapper incluido
- Docker Desktop para PostgreSQL local

## Arranque local

1. Levantar PostgreSQL:

```bash
docker compose up -d
```

2. Verificar que el contenedor quede `healthy`:

```bash
docker ps
```

Debes ver el puerto:

```text
0.0.0.0:5433->5432/tcp
```

3. Ejecutar backend:

```bash
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Reinicio limpio de base local

Si necesitas reconstruir la base desde cero y reaplicar migraciones:

```bash
docker compose down -v
docker compose up -d
```

## Tests

```bash
./mvnw test
```

En Windows PowerShell:

```powershell
.\mvnw.cmd test
```

## Configuracion principal

Archivo: `src/main/resources/application.yml`

Valores por defecto de desarrollo:

- `DB_URL=jdbc:postgresql://localhost:5433/neogaming`
- `DB_USERNAME=neogaming`
- `DB_PASSWORD=neogaming_dev_password`
- `JWT_SECRET=change-this-dev-secret-before-deploying-neogaming`
- `JWT_EXPIRATION_MINUTES=120`
- `APP_CORS_ORIGIN_1=http://localhost:4200`
- `APP_CORS_ORIGIN_2=http://localhost:4000`

## Base de datos

- Imagen Docker local: `postgres:17-alpine`
- Puerto host: `5433`
- Puerto interno del contenedor: `5432`
- Migraciones Flyway en `src/main/resources/db/migration`

Migraciones relevantes recientes:

- `V6__create_carrito_tables.sql`
- `V9__create_catalog_reference_tables.sql`
- `V10__align_postgres_types_with_jpa.sql`
- `V11__normalize_string_enum_columns.sql`

## Endpoints utiles

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

## Flujos principales del MVP

- `POST /api/auth/registro`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/usuarios/me`
- `GET /api/catalogo/productos`
- `GET /api/catalogo/productos/slug/{slug}`
- `GET /api/carrito`
- `POST /api/carrito/items`
- `PATCH /api/carrito/items/{idItem}`
- `DELETE /api/carrito/items/{idItem}`
- `POST /api/checkout`
- `POST /api/checkout/envio`
- `POST /api/checkout/pago`
- `GET /api/checkout/confirmacion/{numeroPedido}`
- `GET /api/pedidos/mis-pedidos`

## Checklist rapido de validacion

1. Registro de usuario nuevo.
2. Login con JWT.
3. Consulta de catalogo y detalle por slug.
4. Agregar producto al carrito desde varias pantallas.
5. Checkout completo.
6. Confirmacion de pedido.
7. Consulta de historial de pedidos.

## Pendientes

- Ampliar cobertura de tests en carrito, checkout y pagos.
- Reducir mas datos demo en algunas pantallas del frontend.
- Mejorar observabilidad mas alla de `/actuator/health`.
- Endurecer configuracion de produccion y manejo de secretos.
