Write-Host "🏥 Testing Health Endpoints Access..." -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/v1"

Write-Host "`n📱 Test 1: Application Health Endpoint" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/health/app" -Method GET
    Write-Host "✅ App health endpoint accessible!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor White
    $response | ConvertTo-Json
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ App health endpoint failed with status: $statusCode" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
        $errorResponse | ConvertTo-Json
    }
}

Write-Host "`n🗄️ Test 2: Database Health Endpoint" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/health/database" -Method GET
    Write-Host "✅ Database health endpoint accessible!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor White
    $response | ConvertTo-Json
    
    # Validate HikariCP data
    if ($response.poolName -eq "BookshopHikariPool") {
        Write-Host "✅ HikariCP pool name correct!" -ForegroundColor Green
    }
    
    if ($response.maxPoolSize -eq 20) {
        Write-Host "✅ Max pool size configured correctly!" -ForegroundColor Green
    }
    
    if ($response.minIdle -eq 5) {
        Write-Host "✅ Min idle connections configured correctly!" -ForegroundColor Green
    }
    
    if ($response.connectionTest -eq "SUCCESS") {
        Write-Host "✅ Database connection test successful!" -ForegroundColor Green
    }
    
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ Database health endpoint failed with status: $statusCode" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "Error details:" -ForegroundColor White
        $errorResponse | ConvertTo-Json
    }
}

Write-Host "`n🔐 Test 3: Verify Security Still Works (should require auth)" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/users" -Method GET
    Write-Host "⚠️ Protected endpoint accessible without auth - This is unexpected!" -ForegroundColor Yellow
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401) {
        Write-Host "✅ Protected endpoints still require authentication!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Unexpected status code: $statusCode" -ForegroundColor Yellow
    }
}

Write-Host "`n✅ Health endpoints test completed!" -ForegroundColor Green
