<template>
  <el-container class="main-layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">地方志智能编修平台</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#1a1a2e"
        text-color="#eee"
        active-text-color="#4fc3f7"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/materials">
          <el-icon><Folder /></el-icon>
          <span>资料管理</span>
        </el-menu-item>
        <el-menu-item index="/catalog">
          <el-icon><Menu /></el-icon>
          <span>志书纲目</span>
        </el-menu-item>
        <el-menu-item index="/entries">
          <el-icon><Edit /></el-icon>
          <span>条目编写</span>
        </el-menu-item>
        <el-menu-item index="/events">
          <el-icon><Calendar /></el-icon>
          <span>大事记</span>
        </el-menu-item>
        <el-menu-item index="/knowledge">
          <el-icon><Search /></el-icon>
          <span>知识库</span>
        </el-menu-item>
        <el-menu-item index="/statistics">
          <el-icon><PieChart /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">{{ pageTitle }}</span>
        <el-dropdown @command="handleCommand">
          <span class="user">
            <el-icon><User /></el-icon>
            {{ auth.user?.username || '用户' }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { DataAnalysis, Folder, Menu, Edit, Calendar, Search, PieChart, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const pageTitle = computed(() => {
  const map = {
    '/dashboard': '工作台',
    '/materials': '资料管理',
    '/catalog': '志书纲目',
    '/entries': '条目编写',
    '/events': '大事记',
    '/knowledge': '知识库',
    '/statistics': '数据统计',
  }
  return map[route.path] || '地方志智能编修平台'
})

function handleCommand(cmd) {
  if (cmd === 'logout') {
    auth.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.main-layout { height: 100vh; }
.sidebar {
  background: #1a1a2e;
  color: #eee;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-weight: bold;
  color: #4fc3f7;
  font-size: 14px;
  padding: 0 10px;
}
.header {
  background: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.title { font-size: 18px; font-weight: 500; }
.user { cursor: pointer; display: flex; align-items: center; gap: 6px; }
.content { background: #f5f7fa; padding: 20px; overflow: auto; }
</style>
