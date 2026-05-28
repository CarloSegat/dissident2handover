from pymongo import MongoClient

from src.event_type import Event


class DB():
    def connect(self):
        client = MongoClient("mongodb://myuser:mypassword@localhost:48017")
        self.client = client
        self.db = client["ui-backend"]
        self.db_events = self.db.events

    def get_events(self):
        return list(self.db_events.find(projection={'_id': 0}))
    
    def get_discovered_entities(self):
        """
        Discovered entites are those for which at least one message exists in the DB.
        """
        discovered_entities = self._get_unique_entity_names()
        attached_entites = self._check_attachment_response_received(discovered_entities)

        return [
            { 
                "name": ent, 
                "attached" : ent in attached_entites 
            } 
            for ent in discovered_entities
        ]


    def insert_event(self, event: Event):
        oId = self.db_events.insert_one(event).inserted_id
        id = str(oId)
        return event

    def get_aggregated_events(self):

        result = []
        
        events = self._filter_mutual_events()
        
        events.sort(key=lambda e: e["timestamp"])
        
        for send in events:
            if "Received" in send['sub_group']:
                continue

            aggregated = {
                "type": send['sub_group'],
                "src": send['src_entity'],
                "trg": send['trg_entity'],
                "timestamp": send['timestamp'],
            }

            result.append(aggregated)
            
        # ServiceUsageResponseSend gets logged twice per grant (mutual side effect);
        # drop the second so the seq diagram shows one "Grant Service Usage" message.
        service_use_reqs = [a for a in result if a['type'] == "ServiceUsageResponseSend"]
        if len(service_use_reqs) >= 2:
            result.remove(service_use_reqs[1])
            
            
            
        result.sort(key=lambda e: e["timestamp"])
            
        result = [
            e for e in result if 'connection' not in e['type'].lower()
        ]

        return result

    ## PRIVATE

    def _get_unique_entity_names(self):
    # Aggregation pipeline for src_entity names
        pipeline_src = [
            {'$group': {'_id': '$src_entity'}},
            {'$project': {'entityName': '$_id', '_id': 0}}
        ]
        src_entities = list(self.db_events.aggregate(pipeline_src))

        # Aggregation pipeline for trg_entity names
        pipeline_trg = [
            {'$group': {'_id': '$trg_entity'}},
            {'$project': {'entityName': '$_id', '_id': 0}}
        ]
        trg_entities = list(self.db_events.aggregate(pipeline_trg))

        # Combine and deduplicate entity names
        all_entities = {entity['entityName'] for entity in src_entities + trg_entities}

        return list(all_entities)
    
    def _check_attachment_response_received(self, entity_names):
        entities_with_attachment = []
        for name in entity_names:
            # Query to find an event with the specific action and target entity
            event = self.db_events.find_one({
                'group': 'Attachment',
                'sub_group': 'AttachmentResponseReceived',
                'trg_entity': name
            }, {
                '_id': 0,
                'trg_entity': 1
            })
            
            # If such an event exists, add the entity name to the result list
            if event:
                entities_with_attachment.append(name)

        return entities_with_attachment

    def _get_request_send_events(self):
        events = list(self.db_events.find(
            {"sub_group": {"$regex": ".*RequestSend$"}},
            projection={'_id': 0}
        ))
        return events

    def _filter_mutual_events(self):
        """Drop didExchange shadow events (e.g. AP seeing a request from DLG when AP initiated) so the seq diagram is one-sided."""

        req_sent_events = self._get_request_send_events()

        def is_not_mutual_req_received(event, send_pairs):
            return (
                event['sub_group'] != "ConnectionRequestReceived" 
                or 
                (
                    event['sub_group'] == "ConnectionRequestReceived" and 
                    (event['trg_entity'], event['src_entity']) not in send_pairs
                )
            )

        def is_not_mutual_resp_sent(event, send_pairs):
            return (
                event['sub_group'] != "ConnectionResponseSend" 
                or 
                (
                    event['sub_group'] == "ConnectionResponseSend" and 
                    (event['src_entity'], event['trg_entity']) not in send_pairs
                )
            )

        all_events = self.get_events()

        send_pairs = {
            (event['src_entity'], event['trg_entity']) 
            for event in req_sent_events
        }

        filtered_received_events = [
            event for event in all_events
            if is_not_mutual_req_received(event, send_pairs) and is_not_mutual_resp_sent(event, send_pairs)
        ]

        return filtered_received_events
