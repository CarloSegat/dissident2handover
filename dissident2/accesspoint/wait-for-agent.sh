#!/bin/sh
# wait-for-agent.sh

set -e

# Wait for the service to be available, handling HTTP redirects and checking for successful connection
until curl --output /dev/null --silent --location http://localhost:7000; do
  printf '.'
  sleep 2
done

# Execute the Java application
exec java -jar app.jar

