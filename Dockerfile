FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copiar archivos pom.xml y descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el código fuente
COPY src ./src

# Empaquetar la aplicación
RUN mvn package -DskipTests

# Segunda etapa: solo el JRE y el archivo JAR
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copiar el archivo JAR de la etapa de construcción
COPY --from=build /app/target/*.jar app.jar

# Puerto expuesto
EXPOSE 8080

# Configuración del punto de entrada
ENTRYPOINT ["java", "-jar", "app.jar"]