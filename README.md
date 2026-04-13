# Proyecto Formativo Backend

Backend de ecommerce tipo marketplace construido con Spring Boot. La API cubre el flujo principal de una tienda en linea: autenticacion, gestion de usuarios, catalogo, carrito, checkout, pedidos, pagos, inventuracion, facturacion, reseñas, wishlist y analitica.

El proyecto esta organizado como un monolito modular, con separacion por dominios de negocio:

- `usuario`: registro, login, perfil, direcciones y conversion a vendedor.
- `catalogo`: productos, categorias, ofertas e imagenes.
- `carrito`: carrito del usuario autenticado.
- `checkout`: proceso de compra desde carrito hasta confirmacion.
- `pedido`: gestion y consulta de pedidos.
- `pago`: pagos y sus transiciones.
- `inventario`: reserva, confirmacion y liberacion de stock.
- `facturacion`: emision y consulta de facturas.
- `resena`: calificaciones y comentarios de productos.
- `interaccion`: likes y wishlist.
- `analitica`: indicadores para admin y vendedor.

## 1. Para que sirve este backend

Este backend sirve para exponer una API REST que pueda ser consumida por:

- un frontend web o mobile,
- Postman o Insomnia,
- Swagger UI,
- integraciones internas de administracion,
- pruebas tecnicas o demos del flujo ecommerce.

En terminos funcionales, permite:

- registrar usuarios y autenticarlos con JWT,
- consultar y administrar productos del catalogo,
- buscar productos por filtros y por busqueda natural,
- agregar productos al carrito y convertirlos en pedido,
- ejecutar checkout con direccion, pago y confirmacion,
- administrar stock durante el proceso de compra,
- aprobar o rechazar pagos,
- generar factura luego de un pago aprobado,
- dejar reseñas sobre productos comprados,
- guardar productos en wishlist y marcar likes,
- consultar dashboards de analitica para vendedor y administrador.

## 2. Stack y dependencias del proyecto

### Tecnologias principales

- Java 21
- Spring Boot 4.0.5
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- PostgreSQL
- Flyway
- JWT con `jjwt`
- Springdoc OpenAPI / Swagger UI
- Lombok
- Maven

### Dependencias Maven destacadas

Estas son las dependencias mas importantes del `pom.xml` y para que se usan:

- `spring-boot-starter-web`: expone la API REST.
- `spring-boot-starter-validation`: valida requests con anotaciones como `@Valid`.
- `spring-boot-starter-security`: maneja autenticacion y autorizacion.
- `spring-boot-starter-data-jpa`: persistencia con JPA/Hibernate.
- `postgresql`: driver de base de datos.
- `flyway-core` y `flyway-database-postgresql`: migraciones versionadas de esquema.
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`: generacion y validacion de tokens JWT.
- `springdoc-openapi-starter-webmvc-ui`: documentacion y UI interactiva de Swagger.
- `lombok`: reduce boilerplate en entidades, DTOs y servicios.

### Dependencias para consumirlo

Si solo vas a consumir la API, realmente necesitas:

- que el backend este ejecutandose,
- una base de datos PostgreSQL disponible,
- un cliente HTTP como navegador, Postman, Insomnia, curl o un frontend,
- un JWT valido para los endpoints protegidos.

Si ademas quieres levantarlo localmente en desarrollo, necesitas:

- JDK 21,
- Maven Wrapper del proyecto (`./mvnw`),
- Docker y Docker Compose si quieres levantar PostgreSQL por contenedor.

## 3. Requisitos previos

Antes de iniciar, valida que tengas:

- Java 21 instalado.
- Docker instalado si vas a usar `compose.yaml`.
- Un puerto disponible para PostgreSQL.
- Acceso a una base PostgreSQL vacia o reutilizable para las migraciones Flyway.

## 4. Configuracion del proyecto

La configuracion principal esta en `src/main/resources/application.yml`.

Valores relevantes:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES`
- `SPRINGDOC_API_DOCS_ENABLED`
- `SPRINGDOC_SWAGGER_UI_ENABLED`

Por defecto, la app intenta conectarse a:

```yaml
DB_URL=jdbc:postgresql://localhost:5432/neogaming
DB_USERNAME=yuseth
DB_PASSWORD=1234
```

Tambien habilita:

- Flyway para migraciones automaticas.
- Swagger UI.
- OpenAPI docs.

## 5. Docker Compose explicado

El archivo `compose.yaml` define un servicio `postgres`:

```yaml
services:
  postgres:
    image: 'postgres:latest'
    environment:
      - 'POSTGRES_DB=mydatabase'
      - 'POSTGRES_PASSWORD=secret'
      - 'POSTGRES_USER=myuser'
    ports:
      - '5432'
```

### Que hace este compose

- Descarga una imagen oficial de PostgreSQL.
- Crea una base de datos llamada `mydatabase`.
- Crea un usuario `myuser`.
- Le asigna la clave `secret`.
- Expone el puerto interno `5432` del contenedor.

### Importante: hay una diferencia con `application.yml`

El `compose.yaml` usa:

- base de datos: `mydatabase`
- usuario: `myuser`
- clave: `secret`

Pero `application.yml` espera por defecto:

- base de datos: `neogaming`
- usuario: `yuseth`
- clave: `1234`

Eso significa que, si levantas el `compose.yaml` tal como esta y no pasas variables de entorno al backend, la aplicacion no va a conectar correctamente.

### Importante: la publicacion del puerto tambien puede requerir ajuste

El archivo usa:

```yaml
ports:
  - '5432'
```

Con esa forma corta, Docker puede publicar el puerto del contenedor sin fijarlo explicitamente como `localhost:5432`. Si tu backend intenta conectarse a `localhost:5432`, conviene cambiarlo a:

```yaml
ports:
  - "5432:5432"
```

Asi garantizas que PostgreSQL quede disponible exactamente en `localhost:5432`.

### Como resolverlo

Tienes dos opciones:

#### Opcion A: mantener `compose.yaml` y adaptar el backend al compose

Arranca PostgreSQL:

```bash
docker compose up -d
```

Luego arranca la app con estas variables:

```bash
DB_URL=jdbc:postgresql://localhost:5432/mydatabase \
DB_USERNAME=myuser \
DB_PASSWORD=secret \
./mvnw spring-boot:run
```

#### Opcion B: cambiar el compose para que coincida con `application.yml`

Podrias ajustar el contenedor a:

- `POSTGRES_DB=neogaming`
- `POSTGRES_USER=yuseth`
- `POSTGRES_PASSWORD=1234`
- `ports: "5432:5432"`

Y luego iniciar normalmente la app sin variables extras.

### Recomendacion

Para consumo local rapido, la opcion A es la mas segura si no quieres tocar archivos del proyecto.

## 6. Como levantar el proyecto localmente

### 1. Levantar PostgreSQL con Docker Compose

```bash
docker compose up -d
```

### 2. Iniciar el backend

Si usas el `compose.yaml` actual:

```bash
DB_URL=jdbc:postgresql://localhost:5432/mydatabase \
DB_USERNAME=myuser \
DB_PASSWORD=secret \
./mvnw spring-boot:run
```

Si tu base ya coincide con `application.yml`, puedes usar:

```bash
./mvnw spring-boot:run
```

### 3. Verificar que la app levanto bien

URLs utiles:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 7. Seguridad y autenticacion

La API usa JWT Bearer.

### Endpoints publicos

Segun la configuracion de seguridad, son publicos:

- `/api/auth/**`
- `/swagger-ui/**`
- `/swagger-ui.html`
- `/v3/api-docs/**`
- `GET /api/catalogo/productos/**`

Todo lo demas requiere autenticacion, y varios casos ademas exigen rol:

- `ADMIN`
- `VENDEDOR`
- `CLIENTE`

### Flujo para autenticarse

#### 1. Registrar usuario

`POST /api/auth/registro`

Ejemplo:

```json
{
  "nombre": "Usuario Demo",
  "email": "demo@correo.com",
  "password": "Clave123*",
  "telefono": "3001234567"
}
```

#### 2. Iniciar sesion

`POST /api/auth/login`

Ejemplo:

```json
{
  "email": "demo@correo.com",
  "password": "Clave123*"
}
```

Respuesta esperada:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "usuarioId": 2,
  "nombre": "Usuario Demo",
  "email": "demo@correo.com",
  "rol": "ROLE_CLIENTE"
}
```

#### 3. Usar el token

En cada request protegido debes enviar:

```http
Authorization: Bearer TU_TOKEN
```

En Swagger puedes usar el boton `Authorize`.

## 8. Resumen de funcionalidades del backend

### 8.1 Usuarios y autenticacion

Permite:

- registrar nuevos clientes,
- iniciar sesion,
- consultar el usuario autenticado,
- actualizar perfil,
- administrar direcciones,
- convertir un cliente en vendedor.

Sirve para gestionar identidad, acceso y datos del comprador o vendedor dentro de la plataforma.

### 8.2 Catalogo de productos

Permite:

- listar productos con filtros,
- consultar detalle por id o slug,
- crear productos,
- actualizarlos,
- cambiar precio,
- cambiar stock,
- agregar imagenes,
- definir imagen principal.

Sirve para publicar y administrar el inventario comercial de la tienda.

### 8.3 Categorias

Permite:

- crear categorias,
- consultar el arbol de categorias,
- listar productos por categoria.

Sirve para organizar el catalogo y mejorar la navegacion.

### 8.4 Ofertas

Permite:

- consultar ofertas activas,
- crear ofertas comerciales.

Sirve para promociones y reglas de precio temporal.

### 8.5 Busqueda natural

Permite buscar productos usando texto mas libre, con interpretacion de terminos e intencion de consulta.

Sirve para que el usuario no dependa solo de filtros exactos y pueda buscar de forma mas cercana al lenguaje natural.

### 8.6 Carrito

Permite:

- consultar el carrito actual,
- agregar productos,
- cambiar cantidades,
- eliminar items,
- vaciar el carrito,
- convertir carrito a pedido.

Regla importante:

- no permite mezclar productos de distintas monedas en un mismo carrito.

Sirve para construir la compra antes del pago.

### 8.7 Checkout

Permite:

- iniciar checkout desde carrito,
- guardar direccion de envio,
- guardar direccion de facturacion,
- calcular impuestos y costo de envio,
- procesar pago,
- consultar confirmacion del pedido.

Actualmente el flujo contempla logica de pasarela simulada y actualizacion de estados internos del pedido.

### 8.8 Pedidos

Permite:

- crear pedidos,
- agregar items a un pedido,
- recalcular montos,
- hacer checkout,
- listar mis pedidos,
- consultar detalle de pedido.

Sirve para persistir la compra y manejar su ciclo de vida.

### 8.9 Pagos

Permite:

- registrar pagos pendientes,
- aprobar pagos,
- rechazar pagos,
- consultar pagos.

Cuando un pago se aprueba:

- el pedido pasa a pagado,
- el inventario se confirma,
- se emite factura.

Cuando un pago se rechaza:

- el pedido se cancela,
- se libera stock reservado.

### 8.10 Inventario

Gestiona tres acciones clave:

- reservar stock cuando se inicia el pago,
- confirmar salida de stock al aprobar pago,
- liberar stock al rechazar pago.

Sirve para mantener coherencia entre compras y disponibilidad real.

### 8.11 Facturacion

Permite:

- emitir factura asociada a un pedido pagado,
- consultar factura por pedido.

Sirve como soporte administrativo y comprobante de venta.

### 8.12 Reseñas

Permite:

- crear o actualizar reseñas,
- listar reseñas por producto,
- ver resumen de calificaciones,
- eliminar reseñas.

Regla importante:

- un usuario normal solo puede reseñar productos que haya comprado.
- un administrador tiene permisos especiales.

Sirve para reputacion del producto y confianza del comprador.

### 8.13 Interacciones

Permite:

- dar o quitar like a un producto,
- agregar o quitar wishlist,
- listar wishlist del usuario.

Sirve para engagement y seguimiento de interes del usuario.

### 8.14 Analitica

Hay dos capas de analitica:

- analitica de administrador,
- analitica de vendedor.

Permite consultar:

- resumen de ventas,
- ventas por periodo,
- top vendedores,
- top productos,
- ventas por categoria,
- metodos de pago,
- pedidos por estado,
- productos mas vendidos,
- alertas de stock bajo.

Sirve para toma de decisiones operativas y comerciales.

## 9. Flujo recomendado para consumir la API

Si vas a probar la API de punta a punta, este es un flujo recomendado:

### Flujo cliente

1. Registrar usuario.
2. Hacer login.
3. Consultar productos del catalogo.
4. Agregar productos al carrito.
5. Crear direccion del usuario.
6. Iniciar checkout.
7. Guardar envio y facturacion.
8. Procesar pago.
9. Consultar confirmacion del pedido.
10. Revisar mis pedidos.
11. Crear reseña del producto comprado.

### Flujo vendedor

1. Registrarse como cliente.
2. Convertirse en vendedor.
3. Crear productos.
4. Cargar imagenes.
5. Ajustar precio y stock.
6. Consultar analitica de vendedor.

### Flujo admin

1. Iniciar sesion con usuario admin.
2. Crear categorias.
3. Consultar analitica general.
4. Aprobar o rechazar pagos.
5. Revisar facturas y pedidos.

## 10. Endpoints principales para consumo

No es una lista exhaustiva, pero si una guia rapida de consumo.

### Autenticacion

- `POST /api/auth/registro`
- `POST /api/auth/login`

### Usuario

- `GET /api/usuarios/me`
- `GET /api/usuarios/perfil`
- `PUT /api/usuarios/perfil`
- `GET /api/usuarios/direcciones`
- `POST /api/usuarios/direcciones`
- `PUT /api/usuarios/direcciones/{idDireccion}`
- `PATCH /api/usuarios/direcciones/{idDireccion}/principal`
- `DELETE /api/usuarios/direcciones/{idDireccion}`
- `POST /api/usuarios/convertir-vendedor`

### Catalogo

- `GET /api/catalogo/productos`
- `GET /api/catalogo/productos/{idProducto}`
- `GET /api/catalogo/productos/slug/{slug}`
- `GET /api/catalogo/productos/buscar-natural`
- `POST /api/catalogo/productos`
- `PUT /api/catalogo/productos/{idProducto}`
- `PATCH /api/catalogo/productos/{idProducto}/precio`
- `PATCH /api/catalogo/productos/{idProducto}/stock`
- `POST /api/catalogo/productos/{idProducto}/imagenes`
- `PATCH /api/catalogo/productos/{idProducto}/imagenes/{idImagen}/principal`

### Categorias y ofertas

- `POST /api/catalogo/categorias`
- `GET /api/catalogo/categorias/arbol`
- `GET /api/catalogo/categorias/{idCategoria}/productos`
- `GET /api/catalogo/ofertas/activas`
- `POST /api/catalogo/ofertas`

### Carrito

- `GET /api/carrito`
- `POST /api/carrito/items`
- `PATCH /api/carrito/items/{idItem}`
- `DELETE /api/carrito/items/{idItem}`
- `DELETE /api/carrito`
- `POST /api/carrito/convertir-a-pedido`

### Checkout y pedidos

- `POST /api/checkout`
- `POST /api/checkout/envio`
- `POST /api/checkout/pago`
- `GET /api/checkout/confirmacion/{numeroPedido}`
- `GET /api/pedidos/mis-pedidos`
- `GET /api/pedidos/{pedidoId}`

### Pagos y facturas

- `GET /api/pagos/{pagoId}`
- `POST /api/pagos/{pagoId}/aprobar`
- `POST /api/pagos/{pagoId}/rechazar`
- `GET /api/facturas/pedido/{pedidoId}`

### Interacciones y reseñas

- `POST /api/interacciones/productos/{productoId}/like`
- `POST /api/interacciones/productos/{productoId}/wishlist`
- `GET /api/interaccion/wishlist`
- `POST /api/resenas`
- `GET /api/resenas/productos/{productoId}`
- `GET /api/resenas/productos/{productoId}/resumen`
- `DELETE /api/resenas/{resenaId}`

### Analitica

- `GET /api/analitica/admin/resumen`
- `GET /api/analitica/admin/ventas-por-periodo`
- `GET /api/analitica/admin/top-vendedores`
- `GET /api/analitica/admin/top-productos`
- `GET /api/analitica/admin/ventas-por-categoria`
- `GET /api/analitica/admin/metodos-pago`
- `GET /api/analitica/admin/pedidos-por-estado`
- `GET /api/analitica/vendedor/resumen`
- `GET /api/analitica/vendedor/ventas-por-periodo`
- `GET /api/analitica/vendedor/productos-mas-vendidos`
- `GET /api/analitica/vendedor/pedidos-por-estado`
- `GET /api/analitica/vendedor/stock-bajo`

## 11. Consumirlo desde Swagger

Cuando el proyecto este ejecutandose:

1. Abre `http://localhost:8080/swagger-ui/index.html`.
2. Usa `POST /api/auth/login` para obtener el token.
3. Pulsa `Authorize`.
4. Pega el JWT como `Bearer <token>` si Swagger no lo completa automaticamente.
5. Prueba los endpoints protegidos.

Swagger es la manera mas rapida de explorar contratos, DTOs y respuestas.

## 12. Consumirlo desde Postman

El repositorio ya incluye archivos Postman en la carpeta `postman/`.

Puedes apoyarte en:

- `postman/Proyecto_Formativo_Ecommerce.postman_collection.json`
- `postman/Proyecto_Formativo_Ecommerce.postman_environment.json`

Recomendacion:

- importa ambos archivos,
- ejecuta primero login o registro,
- guarda el token en una variable de entorno,
- reutiliza esa variable para los endpoints autenticados.

## 13. Base de datos y migraciones

El proyecto usa Flyway con scripts en:

- `src/main/resources/db/migration`

Cuando la aplicacion inicia:

- se conecta a PostgreSQL,
- valida el esquema JPA,
- ejecuta migraciones pendientes.

Esto permite levantar el entorno sin crear tablas manualmente.

## 14. Observaciones utiles para integracion

- Los endpoints GET de productos son publicos.
- El resto del flujo comercial es autenticado.
- Algunas operaciones exigen roles concretos.
- La app trabaja con estado de pedidos, pagos y stock, por lo que el orden de consumo importa.
- El checkout aplica impuestos y costo de envio.
- La facturacion depende del pago aprobado.
- Las reseñas tienen reglas de negocio asociadas a compras reales.

## 15. Comandos utiles

Levantar base:

```bash
docker compose up -d
```

Detener base:

```bash
docker compose down
```

Ejecutar backend con variables compatibles con el compose actual:

```bash
DB_URL=jdbc:postgresql://localhost:5432/mydatabase \
DB_USERNAME=myuser \
DB_PASSWORD=secret \
./mvnw spring-boot:run
```

Compilar:

```bash
./mvnw compile
```

Ejecutar pruebas:

```bash
./mvnw test
```

## 16. Resumen final

Este backend esta pensado para soportar una tienda online completa con capacidades de marketplace. No solo expone CRUDs basicos, sino que modela flujo real de compra con seguridad, roles, stock, pagos, facturacion y analitica. Para consumirlo correctamente, lo mas importante es:

- levantar PostgreSQL,
- alinear credenciales entre `compose.yaml` y `application.yml`,
- obtener un JWT,
- seguir el flujo funcional segun el rol del usuario.
