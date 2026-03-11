<template>
  <div class="h-screen flex flex-col overflow-hidden bg-[#f5f7f8]">
    <!-- Top header -->
    <div class="flex-shrink-0 bg-white border-b border-slate-200 px-6 py-3 flex items-center justify-between">
      <div class="flex items-center gap-4">
        <nav class="flex items-center gap-2 text-sm text-slate-600">
          <router-link to="/compile" class="hover:text-primary">智能编纂</router-link>
          <span class="material-symbols-outlined text-base">chevron_right</span>
          <span class="text-slate-900 font-medium">条目生成管理</span>
        </nav>
        <span class="px-3 py-1 rounded-full bg-primary/10 text-primary text-sm font-medium">第一卷</span>
      </div>
    </div>

    <!-- Main three-column layout -->
    <div class="flex-1 flex min-h-0 overflow-hidden">
      <!-- Left panel: 条目列表 -->
      <div class="w-[20%] flex-shrink-0 flex flex-col border-r border-slate-200 bg-white overflow-hidden">
        <div class="px-4 py-3 border-b border-slate-200 font-medium text-slate-800">条目列表</div>
        <div class="flex-1 overflow-y-auto custom-scrollbar">
          <div
            v-for="(entry, idx) in entries"
            :key="entry.id"
            @mouseenter="hoveredEntry = entry.id"
            @mouseleave="hoveredEntry = null"
            :class="[
              'group flex items-center gap-2 px-4 py-2.5 mx-2 my-1 rounded-lg cursor-pointer transition-all',
              selectedEntry?.id === entry.id ? 'bg-primary text-white' : 'hover:bg-slate-100 text-slate-700'
            ]"
            @click="selectedEntry = entry"
          >
            <span class="material-symbols-outlined text-xl flex-shrink-0">description</span>
            <span class="truncate flex-1 text-sm font-medium">{{ entry.title }}</span>
            <div
              v-show="hoveredEntry === entry.id || selectedEntry?.id === entry.id"
              class="flex items-center gap-0.5 flex-shrink-0"
              @click.stop
            >
              <button
                :disabled="idx === 0"
                class="p-1 rounded hover:bg-white/20 disabled:opacity-30 disabled:cursor-not-allowed"
                title="上移"
              >
                <span class="material-symbols-outlined text-lg">arrow_upward</span>
              </button>
              <button
                :disabled="idx === entries.length - 1"
                class="p-1 rounded hover:bg-white/20 disabled:opacity-30 disabled:cursor-not-allowed"
                title="下移"
              >
                <span class="material-symbols-outlined text-lg">arrow_downward</span>
              </button>
              <button class="p-1 rounded hover:bg-red-500/30" title="删除">
                <span class="material-symbols-outlined text-lg">delete</span>
              </button>
            </div>
          </div>
        </div>
        <button
          class="m-4 py-2.5 rounded-lg border-2 border-dashed border-slate-300 text-slate-600 text-sm font-medium hover:border-primary hover:text-primary transition-all flex items-center justify-center gap-2"
        >
          <span class="material-symbols-outlined text-xl">add</span>
          新增条目
        </button>
      </div>

      <!-- Middle panel: 条目原始内容 -->
      <div class="w-[35%] flex-shrink-0 flex flex-col border-r border-slate-200 bg-white overflow-hidden">
        <div class="px-4 py-3 border-b border-slate-200 font-medium text-slate-800">条目原始内容</div>
        <div class="flex-1 overflow-y-auto custom-scrollbar p-4">
          <div v-if="selectedEntry" class="text-slate-700 text-sm leading-relaxed whitespace-pre-wrap">{{ selectedEntry.original_content }}</div>
          <div v-else class="flex flex-col items-center justify-center h-full text-slate-400">
            <span class="material-symbols-outlined text-5xl mb-2">description</span>
            <p class="text-sm">请从左侧选择条目</p>
          </div>
        </div>
      </div>

      <!-- Right panel: 富文本编辑区 -->
      <div class="w-[45%] flex-shrink-0 flex flex-col bg-white overflow-hidden">
        <!-- Toolbar header -->
        <div class="flex items-center gap-2 px-4 py-2 border-b border-slate-200 bg-slate-50">
          <button class="px-3 py-1.5 rounded-lg text-sm font-medium text-slate-700 hover:bg-white hover:shadow transition-all">
            数据智能检测
          </button>
          <button class="px-3 py-1.5 rounded-lg text-sm font-medium text-slate-700 hover:bg-white hover:shadow transition-all">
            对比审阅
          </button>
          <button class="px-3 py-1.5 rounded-lg text-sm font-medium text-slate-700 hover:bg-white hover:shadow transition-all">
            草稿记录
          </button>
        </div>
        <!-- Editor toolbar -->
        <div class="flex items-center gap-1 px-4 py-2 border-b border-slate-200 flex-wrap">
          <button class="p-2 rounded hover:bg-slate-100" title="撤销">
            <span class="material-symbols-outlined text-xl">undo</span>
          </button>
          <button class="p-2 rounded hover:bg-slate-100" title="重做">
            <span class="material-symbols-outlined text-xl">redo</span>
          </button>
          <div class="w-px h-6 bg-slate-200 mx-1" />
          <select class="px-2 py-1.5 rounded border border-slate-200 text-sm">
            <option>正文</option>
            <option>标题 1</option>
            <option>标题 2</option>
            <option>标题 3</option>
          </select>
          <div class="w-px h-6 bg-slate-200 mx-1" />
          <button class="p-2 rounded hover:bg-slate-100" title="粗体">
            <span class="material-symbols-outlined text-xl">format_bold</span>
          </button>
          <button class="p-2 rounded hover:bg-slate-100" title="斜体">
            <span class="material-symbols-outlined text-xl">format_italic</span>
          </button>
          <button class="p-2 rounded hover:bg-slate-100" title="下划线">
            <span class="material-symbols-outlined text-xl">format_underlined</span>
          </button>
          <div class="w-px h-6 bg-slate-200 mx-1" />
          <button class="p-2 rounded hover:bg-slate-100" title="左对齐">
            <span class="material-symbols-outlined text-xl">format_align_left</span>
          </button>
          <button class="p-2 rounded hover:bg-slate-100" title="居中">
            <span class="material-symbols-outlined text-xl">format_align_center</span>
          </button>
          <button class="p-2 rounded hover:bg-slate-100" title="右对齐">
            <span class="material-symbols-outlined text-xl">format_align_right</span>
          </button>
          <div class="w-px h-6 bg-slate-200 mx-1" />
          <button class="p-2 rounded hover:bg-slate-100" title="插入图片">
            <span class="material-symbols-outlined text-xl">image</span>
          </button>
          <button class="p-2 rounded hover:bg-slate-100" title="插入表格">
            <span class="material-symbols-outlined text-xl">table_chart</span>
          </button>
        </div>
        <!-- Editor content area (relative for AI float toolbar) -->
        <div class="flex-1 relative overflow-hidden">
          <div class="h-full overflow-y-auto custom-scrollbar p-6">
            <div
              contenteditable="true"
              class="min-h-full outline-none text-slate-800 text-sm leading-relaxed focus:outline-none [&_p]:mb-2 [&_strong]:font-bold"
              v-html="editorContent"
              :key="selectedEntry?.id"
            />
          </div>
          <!-- AI float toolbar -->
          <div class="absolute bottom-4 right-4 flex gap-2 bg-white rounded-lg shadow-lg border border-slate-200 p-1">
            <button class="px-3 py-1.5 rounded-md text-sm font-medium bg-primary text-white hover:bg-primary/90">
              AI 编写
            </button>
            <button class="px-3 py-1.5 rounded-md text-sm font-medium text-slate-700 hover:bg-slate-100">
              扩写
            </button>
            <button class="px-3 py-1.5 rounded-md text-sm font-medium text-slate-700 hover:bg-slate-100">
              润色
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom bar -->
    <div class="flex-shrink-0 bg-white border-t border-slate-200 px-6 py-2.5 flex items-center justify-between">
      <div class="flex items-center gap-4 text-sm text-slate-500">
        <span>{{ autoSaveStatus }}</span>
        <span>字数：{{ wordCount }}</span>
      </div>
      <div class="flex items-center gap-3">
        <button class="px-4 py-2 rounded-lg border border-slate-200 text-slate-700 font-medium hover:bg-slate-50 transition-all">
          保存草稿
        </button>
        <button class="px-4 py-2 rounded-lg bg-primary text-white font-medium hover:bg-primary/90 transition-all">
          提交审核
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { supabase } from '@/lib/supabase'
import { useYearbookStore } from '@/stores/yearbook'

const yearbookStore = useYearbookStore()
const selectedEntry = ref(null)
const hoveredEntry = ref(null)
const autoSaveStatus = ref('已自动保存')
const wordCount = ref(0)

// Mock: 条目数据 (yb_entries structure)
const entries = ref([
  {
    id: 'e1',
    outline_id: 'o1',
    title: '2024年度工作总结',
    original_content: `一、工作概况

本单位2024年度围绕中心工作，扎实推进各项任务落实，取得显著成效。

二、主要成效

1. 完成重点项目X项，超额完成年度目标；
2. 开展专题活动Y次，参与人数达Z人次；
3. 获得上级表彰Z项，工作质量获认可。

三、下一步计划

继续深化工作改革，提升服务效能。`,
    ai_content: '<p><strong>一、工作概况</strong></p><p>本单位2024年度围绕中心工作，扎实推进各项任务落实，取得显著成效。</p><p><strong>二、主要成效</strong></p><p>1. 完成重点项目X项，超额完成年度目标；</p><p>2. 开展专题活动Y次，参与人数达Z人次；</p><p>3. 获得上级表彰Z项，工作质量获认可。</p><p><strong>三、下一步计划</strong></p><p>继续深化工作改革，提升服务效能。</p>',
    sort_order: 1,
    status: 'draft',
  },
  {
    id: 'e2',
    outline_id: 'o1',
    title: '第一季度会议纪要',
    original_content: `会议时间：2024年3月15日
会议地点：三楼会议室
参会人员：张三、李四、王五等

会议内容：
1. 讨论年度工作计划
2. 审议预算方案
3. 其他事项`,
    ai_content: '<p><strong>会议时间：</strong>2024年3月15日</p><p><strong>会议地点：</strong>三楼会议室</p><p><strong>参会人员：</strong>张三、李四、王五等</p><p><strong>会议内容：</strong></p><p>1. 讨论年度工作计划</p><p>2. 审议预算方案</p><p>3. 其他事项</p>',
    sort_order: 2,
    status: 'draft',
  },
  {
    id: 'e3',
    outline_id: 'o1',
    title: '专题活动总结',
    original_content: `活动名称：年度专题学习活动
活动时间：2024年5月
参与人数：120人

活动成效：提升了团队凝聚力，增强了业务能力。`,
    ai_content: '<p><strong>活动名称：</strong>年度专题学习活动</p><p><strong>活动时间：</strong>2024年5月</p><p><strong>参与人数：</strong>120人</p><p><strong>活动成效：</strong>提升了团队凝聚力，增强了业务能力。</p>',
    sort_order: 3,
    status: 'draft',
  },
])

const editorContent = computed(() => {
  if (!selectedEntry.value) return ''
  return selectedEntry.value.ai_content || ''
})

watch(selectedEntry, (entry) => {
  if (entry?.ai_content) {
    const text = entry.ai_content.replace(/<[^>]+>/g, '')
    wordCount.value = text.length
  } else {
    wordCount.value = 0
  }
}, { immediate: true })

// Select first entry by default
if (entries.value.length && !selectedEntry.value) {
  selectedEntry.value = entries.value[0]
}
</script>
