Write-Host "🗄️ Testing HikariCP Connection Pool..." -ForegroundColor Cyan

Write-Host "📊 Initial pool status:" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/health/database" -Method GET
    $response | ConvertTo-Json
} catch {
    Write-Host "❌ Application not running or database health endpoint not accessible" -ForegroundColor Red
    Write-Host "Please start the application first: mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n🔄 Creating concurrent load (10 requests)..." -ForegroundColor Yellow

# Create 10 concurrent requests using background jobs
$jobs = @()
for ($i = 1; $i -le 10; $i++) {
    $job = Start-Job -ScriptBlock {
        try {
            Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users" -Method GET -Headers @{"Authorization" = "Bearer dummy"}
        } catch {
            # Expected to fail due to authentication, but it will test connection pool
        }
    }
    $jobs += $job
}

Start-Sleep -Seconds 2

Write-Host "`n📊 Pool status under load:" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/health/database" -Method GET
    $response | ConvertTo-Json
} catch {
    Write-Host "❌ Failed to get pool status under load" -ForegroundColor Red
}

# Wait for all jobs to complete
$jobs | Wait-Job | Remove-Job

Write-Host "`n📊 Final pool status:" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/health/database" -Method GET
    $response | ConvertTo-Json
} catch {
    Write-Host "❌ Failed to get final pool status" -ForegroundColor Red
}

Write-Host "`n✅ Connection pool test completed!" -ForegroundColor Green
