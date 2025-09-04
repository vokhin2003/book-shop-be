Write-Host "🌐 FRONTEND INTEGRATION TESTING" -ForegroundColor Magenta
Write-Host "Testing CORS and API calls from frontend perspective..." -ForegroundColor Cyan

$frontendUrl = "http://localhost:3000"
$backendUrl = "http://localhost:8080/api/v1"

Write-Host "`n🔍 Step 1: Verify frontend origin is allowed" -ForegroundColor Yellow
try {
    $headers = @{
        "Origin" = $frontendUrl
        "Access-Control-Request-Method" = "GET"
        "Access-Control-Request-Headers" = "Content-Type,Authorization"
    }
    
    $response = Invoke-WebRequest -Uri "$backendUrl/categories" -Method OPTIONS -Headers $headers -UseBasicParsing
    
    Write-Host "✅ CORS Preflight Response:" -ForegroundColor Green
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor White
    
    $corsHeaders = @{}
    foreach ($header in $response.Headers.GetEnumerator()) {
        if ($header.Key -like "*Access-Control*") {
            $corsHeaders[$header.Key] = $header.Value -join ","
            Write-Host "  $($header.Key): $($header.Value -join ',')" -ForegroundColor Gray
        }
    }
    
    # Validate important CORS headers
    if ($corsHeaders["Access-Control-Allow-Origin"] -eq $frontendUrl) {
        Write-Host "✅ Origin $frontendUrl is allowed" -ForegroundColor Green
    } else {
        Write-Host "❌ Origin $frontendUrl is NOT allowed. Allowed: $($corsHeaders['Access-Control-Allow-Origin'])" -ForegroundColor Red
    }
    
    if ($corsHeaders["Access-Control-Allow-Methods"] -like "*GET*") {
        Write-Host "✅ GET method is allowed" -ForegroundColor Green
    } else {
        Write-Host "❌ GET method not allowed" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ CORS Preflight failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "This could be why frontend calls are failing!" -ForegroundColor Yellow
}

Write-Host "`n📋 Step 2: Test actual API calls with CORS headers" -ForegroundColor Yellow

# Test public endpoint (categories)
Write-Host "`n  Testing GET /categories (public endpoint)..." -NoNewline
try {
    $headers = @{
        "Origin" = $frontendUrl
    }
    $response = Invoke-RestMethod -Uri "$backendUrl/categories" -Method GET -Headers $headers
    Write-Host " ✅ SUCCESS" -ForegroundColor Green
    Write-Host "    Response: Got $($response.data.result.Count) categories" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host " ❌ FAILED (Status: $statusCode)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $error = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "    Error: $($error.message)" -ForegroundColor Gray
    }
}

# Test public endpoint (books)
Write-Host "`n  Testing GET /books (public endpoint)..." -NoNewline
try {
    $headers = @{
        "Origin" = $frontendUrl
    }
    $response = Invoke-RestMethod -Uri "$backendUrl/books" -Method GET -Headers $headers
    Write-Host " ✅ SUCCESS" -ForegroundColor Green
    Write-Host "    Response: Got $($response.data.result.Count) books" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host " ❌ FAILED (Status: $statusCode)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $error = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "    Error: $($error.message)" -ForegroundColor Gray
    }
}

# Test auth endpoint
Write-Host "`n  Testing POST /auth/login (auth endpoint)..." -NoNewline
try {
    $headers = @{
        "Origin" = $frontendUrl
        "Content-Type" = "application/json"
    }
    $loginData = @{
        username = "test@example.com"
        password = "wrongpassword"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$backendUrl/auth/login" -Method POST -Headers $headers -Body $loginData
    Write-Host " ✅ REQUEST ALLOWED" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 400) {
        Write-Host " ✅ REQUEST ALLOWED (400 - Invalid credentials expected)" -ForegroundColor Green
    } elseif ($statusCode -eq 429) {
        Write-Host " ✅ REQUEST ALLOWED (429 - Rate limited)" -ForegroundColor Green
    } else {
        Write-Host " ❌ FAILED (Status: $statusCode)" -ForegroundColor Red
        if ($_.ErrorDetails.Message) {
            $error = $_.ErrorDetails.Message | ConvertFrom-Json
            Write-Host "    Error: $($error.message)" -ForegroundColor Gray
        }
    }
}

Write-Host "`n🔍 Step 3: Test protected endpoint (should fail with 401)" -ForegroundColor Yellow
Write-Host "`n  Testing GET /users (protected endpoint)..." -NoNewline
try {
    $headers = @{
        "Origin" = $frontendUrl
    }
    $response = Invoke-RestMethod -Uri "$backendUrl/users" -Method GET -Headers $headers
    Write-Host " ⚠️ UNEXPECTED SUCCESS (should require auth)" -ForegroundColor Yellow
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401) {
        Write-Host " ✅ CORRECTLY PROTECTED (401 - Unauthorized)" -ForegroundColor Green
    } else {
        Write-Host " ❌ UNEXPECTED STATUS: $statusCode" -ForegroundColor Red
    }
}

Write-Host "`n🔧 Step 4: Debug information" -ForegroundColor Yellow
Write-Host "Backend URL: $backendUrl" -ForegroundColor White
Write-Host "Frontend URL: $frontendUrl" -ForegroundColor White

# Check if backend is actually running
Write-Host "`n  Checking if backend is running..." -NoNewline
try {
    $health = Invoke-RestMethod -Uri "$backendUrl/health/app" -Method GET -TimeoutSec 5
    Write-Host " ✅ Backend is running" -ForegroundColor Green
} catch {
    Write-Host " ❌ Backend is NOT running!" -ForegroundColor Red
    Write-Host "    Please start backend: mvn spring-boot:run" -ForegroundColor Yellow
}

Write-Host "`n📋 SUMMARY & TROUBLESHOOTING:" -ForegroundColor Cyan
Write-Host "If frontend calls are still failing, check:" -ForegroundColor White
Write-Host "1. 🌐 Browser Network tab for CORS errors" -ForegroundColor White
Write-Host "2. 🔍 Backend console logs for errors" -ForegroundColor White
Write-Host "3. 📱 Frontend console for JavaScript errors" -ForegroundColor White
Write-Host "4. 🔧 Ensure frontend is making requests to http://localhost:8080/api/v1/" -ForegroundColor White
Write-Host "5. ⚡ Check if rate limiting is blocking requests" -ForegroundColor White

Write-Host "`n🛠️ Common Frontend Issues:" -ForegroundColor Yellow
Write-Host "- Wrong API base URL in frontend config" -ForegroundColor White
Write-Host "- Missing Content-Type header for POST requests" -ForegroundColor White
Write-Host "- Credentials not being sent with requests" -ForegroundColor White
Write-Host "- Browser blocking mixed content (HTTP/HTTPS)" -ForegroundColor White
