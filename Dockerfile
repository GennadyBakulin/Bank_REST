FROM openjdk:17-jdk-alpine

WORKDIR /app

# Копируем JAR файл приложения
COPY target/*.jar bankcards-1.0.0.jar

# Открываем порт приложения
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "bankcards-1.0.0.jar"]