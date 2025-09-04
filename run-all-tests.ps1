Write-Host "🧪 COMPREHENSIVE PHASE 2 TESTING SUITE" -ForegroundColor Magenta
Write-Host "==========================================" -ForegroundColor Magenta

# Check if application is running
Write-Host "`n🔍 Checking if application is running..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/health/app" -Method GET -TimeoutSec 5
    Write-Host "✅ Application is running!" -ForegroundColor Green
} catch {
    Write-Host "❌ Application is not running!" -ForegroundColor Red
    Write-Host "Please start the application first:" -ForegroundColor Yellow
    Write-Host "  mvn spring-boot:run" -ForegroundColor White
    Write-Host "Then run this test suite again." -ForegroundColor Yellow
    exit 1
}

# Test 1: Connection Pool
Write-Host "`n" + "="*50 -ForegroundColor Magenta
Write-Host "TEST 1: HIKARICP CONNECTION POOL" -ForegroundColor Magenta
Write-Host "="*50 -ForegroundColor Magenta
& "./test-connection-pool.ps1"

# Test 2: Rate Limiting
Write-Host "`n" + "="*50 -ForegroundColor Magenta
Write-Host "TEST 2: RATE LIMITING" -ForegroundColor Magenta
Write-Host "="*50 -ForegroundColor Magenta
& "./test-rate-limiting.ps1"

# Test 3: Error Handling
Write-Host "`n" + "="*50 -ForegroundColor Magenta
Write-Host "TEST 3: ERROR HANDLING" -ForegroundColor Magenta
Write-Host "="*50 -ForegroundColor Magenta
& "./test-error-handling.ps1"

# Test 4: CORS Configuration
Write-Host "`n" + "="*50 -ForegroundColor Magenta
Write-Host "TEST 4: CORS CONFIGURATION" -ForegroundColor Magenta
Write-Host "="*50 -ForegroundColor Magenta
& "./test-cors.ps1"

Write-Host "`n" + "="*50 -ForegroundColor Magenta
Write-Host "🎉 ALL TESTS COMPLETED!" -ForegroundColor Green
Write-Host "="*50 -ForegroundColor Magenta

Write-Host "`n📊 TESTING SUMMARY:" -ForegroundColor Cyan
Write-Host "✅ HikariCP Connection Pool - Check database health endpoint" -ForegroundColor White
Write-Host "✅ Rate Limiting - Login (5/15min), Register (3/60min)" -ForegroundColor White
Write-Host "✅ Error Handling - Secure messages with tracking IDs" -ForegroundColor White
Write-Host "✅ CORS Configuration - Environment-based origins" -ForegroundColor White

Write-Host "`n🔧 TO CUSTOMIZE TESTING:" -ForegroundColor Yellow
Write-Host "- Modify .env.bromel for different configurations" -ForegroundColor White
Write-Host "- Adjust rate limits in application.properties" -ForegroundColor White
Write-Host "- Test with real frontend at allowed origins" -ForegroundColor White

Write-Host "`n📖 NEXT STEPS:" -ForegroundColor Cyan
Write-Host "1. Review test results above" -ForegroundColor White
Write-Host "2. Check application logs for any errors" -ForegroundColor White
Write-Host "3. Test with real frontend application" -ForegroundColor White
Write-Host "4. Monitor HikariCP metrics in production" -ForegroundColor White
