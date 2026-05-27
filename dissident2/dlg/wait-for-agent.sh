#!/bin/sh
# wait-for-agent.sh

set -e

until curl --output /dev/null --silent --head --fail http://localhost:8000; do
  printf '.'
  sleep 2
done

exec java -jar app.jar

