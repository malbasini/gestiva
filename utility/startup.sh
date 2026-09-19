#!/bin/bash

set -e

APP_DIR="/opt/vericert"
COMPOSE_FILE="docker-compose.yml"

echo "==> Entro nella cartella applicazione"
cd "$APP_DIR"

echo "==> Build e riavvio VeriCert"
docker compose -f "$COMPOSE_FILE" up -d --build db app

echo "==> Stato container"
docker compose -f "$COMPOSE_FILE" ps

echo "==> Ultimi log app"
docker compose -f "$COMPOSE_FILE" logs --tail=50 app

echo "==> Deploy completato"

