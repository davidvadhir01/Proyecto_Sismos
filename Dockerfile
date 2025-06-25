FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/ProyectoFinal-*.jar app.jar

ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_USER=admin
ENV DB_PASSWORD=admin
ENV DB_NAME=sismos
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]