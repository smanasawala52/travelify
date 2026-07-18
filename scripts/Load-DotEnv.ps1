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