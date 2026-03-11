<template>
  <div class="outline-tree-node">
    <div
      :class="[
        'group flex items-center gap-2 py-2 px-2 rounded-lg cursor-pointer transition-colors',
        selectedId === item.id ? 'bg-primary/10 text-primary' : 'hover:bg-slate-50',
        !item.hasPermission && 'opacity-40'
      ]"
      :style="{ paddingLeft: `${(level || 0) * 12 + 8}px` }"
      @click="item.hasPermission && $emit('select', item)"
    >
      <button
        v-if="item.children?.length"
        @click.stop="expanded = !expanded"
        class="p-0.5 rounded hover:bg-slate-200 transition-transform"
        :class="{ 'rotate-90': expanded }"
      >
        <span class="material-symbols-outlined text-lg">chevron_right</span>
      </button>
      <span v-else class="w-6" />
      <span v-if="!item.hasPermission" class="material-symbols-outlined text-slate-400 text-sm">lock</span>
      <span class="flex-1 truncate text-sm font-medium">{{ item.title }}</span>
      <span
        :class="[
          'px-1.5 py-0.5 rounded text-xs',
          item.status === 'submitted' && 'bg-green-100 text-green-700',
          item.status === 'in_progress' && 'bg-primary/20 text-primary',
          item.status === 'not_started' && 'bg-slate-100 text-slate-600'
        ]"
      >
        {{ statusLabel(item.status) }}
      </span>
      <span v-if="item.version > 0" class="text-xs text-slate-500">v{{ item.version }}</span>
      <div v-if="item.hasPermission" class="opacity-0 group-hover:opacity-100 flex items-center gap-0.5 transition-opacity">
        <button @click.stop="$emit('smart-compile', item)" class="p-1 rounded hover:bg-slate-200" title="智能编纂">
          <span class="material-symbols-outlined text-lg">psychology</span>
        </button>
        <button @click.stop="$emit('merge', item)" class="p-1 rounded hover:bg-slate-200" title="合并">
          <span class="material-symbols-outlined text-lg">call_merge</span>
        </button>
        <button @click.stop="$emit('split', item)" class="p-1 rounded hover:bg-slate-200" title="拆分">
          <span class="material-symbols-outlined text-lg">call_split</span>
        </button>
      </div>
    </div>
    <div v-if="item.children?.length && expanded" class="ml-0">
      <ProofreadOutlineTreeNode
        v-for="child in item.children"
        :key="child.id"
        :item="child"
        :selected-id="selectedId"
        :level="(level || 0) + 1"
        @select="$emit('select', $event)"
        @smart-compile="$emit('smart-compile', $event)"
        @merge="$emit('merge', $event)"
        @split="$emit('split', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  item: { type: Object, required: true },
  selectedId: { type: [String, Number], default: null },
  level: { type: Number, default: 0 }
})

defineEmits(['select', 'smart-compile', 'merge', 'split'])

const expanded = ref(true)

function statusLabel(status) {
  const map = { submitted: '已提交', in_progress: '编辑中', not_started: '未开始' }
  return map[status] || '未开始'
}
</script>
