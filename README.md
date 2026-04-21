# Spring Boot Greetings API

Sample Spring Boot application for the **Duoc DevOps** class. Students can use this project to learn about building, running, and deploying a Java REST API.

## Prerequisites

- **Java 21** — Download the Microsoft Build of OpenJDK 21 from:
  https://learn.microsoft.com/en-us/java/openjdk/download
- **Apache Maven 3.8+**

### Installing Maven on Windows (using Chocolatey)

1. Install Chocolatey from an **admin PowerShell** terminal:

```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
```

2. Install Maven from an **admin PowerShell** terminal:

```powershell
choco install maven --force
```

> **Important:** After installing Maven, restart your terminal or VS Code editor for the changes to take effect.

### Verify your installation

```bash
java -version
mvn -version
```

## Build & Run

### Compile the project

```bash
mvn clean compile
```

### Run tests

```bash
mvn test
```

### Run tests with code coverage (JaCoCo)

```bash
mvn clean test
```

The project uses the **JaCoCo Maven plugin** with two goals:

| Goal | Phase | Description |
|------|-------|-------------|
| `prepare-agent` | Before tests | Instruments the compiled classes to track which lines and branches are executed during tests |
| `report` | `test` | Generates an HTML, CSV, and XML coverage report from the collected execution data |

After running, the coverage report is available at:

```
target/site/jacoco/index.html
```

Open this file in a browser to view detailed line and branch coverage per class.

### Package into a JAR

```bash
mvn clean package
```

### Run the application

Using Maven:

```bash
mvn spring-boot:run
```

Or with the JAR directly:

```bash
java -jar target/spring-app-duoc-0.0.1-SNAPSHOT.jar
```

The application starts on **port 8080**.

## Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/` | Welcome page with links to the API documentation |
| GET | `/greetings` | Returns `Hello world` |
| GET | `/greetings?message=YourName` | Returns `Hello YourName` |

## Docker

### Build & run with the single-stage Dockerfile

```bash
docker build -t spring-app-duoc:single .
docker run -d -p 8080:8080 --name spring-app spring-app-duoc:single
```

### Build & run with the multi-stage Dockerfile

```bash
docker build -f Dockerfile.multistage -t spring-app-duoc:multi .
docker run -d -p 8080:8080 --name spring-app spring-app-duoc:multi
```

### List images and running containers

```bash
# List all Docker images
docker images

# List running containers
docker ps

# List all containers (including stopped)
docker ps -a
```

### Remove containers and images

```bash
# Stop a running container
docker stop spring-app

# Remove a container
docker rm spring-app

# Remove an image
docker rmi spring-app-duoc:single

# Remove an image (multi-stage)
docker rmi spring-app-duoc:multi
```

## API Documentation

Once the application is running:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI spec (JSON):** http://localhost:8080/api-docs
