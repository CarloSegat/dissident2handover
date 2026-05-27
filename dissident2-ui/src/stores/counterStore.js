import { defineStore } from 'pinia';
import { ui_backend_ip_port, ip } from '@/constants'

export const useCounterStore = defineStore('counter', {
  state: () => ({
    entities: [],
    mermaidString: "",
    clientsState: {
      customer: {
        isAttached: false,
        isWaitingAttachment: false,
        hasJustConsumed: false,
      },
      provider: {
        isAttached: false,
        isWaitingAttachment: false,
      }
    },
    servicefetched: [],
    justAddedService: [],
    registeredServices: [], // not necessarily discovered yet
    consumptions: [],
    resolutions: [],
    issuances: [],
    events: [],
  }),
  actions: {
    async fetchEvents() {
      try {
        const response = await fetch(`http://${ui_backend_ip_port}/events`);

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        this.events = await response.json();

        // console.log('Fetched Entites:', this.entities);

      } catch (error) {
        console.error('Error fetching data:', error);
      }
    },
    async fetchEntities() {
      try {
        const response = await fetch(`http://${ui_backend_ip_port}/entities`);

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        this.entities = await response.json();

        // console.log('Fetched Entites:', this.entities);

      } catch (error) {
        console.error('Error fetching data:', error);
      }
    },
    async fetchMermaidSequenceDiagram() {
      try {
        const response = await fetch(`http://${ui_backend_ip_port}/mermaid`);

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const newMermeid = await response.json();
        if (newMermeid != this.mermaidString) {
          console.log("setting the mermeid stirng in store");
          this.mermaidString = newMermeid;
        } else {
          console.log("NOT setting the mermeid stirng in store");
        }

        // console.log('Fetched mermaid:', this.mermaidString);

      } catch (error) {
        console.error('Error fetching data:', error);
      }
    },
    async sendAttachmentRequest(name, did) {
      const name_server_convention = name.toLowerCase() == "customer" ? "customer" : "provider"

      this.clientsState[name_server_convention]["isWaitingAttachment"] = true;

      let data = await fetch(`http://${ip}/send-attachment-${name_server_convention.toLowerCase()}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json' // Set the Content-Type header
          },
          body: JSON.stringify(
            {
              role: name_server_convention,
              did: did,
            }
          )
        })

      data = await data.json();

      this.clientsState[name_server_convention]["isWaitingAttachment"] = false;

      if (data["succesful"]) {
        this.clientsState[name_server_convention]["isAttached"] = true;
      }

      return data;

    },
    async requestConnectionWithAp(name, attachemntResponse) {

      console.log("attachemntResponse >>> " + attachemntResponse);

      const name_server_convention = name.toLowerCase() == "customer" ? "customer" : "provider"

      const response = await fetch(`http://${ip}/process-response-${name_server_convention.toLowerCase()}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(attachemntResponse)
      });

      if (!response.ok) {
        throw new Error('Response from backend was not ok');
      }

    },
    initIsAttached(name) {
      const name_server_convention = name.toLowerCase() == "customer" ? "customer" : "provider"

      const thisEntity = this.entities.filter(e => e["name"].toLowerCase() == name_server_convention)
      // console.log("thisEntity ", thisEntity);
      if (thisEntity.length > 0) {
        this.clientsState[name_server_convention]["isAttached"] = thisEntity[0]['attached']
      }
    },
    async serviceInquiry() {
      let data = await fetch(`http://${ip}/service-inquiry`,
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        }
      )
      // {
      //   "serviceType": "serviceTypeTest",
      //   "serviceId": "1",
      //   "serviceName": "serviceNameTest",
      //   "provider": "provider",
      //   "endpoint": "http://provider.com:6666"
      // }
      data = await data.json();
      console.log("data", data);
      this.servicefetched = [...data];
    },
    async registerService(name, type, url, producer) {

      const newService = {
        name: name,
        type: type,
        url: url,
        id: Math.floor(Math.random() * 2 ** 16),
        producer,
      }

      let data = await fetch(`http://${ip}/service-registration`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json' // Set the Content-Type header
          },
          body: JSON.stringify(
            newService
          )
        })

      data = await data.json();

      this.justAddedService = [newService, ...this.justAddedService]

      return newService;

    },
    async consumeService(serviceId) {

      let data = await fetch(`http://${ip}/consume-service/${serviceId}`,
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json' // Set the Content-Type header
          }
        })

      data = await data.text();

      console.log("consume service got a response:", data);

      this.consumptions = [
        {
          id: serviceId,
          timestamp: new Date().toISOString(),
          name: data.serviceName,
          producer: data.provider,
        },
        ...this.consumptions
      ]

      this.clientsState['customer']['hasJustConsumed'] = true;

      return "ok"
    },
    async fetchResolutions() {
      try {
        const response = await fetch(`http://${ui_backend_ip_port}/did-resolutions`);

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        this.resolutions = await response.json();

        console.log(this.resolutions);

        // console.log('Fetched Entites:', this.entities);

      } catch (error) {
        console.error('Error fetching data:', error);
      }
    },
    async fetchIssuances() {
      try {
        const response = await fetch(`http://${ui_backend_ip_port}/issuances`);

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        this.issuances = await response.json();

        console.log("this.issuances", this.issuances);

      } catch (error) {
        console.error('Error fetching data:', error);
      }
    },
    setJustConsumed(val) {
      this.clientsState['customer']['hasJustConsumed'] = val;
    },
    async fetchServices() {
      try {
        const response = await fetch(`http://${ui_backend_ip_port}/services`);

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const res = await response.json();
        console.log("fetchServices", res);

        this.registeredServices = [...res];

        // console.log('Fetched mermaid:', this.mermaidString);

      } catch (error) {
        console.error('Error fetching data:', error);
      }
    },
  },
});