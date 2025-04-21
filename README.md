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

You can run the application using Maven:

```bash
./mvnw spring-boot:run
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

## Usage

### Default Credentials

The default credentials for the admin user are:
- Username: `admin`
- Password: `password`

These values must be moved to environment variables for secure storage, especially for production deployment.

### Customer Signup & Login

New customers can sign up through the `/auth/signup` endpoint. After signing up, a bearer token can be generated through the `/auth/login` endpoint and can be used for authentication in subsequent requests.

### Postman Collection
Sample Postman collection for testing purposes can be found: 

[BrokageDemo.postman_collection.json](BrokageDemo.postman_collection.json)

## Testing

Run the tests using:
```bash
./mvnw test
```