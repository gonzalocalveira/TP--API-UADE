# GestionDeCine: API REST (Etapa 1: back-end y persistencia)

TPO de **Aplicaciones Interactivas (UADE, 2° cuatrimestre 2026)**. Back-end de un sistema de gestión de cine con funcionalidad de e-commerce: catálogo de películas, salas y butacas, programación de funciones, compra y cancelación de entradas, y programa de socios.

Stack: **Java 25 + Spring Boot 4.1 (Web MVC, Data JPA, Validation) + MySQL 8**, más Lombok.
Arquitectura cliente-servidor: `Front-end → HTTP/JSON → API Spring → JDBC → MySQL`.

## Documentación

| Documento | Contenido |
|---|---|
| [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md) | Capas, recorrido de un request, responsabilidad de cada clase, MVC, patrones, cómo mover el servidor |
| [docs/MODELO_DATOS.md](docs/MODELO_DATOS.md) | Diagrama ER, PK, relaciones JPA, borrados, cascadas, fetch, `validate` vs `update` vs `create` |
| [docs/ENDPOINTS.md](docs/ENDPOINTS.md) | Cada endpoint con body, respuesta y códigos de estado |
| [docs/REGLAS_NEGOCIO.md](docs/REGLAS_NEGOCIO.md) | Cada regla, dónde está implementada y qué error devuelve |
| [docs/ANOTACIONES_Y_SINTAXIS.md](docs/ANOTACIONES_Y_SINTAXIS.md) | Glosario de anotaciones (Spring, JPA, Validation, Lombok) con ejemplos del código |
| [docs/PREGUNTAS_DEFENSA.md](docs/PREGUNTAS_DEFENSA.md) | Respuestas a las preguntas guía de la cátedra + 10 preguntas extra |
| [docs/CAMBIOS_API.md](docs/CAMBIOS_API.md) | Cambios de contrato respecto de la versión anterior (para el front) |
| [docs/AUDITORIA.md](docs/AUDITORIA.md) | Diagnóstico inicial y estado final de cada requisito de la rúbrica |
| [database/README.md](database/README.md) | Cómo crear la base con los scripts (Workbench o consola) |
| [docs/cine-api.postman_collection.json](docs/cine-api.postman_collection.json) | Colección Postman con 68 requests (variable `baseUrl`) |

## Requisitos

- **JDK 25** (`java -version`). Maven no hace falta: se usa el wrapper `mvnw` / `mvnw.cmd`.
- **MySQL Server 8** corriendo (por defecto en `localhost:3306`).

## 1. Crear la base de datos

La base **no la crea Spring**: se crea con los scripts de `database/` (la app usa `ddl-auto=validate`, que solo verifica). Ver [database/README.md](database/README.md).
1. En MySQL Workbench: **File → Open SQL Script → `database/01_schema.sql` → ejecutar ⚡**.
2. Lo mismo con **`database/02_datos.sql`**.

Correr `01` de nuevo **resetea** la base. Conviene correr 01 + 02 el día de la demo, porque las funciones de prueba tienen fechas relativas a "hoy".

## 2. Configurar las variables de entorno

Todo lo que cambia entre equipos se lee de variables de entorno (lista completa en `src/main/resources/application.properties.example`):

| Variable | Default | Para qué |
|---|---|---|
| `DB_PASSWORD` | *(vacía)* | Password de MySQL. **No está en el repo**: hay que definirla |
| `DB_USERNAME` | `root` | Usuario de MySQL |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `3306` / `gestioncine` | Dónde está la base |
| `PORT` | `8080` | Puerto de la API |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000,http://127.0.0.1:5500` | URLs del front que pueden llamar a la API |
| `SHOW_SQL` | `true` | Mostrar en consola el SQL que genera Hibernate |

Windows (permanente): *Configuración → Sistema → Variables de entorno*, o en PowerShell `setx DB_PASSWORD "miPassword"` (y abrir una terminal nueva).

## 3. Ejecutar

```bash
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```
O desde el IDE, ejecutando `GestionDeCineApplication`. Cuando aparece `Started GestionDeCineApplication`, la API responde en **http://localhost:8080** (por ejemplo, `http://localhost:8080/api/peliculas`).

Errores comunes al arrancar:
- `Access denied for user 'root'`: falta `DB_PASSWORD` o es incorrecta.
- `Unknown database 'gestioncine'`: no se corrió `01_schema.sql`.
- `Schema-validation: missing column/table`: las tablas no coinciden con las entidades. Correr de nuevo los scripts de `database/`.
- `Communications link failure`: MySQL no está corriendo.

## 4. Probar

- **Tests automáticos:** `./mvnw clean test`. Son 20 tests: reglas de negocio con Mockito, controller con MockMvc y `contextLoads` contra la base MySQL ya creada (necesita MySQL y `DB_PASSWORD`).
- **Manual:** importar `docs/cine-api.postman_collection.json` en Postman. La carpeta **"1. Flujo de compra (demo)"** recorre el caso completo en orden.

### Demo en 5 pasos
1. Correr `database/01_schema.sql` y `database/02_datos.sql` en Workbench.
2. `./mvnw spring-boot:run` y esperar a que aparezca `Started GestionDeCineApplication`.
3. En Postman, importar la colección y abrir la carpeta "1. Flujo de compra (demo)".
4. Ejecutar en orden: cartelera → funciones de Interstellar → mapa de butacas → comprar A1 y A2 (**201**, total 9000) → otro cliente intenta A2 (**409**) → cancelar (**200**) → mapa de butacas (A1 y A2 libres otra vez) → el otro cliente compra A2 (**201**).
5. Mostrar algún error de validación (**400**), un id inexistente (**404**) y una función superpuesta (**409**) desde las otras carpetas. Con `SHOW_SQL=true` se ve en la consola el SQL que ejecuta Hibernate.

## 5. Estructura del proyecto

```
database/                 01_schema.sql (tablas), 02_datos.sql (datos de prueba), README
docs/                     documentación y colección Postman
src/main/java/com/example/GestionDeCine/
├── GestionDeCineApplication.java   punto de entrada (main)
├── config/        CorsConfig: CORS configurable por properties
├── controller/    capa HTTP (@RestController): recibe, valida con @Valid, delega, responde
├── dto/           *RequestDTO (entrada validada) y *ResponseDTO (salida); ErrorResponseDTO
├── exception/     excepciones propias + GlobalExceptionHandler (@RestControllerAdvice)
├── mapper/        conversión entidad <-> DTO escrita a mano
├── model/         entidades JPA (@Entity) y enums
├── repository/    DAO: interfaces JpaRepository
└── service/       reglas de negocio (@Service, @Transactional)
    └── interfaces/  contratos de los services
src/main/resources/application.properties   configuración (lee variables de entorno)
src/test/java/...                           tests (Mockito, MockMvc, contexto completo)
```

## Fuera del alcance de la Etapa 1

- **Autenticación y autorización con Spring Security**: el login compara la password en texto plano y no emite token. Cualquiera puede llamar a cualquier endpoint.
- **Front-end**: es la Etapa 2. Va a consumir esta API por HTTP sin tocar la base.
