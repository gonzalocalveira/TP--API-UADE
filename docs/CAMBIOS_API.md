# Cambios de contrato de la API (para adaptar el front-end)

Las **rutas y los métodos existentes se mantuvieron todos**. Lo que cambió es la forma de algunos JSON, porque los controllers dejaron de recibir y devolver entidades JPA y ahora usan DTOs planos.
El **formato de error no cambió**: `timestamp, status, error, message, path, detalles`.

| # | Endpoint | Antes | Después | Motivo |
|---|---|---|---|---|
| 1 | `POST/PUT /api/asientos` (request) | `{"numeroAsiento":1,"filaAsiento":"A","sala":{"id":1}}` | `{"numeroAsiento":1,"filaAsiento":"A","salaId":1}` | El DTO pide solo el id. El cliente no tiene por qué conocer la estructura de la entidad `Sala` |
| 2 | Respuestas de asientos (`/api/asientos`, `/api/salas/{id}/asientos`, `/api/funciones/{id}/asientos-disponibles`) | Objeto `sala` completo anidado | `"salaId": 1` | Respuesta plana, sin cargar la sala de cada butaca |
| 3 | `POST/PUT /api/funciones` (request) | `"pelicula":{"id":1},"sala":{"id":1}` | `"peliculaId":1,"salaId":1` | Igual que el #1 |
| 4 | Respuestas de funciones | Objetos `pelicula` y `sala` completos anidados | `peliculaId, peliculaNombre, duracion, salaId, salaNombre` | Solo lo que el front necesita mostrar |
| 5 | `POST /api/clientes/compras` (request) | `{"usuarioId":1,"funcionId":1,"asientoId":1}` (una butaca) | `{"usuarioId":1,"funcionId":1,"asientoIds":[1,2]}` (una o varias) | Una compra ahora puede incluir varias butacas |
| 6 | Respuesta de compra y de `GET /api/clientes/{id}/historial` | Entidad `TicketCompra` con `precio`, `usuario` (completo) y `entrada` (una) | `id, fechaCompra, estado, total, usuarioId, funcionId, peliculaNombre, salaNombre, fechaFuncion, horaFuncion, entradas[]` | Nuevo modelo: total calculado en el servidor, estado y lista de entradas |
| 7 | `POST /api/membresias` (request) | `{"usuario":{"id":1},"puntos":0,"tipoMembresia":"GOLD"}` | `{"usuarioId":1,"puntos":0,"tipoMembresia":"GOLD"}` | Igual que el #1 |
| 8 | Respuestas de membresía (`POST /api/membresias`, `GET /api/clientes/{id}/membresia`) | Entidad con el `usuario` completo anidado | `id, usuarioId, puntos, tipoMembresia` | Respuesta plana |
| 9 | `POST/PUT /api/usuarios` (request) | Incluía `isSocio` y `rol` | Ya no se leen (si se mandan, se ignoran) | Ya antes se ignoraban al crear. Ser socio depende de tener membresía y el rol no lo elige el cliente |
| 10 | `PUT /api/usuarios/{id}` | No cambiaba la password aunque viniera en el body | Sí actualiza la password (el campo sigue siendo obligatorio) | Antes era obligatoria y no tenía efecto, lo que confundía |
| 11 | Respuestas de usuarios | `isSocio` (en Jackson 3 aparecía duplicado como `socio`) | Solo `isSocio` | Corrección de bug |

## Endpoints nuevos

| Método | Ruta | Qué hace |
|---|---|---|
| `GET` | `/api/funciones?peliculaId=&fecha=` | Filtros opcionales sobre el listado existente (sin filtros devuelve todo, como antes) |
| `GET` | `/api/funciones/{id}/butacas` | Mapa de la sala para una función: cada butaca con `ocupada: true/false` |
| `PATCH` | `/api/clientes/compras/{id}/cancelar` | Cancela una compra y libera sus butacas |

## Códigos de estado que cambiaron

| Caso | Antes | Después |
|---|---|---|
| Id no numérico en la URL (`/api/peliculas/abc`) | 500 | 400 |
| Ruta inexistente (`/api/pelis`) | 500 | 404 |
| Borrar película, sala, butaca, función o usuario con dependencias | 409 genérico ("viola una restricción...") | 409 con un mensaje claro ("tiene funciones asociadas", etc.) |
