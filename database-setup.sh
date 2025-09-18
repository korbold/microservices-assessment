#!/bin/bash

# Database Setup Script for Banking Microservices
# This script handles MySQL database operations

echo "🗄️  Banking Database Management Script"
echo "======================================"

# Function to start MySQL
start_mysql() {
    echo "🚀 Starting MySQL container..."
    docker-compose up -d mysql
    
    echo "⏳ Waiting for MySQL to be ready..."
    sleep 10
    
    until docker exec banking-mysql mysqladmin ping -h"localhost" -u"root" -p"root" --silent; do
        echo "Waiting for MySQL to be ready..."
        sleep 5
    done
    
    echo "✅ MySQL is ready!"
}

# Function to initialize database
init_database() {
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
        echo "❌ Database initialization failed!"
        exit 1
    fi
}

# Function to reset database
reset_database() {
    echo "🔄 Resetting database..."
    docker-compose down mysql
    docker volume rm test_mysql_data 2>/dev/null || true
    start_mysql
    init_database
    echo "✅ Database reset completed!"
}

# Function to show database status
show_status() {
    echo "📊 Database Status:"
    echo "=================="
    
    if docker ps | grep -q banking-mysql; then
        echo "✅ MySQL container is running"
        
        # Show tables
        echo ""
        echo "📋 Database tables:"
        docker exec banking-mysql mysql -u"banking_user" -p"banking_password" banking_db -e "SHOW TABLES;"
        
        # Show record counts
        echo ""
        echo "📈 Record counts:"
        docker exec banking-mysql mysql -u"banking_user" -p"banking_password" banking_db -e "
        SELECT 'personas' as table_name, COUNT(*) as count FROM personas
        UNION ALL
        SELECT 'clientes', COUNT(*) FROM clientes
        UNION ALL
        SELECT 'cuentas', COUNT(*) FROM cuentas
        UNION ALL
        SELECT 'movimientos', COUNT(*) FROM movimientos;"
        
    else
        echo "❌ MySQL container is not running"
    fi
}

# Function to connect to database
connect_database() {
    echo "🔌 Connecting to MySQL database..."
    docker exec -it banking-mysql mysql -u"banking_user" -p"banking_password" banking_db
}

# Function to backup database
backup_database() {
    local backup_file="backup_$(date +%Y%m%d_%H%M%S).sql"
    echo "💾 Creating database backup: $backup_file"
    docker exec banking-mysql mysqldump -u"root" -p"root" banking_db > "$backup_file"
    
    if [ $? -eq 0 ]; then
        echo "✅ Backup created successfully: $backup_file"
    else
        echo "❌ Backup failed!"
    fi
}

# Function to restore database
restore_database() {
    local backup_file=$1
    if [ -z "$backup_file" ]; then
        echo "❌ Please provide backup file: $0 restore <backup_file.sql>"
        exit 1
    fi
    
    if [ ! -f "$backup_file" ]; then
        echo "❌ Backup file not found: $backup_file"
        exit 1
    fi
    
    echo "🔄 Restoring database from: $backup_file"
    docker exec -i banking-mysql mysql -u"root" -p"root" banking_db < "$backup_file"
    
    if [ $? -eq 0 ]; then
        echo "✅ Database restored successfully!"
    else
        echo "❌ Database restore failed!"
    fi
}

# Main script logic
case "$1" in
    "start")
        start_mysql
        ;;
    "init")
        start_mysql
        init_database
        ;;
    "reset")
        reset_database
        ;;
    "status")
        show_status
        ;;
    "connect")
        connect_database
        ;;
    "backup")
        backup_database
        ;;
    "restore")
        restore_database "$2"
        ;;
    *)
        echo "Usage: $0 {start|init|reset|status|connect|backup|restore}"
        echo ""
        echo "Commands:"
        echo "  start   - Start MySQL container"
        echo "  init    - Start MySQL and initialize database"
        echo "  reset   - Reset database (remove data and reinitialize)"
        echo "  status  - Show database status and statistics"
        echo "  connect - Connect to MySQL database"
        echo "  backup  - Create database backup"
        echo "  restore - Restore database from backup file"
        echo ""
        echo "Examples:"
        echo "  $0 init"
        echo "  $0 status"
        echo "  $0 backup"
        echo "  $0 restore backup_20241201_120000.sql"
        exit 1
        ;;
esac
