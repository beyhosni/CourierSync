#!/bin/bash
# Script d'arrêt des services backend CourierSync

echo "========================================"
echo "Arrêt des services backend CourierSync"
echo "========================================"
echo ""

# Arrêt des services par port
echo "Arrêt du User Auth Service (port 8081)..."
lsof -ti:8081 | xargs kill -9 2>/dev/null || echo "Aucun processus trouvé sur le port 8081"

echo "Arrêt du Dispatch Service (port 8082)..."
lsof -ti:8082 | xargs kill -9 2>/dev/null || echo "Aucun processus trouvé sur le port 8082"

echo "Arrêt du Tracking Service (port 8083)..."
lsof -ti:8083 | xargs kill -9 2>/dev/null || echo "Aucun processus trouvé sur le port 8083"

echo "Arrêt du Billing Service (port 8084)..."
lsof -ti:8084 | xargs kill -9 2>/dev/null || echo "Aucun processus trouvé sur le port 8084"

echo "Arrêt de l'API Gateway (port 8080)..."
lsof -ti:8080 | xargs kill -9 2>/dev/null || echo "Aucun processus trouvé sur le port 8080"

echo ""
echo "========================================"
echo "Tous les services ont été arrêtés"
echo "========================================"
