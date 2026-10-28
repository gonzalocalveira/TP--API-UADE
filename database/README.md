# Base de datos `gestioncine`

La base de datos **se crea desde MySQL con estos scripts**, no desde Spring. La aplicación arranca con `spring.jpa.hibernate.ddl-auto=validate`: solo **verifica** que las tablas coincidan con las clases `@Entity` y, si no coinciden, no levanta. Nunca crea ni modifica tablas.

| Script | Qué hace |
|---|---|
| `01_schema.sql` | `DROP DATABASE IF EXISTS gestioncine` → `CREATE DATABASE` → `CREATE TABLE` de las 8 tablas, con sus PK, FK, UNIQUE y CHECK |
| `02_datos.sql` | Datos de prueba para la demo: 4 películas, 2 salas, 70 butacas, 6 funciones futuras, 2 clientes, 1 admin y 1 membresía |

> ⚠️ **Correr `01_schema.sql` de nuevo resetea la base**: borra todos los datos y deja las tablas vacías. Para volver al estado inicial de la demo se corren **01 y después 02**.

## Opción A: MySQL Workbench (paso a paso)

1. Abrir MySQL Workbench y conectarse a la instancia local (`Local instance MySQL80`, usuario `root`).
2. **File → Open SQL Script…** y elegir `database/01_schema.sql`.
3. Ejecutar el script completo con el **rayo** ⚡ ("Execute the selected portion of the script or everything"), o con `Ctrl + Shift + Enter`.
   No usar el rayo con cursor, que ejecuta una sola sentencia.
4. Verificar en el panel *Output* que todas las líneas tengan ✅.
5. **File → Open SQL Script…** y elegir `database/02_datos.sql`. Ejecutarlo igual que el anterior.
6. En el panel *Navigator → Schemas* hacer clic derecho → **Refresh All**. Tiene que aparecer `gestioncine` con sus 8 tablas.
7. Comprobación rápida (en una pestaña de query nueva):
   ```sql
   USE gestioncine;
   SELECT COUNT(*) FROM asiento;   -- 70
   SELECT * FROM funcion;          -- 6 funciones, todas con fecha futura
   ```

## Opción B: consola

CMD o Git Bash, desde la raíz del repo:
```bat
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < database\01_schema.sql
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < database\02_datos.sql
```
PowerShell (no tiene el operador `<`):
```powershell
Get-Content database\01_schema.sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
Get-Content database\02_datos.sql  | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

## Datos de prueba que quedan cargados

| Tabla | Datos |
|---|---|
| `pelicula` | 1 Interstellar (169'), 2 Toy Story 3 (103'), 3 El secreto de sus ojos (129'), 4 Dune: Parte Dos (166') |
| `sala` | 1 "Sala 1" (capacidad 40), 2 "Sala 2" (capacidad 40, con 30 butacas cargadas) |
| `asiento` | Sala 1: ids **1–40** (A1 = 1 … E8 = 40). Sala 2: ids **41–70** (A1 = 41 … C10 = 70) |
| `funcion` | 6 funciones entre mañana y dentro de 3 días (fechas relativas con `DATE_ADD(CURDATE(), INTERVAL n DAY)`) |
| `usuarios` | 1 Ana Gomez (CLIENTE, socia) `ana@mail.com` / `ana1234` · 2 Bruno Diaz (CLIENTE) `bruno@mail.com` / `bruno1234` · 3 Admin (ADMINISTRADOR) `admin@cine.com` / `admin1234` |
| `membresia` | Ana: GOLD, 150 puntos |

Como las fechas de las funciones son relativas al día en que se corre el script, **conviene correr 01 + 02 el mismo día de la demo**. Si no, las funciones de "mañana" pueden haber pasado y la compra responde 409 ("la función ya empezó").

## ¿Por qué scripts y no `ddl-auto=update`?

Ver la explicación completa de `validate`, `update` y `create` en [`docs/MODELO_DATOS.md`](../docs/MODELO_DATOS.md#5-quién-crea-las-tablas-validate-vs-update-vs-create).
