<template>
  <v-expansion-panels class="homeViewTable my-shadow" v-model="panel" style="font-size: var(--my-font-small);">
    <v-expansion-panel>
      <v-expansion-panel-title color="var(--c-1)" class="collapsableSectionTitle">CONNECTED NETWORK ELEMENTS</v-expansion-panel-title>
      <v-expansion-panel-text class="swag">
        <v-table class="swag" style="font-size: var(--my-font-small);">
          <thead>
            <tr>
              <th>
                ELEMENTS
              </th>
              <th>
                DID
              </th>
              <th>
                NETWORK
              </th>
      
            </tr>
          </thead>
          <tbody style="font-size: var(--my-font-small);">
            <tr v-if="entities.filter(e => e['attached']).length == 0" >
              <th colspan="6" style="text-align: center; opacity: 25%;">
                NO ENTITIES YET CONNECTED
              </th>
            </tr>
           
            <tr v-for="item in entities.filter(e => e['attached'])" :key="item.name" class="my-table-row">
              <td>
                <div class="nameIcon">
                  <MyIcon :icon="mapp[item.name]" name="" iconColor="var(--c-1)" :boxed="false"></MyIcon>
                  {{ entityNameMapping[item.name] }}
                </div>
              </td>
              <td>{{ staticEntities.filter(e => e.serverName == item.name)[0].did }}</td>
              <td>{{ staticEntities.filter(e => e.serverName == item.name)[0].network }}</td>
            </tr>
          </tbody>
        </v-table>
      </v-expansion-panel-text>
    </v-expansion-panel>
  </v-expansion-panels>
</template>
  
<script>
import { staticEntities, entityNameMapping } from '@/constants';
import { useCounterStore } from '@/stores/counterStore';
import MyIcon from '@/components/MyIcon.vue';
import { storeToRefs } from 'pinia'

export default {
  setup() {
    const counterStore = useCounterStore();
    const { entities } = storeToRefs(counterStore)
    // the increment action can just be destructured
    const { fetchEntities } = counterStore
    return { entities, fetchEntities }
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
        "CUSTOMER": "mdi-star-box-outline",
        "PROVIDER": "mdi-qrcode",
      },
      entityNameMapping,
    }
  },
  components: {MyIcon},
  created() {
    // TODO move in refresh
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
  methods: {
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