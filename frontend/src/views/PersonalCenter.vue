<template>
  <!-- 个人中心弹窗 -->
  <div
    v-if="visible"
    class="fixed inset-0 z-50 flex items-center justify-end"
    style="background:rgba(0,0,0,0.3);"
    @click.self="$emit('update:visible', false)"
  >
    <div class="h-full w-full max-w-md bg-white shadow-2xl flex flex-col fade-in">
      <!-- 头部 -->
      <div class="px-6 py-5 border-b border-slate-200 flex items-center justify-between flex-shrink-0">
        <h2 class="text-base font-semibold text-slate-900">个人中心</h2>
        <button @click="$emit('update:visible', false)" class="p-1.5 rounded-lg text-slate-400 hover:bg-slate-100">
          <span class="material-symbols-outlined" style="font-size:20px;">close</span>
        </button>
      </div>

      <!-- 内容区 -->
      <div class="flex-1 overflow-y-auto">
        <!-- 头像与基本信息 -->
        <div class="px-6 py-6 border-b border-slate-100">
          <div class="flex items-center gap-4 mb-5">
            <div
              class="w-16 h-16 rounded-2xl flex items-center justify-center text-white text-2xl font-bold flex-shrink-0"
              :style="{ background: auth.user?.avatar_color || 'linear-gradient(135deg,#1a90ff,#0059b3)' }"
            >{{ auth.userName?.charAt(0) }}</div>
            <div>
              <h3 class="text-base font-semibold text-slate-900">{{ auth.userName }}</h3>
              <span class="badge mt-1" :class="roleBadge(auth.user?.role)">{{ roleLabel(auth.user?.role) }}</span>
            </div>
          </div>

          <div class="space-y-4">
            <div>
              <label class="block text-xs font-medium text-slate-500 mb-1.5">姓名</label>
              <div class="flex items-center gap-2">
                <input
                  v-if="editing"
                  v-model="form.name"
                  type="text"
                  maxlength="10"
                  class="flex-1 px-3 py-2 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                />
                <span v-else class="text-sm text-slate-800 font-medium">{{ auth.userName }}</span>
              </div>
            </div>

            <div>
              <label class="block text-xs font-medium text-slate-500 mb-1.5">手机号</label>
              <input
                v-if="editing"
                v-model="form.phone"
                type="tel"
                placeholder="请输入手机号"
                class="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
              />
              <span v-else class="text-sm text-slate-800">{{ auth.user?.phone || '未设置' }}</span>
            </div>

            <div>
              <label class="block text-xs font-medium text-slate-500 mb-1.5">邮箱</label>
              <input
                v-if="editing"
                v-model="form.email"
                type="email"
                placeholder="请输入邮箱"
                class="w-full px-3 py-2 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
              />
              <span v-else class="text-sm text-slate-800">{{ auth.user?.email || '未设置' }}</span>
            </div>
          </div>

          <div class="flex gap-3 mt-5">
            <template v-if="editing">
              <button @click="editing=false" class="flex-1 py-2 text-sm border border-slate-300 rounded-xl text-slate-600 hover:bg-slate-50">取消</button>
              <button :disabled="saving" class="flex-1 py-2 text-sm text-white rounded-xl disabled:opacity-50" style="background:linear-gradient(135deg,#1a90ff,#0059b3);" @click="saveProfile">
                {{ saving ? '保存中...' : '保存修改' }}
              </button>
            </template>
            <button v-else class="flex-1 py-2 text-sm border border-slate-300 rounded-xl text-slate-600 hover:bg-slate-50" @click="startEdit">编辑信息</button>
          </div>
        </div>

        <!-- 我的任务 -->
        <div class="px-6 py-4">
          <h4 class="text-sm font-semibold text-slate-800 mb-3 flex items-center gap-2">
            <span class="material-symbols-outlined text-blue-500" style="font-size:16px;">assignment</span>
            我的任务 {{ myTasks.length > 0 ? `(${myTasks.length})` : '' }}
          </h4>

          <div v-if="loadingTasks" class="text-center py-4 text-slate-400 text-xs">加载中...</div>
          <div v-else-if="myTasks.length === 0" class="text-center py-8 text-slate-400 text-sm">暂无分配给您的任务</div>
          <div v-else class="space-y-2">
            <div
              v-for="task in myTasks"
              :key="task.id"
              class="border border-slate-200 rounded-xl p-3 cursor-pointer hover:border-blue-300 hover:bg-blue-50 transition-colors"
              @click="goToTask(task)"
            >
              <div class="flex items-start justify-between gap-2 mb-1.5">
                <span class="text-xs font-medium text-slate-800 flex-1 leading-snug">{{ task.title }}</span>
                <span class="badge text-[10px] flex-shrink-0" :class="statusBadge(task.status)">{{ statusLabel(task.status) }}</span>
              </div>
              <div class="flex items-center gap-3 text-[11px] text-slate-400">
                <span class="flex items-center gap-1">
                  <span class="material-symbols-outlined" style="font-size:11px;">corporate_fare</span>
                  {{ task.unit_name || '无供稿单位' }}
                </span>
                <span v-if="task.created_at" class="flex items-center gap-1">
                  <span class="material-symbols-outlined" style="font-size:11px;">calendar_today</span>
                  {{ formatDate(task.created_at) }}
                </span>
              </div>
              <div class="mt-1.5 flex items-center gap-1 text-[11px] text-primary">
                <span class="material-symbols-outlined" style="font-size:11px;">auto_fix_high</span>
                点击进入智能编纂
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部退出 -->
      <div class="px-6 py-4 border-t border-slate-100 flex-shrink-0">
        <button
          class="w-full py-2.5 text-sm text-red-500 border border-red-200 rounded-xl hover:bg-red-50 transition-colors flex items-center justify-center gap-2"
          @click="handleLogout"
        >
          <span class="material-symbols-outlined" style="font-size:16px;">logout</span>
          退出登录
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useYearbookStore } from '@/stores/yearbook'
import { fetchMyTasks } from '@/api/outline'
import type { OutlineNode } from '@/types'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits(['update:visible'])

const router = useRouter()
const auth = useAuthStore()
const yearbookStore = useYearbookStore()

const editing = ref(false)
const saving = ref(false)
const loadingTasks = ref(false)
const myTasks = ref<OutlineNode[]>([])
const form = reactive({ name: '', phone: '', email: '' })

function startEdit() {
  form.name = auth.user?.name || ''
  form.phone = auth.user?.phone || ''
  form.email = auth.user?.email || ''
  editing.value = true
}

async function saveProfile() {
  if (!form.name.trim()) { ElMessage.warning('姓名不能为空'); return }
  saving.value = true
  try {
    await auth.updateProfile({ name: form.name, phone: form.phone, email: form.email })
    ElMessage.success('保存成功')
    editing.value = false
  } catch (e: any) { ElMessage.error(e.message || '保存失败') }
  finally { saving.value = false }
}

async function loadTasks() {
  if (!auth.user?.id) return
  loadingTasks.value = true
  try {
    myTasks.value = await fetchMyTasks(auth.user.id)
  } catch {}
  finally { loadingTasks.value = false }
}

function goToTask(task: OutlineNode) {
  if (task.yearbook_id) yearbookStore.setCurrentYearbook(task.yearbook_id, '')
  yearbookStore.setCurrentOutline(task.id, task.title)
  emit('update:visible', false)
  router.push('/compile')
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
      confirmButtonText: '退出', cancelButtonText: '取消', type: 'warning',
    })
    auth.logout()
    router.push('/login')
  } catch {}
}

watch(() => props.visible, (val) => {
  if (val) { editing.value = false; loadTasks() }
})

function roleLabel(r?: string) {
  return { admin: '管理员', manager: '负责人', editor: '编辑' }[r || ''] || '编辑'
}
function roleBadge(r?: string) {
  return { admin: 'badge-red', manager: 'badge-purple', editor: 'badge-blue' }[r || ''] || 'badge-blue'
}
function statusLabel(s: string) {
  return { not_started: '未开始', in_progress: '编纂中', submitted: '已提交', editing: '编辑中' }[s] || s
}
function statusBadge(s: string) {
  return { not_started: 'badge-gray', in_progress: 'badge-yellow', submitted: 'badge-green', editing: 'badge-blue' }[s] || 'badge-gray'
}
function formatDate(d: string) { return d ? d.substring(0, 10) : '' }
</script>
