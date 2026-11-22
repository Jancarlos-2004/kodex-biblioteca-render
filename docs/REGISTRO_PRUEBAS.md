# KODEX — REGISTRO DE PRUEBAS FUNCIONALES (BDD Given / When / Then)

**Proyecto:** KODEX - Sistema Integral de Gestión de Biblioteca con Préstamos  
**Marco de Verificación:** SDD (Specification-Driven Development) & BDD  
**Entorno de Ejecución:** Spring Boot 3.2.5, Java 17, Spring Data JPA, MySQL  

---

## Escenarios de Prueba Ejecutados y Verificados

### 1. SDD-01: Autenticación y Control de Sesión
- **Escenario 1.1: Inicio de sesión válido para bibliotecario**
  - **Given:** Un usuario con correo `bibliotecario@kodex.com` y rol `BIBLIOTECARIO`.
  - **When:** Ingresa clave `kodex123`.
  - **Then:** Accede correctamente al Dashboard central con acceso a los módulos de Lectores, Préstamos y Reservas.
  - **Resultado:** **APROBADO**.
- **Escenario 1.2: Redirección automática de rol LECTOR**
  - **Given:** Un usuario con correo `lector@kodex.com` y rol `LECTOR`.
  - **When:** Inicia sesión con credenciales válidas.
  - **Then:** El manejador `AuthenticationSuccessHandler` lo redirige de inmediato a `/portal` (Portal de Lectores).
  - **Resultado:** **APROBADO**.
- **Escenario 1.3: Credenciales incorrectas**
  - **Given:** Usuario en pantalla de login.
  - **When:** Ingresa clave errónea.
  - **Then:** El sistema rechaza el acceso y muestra alerta: *"Credenciales incorrectas o usuario desactivado"*.
  - **Resultado:** **APROBADO**.

---

### 2. SDD-02: Creación y Edición de Libros con ISBN Único
- **Escenario 2.1: Registro de libro válido**
  - **Given:** Formulario de registro de libro.
  - **When:** Se ingresan ISBN `978-0134685991`, título `"Effective Java"`, autor Joshua Bloch y categoría válida.
  - **Then:** El libro queda registrado y aparece disponible en el catálogo.
  - **Resultado:** **APROBADO**.
- **Escenario 2.2: Intento de registro con ISBN duplicado**
  - **Given:** Un libro ya existente con ISBN `978-0134685991`.
  - **When:** El catalogador intenta crear otro libro con ese mismo ISBN.
  - **Then:** El sistema rechaza la operación señalando *"El ISBN ya está registrado en el catálogo"*.
  - **Resultado:** **APROBADO**.

---

### 3. SDD-03 y SDD-04: Gestión de Ejemplares y Estados
- **Escenario 3.1: Registro de ejemplar nuevo**
  - **Given:** Libro registrado.
  - **When:** Se crea ejemplar con código de inventario `"KDX-LIB-001-A"`.
  - **Then:** El ejemplar queda persistido en estado `DISPONIBLE` y vinculado al libro.
  - **Resultado:** **APROBADO**.
- **Escenario 3.2: Bloqueo de préstamo para ejemplar EN_REPARACION**
  - **Given:** Un ejemplar con estado `EN_REPARACION` o `BAJA`.
  - **When:** El bibliotecario consulta el formulario de préstamo.
  - **Then:** El selector no incluye copias en reparación, y si se fuerza el envío, el controlador lo rechaza con mensaje de error.
  - **Resultado:** **APROBADO**.

---

### 4. SDD-05: Registro de Lectores y Validación de Documento Único
- **Escenario 4.1: Guardar nuevo lector con documento único**
  - **Given:** Formulario de lector con documento `"1098765432"`, nombre `"Carlos Pérez"`.
  - **When:** Se envía el formulario.
  - **Then:** El lector se almacena con estado `ACTIVO`.
  - **Resultado:** **APROBADO**.
- **Escenario 4.2: Rechazo de documento duplicado**
  - **Given:** Un lector ya registrado con documento `"1098765432"`.
  - **When:** Se intenta registrar otro lector con la misma cédula.
  - **Then:** El sistema invalida el formulario y muestra *"El documento ya se encuentra registrado para otro lector"*.
  - **Resultado:** **APROBADO**.

---

### 5. SDD-06, SDD-07, SDD-08, SDD-09: Reglas de Creación de Préstamo
- **Escenario 5.1: Creación de préstamo regular**
  - **Given:** Lector activo sin sanciones ni multas, y ejemplar disponible.
  - **When:** Se registra el préstamo.
  - **Then:** El préstamo se guarda en estado `ACTIVO`, con fecha límite calculada a 7 días, y el ejemplar cambia a `PRESTADO`.
  - **Resultado:** **APROBADO**.
- **Escenario 5.2: Doble préstamo bloqueado (SDD-07)**
  - **Given:** Ejemplar `"KDX-LIB-002-A"` en estado `PRESTADO`.
  - **When:** Se intenta prestar a otro lector.
  - **Then:** El sistema rechaza la solicitud porque el ejemplar no está DISPONIBLE.
  - **Resultado:** **APROBADO**.
- **Escenario 5.3: Límite máximo de 3 préstamos activos (SDD-08)**
  - **Given:** Un lector con 3 préstamos activos.
  - **When:** Se intenta registrar un cuarto préstamo.
  - **Then:** El controlador rechaza la operación indicando que se alcanzó el tope máximo permitido.
  - **Resultado:** **APROBADO**.
- **Escenario 5.4: Bloqueo por multas pendientes o lector sancionado (SDD-09)**
  - **Given:** Lector que presenta 1 multa en estado `PENDIENTE`.
  - **When:** Se intenta registrar un nuevo préstamo.
  - **Then:** Se bloquea el préstamo indicando *"El lector tiene multas PENDIENTES. Debe estar a paz y salvo"*.
  - **Resultado:** **APROBADO**.

---

### 6. SDD-10: Renovación de Préstamos
- **Escenario 6.1: Primera renovación exitosa**
  - **Given:** Préstamo activo con 0 renovaciones previas y sin reservas del libro.
  - **When:** Se pulsa "Renovar".
  - **Then:** La fecha límite se extiende 7 días más y el contador de renovaciones pasa a 1.
  - **Resultado:** **APROBADO**.
- **Escenario 6.2: Intento de segunda renovación rechazada**
  - **Given:** Préstamo ya renovado 1 vez.
  - **When:** Se intenta renovar por segunda vez.
  - **Then:** El botón está deshabilitado en interfaz y el backend rechaza la petición con alerta.
  - **Resultado:** **APROBADO**.

---

### 7. SDD-11 y SDD-12: Devolución y Cálculo Automático de Multas
- **Escenario 7.1: Devolución a tiempo**
  - **Given:** Préstamo con fecha límite día 15 devuelto el día 14.
  - **When:** Se registra la devolución.
  - **Then:** El préstamo queda `DEVUELTO`, el ejemplar pasa a `DISPONIBLE` y NO se genera multa.
  - **Resultado:** **APROBADO**.
- **Escenario 7.2: Devolución tardía con generación de multa**
  - **Given:** Préstamo con fecha límite día 10 devuelto el día 13 (3 días de mora).
  - **When:** Se registra la devolución.
  - **Then:** El préstamo queda `DEVUELTO`, el ejemplar se libera y se crea automáticamente un registro en `multa` por $3.000 (3 días x $1.000) en estado `PENDIENTE`. El lector queda temporalmente sancionado.
  - **Resultado:** **APROBADO**.

---

### 8. SDD-13 y SDD-14: Pago de Multas y Prevención de Cobro Duplicado
- **Escenario 8.1: Pago total de multa en caja**
  - **Given:** Multa en estado `PENDIENTE` por $3.000.
  - **When:** El Tesorero registra el pago.
  - **Then:** La multa cambia a `PAGADA`, se estampa la fecha de pago y el lector se rehabilita a `ACTIVO`.
  - **Resultado:** **APROBADO**.
- **Escenario 8.2: Intento de cobro duplicado (SDD-14)**
  - **Given:** Multa ya pagada.
  - **When:** Se intenta registrar un nuevo pago.
  - **Then:** El sistema rechaza la acción informando que ya fue saldada previamente.
  - **Resultado:** **APROBADO**.

---

### 9. SDD-15 y SDD-16: Búsqueda y Reserva de Libros
- **Escenario 9.1: Consulta con cálculo de copias disponibles**
  - **Given:** Catálogo con 5 libros.
  - **When:** El usuario busca por título o categoría.
  - **Then:** La tabla muestra cuántas copias están disponibles para préstamo inmediato.
  - **Resultado:** **APROBADO**.
- **Escenario 9.2: Reserva permitida solo cuando no hay ejemplares disponibles (SDD-16)**
  - **Given:** Libro con 0 ejemplares disponibles (todos prestados o en mantenimiento).
  - **When:** Un lector activo solicita la reserva.
  - **Then:** Se genera la reserva en estado `ACTIVA` con vencimiento a 5 días.
- **Escenario 9.3: Rechazo de reserva cuando existen copias en sala**
  - **Given:** Libro con 2 ejemplares disponibles.
  - **When:** Se intenta generar una reserva.
  - **Then:** El sistema rechaza la solicitud indicando que debe realizarse un préstamo directo.
  - **Resultado:** **APROBADO**.

---

### 10. SDD-18 y SDD-19: Reportes Administrativos
- **Escenario 10.1: Reporte de préstamos y ranking de demanda**
  - **Given:** Rango de fechas seleccionado (mes actual).
  - **When:** Se genera el informe de préstamos.
  - **Then:** Se listan únicamente los préstamos del periodo y se genera la tabla con los libros más prestados.
  - **Resultado:** **APROBADO**.
- **Escenario 10.2: Reporte de multas y total recaudado**
  - **Given:** Rango de fechas seleccionado.
  - **When:** Se consulta el reporte de multas.
  - **Then:** Se muestran las multas pagadas y la suma total recaudada calculada con precisión decimal.
  - **Resultado:** **APROBADO**.

---

### 11. SDD-20 y SDD-24: Seguridad por Roles y Portal de Lectores
- **Escenario 11.1: Restricción de acceso a configuración de usuarios**
  - **Given:** Usuario con rol `BIBLIOTECARIO` autenticado.
  - **When:** Intenta navegar a `/usuarios` o `/configuracion`.
  - **Then:** Es interceptado por Spring Security y dirigido a `/error/403` con mensaje claro de permisos insuficientes.
  - **Resultado:** **APROBADO**.
- **Escenario 11.2: Aislamiento de datos en Portal de Lectores (SDD-24)**
  - **Given:** Lector Carlos Pérez autenticado.
  - **When:** Accede a `/portal/prestamos` y `/portal/multas`.
  - **Then:** Solo visualiza sus propios registros sin acceso a datos de otros lectores.
  - **Resultado:** **APROBADO**.
