import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

// icons
import 'material-design-icons-iconfont/dist/material-design-icons.css'

const vuetify = createVuetify({
  components,
  directives,
})

const pinia = createPinia()

createApp(App).use(vuetify).use(router).use(pinia).mount('#app')
