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

-----------------------------------------------------------------------------------
## Pipeline CI/CD — Trazabilidad y Calidad

Este proyecto implementa un pipeline de integración y entrega continua con GitHub Actions, organizado en tres etapas secuenciales.

### Etapas del pipeline

**1. Build & Test**
Se compila el proyecto y se ejecutan las pruebas unitarias con JUnit 5 usando `mvn clean test`. El plugin JaCoCo genera un reporte de cobertura de código disponible como artefacto descargable en cada ejecución. Esto garantiza que el código tenga cobertura verificable antes de continuar.

**2. Security Scan**
Se ejecuta un análisis de dependencias con Snyk. Si se detectan más de 3 vulnerabilidades de severidad `high` o `critical`, el pipeline se detiene con `exit 1`, bloqueando el despliegue. Los reportes en formato JSON y SARIF quedan disponibles como artefactos para auditoría.

**3. Publish & Deploy**
La imagen Docker se construye usando un Dockerfile multistage (imagen base `eclipse-temurin:21-jre-alpine` para menor superficie de ataque) y se publica en GitHub Container Registry (GHCR) con dos tags: `latest` y el SHA del commit para trazabilidad exacta. Finalmente se despliega usando Docker Compose y se verifica que el endpoint `/greetings` responde correctamente.

### Cómo se garantiza la trazabilidad

- Cada imagen publicada lleva el tag del SHA del commit, lo que permite saber exactamente qué código está corriendo en producción.
- Los artefactos de cobertura (JaCoCo) y seguridad (Snyk) quedan almacenados 14 días en cada ejecución del pipeline.
- El pipeline falla de forma explícita en cada etapa, impidiendo que código sin pruebas o con vulnerabilidades críticas llegue a producción.

### Cómo se garantiza la calidad

- **Pruebas unitarias**: JUnit 5 con 4 casos de prueba que cubren los escenarios críticos del controlador.
- **Cobertura de código**: JaCoCo mide las líneas y ramas ejecutadas durante los tests.
- **Análisis de seguridad**: Snyk escanea dependencias y bloquea el pipeline si supera el umbral de vulnerabilidades definido.
- **Orquestación**: Docker Compose gestiona el ciclo de vida del contenedor con healthcheck incorporado, garantizando que la aplicación está saludable antes de considerarse desplegada.

### Cómo ejecutar localmente con Docker Compose

```bash
docker compose up --build
```

La aplicación queda disponible en `http://localhost:8080`.

## 📊 Sistema de Monitoreo y Observabilidad (IE1, IE3, IE4)

Para garantizar la visibilidad total del microservicio en el entorno orquestado, se ha integrado una arquitectura de monitoreo basada en **Prometheus** (recolección) y **Grafana** (visualización en tiempo real).

### Métricas Críticas en el Dashboard
A través de la interfaz web de Grafana (`http://localhost:3000`), el equipo tiene acceso a un panel de control con cuatro indicadores clave para la toma de decisiones técnicas:

| Panel | Tipo de Gráfico | Métrica / Origen de Datos | Justificación Técnica y Decisiones |
| :--- | :--- | :--- | :--- |
| **1. Tiempo de Despliegue** | Historial / Líneas | API de GitHub Actions (`workflow_run`) | **Optimización:** Permite evaluar la eficiencia del pipeline. Un incremento en el tiempo alerta sobre ineficiencias en la construcción de capas Docker o lentitud en las pruebas unitarias. |
| **2. Cobertura de Pruebas** | Indicador (Gauge) | Reporte Jacoco / API de SonarQube | **Calidad:** Muestra el porcentaje de código respaldado por JUnit. Si baja del 80%, se toman decisiones de refactorización inmediata antes de generar deuda técnica. |
| **3. Uso de CPU y Memoria** | Área / Líneas | Prometheus: `jvm_memory_used_bytes` | **Escalabilidad:** Monitorea el comportamiento de la Máquina Virtual de Java (JVM). Si el consumo de RAM supera de forma sostenida el 75%, justifica técnicamente el autoescalado horizontal (HPA) en Kubernetes. |
| **4. Errores Registrados** | Contador / Barras | Prometheus: `http_server_requests_seconds_count` | **Resiliencia:** Filtra las respuestas de estado HTTP `5xx`. Si registra fallos, el equipo técnico toma la decisión de ejecutar un *rollback* inmediato a la versión estable anterior. |

---

## 🛡 Gobernanza y Políticas de Cumplimiento (IE5, IE6)

La seguridad y la calidad del software no se negocian. El repositorio implementa mecanismos automatizados para asegurar el cumplimiento normativo en cada integración:

### 1. Control de Cambios Seguro (Branch Protection)
Se ha estructurado una política mandatoria sobre la rama principal `main`:
- **Prohibición de Push Directo:** Todo cambio debe originarse obligatoriamente desde una rama de características (`feature/`) mediante un Pull Request (PR).
- **Bloqueo Basado en Estado (Status Checks):** El botón de integración (*Merge*) se deshabilita automáticamente si el pipeline de GitHub Actions arroja un estado fallido.

### 2. Script Personalizado de Auditoría de Calidad
Como mecanismo complementario de gobernanza (e infraestructura como código), el proyecto incorpora un script de auditoría automatizado (`check-quality.sh`). Este script parsea los reportes XML de JaCoCo en el pipeline y fuerza un código de salida `exit 1` si detecta que la cobertura del código Java es inferior al umbral del 80%.

### 3. Demostración de Resiliencia ante Fallas Críticas
El pipeline actúa como un escudo activo. Como se evidencia en el historial de commits del repositorio (marcado con una **`X` roja** en la interfaz de GitHub), ante cualquier fallo en las pruebas automatizadas de JUnit, vulnerabilidades de Snyk o incumplimiento de métricas, el flujo **se interrumpe por completo**. Esto bloquea los jobs de empaquetado y evita de forma proactiva que una versión defectuosa o insegura sea publicada en el registro de contenedores o desplegada en la nube.


### Integrantes

- Benjamin Vasquez (benjav892)
- Camilo Vera(cvera4)

### Declaración de uso de IA

Se utilizó Claude (Anthropic) como apoyo para la estructuración del pipeline CI/CD y la redacción técnica de esta documentación. Todas las decisiones técnicas, justificaciones y reflexiones individuales son propias del equipo. Referencia: https://bibliotecas.duoc.cl/ia

