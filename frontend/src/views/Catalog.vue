<template>
  <div class="catalog">
    <el-card>
      <template #header>
        <span>志书纲目</span>
        <el-button type="primary" @click="addRoot" style="float:right">添加篇</el-button>
      </template>
      <el-tree
        :data="tree"
        :props="{ label: 'title', children: 'children' }"
        node-key="id"
        default-expand-all
        draggable
      >
        <template #default="{ node, data }">
          <span class="node">
            <span>{{ data.title }}</span>
            <el-button link size="small" @click.stop="addChild(data)">添加子项</el-button>
            <el-button link size="small" @click.stop="edit(data)">编辑</el-button>
            <el-button link type="danger" size="small" @click.stop="del(data)">删除</el-button>
          </span>
        </template>
      </el-tree>
    </el-card>

    <el-dialog v-model="showEdit" :title="editing.id ? '编辑' : '新增'" width="500">
      <el-form :model="form" label-width="80">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="层级">
          <el-select v-model="form.level" style="width:100%">
            <el-option label="篇" :value="1" />
            <el-option label="章" :value="2" />
            <el-option label="节" :value="3" />
            <el-option label="条目" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const tree = ref([])
const showEdit = ref(false)
const editing = ref({})
const form = reactive({ title: '', level: 1, parentId: null })

async function load() {
  const { data } = await api.get('/api/catalogs/tree')
  tree.value = buildTree(Array.isArray(data) ? data : [], null)
}

function buildTree(items, parentId) {
  const roots = items.filter(c => (c.parentId == null && parentId == null) || (c.parentId === parentId))
  return roots.map(c => ({
    ...c,
    children: buildTree(items, c.id)
  }))
}

function addRoot() {
  editing.value = {}
  form.title = ''
  form.level = 1
  form.parentId = null
  showEdit.value = true
}

function addChild(parent) {
  editing.value = {}
  form.title = ''
  form.level = (parent.level || 1) + 1
  form.parentId = parent.id
  showEdit.value = true
}

function edit(row) {
  editing.value = row
  form.title = row.title
  form.level = row.level || 1
  form.parentId = row.parentId
  showEdit.value = true
}

async function save() {
  const payload = { title: form.title, level: form.level, parentId: form.parentId }
  if (editing.value.id) {
    await api.put(`/api/catalogs/${editing.value.id}`, payload)
  } else {
    await api.post('/api/catalogs', payload)
  }
  ElMessage.success('保存成功')
  showEdit.value = false
  load()
}

function del(row) {
  ElMessageBox.confirm('确定删除？').then(async () => {
    await api.delete(`/api/catalogs/${row.id}`)
    ElMessage.success('已删除')
    load()
  })
}

onMounted(load)
</script>

<style scoped>
.node { display: flex; align-items: center; gap: 8px; }
</style>
