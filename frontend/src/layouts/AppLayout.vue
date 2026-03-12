<template>
  <div class="h-screen flex flex-col overflow-hidden bg-slate-50">
    <!-- 顶部导航栏 -->
    <header class="flex-shrink-0 flex items-center justify-between border-b border-slate-200 bg-white px-5 z-50" style="height:60px;">
      <!-- Logo -->
      <div class="flex items-center gap-3" style="min-width:210px;">
        <div class="w-8 h-8 rounded-lg flex items-center justify-center" style="background:linear-gradient(135deg,#1a90ff,#0059b3);">
          <span class="material-symbols-outlined text-white" style="font-size:18px;">auto_stories</span>
        </div>
        <span class="text-sm font-bold text-slate-900 whitespace-nowrap">智能年鉴编纂系统</span>
      </div>

      <!-- 模块导航 -->
      <nav class="flex items-center h-full gap-1">
        <button
          v-for="mod in modules"
          :key="mod.key"
          class="nav-tab"
          :class="{ active: activeModule === mod.key }"
          @click="switchModule(mod.key)"
        >
          <span class="material-symbols-outlined" style="font-size:16px;">{{ mod.icon }}</span>
          {{ mod.label }}
        </button>
      </nav>

      <!-- 右侧用户区 -->
      <div class="flex items-center gap-3">
        <div class="w-px h-5 bg-slate-200"></div>
        <el-dropdown trigger="click" @command="handleUserCommand">
          <button class="flex items-center gap-2 text-sm text-slate-700 hover:text-primary transition-colors rounded-lg px-2 py-1.5 hover:bg-blue-50">
            <div
              class="w-7 h-7 rounded-full flex items-center justify-center text-white text-xs font-bold"
              :style="{ background: auth.user?.avatar_color || 'linear-gradient(135deg,#1a90ff,#0059b3)' }"
            >{{ auth.userName.charAt(0) }}</div>
            <span class="font-medium">{{ auth.userName }}</span>
            <span class="material-symbols-outlined text-slate-400" style="font-size:16px;">expand_more</span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <span class="material-symbols-outlined mr-2" style="font-size:16px;">account_circle</span>个人中心
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <span class="material-symbols-outlined mr-2 text-red-500" style="font-size:16px;">logout</span>
                <span class="text-red-500">退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 主体布局 -->
    <div class="flex flex-1 overflow-hidden">
      <!-- 左侧边栏 -->
      <aside class="flex-shrink-0 flex flex-col overflow-y-auto" style="width:220px; background:#0d1b2e;">
        <!-- 模块标题 -->
        <div class="px-5 py-4 border-b" style="border-color:rgba(255,255,255,0.08);">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-blue-400" style="font-size:16px;">{{ currentModuleData?.icon }}</span>
            <span class="text-xs font-semibold text-blue-300 uppercase tracking-wider">{{ currentModuleData?.label }}</span>
          </div>
        </div>

        <!-- 当前年鉴选择器（部分模块显示） -->
        <div v-if="showYearbookSelector" class="px-4 py-3 border-b" style="border-color:rgba(255,255,255,0.06);">
          <div class="flex items-center gap-2 px-3 py-2 rounded-lg cursor-pointer" style="background:rgba(255,255,255,0.06);">
            <span class="material-symbols-outlined text-slate-400" style="font-size:14px;">book_2</span>
            <span class="text-xs text-slate-300 flex-1 truncate">{{ yearbookStore.currentYearbookName || '请选择年鉴' }}</span>
          </div>
        </div>

        <!-- 菜单列表 -->
        <nav class="flex-1 py-3">
          <div
            v-for="item in currentMenuItems"
            :key="item.path"
            class="sidebar-item"
            :class="{ active: isMenuActive(item) }"
            @click="navigateTo(item)"
          >
            <span class="material-symbols-outlined sidebar-icon">{{ item.icon }}</span>
            <span class="text-sm">{{ item.label }}</span>
          </div>
        </nav>

        <!-- 底部版本 -->
        <div class="px-5 py-3 border-t" style="border-color:rgba(255,255,255,0.06);">
          <p class="text-xs text-slate-600">v1.0.0 · 智能年鉴编纂系统</p>
        </div>
      </aside>

      <!-- 主内容区 -->
      <main class="flex-1 overflow-hidden">
        <router-view />
      </main>
    </div>

    <!-- 个人中心弹窗 -->
    <PersonalCenter v-model:visible="profileVisible" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useYearbookStore } from '@/stores/yearbook'
import PersonalCenter from '@/views/PersonalCenter.vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const yearbookStore = useYearbookStore()
const profileVisible = ref(false)

const modules = [
  { key: 'yearbook', label: '年鉴管理', icon: 'book_2', path: '/yearbooks' },
  { key: 'outline', label: '大纲管理', icon: 'account_tree', path: '/outlines' },
  { key: 'library', label: '资料库', icon: 'database', path: '/resources' },
  { key: 'compile', label: '智能编纂', icon: 'auto_fix_high', path: '/compile' },
  { key: 'review', label: '统稿', icon: 'rate_review', path: '/proofread' },
]

const menuMap: Record<string, { icon: string; label: string; path: string }[]> = {
  yearbook: [
    { icon: 'grid_view', label: '年鉴列表', path: '/yearbooks' },
  ],
  outline: [
    { icon: 'list_alt', label: '大纲列表', path: '/outlines' },
  ],
  library: [
    { icon: 'corporate_fare', label: '资料单位', path: '/resources' },
  ],
  compile: [
    { icon: 'auto_fix_high', label: '编纂主界面', path: '/compile' },
  ],
  review: [
    { icon: 'menu_book', label: '统稿主界面', path: '/proofread' },
  ],
}

const activeModule = ref('yearbook')

const currentModuleData = computed(() => modules.find(m => m.key === activeModule.value))
const currentMenuItems = computed(() => menuMap[activeModule.value] || [])
const showYearbookSelector = computed(() => ['outline', 'compile', 'review'].includes(activeModule.value))

function getModuleFromPath(path: string): string {
  if (path.startsWith('/yearbooks')) return 'yearbook'
  if (path.startsWith('/outlines')) return 'outline'
  if (path.startsWith('/resources')) return 'library'
  if (path.startsWith('/compile')) return 'compile'
  if (path.startsWith('/proofread')) return 'review'
  return 'yearbook'
}

watch(() => route.path, (path) => {
  activeModule.value = getModuleFromPath(path)
}, { immediate: true })

function switchModule(key: string) {
  activeModule.value = key
  const mod = modules.find(m => m.key === key)
  if (mod) router.push(mod.path)
}

function navigateTo(item: { path: string }) {
  router.push(item.path)
}

function isMenuActive(item: { path: string }) {
  return route.path === item.path || route.path.startsWith(item.path + '/')
}

async function handleUserCommand(command: string) {
  if (command === 'profile') {
    profileVisible.value = true
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
        confirmButtonText: '退出',
        cancelButtonText: '取消',
        type: 'warning',
      })
      auth.logout()
      router.push('/login')
    } catch {}
  }
}
</script>
