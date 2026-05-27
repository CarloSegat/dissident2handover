<template>
  <v-expansion-panels class="homeViewTable my-shadow" v-model="panel">
    <v-expansion-panel>
      <v-expansion-panel-title color="var(--c-1)" class="collapsableSectionTitle">SERVICE REPOSITORY
        </v-expansion-panel-title>
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
          <tbody>
            <tr v-if="this.registeredServices.length == 0" style="font-size: var(--my-font-small);">
                            <th colspan="6" style="text-align: center; opacity: 25%; font-size: var(--my-font-small);">NO SERVICES YET AVAILABLE</th>
                        </tr>
            <tr v-for="service in this.registeredServices" :key="service.name" class="my-table-row">
              <td>{{ service.name }}</td>
              <td>{{ service.type }}</td>
              <td>{{ service.url }}</td>
              <td>{{ staticEntities.filter(se => se.did == service.producer)[0].displayName }}</td>
              <td>{{ staticEntities.filter(se => se.did == service.producer)[0].did }}</td>
              <td>{{ "TUB Network" }}</td>
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

export default {
      setup() {
        const counterStore = useCounterStore();
        const { entities } = storeToRefs(counterStore)
        // the increment action can just be destructured
        const { fetchEntities } = counterStore
        const { fetchServices } = counterStore
        const { registeredServices } = storeToRefs(counterStore)
        // the increment action can just be destructured
        return {
            entities,
            fetchEntities,
            registeredServices,
            fetchServices,
        }
      },
      data() {
        return {
          loading: false,
          error: null,
          staticEntities: staticEntities,
          panel: [0],
          entityNameMapping,
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
      mounted(){
        this.fetchServices();
      },
      methods: {}
    }
</script>
  
  
<style scoped>
.homeViewTable {
  max-width: var(--max-width);
}
</style>