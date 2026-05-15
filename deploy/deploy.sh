#!/usr/bin/env bash
set -euo pipefail

# ── Library App Deploy Script ──
# Run this on the droplet to pull latest code and restart services.
#
# Prerequisites:
#   - Docker & Docker Compose installed
#   - .env file configured (cp .env.example .env && edit)
#   - Git repo cloned to /opt/library (or wherever you prefer)

APP_DIR="${APP_DIR:-/opt/library}"

echo "==> Pulling latest code..."
cd "$APP_DIR"
git pull origin main

echo "==> Building services..."
docker compose -f docker-compose.prod.yml build backend
docker compose -f docker-compose.prod.yml build frontend

echo "==> Restarting services..."
docker compose -f docker-compose.prod.yml down backend
docker compose -f docker-compose.prod.yml down frontend
docker compose -f docker-compose.prod.yml up backend -d
docker compose -f docker-compose.prod.yml up frontend -d

echo "==> Cleaning up old images..."
docker image prune -f

echo "==> Done! Services:"
docker compose -f docker-compose.prod.yml ps
