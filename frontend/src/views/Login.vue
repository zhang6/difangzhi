<template>
  <div class="min-h-screen flex">
    <!-- Left decorative panel - hidden on mobile -->
    <div class="hidden lg:flex lg:w-1/2 relative overflow-hidden bg-gradient-to-br from-blue-600 to-primary">
      <!-- Abstract pattern overlay -->
      <div class="absolute inset-0 opacity-30">
        <svg class="absolute w-full h-full" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <pattern id="dots" x="0" y="0" width="40" height="40" patternUnits="userSpaceOnUse">
              <circle cx="20" cy="20" r="1.5" fill="white" fill-opacity="0.6" />
            </pattern>
            <pattern id="circles" x="0" y="0" width="120" height="120" patternUnits="userSpaceOnUse">
              <circle cx="60" cy="60" r="40" fill="none" stroke="white" stroke-width="0.5" stroke-opacity="0.2" />
              <circle cx="60" cy="60" r="25" fill="none" stroke="white" stroke-width="0.5" stroke-opacity="0.15" />
            </pattern>
          </defs>
          <rect width="100%" height="100%" fill="url(#dots)" />
          <rect width="100%" height="100%" fill="url(#circles)" />
        </svg>
      </div>
      <!-- Floating shapes -->
      <div class="absolute top-1/4 left-1/4 w-32 h-32 rounded-full bg-white/10 blur-2xl" />
      <div class="absolute bottom-1/3 right-1/4 w-48 h-48 rounded-full bg-white/5 blur-3xl" />
      <div class="absolute top-1/2 right-1/3 w-24 h-24 rounded-full border border-white/20" />
      <div class="absolute bottom-1/4 left-1/3 w-16 h-16 rounded-full border border-white/15" />

      <div class="relative z-10 flex flex-col justify-center px-16 xl:px-24">
        <h1 class="text-4xl xl:text-5xl font-bold text-white tracking-tight mb-4">
          智能年鉴编纂系统
        </h1>
        <p class="text-lg xl:text-xl text-white/90 max-w-md leading-relaxed">
          助力地方志年鉴编纂工作线上化、智能化
        </p>
        <div class="mt-12 flex gap-4">
          <div class="flex items-center gap-2 text-white/80">
            <span class="material-symbols-outlined text-2xl">edit_document</span>
            <span class="text-sm font-medium">智能编纂</span>
          </div>
          <div class="flex items-center gap-2 text-white/80">
            <span class="material-symbols-outlined text-2xl">folder_special</span>
            <span class="text-sm font-medium">资料库</span>
          </div>
          <div class="flex items-center gap-2 text-white/80">
            <span class="material-symbols-outlined text-2xl">groups</span>
            <span class="text-sm font-medium">协同统稿</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Right form panel -->
    <div class="flex-1 flex flex-col min-h-screen bg-slate-50">
      <div class="flex-1 flex items-center justify-center p-6 lg:p-12">
        <div class="w-full max-w-md">
          <!-- Mobile header -->
          <div class="lg:hidden text-center mb-8">
            <h1 class="text-2xl font-bold text-primary">智能年鉴编纂系统</h1>
            <p class="text-slate-500 text-sm mt-1">助力地方志年鉴编纂工作线上化、智能化</p>
          </div>

          <div class="bg-white rounded-2xl shadow-xl shadow-slate-200/50 border border-slate-100 p-8 lg:p-10">
            <h2 class="text-2xl font-bold text-slate-900 mb-1">欢迎登录</h2>
            <p class="text-slate-500 text-sm mb-8">请输入您的账号信息</p>

            <form @submit.prevent="handleLogin" class="space-y-5">
              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1.5">用户名</label>
                <div class="relative">
                  <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-xl">person</span>
                  <input
                    v-model="username"
                    type="text"
                    placeholder="请输入用户名"
                    class="w-full pl-12 pr-4 py-3.5 rounded-xl border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all text-slate-900 placeholder:text-slate-400"
                    :disabled="loading"
                    autocomplete="username"
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1.5">密码</label>
                <div class="relative">
                  <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-xl">lock</span>
                  <input
                    v-model="password"
                    :type="showPassword ? 'text' : 'password'"
                    placeholder="请输入密码"
                    class="w-full pl-12 pr-12 py-3.5 rounded-xl border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all text-slate-900 placeholder:text-slate-400"
                    :disabled="loading"
                    autocomplete="current-password"
                  />
                  <button
                    type="button"
                    @click="showPassword = !showPassword"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors p-1 rounded-lg hover:bg-slate-100"
                    tabindex="-1"
                  >
                    <span class="material-symbols-outlined text-xl">{{ showPassword ? 'visibility_off' : 'visibility' }}</span>
                  </button>
                </div>
              </div>

              <div class="flex items-center">
                <input
                  id="remember"
                  v-model="rememberMe"
                  type="checkbox"
                  class="w-4 h-4 rounded border-slate-300 text-primary focus:ring-primary/20"
                />
                <label for="remember" class="ml-2 text-sm text-slate-600 cursor-pointer">记住我</label>
              </div>

              <div v-if="error" class="flex items-center gap-2 px-4 py-3 rounded-xl bg-red-50 border border-red-100 text-red-600 text-sm">
                <span class="material-symbols-outlined text-lg shrink-0">error</span>
                {{ error }}
              </div>

              <button
                type="submit"
                :disabled="loading"
                class="w-full py-3.5 rounded-xl bg-primary text-white font-semibold shadow-lg shadow-primary/25 hover:shadow-xl hover:shadow-primary/30 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-70 disabled:cursor-not-allowed disabled:hover:scale-100 transition-all duration-200 flex items-center justify-center gap-2"
              >
                <span v-if="loading" class="material-symbols-outlined animate-spin text-xl">progress_activity</span>
                <span>{{ loading ? '登录中...' : '登录' }}</span>
              </button>
            </form>
          </div>
        </div>
      </div>

      <footer class="py-6 text-center text-slate-500 text-sm">
        © 2026 智能年鉴编纂系统 · 数据安全受保护
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const rememberMe = ref(false)
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  error.value = ''
  if (!username.value.trim() || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  try {
    await auth.login(username.value.trim(), password.value)
    const redirect = route.query.redirect || '/yearbooks'
    router.push(redirect)
  } catch (e) {
    error.value = e.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>
