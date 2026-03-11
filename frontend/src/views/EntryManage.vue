<template>
  <div class="h-screen flex flex-col overflow-hidden bg-[#f5f7f8]">
    <div class="flex-shrink-0 bg-white border-b border-slate-200 px-6 py-3 flex items-center justify-between">
      <div class="flex items-center gap-4">
        <nav class="flex items-center gap-2 text-sm text-slate-600">
          <router-link to="/compile" class="hover:text-primary transition-colors">智能编纂</router-link>
          <span class="material-symbols-outlined text-base text-slate-400">chevron_right</span>
          <span class="text-slate-900 font-medium">条目生成管理</span>
        </nav>
        <span class="px-3 py-1 rounded-full bg-primary/10 text-primary text-xs font-medium">{{ entries.length }} 条</span>
      </div>
      <router-link to="/compile" class="flex items-center gap-1 text-sm text-slate-600 hover:text-primary transition-colors">
        <span class="material-symbols-outlined text-lg">arrow_back</span>返回
      </router-link>
    </div>

    <div class="flex-1 flex min-h-0 overflow-hidden">
      <!-- Left: entry list -->
      <div class="w-[20%] min-w-[220px] flex-shrink-0 flex flex-col border-r border-slate-200 bg-white">
        <div class="px-4 py-3 border-b border-slate-100 flex items-center justify-between">
          <span class="font-semibold text-slate-800 text-sm">条目列表</span>
          <span class="text-xs text-slate-500 bg-slate-100 px-2 py-0.5 rounded-full">{{ entries.length }} 条</span>
        </div>
        <div class="flex-1 overflow-y-auto custom-scrollbar p-2 space-y-0.5">
          <div
            v-for="(entry, idx) in entries"
            :key="entry.id"
            @click="selectedEntry = entry"
            :class="[
              'group flex items-center gap-2 px-3 py-2.5 rounded-lg cursor-pointer transition-all',
              selectedEntry?.id === entry.id
                ? 'bg-primary/10 text-primary border border-primary/20'
                : 'hover:bg-slate-50 text-slate-700 border border-transparent'
            ]"
          >
            <span class="material-symbols-outlined text-lg flex-shrink-0">description</span>
            <span class="truncate flex-1 text-sm">{{ entry.title }}</span>
            <div class="flex items-center gap-0.5 flex-shrink-0 opacity-0 group-hover:opacity-100 transition-opacity" @click.stop>
              <button @click="moveEntry(idx, -1)" :disabled="idx === 0" class="p-0.5 rounded hover:bg-slate-200 disabled:opacity-30" title="上移">
                <span class="material-symbols-outlined text-base">expand_less</span>
              </button>
              <button @click="moveEntry(idx, 1)" :disabled="idx === entries.length - 1" class="p-0.5 rounded hover:bg-slate-200 disabled:opacity-30" title="下移">
                <span class="material-symbols-outlined text-base">expand_more</span>
              </button>
              <button @click="deleteEntry(idx)" class="p-0.5 rounded hover:bg-red-100 text-red-500" title="删除">
                <span class="material-symbols-outlined text-base">close</span>
              </button>
            </div>
          </div>
        </div>
        <div class="p-3 border-t border-slate-100">
          <button @click="addEntry" class="w-full py-2 rounded-lg border-2 border-dashed border-slate-200 text-slate-500 text-sm hover:border-primary hover:text-primary transition-all flex items-center justify-center gap-1">
            <span class="material-symbols-outlined text-lg">add</span>新增条目
          </button>
        </div>
      </div>

      <!-- Middle: original content -->
      <div class="w-[35%] flex-shrink-0 flex flex-col border-r border-slate-200 bg-white">
        <div class="px-4 py-3 border-b border-slate-100 flex items-center justify-between">
          <span class="font-semibold text-slate-800 text-sm">条目原始内容</span>
          <button v-if="selectedEntry" class="text-xs text-primary hover:underline">查看来源</button>
        </div>
        <div class="flex-1 overflow-y-auto custom-scrollbar p-5">
          <div v-if="selectedEntry" class="text-slate-700 text-sm leading-7 whitespace-pre-wrap">{{ selectedEntry.original_content }}</div>
          <div v-else class="flex flex-col items-center justify-center h-full text-slate-400">
            <span class="material-symbols-outlined text-5xl mb-3 opacity-50">description</span>
            <p class="text-sm">请从左侧选择条目查看</p>
          </div>
        </div>
      </div>

      <!-- Right: editor -->
      <div class="flex-1 flex flex-col bg-[#f5f7f8] overflow-hidden">
        <div class="flex items-center justify-between px-4 py-2 bg-white border-b border-slate-200">
          <div class="flex items-center gap-2">
            <button @click="showDetect" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-primary/10 text-primary hover:bg-primary/20 transition-all flex items-center gap-1">
              <span class="material-symbols-outlined text-sm">fact_check</span>数据智能检测
            </button>
            <button @click="showCompare" class="px-3 py-1.5 rounded-lg text-xs font-medium text-slate-600 hover:bg-slate-100 transition-all flex items-center gap-1">
              <span class="material-symbols-outlined text-sm">compare_arrows</span>对比审阅
            </button>
            <button @click="showDrafts" class="px-3 py-1.5 rounded-lg text-xs font-medium text-slate-600 hover:bg-slate-100 transition-all flex items-center gap-1">
              <span class="material-symbols-outlined text-sm">history</span>草稿记录
            </button>
          </div>
        </div>
        <div class="flex items-center gap-1 px-3 py-1.5 bg-white border-b border-slate-100 flex-wrap">
          <button @click="execCmd('undo')" class="p-1.5 rounded hover:bg-slate-100"><span class="material-symbols-outlined text-lg">undo</span></button>
          <button @click="execCmd('redo')" class="p-1.5 rounded hover:bg-slate-100"><span class="material-symbols-outlined text-lg">redo</span></button>
          <div class="w-px h-5 bg-slate-200 mx-1" />
          <button @click="execCmd('bold')" class="p-1.5 rounded hover:bg-slate-100 font-bold text-sm">B</button>
          <button @click="execCmd('italic')" class="p-1.5 rounded hover:bg-slate-100 italic text-sm">I</button>
          <button @click="execCmd('underline')" class="p-1.5 rounded hover:bg-slate-100 underline text-sm">U</button>
          <div class="w-px h-5 bg-slate-200 mx-1" />
          <button @click="execCmd('justifyLeft')" class="p-1.5 rounded hover:bg-slate-100"><span class="material-symbols-outlined text-lg">format_align_left</span></button>
          <button @click="execCmd('justifyCenter')" class="p-1.5 rounded hover:bg-slate-100"><span class="material-symbols-outlined text-lg">format_align_center</span></button>
          <div class="w-px h-5 bg-slate-200 mx-1" />
          <button @click="toast?.('图片上传功能开发中', 'info')" class="p-1.5 rounded hover:bg-slate-100"><span class="material-symbols-outlined text-lg">image</span></button>
          <button @click="toast?.('表格功能开发中', 'info')" class="p-1.5 rounded hover:bg-slate-100"><span class="material-symbols-outlined text-lg">table_chart</span></button>
        </div>
        <div class="flex-1 p-4 overflow-hidden">
          <div class="bg-white rounded-xl shadow-sm border border-slate-200 h-full flex flex-col relative">
            <div class="flex-1 overflow-y-auto custom-scrollbar p-6">
              <div
                ref="editorRef"
                contenteditable="true"
                class="min-h-full outline-none text-slate-800 text-[15px] leading-8 [&_p]:mb-3 [&_strong]:font-bold"
                v-html="editorContent"
                :key="selectedEntry?.id"
                @input="onEditorInput"
              />
            </div>
            <div class="absolute bottom-3 left-1/2 -translate-x-1/2 flex items-center gap-1 bg-white rounded-xl shadow-xl border border-primary/20 p-1 ring-2 ring-primary/5">
              <button @click="aiWrite" class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-primary text-white text-xs font-medium hover:bg-primary/90 transition-all">
                <span class="material-symbols-outlined text-sm">smart_toy</span>AI 编写
              </button>
              <button @click="aiExpand" class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-slate-700 text-xs font-medium hover:bg-slate-100 transition-all">
                <span class="material-symbols-outlined text-sm">format_line_spacing</span>扩写
              </button>
              <button @click="aiPolish" class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-slate-700 text-xs font-medium hover:bg-slate-100 transition-all">
                <span class="material-symbols-outlined text-sm">auto_fix_high</span>润色
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="flex-shrink-0 bg-white border-t border-slate-200 px-6 py-2.5 flex items-center justify-between">
      <div class="flex items-center gap-4 text-xs text-slate-500">
        <span class="flex items-center gap-1"><span class="size-2 rounded-full bg-emerald-500"></span>{{ autoSaveStatus }}</span>
        <span>字数：{{ wordCount }}</span>
      </div>
      <div class="flex items-center gap-3">
        <button @click="saveDraft" class="px-5 py-2 rounded-lg border border-slate-200 text-slate-700 text-sm font-medium hover:bg-slate-50 transition-all">
          保存草稿
        </button>
        <button @click="submitEntry" class="px-5 py-2 rounded-lg bg-primary text-white text-sm font-medium hover:bg-primary/90 shadow-lg shadow-primary/20 transition-all flex items-center gap-1">
          提交审核 <span class="material-symbols-outlined text-sm">send</span>
        </button>
      </div>
    </div>

    <!-- Delete confirm -->
    <Teleport to="body">
      <div v-if="deleteIdx !== null" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40" @click.self="deleteIdx = null">
        <div class="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6 text-center">
          <div class="size-12 mx-auto mb-4 rounded-full bg-red-100 flex items-center justify-center">
            <span class="material-symbols-outlined text-red-500 text-2xl">warning</span>
          </div>
          <h3 class="text-lg font-semibold text-slate-900 mb-2">删除条目</h3>
          <p class="text-sm text-slate-600 mb-6">确定要删除该条目吗？删除后将无法恢复。</p>
          <div class="flex gap-3">
            <button @click="deleteIdx = null" class="flex-1 py-2.5 rounded-xl border border-slate-200 hover:bg-slate-50 text-sm font-medium transition-all">取消</button>
            <button @click="confirmDelete" class="flex-1 py-2.5 rounded-xl bg-red-500 text-white text-sm font-medium hover:bg-red-600 transition-all">确认删除</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, inject } from 'vue'
import { useYearbookStore } from '@/stores/yearbook'

const yearbookStore = useYearbookStore()
const toast = inject('toast', null)

const selectedEntry = ref(null)
const autoSaveStatus = ref('已自动保存')
const wordCount = ref(0)
const editorRef = ref(null)
const deleteIdx = ref(null)

const entries = ref([
  {
    id: 'e1', title: '经济发展综述', sort_order: 1, status: 'draft',
    original_content: '【数据概况】\n2023年，全区地区生产总值（GDP）达到1523.4亿元，同比增长6.8%。其中，第一产业增加值42.1亿元，增长3.2%；第二产业增加值712.8亿元，增长7.5%；第三产业增加值768.5亿元，增长6.5%。\n\n【重点项目】\n全年新开工亿元以上项目120个，其中5亿元以上项目45个。智能制造产业园一期全面投产，新增产值80亿元。',
    ai_content: '<p><strong>【综述】</strong>2023年，区域经济在复杂环境中稳步前进。全区生产总值（GDP）突破1500亿元大关，达1523.4亿元，同比增长6.8%，增速高于全市平均水平0.5个百分点。</p><p><strong>【固定资产投资】</strong>全年固定资产投资规模持续扩大，完成投资890亿元，增长12.5%。其中，高技术制造业投资增长25.4%。</p><p><strong>【消费市场】</strong>社会消费品零售总额实现650亿元，新型消费业态蓬勃发展。</p>',
  },
  {
    id: 'e2', title: '工业企业概况', sort_order: 2, status: 'draft',
    original_content: '全区规模以上工业企业达到856家，实现工业总产值3200亿元，同比增长8.2%。\n\n新增高新技术企业120家，累计达到580家。战略性新兴产业产值占比达到38.5%。',
    ai_content: '<p><strong>【工业经济】</strong>全区工业经济保持稳健增长态势。规模以上工业企业达到856家，实现工业总产值3200亿元，同比增长8.2%。</p><p><strong>【创新驱动】</strong>新增高新技术企业120家，累计达到580家。战略性新兴产业产值占比达到38.5%，产业结构持续优化。</p>',
  },
  {
    id: 'e3', title: '对外贸易合作', sort_order: 3, status: 'draft',
    original_content: '出口总额实现450亿元，同比增长8.2%。主要出口产品涵盖电子信息、精密仪器、新型材料等领域。\n\n新引进外资项目35个，实际利用外资12.5亿美元。',
    ai_content: '<p><strong>【对外贸易】</strong>全区对外贸易保持良好发展势头。出口总额实现450亿元，同比增长8.2%。主要出口产品涵盖电子信息、精密仪器、新型材料等领域。</p><p><strong>【招商引资】</strong>新引进外资项目35个，实际利用外资12.5亿美元，外资质量和结构持续优化。</p>',
  },
])

const editorContent = computed(() => selectedEntry.value?.ai_content || '')

watch(selectedEntry, (entry) => {
  wordCount.value = entry?.ai_content ? entry.ai_content.replace(/<[^>]+>/g, '').length : 0
}, { immediate: true })

if (entries.value.length) selectedEntry.value = entries.value[0]

function moveEntry(idx, dir) {
  const target = idx + dir
  if (target < 0 || target >= entries.value.length) return
  const temp = entries.value[idx]
  entries.value[idx] = entries.value[target]
  entries.value[target] = temp
  entries.value = [...entries.value]
}

function deleteEntry(idx) {
  deleteIdx.value = idx
}

function confirmDelete() {
  if (deleteIdx.value === null) return
  const removed = entries.value[deleteIdx.value]
  entries.value.splice(deleteIdx.value, 1)
  entries.value = [...entries.value]
  if (selectedEntry.value?.id === removed.id) {
    selectedEntry.value = entries.value[0] || null
  }
  deleteIdx.value = null
  toast?.('条目已删除', 'success')
}

function addEntry() {
  const newEntry = {
    id: 'e' + Date.now(),
    title: '新条目',
    sort_order: entries.value.length + 1,
    status: 'draft',
    original_content: '',
    ai_content: '<p>请在此编辑条目内容...</p>',
  }
  entries.value.push(newEntry)
  selectedEntry.value = newEntry
  toast?.('已添加新条目', 'success')
}

function onEditorInput() {
  autoSaveStatus.value = '编辑中...'
  wordCount.value = editorRef.value?.innerText?.length || 0
  setTimeout(() => { autoSaveStatus.value = '已自动保存' }, 1000)
}

function execCmd(cmd) {
  document.execCommand(cmd, false, null)
}

function saveDraft() {
  autoSaveStatus.value = '已保存'
  toast?.('草稿已保存', 'success')
}

function submitEntry() {
  if (selectedEntry.value) selectedEntry.value.status = 'submitted'
  toast?.('条目已提交审核', 'success')
}

function aiWrite() { toast?.('AI 编写功能调用中，请稍候...', 'info') }
function aiExpand() { toast?.('AI 扩写功能调用中，请稍候...', 'info') }
function aiPolish() { toast?.('AI 润色功能调用中，请稍候...', 'info') }
function showDetect() { toast?.('数据智能检测功能开发中', 'info') }
function showCompare() { toast?.('对比审阅功能开发中', 'info') }
function showDrafts() { toast?.('草稿记录功能开发中', 'info') }
</script>
