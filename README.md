# Brokage Firm Case

A Spring Boot application for managing brokerage operations with JWT-based authentication.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Your favorite IDE

## Project Structure

The project uses:
- Spring Boot
- Spring Security for authentication
- Spring Data JPA for database operations
- H2 Database (in-memory)
- JWT for token-based authentication
- Lombok for reducing boilerplate code

## Building the Project

You can build the project using Maven:

```bash
# Using Maven wrapper
./mvnw clean install

# Or using Maven directly
mvn clean install
```

## Running the Application

You can run the application in several ways:

1. Using Maven:
```bash
./mvnw spring-boot:run
```

2. Running the JAR file:
```bash
java -jar target/brokage-0.0.1-SNAPSHOT.jar
```

The application will start on port 8080 by default.

## Docker Deployment

You can also run the application using Docker:

1. Build the Docker image:
```bash
docker build -t hkalkan/brokage-firm-case .
```

2. Run the container:
```bash
docker run -p 8080:8080 hkalkan/brokage-firm-case
```

The application will be accessible at `http://localhost:8080`.

## Testing

Run the tests using:
```bash
./mvnw test
```