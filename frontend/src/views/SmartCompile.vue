<template>
  <div class="h-screen flex flex-col overflow-hidden bg-[#f5f7f8]">
    <!-- No outline selected -->
    <div
      v-if="!yearbookStore.currentOutline"
      class="flex-1 flex flex-col items-center justify-center text-slate-500"
    >
      <span class="material-symbols-outlined text-7xl mb-4 text-slate-300">psychology</span>
      <p class="text-lg mb-6">请先从大纲管理选择需要编纂的内容</p>
      <router-link
        to="/outlines"
        class="inline-flex items-center gap-2 px-5 py-2.5 rounded-lg bg-primary text-white font-medium hover:bg-primary/90 transition-all"
      >
        <span class="material-symbols-outlined">arrow_back</span>
        返回大纲管理
      </router-link>
    </div>

    <!-- Three column layout -->
    <template v-else>
      <!-- Top bar -->
      <div class="flex-shrink-0 bg-white border-b border-slate-200 px-6 py-3">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <span class="text-slate-700 font-medium">正在编纂：{{ yearbookStore.currentYearbook?.name || '未命名年鉴' }}</span>
            <div class="w-48 h-2 bg-slate-200 rounded-full overflow-hidden">
              <div class="h-full bg-primary rounded-full transition-all" :style="{ width: progress + '%' }" />
            </div>
            <span class="text-sm text-slate-600">{{ progress }}%</span>
          </div>
          <div class="flex items-center gap-2">
            <router-link
              :to="{ name: 'EntryManage' }"
              class="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white font-medium hover:bg-primary/90 transition-all"
            >
              <span class="material-symbols-outlined text-xl">list_alt</span>
              生成条目管理 ({{ entryCount }})
            </router-link>
            <button
              class="inline-flex items-center gap-2 px-4 py-2 rounded-lg border border-slate-200 text-slate-700 font-medium hover:bg-slate-50 hover:border-primary/50 hover:text-primary transition-all"
              @click="handleRobotAssistant"
            >
              <span class="material-symbols-outlined text-xl">smart_toy</span>
              机器人助理
            </button>
          </div>
        </div>
        <div class="mt-2 flex items-center gap-2 text-sm text-slate-600">
          <span>{{ yearbookStore.currentYearbook?.name || '年鉴' }}</span>
          <span class="material-symbols-outlined text-base">chevron_right</span>
          <span>{{ outline?.section_name || '章节' }}</span>
          <span class="material-symbols-outlined text-base">chevron_right</span>
          <span>{{ outline?.subsection_name || '子章节' }}</span>
          <span class="material-symbols-outlined text-base">chevron_right</span>
          <span class="text-slate-900 font-medium">{{ outline?.title || '当前' }}</span>
        </div>
      </div>

      <!-- Main content -->
      <div class="flex-1 flex min-h-0 overflow-hidden">
        <!-- Left column: 资料列表 -->
        <div class="w-1/5 min-w-[240px] flex-shrink-0 flex flex-col border-r border-slate-200 bg-white">
          <div class="px-4 py-3 border-b border-slate-200 font-medium text-slate-800 flex items-center gap-2">
            资料列表
            <span class="px-2 py-0.5 rounded-full bg-slate-100 text-slate-600 text-xs">{{ totalMaterialCount }}</span>
          </div>
          <div class="flex-1 overflow-y-auto custom-scrollbar">
            <!-- 切分后资料 -->
            <div class="border-b border-slate-100">
              <button
                @click="splitCollapsed = !splitCollapsed"
                class="w-full flex items-center justify-between px-4 py-2.5 text-left text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors"
              >
                <span>切分后资料 ({{ splitMaterials.length }})</span>
                <span class="material-symbols-outlined text-lg transition-transform" :class="{ 'rotate-180': !splitCollapsed }">expand_more</span>
              </button>
              <div v-show="!splitCollapsed" class="pb-2">
                <div
                  v-for="item in splitMaterials"
                  :key="item.id"
                  @click="selectedMaterial = item"
                  :class="[
                    'flex items-center gap-2 px-4 py-2 mx-2 rounded-r-lg cursor-pointer transition-all border-l-2',
                    selectedMaterial?.id === item.id ? 'bg-primary/10 border-primary text-primary' : 'border-transparent hover:bg-slate-100 text-slate-700'
                  ]"
                >
                  <span class="material-symbols-outlined text-xl shrink-0">description</span>
                  <span class="truncate flex-1 text-sm">{{ item.name }}</span>
                </div>
              </div>
            </div>
            <!-- 上传资料 -->
            <div>
              <button
                @click="uploadCollapsed = !uploadCollapsed"
                class="w-full flex items-center justify-between px-4 py-2.5 text-left text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors"
              >
                <span>上传资料 ({{ uploadMaterials.length }})</span>
                <span class="material-symbols-outlined text-lg transition-transform" :class="{ 'rotate-180': !uploadCollapsed }">expand_more</span>
              </button>
              <div v-show="!uploadCollapsed" class="pb-2">
                <div
                  v-for="item in uploadMaterials"
                  :key="item.id"
                  @click="selectedMaterial = item"
                  :class="[
                    'flex items-center gap-2 px-4 py-2 mx-2 rounded-r-lg cursor-pointer transition-all border-l-2',
                    selectedMaterial?.id === item.id ? 'bg-primary/10 border-primary text-primary' : 'border-transparent hover:bg-slate-100 text-slate-700'
                  ]"
                >
                  <span class="material-symbols-outlined text-xl shrink-0">description</span>
                  <span class="truncate flex-1 text-sm">{{ item.name }}</span>
                </div>
              </div>
            </div>
          </div>
          <button
            class="m-4 py-2.5 rounded-lg border-2 border-dashed border-slate-300 text-slate-600 text-sm font-medium hover:border-primary hover:text-primary transition-all flex items-center justify-center gap-2"
            @click="showUploadDialog = true"
          >
            <span class="material-symbols-outlined text-xl">upload_file</span>
            上传资料
          </button>
        </div>

        <!-- Middle column: 资料预览 -->
        <div class="w-2/5 flex-shrink-0 flex flex-col bg-slate-100 p-4 overflow-hidden">
          <div class="mb-3 font-medium text-slate-800">资料预览</div>
          <div class="flex-1 overflow-y-auto custom-scrollbar">
            <div class="bg-white rounded-xl shadow-sm p-6 min-h-[200px] border border-slate-200">
              <template v-if="selectedMaterial">
                <h3 class="text-lg font-semibold text-slate-900 mb-4">{{ selectedMaterial.name }}</h3>
                <div class="text-slate-600 text-sm leading-relaxed whitespace-pre-wrap">{{ selectedMaterial.preview }}</div>
              </template>
              <template v-else>
                <div class="flex flex-col items-center justify-center h-48 text-slate-400">
                  <span class="material-symbols-outlined text-5xl mb-2">description</span>
                  <p class="text-sm">请从左侧选择资料</p>
                </div>
              </template>
            </div>
          </div>
        </div>

        <!-- Right column: 条目生成 -->
        <div class="w-2/5 flex-shrink-0 flex flex-col bg-white border-l border-slate-200 p-4 overflow-hidden">
          <div class="mb-3 font-medium text-slate-800">条目生成</div>
          <div class="flex-1 flex flex-col min-h-0">
            <label class="text-sm text-slate-600 mb-1.5 block">条目原始数据</label>
            <textarea
              v-model="entryRawData"
              class="h-40 w-full px-4 py-3 rounded-lg border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none resize-y text-sm"
              placeholder="请粘贴会议纪要、活动总结等原始资料内容，系统将自动识别并生成年鉴条目..."
            />
            <div class="mt-4">
              <label class="text-sm text-slate-600 mb-2 block">选择往年历史数据</label>
              <input
                v-model="historySearch"
                type="text"
                placeholder="搜索历史条目..."
                class="w-full px-4 py-2 rounded-lg border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none text-sm mb-2"
              />
              <div class="max-h-40 overflow-y-auto custom-scrollbar border border-slate-200 rounded-lg p-2">
                <label
                  v-for="item in filteredHistory"
                  :key="item.id"
                  class="flex items-center gap-2 py-1.5 px-2 rounded hover:bg-slate-50 cursor-pointer"
                >
                  <input type="checkbox" :value="item.id" v-model="selectedHistory" class="rounded text-primary" />
                  <span class="text-sm text-slate-700">{{ item.title }}</span>
                </label>
              </div>
            </div>
            <div class="mt-4 flex-1 flex flex-col justify-end relative">
              <button
                @click="handleAiGenerate"
                :disabled="!entryRawData.trim() || aiGenerating"
                class="w-full py-3 rounded-lg bg-primary text-white font-medium hover:bg-primary/90 disabled:opacity-70 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-2"
              >
                <span v-if="aiGenerating" class="material-symbols-outlined animate-spin">progress_activity</span>
                <span v-else class="material-symbols-outlined">bolt</span>
                {{ aiGenerating ? '正在生成...' : '条目 AI 生成' }}
              </button>
              <p class="text-xs text-slate-500 mt-2">AI 将根据原始数据与历史条目风格自动生成年鉴条目，生成后可进入条目管理进行编辑与审核。</p>
              <!-- Loading overlay -->
              <div
                v-if="aiGenerating"
                class="absolute inset-0 flex flex-col items-center justify-center bg-white/80 rounded-lg"
              >
                <span class="material-symbols-outlined text-5xl text-primary animate-spin mb-2">progress_activity</span>
                <p class="text-sm text-slate-600">AI 正在生成条目...</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Upload dialog -->
      <Teleport to="body">
        <div
          v-if="showUploadDialog"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
          @click.self="showUploadDialog = false"
        >
          <div class="bg-white rounded-xl shadow-2xl max-w-lg w-full p-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-semibold text-slate-900">上传资料</h3>
              <button @click="showUploadDialog = false" class="p-2 rounded-lg hover:bg-slate-100">
                <span class="material-symbols-outlined">close</span>
              </button>
            </div>
            <div
              :class="[
                'border-2 border-dashed rounded-xl p-8 text-center transition-colors',
                isDragging ? 'border-primary bg-primary/5' : 'border-slate-200 hover:border-primary/50'
              ]"
              @dragover.prevent="isDragging = true"
              @dragleave="isDragging = false"
              @drop.prevent="handleDrop"
            >
              <span class="material-symbols-outlined text-5xl text-slate-400 mb-3 block">upload_file</span>
              <p class="text-slate-600 mb-1">拖拽文件到此处，或点击选择</p>
              <p class="text-xs text-slate-500">支持 Word、PDF 格式，单个文件 ≤ 5MB</p>
              <input
                ref="fileInputRef"
                type="file"
                accept=".doc,.docx,.pdf"
                class="hidden"
                @change="handleFileSelect"
              />
              <button
                @click="fileInputRef?.click()"
                class="mt-4 px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium hover:bg-primary/90"
              >
                选择文件
              </button>
            </div>
            <div class="mt-4 flex justify-end gap-2">
              <button @click="showUploadDialog = false" class="px-4 py-2 rounded-lg border border-slate-200 hover:bg-slate-50">取消</button>
            </div>
          </div>
        </div>
      </Teleport>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, inject } from 'vue'
import { useRouter } from 'vue-router'
import { supabase } from '@/lib/supabase'
import { useYearbookStore } from '@/stores/yearbook'
import { useAuthStore } from '@/stores/auth'

const yearbookStore = useYearbookStore()
const auth = useAuthStore()
const router = useRouter()
const toast = inject('toast')

const outline = computed(() => yearbookStore.currentOutline)
const progress = ref(35)
const splitCollapsed = ref(false)
const uploadCollapsed = ref(false)
const selectedMaterial = ref(null)
const entryRawData = ref('')
const historySearch = ref('')
const selectedHistory = ref([])
const aiGenerating = ref(false)
const showUploadDialog = ref(false)
const isDragging = ref(false)
const fileInputRef = ref(null)

// Mock: 切分后资料
const splitMaterials = ref([
  { id: 's1', name: '2024年度工作总结.docx', preview: '一、工作概况\n\n本单位2024年度围绕中心工作，扎实推进各项任务落实...\n\n二、主要成效\n\n1. 完成重点项目X项\n2. 开展专题活动Y次\n3. 获得上级表彰Z项\n\n三、下一步计划\n\n继续深化...' },
  { id: 's2', name: '第一季度会议纪要.pdf', preview: '会议时间：2024年3月15日\n会议地点：三楼会议室\n参会人员：张三、李四、王五...\n\n会议内容：\n1. 讨论年度工作计划\n2. 审议预算方案\n3. 其他事项' },
])

// Mock: 上传资料
const uploadMaterials = ref([
  { id: 'u1', name: '活动照片汇总.zip', preview: '（压缩包，包含活动相关图片文件）' },
  { id: 'u2', name: '补充说明.txt', preview: '关于年度工作的补充说明材料...' },
])

// Mock: 往年历史数据
const historyData = ref([
  { id: 'h1', title: '2023年度工作总结' },
  { id: 'h2', title: '2022年度工作总结' },
  { id: 'h3', title: '2023年第一季度会议纪要' },
  { id: 'h4', title: '2022年专题活动总结' },
])

const totalMaterialCount = computed(() => splitMaterials.value.length + uploadMaterials.value.length)
const entryCount = ref(5)

const filteredHistory = computed(() => {
  const q = historySearch.value.trim().toLowerCase()
  if (!q) return historyData.value
  return historyData.value.filter(h => h.title.toLowerCase().includes(q))
})

watch(outline, (val) => {
  if (val && splitMaterials.value.length && !selectedMaterial.value) {
    selectedMaterial.value = splitMaterials.value[0]
  }
}, { immediate: true })

function handleRobotAssistant() {
  toast('机器人助理功能已开启', 'info')
}

function handleDrop(e) {
  isDragging.value = false
  const files = e.dataTransfer?.files
  if (files?.length) handleFiles(Array.from(files))
}

function handleFileSelect(e) {
  const files = e.target.files
  if (files?.length) handleFiles(Array.from(files))
  e.target.value = ''
}

function handleFiles(files) {
  const valid = ['.doc', '.docx', '.pdf']
  const maxSize = 5 * 1024 * 1024
  for (const f of files) {
    const ext = '.' + (f.name.split('.').pop() || '').toLowerCase()
    if (!valid.includes(ext)) {
      toast(`不支持的文件格式：${f.name}`, 'error')
      continue
    }
    if (f.size > maxSize) {
      toast(`文件过大：${f.name}（需 ≤ 5MB）`, 'error')
      continue
    }
    uploadMaterials.value.push({
      id: 'u' + Date.now() + Math.random().toString(36).slice(2),
      name: f.name,
      preview: `（已上传：${f.name}）`,
    })
  }
  if (files.length) {
    toast('文件上传成功', 'success')
    showUploadDialog.value = false
  }
}

async function handleAiGenerate() {
  if (!entryRawData.value.trim()) return
  aiGenerating.value = true
  await new Promise(r => setTimeout(r, 2000))
  aiGenerating.value = false
  toast('条目生成成功', 'success')
  router.push({ name: 'EntryManage' })
}
</script>
