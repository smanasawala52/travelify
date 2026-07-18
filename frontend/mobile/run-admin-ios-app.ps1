#Requires -Version 5.1
$ErrorActionPreference = 'Stop'
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Travelify Admin iOS" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Requires macOS + Xcode." -ForegroundColor Yellow
Write-Host "Intended commands:" -ForegroundColor Cyan
Write-Host "  xcodebuild -scheme TravelifyAdmin -destination ""platform=iOS Simulator,name=iPhone 15"" build"
Write-Host ""
Write-Host "See: frontend/mobile/ios/Admin/README.md" -ForegroundColor DarkGray