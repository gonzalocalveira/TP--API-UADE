# Endpoints de la API

- **Base URL:** `http://localhost:8080` (configurable con `PORT`; en otro equipo, `http://<host>:<puerto>`).
- **Formato:** todo request y toda respuesta es JSON (`Content-Type: application/json`).
- **Colección Postman:** [`cine-api.postman_collection.json`](cine-api.postman_collection.json), con los 68 requests de este documento.
- **Fechas:** `yyyy-MM-dd`. **Horas:** `HH:mm:ss`. **Fecha y hora:** `yyyy-MM-ddTHH:mm:ss`.
- Los ejemplos usan los datos de [`database/02_datos.sql`](../database/02_datos.sql).

## Formato de error (igual para todos los endpoints)

```json
{
  "timestamp": "2026-10-27T19:41:52.5466211",
  "status": 400,
  "error": "Bad Request",
  "message": "Datos de entrada invalidos",
  "path": "/api/salas",
  "detalles": [
    "nombre: El nombre de la sala es obligatorio",
    "capacidad: La capacidad debe ser mayor a 0"
  ]
}
```
`detalles` solo viene con errores de validación (400); en los demás casos es `null`.

| Código | Cuándo |
|---|---|
| 200 OK | Consulta o modificación exitosa |
| 201 Created | Se creó un recurso (POST). El body trae el recurso creado con su `id` |
| 204 No Content | Se eliminó (DELETE). Sin body |
| 400 Bad Request | Body inválido (`@Valid`), JSON mal formado, enum inexistente, parámetro de URL con tipo incorrecto o faltante |
| 401 Unauthorized | Login incorrecto |
| 404 Not Found | El id no existe, o la ruta no existe |
| 405 Method Not Allowed | La ruta existe pero no acepta ese método |
| 409 Conflict | Se viola una regla de negocio (butaca ocupada, nombre repetido, función superpuesta, borrar con dependencias…) |
| 500 Internal Server Error | Error inesperado. El detalle queda en el log del servidor, nunca en la respuesta |

---

## Películas: `/api/peliculas`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/peliculas` | Lista todas | 200 | — |
| GET | `/api/peliculas/{id}` | Una película | 200 | 400 (id no numérico), 404 |
| POST | `/api/peliculas` | Crea | 201 | 400 |
| PUT | `/api/peliculas/{id}` | Modifica (todos los campos) | 200 | 400, 404 |
| DELETE | `/api/peliculas/{id}` | Elimina | 204 | 404, **409 si tiene funciones** |

**Body (POST/PUT)**, todos los campos son obligatorios:
```json
{
  "nombre": "Relatos salvajes",
  "director": "Damian Szifron",
  "duracion": 122,
  "fechaEstreno": "2014-08-21",
  "idioma": "Espanol",
  "genero": "Comedia negra",
  "clasificacionEdad": "+16",
  "sinopsis": "Seis historias de venganza."
}
```
Validaciones:
- `nombre` hasta 150 caracteres.
- `director` hasta 100.
- `duracion` > 0 (en minutos).
- `idioma` y `genero` hasta 50.
- `clasificacionEdad` hasta 20.
- `sinopsis` hasta 1000.

**Respuesta:** los mismos campos, más `"id": 5`.

## Salas: `/api/salas`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/salas` | Lista todas | 200 | — |
| GET | `/api/salas/{id}` | Una sala | 200 | 404 |
| GET | `/api/salas/{id}/asientos` | Butacas de la sala, ordenadas por fila y número | 200 | 404 |
| POST | `/api/salas` | Crea | 201 | 400, **409 si el nombre ya existe** (sin distinguir mayúsculas) |
| PUT | `/api/salas/{id}` | Modifica | 200 | 400, 404, **409 si el nombre lo usa otra sala o la capacidad es menor a las butacas cargadas** |
| DELETE | `/api/salas/{id}` | Elimina | 204 | 404, **409 si tiene butacas o funciones** |

**Body:** `{"nombre": "Sala 3", "capacidad": 50}`. El nombre es obligatorio (hasta 50 caracteres) y la capacidad debe ser > 0.
**Respuesta:** `{"id": 3, "nombre": "Sala 3", "capacidad": 50}`

## Butacas: `/api/asientos`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/asientos` | Lista todas | 200 | — |
| GET | `/api/asientos/{id}` | Una butaca | 200 | 404 |
| POST | `/api/asientos` | Crea | 201 | 400, 404 (sala), **409 si la ubicación ya existe en la sala o la sala está completa** |
| PUT | `/api/asientos/{id}` | Modifica | 200 | 400, 404, 409 (mismas reglas) |
| DELETE | `/api/asientos/{id}` | Elimina | 204 | 404, **409 si alguna vez se vendió** |

**Body:** `{"numeroAsiento": 9, "filaAsiento": "F", "salaId": 2}`. El número debe ser > 0 y la fila obligatoria (hasta 5 caracteres; se guarda en mayúsculas).
**Respuesta:** `{"id": 71, "numeroAsiento": 9, "filaAsiento": "F", "salaId": 2}`

## Funciones: `/api/funciones`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/funciones` | Lista, ordenadas por fecha y hora. Filtros opcionales `?peliculaId=1` y `?fecha=2026-10-28` | 200 | 400 (fecha mal escrita) |
| GET | `/api/funciones/{id}` | Una función | 200 | 404 |
| POST | `/api/funciones` | Crea | 201 | 400, 404 (película o sala), **409 si es en el pasado o se superpone con otra de la misma sala** |
| PUT | `/api/funciones/{id}` | Modifica | 200 | 400, 404, **409 (las mismas reglas, o si ya tiene compras)** |
| DELETE | `/api/funciones/{id}` | Elimina | 204 | 404, **409 si tiene compras** |
| GET | `/api/funciones/{id}/butacas` | Mapa de la sala: todas las butacas con `ocupada` | 200 | 404 |
| GET | `/api/funciones/{id}/asientos-disponibles` | Solo las butacas libres | 200 | 404 |

**Body (POST/PUT):**
```json
{
  "fechaFuncion": "2026-11-01",
  "horaFuncion": "20:00:00",
  "formatoFuncion": "TRES_D",
  "precio": 5000,
  "peliculaId": 4,
  "salaId": 1
}
```
- `formatoFuncion` acepta `DOS_D`, `TRES_D` o `CUATRO_DX`; otro valor da 400.
- `fechaFuncion` no puede ser pasada.
- `precio` > 0, con hasta 2 decimales.

**Respuesta:**
```json
{
  "id": 1,
  "fechaFuncion": "2026-10-28",
  "horaFuncion": "18:00:00",
  "formatoFuncion": "DOS_D",
  "precio": 4500.0,
  "peliculaId": 1,
  "peliculaNombre": "Interstellar",
  "duracion": 169,
  "salaId": 1,
  "salaNombre": "Sala 1"
}
```
**Respuesta de `/butacas`:**
```json
[
  { "id": 1, "filaAsiento": "A", "numeroAsiento": 1, "ocupada": false },
  { "id": 2, "filaAsiento": "A", "numeroAsiento": 2, "ocupada": true }
]
```
**409 por superposición:**
`"message": "La funcion se superpone en Sala 1 con la funcion 1 (Interstellar, de 18:00 a 20:49)"`

## Usuarios: `/api/usuarios`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/usuarios` | Lista todos | 200 | — |
| GET | `/api/usuarios/{id}` | Un usuario | 200 | 404 |
| POST | `/api/usuarios` | Registra un **CLIENTE no socio** | 201 | 400, **409 si el mail o el DNI ya existen** |
| PUT | `/api/usuarios/{id}` | Modifica datos y password (no cambia rol ni socio) | 200 | 400, 404, 409 |
| DELETE | `/api/usuarios/{id}` | Elimina (su membresía se borra en cascada) | 204 | 404, **409 si tiene compras** |
| POST | `/api/usuarios/login` | Login con mail y password | 200 | 400, **401** |

**Body (POST/PUT):**
```json
{
  "nombreApellido": "Carla Ruiz",
  "dni": 40111222,
  "fechaNacimiento": "2001-05-05",
  "mail": "carla@mail.com",
  "password": "carla1234"
}
```
- `mail` con formato válido; se guarda en minúsculas.
- `password` de 4 a 100 caracteres.
- `fechaNacimiento` en el pasado.
- `dni` > 0.

**Body (login):** `{"mail": "ana@mail.com", "password": "ana1234"}`

**Respuesta (nunca incluye la password):**
```json
{
  "id": 1,
  "nombreApellido": "Ana Gomez",
  "dni": 30111222,
  "fechaNacimiento": "1990-04-12",
  "mail": "ana@mail.com",
  "isSocio": true,
  "rol": "CLIENTE"
}
```

## Membresías: `/api/membresias`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| POST | `/api/membresias` | Alta. El usuario pasa a `isSocio: true` | 201 | 400, 404 (usuario), **409 si ya tiene una** |
| DELETE | `/api/membresias/{id}` | Baja. El usuario pasa a `isSocio: false` | 204 | 404 |

**Body:** `{"usuarioId": 2, "puntos": 0, "tipoMembresia": "CLASSIC"}`. `tipoMembresia` acepta `CLASSIC`, `GOLD` o `BLACK`; `puntos` >= 0.
**Respuesta:** `{"id": 2, "usuarioId": 2, "puntos": 0, "tipoMembresia": "CLASSIC"}`

## Clientes / compras: `/api/clientes`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/clientes/cartelera` | Catálogo de películas | 200 | — |
| POST | `/api/clientes/compras` | Compra una o más butacas de una función | 201 | 400, 404 (usuario, función o butaca), **409** (ver abajo) |
| PATCH | `/api/clientes/compras/{id}/cancelar` | Cancela la compra y libera sus butacas | 200 | 404, **409 si ya estaba cancelada o la función ya empezó** |
| GET | `/api/clientes/{usuarioId}/historial` | Compras del cliente, de la más nueva a la más vieja | 200 | 404, 409 (no es cliente) |
| GET | `/api/clientes/{usuarioId}/membresia` | Membresía del cliente | 200 | 404 (usuario o membresía), 409 (no es cliente) |

**Body (compra):**
```json
{ "usuarioId": 1, "funcionId": 1, "asientoIds": [1, 2] }
```
No se manda precio ni total: los calcula el servidor.

Errores 409 de la compra:
- La butaca ya está ocupada para la función.
- La misma butaca se pidió dos veces.
- La butaca no pertenece a la sala de la función.
- La función ya empezó.
- El usuario no es CLIENTE.

**Respuesta (compra, cancelación e historial):**
```json
{
  "id": 2,
  "fechaCompra": "2026-10-27T19:41:44.662157",
  "estado": "CONFIRMADA",
  "total": 9000.0,
  "usuarioId": 2,
  "funcionId": 1,
  "peliculaNombre": "Interstellar",
  "salaNombre": "Sala 1",
  "fechaFuncion": "2026-10-28",
  "horaFuncion": "18:00:00",
  "entradas": [
    { "id": 3, "asientoId": 2, "filaAsiento": "A", "numeroAsiento": 2, "precioUnitario": 4500.0, "estado": "OCUPADA" },
    { "id": 4, "asientoId": 3, "filaAsiento": "A", "numeroAsiento": 3, "precioUnitario": 4500.0, "estado": "OCUPADA" }
  ]
}
```
Después de cancelar: `"estado": "CANCELADA"` y cada entrada `"estado": "CANCELADA"`.

**409 por butaca ocupada:**
`"message": "La butaca A2 ya esta ocupada para esta funcion"`
