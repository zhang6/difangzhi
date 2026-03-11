<template>
  <el-container class="app-layout">
    <el-header class="app-header">
      <div class="header-left">
        <el-icon :size="24" color="#409eff"><Reading /></el-icon>
        <span class="header-title">智能年鉴编纂系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        mode="horizontal"
        :ellipsis="false"
        class="header-menu"
        router
      >
        <el-menu-item index="/yearbooks">
          <el-icon><Files /></el-icon>
          <span>年鉴管理</span>
        </el-menu-item>
        <el-menu-item index="/outlines">
          <el-icon><List /></el-icon>
          <span>大纲管理</span>
        </el-menu-item>
        <el-menu-item index="/resources">
          <el-icon><FolderOpened /></el-icon>
          <span>资料库</span>
        </el-menu-item>
        <el-menu-item index="/compile">
          <el-icon><MagicStick /></el-icon>
          <span>智能编纂</span>
        </el-menu-item>
        <el-menu-item index="/proofread">
          <el-icon><EditPen /></el-icon>
          <span>统稿</span>
        </el-menu-item>
      </el-menu>
      <div class="header-right">
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-avatar">
            <el-avatar
              :size="32"
              :style="{ backgroundColor: auth.user?.avatar_color || '#409eff' }"
            >
              {{ auth.userName?.charAt(0) || '?' }}
            </el-avatar>
            <span class="user-name">{{ auth.userName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><UserFilled /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Reading, Files, List, FolderOpened, MagicStick, EditPen,
  ArrowDown, UserFilled, SwitchButton,
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/resources')) return '/resources'
  if (path.startsWith('/compile')) return '/compile'
  return path
})

function handleCommand(cmd: string) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(() => {
      auth.logout()
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: #f5f7fa;
}

.app-header {
  display: flex;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 0 24px;
  height: 60px;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 32px;
  flex-shrink: 0;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
}

.header-menu {
  flex: 1;
  border-bottom: none !important;
}

.header-menu .el-menu-item {
  font-size: 14px;
}

.header-right {
  margin-left: auto;
  flex-shrink: 0;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 0;
}

.user-name {
  font-size: 14px;
  color: #606266;
}

.app-main {
  padding: 20px;
  background: #f5f7fa;
}
</style>
