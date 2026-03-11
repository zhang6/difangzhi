<template>
  <div class="resource-library">
    <div class="page-header">
      <h2>资料库</h2>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索供稿单位"
          :prefix-icon="Search"
          clearable
          style="width: 240px"
          @input="handleSearch"
        />
        <el-button v-if="auth.isAdmin" type="primary" :icon="Plus" @click="openCreateDialog">
          新增单位
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="folder-grid">
      <el-empty v-if="!loading && folders.length === 0" description="暂无供稿单位">
        <el-button v-if="keyword" type="primary" @click="keyword = ''; loadData()">重置搜索</el-button>
      </el-empty>
      <div
        v-for="folder in folders"
        :key="folder.id"
        class="folder-card"
        @click="goToFiles(folder)"
      >
        <div class="folder-icon">
          <el-icon :size="36" color="#409eff"><FolderOpened /></el-icon>
        </div>
        <div class="folder-info">
          <h4>{{ folder.unit_name }}</h4>
          <div class="folder-meta">
            <span>共 {{ folder.file_count || 0 }} 个文件</span>
            <el-tag v-if="folder.tags" size="small" type="info">{{ folder.tags }}</el-tag>
          </div>
        </div>
        <div v-if="auth.isAdmin" class="folder-actions" @click.stop>
          <el-button text size="small" :icon="Edit" @click="openEditDialog(folder)" />
          <el-button text size="small" type="danger" :icon="Delete" @click="handleDelete(folder)" />
        </div>
      </div>
    </div>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>

    <!-- 新增/编辑单位 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑供稿单位' : '新增供稿单位'" width="460px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="单位名称" prop="unit_name">
          <el-input v-model="form.unit_name" placeholder="如：省委办公厅" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="如：政治、经济" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, FolderOpened, Edit, Delete } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { fetchFolders, createFolder, updateFolder, deleteFolder } from '@/api/resource'
import type { ResourceFolder } from '@/types'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const folders = ref<ResourceFolder[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 12
const keyword = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref('')
const formRef = ref()
const form = reactive({ unit_name: '', tags: '' })
const formRules = { unit_name: [{ required: true, message: '请输入单位名称', trigger: 'blur' }] }

let timer: ReturnType<typeof setTimeout>
function handleSearch() {
  clearTimeout(timer)
  timer = setTimeout(() => { page.value = 1; loadData() }, 300)
}

async function loadData() {
  loading.value = true
  try {
    const result = await fetchFolders({ page: page.value, pageSize, keyword: keyword.value })
    folders.value = result.data
    total.value = result.total
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function goToFiles(folder: ResourceFolder) {
  router.push({ name: 'ResourceFiles', params: { folderId: folder.id }, query: { name: folder.unit_name } })
}

function openCreateDialog() {
  isEdit.value = false; editId.value = ''
  Object.assign(form, { unit_name: '', tags: '' })
  dialogVisible.value = true
}

function openEditDialog(f: ResourceFolder) {
  isEdit.value = true; editId.value = f.id
  Object.assign(form, { unit_name: f.unit_name, tags: f.tags || '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateFolder(editId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createFolder({ ...form })
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

async function handleDelete(f: ResourceFolder) {
  try {
    await ElMessageBox.confirm(`确定删除「${f.unit_name}」及其所有文件吗？`, '删除确认', { type: 'warning' })
    await deleteFolder(f.id)
    ElMessage.success('已删除')
    loadData()
  } catch {}
}

onMounted(loadData)
</script>

<style scoped>
.resource-library { max-width: 1200px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
.page-header h2 { font-size: 22px; font-weight: 600; margin: 0; }
.header-actions { display: flex; gap: 12px; }

.folder-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  min-height: 200px;
}

.folder-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e4e7ed;
  position: relative;
}
.folder-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  border-color: #409eff;
}
.folder-info h4 { margin: 0 0 4px; font-size: 15px; font-weight: 600; }
.folder-meta { display: flex; align-items: center; gap: 8px; font-size: 12px; color: #909399; }

.folder-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}
.folder-card:hover .folder-actions { opacity: 1; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 32px; }
</style>
