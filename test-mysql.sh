#!/bin/bash

# MySQL Test Script for Banking Microservices
# This script tests the MySQL database connection and initialization

echo "🧪 Testing MySQL Database Configuration"
echo "======================================"

# Check if MySQL container is running
if ! docker ps | grep -q banking-mysql; then
    echo "❌ MySQL container is not running. Starting it first..."
    docker-compose up -d mysql
    sleep 10
fi

# Wait for MySQL to be ready
echo "⏳ Waiting for MySQL to be ready..."
until docker exec banking-mysql mysqladmin ping -h"localhost" -u"root" -p"root" --silent; do
    echo "Waiting for MySQL to be ready..."
    sleep 5
done

echo "✅ MySQL is ready!"

# Test database connection
echo "🔌 Testing database connection..."
docker exec banking-mysql mysql -u"banking_user" -p"banking_password" banking_db -e "SELECT 1 as test_connection;"

if [ $? -eq 0 ]; then
    echo "✅ Database connection successful!"
else
    echo "❌ Database connection failed!"
    exit 1
fi

# Test if tables exist
echo "📋 Checking database tables..."
docker exec banking-mysql mysql -u"banking_user" -p"banking_password" banking_db -e "SHOW TABLES;"

if [ $? -eq 0 ]; then
    echo "✅ Database tables are accessible!"
else
    echo "❌ Database tables check failed!"
    exit 1
fi

# Test data integrity
echo "📊 Checking data integrity..."
docker exec banking-mysql mysql -u"banking_user" -p"banking_password" banking_db -e "
SELECT 
    'personas' as table_name, COUNT(*) as count FROM personas
UNION ALL
SELECT 'clientes', COUNT(*) FROM clientes
UNION ALL
SELECT 'cuentas', COUNT(*) FROM cuentas
UNION ALL
SELECT 'movimientos', COUNT(*) FROM movimientos;"

if [ $? -eq 0 ]; then
    echo "✅ Data integrity check passed!"
else
    echo "❌ Data integrity check failed!"
    exit 1
fi

# Test stored procedures
echo "🔧 Testing stored procedures..."
docker exec banking-mysql mysql -u"banking_user" -p"banking_password" banking_db -e "
CALL GetAccountBalance(1, @balance);
SELECT @balance as account_balance;"

if [ $? -eq 0 ]; then
    echo "✅ Stored procedures are working!"
else
    echo "❌ Stored procedures test failed!"
    exit 1
fi

echo ""
echo "🎉 All MySQL tests passed successfully!"
echo ""
echo "📋 Database Summary:"
echo "  • Host: localhost:3306"
echo "  • Database: banking_db"
echo "  • Username: banking_user"
echo "  • Password: banking_password"
echo ""
echo "🔍 You can connect to the database using:"
echo "  docker exec -it banking-mysql mysql -u banking_user -p banking_db"
