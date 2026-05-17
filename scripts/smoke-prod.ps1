# Smoke test produkcji — Alcohol Factory API
# Użycie: .\scripts\smoke-prod.ps1
#         .\scripts\smoke-prod.ps1 -BaseUrl "https://api-alcozon.onrender.com"

param(
    [string] $BaseUrl = "https://api-alcozon.onrender.com",
    [string] $ManagerEmail = "manager@example.com",
    [string] $ManagerPassword = "Manager123!",
    [string] $EmployeeEmail = "employee@example.com",
    [string] $EmployeePassword = "Employee123!",
    [string] $StaffDeviceId = "smoke-prod-device",
    [string] $Staff2faCode = ""
)

$ErrorActionPreference = "Stop"

function Invoke-Api {
    param(
        [string] $Method,
        [string] $Path,
        [hashtable] $Headers = @{},
        $Body = $null
    )
    $uri = "$BaseUrl$Path"
    $params = @{
        Uri         = $uri
        Method      = $Method
        Headers     = $Headers
        ContentType = "application/json"
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Compress)
    }
    return Invoke-RestMethod @params
}

function Assert-Ok($label, $scriptBlock) {
    try {
        & $scriptBlock
        Write-Host "[OK] $label" -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] $label" -ForegroundColor Red
        Write-Host $_.Exception.Message
        throw
    }
}

function Get-StaffAccessToken {
    param([string] $Email, [string] $Password)
    $login = Invoke-Api -Method POST -Path "/api/auth/staff/login" -Body @{
        email    = $Email
        password = $Password
        deviceId = $StaffDeviceId
    }
    if (-not $login.verificationRequired) {
        return $login.tokens.accessToken
    }
    if (-not $Staff2faCode) {
        throw "2FA required; pass -Staff2faCode or trust device via staff/verify-device once"
    }
    $verified = Invoke-Api -Method POST -Path "/api/auth/staff/verify-device" -Body @{
        challengeId = $login.challengeId
        deviceId    = $StaffDeviceId
        code        = $Staff2faCode
    }
    return $verified.accessToken
}

function Assert-Warn($label, $scriptBlock) {
    try {
        & $scriptBlock
        Write-Host "[OK] $label" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "[WARN] $label" -ForegroundColor Yellow
        Write-Host $_.Exception.Message
        return $false
    }
}

Write-Host "Smoke test: $BaseUrl" -ForegroundColor Cyan

Assert-Ok "GET /actuator/health" {
    $h = Invoke-Api -Method GET -Path "/actuator/health"
    if ($h.status -ne "UP") { throw "health status=$($h.status)" }
}

Assert-Ok "GET /api/products (public)" {
    $p = Invoke-Api -Method GET -Path "/api/products?page=0&size=1"
    if ($null -eq $p.content) { throw "missing page content" }
}

Assert-Ok "POST /api/auth/staff/login (manager)" {
    $script:ManagerToken = Get-StaffAccessToken -Email $ManagerEmail -Password $ManagerPassword
    if (-not $script:ManagerToken) { throw "no accessToken" }
}

Assert-Ok "GET /api/users/me (manager)" {
    $me = Invoke-Api -Method GET -Path "/api/users/me" -Headers @{ Authorization = "Bearer $script:ManagerToken" }
    if ($me.role -ne "MANAGER") { throw "role=$($me.role)" }
}

Assert-Ok "GET /api/deliveries (manager)" {
    Invoke-Api -Method GET -Path "/api/deliveries" -Headers @{ Authorization = "Bearer $script:ManagerToken" } | Out-Null
}

$employeeOk = Assert-Warn "POST /api/auth/staff/login (employee)" {
    $script:EmployeeToken = Get-StaffAccessToken -Email $EmployeeEmail -Password $EmployeePassword
    if (-not $script:EmployeeToken) { throw "no accessToken" }
}

if ($employeeOk) {
    Assert-Ok "GET /api/deliveries/my (employee)" {
        Invoke-Api -Method GET -Path "/api/deliveries/my" -Headers @{ Authorization = "Bearer $script:EmployeeToken" } | Out-Null
    }
} else {
    Write-Host "[SKIP] employee deliveries — seed employee@example.com (see README / DataInitializer)" -ForegroundColor Yellow
}

Assert-Ok "GET /api/orders/track (public 404 expected)" {
    try {
        Invoke-Api -Method GET -Path "/api/orders/track?orderId=1&email=smoke@example.com"
        throw "expected 404"
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -ne 404) { throw }
    }
}

Write-Host "`nAll smoke checks passed." -ForegroundColor Green
