# Anotaciones y sintaxis usadas en el proyecto

Una **anotación** (`@Algo`) es metadata que se pega a una clase, un método, un parámetro o un atributo. Por sí sola no ejecuta nada: la lee otra herramienta (Spring, Hibernate, Jackson, Lombok, JUnit) y en base a ella hace algo.
Todos los ejemplos son código real de este proyecto.

---

## 1. Arranque y configuración (Spring Boot)

### `@SpringBootApplication`
Junta tres anotaciones:
- `@Configuration`: la clase puede definir beans.
- `@EnableAutoConfiguration`: Spring Boot configura solo Tomcat, el DataSource, JPA y Jackson según las dependencias del `pom.xml`.
- `@ComponentScan`: busca `@Component`, `@Service`, `@Repository` y `@RestController` en el paquete `com.example.GestionDeCine` y sus subpaquetes.

```java
@SpringBootApplication
public class GestionDeCineApplication {
    public static void main(String[] args) {
        SpringApplication.run(GestionDeCineApplication.class, args);   // levanta el contexto y Tomcat
    }
}
```
Solo puede haber **una** clase así. El proyecto tenía dos (`EjemploApplication`) y eso rompía los tests.

### `@Configuration` y `WebMvcConfigurer`
`@Configuration` marca una clase de configuración (es un bean). Implementar `WebMvcConfigurer` permite ajustar Spring MVC, en este caso CORS.
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(origenesPermitidos)...;
    }
}
```

### `@Value("${propiedad}")`
Inyecta un valor de `application.properties`. Si el valor tiene comas, Spring lo convierte en un array.
```java
public CorsConfig(@Value("${cors.allowed-origins}") String[] origenesPermitidos) { ... }
```

### `${VARIABLE:default}` en `application.properties`
Lee la variable de entorno `VARIABLE`; si no existe, usa `default`.
```properties
spring.datasource.password=${DB_PASSWORD:}
server.port=${PORT:8080}
```

---

## 2. Capa controller (Spring MVC)

### `@RestController`
Marca la clase como controlador REST: Spring la registra como bean y lo que devuelve cada método se convierte a **JSON** (con Jackson) en el body de la respuesta. Equivale a `@Controller` + `@ResponseBody`.

### `@RequestMapping("/api/peliculas")`
Prefijo común de las rutas de toda la clase.

### `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`
Asocian un método a un verbo HTTP y una ruta (relativa al `@RequestMapping`).
```java
@GetMapping("/{id}")                         // GET /api/peliculas/{id}
@PostMapping                                 // POST /api/peliculas
@PatchMapping("/compras/{id}/cancelar")      // PATCH /api/clientes/compras/{id}/cancelar
```

| Verbo | Uso en el proyecto | ¿Idempotente? |
|---|---|---|
| GET | Consultar | Sí (no cambia nada) |
| POST | Crear (película, compra) o una acción (login) | No: dos POST crean dos compras |
| PUT | Reemplazar todos los datos de un recurso | Sí |
| PATCH | Modificar una parte (el estado de una compra al cancelar) | — |
| DELETE | Eliminar | Sí |

### `@PathVariable`
Toma un valor de la ruta. Si no se puede convertir (`/api/peliculas/abc`), da `MethodArgumentTypeMismatchException`, que el handler devuelve como 400.
```java
public ResponseEntity<PeliculaResponseDTO> obtenerPeliculaPorId(@PathVariable Integer id)
```

### `@RequestParam`
Toma un parámetro de la query string (`?peliculaId=1&fecha=2026-10-28`). Con `required = false` es opcional y vale `null` si no viene.
```java
public ResponseEntity<List<FuncionResponseDTO>> obtenerFunciones(
        @RequestParam(required = false) Integer peliculaId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha)
```
`@DateTimeFormat(iso = ISO.DATE)` indica que el texto viene como `yyyy-MM-dd`.

### `@RequestBody`
Indica que el parámetro sale del **body** del request: Jackson convierte el JSON en el objeto. Si el JSON está mal formado, da `HttpMessageNotReadableException` (400).

### `@Valid`
Antes de entrar al método, ejecuta las validaciones (`@NotBlank`, `@Positive`…) del DTO. Si alguna falla, el método **no se ejecuta** y Spring lanza `MethodArgumentNotValidException`, que el handler convierte en 400 con el detalle.
```java
public ResponseEntity<PeliculaResponseDTO> crearPelicula(@Valid @RequestBody PeliculaRequestDTO dto)
```

### `ResponseEntity<T>` y `HttpStatus`
Representa la respuesta HTTP completa: código de estado, headers y body.
```java
return ResponseEntity.ok(dto);                                      // 200 + body
return ResponseEntity.status(HttpStatus.CREATED).body(creada);      // 201 + body
return ResponseEntity.noContent().build();                          // 204 sin body
```

---

## 3. Manejo de errores

### `@RestControllerAdvice`
Clase que "aconseja" a todos los controllers: sus métodos se ejecutan cuando un controller (o el service que llamó) lanza una excepción. Como es *Rest*, lo que devuelve va como JSON.

### `@ExceptionHandler(Tipo.class)`
Indica qué excepción atiende cada método. Spring elige el handler más específico; `Exception.class` atrapa todo lo que no tenga uno propio.
```java
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ErrorResponseDTO> handleBusiness(BusinessException ex, WebRequest request) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);   // 409
}
```

### Excepciones propias (`extends RuntimeException`)
```java
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
```
Son *unchecked* (heredan de `RuntimeException`) por dos motivos:
- No obligan a escribir `throws` en cada método.
- `@Transactional` hace **rollback automático** ante `RuntimeException`.

---

## 4. Capa service

### `@Service`
Marca la clase como componente de lógica de negocio. Spring crea **una instancia** (singleton) y la inyecta donde se pida su interfaz.

### Inyección por constructor
```java
private final PeliculaRepository peliculaRepository;

public PeliculaService(PeliculaRepository peliculaRepository, FuncionRepository funcionRepository) {
    this.peliculaRepository = peliculaRepository;
    ...
}
```
No hace falta `@Autowired`: si la clase tiene un solo constructor, Spring lo usa para inyectar. Los atributos pueden ser `final` (no cambian) y en los tests se pasan mocks.

### `@Transactional` y `@Transactional(readOnly = true)`
Spring envuelve el método en una transacción de base: `BEGIN` al entrar, `COMMIT` al salir bien y `ROLLBACK` si sale con una `RuntimeException`. En `comprarEntradas` garantiza que el ticket y sus entradas se guardan **todos o ninguno**.
`readOnly = true` se usa en las consultas: le avisa a Hibernate que no hay cambios que guardar y evita trabajo extra.
Además, dentro de una transacción, Hibernate detecta los cambios en las entidades cargadas (*dirty checking*) y los guarda al hacer commit, aunque no se llame a `save`:
```java
ticket.setEstado(EstadoCompra.CANCELADA);                     // en cancelarCompra: se guarda al COMMIT
```

### `@Override`
Indica que el método implementa uno de la interfaz. El compilador avisa si la firma no coincide.

---

## 5. Capa de datos: entidades JPA (`jakarta.persistence`)

### `@Entity` y `@Table(name = "...")`
`@Entity` dice que la clase se persiste y Hibernate la mapea a una tabla. `@Table` da el nombre exacto de la tabla y sus restricciones únicas.
```java
@Entity
@Table(name = "asiento",
        uniqueConstraints = @UniqueConstraint(name = "uk_asiento_ubicacion",
                columnNames = { "sala_id", "fila_asiento", "numero_asiento" }))
public class Asiento { ... }
```

### `@Id` y `@GeneratedValue(strategy = GenerationType.IDENTITY)`
`@Id` marca la clave primaria. `IDENTITY` delega el valor en el `AUTO_INCREMENT` de MySQL.

### `@Column(name = ..., nullable = ..., unique = ..., length = ..., precision = ..., scale = ...)`
Mapea el atributo a una columna con su nombre exacto (`fechaEstreno` → `fecha_estreno`). Como usamos `ddl-auto=validate`, el nombre tiene que coincidir con el del script.
```java
@Column(name = "precio", nullable = false, precision = 10, scale = 2)
private BigDecimal precio;        // DECIMAL(10,2)
```

### `@Enumerated(EnumType.STRING)`
Guarda el enum por su **nombre** (`'TRES_D'`) y no por su posición (0, 1, 2). Si se reordena o se agrega un valor al enum, los datos guardados siguen siendo correctos.

### `@ManyToOne` y `@JoinColumn`
Relación "muchos a uno": muchas butacas pertenecen a una sala. `@JoinColumn` nombra la columna FK.
```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "sala_id", nullable = false)
private Sala sala;
```

### `@OneToOne`
Uno a uno. En la base es una FK con `UNIQUE`.
```java
@OneToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "usuario_id", nullable = false, unique = true)
private Usuario usuario;          // en Membresia
```

### `@OneToMany(mappedBy = ..., cascade = ...)`
Lado "uno" de una relación uno a muchos: una compra tiene muchas entradas.
```java
@OneToMany(mappedBy = "ticketCompra", cascade = CascadeType.ALL)
private List<Entrada> entradas = new ArrayList<>();
```
- `mappedBy = "ticketCompra"`: la FK la maneja el atributo `ticketCompra` de `Entrada`, así que no se crea otra columna ni tabla intermedia.
- `cascade = CascadeType.ALL`: las operaciones sobre el ticket (guardar, borrar) se propagan a sus entradas.

### `FetchType.LAZY` vs `EAGER`
- `EAGER`: trae la relación siempre, junto con la entidad.
- `LAZY`: pone un proxy y consulta recién cuando se usa. En el proyecto **todas** las relaciones son LAZY para no cargar datos de más.

---

## 6. Repositorios (Spring Data JPA)

### `@Repository`
Marca la interfaz como DAO. Además, Spring traduce las excepciones del motor a excepciones propias, como `DataIntegrityViolationException`.

### `JpaRepository<Entidad, TipoId>`
Al extender esta interfaz, Spring genera la implementación en tiempo de ejecución con `save`, `findById`, `findAll`, `delete`, `existsById`, `count`… sin escribir una línea.
```java
public interface PeliculaRepository extends JpaRepository<Pelicula, Integer> { }
```

### Query methods (consultas derivadas del nombre)
Spring Data lee el nombre del método y arma la consulta:
```java
boolean existsBySalaIdAndFilaAsientoIgnoreCaseAndNumeroAsiento(Integer salaId, String fila, Integer numero);
//  → select count(*) > 0 from asiento where sala_id = ? and upper(fila_asiento) = upper(?) and numero_asiento = ?
List<Asiento> findBySalaIdOrderByFilaAsientoAscNumeroAsientoAsc(Integer salaId);
long countBySalaId(Integer salaId);
```

### `@Query("JPQL")`
Consulta escrita a mano en **JPQL**: es como SQL, pero sobre clases y atributos, no tablas y columnas. Los `:parametro` se completan con los argumentos del método. `join fetch` trae la relación en la misma consulta.
```java
@Query("""
        select f from Funcion f join fetch f.pelicula join fetch f.sala
        where (:peliculaId is null or f.pelicula.id = :peliculaId)
          and (:fecha is null or f.fechaFuncion = :fecha)
        order by f.fechaFuncion, f.horaFuncion
        """)
List<Funcion> buscar(Integer peliculaId, LocalDate fecha);
```
Las `"""` son un **text block** de Java (un string de varias líneas).

### `@Lock(LockModeType.PESSIMISTIC_WRITE)`
Hace que la consulta bloquee las filas que lee (`SELECT ... FOR UPDATE`) hasta que termina la transacción. Se usa para que dos compras simultáneas de la misma función se ejecuten de a una.
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select f from Funcion f where f.id = :id")
Optional<Funcion> findByIdParaCompra(Integer id);
```

### `@EntityGraph(attributePaths = {...})`
Le pide a Hibernate que traiga esas relaciones en la misma consulta, para evitar N+1.
```java
@EntityGraph(attributePaths = { "funcion.pelicula", "funcion.sala", "entradas.asiento" })
List<TicketCompra> findByUsuarioIdOrderByFechaCompraDesc(Integer usuarioId);
```

---

## 7. Validación (Bean Validation, `jakarta.validation.constraints`)

Se ponen en los atributos de los DTOs de request y se ejecutan con `@Valid`. El `message` es lo que se devuelve en `detalles`.

| Anotación | Valida | Ejemplo del proyecto |
|---|---|---|
| `@NotNull` | No es `null` | `@NotNull Integer funcionId` (CompraRequestDTO) |
| `@NotBlank` | String no nulo y con al menos un carácter que no sea espacio | `@NotBlank String nombre` (PeliculaRequestDTO) |
| `@NotEmpty` | Colección o string no nulo ni vacío | `@NotEmpty List<Integer> asientoIds` |
| `@Size(min, max)` | Largo de un string o colección | `@Size(min = 4, max = 100) String password` |
| `@Positive` | Número > 0 | `@Positive Integer duracion` |
| `@PositiveOrZero` | Número >= 0 | `@PositiveOrZero int puntos` (MembresiaRequestDTO) |
| `@Email` | Formato de mail | `@Email String mail` (UsuarioRequestDTO, LoginRequestDTO) |
| `@Past` | Fecha anterior a hoy | `@Past LocalDate fechaNacimiento` |
| `@FutureOrPresent` | Fecha de hoy o futura | `@FutureOrPresent LocalDate fechaFuncion` |
| `@Digits(integer, fraction)` | Cantidad máxima de dígitos enteros y decimales | `@Digits(integer = 8, fraction = 2) BigDecimal precio` |

Las validaciones también se pueden aplicar a los elementos de una lista: `List<@NotNull Integer> asientoIds`.

---

## 8. Lombok

Lombok es un **procesador de anotaciones**: durante la compilación (no en ejecución) genera código Java que no escribimos. El `.class` final tiene los getters y constructores como si los hubiéramos escrito.
> En Java 23+ hay que declararlo en `annotationProcessorPaths` del `maven-compiler-plugin` (ver `pom.xml`); si no, no se ejecuta y el proyecto no compila.

| Anotación | Qué genera | Dónde se usa |
|---|---|---|
| `@Getter` | Un `getX()` por atributo (`isX()` si es `boolean`) | Todas las entidades |
| `@Setter` | Un `setX(valor)` por atributo | Todas las entidades |
| `@NoArgsConstructor` | Constructor sin parámetros: `public Pelicula() {}` | Entidades (JPA **lo exige** para crear objetos al leer de la base) y DTOs (Jackson lo usa para leer el JSON) |
| `@AllArgsConstructor` | Constructor con todos los atributos, **en el orden en que están declarados** | DTOs: `new SalaResponseDTO(id, nombre, capacidad)` en los mappers |
| `@Data` | `@Getter` + `@Setter` + `@ToString` + `@EqualsAndHashCode` + `@RequiredArgsConstructor` | Todos los DTOs |
| `@Builder` | Patrón builder (`X.builder().a(1).b(2).build()`) | **No se usa**. Se prefirieron los constructores para que la conversión sea explícita |

Equivalente escrito a mano de `@Getter @Setter @NoArgsConstructor` en `Sala`:
```java
public Sala() { }
public Integer getId() { return id; }
public void setId(Integer id) { this.id = id; }
public String getNombre() { return nombre; }
public void setNombre(String nombre) { this.nombre = nombre; }
public Integer getCapacidad() { return capacidad; }
public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
```
Y `@Data` además genera:
```java
public String toString() { return "SalaResponseDTO(id=" + id + ", nombre=" + nombre + ", capacidad=" + capacidad + ")"; }
public boolean equals(Object o) { /* compara id, nombre y capacidad */ }
public int hashCode() { /* combina id, nombre y capacidad */ }
```

### ¿Por qué en las entidades se reemplazó `@Data` por `@Getter` + `@Setter`?

El proyecto original usaba `@Data` en las entidades. Con relaciones JPA eso genera tres problemas:
1. **Recursión infinita.** `toString`, `equals` y `hashCode` recorren **todos** los atributos, incluidas las relaciones. En la relación bidireccional `TicketCompra ⇄ Entrada`, el `toString()` de un ticket llama al de sus entradas, que llama al de su ticket, y así hasta un `StackOverflowError`.
2. **Consultas ocultas.** Con `FetchType.LAZY`, imprimir una entidad (por ejemplo en un log) dispararía consultas a la base para cargar cada relación. Fuera de la transacción, tira `LazyInitializationException`.
3. **`hashCode` que cambia.** Un objeto nuevo tiene `id = null`. Al guardarlo pasa a tener id, así que su `hashCode` cambia. Si estaba dentro de un `HashSet`, "se pierde".

Por eso las entidades usan solo `@Getter`, `@Setter` y `@NoArgsConstructor`, y heredan el `equals` de `Object` (identidad).
En los **DTOs** `@Data` no tiene esos problemas: no tienen relaciones JPA ni ids generados, son datos planos.

### `@JsonProperty("isSocio")` (Jackson)
Lombok genera `isSocio()` para el boolean `socio`, y Jackson lo expondría como `"socio"`. `@JsonProperty` lo renombra a `"isSocio"` para mantener el nombre que ya usaba la API.

---

## 9. Sintaxis de Java que aparece seguido

| Sintaxis | Qué es | Ejemplo |
|---|---|---|
| `Optional<T>` + `orElseThrow` | Un valor que puede no estar. Si no está, se lanza la excepción | `peliculaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No existe la pelicula con id " + id))` |
| Lambda `x -> ...` | Función anónima | `asiento -> !ocupadas.contains(asiento.getId())` |
| Referencia a método `Clase::metodo` | Atajo de una lambda que solo llama a un método | `.map(PeliculaMapper::toResponse)` |
| Streams `.stream().filter().map().toList()` | Transformar una colección paso a paso | `findAll().stream().map(SalaMapper::toResponse).toList()` |
| `Set` / `HashSet` | Colección sin repetidos y búsqueda rápida | `new HashSet<>(asientoIds).size() != asientoIds.size()` → hay butacas repetidas |
| `BigDecimal.multiply` | Aritmética exacta para dinero | `funcion.getPrecio().multiply(BigDecimal.valueOf(asientoIds.size()))` |
| `final class` + constructor `private` | Clase utilitaria que no se instancia | `public final class PeliculaMapper { private PeliculaMapper() {} ... }` |
| `static` | Método que pertenece a la clase y no a un objeto | `PeliculaMapper.toResponse(p)` |
| `LocalDate`, `LocalTime`, `LocalDateTime` | Fechas y horas sin zona horaria | `LocalDateTime.of(fechaFuncion, horaFuncion)` |

---

## 10. Tests

| Anotación / clase | Qué hace | Ejemplo |
|---|---|---|
| `@Test` (JUnit 5) | Marca un método como test | `void comprar_butacaOcupada_lanzaBusinessExceptionYNoGuardaNada()` |
| `@BeforeEach` | Se ejecuta antes de cada test, para armar los datos | `setUp()` en `ClienteServiceTest` |
| `@ExtendWith(MockitoExtension.class)` | Activa Mockito en la clase de test | `ClienteServiceTest` |
| `@Mock` | Crea un objeto falso: sus métodos devuelven lo que le digamos con `when(...).thenReturn(...)` | `@Mock private FuncionRepository funcionRepository;` |
| `@InjectMocks` | Crea el objeto a probar y le pasa los mocks por constructor | `@InjectMocks private ClienteService clienteService;` |
| `assertEquals`, `assertThrows` | Verifican el resultado o que se lanzó una excepción | `assertThrows(BusinessException.class, () -> clienteService.comprarEntradas(compra(100, 100)))` |
| `verify(mock, never()).save(any())` | Verifica que un método **no** se llamó | No se guardó nada si la butaca estaba ocupada |
| `MockMvc` + `standaloneSetup` | Simula requests HTTP contra un controller sin levantar Tomcat | `mockMvc.perform(post("/api/peliculas")...).andExpect(status().isCreated())` |
| `jsonPath("$.campo")` | Lee un valor del JSON de la respuesta | `jsonPath("$.detalles").isArray()` |
| `@SpringBootTest` | Levanta **toda** la aplicación (con la base real) | `contextLoads`: verifica que el script coincida con las entidades |
