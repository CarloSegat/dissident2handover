<template>
  <div class="left">
    <div class="leftin">

      <!-- <hr class="line"> -->

      <RouterLink v-bind:class="{ 'selected': isSelected('home') }" to="/" class="clear ok">
        <MyIcon icon="mdi-home-outline" name="HOME" iconColor="var(--my-white)" :boxed="false"
          :selected="isSelected('home')"></MyIcon>
      </RouterLink>

      <hr class="line">

      <!-- <h3 class="entities">Entities</h3> -->

      <RouterLink v-bind:class="{ 'selected': isSelected('dt_issuer') }" to="/dt_issuer" class="clear ok">
        <MyIcon icon="mdi-certificate-outline" name="DT IDM" iconColor="var(--my-white)" :boxed="false"
          :selected="isSelected('dt_issuer')" role="issuer">
        </MyIcon>
      </RouterLink>

      <RouterLink v-bind:class="{ 'selected': isSelected('magenta_issuer') }" to="/magenta_issuer" class="clear ok">
        <MyIcon icon="mdi-certificate-outline" name="MAGENTA IDM" iconColor="var(--my-white)" :boxed="false"
          :selected="isSelected('magenta_issuer')" role="issuer">
        </MyIcon>
      </RouterLink>

      <RouterLink v-bind:class="{ 'selected': isSelected('ap') }" to="/ap" class="clear ok">
        <MyIcon icon="mdi-wifi-strength-outline" name="NSC" iconColor="var(--my-white)" :boxed="false"
          :selected="isSelected('ap')" role="smart router"></MyIcon>
      </RouterLink>

      <RouterLink v-bind:class="{ 'selected': isSelected('dlg') }" to="/dlg" class="clear ok">
        <MyIcon icon="mdi-cube-outline" name="DLG" iconColor="var(--my-white)" :boxed="false"
          :selected="isSelected('dlg')" role="ledger access">
        </MyIcon>
      </RouterLink>

      <RouterLink v-bind:class="{ 'selected': isSelected('customer') }" to="/customer" class="clear ok">
        <MyIcon icon="mdi-star-box-outline" :name="entityNameMapping['CUSTOMER']" iconColor="var(--my-white)"
          :boxed="false" :selected="isSelected('customer')" role="service consumer"></MyIcon>
      </RouterLink>

      <RouterLink v-bind:class="{ 'selected': isSelected('provider') }" to="/provider" class="clear ok">
        <MyIcon icon="mdi-square-outline" :name="entityNameMapping['PROVIDER']" iconColor="var(--my-white)"
          :boxed="false" :selected="isSelected('provider')" role="service producer"></MyIcon>
      </RouterLink>

    </div>
    <div class="bottomAuthors">
      <div style="font-size: var(--my-font-small); font-weight: 500">
        CONTACTS
      </div>

      <div class="author">
        <a href="mailto:sandro.rodriguezgarzon@tu-berlin.de">Sandro Rodriguez Garzon</a>
      </div>
      <div class="author">
        <a href="mailto:hai.dinhtuan@tu-berlin.de">Hai Dinh Tuan</a>
      </div>
      <div class="author">
        <a href="mailto:carlo.segat@tu-berlin.de">Carlo Segat</a>
      </div>

      <div style="font-size: var(--my-font-small); margin-top: 0.5rem; font-weight: 500">
        VERSION
      </div>

      <div class="author">
        0.1.5
      </div>

    </div>

  </div>
</template>

<script>
import { entityNameMapping } from "@/constants"
import MyIcon from "@/components/MyIcon.vue";

export default {
  data() {
    return {
      activeRoute: "",
      entityNameMapping
    }
  },
  watch: {
    '$route': {
      immediate: true,
      async handler(to, from) {
        const active = to.path.slice(1);
        console.log("activeeeee", active);
        this.activeRoute = active;
        console.log(this.activeRoute);
      }
    }
  },
  methods: {
    isSelected(name) {
      return this.activeRoute.includes(name)
    }
  },
  components: {
    MyIcon
  }
}

</script>

<style scoped>
.left {
  background-color: var(--c-4);
  /* width: 15rem; */
  padding: var(--padding-m);
  display: flex;
  flex-direction: column;
  gap: 1rem;
  /* width: fit-content; */
  justify-content: space-between;
}

.leftin {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  position: sticky;
  top: 1rem;
  /* width: fit-content; */
}

.nav {
  display: flex;
  flex-direction: column;
}

.clear {
  text-decoration: none;
  /* no underline */
  text-color: black;
  /* all: unset; */
}

.clear:visited {
  text-decoration: none;
  color: inherit;
  all: unset;
}

.button {
  width: 1rem;
  height: 1rem;
  background-color: rgb(255, 255, 255);
  padding: 0px;
  overflow: hidden;
}

.entities {
  color: var(--my-white);
  font-weight: 600;
  font-size: var(--my-font-big);
}

.cute {
  margin-top: auto;
}

body,
html {
  height: 100%;
  margin: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f0f0;
}

@font-face {
  font-family: "lcd";
  src: url('src/assets/fonts/Linebeam.ttf');
}

.ok {
  -webkit-font-smoothing: antialiased;
  letter-spacing: -.05rem;
  /* cursor: pointer; */
  /* display: flex; */
  /* align-items: center; */
  /* white-space: nowrap; */
}

.ok2 {
  font-family: var(--bs-body-font-family);
  -webkit-font-smoothing: antialiased;
  color: #111;
  display: flex !important;
  box-shadow: 0 .25rem .375rem -.0625rem rgba(20, 20, 20, .12), 0 .125rem .25rem -.0625rem rgba(20, 20, 20, .07) !important;
  justify-content: center !important;
  align-items: center !important;
  margin-right: .5rem !important;
  background-color: #fff !important;
  width: 2.5rem;
  height: 2.5rem;
  border-radius: .5rem;
}

.ok3 {
  font-size: var(--my-font-medium) !important;
  color: var(--my-white);
}

.author {
  font-size: var(--my-font-small);
  overflow-wrap: break-word;
  font-weight: 300;
}

.bottomAuthors {
  align-items: flex-end;
  position: sticky;
  bottom: 1rem;
  color: var(--my-white);
}

.selected {
  /* background-color: var(--c-2); */
  color: var(--c-3);
  /* transform: scale(1.33) translate(12dopx); */
  /* font-weight: 900 */
}
</style>