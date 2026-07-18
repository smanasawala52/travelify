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