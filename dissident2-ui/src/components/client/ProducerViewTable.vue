<template>
    <img src="../../assets/achitecture_diagram.png" class="diagramHai">
    
    <v-expansion-panels class="homeViewTable my-shadow swag" v-model="panel">
        <v-expansion-panel class="swag">
            <v-expansion-panel-title color="var(--c-1)" class="swag collapsableSectionTitle">SERVICE OFFERED
                </v-expansion-panel-title>
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
                                USAGE
                            </th>
                            <th>
                                STATUS
                            </th>
                            <!-- <th>
                                ACTION
                            </th> -->
                        </tr>
                    </thead>
                    <tbody class="swag">
                        <tr v-if="this.allServicesComputed.length == 0" >
                            <th colspan="6" style="text-align: center; opacity: 25%;">NO SERVICES YET AVAILABLE</th>
                        </tr>
                        <tr v-for="service in this.allServicesComputed" :key="service.name">
                            <td>{{ service.name }}</td>
                            <td>{{ service.type }}</td>
                            <td>{{ service.url }}</td>
                            <td>Service used {{ this.isServiceUsed() }} time</td>
                            <td>registered</td>
                            <!-- <td>
                                <div class="actions-container">
                                    <v-btn v-if="service.status == 'draft'" variant="elevated" density="compact"
                                    color="var(--c-client)" class="text-white action-service" @click="hasJustCompletedAttachment = false">
                                    Register
                                </v-btn>

                                <v-btn v-if="service.status == 'registered'" color="var(--c-client)" variant="elevated"
                                    class="text-white action-service" density="compact" @click="hasJustCompletedAttachment = false">
                                    Deregister
                                </v-btn>

                                <v-btn color="var(--c-0)" class="text-white action-service" variant="elevated" density="compact"
                                    @click="hasJustCompletedAttachment = false">
                                    Delete
                                </v-btn>
                                </div>
                            </td> -->
                        </tr>
                    </tbody>
                </v-table>

            </v-expansion-panel-text>
        </v-expansion-panel>
    </v-expansion-panels>

    <br>

    <v-expansion-panels class="homeViewTable my-shadow swag" v-model="panel">
        <v-expansion-panel class="swag">
            <v-expansion-panel-title color="var(--c-1)" class="swag collapsableSectionTitle">OFFER NEW SERVICE
                </v-expansion-panel-title>
            <v-expansion-panel-text class="swag">
                <br>
                <ServiceForm v-if="this.isAttached"></ServiceForm>
            </v-expansion-panel-text>
        </v-expansion-panel>
    </v-expansion-panels>


</template>

<script>
import { staticEntities } from '@/constants';
import { useCounterStore } from '@/stores/counterStore';
import { storeToRefs } from 'pinia'
import ServiceForm from './ServiceForm.vue';

export default {
    setup() {
        const counterStore = useCounterStore();
        const { entities } = storeToRefs(counterStore);
        const { registeredServices } = storeToRefs(counterStore)
        const { justAddedService } = storeToRefs(counterStore)
        const { events } = storeToRefs(counterStore)
        // the increment action can just be destructured
        const { fetchEntities, registerService, fetchServices, fetchEvents } = counterStore;
        return { entities, fetchEntities, registerService, registeredServices, justAddedService, fetchServices, events, fetchEvents };
    },
    data() {
        return {
            loading: false,
            staticEntities: staticEntities,
            panel: [0],
        };
    },
    created() {
        // watch the params of the route to fetch the data again
        this.fetchEvents();
        this.$watch(() => this.$route.params, () => {
            this.fetchEntities();
        },
            // fetch the data when the view is created and the data is
            // already being observed
            { immediate: true });
    },
    methods: {
        filterUnique(jsonArray) {
            const seen = new Set();
            return jsonArray.filter(obj => {
                const id = obj['name'] + obj['url']
                console.log(id);
                console.log(seen);
                return seen.has(id) ? false : seen.add(id);
            });
        },
        isServiceUsed(){
            console.log(this.events);
            return Math.max(this.events.filter(e => e["sub_group"] == "ServiceUsageResponseReceived").length - 1, 0);
        }
    },
    components: { ServiceForm },
    computed: {
        allServicesComputed(){
            console.log([...this.registeredServices, ...this.justAddedService]);
            // return []
            return this.filterUnique([...this.registeredServices, ...this.justAddedService])
        }
    },
    mounted(){
        this.fetchServices();
    },
    props: {
        isAttached: {
            type: Boolean
        }
    }
}
</script>
  
<style scoped>

.action-service {
    width: 7rem;
}

.actions-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.25rem;

    padding: 1rem;
}

</style>