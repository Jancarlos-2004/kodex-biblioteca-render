# KODEX — Sistema Integral de Gestión de Biblioteca con Préstamos

Proyecto académico desarrollado para la asignatura **Programación en Java** de las **Unidades Tecnológicas de Santander (UTS)**.

---

## 🚀 Tecnologías Utilizadas

- **Java 17 / 21**
- **Spring Boot 3.2.5**
- **Spring MVC** (`@Controller` sin `@RestController`, conforme a restricción académica)
- **Spring Data JPA** (Inyección directa en controladores, sin capa Service intermedia)
- **MySQL** (Base de datos relacional)
- **Spring Security 6** (Autenticación y autorización basada en 6 roles, hash BCrypt)
- **Thymeleaf + Thymeleaf Extras Spring Security 6**
- **Bootstrap 5.3 + FontAwesome 6** (Interfaz moderna, profesional y adaptable)

---

## 👥 Roles del Sistema y Credenciales de Demostración

Contraseña unificada para todas las cuentas de prueba: **`kodex123`**

| Rol | Usuario (Email) | Contraseña | Funcionalidades |
|---|---|---|---|
| **ADMIN** | `admin@kodex.com` | `kodex123` | Administración total de usuarios, roles, parámetros del sistema y acceso global |
| **BIBLIOTECARIO** | `bibliotecario@kodex.com` | `kodex123` | Registro de lectores, préstamos, renovaciones, devoluciones y reservas |
| **CATALOGADOR** | `catalogador@kodex.com` | `kodex123` | Gestión de categorías, libros, ejemplares físicos y estados de inventario |
| **TESORERO** | `tesorero@kodex.com` | `kodex123` | Control de caja, cobro de multas y consulta de recaudos |
| **GERENTE** | `gerente@kodex.com` | `kodex123` | Consulta de reportes ejecutivos de préstamos, demanda y finanzas |
| **LECTOR** | `lector@kodex.com` | `kodex123` | Portal de autoservicio: mis préstamos, mis reservas, mis multas y catálogo |

---

## 📋 Reglas de Negocio Implementadas (SDD)

1. **ISBN y Código de Inventario Únicos:** Validación tanto en frontend como en JPA antes de persistir.
2. **Disponibilidad de Ejemplares:** Solo se pueden prestar ejemplares en estado `DISPONIBLE`.
3. **Prevención de Préstamos Duplicados:** Un ejemplar no puede tener dos préstamos activos al mismo tiempo.
4. **Límite de Préstamos Activos:** Máximo 3 préstamos por lector simultáneos (configurable).
5. **Bloqueo por Sanción y Multas:** Lectores con estado `SANCIONADO` o multas `PENDIENTE` no pueden recibir nuevos préstamos.
6. **Renovación Controlada:** Máximo 1 renovación (+7 días), condicionada a que no existan reservas activas del libro.
7. **Cálculo Automático de Multa por Retraso:** Si la entrega es posterior a la fecha límite, genera multa en base a los días de mora x tarifa diaria ($1.000).
8. **Prevención de Doble Pago:** Validación de no cobro sobre multas ya canceladas.
9. **Reserva Condicionada:** Solo se permite reservar un título cuando tenga 0 copias disponibles en sala.
10. **Portal Privado de Lectores:** El lector únicamente puede visualizar la información vinculada a su cuenta.

---

## 💻 Instrucciones para Ejecutar

1. Asegúrate de tener **MySQL** activo en el puerto local `3306`.
2. Crear la base de datos (o dejar que la aplicación la cree automáticamente):
   ```sql
   CREATE DATABASE IF NOT EXISTS `kodex_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Ejecutar la aplicación:
   ```bash
   mvn spring-boot:run
   ```
4. Abrir en el navegador:
   ```
   http://localhost:8094/login
   ```

---

## 📁 Documentación Técnica Incluida

- [Matriz de Trazabilidad Integral SDD (24 Requisitos + 7 NFRs)](file:///c:/Users/Usuario/Desktop/ANPHERNEE/Tercer%20parcial%20java/docs/MATRIZ_TRAZABILIDAD.md)
- [Registro de Pruebas Funcionales BDD (Given / When / Then)](file:///c:/Users/Usuario/Desktop/ANPHERNEE/Tercer%20parcial%20java/docs/REGISTRO_PRUEBAS.md)
- [Informe Final de Proyecto KODEX](file:///c:/Users/Usuario/Desktop/ANPHERNEE/Tercer%20parcial%20java/docs/INFORME_FINAL_KODEX.md)
- [Script SQL de Creación y Poblado](file:///c:/Users/Usuario/Desktop/ANPHERNEE/Tercer%20parcial%20java/database/kodex_db_setup.sql)
