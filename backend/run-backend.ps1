clear
git pull origin main
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Travelify Backend" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan



# Remove existing environment variables if present
@(
    "SPRING_DATASOURCE_DRIVERCLASSNAME",
    "SPRING_DATASOURCE_URL",
    "SPRING_DATASOURCE_USERNAME",
    "SPRING_DATASOURCE_PASSWORD",
    "APP_JWT_SECRET",
    "SPRING_PROFILES_ACTIVE"
) | ForEach-Object {
    Remove-Item "Env:$_" -ErrorAction SilentlyContinue
}

Write-Host "Building backend..." -ForegroundColor Cyan
mvn clean install

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Build failed. Backend will not start." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Setting production environment..." -ForegroundColor Cyan

$env:SPRING_PROFILES_ACTIVE = "dev"
Write-Host "Starting Spring Boot..." -ForegroundColor Green
mvn spring-boot:run
