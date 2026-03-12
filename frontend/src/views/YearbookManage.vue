<template>
  <div class="page-container bg-slate-50">
    <!-- 页面头部 -->
    <div class="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between sticky top-0 z-10">
      <div>
        <div class="flex items-center gap-2 text-xs text-slate-400 mb-1">
          <span>首页</span>
          <span class="material-symbols-outlined" style="font-size:12px;">chevron_right</span>
          <span class="text-slate-600 font-medium">年鉴管理</span>
        </div>
        <h1 class="text-lg font-bold text-slate-900">年鉴管理</h1>
      </div>
      <div class="flex items-center gap-3">
        <div class="relative">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" style="font-size:16px;">search</span>
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索年鉴名称..."
            class="pl-9 pr-4 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 w-56"
            @input="handleSearch"
          />
        </div>
        <button
          v-if="auth.isAdmin"
          class="flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-medium text-white transition-all hover:shadow-md"
          style="background:linear-gradient(135deg,#1a90ff,#0059b3);"
          @click="openCreate"
        >
          <span class="material-symbols-outlined" style="font-size:16px;">add</span>
          新建年鉴
        </button>
      </div>
    </div>

    <div class="px-6 py-5">
      <!-- 统计卡片 -->
      <div class="grid grid-cols-4 gap-4 mb-6">
        <div class="bg-white rounded-xl p-5 border border-slate-200 flex items-center gap-4">
          <div class="w-11 h-11 rounded-xl flex items-center justify-center" style="background:#eff6ff;">
            <span class="material-symbols-outlined text-blue-600" style="font-size:22px;">book_2</span>
          </div>
          <div>
            <p class="text-2xl font-bold text-slate-900">{{ stats.total }}</p>
            <p class="text-xs text-slate-500">年鉴总数</p>
          </div>
        </div>
        <div class="bg-white rounded-xl p-5 border border-slate-200 flex items-center gap-4">
          <div class="w-11 h-11 rounded-xl flex items-center justify-center" style="background:#fef3c7;">
            <span class="material-symbols-outlined" style="font-size:22px;color:#d97706;">edit_note</span>
          </div>
          <div>
            <p class="text-2xl font-bold text-slate-900">{{ stats.in_progress }}</p>
            <p class="text-xs text-slate-500">编纂中</p>
          </div>
        </div>
        <div class="bg-white rounded-xl p-5 border border-slate-200 flex items-center gap-4">
          <div class="w-11 h-11 rounded-xl flex items-center justify-center" style="background:#d1fae5;">
            <span class="material-symbols-outlined" style="font-size:22px;color:#059669;">task_alt</span>
          </div>
          <div>
            <p class="text-2xl font-bold text-slate-900">{{ stats.completed }}</p>
            <p class="text-xs text-slate-500">已完成</p>
          </div>
        </div>
        <div class="bg-white rounded-xl p-5 border border-slate-200 flex items-center gap-4">
          <div class="w-11 h-11 rounded-xl flex items-center justify-center" style="background:#f0f9ff;">
            <span class="material-symbols-outlined" style="font-size:22px;color:#0284c7;">pending_actions</span>
          </div>
          <div>
            <p class="text-2xl font-bold text-slate-900">{{ stats.not_started }}</p>
            <p class="text-xs text-slate-500">未开始</p>
          </div>
        </div>
      </div>

      <!-- 状态筛选 -->
      <div class="flex items-center gap-3 mb-5">
        <span class="text-sm text-slate-600 font-medium">状态筛选：</span>
        <button
          v-for="f in statusFilters"
          :key="f.value"
          class="px-3 py-1.5 text-xs rounded-lg font-medium transition-colors"
          :class="statusFilter === f.value
            ? 'bg-blue-600 text-white'
            : 'bg-white border border-slate-300 text-slate-600 hover:border-blue-400 hover:text-blue-600'"
          @click="statusFilter = f.value; loadData()"
        >
          {{ f.label }} {{ f.value === 'all' ? `(${stats.total})` : '' }}
        </button>
      </div>

      <!-- 年鉴卡片网格 -->
      <div v-loading="loading" class="grid gap-5" style="grid-template-columns:repeat(auto-fill,minmax(300px,1fr));">
        <!-- 年鉴卡片 -->
        <div
          v-for="yb in yearbooks"
          :key="yb.id"
          class="bg-white rounded-xl border border-slate-200 p-5 card-hover cursor-pointer group"
          @click="enterYearbook(yb)"
        >
          <div class="flex items-start justify-between mb-3">
            <div class="flex items-center gap-2">
              <span class="text-xs font-bold px-2 py-0.5 rounded text-white" style="background:#0059b3;">
                {{ getYear(yb.start_date) }}
              </span>
              <span class="badge" :class="statusBadge(yb.status)">{{ statusLabel(yb.status) }}</span>
            </div>
            <el-dropdown v-if="auth.isAdmin" trigger="click" @click.stop @command="(cmd: string) => handleCardAction(cmd, yb)">
              <button class="p-1 rounded text-slate-400 hover:bg-slate-100 opacity-0 group-hover:opacity-100 transition-opacity" @click.stop>
                <span class="material-symbols-outlined" style="font-size:18px;">more_horiz</span>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <span class="material-symbols-outlined mr-1" style="font-size:15px;">edit</span>编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="manager">
                    <span class="material-symbols-outlined mr-1" style="font-size:15px;">manage_accounts</span>分配负责人
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <span class="material-symbols-outlined mr-1 text-red-500" style="font-size:15px;">delete</span>
                    <span class="text-red-500">删除</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <h3 class="text-sm font-semibold text-slate-900 mb-1 leading-snug">{{ yb.name }}</h3>
          <p class="text-xs text-slate-400 mb-4">
            负责人：{{ yb._manager_names || '未分配' }} &nbsp;|&nbsp; 创建于 {{ formatDate(yb.created_at) }}
          </p>

          <div class="mb-4">
            <div class="flex justify-between items-center mb-1">
              <span class="text-xs text-slate-500">整体进度</span>
              <span class="text-xs font-semibold" :class="yb.progress === 100 ? 'text-green-600' : 'text-blue-600'">{{ yb.progress }}%</span>
            </div>
            <div class="progress-track">
              <div class="progress-fill" :class="{ green: yb.progress === 100 }" :style="{ width: yb.progress + '%' }"></div>
            </div>
          </div>

          <div class="flex items-center justify-between text-xs text-slate-400 border-t border-slate-100 pt-3">
            <div class="flex items-center gap-3">
              <span class="flex items-center gap-1">
                <span class="material-symbols-outlined" style="font-size:13px;">calendar_today</span>
                {{ yb.start_date }} ~ {{ yb.end_date }}
              </span>
            </div>
            <div class="flex gap-2" @click.stop>
              <template v-if="yb.status === 'in_progress' || yb.status === 'not_started'">
                <button class="text-primary hover:underline text-xs" @click="goToOutline(yb)">大纲</button>
                <button class="text-primary hover:underline text-xs" @click="goToProofread(yb)">统稿</button>
              </template>
              <template v-else>
                <button class="text-primary hover:underline text-xs" @click="goToProofread(yb)">查看</button>
              </template>
            </div>
          </div>
        </div>

        <!-- 新建年鉴占位卡片 -->
        <div
          v-if="auth.isAdmin"
          class="rounded-xl border-2 border-dashed border-blue-200 p-5 flex flex-col items-center justify-center cursor-pointer hover:border-blue-400 hover:bg-blue-50 transition-all"
          style="min-height:196px;"
          @click="openCreate"
        >
          <div class="w-12 h-12 rounded-full flex items-center justify-center mb-3" style="background:#eff6ff;">
            <span class="material-symbols-outlined text-blue-500" style="font-size:24px;">add</span>
          </div>
          <p class="text-sm font-medium text-blue-600">新建年鉴</p>
          <p class="text-xs text-slate-400 mt-1">点击开始创建新年鉴</p>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && yearbooks.length === 0" description="暂无年鉴数据" class="mt-12" />

      <!-- 分页 -->
      <div v-if="total > pageSize" class="flex justify-center mt-8">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </div>

    <!-- 新建/编辑年鉴弹窗 -->
    <div v-if="dialogVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="dialogVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg mx-4 fade-in">
        <div class="flex items-center justify-between px-6 py-4 border-b border-slate-200">
          <h2 class="text-base font-semibold text-slate-900">{{ isEdit ? '编辑年鉴' : '新建年鉴' }}</h2>
          <button @click="dialogVisible=false" class="p-1 rounded-lg text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="px-6 py-5 space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">年鉴名称 <span class="text-red-500">*</span></label>
            <input v-model="form.name" type="text" placeholder="如：江苏年鉴2024"
              class="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"/>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1.5">开始日期 <span class="text-red-500">*</span></label>
              <input v-model="form.start_date" type="date"
                class="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"/>
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1.5">结束日期 <span class="text-red-500">*</span></label>
              <input v-model="form.end_date" type="date"
                class="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"/>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">封面风格</label>
            <div class="flex gap-3">
              <div
                v-for="c in coverOptions"
                :key="c.value"
                class="w-20 h-12 rounded-lg cursor-pointer border-2 transition-all flex items-center justify-center relative"
                :class="form.cover_type === c.value ? 'border-blue-500' : 'border-transparent'"
                :style="{ background: c.bg }"
                @click="form.cover_type = c.value"
              >
                <span v-if="form.cover_type === c.value" class="material-symbols-outlined text-white" style="font-size:18px;">check</span>
              </div>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">状态</label>
            <select v-model="form.status" class="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400">
              <option value="not_started">未开始</option>
              <option value="in_progress">编纂中</option>
              <option value="completed">已完成</option>
            </select>
          </div>
        </div>
        <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-slate-200">
          <button @click="dialogVisible=false" class="px-5 py-2 text-sm text-slate-600 border border-slate-300 rounded-xl hover:bg-slate-50 transition-colors">取消</button>
          <button
            :disabled="submitting"
            class="px-5 py-2 text-sm text-white rounded-xl transition-all hover:shadow-md disabled:opacity-50"
            style="background:linear-gradient(135deg,#1a90ff,#0059b3);"
            @click="handleSubmit"
          >
            {{ submitting ? '保存中...' : (isEdit ? '保存修改' : '确认创建') }}
          </button>
        </div>
      </div>
    </div>

    <!-- 分配负责人弹窗 -->
    <div v-if="managerVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="managerVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 fade-in">
        <div class="flex items-center justify-between px-6 py-4 border-b border-slate-200">
          <h2 class="text-base font-semibold text-slate-900">分配负责人</h2>
          <button @click="managerVisible=false" class="p-1 rounded-lg text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="px-6 py-5 space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">年鉴</label>
            <input :value="selectedYb?.name" disabled class="w-full px-3 py-2.5 border border-slate-200 rounded-xl text-sm bg-slate-50 text-slate-500"/>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">当前负责人</label>
            <div class="flex flex-wrap gap-2 min-h-[36px] p-2 border border-slate-300 rounded-xl">
              <span
                v-for="m in currentManagers"
                :key="m.user_id"
                class="flex items-center gap-1 bg-blue-50 text-blue-700 text-xs px-2 py-1 rounded-lg"
              >
                {{ m.user?.name }}
                <button @click="removeManager(m.user_id)" class="ml-1 hover:text-red-500">×</button>
              </span>
              <span v-if="currentManagers.length === 0" class="text-xs text-slate-400 px-1 py-1">未分配负责人</span>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">添加负责人</label>
            <select v-model="addManagerId" class="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400">
              <option value="">请选择用户</option>
              <option v-for="u in availableUsers" :key="u.id" :value="u.id">{{ u.name }} ({{ u.role }})</option>
            </select>
          </div>
        </div>
        <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-slate-200">
          <button @click="managerVisible=false" class="px-5 py-2 text-sm text-slate-600 border border-slate-300 rounded-xl hover:bg-slate-50">取消</button>
          <button
            :disabled="!addManagerId || submitting"
            class="px-5 py-2 text-sm text-white rounded-xl hover:shadow-md disabled:opacity-50"
            style="background:linear-gradient(135deg,#1a90ff,#0059b3);"
            @click="handleAddManager"
          >确认添加</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useYearbookStore } from '@/stores/yearbook'
import { fetchYearbooks, createYearbook, updateYearbook, deleteYearbook, fetchManagers, addManager, removeManagerApi, fetchStats } from '@/api/yearbook'
import type { Yearbook, UserProfile } from '@/types'

const router = useRouter()
const auth = useAuthStore()
const yearbookStore = useYearbookStore()

const loading = ref(false)
const submitting = ref(false)
const yearbooks = ref<(Yearbook & { _manager_names?: string })[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 12
const keyword = ref('')
const statusFilter = ref('all')
const stats = ref<Record<string, number>>({ total: 0, in_progress: 0, completed: 0, not_started: 0 })

const statusFilters = [
  { value: 'all', label: '全部' },
  { value: 'not_started', label: '未开始' },
  { value: 'in_progress', label: '编纂中' },
  { value: 'completed', label: '已完成' },
]

const coverOptions = [
  { value: 'default_1', bg: 'linear-gradient(135deg,#667eea,#764ba2)' },
  { value: 'default_2', bg: 'linear-gradient(135deg,#f093fb,#f5576c)' },
  { value: 'default_3', bg: 'linear-gradient(135deg,#4facfe,#00f2fe)' },
]

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref('')
const form = reactive({ name: '', start_date: '', end_date: '', cover_type: 'default_1', status: 'not_started' as 'not_started' | 'in_progress' | 'completed' })

const managerVisible = ref(false)
const selectedYb = ref<Yearbook | null>(null)
const currentManagers = ref<any[]>([])
const addManagerId = ref('')
const allUsers = ref<UserProfile[]>([])
const availableUsers = computed(() => allUsers.value.filter(u => !currentManagers.value.some(m => m.user_id === u.id)))

let searchTimer: any
function handleSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { page.value = 1; loadData() }, 300)
}

async function loadData() {
  loading.value = true
  try {
    const [result, statsData] = await Promise.all([
      fetchYearbooks({ page: page.value, pageSize, keyword: keyword.value, status: statusFilter.value }),
      fetchStats(),
    ])
    stats.value = statsData
    const ybs = result.data
    for (const yb of ybs) {
      try {
        const mgrs = await fetchManagers(yb.id)
        ;(yb as any)._manager_names = mgrs.map((m: any) => m.user?.name).filter(Boolean).join('、') || ''
      } catch { (yb as any)._manager_names = '' }
    }
    yearbooks.value = ybs as any
    total.value = result.total
  } catch (e: any) {
    ElMessage.error('加载失败: ' + (e.message || ''))
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  editId.value = ''
  const year = new Date().getFullYear()
  Object.assign(form, {
    name: '', start_date: `${year}-01-01`, end_date: `${year}-12-31`,
    cover_type: 'default_1', status: 'not_started',
  })
  dialogVisible.value = true
}

function openEdit(yb: Yearbook) {
  isEdit.value = true
  editId.value = yb.id
  Object.assign(form, {
    name: yb.name, start_date: yb.start_date, end_date: yb.end_date,
    cover_type: yb.cover_type || 'default_1', status: yb.status,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.name.trim()) { ElMessage.warning('请输入年鉴名称'); return }
  if (!form.start_date || !form.end_date) { ElMessage.warning('请选择日期'); return }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateYearbook(editId.value, { ...form })
      ElMessage.success('修改成功')
    } else {
      await createYearbook({ ...form, progress: 0, created_by: auth.user?.id })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(yb: Yearbook) {
  try {
    await ElMessageBox.confirm(
      `确定要删除《${yb.name}》吗？删除后将无法恢复，所有相关数据都将被清除。`,
      '删除确认', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteYearbook(yb.id)
    ElMessage.success('已删除')
    loadData()
  } catch {}
}

async function openManagerDialog(yb: Yearbook) {
  selectedYb.value = yb
  addManagerId.value = ''
  try {
    [currentManagers.value, allUsers.value] = await Promise.all([
      fetchManagers(yb.id),
      auth.fetchAllUsers(),
    ])
  } catch {}
  managerVisible.value = true
}

async function handleAddManager() {
  if (!selectedYb.value || !addManagerId.value) return
  submitting.value = true
  try {
    await addManager(selectedYb.value.id, addManagerId.value)
    currentManagers.value = await fetchManagers(selectedYb.value.id)
    addManagerId.value = ''
    ElMessage.success('分配成功')
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '分配失败')
  } finally {
    submitting.value = false
  }
}

async function removeManager(userId: string) {
  if (!selectedYb.value) return
  try {
    await removeManagerApi(selectedYb.value.id, userId)
    currentManagers.value = await fetchManagers(selectedYb.value.id)
    ElMessage.success('已移除')
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

function handleCardAction(cmd: string, yb: Yearbook) {
  if (cmd === 'edit') openEdit(yb)
  else if (cmd === 'manager') openManagerDialog(yb)
  else if (cmd === 'delete') handleDelete(yb)
}

function enterYearbook(yb: Yearbook) {
  yearbookStore.setCurrentYearbook(yb.id, yb.name)
}

function goToOutline(yb: Yearbook) {
  yearbookStore.setCurrentYearbook(yb.id, yb.name)
  router.push('/outlines')
}

function goToProofread(yb: Yearbook) {
  yearbookStore.setCurrentYearbook(yb.id, yb.name)
  router.push('/proofread')
}

function getYear(dateStr: string) {
  return dateStr ? dateStr.substring(0, 4) : '----'
}

function formatDate(dateStr: string) {
  return dateStr ? dateStr.substring(0, 10) : ''
}

function statusLabel(s: string) {
  return { not_started: '未开始', in_progress: '编纂中', completed: '已完成' }[s] || s
}

function statusBadge(s: string) {
  return { not_started: 'badge-gray', in_progress: 'badge-yellow', completed: 'badge-green' }[s] || 'badge-gray'
}

function coverBg(type: string) {
  return coverOptions.find(c => c.value === type)?.bg || coverOptions[0].bg
}

onMounted(loadData)
</script>
