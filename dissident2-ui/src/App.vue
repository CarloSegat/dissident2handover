<script setup>
import { onMounted } from 'vue';
import LeftBar from './layoutcomponents/LeftBar.vue';
import RightBar from './layoutcomponents/RightBar.vue';
import Top from './layoutcomponents/Top.vue';
import { useCounterStore } from '@/stores/counterStore';
import { storeToRefs } from 'pinia';

const counterStore = useCounterStore();

onMounted(() => {
  counterStore.fetchEntities();
});
</script>

<template>
  <div class="outmost">
    <Top class="gridTop"></Top>
    <LeftBar class="gridLeft"></LeftBar>
    <RightBar ></RightBar> <!-- class="gridRight" -->
  </div>
</template>

<style scoped>
.outmost {
  width: 100vw;
  height: 100vh;
  display: grid;
  grid-template-columns: 14.5rem auto;
  grid-template-rows: 7rem auto;
}

.gridTop {
  grid-column-start: 1;
  grid-column-end: 3;
  /* grid-row-start: 1;
  grid-row-end: 2; */
}

.gridLeft {
  grid-column-start: 1;
  grid-column-end: 2;
}

/* .gridRight {
  grid-column-start: 2;
  grid-column-end: 3;
} */
</style>
