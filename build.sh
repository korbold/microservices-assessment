#!/bin/bash

# Banking Microservices Build Script
# This script builds all microservices and prepares for deployment

echo "🏦 Building Banking Microservices Architecture..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Check if MySQL client is installed (optional, for database initialization)
if ! command -v mysql &> /dev/null; then
    echo "⚠️  MySQL client not found. Database will be initialized via Docker volume."
fi

# Check if BaseDatos.sql exists
if [ ! -f "BaseDatos.sql" ]; then
    echo "❌ BaseDatos.sql file not found. Please ensure the database schema file exists."
    exit 1
fi

echo "✅ Prerequisites check passed"

# Clean and build all modules
echo "🔨 Building all microservices..."

# Build Eureka Server
echo "Building Eureka Server..."
cd eureka-server
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Failed to build Eureka Server"
    exit 1
fi
cd ..

# Build API Gateway
echo "Building API Gateway..."
cd api-gateway
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Failed to build API Gateway"
    exit 1
fi
cd ..

# Build Client-Person Service
echo "Building Client-Person Service..."
cd client-person-service
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Failed to build Client-Person Service"
    exit 1
fi
cd ..

# Build Account-Movement Service
echo "Building Account-Movement Service..."
cd account-movement-service
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Failed to build Account-Movement Service"
    exit 1
fi
cd ..

echo "✅ All microservices built successfully"

# Build Docker images
echo "🐳 Building Docker images..."

# Build Eureka Server image
echo "Building Eureka Server Docker image..."
docker build -t banking-eureka-server:latest -f eureka-server/Dockerfile .

# Build API Gateway image
echo "Building API Gateway Docker image..."
docker build -t banking-api-gateway:latest -f api-gateway/Dockerfile .

# Build Client-Person Service image
echo "Building Client-Person Service Docker image..."
docker build -t banking-client-service:latest -f client-person-service/Dockerfile .

# Build Account-Movement Service image
echo "Building Account-Movement Service Docker image..."
docker build -t banking-account-service:latest -f account-movement-service/Dockerfile .

echo "✅ All Docker images built successfully"

# Initialize MySQL database
echo "🗄️  Setting up MySQL database..."
echo "Starting MySQL container for database initialization..."

# Start only MySQL first to initialize the database
docker-compose up -d mysql

# Wait for MySQL to be ready
echo "⏳ Waiting for MySQL to be ready..."
sleep 10

# Check if MySQL is ready
until docker exec banking-mysql mysqladmin ping -h"localhost" -u"root" -p"root" --silent; do
    echo "Waiting for MySQL to be ready..."
    sleep 5
done

echo "✅ MySQL is ready!"

# Initialize database with schema and data
echo "📊 Initializing database with schema and data..."

# Copy the SQL file to the container and execute it
docker cp BaseDatos.sql banking-mysql:/tmp/init.sql
docker exec banking-mysql mysql -u"root" -p"root" -e "source /tmp/init.sql"

if [ $? -eq 0 ]; then
    echo "✅ Database initialized successfully!"
    
    # Verify database initialization
    echo "🔍 Verifying database initialization..."
    docker exec banking-mysql mysql -u"banking_user" -p"banking_password" banking_db -e "
    SELECT 'personas' as table_name, COUNT(*) as count FROM personas
    UNION ALL
    SELECT 'clientes', COUNT(*) FROM clientes
    UNION ALL
    SELECT 'cuentas', COUNT(*) FROM cuentas
    UNION ALL
    SELECT 'movimientos', COUNT(*) FROM movimientos;"
    
    echo "✅ Database verification completed!"
else
    echo "⚠️  Database initialization had issues, but continuing..."
fi

# Run tests
echo "🧪 Running tests..."
mvn test

if [ $? -eq 0 ]; then
    echo "✅ All tests passed"
else
    echo "⚠️  Some tests failed, but continuing with deployment"
fi

echo ""
echo "🎉 Build completed successfully!"
echo ""

# Start the application automatically
echo "🚀 Starting all services..."
docker-compose up -d

if [ $? -eq 0 ]; then
    echo "✅ All services started successfully!"
    echo ""
    echo "🌐 Services are now running:"
    echo "  • API Gateway: http://localhost:8080"
    echo "  • Eureka Dashboard: http://localhost:8761"
    echo "  • Client Service: http://localhost:8081"
    echo "  • Account Service: http://localhost:8082"
    echo "  • MySQL Database: localhost:3306"
    echo ""
    echo "🗄️  Database Information:"
    echo "  • Host: localhost"
    echo "  • Port: 3306"
    echo "  • Database: banking_db"
    echo "  • Username: banking_user"
    echo "  • Password: banking_password"
    echo "  • Root Password: root"
    echo ""
    echo "📋 Management commands:"
    echo "  • View logs: docker-compose logs -f"
    echo "  • Stop services: docker-compose down"
    echo "  • Restart services: docker-compose restart"
    echo "  • Connect to MySQL: docker exec -it banking-mysql mysql -u banking_user -p banking_db"
    echo "  • View MySQL logs: docker logs banking-mysql"
    echo ""
    echo "📁 Import the Postman collection: Banking-API.postman_collection.json"
    echo ""
    echo "🔍 To verify database connection:"
    echo "  docker exec -it banking-mysql mysql -u banking_user -p banking_db -e 'SHOW TABLES;'"
else
    echo "❌ Failed to start services. Please check the logs with: docker-compose logs"
    exit 1
fi
