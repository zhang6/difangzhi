import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'materials', name: 'Materials', component: () => import('../views/Materials.vue') },
      { path: 'catalog', name: 'Catalog', component: () => import('../views/Catalog.vue') },
      { path: 'entries', name: 'Entries', component: () => import('../views/Entries.vue') },
      { path: 'entries/:id', name: 'EntryEdit', component: () => import('../views/EntryEdit.vue') },
      { path: 'events', name: 'Events', component: () => import('../views/Events.vue') },
      { path: 'knowledge', name: 'Knowledge', component: () => import('../views/Knowledge.vue') },
      { path: 'statistics', name: 'Statistics', component: () => import('../views/Statistics.vue') },
    ],
  },
]

const router = createRouter({ history: createWebHistory(import.meta.env.BASE_URL), routes })

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.meta.public || auth.token) {
    next()
  } else {
    next({ path: '/login', query: { redirect: to.fullPath } })
  }
})

export default router
