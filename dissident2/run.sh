#!/bin/sh
# Build the controller JARs and bring up the full main BE via docker-compose (no k8s).
set -e
cd "$(dirname "$0")"
mvn clean package -DskipTests
docker compose up -d --build
docker compose ps
