<template>
  <div class="h-screen flex flex-col overflow-hidden bg-[#f5f7f8]">
    <!-- No yearbook selected -->
    <div
      v-if="!yearbookStore.currentYearbook"
      class="flex-1 flex flex-col items-center justify-center text-slate-500"
    >
      <span class="material-symbols-outlined text-7xl mb-4 text-slate-300">edit_note</span>
      <p class="text-lg">请先从年鉴管理选择需要统稿的年鉴</p>
    </div>

    <template v-else>
      <!-- Top progress bar -->
      <div class="flex-shrink-0 bg-white border-b border-slate-200 px-6 py-3">
        <div class="flex items-center gap-4">
          <span class="text-slate-700 font-medium">正在编纂：{{ yearbookStore.currentYearbook?.name || '未命名年鉴' }}</span>
          <div class="flex-1 max-w-md h-2 bg-slate-200 rounded-full overflow-hidden">
            <div
              class="h-full rounded-full transition-all"
              :style="{ width: progress + '%', backgroundColor: '#1a90ff' }"
            />
          </div>
          <span class="text-sm text-slate-600 font-medium">{{ progress }}%</span>
        </div>
      </div>

      <!-- Main layout: sidebar + content -->
      <div class="flex-1 flex min-h-0 overflow-hidden">
        <!-- Left sidebar (25%, min-300px) -->
        <aside class="w-[25%] min-w-[300px] flex-shrink-0 flex flex-col border-r border-slate-200 bg-white">
          <div class="flex items-center justify-between px-4 py-3 border-b border-slate-200">
            <span class="font-medium text-slate-800">大纲目录</span>
            <button class="p-2 rounded-lg hover:bg-slate-100 text-slate-600 transition-colors">
              <span class="material-symbols-outlined">filter_list</span>
            </button>
          </div>
          <div class="flex-1 overflow-y-auto custom-scrollbar p-2 relative">
            <!-- Export mode overlay -->
            <div
              v-if="exportMode"
              class="absolute inset-0 z-10 bg-white/95 backdrop-blur-sm flex flex-col p-4 rounded-lg"
            >
              <p class="text-sm text-slate-600 mb-3">选择要导出的章节：</p>
              <div class="flex-1 overflow-y-auto space-y-2">
                <label
                  v-for="item in flatOutlineItems"
                  :key="item.id"
                  class="flex items-center gap-2 py-2 px-3 rounded-lg hover:bg-slate-50 cursor-pointer"
                >
                  <input
                    type="checkbox"
                    :value="item.id"
                    v-model="exportSelectedIds"
                    class="rounded text-primary"
                  />
                  <span class="text-sm text-slate-700">{{ item.title }}</span>
                </label>
              </div>
              <button
                @click="confirmExport"
                class="mt-4 w-full py-2.5 rounded-lg text-white font-medium transition-colors"
                style="background-color: #1a90ff"
              >
                确认导出
              </button>
            </div>
            <!-- Tree structure -->
            <div v-else class="space-y-0.5">
              <div
                v-for="item in outlineTree"
                :key="item.id"
                class="outline-tree-item"
              >
                <ProofreadOutlineTreeNode
                  :item="item"
                  :selected-id="selectedOutlineId"
                  @select="selectOutline"
                  @smart-compile="handleSmartCompile"
                  @merge="openMergeDialog"
                  @split="openSplitDialog"
                />
              </div>
            </div>
          </div>
          <div class="p-4 border-t border-slate-200">
            <button
              @click="toggleExportMode"
              class="w-full py-2.5 rounded-lg text-white font-medium transition-colors flex items-center justify-center gap-2"
              style="background-color: #1a90ff"
            >
              <span class="material-symbols-outlined">download</span>
              导出全文
            </button>
          </div>
        </aside>

        <!-- Right content (75%) -->
        <div class="flex-1 flex min-h-0 overflow-hidden">
          <!-- Editor area -->
          <div class="flex-1 flex flex-col min-w-0 bg-white">
            <!-- Editor header -->
            <div class="flex items-center justify-between px-4 py-2 border-b border-slate-200 bg-slate-50">
              <h3 class="font-medium text-slate-800">{{ selectedOutline?.title || '请选择章节' }}</h3>
              <div class="flex items-center gap-2">
                <span class="text-xs text-slate-500">最后保存：{{ lastSaveTime }}</span>
                <button class="px-3 py-1.5 rounded-lg border border-slate-200 text-sm hover:bg-slate-100 transition-colors">
                  批注
                </button>
                <button class="px-3 py-1.5 rounded-lg border border-slate-200 text-sm hover:bg-slate-100 transition-colors">
                  版本历史
                </button>
              </div>
            </div>
            <!-- Rich text toolbar -->
            <div class="flex items-center gap-1 px-4 py-2 border-b border-slate-200 bg-white">
              <button class="p-2 rounded hover:bg-slate-100" title="粗体">
                <span class="material-symbols-outlined text-lg">format_bold</span>
              </button>
              <button class="p-2 rounded hover:bg-slate-100" title="斜体">
                <span class="material-symbols-outlined text-lg">format_italic</span>
              </button>
              <button class="p-2 rounded hover:bg-slate-100" title="下划线">
                <span class="material-symbols-outlined text-lg">format_underlined</span>
              </button>
              <div class="w-px h-6 bg-slate-200 mx-1" />
              <button class="p-2 rounded hover:bg-slate-100" title="左对齐">
                <span class="material-symbols-outlined text-lg">format_align_left</span>
              </button>
              <button class="p-2 rounded hover:bg-slate-100" title="居中">
                <span class="material-symbols-outlined text-lg">format_align_center</span>
              </button>
              <button class="p-2 rounded hover:bg-slate-100" title="右对齐">
                <span class="material-symbols-outlined text-lg">format_align_right</span>
              </button>
              <div class="w-px h-6 bg-slate-200 mx-1" />
              <button class="p-2 rounded hover:bg-slate-100" title="插入图片">
                <span class="material-symbols-outlined text-lg">image</span>
              </button>
              <button class="p-2 rounded hover:bg-slate-100" title="插入表格">
                <span class="material-symbols-outlined text-lg">table_chart</span>
              </button>
              <button class="p-2 rounded hover:bg-slate-100" title="插入链接">
                <span class="material-symbols-outlined text-lg">link</span>
              </button>
            </div>
            <!-- Editor area -->
            <div class="flex-1 overflow-y-auto p-6">
              <div
                class="prose prose-slate max-w-none min-h-[300px] rounded-lg border border-slate-200 p-6 focus-within:ring-2 focus-within:ring-primary/20 focus-within:border-primary outline-none"
                contenteditable="true"
                @input="onEditorInput"
              >
                <div v-html="editorContent" class="outline-none" />
              </div>
            </div>
            <!-- Footer buttons -->
            <div class="flex items-center gap-3 px-4 py-3 border-t border-slate-200 bg-slate-50">
              <button
                class="px-4 py-2 rounded-lg border border-slate-200 hover:bg-slate-100 transition-colors"
                @click="saveDraft"
              >
                保存草稿
              </button>
              <button
                class="px-4 py-2 rounded-lg text-white font-medium transition-colors"
                style="background-color: #1a90ff"
                @click="submitReview"
              >
                提交审阅
              </button>
            </div>
          </div>

          <!-- Right comments panel (w-80) -->
          <div class="w-80 flex-shrink-0 flex flex-col border-l border-slate-200 bg-slate-50">
            <div class="px-4 py-3 border-b border-slate-200 font-medium text-slate-800">
              当前批注 ({{ comments.length }})
            </div>
            <div class="flex-1 overflow-y-auto custom-scrollbar p-3 space-y-3">
              <div
                v-for="c in comments"
                :key="c.id"
                :class="[
                  'rounded-lg p-3 border',
                  c.isMine ? 'border-primary bg-primary/5' : 'border-slate-200 bg-white'
                ]"
              >
                <div class="flex items-center justify-between mb-1">
                  <span class="text-sm font-medium text-slate-700">{{ c.author }}</span>
                  <span class="text-xs text-slate-500">{{ c.time }}</span>
                </div>
                <p class="text-sm text-slate-600">{{ c.text }}</p>
                <div v-if="!c.isMine" class="flex gap-2 mt-2">
                  <button class="text-xs px-2 py-1 rounded bg-primary/20 text-primary hover:bg-primary/30">采纳</button>
                  <button class="text-xs px-2 py-1 rounded bg-slate-200 text-slate-600 hover:bg-slate-300">忽略</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Split dialog -->
      <Teleport to="body">
        <div
          v-if="showSplitDialog"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
          @click.self="showSplitDialog = false"
        >
          <div class="bg-white rounded-xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
            <div class="flex items-center justify-between p-4 border-b border-slate-200">
              <h3 class="text-lg font-semibold text-slate-900">拆分章节</h3>
              <button @click="showSplitDialog = false" class="p-2 rounded-lg hover:bg-slate-100">
                <span class="material-symbols-outlined">close</span>
              </button>
            </div>
            <div class="flex-1 flex min-h-0 p-4 gap-4 overflow-hidden">
              <div class="w-1/2 flex flex-col">
                <p class="text-sm font-medium text-slate-700 mb-2">原文</p>
                <div class="flex-1 overflow-y-auto p-4 rounded-lg border border-slate-200 bg-slate-50 text-sm text-slate-600">
                  {{ splitOriginalText }}
                </div>
              </div>
              <div class="w-1/2 flex flex-col">
                <p class="text-sm font-medium text-slate-700 mb-2">拆分后 (最多 5 项)</p>
                <div class="flex-1 overflow-y-auto space-y-2">
                  <input
                    v-for="(part, i) in splitParts"
                    :key="i"
                    v-model="splitParts[i]"
                    type="text"
                    placeholder="输入拆分后的标题"
                    class="w-full px-3 py-2 rounded-lg border border-slate-200 focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none text-sm"
                  />
                  <button
                    v-if="splitParts.length < 5"
                    @click="splitParts.push('')"
                    class="w-full py-2 rounded-lg border-2 border-dashed border-slate-200 text-slate-500 hover:border-primary hover:text-primary text-sm flex items-center justify-center gap-1"
                  >
                    <span class="material-symbols-outlined text-lg">add</span>
                    添加
                  </button>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 p-4 border-t border-slate-200">
              <button @click="showSplitDialog = false" class="px-4 py-2 rounded-lg border border-slate-200 hover:bg-slate-50">取消</button>
              <button @click="confirmSplit" class="px-4 py-2 rounded-lg text-white" style="background-color: #1a90ff">确认拆分</button>
            </div>
          </div>
        </div>
      </Teleport>

      <!-- Merge dialog -->
      <Teleport to="body">
        <div
          v-if="showMergeDialog"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
          @click.self="showMergeDialog = false"
        >
          <div class="bg-white rounded-xl shadow-2xl max-w-md w-full p-6">
            <h3 class="text-lg font-semibold text-slate-900 mb-4">合并章节</h3>
            <p class="text-sm text-slate-600 mb-3">选择要合并的另一个子项：</p>
            <div class="space-y-2 max-h-48 overflow-y-auto mb-4">
              <button
                v-for="sib in mergeSiblings"
                :key="sib.id"
                @click="mergeTargetId = sib.id"
                :class="[
                  'w-full text-left px-4 py-2 rounded-lg border transition-colors',
                  mergeTargetId === sib.id ? 'border-primary bg-primary/10 text-primary' : 'border-slate-200 hover:bg-slate-50'
                ]"
              >
                {{ sib.title }}
              </button>
            </div>
            <div class="flex justify-end gap-3">
              <button @click="showMergeDialog = false" class="px-4 py-2 rounded-lg border border-slate-200 hover:bg-slate-50">取消</button>
              <button @click="confirmMerge" :disabled="!mergeTargetId" class="px-4 py-2 rounded-lg text-white disabled:opacity-50" style="background-color: #1a90ff">
                确认合并
              </button>
            </div>
          </div>
        </div>
      </Teleport>
    </template>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useYearbookStore } from '@/stores/yearbook'
import { useAuthStore } from '@/stores/auth'
import ProofreadOutlineTreeNode from '@/components/ProofreadOutlineTreeNode.vue'

const yearbookStore = useYearbookStore()
const auth = useAuthStore()

const progress = ref(42)
const selectedOutlineId = ref(null)
const lastSaveTime = ref('--')
const editorContent = ref('<p>一、工作概况</p><p>本单位2024年度围绕中心工作，扎实推进各项任务落实。<mark class="bg-yellow-200">重点完成</mark>了以下工作：</p><p>二、主要成效</p><p>1. 完成重点项目若干项</p><p>2. 开展专题活动多次</p><p>三、下一步计划</p><p>继续深化各项工作...</p>')
const exportMode = ref(false)
const exportSelectedIds = ref([])
const showSplitDialog = ref(false)
const showMergeDialog = ref(false)
const splitOriginalText = ref('')
const splitParts = ref(['', ''])
const mergeTargetId = ref(null)
const mergeSourceItem = ref(null)

// Mock outline tree
const outlineTree = ref([
  {
    id: 'ch1',
    title: '第一章 概述',
    status: 'submitted',
    version: 2,
    hasPermission: true,
    children: [
      { id: 'ch1-1', title: '1.1 背景介绍', status: 'submitted', version: 1, hasPermission: true, children: [] },
      { id: 'ch1-2', title: '1.2 工作目标', status: 'in_progress', version: 2, hasPermission: true, children: [] },
      { id: 'ch1-3', title: '1.3 组织架构', status: 'not_started', version: 0, hasPermission: false, children: [] },
    ]
  },
  {
    id: 'ch2',
    title: '第二章 主要工作',
    status: 'in_progress',
    version: 1,
    hasPermission: true,
    children: [
      { id: 'ch2-1', title: '2.1 重点工作', status: 'in_progress', version: 1, hasPermission: true, children: [] },
      { id: 'ch2-2', title: '2.2 创新举措', status: 'not_started', version: 0, hasPermission: true, children: [] },
    ]
  },
])

const flatOutlineItems = computed(() => {
  const flat = []
  function walk(items) {
    for (const item of items) {
      flat.push(item)
      if (item.children?.length) walk(item.children)
    }
  }
  walk(outlineTree.value)
  return flat
})

const selectedOutline = computed(() => {
  const find = (items, id) => {
    for (const item of items) {
      if (item.id === id) return item
      const found = find(item.children || [], id)
      if (found) return found
    }
    return null
  }
  return find(outlineTree.value, selectedOutlineId.value)
})

const mergeSiblings = computed(() => {
  if (!mergeSourceItem.value) return []
  const parent = outlineTree.value.find(p => p.children?.some(c => c.id === mergeSourceItem.value.id))
    || outlineTree.value.flatMap(p => p.children || []).find(p => p.children?.some(c => c.id === mergeSourceItem.value.id))
  const siblings = parent?.children || outlineTree.value
  return siblings.filter(s => s.id !== mergeSourceItem.value?.id)
})

// Mock comments
const comments = ref([
  { id: 1, author: auth.userName || '我', text: '建议补充数据支撑', time: '10:30', isMine: true },
  { id: 2, author: '李四', text: '此处表述可再精简', time: '09:15', isMine: false },
])

function selectOutline(item) {
  selectedOutlineId.value = item.id
  yearbookStore.setCurrentOutline(item)
}

function handleSmartCompile(item) {
  yearbookStore.setCurrentOutline(item)
  // Navigate to smart compile - could use router
}

function toggleExportMode() {
  exportMode.value = !exportMode.value
  if (exportMode.value) exportSelectedIds.value = flatOutlineItems.value.map(i => i.id)
}

function confirmExport() {
  exportMode.value = false
  // Mock export
}

function openSplitDialog(item) {
  mergeSourceItem.value = item
  splitOriginalText.value = '本单位2024年度围绕中心工作，扎实推进各项任务落实。重点完成了以下工作...'
  splitParts.value = ['第一部分', '第二部分']
  showSplitDialog.value = true
}

function confirmSplit() {
  showSplitDialog.value = false
}

function openMergeDialog(item) {
  mergeSourceItem.value = item
  mergeTargetId.value = null
  showMergeDialog.value = true
}

function confirmMerge() {
  showMergeDialog.value = false
  mergeTargetId.value = null
}

function onEditorInput() {
  lastSaveTime.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function saveDraft() {
  lastSaveTime.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function submitReview() {
  // Mock submit
}
</script>
