<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/30 backdrop-blur-sm" @click.self="$emit('close')">
    <div class="bg-white rounded-xl shadow-2xl max-w-lg w-full max-h-[90vh] overflow-hidden flex flex-col" @click.stop>
      <!-- Header -->
      <div class="flex items-center justify-between p-6 border-b border-slate-200">
        <h2 class="text-lg font-semibold text-slate-900">个人中心</h2>
        <button @click="$emit('close')" class="p-2 rounded-lg hover:bg-slate-100 transition-colors">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>

      <div class="flex-1 overflow-y-auto p-6 space-y-6">
        <!-- Avatar -->
        <div class="flex flex-col items-center">
          <div
            class="size-20 rounded-full flex items-center justify-center text-white text-2xl font-bold"
            :style="{ background: auth.user?.avatar_color || '#1a90ff' }"
          >
            {{ (editName || auth.userName || '?').charAt(0) }}
          </div>
        </div>

        <!-- Name input -->
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">姓名</label>
          <input
            v-model="editName"
            type="text"
            maxlength="10"
            placeholder="请输入姓名"
            class="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
          />
          <p class="text-xs text-slate-500 mt-1">{{ editName.length }}/10 字</p>
        </div>

        <!-- My tasks -->
        <div>
          <h3 class="text-sm font-medium text-slate-800 mb-3">我的任务</h3>
          <div v-if="loading" class="text-center py-8 text-slate-500">
            <span class="material-symbols-outlined animate-spin text-2xl">progress_activity</span>
          </div>
          <div v-else-if="groupedTasks.length === 0" class="text-center py-8 text-slate-500 text-sm">
            暂无分配任务
          </div>
          <div v-else class="space-y-4">
            <div v-for="group in groupedTasks" :key="group.yearbookId" class="border border-slate-200 rounded-lg overflow-hidden">
              <div class="px-4 py-2 bg-slate-50 font-medium text-slate-700 text-sm">
                {{ group.yearbookName }}
              </div>
              <div class="divide-y divide-slate-100">
                <button
                  v-for="task in group.tasks"
                  :key="task.id"
                  @click="goToTask(task)"
                  class="w-full text-left px-4 py-3 hover:bg-slate-50 transition-colors flex items-center justify-between gap-2"
                >
                  <span class="text-sm text-slate-700 truncate flex-1">{{ task.path || task.title }}</span>
                  <span
                    :class="[
                      'px-2 py-0.5 rounded text-xs shrink-0',
                      task.status === 'submitted' && 'bg-green-100 text-green-700',
                      task.status === 'in_progress' && 'bg-primary/20 text-primary',
                      task.status === 'not_started' && 'bg-slate-100 text-slate-600'
                    ]"
                  >
                    {{ statusLabel(task.status) }}
                  </span>
                  <span class="text-xs text-slate-500 shrink-0">{{ formatTimeRange(task) }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="flex justify-end gap-3 p-6 border-t border-slate-200">
        <button @click="$emit('close')" class="px-4 py-2 rounded-lg border border-slate-200 hover:bg-slate-50 transition-colors">
          取消
        </button>
        <button
          @click="saveChanges"
          :disabled="saving"
          class="px-4 py-2 rounded-lg text-white font-medium transition-colors disabled:opacity-70"
          style="background-color: #1a90ff"
        >
          {{ saving ? '保存中...' : '保存修改' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useYearbookStore } from '@/stores/yearbook'
import { supabase } from '@/lib/supabase'

const emit = defineEmits(['close'])

const auth = useAuthStore()
const router = useRouter()
const yearbookStore = useYearbookStore()

const editName = ref('')
const tasks = ref([])
const yearbooks = ref({})
const loading = ref(true)
const saving = ref(false)

const groupedTasks = computed(() => {
  const groups = {}
  for (const task of tasks.value) {
    const ybId = task.yearbook_id
    if (!groups[ybId]) {
      groups[ybId] = {
        yearbookId: ybId,
        yearbookName: yearbooks.value[ybId]?.name || '未命名年鉴',
        tasks: []
      }
    }
    groups[ybId].tasks.push({
      ...task,
      path: buildOutlinePath(task)
    })
  }
  return Object.values(groups)
})

function buildOutlinePath(task) {
  // Build path from root to this outline - would need full outline tree
  return task.title
}

function statusLabel(status) {
  const map = { not_started: '未开始', in_progress: '编纂中', submitted: '已提交' }
  return map[status] || '未开始'
}

function formatTimeRange(task) {
  const start = task.created_at ? new Date(task.created_at).toLocaleDateString('zh-CN') : '-'
  const end = task.updated_at ? new Date(task.updated_at).toLocaleDateString('zh-CN') : '-'
  return `${start} ~ ${end}`
}

async function fetchTasks() {
  const userId = auth.user?.id
  if (!userId) return
  loading.value = true
  try {
    const { data, error } = await supabase
      .from('yb_outlines')
      .select('*')
      .eq('assigned_user_id', userId)
      .order('updated_at', { ascending: false })

    if (error) throw error
    tasks.value = data || []

    // Fetch yearbook names
    const ybIds = [...new Set(tasks.value.map(t => t.yearbook_id))]
    for (const id of ybIds) {
      const { data: yb } = await supabase.from('yb_yearbooks').select('name').eq('id', id).single()
      yearbooks.value[id] = yb || { name: '未命名' }
    }
  } catch (e) {
    console.error('Fetch tasks error:', e)
    tasks.value = []
  } finally {
    loading.value = false
  }
}

function goToTask(task) {
  const yb = yearbooks.value[task.yearbook_id]
  if (yb) {
    yearbookStore.setCurrentYearbook({ id: task.yearbook_id, name: yb.name })
  }
  yearbookStore.setCurrentOutline(task)
  emit('close')
  router.push('/compile')
}

async function saveChanges() {
  const name = editName.value.trim()
  if (name === (auth.userName || '')) {
    emit('close')
    return
  }
  if (!name) {
    alert('姓名不能为空')
    return
  }
  saving.value = true
  try {
    await auth.updateProfile(name)
    emit('close')
  } catch (e) {
    console.error('Save error:', e)
    alert('保存失败：' + (e.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

watch(() => auth.userName, (name) => {
  editName.value = name || ''
}, { immediate: true })

onMounted(() => {
  fetchTasks()
})
</script>
