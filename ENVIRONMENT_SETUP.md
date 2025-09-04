# 🔧 Environment Variables Setup Guide

## 📋 Required Environment Variables

Create a file named `.env.bromel` in the root directory and add the following variables:

### 🗄️ Database Configuration
```bash
DB_URL=jdbc:postgresql://localhost:5432/bookshop
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password
```

### 📧 SendGrid Email Configuration
```bash
SENDGRID_API_KEY=your_sendgrid_api_key_here
SENDGRID_MAIL_FROM=noreply@yourdomain.com
SENDGRID_TEMPLATE_VERIFY=d-your_verification_template_id
SENDGRID_TEMPLATE_FORGOT=d-your_forgot_password_template_id
SENDGRID_TEMPLATE_ORDDER=d-your_order_template_id
```

### ☁️ Cloudinary Configuration
```bash
CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret
```

### 🔐 JWT Security Configuration
```bash
# Generate using: openssl rand -base64 64
JWT_BASE64_SECRET=your_jwt_base64_secret_key_here
```

### 💳 VNPay Payment Configuration
```bash
VNP_TMP_CODE=your_vnpay_terminal_code
VNP_HASH_SECRET=your_vnpay_hash_secret
```

### 🔑 Google OAuth Configuration
```bash
GOOGLE_CLIENT_ID=your_google_oauth_client_id
GOOGLE_CLIENT_SECRET=your_google_oauth_client_secret
GOOGLE_REDIRECT_URI=http://localhost:8080/auth/google/callback
```

### 📬 Spring Mail Configuration
```bash
# IMPORTANT: These were previously hard-coded - now secure!
MAIL_USERNAME=your_gmail_username@gmail.com
MAIL_PASSWORD=your_gmail_app_password
```

### 🖥️ Server Configuration
```bash
SERVER_IP=localhost
```

### 🌐 CORS Configuration
```bash
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4173,http://localhost:5173
```

### 🛡️ Security Configuration
```bash
# Show detailed error messages (development only)
SHOW_ERROR_DETAILS=false

# Rate limiting settings
RATE_LIMIT_MAX_ATTEMPTS=5
RATE_LIMIT_WINDOW_MINUTES=15
```

### 🗄️ Database Pool Configuration
```bash
DB_POOL_MAX_SIZE=20
DB_POOL_MIN_IDLE=5
DB_POOL_CONNECTION_TIMEOUT=30000
DB_POOL_IDLE_TIMEOUT=600000
DB_POOL_MAX_LIFETIME=1800000
```

## 🚀 Setup Instructions

1. **Copy the template**: Create `.env.bromel` file in the root directory
2. **Fill in values**: Replace all placeholder values with your actual credentials
3. **Verify .gitignore**: Ensure `.env.bromel` is listed in `.gitignore` (✅ already done)

## 🔑 Important Security Notes

### Gmail App Password Setup
- Use **App Password**, not your regular Gmail password
- Enable **2FA** on your Gmail account first
- Generate App Password: Gmail Settings → Security → App passwords

### JWT Secret Generation
```bash
# Generate secure JWT secret
openssl rand -base64 64
```

### Production Deployment
- Use **strong, unique passwords**
- Enable **database SSL**
- Set up **proper firewall rules**
- Use **environment-specific configurations**

## ✅ Security Improvements Made

| Issue | Status | Fix |
|-------|--------|-----|
| Hard-coded email credentials | ✅ Fixed | Moved to environment variables |
| JWT token expiry too long | ✅ Fixed | Access: 1 hour, Refresh: 7 days |
| Server IP hard-coded | ✅ Fixed | Now uses environment variable |

## 🔍 Verification

After setting up `.env.bromel`, verify your application starts without errors:

```bash
mvn spring-boot:run
```

If you see any missing environment variable errors, double-check your `.env.bromel` file.
