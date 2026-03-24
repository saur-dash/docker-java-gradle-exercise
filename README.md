#### This project is for the Devops bootcamp exercise for 
#### "Containers - Docker" 

This repository supports my personal learning in the DevOps field, specifically around containerisation with Docker. It demonstrates building a Java Spring Boot application as a Docker image and running it alongside its dependent services using Docker Compose.

---

## Application Overview

A simple Spring Boot REST API backed by a MySQL database. It exposes two endpoints:

| Method | Path            | Description                        |
|--------|-----------------|------------------------------------|
| GET    | `/get-data`     | Fetch all team members from the DB |
| POST   | `/update-roles` | Update team member roles in the DB |

A static front-end is served at the application root (`/`).

---

## Tech Stack

| Layer       | Technology                     |
|-------------|--------------------------------|
| Language    | Java 17                        |
| Framework   | Spring Boot 3.x                |
| Build tool  | Gradle 8                       |
| Database    | MySQL 8.4                      |
| DB admin    | phpMyAdmin 5.2                 |
| Runtime     | Docker / Docker Compose        |

---

## Repository Structure

```
.
├── Dockerfile              # Multi-stage build: Gradle build → slim JRE runtime
├── docker-compose.yml      # Orchestrates app, MySQL, and phpMyAdmin
├── build.gradle            # Gradle build configuration
├── secrets/                # Docker secrets (plain-text files, NOT committed)
│   ├── db_name.txt
│   ├── db_pwd.txt
│   ├── db_root_pwd.txt
│   ├── db_server.txt
│   └── db_user.txt
└── src/
    └── main/java/com/example/
        ├── Application.java       # Spring Boot entry point
        ├── AppController.java     # REST endpoints
        └── DatabaseConfig.java    # DB connection via Docker secrets
```

---

## Docker Image — Multi-Stage Build

The [Dockerfile](Dockerfile) uses a two-stage build to keep the runtime image small:

1. **Build stage** (`gradle:8-jdk17-alpine`) — compiles the source and runs tests via `gradle clean build`.
2. **Runtime stage** (`eclipse-temurin:17-jre-alpine`) — copies only the assembled JAR, runs as a non-root user (`10001:10001`), and exposes port `8080`.

### Build the image locally

```bash
docker build -t java-gradle-app:1.0 .
```

---

## Services — Docker Compose

docker-compose.yml defines three services:

| Service       | Image                             | Published port | Purpose              |
|---------------|-----------------------------------|---------------|----------------------|
| `app-main`    | `<registry>/java-gradle-app:1.0`  | `8080`        | Spring Boot REST API |
| `mysql`       | `mysql:8.4`                       | `3306`        | Relational database  |
| `phpmyadmin`  | `phpmyadmin:5.2-apache`           | `8888`        | Database admin UI    |

**Key design decisions:**
- All credentials are passed via [Docker secrets](https://docs.docker.com/compose/use-secrets/) (file-based) — no passwords in environment variables.
- `app-main` uses `depends_on` with a health check condition on `mysql`, so the app only starts once the database is ready.
- MySQL data is persisted in a named volume (`mysql-data`).

### Secrets setup

Create the secrets directory and populate each file before starting the stack:

```bash
mkdir -p secrets
echo "my_db"       > secrets/db_name.txt
echo "db_user"     > secrets/db_user.txt
echo "db_password" > secrets/db_pwd.txt
echo "root_password" > secrets/db_root_pwd.txt
echo "mysql"       > secrets/db_server.txt   # hostname = the Compose service name
```

> **Note:** The secrets directory is not committed to version control. Never commit credentials.

### Start the stack

```bash
docker compose up -d
```

### Stop the stack

```bash
docker compose down
```

---

## CI/CD Pipeline Simulation

To simulate a real-world CD pipeline this project was deployed to a cloud server (DigitalOcean Droplet) using the following steps:

1. **Provisioned a cloud server** — launched a Droplet on DigitalOcean and configured firewall rules to permit SSH and traffic on ports `8080` and `8888`.
2. **Installed Docker** — installed and started Docker as a non-root user.
3. **Launched Nexus** — ran a Sonatype Nexus container with a named volume for data persistence, then configured a Docker-hosted repository and a scoped user role.
4. **Published the image** — built the application Docker image locally and pushed it to the private Nexus registry.
5. **Deployed** — cloned this repository on the cloud server, populated the secrets files, and ran `docker compose up -d`.
6. **Verified** — confirmed the application and phpMyAdmin were accessible on their published ports.


