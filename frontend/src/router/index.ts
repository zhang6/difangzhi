import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
    },
    {
      path: '/',
      component: () => import('@/layouts/AppLayout.vue'),
      redirect: '/yearbooks',
      children: [
        {
          path: '/yearbooks',
          name: 'yearbooks',
          component: () => import('@/views/YearbookManage.vue'),
        },
        {
          path: '/outlines',
          name: 'outlines',
          component: () => import('@/views/OutlineManage.vue'),
        },
        {
          path: '/resources',
          name: 'resources',
          component: () => import('@/views/ResourceLibrary.vue'),
        },
        {
          path: '/resources/:folderId',
          name: 'resource-files',
          component: () => import('@/views/ResourceFiles.vue'),
        },
        {
          path: '/compile',
          name: 'compile',
          component: () => import('@/views/SmartCompile.vue'),
        },
        {
          path: '/proofread',
          name: 'proofread',
          component: () => import('@/views/ProofreadView.vue'),
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.isLoggedIn) {
    return '/login'
  }
  if (to.path === '/login' && auth.isLoggedIn) {
    return '/'
  }
})

export default router
