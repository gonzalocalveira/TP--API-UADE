# Preguntas de defensa (con respuestas sobre ESTE proyecto)

Respuestas pensadas para decirlas en voz alta. Los nombres entre `comillas` son clases, tablas o endpoints reales del proyecto.

---

## Arquitectura

**¿Por qué dividieron el proyecto en estas capas?**
Para que cada parte tenga una sola razón para cambiar.
- `controller/` sabe de HTTP.
- `service/` sabe de las reglas del cine.
- `repository/` sabe de cómo se guardan los datos.

Si mañana cambia el formato del JSON, toco controllers y DTOs. Si cambia una regla (por ejemplo, que se pueda cancelar hasta una hora antes), toco solo `ClienteService`. Si cambio el motor de base, toco la configuración.

**¿Qué responsabilidad tiene cada capa?**
- **Controller** (`FuncionController`): recibe el request, valida el body con `@Valid`, llama al service y devuelve un `ResponseEntity` con el código correcto.
- **Service** (`FuncionService`): aplica las reglas (que no se superponga con otra función de la sala), maneja la transacción y convierte entidades a DTOs.
- **Repository** (`FuncionRepository`): hace las consultas a la base.
- **Model** (`Funcion`): describe cómo se mapea la clase a la tabla `funcion`.

**¿Qué ocurriría si colocáramos toda la lógica en el Controller?**
- La regla "no vender una butaca ocupada" quedaría atada a HTTP: para reutilizarla desde otro endpoint habría que copiarla.
- No se podría testear sin simular requests. Hoy `ClienteServiceTest` prueba la regla directamente, con mocks y sin servidor.
- Los controllers serían enormes y mezclarían parsear JSON, validar, hacer SQL y armar respuestas.
- Las transacciones quedarían en el lugar equivocado.

**¿Dónde debería estar una regla de negocio y por qué?**
En el service. Por ejemplo, la superposición de funciones está en `FuncionService.validarSinSuperposicion`. No va en el controller, porque la regla tiene que cumplirse la llame quien la llame. Tampoco va en el repository, porque el repository solo consulta datos y no decide nada. Lo que sí va en el DTO es la validación de **formato** (`@Positive` en el precio), porque eso se puede chequear sin la base.

**¿Qué componente debería modificarse si cambiamos el motor de base de datos?**
Casi nada de código:
- La URL y el driver en `application.properties`.
- La dependencia del driver en el `pom.xml`.
- El script `database/01_schema.sql`, porque los `ENUM` y `BIT` son de MySQL.

Services y controllers no cambian. Los repositorios tampoco, porque usan JPQL (sobre clases, no sobre tablas) y Hibernate genera el SQL del motor nuevo a través de su *dialecto*.

---

## MVC

**¿Dónde está el Model en su aplicación?**
En `model/` (las entidades como `Funcion` y `TicketCompra`) junto con la lógica que las usa: `service/` y `repository/`.

**¿Cuál es la responsabilidad del Controller?**
Ser la puerta de entrada HTTP. Por ejemplo, `ClienteController.comprarEntradas`:
1. Recibe el `CompraRequestDTO`.
2. Lo valida con `@Valid`.
3. Llama a `clienteService.comprarEntradas(dto)`.
4. Devuelve `201 Created`.

No decide si la butaca está libre.

**¿Qué diferencia hay entre Controller y Service?**
- El controller **traduce** HTTP a Java y de vuelta: sabe de rutas, verbos, códigos de estado y JSON.
- El service **decide**: sabe de reglas y transacciones, y no sabe que existe HTTP. Por eso el mismo `ClienteService` se usa desde un test o se podría usar desde otro controller.

**¿Por qué la View no debería acceder directamente a la base de datos?**
- La View es el front, que corre en el navegador del usuario. Si se conectara a MySQL, las credenciales viajarían al navegador.
- Se saltearían todas las reglas del service: cualquiera podría insertar una entrada en una butaca ocupada.
- Cualquier cambio en una tabla rompería el front.

Por eso la View solo habla con la API por HTTP.

---

## DAO

**¿Qué problema resuelve DAO?**
Separa el *cómo se guardan los datos* del resto del programa. `ClienteService` llama a `ticketCompraRepository.save(ticket)` y no sabe si debajo hay MySQL, otra base o un archivo; tampoco escribe SQL.

**¿Qué responsabilidad tiene?**
Ofrecer operaciones de acceso a datos de una entidad (guardar, buscar, borrar, consultas específicas) y esconder los detalles de la base. Por ejemplo:
- `AsientoRepository.countBySalaId`
- `EntradaRepository.findAsientoIdsByFuncionIdAndEstado`

**¿Qué ocurriría si elimináramos esta capa?**
Los services tendrían que abrir conexiones, escribir SQL a mano y convertir cada fila del `ResultSet` en objetos. El SQL quedaría repetido y mezclado con las reglas, y para testear un service haría falta una base real. Hoy, en cambio, se mockea el repositorio.

**¿DAO y ORM son lo mismo?** (diferencia importante)
**No.**
- **DAO es un patrón de diseño**: una idea de *cómo organizar el código*. Una capa con operaciones como `save` o `findById` que oculta el acceso a datos. Se puede implementar de muchas formas, incluso con JDBC puro y SQL a mano, sin ningún ORM.
- **ORM es una técnica/herramienta**: el *mecanismo* que convierte objetos en filas y filas en objetos. En este proyecto es **Hibernate**, que lee `@Entity`, `@Column` y `@ManyToOne` y genera el SQL.

En este proyecto se usan juntos: `FuncionRepository` **es** el DAO (la interfaz que usa el service). Spring Data JPA genera su implementación, y esa implementación usa el ORM (Hibernate) para hablar con MySQL.
Una analogía: el DAO es *el mozo* al que le pedís el plato; el ORM es *la cocina* que lo prepara. Podría haber un mozo con otra cocina (JDBC a mano) o una cocina sin mozo (usar el `EntityManager` directo desde el controller, que sería una mala práctica).

---

## ORM

**¿Qué significa Object-Relational Mapping?**
Mapeo objeto-relacional: traducir automáticamente entre el mundo de los **objetos** (clases Java con atributos y referencias a otros objetos) y el mundo **relacional** (tablas con columnas y claves foráneas). Escribimos `entrada.getAsiento()` y Hibernate hace el `SELECT` por `asiento_id`.

**¿Qué relación existe entre una Entity y una tabla?**
Cada clase `@Entity` es una tabla, cada atributo es una columna y cada objeto es una fila. Por ejemplo:
- `Pelicula` ↔ tabla `pelicula`.
- El atributo `fechaEstreno` ↔ columna `fecha_estreno` (por `@Column(name = "fecha_estreno")`).
- Un `new Pelicula()` guardado ↔ un `INSERT`.

Con `ddl-auto=validate`, Hibernate verifica al arrancar que esa correspondencia sea cierta.

**¿Qué representa una relación `@OneToMany`?**
Que un objeto tiene **muchos** de otro. En `TicketCompra`:
```java
@OneToMany(mappedBy = "ticketCompra", cascade = CascadeType.ALL)
private List<Entrada> entradas;
```
Una compra tiene varias entradas. En la base no hay una "lista": cada fila de `entrada` tiene la FK `ticket_compra_id`. `mappedBy` indica que esa FK está mapeada del otro lado, en `Entrada.ticketCompra` (`@ManyToOne`).

**¿Qué ventajas tiene utilizar ORM?**
- No se escribe SQL para el CRUD.
- Se trabaja con objetos y tipos de Java (`LocalDate`, `BigDecimal`, enums).
- Se cambia de motor más fácilmente (dialectos).
- Hay *dirty checking*: en `cancelarCompra` solo cambiamos el estado y Hibernate hace el `UPDATE` al commit.
- Las consultas están parametrizadas, así que no hay inyección SQL.
- Se integra con las transacciones de Spring.

**¿Qué problemas puede generar una mala utilización del ORM?**
- **N+1:** listar 6 funciones y, para mostrar el nombre de la película, hacer 1 consulta de funciones + 6 de películas. Lo evitamos con `join fetch` en `FuncionRepository.buscar` y `@EntityGraph` en el historial. La versión original hacía un `exists` por cada butaca para ver la disponibilidad; ahora es una sola consulta (`findAsientoIdsByFuncionIdAndEstado`).
- **Lazy loading fuera de la transacción:** acceder a `funcion.getPelicula()` en el controller, con la sesión cerrada, tira `LazyInitializationException`. Lo evitamos armando los DTOs dentro del service (`open-in-view=false`).
- **Cascadas peligrosas:** un `cascade = REMOVE` en `Sala → Asientos` borraría todas las butacas al borrar la sala. Solo usamos cascada donde el hijo no existe sin el padre (`TicketCompra.entradas`).
- **Exponer entidades:** devolver `Usuario` en el JSON mostraría la password; con relaciones bidireccionales, Jackson entra en un bucle infinito. Por eso se usan DTOs.
- **EAGER por defecto:** traer todo siempre. Por eso todas las relaciones son `LAZY`.

---

## SQL

**¿Por qué esta tabla tiene esa clave primaria?**
Por ejemplo, `asiento.id INT AUTO_INCREMENT`: es una clave **subrogada**. Un asiento no tiene un dato propio único: "A5" existe en cada sala. Podríamos usar una PK compuesta `(sala_id, fila_asiento, numero_asiento)`, pero cada FK que apunte a una butaca (como `entrada.asiento_id`) necesitaría tres columnas. Con un `id` numérico la FK es una sola columna chica que nunca cambia. La unicidad real del negocio se mantiene con `UNIQUE (sala_id, fila_asiento, numero_asiento)`. Lo mismo en `usuarios`: el mail puede cambiar, entonces no es PK sino `UNIQUE`.

**¿Qué relación existe entre estas tablas?** (ejemplo: `funcion`, `ticket_compra`, `entrada`, `asiento`)
- `funcion` 1 → N `ticket_compra`, por `ticket_compra.funcion_id`.
- `ticket_compra` 1 → N `entrada`, por `entrada.ticket_compra_id`.
- `asiento` 1 → N `entrada`, por `entrada.asiento_id`.

Así, "las butacas ocupadas de la función 1" son las entradas OCUPADAS de los tickets de la función 1:
```sql
SELECT e.asiento_id FROM entrada e JOIN ticket_compra t ON t.id = e.ticket_compra_id
WHERE t.funcion_id = 1 AND e.estado = 'OCUPADA';
```

**¿Qué sucede cuando eliminás este registro?**
Depende del `ON DELETE` de la FK y de la regla del service (tabla completa en [MODELO_DATOS.md](MODELO_DATOS.md#4-fetch-cascadas-y-borrado)). Por ejemplo, `DELETE /api/salas/1`:
1. `SalaService.eliminarSala` ve que tiene butacas y responde **409**.
2. Aunque no lo viera, la FK `fk_asiento_sala ... ON DELETE RESTRICT` haría que MySQL rechazara el `DELETE`.

En cambio, `DELETE /api/usuarios/{id}` de un usuario sin compras borra también su membresía, por `ON DELETE CASCADE`.

**¿Dónde se está realizando realmente la persistencia?**
En **MySQL**. Java solo arma objetos. Hibernate traduce `save` a `INSERT`, el driver `mysql-connector-j` lo envía por TCP al puerto 3306, y el dato queda guardado recién en el **COMMIT**, que hace Spring al terminar el método `@Transactional`. Si la app se cae antes del commit, no se guarda nada.

---

## HTTP / API

**¿Qué diferencia hay entre GET y POST?**
- **GET** pide datos y no cambia nada en el servidor (`GET /api/funciones/1/butacas`). No lleva body, los parámetros van en la URL (`?peliculaId=1`). Es *idempotente*: llamarlo 10 veces da lo mismo.
- **POST** envía datos para **crear** algo o ejecutar una acción (`POST /api/clientes/compras`). Lleva body JSON. No es idempotente: dos POST iguales crean dos compras (por eso la segunda da 409 si la butaca ya se vendió).

**¿Por qué utilizaron este endpoint?** (ejemplo: `PATCH /api/clientes/compras/{id}/cancelar`)
- Las rutas son **sustantivos en plural** (`compras`) y la acción la da el verbo.
- Cancelar no borra la compra (queda en el historial), así que no es `DELETE`.
- Tampoco reemplaza el recurso entero, así que no es `PUT`.
- Es un cambio **parcial** (solo el estado), y para eso está `PATCH`.
- Se mantuvo el prefijo `/api/clientes` porque el front ya usaba `POST /api/clientes/compras`.

**¿Qué significa un HTTP 404?**
"Not Found": lo que pediste no existe. En esta API aparece en dos casos:
- Un id que no está en la base (`GET /api/peliculas/999` → `"No existe la pelicula con id 999"`, por `ResourceNotFoundException`).
- Una ruta que no existe (`GET /api/no-existe`).

No hay que confundirlo con el 409, que significa "existe, pero la regla no te deja", ni con el 400, que significa "lo que mandaste está mal formado".

**¿Qué información viaja en el request?**
- La **línea de inicio**: método + ruta + versión (`POST /api/clientes/compras HTTP/1.1`).
- Los **headers**: `Host`, `Content-Type: application/json`, `Origin` (lo usa CORS).
- El **body** con el JSON: `{"usuarioId":1,"funcionId":1,"asientoIds":[1,2]}`.
- En los GET, los parámetros van en la ruta (`/funciones/1`) o en la query string (`?fecha=2026-10-28`).

**¿Qué devuelve el servidor?**
- Un **código de estado** (201).
- **Headers** (`Content-Type: application/json`).
- Un **body JSON**: el recurso (el `TicketCompraResponseDTO` con sus entradas y el total) o un `ErrorResponseDTO` con `status`, `message`, `path` y `detalles`.

**¿Qué formato usamos (JSON) y por qué no otro (XML, etc.)?**
JSON porque:
- Es el formato nativo de JavaScript, así que el front lo usa directo con `response.json()`.
- Es más liviano y legible que XML (no repite etiquetas de apertura y cierre).
- Spring Boot lo trae configurado con Jackson.
- Es el estándar de facto de las APIs REST.

XML sería válido, pero más verboso y necesitaría otra librería en el front. Formatos binarios como Protocol Buffers son más rápidos, pero no se pueden leer ni probar a mano con Postman.

---

## Diseño

**¿Qué patrón están utilizando?**
Varios (detalle en [ARQUITECTURA.md §6](ARQUITECTURA.md#6-patrones-de-diseño-usados)):
- **Arquitectura en capas / MVC.**
- **DAO/Repository** (`*Repository`).
- **DTO** (`*RequestDTO`, `*ResponseDTO`).
- **Service Layer** (`*Service` + `I*Service`).
- **Mapper** (`*Mapper`).
- **Inyección de dependencias por constructor.**
- **Singleton**: los beans de Spring.

**¿Por qué eligieron ese patrón? ¿Qué problema soluciona?** (ejemplo: DTO)
Sin DTO, `GET /api/usuarios` devolvería la entidad `Usuario` con su password. Además, la compra devolvía `TicketCompra` con el `Usuario`, la `Entrada`, el `Asiento` y la `Sala` anidados: mucho más de lo que el front necesita y frágil ante cambios en la base. El DTO define exactamente qué entra (con sus validaciones) y qué sale, y desacopla el JSON de las tablas.

**¿Podrían haberlo hecho de otra manera?**
Sí:
- Devolver entidades y ocultar campos con `@JsonIgnore`. Mezcla persistencia con presentación y es fácil olvidarse un campo.
- Generar los mappers con MapStruct. Es una dependencia fuera del stack permitido.
- Usar `record` de Java para los DTOs. Es válido, pero elegimos Lombok porque lo vimos en la cursada.

**¿Qué ventaja aporta esta decisión?**
- Seguridad (no se filtra la password).
- Estabilidad del contrato: puedo cambiar una tabla sin romper el front.
- Validaciones claras en un solo lugar.
- No hay recursión infinita ni `LazyInitializationException`.

---

## El bloqueo pesimista, explicado simple

**¿Qué problema resuelve?**
Que **dos personas compren la misma butaca al mismo tiempo**. Sin bloqueo, el chequeo "¿está libre?" y el "guardar" son dos pasos separados, y entre uno y otro puede meterse otra compra.

**Ejemplo sin bloqueo:**

| Momento | Ana (request 1) | Bruno (request 2) |
|---|---|---|
| 1 | Pregunta: ¿la A5 de la función 1 está libre? → **Sí** | |
| 2 | | Pregunta: ¿la A5 de la función 1 está libre? → **Sí** (Ana todavía no guardó) |
| 3 | Guarda su entrada A5 ✅ | |
| 4 | | Guarda su entrada A5 ✅ → **la butaca se vendió dos veces** ❌ |

**Ejemplo con bloqueo (lo que hace el proyecto):**
`ClienteService.comprarEntradas` busca la función con `FuncionRepository.findByIdParaCompra`, que tiene `@Lock(PESSIMISTIC_WRITE)`. Eso genera `SELECT ... FROM funcion WHERE id = 1 FOR UPDATE`: MySQL **bloquea la fila de esa función** hasta que termina la transacción.

| Momento | Ana | Bruno |
|---|---|---|
| 1 | Bloquea la función 1 y pregunta: ¿A5 libre? → Sí | |
| 2 | | Quiere bloquear la función 1 → **espera** ⏳ |
| 3 | Guarda la A5 y hace COMMIT → libera el bloqueo | |
| 4 | | Ahora sí bloquea, pregunta: ¿A5 libre? → **No** → 409 "La butaca A5 ya esta ocupada" |

Es como una sola caja para cobrar las entradas de cada función: los clientes de **la misma función** pasan de a uno. Los de **otras funciones** no esperan, porque cada función es otra fila.
Lo probamos en vivo: 5 compras simultáneas de la misma butaca dieron un 201 y cuatro 409.

**¿Qué alternativas había?**
1. **Restricción `UNIQUE` en la base** sobre (función, butaca). Es la más robusta, pero acá no se puede directamente: una butaca puede tener varias entradas para la misma función si las anteriores se **cancelaron**, y MySQL no tiene índices únicos "solo para las filas OCUPADAS" (se podría con una columna calculada, pero es más difícil de explicar).
2. **Bloqueo optimista** (`@Version` en la entidad): no bloquea a nadie y, al guardar, verifica que nadie haya modificado el registro mientras tanto. Si alguien lo hizo, falla y el cliente reintenta. Es mejor cuando los conflictos son raros. Además habría que "versionar" algo que cambie con cada compra, como la función, y manejar la excepción para devolver 409.
3. **`synchronized` en Java:** solo funciona si hay **un solo** servidor. Con dos instancias de la app no sirve, porque el bloqueo de la base sí lo comparten todas.

Elegimos el pesimista porque es simple, lo garantiza la propia base y en un cine es esperable que muchos compren a la vez para un estreno.

---

## 10 preguntas extra probables (modificar, extender, justificar)

**1. ¿Qué pasa si dos personas compran la misma butaca a la vez?**
Ver la sección anterior: la segunda espera el bloqueo de la función y recibe 409. La protección depende del bloqueo: no hay una restricción `UNIQUE` en la base que lo respalde, por lo explicado en "alternativas".

**2. ¿Cómo agregarías un descuento del 10% para socios GOLD?**
En `ClienteService.comprarEntradas`, después de calcular el total:
1. Consultar `membresiaRepository.findByUsuarioId(usuario.getId())`.
2. Si es `GOLD`, hacer `total = total.multiply(new BigDecimal("0.90"))`.

Es una regla de negocio, así que va en el service; no se toca el controller ni la base. Si el porcentaje cambia seguido, convendría guardarlo en una tabla. Y se agregaría un test en `ClienteServiceTest`.

**3. ¿Cómo agregarías una entidad `Genero` en lugar del String `genero`?**
1. Crear la tabla `genero (id, nombre UNIQUE)` en `01_schema.sql`.
2. Cambiar `pelicula.genero` por `genero_id` con FK.
3. Crear la entidad `Genero` y `GeneroRepository`.
4. En `Pelicula`, poner `@ManyToOne(fetch = LAZY) @JoinColumn(name = "genero_id") private Genero genero;`.
5. En `PeliculaRequestDTO`, cambiar `genero` por `generoId`.
6. En el service, buscar el género (404 si no existe).
7. Registrar el cambio de contrato en `CAMBIOS_API.md`.

**4. ¿Cómo permitirías cancelar solo hasta 1 hora antes de la función?**
En `ClienteService.cancelarCompra`, cambiar el chequeo por:
```java
if (!ticket.getFuncion().getInicio().minusHours(1).isAfter(LocalDateTime.now())) throw new BusinessException(...);
```
Un solo lugar, más un test.

**5. ¿Cómo agregarías autenticación real?**
Con Spring Security: hashear las passwords con `BCryptPasswordEncoder` al registrar y comparar con `matches` en `iniciarSesion`, emitir una sesión o un token al hacer login, y proteger las rutas de administración por rol (`ADMINISTRADOR`). Hoy no está porque la Etapa 1 no lo pide y la consigna limita el stack.

**6. ¿Por qué `total` se guarda si se puede calcular?**
Porque es el **importe que se cobró**. Si mañana cambia el precio de la función, el ticket tiene que seguir mostrando lo que pagó el cliente. Por la misma razón cada `Entrada` guarda su `precioUnitario`.

**7. ¿Qué pasa si se apaga el servidor en medio de una compra?**
La compra se hace en una transacción (`@Transactional`). Si no llegó al COMMIT, MySQL descarta todo: no queda un ticket sin entradas ni entradas sin ticket. Si llegó, la compra está completa.

**8. ¿Cómo listarías las funciones de hoy de una película?**
Ya existe: `GET /api/funciones?peliculaId=1&fecha=2026-10-28`. La consulta `FuncionRepository.buscar` aplica cada filtro solo si el parámetro no es `null`.

**9. Si agregás un atributo a una entidad y no al script, ¿qué pasa?**
La aplicación no arranca: `ddl-auto=validate` detecta que falta la columna (`Schema-validation: missing column`). El test `contextLoads` también falla. Hay que agregar la columna en `01_schema.sql` (y en un sistema con datos, con un `ALTER TABLE`). Es intencional: el modelo SQL se maneja desde MySQL.

**10. ¿Cómo agregarías la venta de "combos" (pochoclos) en la misma compra?**
1. Crear la tabla y entidad `Producto` (nombre, precio).
2. Crear `ItemCompra` (`ticket_compra_id`, `producto_id`, cantidad, precio_unitario), con `@ManyToOne` a ticket y a producto.
3. En `TicketCompra`, agregar un `@OneToMany(mappedBy = "ticketCompra", cascade = ALL) List<ItemCompra> items`.
4. En `CompraRequestDTO`, agregar `List<ItemDTO> items`.
5. En `ClienteService.comprarEntradas`, sumar los items al total, dentro de la misma transacción.

Las capas existentes no cambian de forma: se extienden.
