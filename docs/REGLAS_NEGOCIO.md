# Reglas de negocio

Todas las reglas viven en la **capa service** (`service/*Service.java`), nunca en los controllers. Las validaciones de **formato** (campo obligatorio, número positivo, mail válido) las hace Bean Validation en los DTOs de request. Las reglas que **dependen de la base** (existencia, duplicados, superposiciones, estado) las hace el service.

Excepciones y código HTTP:
- `ResourceNotFoundException` → **404**
- `BusinessException` → **409**
- `AutenticacionException` → **401**

El que traduce cada excepción a HTTP es `GlobalExceptionHandler`.

## Compras (`ClienteService`)

| # | Regla | Método | Error |
|---|---|---|---|
| C1 | Solo un usuario con rol CLIENTE puede comprar y ver historial/membresía | `buscarCliente` | 409 "Esta operacion solo esta permitida para clientes" |
| C2 | El usuario, la función y cada butaca deben existir | `comprarEntradas` | 404 |
| C3 | No se puede comprar para una función que ya empezó o ya pasó | `validarQueNoEmpezo` | 409 "La funcion X ya empezo o ya paso" |
| C4 | No se puede pedir la misma butaca dos veces en una compra | `comprarEntradas` (`HashSet` del tamaño de la lista) | 409 |
| C5 | Cada butaca debe pertenecer a la sala de la función | `comprarEntradas` | 409 "La butaca X no pertenece a la sala de la funcion" |
| C6 | No se puede vender una butaca ya ocupada (entrada OCUPADA) para esa función | `comprarEntradas` + `EntradaRepository.findAsientoIdsByFuncionIdAndEstado` | 409 "La butaca A2 ya esta ocupada para esta funcion" |
| C7 | El total lo calcula el servidor: `precio de la función × cantidad de butacas`. El request no tiene campo de precio | `comprarEntradas` | — |
| C8 | Cada entrada guarda el precio unitario del momento de la compra (si después cambia el precio de la función, el historial no cambia) | `comprarEntradas` | — |
| C9 | Dos compras simultáneas para la misma función no pueden vender la misma butaca: se bloquea la fila de la función (`SELECT … FOR UPDATE`) hasta terminar la transacción | `FuncionRepository.findByIdParaCompra` (`@Lock(PESSIMISTIC_WRITE)`) | La segunda compra espera y recibe 409 (C6) |
| C10 | Toda la compra es atómica: si falla cualquier regla, no se guarda ni el ticket ni ninguna entrada | `@Transactional` en `comprarEntradas` | — |
| C11 | Solo se cancela una compra CONFIRMADA | `cancelarCompra` | 409 "La compra X ya estaba cancelada" |
| C12 | Solo se cancela antes de que empiece la función | `cancelarCompra` → `validarQueNoEmpezo` | 409 |
| C13 | Cancelar no borra la compra: el ticket y sus entradas pasan a CANCELADA, y una entrada CANCELADA ya no ocupa la butaca | `cancelarCompra` | — |

Verificación en vivo (curl, 27/10/2026): 5 compras simultáneas de la butaca 10 de la función 2 dieron **un 201 y cuatro 409**, y en la base quedó **una sola** entrada OCUPADA.

## Funciones (`FuncionService`)

| # | Regla | Método | Error |
|---|---|---|---|
| F1 | La película y la sala deben existir | `validarYCopiar` | 404 |
| F2 | La fecha no puede ser pasada | `@FutureOrPresent` en `FuncionRequestDTO` | 400 |
| F3 | La fecha **y hora** deben ser futuras (cubre "hoy a una hora que ya pasó") | `validarYCopiar` | 409 |
| F4 | Dos funciones de la misma sala no se superponen. Fin = inicio + duración de la película. Se superponen si `inicioA < finB` y `inicioB < finA`. Una función puede empezar justo cuando termina la otra | `validarSinSuperposicion` | 409 "La funcion se superpone en Sala 1 con la funcion 1 (...)" |
| F5 | Al modificar, la función no se compara consigo misma | `validarSinSuperposicion(idExcluido)` | — |
| F6 | No se modifica ni se borra una función que ya tiene compras | `modificarFuncion`, `eliminarFuncion` | 409 |
| F7 | Disponibilidad: una butaca está ocupada para una función si tiene una entrada OCUPADA en una compra de esa función | `obtenerButacas`, `obtenerAsientosDisponibles` | — |

## Salas (`SalaService`) y butacas (`AsientoService`)

| # | Regla | Método | Error |
|---|---|---|---|
| S1 | El nombre de sala es único (sin distinguir mayúsculas) | `crearSala`, `modificarSala` (también `UNIQUE` en la base) | 409 |
| S2 | La capacidad no puede bajar de la cantidad de butacas cargadas | `modificarSala` | 409 |
| S3 | No se borra una sala con butacas o funciones | `eliminarSala` (también `ON DELETE RESTRICT`) | 409 |
| A1 | La sala de la butaca debe existir | `crearAsiento`, `modificarAsiento` | 404 |
| A2 | No puede haber dos butacas con la misma fila y número en una sala | `validarUbicacionLibre` (también `UNIQUE` en la base) | 409 |
| A3 | No se cargan más butacas que la capacidad de la sala | `validarCapacidad` | 409 |
| A4 | No se borra una butaca que alguna vez se vendió | `eliminarAsiento` | 409 |

## Películas (`PeliculaService`)

| # | Regla | Método | Error |
|---|---|---|---|
| P1 | No se borra una película con funciones | `eliminarPelicula` | 409 |

## Usuarios (`UsuarioService`) y membresías (`MembresiaService`)

| # | Regla | Método | Error |
|---|---|---|---|
| U1 | El mail y el DNI son únicos (al modificar, los propios datos del usuario no cuentan como duplicado) | `validarMailYDniUnicos` (también `UNIQUE` en la base) | 409 |
| U2 | El registro público siempre crea un CLIENTE no socio; el `rol` del request se ignora | `crearUsuario` | — |
| U3 | El login responde el mismo error si el mail no existe o la password es incorrecta, para no revelar qué mails están registrados | `iniciarSesion` | 401 "Usuario o contraseña incorrectos" |
| U4 | No se borra un usuario con compras. Su membresía se borra en cascada por la FK | `eliminarUsuario` | 409 |
| M1 | Un usuario tiene como máximo una membresía | `crearMembresia` (también `UNIQUE` en la base) | 409 |
| M2 | Dar de alta una membresía marca al usuario como socio y darla de baja lo desmarca. Modifica dos tablas en una misma transacción | `crearMembresia`, `eliminarMembresia` | — |

## Por qué hay reglas duplicadas en el service y en la base

Varias reglas están dos veces: por ejemplo, el nombre de sala único está en `SalaService` **y** como `UNIQUE` en la tabla.
- **El service** da un mensaje claro (409 "Ya existe una sala con el nombre…").
- **La base** es la última barrera, por si dos requests simultáneos pasan el chequeo del service al mismo tiempo. En ese caso MySQL rechaza el segundo y `GlobalExceptionHandler` lo convierte en 409 (`DataIntegrityViolationException`).
