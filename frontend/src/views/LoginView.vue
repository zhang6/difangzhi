<template>
  <div class="h-screen flex overflow-hidden">
    <!-- 左侧品牌区 -->
    <div class="hidden lg:flex flex-col justify-between p-12 text-white flex-shrink-0" style="width:480px; background:linear-gradient(135deg,#0d1b2e 0%,#1a3a5c 100%);">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl flex items-center justify-center bg-white/10">
          <span class="material-symbols-outlined text-white" style="font-size:22px;">auto_stories</span>
        </div>
        <span class="text-lg font-bold">智能年鉴编纂系统</span>
      </div>

      <div>
        <h1 class="text-4xl font-bold mb-4 leading-tight">
          让年鉴编纂<br/>
          <span style="color:#66b5ff;">更智能、更高效</span>
        </h1>
        <p class="text-slate-300 text-base leading-relaxed mb-8">
          结合大模型与NLP技术，为地方志年鉴编纂工作提供全流程数字化支持，实现年鉴内容的智能撰写与协同管理。
        </p>

        <div class="grid grid-cols-3 gap-4">
          <div class="bg-white/10 rounded-xl p-4 text-center">
            <p class="text-2xl font-bold text-white mb-1">AI</p>
            <p class="text-xs text-slate-300">智能生成</p>
          </div>
          <div class="bg-white/10 rounded-xl p-4 text-center">
            <p class="text-2xl font-bold text-white mb-1">多端</p>
            <p class="text-xs text-slate-300">协同编纂</p>
          </div>
          <div class="bg-white/10 rounded-xl p-4 text-center">
            <p class="text-2xl font-bold text-white mb-1">全程</p>
            <p class="text-xs text-slate-300">流程管理</p>
          </div>
        </div>
      </div>

      <div class="text-slate-500 text-xs">
        © 2024 智能年鉴编纂系统 · All rights reserved
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="flex-1 flex flex-col items-center justify-center p-8 bg-white">
      <div class="w-full max-w-sm">
        <!-- 移动端Logo -->
        <div class="flex items-center gap-2 mb-8 lg:hidden">
          <div class="w-8 h-8 rounded-lg flex items-center justify-center" style="background:linear-gradient(135deg,#1a90ff,#0059b3);">
            <span class="material-symbols-outlined text-white" style="font-size:18px;">auto_stories</span>
          </div>
          <span class="font-bold text-slate-800">智能年鉴编纂系统</span>
        </div>

        <h2 class="text-2xl font-bold text-slate-900 mb-2">欢迎回来</h2>
        <p class="text-slate-500 text-sm mb-8">登录您的账号开始编纂工作</p>

        <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              size="large"
              prefix-icon="User"
              :disabled="loading"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              prefix-icon="Lock"
              show-password
              :disabled="loading"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="w-full mt-2"
            style="background:linear-gradient(135deg,#1a90ff,#0059b3);border:none;height:48px;font-size:15px;"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form>

        <div class="mt-8 p-4 bg-slate-50 rounded-xl border border-slate-200">
          <p class="text-xs font-medium text-slate-600 mb-2">演示账号</p>
          <div class="space-y-1.5">
            <div class="flex items-center justify-between text-xs text-slate-500">
              <span>管理员：admin / admin123</span>
              <button class="text-primary hover:underline" @click="fillDemo('admin', 'admin123')">快速填入</button>
            </div>
            <div class="flex items-center justify-between text-xs text-slate-500">
              <span>编辑：editor1 / 123456</span>
              <button class="text-primary hover:underline" @click="fillDemo('editor1', '123456')">快速填入</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const formRef = ref()

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

function fillDemo(username: string, password: string) {
  form.username = username
  form.password = password
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/yearbooks')
  } catch (e: any) {
    ElMessage.error(e.message || '用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>
