<template>
    <v-expansion-panels class="homeViewTable my-shadow" v-model="panel">
        <v-expansion-panel>
            <v-expansion-panel-title color="var(--c-1)" class="collapsableSectionTitle">RESOLUTION
                HISTORY</v-expansion-panel-title>
            <v-expansion-panel-text class="swag">
                <v-table class="swag" style="font-size: var(--my-font-small);">
                    <thead>
                        <tr class="swag">
                            <th>
                                REQUESTER ROLE
                            </th>
                            <th>
                                TARGET ROLE
                            </th>
                            <th>
                                TARGET DID
                            </th>

                            <th>
                                NETWORK
                            </th>
                            <th>
                                TIMESTAMP
                            </th>
                            <th>
                                RESOLVED DID DOCUMENT
                            </th>
                        </tr>
                    </thead>
                    <tbody class="swag">
                        <tr v-if="resolutions.length == 0" >
                            <th colspan="6" style="text-align: center; opacity: 25%;">
                                NO DID RESOLUTONS HAVE OCCURRED
                            </th>
                        </tr>
                        <tr v-for="item in resolutions" :key="item.timestamp" class="my-table-row">
                            <td>{{ this.staticEntities.filter(a => a['serverName'] ==
        item.requester_role)[0]["displayName"] }}</td>
                            <td>{{ this.staticEntities.filter(a => a['did'] == item.target_did)[0]["displayName"] }}
                            </td>
                            <td>{{ item.target_did }}</td>
                            <td>{{ "TUB Network" }}</td>
                            <td>{{ toDate(item.timestamp) }}</td>
                            <td>
                                <JsonViewer :json="item.did_document"></JsonViewer>
                            </td>
                        </tr>
                    </tbody>
                </v-table>
            </v-expansion-panel-text>
        </v-expansion-panel>
    </v-expansion-panels>
</template>

<script>
import { staticEntities, toDate } from '@/constants';
import { useCounterStore } from '@/stores/counterStore';
import MyIcon from '@/components/MyIcon.vue';
import JsonViewer from '@/components/JsonViewer.vue';
import { storeToRefs } from 'pinia'

export default {
    setup() {
        const counterStore = useCounterStore();
        const { resolutions } = storeToRefs(counterStore)
        // the increment action can just be destructured
        const { fetchResolutions } = counterStore
        return { resolutions, fetchResolutions, staticEntities, toDate }
    },
    data() {
        return {
            panel: [0],
        }
    },
    mounted() {
        this.fetchResolutions();
    },
    components: { MyIcon, JsonViewer },
}
</script>

<style>
.homeViewTable {
    max-width: var(--max-width);
    height: fit-content;
}

.didDocLink {
    text-decoration: underline !important;
    color: blue !important;
}
</style>