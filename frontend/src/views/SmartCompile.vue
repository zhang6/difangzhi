<template>
  <div class="smart-compile">
    <div class="page-header">
      <h2>智能编纂</h2>
      <div class="header-actions">
        <el-select v-model="selectedYearbookId" placeholder="选择年鉴" style="width: 200px" @change="onYearbookChange">
          <el-option v-for="yb in yearbookList" :key="yb.id" :label="yb.name" :value="yb.id" />
        </el-select>
        <el-select v-model="selectedOutlineId" placeholder="选择大纲节点" style="width: 200px" @change="loadEntries">
          <el-option v-for="o in flatOutlines" :key="o.id" :label="'  '.repeat(o.level - 1) + o.title" :value="o.id" />
        </el-select>
      </div>
    </div>

    <div v-if="!selectedOutlineId" class="empty-state">
      <el-empty description="请先选择年鉴和大纲节点" />
    </div>

    <template v-else>
      <div class="compile-layout">
        <!-- 左侧：资料列表 -->
        <div class="panel panel-materials">
          <div class="panel-header">
            <h4>参考资料</h4>
            <el-button size="small" text type="primary" @click="searchHistoryVisible = true">
              <el-icon><Search /></el-icon> 检索历史数据
            </el-button>
          </div>
          <div class="material-list">
            <div v-for="(entry, idx) in entries" :key="entry.id" class="material-item" :class="{ active: activeEntryId === entry.id }" @click="selectEntry(entry)">
              <div class="material-title">
                <el-tag size="small" :type="(STATUS_TYPE[entry.status] as any)">{{ STATUS_LABEL[entry.status] }}</el-tag>
                <span>{{ entry.title }}</span>
              </div>
            </div>
            <el-empty v-if="entries.length === 0" :image-size="60" description="暂无条目" />
          </div>
          <el-button type="primary" style="width: 100%; margin-top: 8px" @click="createNewEntry">
            <el-icon><Plus /></el-icon> 新建条目
          </el-button>
        </div>

        <!-- 中间：原始数据 + AI生成 -->
        <div class="panel panel-editor">
          <template v-if="activeEntry">
            <div class="panel-header">
              <h4>{{ activeEntry.title }}</h4>
              <div>
                <el-button size="small" type="success" :loading="aiLoading" @click="handleAIGenerate">
                  <el-icon><MagicStick /></el-icon> AI生成条目
                </el-button>
                <el-button size="small" @click="detectConflicts">
                  <el-icon><Warning /></el-icon> 数据检测
                </el-button>
              </div>
            </div>

            <el-tabs v-model="activeTab">
              <el-tab-pane label="原始数据" name="raw">
                <div class="editor-area">
                  <el-input
                    v-model="activeEntry.original_content"
                    type="textarea"
                    :rows="12"
                    placeholder="填入原始数据（统计数据、工作报告等）"
                    @change="handleSaveRaw"
                  />
                </div>
              </el-tab-pane>

              <el-tab-pane label="AI生成内容" name="ai">
                <div class="editor-area">
                  <div class="ai-toolbar">
                    <el-button-group>
                      <el-button size="small" :loading="aiLoading" @click="handleAIRewrite">
                        <el-icon><Edit /></el-icon> AI润色
                      </el-button>
                      <el-button size="small" :loading="aiLoading" @click="handleAIExpand">
                        <el-icon><DocumentAdd /></el-icon> AI扩写
                      </el-button>
                    </el-button-group>
                    <el-button size="small" type="primary" @click="saveVersion">
                      <el-icon><Check /></el-icon> 保存版本
                    </el-button>
                  </div>
                  <div class="rich-content" v-html="activeEntry.ai_content || '<p style=&quot;color:#909399&quot;>请先点击「AI生成条目」生成内容</p>'" />
                </div>
              </el-tab-pane>

              <el-tab-pane label="版本记录" name="versions">
                <el-table :data="versions" stripe>
                  <el-table-column label="版本" width="80">
                    <template #default="{ row }">v{{ row.version }}</template>
                  </el-table-column>
                  <el-table-column label="编辑人" prop="editor_name" width="100" />
                  <el-table-column label="修订说明" prop="revision_note" />
                  <el-table-column label="时间" width="180">
                    <template #default="{ row }">{{ formatDate(row.created_at) }}</template>
                  </el-table-column>
                  <el-table-column label="操作" width="100">
                    <template #default="{ row }">
                      <el-button text type="primary" size="small" @click="restoreVersion(row)">还原</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
            </el-tabs>
          </template>
          <el-empty v-else description="请选择一个条目" />
        </div>

        <!-- 右侧：辅助面板 -->
        <div class="panel panel-assist">
          <el-tabs v-model="assistTab">
            <el-tab-pane label="历史数据" name="history">
              <div v-for="h in historyData" :key="h.id" class="history-item">
                <div class="history-year">{{ h.year }}年</div>
                <div class="history-title">{{ h.entry_title }}</div>
                <div class="history-content">{{ h.content?.substring(0, 120) }}...</div>
                <el-button text size="small" type="primary" @click="useHistoryData(h)">引用</el-button>
              </div>
              <el-empty v-if="historyData.length === 0" :image-size="60" description="暂无历史数据" />
            </el-tab-pane>

            <el-tab-pane label="AI助手" name="bot">
              <div class="bot-chat">
                <div class="bot-messages">
                  <div v-for="(msg, i) in botMessages" :key="i" class="bot-message" :class="msg.role">
                    <div class="msg-content">{{ msg.content }}</div>
                  </div>
                </div>
                <div class="bot-input">
                  <el-input v-model="botQuestion" placeholder="输入问题..." @keyup.enter="askBot">
                    <template #append>
                      <el-button :loading="botLoading" @click="askBot">发送</el-button>
                    </template>
                  </el-input>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="检测结果" name="detect">
              <div v-for="(c, i) in conflicts" :key="i" class="conflict-item">
                <el-tag :type="c.severity === 'high' ? 'danger' : c.severity === 'medium' ? 'warning' : 'info'" size="small">
                  {{ c.type }}
                </el-tag>
                <div class="conflict-desc">{{ c.description }}</div>
                <div class="conflict-loc">位置: {{ c.location }}</div>
              </div>
              <el-empty v-if="conflicts.length === 0" :image-size="60" description="暂无检测结果" />
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </template>

    <!-- 历史数据检索 -->
    <el-dialog v-model="searchHistoryVisible" title="检索历史数据" width="500px" destroy-on-close>
      <el-input v-model="historyKeyword" placeholder="输入关键词检索" @keyup.enter="searchHistory">
        <template #append>
          <el-button @click="searchHistory">检索</el-button>
        </template>
      </el-input>
    </el-dialog>

    <!-- 新建条目 -->
    <el-dialog v-model="newEntryVisible" title="新建条目" width="420px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="条目标题">
          <el-input v-model="newEntryTitle" placeholder="请输入条目标题" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="newEntryVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreateEntry">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, MagicStick, Warning, Edit, DocumentAdd, Check } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { fetchYearbooks } from '@/api/yearbook'
import { fetchOutlineTree } from '@/api/outline'
import { fetchEntries, createEntry, updateEntry, fetchVersions, createVersion, fetchHistoryData } from '@/api/entry'
import { aiGenerateEntry, aiRewrite, aiExpand, aiDetectConflicts, aiBotAnswer } from '@/api/ai'
import { STATUS_LABEL, STATUS_TYPE } from '@/types'
import type { Yearbook, OutlineNode, Entry, EntryVersion, HistoryData } from '@/types'

const auth = useAuthStore()
const yearbookList = ref<Yearbook[]>([])
const selectedYearbookId = ref('')
const selectedOutlineId = ref('')
const flatOutlines = ref<OutlineNode[]>([])

const entries = ref<Entry[]>([])
const activeEntryId = ref('')
const activeEntry = ref<Entry | null>(null)
const activeTab = ref('raw')
const assistTab = ref('history')

const versions = ref<EntryVersion[]>([])
const historyData = ref<HistoryData[]>([])
const conflicts = ref<any[]>([])

const aiLoading = ref(false)
const submitting = ref(false)

const searchHistoryVisible = ref(false)
const historyKeyword = ref('')

const newEntryVisible = ref(false)
const newEntryTitle = ref('')

const botMessages = ref<{ role: string; content: string }[]>([
  { role: 'assistant', content: '您好，我是AI助手。有什么关于年鉴编纂的问题可以问我。' },
])
const botQuestion = ref('')
const botLoading = ref(false)

function flattenTree(nodes: OutlineNode[]): OutlineNode[] {
  const result: OutlineNode[] = []
  function walk(list: OutlineNode[]) {
    for (const n of list) {
      result.push(n)
      if (n.children) walk(n.children)
    }
  }
  walk(nodes)
  return result
}

async function loadYearbooks() {
  const result = await fetchYearbooks({ page: 1, pageSize: 100 })
  yearbookList.value = result.data
}

async function onYearbookChange() {
  selectedOutlineId.value = ''
  entries.value = []
  activeEntry.value = null
  const tree = await fetchOutlineTree(selectedYearbookId.value)
  flatOutlines.value = flattenTree(tree)
}

async function loadEntries() {
  if (!selectedOutlineId.value) return
  const result = await fetchEntries({ outlineId: selectedOutlineId.value, page: 1, pageSize: 100 })
  entries.value = result.data
  activeEntry.value = null
  activeEntryId.value = ''

  try {
    historyData.value = await fetchHistoryData(
      flatOutlines.value.find((o) => o.id === selectedOutlineId.value)?.title || ''
    )
  } catch { historyData.value = [] }
}

function selectEntry(entry: Entry) {
  activeEntryId.value = entry.id
  activeEntry.value = { ...entry }
  loadVersions()
}

async function loadVersions() {
  if (!activeEntry.value) return
  try {
    versions.value = await fetchVersions(activeEntry.value.id)
  } catch { versions.value = [] }
}

async function handleSaveRaw() {
  if (!activeEntry.value) return
  try {
    await updateEntry(activeEntry.value.id, { original_content: activeEntry.value.original_content })
  } catch {}
}

async function handleAIGenerate() {
  if (!activeEntry.value) return
  aiLoading.value = true
  try {
    const result = await aiGenerateEntry(
      activeEntry.value.original_content || '',
      historyData.value.map((h) => h.content).join('\n')
    )
    activeEntry.value.ai_content = result
    await updateEntry(activeEntry.value.id, { ai_content: result, status: 'editing' })
    activeTab.value = 'ai'
    ElMessage.success('AI生成完成')

    const idx = entries.value.findIndex((e) => e.id === activeEntry.value!.id)
    if (idx >= 0) entries.value[idx].status = 'editing'
  } catch (e: any) {
    ElMessage.error(e.message || '生成失败')
  } finally {
    aiLoading.value = false
  }
}

async function handleAIRewrite() {
  if (!activeEntry.value?.ai_content) return
  aiLoading.value = true
  try {
    const result = await aiRewrite(activeEntry.value.ai_content)
    activeEntry.value.ai_content = result
    await updateEntry(activeEntry.value.id, { ai_content: result })
    ElMessage.success('润色完成')
  } catch { ElMessage.error('润色失败') }
  finally { aiLoading.value = false }
}

async function handleAIExpand() {
  if (!activeEntry.value?.ai_content) return
  aiLoading.value = true
  try {
    const result = await aiExpand(activeEntry.value.ai_content)
    activeEntry.value.ai_content = result
    await updateEntry(activeEntry.value.id, { ai_content: result })
    ElMessage.success('扩写完成')
  } catch { ElMessage.error('扩写失败') }
  finally { aiLoading.value = false }
}

async function saveVersion() {
  if (!activeEntry.value?.ai_content) return
  try {
    const nextVersion = versions.value.length > 0 ? versions.value[0].version + 1 : 1
    await createVersion({
      entry_id: activeEntry.value.id,
      version: nextVersion,
      content: activeEntry.value.ai_content,
      revision_note: `版本 v${nextVersion}`,
      editor_id: auth.user?.id,
    })
    ElMessage.success('版本已保存')
    loadVersions()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

function restoreVersion(ver: EntryVersion) {
  if (!activeEntry.value || !ver.content) return
  activeEntry.value.ai_content = ver.content
  updateEntry(activeEntry.value.id, { ai_content: ver.content })
  ElMessage.success('已还原到 v' + ver.version)
}

async function detectConflicts() {
  if (!activeEntry.value?.ai_content) {
    ElMessage.warning('请先生成内容')
    return
  }
  assistTab.value = 'detect'
  conflicts.value = await aiDetectConflicts(activeEntry.value.ai_content)
}

async function searchHistory() {
  if (!historyKeyword.value) return
  try {
    historyData.value = await fetchHistoryData(historyKeyword.value)
    searchHistoryVisible.value = false
    assistTab.value = 'history'
  } catch {}
}

function useHistoryData(h: HistoryData) {
  if (!activeEntry.value) return
  activeEntry.value.original_content = (activeEntry.value.original_content || '') + '\n\n【历史数据 ' + h.year + '年】\n' + h.content
  activeTab.value = 'raw'
  ElMessage.success('已引用历史数据')
}

function createNewEntry() {
  newEntryTitle.value = ''
  newEntryVisible.value = true
}

async function handleCreateEntry() {
  if (!newEntryTitle.value.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  submitting.value = true
  try {
    await createEntry({
      outline_id: selectedOutlineId.value,
      title: newEntryTitle.value,
      sort_order: entries.value.length,
      status: 'draft',
      created_by: auth.user?.id,
    })
    ElMessage.success('创建成功')
    newEntryVisible.value = false
    loadEntries()
  } catch (e: any) {
    ElMessage.error(e.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

async function askBot() {
  if (!botQuestion.value.trim()) return
  const q = botQuestion.value
  botMessages.value.push({ role: 'user', content: q })
  botQuestion.value = ''
  botLoading.value = true
  try {
    const answer = await aiBotAnswer(q)
    botMessages.value.push({ role: 'assistant', content: answer })
  } catch {
    botMessages.value.push({ role: 'assistant', content: '抱歉，我暂时无法回答。' })
  } finally {
    botLoading.value = false
  }
}

function formatDate(str: string): string {
  if (!str) return ''
  return new Date(str).toLocaleString('zh-CN')
}

onMounted(loadYearbooks)
</script>

<style scoped>
.smart-compile { max-width: 1400px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.page-header h2 { font-size: 22px; font-weight: 600; margin: 0; }
.header-actions { display: flex; gap: 12px; }
.empty-state { padding: 60px 0; }

.compile-layout {
  display: grid;
  grid-template-columns: 240px 1fr 280px;
  gap: 16px;
  min-height: calc(100vh - 180px);
}

.panel {
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
  flex-wrap: wrap;
  gap: 8px;
}
.panel-header h4 { margin: 0; font-size: 15px; font-weight: 600; }

.material-list { flex: 1; overflow-y: auto; }
.material-item {
  padding: 10px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: background 0.2s;
}
.material-item:hover { background: #f5f7fa; }
.material-item.active { background: #ecf5ff; border: 1px solid #409eff; }
.material-title { display: flex; align-items: center; gap: 6px; font-size: 13px; }

.editor-area { flex: 1; }
.ai-toolbar { display: flex; justify-content: space-between; margin-bottom: 12px; }
.rich-content {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 16px;
  min-height: 300px;
  line-height: 1.8;
  font-size: 14px;
}
.rich-content :deep(p) { margin: 0 0 8px; }

.history-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.history-year { font-size: 12px; color: #409eff; font-weight: 600; }
.history-title { font-size: 14px; font-weight: 500; margin: 4px 0; }
.history-content { font-size: 12px; color: #909399; line-height: 1.5; }

.conflict-item { padding: 10px; border-bottom: 1px solid #f0f0f0; }
.conflict-desc { font-size: 13px; margin: 4px 0; }
.conflict-loc { font-size: 12px; color: #909399; }

.bot-chat { display: flex; flex-direction: column; height: 400px; }
.bot-messages { flex: 1; overflow-y: auto; padding: 8px 0; }
.bot-message { margin-bottom: 12px; }
.bot-message.user .msg-content { background: #ecf5ff; color: #409eff; text-align: right; }
.bot-message.assistant .msg-content { background: #f5f7fa; }
.msg-content { padding: 8px 12px; border-radius: 8px; font-size: 13px; line-height: 1.6; display: inline-block; max-width: 100%; white-space: pre-wrap; }
.bot-input { margin-top: 8px; }
</style>
