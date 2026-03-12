<template>
  <div class="flex flex-col h-full overflow-hidden bg-slate-50">
    <!-- 顶部进度栏 -->
    <div class="bg-white border-b border-slate-200 px-5 py-3 flex items-center gap-4 flex-shrink-0">
      <div class="flex items-center gap-2 text-xs text-slate-400">
        <button class="hover:text-primary" @click="$router.push('/yearbooks')">年鉴管理</button>
        <span class="material-symbols-outlined" style="font-size:12px;">chevron_right</span>
        <span class="text-slate-600 font-medium">统稿</span>
      </div>
      <div class="flex-1 flex items-center gap-3">
        <select v-model="selectedYearbookId" class="text-xs border border-slate-300 rounded-lg px-2 py-1.5 focus:outline-none" @change="loadOutlines">
          <option value="">选择年鉴</option>
          <option v-for="yb in yearbookList" :key="yb.id" :value="yb.id">{{ yb.name }}</option>
        </select>
        <div class="flex items-center gap-2">
          <span class="text-xs text-slate-500">进度：</span>
          <div class="progress-track w-32">
            <div class="progress-fill" :style="{ width: progress + '%' }"></div>
          </div>
          <span class="text-xs font-semibold text-blue-600">{{ progress }}%</span>
        </div>
      </div>
      <button
        v-if="exportMode"
        class="flex items-center gap-1.5 px-4 py-1.5 text-xs text-white rounded-lg"
        style="background:linear-gradient(135deg,#10b981,#059669);"
        @click="doExport"
      >
        <span class="material-symbols-outlined" style="font-size:14px;">file_download</span>
        导出选中 ({{ exportSelected.size }})
      </button>
      <button
        class="flex items-center gap-1.5 px-4 py-1.5 text-xs border border-slate-300 rounded-lg text-slate-600 hover:bg-slate-50"
        @click="exportMode = !exportMode"
      >
        <span class="material-symbols-outlined" style="font-size:14px;">download</span>
        {{ exportMode ? '退出导出' : '导出文档' }}
      </button>
    </div>

    <div v-if="!selectedYearbookId" class="flex-1 flex items-center justify-center text-slate-400 text-sm">
      请先选择年鉴
    </div>

    <!-- 三栏布局 -->
    <div v-else class="flex flex-1 overflow-hidden">

      <!-- 左栏：大纲目录 -->
      <div class="flex flex-col border-r border-slate-200 bg-white overflow-hidden flex-shrink-0" style="width:240px;">
        <div class="px-4 py-3 border-b border-slate-100 flex-shrink-0">
          <div class="relative">
            <span class="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400" style="font-size:14px;">search</span>
            <input v-model="searchKeyword" type="text" placeholder="搜索章节..." class="w-full pl-8 pr-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none"/>
          </div>
        </div>
        <div v-loading="loading" class="flex-1 overflow-y-auto py-2 px-1.5">
          <div
            v-for="node in filteredOutlines"
            :key="node.id"
            class="flex items-center gap-2 py-1.5 px-2 rounded cursor-pointer transition-colors text-xs"
            :class="[
              node.level === 1 ? 'font-semibold text-slate-700 pl-2 mt-1' : 'text-slate-500 pl-4',
              selectedOutlineId === node.id ? 'bg-blue-50 text-blue-700 border border-blue-200' : 'hover:bg-slate-50'
            ]"
            @click="selectOutline(node)"
          >
            <input
              v-if="exportMode && node.level > 1"
              type="checkbox"
              :checked="exportSelected.has(node.id)"
              class="w-3 h-3 flex-shrink-0"
              @click.stop
              @change="toggleExportSelect(node.id)"
            />
            <span class="flex-1 truncate">{{ node.title }}</span>
            <span class="badge text-[10px] py-0 px-1.5 flex-shrink-0" :class="statusBadge(node.status)">{{ statusLabel(node.status) }}</span>
          </div>
        </div>
      </div>

      <!-- 中栏：内容编辑 -->
      <div class="flex-1 flex flex-col overflow-hidden">
        <div v-if="!selectedOutlineId" class="flex-1 flex items-center justify-center text-slate-400 text-sm">
          请从左侧选择章节
        </div>

        <template v-else>
          <!-- 节标题 -->
          <div class="bg-white border-b border-slate-200 px-5 py-3 flex items-center gap-3 flex-shrink-0">
            <div class="flex-1">
              <h2 class="text-sm font-bold text-slate-900">{{ selectedOutlineName }}</h2>
            </div>
            <div class="flex items-center gap-2">
              <span class="badge" :class="statusBadge(selectedEntry?.status)">{{ statusLabel(selectedEntry?.status) }}</span>
              <button class="flex items-center gap-1 px-2.5 py-1.5 text-xs border border-slate-300 rounded-lg text-slate-600 hover:bg-slate-50" @click="versionVisible=true">
                <span class="material-symbols-outlined" style="font-size:13px;">history</span>版本
              </button>
              <button class="flex items-center gap-1 px-2.5 py-1.5 text-xs border border-slate-300 rounded-lg text-slate-600 hover:bg-slate-50" @click="annotationVisible=!annotationVisible">
                <span class="material-symbols-outlined" style="font-size:13px;">comment</span>
                批注 {{ annotations.length > 0 ? `(${annotations.length})` : '' }}
              </button>
            </div>
          </div>

          <!-- 工具栏 -->
          <div class="bg-white border-b border-slate-100 px-3 py-1.5 flex items-center gap-0.5 flex-shrink-0">
            <button class="toolbar-btn font-bold text-sm" @click="execCmd('bold')">B</button>
            <button class="toolbar-btn italic text-sm" @click="execCmd('italic')">I</button>
            <button class="toolbar-btn underline text-xs" @click="execCmd('underline')">U</button>
            <div class="w-px h-5 bg-slate-200 mx-1"></div>
            <div class="ml-auto flex items-center gap-2">
              <span class="text-xs text-slate-400">{{ wordCount }} 字</span>
              <button
                class="flex items-center gap-1 px-3 py-1 text-xs border border-slate-300 rounded-lg text-slate-600 hover:bg-slate-50"
                @click="saveContent"
              >
                <span class="material-symbols-outlined" style="font-size:13px;">save</span>保存
              </button>
              <button
                class="flex items-center gap-1 px-3 py-1 text-xs text-white rounded-lg hover:shadow"
                style="background:linear-gradient(135deg,#10b981,#059669);"
                @click="submitContent"
              >
                <span class="material-symbols-outlined" style="font-size:13px;">send</span>提交
              </button>
            </div>
          </div>

          <!-- 编辑区 -->
          <div class="flex-1 overflow-y-auto p-5">
            <div class="bg-white rounded-xl border border-slate-200 p-6 shadow-sm min-h-full">
              <div v-if="!selectedEntry" class="text-center text-slate-400 text-sm py-8">暂无内容，请前往智能编纂生成条目</div>
              <div
                v-else
                ref="editorRef"
                class="rich-editor"
                contenteditable="true"
                :data-placeholder="'开始编辑内容...'"
                @input="onInput"
                @blur="saveContent"
              ></div>
            </div>
          </div>
        </template>
      </div>

      <!-- 右栏：批注面板 -->
      <div
        v-if="annotationVisible"
        class="flex flex-col border-l border-slate-200 bg-white overflow-hidden flex-shrink-0 fade-in"
        style="width:260px;"
      >
        <div class="px-4 py-3 border-b border-slate-100 flex items-center justify-between flex-shrink-0">
          <h3 class="text-sm font-semibold text-slate-800">批注 ({{ annotations.length }})</h3>
          <button class="text-slate-400 hover:text-slate-600" @click="annotationVisible=false">
            <span class="material-symbols-outlined" style="font-size:18px;">close</span>
          </button>
        </div>

        <!-- 添加批注 -->
        <div class="px-4 py-3 border-b border-slate-100 flex-shrink-0">
          <textarea
            v-model="newAnnotation"
            rows="3"
            maxlength="200"
            placeholder="输入批注内容（最多200字）..."
            class="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-blue-400 resize-none"
          ></textarea>
          <button
            :disabled="!newAnnotation.trim()"
            class="w-full mt-2 py-1.5 text-xs text-white rounded-lg disabled:opacity-50"
            style="background:#1a90ff;"
            @click="addAnnotation"
          >提交批注</button>
        </div>

        <!-- 批注列表 -->
        <div class="flex-1 overflow-y-auto p-3 space-y-3">
          <div v-if="annotations.length === 0" class="text-center text-slate-400 text-xs py-4">暂无批注</div>
          <div v-for="ann in annotations" :key="ann.id" class="bg-slate-50 rounded-xl p-3 border border-slate-200">
            <div class="flex items-center gap-2 mb-1.5">
              <div class="w-5 h-5 rounded-full flex items-center justify-center text-white text-[10px] font-bold bg-blue-500 flex-shrink-0">
                {{ ann.author_name?.charAt(0) || '?' }}
              </div>
              <span class="text-xs font-medium text-slate-700">{{ ann.author_name || '匿名' }}</span>
              <span class="ml-auto text-[10px] text-slate-400">{{ formatDate(ann.created_at) }}</span>
            </div>
            <p class="text-xs text-slate-600 leading-5">{{ ann.content }}</p>
            <div class="flex items-center justify-between mt-2">
              <span
                class="badge text-[10px]"
                :class="ann.process_status === 'processed' ? 'badge-green' : 'badge-yellow'"
              >{{ ann.process_status === 'processed' ? '已处理' : '待处理' }}</span>
              <div class="flex gap-2">
                <button
                  v-if="ann.author_id === auth.user?.id"
                  class="text-[11px] text-slate-400 hover:text-red-500"
                  @click="deleteAnnotation(ann.id)"
                >删除</button>
                <button
                  v-if="ann.author_id !== auth.user?.id && ann.process_status === 'pending'"
                  class="text-[11px] text-primary hover:underline"
                  @click="markProcessed(ann.id)"
                >处理完毕</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 版本历史弹窗 -->
    <div v-if="versionVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="versionVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg mx-4 fade-in">
        <div class="flex items-center justify-between px-6 py-4 border-b">
          <h2 class="text-base font-semibold">版本历史</h2>
          <button @click="versionVisible=false" class="p-1 rounded text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="px-6 py-4 max-h-96 overflow-y-auto">
          <div v-if="versions.length === 0" class="text-center text-slate-400 text-sm py-8">暂无版本记录</div>
          <div v-else class="space-y-3">
            <div v-for="(v, i) in versions" :key="v.id" class="flex items-start gap-3 p-3 rounded-xl" :class="i === 0 ? 'bg-blue-50 border border-blue-200' : 'bg-slate-50'">
              <div class="w-8 h-8 rounded-full flex items-center justify-center text-white text-xs font-bold flex-shrink-0" style="background:#1a90ff;">v{{ v.version }}</div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-700">{{ v.editor_name || '系统' }}</p>
                <p class="text-xs text-slate-400 mt-0.5">{{ v.created_at?.substring(0, 16).replace('T', ' ') }}</p>
                <p v-if="v.revision_note" class="text-xs text-slate-500 mt-1">{{ v.revision_note }}</p>
              </div>
              <div>
                <span v-if="i === 0" class="text-xs text-blue-600 font-medium">当前</span>
                <button v-else class="text-xs text-primary hover:underline" @click="restoreVersion(v)">回滚</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useYearbookStore } from '@/stores/yearbook'
import { fetchYearbooks } from '@/api/yearbook'
import { fetchOutlineFlat } from '@/api/outline'
import { fetchEntries, updateEntry, fetchVersions, createVersion, fetchAnnotations, createAnnotation, updateAnnotation, deleteAnnotation as deleteAnnotationApi } from '@/api/entry'
import type { Yearbook, OutlineNode, Entry, EntryVersion, Annotation } from '@/types'

const router = useRouter()
const auth = useAuthStore()
const yearbookStore = useYearbookStore()

const loading = ref(false)
const yearbookList = ref<Yearbook[]>([])
const selectedYearbookId = ref(yearbookStore.currentYearbookId || '')
const flatOutlines = ref<OutlineNode[]>([])
const selectedOutlineId = ref('')
const selectedOutlineName = ref('')
const progress = ref(0)
const searchKeyword = ref('')

const filteredOutlines = computed(() => {
  if (!searchKeyword.value) return flatOutlines.value
  return flatOutlines.value.filter(n => n.title.toLowerCase().includes(searchKeyword.value.toLowerCase()))
})

const selectedEntry = ref<Entry | null>(null)
const editorRef = ref<HTMLElement>()
const wordCount = computed(() => editorRef.value?.innerText?.length || 0)

const versions = ref<EntryVersion[]>([])
const annotations = ref<Annotation[]>([])
const newAnnotation = ref('')
const annotationVisible = ref(false)
const versionVisible = ref(false)

const exportMode = ref(false)
const exportSelected = ref(new Set<string>())

async function loadYearbooks() {
  const result = await fetchYearbooks({ page: 1, pageSize: 100 })
  yearbookList.value = result.data
}

async function loadOutlines() {
  if (!selectedYearbookId.value) return
  loading.value = true
  try {
    flatOutlines.value = await fetchOutlineFlat(selectedYearbookId.value)
    const submitted = flatOutlines.value.filter(n => n.status === 'submitted').length
    const total = flatOutlines.value.filter(n => n.level > 1).length
    progress.value = total > 0 ? Math.round(submitted / total * 100) : 0
    if (flatOutlines.value.length > 0) {
      const first = flatOutlines.value.find(n => n.level > 1) || flatOutlines.value[0]
      await selectOutline(first)
    }
  } finally { loading.value = false }
}

async function selectOutline(node: OutlineNode) {
  selectedOutlineId.value = node.id
  selectedOutlineName.value = node.title
  const result = await fetchEntries({ outlineId: node.id, page: 1, pageSize: 1 })
  selectedEntry.value = result.data[0] || null
  await nextTick()
  if (editorRef.value && selectedEntry.value) {
    editorRef.value.innerHTML = selectedEntry.value.ai_content || selectedEntry.value.original_content || ''
  }
  try {
    if (selectedEntry.value) {
      [versions.value, annotations.value] = await Promise.all([
        fetchVersions(selectedEntry.value.id),
        fetchAnnotations(selectedEntry.value.id),
      ])
    }
  } catch {}
}

function onInput() {}

async function saveContent() {
  if (!selectedEntry.value || !editorRef.value) return
  const content = editorRef.value.innerHTML
  try {
    await updateEntry(selectedEntry.value.id, { ai_content: content })
    ElMessage.success('已保存')
  } catch {}
}

async function submitContent() {
  if (!selectedEntry.value || !editorRef.value) return
  try {
    const content = editorRef.value.innerHTML
    const nextVer = (versions.value[0]?.version || 0) + 1
    await Promise.all([
      updateEntry(selectedEntry.value.id, { ai_content: content, status: 'submitted' }),
      createVersion({ entry_id: selectedEntry.value.id, version: nextVer, content, editor_id: auth.user?.id }),
    ])
    selectedEntry.value.status = 'submitted'
    versions.value = await fetchVersions(selectedEntry.value.id)
    const outline = flatOutlines.value.find(n => n.id === selectedOutlineId.value)
    if (outline) outline.status = 'submitted'
    const submitted = flatOutlines.value.filter(n => n.status === 'submitted').length
    const total = flatOutlines.value.filter(n => n.level > 1).length
    progress.value = total > 0 ? Math.round(submitted / total * 100) : 0
    ElMessage.success('已提交')
  } catch (e: any) { ElMessage.error(e.message || '提交失败') }
}

async function addAnnotation() {
  if (!selectedEntry.value || !newAnnotation.value.trim()) return
  try {
    await createAnnotation({
      entry_id: selectedEntry.value.id, content: newAnnotation.value,
      author_id: auth.user?.id, annotation_type: 'comment',
    })
    newAnnotation.value = ''
    annotations.value = await fetchAnnotations(selectedEntry.value.id)
    ElMessage.success('批注已添加')
  } catch (e: any) { ElMessage.error(e.message || '添加失败') }
}

async function deleteAnnotation(id: string) {
  try {
    await deleteAnnotationApi(id)
    if (selectedEntry.value) annotations.value = await fetchAnnotations(selectedEntry.value.id)
  } catch {}
}

async function markProcessed(id: string) {
  try {
    await updateAnnotation(id, { process_status: 'processed' })
    if (selectedEntry.value) annotations.value = await fetchAnnotations(selectedEntry.value.id)
  } catch {}
}

function restoreVersion(ver: EntryVersion) {
  if (!editorRef.value) return
  editorRef.value.innerHTML = ver.content
  saveContent()
  ElMessage.success(`已回滚到 v${ver.version}`)
}

function execCmd(cmd: string) { document.execCommand(cmd, false); editorRef.value?.focus() }

function toggleExportSelect(id: string) {
  if (exportSelected.value.has(id)) exportSelected.value.delete(id)
  else exportSelected.value.add(id)
}

function doExport() {
  const selected = flatOutlines.value.filter(n => exportSelected.value.has(n.id))
  if (selected.length === 0) { ElMessage.warning('请至少选择一个章节'); return }
  ElMessage.success(`准备导出 ${selected.length} 个章节（Word 功能需后端支持）`)
  exportMode.value = false
  exportSelected.value.clear()
}

function statusLabel(s?: string) {
  return { not_started: '未开始', in_progress: '编纂中', submitted: '已提交', editing: '编辑中' }[s || ''] || '未开始'
}
function statusBadge(s?: string) {
  return { not_started: 'badge-gray', in_progress: 'badge-yellow', submitted: 'badge-green', editing: 'badge-blue' }[s || ''] || 'badge-gray'
}
function formatDate(d: string) { return d ? d.substring(0, 10) : '' }

onMounted(async () => {
  await loadYearbooks()
  if (selectedYearbookId.value) await loadOutlines()
})
</script>

<style scoped>
.rich-editor:empty:before {
  content: attr(data-placeholder);
  color: #9ca3af;
  pointer-events: none;
}
</style>
