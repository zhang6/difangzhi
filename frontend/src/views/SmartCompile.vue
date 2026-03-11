<template>
  <div class="h-screen flex flex-col overflow-hidden bg-[#f5f7f8]">
    <!-- No outline selected -->
    <div
      v-if="!yearbookStore.currentOutline"
      class="flex-1 flex flex-col items-center justify-center text-slate-500"
    >
      <span class="material-symbols-outlined text-7xl mb-4 text-slate-300">psychology</span>
      <p class="text-lg">请先从大纲管理选择需要编纂的内容</p>
    </div>

    <!-- Three column layout -->
    <template v-else>
      <!-- Top bar -->
      <div class="flex-shrink-0 bg-slate-200 px-6 py-3">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <span class="text-slate-700 font-medium">正在编纂：{{ yearbookStore.currentYearbook?.name || '未命名年鉴' }}</span>
            <div class="w-48 h-2 bg-slate-300 rounded-full overflow-hidden">
              <div class="h-full bg-primary rounded-full transition-all" :style="{ width: progress + '%' }" />
            </div>
            <span class="text-sm text-slate-600">{{ progress }}%</span>
          </div>
          <button
            class="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white font-medium hover:bg-primary/90 transition-all"
          >
            <span class="material-symbols-outlined text-xl">smart_toy</span>
            机器人智能助理
          </button>
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
        <div class="w-[20%] flex-shrink-0 flex flex-col border-r border-slate-200 bg-white">
          <div class="px-4 py-3 border-b border-slate-200 font-medium text-slate-800">资料列表</div>
          <div class="flex-1 overflow-y-auto custom-scrollbar">
            <!-- 切分后资料 -->
            <div class="border-b border-slate-100">
              <button
                @click="splitCollapsed = !splitCollapsed"
                class="w-full flex items-center justify-between px-4 py-2.5 text-left text-sm font-medium text-slate-700 hover:bg-slate-50"
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
                    'flex items-center gap-2 px-4 py-2 mx-2 rounded-lg cursor-pointer transition-all',
                    selectedMaterial?.id === item.id ? 'bg-primary text-white' : 'hover:bg-slate-100 text-slate-700'
                  ]"
                >
                  <span class="material-symbols-outlined text-xl">description</span>
                  <span class="truncate flex-1 text-sm">{{ item.name }}</span>
                </div>
              </div>
            </div>
            <!-- 上传资料 -->
            <div>
              <button
                @click="uploadCollapsed = !uploadCollapsed"
                class="w-full flex items-center justify-between px-4 py-2.5 text-left text-sm font-medium text-slate-700 hover:bg-slate-50"
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
                    'flex items-center gap-2 px-4 py-2 mx-2 rounded-lg cursor-pointer transition-all',
                    selectedMaterial?.id === item.id ? 'bg-primary text-white' : 'hover:bg-slate-100 text-slate-700'
                  ]"
                >
                  <span class="material-symbols-outlined text-xl">description</span>
                  <span class="truncate flex-1 text-sm">{{ item.name }}</span>
                </div>
              </div>
            </div>
          </div>
          <button
            class="m-4 py-2.5 rounded-lg border-2 border-dashed border-slate-300 text-slate-600 text-sm font-medium hover:border-primary hover:text-primary transition-all flex items-center justify-center gap-2"
          >
            <span class="material-symbols-outlined text-xl">upload_file</span>
            上传资料
          </button>
        </div>

        <!-- Middle column: 资料预览 -->
        <div class="w-[40%] flex-shrink-0 flex flex-col bg-slate-100 p-4 overflow-hidden">
          <div class="mb-3 font-medium text-slate-800">资料预览</div>
          <div class="flex-1 overflow-y-auto custom-scrollbar">
            <div class="bg-white rounded-xl shadow-sm p-6 min-h-[200px]">
              <template v-if="selectedMaterial">
                <h3 class="text-lg font-semibold text-slate-900 mb-4">{{ selectedMaterial.name }}</h3>
                <div class="text-slate-600 text-sm leading-relaxed whitespace-pre-wrap">{{ selectedMaterial.preview }}</div>
              </template>
              <template v-else>
                <div class="flex flex-col items-center justify-center h-48 text-slate-400">
                  <span class="material-symbols-outlined text-5xl mb-2">description</span>
                  <p class="text-sm">请从左侧选择资料查看预览</p>
                </div>
              </template>
            </div>
          </div>
        </div>

        <!-- Right column: 条目生成 -->
        <div class="w-[40%] flex-shrink-0 flex flex-col bg-white border-l border-slate-200 p-4 overflow-hidden">
          <div class="mb-3 font-medium text-slate-800">条目生成</div>
          <div class="flex-1 flex flex-col min-h-0">
            <label class="text-sm text-slate-600 mb-1.5 block">条目原始数据</label>
            <textarea
              v-model="entryRawData"
              class="h-48 w-full px-4 py-3 rounded-lg border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none resize-none text-sm"
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
              <div class="h-24 overflow-y-auto custom-scrollbar border border-slate-200 rounded-lg p-2">
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
            <div class="mt-4 flex-1 flex flex-col justify-end">
              <button
                @click="handleAiGenerate"
                :disabled="aiGenerating"
                class="w-full py-3 rounded-lg bg-primary text-white font-medium hover:bg-primary/90 disabled:opacity-70 transition-all flex items-center justify-center gap-2"
              >
                <span v-if="aiGenerating" class="material-symbols-outlined animate-spin">progress_activity</span>
                <span v-else class="material-symbols-outlined">bolt</span>
                {{ aiGenerating ? '正在生成...' : '条目 AI 生成' }}
              </button>
              <p class="text-xs text-slate-500 mt-2">AI 将根据原始数据与历史条目风格自动生成年鉴条目，生成后可进入条目管理进行编辑与审核。</p>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { supabase } from '@/lib/supabase'
import { useYearbookStore } from '@/stores/yearbook'

const yearbookStore = useYearbookStore()
const router = useRouter()

const outline = computed(() => yearbookStore.currentOutline)
const progress = ref(35)

const splitCollapsed = ref(false)
const uploadCollapsed = ref(false)
const selectedMaterial = ref(null)
const entryRawData = ref('')
const historySearch = ref('')
const selectedHistory = ref([])
const aiGenerating = ref(false)

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

const filteredHistory = computed(() => {
  const q = historySearch.value.trim().toLowerCase()
  if (!q) return historyData.value
  return historyData.value.filter(h => h.title.toLowerCase().includes(q))
})

// Select first material by default when outline exists
watch(outline, (val) => {
  if (val && splitMaterials.value.length && !selectedMaterial.value) {
    selectedMaterial.value = splitMaterials.value[0]
  }
}, { immediate: true })

async function handleAiGenerate() {
  aiGenerating.value = true
  await new Promise(r => setTimeout(r, 1500))
  aiGenerating.value = false
  router.push({ name: 'EntryManage' })
}
</script>
