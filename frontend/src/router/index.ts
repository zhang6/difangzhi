import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      component: () => import('@/layouts/AppLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/yearbooks' },
        {
          path: 'yearbooks',
          name: 'YearbookManage',
          component: () => import('@/views/YearbookManage.vue'),
          meta: { title: '年鉴管理' },
        },
        {
          path: 'outlines',
          name: 'OutlineManage',
          component: () => import('@/views/OutlineManage.vue'),
          meta: { title: '大纲管理' },
        },
        {
          path: 'resources',
          name: 'ResourceLibrary',
          component: () => import('@/views/ResourceLibrary.vue'),
          meta: { title: '资料库' },
        },
        {
          path: 'resources/:folderId',
          name: 'ResourceFiles',
          component: () => import('@/views/ResourceFiles.vue'),
          meta: { title: '资料文件' },
        },
        {
          path: 'compile',
          name: 'SmartCompile',
          component: () => import('@/views/SmartCompile.vue'),
          meta: { title: '智能编纂' },
        },
        {
          path: 'proofread',
          name: 'ProofreadView',
          component: () => import('@/views/ProofreadView.vue'),
          meta: { title: '统稿' },
        },
        {
          path: 'profile',
          name: 'PersonalCenter',
          component: () => import('@/views/PersonalCenter.vue'),
          meta: { title: '个人中心' },
        },
      ],
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  if (to.meta.public || auth.isLoggedIn) {
    next()
  } else {
    next({ path: '/login', query: { redirect: to.fullPath } })
  }
})

export default router
