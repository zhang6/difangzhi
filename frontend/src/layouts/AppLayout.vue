<template>
  <div class="flex h-screen flex-col">
    <header class="flex items-center justify-between whitespace-nowrap border-b border-slate-200 bg-white px-6 py-3 sticky top-0 z-50">
      <div class="flex items-center gap-4 text-primary">
        <div class="size-8 flex items-center justify-center">
          <svg fill="none" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
            <path d="M42.4379 44C42.4379 44 36.0744 33.9038 41.1692 24C46.8624 12.9336 42.2078 4 42.2078 4L7.01134 4C7.01134 4 11.6577 12.932 5.96912 23.9969C0.876273 33.9029 7.27094 44 7.27094 44L42.4379 44Z" fill="currentColor"/>
          </svg>
        </div>
        <h2 class="text-slate-900 text-xl font-bold leading-tight tracking-[-0.015em]">智能年鉴编纂系统</h2>
      </div>
      <div class="flex flex-1 justify-end gap-8">
        <nav class="flex items-center gap-9">
          <router-link
            v-for="nav in navItems" :key="nav.path" :to="nav.path"
            :class="[isActive(nav.path)
              ? 'text-primary font-semibold border-b-2 border-primary pb-1'
              : 'text-slate-600 font-medium hover:text-primary transition-colors']"
            class="text-sm leading-normal"
          >{{ nav.label }}</router-link>
        </nav>
        <div class="flex items-center gap-4">
          <span class="material-symbols-outlined text-slate-500 cursor-pointer hover:text-primary transition-colors">notifications</span>
          <div class="relative" ref="userMenuRef">
            <button @click="showUserMenu = !showUserMenu" class="flex items-center gap-2 hover:bg-slate-50 rounded-lg px-2 py-1 transition-colors">
              <span class="text-sm font-medium text-slate-700">{{ auth.userName }}</span>
              <div class="size-9 rounded-full flex items-center justify-center text-white font-bold text-sm" :style="{ background: auth.user?.avatar_color || '#1a90ff' }">
                {{ auth.userInitial }}
              </div>
            </button>
            <div v-if="showUserMenu" class="absolute right-0 top-full mt-2 w-48 bg-white border border-slate-200 rounded-lg shadow-xl z-50 py-1">
              <button @click="openPersonalCenter" class="w-full text-left px-4 py-2 text-sm hover:bg-slate-50 flex items-center gap-2">
                <span class="material-symbols-outlined text-lg">person</span>个人中心
              </button>
              <button @click="handleLogout" class="w-full text-left px-4 py-2 text-sm hover:bg-slate-50 text-red-500 flex items-center gap-2">
                <span class="material-symbols-outlined text-lg">logout</span>退出登录
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>
    <main class="flex-1 overflow-auto">
      <router-view />
    </main>
    <PersonalCenter v-if="showPersonal" @close="showPersonal = false" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import PersonalCenter from '@/components/PersonalCenter.vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const showUserMenu = ref(false)
const showPersonal = ref(false)
const userMenuRef = ref(null)

const navItems = [
  { label: '年鉴管理', path: '/yearbooks' },
  { label: '大纲管理', path: '/outlines' },
  { label: '资料库', path: '/materials' },
  { label: '智能编纂', path: '/compile' },
  { label: '统稿', path: '/proofread' },
]

function isActive(path) {
  return route.path.startsWith(path)
}

function openPersonalCenter() {
  showUserMenu.value = false
  showPersonal.value = true
}

function handleLogout() {
  auth.logout()
  router.push('/login')
}

function handleClickOutside(e) {
  if (userMenuRef.value && !userMenuRef.value.contains(e.target)) {
    showUserMenu.value = false
  }
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>
