Write-Host "🚀 Quick CORS Test for Frontend" -ForegroundColor Cyan

# Test 1: OPTIONS preflight
Write-Host "`n1. Testing CORS preflight..." -ForegroundColor Yellow
curl.exe -X OPTIONS http://localhost:8080/api/v1/categories `
  -H "Origin: http://localhost:3000" `
  -H "Access-Control-Request-Method: GET" `
  -H "Access-Control-Request-Headers: Content-Type" `
  -v

Write-Host "`n2. Testing actual GET request..." -ForegroundColor Yellow
curl.exe -X GET http://localhost:8080/api/v1/categories `
  -H "Origin: http://localhost:3000" `
  -v

Write-Host "`n3. Testing health endpoint..." -ForegroundColor Yellow
curl.exe http://localhost:8080/api/v1/health/app
