#Requires -Version 5.1
<#
.SYNOPSIS
    Creates the complete Travelify travel booking platform project structure.
.DESCRIPTION
    Self-contained scaffolding script. Run from an empty (or target) directory:
      .\create-project.ps1
    Then start components with the generated run-*.ps1 scripts.
#>
[CmdletBinding()]
param(
    [string]$Root
)

$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($Root)) {
    if (-not [string]::IsNullOrWhiteSpace($PSScriptRoot)) {
        $Root = $PSScriptRoot
    } else {
        $Root = (Get-Location).Path
    }
}
$Root = [System.IO.Path]::GetFullPath($Root)

function Write-Banner {
    param([string]$Message, [string]$Color = 'Cyan')
    Write-Host ""
    Write-Host "========================================" -ForegroundColor $Color
    Write-Host "  $Message" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor $Color
}

function Ensure-Dir {
    param([string]$Path)
    if (-not (Test-Path -LiteralPath $Path)) {
        New-Item -ItemType Directory -Path $Path -Force | Out-Null
    }
}

function Write-ProjectFile {
    param(
        [Parameter(Mandatory = $true)][string]$RelativePath,
        [Parameter(Mandatory = $true)][string]$Content
    )
    $fullPath = Join-Path $Root $RelativePath
    $dir = Split-Path -Parent $fullPath
    Ensure-Dir -Path $dir
    # UTF-8 without BOM (avoids Maven/npm/Java quirks)
    $utf8 = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($fullPath, $Content, $utf8)
    Write-Host "  Created: $RelativePath" -ForegroundColor Gray
}

Write-Banner "TRAVELIFY PROJECT SETUP"
Write-Host "Root: $Root" -ForegroundColor DarkGray
Write-Host ""

# ---------------------------------------------------------------------------
# Directories
# ---------------------------------------------------------------------------
Write-Host "Creating directories..." -ForegroundColor Yellow

$dirs = @(
    "backend/src/main/java/com/travelify",
    "backend/src/main/java/com/travelify/config",
    "backend/src/main/java/com/travelify/security",
    "backend/src/main/java/com/travelify/controller",
    "backend/src/main/java/com/travelify/service",
    "backend/src/main/java/com/travelify/repository",
    "backend/src/main/java/com/travelify/model",
    "backend/src/main/java/com/travelify/dto",
    "backend/src/main/java/com/travelify/exception",
    "backend/src/main/resources",
    "backend/src/test/java/com/travelify",
    "frontend/webapp/public",
    "frontend/webapp/src/pages",
    "frontend/webapp/src/components",
    "frontend/webapp/src/layouts",
    "frontend/shared/api",
    "frontend/shared/context",
    "frontend/shared/hooks",
    "frontend/shared/components",
    "frontend/shared/styles",
    "frontend/mobile/android/customer",
    "frontend/mobile/android/travel-agent",
    "frontend/mobile/android/admin",
    "frontend/mobile/android/shared",
    "frontend/mobile/ios/Customer",
    "frontend/mobile/ios/TravelAgent",
    "frontend/mobile/ios/Admin",
    "frontend/mobile/ios/Shared",
    "scripts"
)

foreach ($d in $dirs) {
    Ensure-Dir -Path (Join-Path $Root $d)
    Write-Host "  Created: $d" -ForegroundColor Gray
}

# ---------------------------------------------------------------------------
# Root files
# ---------------------------------------------------------------------------
Write-Host ""
Write-Host "Creating root config files..." -ForegroundColor Yellow

Write-ProjectFile -RelativePath ".gitignore" -Content @'
# Backend
backend/target/
backend/.mvn/wrapper/maven-wrapper.jar
*.log
*.pid
*.class

# Frontend
node_modules/
dist/
build/
.env.local
.env.*.local
*.local

# Mobile
*.iml
.gradle/
local.properties
*.apk
*.aab
*.ipa
Pods/
DerivedData/
*.xcuserdata/

# IDE / OS
.idea/
.vscode/
*.swp
*.swo
*~
.DS_Store
Thumbs.db

# Secrets (keep examples)
.env
backend/.env
backend/.env.prod
frontend/webapp/.env
'@

Write-ProjectFile -RelativePath ".env.example" -Content @'
# Shared Travelify environment example (copy values into component .env files)

# Backend
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
JWT_SECRET=change-me-to-a-long-random-secret-key-at-least-256-bits
JWT_EXPIRATION_MS=86400000

# H2 (dev)
H2_CONSOLE_ENABLED=true

# PostgreSQL (prod)
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=travelify
POSTGRES_USER=travelify
POSTGRES_PASSWORD=travelify

# Frontend
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=Travelify
'@

Write-ProjectFile -RelativePath "README.md" -Content @'
# Travelify - Travel Booking Platform

Full-stack travel booking platform with Spring Boot, React (Vite), and native Android/iOS role builds.

## Architecture

| Layer | Stack |
|-------|--------|
| Backend | Spring Boot 3.1.5, JPA, Security, JWT, Lombok |
| Web | React 18, Vite, Material-UI, React Router, Axios |
| Mobile | Kotlin (Android) & Swift (iOS) — Customer / Travel Agent / Admin |
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

- `backend/.env` / `.env.dev` / `.env.prod` — loaded by `run-backend.ps1`
- `frontend/webapp/.env` — loaded by Vite and `run-webapp-app.ps1`
- OS environment variables override file values when set

## API overview

- `POST /api/auth/register` — register
- `POST /api/auth/login` — login (returns JWT)
- `GET /api/packages` — list packages
- `GET /api/customer/**` — customer bookings
- `GET /api/agent/**` — agent package/booking management
- `GET /api/admin/**` — admin users & overview

## License

MIT
'@

# ---------------------------------------------------------------------------
# Scripts helpers
# ---------------------------------------------------------------------------
Write-Host ""
Write-Host "Creating shared scripts..." -ForegroundColor Yellow

Write-ProjectFile -RelativePath "scripts/Load-DotEnv.ps1" -Content @'
function Import-DotEnv {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [switch]$Override
    )
    if (-not (Test-Path -LiteralPath $Path)) {
        Write-Host "  .env not found: $Path (skipping)" -ForegroundColor DarkYellow
        return
    }
    Write-Host "  Loading env from $Path" -ForegroundColor DarkGray
    Get-Content -LiteralPath $Path | ForEach-Object {
        $line = $_.Trim()
        if ([string]::IsNullOrWhiteSpace($line) -or $line.StartsWith('#')) { return }
        $idx = $line.IndexOf('=')
        if ($idx -lt 1) { return }
        $name = $line.Substring(0, $idx).Trim()
        $value = $line.Substring($idx + 1).Trim()
        if ($value.StartsWith('"') -and $value.EndsWith('"')) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        $existing = [Environment]::GetEnvironmentVariable($name, 'Process')
        if ($Override -or [string]::IsNullOrEmpty($existing)) {
            [Environment]::SetEnvironmentVariable($name, $value, 'Process')
        }
    }
}
'@

# ---------------------------------------------------------------------------
# Backend env + run script
# ---------------------------------------------------------------------------
Write-Host ""
Write-Host "Creating backend env and run script..." -ForegroundColor Yellow

Write-ProjectFile -RelativePath "backend/.env" -Content @'
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
JWT_SECRET=TravelifyDevSecretKeyThatIsLongEnoughForHS256Algorithm!!
JWT_EXPIRATION_MS=86400000
H2_CONSOLE_ENABLED=true
'@

Write-ProjectFile -RelativePath "backend/.env.dev" -Content @'
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
JWT_SECRET=TravelifyDevSecretKeyThatIsLongEnoughForHS256Algorithm!!
JWT_EXPIRATION_MS=86400000
H2_CONSOLE_ENABLED=true
'@

Write-ProjectFile -RelativePath "backend/.env.prod" -Content @'
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
JWT_SECRET=CHANGE_ME_PROD_SECRET_AT_LEAST_32_CHARS_LONG_SECURE
JWT_EXPIRATION_MS=86400000
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=travelify
POSTGRES_USER=travelify
POSTGRES_PASSWORD=travelify
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/travelify
SPRING_DATASOURCE_USERNAME=travelify
SPRING_DATASOURCE_PASSWORD=travelify
'@

Write-ProjectFile -RelativePath "backend/run-backend.ps1" -Content @'
#Requires -Version 5.1
param(
    [ValidateSet('dev', 'prod')]
    [string]$Profile = 'dev'
)

$ErrorActionPreference = 'Stop'
$BackendRoot = $PSScriptRoot
$RepoRoot = Split-Path -Parent $BackendRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Travelify Backend ($Profile)" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

. (Join-Path $RepoRoot 'scripts\Load-DotEnv.ps1')

# Load base then profile-specific env (profile overrides)
Import-DotEnv -Path (Join-Path $BackendRoot '.env')
Import-DotEnv -Path (Join-Path $BackendRoot ".env.$Profile") -Override

if (-not $env:SPRING_PROFILES_ACTIVE) {
    $env:SPRING_PROFILES_ACTIVE = $Profile
} else {
    $env:SPRING_PROFILES_ACTIVE = $Profile
}

if ($Profile -eq 'prod') {
    if (-not $env:SPRING_DATASOURCE_URL -and $env:POSTGRES_HOST) {
        $db = if ($env:POSTGRES_DB) { $env:POSTGRES_DB } else { 'travelify' }
        $port = if ($env:POSTGRES_PORT) { $env:POSTGRES_PORT } else { '5432' }
        $env:SPRING_DATASOURCE_URL = "jdbc:postgresql://$($env:POSTGRES_HOST):${port}/${db}"
    }
    if (-not $env:SPRING_DATASOURCE_USERNAME -and $env:POSTGRES_USER) {
        $env:SPRING_DATASOURCE_USERNAME = $env:POSTGRES_USER
    }
    if (-not $env:SPRING_DATASOURCE_PASSWORD -and $env:POSTGRES_PASSWORD) {
        $env:SPRING_DATASOURCE_PASSWORD = $env:POSTGRES_PASSWORD
    }
    Write-Host "Database: PostgreSQL ($($env:SPRING_DATASOURCE_URL))" -ForegroundColor Yellow
} else {
    Write-Host "Database: H2 in-memory" -ForegroundColor Yellow
}

Write-Host "JWT secret length: $($env:JWT_SECRET.Length)" -ForegroundColor DarkGray
Write-Host "Starting Spring Boot..." -ForegroundColor Cyan

Set-Location $BackendRoot
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    mvn spring-boot:run "-Dspring-boot.run.profiles=$Profile"
} elseif (Test-Path (Join-Path $BackendRoot 'mvnw.cmd')) {
    & (Join-Path $BackendRoot 'mvnw.cmd') spring-boot:run "-Dspring-boot.run.profiles=$Profile"
} else {
    Write-Host "Maven not found. Install Maven or add mvnw wrapper." -ForegroundColor Red
    exit 1
}
'@

Write-ProjectFile -RelativePath "backend/pom.xml" -Content @'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.1.5</version>
    <relativePath/>
  </parent>

  <groupId>com.travelify</groupId>
  <artifactId>travelify-backend</artifactId>
  <version>1.0.0</version>
  <name>travelify-backend</name>
  <description>Travelify travel booking platform API</description>

  <properties>
    <java.version>17</java.version>
    <jjwt.version>0.11.5</jjwt.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
    </dependency>

    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-api</artifactId>
      <version>${jjwt.version}</version>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-impl</artifactId>
      <version>${jjwt.version}</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-jackson</artifactId>
      <version>${jjwt.version}</version>
      <scope>runtime</scope>
    </dependency>

    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework.security</groupId>
      <artifactId>spring-security-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
          <excludes>
            <exclude>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
            </exclude>
          </excludes>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
'@

Write-Host ""
Write-Host "Creating backend application.yml files..." -ForegroundColor Yellow

Write-ProjectFile -RelativePath "backend/src/main/resources/application.yml" -Content @'
spring:
  application:
    name: travelify
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true

server:
  port: ${SERVER_PORT:8080}

travelify:
  jwt:
    secret: ${JWT_SECRET:TravelifyDevSecretKeyThatIsLongEnoughForHS256Algorithm!!}
    expiration-ms: ${JWT_EXPIRATION_MS:86400000}

logging:
  level:
    com.travelify: INFO
    org.springframework.security: INFO
'@

Write-ProjectFile -RelativePath "backend/src/main/resources/application-dev.yml" -Content @'
spring:
  datasource:
    url: jdbc:h2:mem:travelify;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: ${H2_CONSOLE_ENABLED:true}
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: true

travelify:
  cors:
    allowed-origins: http://localhost:5173,http://127.0.0.1:5173
'@

Write-ProjectFile -RelativePath "backend/src/main/resources/application-prod.yml" -Content @'
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/travelify}
    username: ${SPRING_DATASOURCE_USERNAME:travelify}
    password: ${SPRING_DATASOURCE_PASSWORD:travelify}
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: false
    hibernate:
      ddl-auto: validate

travelify:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173}
'@

Write-Host "Creating backend Java sources..." -ForegroundColor Yellow

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/TravelifyApplication.java" -Content @'
package com.travelify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TravelifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelifyApplication.class, args);
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/model/Role.java" -Content @'
package com.travelify.model;

public enum Role {
    CUSTOMER,
    TRAVEL_AGENT,
    ADMIN
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/model/BookingStatus.java" -Content @'
package com.travelify.model;

public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/model/User.java" -Content @'
package com.travelify.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/model/TravelPackage.java" -Content @'
package com.travelify.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "travel_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer durationDays;

    @Column(nullable = false)
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (active == null) {
            active = true;
        }
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/model/Booking.java" -Content @'
package com.travelify.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id")
    private TravelPackage travelPackage;

    @Column(nullable = false)
    private LocalDate travelDate;

    @Column(nullable = false)
    private Integer travelers;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = BookingStatus.PENDING;
        }
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/repository/UserRepository.java" -Content @'
package com.travelify.repository;

import com.travelify.model.Role;
import com.travelify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/repository/TravelPackageRepository.java" -Content @'
package com.travelify.repository;

import com.travelify.model.TravelPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelPackageRepository extends JpaRepository<TravelPackage, Long> {
    List<TravelPackage> findByActiveTrue();
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/repository/BookingRepository.java" -Content @'
package com.travelify.repository;

import com.travelify.model.Booking;
import com.travelify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomer(User customer);
    List<Booking> findByCustomerId(Long customerId);
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/dto/AuthDtos.java" -Content @'
package com.travelify.dto;

import com.travelify.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

public final class AuthDtos {
    private AuthDtos() {}

    @Data
    public static class RegisterRequest {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 6, max = 100)
        private String password;
        @NotBlank
        private String fullName;
        @NotNull
        private Role role;
    }

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    @Builder
    public static class AuthResponse {
        private String token;
        private String email;
        private String fullName;
        private Role role;
        private Long userId;
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/dto/PackageDtos.java" -Content @'
package com.travelify.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

public final class PackageDtos {
    private PackageDtos() {}

    @Data
    public static class PackageRequest {
        @NotBlank
        private String title;
        private String description;
        @NotBlank
        private String destination;
        @NotNull @DecimalMin("0.0")
        private BigDecimal price;
        @NotNull @Min(1)
        private Integer durationDays;
        private Boolean active = true;
    }

    @Data
    @Builder
    public static class PackageResponse {
        private Long id;
        private String title;
        private String description;
        private String destination;
        private BigDecimal price;
        private Integer durationDays;
        private Boolean active;
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/dto/BookingDtos.java" -Content @'
package com.travelify.dto;

import com.travelify.model.BookingStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class BookingDtos {
    private BookingDtos() {}

    @Data
    public static class BookingRequest {
        @NotNull
        private Long packageId;
        @NotNull
        private LocalDate travelDate;
        @NotNull @Min(1)
        private Integer travelers;
    }

    @Data
    @Builder
    public static class BookingResponse {
        private Long id;
        private Long packageId;
        private String packageTitle;
        private String destination;
        private LocalDate travelDate;
        private Integer travelers;
        private BigDecimal totalPrice;
        private BookingStatus status;
        private String customerEmail;
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/dto/UserDtos.java" -Content @'
package com.travelify.dto;

import com.travelify.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDtos {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/exception/ApiException.java" -Content @'
package com.travelify.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/exception/GlobalExceptionHandler.java" -Content @'
package com.travelify.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
        return body(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fields.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", Instant.now().toString());
        payload.put("status", HttpStatus.BAD_REQUEST.value());
        payload.put("error", "Validation failed");
        payload.put("fields", fields);
        return ResponseEntity.badRequest().body(payload);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", Instant.now().toString());
        payload.put("status", status.value());
        payload.put("error", message);
        return ResponseEntity.status(status).body(payload);
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/security/JwtProperties.java" -Content @'
package com.travelify.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "travelify.jwt")
public class JwtProperties {
    private String secret;
    private long expirationMs;
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/security/JwtService.java" -Content @'
package com.travelify.security;

import com.travelify.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getExpirationMs());
        return Jwts.builder()
                .setSubject(user.getEmail())
                .addClaims(Map.of(
                        "role", user.getRole().name(),
                        "uid", user.getId(),
                        "name", user.getFullName()
                ))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(String token, String email) {
        Claims claims = parseClaims(token);
        return claims.getSubject().equals(email) && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key signingKey() {
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // pad short secrets for HS256 minimum key length in local/dev
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/security/UserDetailsServiceImpl.java" -Content @'
package com.travelify.security;

import com.travelify.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.travelify.model.User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/security/JwtAuthenticationFilter.java" -Content @'
package com.travelify.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        try {
            String email = jwtService.extractEmail(token);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtService.isValid(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/config/SecurityConfig.java" -Content @'
package com.travelify.config;

import com.travelify.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${travelify.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtFilter,
                                                   AuthenticationProvider authenticationProvider) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/packages/**").permitAll()
                        .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/agent/**").hasAnyRole("TRAVEL_AGENT", "ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/config/DataSeeder.java" -Content @'
package com.travelify.config;

import com.travelify.model.Role;
import com.travelify.model.TravelPackage;
import com.travelify.model.User;
import com.travelify.repository.TravelPackageRepository;
import com.travelify.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               TravelPackageRepository packageRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            User admin = userRepository.save(User.builder()
                    .email("admin@travelify.com")
                    .password(passwordEncoder.encode("password123"))
                    .fullName("Travelify Admin")
                    .role(Role.ADMIN)
                    .build());

            User agent = userRepository.save(User.builder()
                    .email("agent@travelify.com")
                    .password(passwordEncoder.encode("password123"))
                    .fullName("Travel Agent")
                    .role(Role.TRAVEL_AGENT)
                    .build());

            userRepository.save(User.builder()
                    .email("customer@travelify.com")
                    .password(passwordEncoder.encode("password123"))
                    .fullName("Demo Customer")
                    .role(Role.CUSTOMER)
                    .build());

            packageRepository.save(TravelPackage.builder()
                    .title("Bali Escape")
                    .description("7-day beach and temple tour across Bali.")
                    .destination("Bali, Indonesia")
                    .price(new BigDecimal("1299.00"))
                    .durationDays(7)
                    .active(true)
                    .createdBy(agent)
                    .build());

            packageRepository.save(TravelPackage.builder()
                    .title("Paris Romance")
                    .description("5-day city break with museum passes and Seine cruise.")
                    .destination("Paris, France")
                    .price(new BigDecimal("1599.00"))
                    .durationDays(5)
                    .active(true)
                    .createdBy(admin)
                    .build());

            packageRepository.save(TravelPackage.builder()
                    .title("Tokyo Discovery")
                    .description("8-day culture, food, and tech itinerary.")
                    .destination("Tokyo, Japan")
                    .price(new BigDecimal("1899.00"))
                    .durationDays(8)
                    .active(true)
                    .createdBy(agent)
                    .build());
        };
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/service/AuthService.java" -Content @'
package com.travelify.service;

import com.travelify.dto.AuthDtos;
import com.travelify.exception.ApiException;
import com.travelify.model.Role;
import com.travelify.model.User;
import com.travelify.repository.UserRepository;
import com.travelify.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email already registered", HttpStatus.CONFLICT);
        }
        Role role = request.getRole() == null ? Role.CUSTOMER : request.getRole();
        if (role == Role.ADMIN) {
            throw new ApiException("Cannot self-register as ADMIN", HttpStatus.FORBIDDEN);
        }
        User user = userRepository.save(User.builder()
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(role)
                .build());
        return toResponse(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = request.getEmail().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED));
        return toResponse(user);
    }

    private AuthDtos.AuthResponse toResponse(User user) {
        return AuthDtos.AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .userId(user.getId())
                .build();
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/service/PackageService.java" -Content @'
package com.travelify.service;

import com.travelify.dto.PackageDtos;
import com.travelify.exception.ApiException;
import com.travelify.model.TravelPackage;
import com.travelify.model.User;
import com.travelify.repository.TravelPackageRepository;
import com.travelify.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PackageService {
    private final TravelPackageRepository packageRepository;
    private final UserRepository userRepository;

    public PackageService(TravelPackageRepository packageRepository, UserRepository userRepository) {
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
    }

    public List<PackageDtos.PackageResponse> listActive() {
        return packageRepository.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    public List<PackageDtos.PackageResponse> listAll() {
        return packageRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PackageDtos.PackageResponse getById(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public PackageDtos.PackageResponse create(PackageDtos.PackageRequest request, String agentEmail) {
        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        TravelPackage entity = TravelPackage.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .destination(request.getDestination())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .active(request.getActive() == null || request.getActive())
                .createdBy(agent)
                .build();
        return toResponse(packageRepository.save(entity));
    }

    @Transactional
    public PackageDtos.PackageResponse update(Long id, PackageDtos.PackageRequest request) {
        TravelPackage entity = find(id);
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setDestination(request.getDestination());
        entity.setPrice(request.getPrice());
        entity.setDurationDays(request.getDurationDays());
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }
        return toResponse(entity);
    }

    private TravelPackage find(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new ApiException("Package not found", HttpStatus.NOT_FOUND));
    }

    private PackageDtos.PackageResponse toResponse(TravelPackage entity) {
        return PackageDtos.PackageResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .destination(entity.getDestination())
                .price(entity.getPrice())
                .durationDays(entity.getDurationDays())
                .active(entity.getActive())
                .build();
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/service/BookingService.java" -Content @'
package com.travelify.service;

import com.travelify.dto.BookingDtos;
import com.travelify.exception.ApiException;
import com.travelify.model.Booking;
import com.travelify.model.BookingStatus;
import com.travelify.model.TravelPackage;
import com.travelify.model.User;
import com.travelify.repository.BookingRepository;
import com.travelify.repository.TravelPackageRepository;
import com.travelify.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final TravelPackageRepository packageRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          TravelPackageRepository packageRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingDtos.BookingResponse create(BookingDtos.BookingRequest request, String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        TravelPackage travelPackage = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new ApiException("Package not found", HttpStatus.NOT_FOUND));
        if (!Boolean.TRUE.equals(travelPackage.getActive())) {
            throw new ApiException("Package is not available", HttpStatus.BAD_REQUEST);
        }
        BigDecimal total = travelPackage.getPrice().multiply(BigDecimal.valueOf(request.getTravelers()));
        Booking booking = bookingRepository.save(Booking.builder()
                .customer(customer)
                .travelPackage(travelPackage)
                .travelDate(request.getTravelDate())
                .travelers(request.getTravelers())
                .totalPrice(total)
                .status(BookingStatus.PENDING)
                .build());
        return toResponse(booking);
    }

    public List<BookingDtos.BookingResponse> forCustomer(String email) {
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return bookingRepository.findByCustomer(customer).stream().map(this::toResponse).toList();
    }

    public List<BookingDtos.BookingResponse> all() {
        return bookingRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public BookingDtos.BookingResponse updateStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException("Booking not found", HttpStatus.NOT_FOUND));
        booking.setStatus(status);
        return toResponse(booking);
    }

    private BookingDtos.BookingResponse toResponse(Booking booking) {
        return BookingDtos.BookingResponse.builder()
                .id(booking.getId())
                .packageId(booking.getTravelPackage().getId())
                .packageTitle(booking.getTravelPackage().getTitle())
                .destination(booking.getTravelPackage().getDestination())
                .travelDate(booking.getTravelDate())
                .travelers(booking.getTravelers())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .customerEmail(booking.getCustomer().getEmail())
                .build();
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/service/UserService.java" -Content @'
package com.travelify.service;

import com.travelify.dto.UserDtos;
import com.travelify.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDtos> listAll() {
        return userRepository.findAll().stream()
                .map(u -> UserDtos.builder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .fullName(u.getFullName())
                        .role(u.getRole())
                        .build())
                .toList();
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/controller/AuthController.java" -Content @'
package com.travelify.controller;

import com.travelify.dto.AuthDtos;
import com.travelify.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/controller/PackageController.java" -Content @'
package com.travelify.controller;

import com.travelify.dto.PackageDtos;
import com.travelify.service.PackageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class PackageController {
    private final PackageService packageService;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @GetMapping
    public List<PackageDtos.PackageResponse> list() {
        return packageService.listActive();
    }

    @GetMapping("/{id}")
    public PackageDtos.PackageResponse get(@PathVariable Long id) {
        return packageService.getById(id);
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/controller/CustomerController.java" -Content @'
package com.travelify.controller;

import com.travelify.dto.BookingDtos;
import com.travelify.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    private final BookingService bookingService;

    public CustomerController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/bookings")
    public List<BookingDtos.BookingResponse> myBookings(Authentication authentication) {
        return bookingService.forCustomer(authentication.getName());
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDtos.BookingResponse book(@Valid @RequestBody BookingDtos.BookingRequest request,
                                            Authentication authentication) {
        return bookingService.create(request, authentication.getName());
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/controller/AgentController.java" -Content @'
package com.travelify.controller;

import com.travelify.dto.BookingDtos;
import com.travelify.dto.PackageDtos;
import com.travelify.model.BookingStatus;
import com.travelify.service.BookingService;
import com.travelify.service.PackageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final PackageService packageService;
    private final BookingService bookingService;

    public AgentController(PackageService packageService, BookingService bookingService) {
        this.packageService = packageService;
        this.bookingService = bookingService;
    }

    @GetMapping("/packages")
    public List<PackageDtos.PackageResponse> packages() {
        return packageService.listAll();
    }

    @PostMapping("/packages")
    @ResponseStatus(HttpStatus.CREATED)
    public PackageDtos.PackageResponse create(@Valid @RequestBody PackageDtos.PackageRequest request,
                                              Authentication authentication) {
        return packageService.create(request, authentication.getName());
    }

    @PutMapping("/packages/{id}")
    public PackageDtos.PackageResponse update(@PathVariable Long id,
                                              @Valid @RequestBody PackageDtos.PackageRequest request) {
        return packageService.update(id, request);
    }

    @GetMapping("/bookings")
    public List<BookingDtos.BookingResponse> bookings() {
        return bookingService.all();
    }

    @PatchMapping("/bookings/{id}/status")
    public BookingDtos.BookingResponse updateStatus(@PathVariable Long id,
                                                    @RequestBody Map<String, String> body) {
        BookingStatus status = BookingStatus.valueOf(body.get("status"));
        return bookingService.updateStatus(id, status);
    }
}
'@

Write-ProjectFile -RelativePath "backend/src/main/java/com/travelify/controller/AdminController.java" -Content @'
package com.travelify.controller;

import com.travelify.dto.BookingDtos;
import com.travelify.dto.PackageDtos;
import com.travelify.dto.UserDtos;
import com.travelify.service.BookingService;
import com.travelify.service.PackageService;
import com.travelify.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;
    private final PackageService packageService;
    private final BookingService bookingService;

    public AdminController(UserService userService,
                           PackageService packageService,
                           BookingService bookingService) {
        this.userService = userService;
        this.packageService = packageService;
        this.bookingService = bookingService;
    }

    @GetMapping("/users")
    public List<UserDtos> users() {
        return userService.listAll();
    }

    @GetMapping("/packages")
    public List<PackageDtos.PackageResponse> packages() {
        return packageService.listAll();
    }

    @GetMapping("/bookings")
    public List<BookingDtos.BookingResponse> bookings() {
        return bookingService.all();
    }

    @GetMapping("/overview")
    public Map<String, Object> overview() {
        Map<String, Object> map = new HashMap<>();
        map.put("users", userService.listAll().size());
        map.put("packages", packageService.listAll().size());
        map.put("bookings", bookingService.all().size());
        return map;
    }
}
'@

Write-Host "Backend Java sources done." -ForegroundColor Green

# Continue in next section via same script execution - frontend follows
Write-Host ""
Write-Host "Creating frontend shared modules..." -ForegroundColor Yellow

Write-ProjectFile -RelativePath "frontend/shared/api/client.js" -Content @'
import axios from 'axios';

const API_BASE_URL = import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8080/api';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('travelify_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('travelify_token');
      localStorage.removeItem('travelify_user');
    }
    return Promise.reject(error);
  }
);

export const authApi = {
  login: (payload) => apiClient.post('/auth/login', payload),
  register: (payload) => apiClient.post('/auth/register', payload),
};

export const packageApi = {
  list: () => apiClient.get('/packages'),
  get: (id) => apiClient.get(`/packages/${id}`),
};

export const customerApi = {
  bookings: () => apiClient.get('/customer/bookings'),
  createBooking: (payload) => apiClient.post('/customer/bookings', payload),
};

export const agentApi = {
  packages: () => apiClient.get('/agent/packages'),
  createPackage: (payload) => apiClient.post('/agent/packages', payload),
  updatePackage: (id, payload) => apiClient.put(`/agent/packages/${id}`, payload),
  bookings: () => apiClient.get('/agent/bookings'),
  updateBookingStatus: (id, status) =>
    apiClient.patch(`/agent/bookings/${id}/status`, { status }),
};

export const adminApi = {
  overview: () => apiClient.get('/admin/overview'),
  users: () => apiClient.get('/admin/users'),
  packages: () => apiClient.get('/admin/packages'),
  bookings: () => apiClient.get('/admin/bookings'),
};

export default apiClient;
'@

Write-ProjectFile -RelativePath "frontend/shared/context/AuthContext.jsx" -Content @'
import React, { createContext, useContext, useMemo, useState, useEffect } from 'react';
import { authApi } from '../api/client';

const AuthContext = createContext(null);
const TOKEN_KEY = 'travelify_token';
const USER_KEY = 'travelify_user';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedToken = localStorage.getItem(TOKEN_KEY);
    const savedUser = localStorage.getItem(USER_KEY);
    if (savedToken && savedUser) {
      setToken(savedToken);
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  const persist = (auth) => {
    localStorage.setItem(TOKEN_KEY, auth.token);
    const profile = {
      email: auth.email,
      fullName: auth.fullName,
      role: auth.role,
      userId: auth.userId,
    };
    localStorage.setItem(USER_KEY, JSON.stringify(profile));
    setToken(auth.token);
    setUser(profile);
  };

  const login = async (email, password) => {
    const { data } = await authApi.login({ email, password });
    persist(data);
    return data;
  };

  const register = async (payload) => {
    const { data } = await authApi.register(payload);
    persist(data);
    return data;
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setToken(null);
    setUser(null);
  };

  const value = useMemo(
    () => ({ user, token, loading, login, register, logout, isAuthenticated: !!token }),
    [user, token, loading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
'@

Write-ProjectFile -RelativePath "frontend/shared/hooks/usePackages.js" -Content @'
import { useEffect, useState } from 'react';
import { packageApi } from '../api/client';

export function usePackages() {
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let mounted = true;
    packageApi
      .list()
      .then((res) => {
        if (mounted) setPackages(res.data);
      })
      .catch((err) => {
        if (mounted) setError(err.message || 'Failed to load packages');
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });
    return () => {
      mounted = false;
    };
  }, []);

  return { packages, loading, error };
}
'@

Write-ProjectFile -RelativePath "frontend/shared/components/PackageCard.jsx" -Content @'
import React from 'react';
import { Card, CardContent, CardActions, Typography, Button, Stack, Chip } from '@mui/material';

export default function PackageCard({ pkg, onBook, actionLabel = 'Book now' }) {
  return (
    <Card variant="outlined" sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardContent sx={{ flexGrow: 1 }}>
        <Stack spacing={1}>
          <Typography variant="h6">{pkg.title}</Typography>
          <Chip label={pkg.destination} size="small" color="primary" variant="outlined" />
          <Typography variant="body2" color="text.secondary">
            {pkg.description}
          </Typography>
          <Typography variant="subtitle1">${Number(pkg.price).toFixed(2)} · {pkg.durationDays} days</Typography>
        </Stack>
      </CardContent>
      {onBook && (
        <CardActions>
          <Button size="small" variant="contained" onClick={() => onBook(pkg)}>
            {actionLabel}
          </Button>
        </CardActions>
      )}
    </Card>
  );
}
'@

Write-ProjectFile -RelativePath "frontend/shared/styles/theme.js" -Content @'
import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#0B6E4F' },
    secondary: { main: '#1B4965' },
    background: { default: '#F4F7F5', paper: '#FFFFFF' },
  },
  typography: {
    fontFamily: '"DM Sans", "Segoe UI", sans-serif',
    h3: { fontFamily: '"Fraunces", Georgia, serif', fontWeight: 600 },
    h4: { fontFamily: '"Fraunces", Georgia, serif', fontWeight: 600 },
    h5: { fontFamily: '"Fraunces", Georgia, serif', fontWeight: 600 },
  },
  shape: { borderRadius: 10 },
});

export default theme;
'@

Write-Host ""
Write-Host "Creating webapp..." -ForegroundColor Yellow

Write-ProjectFile -RelativePath "frontend/webapp/.env" -Content @'
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=Travelify
'@

Write-ProjectFile -RelativePath "frontend/webapp/.env.example" -Content @'
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=Travelify
'@

Write-ProjectFile -RelativePath "frontend/webapp/package.json" -Content @'
{
  "name": "travelify-webapp",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "@emotion/react": "^11.11.1",
    "@emotion/styled": "^11.11.0",
    "@mui/icons-material": "^5.14.14",
    "@mui/material": "^5.14.14",
    "axios": "^1.5.1",
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.17.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.31",
    "@types/react-dom": "^18.2.14",
    "@vitejs/plugin-react": "^4.1.0",
    "vite": "^4.5.0"
  }
}
'@

Write-ProjectFile -RelativePath "frontend/webapp/vite.config.js" -Content @'
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@shared': path.resolve(__dirname, '../shared'),
    },
  },
  server: {
    port: 5173,
    host: true,
  },
});
'@

Write-ProjectFile -RelativePath "frontend/webapp/index.html" -Content @'
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Travelify</title>
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;700&family=Fraunces:wght@600;700&display=swap" rel="stylesheet" />
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.jsx"></script>
  </body>
</html>
'@

Write-ProjectFile -RelativePath "frontend/webapp/run-webapp-app.ps1" -Content @'
#Requires -Version 5.1
$ErrorActionPreference = 'Stop'
$WebRoot = $PSScriptRoot
$RepoRoot = Split-Path -Parent (Split-Path -Parent $WebRoot)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Travelify Web App" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

. (Join-Path $RepoRoot 'scripts\Load-DotEnv.ps1')
Import-DotEnv -Path (Join-Path $WebRoot '.env')

Write-Host "API: $($env:VITE_API_BASE_URL)" -ForegroundColor Yellow
Set-Location $WebRoot

if (-not (Test-Path (Join-Path $WebRoot 'node_modules'))) {
    Write-Host "Installing npm dependencies..." -ForegroundColor Cyan
    npm install
}

Write-Host "Starting Vite dev server..." -ForegroundColor Cyan
npm run dev
'@

Write-ProjectFile -RelativePath "frontend/webapp/src/main.jsx" -Content @'
import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { CssBaseline, ThemeProvider } from '@mui/material';
import App from './App';
import { AuthProvider } from '@shared/context/AuthContext';
import theme from '@shared/styles/theme';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <AuthProvider>
          <App />
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  </React.StrictMode>
);
'@

Write-ProjectFile -RelativePath "frontend/webapp/src/App.jsx" -Content @'
import React from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import AppLayout from './layouts/AppLayout';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import PackagesPage from './pages/PackagesPage';
import BookingPage from './pages/BookingPage';
import CustomerDashboard from './pages/CustomerDashboard';
import AgentDashboard from './pages/AgentDashboard';
import AdminDashboard from './pages/AdminDashboard';
import ProtectedRoute from './components/ProtectedRoute';

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/packages" element={<PackagesPage />} />
        <Route
          path="/book/:packageId"
          element={
            <ProtectedRoute roles={['CUSTOMER']}>
              <BookingPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard/customer"
          element={
            <ProtectedRoute roles={['CUSTOMER']}>
              <CustomerDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard/agent"
          element={
            <ProtectedRoute roles={['TRAVEL_AGENT', 'ADMIN']}>
              <AgentDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard/admin"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <AdminDashboard />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
'@

Write-ProjectFile -RelativePath "frontend/webapp/src/components/ProtectedRoute.jsx" -Content @'
import React from 'react';
import { Navigate } from 'react-router-dom';
import { CircularProgress, Box } from '@mui/material';
import { useAuth } from '@shared/context/AuthContext';

export default function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, user, loading } = useAuth();

  if (loading) {
    return (
      <Box sx={{ display: 'grid', placeItems: 'center', minHeight: 240 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(user?.role)) return <Navigate to="/" replace />;
  return children;
}
'@

Write-ProjectFile -RelativePath "frontend/webapp/src/layouts/AppLayout.jsx" -Content @'
import React from 'react';
import { Link as RouterLink, Outlet, useNavigate } from 'react-router-dom';
import {
  AppBar,
  Box,
  Button,
  Container,
  Link,
  Stack,
  Toolbar,
  Typography,
} from '@mui/material';
import { useAuth } from '@shared/context/AuthContext';

function dashboardPath(role) {
  if (role === 'ADMIN') return '/dashboard/admin';
  if (role === 'TRAVEL_AGENT') return '/dashboard/agent';
  return '/dashboard/customer';
}

export default function AppLayout() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <Box
      sx={{
        minHeight: '100vh',
        background:
          'radial-gradient(circle at top left, rgba(11,110,79,0.12), transparent 40%), linear-gradient(180deg, #F4F7F5 0%, #E8F0EC 100%)',
      }}
    >
      <AppBar position="sticky" color="transparent" elevation={0} sx={{ borderBottom: '1px solid rgba(0,0,0,0.08)' }}>
        <Toolbar>
          <Typography
            component={RouterLink}
            to="/"
            variant="h5"
            sx={{ flexGrow: 1, textDecoration: 'none', color: 'primary.main', fontFamily: 'Fraunces, serif' }}
          >
            Travelify
          </Typography>
          <Stack direction="row" spacing={1} alignItems="center">
            <Button component={RouterLink} to="/packages" color="inherit">
              Packages
            </Button>
            {isAuthenticated ? (
              <>
                <Button component={RouterLink} to={dashboardPath(user.role)} color="inherit">
                  Dashboard
                </Button>
                <Typography variant="body2" sx={{ px: 1 }}>
                  {user.fullName}
                </Typography>
                <Button
                  variant="outlined"
                  onClick={() => {
                    logout();
                    navigate('/');
                  }}
                >
                  Logout
                </Button>
              </>
            ) : (
              <>
                <Button component={RouterLink} to="/login" color="inherit">
                  Login
                </Button>
                <Button component={RouterLink} to="/register" variant="contained">
                  Register
                </Button>
              </>
            )}
          </Stack>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Outlet />
      </Container>
      <Box component="footer" sx={{ py: 3, textAlign: 'center' }}>
        <Typography variant="body2" color="text.secondary">
          © {new Date().getFullYear()} Travelify · Book journeys with confidence
        </Typography>
        <Link href="http://localhost:8080/api/packages" target="_blank" rel="noreferrer" underline="hover">
          API health
        </Link>
      </Box>
    </Box>
  );
}
'@

Write-ProjectFile -RelativePath "frontend/webapp/src/pages/HomePage.jsx" -Content @'
import React from 'react';
import { Box, Button, Stack, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

export default function HomePage() {
  return (
    <Box
      sx={{
        minHeight: '70vh',
        display: 'grid',
        alignContent: 'center',
        gap: 2,
        backgroundImage:
          'linear-gradient(120deg, rgba(11,110,79,0.85), rgba(27,73,101,0.75)), url(https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=1600&q=80)',
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        color: '#fff',
        borderRadius: 3,
        px: { xs: 3, md: 6 },
        py: { xs: 8, md: 10 },
      }}
    >
      <Typography variant="h3">Travelify</Typography>
      <Typography variant="h5" sx={{ maxWidth: 560, opacity: 0.95 }}>
        Plan, book, and manage trips for customers, agents, and admins in one place.
      </Typography>
      <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
        <Button component={RouterLink} to="/packages" variant="contained" color="secondary" size="large">
          Browse packages
        </Button>
        <Button component={RouterLink} to="/register" variant="outlined" size="large" sx={{ color: '#fff', borderColor: '#fff' }}>
          Create account
        </Button>
      </Stack>
    </Box>
  );
}
'@

Write-ProjectFile -RelativePath "frontend/webapp/src/pages/LoginPage.jsx" -Content @'
import React, { useState } from 'react';
import { Alert, Box, Button, Paper, Stack, TextField, Typography } from '@mui/material';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { useAuth } from '@shared/context/AuthContext';

function dashboardFor(role) {
  if (role === 'ADMIN') return '/dashboard/admin';
  if (role === 'TRAVEL_AGENT') return '/dashboard/agent';
  return '/dashboard/customer';
}

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('customer@travelify.com');
  const [password, setPassword] = useState('password123');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      const auth = await login(email, password);
      navigate(dashboardFor(auth.role));
    } catch (err) {
      setError(err.response?.data?.error || 'Login failed');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Box sx={{ maxWidth: 420, mx: 'auto' }}>
      <Paper sx={{ p: 3 }} elevation={0} variant="outlined">
        <Typography variant="h5" gutterBottom>
          Sign in
        </Typography>
        <Stack component="form" spacing={2} onSubmit={onSubmit}>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required fullWidth />
          <TextField label="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required fullWidth />
          <Button type="submit" variant="contained" disabled={busy}>
            {busy ? 'Signing in…' : 'Login'}
          </Button>
          <Button component={RouterLink} to="/register">
            Need an account? Register
          </Button>
        </Stack>
      </Paper>
    </Box>
  );
}
'@

Write-ProjectFile -RelativePath "frontend/webapp/src/pages/RegisterPage.jsx" -Content @'
import React, { useState } from 'react';
import {
  Alert,
  Box,
  Button,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@shared/context/AuthContext';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    role: 'CUSTOMER',
  });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const onSubmit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      const auth = await register(form);
      if (auth.role === 'TRAVEL_AGENT') navigate('/dashboard/agent');
      else navigate('/dashboard/customer');
    } catch (err) {
      setError(err.response?.data?.error || 'Registration failed');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Box sx={{ maxWidth: 480, mx: 'auto' }}>
      <Paper sx={{ p: 3 }} elevation={0} variant="outlined">
        <Typography variant="h5" gutterBottom>
          Create account
        </Typography>
        <Stack component="form" spacing={2} onSubmit={onSubmit}>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField name="fullName" label="Full name" value={form.fullName} onChange={onChange} required fullWidth />
          <TextField name="email" label="Email" type="email" value={form.email} onChange={onChange} required fullWidth />
          <TextField name="password" label="Password" type="password" value={form.password} onChange={onChange} required fullWidth />
          <TextField select name="role" label="Role" value={form.role} onChange={onChange} fullWidth>
            <MenuItem value="CUSTOMER">Customer</MenuItem>
            <MenuItem value="TRAVEL_AGENT">Travel Agent</MenuItem>
          </TextField>
          <Button type="submit" variant="contained" disabled={busy}>
            {busy ? 'Creating…' : 'Register'}
          </Button>
        </Stack>
      </Paper>
    </Box>
  );
}
'@

Write-ProjectFile -RelativePath "frontend/webapp/src/pages/PackagesPage.jsx" -Content @'
import React from 'react';
import { Alert, CircularProgress, Grid, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import PackageCard from '@shared/components/PackageCard';
import { usePackages } from '@shared/hooks/usePackages';
import { useAuth } from '@shared/context/AuthContext';

export default function PackagesPage() {
  const { packages, loading, error } = usePackages();
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  const onBook = (pkg) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (user.role !== 'CUSTOMER') {
      navigate(user.role === 'ADMIN' ? '/dashboard/admin' : '/dashboard/agent');
      return;
    }
    navigate(`/book/${pkg.id}`);
  };

  if (loading) return <CircularProgress />;
  if (error) return <Alert severity="error">{error}</Alert>;

  return (
    <>
      <Typography variant="h4" gutterBottom>
        Travel packages
      </Typography>
      <Grid container spacing={2}>
        {packages.map((pkg) => (
          <Grid item xs={12} md={4} key={pkg.id}>
            <PackageCard pkg={pkg} onBook={onBook} />
          </Grid>
        ))}
      </Grid>
    </>
  );
}
'@

Write-ProjectFile -RelativePath "frontend/webapp/src/pages/BookingPage.jsx" -Content @'
import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Alert,
  Button,
  CircularProgress,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { customerApi, packageApi } from '@shared/api/client';

export default function BookingPage() {
  const { packageId } = useParams();
  const navigate = useNavigate();
  const [pkg, setPkg] = useState(null);
  const [travelDate, setTravelDate] = useState('');
  const [travelers, setTravelers] = useState(1);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    packageApi
      .get(packageId)
      .then((res) => setPkg(res.data))
      .catch(() => setError('Package not found'));
  }, [packageId]);

  const onSubmit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      await customerApi.createBooking({
        packageId: Number(packageId),
        travelDate,
        travelers: Number(travelers),
      });
      navigate('/dashboard/customer');
    } catch (err) {
      setError(err.response?.data?.error || 'Booking failed');
    } finally {
      setBusy(false);
    }
  };

  if (!pkg && !error) return <CircularProgress />;

  return (
    <Paper sx={{ p: 3, maxWidth: 520 }} variant="outlined">
      <Typography variant="h5" gutterBottom>
        Book: {pkg?.title}
      </Typography>
      <Typography variant="body2" color="text.secondary" gutterBottom>
        {pkg?.destination} · ${Number(pkg?.price || 0).toFixed(2)} / person
      </Typography>
      <Stack component="form" spacing={2} onSubmit={onSubmit} sx={{ mt: 2 }}>
        {error && <Alert severity="error">{error}</Alert>}
        <TextField
          label="Travel date"
          type="date"
          InputLabelProps={{ shrink: true }}
          value={travelDate}
          onChange={(e) => setTravelDate(e.target.value)}
          required
          fullWidth
        />
        <TextField
          label="Travelers"
          type="number"
          inputProps={{ min: 1 }}
          value={travelers}
          onChange={(e) => setTravelers(e.target.value)}
          required
          fullWidth
        />
        <Button type="submit" variant="contained" disabled={busy}>
          {busy ? 'Booking…' : 'Confirm booking'}
        </Button>
      </Stack>
    </Paper>
  );
}
'@

Write-ProjectFile -RelativePath "frontend/webapp/src/pages/CustomerDashboard.jsx" -Content @'
import React, { useEffect, useState } from 'react';
import {
  Alert,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { customerApi } from '@shared/api/client';

export default function CustomerDashboard() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    customerApi
      .bookings()
      .then((res) => setBookings(res.data))
      .catch((err) => setError(err.response?.data?.error || 'Failed to load bookings'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <CircularProgress />;
  if (error) return <Alert severity="error">{error}</Alert>;

  return (
    <>
      <Typography variant="h4" gutterBottom>
        Customer dashboard
      </Typography>
      <Paper variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Package</TableCell>
              <TableCell>Destination</TableCell>
              <TableCell>Date</TableCell>
              <TableCell>Travelers</TableCell>
              <TableCell>Total</TableCell>
              <TableCell>Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {bookings.map((b) => (
              <TableRow key={b.id}>
                <TableCell>{b.packageTitle}</TableCell>
                <TableCell>{b.destination}</TableCell>
                <TableCell>{b.travelDate}</TableCell>
                <TableCell>{b.travelers}</TableCell>
                <TableCell>${Number(b.totalPrice).toFixed(2)}</TableCell>
                <TableCell>{b.status}</TableCell>
              </TableRow>
            ))}
            {bookings.length === 0 && (
              <TableRow>
                <TableCell colSpan={6}>No bookings yet. Browse packages to get started.</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>
    </>
  );
}
'@

Write-ProjectFile -RelativePath "frontend/webapp/src/pages/AgentDashboard.jsx" -Content @'
import React, { useEffect, useState } from 'react';
import {
  Alert,
  Button,
  CircularProgress,
  Grid,
  MenuItem,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { agentApi } from '@shared/api/client';
import PackageCard from '@shared/components/PackageCard';

const emptyForm = {
  title: '',
  description: '',
  destination: '',
  price: '',
  durationDays: 5,
  active: true,
};

export default function AgentDashboard() {
  const [packages, setPackages] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const refresh = async () => {
    const [pkgRes, bookRes] = await Promise.all([agentApi.packages(), agentApi.bookings()]);
    setPackages(pkgRes.data);
    setBookings(bookRes.data);
  };

  useEffect(() => {
    refresh()
      .catch((err) => setError(err.response?.data?.error || 'Failed to load agent data'))
      .finally(() => setLoading(false));
  }, []);

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const onCreate = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await agentApi.createPackage({
        ...form,
        price: Number(form.price),
        durationDays: Number(form.durationDays),
        active: true,
      });
      setForm(emptyForm);
      await refresh();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create package');
    }
  };

  const onStatus = async (id, status) => {
    await agentApi.updateBookingStatus(id, status);
    await refresh();
  };

  if (loading) return <CircularProgress />;

  return (
    <Stack spacing={3}>
      <Typography variant="h4">Travel agent dashboard</Typography>
      {error && <Alert severity="error">{error}</Alert>}

      <Paper variant="outlined" sx={{ p: 2 }}>
        <Typography variant="h6" gutterBottom>
          Create package
        </Typography>
        <Stack component="form" spacing={2} onSubmit={onCreate}>
          <TextField name="title" label="Title" value={form.title} onChange={onChange} required />
          <TextField name="destination" label="Destination" value={form.destination} onChange={onChange} required />
          <TextField name="description" label="Description" value={form.description} onChange={onChange} multiline minRows={2} />
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <TextField name="price" label="Price" type="number" value={form.price} onChange={onChange} required fullWidth />
            <TextField name="durationDays" label="Days" type="number" value={form.durationDays} onChange={onChange} required fullWidth />
          </Stack>
          <Button type="submit" variant="contained">
            Add package
          </Button>
        </Stack>
      </Paper>

      <Typography variant="h6">Packages</Typography>
      <Grid container spacing={2}>
        {packages.map((pkg) => (
          <Grid item xs={12} md={4} key={pkg.id}>
            <PackageCard pkg={pkg} />
          </Grid>
        ))}
      </Grid>

      <Typography variant="h6">Bookings</Typography>
      <Paper variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Customer</TableCell>
              <TableCell>Package</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {bookings.map((b) => (
              <TableRow key={b.id}>
                <TableCell>{b.customerEmail}</TableCell>
                <TableCell>{b.packageTitle}</TableCell>
                <TableCell>{b.status}</TableCell>
                <TableCell>
                  <TextField
                    select
                    size="small"
                    value={b.status}
                    onChange={(e) => onStatus(b.id, e.target.value)}
                    sx={{ minWidth: 140 }}
                  >
                    {['PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'].map((s) => (
                      <MenuItem key={s} value={s}>
                        {s}
                      </MenuItem>
                    ))}
                  </TextField>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Stack>
  );
}
'@

Write-ProjectFile -RelativePath "frontend/webapp/src/pages/AdminDashboard.jsx" -Content @'
import React, { useEffect, useState } from 'react';
import {
  Alert,
  CircularProgress,
  Grid,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { adminApi } from '@shared/api/client';

export default function AdminDashboard() {
  const [overview, setOverview] = useState(null);
  const [users, setUsers] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([adminApi.overview(), adminApi.users(), adminApi.bookings()])
      .then(([o, u, b]) => {
        setOverview(o.data);
        setUsers(u.data);
        setBookings(b.data);
      })
      .catch((err) => setError(err.response?.data?.error || 'Failed to load admin data'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <CircularProgress />;
  if (error) return <Alert severity="error">{error}</Alert>;

  return (
    <Stack spacing={3}>
      <Typography variant="h4">Admin dashboard</Typography>
      <Grid container spacing={2}>
        {['users', 'packages', 'bookings'].map((key) => (
          <Grid item xs={12} md={4} key={key}>
            <Paper variant="outlined" sx={{ p: 2 }}>
              <Typography variant="overline">{key}</Typography>
              <Typography variant="h4">{overview?.[key] ?? 0}</Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>

      <Typography variant="h6">Users</Typography>
      <Paper variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Role</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.map((u) => (
              <TableRow key={u.id}>
                <TableCell>{u.fullName}</TableCell>
                <TableCell>{u.email}</TableCell>
                <TableCell>{u.role}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>

      <Typography variant="h6">All bookings</Typography>
      <Paper variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Customer</TableCell>
              <TableCell>Package</TableCell>
              <TableCell>Total</TableCell>
              <TableCell>Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {bookings.map((b) => (
              <TableRow key={b.id}>
                <TableCell>{b.customerEmail}</TableCell>
                <TableCell>{b.packageTitle}</TableCell>
                <TableCell>${Number(b.totalPrice).toFixed(2)}</TableCell>
                <TableCell>{b.status}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Stack>
  );
}
'@

Write-Host "Webapp done." -ForegroundColor Green

# ---------------------------------------------------------------------------
# Mobile placeholders
# ---------------------------------------------------------------------------
Write-Host ""
Write-Host "Creating mobile placeholders and run scripts..." -ForegroundColor Yellow

Write-ProjectFile -RelativePath "frontend/mobile/android/shared/ApiClient.kt" -Content @'
package com.travelify.shared

/**
 * Shared Android API client stub reused by Customer / Travel Agent / Admin apps.
 * Wire OkHttp/Retrofit against VITE-equivalent base URL from BuildConfig.
 */
object ApiClient {
    const val DEFAULT_BASE_URL = "http://10.0.2.2:8080/api"

    fun authHeader(token: String): String = "Bearer $token"
}
'@

Write-ProjectFile -RelativePath "frontend/mobile/android/shared/AuthStore.kt" -Content @'
package com.travelify.shared

/** Minimal shared auth token holder for role-specific Android apps. */
object AuthStore {
    @Volatile
    var token: String? = null

    @Volatile
    var role: String? = null

    fun clear() {
        token = null
        role = null
    }
}
'@

Write-ProjectFile -RelativePath "frontend/mobile/android/customer/README.md" -Content @'
# Travelify Customer (Android)

Kotlin placeholder for the Customer role app.

## Intended build

```bash
./gradlew :customer:assembleDebug
adb install -r customer/build/outputs/apk/debug/customer-debug.apk
```

Point `ApiClient.DEFAULT_BASE_URL` at your machine (`10.0.2.2` for emulator).
'@

Write-ProjectFile -RelativePath "frontend/mobile/android/travel-agent/README.md" -Content @'
# Travelify Travel Agent (Android)

Kotlin placeholder for the Travel Agent role app.

## Intended build

```bash
./gradlew :travel-agent:assembleDebug
```
'@

Write-ProjectFile -RelativePath "frontend/mobile/android/admin/README.md" -Content @'
# Travelify Admin (Android)

Kotlin placeholder for the Admin role app.

## Intended build

```bash
./gradlew :admin:assembleDebug
```
'@

Write-ProjectFile -RelativePath "frontend/mobile/ios/Shared/ApiClient.swift" -Content @'
import Foundation

/// Shared iOS API client stub for Customer / Travel Agent / Admin targets.
enum ApiClient {
    static let defaultBaseURL = URL(string: "http://localhost:8080/api")!

    static func authHeader(token: String) -> String {
        "Bearer \(token)"
    }
}
'@

Write-ProjectFile -RelativePath "frontend/mobile/ios/Shared/AuthStore.swift" -Content @'
import Foundation

final class AuthStore {
    static let shared = AuthStore()
    var token: String?
    var role: String?

    func clear() {
        token = nil
        role = nil
    }
}
'@

Write-ProjectFile -RelativePath "frontend/mobile/ios/Customer/README.md" -Content @'
# Travelify Customer (iOS)

Swift placeholder. Open in Xcode when the native project is generated.

## Intended build

```bash
xcodebuild -scheme TravelifyCustomer -destination "platform=iOS Simulator,name=iPhone 15" build
```
'@

Write-ProjectFile -RelativePath "frontend/mobile/ios/TravelAgent/README.md" -Content @'
# Travelify Travel Agent (iOS)

## Intended build

```bash
xcodebuild -scheme TravelifyTravelAgent -destination "platform=iOS Simulator,name=iPhone 15" build
```
'@

Write-ProjectFile -RelativePath "frontend/mobile/ios/Admin/README.md" -Content @'
# Travelify Admin (iOS)

## Intended build

```bash
xcodebuild -scheme TravelifyAdmin -destination "platform=iOS Simulator,name=iPhone 15" build
```
'@

$mobileRunScripts = @(
    @{
        Name = 'run-customer-android-app.ps1'
        Title = 'Customer Android'
        Body = @'
Write-Host "Requires Android SDK / Android Studio." -ForegroundColor Yellow
Write-Host "Intended commands:" -ForegroundColor Cyan
Write-Host "  cd frontend/mobile/android"
Write-Host "  ./gradlew :customer:assembleDebug"
Write-Host "  adb install -r customer/build/outputs/apk/debug/customer-debug.apk"
Write-Host ""
Write-Host "Shared API client: frontend/mobile/android/shared/ApiClient.kt" -ForegroundColor DarkGray
Write-Host "See: frontend/mobile/android/customer/README.md" -ForegroundColor DarkGray
'@
    },
    @{
        Name = 'run-travel-agent-android-app.ps1'
        Title = 'Travel Agent Android'
        Body = @'
Write-Host "Requires Android SDK / Android Studio." -ForegroundColor Yellow
Write-Host "Intended commands:" -ForegroundColor Cyan
Write-Host "  cd frontend/mobile/android"
Write-Host "  ./gradlew :travel-agent:assembleDebug"
Write-Host ""
Write-Host "See: frontend/mobile/android/travel-agent/README.md" -ForegroundColor DarkGray
'@
    },
    @{
        Name = 'run-admin-android-app.ps1'
        Title = 'Admin Android'
        Body = @'
Write-Host "Requires Android SDK / Android Studio." -ForegroundColor Yellow
Write-Host "Intended commands:" -ForegroundColor Cyan
Write-Host "  cd frontend/mobile/android"
Write-Host "  ./gradlew :admin:assembleDebug"
Write-Host ""
Write-Host "See: frontend/mobile/android/admin/README.md" -ForegroundColor DarkGray
'@
    },
    @{
        Name = 'run-customer-ios-app.ps1'
        Title = 'Customer iOS'
        Body = @'
Write-Host "Requires macOS + Xcode." -ForegroundColor Yellow
Write-Host "Intended commands:" -ForegroundColor Cyan
Write-Host "  xcodebuild -scheme TravelifyCustomer -destination ""platform=iOS Simulator,name=iPhone 15"" build"
Write-Host ""
Write-Host "Shared API client: frontend/mobile/ios/Shared/ApiClient.swift" -ForegroundColor DarkGray
Write-Host "See: frontend/mobile/ios/Customer/README.md" -ForegroundColor DarkGray
'@
    },
    @{
        Name = 'run-travel-agent-ios-app.ps1'
        Title = 'Travel Agent iOS'
        Body = @'
Write-Host "Requires macOS + Xcode." -ForegroundColor Yellow
Write-Host "Intended commands:" -ForegroundColor Cyan
Write-Host "  xcodebuild -scheme TravelifyTravelAgent -destination ""platform=iOS Simulator,name=iPhone 15"" build"
Write-Host ""
Write-Host "See: frontend/mobile/ios/TravelAgent/README.md" -ForegroundColor DarkGray
'@
    },
    @{
        Name = 'run-admin-ios-app.ps1'
        Title = 'Admin iOS'
        Body = @'
Write-Host "Requires macOS + Xcode." -ForegroundColor Yellow
Write-Host "Intended commands:" -ForegroundColor Cyan
Write-Host "  xcodebuild -scheme TravelifyAdmin -destination ""platform=iOS Simulator,name=iPhone 15"" build"
Write-Host ""
Write-Host "See: frontend/mobile/ios/Admin/README.md" -ForegroundColor DarkGray
'@
    }
)

foreach ($script in $mobileRunScripts) {
    $content = @"
#Requires -Version 5.1
`$ErrorActionPreference = 'Stop'
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Travelify $($script.Title)" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
$($script.Body)
"@
    Write-ProjectFile -RelativePath ("frontend/mobile/" + $script.Name) -Content $content
}

Write-ProjectFile -RelativePath "frontend/mobile/android/settings.gradle.kts.placeholder" -Content @'
// Placeholder Gradle settings for future multi-module Android project:
// include(":customer", ":travel-agent", ":admin", ":shared")
'@

Write-ProjectFile -RelativePath "backend/src/test/java/com/travelify/TravelifyApplicationTests.java" -Content @'
package com.travelify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class TravelifyApplicationTests {
    @Test
    void contextLoads() {
    }
}
'@

Write-Banner "SETUP COMPLETE" "Green"
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. cd backend; .\run-backend.ps1" -ForegroundColor White
Write-Host "  2. cd frontend\webapp; .\run-webapp-app.ps1" -ForegroundColor White
Write-Host "  3. Open http://localhost:5173" -ForegroundColor White
Write-Host ""
Write-Host "Demo logins: admin@travelify.com / agent@travelify.com / customer@travelify.com" -ForegroundColor DarkGray
Write-Host "Password: password123" -ForegroundColor DarkGray
Write-Host ""
