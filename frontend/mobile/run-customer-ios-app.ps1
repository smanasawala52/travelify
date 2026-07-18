#Requires -Version 5.1
$ErrorActionPreference = 'Stop'
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Travelify Customer iOS" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Requires macOS + Xcode." -ForegroundColor Yellow
Write-Host "Intended commands:" -ForegroundColor Cyan
Write-Host "  xcodebuild -scheme TravelifyCustomer -destination ""platform=iOS Simulator,name=iPhone 15"" build"
Write-Host ""
Write-Host "Shared API client: frontend/mobile/ios/Shared/ApiClient.swift" -ForegroundColor DarkGray
Write-Host "See: frontend/mobile/ios/Customer/README.md" -ForegroundColor DarkGray