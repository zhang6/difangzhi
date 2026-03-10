<template>
  <div class="materials">
    <el-card>
      <template #header>
        <span>资料管理</span>
        <el-button type="primary" @click="showUpload = true" style="float:right">上传资料</el-button>
      </template>
      <el-form :inline="true" class="filter">
        <el-form-item>
          <el-input v-model="filters.title" placeholder="标题" clearable @clear="load" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="filters.type" placeholder="分类" clearable @change="load">
            <el-option label="政府报告" value="GOVERNMENT_REPORT" />
            <el-option label="统计数据" value="STATISTICS" />
            <el-option label="新闻资料" value="NEWS" />
            <el-option label="部门资料" value="DEPARTMENT" />
            <el-option label="历史资料" value="HISTORY" />
            <el-option label="图片资料" value="IMAGE" />
            <el-option label="表格数据" value="TABLE_DATA" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="source" label="来源" width="120" />
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column prop="type" label="分类" width="120" />
        <el-table-column prop="createdAt" label="上传时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link @click="genSummary(row)">AI摘要</el-button>
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        :page-size="10"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
        style="margin-top:16px"
      />
    </el-card>

    <el-dialog v-model="showUpload" title="上传资料" width="500">
      <el-form :model="uploadForm" label-width="80">
        <el-form-item label="文件">
          <el-upload
            :auto-upload="false"
            :limit="1"
            :on-change="(f) => uploadForm.file = f.raw"
          >
            <el-button>选择文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="uploadForm.title" /></el-form-item>
        <el-form-item label="来源"><el-input v-model="uploadForm.source" /></el-form-item>
        <el-form-item label="年份"><el-input-number v-model="uploadForm.year" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="uploadForm.type" style="width:100%">
            <el-option label="政府报告" value="GOVERNMENT_REPORT" />
            <el-option label="统计数据" value="STATISTICS" />
            <el-option label="新闻资料" value="NEWS" />
            <el-option label="部门资料" value="DEPARTMENT" />
            <el-option label="历史资料" value="HISTORY" />
            <el-option label="图片资料" value="IMAGE" />
            <el-option label="表格数据" value="TABLE_DATA" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUpload = false">取消</el-button>
        <el-button type="primary" @click="upload" :loading="uploading">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const filters = reactive({ title: '', type: '' })
const showUpload = ref(false)
const uploading = ref(false)
const uploadForm = reactive({ file: null, title: '', source: '', year: new Date().getFullYear(), type: 'NEWS' })

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/api/materials', {
      params: { page: page.value - 1, size: 10, title: filters.title || undefined, type: filters.type || undefined }
    })
    list.value = data.content || []
    total.value = data.totalElements || 0
  } finally {
    loading.value = false
  }
}

async function upload() {
  uploading.value = true
  try {
    const payload = { title: uploadForm.title || '新资料', source: uploadForm.source, year: uploadForm.year, type: uploadForm.type }
    await api.post('/api/materials', payload)
    ElMessage.success('保存成功')
    showUpload.value = false
    load()
  } catch (e) {
    ElMessage.error(e.response?.data?.error || e.message || '保存失败')
  } finally {
    uploading.value = false
  }
}

async function genSummary(row) {
  try {
    const { data } = await api.post(`/api/materials/${row.id}/ai-summary?length=500`)
    ElMessage.success('摘要已生成')
    row.summary = data.summary
  } catch (e) {
    ElMessage.error('生成失败')
  }
}

function del(row) {
  ElMessageBox.confirm('确定删除？').then(async () => {
    await api.delete(`/api/materials/${row.id}`)
    ElMessage.success('已删除')
    load()
  })
}

onMounted(load)
</script>

<style scoped>
.filter { margin-bottom: 16px; }
</style>
