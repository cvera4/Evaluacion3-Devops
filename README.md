# Spring Boot Greetings API — Proyecto DevOps (EFT DOY0101)

Aplicación Spring Boot de ejemplo utilizada como base para automatizar su ciclo de vida completo aplicando prácticas DevOps: control de versiones, CI/CD, análisis de calidad y seguridad, contenedores, despliegue continuo y monitoreo.

**Integrantes:** Benjamín Vásquez (`benjav892`) · Camilo Vera (`cvera4`)

---

## Tabla de contenidos

1. [Requisitos previos](#requisitos-previos)
2. [Compilar y ejecutar](#compilar-y-ejecutar)
3. [Endpoints](#endpoints)
4. [Estrategia de ramificación y control de versiones](#1-estrategia-de-ramificación-y-control-de-versiones)
5. [Docker](#2-contenedores-con-docker)
6. [Pipeline de CI/CD con GitHub Actions](#3-pipeline-de-cicd-con-github-actions)
7. [Análisis de código y seguridad](#4-análisis-de-código-y-seguridad)
8. [Despliegue continuo en entorno cloud simulado](#5-despliegue-continuo-en-entorno-cloud-simulado)
9. [Monitoreo y observabilidad](#6-monitoreo-y-observabilidad)
10. [Gobernanza y políticas de cumplimiento](#7-gobernanza-y-políticas-de-cumplimiento)
11. [Estado de las integraciones](#estado-de-las-integraciones)
12. [Trazabilidad de indicadores de logro](#trazabilidad-de-indicadores-de-logro-eft)
13. [Declaración de uso de IA](#declaración-de-uso-de-ia)

---

## Requisitos previos

- **Java 21** — Microsoft Build de OpenJDK 21: https://learn.microsoft.com/en-us/java/openjdk/download
- **Apache Maven 3.8+**

### Instalar Maven en Windows (con Chocolatey)

```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
choco install maven --force
```

> Reinicia la terminal o el editor después de instalar Maven.

Verificar instalación:

```bash
java -version
mvn -version
```

## Compilar y ejecutar

```bash
mvn clean compile          # Compilar
mvn test                   # Ejecutar tests
mvn clean test              # Ejecutar tests + reporte de cobertura JaCoCo
mvn clean package           # Empaquetar en JAR
mvn spring-boot:run          # Ejecutar
```

El plugin **JaCoCo** corre en dos fases: `prepare-agent` (instrumenta las clases antes de los tests) y `report` (genera el reporte HTML/CSV/XML). El reporte queda disponible en:

```
target/site/jacoco/index.html
```

## Endpoints

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/` | Página de bienvenida con enlaces a la documentación |
| GET | `/greetings` | Retorna `Hello world` |
| GET | `/greetings?message=TuNombre` | Retorna `Hello TuNombre` |
| GET | `/swagger-ui.html` | Documentación interactiva (Swagger UI) |
| GET | `/api-docs` | Especificación OpenAPI en JSON |

---

## 1. Estrategia de ramificación y control de versiones

**Modelo:** Trunk-Based Development con ramas de apoyo de corta duración. `main` es la única rama de release y está protegida: no admite push directo, todo cambio ingresa exclusivamente mediante Pull Request y revisión de código.

**Convención de nombres de rama:**

| Prefijo | Uso |
|---|---|
| `feature/...` | Nuevas funcionalidades (ej: `feature/github-actions`, `feature/readme-update`) |
| `hotfix/...` | Correcciones urgentes sobre `main` (ej: `hotfix/arreglo-final`) |
| `bugfix/...` | Corrección de errores no urgentes |

**Por qué esta estrategia:** favorece una integración continua real, con commits pequeños y frecuentes hacia `main`, reduciendo la probabilidad de conflictos grandes frente a un modelo de ramas de larga duración (como GitFlow clásico).

**Flujo de trabajo demostrado:** durante el desarrollo del proyecto se crearon ramas `feature/*` y una `hotfix/*`, cada una integrada a `main` mediante Pull Request revisado (ver historial de commits y PRs mergeados del proyecto). En este repositorio consolidado, `main` concentra el estado final validado de todas esas integraciones.

---

## 2. Contenedores con Docker

Se mantienen dos variantes de imagen para comparar enfoques:

### Dockerfile (single-stage)

```bash
docker build -t spring-app-duoc:single .
docker run -d -p 8080:8080 --name spring-app spring-app-duoc:single
```

### Dockerfile.multistage (recomendado)

Primera etapa compila con `maven:3.9.6-eclipse-temurin-21-alpine`; la etapa final corre sobre `eclipse-temurin:21-jre-alpine`, reduciendo tamaño de imagen y superficie de ataque al no incluir el JDK ni Maven en la imagen final.

```bash
docker build -f Dockerfile.multistage -t spring-app-duoc:multi .
docker run -d -p 8080:8080 --name spring-app spring-app-duoc:multi
```

### Comandos útiles

```bash
docker images
docker ps -a
docker stop spring-app && docker rm spring-app
docker rmi spring-app-duoc:single spring-app-duoc:multi
```

### Ejecutar todo el stack localmente (app + Prometheus + Grafana)

```bash
docker compose up --build
```

La aplicación queda disponible en `http://localhost:8080`.

---

## 3. Pipeline de CI/CD con GitHub Actions

Archivo: `.github/workflows/CICD.yml`. Se dispara con **push a `develop`**, **Pull Request hacia `main`**, y también admite ejecución manual (`workflow_dispatch`).

El pipeline está organizado en 4 jobs secuenciales, cada uno depende del anterior (`needs`):

### Job 1 — `build-and-test`
- Checkout del código y setup de JDK 21 (Temurin)
- `mvn clean test` → ejecuta JUnit 5 (4 casos de prueba sobre `GreetingsController`)
- JaCoCo genera el reporte de cobertura, publicado como artefacto descargable (retención 14 días)

### Job 2 — `security-scan`
- Instala y autentica Snyk CLI
- `snyk test --all-projects` sobre las dependencias del proyecto
- Genera reportes en JSON y SARIF, publicados como artefactos

### Job 3 — `publish-image`
- Se ejecuta solo en Pull Request hacia `main` o de forma manual
- Construye la imagen con `Dockerfile.multistage` y la publica en Docker Hub con dos tags: `latest` y el **SHA del commit** (trazabilidad exacta de qué versión corre en cada ambiente)

### Job 4 — `deploy-kubernetes`
- Aplica los manifiestos de Kubernetes en un entorno cloud simulado
- Configura inyección de Istio y envío de métricas a AWS CloudWatch
- Ejecuta pruebas de aceptación antes de autorizar el paso a producción

**Garantía de calidad antes de avanzar de etapa:** si los tests fallan, si Snyk detecta vulnerabilidades sobre el umbral, o si las pruebas de aceptación no pasan, el pipeline se detiene y bloquea el avance a la siguiente etapa.

---

## 4. Análisis de código y seguridad

### SonarQube
Configurado vía `sonar-project.properties`:
- `sonar.projectKey=cl.duoc:spring-app-duoc`
- `sonar.organization=duoc-devops-team`, host: SonarCloud
- Integrado con el reporte de cobertura de JaCoCo (`target/site/jacoco/jacoco.xml`)
- Excluye `pom.xml` y clases de test del análisis

### Snyk
- Escanea dependencias del proyecto (`package.json`/`pom.xml`) en busca de vulnerabilidades conocidas
- **Umbral de corte:** si se detectan más de 3 vulnerabilidades de severidad `high` o `critical`, el pipeline se detiene con `exit 1`

### Dependabot
Recomendado como complemento para mantener dependencias actualizadas automáticamente (ver sección [Estado de las integraciones](#estado-de-las-integraciones)).

---

## 5. Despliegue continuo en entorno cloud simulado

El manifiesto `k8s/deployment.yaml` define:
- Un `Deployment` con **2 réplicas** (alta disponibilidad) y límites de CPU/memoria
- Un `Service` tipo `LoadBalancer` para balanceo de carga
- Recursos de **Istio** (`Gateway` y `VirtualService`) para enrutamiento y tráfico seguro (mTLS) entre servicios, mediante inyección automática de sidecar

El job de despliegue del pipeline simula, sobre un entorno cloud controlado (según lo permite la evaluación):
- Aplicación de los manifiestos en el namespace `produccion`
- Habilitación del sidecar de Istio y sus métricas de red
- Envío de logs y métricas de la JVM a AWS CloudWatch
- Ejecución de pruebas de aceptación (smoke tests) antes de dar por autorizada la carga en producción

---

## 6. Monitoreo y observabilidad

Arquitectura basada en **Prometheus** (recolección de métricas) y **Grafana** (visualización), levantados como contenedores adicionales en `docker-compose.yml`.

- Prometheus scrapea `/actuator/prometheus` cada 15 segundos (`prometheus.yml`)
- Grafana disponible en `http://localhost:3000`

| Panel | Métrica | Para qué sirve |
|---|---|---|
| Tiempo de despliegue | API de GitHub Actions (`workflow_run`) | Detectar degradación en la velocidad del pipeline |
| Cobertura de pruebas | Reporte JaCoCo / SonarQube | Alertar si la cobertura cae bajo el 80% |
| CPU y memoria de la JVM | `jvm_memory_used_bytes` (Prometheus) | Justificar autoescalado horizontal en Kubernetes |
| Errores HTTP 5xx | `http_server_requests_seconds_count` | Disparar decisión de rollback si aumentan los fallos |

---

## 7. Gobernanza y políticas de cumplimiento

- **Branch protection en `main`:** no se admite push directo; todo cambio requiere Pull Request, y el botón de merge se bloquea automáticamente si el pipeline de GitHub Actions falla.
- **Resiliencia ante fallas:** ante cualquier fallo en tests, vulnerabilidades críticas de Snyk, o incumplimiento de las métricas de calidad, el pipeline se interrumpe por completo, evitando publicar o desplegar una versión defectuosa.

---

## Estado de las integraciones

Documentamos honestamente qué está automatizado end-to-end y qué queda como configuración lista para conectar, de forma que el flujo sea auditable:

| Integración | Estado |
|---|---|
| Build + JUnit 5 + JaCoCo | ✅ Automatizado y funcional en el pipeline |
| Snyk (seguridad de dependencias) | ✅ Automatizado, con corte por umbral de severidad |
| Publicación de imagen (Docker Hub, tag por SHA) | ✅ Automatizado |
| Prometheus + Grafana | ✅ Funcional vía `docker-compose.yml` |
| SonarQube | ⚙️ Configuración lista (`sonar-project.properties`); pendiente conectar la action oficial de análisis en el step del workflow |
| Kubernetes + Istio + AWS CloudWatch | 🧪 Simulado en el pipeline (entorno cloud simulado, según lo requerido por la evaluación) |
| Dependabot | 🔜 Recomendado, no configurado aún en este repositorio |

---

## Trazabilidad de indicadores de logro (EFT)

| Indicador | Dónde se evidencia |
|---|---|
| IE1, IE2, IE3, IE5 | Sección [Estrategia de ramificación](#1-estrategia-de-ramificación-y-control-de-versiones) |
| IE4, IE6, IE7, IE9 | Sección [Pipeline de CI/CD](#3-pipeline-de-cicd-con-github-actions) |
| IE8 | Sección [Análisis de código y seguridad](#4-análisis-de-código-y-seguridad) |
| IE10, IE12, IE13 | Sección [Despliegue continuo](#5-despliegue-continuo-en-entorno-cloud-simulado) |
| IE11 | Sección [Monitoreo y observabilidad](#6-monitoreo-y-observabilidad) |

---

## Declaración de uso de IA

Se utilizó Claude (Anthropic) como apoyo para estructurar esta documentación técnica y organizarla según los indicadores de la evaluación, a partir del código y configuración ya existentes en el repositorio. Todas las decisiones técnicas, la arquitectura y las justificaciones fueron definidas por el equipo. Referencia: https://bibliotecas.duoc.cl/ia
