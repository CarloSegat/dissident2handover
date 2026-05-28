# HOW2RUN

From-scratch bring-up on the NUC. Run in order.

## Prerequisites

This guide assumes the **local von ledger** and **d2-ui-mongo** are already running. They are brought up as part of the dissident1 stack — see `/opt/dissi1proto/QUICKSTART.md` (or `HANDOVER.md` / `README.md` in the same directory) for setup.

Quick sanity check:

```sh
docker ps --format '{{.Names}}' | grep -E '^von-(node[1-4]|webserver)|^d2-ui-mongo$'
curl -sf http://172.17.0.1:9000/genesis > /dev/null && echo "genesis OK"
docker exec d2-ui-mongo mongo -u myuser -p mypassword \
  --authenticationDatabase admin --quiet --eval 'db.runCommand({ping:1})'
```

## 1. Build & start d2-ui-backend

```sh
cd /home/snet/dissident2handover
docker build -t d2-ui-backend ui-backend
docker run -d --name d2-ui-backend --restart unless-stopped \
  --network host d2-ui-backend
curl -sf http://localhost:48024/docs > /dev/null && echo "ui-backend OK"
```

## 2. Bring up the dissident2 stack

```sh
cd /home/snet/dissident2handover
./fresh-run.sh
```

Wait for "Credential Definition created successfully".

## 3. Start the UI

```sh
cd /home/snet/dissident2handover/dissident2-ui
npm install          # first time only
npm run dev -- --port 48173 --host
```

Open <http://localhost:48173> (hard-refresh after every `fresh-run.sh`).

## Remote browser access (optional)

```sh
ssh -N \
  -L 48173:localhost:48173 \
  -L 48080:localhost:48080 \
  -L 48024:localhost:48024 \
  snet@141.23.117.100
```

## Reset between demos

```sh
cd /home/snet/dissident2handover && ./fresh-run.sh
```
