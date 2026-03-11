<template>
  <div class="outline-manage">
    <div class="page-header">
      <h2>大纲管理</h2>
      <div class="header-actions">
        <el-select v-model="selectedYearbookId" placeholder="选择年鉴" style="width: 220px" @change="loadOutline">
          <el-option v-for="yb in yearbookList" :key="yb.id" :label="yb.name" :value="yb.id" />
        </el-select>
        <template v-if="selectedYearbookId && auth.isAdmin">
          <el-button type="primary" :icon="Plus" @click="addRootNode">添加章节</el-button>
          <el-button :icon="Upload" @click="importVisible = true">导入大纲</el-button>
        </template>
      </div>
    </div>

    <el-card v-loading="loading" shadow="never">
      <el-empty v-if="!loading && !selectedYearbookId" description="请先选择一个年鉴" />
      <el-empty v-else-if="!loading && treeData.length === 0" description="暂无大纲数据" />

      <el-tree
        v-else
        :data="treeData"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
        :props="{ children: 'children', label: 'title' }"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <div class="node-content">
              <span class="node-title">{{ data.title }}</span>
              <el-tag :type="(STATUS_TYPE[data.status] as any) || 'info'" size="small">
                {{ STATUS_LABEL[data.status] || data.status }}
              </el-tag>
              <span v-if="data.assigned_user_name" class="node-assignee">
                <el-icon><User /></el-icon>
                {{ data.assigned_user_name }}
              </span>
              <span v-if="data.unit_name" class="node-unit">
                <el-icon><OfficeBuilding /></el-icon>
                {{ data.unit_name }}
              </span>
            </div>
            <div v-if="auth.isAdmin" class="node-actions">
              <el-button text size="small" type="primary" @click.stop="addChildNode(data)">
                添加子节点
              </el-button>
              <el-button text size="small" type="primary" @click.stop="editNode(data)">
                编辑
              </el-button>
              <el-button text size="small" type="primary" @click.stop="assignNode(data)">
                指派
              </el-button>
              <el-button text size="small" type="danger" @click.stop="removeNode(data)">
                删除
              </el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </el-card>

    <!-- 添加/编辑节点 -->
    <el-dialog v-model="nodeDialogVisible" :title="nodeIsEdit ? '编辑节点' : '添加节点'" width="480px" destroy-on-close>
      <el-form ref="nodeFormRef" :model="nodeForm" :rules="nodeRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="nodeForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="供稿单位">
          <el-input v-model="nodeForm.unit_name" placeholder="如：省委办公厅" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="nodeForm.status" style="width: 100%">
            <el-option label="未开始" value="not_started" />
            <el-option label="编纂中" value="in_progress" />
            <el-option label="已提交" value="submitted" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nodeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleNodeSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 指派编辑 -->
    <el-dialog v-model="assignVisible" title="任务指派" width="420px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="节点">
          <el-input :model-value="assignTarget?.title" disabled />
        </el-form-item>
        <el-form-item label="指派给">
          <el-select v-model="assignUserId" placeholder="选择编辑人员" style="width: 100%" filterable>
            <el-option v-for="u in allUsers" :key="u.id" :label="`${u.name} (${u.role})`" :value="u.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAssign">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入大纲 -->
    <el-dialog v-model="importVisible" title="导入大纲" width="480px" destroy-on-close>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
        请上传大纲文件（Word格式），系统将自动解析标题层级结构。
        当前版本采用简化解析，每行作为一个节点，缩进表示层级。
      </el-alert>
      <el-input
        v-model="importText"
        type="textarea"
        :rows="10"
        placeholder="手动输入大纲（每行一个节点，用缩进表示层级）
例如：
第一篇 总述
  第一章 基本省情
    第一节 地理位置
  第二章 经济发展"
      />
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, User, OfficeBuilding } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { fetchYearbooks } from '@/api/yearbook'
import { fetchOutlineTree, createOutlineNode, updateOutlineNode, deleteOutlineNode, batchCreateOutlineNodes } from '@/api/outline'
import { STATUS_LABEL, STATUS_TYPE } from '@/types'
import type { OutlineNode, Yearbook, UserProfile } from '@/types'

const auth = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const yearbookList = ref<Yearbook[]>([])
const selectedYearbookId = ref('')
const treeData = ref<OutlineNode[]>([])
const allUsers = ref<UserProfile[]>([])

const nodeDialogVisible = ref(false)
const nodeIsEdit = ref(false)
const nodeEditId = ref('')
const nodeParentId = ref<string | null>(null)
const nodeFormRef = ref()
const nodeForm = reactive({ title: '', unit_name: '', status: 'not_started' as any })
const nodeRules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }

const assignVisible = ref(false)
const assignTarget = ref<OutlineNode | null>(null)
const assignUserId = ref('')

const importVisible = ref(false)
const importText = ref('')

async function loadYearbooks() {
  const result = await fetchYearbooks({ page: 1, pageSize: 100 })
  yearbookList.value = result.data
  if (result.data.length > 0 && !selectedYearbookId.value) {
    selectedYearbookId.value = result.data[0].id
    loadOutline()
  }
}

async function loadOutline() {
  if (!selectedYearbookId.value) return
  loading.value = true
  try {
    treeData.value = await fetchOutlineTree(selectedYearbookId.value)
  } catch (e: any) {
    ElMessage.error('加载大纲失败')
  } finally {
    loading.value = false
  }
}

function addRootNode() {
  nodeIsEdit.value = false
  nodeEditId.value = ''
  nodeParentId.value = null
  Object.assign(nodeForm, { title: '', unit_name: '', status: 'not_started' })
  nodeDialogVisible.value = true
}

function addChildNode(parent: OutlineNode) {
  nodeIsEdit.value = false
  nodeEditId.value = ''
  nodeParentId.value = parent.id
  Object.assign(nodeForm, { title: '', unit_name: '', status: 'not_started' })
  nodeDialogVisible.value = true
}

function editNode(node: OutlineNode) {
  nodeIsEdit.value = true
  nodeEditId.value = node.id
  nodeParentId.value = node.parent_id
  Object.assign(nodeForm, { title: node.title, unit_name: node.unit_name || '', status: node.status })
  nodeDialogVisible.value = true
}

async function handleNodeSubmit() {
  const valid = await nodeFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (nodeIsEdit.value) {
      await updateOutlineNode(nodeEditId.value, { ...nodeForm })
    } else {
      const siblings = countSiblings(treeData.value, nodeParentId.value)
      await createOutlineNode({
        ...nodeForm,
        yearbook_id: selectedYearbookId.value,
        parent_id: nodeParentId.value,
        level: nodeParentId.value ? getNodeLevel(treeData.value, nodeParentId.value) + 1 : 1,
        sort_order: siblings,
      })
    }
    ElMessage.success(nodeIsEdit.value ? '已更新' : '已添加')
    nodeDialogVisible.value = false
    loadOutline()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function countSiblings(nodes: OutlineNode[], parentId: string | null): number {
  if (!parentId) return nodes.length
  const parent = findNode(nodes, parentId)
  return parent?.children?.length || 0
}

function getNodeLevel(nodes: OutlineNode[], id: string): number {
  const node = findNode(nodes, id)
  return node?.level || 1
}

function findNode(nodes: OutlineNode[], id: string): OutlineNode | null {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children) {
      const found = findNode(n.children, id)
      if (found) return found
    }
  }
  return null
}

async function removeNode(node: OutlineNode) {
  try {
    await ElMessageBox.confirm(`确定删除「${node.title}」及其所有子节点吗？`, '删除确认', { type: 'warning' })
    await deleteOutlineNode(node.id)
    ElMessage.success('已删除')
    loadOutline()
  } catch {}
}

async function assignNode(node: OutlineNode) {
  assignTarget.value = node
  assignUserId.value = node.assigned_user_id || ''
  if (allUsers.value.length === 0) {
    allUsers.value = await auth.fetchAllUsers()
  }
  assignVisible.value = true
}

async function handleAssign() {
  if (!assignTarget.value) return
  submitting.value = true
  try {
    await updateOutlineNode(assignTarget.value.id, { assigned_user_id: assignUserId.value } as any)
    ElMessage.success('指派成功')
    assignVisible.value = false
    loadOutline()
  } catch (e: any) {
    ElMessage.error(e.message || '指派失败')
  } finally {
    submitting.value = false
  }
}

async function handleImport() {
  if (!importText.value.trim()) {
    ElMessage.warning('请输入大纲内容')
    return
  }
  submitting.value = true
  try {
    const lines = importText.value.split('\n').filter((l) => l.trim())
    const nodes: Partial<OutlineNode>[] = []
    const parentStack: { id: string; level: number }[] = []
    let sortCounter = 0

    for (const line of lines) {
      const indent = line.search(/\S/)
      const level = Math.floor(indent / 2) + 1
      const title = line.trim()
      const tempId = `temp_${sortCounter}`

      while (parentStack.length > 0 && parentStack[parentStack.length - 1].level >= level) {
        parentStack.pop()
      }

      nodes.push({
        yearbook_id: selectedYearbookId.value,
        parent_id: parentStack.length > 0 ? parentStack[parentStack.length - 1].id : null,
        title,
        level,
        sort_order: sortCounter++,
        status: 'not_started',
      })

      parentStack.push({ id: tempId, level })
    }

    const flatNodes = nodes.map((n) => ({
      yearbook_id: n.yearbook_id,
      title: n.title,
      level: n.level,
      sort_order: n.sort_order,
      status: n.status,
      parent_id: null as string | null,
    }))

    await batchCreateOutlineNodes(flatNodes)
    ElMessage.success(`已导入 ${flatNodes.length} 个节点`)
    importVisible.value = false
    importText.value = ''
    loadOutline()
  } catch (e: any) {
    ElMessage.error(e.message || '导入失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadYearbooks()
})
</script>

<style scoped>
.outline-manage { max-width: 1200px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
.page-header h2 { font-size: 22px; font-weight: 600; margin: 0; }
.header-actions { display: flex; gap: 12px; align-items: center; }

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 4px 0;
}
.node-content { display: flex; align-items: center; gap: 8px; }
.node-title { font-weight: 500; }
.node-assignee, .node-unit { display: flex; align-items: center; gap: 2px; font-size: 12px; color: #909399; }
.node-actions { display: flex; gap: 2px; opacity: 0; transition: opacity 0.2s; }
.tree-node:hover .node-actions { opacity: 1; }
</style>
