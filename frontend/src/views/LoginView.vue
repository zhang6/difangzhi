<template>
  <div class="login-container">
    <div class="login-left">
      <div class="login-brand">
        <el-icon :size="48" color="#fff"><Reading /></el-icon>
        <h1>智能年鉴编纂系统</h1>
        <p>结合大模型与NLP技术，实现年鉴编纂工作线上化管理</p>
      </div>
      <div class="login-features">
        <div class="feature-item">
          <el-icon :size="20"><Document /></el-icon>
          <span>年鉴管理与大纲编排</span>
        </div>
        <div class="feature-item">
          <el-icon :size="20"><MagicStick /></el-icon>
          <span>AI智能编纂与润色</span>
        </div>
        <div class="feature-item">
          <el-icon :size="20"><EditPen /></el-icon>
          <span>多人协作统稿</span>
        </div>
      </div>
    </div>
    <div class="login-right">
      <div class="login-form-wrapper">
        <h2>用户登录</h2>
        <p class="login-subtitle">欢迎使用智能年鉴编纂系统</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              size="large"
              placeholder="请输入用户名"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              size="large"
              type="password"
              placeholder="请输入密码"
              show-password
              :prefix-icon="Lock"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              style="width: 100%"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="login-hint">
          <el-text type="info" size="small">
            演示账号: admin / admin123 &nbsp;|&nbsp; editor1 / 123456
          </el-text>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Reading, Document, MagicStick, EditPen } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await auth.login(form.username, form.password)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/yearbooks'
    router.push(redirect)
  } catch (e: any) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 50%, #2563a0 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px;
  color: #fff;
}

.login-brand h1 {
  font-size: 32px;
  margin: 20px 0 12px;
  font-weight: 700;
}

.login-brand p {
  font-size: 16px;
  opacity: 0.85;
  max-width: 360px;
  text-align: center;
  line-height: 1.6;
}

.login-features {
  margin-top: 48px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  opacity: 0.9;
}

.login-right {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}

.login-form-wrapper {
  width: 400px;
  padding: 48px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.login-form-wrapper h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px;
}

.login-subtitle {
  color: #909399;
  margin: 0 0 32px;
  font-size: 14px;
}

.login-hint {
  text-align: center;
  margin-top: 16px;
}
</style>
