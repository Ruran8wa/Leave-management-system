# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# RUN mvn dependency:go-offline 
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:26-ea-jdk AS ready
WORKDIR /app
COPY --from=build /app/target/*.jar ./app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]