Write-Host "⚡ Testing Rate Limiting Features..." -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/v1"

# Test data
$loginData = @{
    username = "test@example.com"
    password = "wrongpassword"
} | ConvertTo-Json

$registerData = @{
    email = "test@example.com"
    password = "password123"
    fullName = "Test User"
    phone = "1234567890"
    role = 1
} | ConvertTo-Json

Write-Host "`n🔐 Testing Login Rate Limiting (5 attempts allowed)" -ForegroundColor Yellow

for ($i = 1; $i -le 7; $i++) {
    Write-Host "Attempt $i..." -NoNewline
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginData -ContentType "application/json"
        Write-Host " ✅ Request allowed" -ForegroundColor Green
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq 429) {
            Write-Host " 🚫 Rate limited (429)" -ForegroundColor Red
            
            # Get rate limit headers if available
            $headers = $_.Exception.Response.Headers
            if ($headers) {
                Write-Host "   Headers:" -ForegroundColor Gray
                foreach ($header in $headers) {
                    if ($header.Key -like "*RateLimit*" -or $header.Key -like "*Retry*") {
                        Write-Host "   $($header.Key): $($header.Value)" -ForegroundColor Gray
                    }
                }
            }
        } elseif ($statusCode -eq 400) {
            Write-Host " ✅ Request allowed (400 - Invalid credentials)" -ForegroundColor Green
        } else {
            Write-Host " ❌ Unexpected status: $statusCode" -ForegroundColor Yellow
        }
    }
    
    Start-Sleep -Seconds 1
}

Write-Host "`n📝 Testing Register Rate Limiting (3 attempts in 60 minutes)" -ForegroundColor Yellow

for ($i = 1; $i -le 5; $i++) {
    Write-Host "Register attempt $i..." -NoNewline
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method POST -Body $registerData -ContentType "application/json"
        Write-Host " ✅ Request allowed" -ForegroundColor Green
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq 429) {
            Write-Host " 🚫 Rate limited (429)" -ForegroundColor Red
        } elseif ($statusCode -eq 400) {
            Write-Host " ✅ Request allowed (400 - Validation error)" -ForegroundColor Green
        } else {
            Write-Host " ❌ Unexpected status: $statusCode" -ForegroundColor Yellow
        }
    }
    
    Start-Sleep -Seconds 1
}

Write-Host "`n🧪 Testing Rate Limit with Different IPs (using X-Forwarded-For)" -ForegroundColor Yellow

for ($i = 1; $i -le 3; $i++) {
    $fakeIp = "192.168.1.$i"
    Write-Host "Testing from IP $fakeIp..." -NoNewline
    
    try {
        $headers = @{
            "Content-Type" = "application/json"
            "X-Forwarded-For" = $fakeIp
        }
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginData -Headers $headers
        Write-Host " ✅ Request allowed" -ForegroundColor Green
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host " Status: $statusCode" -ForegroundColor Yellow
    }
    
    Start-Sleep -Seconds 1
}

Write-Host "`n✅ Rate limiting test completed!" -ForegroundColor Green
Write-Host "Expected behavior:" -ForegroundColor Cyan
Write-Host "- Login: First 5 attempts should be allowed, then rate limited" -ForegroundColor White
Write-Host "- Register: First 3 attempts should be allowed, then rate limited" -ForegroundColor White
Write-Host "- Different IPs should have separate rate limits" -ForegroundColor White
