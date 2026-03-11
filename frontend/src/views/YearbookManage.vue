<template>
  <div class="yearbook-manage">
    <div class="page-header">
      <h2>年鉴管理</h2>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索年鉴名称"
          :prefix-icon="Search"
          clearable
          style="width: 240px"
          @input="handleSearch"
        />
        <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 140px" @change="loadData">
          <el-option label="全部" value="all" />
          <el-option label="未开始" value="not_started" />
          <el-option label="编纂中" value="in_progress" />
          <el-option label="已完成" value="completed" />
        </el-select>
        <el-button v-if="auth.isAdmin" type="primary" :icon="Plus" @click="openCreateDialog">
          新建年鉴
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="card-grid">
      <el-empty v-if="!loading && yearbooks.length === 0" description="暂无年鉴数据" />
      <div
        v-for="yb in yearbooks"
        :key="yb.id"
        class="yearbook-card"
      >
        <div class="card-cover" :class="'cover-' + yb.cover_type">
          <span class="cover-text">{{ yb.name }}</span>
        </div>
        <div class="card-body">
          <h3 class="card-title">{{ yb.name }}</h3>
          <div class="card-meta">
            <el-tag :type="(STATUS_TYPE[yb.status] as any) || 'info'" size="small">
              {{ STATUS_LABEL[yb.status] || yb.status }}
            </el-tag>
            <span class="card-date">{{ yb.start_date }} ~ {{ yb.end_date }}</span>
          </div>
          <div class="card-progress">
            <span>编纂进度</span>
            <el-progress :percentage="yb.progress" :stroke-width="6" />
          </div>
          <div v-if="yb._manager_name" class="card-manager">
            <el-icon><User /></el-icon>
            <span>负责人: {{ yb._manager_name }}</span>
          </div>
        </div>
        <div v-if="auth.isAdmin" class="card-actions">
          <el-button text type="primary" size="small" @click="openEditDialog(yb)">编辑</el-button>
          <el-button text type="primary" size="small" @click="openManagerDialog(yb)">分配负责人</el-button>
          <el-button text type="danger" size="small" @click="handleDelete(yb)">删除</el-button>
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

    <!-- 新建/编辑年鉴 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑年鉴' : '新建年鉴'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="年鉴名称" prop="name">
          <el-input v-model="form.name" placeholder="如：江苏年鉴2024" />
        </el-form-item>
        <el-form-item label="开始日期" prop="start_date">
          <el-date-picker v-model="form.start_date" type="date" value-format="YYYY-MM-DD" placeholder="选择开始日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="end_date">
          <el-date-picker v-model="form.end_date" type="date" value-format="YYYY-MM-DD" placeholder="选择结束日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="封面风格">
          <div class="cover-select">
            <div
              v-for="c in ['default_1', 'default_2', 'default_3']"
              :key="c"
              class="cover-option"
              :class="{ active: form.cover_type === c, ['cover-' + c]: true }"
              @click="form.cover_type = c as any"
            >
              <el-icon v-if="form.cover_type === c" class="check-icon"><Check /></el-icon>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="未开始" value="not_started" />
            <el-option label="编纂中" value="in_progress" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配负责人 -->
    <el-dialog v-model="managerVisible" title="分配负责人" width="420px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="年鉴">
          <el-input :model-value="selectedYb?.name" disabled />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="selectedManagerId" placeholder="选择负责人" style="width: 100%" filterable>
            <el-option v-for="u in managerUsers" :key="u.id" :label="u.name" :value="u.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="managerVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAssignManager">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, User, Check } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { fetchYearbooks, createYearbook, updateYearbook, deleteYearbook, setManager, fetchManagers } from '@/api/yearbook'
import { STATUS_LABEL, STATUS_TYPE } from '@/types'
import type { Yearbook, UserProfile } from '@/types'

const auth = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const yearbooks = ref<(Yearbook & { _manager_name?: string })[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 12
const keyword = ref('')
const statusFilter = ref('all')

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref('')
const formRef = ref()
const form = reactive({
  name: '',
  start_date: '',
  end_date: '',
  cover_type: 'default_1' as any,
  status: 'not_started' as any,
})
const formRules = {
  name: [{ required: true, message: '请输入年鉴名称', trigger: 'blur' }],
  start_date: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  end_date: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
}

const managerVisible = ref(false)
const selectedYb = ref<Yearbook | null>(null)
const selectedManagerId = ref('')
const managerUsers = ref<UserProfile[]>([])

let searchTimer: ReturnType<typeof setTimeout>
function handleSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    loadData()
  }, 300)
}

async function loadData() {
  loading.value = true
  try {
    const result = await fetchYearbooks({
      page: page.value,
      pageSize,
      keyword: keyword.value,
      status: statusFilter.value,
    })
    const ybs = result.data
    for (const yb of ybs) {
      try {
        const mgrs = await fetchManagers(yb.id)
        ;(yb as any)._manager_name = mgrs.map((m: any) => m.user?.name).filter(Boolean).join('、') || ''
      } catch {
        (yb as any)._manager_name = ''
      }
    }
    yearbooks.value = ybs as any
    total.value = result.total
  } catch (e: any) {
    ElMessage.error('加载失败: ' + (e.message || ''))
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  isEdit.value = false
  editId.value = ''
  Object.assign(form, { name: '', start_date: '', end_date: '', cover_type: 'default_1', status: 'not_started' })
  dialogVisible.value = true
}

function openEditDialog(yb: Yearbook) {
  isEdit.value = true
  editId.value = yb.id
  Object.assign(form, {
    name: yb.name,
    start_date: yb.start_date,
    end_date: yb.end_date,
    cover_type: yb.cover_type,
    status: yb.status,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateYearbook(editId.value, { ...form })
      ElMessage.success('更新成功')
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
    await ElMessageBox.confirm(`确定要删除年鉴「${yb.name}」吗？此操作不可恢复。`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteYearbook(yb.id)
    ElMessage.success('已删除')
    loadData()
  } catch {}
}

async function openManagerDialog(yb: Yearbook) {
  selectedYb.value = yb
  selectedManagerId.value = ''
  try {
    managerUsers.value = await auth.fetchAllUsers()
    const mgrs = await fetchManagers(yb.id)
    if (mgrs.length > 0) selectedManagerId.value = mgrs[0].user_id
  } catch {}
  managerVisible.value = true
}

async function handleAssignManager() {
  if (!selectedManagerId.value || !selectedYb.value) return
  submitting.value = true
  try {
    await setManager(selectedYb.value.id, selectedManagerId.value)
    ElMessage.success('分配成功')
    managerVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '分配失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.yearbook-manage { max-width: 1200px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
.page-header h2 { font-size: 22px; font-weight: 600; margin: 0; }
.header-actions { display: flex; gap: 12px; align-items: center; }

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  min-height: 200px;
}

.yearbook-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: all 0.3s;
  border: 1px solid #e4e7ed;
}
.yearbook-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}

.card-cover {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.cover-default_1 { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.cover-default_2 { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.cover-default_3 { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.cover-custom { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.cover-text { color: #fff; font-size: 20px; font-weight: 600; text-shadow: 0 2px 4px rgba(0,0,0,0.2); }

.card-body { padding: 16px; }
.card-title { font-size: 16px; font-weight: 600; margin: 0 0 12px; }
.card-meta { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.card-date { font-size: 12px; color: #909399; }
.card-progress { margin-bottom: 8px; }
.card-progress span { font-size: 12px; color: #909399; }
.card-manager { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #606266; }

.card-actions {
  padding: 8px 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 4px;
}

.pagination-wrapper { display: flex; justify-content: center; margin-top: 32px; }

.cover-select { display: flex; gap: 12px; }
.cover-option {
  width: 80px;
  height: 50px;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.cover-option.active { border-color: #409eff; }
.check-icon { color: #fff; font-size: 20px; }
</style>
