<template>
  <div class="resource-files">
    <div class="page-header">
      <div class="breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/resources' }">资料库</el-breadcrumb-item>
          <el-breadcrumb-item>{{ folderName }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-actions">
        <el-radio-group v-model="yearFilter" size="small" @change="loadData">
          <el-radio-button :value="0">全部</el-radio-button>
          <el-radio-button :value="2025">2025</el-radio-button>
          <el-radio-button :value="2024">2024</el-radio-button>
          <el-radio-button :value="2023">2023</el-radio-button>
        </el-radio-group>
        <el-button type="primary" :icon="Upload" @click="uploadVisible = true">上传文件</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="files" stripe style="width: 100%">
        <el-table-column label="文件名" min-width="260">
          <template #default="{ row }">
            <div class="file-name">
              <el-icon :size="20" :color="getFileColor(row.file_type)">
                <Document />
              </el-icon>
              <span>{{ row.file_name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="文件类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getFileTagType(row.file_type)">
              {{ (row.file_type || '').toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="100">
          <template #default="{ row }">
            {{ formatSize(row.file_size) }}
          </template>
        </el-table-column>
        <el-table-column label="年份" width="80" prop="upload_year" />
        <el-table-column label="上传时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.created_at) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="previewFile(row)">预览</el-button>
            <el-button text type="primary" size="small" @click="downloadFile(row)">下载</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 上传文件 -->
    <el-dialog v-model="uploadVisible" title="上传文件" width="500px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="年份">
          <el-input-number v-model="uploadYear" :min="2020" :max="2030" />
        </el-form-item>
        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="5"
            :on-exceed="() => ElMessage.warning('最多上传5个文件')"
            accept=".doc,.docx,.pdf"
            multiple
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽文件到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 Word(.doc/.docx) / PDF 格式，单文件不超过 5MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">开始上传</el-button>
      </template>
    </el-dialog>

    <!-- 文件预览 -->
    <el-dialog v-model="previewVisible" title="文件预览" width="80%" top="5vh" destroy-on-close>
      <div v-if="previewUrl" style="height: 70vh">
        <iframe :src="previewUrl" style="width: 100%; height: 100%; border: none;" />
      </div>
      <div v-else style="text-align: center; padding: 40px">
        <el-text type="info">该文件类型暂不支持在线预览，请下载后查看</el-text>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Document, UploadFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { fetchFiles, uploadFile, deleteFile, getFileUrl } from '@/api/resource'
import type { ResourceFile } from '@/types'

const route = useRoute()
const auth = useAuthStore()
const folderId = route.params.folderId as string
const folderName = (route.query.name as string) || '文件列表'

const loading = ref(false)
const uploading = ref(false)
const files = ref<ResourceFile[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 12
const yearFilter = ref(0)

const uploadVisible = ref(false)
const uploadYear = ref(new Date().getFullYear())
const uploadRef = ref()

const previewVisible = ref(false)
const previewUrl = ref('')

async function loadData() {
  loading.value = true
  try {
    const result = await fetchFiles({
      folderId,
      page: page.value,
      pageSize,
      year: yearFilter.value || null,
    })
    files.value = result.data
    total.value = result.total
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function handleUpload() {
  const fileList = uploadRef.value?.uploadRef?.uploadFiles
  if (!fileList || fileList.length === 0) {
    ElMessage.warning('请选择文件')
    return
  }
  uploading.value = true
  try {
    for (const item of fileList) {
      const raw = item.raw as File
      if (raw.size > 5 * 1024 * 1024) {
        ElMessage.warning(`${raw.name} 超过5MB限制`)
        continue
      }
      await uploadFile(raw, folderId, auth.user?.id || '', uploadYear.value)
    }
    ElMessage.success('上传成功')
    uploadVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

async function previewFile(file: ResourceFile) {
  if (!file.file_path) {
    ElMessage.warning('文件路径不存在')
    return
  }
  try {
    const url = await getFileUrl(file.file_path)
    if (file.file_type === 'pdf') {
      previewUrl.value = url
      previewVisible.value = true
    } else {
      previewUrl.value = ''
      previewVisible.value = true
    }
  } catch {
    ElMessage.error('获取预览链接失败')
  }
}

async function downloadFile(file: ResourceFile) {
  if (!file.file_path) return
  try {
    const url = await getFileUrl(file.file_path)
    const a = document.createElement('a')
    a.href = url
    a.download = file.file_name
    a.click()
  } catch {
    ElMessage.error('下载失败')
  }
}

async function handleDelete(file: ResourceFile) {
  try {
    await ElMessageBox.confirm(`确定删除「${file.file_name}」吗？`, '删除确认', { type: 'warning' })
    await deleteFile(file.id, file.file_path || '', folderId)
    ElMessage.success('已删除')
    loadData()
  } catch {}
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / 1024 / 1024).toFixed(1) + 'MB'
}

function formatDate(str: string): string {
  if (!str) return ''
  return new Date(str).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function getFileColor(type?: string): string {
  const map: Record<string, string> = { pdf: '#f56c6c', doc: '#409eff', docx: '#409eff' }
  return map[type || ''] || '#909399'
}

function getFileTagType(type?: string): any {
  const map: Record<string, string> = { pdf: 'danger', doc: '', docx: '' }
  return map[type || ''] || 'info'
}

onMounted(loadData)
</script>

<style scoped>
.resource-files { max-width: 1200px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
.header-actions { display: flex; gap: 12px; align-items: center; }
.file-name { display: flex; align-items: center; gap: 8px; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 16px; }
</style>
