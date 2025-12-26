#!/bin/bash
# Script de lancement des services backend CourierSync

echo "========================================"
echo "Lancement des services backend CourierSync"
echo "========================================"
echo ""

# Obtenir le répertoire du script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Vérification des prérequis
echo "Verification des pre-requis..."
echo ""

# Démarrage des services dans l'ordre
echo "1. Installation des modules Maven (si necessaire)"
cd "$SCRIPT_DIR"
mvn clean install -DskipTests -q
if [ $? -ne 0 ]; then
    echo "Erreur lors de la construction Maven"
    exit 1
fi

echo ""
echo "2. Lancement du User Auth Service (port 8081)"
cd "$SCRIPT_DIR/user-auth-service"
mvn spring-boot:run > /tmp/user-auth-service.log 2>&1 &
USER_AUTH_PID=$!
echo "Service démarré avec PID: $USER_AUTH_PID"
sleep 15

echo ""
echo "3. Lancement du Dispatch Service (port 8082)"
cd "$SCRIPT_DIR/dispatch-service"
mvn spring-boot:run > /tmp/dispatch-service.log 2>&1 &
DISPATCH_PID=$!
echo "Service démarré avec PID: $DISPATCH_PID"
sleep 15

echo ""
echo "4. Lancement du Tracking Service (port 8083)"
cd "$SCRIPT_DIR/tracking-service"
mvn spring-boot:run > /tmp/tracking-service.log 2>&1 &
TRACKING_PID=$!
echo "Service démarré avec PID: $TRACKING_PID"
sleep 15

echo ""
echo "5. Lancement du Billing Service (port 8084)"
cd "$SCRIPT_DIR/billing-service"
mvn spring-boot:run > /tmp/billing-service.log 2>&1 &
BILLING_PID=$!
echo "Service démarré avec PID: $BILLING_PID"
sleep 15

echo ""
echo "6. Lancement de l'API Gateway (port 8080)"
cd "$SCRIPT_DIR/api-gateway"
mvn spring-boot:run > /tmp/api-gateway.log 2>&1 &
GATEWAY_PID=$!
echo "Service démarré avec PID: $GATEWAY_PID"

echo ""
echo "========================================"
echo "Tous les services sont en cours de lancement"
echo "========================================"
echo ""
echo "Services:"
echo "- User Auth Service: http://localhost:8081"
echo "- Dispatch Service: http://localhost:8082"
echo "- Tracking Service: http://localhost:8083"
echo "- Billing Service: http://localhost:8084"
echo "- API Gateway: http://localhost:8080"
echo ""
echo "Logs disponibles dans /tmp/"
echo "- User Auth Service: /tmp/user-auth-service.log"
echo "- Dispatch Service: /tmp/dispatch-service.log"
echo "- Tracking Service: /tmp/tracking-service.log"
echo "- Billing Service: /tmp/billing-service.log"
echo "- API Gateway: /tmp/api-gateway.log"
echo ""
echo "Pour arrêter tous les services, exécutez: kill $USER_AUTH_PID $DISPATCH_PID $TRACKING_PID $BILLING_PID $GATEWAY_PID"
echo "Appuyez sur Ctrl+C pour quitter ce script (les services continueront de tourner)"
wait
