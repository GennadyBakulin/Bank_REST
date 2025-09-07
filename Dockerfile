# Используем многостадийную сборку
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-alpine

WORKDIR /app

# Копируем JAR файл приложения
COPY --from=build /app/target/*.jar bankcards-1.0.0.jar

# Открываем порт приложения
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "bankcards-1.0.0.jar"]