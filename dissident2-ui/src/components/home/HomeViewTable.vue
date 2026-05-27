<template>
  <v-expansion-panels class="homeViewTable my-shadow swag" v-model="panel">
    <v-expansion-panel class="swag">
      <v-expansion-panel-title color="var(--c-1)" class="swag collapsableSectionTitle">NETWORK ELEMENTS
      </v-expansion-panel-title>
      <v-expansion-panel-text class="swag">
        <v-table class="swag my-table-row" style="font-size: var(--my-font-small);">
          <thead>
            <tr>
              <th>
                ELEMENT
              </th>
              <th>
                DID
              </th>
              <th>
                NETWORK
              </th>
              <th>
                ROLE
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="se in staticEntities" :key="se.name">
              <td>
                <div class="nameIcon">
                  <MyIcon :icon="mapp[se.name]" name="" iconColor="var(--c-1)" :boxed="false"></MyIcon>
                  {{ se["displayNameShort"] }}
                </div>
              </td>
              <td>{{ se.did }}</td>
              <td>{{ se.name == "AP" || se.name == "DLG" ? "TUB Network" : entities.filter(e => e["attached"] && e["name"] == se.serverName).length == 1 ? "TUB Network": "N/A" }}</td>
              <td>{{ se.roles }}</td>

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
import { storeToRefs } from 'pinia'
import MyIcon from '@/components/MyIcon.vue';

export default {
  setup() {
    const counterStore = useCounterStore();
    const { entities } = storeToRefs(counterStore)
    // the increment action can just be destructured
    const { fetchEntities } = counterStore
    return { entities, fetchEntities, entityNameMapping }
  },
  data() {
    return {
      loading: false,
      entityStatuses: [],
      error: null,
      staticEntities: staticEntities,
      panel: [0],
      mapp: {
        "AP": "mdi-access-point",
        "DLG": "mdi-cube-outline",
        "CUSTOMER": "mdi-star-box-outline",
        "qr generator": "mdi-qrcode",
      }
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
  methods: {},
  components: {
    MyIcon
  }
}
</script>


<style scoped>
.homeViewTable {
  max-width: var(--max-width);
}

.nameIcon {
  display: flex;
  align-items: top;
}
</style>