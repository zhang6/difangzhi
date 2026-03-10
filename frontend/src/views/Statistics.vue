<template>
  <div class="statistics">
    <el-card>
      <template #header>编写进度统计</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="总条目数">{{ progress.total }}</el-descriptions-item>
        <el-descriptions-item label="已完成">{{ progress.completed }}</el-descriptions-item>
        <el-descriptions-item label="审核中">{{ progress.inReview }}</el-descriptions-item>
        <el-descriptions-item label="未开始">{{ progress.notStarted }}</el-descriptions-item>
        <el-descriptions-item label="编写中">{{ progress.inProgress }}</el-descriptions-item>
        <el-descriptions-item label="完成率">{{ progress.completionRate?.toFixed(1) }}%</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const progress = ref({})

onMounted(async () => {
  const { data } = await api.get('/api/statistics/progress')
  progress.value = data
})
</script>
