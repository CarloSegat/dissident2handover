<script setup>
import HomeTable from './HomeViewTable.vue';
import SequenceDiagram from './SequenceDiagram.vue';

</script>

<template>
  <div class="containerDiagram">
    <HomeTable></HomeTable>
    <img src="../../assets/achitecture_diagram.png" class="diagramHai">
  </div>
  <br>
  <SequenceDiagram></SequenceDiagram>
</template>

<style scoped>
</style>
