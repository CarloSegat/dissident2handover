#!/usr/bin/env bash
#
# fresh-run.sh — clean restart of the dissident2 stack for a demo.
#
# What it does, in order:
#   1. Tears down the compose stack (this also wipes the agents' askar wallets,
#      since they run --auto-provision with NO persistent volume -> no stale
#      DID-exchange connections survive).
#   2. Bumps ENTITY_SCHEMAID to a fresh, never-anchored version. Required every
#      run: the issuer re-creates the credential schema + cred-def in its clean
#      wallet, and a previously-anchored version would crash it with
#      "on ledger ... but not in wallet".
#   3. Clears the app-state DB: the mongo `events` collection in d2-ui-mongo
#      (db `ui-backend`). The UI derives the "connected/associated" state purely
#      from these events, so this is what makes consumer/provider start
#      DISCONNECTED for a manual-connect demo.
#   4. Brings the stack back up and waits until all 5 agents are ready and the
#      issuer has anchored the new schema.
#
# Usage:  ./fresh-run.sh
set -euo pipefail

COMPOSE_DIR="/home/snet/dissident2handover/dissident2"
COMPOSE_FILE="$COMPOSE_DIR/docker-compose.yml"
SCHEMA_PREFIX='WQtxQy4ERo6vgxkM1o5BPh:2:dissident2:'
MONGO_CONTAINER="d2-ui-mongo"
UI_BACKEND_EVENTS_URL="http://localhost:48024/events"

cd "$COMPOSE_DIR"

echo "==> [1/4] Tearing down stack (wipes ephemeral agent wallets)..."
docker compose down

echo "==> [2/4] Bumping ENTITY_SCHEMAID to a fresh version..."
NEWVER="$(date +%Y.%-m%d.%H%M%S)"
sed -i -E "s#(ENTITY_SCHEMAID: \"${SCHEMA_PREFIX})[^\"]*#\1${NEWVER}#" "$COMPOSE_FILE"
echo "    new schema id: ${SCHEMA_PREFIX}${NEWVER}"
grep ENTITY_SCHEMAID "$COMPOSE_FILE"

echo "==> [3/4] Clearing app-state DB (mongo events collection)..."
if docker ps --format '{{.Names}}' | grep -q "^${MONGO_CONTAINER}$"; then
  docker exec "$MONGO_CONTAINER" mongo -u myuser -p mypassword \
    --authenticationDatabase admin ui-backend --quiet \
    --eval "db.events.deleteMany({})"
else
  echo "    WARNING: ${MONGO_CONTAINER} not running; skipping event clear."
fi

echo "==> [4/4] Bringing stack up (--build picks up any rebuilt controller jars)..."
docker compose up -d --build

echo "    waiting for all 5 agents to report ready..."
for i in $(seq 1 60); do
  ready=0
  for a in customer:5000 provider:6000 accesspoint:7000 dlg:8000 issuer:9000; do
    name="${a%%:*}-agent-service"; port="${a##*:}"
    if docker exec "$name" curl -s -o /dev/null -w "%{http_code}" \
         "http://localhost:$port/status" 2>/dev/null | grep -q 200; then
      ready=$((ready+1))
    fi
  done
  echo "    tick $i: $ready/5 agents ready"
  [ "$ready" = "5" ] && break
  sleep 5
done

echo "    waiting for issuer to anchor schema ${NEWVER}..."
for i in $(seq 1 30); do
  log="$(docker logs issuer-service 2>&1 || true)"
  if echo "$log" | grep -q "not in wallet"; then
    echo "    !!! SCHEMA COLLISION — issuer rejected ${NEWVER}. Inspect: docker logs issuer-service"
    exit 1
  fi
  if echo "$log" | grep -q "Credential Definition created successfully"; then
    echo "$log" | grep -E "Schema created successfully|Credential Definition created successfully" | tail -2
    break
  fi
  sleep 5
done

echo
echo "==> Done. State check:"
echo -n "    discovered/associated services: "; curl -s http://localhost:48024/services || true; echo
echo -n "    ui events count: "; curl -s "$UI_BACKEND_EVENTS_URL" | python3 -c "import sys,json;print(len(json.load(sys.stdin)))" 2>/dev/null || echo "?"
echo
echo "    Hard-refresh the UI (Cmd+Shift+R) at http://localhost:48173 — consumer/provider"
echo "    will show DISCONNECTED. Click each entity's connect button exactly ONCE."
