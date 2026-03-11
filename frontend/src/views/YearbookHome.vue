<template>
  <div class="p-6 max-w-7xl mx-auto">
    <!-- Top section: search + new button -->
    <div class="flex flex-col sm:flex-row gap-4 justify-between items-stretch sm:items-center mb-8">
      <div class="relative flex-1 max-w-md">
        <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">search</span>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索年鉴..."
          class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
        />
      </div>
      <button
        @click="openCreateModal"
        class="flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl bg-primary text-white font-medium hover:bg-primary/90 transition-colors shrink-0"
      >
        <span class="material-symbols-outlined">add_box</span>
        新建年鉴
      </button>
    </div>

    <!-- Yearbook grid -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <div
        v-for="yb in filteredYearbooks"
        :key="yb.id"
        class="yearbook-card bg-white rounded-xl border border-slate-200 overflow-hidden shadow-sm hover:shadow-lg cursor-pointer"
      >
        <!-- Cover area -->
        <div class="relative aspect-[3/4] overflow-hidden">
          <div
            class="absolute inset-0"
            :class="getCoverGradient(yb.cover_type)"
          />
          <div v-if="yb.cover_url" class="absolute inset-0">
            <img :src="yb.cover_url" alt="" class="w-full h-full object-cover" />
          </div>
          <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent" />
          <span
            class="absolute top-3 right-3 px-2.5 py-1 rounded-full text-xs font-medium text-white"
            :class="getStatusBadgeClass(yb.status)"
          >
            {{ getStatusLabel(yb.status) }}
          </span>
          <div class="absolute bottom-0 left-0 right-0 p-4">
            <h3 class="text-white font-semibold text-lg border-l-4 border-white pl-3">{{ yb.name }}</h3>
          </div>
        </div>

        <!-- Info section -->
        <div class="p-4 space-y-3">
          <div class="flex items-center gap-2 text-slate-600 text-sm">
            <span class="material-symbols-outlined text-lg text-slate-400">person</span>
            <span>{{ getManagerNames(yb) || '未指定' }}</span>
          </div>
          <div class="flex items-center gap-2 text-slate-600 text-sm">
            <span class="material-symbols-outlined text-lg text-slate-400">calendar_today</span>
            <span>{{ formatDateRange(yb.start_date, yb.end_date) }}</span>
          </div>
          <div>
            <div class="flex justify-between text-sm mb-1">
              <span class="text-slate-500">进度</span>
              <span class="font-medium text-slate-700">{{ yb.progress ?? 0 }}%</span>
            </div>
            <div class="h-1.5 bg-slate-100 rounded-full overflow-hidden">
              <div
                class="h-full bg-primary rounded-full transition-all"
                :style="{ width: `${yb.progress ?? 0}%` }"
              />
            </div>
          </div>
          <div class="flex gap-2 pt-1">
            <button
              @click.stop="handleCardAction(yb)"
              class="flex-1 py-2 rounded-lg bg-primary text-white text-sm font-medium hover:bg-primary/90 transition-colors"
            >
              {{ getActionLabel(yb.status) }}
            </button>
            <button
              @click.stop="openEditModal(yb)"
              class="p-2 rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-50 transition-colors"
              title="编辑"
            >
              <span class="material-symbols-outlined text-lg">edit</span>
            </button>
            <button
              @click.stop="openDeleteConfirm(yb)"
              class="p-2 rounded-lg border border-slate-200 text-slate-600 hover:bg-red-50 hover:text-red-500 hover:border-red-200 transition-colors"
              title="删除"
            >
              <span class="material-symbols-outlined text-lg">delete</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="!loading && filteredYearbooks.length === 0" class="text-center py-16 text-slate-500">
      <span class="material-symbols-outlined text-6xl mb-4 block opacity-50">menu_book</span>
      <p class="text-lg">暂无年鉴</p>
      <p class="text-sm mt-1">点击「新建年鉴」创建第一个年鉴</p>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-16">
      <span class="material-symbols-outlined text-4xl text-primary animate-spin">progress_activity</span>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="flex justify-center gap-2 mt-8">
      <button
        @click="page = Math.max(1, page - 1)"
        :disabled="page <= 1"
        class="px-4 py-2 rounded-lg border border-slate-200 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-slate-50 transition-colors"
      >
        <span class="material-symbols-outlined align-middle">chevron_left</span>
      </button>
      <span class="px-4 py-2 text-slate-600 text-sm">{{ page }} / {{ totalPages }}</span>
      <button
        @click="page = Math.min(totalPages, page + 1)"
        :disabled="page >= totalPages"
        class="px-4 py-2 rounded-lg border border-slate-200 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-slate-50 transition-colors"
      >
        <span class="material-symbols-outlined align-middle">chevron_right</span>
      </button>
    </div>

    <!-- Create/Edit Modal -->
    <Teleport to="body">
      <div
        v-if="showFormModal"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
        @click.self="closeFormModal"
      >
        <div class="bg-white rounded-xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
          <div class="flex items-center justify-between p-6 border-b border-slate-200">
            <h2 class="text-lg font-semibold text-slate-900">{{ editingYearbook ? '编辑年鉴' : '新建年鉴' }}</h2>
            <button @click="closeFormModal" class="p-2 rounded-lg hover:bg-slate-100 transition-colors">
              <span class="material-symbols-outlined">close</span>
            </button>
          </div>
          <form @submit.prevent="submitForm" class="p-6 space-y-5">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1.5">年鉴名称</label>
              <input
                v-model="form.name"
                type="text"
                placeholder="请输入年鉴名称"
                required
                class="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
              />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1.5">开始日期</label>
                <input
                  v-model="form.start_date"
                  type="date"
                  class="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1.5">结束日期</label>
                <input
                  v-model="form.end_date"
                  type="date"
                  class="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
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
                  class="w-20 h-28 rounded-lg overflow-hidden border-2 transition-all"
                  :class="form.cover_type === opt.value ? 'border-primary ring-2 ring-primary/20' : 'border-slate-200 hover:border-slate-300'"
                >
                  <div class="w-full h-full" :class="opt.gradient" />
                </button>
                <label class="w-20 h-28 rounded-lg border-2 border-dashed border-slate-200 hover:border-primary/50 flex items-center justify-center cursor-pointer transition-colors">
                  <input type="file" accept="image/*" class="hidden" @change="handleCoverUpload" />
                  <span class="material-symbols-outlined text-slate-400">upload</span>
                </label>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1.5">负责人</label>
              <div class="flex flex-wrap gap-2 items-center">
                <span
                  v-for="m in form.managers"
                  :key="m.id"
                  class="inline-flex items-center gap-1 px-3 py-1.5 rounded-full bg-primary/10 text-primary text-sm"
                >
                  {{ m.name }}
                  <button type="button" @click="removeManager(m)" class="hover:text-primary/80">
                    <span class="material-symbols-outlined text-lg">close</span>
                  </button>
                </span>
                <div class="relative" ref="managerDropdownRef">
                  <button
                    type="button"
                    @click="showManagerDropdown = !showManagerDropdown"
                    class="inline-flex items-center gap-1 px-3 py-1.5 rounded-full border border-dashed border-slate-300 text-slate-500 hover:border-primary hover:text-primary text-sm transition-colors"
                  >
                    <span class="material-symbols-outlined text-lg">add</span>
                    添加
                  </button>
                  <div
                    v-if="showManagerDropdown"
                    class="absolute left-0 top-full mt-1 w-48 max-h-40 overflow-y-auto bg-white border border-slate-200 rounded-lg shadow-xl z-10 py-1"
                  >
                    <button
                      v-for="u in availableUsers"
                      :key="u.id"
                      type="button"
                      @click="addManager(u)"
                      class="w-full text-left px-4 py-2 text-sm hover:bg-slate-50"
                    >
                      {{ u.name || u.username }}
                    </button>
                    <p v-if="availableUsers.length === 0" class="px-4 py-2 text-sm text-slate-500">暂无用户</p>
                  </div>
                </div>
              </div>
            </div>
          </form>
          <div class="flex justify-end gap-3 p-6 border-t border-slate-200">
            <button @click="closeFormModal" class="px-4 py-2 rounded-lg border border-slate-200 hover:bg-slate-50 transition-colors">
              取消
            </button>
            <button @click="submitForm" :disabled="formSubmitting" class="px-4 py-2 rounded-lg bg-primary text-white hover:bg-primary/90 disabled:opacity-70 transition-colors">
              {{ formSubmitting ? '提交中...' : (editingYearbook ? '保存' : '确认创建') }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Delete confirmation -->
    <Teleport to="body">
      <div
        v-if="showDeleteConfirm"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
        @click.self="showDeleteConfirm = false"
      >
        <div class="bg-white rounded-xl shadow-2xl max-w-md w-full p-6">
          <h3 class="text-lg font-semibold text-slate-900 mb-2">确认删除</h3>
          <p class="text-slate-600 mb-6">确定要删除「{{ yearbookToDelete?.name }}」吗？此操作不可恢复。</p>
          <div class="flex justify-end gap-3">
            <button @click="showDeleteConfirm = false" class="px-4 py-2 rounded-lg border border-slate-200 hover:bg-slate-50 transition-colors">
              取消
            </button>
            <button @click="confirmDelete" :disabled="deleteSubmitting" class="px-4 py-2 rounded-lg bg-red-500 text-white hover:bg-red-600 disabled:opacity-70 transition-colors">
              {{ deleteSubmitting ? '删除中...' : '确认删除' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useYearbookStore } from '@/stores/yearbook'
import { supabase } from '@/lib/supabase'

const auth = useAuthStore()
const yearbookStore = useYearbookStore()
const router = useRouter()

const yearbooks = ref([])
const users = ref([])
const loading = ref(true)
const searchQuery = ref('')
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
  { value: 'default_1', gradient: 'bg-gradient-to-br from-primary/80 to-blue-600' },
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

function getCoverGradient(coverType) {
  const opt = coverOptions.find(o => o.value === coverType)
  return opt?.gradient || coverOptions[0].gradient
}

function getStatusLabel(status) {
  const map = { not_started: '未开始', in_progress: '编纂中', completed: '已完成' }
  return map[status] || '未开始'
}

function getStatusBadgeClass(status) {
  const map = {
    not_started: 'bg-slate-400',
    in_progress: 'bg-primary',
    completed: 'bg-green-500'
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
          yb_users (id, name, username)
        )
      `)
      .order('created_at', { ascending: false })

    if (error) throw error
    yearbooks.value = data || []
  } catch (e) {
    console.error('Fetch yearbooks error:', e)
    yearbooks.value = []
  } finally {
    loading.value = false
  }
}

async function fetchUsers() {
  try {
    const { data, error } = await supabase.from('yb_users').select('id, username, name')
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
    name: m.yb_users?.name || m.yb_users?.username || ''
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
    form.value.managers.push({ id: user.id, name: user.name || user.username })
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
    const { data, error } = await supabase.storage.from('yearbook-covers').upload(path, file, { upsert: true })
    if (error) throw error
    const { data: urlData } = supabase.storage.from('yearbook-covers').getPublicUrl(path)
    form.value.cover_type = 'custom'
    form.value.cover_url = urlData.publicUrl
  } catch (err) {
    console.error('Upload cover error:', err)
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
    }
    closeFormModal()
    await fetchYearbooks()
  } catch (e) {
    console.error('Submit form error:', e)
    alert('操作失败：' + (e.message || '未知错误'))
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
    showDeleteConfirm.value = false
    yearbookToDelete.value = null
    await fetchYearbooks()
  } catch (e) {
    console.error('Delete error:', e)
    alert('删除失败：' + (e.message || '未知错误'))
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
