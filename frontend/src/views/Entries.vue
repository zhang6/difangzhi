<template>
  <div class="entries">
    <el-card>
      <template #header>
        <span>条目编写</span>
        <el-button type="primary" @click="createEntry" style="float:right">新建条目</el-button>
      </template>
      <el-form :inline="true" class="filter">
        <el-form-item>
          <el-select v-model="filters.status" placeholder="状态" clearable @change="load">
            <el-option label="未开始" value="NOT_STARTED" />
            <el-option label="编写中" value="IN_PROGRESS" />
            <el-option label="审核中" value="IN_REVIEW" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/entries/${row.id}`)">编辑</el-button>
            <el-button link @click="aiGenerate(row)">AI生成</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const list = ref([])
const loading = ref(false)
const filters = reactive({ status: '' })

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/api/entries', {
      params: { status: filters.status || undefined }
    })
    list.value = data || []
  } finally {
    loading.value = false
  }
}

async function createEntry() {
  try {
    const { data } = await api.post('/api/entries', {
      title: '新条目',
      catalogId: 1,
      content: '',
      status: 'NOT_STARTED'
    })
    ElMessage.success('创建成功')
    location.href = `/entries/${data.id}`
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

async function aiGenerate(row) {
  try {
    await api.post(`/api/entries/${row.id}/ai-generate`)
    ElMessage.success('AI生成完成')
    load()
  } catch (e) {
    ElMessage.error('生成失败')
  }
}

onMounted(load)
</script>

<style scoped>
.filter { margin-bottom: 16px; }
</style>
