# GestionDeCine — API REST (Etapa 1: Back-end y Persistencia)

TPO de Aplicaciones Interactivas (UADE). Back-end de un sistema de gestión de
cine (catálogo de películas, salas, funciones, compra de entradas) construido
con **Spring Boot + Spring Data JPA + SQL**, expuesto como una **API REST**
sobre HTTP, pensado para que un front-end (Etapa 2) lo consuma como cliente
remoto.

> Este documento explica qué hace cada capa, por qué se tomó cada decisión de
> diseño y cómo correr y probar el proyecto. Está pensado para que cualquier
> integrante del grupo pueda defenderlo en la evaluación individual.

---

## 1. Cómo correr el proyecto

Requisitos: **JDK 21+**. Maven no hace falta instalarlo: se usa el wrapper
incluido (`./mvnw` en Linux/Mac, `mvnw.cmd` en Windows).

### Compilar y correr los tests (no necesita MySQL)

```bash
./mvnw clean package
```

Compila el proyecto y corre los tests. Los tests usan una base **H2 en
memoria** (ver `src/test/resources/application.properties`), así que esto
funciona en cualquier máquina aunque no tenga MySQL instalado.

### Levantar la API contra MySQL (configuración por defecto)

1. Tener **MySQL Server 8** instalado y corriendo en `localhost:3306`.
2. Usuario/password por defecto: `root` / `admin` (como en las clases). Si tu
   MySQL tiene otra password, cambiala en `src/main/resources/application.properties`
   o pasala por variable de entorno sin tocar el archivo
   (`DB_USERNAME`, `DB_PASSWORD`; también `DB_HOST`, `DB_PORT`, `DB_NAME`).
3. Levantar:
   ```bash
   ./mvnw spring-boot:run
   ```
   o desde el IDE, ejecutando `GestionDeCineApplication`.

**No hace falta crear la base ni las tablas a mano**:
- `createDatabaseIfNotExist=true` (en la URL de conexión) crea la base
  `gestioncine` si no existe.
- `spring.jpa.hibernate.ddl-auto=update` hace que Hibernate cree/actualice
  las tablas a partir de las `@Entity` cada vez que arranca la app.

El servidor queda escuchando en `http://localhost:8080`.

> Si al arrancar aparece `Public Key Retrieval is not allowed`, falta el
> parámetro `allowPublicKeyRetrieval=true` en la URL (ya está incluido). Si
> aparece `Access denied for user 'root'`, la password configurada no es la
> de tu MySQL. Si aparece `Communications link failure`, MySQL no está
> corriendo.

### Levantar la API sin MySQL (perfil `h2`)

Para probar los endpoints en una máquina sin MySQL:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

Usa **H2**, una base SQL embebida que se guarda en `./data/gestioncine.mv.db`.
Consola web para ver las tablas: `http://localhost:8080/h2-console`
(JDBC URL `jdbc:h2:file:./data/gestioncine`, user `sa`, password vacía).

Que se pueda pasar de MySQL a H2 cambiando **solo configuración**
(`application*.properties`), sin tocar una línea de código de negocio,
demuestra que la arquitectura no depende de un motor de base de datos en
particular: Spring Data JPA genera el SQL específico de cada motor a través
del *dialecto* de Hibernate.

### Probar los endpoints

Abrir `src/main/java/com/example/GestionDeCine/request.http` con la extensión
**REST Client** de VS Code (o IntelliJ, o copiar cada request a Postman).
Incluye casos exitosos y casos de error para cada operación
(GET/POST/PUT/DELETE). Ejecutados en orden sobre una base vacía, todos dan el
código de estado que dice su comentario.

---

## 2. Arquitectura general

```
Cliente HTTP (Postman / futuro front-end)
        │  JSON sobre HTTP
        ▼
┌───────────────────┐
│    controller/     │  Traduce HTTP <-> objetos Java. Sin lógica de negocio.
└─────────┬──────────┘
          │ llama a
          ▼
┌───────────────────┐
│  service/           │  Lógica de negocio y reglas de validación de dominio.
│  service/interfaces/│  (interfaces, para desacoplar el contrato de la impl.)
└─────────┬──────────┘
          │ usa
          ▼
┌───────────────────┐
│   repository/       │  Acceso a datos (Spring Data JPA / patrón DAO).
└─────────┬──────────┘
          │ SQL generado por Hibernate
          ▼
┌───────────────────┐
│   Base de datos SQL │  MySQL (por defecto) o H2 (perfil "h2" y tests)
└───────────────────┘
```

Capas adicionales, transversales a las de arriba:

- **`model/`**: las entidades del dominio (`@Entity`), mapeadas 1 a 1 (o a las
  relaciones que corresponda) con tablas SQL vía JPA/Hibernate (ORM).
- **`dto/`**: objetos "planos" que viajan en el body de ciertos requests/
  responses cuando conviene que no coincidan exactamente con una entidad
  (ver sección 5).
- **`exception/`**: excepciones de dominio (`ResourceNotFoundException`,
  `BusinessException`, `AutenticacionException`) y un `GlobalExceptionHandler`
  que centraliza la traducción de esas excepciones a códigos HTTP.

### ¿Por qué se dividió el proyecto en estas capas?

Cada capa tiene **una sola razón para cambiar** (principio de responsabilidad
única):

- Si cambia el motor de base de datos → se toca `repository/` (y
  `application.properties`), nunca `controller/` ni `service/`.
- Si cambia una regla de negocio (p. ej. cómo se calcula si un asiento está
  disponible) → se toca `service/`, sin tocar `controller/`.
- Si cambia el formato de entrada/salida de la API (p. ej. pasar de JSON a
  otro formato, o cambiar qué campos se exponen) → se toca `controller/`
  y/o `dto/`, sin tocar la lógica de negocio.

### ¿Qué pasaría si pusiéramos toda la lógica en el Controller?

El Controller quedaría acoplado a HTTP: la única forma de reutilizar esa
lógica sería haciendo un request HTTP a uno mismo. Además se mezclarían dos
responsabilidades distintas (parsear/validar HTTP vs. reglas de negocio),
lo que hace el código más difícil de testear (no se puede probar la regla de
negocio sin levantar un servidor HTTP) y de mantener. Por eso en este
proyecto los controllers son "delgados": arman la respuesta HTTP a partir de
lo que devuelve el Service, y nada más. Un ejemplo directo de esto es
`ClienteService.comprarEntrada(...)`: contiene **toda** la regla de venta de
una entrada (validar existencia, validar que el asiento sea de la sala
correcta, validar que no esté ocupado, crear la Entrada y el TicketCompra);
`ClienteController.comprarEntrada(...)` solo llama a ese método y arma el
`ResponseEntity`.

### MVC / capas: ¿dónde está cada cosa?

- **Model**: paquete `model/` (entidades JPA).
- **Controller**: paquete `controller/` (clases `@RestController`).
- **Service** (la "lógica" que en un MVC clásico a veces se mete a presión en
  el Controller): paquete `service/`. Es la capa intermedia que el Controller
  no debería saltear nunca, porque ahí es donde vive la regla de negocio y
  las validaciones de dominio (no las de formato, esas las hace Bean
  Validation antes de llegar al Controller — ver sección 6).
- No hay una "View" en el sentido clásico de MVC porque esto es una API REST:
  la "vista" es el JSON que arma Jackson automáticamente a partir de los
  objetos que devuelve el Controller. Por eso tampoco tiene sentido que algo
  le pida datos a la base directamente: todo pasa por Service → Repository.

---

## 3. Entidades del dominio (`model/`)

| Entidad | Representa | Relaciones |
|---|---|---|
| `Usuario` | Una persona con acceso al sistema | — (ver diseño de Membresía abajo) |
| `Membresia` | El programa de puntos/categoría de un socio | `@OneToOne` → `Usuario` (dueña de la FK `usuario_id`) |
| `Pelicula` | Una película del catálogo | — |
| `Sala` | Una sala física del cine | — |
| `Asiento` | Un asiento físico dentro de una sala | `@ManyToOne` → `Sala` |
| `Funcion` | Una proyección puntual: película + sala + fecha/hora + formato + precio | `@ManyToOne` → `Pelicula`, `@ManyToOne` → `Sala` |
| `Entrada` | La ocupación de un asiento para una función puntual | `@ManyToOne` → `Funcion`, `@ManyToOne` → `Asiento` |
| `TicketCompra` | El comprobante de que un Usuario pagó una Entrada | `@ManyToOne` → `Usuario`, `@OneToOne` → `Entrada` |

### Decisión de diseño: relaciones unidireccionales

Todas las relaciones son **unidireccionales**, desde el lado "muchos" (o el
lado que tiene la foreign key) hacia el lado "uno". Por ejemplo, `Asiento`
tiene un campo `Sala sala`, pero `Sala` **no** tiene una lista de `Asiento`.

Esto es intencional, no un olvido: con Lombok (`@Data` genera automáticamente
`equals`, `hashCode` y `toString`) y con la serialización JSON de Jackson,
una relación **bidireccional** (por ejemplo `Sala` con una `List<Asiento>` y
cada `Asiento` con un campo `Sala`) genera referencias circulares: llamar a
`sala.toString()` llamaría a `asiento.toString()`, que a su vez llamaría a
`sala.toString()`, en un loop infinito (`StackOverflowError`), y lo mismo le
pasaría a Jackson al intentar convertir el objeto a JSON. Evitarlo desde el
diseño (en vez de parchear después con `@JsonIgnore` / `@ToString.Exclude` en
cada entidad) simplifica el modelo y evita esta clase de bug.

Para obtener "los asientos de una sala" no se navega `sala.getAsientos()`
(no existe ese campo): se consulta el repositorio correspondiente,
`AsientoRepository.findBySalaId(salaId)` (ver `SalaController.obtenerAsientosDeLaSala`).
Lo mismo aplica para `Usuario`/`Membresia`: no existe `usuario.getMembresia()`,
se consulta `MembresiaRepository.findByUsuarioId(usuarioId)`.

### ¿Qué significa Object-Relational Mapping (ORM) acá?

Cada clase anotada `@Entity` (por ejemplo `Pelicula`) se corresponde con una
tabla SQL (`pelicula`), y cada atributo de la clase con una columna. Hibernate
(la implementación de JPA que usa Spring Data JPA) es el que traduce:

- `peliculaRepository.save(pelicula)` → `INSERT INTO pelicula (...) VALUES (...)`
- `peliculaRepository.findById(id)` → `SELECT * FROM pelicula WHERE id = ?`
- un campo `@ManyToOne private Sala sala;` con `@JoinColumn(name = "sala_id")`
  → una columna `sala_id` en la tabla, con foreign key hacia `sala.id`.

`spring.jpa.hibernate.ddl-auto=update` (ver `application.properties`) le dice
a Hibernate que **cree o actualice automáticamente** las tablas a partir de
las entidades, cada vez que arranca la aplicación. Es muy cómodo para el
desarrollo de la materia; en un sistema productivo real se reemplazaría por
migraciones versionadas (Flyway/Liquibase) para tener control explícito de
cada cambio de esquema.

### ¿Qué problema resuelve el patrón DAO / Spring Data JPA?

El patrón DAO (Data Access Object) aísla el "cómo se accede a los datos" del
resto de la aplicación: el resto del código no sabe (ni le importa) si detrás
hay SQL, un archivo, o una API externa. `repository/` es esa capa. Spring
Data JPA además evita escribir la implementación a mano: con solo declarar
una interfaz que extiende `JpaRepository<Entidad, TipoDeId>`, en tiempo de
ejecución Spring genera un proxy con la implementación de `save`, `findById`,
`findAll`, `deleteById`, etc. Los métodos personalizados (por ejemplo
`UsuarioRepository.findByMail(String mail)`) se implementan solos a partir
del **nombre del método**: Spring Data lo interpreta ("find by mail" →
`SELECT * FROM usuarios WHERE mail = ?`) y arma la consulta.

`DAO` y `ORM` no son lo mismo: el ORM (Hibernate) es el motor que mapea
objetos Java a filas SQL; DAO es un patrón de diseño (una capa que expone
operaciones de persistencia sin exponer detalles de implementación). Spring
Data JPA es una herramienta que usa un ORM (Hibernate) por debajo para
generar automáticamente la implementación del patrón DAO.

### Validación (Bean Validation) sobre las entidades

Los campos de las entidades tienen anotaciones como `@NotBlank`, `@NotNull`,
`@Email`, `@Positive`, `@Past`. Estas se evalúan cuando el Controller recibe
el body con `@Valid` (ver `UsuarioController.crearUsuario`, por ejemplo). Si
algo no cumple, Spring lanza `MethodArgumentNotValidException`, que el
`GlobalExceptionHandler` traduce a un `400 Bad Request` con el detalle de
qué campo falló y por qué (ver sección 6).

---

## 4. Reglas de negocio (`service/`)

La regla de negocio más importante del sistema es
**`ClienteService.comprarEntrada(usuarioId, funcionId, asientoId)`**, porque
es donde se garantiza la consistencia del "e-commerce" (nunca se vende dos
veces el mismo asiento para la misma función). Pasos, en orden:

1. Buscar `Usuario`, `Funcion` y `Asiento` por id. Si alguno no existe →
   `ResourceNotFoundException` (HTTP 404).
2. Validar que el `Asiento` pertenezca a la `Sala` de esa `Funcion` (no
   tendría sentido vender el asiento de una sala para una función que se
   proyecta en otra). Si no → `BusinessException` (HTTP 409).
3. Validar que no exista ya una `Entrada` con estado `OCUPADA` para esa
   combinación función+asiento (`EntradaRepository.existsByFuncionIdAndAsientoIdAndEstado`).
   Si ya está ocupado → `BusinessException` (HTTP 409).
4. Recién ahí se crea la `Entrada` (estado `OCUPADA`) y el `TicketCompra`
   asociado, copiando el precio vigente de la `Funcion` en ese momento (para
   que un cambio de precio futuro no altere el historial de compras ya
   hechas).

El método está anotado `@Transactional`: si algo falla después de guardar la
`Entrada` pero antes de terminar de guardar el `TicketCompra`, la
transacción completa se revierte (no queda una `Entrada` "húerfana" sin su
`TicketCompra`).

### ¿Dónde debería estar una regla de negocio, y por qué?

En `service/`, nunca en `controller/` ni en `repository/`. El repositorio
solo sabe hacer consultas; no tiene forma de coordinar varias consultas ni de
decidir "si esto ya está ocupado, no dejes comprarlo". El controller no
debería validar reglas de dominio porque las mismas reglas tendrían que
aplicarse sin importar quién llama al service (HTTP, un test, un futuro
batch job), y ese código no debería depender de estar corriendo dentro de un
request HTTP.

### Otras decisiones de negocio

- **`FuncionService.obtenerAsientosDisponibles(funcionId)`**: en vez de
  "pre-crear" una `Entrada` para cada asiento de cada función (lo que
  generaría filas innecesarias para asientos que nunca se compran), los
  asientos disponibles se calculan al vuelo: todos los asientos de la sala
  de esa función, menos los que ya tengan una `Entrada` `OCUPADA` para esa
  función puntual.
- **`UsuarioService.tienePermiso(usuario, operacion)`**: queda disponible
  como base para un futuro control de autorización por rol (no se conectó a
  un filtro de seguridad todavía — ver sección 7, fuera de alcance de la
  Etapa 1).

---

## 5. DTOs: ¿por qué algunos endpoints no usan la entidad directamente?

La mayoría de los endpoints reciben y devuelven la entidad de JPA
directamente (`Pelicula`, `Sala`, `Asiento`, `Funcion`) porque no tienen
datos sensibles ni necesitan una forma distinta a como se persisten.

`Usuario` es la excepción: tiene un campo `password`. Para no devolverlo
nunca en una respuesta HTTP (ni siquiera en texto plano dentro del JSON), se
definió `UsuarioResponseDTO`, que expone todos los campos de `Usuario`
**excepto** `password`. Todos los endpoints de `UsuarioController` devuelven
este DTO en vez de la entidad `Usuario`.

Además, como un `Usuario` también viaja anidado dentro de otras respuestas
(por ejemplo, en el `TicketCompra` que devuelve la compra o el historial), el
campo `password` de la entidad está anotado con
`@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)`: Jackson lo lee del
body de los requests (alta/modificación) pero nunca lo escribe en un JSON de
respuesta.

También hay DTOs de entrada donde la forma del request no coincide con
ninguna entidad: `LoginRequestDTO` (mail + password) y `CompraRequestDTO`
(usuarioId + funcionId + asientoId, que en el dominio terminan siendo tres
entidades distintas relacionadas entre sí).

---

## 6. Manejo de errores y códigos HTTP (`exception/`)

`GlobalExceptionHandler` (anotado `@RestControllerAdvice`) es el único lugar
del proyecto que decide qué código HTTP corresponde a cada tipo de error, en
vez de repetir `try/catch` en cada Controller:

| Excepción | HTTP | Cuándo se lanza |
|---|---|---|
| `MethodArgumentNotValidException` (de Spring) | 400 Bad Request | El body no pasó las validaciones `@Valid` (`@NotBlank`, `@Email`, etc.) |
| `HttpMessageNotReadableException` (de Spring) | 400 Bad Request | El body no es un JSON válido o tiene un valor con formato incorrecto (fecha mal escrita, enum inexistente) |
| `AutenticacionException` | 401 Unauthorized | Login con mail o password incorrectos |
| `ResourceNotFoundException` | 404 Not Found | Se pidió por id una entidad que no existe |
| `HttpRequestMethodNotSupportedException` (de Spring) | 405 Method Not Allowed | Se usó un verbo HTTP no soportado por ese endpoint |
| `BusinessException` | 409 Conflict | Una regla de negocio impide la operación (asiento ya ocupado, mail duplicado, etc.) |
| `DataIntegrityViolationException` (de Spring) | 409 Conflict | La base rechazó la operación por una restricción SQL (p. ej. borrar una `Sala` que todavía tiene `Asiento`s o `Funcion`es asociadas) |
| `Exception` (cualquier otra) | 500 Internal Server Error | Error no previsto; nunca se filtra el detalle interno al cliente |

Todas las respuestas de error tienen el mismo formato (`ErrorResponseDTO`):
`timestamp`, `status`, `error`, `message`, `path` y, cuando aplica,
`detalles` (lista de errores de validación campo por campo). Esto facilita
mucho el trabajo del front-end en la Etapa 2, que puede manejar cualquier
error de la API con el mismo código.

---

## 7. Endpoints disponibles

Base URL: `http://localhost:8080`

### Usuarios (`/api/usuarios`)
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/usuarios` | Lista todos los usuarios |
| GET | `/api/usuarios/{id}` | Obtiene un usuario por id |
| POST | `/api/usuarios` | Crea un usuario |
| PUT | `/api/usuarios/{id}` | Modifica un usuario |
| DELETE | `/api/usuarios/{id}` | Elimina un usuario |
| POST | `/api/usuarios/login` | Inicia sesión (mail + password) |

### Películas (`/api/peliculas`) — CRUD completo (GET all, GET by id, POST, PUT, DELETE)

### Salas (`/api/salas`) — CRUD completo + `GET /api/salas/{id}/asientos`

### Asientos (`/api/asientos`) — CRUD completo

### Funciones (`/api/funciones`) — CRUD completo + `GET /api/funciones/{id}/asientos-disponibles`

### Clientes / e-commerce (`/api/clientes`)
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/clientes/cartelera` | Catálogo de películas |
| POST | `/api/clientes/compras` | Compra una entrada (body: `CompraRequestDTO`) |
| GET | `/api/clientes/{usuarioId}/historial` | Historial de compras del usuario |
| GET | `/api/clientes/{usuarioId}/membresia` | Membresía del usuario |

### Membresías (`/api/membresias`)
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/membresias` | Da de alta una membresía para un usuario |
| DELETE | `/api/membresias/{id}` | Elimina una membresía |

Casos de uso completos (exitosos y con error) para cada uno de estos
endpoints están en `src/main/java/com/example/GestionDeCine/request.http`.

---

## 8. Qué queda deliberadamente fuera de la Etapa 1

- **Autenticación con tokens (JWT/sesión) y autorización por rol vía
  Spring Security**: el enunciado de la Etapa 1 no pide seguridad de
  acceso, solo login básico y una capa de negocio (`tienePermiso`) que ya
  modela qué puede hacer cada rol. Conectar eso a un filtro real de
  seguridad queda como una extensión natural, no como parte de este
  entregable.
- **Hash de contraseñas**: por el mismo motivo, `Usuario.password` se guarda
  en texto plano. En un sistema real se usaría `BCryptPasswordEncoder` de
  Spring Security antes de persistir y para comparar en el login.
- **Front-end**: es el objetivo de la Etapa 2. Este back-end ya cumple la
  condición de no depender de una ejecución local o monolítica: el cliente
  (hoy, herramientas de prueba HTTP; en la Etapa 2, el front-end en
  HTML/CSS/JS) se comunica exclusivamente por HTTP contra
  `http://localhost:8080` (o el host donde se despliegue el servidor), nunca
  accediendo directo a la base de datos.

---

## 9. Estructura de paquetes

```
com.example.GestionDeCine
├── GestionDeCineApplication.java   Punto de entrada (main)
├── controller/                     Traduce HTTP <-> Java. Sin lógica de negocio.
├── dto/                            Objetos de transporte para requests/responses
├── exception/                      Excepciones de dominio + manejo global de errores
├── model/                          Entidades JPA (mapeadas a tablas SQL)
├── repository/                     Interfaces Spring Data JPA (capa DAO)
└── service/
    ├── interfaces/                 Contratos de la lógica de negocio
    └── *.java                      Implementaciones de la lógica de negocio
```
