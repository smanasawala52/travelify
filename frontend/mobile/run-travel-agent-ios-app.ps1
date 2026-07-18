#Requires -Version 5.1
$ErrorActionPreference = 'Stop'
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Travelify Travel Agent iOS" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Requires macOS + Xcode." -ForegroundColor Yellow
Write-Host "Intended commands:" -ForegroundColor Cyan
Write-Host "  xcodebuild -scheme TravelifyTravelAgent -destination ""platform=iOS Simulator,name=iPhone 15"" build"
Write-Host ""
Write-Host "See: frontend/mobile/ios/TravelAgent/README.md" -ForegroundColor DarkGray