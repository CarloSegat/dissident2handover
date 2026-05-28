FROM python:3.11.7

RUN adduser app
WORKDIR /home/app
COPY --chown=app . .
USER app
RUN python -m pip install --no-cache-dir pipenv --user && ~/.local/bin/pipenv lock && ~/.local/bin/pipenv sync
ENTRYPOINT ["/home/app/.local/bin/pipenv", "run", "uvicorn", "src.main:app", "--host", "0.0.0.0", "--port", "48024"]
EXPOSE 48024
