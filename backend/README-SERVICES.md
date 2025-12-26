# Lancement des services backend CourierSync

## Prérequis

Avant de lancer les services, assurez-vous que les infrastructures suivantes sont disponibles :
- PostgreSQL (ports 5432, 5433, 5434)
- MongoDB (port 27017)
- Redis (port 6379)
- Kafka (port 9092)

## Services

L'application backend se compose de 5 services :

1. **API Gateway** (port 8080)
   - Point d'entrée unique pour toutes les requêtes
   - Gère l'authentification et le routage vers les services appropriés

2. **User Auth Service** (port 8081)
   - Gestion des utilisateurs et de l'authentification
   - Émission des tokens JWT

3. **Dispatch Service** (port 8082)
   - Gestion des commandes de livraison
   - Assignation des chauffeurs

4. **Tracking Service** (port 8083)
   - Suivi en temps réel des livraisons
   - WebSocket pour les mises à jour en temps réel

5. **Billing Service** (port 8084)
   - Gestion de la facturation
   - Génération des factures PDF

## Lancement des services

### Windows

1. Double-cliquez sur `start-services.bat` ou exécutez-le depuis une invite de commande
2. Les services seront lancés dans l'ordre approprié avec un délai de 15 secondes entre chaque service
3. Chaque service s'exécutera dans une fenêtre de commande séparée

Pour arrêter tous les services :
- Double-cliquez sur `stop-services.bat` ou exécutez-le depuis une invite de commande

### Linux/Mac

1. Rendez le script exécutable : `chmod +x start-services.sh`
2. Exécutez le script : `./start-services.sh`
3. Les services seront lancés en arrière-plan avec les logs dans /tmp/

Pour arrêter tous les services :
- Exécutez : `./stop-services.sh`

## Vérification du bon fonctionnement

Une fois les services lancés, vous pouvez vérifier leur état en accédant aux endpoints de santé :

- API Gateway : http://localhost:8080/actuator/health
- User Auth Service : http://localhost:8081/actuator/health
- Dispatch Service : http://localhost:8082/actuator/health
- Tracking Service : http://localhost:8083/actuator/health
- Billing Service : http://localhost:8084/actuator/health

## Dépannage

Si un service ne démarre pas correctement :
1. Vérifiez les logs dans la fenêtre de commande correspondante (Windows) ou dans /tmp/ (Linux/Mac)
2. Vérifiez que les ports requis sont disponibles
3. Vérifiez que les infrastructures (PostgreSQL, MongoDB, Redis, Kafka) sont en cours d'exécution
4. Vérifiez que les bases de données sont correctement configurées

## Notes importantes

- Le premier lancement peut prendre plus de temps car Maven doit télécharger les dépendances
- Assurez-vous d'avoir suffisamment de mémoire disponible (au moins 4 Go recommandés)
- Les services doivent être lancés dans l'ordre spécifié dans les scripts pour assurer le bon fonctionnement des dépendances
