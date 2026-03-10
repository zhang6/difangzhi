<template>
  <div class="dashboard">
    <h2>工作台</h2>
    <el-row :gutter="20" class="cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat">
            <span class="num">{{ progress.total }}</span>
            <span class="label">总条目数</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat">
            <span class="num success">{{ progress.completed }}</span>
            <span class="label">已完成</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat">
            <span class="num warning">{{ progress.inReview }}</span>
            <span class="label">审核中</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat">
            <span class="num">{{ progress.completionRate?.toFixed(1) }}%</span>
            <span class="label">完成率</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="flow-card">
      <template #header>编写流程</template>
      <div class="flow">
        资料采集 → 自动整理 → AI辅助编写 → 编辑审核 → 出版发布
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const progress = ref({ total: 0, completed: 0, inReview: 0, completionRate: 0 })

onMounted(async () => {
  const { data } = await api.get('/api/statistics/progress')
  progress.value = data
})
</script>

<style scoped>
.dashboard h2 { margin-bottom: 20px; }
.cards { margin-bottom: 20px; }
.stat { text-align: center; padding: 10px 0; }
.stat .num { font-size: 28px; font-weight: bold; display: block; }
.stat .num.success { color: #67c23a; }
.stat .num.warning { color: #e6a23c; }
.stat .label { color: #909399; font-size: 14px; }
.flow { font-size: 16px; color: #606266; text-align: center; padding: 20px; }
</style>
