#!/bin/bash

echo "🛑 Stopping Docker containers..."
docker compose down

echo "✅ All containers stopped!"
echo ""
echo "💡 To remove volumes (delete all data), run:"
echo "   docker compose down -v"
