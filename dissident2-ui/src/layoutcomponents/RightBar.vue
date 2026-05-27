<script setup>
import MyIcon from '../components/MyIcon.vue';
</script>

<template>
  <div class="right">
    <RouterView/>
  </div>
</template>

<style scoped>
.right {
  background: var(--my-white);
  /* flex-basis: 80%; */
  width: fit-content;
  width: 100%;
  padding: var(--padding-l);
  /* overflow-y: scroll; */
  display: flex;
  flex-direction: column;
  color: black;
}

.wave-container {
    /* background-image: url("@/assets/a-normal.png"); */
    background-size: 70rem 70rem;
    background-position: right;
    right: 0rem;
    background-repeat: no-repeat;
}

@media (max-width: 1200px) {
  .wave-container {
    background-image: none;
  }
}
</style>