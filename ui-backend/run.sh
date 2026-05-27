#!/bin/bash
docker stop ui-backend
docker rm ui-backend
docker run --name ui-backend -d -p 48017:27017 -e MONGO_INITDB_ROOT_USERNAME=myuser -e MONGO_INITDB_ROOT_PASSWORD=mypassword mongo:4.4

pipenv run uvicorn src.main:app --host 0.0.0.0 --port 48024 --reload
