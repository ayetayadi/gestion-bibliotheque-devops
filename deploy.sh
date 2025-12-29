#!/bin/bash
set -e

echo "Arrêt des conteneurs existants"
docker compose down

echo "Démarrage de la nouvelle version"
docker compose up -d --build

echo "Déploiement terminé avec succès"
