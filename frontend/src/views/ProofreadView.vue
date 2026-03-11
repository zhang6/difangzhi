<template>
  <div class="proofread-view">
    <div class="page-header">
      <h2>统稿</h2>
      <div class="header-actions">
        <el-select v-model="selectedYearbookId" placeholder="选择年鉴" style="width: 200px" @change="onYearbookChange">
          <el-option v-for="yb in yearbookList" :key="yb.id" :label="yb.name" :value="yb.id" />
        </el-select>
        <el-button :icon="Download" @click="exportVisible = true">导出Word</el-button>
      </div>
    </div>

    <div v-if="!selectedYearbookId" class="empty-state">
      <el-empty description="请先选择年鉴" />
    </div>

    <div v-else class="proofread-layout">
      <!-- 左侧：大纲目录 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h4>大纲目录</h4>
        </div>
        <el-tree
          :data="outlineTree"
          node-key="id"
          default-expand-all
          highlight-current
          :props="{ children: 'children', label: 'title' }"
          @node-click="onOutlineClick"
        >
          <template #default="{ data }">
            <div class="outline-node">
              <span>{{ data.title }}</span>
              <el-tag :type="(STATUS_TYPE[data.status] as any)" size="small">
                {{ STATUS_LABEL[data.status] }}
              </el-tag>
            </div>
          </template>
        </el-tree>
      </div>

      <!-- 中间：内容编辑 -->
      <div class="main-content">
        <template v-if="selectedOutline">
          <div class="content-header">
            <h3>{{ selectedOutline.title }}</h3>
            <div class="content-actions">
              <el-button size="small" type="primary" @click="handleSubmitOutline">
                <el-icon><Check /></el-icon> 提交
              </el-button>
            </div>
          </div>

          <div v-loading="entriesLoading" class="entries-area">
            <div v-for="entry in currentEntries" :key="entry.id" class="entry-block">
              <div class="entry-header">
                <h4>{{ entry.title }}</h4>
                <el-tag :type="(STATUS_TYPE[entry.status] as any)" size="small">
                  {{ STATUS_LABEL[entry.status] }}
                </el-tag>
              </div>
              <div class="entry-content" v-html="entry.ai_content || '<span style=&quot;color:#c0c4cc&quot;>暂无内容</span>'" />
              <div class="entry-actions">
                <el-button text type="primary" size="small" @click="openVersionHistory(entry)">
                  <el-icon><Clock /></el-icon> 版本历史
                </el-button>
                <el-button text type="primary" size="small" @click="openAnnotateDialog(entry)">
                  <el-icon><ChatDotRound /></el-icon> 添加批注
                </el-button>
              </div>
            </div>
            <el-empty v-if="!entriesLoading && currentEntries.length === 0" description="该节点暂无条目" />
          </div>
        </template>
        <el-empty v-else description="请选择大纲节点查看内容" />
      </div>

      <!-- 右侧：批注面板 -->
      <div class="annotation-panel">
        <div class="panel-header">
          <h4>批注</h4>
          <el-radio-group v-model="annotationFilter" size="small">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="pending">待处理</el-radio-button>
          </el-radio-group>
        </div>
        <div class="annotation-list">
          <div v-for="a in filteredAnnotations" :key="a.id" class="annotation-item">
            <div class="annotation-meta">
              <el-avatar :size="24" :style="{ backgroundColor: '#409eff', fontSize: '12px' }">
                {{ (a.author_name || '?').charAt(0) }}
              </el-avatar>
              <span class="annotation-author">{{ a.author_name || '未知' }}</span>
              <el-tag v-if="a.process_status === 'processed'" type="success" size="small">已处理</el-tag>
              <el-tag v-else type="warning" size="small">待处理</el-tag>
            </div>
            <div class="annotation-content">{{ a.content }}</div>
            <div class="annotation-time">{{ formatDate(a.created_at) }}</div>
            <el-button
              v-if="a.process_status === 'pending'"
              text type="success" size="small"
              @click="markAnnotationProcessed(a.id)"
            >标记已处理</el-button>
          </div>
          <el-empty v-if="filteredAnnotations.length === 0" :image-size="40" description="暂无批注" />
        </div>
      </div>
    </div>

    <!-- 版本历史 -->
    <el-dialog v-model="versionVisible" title="版本历史" width="600px" destroy-on-close>
      <el-table :data="versionList" stripe>
        <el-table-column label="版本" width="80">
          <template #default="{ row }">v{{ row.version }}</template>
        </el-table-column>
        <el-table-column label="编辑人" prop="editor_name" width="100" />
        <el-table-column label="修订说明" prop="revision_note" />
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ formatDate(row.created_at) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加批注 -->
    <el-dialog v-model="annotateVisible" title="添加批注" width="420px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="条目">
          <el-input :model-value="annotateEntry?.title" disabled />
        </el-form-item>
        <el-form-item label="批注类型">
          <el-radio-group v-model="annotateType">
            <el-radio value="mine">我的批注</el-radio>
            <el-radio value="revision">审阅批注</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="批注内容">
          <el-input v-model="annotateContent" type="textarea" :rows="4" placeholder="请输入批注内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="annotateVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAnnotate">提交</el-button>
      </template>
    </el-dialog>

    <!-- 导出 -->
    <el-dialog v-model="exportVisible" title="导出Word" width="500px" destroy-on-close>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
        选择需要导出的大纲节点，系统将生成Word文档。
      </el-alert>
      <el-tree
        :data="outlineTree"
        node-key="id"
        show-checkbox
        default-expand-all
        ref="exportTreeRef"
        :props="{ children: 'children', label: 'title' }"
      />
      <template #footer>
        <el-button @click="exportVisible = false">取消</el-button>
        <el-button type="primary" @click="handleExport">导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Check, Clock, ChatDotRound } from '@element-plus/icons-vue'
import { fetchYearbooks } from '@/api/yearbook'
import { fetchOutlineTree } from '@/api/outline'
import { fetchEntries, fetchVersions, fetchAnnotations, createAnnotation, updateAnnotationStatus, updateEntry } from '@/api/entry'
import { useAuthStore } from '@/stores/auth'
import { STATUS_LABEL, STATUS_TYPE } from '@/types'
import type { Yearbook, OutlineNode, Entry, EntryVersion, Annotation } from '@/types'

const auth = useAuthStore()
const yearbookList = ref<Yearbook[]>([])
const selectedYearbookId = ref('')
const outlineTree = ref<OutlineNode[]>([])
const selectedOutline = ref<OutlineNode | null>(null)

const currentEntries = ref<Entry[]>([])
const entriesLoading = ref(false)
const annotations = ref<Annotation[]>([])
const annotationFilter = ref('all')

const versionVisible = ref(false)
const versionList = ref<EntryVersion[]>([])

const annotateVisible = ref(false)
const annotateEntry = ref<Entry | null>(null)
const annotateContent = ref('')
const annotateType = ref<'mine' | 'revision'>('mine')
const submitting = ref(false)

const exportVisible = ref(false)
const exportTreeRef = ref()

const filteredAnnotations = computed(() => {
  if (annotationFilter.value === 'pending') {
    return annotations.value.filter((a) => a.process_status === 'pending')
  }
  return annotations.value
})

async function loadYearbooks() {
  const result = await fetchYearbooks({ page: 1, pageSize: 100 })
  yearbookList.value = result.data
}

async function onYearbookChange() {
  selectedOutline.value = null
  currentEntries.value = []
  annotations.value = []
  outlineTree.value = await fetchOutlineTree(selectedYearbookId.value)
}

async function onOutlineClick(node: OutlineNode) {
  selectedOutline.value = node
  entriesLoading.value = true
  try {
    const result = await fetchEntries({ outlineId: node.id, page: 1, pageSize: 100 })
    currentEntries.value = result.data
    await loadAnnotations()
  } catch {
    ElMessage.error('加载失败')
  } finally {
    entriesLoading.value = false
  }
}

async function loadAnnotations() {
  annotations.value = []
  for (const entry of currentEntries.value) {
    try {
      const list = await fetchAnnotations(entry.id)
      annotations.value.push(...list)
    } catch {}
  }
}

async function openVersionHistory(entry: Entry) {
  try {
    versionList.value = await fetchVersions(entry.id)
    versionVisible.value = true
  } catch {
    ElMessage.error('加载版本失败')
  }
}

function openAnnotateDialog(entry: Entry) {
  annotateEntry.value = entry
  annotateContent.value = ''
  annotateType.value = 'mine'
  annotateVisible.value = true
}

async function handleAnnotate() {
  if (!annotateContent.value.trim() || !annotateEntry.value) return
  submitting.value = true
  try {
    await createAnnotation({
      entry_id: annotateEntry.value.id,
      content: annotateContent.value,
      author_id: auth.user?.id || '',
      annotation_type: annotateType.value,
    })
    ElMessage.success('批注已添加')
    annotateVisible.value = false
    loadAnnotations()
  } catch (e: any) {
    ElMessage.error(e.message || '添加失败')
  } finally {
    submitting.value = false
  }
}

async function markAnnotationProcessed(id: string) {
  try {
    await updateAnnotationStatus(id, 'processed')
    ElMessage.success('已标记为已处理')
    loadAnnotations()
  } catch {}
}

async function handleSubmitOutline() {
  if (!selectedOutline.value) return
  try {
    for (const entry of currentEntries.value) {
      if (entry.status !== 'submitted') {
        await updateEntry(entry.id, { status: 'submitted' })
      }
    }
    ElMessage.success('已提交')
    if (selectedOutline.value) {
      onOutlineClick(selectedOutline.value)
    }
  } catch (e: any) {
    ElMessage.error(e.message || '提交失败')
  }
}

function handleExport() {
  ElMessage.info('导出功能开发中，敬请期待')
  exportVisible.value = false
}

function formatDate(str: string): string {
  if (!str) return ''
  return new Date(str).toLocaleString('zh-CN')
}

onMounted(loadYearbooks)
</script>

<style scoped>
.proofread-view { max-width: 1400px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.page-header h2 { font-size: 22px; font-weight: 600; margin: 0; }
.header-actions { display: flex; gap: 12px; }
.empty-state { padding: 60px 0; }

.proofread-layout {
  display: grid;
  grid-template-columns: 260px 1fr 280px;
  gap: 16px;
  min-height: calc(100vh - 180px);
}

.sidebar {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  padding: 16px;
  overflow-y: auto;
}
.sidebar-header { margin-bottom: 12px; }
.sidebar-header h4 { margin: 0; font-size: 15px; font-weight: 600; }

.outline-node {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  font-size: 13px;
}

.main-content {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  padding: 20px;
  overflow-y: auto;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.content-header h3 { margin: 0; font-size: 18px; }

.entry-block {
  margin-bottom: 20px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}
.entry-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.entry-header h4 { margin: 0; font-size: 15px; }
.entry-content {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 12px;
}
.entry-content :deep(p) { margin: 0 0 8px; }
.entry-actions {
  display: flex;
  gap: 8px;
  border-top: 1px solid #f0f0f0;
  padding-top: 8px;
}

.annotation-panel {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  padding: 16px;
  display: flex;
  flex-direction: column;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.panel-header h4 { margin: 0; font-size: 15px; font-weight: 600; }

.annotation-list { flex: 1; overflow-y: auto; }
.annotation-item {
  padding: 12px;
  border-bottom: 1px solid #f5f5f5;
}
.annotation-meta { display: flex; align-items: center; gap: 6px; margin-bottom: 6px; }
.annotation-author { font-size: 13px; font-weight: 500; }
.annotation-content { font-size: 13px; line-height: 1.6; color: #606266; }
.annotation-time { font-size: 11px; color: #c0c4cc; margin-top: 4px; }
</style>
