# Dockerfile (Maven)
# 1) Etapa de build: compila el JAR
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# 2) Etapa de runtime: imagen ligera para ejecutar
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copia el JAR generado
COPY --from=build /app/target/*.jar app.jar

# Variables de entorno (opcional)
ENV JAVA_OPTS=""
# Puerto que expone tu app (ajústalo si usas otro)
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
