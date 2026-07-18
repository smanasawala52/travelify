#Requires -Version 5.1
$ErrorActionPreference = 'Stop'
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Travelify Customer Android" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Requires Android SDK / Android Studio." -ForegroundColor Yellow
Write-Host "Intended commands:" -ForegroundColor Cyan
Write-Host "  cd frontend/mobile/android"
Write-Host "  ./gradlew :customer:assembleDebug"
Write-Host "  adb install -r customer/build/outputs/apk/debug/customer-debug.apk"
Write-Host ""
Write-Host "Shared API client: frontend/mobile/android/shared/ApiClient.kt" -ForegroundColor DarkGray
Write-Host "See: frontend/mobile/android/customer/README.md" -ForegroundColor DarkGray