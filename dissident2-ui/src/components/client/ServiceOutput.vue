<template>
    <v-expansion-panels class="homeViewTable my-shadow swag" v-model="panel">
        <v-expansion-panel class="swag">
            <v-expansion-panel-title color="var(--c-1)" class="swag collapsableSectionTitle">SERVICE CONSUMPTION  HISTORY</v-expansion-panel-title>
            <v-expansion-panel-text class="swag">
                <div>
                    TBD
                </div>
            </v-expansion-panel-text>
        </v-expansion-panel>
    </v-expansion-panels>
</template>

<script>
// import { staticEntities } from '@/constants';

export default {
    data() {
        return {
            loading: false,
            panel: [1],
            consumptions: [],
        }
    },
    mounted() {
        this.fetchConsumptions();
    },
    methods: {
        async fetchConsumptions() {
            const response = await fetch(`http://${ui_backend_ip_port}/events`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            let temp = await response.json();
            this.consumptions = temp.filter(e => e['sub_group'] == "ServiceUsageResponseReceived");
        }
    }
}
</script>
  
<style scoped></style>