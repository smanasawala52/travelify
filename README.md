# Travelify - Travel Booking Platform

Full-stack travel booking platform with Spring Boot, React (Vite), and native Android/iOS role builds.

## Architecture

| Layer | Stack |
|-------|--------|
| Backend | Spring Boot 3.1.5, JPA, Security, JWT, Lombok |
| Web | React 18, Vite, Material-UI, React Router, Axios |
| Mobile | Kotlin (Android) & Swift (iOS) â€” Customer / Travel Agent / Admin |
| Database | H2 (dev), PostgreSQL (prod) |

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+ and npm
- (Optional) Android Studio / Xcode for native apps
- (Prod) PostgreSQL 14+

## Generate / regenerate project

If you only have the scaffold script:

```powershell
.\create-project.ps1
```

## Quick start

### 1. Backend (H2 / dev)

```powershell
cd backend
.\run-backend.ps1
# or
.\run-backend.ps1 -Profile dev
```

API: `http://localhost:8080/api`  
H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:travelify`)

### 2. Backend (PostgreSQL / prod)

1. Edit `backend/.env.prod` with your Postgres credentials.
2. Ensure PostgreSQL is running and database exists.
3. Run:

```powershell
cd backend
.\run-backend.ps1 -Profile prod
```

### 3. Web app

```powershell
cd frontend/webapp
.\run-webapp-app.ps1
```

App: `http://localhost:5173`

### 4. Mobile (placeholders)

Scripts live under `frontend/mobile/` and print the intended build commands:

```powershell
cd frontend/mobile
.\run-customer-android-app.ps1
.\run-travel-agent-android-app.ps1
.\run-admin-android-app.ps1
.\run-customer-ios-app.ps1
.\run-travel-agent-ios-app.ps1
.\run-admin-ios-app.ps1
```

## Default demo users (seeded on startup)

| Email | Password | Role |
|-------|----------|------|
| admin@travelify.com | password123 | ADMIN |
| agent@travelify.com | password123 | TRAVEL_AGENT |
| customer@travelify.com | password123 | CUSTOMER |

## Project layout

```
backend/                 Spring Boot API + run-backend.ps1
frontend/webapp/         React Vite SPA + run-webapp-app.ps1
frontend/shared/         Shared API client / auth helpers (web)
frontend/mobile/         Android & iOS role apps + run scripts
scripts/                 Shared env loader helpers
```

## Environment files

- `backend/.env` / `.env.dev` / `.env.prod` â€” loaded by `run-backend.ps1`
- `frontend/webapp/.env` â€” loaded by Vite and `run-webapp-app.ps1`
- OS environment variables override file values when set

## API overview

- `POST /api/auth/register` â€” register
- `POST /api/auth/login` â€” login (returns JWT)
- `GET /api/packages` â€” list packages
- `GET /api/customer/**` â€” customer bookings
- `GET /api/agent/**` â€” agent package/booking management
- `GET /api/admin/**` â€” admin users & overview

## License

MIT