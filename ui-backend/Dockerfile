FROM python:3.11.6

RUN adduser app
WORKDIR /home/app
COPY --chown=app . .
USER app
RUN python -m pip install --no-cache-dir pipenv --user && ~/.local/bin/pipenv sync
ENTRYPOINT ~/.local/bin/pipenv run uvicorn src.main:app --host 0.0.0.0 --port 8024
EXPOSE 8000
