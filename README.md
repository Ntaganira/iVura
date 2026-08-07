# iVura - Hospital Management System

A full-featured hospital management system built with **Spring Boot 3**, **Thymeleaf**, and **PostgreSQL**. It covers patient registration, doctor and department management, hospital services with pricing, appointments, billing, user management with role-based access control, and a full activity audit trail.

## Features

- **Dashboard** — overview statistics and charts
- **Patients** — registration and management
- **Doctors** — profiles linked to departments, specializations, and services (multi-select via Choices.js)
- **Specializations** — medical expertise areas assigned to doctors
- **Departments** — organization units with doctor counts
- **Services** — medical services offered by the hospital, each with a price (RWF)
- **Appointments** — scheduling between patients and doctors with a calendar view (FullCalendar)
- **Billing** — invoices linked to patients and services
- **Payments** — payment collections against bills (cash, mobile money, card, bank, insurance) with automatic bill status reconciliation
- **Reports** — financial and activity analytics dashboard with charts and HTML-to-PDF export (OpenHTMLToPDF)
- **Users, Roles & Permissions** — role-based access control (RBAC) with fine-grained page/action permissions
- **Activity Logs** — audit trail of every system action (success/failure)
- **Profile** — avatar upload, profile editing, and password change
- **File Uploads** — MinIO object storage
- **i18n** — English, French, and Kinyarwanda

## Tech Stack

| Layer     | Technology                                              |
|-----------|---------------------------------------------------------|
| Backend   | Java 17, Spring Boot 3.4, Spring MVC, Spring Security   |
| Persistence | Spring Data JPA (Hibernate), Flyway migrations        |
| Frontend  | Thymeleaf, Thymeleaf Spring Security extras, custom CSS, Choices.js, Chart.js, FullCalendar (all vendored locally, no CDN) |
| Database  | PostgreSQL 16                                           |
| Storage   | MinIO (object storage)                                  |
| PDF       | OpenHTMLToPDF (HTML-to-PDF, pure Java)                  |
| Build     | Maven, Lombok                                           |

## Requirements

- Java 17+
- Maven (or use the bundled `mvnw` wrapper)
- Docker (optional, for PostgreSQL and MinIO)
- PostgreSQL 16

## Getting Started

### 1. Start the infrastructure

```bash
docker compose up -d
```

This starts:

- **PostgreSQL** on `localhost:5432` (db: `ivura`, user/password: `postgres`/`postgres`)
- **MinIO** on `localhost:9000` (console `localhost:9001`, credentials `minioadmin`/`minioadmin123`)

### 2. Run the application

```bash
./mvnw spring-boot:run
```

Or build and run:

```bash
./mvnw clean package -DskipTests
java -jar target/iVura-0.0.1-SNAPSHOT.jar
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The app starts on `http://localhost:8080`.

### 3. Log in

| Username | Password     | Role  |
|----------|--------------|-------|
| `admin`  | `password123` | ADMIN |

> The default admin user is seeded by `V1__init_schema.sql`. The default password is also printed to the application log on startup. **Change it after your first login.**

## Configuration

All settings live in `src/main/resources/application.yml`.

| Setting            | Default                 | Description                    |
|--------------------|-------------------------|--------------------------------|
| `server.port`      | `8080`                  | HTTP port                      |
| `spring.datasource`| `localhost:5432/ivura`  | PostgreSQL connection          |
| `minio.url`        | `http://localhost:9000` | MinIO endpoint                 |
| `minio.bucket`     | `ivura-files`           | Upload bucket (auto-created)   |
| `spring.jpa.hibernate.ddl-auto` | `validate` | Hibernate validates against Flyway schema |

When running the app in a Docker network, override the datasource and MinIO hosts with the `docker` profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

## Database Migrations

Schema is managed by **Flyway**. Migrations live in `src/main/resources/db/migration/` and are applied automatically on startup:

| Version | Purpose                                       |
|---------|-----------------------------------------------|
| `V1`    | Initial schema (patients, doctors, appointments, billing) + admin seed |
| `V2`    | User management (users, roles, permissions, pages) |
| `V3`    | Activity logs                                 |
| `V4`    | User profiles (avatar, phone)                 |
| `V5`    | Departments + service lookup + grants         |
| `V6`    | Service pricing                              |
| `V7`    | Rename specialization → services             |
| `V8`    | Separate specializations and services        |
| `V9`    | Payments module (payments table, permissions, page) |
| `V10`   | *(none — Reports reuses existing REPORT permissions and REPORTS page)* |

## Project Structure

```
src/main/java/com/ntaganira/heritier/iVura/
├── config/        # Spring Security, MinIO, etc.
├── controller/    # MVC controllers
├── dto/           # Data transfer objects
├── entity/        # JPA entities
├── enums/         # Enumerations (activity status, etc.)
├── repository/    # Spring Data repositories
├── service/       # Business logic
└── IVuraApplication.java

src/main/resources/
├── db/migration/          # Flyway SQL migrations
├── templates/             # Thymeleaf views
│   ├── layout/            # Sidebar, pagination, errors fragments
│   ├── patients/ doctors/ appointments/ billings/ payments/
│   ├── reports/ departments/ services/ specializations/ users/ roles/ permissions/
│   └── activity/ profile/ auth/
├── static/css/            # Stylesheets
├── static/js/             # Application scripts (wecare.js)
├── static/vendor/         # Vendored libs: Choices.js, Chart.js, FullCalendar (no CDN)
└── messages*.properties   # i18n (EN, FR, RW)
```

## License

This is a private project. All rights reserved.
