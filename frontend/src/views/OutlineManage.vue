<template>
  <div class="min-h-screen bg-[#f5f7f8]">
    <!-- No yearbook selected -->
    <div
      v-if="!yearbookStore.currentYearbook"
      class="flex flex-col items-center justify-center py-24 text-slate-500"
    >
      <span class="material-symbols-outlined text-7xl mb-4 text-slate-300">account_tree</span>
      <p class="text-lg mb-6">请先从年鉴管理选择需要梳理的大纲</p>
      <router-link
        to="/yearbooks"
        class="px-5 py-2.5 rounded-xl bg-primary text-white font-medium hover:bg-primary/90 transition-all flex items-center gap-2"
      >
        <span class="material-symbols-outlined text-xl">arrow_back</span>
        返回年鉴管理
      </router-link>
    </div>

    <template v-else>
      <!-- Top bar -->
      <div class="bg-white border-b border-slate-200 px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <span class="text-slate-700 font-medium">正在编纂：{{ yearbookStore.currentYearbook?.name || '未命名年鉴' }}</span>
            <div class="w-48 h-2 bg-slate-200 rounded-full overflow-hidden">
              <div
                class="h-full rounded-full bg-primary transition-all"
                :style="{ width: `${outlineProgress}%` }"
              />
            </div>
            <span class="text-sm text-slate-600">{{ outlineProgress }}%</span>
          </div>
          <button
            @click="showImportDialog = true"
            class="flex items-center gap-2 px-4 py-2 rounded-xl bg-primary text-white font-medium hover:bg-primary/90 transition-all"
          >
            <span class="material-symbols-outlined">upload_file</span>
            导入大纲
          </button>
        </div>
      </div>

      <!-- Help tooltip -->
      <div class="max-w-5xl mx-auto px-6 pt-4 flex justify-end">
        <div class="relative" ref="helpRef">
          <button
            @click="showHelp = !showHelp"
            class="p-2 rounded-lg text-slate-400 hover:text-primary hover:bg-slate-100 transition-colors"
          >
            <span class="material-symbols-outlined text-xl">help</span>
          </button>
          <Transition
            enter-active-class="transition-all duration-200 ease-out"
            enter-from-class="opacity-0 translate-y-2"
            enter-to-class="opacity-100 translate-y-0"
            leave-active-class="transition-all duration-150 ease-in"
            leave-from-class="opacity-100 translate-y-0"
            leave-to-class="opacity-0 translate-y-2"
          >
            <div
              v-if="showHelp"
              class="absolute right-0 top-full mt-2 w-72 bg-white rounded-xl border border-slate-200 shadow-lg z-40 p-4 text-sm text-slate-600"
            >
              <p class="font-medium text-slate-800 mb-2">使用提示</p>
              <ul class="space-y-1.5 list-disc list-inside">
                <li>可拖拽调整顺序</li>
                <li>点击编辑修改标题</li>
                <li>支持最多3级目录</li>
                <li>子章节可分配负责人</li>
                <li>支持智能编纂跳转</li>
              </ul>
            </div>
          </Transition>
        </div>
      </div>

      <!-- Outline tree area -->
      <div class="max-w-5xl mx-auto p-6">
        <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
          <div class="px-5 py-4 border-b border-slate-200 flex items-center justify-between">
            <span class="flex items-center gap-2 font-medium text-slate-800">
              大纲目录
              <button
                @click="showHelp = !showHelp"
                class="p-1 rounded text-slate-400 hover:text-primary hover:bg-slate-100 transition-colors"
              >
                <span class="material-symbols-outlined text-lg">help</span>
              </button>
            </span>
            <button
              v-if="rootOutlines.length === 0"
              @click="addSibling(null)"
              class="text-sm px-3 py-2 rounded-xl bg-primary/10 text-primary hover:bg-primary/20 transition-colors flex items-center gap-1.5 font-medium"
            >
              <span class="material-symbols-outlined text-lg">add</span>
              添加根章节
            </button>
          </div>
          <div class="p-4">
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
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
          @click.self="showImportDialog = false"
        >
          <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
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
              <span class="material-symbols-outlined text-6xl text-slate-400 mb-4 block">cloud_upload</span>
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
                v-if="!selectedFile"
                @click="importFileRef?.click()"
                class="px-4 py-2 rounded-xl border border-slate-200 hover:bg-slate-50 transition-colors"
              >
                选择文件
              </button>
              <div v-else class="flex items-center justify-center gap-2 text-emerald-600">
                <span class="material-symbols-outlined text-2xl">check_circle</span>
                <span class="text-sm font-medium truncate max-w-[200px]">{{ selectedFile.name }}</span>
              </div>
            </div>
            <div class="flex justify-end gap-3 mt-4">
              <button
                @click="cancelImport"
                class="px-4 py-2 rounded-xl border border-slate-200 hover:bg-slate-50 transition-colors"
              >
                取消
              </button>
              <button
                v-if="selectedFile"
                @click="confirmImport"
                class="px-4 py-2 rounded-xl bg-primary text-white font-medium hover:bg-primary/90 transition-colors"
              >
                确认导入
              </button>
            </div>
          </div>
        </div>
      </Teleport>

      <!-- Assign dialog -->
      <Teleport to="body">
        <div
          v-if="showAssignDialog"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
          @click.self="showAssignDialog = false"
        >
          <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
            <h3 class="text-lg font-semibold text-slate-900 mb-4">分配负责人</h3>
            <div class="mb-4">
              <label class="block text-sm font-medium text-slate-700 mb-1.5">章节名称</label>
              <input
                :value="assignTarget?.title"
                readonly
                class="w-full px-4 py-2.5 rounded-xl border border-slate-200 bg-slate-50 text-slate-600"
              />
            </div>
            <div class="mb-4">
              <label class="block text-sm font-medium text-slate-700 mb-1.5">选择负责人</label>
              <select
                v-model="assignUserId"
                class="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none"
              >
                <option value="">请选择</option>
                <option v-for="u in users" :key="u.id" :value="u.id">{{ u.name || u.username }}</option>
              </select>
            </div>
            <div class="flex justify-end gap-3">
              <button
                @click="showAssignDialog = false"
                class="px-4 py-2 rounded-xl border border-slate-200 hover:bg-slate-50 transition-colors"
              >
                取消
              </button>
              <button
                @click="confirmAssign"
                :disabled="!assignUserId"
                class="px-4 py-2 rounded-xl bg-primary text-white font-medium hover:bg-primary/90 disabled:opacity-50 transition-colors"
              >
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
import { ref, computed, watch, onMounted, onUnmounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useYearbookStore } from '@/stores/yearbook'
import { supabase } from '@/lib/supabase'
import OutlineTreeItem from '@/components/OutlineTreeItem.vue'

const toast = inject('toast')
const yearbookStore = useYearbookStore()
const router = useRouter()

const outlines = ref([])
const users = ref([])
const loading = ref(true)
const showImportDialog = ref(false)
const showAssignDialog = ref(false)
const showHelp = ref(false)
const isDragging = ref(false)
const importFileRef = ref(null)
const selectedFile = ref(null)
const assignTarget = ref(null)
const assignUserId = ref('')
const helpRef = ref(null)

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
    toast?.('加载大纲失败', 'error')
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
    toast?.('删除成功', 'success')
  } catch (e) {
    console.error('Delete error:', e)
    toast?.('删除失败：' + (e.message || '未知错误'), 'error')
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
      toast?.('更新成功', 'success')
    })
    .catch(e => {
      console.error('Update error:', e)
      toast?.('更新失败', 'error')
    })
}

async function addSibling(item) {
  const parentId = item?.parent_id ?? null
  const level = item ? ((item.level ?? 0) || 1) : 1
  if (level > 3) {
    toast?.('最多支持3级目录', 'error')
    return
  }
  const siblings = parentId ? getChildren(parentId) : rootOutlines.value
  const maxOrder = siblings.length ? Math.max(...siblings.map(s => s.sort_order ?? 0)) + 1 : 0
  try {
    const { error } = await supabase
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
    toast?.('添加成功', 'success')
  } catch (e) {
    console.error('Add error:', e)
    toast?.('添加失败：' + (e.message || '未知错误'), 'error')
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
    toast?.('负责人分配成功', 'success')
  } catch (e) {
    console.error('Assign error:', e)
    toast?.('分配失败：' + (e.message || '未知错误'), 'error')
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
    toast?.('文件大小不能超过5M', 'error')
    return
  }
  const ext = file.name.split('.').pop()?.toLowerCase()
  if (!['doc', 'docx'].includes(ext)) {
    toast?.('仅支持 .doc / .docx 格式', 'error')
    return
  }
  selectedFile.value = file
}

function cancelImport() {
  showImportDialog.value = false
  selectedFile.value = null
}

function confirmImport() {
  if (!selectedFile.value) return
  toast?.('大纲导入成功（模拟）', 'success')
  showImportDialog.value = false
  selectedFile.value = null
}

watch(() => yearbookStore.currentYearbook?.id, (id) => {
  if (id) fetchOutlines()
}, { immediate: true })

function handleClickOutside(e) {
  if (helpRef.value && !helpRef.value.contains(e.target)) {
    showHelp.value = false
  }
}

onMounted(() => {
  fetchUsers()
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>
