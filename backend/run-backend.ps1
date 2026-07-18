#Requires -Version 5.1
param(
    [ValidateSet('dev', 'prod')]
    [string]$Profile = 'dev'
)
clear
git pull origin main

$env:SPRING_PROFILES_ACTIVE = "dev"
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
    # Prevent machine-level Postgres/Supabase env vars from overriding H2 in dev
    Remove-Item Env:SPRING_DATASOURCE_URL -ErrorAction SilentlyContinue
    Remove-Item Env:SPRING_DATASOURCE_USERNAME -ErrorAction SilentlyContinue
    Remove-Item Env:SPRING_DATASOURCE_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:SPRING_DATASOURCE_DRIVER_CLASS_NAME -ErrorAction SilentlyContinue
    $env:SPRING_DATASOURCE_URL = 'jdbc:h2:mem:travelify;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH'
    $env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = 'org.h2.Driver'
    $env:SPRING_DATASOURCE_USERNAME = 'sa'
    $env:SPRING_DATASOURCE_PASSWORD = ''
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