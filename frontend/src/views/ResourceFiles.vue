<template>
  <div class="page-container bg-slate-50">
    <!-- 头部 -->
    <div class="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between sticky top-0 z-10">
      <div>
        <div class="flex items-center gap-2 text-xs text-slate-400 mb-1">
          <button class="hover:text-primary" @click="$router.push('/resources')">资料库</button>
          <span class="material-symbols-outlined" style="font-size:12px;">chevron_right</span>
          <span class="text-slate-600 font-medium">{{ folder?.unit_name }}</span>
        </div>
        <h1 class="text-lg font-bold text-slate-900">{{ folder?.unit_name }}</h1>
      </div>
      <div class="flex items-center gap-3">
        <select v-model="yearFilter" class="px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none" @change="loadFiles">
          <option value="">全部年份</option>
          <option v-for="y in yearOptions" :key="y" :value="y">{{ y }} 年</option>
        </select>
        <button
          class="flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-medium text-white transition-all hover:shadow-md"
          style="background:linear-gradient(135deg,#1a90ff,#0059b3);"
          @click="uploadVisible=true"
        >
          <span class="material-symbols-outlined" style="font-size:16px;">upload_file</span>
          上传文件
        </button>
      </div>
    </div>

    <div class="px-6 py-5">
      <!-- 标签展示 -->
      <div v-if="folder?.tags" class="flex items-center gap-2 mb-5 flex-wrap">
        <span class="text-xs text-slate-500">标签：</span>
        <span v-for="tag in folder.tags.split(',')" :key="tag" class="text-xs px-2 py-0.5 bg-blue-50 text-blue-600 rounded-full">{{ tag.trim() }}</span>
      </div>

      <!-- 文件列表 -->
      <div v-loading="loading" class="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <div class="px-5 py-3 border-b border-slate-100 flex items-center justify-between">
          <span class="text-sm font-medium text-slate-700">共 {{ total }} 个文件</span>
        </div>

        <div v-if="files.length === 0 && !loading" class="py-12 text-center text-slate-400 text-sm">暂无文件，点击右上角上传</div>

        <div v-else class="divide-y divide-slate-100">
          <div
            v-for="file in files"
            :key="file.id"
            class="flex items-center gap-4 px-5 py-3 hover:bg-slate-50 transition-colors group"
          >
            <span class="material-symbols-outlined" :class="fileIconColor(file.file_type)" style="font-size:28px;">{{ fileIcon(file.file_type) }}</span>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-slate-800 truncate">{{ file.file_name }}</p>
              <p class="text-xs text-slate-400 mt-0.5">{{ formatSize(file.file_size) }} · {{ formatDate(file.created_at) }}</p>
            </div>
            <span v-if="file.upload_year" class="text-xs px-2 py-0.5 bg-slate-100 text-slate-600 rounded-full">{{ file.upload_year }}年</span>
            <div class="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
              <button class="p-1.5 rounded-lg text-slate-400 hover:text-red-500 hover:bg-red-50" @click="handleDeleteFile(file)">
                <span class="material-symbols-outlined" style="font-size:16px;">delete</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="flex justify-center mt-6">
        <el-pagination v-model:current-page="page" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="loadFiles" />
      </div>
    </div>

    <!-- 上传文件弹窗 -->
    <div v-if="uploadVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="uploadVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 fade-in">
        <div class="flex items-center justify-between px-6 py-4 border-b border-slate-200">
          <h2 class="text-base font-semibold text-slate-900">上传文件</h2>
          <button @click="uploadVisible=false" class="p-1 rounded-lg text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="px-6 py-5">
          <div
            class="border-2 border-dashed border-slate-300 rounded-xl p-8 text-center cursor-pointer hover:border-blue-400 hover:bg-blue-50 transition-all"
            @click="fileInputRef?.click()"
            @dragover.prevent
            @drop.prevent="handleDrop"
          >
            <span class="material-symbols-outlined text-slate-400 mb-2" style="font-size:40px;">cloud_upload</span>
            <p class="text-sm text-slate-600 mb-1">点击或拖拽文件到此处</p>
            <p class="text-xs text-slate-400">支持 Word(.doc/.docx)、PDF，单文件 ≤ 5MB</p>
            <input ref="fileInputRef" type="file" class="hidden" multiple accept=".doc,.docx,.pdf" @change="handleFileChange"/>
          </div>

          <div v-if="uploadFiles.length > 0" class="mt-4 space-y-2">
            <div v-for="(f, i) in uploadFiles" :key="i" class="flex items-center gap-3 bg-slate-50 rounded-lg p-2.5 text-xs">
              <span class="material-symbols-outlined text-blue-500" style="font-size:18px;">description</span>
              <span class="flex-1 truncate text-slate-700">{{ f.name }}</span>
              <span class="text-slate-400">{{ formatSize(f.size) }}</span>
              <button class="text-slate-400 hover:text-red-500 font-bold" @click="uploadFiles.splice(i,1)">×</button>
            </div>
          </div>
        </div>
        <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-slate-200">
          <button @click="uploadVisible=false" class="px-5 py-2 text-sm text-slate-600 border border-slate-300 rounded-xl hover:bg-slate-50">取消</button>
          <button
            :disabled="uploadFiles.length === 0 || uploading"
            class="px-5 py-2 text-sm text-white rounded-xl hover:shadow-md disabled:opacity-50"
            style="background:linear-gradient(135deg,#1a90ff,#0059b3);"
            @click="handleUpload"
          >{{ uploading ? '上传中...' : `上传 ${uploadFiles.length} 个文件` }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchFolder, fetchFiles, createFile, deleteFile } from '@/api/resource'
import { useAuthStore } from '@/stores/auth'
import type { ResourceFolder, ResourceFile } from '@/types'

const route = useRoute()
const auth = useAuthStore()
const folderId = computed(() => route.params.folderId as string)

const loading = ref(false)
const uploading = ref(false)
const folder = ref<ResourceFolder | null>(null)
const files = ref<ResourceFile[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 12
const yearFilter = ref<number | ''>('')
const yearOptions = [2024, 2023, 2022, 2021, 2020]

const uploadVisible = ref(false)
const fileInputRef = ref<HTMLInputElement>()
const uploadFiles = ref<File[]>([])

async function loadFolder() {
  folder.value = await fetchFolder(folderId.value)
}

async function loadFiles() {
  loading.value = true
  try {
    const result = await fetchFiles({
      folderId: folderId.value,
      year: yearFilter.value || undefined,
      page: page.value, pageSize,
    })
    files.value = result.data
    total.value = result.total
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleFileChange(e: Event) {
  const f = (e.target as HTMLInputElement).files
  if (f) uploadFiles.value.push(...Array.from(f))
}

function handleDrop(e: DragEvent) {
  const f = e.dataTransfer?.files
  if (f) uploadFiles.value.push(...Array.from(f))
}

async function handleUpload() {
  if (!folderId.value || uploadFiles.value.length === 0) return
  uploading.value = true
  try {
    for (const file of uploadFiles.value) {
      await createFile({
        folder_id: folderId.value,
        file_name: file.name,
        file_path: `/uploads/${folderId.value}/${file.name}`,
        file_size: file.size,
        file_type: file.name.split('.').pop()?.toLowerCase(),
        upload_year: new Date().getFullYear(),
        source: 'library',
        uploaded_by: auth.user?.id,
      })
    }
    ElMessage.success(`成功上传 ${uploadFiles.value.length} 个文件`)
    uploadVisible.value = false
    uploadFiles.value = []
    await loadFiles()
    await loadFolder()
  } catch (e: any) { ElMessage.error(e.message || '上传失败') }
  finally { uploading.value = false }
}

async function handleDeleteFile(file: ResourceFile) {
  try {
    await ElMessageBox.confirm(`确定删除文件「${file.file_name}」吗？`, '删除确认', { type: 'warning' })
    await deleteFile(file.id)
    ElMessage.success('已删除')
    await loadFiles()
    await loadFolder()
  } catch {}
}

function fileIcon(type?: string) {
  if (!type) return 'description'
  if (type === 'pdf') return 'picture_as_pdf'
  if (['doc', 'docx'].includes(type)) return 'description'
  return 'draft'
}

function fileIconColor(type?: string) {
  if (!type) return 'text-slate-400'
  if (type === 'pdf') return 'text-red-500'
  return 'text-blue-500'
}

function formatSize(bytes?: number) {
  if (!bytes) return '0 KB'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

function formatDate(d: string) { return d ? d.substring(0, 10) : '' }

onMounted(async () => {
  await loadFolder()
  await loadFiles()
})
</script>
