# EduLingua Database Setup Script for Windows
# This script automates the database creation and migration process

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "EduLingua Database Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$DB_NAME = "edulingua"
$DB_USER = "postgres"
$DB_HOST = "localhost"
$DB_PORT = "5432"

# Prompt for password
Write-Host "Enter PostgreSQL password for user '$DB_USER': " -ForegroundColor Yellow -NoNewline
$DB_PASSWORD = Read-Host -AsSecureString
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_PASSWORD)
$DB_PASSWORD_PLAIN = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

Write-Host ""
Write-Host "Configuration:" -ForegroundColor Green
Write-Host "  Database: $DB_NAME"
Write-Host "  User: $DB_USER"
Write-Host "  Host: $DB_HOST"
Write-Host "  Port: $DB_PORT"
Write-Host ""

# Set PostgreSQL password environment variable
$env:PGPASSWORD = $DB_PASSWORD_PLAIN

# Check if psql is available
Write-Host "Checking PostgreSQL installation..." -ForegroundColor Yellow
try {
    $psqlVersion = & psql --version 2>&1
    Write-Host "✓ PostgreSQL found: $psqlVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Error: psql not found. Please install PostgreSQL and add it to PATH." -ForegroundColor Red
    exit 1
}

Write-Host ""

# Check if database exists
Write-Host "Checking if database '$DB_NAME' exists..." -ForegroundColor Yellow
$dbExists = & psql -U $DB_USER -h $DB_HOST -p $DB_PORT -lqt 2>&1 | Select-String -Pattern $DB_NAME -Quiet

if ($dbExists) {
    Write-Host "✓ Database '$DB_NAME' already exists." -ForegroundColor Green
    $response = Read-Host "Do you want to recreate it? This will DELETE ALL DATA! (yes/no)"

    if ($response -eq "yes") {
        Write-Host "Dropping database '$DB_NAME'..." -ForegroundColor Yellow
        & psql -U $DB_USER -h $DB_HOST -p $DB_PORT -c "DROP DATABASE IF EXISTS $DB_NAME;" 2>&1 | Out-Null
        Write-Host "✓ Database dropped." -ForegroundColor Green

        Write-Host "Creating database '$DB_NAME'..." -ForegroundColor Yellow
        & psql -U $DB_USER -h $DB_HOST -p $DB_PORT -c "CREATE DATABASE $DB_NAME;" 2>&1 | Out-Null
        Write-Host "✓ Database created." -ForegroundColor Green
    } else {
        Write-Host "Keeping existing database. Migrations will be applied to existing database." -ForegroundColor Yellow
    }
} else {
    Write-Host "Creating database '$DB_NAME'..." -ForegroundColor Yellow
    & psql -U $DB_USER -h $DB_HOST -p $DB_PORT -c "CREATE DATABASE $DB_NAME;" 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Database created successfully." -ForegroundColor Green
    } else {
        Write-Host "✗ Error creating database." -ForegroundColor Red
        exit 1
    }
}

Write-Host ""

# Get script directory
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path

# Run migrations
Write-Host "Running migrations..." -ForegroundColor Yellow
Write-Host ""

Write-Host "[1/2] Creating core tables..." -ForegroundColor Cyan
$migration1 = Join-Path $SCRIPT_DIR "V1__create_core_tables.sql"
& psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -f $migration1 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Core tables created successfully." -ForegroundColor Green
} else {
    Write-Host "✗ Error creating core tables." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[2/2] Seeding initial data..." -ForegroundColor Cyan
$migration2 = Join-Path $SCRIPT_DIR "V2__seed_initial_data.sql"
& psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -f $migration2 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Initial data seeded successfully." -ForegroundColor Green
} else {
    Write-Host "✗ Error seeding initial data." -ForegroundColor Red
    exit 1
}

Write-Host ""

# Run verification
Write-Host "Running verification..." -ForegroundColor Yellow
$verifyScript = Join-Path $SCRIPT_DIR "VERIFY.sql"
& psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -f $verifyScript 2>&1

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Database Details:" -ForegroundColor Yellow
Write-Host "  Connection URL: jdbc:postgresql://$DB_HOST`:$DB_PORT/$DB_NAME"
Write-Host "  Username: $DB_USER"
Write-Host ""
Write-Host "Default Admin Credentials:" -ForegroundColor Yellow
Write-Host "  Email: admin@edulingua.com"
Write-Host "  Username: admin"
Write-Host "  Password: Admin@123"
Write-Host ""
Write-Host "⚠️  IMPORTANT: Change the default password immediately after first login!" -ForegroundColor Red
Write-Host ""
Write-Host "Update your application.yml with the connection details above." -ForegroundColor Cyan
Write-Host ""

# Clear password from environment
$env:PGPASSWORD = ""
