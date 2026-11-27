# Manual de usuario — KODEX Biblioteca (UTS)

**Versión:** 1.0 · **Fecha:** 26 de noviembre de 2026  
**Aplicación final:** https://kodex-biblioteca.onrender.com  
**Versión avance:** https://kodex-biblioteca-avance.onrender.com  
**Acceso local (Spring Tool Suite):** http://localhost:8094

---

## 1. Qué es KODEX

KODEX es un sistema integral de gestión de biblioteca para la Universitaria Tecnológica de Santander. Cubre seis perfiles de usuario, catálogo, ejemplares físicos, lectores, préstamos, devoluciones, renovaciones, reservas, multas, reportes y configuración operativa.

> **Importante (Render gratis):** los servicios se duermen después de ~15 min sin actividad. El primer acceso puede tardar cerca de 1 minuto en despertar. La base de datos PostgreSQL gratuita caduca a los 30 días.

---

## 1.1 Guía rápida paso a paso

### A) Prestar un libro disponible (rol BIBLIOTECARIO)

1. Abra **https://kodex-biblioteca.onrender.com** (o `http://localhost:8094` en local). Si tarda, espere: Render está despertando el servidor.
2. En la portada pulse **Ingresar al Sistema** (arriba a la derecha) o abra directamente `/login`.
3. En **Correo Electrónico** escriba `bibliotecario@kodex.com`.
4. En **Contraseña** escriba `kodex123`.
5. Pulse **Ingresar al Sistema**. El sistema lo lleva al **Dashboard Central**.
6. En el menú lateral, bajo **CIRCULACIÓN**, pulse **Préstamos**.
7. En la pantalla "Control de Préstamos y Circulación", pulse el botón azul **Registrar Préstamo**.
8. En **Lector Beneficiario**, despliegue la lista y elija un lector. Sólo aparecen los lectores activos.
9. En **Ejemplar Disponible**, despliegue la lista y elija una copia. Sólo aparecen los ejemplares con estado `DISPONIBLE`.
10. Verifique la fecha límite sugerida (7 días por defecto) y pulse **Confirmar Préstamo**.
11. Aparece el mensaje verde: **“Préstamo registrado exitosamente. Fecha límite de devolución: …”**. El préstamo queda listado con estado **Activo**.

**Para devolverlo:** en la misma pantalla **Préstamos**, busque la fila del préstamo y pulse **Devolver**. Verá **“Devolución registrada exitosamente… El ejemplar queda DISPONIBLE”**.  
**Para renovarlo:** pulse **Renovar** en la fila correspondiente; se amplía la fecha límite.

> Un **ADMIN** (`admin@kodex.com` / `kodex123`) hace exactamente lo mismo. Un **LECTOR** no puede prestar: si abre `/prestamos/nuevo` ve una pantalla 403. Debe pedirle el libro al bibliotecario.

### B) Solicitar una reserva (rol LECTOR, sólo si no hay copias)

1. Ingrese con `lector@kodex.com` / `kodex123`. El sistema lo lleva a **Mi Perfil – Portal de Lectores** (no al dashboard).
2. En el menú **MI CUENTA**, pulse **Catálogo General**.
3. Busque el libro. Si dice **0 DISPONIBLE(S)**, pulse **Ver Ubicaciones** y luego **Reservar**.
4. Si el libro tiene copias disponibles, el botón de reserva no aparece o es rechazado con: *“El libro tiene ejemplares DISPONIBLES en sala. Puede acercarse al bibliotecario para un préstamo directo.”* Eso es correcto: para esos libros se usa el paso A.
5. Consulte el estado de su solicitud en **Mis Reservas**.

### C) Registrar el pago de una multa (rol TESORERO)

1. Ingrese con `tesorero@kodex.com` / `kodex123`.
2. En el menú lateral pulse **Multas**.
3. Filtre si desea por estado o rango de fechas de pago.
4. En la fila de la multa pendiente pulse **Registrar Pago**. La multa pasa a **Pagada** y se asienta la fecha de pago.

### D) Consultar un reporte (rol GERENTE o ADMIN)

1. Ingrese con `gerente@kodex.com` / `kodex123` (o con `admin@kodex.com`).
2. En el menú lateral pulse **Reportes**.
3. Elija **Reporte de Préstamos** o **Reporte de Multas**.
4. Ajuste **Desde**, **Hasta** y, si aplica, **Estado**, y pulse **Generar Reporte** / **Consultar**.

---

## 2. Usuarios de demostración

Contraseña para todos: **`kodex123`**

| Correo | Rol | Pantalla de inicio | Módulos permitidos |
|---|---|---|---|
| `admin@kodex.com` | ADMIN | `/dashboard` | Todos |
| `bibliotecario@kodex.com` | BIBLIOTECARIO | `/dashboard` | Catálogo, Lectores, Préstamos, Reservas |
| `catalogador@kodex.com` | CATALOGADOR | `/dashboard` | Catálogo, Categorías, Libros, Ejemplares |
| `tesorero@kodex.com` | TESORERO | `/dashboard` | Catálogo, Multas y Caja |
| `gerente@kodex.com` | GERENTE | `/dashboard` | Catálogo, Reportes |
| `lector@kodex.com` | LECTOR | `/portal` | Portal personal y Catálogo |

---

## 3. Inicio de sesión y cierre de sesión

1. Abra la URL de la aplicación.
2. En la portada pública, pulse **Ingresar al Sistema** o abra `/login`.
3. Escriba el correo y la contraseña y pulse **Ingresar al Sistema**.
   - ADMIN, BIBLIOTECARIO, CATALOGADOR, TESORERO y GERENTE son llevados a `/dashboard`.
   - LECTOR es llevado a `/portal` (no tiene dashboard administrativo).
4. Para salir, pulse **Salir** en la barra superior. La sesión se invalida y vuelve a `/login`.

**Credenciales inválidas:** aparece el mensaje *“Correo o contraseña incorrectos”*.  
**Sin permiso:** al intentar una ruta ajena al rol se muestra la pantalla **403 – Acceso Restringido**, con el correo y los roles activos.

---

## 4. Panel por rol

La barra lateral y la parte superior muestran sólo las opciones permitidas para el rol.

| Módulo | Ruta | Roles |
|---|---|---|
| Dashboard | `/dashboard` | ADMIN, BIBLIOTECARIO, CATALOGADOR, TESORERO, GERENTE |
| Consultar catálogo | `/catalogo` | Todos los roles (incluido LECTOR) |
| Categorías | `/categorias` | ADMIN, CATALOGADOR |
| Libros | `/libros` | ADMIN, CATALOGADOR |
| Ejemplares físicos | `/ejemplares` | ADMIN, CATALOGADOR |
| Lectores | `/lectores` | ADMIN, BIBLIOTECARIO |
| Préstamos | `/prestamos` | ADMIN, BIBLIOTECARIO |
| Reservas | `/reservas` | ADMIN, BIBLIOTECARIO |
| Multas y caja | `/multas` | ADMIN, TESORERO |
| Reportes | `/reportes/prestamos`, `/reportes/multas` | ADMIN, GERENTE |
| Usuarios y roles | `/usuarios` | ADMIN |
| Configuración general | `/configuracion` | ADMIN |
| Portal del lector | `/portal` | LECTOR |

---

## 5. Uso por módulo

### 5.1 Dashboard
Muestra indicadores globales: libros, ejemplares disponibles, lectores activos, préstamos activos, vencidos, reservas y multas pendientes. Es la pantalla inicial de los roles administrativos.

### 5.2 Catálogo (todos los roles)
- Busque por título, autor o ISBN en el cuadro de búsqueda y pulse **Buscar**.
- Filtre por categoría.
- Cada tarjeta muestra el número de ejemplares disponibles.
- **Ver Ubicaciones** abre `/catalogo/detalle/{id}` con código de inventario, estante/pasillo y estado de cada copia.
- El rol LECTOR ve además **Mis Préstamos**, **Mis Reservas** y **Mis Multas** en su menú **MI CUENTA**.

### 5.3 Categorías (ADMIN / CATALOGADOR)
- **Nueva Categoría:** nombre único y descripción opcional.
- **Editar** desde el listado.
- **Eliminar** desde el listado (se muestra confirmación).

### 5.4 Libros (ADMIN / CATALOGADOR)
- Los campos obligatorios son ISBN único, categoría, título, autor, editorial y año.
- **Detalle** muestra metadatos y ejemplares asociados.
- **Eliminar** sólo si no tiene dependencias que lo impidan.

### 5.5 Ejemplares físicos (ADMIN / CATALOGADOR)
- Cada copia lleva un código de inventario único, ubicación física y estado (`DISPONIBLE`, `PRESTADO`, `RESERVADO`, `DAÑADO`, etc.).
- En el listado se puede filtrar por estado y cambiar el estado de una copia.

### 5.6 Lectores (ADMIN / BIBLIOTECARIO)
- Registro con documento único, nombre, correo, teléfono y estado (`ACTIVO` / `INACTIVO` / `SANCIONADO`).
- **Editar** datos, **Ver historial** (préstamos, reservas y multas) y activar/desactivar sanción.

### 5.7 Préstamos (ADMIN / BIBLIOTECARIO)
**Registrar préstamo** en `/prestamos/nuevo`:
1. Seleccione un lector activo (sólo aparecen lectores habilitados).
2. Seleccione un ejemplar disponible (sólo aparecen copias en estado `DISPONIBLE`).
3. Pulse **Confirmar Préstamo**.

El sistema valida automáticamente: que el lector esté activo, que no tenga multas pendientes, que no supere el máximo de 3 préstamos activos y que la copia esté disponible. La fecha límite se calcula con el plazo de la configuración (7 días por defecto).

**Operaciones desde el listado:**
- **Renovar:** amplía la fecha límite (validando reglas de renovación).
- **Devolver:** marca la devolución, deja la copia disponible y genera una multa si hay retraso.

> **Nota de diseño:** los lectores NO registran préstamos por sí mismos. Deben acercarse al bibliotecario con un ejemplar disponible. Si intentan abrir `/prestamos/nuevo`, reciben 403. Esto es intencional y está confirmado por las pruebas.

### 5.8 Reservas (ADMIN / BIBLIOTECARIO y portal del lector)
- Se pueden reservar **sólo libros sin ejemplares disponibles**. Si hay copias en sala, el sistema rechaza la reserva e invita a registrar el préstamo directo.
- La vigencia por defecto es de 5 días (parámetro configurable).
- Desde el listado se pueden **Cancelar** reservas.

### 5.9 Multas y caja (ADMIN / TESORERO)
- Listado filtrable por estado y rango de fechas de pago.
- **Registrar Pago** cambia la multa a `PAGADA` y asienta la fecha de pago.

### 5.10 Reportes (ADMIN / GERENTE)
- **Reporte de préstamos:** filtra por rango de fechas y estado, muestra total del periodo y ranking de los 5 libros más solicitados.
- **Reporte de multas:** filtra por fecha de pago, muestra recaudo del periodo, total histórico y cartera pendiente.

> **Estado de pruebas:** se detectó un error 500 en ambos reportes y ya fue corregido y publicado. `ReporteController` calcula los agregados y filtra en memoria, evitando enviar parámetros `null` a PostgreSQL en predicados JPQL. Verificado con HTTP 200 en las dos versiones desplegadas.

### 5.11 Usuarios y roles (ADMIN)
- Alta de usuarios con nombre, correo (login), contraseña, rol y lector vinculado (obligatorio para `LECTOR`).
- **Editar** permite cambiar contraseña (en blanco la conserva), rol y activación.
- **Activar/Desactivar** usuario desde el listado.

### 5.12 Configuración general (ADMIN)
Parámetros operativos:
- Nombre de la institución/biblioteca.
- Plazo de préstamo regular (días).
- Máximo de préstamos activos por lector.
- Valor de multa diaria (COP).
- Vigencia de reservas (días).

### 5.13 Portal del lector (LECTOR)
- **Mi Perfil:** datos del lector vinculado.
- **Mis Préstamos:** préstamos propios con estado y fechas.
- **Mis Reservas:** solicitudes propias.
- **Mis Multas:** multas propias con valor y estado.
- Desde el catálogo, si un libro no tiene copias disponibles, puede solicitar reserva mediante **Reservar**.

---

## 6. Resultado de las pruebas funcionales

Se ejecutaron pruebas automatizadas sobre **https://kodex-biblioteca.onrender.com** con los seis roles.

### 6.1 Navegación y control de acceso (matriz de rutas)

| Rol | Rutas permitidas verificadas (HTTP 200) | Rutas denegadas verificadas (HTTP 403) |
|---|---|---|
| ADMIN | `/`, `/dashboard`, `/catalogo`, detalle de catálogo, `/categorias`, `/libros`, `/ejemplares`, `/lectores`, `/prestamos`, `/reservas`, `/multas`, `/usuarios`, `/configuracion` | `/portal` |
| CATALOGADOR | `/dashboard`, `/catalogo`, `/categorias`, `/libros`, `/ejemplares` | `/prestamos`, `/multas`, `/reportes/prestamos`, `/usuarios` |
| BIBLIOTECARIO | `/dashboard`, `/catalogo`, `/lectores`, `/prestamos`, `/reservas` | `/multas`, `/reportes/prestamos`, `/usuarios` |
| TESORERO | `/dashboard`, `/catalogo`, `/multas` | `/prestamos`, `/reportes/prestamos`, `/usuarios` |
| GERENTE | `/dashboard`, `/catalogo` | `/multas`, `/prestamos` |
| LECTOR | `/portal`, `/portal/prestamos`, `/portal/reservas`, `/portal/multas`, `/catalogo`, detalle de catálogo | `/dashboard`, `/prestamos/nuevo`, `/usuarios` |

### 6.2 Operaciones probadas

| Operación | Resultado |
|---|---|
| Inicio de sesión de los 6 roles | Correcto; redirección según rol |
| Cierre de sesión | Correcto; sesión invalidada |
| Registrar préstamo (bibliotecario) | **Correcto**: “Préstamo registrado exitosamente. Fecha límite 2026-12-04” |
| Renovar préstamo | **Correcto**: nueva fecha límite 2026-12-11 |
| Devolver préstamo | **Correcto**: “El ejemplar queda DISPONIBLE (SDD-11)” |
| Crear categoría | Correcto: “Categoría guardada exitosamente” |
| Eliminar categoría | Correcto: “Categoría eliminada exitosamente” |
| Guardar configuración (sin cambios) | Correcto: “Configuración del sistema actualizada correctamente” |
| Crear reserva de libro disponible | **Rechazo correcto**: “El libro cuenta con 1 ejemplar DISPONIBLE… puede solicitarse como préstamo” |
| Cancelar reserva | Correcto: “Reserva CANCELADA exitosamente (SDD-17)” |
| Lector solicita reserva de libro disponible | **Rechazo correcto**: “El libro tiene ejemplares DISPONIBLES en sala…” |
| Lector abre `/prestamos/nuevo` | **403 esperado** (diseño por rol) |
| Reportes (prestamos y multas) | **Correcto tras la corrección**: HTTP 200 en final y avance |

### 6.3 Hallazgos

1. **Acceso a “prestar libros disponibles”:** el préstamo funciona correctamente con ADMIN y BIBLIOTECARIO. Un LECTOR no puede entrar a `/prestamos/nuevo` (403) porque la regla de negocio del proyecto reserva esa operación al bibliotecario. El catálogo del lector muestra **Reservar** sólo cuando no hay copias disponibles. No es un error de código, sino el diseño de permisos.
2. **Reportes 500 (resuelto):** ocurría en `/reportes/prestamos` y `/reportes/multas`. Causa confirmada en logs: `ERROR: could not determine data type of parameter $N` de PostgreSQL, provocada por predicados JPQL `(:param IS NULL OR ...)` con parámetros `null`. Corrección: filtrar y agregar en Java dentro de `ReporteController`, sin enviar esos parámetros a la BD. Verificado HTTP 200 en ambas versiones.
3. **Datos de prueba:** durante las pruebas se crearon y eliminaron registros temporales; el préstamo de prueba fue renovado y devuelto, quedando el ejemplar disponible nuevamente.

---

## 7. Estado de despliegue y repositorios

| Versión | Repositorio | Rama publicada | Commit de corrección | URL |
|---|---|---|---|---|
| Final | `Jancarlos-2004/kodex-biblioteca-render` | `main` | `3bac568` | https://kodex-biblioteca.onrender.com |
| Avance | `Jancarlos-2004/kodex-biblioteca-avance` | `deploy` | `c05f671` | https://kodex-biblioteca-avance.onrender.com |

Ambos servicios comparten la base de datos PostgreSQL gratuita **`kodex-db`** de Render. El proyecto final usa Docker + PostgreSQL (no MySQL) porque Render no ejecuta Java de forma nativa.

Para publicar cambios en el futuro:

```bash
git add .
git commit -m "Descripción del cambio"
git push origin main        # repositorio final
# o
git push origin deploy      # rama publicada del repositorio avance
```

Si Render no despliega automáticamente, entrar al panel del servicio y pulsar **Manual Deploy → Deploy latest commit**.

---

## 8. Ejecución local con Spring Tool Suite

1. Abra STS (extraído en `C:\Spring\sts-5.4.0.RELEASE`).
2. `File → Open Projects from File System…` y seleccione la carpeta del proyecto KODEX que contiene `pom.xml`.
3. Inicie XAMPP/MySQL en `localhost:3306` e importe `database/kodex_db_setup.sql`.
4. Ejecute `KodexApplication` (clase `com.kodex.KodexApplication`).
5. Abra http://localhost:8094.

La configuración local usa MySQL `kodex_db`, usuario `root`, contraseña vacía. La configuración de Render usa variables `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER` y `PGPASSWORD`.
