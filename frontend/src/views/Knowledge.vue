<template>
  <div class="knowledge">
    <el-card>
      <template #header>知识库检索与问答</template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全文搜索" name="search">
          <el-form :inline="true" @submit.prevent="doSearch">
            <el-form-item>
              <el-input v-model="query" placeholder="输入关键词" style="width:300px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="doSearch">搜索</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="searchResults" v-loading="searching">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="content" label="内容摘要" min-width="300" />
            <el-table-column prop="source" label="来源" width="120" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="AI问答" name="ask">
          <el-form @submit.prevent="doAsk">
            <el-form-item>
              <el-input v-model="question" placeholder="例如：XX市什么时候成立？" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="doAsk" :loading="asking">提问</el-button>
            </el-form-item>
          </el-form>
          <el-card v-if="answer" class="answer-card">
            <div v-html="answer"></div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const activeTab = ref('search')
const query = ref('')
const question = ref('')
const searchResults = ref([])
const answer = ref('')
const searching = ref(false)
const asking = ref(false)

async function doSearch() {
  if (!query.value.trim()) return
  searching.value = true
  try {
    const { data } = await api.get('/api/knowledge/search', { params: { query: query.value, limit: 10 } })
    searchResults.value = data || []
  } catch (e) {
    ElMessage.error('搜索失败')
  } finally {
    searching.value = false
  }
}

async function doAsk() {
  if (!question.value.trim()) return
  asking.value = true
  answer.value = ''
  try {
    const { data } = await api.post('/api/knowledge/ask', { question: question.value })
    answer.value = (data.answer || '').replace(/\n/g, '<br>')
  } catch (e) {
    ElMessage.error('问答失败')
  } finally {
    asking.value = false
  }
}
</script>

<style scoped>
.answer-card { margin-top: 16px; }
</style>
