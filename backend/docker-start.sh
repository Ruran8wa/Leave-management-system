#!/bin/bash

echo "🐘 Starting PostgreSQL container..."
docker compose up -d postgres

echo ""
echo "⏳ Waiting for PostgreSQL to be ready..."
sleep 3

# Check if container is healthy
if docker compose ps postgres | grep -q "healthy"; then
    echo "✅ PostgreSQL is running!"
    echo ""
    echo "📊 Connection Details:"
    echo "  Host: localhost"
    echo "  Port: 5432"
    echo "  Database: lmsdb"
    echo "  Username: lmsuser"
    echo "  Password: lmspassword"
    echo ""
    echo "🔗 Connection String:"
    echo "  jdbc:postgresql://:5432/lmsdb"
    echo ""
    echo "💡 To connect with psql:"
    echo "  docker exec -it lms-postgres psql -U lmsuser -d lmsdb"
else
    echo "⚠️  PostgreSQL container started but may not be healthy yet."
    echo "   Run 'docker compose ps' to check status."
fi
