export const ip = "localhost:48080"
export const ui_backend_ip_port = "localhost:48024"

export const providerDid = "did:sov:KssDMmREv3migEZNLMThjc"
export const providerUrl = "http:/test/provider"
export const providerPort = "6000"

export const apDid = "did:sov:SUqWD8ZL3r6KeYTKKQ6zRw"
export const apUrl = "http:/dissidenttwo/networkservicecontroller"
export const apPort = "7000"

export const issuerDid = "did:sov:WQtxQy4ERo6vgxkM1o5BPh"
export const issuerUrl = "http:/issuer:9000"

export const magentaIssuerDid = "did:sov:8eQhKkZMjKXbLwaBNEKRu3"
export const magentaIssuerUrl = "http:/magenta_issuer:TODO"

export const c1Did = "did:sov:V9fvKQjtmbsoJb7gLm2Fka"
export const c1Url = "http:/test/c1"
export const c1Port = "5000"

export const dlgDid = "did:sov:8eQhKkZMjKXbLwaBNEKRu3"
export const dlgUrl = "http:/test/dlg"
export const dlgPort = "8000"

export const defaultNetwork = "TUB Network"

export const staticEntities = [
    {
        name: "AP",
        serverName: "AP",
        did: apDid,
        network: defaultNetwork,
        url: apUrl,
        roles: "Smart Router",
        visible: false,
        displayName: "Network Service Controller",
        displayNameShort: "NSC",
    },
    {
        name: "DLG",
        serverName: "DLG",
        did: dlgDid,
        network: defaultNetwork,
        url: dlgUrl,
        roles: "Ledger Access",
        visible: false,
        displayName: "Distributed Ledger Gateway",
        displayNameShort: "DLG",
    },
    {
        name: "CUSTOMER",
        serverName: "CUSTOMER",
        did: c1Did,
        network: defaultNetwork,
        url: c1Url,
        roles: "Service Consumer",
        visible: false,
        displayName: "Customer",
        displayNameShort: "Customer",
    },
    {
        name: "qr generator",
        serverName: "PROVIDER",
        did: providerDid,
        network: defaultNetwork,
        url: providerUrl,
        roles: "Service Producer",
        visible: false,
        displayName: "QR GENERATOR",
        displayNameShort: "QR Generator",
    },
]

export const entityNameMapping = {
    "CUSTOMER": "CUSTOMER",
    "CLIENT": "CUSTOMER",
    "PROVIDER": "QR GENERATOR",
    "AP": "AP",
    "DLG": "DLG",
}

export function toDate(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleString();
}