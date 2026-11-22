# ---------- Etapa 1: compilar con Maven + JDK 17 ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Descargar dependencias primero (aprovecha la caché de Docker)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests clean package

# ---------- Etapa 2: imagen final ligera ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Render asigna el puerto real mediante la variable PORT,
# que es leída por application.properties (server.port=${PORT:8080}).
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
