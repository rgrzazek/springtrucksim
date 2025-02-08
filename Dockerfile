# Use an official OpenJDK image as the base image
FROM amazoncorretto:21-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container
COPY build/libs/spring-truck-sim-0.0.1-SNAPSHOT.jar app.jar

# Expose the port that the Spring Boot app will run on
EXPOSE 8080

# Run the JAR file when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
