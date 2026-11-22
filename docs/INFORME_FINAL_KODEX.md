# INFORME FINAL DE ENTREGA — PROYECTO KODEX

**Sistema Integral de Gestión de Biblioteca con Préstamos**  
**Institución:** Unidades Tecnológicas de Santander (UTS)  
**Asignatura:** Programación en Java  
**Docente:** Julián Barney Jaimes Rincón  
**Estudiantes:** Jan Carlos Galvis Solano & Anphernee Hooker Sanchez  
**Desarrollador Scrum (SDD):** Paola Velasco Manrique  
**Fecha:** Noviembre de 2026  

---

## 1. Resumen Ejecutivo del Proyecto

El proyecto **KODEX** ha sido completado y puesto a punto en su totalidad, transformando las especificaciones formales contenidas en el documento `KODEX.pdf` en un sistema web robusto, seguro, adaptable y completamente funcional.

Se construyó una solución basada en **Spring Boot 3 MVC**, **Spring Data JPA**, **MySQL**, **Thymeleaf**, **Spring Security** y **Bootstrap 5**, respetando rigurosamente todas y cada una de las directrices académicas y de negocio:
- **Cumplimiento estricto de las 24 especificaciones funcionales (SDD-01 a SDD-24).**
- **Cumplimiento de los requisitos no funcionales (NFR-01 a NFR-07).**
- **Soporte pleno a los 6 roles definidos con permisos diferenciados:** Administrador General, Bibliotecario, Catalogador, Tesorero, Gerente y Lector.
- **Cumplimiento de las restricciones arquitectónicas académicas:**
  - Cero `@RestController` (no APIs REST, todo MVC server-side rendering).
  - Cero Servlets programados manualmente.
  - Cero capa Service intermedia: Los controladores `@Controller` interactúan de manera directa, limpia y segura con las interfaces `JpaRepository`.
  - Persistencia exclusiva en **MySQL** (descartando H2 y MongoDB).

---

## 2. Decisiones de Diseño y Trazabilidad de Preguntas Abiertas del PDF

Durante el análisis del documento se identificaron dudas conceptuales marcadas como `[NECESITA ACLARACIÓN]`. Se resolvieron de forma coherente conservando la trazabilidad formal:

1. **Relación Usuario - Lector para el Portal de Lectores (SDD-24):**
   - *Problema:* El PDF preguntaba si un lector debía tener un usuario asociado.
   - *Solución implementada:* Se integró una relación opcional `@OneToOne` entre `Usuario` y `Lector`. Al autenticarse un usuario con rol `LECTOR`, el sistema detecta su perfil y lo redirige automáticamente a su portal privado (`/portal`), permitiéndole consultar exclusivamente sus préstamos, multas y reservas, además de reservar libros agotados en sala.
2. **Estado RESERVADO de Ejemplares vs Reserva del Libro (SDD-16):**
   - *Problema:* Definir si `RESERVADO` es un estado del ejemplar físico o de la reserva.
   - *Solución implementada:* La reserva se efectúa a nivel del título bibliográfico (`ReservaLibro` asociado a `Libro`), cumpliendo la regla de que solo es permitida cuando el libro no tenga ejemplares disponibles en sala. El estado `RESERVADO` en el ejemplar queda habilitado para cuando un libro reservado se devuelve o se aparta en mostrador.
3. **Parámetros del Sistema (Plazos y Multas):**
   - *Problema:* Confirmar plazo de préstamo (7 días), máximo de préstamos activos (3), valor diario de multa ($1.000 COP) y vigencia de reserva (5 días).
   - *Solución implementada:* Se creó la entidad y módulo de `Configuracion`, editable por el Administrador General, permitiendo cambiar dinámicamente estos valores sin recompilar ni alterar el código fuente.
4. **Levantamiento automático de sanciones:**
   - Al registrarse el pago total de la última multa pendiente de un lector moroso, el sistema comprueba que no existan más deudas y restablece automáticamente su estado a `ACTIVO`.

---

## 3. Arquitectura y Módulos Desarrollados

### 3.1 Módulos Implementados
1. **Seguridad y Acceso:** Formulario de login corporativo, hash seguro con BCrypt, cierre de sesión seguro, control de acceso por roles y manejador de error 403 Forbidden.
2. **Dashboard Ejecutivo:** Tarjetas de estadísticas en tiempo real (Títulos, Disponibles, Préstamos Activos, Multas, Préstamos Vencidos, Recaudo Histórico) y tablas de préstamos recientes y libros más demandados.
3. **Usuarios y Roles (Admin):** CRUD completo de cuentas, asignación de los 6 roles, vinculación a lectores y activación/desactivación de cuentas.
4. **Configuración General (Admin):** Ajuste de parámetros operativos de biblioteca.
5. **Categorías (Catalogador / Admin):** Creación, edición, listado y eliminación protegida (solo si no tiene libros asociados).
6. **Libros (Catalogador / Admin):** Catálogo bibliográfico con control de ISBN único y consulta de copias.
7. **Ejemplares Físicos (Catalogador / Admin):** Gestión de código de inventario único, ubicación en sala y cambio de estados (DISPONIBLE, EN_REPARACION, BAJA).
8. **Lectores (Bibliotecario / Admin):** Registro con cédula/documento único, detalle con pestañas de historial de préstamos, multas y reservas.
9. **Circulación y Préstamos (Bibliotecario / Admin):**
   - Validación de disponibilidad de ejemplar.
   - Bloqueo por lector sancionado o con multas pendientes (SDD-09).
   - Bloqueo por superar límite de préstamos activos (SDD-08).
   - Renovación con límite de 1 vez y bloqueo si existe reserva activa (SDD-10).
   - Devolución con liberación automática de ejemplar (SDD-11) y cálculo automático de multa por días de mora (SDD-12).
10. **Finanzas y Multas (Tesorero / Admin):** Listado con filtros, registro de pago en caja y bloqueo de cobro duplicado (SDD-13, SDD-14).
11. **Reservas de Libros (Bibliotecario / Admin / Lector):** Solicitud condicionada a 0 ejemplares disponibles (SDD-16), cancelación y expiración por fecha (SDD-17).
12. **Consulta del Catálogo (Todos los Roles):** Buscador reactivo por título, autor o categoría con conteo de copias disponibles en tiempo real (SDD-15).
13. **Reportes Gerenciales (Gerente / Admin):**
   - Reporte de préstamos por fecha con ranking de libros más solicitados (SDD-18).
   - Reporte de multas con total recaudado en el periodo (SDD-19).
14. **Portal del Lector (Lector):** Autoservicio para ver préstamos activos, reservas y estado de cuenta personal (SDD-24).

---

## 4. Credenciales de Acceso para Pruebas

Todas las cuentas fueron precargadas con la contraseña: **`kodex123`**

| Rol | Correo Electrónico | Contraseña | Alcance Principal |
|---|---|---|---|
| **Administrador General** | `admin@kodex.com` | `kodex123` | Control total: Usuarios, Configuración, Catálogo, Préstamos, Multas, Reportes |
| **Bibliotecario** | `bibliotecario@kodex.com` | `kodex123` | Circulación: Lectores, Préstamos, Renovaciones, Devoluciones, Reservas |
| **Catalogador** | `catalogador@kodex.com` | `kodex123` | Inventario: Categorías, Libros, Ejemplares, Mantenimiento/Bajas |
| **Tesorero** | `tesorero@kodex.com` | `kodex123` | Caja: Cobro de multas pendientes, consulta de recaudos y balances |
| **Gerente** | `gerente@kodex.com` | `kodex123` | Auditoría: Reportes de préstamos, libros más prestados y recaudos financieros |
| **Lector** | `lector@kodex.com` | `kodex123` | Portal de autoservicio: Mis préstamos, mis reservas, mis multas, catálogo |

---

## 5. Instrucciones de Ejecución

### Prerrequisitos
- **Java 17 o 21** instalado y configurado en el `PATH`.
- Servidor **MySQL** ejecutándose en el puerto 3306 (por ejemplo XAMPP, WAMP o servicio nativo de MySQL).
- Usuario por defecto: `root` sin contraseña (o configurar en `src/main/resources/application.properties`).

### Pasos
1. **Crear o verificar la base de datos:**
   Ejecutar el script `database/kodex_db_setup.sql` en MySQL Workbench, phpMyAdmin o terminal:
   ```sql
   CREATE DATABASE IF NOT EXISTS `kodex_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. **Iniciar la aplicación:**
   Desde la raíz del proyecto, ejecutar en terminal:
   ```bash
   mvn spring-boot:run
   ```
   *(O abrir el proyecto directamente en IntelliJ IDEA, Eclipse, VS Code o Antigravity IDE y ejecutar la clase principal `KodexApplication.java`).*
3. **Abrir en el Navegador:**
   Navegar a: **`http://localhost:8094/login`**
   Ingresar con cualquiera de las cuentas de prueba indicadas en la tabla anterior.
