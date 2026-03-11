<template>
  <div class="min-h-full bg-[#f5f7f8]">
    <div class="p-6 md:p-8 max-w-7xl mx-auto">
      <!-- Header section -->
      <header class="mb-10">
        <div class="mb-6">
          <h1 class="text-2xl md:text-3xl font-bold text-slate-900 tracking-tight">年鉴管理</h1>
          <p class="mt-1.5 text-slate-500 text-sm md:text-base">创建、编辑和管理您的年鉴项目</p>
        </div>
        <div class="flex flex-col sm:flex-row gap-4 justify-between items-stretch sm:items-center">
          <div
            class="relative flex-1 max-w-md group"
            :class="{ 'ring-2 ring-primary/30 ring-offset-2 rounded-xl': searchFocused }"
          >
            <span
              class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 transition-colors duration-200"
              :class="{ 'text-primary': searchFocused }"
            >
              search
            </span>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索年鉴名称..."
              class="w-full pl-12 pr-4 py-3 rounded-xl border border-slate-200 bg-white focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all duration-200 placeholder:text-slate-400"
              @focus="searchFocused = true"
              @blur="searchFocused = false"
            />
          </div>
          <button
            @click="openCreateModal"
            class="flex items-center justify-center gap-2 px-6 py-3 rounded-xl bg-primary text-white font-semibold shadow-lg shadow-primary/25 hover:shadow-xl hover:shadow-primary/30 hover:bg-primary/95 active:scale-[0.98] transition-all duration-200 shrink-0"
          >
            <span class="material-symbols-outlined text-xl">add_box</span>
            新建年鉴
          </button>
        </div>
      </header>

      <!-- Yearbook card grid -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div
          v-for="yb in filteredYearbooks"
          :key="yb.id"
          class="yearbook-card group bg-white rounded-2xl border border-slate-200/80 overflow-hidden shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 cursor-pointer"
        >
          <!-- Cover area -->
          <div class="relative aspect-[3/4] overflow-hidden">
            <div
              class="absolute inset-0 transition-transform duration-500 group-hover:scale-105"
              :class="getCoverGradient(yb.cover_type)"
            />
            <img
              v-if="yb.cover_url"
              :src="yb.cover_url"
              alt=""
              class="absolute inset-0 w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
            />
            <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent" />
            <span
              class="absolute top-3 right-3 px-3 py-1 rounded-full text-xs font-medium text-white shadow-sm"
              :class="getStatusBadgeClass(yb.status)"
            >
              {{ getStatusLabel(yb.status) }}
            </span>
            <div class="absolute bottom-0 left-0 right-0 p-4">
              <h3 class="text-white font-semibold text-lg border-l-4 border-white pl-3 drop-shadow-sm">
                {{ yb.name }}
              </h3>
            </div>
          </div>

          <!-- Info section -->
          <div class="p-4 space-y-3">
            <div class="flex items-center gap-2 text-slate-600 text-sm">
              <span class="material-symbols-outlined text-lg text-slate-400 shrink-0">person</span>
              <span class="truncate">{{ getManagerNames(yb) || '未指定' }}</span>
            </div>
            <div class="flex items-center gap-2 text-slate-600 text-sm">
              <span class="material-symbols-outlined text-lg text-slate-400 shrink-0">calendar_today</span>
              <span>{{ formatDateRange(yb.start_date, yb.end_date) }}</span>
            </div>
            <div>
              <div class="flex justify-between text-sm mb-1">
                <span class="text-slate-500">进度</span>
                <span class="font-medium text-slate-700">{{ yb.progress ?? 0 }}%</span>
              </div>
              <div class="h-1.5 bg-slate-100 rounded-full overflow-hidden">
                <div
                  class="h-full rounded-full transition-all duration-500"
                  :class="getProgressBarClass(yb.status)"
                  :style="{ width: `${yb.progress ?? 0}%` }"
                />
              </div>
            </div>
            <div class="flex gap-2 pt-1">
              <button
                @click.stop="handleCardAction(yb)"
                class="flex-1 py-2.5 rounded-xl bg-primary text-white text-sm font-medium hover:bg-primary/90 transition-colors"
              >
                {{ getActionLabel(yb.status) }}
              </button>
              <button
                @click.stop="openEditModal(yb)"
                class="p-2.5 rounded-xl border border-slate-200 text-slate-600 hover:bg-slate-50 hover:border-slate-300 transition-colors"
                title="编辑"
              >
                <span class="material-symbols-outlined text-lg">edit</span>
              </button>
              <button
                @click.stop="openDeleteConfirm(yb)"
                class="p-2.5 rounded-xl border border-slate-200 text-slate-600 hover:bg-red-50 hover:text-red-500 hover:border-red-200 transition-colors"
                title="删除"
              >
                <span class="material-symbols-outlined text-lg">delete</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div
        v-if="!loading && filteredYearbooks.length === 0"
        class="flex flex-col items-center justify-center py-24 text-slate-400"
      >
        <span class="material-symbols-outlined text-7xl mb-6 opacity-40">menu_book</span>
        <p class="text-lg font-medium text-slate-500">暂无年鉴</p>
        <p class="text-sm mt-2 text-slate-400">点击「新建年鉴」创建您的第一个年鉴项目</p>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-24">
        <span class="material-symbols-outlined text-5xl text-primary animate-spin">progress_activity</span>
        <p class="mt-4 text-slate-500 text-sm">加载中...</p>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 mt-10">
        <button
          @click="page = Math.max(1, page - 1)"
          :disabled="page <= 1"
          class="p-2 rounded-lg border border-slate-200 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-slate-50 transition-colors"
        >
          <span class="material-symbols-outlined text-lg">chevron_left</span>
        </button>
        <div class="flex gap-1 mx-2">
          <button
            v-for="p in visiblePages"
            :key="p"
            @click="page = p"
            class="min-w-[2.25rem] h-9 px-2 rounded-lg text-sm font-medium transition-colors"
            :class="p === page
              ? 'bg-primary text-white shadow-md'
              : 'border border-slate-200 text-slate-600 hover:bg-slate-50'"
          >
            {{ p }}
          </button>
        </div>
        <button
          @click="page = Math.min(totalPages, page + 1)"
          :disabled="page >= totalPages"
          class="p-2 rounded-lg border border-slate-200 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-slate-50 transition-colors"
        >
          <span class="material-symbols-outlined text-lg">chevron_right</span>
        </button>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="showFormModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm"
          @click.self="closeFormModal"
        >
          <div
            class="bg-white rounded-2xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-hidden flex flex-col"
          >
            <div class="flex items-center justify-between p-6 border-b border-slate-200 shrink-0">
              <h2 class="text-xl font-semibold text-slate-900">
                {{ editingYearbook ? '编辑年鉴' : '新建年鉴' }}
              </h2>
              <button
                @click="closeFormModal"
                class="p-2 rounded-xl hover:bg-slate-100 transition-colors"
              >
                <span class="material-symbols-outlined">close</span>
              </button>
            </div>
            <form @submit.prevent="submitForm" class="p-6 space-y-5 overflow-y-auto flex-1">
              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1.5">年鉴名称</label>
                <div class="relative">
                  <input
                    v-model="form.name"
                    type="text"
                    placeholder="请输入年鉴名称"
                    maxlength="20"
                    required
                    class="w-full pl-4 pr-14 py-3 rounded-xl border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
                  />
                  <span class="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-slate-400 tabular-nums">
                    {{ form.name.length }}/20
                  </span>
                </div>
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-slate-700 mb-1.5">开始日期</label>
                  <input
                    v-model="form.start_date"
                    type="date"
                    class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-slate-700 mb-1.5">结束日期</label>
                  <input
                    v-model="form.end_date"
                    type="date"
                    class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
                  />
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1.5">封面选择</label>
                <div class="flex gap-4 flex-wrap">
                  <button
                    v-for="opt in coverOptions"
                    :key="opt.value"
                    type="button"
                    @click="form.cover_type = opt.value; form.cover_url = ''"
                    class="w-20 h-28 rounded-xl overflow-hidden border-2 transition-all shrink-0"
                    :class="form.cover_type === opt.value
                      ? 'border-primary ring-2 ring-primary/20'
                      : 'border-slate-200 hover:border-slate-300'"
                  >
                    <div class="w-full h-full" :class="opt.gradient" />
                  </button>
                  <label
                    class="w-20 h-28 rounded-xl border-2 border-dashed border-slate-200 hover:border-primary/50 flex items-center justify-center cursor-pointer transition-colors shrink-0"
                  >
                    <input type="file" accept="image/*" class="hidden" @change="handleCoverUpload" />
                    <span class="material-symbols-outlined text-slate-400 text-2xl">upload</span>
                  </label>
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1.5">负责人</label>
                <div class="flex flex-wrap gap-2 items-center">
                  <span
                    v-for="m in form.managers"
                    :key="m.id"
                    class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-primary/10 text-primary text-sm"
                  >
                    <span
                      class="size-6 rounded-full flex items-center justify-center text-white text-xs font-bold"
                      :style="{ background: m.avatar_color || '#1a90ff' }"
                    >
                      {{ (m.name || '?').charAt(0) }}
                    </span>
                    {{ m.name }}
                    <button type="button" @click="removeManager(m)" class="hover:text-primary/80">
                      <span class="material-symbols-outlined text-lg">close</span>
                    </button>
                  </span>
                  <div class="relative" ref="managerDropdownRef">
                    <button
                      type="button"
                      @click="showManagerDropdown = !showManagerDropdown"
                      class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-dashed border-slate-300 text-slate-500 hover:border-primary hover:text-primary text-sm transition-colors"
                    >
                      <span class="material-symbols-outlined text-lg">person_add</span>
                      添加
                    </button>
                    <Transition name="dropdown">
                      <div
                        v-if="showManagerDropdown"
                        class="absolute left-0 top-full mt-1 w-56 max-h-48 overflow-y-auto bg-white border border-slate-200 rounded-xl shadow-xl z-10 py-1"
                      >
                        <button
                          v-for="u in availableUsers"
                          :key="u.id"
                          type="button"
                          @click="addManager(u)"
                          class="w-full text-left px-4 py-2.5 text-sm hover:bg-slate-50 flex items-center gap-3 transition-colors"
                        >
                          <span
                            class="size-8 rounded-full flex items-center justify-center text-white text-sm font-bold shrink-0"
                            :style="{ background: u.avatar_color || '#1a90ff' }"
                          >
                            {{ (u.name || u.username || '?').charAt(0) }}
                          </span>
                          {{ u.name || u.username }}
                        </button>
                        <p v-if="availableUsers.length === 0" class="px-4 py-3 text-sm text-slate-500">
                          暂无可用用户
                        </p>
                      </div>
                    </Transition>
                  </div>
                </div>
              </div>
            </form>
            <div class="flex justify-end gap-3 p-6 border-t border-slate-200 shrink-0">
              <button
                @click="closeFormModal"
                class="px-5 py-2.5 rounded-xl border border-slate-200 hover:bg-slate-50 transition-colors"
              >
                取消
              </button>
              <button
                @click="submitForm"
                :disabled="formSubmitting"
                class="px-5 py-2.5 rounded-xl bg-primary text-white font-medium hover:bg-primary/90 disabled:opacity-70 transition-colors"
              >
                {{ formSubmitting ? '提交中...' : (editingYearbook ? '保存' : '确认创建') }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Delete confirmation modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="showDeleteConfirm"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm"
          @click.self="showDeleteConfirm = false"
        >
          <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
            <div class="flex items-center gap-4 mb-4">
              <div class="size-12 rounded-full bg-red-100 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-2xl text-red-500">warning</span>
              </div>
              <div>
                <h3 class="text-lg font-semibold text-slate-900">确认删除</h3>
                <p class="text-slate-500 text-sm mt-0.5">此操作不可恢复</p>
              </div>
            </div>
            <p class="text-slate-600 mb-6">
              确定要删除「<strong class="text-slate-900">{{ yearbookToDelete?.name }}</strong>」吗？该年鉴下的所有大纲和资料将一并删除。
            </p>
            <div class="flex justify-end gap-3">
              <button
                @click="showDeleteConfirm = false"
                class="px-5 py-2.5 rounded-xl border border-slate-200 hover:bg-slate-50 transition-colors"
              >
                取消
              </button>
              <button
                @click="confirmDelete"
                :disabled="deleteSubmitting"
                class="px-5 py-2.5 rounded-xl bg-red-500 text-white font-medium hover:bg-red-600 disabled:opacity-70 transition-colors"
              >
                {{ deleteSubmitting ? '删除中...' : '确认删除' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useYearbookStore } from '@/stores/yearbook'
import { supabase } from '@/lib/supabase'

const toast = inject('toast', (msg, type) => {
  if (type === 'error') alert(msg)
})
const auth = useAuthStore()
const yearbookStore = useYearbookStore()
const router = useRouter()

const yearbooks = ref([])
const users = ref([])
const loading = ref(true)
const searchQuery = ref('')
const searchFocused = ref(false)
const page = ref(1)
const pageSize = 12

const showFormModal = ref(false)
const showDeleteConfirm = ref(false)
const editingYearbook = ref(null)
const yearbookToDelete = ref(null)
const formSubmitting = ref(false)
const deleteSubmitting = ref(false)
const showManagerDropdown = ref(false)
const managerDropdownRef = ref(null)

const form = ref({
  name: '',
  start_date: '',
  end_date: '',
  cover_type: 'default_1',
  cover_url: '',
  managers: []
})

const coverOptions = [
  { value: 'default_1', gradient: 'bg-gradient-to-br from-blue-500 to-indigo-600' },
  { value: 'default_2', gradient: 'bg-gradient-to-br from-emerald-500 to-teal-600' },
  { value: 'default_3', gradient: 'bg-gradient-to-br from-amber-500 to-orange-600' }
]

const filteredList = computed(() => {
  let list = yearbooks.value
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(yb => yb.name?.toLowerCase().includes(q))
  }
  return list
})

const filteredYearbooks = computed(() => {
  const list = filteredList.value
  const start = (page.value - 1) * pageSize
  return list.slice(start, start + pageSize)
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredList.value.length / pageSize)))

const visiblePages = computed(() => {
  const total = totalPages.value
  const current = page.value
  const delta = 2
  const range = []
  for (let i = Math.max(1, current - delta); i <= Math.min(total, current + delta); i++) {
    range.push(i)
  }
  return range
})

function getCoverGradient(coverType) {
  if (coverType === 'custom') return ''
  const opt = coverOptions.find(o => o.value === coverType)
  return opt?.gradient || coverOptions[0].gradient
}

function getStatusLabel(status) {
  const map = { not_started: '未开始', in_progress: '编纂中', completed: '已完成' }
  return map[status] || '未开始'
}

function getStatusBadgeClass(status) {
  const map = {
    not_started: 'bg-slate-400/80',
    in_progress: 'bg-primary',
    completed: 'bg-emerald-500'
  }
  return map[status] || 'bg-slate-400/80'
}

function getProgressBarClass(status) {
  const map = {
    not_started: 'bg-slate-400',
    in_progress: 'bg-primary',
    completed: 'bg-emerald-500'
  }
  return map[status] || 'bg-slate-400'
}

function getManagerNames(yb) {
  const managers = yb.yb_yearbook_managers
  if (!managers?.length) return ''
  return managers.map(m => m.yb_users?.name || m.yb_users?.username || '').filter(Boolean).join('、')
}

function formatDateRange(start, end) {
  if (!start && !end) return '未设置'
  const s = start ? new Date(start).toLocaleDateString('zh-CN') : '?'
  const e = end ? new Date(end).toLocaleDateString('zh-CN') : '?'
  return `${s} - ${e}`
}

function getActionLabel(status) {
  const map = { not_started: '开始编纂', in_progress: '进入编纂', completed: '查看详情' }
  return map[status] || '进入编纂'
}

function handleCardAction(yb) {
  yearbookStore.setCurrentYearbook(yb)
  router.push('/outlines')
}

const availableUsers = computed(() => {
  const ids = form.value.managers.map(m => m.id)
  return users.value.filter(u => !ids.includes(u.id))
})

async function fetchYearbooks() {
  loading.value = true
  try {
    const { data, error } = await supabase
      .from('yb_yearbooks')
      .select(`
        *,
        yb_yearbook_managers (
          user_id,
          yb_users (id, name, username, avatar_color)
        )
      `)
      .order('created_at', { ascending: false })

    if (error) throw error
    yearbooks.value = data || []
  } catch (e) {
    console.error('Fetch yearbooks error:', e)
    yearbooks.value = []
    toast('加载年鉴失败：' + (e.message || '未知错误'), 'error')
  } finally {
    loading.value = false
  }
}

async function fetchUsers() {
  try {
    const { data, error } = await supabase
      .from('yb_users')
      .select('id, username, name, avatar_color')
    if (error) throw error
    users.value = data || []
  } catch (e) {
    console.error('Fetch users error:', e)
    users.value = []
  }
}

function openCreateModal() {
  editingYearbook.value = null
  form.value = {
    name: '',
    start_date: '',
    end_date: '',
    cover_type: 'default_1',
    cover_url: '',
    managers: []
  }
  showFormModal.value = true
}

function openEditModal(yb) {
  editingYearbook.value = yb
  const managers = (yb.yb_yearbook_managers || []).map(m => ({
    id: m.yb_users?.id || m.user_id,
    name: m.yb_users?.name || m.yb_users?.username || '',
    avatar_color: m.yb_users?.avatar_color
  })).filter(m => m.id)
  form.value = {
    name: yb.name,
    start_date: yb.start_date?.slice(0, 10) || '',
    end_date: yb.end_date?.slice(0, 10) || '',
    cover_type: yb.cover_type || 'default_1',
    cover_url: yb.cover_url || '',
    managers
  }
  showFormModal.value = true
}

function closeFormModal() {
  showFormModal.value = false
  editingYearbook.value = null
  showManagerDropdown.value = false
}

function addManager(user) {
  if (!form.value.managers.some(m => m.id === user.id)) {
    form.value.managers.push({
      id: user.id,
      name: user.name || user.username,
      avatar_color: user.avatar_color
    })
  }
  showManagerDropdown.value = false
}

function removeManager(manager) {
  form.value.managers = form.value.managers.filter(m => m.id !== manager.id)
}

async function handleCoverUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    const ext = file.name.split('.').pop()
    const path = `covers/${Date.now()}-${Math.random().toString(36).slice(2)}.${ext}`
    const { error } = await supabase.storage.from('yearbook-covers').upload(path, file, { upsert: true })
    if (error) throw error
    const { data: urlData } = supabase.storage.from('yearbook-covers').getPublicUrl(path)
    form.value.cover_type = 'custom'
    form.value.cover_url = urlData.publicUrl
    toast('封面上传成功', 'success')
  } catch (err) {
    console.error('Upload cover error:', err)
    toast('封面上传失败：' + (err.message || '未知错误'), 'error')
    form.value.cover_type = 'default_1'
    form.value.cover_url = ''
  }
  e.target.value = ''
}

async function submitForm() {
  if (!form.value.name.trim()) return
  formSubmitting.value = true
  try {
    const payload = {
      name: form.value.name.trim(),
      start_date: form.value.start_date || null,
      end_date: form.value.end_date || null,
      cover_type: form.value.cover_type,
      cover_url: form.value.cover_url || null,
      status: editingYearbook.value?.status || 'not_started',
      progress: editingYearbook.value?.progress ?? 0,
      updated_at: new Date().toISOString()
    }

    if (editingYearbook.value) {
      const { error } = await supabase
        .from('yb_yearbooks')
        .update(payload)
        .eq('id', editingYearbook.value.id)
      if (error) throw error

      await supabase.from('yb_yearbook_managers').delete().eq('yearbook_id', editingYearbook.value.id)
      if (form.value.managers.length) {
        await supabase.from('yb_yearbook_managers').insert(
          form.value.managers.map(m => ({ yearbook_id: editingYearbook.value.id, user_id: m.id }))
        )
      }
      toast('年鉴已更新', 'success')
    } else {
      payload.created_by = auth.user?.id
      payload.created_at = new Date().toISOString()
      const { data, error } = await supabase.from('yb_yearbooks').insert(payload).select('id').single()
      if (error) throw error
      if (form.value.managers.length) {
        await supabase.from('yb_yearbook_managers').insert(
          form.value.managers.map(m => ({ yearbook_id: data.id, user_id: m.id }))
        )
      }
      toast('年鉴创建成功', 'success')
    }
    closeFormModal()
    await fetchYearbooks()
  } catch (e) {
    console.error('Submit form error:', e)
    toast('操作失败：' + (e.message || '未知错误'), 'error')
  } finally {
    formSubmitting.value = false
  }
}

function openDeleteConfirm(yb) {
  yearbookToDelete.value = yb
  showDeleteConfirm.value = true
}

async function confirmDelete() {
  if (!yearbookToDelete.value) return
  deleteSubmitting.value = true
  try {
    await supabase.from('yb_yearbook_managers').delete().eq('yearbook_id', yearbookToDelete.value.id)
    const { error } = await supabase.from('yb_yearbooks').delete().eq('id', yearbookToDelete.value.id)
    if (error) throw error
    toast('年鉴已删除', 'success')
    showDeleteConfirm.value = false
    yearbookToDelete.value = null
    await fetchYearbooks()
  } catch (e) {
    console.error('Delete error:', e)
    toast('删除失败：' + (e.message || '未知错误'), 'error')
  } finally {
    deleteSubmitting.value = false
  }
}

function handleClickOutside(e) {
  if (managerDropdownRef.value && !managerDropdownRef.value.contains(e.target)) {
    showManagerDropdown.value = false
  }
}

watch(searchQuery, () => { page.value = 1 })

onMounted(() => {
  fetchYearbooks()
  fetchUsers()
  document.addEventListener('click', handleClickOutside)
})
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.25s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-active > div:last-child,
.modal-leave-active > div:last-child {
  transition: transform 0.25s ease;
}
.modal-enter-from > div:last-child,
.modal-leave-to > div:last-child {
  transform: scale(0.95);
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
