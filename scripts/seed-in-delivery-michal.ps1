# Creates shop order -> IN_DELIVERY -> assign courier Michal (prod Render).
#
# Usage:
#   .\scripts\seed-in-delivery-michal.ps1
# When 2FA is required, the script pauses and asks for the code from Render logs
# (line printed by the login that just happened in this same run).
#
# Non-interactive (code must match login in THIS process - rarely useful):
#   .\scripts\seed-in-delivery-michal.ps1 -Staff2faCode 1234
#
# Do NOT pass a code from an older log line / earlier run.

param(
    [string]$BaseUrl = "https://api-alcozon.onrender.com",
    [string]$ClientOrderNumber = "MN$(Get-Random -Minimum 100000 -Maximum 999999)",
    [string]$Staff2faCode = ""
)

$ErrorActionPreference = "Stop"
$deviceId = "seed-prod-script-001"

function Get-ApiErrorBody {
    param($ErrorRecord)
    if (-not $ErrorRecord.Exception.Response) { return $null }
    try {
        $stream = $ErrorRecord.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        return $reader.ReadToEnd()
    } catch {
        return $null
    }
}

function Invoke-Api {
    param(
        [string]$Step,
        [string]$Method,
        [string]$Path,
        [hashtable]$Headers = @{},
        $Body = $null
    )
    $params = @{
        Uri         = "$BaseUrl$Path"
        Method      = $Method
        Headers     = $Headers
        ContentType = "application/json"
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Compress -Depth 8)
    }
    try {
        return Invoke-RestMethod @params
    } catch {
        $status = $ErrorRecord.Exception.Response.StatusCode.value__
        $body = Get-ApiErrorBody -ErrorRecord $_
        Write-Host "[FAIL] $Step" -ForegroundColor Red
        Write-Host "  $Method $Path -> HTTP $status"
        if ($body) { Write-Host "  $body" }
        throw
    }
}

function Get-StaffToken {
    param([string]$Email, [string]$Password)

    Write-Host "Staff login: $Email (deviceId=$deviceId)" -ForegroundColor Cyan
    $login = Invoke-Api -Step "staff/login" -Method POST -Path "/api/auth/staff/login" -Body @{
        email    = $Email
        password = $Password
        deviceId = $deviceId
    }

    if (-not $login.verificationRequired) {
        Write-Host "  trusted device -> token OK" -ForegroundColor DarkGray
        return $login.tokens.accessToken
    }

    Write-Host "  2FA required. challengeId=$($login.challengeId)" -ForegroundColor Yellow
    Write-Host "  Render logs -> newest line: 2FA mail (log-only) to=$Email code=XXXX" -ForegroundColor Yellow

    $code = $Staff2faCode
    if ([string]::IsNullOrWhiteSpace($code)) {
        $code = Read-Host "Paste 4-digit code from that log line (this login only)"
    } else {
        Write-Host "  Using -Staff2faCode from command line (must be from log line above)" -ForegroundColor DarkGray
    }

    $code = $code.Trim()
    Write-Host "  verify-device ..." -ForegroundColor Cyan
    try {
        $verified = Invoke-Api -Step "staff/verify-device" -Method POST -Path "/api/auth/staff/verify-device" -Body @{
            challengeId = [string]$login.challengeId
            deviceId    = $deviceId
            code        = $code
        }
    } catch {
        if ([string]::IsNullOrWhiteSpace($Staff2faCode)) { throw }
        Write-Host "  Wrong -Staff2faCode for this login. Enter code from the log line for challengeId above." -ForegroundColor Yellow
        $code = (Read-Host "Paste 4-digit code again").Trim()
        $verified = Invoke-Api -Step "staff/verify-device (retry)" -Method POST -Path "/api/auth/staff/verify-device" -Body @{
            challengeId = [string]$login.challengeId
            deviceId    = $deviceId
            code        = $code
        }
    }

    Write-Host "  2FA OK -> token" -ForegroundColor DarkGray
    return $verified.accessToken
}

Write-Host "Warm-up: GET /actuator/health" -ForegroundColor Cyan
$health = Invoke-Api -Step "health" -Method GET -Path "/actuator/health"
if ($health.status -ne "UP") {
    throw "API health status=$($health.status)"
}

$managerToken = Get-StaffToken -Email "manager@example.com" -Password "Manager123!"
$mh = @{ Authorization = "Bearer $managerToken" }

Write-Host "Resolve Michal user id..." -ForegroundColor Cyan
$michal = (Invoke-Api -Step "admin/users" -Method GET -Path "/api/admin/users" -Headers $mh) |
    Where-Object { $_.email -eq "michal.nocun@studenci.collegiumwitelona.pl" } |
    Select-Object -First 1
if (-not $michal) {
    throw "User not found: michal.nocun@studenci.collegiumwitelona.pl"
}

$products = Invoke-Api -Step "products" -Method GET -Path "/api/products?page=0&size=1"
$productId = $products.content[0].id
if (-not $productId) {
    throw "No products in catalog"
}

Write-Host "Create order $ClientOrderNumber ..." -ForegroundColor Cyan
$customer = Invoke-Api -Step "customer/login" -Method POST -Path "/api/auth/login" -Body @{
    email    = "customer@example.com"
    password = "Customer123!"
}
$order = Invoke-Api -Step "orders" -Method POST -Path "/api/orders" -Headers @{
    Authorization = "Bearer $($customer.accessToken)"
} -Body @{
    clientOrderNumber = $ClientOrderNumber
    items             = @(@{ productId = $productId; quantity = 1 })
    delivery          = @{
        recipientName  = "Test Render IN_DELIVERY"
        streetAddress  = "ul. Swidnicka 12"
        city           = "Wroclaw"
        postalCode     = "50-068"
        country        = "Polska"
        deliveryNotes  = "Seed prod script"
        paymentMethod  = "Platnosc przy odbiorze"
    }
}

foreach ($status in @("IN_PRODUCTION", "IN_PACKING", "IN_DELIVERY")) {
    Invoke-Api -Step "order/status->$status" -Method PATCH -Path "/api/orders/$($order.id)/status" -Headers $mh -Body @{
        status = $status
    } | Out-Null
}

$delivery = (Invoke-Api -Step "deliveries" -Method GET -Path "/api/deliveries" -Headers $mh) |
    Where-Object { $_.clientOrderNumber -eq $ClientOrderNumber } |
    Select-Object -First 1
if (-not $delivery) {
    throw "Delivery not found for clientOrderNumber=$ClientOrderNumber"
}

Invoke-Api -Step "deliveries/assign" -Method PATCH -Path "/api/deliveries/$($delivery.id)/assign" -Headers $mh -Body @{
    courierId = $michal.id
} | Out-Null

Write-Host "OK: $ClientOrderNumber | orderId=$($order.id) | deliveryId=$($delivery.id) | courier=$($michal.email) id=$($michal.id)" -ForegroundColor Green
