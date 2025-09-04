Write-Host "🌐 Testing CORS Configuration..." -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/v1"

Write-Host "`n✅ Test 1: Allowed Origin (localhost:3000)" -ForegroundColor Yellow
try {
    $headers = @{
        "Origin" = "http://localhost:3000"
        "Access-Control-Request-Method" = "POST"
        "Access-Control-Request-Headers" = "Content-Type,Authorization"
    }
    
    $response = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method OPTIONS -Headers $headers
    
    Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor White
    Write-Host "CORS Headers:" -ForegroundColor White
    
    $corsHeaders = $response.Headers | Where-Object { $_.Key -like "*Access-Control*" }
    foreach ($header in $corsHeaders) {
        Write-Host "  $($header.Key): $($header.Value)" -ForegroundColor Gray
    }
    
    if ($response.Headers["Access-Control-Allow-Origin"] -contains "http://localhost:3000") {
        Write-Host "✅ Allowed origin working!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Origin not properly allowed" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ CORS preflight failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n❌ Test 2: Blocked Origin (not in allowed list)" -ForegroundColor Yellow
try {
    $headers = @{
        "Origin" = "http://malicious-site.com"
        "Access-Control-Request-Method" = "POST"
        "Access-Control-Request-Headers" = "Content-Type,Authorization"
    }
    
    $response = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method OPTIONS -Headers $headers
    
    Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor White
    
    if ($response.Headers["Access-Control-Allow-Origin"] -notcontains "http://malicious-site.com") {
        Write-Host "✅ Blocked origin working!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Origin should be blocked but isn't" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✅ Origin properly blocked" -ForegroundColor Green
}

Write-Host "`n🔍 Test 3: Allowed Methods" -ForegroundColor Yellow
try {
    $headers = @{
        "Origin" = "http://localhost:3000"
        "Access-Control-Request-Method" = "PUT"
    }
    
    $response = Invoke-WebRequest -Uri "$baseUrl/users/1" -Method OPTIONS -Headers $headers
    
    $allowedMethods = $response.Headers["Access-Control-Allow-Methods"]
    Write-Host "Allowed Methods: $allowedMethods" -ForegroundColor White
    
    if ($allowedMethods -like "*PUT*") {
        Write-Host "✅ PUT method allowed!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ PUT method not in allowed methods" -ForegroundColor Yellow
    }
    
    if ($allowedMethods -notlike "*PATCH*") {
        Write-Host "✅ PATCH method properly not allowed!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ PATCH method should not be allowed" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Method check failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n📋 Test 4: Allowed Headers" -ForegroundColor Yellow
try {
    $headers = @{
        "Origin" = "http://localhost:3000"
        "Access-Control-Request-Method" = "POST"
        "Access-Control-Request-Headers" = "Authorization,Content-Type,X-Client-Platform"
    }
    
    $response = Invoke-WebRequest -Uri "$baseUrl/auth/register" -Method OPTIONS -Headers $headers
    
    $allowedHeaders = $response.Headers["Access-Control-Allow-Headers"]
    Write-Host "Allowed Headers: $allowedHeaders" -ForegroundColor White
    
    $requiredHeaders = @("Authorization", "Content-Type", "X-Client-Platform")
    $allHeadersAllowed = $true
    
    foreach ($header in $requiredHeaders) {
        if ($allowedHeaders -like "*$header*") {
            Write-Host "✅ $header allowed" -ForegroundColor Green
        } else {
            Write-Host "❌ $header not allowed" -ForegroundColor Red
            $allHeadersAllowed = $false
        }
    }
    
    if ($allHeadersAllowed) {
        Write-Host "✅ All required headers allowed!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Some headers missing" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Headers check failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🍪 Test 5: Credentials Support" -ForegroundColor Yellow
try {
    $headers = @{
        "Origin" = "http://localhost:3000"
        "Access-Control-Request-Method" = "POST"
    }
    
    $response = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method OPTIONS -Headers $headers
    
    $allowCredentials = $response.Headers["Access-Control-Allow-Credentials"]
    Write-Host "Allow Credentials: $allowCredentials" -ForegroundColor White
    
    if ($allowCredentials -eq "true") {
        Write-Host "✅ Credentials support enabled!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Credentials support disabled" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Credentials check failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n✅ CORS configuration test completed!" -ForegroundColor Green
