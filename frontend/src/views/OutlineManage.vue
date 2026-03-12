<template>
  <div class="flex h-full overflow-hidden bg-slate-50">
    <!-- 左侧大纲树 -->
    <div class="flex flex-col border-r border-slate-200 bg-white overflow-hidden flex-shrink-0" style="width:320px;">
      <!-- 头部 -->
      <div class="bg-white border-b border-slate-200 px-4 py-3 flex-shrink-0">
        <div class="flex items-center justify-between mb-3">
          <h2 class="text-sm font-bold text-slate-900">大纲管理</h2>
          <div class="flex gap-1">
            <button
              v-if="auth.isAdmin && yearbookStore.currentYearbookId"
              class="flex items-center gap-1 px-2.5 py-1.5 text-xs border border-slate-300 rounded-lg text-slate-600 hover:bg-slate-50"
              @click="importVisible=true"
            >
              <span class="material-symbols-outlined" style="font-size:13px;">upload_file</span>导入
            </button>
            <button
              v-if="auth.isAdmin && yearbookStore.currentYearbookId"
              class="flex items-center gap-1 px-2.5 py-1.5 text-xs text-white rounded-lg hover:shadow"
              style="background:linear-gradient(135deg,#1a90ff,#0059b3);"
              @click="addRoot"
            >
              <span class="material-symbols-outlined" style="font-size:13px;">add</span>新增
            </button>
          </div>
        </div>
        <!-- 年鉴选择 -->
        <div class="mb-2">
          <label class="text-xs text-slate-500 font-medium block mb-1">当前年鉴</label>
          <select
            v-model="selectedYearbookId"
            class="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
            @change="loadOutline"
          >
            <option value="">请选择年鉴</option>
            <option v-for="yb in yearbookList" :key="yb.id" :value="yb.id">{{ yb.name }}</option>
          </select>
        </div>
        <!-- 搜索 -->
        <div class="relative">
          <span class="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400" style="font-size:14px;">search</span>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索章节..."
            class="w-full pl-8 pr-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-blue-400"
          />
        </div>
      </div>

      <!-- 树形列表 -->
      <div v-loading="loading" class="flex-1 overflow-y-auto px-2 py-2 text-sm">
        <template v-if="!selectedYearbookId">
          <div class="text-center text-slate-400 text-xs py-8">请先选择年鉴</div>
        </template>
        <template v-else-if="filteredTree.length === 0 && !loading">
          <div class="text-center text-slate-400 text-xs py-8">暂无大纲，点击「新增」添加章节</div>
        </template>
        <TreeNode
          v-for="node in filteredTree"
          :key="node.id"
          :node="node"
          :selected-id="selectedNode?.id"
          :is-admin="auth.isAdmin"
          @select="selectNode"
          @add-child="openAddChild"
          @edit="openEdit"
          @delete="handleDelete"
          @assign="openAssign"
          @compile="goToCompile"
        />
        <!-- 添加章节按钮 -->
        <button
          v-if="selectedYearbookId && auth.isAdmin"
          class="w-full flex items-center gap-2 px-3 py-2.5 mt-2 text-xs text-slate-400 hover:text-blue-500 hover:bg-blue-50 rounded-lg transition-colors border-2 border-dashed border-slate-200 hover:border-blue-300"
          @click="addRoot"
        >
          <span class="material-symbols-outlined" style="font-size:16px;">add</span>
          添加新章节
        </button>
      </div>
    </div>

    <!-- 右侧详情面板 -->
    <div class="flex-1 overflow-y-auto p-6">
      <div v-if="!selectedNode" class="flex flex-col items-center justify-center h-full text-slate-400">
        <span class="material-symbols-outlined mb-3" style="font-size:48px;color:#cbd5e1;">account_tree</span>
        <p class="text-sm">请从左侧选择一个大纲节点查看详情</p>
      </div>

      <div v-else class="fade-in space-y-5">
        <!-- 节点信息 -->
        <div class="bg-white rounded-xl border border-slate-200 p-5">
          <div class="flex items-start justify-between mb-4">
            <div>
              <h2 class="text-base font-semibold text-slate-900 mb-1">{{ selectedNode.title }}</h2>
              <div class="flex items-center gap-3 text-xs text-slate-500">
                <span class="flex items-center gap-1">
                  <span class="material-symbols-outlined" style="font-size:13px;">folder</span>
                  层级：第 {{ selectedNode.level }} 级
                </span>
                <span v-if="selectedNode.unit_name" class="flex items-center gap-1">
                  <span class="material-symbols-outlined" style="font-size:13px;">corporate_fare</span>
                  {{ selectedNode.unit_name }}
                </span>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span class="badge" :class="statusBadge(selectedNode.status)">{{ statusLabel(selectedNode.status) }}</span>
              <button v-if="auth.isAdmin" class="p-1.5 rounded-lg border border-slate-300 text-slate-500 hover:bg-slate-50" @click="openEdit(selectedNode)">
                <span class="material-symbols-outlined" style="font-size:16px;">edit</span>
              </button>
              <button v-if="auth.isAdmin" class="p-1.5 rounded-lg border border-red-200 text-red-500 hover:bg-red-50" @click="handleDelete(selectedNode)">
                <span class="material-symbols-outlined" style="font-size:16px;">delete</span>
              </button>
            </div>
          </div>

          <div class="grid grid-cols-3 gap-4">
            <div class="bg-slate-50 rounded-lg p-3">
              <p class="text-xs text-slate-500 mb-1">负责人</p>
              <div class="flex items-center gap-2">
                <div v-if="selectedNode.assigned_user" class="w-6 h-6 rounded-full flex items-center justify-center text-white text-[10px] font-bold bg-blue-500">
                  {{ selectedNode.assigned_user.name?.charAt(0) }}
                </div>
                <span class="text-sm font-medium text-slate-800">{{ selectedNode.assigned_user?.name || '未分配' }}</span>
              </div>
            </div>
            <div class="bg-slate-50 rounded-lg p-3">
              <p class="text-xs text-slate-500 mb-1">节点状态</p>
              <p class="text-sm font-medium text-slate-800">{{ statusLabel(selectedNode.status) }}</p>
            </div>
            <div class="bg-slate-50 rounded-lg p-3">
              <p class="text-xs text-slate-500 mb-1">供稿单位</p>
              <p class="text-sm font-medium text-slate-800">{{ selectedNode.unit_name || '未设置' }}</p>
            </div>
          </div>
        </div>

        <!-- 关联资料 -->
        <div class="bg-white rounded-xl border border-slate-200 p-5">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-sm font-semibold text-slate-900">关联资料</h3>
            <button class="flex items-center gap-1.5 px-3 py-1.5 text-xs text-primary border border-blue-300 rounded-lg hover:bg-blue-50 transition-colors" @click="materialVisible=true">
              <span class="material-symbols-outlined" style="font-size:13px;">upload</span>上传资料
            </button>
          </div>
          <div v-if="nodeFiles.length === 0" class="text-center text-slate-400 text-xs py-4">暂无关联资料</div>
          <div v-else class="space-y-2">
            <div v-for="f in nodeFiles" :key="f.id" class="flex items-center gap-3 p-3 bg-slate-50 rounded-lg hover:bg-blue-50 transition-colors group">
              <span class="material-symbols-outlined" :class="fileIconColor(f.file_type)" style="font-size:22px;">{{ fileIcon(f.file_type) }}</span>
              <div class="flex-1 min-w-0">
                <p class="text-xs font-medium text-slate-800 truncate">{{ f.file_name }}</p>
                <p class="text-[11px] text-slate-400">{{ formatSize(f.file_size) }} · {{ formatDate(f.created_at) }}</p>
              </div>
              <div class="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                <button class="p-1 rounded text-slate-400 hover:text-red-500" @click="deleteFile(f.id)">
                  <span class="material-symbols-outlined" style="font-size:15px;">delete</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex gap-3" v-if="selectedNode.level > 1">
          <button
            class="flex items-center gap-2 px-4 py-2.5 text-sm text-white rounded-xl hover:shadow-md transition-all"
            style="background:linear-gradient(135deg,#7c3aed,#a78bfa);"
            @click="goToCompile(selectedNode)"
          >
            <span class="material-symbols-outlined" style="font-size:16px;">auto_fix_high</span>
            智能编纂
          </button>
          <button
            v-if="auth.isAdmin"
            class="flex items-center gap-2 px-4 py-2.5 text-sm border border-slate-300 rounded-xl text-slate-600 hover:bg-slate-50 transition-colors"
            @click="openAssign(selectedNode)"
          >
            <span class="material-symbols-outlined" style="font-size:16px;">how_to_reg</span>
            任务指派
          </button>
        </div>
      </div>
    </div>

    <!-- 新建/编辑节点弹窗 -->
    <div v-if="nodeDialogVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="nodeDialogVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 fade-in">
        <div class="flex items-center justify-between px-6 py-4 border-b border-slate-200">
          <h2 class="text-base font-semibold text-slate-900">{{ nodeIsEdit ? '编辑节点' : '添加节点' }}</h2>
          <button @click="nodeDialogVisible=false" class="p-1 rounded-lg text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="px-6 py-5 space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">章节标题 <span class="text-red-500">*</span></label>
            <input v-model="nodeForm.title" type="text" placeholder="请输入章节标题"
              class="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"/>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">供稿单位</label>
            <input v-model="nodeForm.unit_name" type="text" placeholder="如：省委办公厅（选填）"
              class="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"/>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">状态</label>
            <select v-model="nodeForm.status" class="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400">
              <option value="not_started">未开始</option>
              <option value="in_progress">编纂中</option>
              <option value="submitted">已提交</option>
            </select>
          </div>
        </div>
        <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-slate-200">
          <button @click="nodeDialogVisible=false" class="px-5 py-2 text-sm text-slate-600 border border-slate-300 rounded-xl hover:bg-slate-50">取消</button>
          <button
            :disabled="submitting"
            class="px-5 py-2 text-sm text-white rounded-xl hover:shadow-md disabled:opacity-50"
            style="background:linear-gradient(135deg,#1a90ff,#0059b3);"
            @click="handleNodeSubmit"
          >{{ submitting ? '保存中...' : '确定' }}</button>
        </div>
      </div>
    </div>

    <!-- 任务指派弹窗 -->
    <div v-if="assignVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="assignVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 fade-in">
        <div class="flex items-center justify-between px-6 py-4 border-b border-slate-200">
          <h2 class="text-base font-semibold text-slate-900">任务指派</h2>
          <button @click="assignVisible=false" class="p-1 rounded-lg text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="px-6 py-5 space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">章节名称</label>
            <input :value="assignTarget?.title" disabled class="w-full px-3 py-2.5 border border-slate-200 rounded-xl text-sm bg-slate-50 text-slate-500"/>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">指派给</label>
            <select v-model="assignUserId" class="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400">
              <option value="">请选择编辑人员</option>
              <option v-for="u in allUsers" :key="u.id" :value="u.id">{{ u.name }} ({{ u.role }})</option>
            </select>
          </div>
        </div>
        <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-slate-200">
          <button @click="assignVisible=false" class="px-5 py-2 text-sm text-slate-600 border border-slate-300 rounded-xl hover:bg-slate-50">取消</button>
          <button
            :disabled="!assignUserId || submitting"
            class="px-5 py-2 text-sm text-white rounded-xl hover:shadow-md disabled:opacity-50"
            style="background:linear-gradient(135deg,#1a90ff,#0059b3);"
            @click="handleAssign"
          >确认指派</button>
        </div>
      </div>
    </div>

    <!-- 导入大纲弹窗 -->
    <div v-if="importVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="importVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg mx-4 fade-in">
        <div class="flex items-center justify-between px-6 py-4 border-b border-slate-200">
          <h2 class="text-base font-semibold text-slate-900">导入大纲</h2>
          <button @click="importVisible=false" class="p-1 rounded-lg text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="px-6 py-5 space-y-4">
          <div class="bg-blue-50 border border-blue-200 rounded-xl p-3 text-xs text-blue-700">
            每行一个节点，使用空格缩进表示层级（2个空格为一级子节点）
          </div>
          <textarea
            v-model="importText"
            rows="10"
            placeholder="第一篇 总述&#10;  第一章 基本省情&#10;    第一节 地理位置&#10;  第二章 经济发展&#10;第二篇 政治建设"
            class="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 resize-none font-mono"
          ></textarea>
        </div>
        <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-slate-200">
          <button @click="importVisible=false" class="px-5 py-2 text-sm text-slate-600 border border-slate-300 rounded-xl hover:bg-slate-50">取消</button>
          <button
            :disabled="submitting"
            class="px-5 py-2 text-sm text-white rounded-xl hover:shadow-md disabled:opacity-50"
            style="background:linear-gradient(135deg,#1a90ff,#0059b3);"
            @click="handleImport"
          >{{ submitting ? '导入中...' : '确认导入' }}</button>
        </div>
      </div>
    </div>

    <!-- 资料上传弹窗 -->
    <div v-if="materialVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="materialVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 fade-in">
        <div class="flex items-center justify-between px-6 py-4 border-b border-slate-200">
          <h2 class="text-base font-semibold text-slate-900">上传资料</h2>
          <button @click="materialVisible=false" class="p-1 rounded-lg text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="px-6 py-5">
          <p class="text-xs text-slate-500 mb-3">节点：{{ selectedNode?.title }}</p>
          <div
            class="border-2 border-dashed border-slate-300 rounded-xl p-8 text-center cursor-pointer hover:border-blue-400 hover:bg-blue-50 transition-all"
            @click="triggerUpload"
            @dragover.prevent
            @drop.prevent="handleDrop"
          >
            <span class="material-symbols-outlined text-slate-400 mb-2" style="font-size:36px;">cloud_upload</span>
            <p class="text-sm text-slate-600 mb-1">点击或拖拽文件到此处上传</p>
            <p class="text-xs text-slate-400">支持 Word、PDF，单文件 ≤ 5MB</p>
            <input ref="fileInputRef" type="file" class="hidden" multiple accept=".doc,.docx,.pdf" @change="handleFileChange"/>
          </div>
          <div v-if="uploadFiles.length > 0" class="mt-3 space-y-2">
            <div v-for="(f, i) in uploadFiles" :key="i" class="flex items-center gap-2 bg-slate-50 rounded-lg p-2 text-xs">
              <span class="material-symbols-outlined text-blue-500" style="font-size:16px;">description</span>
              <span class="flex-1 truncate">{{ f.name }}</span>
              <span class="text-slate-400">{{ formatSize(f.size) }}</span>
              <button @click="uploadFiles.splice(i,1)" class="text-slate-400 hover:text-red-500">×</button>
            </div>
          </div>
        </div>
        <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-slate-200">
          <button @click="materialVisible=false" class="px-5 py-2 text-sm text-slate-600 border border-slate-300 rounded-xl hover:bg-slate-50">取消</button>
          <button
            :disabled="uploadFiles.length === 0 || uploading"
            class="px-5 py-2 text-sm text-white rounded-xl hover:shadow-md disabled:opacity-50"
            style="background:linear-gradient(135deg,#1a90ff,#0059b3);"
            @click="handleUpload"
          >{{ uploading ? '上传中...' : '确认上传' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useYearbookStore } from '@/stores/yearbook'
import { fetchYearbooks } from '@/api/yearbook'
import { fetchOutlineTree, createOutlineNode, updateOutlineNode, deleteOutlineNode, batchCreateOutlineNodes } from '@/api/outline'
import { fetchFilesByOutline, deleteFile as deleteFileApi } from '@/api/resource'
import type { OutlineNode, Yearbook, UserProfile } from '@/types'
import TreeNode from '@/components/TreeNode.vue'

const router = useRouter()
const auth = useAuthStore()
const yearbookStore = useYearbookStore()

const loading = ref(false)
const submitting = ref(false)
const yearbookList = ref<Yearbook[]>([])
const selectedYearbookId = ref(yearbookStore.currentYearbookId || '')
const treeData = ref<OutlineNode[]>([])
const selectedNode = ref<OutlineNode | null>(null)
const searchKeyword = ref('')
const allUsers = ref<UserProfile[]>([])
const nodeFiles = ref<any[]>([])

const filteredTree = computed(() => {
  if (!searchKeyword.value) return treeData.value
  return filterTree(treeData.value, searchKeyword.value.toLowerCase())
})

function filterTree(nodes: OutlineNode[], kw: string): OutlineNode[] {
  return nodes.reduce((acc, node) => {
    const children = filterTree(node.children || [], kw)
    if (node.title.toLowerCase().includes(kw) || children.length > 0) {
      acc.push({ ...node, children })
    }
    return acc
  }, [] as OutlineNode[])
}

// 节点对话框
const nodeDialogVisible = ref(false)
const nodeIsEdit = ref(false)
const nodeEditId = ref('')
const nodeParentId = ref<string | null>(null)
const nodeForm = reactive({ title: '', unit_name: '', status: 'not_started' })

// 指派对话框
const assignVisible = ref(false)
const assignTarget = ref<OutlineNode | null>(null)
const assignUserId = ref('')

// 导入对话框
const importVisible = ref(false)
const importText = ref('')

// 资料上传
const materialVisible = ref(false)
const fileInputRef = ref<HTMLInputElement>()
const uploadFiles = ref<File[]>([])
const uploading = ref(false)

async function loadYearbooks() {
  const result = await fetchYearbooks({ page: 1, pageSize: 100 })
  yearbookList.value = result.data
  if (!selectedYearbookId.value && result.data.length > 0) {
    selectedYearbookId.value = result.data[0].id
    yearbookStore.setCurrentYearbook(result.data[0].id, result.data[0].name)
  }
}

async function loadOutline() {
  if (!selectedYearbookId.value) { treeData.value = []; return }
  const yb = yearbookList.value.find(y => y.id === selectedYearbookId.value)
  if (yb) yearbookStore.setCurrentYearbook(yb.id, yb.name)
  loading.value = true
  try {
    treeData.value = await fetchOutlineTree(selectedYearbookId.value)
  } catch (e: any) {
    ElMessage.error('加载大纲失败')
  } finally {
    loading.value = false
  }
}

async function selectNode(node: OutlineNode) {
  selectedNode.value = node
  try {
    nodeFiles.value = await fetchFilesByOutline(node.id)
  } catch { nodeFiles.value = [] }
}

function addRoot() {
  nodeIsEdit.value = false; nodeEditId.value = ''; nodeParentId.value = null
  Object.assign(nodeForm, { title: '', unit_name: '', status: 'not_started' })
  nodeDialogVisible.value = true
}

function openAddChild(parent: OutlineNode) {
  nodeIsEdit.value = false; nodeEditId.value = ''; nodeParentId.value = parent.id
  Object.assign(nodeForm, { title: '', unit_name: '', status: 'not_started' })
  nodeDialogVisible.value = true
}

function openEdit(node: OutlineNode) {
  nodeIsEdit.value = true; nodeEditId.value = node.id
  Object.assign(nodeForm, { title: node.title, unit_name: node.unit_name || '', status: node.status })
  nodeDialogVisible.value = true
}

async function handleNodeSubmit() {
  if (!nodeForm.title.trim()) { ElMessage.warning('请输入章节标题'); return }
  submitting.value = true
  try {
    if (nodeIsEdit.value) {
      await updateOutlineNode(nodeEditId.value, { ...nodeForm })
    } else {
      const level = nodeParentId.value ? 2 : 1
      await createOutlineNode({
        yearbook_id: selectedYearbookId.value, parent_id: nodeParentId.value,
        title: nodeForm.title, unit_name: nodeForm.unit_name, status: nodeForm.status,
        level, sort_order: treeData.value.length,
      })
    }
    ElMessage.success(nodeIsEdit.value ? '更新成功' : '添加成功')
    nodeDialogVisible.value = false
    if (nodeIsEdit.value && selectedNode.value) selectedNode.value = null
    loadOutline()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
  finally { submitting.value = false }
}

async function handleDelete(node: OutlineNode) {
  try {
    await ElMessageBox.confirm(`确定删除「${node.title}」及其所有子节点吗？`, '删除确认', { type: 'warning' })
    await deleteOutlineNode(node.id)
    ElMessage.success('已删除')
    if (selectedNode.value?.id === node.id) selectedNode.value = null
    loadOutline()
  } catch {}
}

function openAssign(node: OutlineNode) {
  assignTarget.value = node
  assignUserId.value = node.assigned_user_id || ''
  if (allUsers.value.length === 0) auth.fetchAllUsers().then(u => allUsers.value = u)
  assignVisible.value = true
}

async function handleAssign() {
  if (!assignTarget.value || !assignUserId.value) return
  submitting.value = true
  try {
    await updateOutlineNode(assignTarget.value.id, { assigned_user_id: assignUserId.value } as any)
    ElMessage.success('指派成功')
    assignVisible.value = false
    loadOutline()
  } catch (e: any) { ElMessage.error(e.message || '指派失败') }
  finally { submitting.value = false }
}

async function handleImport() {
  if (!importText.value.trim()) { ElMessage.warning('请输入大纲内容'); return }
  submitting.value = true
  try {
    const lines = importText.value.split('\n').filter(l => l.trim())
    const nodes: any[] = []
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
        parent_id: null, // flat import
        title, level, sort_order: sortCounter++, status: 'not_started',
      })
      parentStack.push({ id: tempId, level })
    }
    await batchCreateOutlineNodes(nodes)
    ElMessage.success(`已导入 ${nodes.length} 个节点`)
    importVisible.value = false
    importText.value = ''
    loadOutline()
  } catch (e: any) { ElMessage.error(e.message || '导入失败') }
  finally { submitting.value = false }
}

function goToCompile(node: OutlineNode) {
  yearbookStore.setCurrentOutline(node.id, node.title)
  router.push('/compile')
}

function triggerUpload() { fileInputRef.value?.click() }
function handleFileChange(e: Event) {
  const files = (e.target as HTMLInputElement).files
  if (files) uploadFiles.value.push(...Array.from(files))
}
function handleDrop(e: DragEvent) {
  const files = e.dataTransfer?.files
  if (files) uploadFiles.value.push(...Array.from(files))
}
async function handleUpload() {
  if (!selectedNode.value || uploadFiles.value.length === 0) return
  uploading.value = true
  try {
    // Mock upload: record metadata only
    const { createFile, getOrCreateFolder } = await import('@/api/resource')
    const folder = await getOrCreateFolder(selectedNode.value.unit_name || '默认单位')
    for (const file of uploadFiles.value) {
      await createFile({
        folder_id: folder.id, outline_id: selectedNode.value.id,
        file_name: file.name, file_path: `/uploads/${file.name}`,
        file_size: file.size, file_type: file.name.split('.').pop(),
        upload_year: new Date().getFullYear(), source: 'outline',
      })
    }
    ElMessage.success('上传成功')
    materialVisible.value = false
    uploadFiles.value = []
    nodeFiles.value = await fetchFilesByOutline(selectedNode.value.id)
  } catch (e: any) { ElMessage.error(e.message || '上传失败') }
  finally { uploading.value = false }
}

async function deleteFile(fileId: string) {
  try {
    await ElMessageBox.confirm('确定删除该文件吗？', '删除确认', { type: 'warning' })
    await deleteFileApi(fileId)
    if (selectedNode.value) {
      nodeFiles.value = await fetchFilesByOutline(selectedNode.value.id)
    }
    ElMessage.success('已删除')
  } catch {}
}

function statusLabel(s: string) {
  return { not_started: '未开始', in_progress: '编纂中', submitted: '已提交', editing: '编辑中' }[s] || s
}
function statusBadge(s: string) {
  return { not_started: 'badge-gray', in_progress: 'badge-yellow', submitted: 'badge-green', editing: 'badge-blue' }[s] || 'badge-gray'
}
function fileIcon(type?: string) {
  if (!type) return 'description'
  if (['pdf'].includes(type)) return 'picture_as_pdf'
  if (['doc', 'docx'].includes(type)) return 'description'
  return 'draft'
}
function fileIconColor(type?: string) {
  if (!type) return 'text-slate-400'
  if (['pdf'].includes(type)) return 'text-red-500'
  return 'text-blue-500'
}
function formatSize(bytes?: number) {
  if (!bytes) return '0 KB'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}
function formatDate(d: string) { return d ? d.substring(0, 10) : '' }

watch(() => yearbookStore.currentYearbookId, (id) => {
  if (id && id !== selectedYearbookId.value) {
    selectedYearbookId.value = id
    loadOutline()
  }
})

onMounted(async () => {
  await loadYearbooks()
  if (selectedYearbookId.value) loadOutline()
})
</script>
