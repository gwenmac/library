#!/usr/bin/env bash
set -euo pipefail

# ── Initial Droplet Setup ──
# Run once on a fresh Ubuntu droplet to install dependencies and configure the app.
#
# Usage: ssh root@your-droplet 'bash -s' < deploy/setup-droplet.sh

echo "==> Updating system..."
apt-get update && apt-get upgrade -y

echo "==> Installing Docker..."
curl -fsSL https://get.docker.com | sh

echo "==> Installing Docker Compose plugin..."
apt-get install -y docker-compose-plugin

echo "==> Creating app directory..."
mkdir -p /opt/library
cd /opt/library

echo "==> Setup complete!"
echo ""
echo "Next steps:"
echo "  1. Clone your repo:  git clone <your-repo-url> /opt/library"
echo "  2. Configure env:    cp .env.example .env && nano .env"
echo "  3. Deploy:           bash deploy/deploy.sh"
echo ""
echo "For HTTPS (Let's Encrypt):"
echo "  1. Point your domain DNS to this droplet's IP"
echo "  2. Run: docker run --rm -v /etc/letsencrypt:/etc/letsencrypt -p 80:80 certbot/certbot certonly --standalone -d your-domain.com"
echo "  3. Uncomment SSL sections in deploy/nginx.conf and docker-compose.prod.yml"
echo "  4. Re-deploy: bash deploy/deploy.sh"
