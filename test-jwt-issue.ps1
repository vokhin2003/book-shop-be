Write-Host "🔐 JWT TOKEN ISSUE DEBUGGING" -ForegroundColor Magenta
Write-Host "Testing API calls without Authorization header..." -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/v1"

Write-Host "`n✅ Test 1: Public endpoints WITHOUT auth header" -ForegroundColor Yellow

# Test categories (should work without auth)
Write-Host "`n  Testing GET /categories..." -NoNewline
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/categories" -Method GET
    Write-Host " ✅ SUCCESS" -ForegroundColor Green
    Write-Host "    Got $($response.data.result.Count) categories" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host " ❌ FAILED (Status: $statusCode)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $error = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "    Error: $($error.message)" -ForegroundColor Gray
    }
}

# Test books (should work without auth)
Write-Host "`n  Testing GET /books..." -NoNewline
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/books" -Method GET
    Write-Host " ✅ SUCCESS" -ForegroundColor Green
    Write-Host "    Got $($response.data.result.Count) books" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host " ❌ FAILED (Status: $statusCode)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $error = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "    Error: $($error.message)" -ForegroundColor Gray
    }
}

Write-Host "`n❌ Test 2: Protected endpoints WITHOUT auth header (should get 401)" -ForegroundColor Yellow

# Test users (should fail with 401)
Write-Host "`n  Testing GET /users..." -NoNewline
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/users" -Method GET
    Write-Host " ⚠️ UNEXPECTED SUCCESS" -ForegroundColor Yellow
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401) {
        Write-Host " ✅ CORRECTLY PROTECTED (401)" -ForegroundColor Green
    } else {
        Write-Host " ❌ UNEXPECTED STATUS: $statusCode" -ForegroundColor Red
    }
}

Write-Host "`n🔐 Test 3: Login endpoint (get fresh token)" -ForegroundColor Yellow

Write-Host "`n  Testing POST /auth/login..." -NoNewline
try {
    $loginData = @{
        username = "admin@gmail.com"
        password = "123456"
    } | ConvertTo-Json
    
    $headers = @{
        "Content-Type" = "application/json"
    }
    
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Headers $headers -Body $loginData
    Write-Host " ✅ LOGIN SUCCESS" -ForegroundColor Green
    
    if ($response.data.accessToken) {
        Write-Host "    Got access token: $($response.data.accessToken.Substring(0,20))..." -ForegroundColor Gray
        
        # Test with new token
        Write-Host "`n  Testing protected endpoint with new token..." -NoNewline
        $authHeaders = @{
            "Authorization" = "Bearer $($response.data.accessToken)"
        }
        
        try {
            $userResponse = Invoke-RestMethod -Uri "$baseUrl/auth/account" -Method GET -Headers $authHeaders
            Write-Host " ✅ SUCCESS WITH NEW TOKEN" -ForegroundColor Green
            Write-Host "    User: $($userResponse.data.user.fullName)" -ForegroundColor Gray
        } catch {
            Write-Host " ❌ FAILED EVEN WITH NEW TOKEN" -ForegroundColor Red
        }
    }
    
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host " ❌ LOGIN FAILED (Status: $statusCode)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $error = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "    Error: $($error.message)" -ForegroundColor Gray
        
        if ($statusCode -eq 429) {
            Write-Host "    💡 Rate limited. Wait 15 minutes or restart application" -ForegroundColor Yellow
        }
    }
}

Write-Host "`n🔧 DIAGNOSIS:" -ForegroundColor Cyan
Write-Host "If public endpoints work but protected ones fail with expired JWT:" -ForegroundColor White
Write-Host "1. 🧹 Clear frontend localStorage/sessionStorage" -ForegroundColor White
Write-Host "2. 🔄 Logout and login again to get fresh token" -ForegroundColor White
Write-Host "3. 🔍 Check frontend code - remove Authorization header from public API calls" -ForegroundColor White
Write-Host "4. ⏰ Token expiry is now 1 hour (was 100 days) - users need to login more often" -ForegroundColor White

Write-Host "`n💡 FRONTEND FIX:" -ForegroundColor Yellow
Write-Host "In your frontend, check if you're adding Authorization header to ALL requests." -ForegroundColor White
Write-Host "Public endpoints (categories, books) should NOT include Authorization header." -ForegroundColor White
