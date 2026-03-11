<template>
  <div class="flex h-screen flex-col">
    <!-- Header -->
    <header class="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-3 sticky top-0 z-50">
      <div class="flex items-center gap-3 text-primary shrink-0">
        <div class="size-9 flex items-center justify-center">
          <svg class="w-full h-full" fill="none" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
            <path d="M42.4379 44C42.4379 44 36.0744 33.9038 41.1692 24C46.8624 12.9336 42.2078 4 42.2078 4L7.01134 4C7.01134 4 11.6577 12.932 5.96912 23.9969C0.876273 33.9029 7.27094 44 7.27094 44L42.4379 44Z" fill="currentColor"/>
          </svg>
        </div>
        <h2 class="text-slate-900 text-xl font-bold leading-tight tracking-[-0.015em]">智能年鉴编纂系统</h2>
      </div>

      <!-- Center nav -->
      <nav class="hidden md:flex flex-1 justify-center items-center gap-1">
        <router-link
          v-for="nav in navItems"
          :key="nav.path"
          :to="nav.path"
          class="relative px-4 py-2 text-sm font-medium rounded-lg transition-colors duration-200"
          :class="isActive(nav.path)
            ? 'text-primary'
            : 'text-slate-600 hover:text-primary hover:bg-slate-50'"
        >
          {{ nav.label }}
          <span
            v-if="isActive(nav.path)"
            class="absolute bottom-0 left-1/2 -translate-x-1/2 w-8 h-0.5 bg-primary rounded-full"
          />
        </router-link>
      </nav>

      <!-- Right actions -->
      <div class="flex items-center gap-2 shrink-0">
        <button class="relative p-2 rounded-lg hover:bg-slate-100 transition-colors">
          <span class="material-symbols-outlined text-slate-500 hover:text-primary">notifications</span>
          <span class="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-red-500" />
        </button>

        <div class="relative" ref="userMenuRef">
          <button
            @click="showUserMenu = !showUserMenu"
            class="flex items-center gap-2 hover:bg-slate-50 rounded-xl px-3 py-2 transition-colors"
          >
            <span class="text-sm font-medium text-slate-700 hidden sm:inline">{{ auth.userName }}</span>
            <div
              class="size-9 rounded-full flex items-center justify-center text-white font-bold text-sm shrink-0"
              :style="{ background: auth.user?.avatar_color || '#1a90ff' }"
            >
              {{ auth.userInitial }}
            </div>
          </button>

          <Transition
            enter-active-class="transition-all duration-200 ease-out"
            enter-from-class="opacity-0 translate-y-2"
            enter-to-class="opacity-100 translate-y-0"
            leave-active-class="transition-all duration-150 ease-in"
            leave-from-class="opacity-100 translate-y-0"
            leave-to-class="opacity-0 translate-y-2"
          >
            <div
              v-if="showUserMenu"
              class="absolute right-0 top-full mt-2 w-52 bg-white border border-slate-200 rounded-xl shadow-xl z-50 py-1.5 overflow-hidden"
            >
              <button
                @click="openPersonalCenter"
                class="w-full text-left px-4 py-2.5 text-sm hover:bg-slate-50 flex items-center gap-3 text-slate-700 transition-colors"
              >
                <span class="material-symbols-outlined text-xl text-slate-500">person</span>
                个人中心
              </button>
              <button
                @click="handleLogout"
                class="w-full text-left px-4 py-2.5 text-sm hover:bg-red-50 flex items-center gap-3 text-red-600 transition-colors"
              >
                <span class="material-symbols-outlined text-xl">logout</span>
                退出登录
              </button>
            </div>
          </Transition>
        </div>
      </div>
    </header>

    <!-- Main content with route transition -->
    <main class="flex-1 overflow-auto">
      <router-view v-slot="{ Component }">
        <Transition
          name="fade-slide"
          mode="out-in"
        >
          <component :is="Component" />
        </Transition>
      </router-view>
    </main>

    <!-- Toast container -->
    <div class="fixed top-4 right-4 z-[100] flex flex-col gap-2 pointer-events-none">
      <TransitionGroup name="toast">
        <div
          v-for="t in toasts"
          :key="t.id"
          class="pointer-events-auto flex items-center gap-3 px-4 py-3 rounded-xl shadow-lg border min-w-[280px] max-w-sm"
          :class="toastClasses(t.type)"
        >
          <span class="material-symbols-outlined text-xl shrink-0">{{ toastIcon(t.type) }}</span>
          <span class="text-sm font-medium flex-1">{{ t.message }}</span>
        </div>
      </TransitionGroup>
    </div>

    <PersonalCenter v-if="showPersonal" @close="showPersonal = false" />
  </div>
</template>

<script setup>
import { ref, provide, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import PersonalCenter from '@/components/PersonalCenter.vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const showUserMenu = ref(false)
const showPersonal = ref(false)
const userMenuRef = ref(null)

const toasts = ref([])
let toastId = 0

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

function showToast(message, type = 'info') {
  const id = ++toastId
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }, 3000)
}

function toastClasses(type) {
  const map = {
    success: 'bg-green-50 border-green-200 text-green-800',
    error: 'bg-red-50 border-red-200 text-red-800',
    info: 'bg-primary/5 border-primary/30 text-primary',
  }
  return map[type] || map.info
}

function toastIcon(type) {
  const map = { success: 'check_circle', error: 'error', info: 'info' }
  return map[type] || 'info'
}

provide('toast', showToast)

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

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.toast-enter-active {
  transition: all 0.3s ease-out;
}
.toast-leave-active {
  transition: all 0.25s ease-in;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style>
