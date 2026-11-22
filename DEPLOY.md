# Despliegue gratuito en Render

Este repositorio es la **versión final** preparada para Render.
La versión del avance (MySQL local / XAMPP) está en el repo `kodex-biblioteca-avance`.

## Cambios respecto al avance

- La app se ejecuta en un **Docker** con JDK 17 (Render no tiene runtime nativo de Java).
- La base de datos es **Render Postgres (plan free)** en vez de MySQL/XAMPP.
- El `pom.xml` usa `org.postgresql:postgresql` en lugar de `mysql-connector-j`.
- `application.properties` lee las credenciales de variables de entorno y el puerto de `PORT`.
- `DataInitializer` crea las tablas y los usuarios de prueba al primer arranque,
  así que **no hay que importar el SQL manualmente**.

## Pasos en Render

1. Crea cuenta en https://dashboard.render.com/register (plan Free).
2. Sube este repositorio a GitHub.
3. En el dashboard: **New → Blueprint** y conecta este repo.
   Render detectará `render.yaml` y creará automáticamente:
   - Web service `kodex-biblioteca` (Docker, plan free)
   - Base de datos `kodex-db` (Postgres, plan free)
4. Espera a que el deploy termine (primera vez: 5-10 min).
5. Abre la URL que muestra Render, por ejemplo:
   `https://kodex-biblioteca.onrender.com`

## Usuarios de prueba

Contraseña para todos: **kodex123**

- `admin@kodex.com`
- `bibliotecario@kodex.com`
- `catalogador@kodex.com`
- `tesorero@kodex.com`
- `gerente@kodex.com`
- `lector@kodex.com`

## Limitaciones del plan gratuito

- El servicio se **duerme tras 15 minutos** sin visitas; la primera visita siguiente
  tarda ~1 minuto en despertar.
- La base de datos free **caduca a los 30 días** (Render avisa por correo).
- Solo una base de datos free por workspace.
- No es adecuado para producción; para un proyecto de clase está bien.

## Desarrollo local (opcional)

```bash
mvn spring-boot:run
```

Con las variables por defecto intentará conectar a `localhost:5432`.
Para usar tu MySQL local, cambia el perfil/propiedades o usa el repo del avance.
