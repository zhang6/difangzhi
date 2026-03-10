<template>
  <div class="entry-edit">
    <el-card v-loading="loading">
      <template #header>
        <span>条目编辑</span>
        <el-button type="primary" @click="aiGenerate" style="float:right">AI辅助生成</el-button>
      </template>
      <el-form :model="form" label-width="80">
        <el-form-item label="标题">
          <el-input v-model="form.title" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:200px">
            <el-option label="未开始" value="NOT_STARTED" />
            <el-option label="编写中" value="IN_PROGRESS" />
            <el-option label="审核中" value="IN_REVIEW" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="20" style="font-family:inherit" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存</el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const id = computed(() => route.params.id)
const loading = ref(false)
const form = reactive({ title: '', content: '', status: 'NOT_STARTED' })

async function load() {
  loading.value = true
  try {
    const { data } = await api.get(`/api/entries/${id.value}`)
    Object.assign(form, data)
  } finally {
    loading.value = false
  }
}

async function save() {
  await api.put(`/api/entries/${id.value}`, form)
  ElMessage.success('保存成功')
}

async function aiGenerate() {
  try {
    const { data } = await api.post(`/api/entries/${id.value}/ai-generate`)
    form.content = data.content
    ElMessage.success('AI生成完成')
  } catch (e) {
    ElMessage.error('生成失败')
  }
}

onMounted(load)
</script>
