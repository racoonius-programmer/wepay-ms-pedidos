# Microservicio de Pedidos (`ms-pedidos`)

## Descripción

`ms-pedidos` es un microservicio backend desarrollado en Spring Boot responsable de la gestión, cálculo y persistencia de las compras realizadas por los usuarios. Opera en un entorno aislado (puerto 8085) y no interactúa directamente con el frontend ni valida tokens de seguridad; confía exclusivamente en el BFF como orquestador central para la autorización y el enrutamiento.

## Tecnologías Utilizadas

* **Java 25** con Spring Boot 3.x
* **Spring Web** para exposición de API REST
* **Spring Data JPA** & **Hibernate** para mapeo objeto-relacional (ORM)
* **H2 Database** (Base de datos relacional en memoria)
* **Jackson Databind** para serialización/deserialización y carga inicial de datos.

## Flujo de Datos

El ciclo de vida de un pedido desde que el usuario confirma la compra hasta que se guarda en la base de datos sigue este flujo exacto:

1. **Recepción desde el BFF (Controller):** El microservicio recibe una petición HTTP `POST` en `/api/pedidos/crear/{oid}` enviada por el BFF. El cuerpo de la petición contiene un `PedidoRequest` con una lista de `ItemCarritoRequest` (nombre del producto, precio unitario y cantidad).
2. **Procesamiento (Service):**
* Se instancia una nueva entidad `Pedido` asignándole la fecha actual, el estado inicial (`CONFIRMADO`) y el `oid` del usuario.
* Se itera sobre la lista de productos (`ItemCarritoRequest`). Por cada ítem, se crea una entidad `DetallePedido` y se añade a la lista interna del pedido.
* Simultáneamente, se multiplica el precio unitario por la cantidad de cada ítem y se acumula en la variable del total.
* Se asigna el total calculado al pedido maestro.


3. **Persistencia (Repository/JPA):** Se invoca el método `save(pedido)`. Gracias a la configuración de cascada (`CascadeType.ALL`), Hibernate inserta automáticamente el registro maestro en la tabla `pedidos` y luego inserta cada producto individual en la tabla `detalles_pedido`, estableciendo las llaves foráneas.
4. **Respuesta (DTO):** La entidad guardada (ahora con su ID generado por la base de datos) se transforma en un `PedidoResponse` y se devuelve al BFF como confirmación de éxito.

## Modelo de Datos

El sistema utiliza un modelo de dos tablas fuertemente relacionadas:

* **Pedido:** Almacena la metadata general de la compra (`id`, `usuarioOid`, `total`, `fechaRegistro`, `estado`).
* **DetallePedido:** Almacena una "fotografía" estática de los productos comprados (`id`, `nombreProducto`, `precioUnitario`, `cantidad`). Esto garantiza que los historiales de compra no se alteren si el catálogo cambia los precios o nombres en el futuro.

## Endpoints Internos

Estos endpoints son consumidos exclusivamente por el BFF.

* **POST `/api/pedidos/crear/{oid}`:**
* Recibe un JSON con la lista de productos y crea el pedido vinculado al identificador del usuario.


* **GET `/api/pedidos`:**
* Devuelve la lista completa de todos los pedidos registrados en el sistema. Utilizado cuando un administrador accede al historial.


* **GET `/api/pedidos/usuario/{oid}`:**
* Devuelve únicamente los pedidos cuyo `usuarioOid` coincida con el parámetro enviado. Utilizado para cargar el historial personal de un usuario estándar.



## Carga Inicial de Datos (DataLoader)

Para facilitar el entorno de desarrollo, el microservicio incluye un componente `DataLoader` que implementa `CommandLineRunner`.
Al iniciar la aplicación, este componente lee automáticamente el archivo `src/main/resources/pedidos-ejemplo.json`, utiliza `ObjectMapper` para parsear la estructura y simula compras iniciales inyectándolas a la base de datos a través de la misma lógica de negocio del servicio.

## Ejecución Local

Asegúrate de no tener conflictos de puertos e inicia el servicio con Maven:

```bash
cd /ruta/a/tu/proyecto/ms-pedidos
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
./mvnw clean spring-boot:run

```

El servidor Tomcat se inicializará en el puerto **8085** y la consola de H2 estará disponible (si está habilitada en el `application.yml`).