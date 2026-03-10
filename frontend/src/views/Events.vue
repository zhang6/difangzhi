<template>
  <div class="events">
    <el-card>
      <template #header>
        <span>大事记</span>
        <el-button type="primary" @click="aiGenerate" style="float:right">AI自动生成</el-button>
      </template>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="eventTime" label="时间" width="120" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="content" label="内容" min-width="300" />
        <el-table-column prop="source" label="来源" width="120" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link @click="edit(row)">编辑</el-button>
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/api/events')
    list.value = data || []
  } finally {
    loading.value = false
  }
}

async function aiGenerate() {
  try {
    const { data } = await api.post('/api/events/ai-generate')
    ElMessage.success(`已生成 ${data.length} 条大事记`)
    load()
  } catch (e) {
    ElMessage.error('生成失败')
  }
}

function edit(row) {
  ElMessage.info('编辑功能开发中')
}

function del(row) {
  ElMessageBox.confirm('确定删除？').then(async () => {
    await api.delete(`/api/events/${row.id}`)
    ElMessage.success('已删除')
    load()
  })
}

onMounted(load)
</script>
