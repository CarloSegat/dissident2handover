# Getting started

## Start the project for testing

The whole project runs in docker.
Start it using docker compose: `docker compose up -d`

## Start the project for development
This project uses `pipenv`. 
- Install the depedendencies with: `pipenv sync -v`

## Access the server
- The server is available at `localhost:8000`
- An OpenAPI UI is automatically generated and available at `localhost:8000/docs` or `localhost:8000/redoc`
