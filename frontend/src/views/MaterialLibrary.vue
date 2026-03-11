<template>
  <div class="p-6 max-w-7xl mx-auto">
    <!-- Header area -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div>
        <h1 class="text-2xl font-bold text-slate-900">资料库</h1>
        <p class="text-slate-500 mt-0.5">管理各供稿单位提交的资料</p>
      </div>
      <button
        @click="showCreateDialog = true"
        class="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-primary text-white font-medium hover:bg-primary/90 transition-all shadow-md shrink-0"
      >
        <span class="material-symbols-outlined text-xl">create_new_folder</span>
        创建文件夹
      </button>
    </div>

    <!-- Search bar -->
    <div class="relative mb-6">
      <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-xl pointer-events-none">search</span>
      <input
        v-model="searchQuery"
        type="text"
        placeholder="请输入供稿单位名称或标签"
        class="w-full pl-12 pr-12 py-3 rounded-xl shadow-sm ring-1 ring-slate-200 focus:ring-2 focus:ring-primary/20 focus:ring-primary outline-none transition-all"
      />
      <button
        v-if="searchQuery"
        @click="searchQuery = ''"
        class="absolute right-3 top-1/2 -translate-y-1/2 p-1 rounded-lg hover:bg-slate-100 text-slate-600 transition-colors"
      >
        <span class="material-symbols-outlined text-xl">close</span>
      </button>
    </div>

    <!-- Folder grid -->
    <div v-if="filteredFolders.length > 0" class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      <div
        v-for="folder in paginatedFolders"
        :key="folder.id"
        @click="$router.push(`/materials/${folder.id}`)"
        class="bg-white rounded-xl border border-slate-200 p-5 cursor-pointer hover:shadow-lg hover:border-primary/20 transition-all group"
      >
        <div class="size-12 flex items-center justify-center bg-primary/10 rounded-xl text-primary mb-4 group-hover:scale-105 transition-transform">
          <span class="material-symbols-outlined text-2xl">folder</span>
        </div>
        <h3 class="font-bold text-base text-slate-900 truncate">{{ folder.unit_name }}</h3>
        <p class="text-xs text-slate-500 mt-1">共 {{ folder.file_count ?? 0 }} 个文件</p>
        <div v-if="folderTags(folder).length" class="flex flex-wrap gap-1.5 mt-3">
          <span
            v-for="tag in folderTags(folder)"
            :key="tag"
            class="px-2 py-0.5 rounded bg-primary/5 text-primary text-xs"
          >
            {{ tag }}
          </span>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div
      v-else
      class="flex flex-col items-center justify-center py-20 text-slate-400"
    >
      <span class="material-symbols-outlined text-6xl mb-4">search_off</span>
      <p class="text-lg">无相关供稿单位</p>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="flex justify-center gap-2">
      <button
        v-for="p in totalPages"
        :key="p"
        @click="currentPage = p"
        :class="[
          'w-10 h-10 rounded-lg font-medium transition-all',
          currentPage === p
            ? 'bg-primary text-white shadow'
            : 'bg-white border border-slate-200 text-slate-600 hover:border-primary/30 hover:text-primary'
        ]"
      >
        {{ p }}
      </button>
    </div>

    <!-- Create folder dialog -->
    <Teleport to="body">
      <div
        v-if="showCreateDialog"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm"
        @click.self="showCreateDialog = false"
      >
        <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6">
          <h2 class="text-xl font-bold text-slate-900 mb-6">创建文件夹</h2>
          <form @submit.prevent="createFolder" class="space-y-5">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1.5">供稿单位 <span class="text-red-500">*</span></label>
              <input
                v-model="createForm.unit_name"
                type="text"
                maxlength="20"
                placeholder="请输入供稿单位名称"
                class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
              />
              <p class="text-xs text-slate-500 mt-1">{{ createForm.unit_name.length }}/20</p>
              <p v-if="createError" class="text-sm text-red-500 mt-1">{{ createError }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1.5">文件标签（选填）</label>
              <input
                v-model="createForm.tags"
                type="text"
                placeholder="多个标签用逗号分隔"
                class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
              />
            </div>
            <div class="flex gap-3 pt-2">
              <button
                type="button"
                @click="showCreateDialog = false"
                class="flex-1 py-3 rounded-xl border border-slate-200 text-slate-600 font-medium hover:bg-slate-50 transition-all"
              >
                取消
              </button>
              <button
                type="submit"
                :disabled="creating"
                class="flex-1 py-3 rounded-xl bg-primary text-white font-medium hover:bg-primary/90 disabled:opacity-70 transition-all"
              >
                {{ creating ? '创建中...' : '确认创建' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, inject } from 'vue'
import { supabase } from '@/lib/supabase'

const toast = inject('toast')

const folders = ref([])
const searchQuery = ref('')
const showCreateDialog = ref(false)
const creating = ref(false)
const createError = ref('')
const currentPage = ref(1)
const pageSize = 12

const createForm = ref({
  unit_name: '',
  tags: ''
})

function folderTags(folder) {
  if (!folder.tags) return []
  return folder.tags.split(',').map(t => t.trim()).filter(Boolean)
}

const filteredFolders = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return folders.value
  return folders.value.filter(f => {
    const name = (f.unit_name || '').toLowerCase()
    const tags = folderTags(f).map(t => t.toLowerCase())
    return name.includes(q) || tags.some(t => t.includes(q))
  })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredFolders.value.length / pageSize)))

const paginatedFolders = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredFolders.value.slice(start, start + pageSize)
})

watch(searchQuery, () => { currentPage.value = 1 })

async function loadFolders() {
  const { data, error } = await supabase
    .from('yb_material_folders')
    .select('*')
    .order('created_at', { ascending: false })
  if (error) {
    console.error('加载文件夹失败:', error)
    toast?.('加载文件夹失败', 'error')
    return
  }
  folders.value = data || []
}

async function createFolder() {
  createError.value = ''
  const name = createForm.value.unit_name.trim()
  if (!name) {
    createError.value = '请输入供稿单位名称'
    return
  }
  if (name.length > 20) {
    createError.value = '供稿单位名称不能超过20个字符'
    return
  }
  creating.value = true
  try {
    const tagsStr = createForm.value.tags.trim()
    const { data, error } = await supabase
      .from('yb_material_folders')
      .insert({
        unit_name: name,
        tags: tagsStr || null,
        file_count: 0
      })
      .select()
      .single()
    if (error) throw error
    folders.value = [data, ...folders.value]
    showCreateDialog.value = false
    createForm.value = { unit_name: '', tags: '' }
    toast?.('创建成功', 'success')
  } catch (e) {
    createError.value = e.message?.includes('unique') ? '该供稿单位已存在' : (e.message || '创建失败')
    toast?.(createError.value, 'error')
  } finally {
    creating.value = false
  }
}

loadFolders()
</script>
