# dissident2 — service-aware network demonstrator

A 5-entity self-sovereign identity demonstrator built on **Hyperledger Indy** and
**ACA-Py** (Aries Cloud Agent — Python). Each entity is a pair of containers — a
Java **controller** holding the business logic, and an **ACA-Py agent** handling
DIDComm / VC issuance / DID resolution.

| Entity          | Role in the scenario                                                                 |
| --------------- | ------------------------------------------------------------------------------------ |
| **Client**      | Browses services listed on the Service Repository.                                   |
| **Provider**    | Offers services to the Service Repository.                                           |
| **AccessPoint** | The Service Repository itself; brokers DIDComm connections between the other parts. |
| **DLG**         | Distributed Ledger Gateway — DID resolution endpoint for everyone else.              |
| **Issuer**      | Issues the credentials that gate access to the scenario.                             |

The whole stack is operated through **Docker Compose**, against the **local
von-network ledger** running on the NUC (see
[Local von ledger](#local-von-ledger-replaces-bcovrin)).

## Repository layout

```
dissident2handover/
├── fresh-run.sh                       # one-shot reset for between demos (see §4)
├── dissident2/                        # the 5-entity stack (Java controllers + ACA-Py agents)
│   ├── docker-compose.yml             # source of truth for ports, env, ENTITY_SCHEMAID
│   ├── customer/ provider/ accesspoint/
│   ├── dlg/ issuer/                   # one Maven module per entity (Spring Boot controllers)
│   └── common/                        # shared controller code (webhook base classes)
├── dissident2-ui/                     # Vue 3 + Vite demo UI (the operator-facing console)
├── ui-backend/                        # FastAPI service that backs the UI; persists events to mongo
└── custom-did-resolution-submodule/   # `snet_resolver` ACA-Py plugin (pip-installed into resolver agents)
```

- **`dissident2/`** — the protocol stack. Each entity has its own Maven module
  containing a Spring Boot controller; ACA-Py runs alongside it as a sidecar
  container. All five entities share `common/` for webhook-handling base
  classes.
- **`dissident2-ui/`** — the Vue 3 single-page app you open in a browser to
  drive the scenario.
- **`ui-backend/`** — the FastAPI service that the UI talks to; it stores
  every interaction as an event document in the `d2-ui-mongo` MongoDB
  container, and the UI derives "connected / associated" state by reading
  those events back.
- **`custom-did-resolution-submodule/`** — the `snet_resolver` Python package,
  an ACA-Py plugin. The customer / provider / accesspoint agents bind-mount it
  read-only into their container at boot, `pip install` it, and start ACA-Py
  with `--plugin snet_resolver`. The plugin routes DID resolution back through
  each entity's own controller webhook
  (`/webhook/topic/did_resolution/`).
- **`fresh-run.sh`** — the one operational script you run between demos. See
  [fresh-run.sh deep-dive](#fresh-runsh-deep-dive).

## Quick start — running and resetting the demo

### Pre-flight

Two dependencies must already be up — `fresh-run.sh` won't start them:

- **Local von ledger** (`von-node1..4`, `von-webserver-1`) — the agents resolve
  `http://172.17.0.1:9000/genesis` against it.
- **`d2-ui-mongo`** — backs the UI's event store; without it the UI won't
  reset to DISCONNECTED.

Both are brought up as part of the **dissident 1 (`dissi1proto`) stack** on
this NUC — refer to the dissident 1 docs to start them. Verify they're
running:

```sh
docker ps --format '{{.Names}}' | grep -E 'von-|d2-ui-mongo'
```

### Reset between demos

From the repo root:

```sh
./fresh-run.sh
```

**Why every demo needs a reset:** the ACA-Py agents run with
`--wallet-type askar --auto-provision` and **no persistent volume**, so each
`docker compose down` wipes the issuer's wallet. When the stack comes back
up, the issuer must re-anchor a new schema + credential definition on the
ledger — and the version must be one that's never been anchored before,
otherwise ACA-Py errors with `<cred-def> is on ledger ... but not in wallet`
and the issuer shuts itself down. `fresh-run.sh` handles that automatically
(see [Schema-id-per-run](#schema-id-per-run-requirement) for the full
explanation). The reset also clears the UI's event store so consumer and
provider start DISCONNECTED — required for the manual-connect demo flow.

When the script finishes, it prints a state check; the stack is ready when
all 5 agents report ready and the issuer logs
`Credential Definition created successfully`.

### Drive the demo from a browser

The UI lives at **http://localhost:48173**. Two more host ports back it:
nginx API gateway at **48080**, ui-backend events service at **48024**. If
you're driving the demo from a remote laptop instead of the NUC itself, set
up the [SSH tunnel](#ssh-port-forwarding-for-external-browser-access) first.

After every `./fresh-run.sh`:

1. **Hard-refresh** the UI — a soft reload will keep stale "connected"
   state from the previous run.
2. Consumer and provider should both show **DISCONNECTED**.
3. Click each entity's **connect button exactly once**. Clicking twice will
   400 with `Connection already exists` (see
   [Operational quirks](#operational-quirks--known-issues)).

## SSH port-forwarding for external browser access

The NUC binds the three host ports to `localhost` only, so they're not
reachable from another machine on the LAN. To drive the demo from a laptop,
tunnel them over SSH:

```sh
ssh -N \
  -L 48173:localhost:48173 \
  -L 48080:localhost:48080 \
  -L 48024:localhost:48024 \
  snet@141.23.117.100
```

| Local port | Forwards to                             |
| ---------- | --------------------------------------- |
| 48173      | `dissident2-ui` (Vue 3 demo UI)         |
| 48080      | nginx API gateway in front of the stack |
| 48024      | `ui-backend` events service (FastAPI)   |

`-N` keeps the connection open without opening a remote shell. Leave the
terminal running for the duration of the demo; then `http://localhost:48173`
on your laptop hits the UI on the NUC.

## DID and port allocation

### DIDs

The five entities are identified by deterministic DIDs derived from their
fixed seeds. The seeds, DIDs, and verkeys do **not** change run-to-run; only
the credential schema version does (see
[Schema-id-per-run](#schema-id-per-run-requirement)). The DIDs were
originally anchored on BCovrin and were re-anchored unchanged on the local
von ledger during the 2026-05-27 migration.

| Entity      | Seed                               | DID                      | Verkey                                         |
| ----------- | ---------------------------------- | ------------------------ | ---------------------------------------------- |
| Client      | `Dissident2_Client_---_0000000000` | `V9fvKQjtmbsoJb7gLm2Fka` | `GLrx5YKrUQyWDp8f7FqbXpMtkaY9KvL97Cti5MH2yXiU` |
| Provider    | `Dissident2_Provider_---_00000000` | `KssDMmREv3migEZNLMThjc` | `BHjs9GVtKs14xqX3BcJGVXamBGAPh7zeKjUn79XxjB5t` |
| AccessPoint | `Dissident2_Ap_---_00000000000000` | `SUqWD8ZL3r6KeYTKKQ6zRw` | `EtUKSFhpxcZqYa3iRzCU9J34zuHvhDNzgBzMJCNHVLKa` |
| DLG         | `Dissident2_Dlg_---_0000000000000` | `8eQhKkZMjKXbLwaBNEKRu3` | `5Ag23enN3zFtvapPKHiEcE9ysi36V4z6GBkKw4w3BKGW` |
| Issuer      | `Dissident2_Issuer_---_0000000000` | `WQtxQy4ERo6vgxkM1o5BPh` | `H2mjGFtNikTXQSVysrcjYSt5iDJoRp4jFmNSY91UJMTy` |

### Ports

All agent and controller ports listed below are **internal to the
`dissident2_default` compose network**. The only ports that bind to the host
are `48080` (nginx api-gateway), `48173` (UI), and `48024` (ui-backend) —
those are the ones you tunnel for remote access.

| Entity      | ACA-Py admin | ACA-Py transport | Controller |
| ----------- | ------------ | ---------------- | ---------- |
| Client      | 5000         | 5100             | 5555       |
| Provider    | 6000         | 6100             | 6666       |
| AccessPoint | 7000         | 7100             | 7777       |
| DLG         | 8000         | 8100             | 8888       |
| Issuer      | 9000         | 9100             | 9999       |

- **ACA-Py admin** — the Swagger UI / admin API for that agent.
- **ACA-Py transport** — inbound DIDComm transport listener.
- **Controller** — the Java controller's UI and the webhook endpoint ACA-Py
  POSTs back to.

## Local von ledger (replaces BCovrin)

On **2026-05-27** the stack was migrated off public `test.bcovrin.vonx.io`
onto the **local von-network** on this NUC (`von-node1..4`,
`von-webserver-1`). Genesis URL for all five agents:
`http://172.17.0.1:9000/genesis` (the host's `docker0` IP, reachable from
the compose network without extra bridging).

The five seed-DIDs were re-anchored on local von via `POST /register` with
role `ENDORSER`; since Indy DIDs are deterministic from their seeds, the
values are identical to the BCovrin originals. The ledger is **shared with
`dissi1proto`** — append-only and non-overlapping namespaces, so the
migration only added transactions.

## Schema-id-per-run requirement

ACA-Py agents run with `--wallet-type askar --auto-provision` and **no
persistent volume**, so every `docker compose down` wipes the issuer's
wallet. When the stack comes back up, the issuer's
`SchemaInitializationRunner` reads `${entity.schemaId}` and:

- if that exact id is **already anchored on the ledger** → ACA-Py errors with
  `<cred-def> is on ledger ... but not in wallet` and the issuer shuts itself
  down,
- if it's **never been anchored** → the issuer creates a new schema + cred-def
  for that version. This is the working path.

So before every fresh run, `ENTITY_SCHEMAID` must be bumped to a new,
never-anchored version like
`WQtxQy4ERo6vgxkM1o5BPh:2:dissident2:2026.<Mdd>.<HHMMSS>`.
`fresh-run.sh` does this automatically.

### Where the schema id lives

Single source of truth: the issuer's `ENTITY_SCHEMAID` env in
`dissident2/docker-compose.yml`. That's what `fresh-run.sh` sed-bumps and
what Spring binds into `${entity.schemaId}` in the issuer at startup.

## `fresh-run.sh` deep-dive

1. **`docker compose down`** — wipes the ephemeral askar wallets, so no
   stale DID-exchange connections survive.
2. **Bump `ENTITY_SCHEMAID`** to `:2026.<Mdd>.<HHMMSS>` via `sed` — the
   never-anchored version the issuer needs (see
   [Schema-id-per-run](#schema-id-per-run-requirement)).
3. **Clear the UI event store** —
   `db.events.deleteMany({})` in `d2-ui-mongo`. The UI reads connected /
   associated state from these events; clearing them is what makes consumer
   and provider start DISCONNECTED.
4. **`docker compose up -d --build`**, then poll `/status` on the 5 agents
   until all `200`, then tail the issuer log for
   `Credential Definition created successfully` (or bail on `not in wallet`).

### What it does NOT touch

- The **local von ledger** containers — separate compose project.
- The **`d2-ui-mongo` container** itself — only documents in one collection
  are deleted.
- **`dissi1proto`** and anything else outside the dissident2 compose project.

## Other scripts

The only operator-facing script is `fresh-run.sh`. The `wait-for-agent.sh`
files under each `dissident2/<entity>/` are Dockerfile-internal startup
plumbing — leave them alone.

## Subprojects

`fresh-run.sh` resets only the 5-agent dissident2 backend. The UI, the
ui-backend FastAPI service, and the mongo it writes into are launched
independently (currently as long-lived `docker run` containers / a host
Vite process) and survive a `fresh-run.sh` cycle — that's by design.

### `dissident2-ui/` — the operator UI

Vue 3 + Vite SPA. The running UI on `:48173` is a Vite dev server started
manually out of this folder:

```sh
cd dissident2-ui && npm install   # first time only
npm run dev -- --port 48173 --host
```

Source-of-truth for everything an operator sees: connection diagrams,
event timeline, the per-entity connect buttons.

### `ui-backend/` — the event-store FastAPI

Python / FastAPI service. Persists every webhook event the controllers
forward into MongoDB; the UI reads connected/associated state back from
that event stream. The running process lives in the **`d2-ui-backend`**
container (image built from this folder's `Dockerfile`) and listens on
host port `:48024`. OpenAPI docs are at `http://localhost:48024/docs`.

To rebuild after a code change:

```sh
docker build -t d2-ui-backend ui-backend
docker restart d2-ui-backend
```

### `custom-did-resolution-submodule/` — the `snet_resolver` ACA-Py plugin

Python plugin that the customer / provider / accesspoint agents
bind-mount into their container and `pip install` at boot (see section 2).
At plugin-`setup` time it **deregisters the default `IndyDIDResolver` and
registers `SnetResolver` in its place** (`snet_resolver/__init__.py`), so
every `did:sov` lookup from those agents goes through the custom path:

```
agent  ──HTTP POST──▶  controller webhook
                       /webhook/topic/did_resolution/   { "requestDid": did }
       ◀──── DID Doc ──── controller resolves locally / via DLG
```

The folder is a fork of Daniel Bluhm's original "did:github" example
plugin (see `pyproject.toml`); the dissident2 fork swapped the supported
method to `did:sov:*` and the routing logic. The DLG and Issuer agents
do **not** load it — they resolve through the ledger directly.

## Operational quirks & known issues

### `Connection already exists` on a second connect click

Once two entities have established a DIDComm connection, calling
`createDidExchangeRequest` again returns `400 Connection already exists`.
Click each entity's connect button **exactly once** after a reset. If you
do double-click, the underlying connection is fine — hard-refresh the UI
and the state catches up.

### Issuer crash: `<cred-def> is on ledger ... but not in wallet`

`ENTITY_SCHEMAID` points at a version already anchored on the ledger from
a previous run. `fresh-run.sh` prevents this by bumping the version every
run, and detects it during its wait-for-anchor step (exits 1). If you
ever see it, just run `./fresh-run.sh` again. Full background in
[Schema-id-per-run](#schema-id-per-run-requirement).
