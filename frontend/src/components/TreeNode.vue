<template>
  <div>
    <div
      class="tree-node-item"
      :class="{ active: selectedId === node.id, 'opacity-60': !isAdmin && !node.assigned_user_id }"
      @click="$emit('select', node)"
    >
      <!-- 展开/收起图标 -->
      <button
        v-if="node.children && node.children.length > 0"
        class="flex-shrink-0 w-5 h-5 flex items-center justify-center text-slate-400 hover:text-slate-600 transition-transform"
        :class="{ 'rotate-90': expanded }"
        @click.stop="expanded = !expanded"
      >
        <span class="material-symbols-outlined" style="font-size:16px;">chevron_right</span>
      </button>
      <span v-else class="w-5 flex-shrink-0"></span>

      <!-- 文件夹/文件图标 -->
      <span
        class="material-symbols-outlined flex-shrink-0"
        :class="folderColor"
        style="font-size:15px;"
      >{{ node.children && node.children.length > 0 ? 'folder' : 'article' }}</span>

      <!-- 标题 -->
      <span class="flex-1 truncate text-xs">{{ node.title }}</span>

      <!-- 状态标签 -->
      <span class="badge text-[10px] py-0 px-1.5 flex-shrink-0" :class="statusBadge(node.status)">
        {{ statusLabel(node.status) }}
      </span>

      <!-- 操作按钮组（hover 时显示） -->
      <div v-if="isAdmin" class="flex-shrink-0 flex gap-0.5 opacity-0 group-hover:opacity-100 node-actions">
        <button
          class="p-0.5 rounded text-slate-400 hover:text-blue-500 hover:bg-blue-50"
          title="添加子节点"
          @click.stop="$emit('add-child', node)"
        >
          <span class="material-symbols-outlined" style="font-size:13px;">add</span>
        </button>
        <button
          class="p-0.5 rounded text-slate-400 hover:text-blue-500 hover:bg-blue-50"
          title="编辑"
          @click.stop="$emit('edit', node)"
        >
          <span class="material-symbols-outlined" style="font-size:13px;">edit</span>
        </button>
        <button
          class="p-0.5 rounded text-slate-400 hover:text-purple-500 hover:bg-purple-50"
          title="指派"
          @click.stop="$emit('assign', node)"
        >
          <span class="material-symbols-outlined" style="font-size:13px;">how_to_reg</span>
        </button>
        <button
          class="p-0.5 rounded text-slate-400 hover:text-red-500 hover:bg-red-50"
          title="删除"
          @click.stop="$emit('delete', node)"
        >
          <span class="material-symbols-outlined" style="font-size:13px;">delete</span>
        </button>
      </div>
      <button
        v-if="node.level > 1"
        class="p-0.5 rounded text-slate-400 hover:text-purple-600 hover:bg-purple-50 flex-shrink-0 node-compile"
        title="智能编纂"
        @click.stop="$emit('compile', node)"
      >
        <span class="material-symbols-outlined" style="font-size:13px;">auto_fix_high</span>
      </button>
    </div>

    <!-- 子节点 -->
    <div v-if="expanded && node.children && node.children.length > 0" class="pl-5">
      <TreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :selected-id="selectedId"
        :is-admin="isAdmin"
        @select="$emit('select', $event)"
        @add-child="$emit('add-child', $event)"
        @edit="$emit('edit', $event)"
        @delete="$emit('delete', $event)"
        @assign="$emit('assign', $event)"
        @compile="$emit('compile', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { OutlineNode } from '@/types'

interface Props {
  node: OutlineNode
  selectedId?: string
  isAdmin?: boolean
}
const props = defineProps<Props>()
defineEmits(['select', 'add-child', 'edit', 'delete', 'assign', 'compile'])

const expanded = ref(true)

const folderColors = ['text-blue-500', 'text-amber-500', 'text-green-500', 'text-purple-500', 'text-slate-500']
const folderColor = computed(() => folderColors[(props.node.level - 1) % folderColors.length])

function statusLabel(s: string) {
  return { not_started: '未开始', in_progress: '编纂中', submitted: '已提交', editing: '编辑中' }[s] || s
}
function statusBadge(s: string) {
  return { not_started: 'badge-gray', in_progress: 'badge-yellow', submitted: 'badge-green', editing: 'badge-blue' }[s] || 'badge-gray'
}
</script>

<style scoped>
.tree-node-item { display: flex; align-items: center; gap: 4px; padding: 6px 8px; border-radius: 8px; cursor: pointer; font-size: 12px; color: #4b5563; transition: all 0.15s; }
.tree-node-item:hover { background: #f8fafc; }
.tree-node-item.active { background: #eff6ff; color: #1d4ed8; font-weight: 500; }
.tree-node-item:hover .node-actions { opacity: 1 !important; }
.tree-node-item:hover .node-compile { opacity: 1 !important; }
.node-actions { opacity: 0; }
.node-compile { opacity: 0; }
</style>
