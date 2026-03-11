<template>
  <div class="min-h-screen bg-[#f5f7f8]">
    <!-- No yearbook selected -->
    <div
      v-if="!yearbookStore.currentYearbook"
      class="flex flex-col items-center justify-center py-24 text-slate-500"
    >
      <span class="material-symbols-outlined text-7xl mb-4 text-slate-300">account_tree</span>
      <p class="text-lg">请先从年鉴管理选择需要梳理的大纲</p>
    </div>

    <template v-else>
      <!-- Top bar -->
      <div class="bg-white border-b border-slate-200 px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <span class="text-slate-700 font-medium">正在编纂：{{ yearbookStore.currentYearbook?.name || '未命名年鉴' }}</span>
            <div class="w-48 h-2 bg-slate-200 rounded-full overflow-hidden">
              <div
                class="h-full rounded-full transition-all"
                :style="{ width: `${outlineProgress}%`, backgroundColor: '#1a90ff' }"
              />
            </div>
            <span class="text-sm text-slate-600">{{ outlineProgress }}%</span>
          </div>
          <button
            @click="showImportDialog = true"
            class="flex items-center gap-2 px-4 py-2 rounded-lg text-white font-medium transition-colors"
            style="background-color: #1a90ff"
          >
            <span class="material-symbols-outlined">upload_file</span>
            导入大纲
          </button>
        </div>
      </div>

      <!-- Outline tree -->
      <div class="max-w-4xl mx-auto p-6">
        <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
          <div class="px-4 py-3 border-b border-slate-200 font-medium text-slate-800 flex items-center justify-between">
            <span class="flex items-center gap-2">
              大纲目录
              <span
                class="material-symbols-outlined text-slate-400 cursor-help text-lg"
                title="使用提示：可拖拽调整顺序，点击编辑修改标题，支持最多3级目录"
              >help</span>
            </span>
            <button
              v-if="rootOutlines.length === 0"
              @click="addSibling(null)"
              class="text-sm px-3 py-1.5 rounded-lg text-primary hover:bg-primary/10 transition-colors flex items-center gap-1"
            >
              <span class="material-symbols-outlined text-lg">add</span>
              添加根章节
            </button>
          </div>
          <div class="p-4 space-y-1">
            <OutlineTreeItem
              v-for="item in rootOutlines"
              :key="item.id"
              :item="item"
              :all-items="outlines"
              :users="users"
              :level="0"
              :max-level="3"
              @move-up="moveUp"
              @move-down="moveDown"
              @delete="deleteOutline"
              @edit="startEdit"
              @add-sibling="addSibling"
              @assign="openAssignDialog"
              @smart-compile="goToSmartCompile"
              @update="updateOutline"
            />
          </div>
        </div>
      </div>

      <!-- Import dialog -->
      <Teleport to="body">
        <div
          v-if="showImportDialog"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
          @click.self="showImportDialog = false"
        >
          <div class="bg-white rounded-xl shadow-2xl max-w-md w-full p-6">
            <h3 class="text-lg font-semibold text-slate-900 mb-4">导入大纲</h3>
            <div
              :class="[
                'border-2 border-dashed rounded-xl p-8 text-center transition-colors',
                isDragging ? 'border-primary bg-primary/5' : 'border-slate-200 hover:border-slate-300'
              ]"
              @dragover.prevent="isDragging = true"
              @dragleave="isDragging = false"
              @drop.prevent="handleDrop"
            >
              <span class="material-symbols-outlined text-6xl text-slate-400 mb-4 block">upload_file</span>
              <p class="text-slate-600 mb-1">支持 .doc / .docx 格式，≤5M</p>
              <p class="text-sm text-slate-500 mb-4">拖拽文件到此处或点击上传</p>
              <input
                ref="importFileRef"
                type="file"
                accept=".doc,.docx"
                class="hidden"
                @change="handleFileSelect"
              />
              <button
                @click="importFileRef?.click()"
                class="px-4 py-2 rounded-lg border border-slate-200 hover:bg-slate-50 transition-colors"
              >
                选择文件
              </button>
            </div>
            <div class="flex justify-end gap-3 mt-4">
              <button @click="showImportDialog = false" class="px-4 py-2 rounded-lg border border-slate-200 hover:bg-slate-50">取消</button>
            </div>
          </div>
        </div>
      </Teleport>

      <!-- Task assignment dialog -->
      <Teleport to="body">
        <div
          v-if="showAssignDialog"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
          @click.self="showAssignDialog = false"
        >
          <div class="bg-white rounded-xl shadow-2xl max-w-md w-full p-6">
            <h3 class="text-lg font-semibold text-slate-900 mb-4">分配负责人</h3>
            <div class="mb-4">
              <label class="block text-sm font-medium text-slate-700 mb-1.5">章节名称</label>
              <input
                :value="assignTarget?.title"
                readonly
                class="w-full px-4 py-2.5 rounded-lg border border-slate-200 bg-slate-50 text-slate-600"
              />
            </div>
            <div class="mb-4">
              <label class="block text-sm font-medium text-slate-700 mb-1.5">选择负责人</label>
              <select
                v-model="assignUserId"
                class="w-full px-4 py-2.5 rounded-lg border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none"
              >
                <option value="">请选择</option>
                <option v-for="u in users" :key="u.id" :value="u.id">{{ u.name || u.username }}</option>
              </select>
            </div>
            <div class="flex justify-end gap-3">
              <button @click="showAssignDialog = false" class="px-4 py-2 rounded-lg border border-slate-200 hover:bg-slate-50">取消</button>
              <button @click="confirmAssign" :disabled="!assignUserId" class="px-4 py-2 rounded-lg text-white disabled:opacity-50" style="background-color: #1a90ff">
                确认分配
              </button>
            </div>
          </div>
        </div>
      </Teleport>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useYearbookStore } from '@/stores/yearbook'
import { supabase } from '@/lib/supabase'
import OutlineTreeItem from '@/components/OutlineTreeItem.vue'

const yearbookStore = useYearbookStore()
const router = useRouter()

const outlines = ref([])
const users = ref([])
const loading = ref(true)
const showImportDialog = ref(false)
const showAssignDialog = ref(false)
const isDragging = ref(false)
const importFileRef = ref(null)
const assignTarget = ref(null)
const assignUserId = ref('')

const rootOutlines = computed(() => {
  return outlines.value
    .filter(o => !o.parent_id)
    .sort((a, b) => (a.sort_order ?? 0) - (b.sort_order ?? 0))
})

const outlineProgress = computed(() => {
  if (!outlines.value.length) return 0
  const submitted = outlines.value.filter(o => o.status === 'submitted').length
  return Math.round((submitted / outlines.value.length) * 100)
})

async function fetchOutlines() {
  const ybId = yearbookStore.currentYearbook?.id
  if (!ybId) return
  loading.value = true
  try {
    const { data, error } = await supabase
      .from('yb_outlines')
      .select('*')
      .eq('yearbook_id', ybId)
      .order('sort_order', { ascending: true })

    if (error) throw error
    outlines.value = data || []
  } catch (e) {
    console.error('Fetch outlines error:', e)
    outlines.value = []
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

function getChildren(parentId) {
  return outlines.value
    .filter(o => o.parent_id === parentId)
    .sort((a, b) => (a.sort_order ?? 0) - (b.sort_order ?? 0))
}

function moveUp(item) {
  const siblings = item.parent_id
    ? getChildren(item.parent_id)
    : rootOutlines.value
  const idx = siblings.findIndex(s => s.id === item.id)
  if (idx <= 0) return
  const prev = siblings[idx - 1]
  swapSortOrder(item, prev)
}

function moveDown(item) {
  const siblings = item.parent_id
    ? getChildren(item.parent_id)
    : rootOutlines.value
  const idx = siblings.findIndex(s => s.id === item.id)
  if (idx < 0 || idx >= siblings.length - 1) return
  const next = siblings[idx + 1]
  swapSortOrder(item, next)
}

async function swapSortOrder(a, b) {
  const aOrder = a.sort_order ?? 0
  const bOrder = b.sort_order ?? 0
  await supabase.from('yb_outlines').update({ sort_order: bOrder }).eq('id', a.id)
  await supabase.from('yb_outlines').update({ sort_order: aOrder }).eq('id', b.id)
  fetchOutlines()
}

async function deleteOutline(item) {
  if (!confirm(`确定要删除「${item.title}」吗？`)) return
  try {
    const { error } = await supabase.from('yb_outlines').delete().eq('id', item.id)
    if (error) throw error
    fetchOutlines()
  } catch (e) {
    console.error('Delete error:', e)
    alert('删除失败：' + (e.message || '未知错误'))
  }
}

function startEdit(item) {
  item._editing = true
}

function updateOutline(item, newTitle) {
  item._editing = false
  if (newTitle === item.title) return
  supabase.from('yb_outlines').update({ title: newTitle, updated_at: new Date().toISOString() }).eq('id', item.id)
    .then(({ error }) => {
      if (error) throw error
      item.title = newTitle
      fetchOutlines()
    })
    .catch(e => {
      console.error('Update error:', e)
      alert('更新失败')
    })
}

async function addSibling(item) {
  const parentId = item?.parent_id ?? null
  const level = item ? ((item.level ?? 0) || 1) : 1
  if (level > 3) {
    alert('最多支持3级目录')
    return
  }
  const siblings = parentId ? getChildren(parentId) : rootOutlines.value
  const maxOrder = siblings.length ? Math.max(...siblings.map(s => s.sort_order ?? 0)) + 1 : 0
  try {
    const { data, error } = await supabase
      .from('yb_outlines')
      .insert({
        yearbook_id: yearbookStore.currentYearbook.id,
        parent_id: parentId,
        title: '新章节',
        level,
        sort_order: maxOrder,
        status: 'not_started'
      })
      .select('id')
      .single()
    if (error) throw error
    fetchOutlines()
  } catch (e) {
    console.error('Add error:', e)
    alert('添加失败：' + (e.message || '未知错误'))
  }
}

function openAssignDialog(item) {
  assignTarget.value = item
  assignUserId.value = item.assigned_user_id || ''
  showAssignDialog.value = true
}

async function confirmAssign() {
  if (!assignTarget.value) return
  try {
    const { error } = await supabase
      .from('yb_outlines')
      .update({ assigned_user_id: assignUserId.value || null, updated_at: new Date().toISOString() })
      .eq('id', assignTarget.value.id)
    if (error) throw error
    showAssignDialog.value = false
    assignTarget.value = null
    fetchOutlines()
  } catch (e) {
    console.error('Assign error:', e)
    alert('分配失败：' + (e.message || '未知错误'))
  }
}

function goToSmartCompile(item) {
  yearbookStore.setCurrentOutline(item)
  router.push('/compile')
}

function handleDrop(e) {
  isDragging.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file) processImportFile(file)
}

function handleFileSelect(e) {
  const file = e.target.files?.[0]
  if (file) processImportFile(file)
  e.target.value = ''
}

function processImportFile(file) {
  if (file.size > 5 * 1024 * 1024) {
    alert('文件大小不能超过5M')
    return
  }
  const ext = file.name.split('.').pop()?.toLowerCase()
  if (!['doc', 'docx'].includes(ext)) {
    alert('仅支持 .doc / .docx 格式')
    return
  }
  showImportDialog.value = false
  // Mock: actual doc parsing would need a library
  console.log('Import file:', file.name)
}

watch(() => yearbookStore.currentYearbook?.id, (id) => {
  if (id) fetchOutlines()
}, { immediate: true })

onMounted(() => {
  fetchUsers()
})
</script>
