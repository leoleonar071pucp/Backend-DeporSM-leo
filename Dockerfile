# Etapa 1: Construir el proyecto con Maven
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Ejecutar el .jar construido
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/deporsm-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
