<template>

    <img src="../../assets/achitecture_diagram.png" class="diagramHai">

    <v-snackbar v-model="hasFinishedConsumption" location="top" vertical timeout="99999" style="font-size: var(--my-font-small);">
        <div class="qr_container">
            <div>Service consumed succesfully, find the returned QR code below
            </div>
            <!-- Hannover messe hot-fix -->
            <!-- <img :src="this.qrCode.replace('&amp;', '&')"> -->
            <img src="https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=https://arxiv.org/html/2310.19366v3">

            <div>SCAN TO KNOW MORE!
            </div>
        </div>

        <template v-slot:actions>
            <v-btn color="var(--c-client)" variant="text" @click="this.hasFinishedConsumption = false">
                Close
            </v-btn>
        </template>
    </v-snackbar>


    <v-expansion-panels class="homeViewTable my-shadow swag" v-model="panel">
        <v-expansion-panel class="swag">
            <v-expansion-panel-title color="var(--c-1)" class="swag collapsableSectionTitle">AVAILABLE
                SERVICES</v-expansion-panel-title>
            <v-expansion-panel-text class="swag">
                <v-table class="swag my-table-row" style="font-size: var(--my-font-small);">
                    <thead>
                        <tr class="swag">
                            <th>
                                NAME
                            </th>
                            <th>
                                TYPE
                            </th>
                            <th>
                                URL
                            </th>
                            <th>
                                PRODUCER
                            </th>
                            <th>
                                PRODUCER DID
                            </th>
                            <th>
                                NETWORK
                            </th>
                        </tr>
                    </thead>
                    <tbody class="swag">
                        <tr v-if="serviceFetched.length == 0" >
                            <th colspan="6" style="text-align: center; opacity: 25%;">NO SERVICES YET AVAILABLE</th>
                        </tr>
                        <tr v-for="service in serviceFetched" :key="service.serviceName">
                            <td>{{ service.serviceName }}</td>
                            <td>{{ service.serviceType }}</td>
                            <td>{{ service.endpoint }}</td>
                            <td>{{ service ? staticEntities.filter(se => se.serverName ==
        "PROVIDER")[0].displayName : null }}</td>
                            <td>{{ service.provider }}</td>
                            <td>{{ "TUB Network" }}</td>
                            <td>
                                <v-btn color="var(--c-1)" variant="elevated" class="text-white" style="width: 6rem;" block
                                    @click="consume(service.serviceId)">
                                    <span v-if="! this.waitingServiceConsumption">Consume</span>
                                    <v-progress-circular v-else
                                        indeterminate
                                    ></v-progress-circular>
                                </v-btn>
                            </td>
                        </tr>
                    </tbody>
                </v-table>
            </v-expansion-panel-text>
        </v-expansion-panel>
    </v-expansion-panels>
</template>

<script>
import { staticEntities, ui_backend_ip_port } from '@/constants';
import { useCounterStore } from '@/stores/counterStore';
import MyIcon from '@/components/MyIcon.vue';
import { storeToRefs } from 'pinia'

export default {
    setup() {
        const counterStore = useCounterStore();
        const { entities } = storeToRefs(counterStore)
        const { servicefetched } = storeToRefs(counterStore)
        const { consumptions } = storeToRefs(counterStore)
        const { fetchEntities, consumeService } = counterStore

        return {
            entities,
            fetchEntities,
            servicefetched,
            consumeService,
            consumptions,
        }
    },
    data() {
        return {
            loading: false,
            staticEntities: staticEntities,
            panel: [0],
            waitingServiceConsumption: false,
            qrCode: "",
            hasFinishedConsumption: false,
        }
    },
    created() {
        // watch the params of the route to fetch the data again
        this.$watch(
            () => this.$route.params,
            () => {
                this.fetchEntities()
            },
            // fetch the data when the view is created and the data is
            // already being observed
            { immediate: true }
        )
    },
    methods: {
        async consume(serviceId){
            console.log("consuming service");
            this.waitingServiceConsumption = true;
            await this.consumeService(serviceId);

            while(this.waitingServiceConsumption){
                try {
                    const response = await fetch(`http://${ui_backend_ip_port}/events`);

                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }

                    let temp = await response.json();
                    temp = temp.filter(e => e['sub_group'] == "ServiceUsageResponseReceived")

                    this.waitingServiceConsumption = temp.length == 0;

                    let qrCode = temp.map(e => e['content']['qrCode']);

                    if(qrCode.length > 0) {
                        this.qrCode = qrCode[0];
                        this.hasFinishedConsumption = true;
                    }

                } catch (error) {
                    console.error('Error fetching data:', error);
                }
            }
        }
    },
    computed: {
        serviceFetched() {
            return this.servicefetched;
        }
    }
}
</script>

<style scoped>
.emptyState {
    padding: 2rem;
    /* display: flex; */
    column-span: all;
}

.qr_container{
    display: flex;
    gap: 1rem;
    flex-direction: column;
    align-items: center;
    /* position: relative; */
    left: 3rem;
    padding: 1.5rem;
    padding-bottom: 0rem;
}
</style>