#!/bin/sh
# From scratch: tear down all containers (fresh ACA-Py askar wallets => clean DIDs/connections),
# rebuild JARs + images, and bring everything back up via docker-compose (no k8s).
set -e
cd "$(dirname "$0")"
docker compose down --remove-orphans
mvn clean package -DskipTests
docker compose up -d --build
docker compose ps
