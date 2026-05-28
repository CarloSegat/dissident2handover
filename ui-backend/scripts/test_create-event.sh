#!/bin/bash
# Smoke-test the ui-backend POST /events endpoint.
# Payload shape mirrors what dissident2/http-logger/server.go sends in production.

curl -X POST -H 'Content-Type: application/json' \
    --data '{
        "SrcEntity":"C2",
        "TrgEntity":"AP",
        "Timestamp":1717000000,
        "Content":{"hello":"world2"},
        "Group":"GROUP_A",
        "SubGroup":"SUB_A"
    }' \
    http://localhost:48024/events
