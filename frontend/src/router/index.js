import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/yearbooks' },
      { path: 'yearbooks', name: 'Yearbooks', component: () => import('@/views/YearbookHome.vue') },
      { path: 'outlines', name: 'Outlines', component: () => import('@/views/OutlineManage.vue') },
      { path: 'materials', name: 'Materials', component: () => import('@/views/MaterialLibrary.vue') },
      { path: 'materials/:folderId', name: 'MaterialFiles', component: () => import('@/views/MaterialFiles.vue') },
      { path: 'compile', name: 'SmartCompile', component: () => import('@/views/SmartCompile.vue') },
      { path: 'compile/entries', name: 'EntryManage', component: () => import('@/views/EntryManage.vue') },
      { path: 'proofread', name: 'Proofread', component: () => import('@/views/Proofread.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.meta.public || auth.isLoggedIn) {
    next()
  } else {
    next({ path: '/login', query: { redirect: to.fullPath } })
  }
})

export default router
