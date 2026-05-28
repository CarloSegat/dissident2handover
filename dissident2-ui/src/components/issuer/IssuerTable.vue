<template>
  <v-expansion-panels class="homeViewTable my-shadow" v-model="panel">
    <v-expansion-panel>
      <v-expansion-panel-title color="var(--c-1)" class="collapsableSectionTitle">ISSUED CREDENTIALS</v-expansion-panel-title>
      <v-expansion-panel-text class="swag">
        <v-table class="swag" style="font-size: var(--my-font-small);">
          <thead>
            <tr>
              <th>
                NAME
              </th>
              <th>
                TYPE
              </th>
              <th>
                RECIPIENT NAME
              </th>
              <th>
                RECIPIENT DID
              </th>
              <th>
                ISSUANCE TIMESTAMP
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="issuances.filter(i => i['src_entity'] == issuerName).length == 0" >
              <th colspan="6" style="text-align: center; opacity: 25%;">
                NO ISSUANCES TO DISPLAY
              </th>
            </tr>

            <tr v-for="item in issuances.filter(i => i['src_entity'] == issuerName)" :key="item.timestamp" class="my-table-row">
             <td>{{ this.issuerName.slice(0, -4) }} </td>
             <td>{{ "AnonCreds v1.0" }}</td>
             <td>{{ this.staticEntities.filter(a => a['serverName'] == item['trg_entity'])[0]["displayName"] }}</td>
             <td>{{ this.staticEntities.filter(a => a['serverName'] == item['trg_entity'])[0]["did"] }}</td>
             <td>{{ toDate(item.timestamp) }}</td>
            </tr>
          </tbody>
        </v-table>
      </v-expansion-panel-text>
    </v-expansion-panel>
  </v-expansion-panels>
</template>
  
<script>
import { staticEntities, entityNameMapping, toDate } from '@/constants';
import { useCounterStore } from '@/stores/counterStore';
import MyIcon from '@/components/MyIcon.vue';
import { storeToRefs } from 'pinia'

export default {
  setup() {
    const counterStore = useCounterStore();
    const { entities } = storeToRefs(counterStore)
    const { issuances } = storeToRefs(counterStore)
    // the increment action can just be destructured
    const { fetchEntities } = counterStore
    const { fetchIssuances } = counterStore
    return { entities, fetchEntities, issuances, fetchIssuances, toDate }
  },
  data() {
    return {
      loading: false,
      error: null,
      staticEntities: staticEntities,
      panel: [0],
      mapp: {
        "AP": "mdi-access-point",
        "DLG": "mdi-cube-outline",
        "CLIENT": "mdi-star-box-outline",
        "PROVIDER": "mdi-star-box-outline",
      },
      entityNameMapping,
    }
  },
  components: {MyIcon},
  created() {
    // watch the params of the route to fetch the data again
    this.$watch(
      () => this.$route.params,
      () => {
        this.fetchEntities();
      },
      // fetch the data when the view is created and the data is
      // already being observed
      { immediate: true }
    )
  },
  async mounted(){
    await this.fetchIssuances();
    console.log(this.issuances.filter(i => i['src_entity'] == this.issuerName));
  },
  props: {
    issuerName: {
      type: String,
      required: true
    }
  }
}
</script>
  
  
<style scoped>
.homeViewTable {
  max-width: var(--max-width);
  height: fit-content;
}

.nameIcon {
  display: flex;
  align-items: flex-end;
}
</style>