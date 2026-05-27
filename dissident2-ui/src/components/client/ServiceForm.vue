<template>
    <v-form>
            <v-container style="margin-left: 0rem !important; font-size: var(--my-font-small);">
                <v-row  justify="space-between">
                    <v-col cols="12" md="3">
                        <v-text-field v-model="serviceName" label="Service Name" required persistent-placeholder density="compact" style="font-size: var(--my-font-small);">
                        </v-text-field>
                    </v-col>

                    <!-- Text field for Service URL -->
                    <v-col cols="12" md="3">
                        <v-text-field v-model="serviceUrl" label="Service URL" persistent-placeholder required density="compact" style="font-size: var(--my-font-small);">
                        </v-text-field>
                    </v-col>

                    <!-- Text field for Service Type -->
                    <v-col cols="12" md="3">
                        <v-text-field v-model="serviceType" label="Service Type" persistent-placeholder required density="compact" style="font-size: var(--my-font-small);">
                        </v-text-field>
                    </v-col>

                    <!-- Button for Adding Service -->
                    <v-col cols="12" md="3">
                        <v-btn color="var(--c-client)" variant="elevated" class="text-white" block @click="myRegisterService" style="font-size: var(--my-font-small);">
                            Add Service
                        </v-btn>
                    </v-col>
                </v-row>
            </v-container>
    </v-form>
</template>



<script>
import { useCounterStore } from '@/stores/counterStore';
import { storeToRefs } from 'pinia'
import {providerDid} from '@/constants'

export default {
    data() {
        return {
            serviceName: "",
            serviceType: "",
            serviceUrl: "",
        }
    },
    setup() {
        const counterStore = useCounterStore();
        const { entities } = storeToRefs(counterStore);
        // the increment action can just be destructured
        const { fetchEntities, registerService } = counterStore;
        return { entities, fetchEntities, registerService };
    },
    methods: {
        async myRegisterService() {
            let response = await this.registerService(
                this.serviceName, 
                this.serviceType,
                this.serviceUrl, 
                providerDid
            );
        }
    },
}
</script>
<style scoped>
.bordered-container {
    /* background-color: var(--c-client); */
    /* border-left: 0.2rem solid var(--c-client); */
    /* border-right: 0.25rem solid var(--c-client); */
    /* border-radius: 0.5rem; */
    /* Example: 1px solid black border */
    /* padding-left: 0.5rem; */
    /* Optional: Adds some spacing inside the container */
    margin: 20px 0;
    /* Optional: Adds some spacing outside the container */
    display: flex;

}

.field {
    width: 10rem;
}

.pullup {
    margin-top: -1rem;
}
</style>