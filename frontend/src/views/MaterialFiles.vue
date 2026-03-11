<template>
  <div class="p-6 max-w-7xl mx-auto">
    <!-- Breadcrumb -->
    <nav class="flex items-center gap-2 text-sm text-slate-600 mb-6">
      <router-link to="/materials" class="hover:text-primary transition-colors">资料库</router-link>
      <span class="material-symbols-outlined text-slate-400 text-lg">chevron_right</span>
      <span class="text-slate-900 font-medium">{{ folder?.unit_name || '加载中...' }}</span>
    </nav>

    <!-- Action buttons -->
    <div class="flex flex-wrap items-center gap-4 mb-6">
      <button
        @click="showUploadDialog = true"
        class="px-4 py-2.5 rounded-xl border-2 border-primary text-primary font-medium hover:bg-primary/5 transition-all flex items-center gap-2"
      >
        <span class="material-symbols-outlined text-xl">upload_file</span>
        上传文件
      </button>
      <button
        @click="uploadFolder"
        class="px-4 py-2.5 rounded-xl border-2 border-primary text-primary font-medium hover:bg-primary/5 transition-all flex items-center gap-2"
      >
        <span class="material-symbols-outlined text-xl">folder_open</span>
        上传文件夹
      </button>
      <div class="ml-auto flex items-center gap-2">
        <label class="text-sm text-slate-600">年份筛选：</label>
        <select
          v-model="yearFilter"
          class="px-4 py-2 rounded-xl border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none"
        >
          <option value="">全部年份</option>
          <option v-for="y in availableYears" :key="y" :value="y">{{ y }}年</option>
        </select>
      </div>
    </div>

    <!-- File table -->
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <table class="w-full">
        <thead class="bg-slate-50 border-b border-slate-200">
          <tr>
            <th class="w-12 px-4 py-3 text-left">
              <input
                type="checkbox"
                :checked="allSelected"
                @change="toggleSelectAll"
                class="rounded border-slate-300 text-primary focus:ring-primary/20"
              />
            </th>
            <th class="px-4 py-3 text-left text-sm font-medium text-slate-600">文件名</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-slate-600">文件大小</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-slate-600">上传时间</th>
            <th class="px-4 py-3 text-right text-sm font-medium text-slate-600">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="file in paginatedFiles"
            :key="file.id"
            class="border-b border-slate-100 hover:bg-slate-50/50 transition-colors"
          >
            <td class="px-4 py-3">
              <input
                type="checkbox"
                :checked="selectedIds.has(file.id)"
                @change="toggleSelect(file.id)"
                class="rounded border-slate-300 text-primary focus:ring-primary/20"
              />
            </td>
            <td class="px-4 py-3">
              <div class="flex items-center gap-3">
                <div
                  :class="[
                    'size-10 flex items-center justify-center rounded-lg',
                    fileIconClass(file)
                  ]"
                >
                  <span class="material-symbols-outlined text-xl">{{ fileIcon(file) }}</span>
                </div>
                <span class="font-medium text-slate-900 truncate max-w-[280px]">{{ file.file_name }}</span>
              </div>
            </td>
            <td class="px-4 py-3 text-slate-600 text-sm">{{ formatSize(file.file_size) }}</td>
            <td class="px-4 py-3 text-slate-600 text-sm">{{ formatDate(file.created_at) }}</td>
            <td class="px-4 py-3 text-right">
              <div class="flex items-center justify-end gap-1">
                <button
                  @click="previewFile(file)"
                  class="p-2 rounded-lg hover:bg-slate-100 text-slate-600 hover:text-primary transition-colors"
                  title="预览"
                >
                  <span class="material-symbols-outlined text-lg">visibility</span>
                </button>
                <button
                  @click="downloadFile(file)"
                  class="p-2 rounded-lg hover:bg-slate-100 text-slate-600 hover:text-primary transition-colors"
                  title="下载"
                >
                  <span class="material-symbols-outlined text-lg">download</span>
                </button>
                <button
                  @click="confirmDelete(file)"
                  class="p-2 rounded-lg hover:bg-red-50 text-slate-600 hover:text-red-500 transition-colors"
                  title="删除"
                >
                  <span class="material-symbols-outlined text-lg">delete</span>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="files.length === 0 && !loading" class="py-16 text-center text-slate-400">
        <span class="material-symbols-outlined text-5xl mb-2 block">folder_open</span>
        <p>暂无文件，请上传</p>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="fileTotalPages > 1" class="flex justify-center gap-2 mt-6">
      <button
        v-for="p in fileTotalPages"
        :key="p"
        @click="fileCurrentPage = p"
        :class="[
          'w-10 h-10 rounded-lg font-medium transition-all',
          fileCurrentPage === p
            ? 'bg-primary text-white'
            : 'bg-white border border-slate-200 text-slate-600 hover:border-primary/30 hover:text-primary'
        ]"
      >
        {{ p }}
      </button>
    </div>

    <!-- Upload dialog -->
    <Teleport to="body">
      <div
        v-if="showUploadDialog"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
        @click.self="showUploadDialog = false"
      >
        <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 p-6">
          <h2 class="text-xl font-bold text-slate-900 mb-4">上传文件</h2>
          <p class="text-sm text-slate-500 mb-4">支持 Word、PDF 格式，单个文件不超过 5MB，可多选</p>
          <div
            :class="[
              'border-2 border-dashed rounded-xl p-8 text-center transition-colors',
              isDragging ? 'border-primary bg-primary/5' : 'border-slate-200 hover:border-primary/50'
            ]"
            @dragover.prevent="isDragging = true"
            @dragleave="isDragging = false"
            @drop.prevent="onDrop"
          >
            <input
              ref="fileInput"
              type="file"
              accept=".doc,.docx,.pdf"
              multiple
              class="hidden"
              @change="onFileSelect"
            />
            <span class="material-symbols-outlined text-5xl text-slate-400 mb-3 block">cloud_upload</span>
            <p class="text-slate-600 mb-2">拖拽文件到此处，或</p>
            <button
              type="button"
              @click="$refs.fileInput?.click()"
              class="px-4 py-2 rounded-lg bg-primary/10 text-primary font-medium hover:bg-primary/20 transition-all"
            >
              选择文件
            </button>
          </div>
          <p v-if="uploadError" class="text-sm text-red-500 mt-3">{{ uploadError }}</p>
          <div class="flex gap-3 mt-6">
            <button
              type="button"
              @click="closeUpload"
              class="flex-1 py-3 rounded-xl border border-slate-200 text-slate-600 font-medium hover:bg-slate-50 transition-all"
            >
              取消
            </button>
            <button
              @click="doUpload"
              :disabled="uploading || selectedFiles.length === 0"
              class="flex-1 py-3 rounded-xl bg-primary text-white font-medium hover:bg-primary/90 disabled:opacity-70 transition-all"
            >
              {{ uploading ? '上传中...' : '确认上传' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Delete confirmation dialog -->
    <Teleport to="body">
      <div
        v-if="deleteTarget"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
        @click.self="deleteTarget = null"
      >
        <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6">
          <h2 class="text-xl font-bold text-slate-900 mb-2">确认删除</h2>
          <p class="text-slate-600 mb-6">确定要删除「{{ deleteTarget?.file_name }}」吗？此操作不可恢复。</p>
          <div class="flex gap-3">
            <button
              @click="deleteTarget = null"
              class="flex-1 py-3 rounded-xl border border-slate-200 text-slate-600 font-medium hover:bg-slate-50 transition-all"
            >
              取消
            </button>
            <button
              @click="doDelete"
              :disabled="deleting"
              class="flex-1 py-3 rounded-xl bg-red-500 text-white font-medium hover:bg-red-600 disabled:opacity-70 transition-all"
            >
              {{ deleting ? '删除中...' : '确认删除' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, inject } from 'vue'
import { useRoute } from 'vue-router'
import { supabase } from '@/lib/supabase'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()
const toast = inject('toast', null)
const folderId = computed(() => route.params.folderId)

const folder = ref(null)
const files = ref([])
const loading = ref(false)
const yearFilter = ref('')
const selectedIds = ref(new Set())
const showUploadDialog = ref(false)
const isDragging = ref(false)
const selectedFiles = ref([])
const fileInput = ref(null)
const uploading = ref(false)
const uploadError = ref('')
const deleteTarget = ref(null)
const deleting = ref(false)
const filePageSize = 10
const fileCurrentPage = ref(1)

const MAX_FILE_SIZE = 5 * 1024 * 1024 // 5MB
const ALLOWED_TYPES = ['.doc', '.docx', '.pdf']

function fileIcon(file) {
  const t = (file.file_type || '').toLowerCase()
  if (t.includes('pdf')) return 'picture_as_pdf'
  if (t.includes('doc')) return 'description'
  return 'insert_drive_file'
}

function fileIconClass(file) {
  const t = (file.file_type || '').toLowerCase()
  if (t.includes('pdf')) return 'bg-red-100 text-red-600'
  if (t.includes('doc')) return 'bg-blue-100 text-blue-600'
  return 'bg-slate-100 text-slate-600'
}

function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function formatDate(val) {
  if (!val) return '-'
  const d = new Date(val)
  return d.toLocaleString('zh-CN')
}

const availableYears = computed(() => {
  const years = new Set(files.value.map(f => f.upload_year).filter(Boolean))
  return [...years].sort((a, b) => b - a)
})

const filteredFiles = computed(() => {
  if (!yearFilter.value) return files.value
  return files.value.filter(f => String(f.upload_year) === String(yearFilter.value))
})

const fileTotalPages = computed(() =>
  Math.max(1, Math.ceil(filteredFiles.value.length / filePageSize))
)

const paginatedFiles = computed(() => {
  const start = (fileCurrentPage.value - 1) * filePageSize
  return filteredFiles.value.slice(start, start + filePageSize)
})

const allSelected = computed(() => {
  const page = paginatedFiles.value
  return page.length > 0 && page.every(f => selectedIds.value.has(f.id))
})

function toggleSelect(id) {
  const next = new Set(selectedIds.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  selectedIds.value = next
}

function toggleSelectAll() {
  const page = paginatedFiles.value
  if (page.every(f => selectedIds.value.has(f.id))) {
    const next = new Set(selectedIds.value)
    page.forEach(f => next.delete(f.id))
    selectedIds.value = next
  } else {
    const next = new Set(selectedIds.value)
    page.forEach(f => next.add(f.id))
    selectedIds.value = next
  }
}

async function loadFolder() {
  if (!folderId.value) return
  const { data, error } = await supabase
    .from('yb_material_folders')
    .select('*')
    .eq('id', folderId.value)
    .single()
  if (error) {
    console.error('加载文件夹失败:', error)
    return
  }
  folder.value = data
}

async function loadFiles() {
  if (!folderId.value) return
  loading.value = true
  const { data, error } = await supabase
    .from('yb_material_files')
    .select('*')
    .eq('folder_id', folderId.value)
    .order('created_at', { ascending: false })
  loading.value = false
  if (error) {
    console.error('加载文件失败:', error)
    return
  }
  files.value = data || []
}

watch(yearFilter, () => { fileCurrentPage.value = 1 })
watch(folderId, () => {
  loadFolder()
  loadFiles()
}, { immediate: true })

function onDrop(e) {
  isDragging.value = false
  const items = e.dataTransfer?.files
  if (!items?.length) return
  addFiles([...items])
}

function onFileSelect(e) {
  const items = e.target.files
  if (!items?.length) return
  addFiles([...items])
  e.target.value = ''
}

function addFiles(list) {
  uploadError.value = ''
  const valid = []
  for (const f of list) {
    const ext = '.' + (f.name.split('.').pop() || '').toLowerCase()
    if (!ALLOWED_TYPES.includes(ext)) continue
    if (f.size > MAX_FILE_SIZE) {
      uploadError.value = `「${f.name}」超过 5MB 限制`
      return
    }
    valid.push(f)
  }
  selectedFiles.value = [...selectedFiles.value, ...valid]
}

function closeUpload() {
  showUploadDialog.value = false
  selectedFiles.value = []
  uploadError.value = ''
}

function uploadFolder() {
  // 上传文件夹：通过 input[webkitdirectory] 或提示用户
  const input = document.createElement('input')
  input.type = 'file'
  input.webkitdirectory = true
  input.multiple = true
  input.accept = '.doc,.docx,.pdf'
  input.onchange = (e) => {
    const items = e.target.files
    if (items?.length) addFiles([...items])
    showUploadDialog.value = true
  }
  input.click()
}

async function doUpload() {
  if (!folderId.value || selectedFiles.value.length === 0) return
  uploading.value = true
  uploadError.value = ''
  const year = new Date().getFullYear()
  try {
    for (const file of selectedFiles.value) {
      const ext = file.name.split('.').pop() || ''
      const path = `${folderId.value}/${Date.now()}_${file.name}`
      const { data: uploadData, error: uploadErr } = await supabase.storage
        .from('materials')
        .upload(path, file, { upsert: false })
      if (uploadErr) throw uploadErr
      const { data: urlData } = supabase.storage.from('materials').getPublicUrl(path)
      await supabase.from('yb_material_files').insert({
        folder_id: folderId.value,
        file_name: file.name,
        file_path: urlData.publicUrl,
        file_size: file.size,
        file_type: ext,
        upload_year: year,
        source: 'upload',
        uploaded_by: auth.user?.id
      })
    }
    closeUpload()
    loadFiles()
    await updateFolderCount()
    toast?.('文件上传成功', 'success')
  } catch (e) {
    uploadError.value = e.message || '上传失败'
  } finally {
    uploading.value = false
  }
}

async function updateFolderCount() {
  if (!folder.value) return
  const { count } = await supabase
    .from('yb_material_files')
    .select('*', { count: 'exact', head: true })
    .eq('folder_id', folderId.value)
  await supabase
    .from('yb_material_folders')
    .update({ file_count: count ?? 0, updated_at: new Date().toISOString() })
    .eq('id', folderId.value)
  folder.value.file_count = count ?? 0
}

function previewFile(file) {
  if (file.file_path) window.open(file.file_path, '_blank')
}

function downloadFile(file) {
  if (file.file_path) {
    const a = document.createElement('a')
    a.href = file.file_path
    a.download = file.file_name
    a.target = '_blank'
    a.click()
  }
}

function confirmDelete(file) {
  deleteTarget.value = file
}

async function doDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await supabase.from('yb_material_files').delete().eq('id', deleteTarget.value.id)
    files.value = files.value.filter(f => f.id !== deleteTarget.value.id)
    deleteTarget.value = null
    await updateFolderCount()
    toast?.('文件已删除', 'success')
  } catch (e) {
    console.error('删除失败:', e)
  } finally {
    deleting.value = false
  }
}
</script>
