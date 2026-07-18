#Requires -Version 5.1
$ErrorActionPreference = 'Stop'
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Travelify Admin Android" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Requires Android SDK / Android Studio." -ForegroundColor Yellow
Write-Host "Intended commands:" -ForegroundColor Cyan
Write-Host "  cd frontend/mobile/android"
Write-Host "  ./gradlew :admin:assembleDebug"
Write-Host ""
Write-Host "See: frontend/mobile/android/admin/README.md" -ForegroundColor DarkGray