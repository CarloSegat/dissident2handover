import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/components/home/Home.vue'
import Ap from '@/components/ap/Ap.vue'
import Dlg from '@/components/dlg/Dlg.vue'
import Client from '@/components/client/Client.vue'
import Issuer from '@/components/issuer/Issuer.vue'
import MagentaIssuer from '@/components/issuer/MagentaIssuer.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      redirect: 'home',
    },
    {
      path: '/home',
      name: 'home',
      component: Home,
    },
    {
      path: '/ap',
      name: 'ap',
      component: Ap,
    },
    {
      path: '/dlg',
      name: 'dlg',
      component: Dlg,
    },
    {
      path: '/dt_issuer',
      name: 'dt_issuer',
      component: Issuer,
    },
    {
      path: '/magenta_issuer',
      name: 'magenta_issuer',
      component: MagentaIssuer,
    },
    {
      path: '/customer',
      name: 'CUSTOMER',
      component: Client,
      props: { clientName: "customer" }
    },
    {
      path: '/provider',
      name: 'PROVIDER',
      component: Client,
      props: { clientName: "qr generator" }
    },
  ]
})

export default router
