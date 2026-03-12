<template>
  <div class="page-container bg-slate-50">
    <!-- 头部 -->
    <div class="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between sticky top-0 z-10">
      <div>
        <div class="flex items-center gap-2 text-xs text-slate-400 mb-1">
          <span>首页</span>
          <span class="material-symbols-outlined" style="font-size:12px;">chevron_right</span>
          <span class="text-slate-600 font-medium">资料库</span>
        </div>
        <h1 class="text-lg font-bold text-slate-900">资料库</h1>
      </div>
      <div class="flex items-center gap-3">
        <div class="relative">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" style="font-size:16px;">search</span>
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索供稿单位、标签..."
            class="pl-9 pr-4 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 w-60"
            @input="handleSearch"
          />
        </div>
        <button
          class="flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-medium text-white transition-all hover:shadow-md"
          style="background:linear-gradient(135deg,#1a90ff,#0059b3);"
          @click="openCreate"
        >
          <span class="material-symbols-outlined" style="font-size:16px;">create_new_folder</span>
          创建文件夹
        </button>
      </div>
    </div>

    <div class="px-6 py-5">
      <p class="text-xs text-slate-500 mb-5">管理各供稿单位提交的资料，集中存储、分类检索</p>

      <!-- 文件夹卡片网格 -->
      <div v-loading="loading" class="grid gap-4" style="grid-template-columns:repeat(auto-fill,minmax(260px,1fr));">
        <div
          v-for="folder in folders"
          :key="folder.id"
          class="bg-white rounded-xl border border-slate-200 p-5 card-hover cursor-pointer group"
          @click="$router.push('/resources/' + folder.id)"
        >
          <div class="flex items-start justify-between mb-3">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center" style="background:#eff6ff;">
              <span class="material-symbols-outlined text-blue-600" style="font-size:24px;">folder</span>
            </div>
            <div class="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity" @click.stop>
              <button class="p-1 rounded-lg text-slate-400 hover:text-blue-500 hover:bg-blue-50" @click="openEdit(folder)">
                <span class="material-symbols-outlined" style="font-size:16px;">edit</span>
              </button>
              <button class="p-1 rounded-lg text-slate-400 hover:text-red-500 hover:bg-red-50" @click="handleDelete(folder)">
                <span class="material-symbols-outlined" style="font-size:16px;">delete</span>
              </button>
            </div>
          </div>
          <h3 class="text-sm font-semibold text-slate-900 mb-1">{{ folder.unit_name }}</h3>
          <div class="flex items-center gap-1 mb-3 flex-wrap">
            <template v-if="folder.tags">
              <span
                v-for="tag in folder.tags.split(',').slice(0, 3)"
                :key="tag"
                class="text-[11px] px-2 py-0.5 bg-blue-50 text-blue-600 rounded-full"
              >{{ tag.trim() }}</span>
              <span v-if="folder.tags.split(',').length > 3" class="text-[11px] text-slate-400">+{{ folder.tags.split(',').length - 3 }}</span>
            </template>
            <span v-else class="text-[11px] text-slate-400">无标签</span>
          </div>
          <div class="flex items-center justify-between text-xs text-slate-400 border-t border-slate-100 pt-3">
            <span class="flex items-center gap-1">
              <span class="material-symbols-outlined" style="font-size:13px;">description</span>
              {{ folder.file_count }} 个文件
            </span>
            <span>{{ formatDate(folder.created_at) }}</span>
          </div>
        </div>

        <!-- 新建占位 -->
        <div
          class="rounded-xl border-2 border-dashed border-blue-200 p-5 flex flex-col items-center justify-center cursor-pointer hover:border-blue-400 hover:bg-blue-50 transition-all"
          style="min-height:160px;"
          @click="openCreate"
        >
          <div class="w-12 h-12 rounded-full flex items-center justify-center mb-3" style="background:#eff6ff;">
            <span class="material-symbols-outlined text-blue-500" style="font-size:24px;">add</span>
          </div>
          <p class="text-sm font-medium text-blue-600">创建文件夹</p>
          <p class="text-xs text-slate-400 mt-1">新增供稿单位</p>
        </div>
      </div>

      <el-empty v-if="!loading && folders.length === 0" description="暂无供稿单位" class="mt-12" />

      <!-- 分页 -->
      <div v-if="total > pageSize" class="flex justify-center mt-8">
        <el-pagination v-model:current-page="page" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="loadData" />
      </div>
    </div>

    <!-- 新建/编辑文件夹弹窗 -->
    <div v-if="dialogVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="dialogVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 fade-in">
        <div class="flex items-center justify-between px-6 py-4 border-b border-slate-200">
          <h2 class="text-base font-semibold text-slate-900">{{ isEdit ? '编辑文件夹' : '创建文件夹' }}</h2>
          <button @click="dialogVisible=false" class="p-1 rounded-lg text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="px-6 py-5 space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">供稿单位 <span class="text-red-500">*</span></label>
            <input v-model="form.unit_name" type="text" maxlength="20" placeholder="如：省委办公厅"
              class="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"/>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">文件标签</label>
            <input v-model="form.tags" type="text" placeholder="多个标签用逗号分隔，如：2024,政治,综合"
              class="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"/>
            <p class="text-xs text-slate-400 mt-1">用逗号分隔，最多20个标签</p>
          </div>
        </div>
        <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-slate-200">
          <button @click="dialogVisible=false" class="px-5 py-2 text-sm text-slate-600 border border-slate-300 rounded-xl hover:bg-slate-50">取消</button>
          <button
            :disabled="submitting"
            class="px-5 py-2 text-sm text-white rounded-xl hover:shadow-md disabled:opacity-50"
            style="background:linear-gradient(135deg,#1a90ff,#0059b3);"
            @click="handleSubmit"
          >{{ submitting ? '保存中...' : (isEdit ? '保存修改' : '确认创建') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchFolders, createFolder, updateFolder, deleteFolder } from '@/api/resource'
import type { ResourceFolder } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const folders = ref<ResourceFolder[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 12
const keyword = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref('')
const form = reactive({ unit_name: '', tags: '' })

let searchTimer: any
function handleSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { page.value = 1; loadData() }, 300)
}

async function loadData() {
  loading.value = true
  try {
    const result = await fetchFolders({ keyword: keyword.value, page: page.value, pageSize })
    folders.value = result.data
    total.value = result.total
  } catch (e: any) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function openCreate() {
  isEdit.value = false; editId.value = ''
  Object.assign(form, { unit_name: '', tags: '' })
  dialogVisible.value = true
}

function openEdit(folder: ResourceFolder) {
  isEdit.value = true; editId.value = folder.id
  Object.assign(form, { unit_name: folder.unit_name, tags: folder.tags || '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.unit_name.trim()) { ElMessage.warning('请输入供稿单位名称'); return }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateFolder(editId.value, { ...form })
      ElMessage.success('修改成功')
    } else {
      await createFolder({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
  finally { submitting.value = false }
}

async function handleDelete(folder: ResourceFolder) {
  try {
    await ElMessageBox.confirm(`确定删除「${folder.unit_name}」文件夹及其所有文件吗？`, '删除确认', { type: 'warning' })
    await deleteFolder(folder.id)
    ElMessage.success('已删除')
    loadData()
  } catch {}
}

function formatDate(d: string) { return d ? d.substring(0, 10) : '' }
onMounted(loadData)
</script>
