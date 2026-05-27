#!/bin/bash

curl -X POST -H 'Content-Type: application/json' \
    --data '{"src_entity":"C2","trg_entity":"AP","timestamp":"2024-01-12T12:24:44.158000","content":"{\"hello\":\"world2\"}","group":"GROUP_A","action":"ACTION_A","did_src":"DID_SRC","did_trg":"DID_TRG"}' \
    http://localhost:8000/events

