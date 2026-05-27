import json
import os
from fastapi import (
    FastAPI,
    HTTPException,
    Response,
)
from fastapi.responses import FileResponse

from src.db import DB
from src.event_type import Event

app = FastAPI()
db = DB()

db.connect()

def there_are_two_presentation_responses_as_last(lines):
    # lines = [l for l in lines if l != "end" and "rect" not in l and "participant" not in l and "sequenceDiagram" not in l]
    a = [a for a in lines if "Provide Identity Information" in a]
    return len(a) % 2 == 0 and len(a) != 0

def there_are_6_issuance_messages(lines):
    lines = [l for l in lines if l != "end" and "rect" not in l and "participant" not in l and "sequenceDiagram" not in l] 
    if len(lines) != 6:
        return False
    lines = [l for l in lines if "digital identity" in l.lower()]
    return len(lines) == 6
    

@app.get("/")
def hello_world():
    return {"Hello": "World"}

@app.get("/services")
def get_services(response: Response):
    '''
    the services that have been registered so far
    Notice that the BE service inquiry should not be used by the UI
    otherwise it causes a message to be added to the seq diagram
    '''
    response.headers["Access-Control-Allow-Origin"] = "http://localhost:48173"
    events = [e['content'] for e in db.get_events() if e['sub_group'] == "ServiceRegistrationRequestSend"] 
    return events

@app.get("/events")
def get_events(response: Response):
    response.headers["Access-Control-Allow-Origin"] = "http://localhost:48173"
    return db.get_events()

@app.get("/did-resolutions")
def get_did_resolution(response: Response):
    response.headers["Access-Control-Allow-Origin"] = "http://localhost:48173"
    events = [e for e in db.get_events() if e['sub_group'] == "DidResolutionResponseReceived"] 
    nice = [
        {
            "requester_role": e['trg_entity'],
            "target_did": e['content']['did'],
            "timestamp": e['timestamp'],
            "did_document": e['content']['didDocumentString']
        } 
        for e in events
    ]
    return nice
    
@app.get("/issuances")
def get_issuances(response: Response):
    response.headers["Access-Control-Allow-Origin"] = "http://localhost:48173"
    issuanceResponseEvents = [e for e in db.get_events() if e['sub_group'] == "IssuanceResponseReceived"] 
    
    # artificially add the second issuer
    issuanceResponseEvents = [
        {
            **e, 
            "src_entity": "DT IDM", 
            'content': {
                **e['content'], 
                "issuer_name": "DT IDM"
            }
        } if e["trg_entity"] == "PROVIDER" 
        else 
        {
            **e, 
            "src_entity": "MAGENTA IDM", 
            'content': {
                **e['content'], 
                "issuer_name": "MAGENTA IDM"
            }
        }
        for e in issuanceResponseEvents
    ]
    return issuanceResponseEvents

@app.get("/entities")
def get_entities(response: Response):
    response.headers["Access-Control-Allow-Origin"] = "http://localhost:48173"
    return db.get_discovered_entities()

@app.post("/events", status_code=201)
def create_event(event: Event):
    db.insert_event(event.dict())
    return "Ok"

@app.get("/mermaid")
def get_mermaid(response: Response):
    
    message_replacements = {
        "AttachmentRequestSend": "Request Connection",
        "AttachmentResponseSend": "Grant Connection",
        "IssuanceRequestSend": "Request Digital Identity",
        "IssuanceResponseSend": "Issue Digital Identity",
        "PresentationRequestSend": "Request Identity Information",
        "PresentationResponseSend": "Provide Identity Information",
        "ServiceRegistrationRequestSend": "Register Service",
        "ServiceRegistrationResponseSend": "Confirm Service Registration",
        "DidResolutionRequestSend": "Request Key Material",
        "DidResolutionResponseSend": "Provide Key Material",
        "ServiceDiscoveryRequestSend": "Request Services",
        "ServiceDiscoveryResponseSend": "Provide Services",
        "ServiceUsageRequestSend": "Request Service Usage",
        "ServiceUsageResponseSend": "Grant Service Usage",
    }

    # actor_string = "    actor {{actor}}"

    def format_sequence_diagram(events):
        NOT_CONNECTED = "  Note over CUSTOMER,DLG: NEITHER \"CUSTOMER\" NOR \"QR GENERATOR\" ARE CONNECTED\n"
        DID_KNOWN_STATUS_BOX = "  Note over CUSTOMER,DLG: DID OF {} BECOMES KNOWN<br/>BUT {} IS NOT AUTHENTICATED\n"
        AUTHENTICATED_STATUS_BOX = "  Note over CUSTOMER,DLG: {} BECOMES AUTHENTICATED\n"
        
        diagram_lines = [
            'sequenceDiagram', 
            "   participant CUSTOMER",
            "   participant QR GENERATOR",
            "   participant NSC",
            "   participant DT IDM",
            "   participant MAGENTA IDM",
            "   participant DLG",
        ]

        
        for event in events:
            sub = {
                "ISSUER": "MAGENTA IDM",
                "AP": "NSC",
                "PROVIDER": "QR GENERATOR",
            }
            src = sub.get(event['src'], event['src'])
            trg = sub.get(event['trg'], event['trg'])
            event_type = event['type']
            
            # fake second issuer 
            if (src == "QR GENERATOR" or trg == "QR GENERATOR") and "issuance" in event_type.lower():
                sub = {
                    "MAGENTA IDM": "DT IDM", 
                }
                src = sub.get(src, src)
                trg = sub.get(trg, trg)
            
            message_type = message_replacements[event_type] if event_type in message_replacements else f"ADD REPLACEMENT: {event_type}"
            color = get_message_color(event_type)
            line = f'    {src}->>{trg}: {message_type}'
            diagram_lines.append(color)
            diagram_lines.append(line)
            diagram_lines.append("end")
            
            # activation for verificaiton
            s = ""
            if message_type == message_replacements["PresentationResponseSend"]:
                s = "  activate {0}\n{0}-->>{1}: confirm authentication\ndeactivate {0}\n"
                s = s.format(trg, src)
                diagram_lines.append(s)
            
            # 3 status boxes
            if there_are_6_issuance_messages(diagram_lines):
                diagram_lines.append(NOT_CONNECTED)
            
            if message_type == message_replacements["AttachmentResponseSend"]:
                diagram_lines.append(DID_KNOWN_STATUS_BOX.format(trg, trg))
                
            if s == "  activate {0}\n{0}-->>{1}: confirm authentication\ndeactivate {0}\n".format("NSC", "QR GENERATOR") or s == "  activate {0}\n{0}-->>{1}: confirm authentication\ndeactivate {0}\n".format("QR GENERATOR", "NSC"):
                
                
                if ''.join(diagram_lines).count("confirm authentication") % 2 != 0:
                    pass
                else:
                    diagram_lines.append(AUTHENTICATED_STATUS_BOX.format("QR GENERATOR"))
                
            if s == "  activate {0}\n{0}-->>{1}: confirm authentication\ndeactivate {0}\n".format("NSC", "CUSTOMER") or s == "  activate {0}\n{0}-->>{1}: confirm authentication\ndeactivate {0}\n".format("CUSTOMER", "NSC"):
                
                
                if ''.join(diagram_lines).count("confirm authentication") % 2 != 0:
                    pass
                else:
                    diagram_lines.append(AUTHENTICATED_STATUS_BOX.format("CUSTOMER"))
                
            
        result = '\n'.join(diagram_lines)
        return result
    
    def get_message_color(event_type):
        ok = {
            "connection": "rect rgb(225, 255, 255)", # 011
            "attachment": "rect rgb(255, 225, 255)",
            "didresolution": "rect rgb(255, 255, 225)",
            "issuance": "rect rgb(225, 225, 255)", # 001
            "presentation": "rect rgb(255, 225, 225)", # 100
            "serviceregistration": "rect rgb(225, 255, 225)", # 010
            "servicediscovery": "rect rgb(225, 225, 225)", 
            "serviceusage": "rect rgb(225, 255, 255)", 
        }
        
        for k in ok.keys():
            if k in event_type.lower():
                return ok[k]
        return ""

    response.headers["Access-Control-Allow-Origin"] = "http://localhost:48173"
    aggregated_events = db.get_aggregated_events()
    return format_sequence_diagram(aggregated_events)