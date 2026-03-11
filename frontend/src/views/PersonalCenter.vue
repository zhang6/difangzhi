<template>
  <div class="personal-center">
    <div class="page-header">
      <h2>个人中心</h2>
    </div>

    <el-row :gutter="20">
      <!-- 个人信息 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span>个人信息</span>
          </template>
          <div class="profile-section">
            <el-avatar
              :size="80"
              :style="{ backgroundColor: auth.user?.avatar_color || '#409eff', fontSize: '32px' }"
            >
              {{ auth.userName?.charAt(0) || '?' }}
            </el-avatar>
            <el-form
              ref="profileFormRef"
              :model="profileForm"
              label-width="80px"
              style="margin-top: 24px; width: 100%"
            >
              <el-form-item label="用户名">
                <el-input :model-value="auth.user?.username" disabled />
              </el-form-item>
              <el-form-item label="姓名">
                <el-input v-model="profileForm.name" />
              </el-form-item>
              <el-form-item label="角色">
                <el-input :model-value="roleLabel" disabled />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="手机">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-col>

      <!-- 我的任务 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="task-header">
              <span>我的任务</span>
              <el-tag>共 {{ tasks.length }} 项</el-tag>
            </div>
          </template>
          <el-table v-loading="tasksLoading" :data="tasks" stripe>
            <el-table-column label="年鉴" prop="yearbook_name" width="150" />
            <el-table-column label="大纲节点" prop="outline_title" min-width="200" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="(STATUS_TYPE[row.status] as any)" size="small">
                  {{ STATUS_LABEL[row.status] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="供稿单位" prop="unit_name" width="140" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button text type="primary" size="small" @click="goToCompile(row)">
                  去编纂
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!tasksLoading && tasks.length === 0" description="暂无分配的任务" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { supabase } from '@/api/supabase'
import { STATUS_LABEL, STATUS_TYPE } from '@/types'

const router = useRouter()
const auth = useAuthStore()
const saving = ref(false)
const tasksLoading = ref(false)
const profileFormRef = ref()

const profileForm = reactive({
  name: auth.user?.name || '',
  email: auth.user?.email || '',
  phone: auth.user?.phone || '',
})

const roleLabel = computed(() => {
  const map: Record<string, string> = { admin: '管理员', manager: '负责人', editor: '编辑' }
  return map[auth.user?.role || ''] || auth.user?.role || ''
})

interface TaskItem {
  outline_id: string
  outline_title: string
  yearbook_name: string
  yearbook_id: string
  status: string
  unit_name: string
}

const tasks = ref<TaskItem[]>([])

async function handleSave() {
  saving.value = true
  try {
    await auth.updateProfile({
      name: profileForm.name,
      email: profileForm.email,
      phone: profileForm.phone,
    })
    ElMessage.success('保存成功')
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function loadTasks() {
  if (!auth.user?.id) return
  tasksLoading.value = true
  try {
    const { data, error } = await supabase
      .from('yb_outlines')
      .select('id, title, status, unit_name, yearbook_id, yearbook:yb_yearbooks!yb_outlines_yearbook_id_fkey(name)')
      .eq('assigned_user_id', auth.user.id)
      .order('created_at', { ascending: false })

    if (error) throw error
    tasks.value = (data || []).map((item: any) => ({
      outline_id: item.id,
      outline_title: item.title,
      yearbook_name: item.yearbook?.name || '',
      yearbook_id: item.yearbook_id,
      status: item.status,
      unit_name: item.unit_name || '',
    }))
  } catch {
    tasks.value = []
  } finally {
    tasksLoading.value = false
  }
}

function goToCompile(task: TaskItem) {
  router.push({ path: '/compile', query: { yearbook: task.yearbook_id, outline: task.outline_id } })
}

onMounted(loadTasks)
</script>

<style scoped>
.personal-center { max-width: 1200px; margin: 0 auto; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 600; margin: 0; }

.profile-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
