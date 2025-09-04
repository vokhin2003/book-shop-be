Write-Host "🛡️ Testing Enhanced Error Handling..." -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/v1"

Write-Host "`n🔍 Test 1: Generic 500 Error (should show error ID)" -ForegroundColor Yellow
try {
    # This should trigger a 500 error
    $response = Invoke-RestMethod -Uri "$baseUrl/nonexistent-endpoint-that-causes-error" -Method POST
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    
    Write-Host "Status Code: $statusCode" -ForegroundColor White
    Write-Host "Error Response:" -ForegroundColor White
    $errorResponse | ConvertTo-Json
    
    if ($errorResponse.message -like "*error ID*") {
        Write-Host "✅ Error ID tracking working!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Error ID not found in response" -ForegroundColor Yellow
    }
}

Write-Host "`n🔐 Test 2: Authentication Error (should be generic)" -ForegroundColor Yellow
try {
    $loginData = @{
        username = "nonexistent@user.com"
        password = "wrongpassword"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginData -ContentType "application/json"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    
    Write-Host "Status Code: $statusCode" -ForegroundColor White
    Write-Host "Error Response:" -ForegroundColor White
    $errorResponse | ConvertTo-Json
    
    if ($errorResponse.message -eq "Invalid credentials provided") {
        Write-Host "✅ Generic auth error message working!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Unexpected auth error message" -ForegroundColor Yellow
    }
}

Write-Host "`n📝 Test 3: Validation Error (should show field details)" -ForegroundColor Yellow
try {
    $invalidData = @{
        email = "invalid-email"
        password = ""
        fullName = ""
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method POST -Body $invalidData -ContentType "application/json"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    
    Write-Host "Status Code: $statusCode" -ForegroundColor White
    Write-Host "Error Response:" -ForegroundColor White
    $errorResponse | ConvertTo-Json
    
    if ($errorResponse.error -eq "Validation Error") {
        Write-Host "✅ Validation error handling working!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Unexpected validation error format" -ForegroundColor Yellow
    }
}

Write-Host "`n🚫 Test 4: 404 Not Found" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/completely/invalid/endpoint" -Method GET
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    
    Write-Host "Status Code: $statusCode" -ForegroundColor White
    Write-Host "Error Response:" -ForegroundColor White
    $errorResponse | ConvertTo-Json
    
    if ($statusCode -eq 404) {
        Write-Host "✅ 404 error handling working!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Expected 404 but got $statusCode" -ForegroundColor Yellow
    }
}

Write-Host "`n🔒 Test 5: Unauthorized Access" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/users" -Method GET
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    
    Write-Host "Status Code: $statusCode" -ForegroundColor White
    Write-Host "Error Response:" -ForegroundColor White
    $errorResponse | ConvertTo-Json
    
    if ($statusCode -eq 401) {
        Write-Host "✅ Unauthorized error handling working!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Expected 401 but got $statusCode" -ForegroundColor Yellow
    }
}

Write-Host "`n✅ Error handling test completed!" -ForegroundColor Green
