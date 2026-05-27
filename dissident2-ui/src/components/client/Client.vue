<template>
    <!-- <div v-if="true"> -->

    <!-- </div> -->
    <div class="flex-hor my-shadow" style="font-size: var(--my-font-small);">
        <EntityDetails v-bind:entityName="clientName.charAt(0).toUpperCase() + clientName.slice(1)">

            <template #icon>
                <MyIcon :icon="clientName == 'customer' ? 'mdi-cube-outline' : 'mdi-qrcode'" name="" iconColor="var(--c-4)" :boxed="false"
                    size="var(--my-font-big2)">
                </MyIcon>
            </template>

            <div class="iconAndTetx">
                <MyIcon icon="mdi-fingerprint" name="" iconColor="var(--c-4)" :boxed="false"
                    size="var(--my-font-medium)">
                </MyIcon>
                <div>{{ thisStaticEntity["did"] }}</div>
            </div>

            <!-- <div class="iconAndTetx">
                <MyIcon icon="mdi-earth" name="" iconColor="var(--c-4)" :boxed="false" size="var(--my-font-medium)">
                </MyIcon>
                <div>{{ thisStaticEntity["url"] }}</div>
            </div> -->



            <div class="iconAndTetx">

                <Transition>
                    <MyIcon :icon="isAttached ? 'mdi-cloud-check-outline' : 'mdi-cloud-off-outline'" name=""
                        iconColor="var(--c-4)" :boxed="false" size="var(--my-font-medium)"></MyIcon>
                </Transition>

                <Transition>
                    <span v-if="isAttached">Connected</span><span v-else>Not connected</span>
                </Transition>

            </div>
        </EntityDetails>

        <div class="icons-flex">
            <MyIcon v-if="isWaitingAttachemnt" darkShadow icon="spinner" name="" :boxed="true"
                iconColor="var(--c-4)">
            </MyIcon>

            <MyIcon v-else-if="!isAttached" darkShadow :callback="toggleAttachment" icon="mdi-attachment" name="Connect"
                iconColor="var(--c-4)" v-bind:boxed="true">
            </MyIcon>

            <div v-else-if="hasJustCompletedAttachment">

                <v-snackbar v-model="hasJustCompletedAttachment" location="top">
                    Connection succesful

                    <template v-slot:actions>
                        <v-btn color="var(--c-client)" variant="text" @click="hasJustCompletedAttachment = false">
                            Close
                        </v-btn>
                    </template>
                </v-snackbar>
            </div>

            <MyIcon 
                :callback="refreshClient" 
                icon="mdi-refresh" 
                :name="clientName == 'customer' ? 'Service Inquiry' : 'Refresh'" 
                iconColor="var(--c-4)"
                :boxed="true" :darkShadow="true" style="margin-left: 1rem;">
            </MyIcon>
        </div>
    </div>

    <ConsumerViewTable v-if="clientName == 'customer'"></ConsumerViewTable>

    <ProducerViewTable v-if="clientName == 'qr generator'" :isAttached="isAttached"></ProducerViewTable>

</template>

<script>
import EntityDetails from '@/components/EntityDetails.vue';
import { staticEntities } from '@/constants';
import MyIcon from '../MyIcon.vue';
import ConsumerViewTable from './ConsumerViewTable.vue';
import ServiceOutput from './ServiceOutput.vue';
import ProducerViewTable from './ProducerViewTable.vue';
import { useCounterStore } from '@/stores/counterStore';
import { storeToRefs } from 'pinia';

export default {
    setup() {
        const counterStore = useCounterStore();
        const { entities } = storeToRefs(counterStore)
        const { clientsState } = storeToRefs(counterStore)
        const { registeredServices } = storeToRefs(counterStore)
        const { servicefetched } = storeToRefs(counterStore)
        const { fetchEntities, sendAttachmentRequest, initIsAttached, requestConnectionWithAp, serviceInquiry, setJustConsumed, fetchServices } = counterStore

        return {
            entities,
            clientsState,
            fetchEntities,
            sendAttachmentRequest,
            initIsAttached,
            requestConnectionWithAp,
            serviceInquiry,
            servicefetched,
            setJustConsumed,
            fetchServices,
            registeredServices
        }
    },
    // it's fine to mix data and setup stuff
    // be aware: only the properties defined under data are reactive
    data() {
        return {
            hasJustCompletedAttachment: false,
        }
    },
    watch: {
        '$route': {
            immediate: true,
            async handler(to, from) {
                await this.fetchEntities();
                this.initIsAttached(this.clientName.toLowerCase());
                this.hasJustCompletedAttachment = false;
                this.setJustConsumed(false);
            }
        }
    },
    async created() {
        await this.fetchEntities();
        this.initIsAttached(this.clientName);
        this.hasJustCompletedAttachment = false;
    },
    components: {
        EntityDetails,
        MyIcon,
        ConsumerViewTable,
        ServiceOutput,
        ProducerViewTable
    },
    computed: {
        isAttached() {
            const name_server_convention = this.clientName.toLowerCase() == "customer" ? "customer" : "provider"
            return this.clientsState[name_server_convention]['isAttached'];
        },
        isWaitingAttachemnt() {
            const name_server_convention = this.clientName.toLowerCase() == "customer" ? "customer" : "provider"
            return this.clientsState[name_server_convention]['isWaitingAttachment'];
        },
        thisStaticEntity() {
            return staticEntities.filter(e => e["name"].toLowerCase() == this.clientName.toLowerCase())[0];
        }
    },
    props: {
        clientName: {
            type: String,
            required: true
        },
    },
    methods: {
        async toggleAttachment(e) {
            let resp = await this.sendAttachmentRequest(this.clientName, this.thisStaticEntity["did"]);
            console.log("response from attachemnt", resp);
            this.hasJustCompletedAttachment = true;
            await this.requestConnectionWithAp(this.clientName, resp);
        },
        async refreshClient(e) {
            if(this.clientName == 'customer'){
                await this.serviceInquiry();
            } else {
                await this.fetchServices();
            }
        }
    }
}
</script>


<style scoped>
.flex-hor {
    display: flex;
    justify-content: space-between;
    max-width: var(--max-width);
}

.icons-flex {
    align-items: flex-start;
    display: flex;
}

.flex-row {
    display: flex;
    align-items: center;
    z-index: 1;
}
</style>

<!-- NOT SCOPED STYLES -->
<style>
.v-overlay__content .v-overlay__content {
    max-width: 80% !important;
}
</style>

