<template>
  <div class="outline-tree-item">
    <div
      :class="[
        'flex items-center gap-2 py-2 px-3 rounded-lg group hover:bg-slate-50 transition-colors',
        `pl-${Math.min(level * 4 + 3, 15)}`
      ]"
      :style="{ paddingLeft: `${level * 16 + 12}px` }"
    >
      <button
        v-if="children.length > 0"
        @click="expanded = !expanded"
        class="p-0.5 rounded hover:bg-slate-200 transition-transform shrink-0"
        :class="{ '-rotate-90': !expanded }"
      >
        <span class="material-symbols-outlined text-lg">expand_more</span>
      </button>
      <span v-else class="w-6 shrink-0" />

      <!-- Inline edit or title -->
      <template v-if="item._editing">
        <input
          ref="editInputRef"
          v-model="editTitle"
          type="text"
          class="flex-1 px-2 py-1 rounded border border-slate-200 focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none text-sm"
          @blur="finishEdit"
          @keydown.enter="finishEdit"
        />
      </template>
      <template v-else>
        <span class="flex-1 font-medium text-slate-800 min-w-0 truncate">{{ item.title }}</span>
      </template>

      <!-- Child-only: 负责人, 对应单位 -->
      <template v-if="level > 0">
        <span class="text-sm text-slate-500 truncate max-w-24">{{ assignedUserName }}</span>
        <span class="text-sm text-slate-500 truncate max-w-24">{{ item.unit_name || '-' }}</span>
      </template>

      <!-- Status badge -->
      <span
        :class="[
          'px-2 py-0.5 rounded text-xs shrink-0',
          item.status === 'submitted' && 'bg-green-100 text-green-700',
          item.status === 'in_progress' && 'bg-primary/20 text-primary',
          item.status === 'not_started' && 'bg-slate-100 text-slate-600'
        ]"
      >
        {{ statusLabel(item.status) }}
      </span>

      <!-- Operations -->
      <div class="opacity-0 group-hover:opacity-100 flex items-center gap-0.5 shrink-0 transition-opacity">
        <button @click="$emit('move-up', item)" class="p-1 rounded hover:bg-slate-200" title="上移">
          <span class="material-symbols-outlined text-lg">arrow_upward</span>
        </button>
        <button @click="$emit('move-down', item)" class="p-1 rounded hover:bg-slate-200" title="下移">
          <span class="material-symbols-outlined text-lg">arrow_downward</span>
        </button>
        <template v-if="level > 0">
          <button @click="$emit('assign', item)" class="p-1 rounded hover:bg-slate-200" title="分配">
            <span class="material-symbols-outlined text-lg">person_add</span>
          </button>
          <button @click="$emit('smart-compile', item)" class="p-1 rounded hover:bg-slate-200" title="智能编纂">
            <span class="material-symbols-outlined text-lg">psychology</span>
          </button>
        </template>
        <button @click="$emit('delete', item)" class="p-1 rounded hover:bg-red-100 text-red-500" title="删除">
          <span class="material-symbols-outlined text-lg">delete</span>
        </button>
        <button @click="$emit('edit', item)" class="p-1 rounded hover:bg-slate-200" title="编辑">
          <span class="material-symbols-outlined text-lg">edit</span>
        </button>
        <button @click="$emit('add-sibling', item)" class="p-1 rounded hover:bg-slate-200 text-primary" title="新增同级">
          <span class="material-symbols-outlined text-lg">add</span>
        </button>
      </div>
    </div>

    <div v-if="children.length > 0 && expanded" class="ml-0">
        <OutlineTreeItem
        v-for="child in children"
        :key="child.id"
        :item="child"
        :all-items="allItems"
        :users="users"
        :level="level + 1"
        :max-level="maxLevel"
        @move-up="$emit('move-up', $event)"
        @move-down="$emit('move-down', $event)"
        @delete="$emit('delete', $event)"
        @edit="$emit('edit', $event)"
        @add-sibling="$emit('add-sibling', $event)"
        @assign="$emit('assign', $event)"
        @smart-compile="$emit('smart-compile', $event)"
        @update="$emit('update', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'

const props = defineProps({
  item: { type: Object, required: true },
  allItems: { type: Array, default: () => [] },
  users: { type: Array, default: () => [] },
  level: { type: Number, default: 0 },
  maxLevel: { type: Number, default: 3 }
})

const emit = defineEmits(['move-up', 'move-down', 'delete', 'edit', 'add-sibling', 'assign', 'smart-compile', 'update'])

const expanded = ref(true)
const editTitle = ref('')
const editInputRef = ref(null)

const children = computed(() => {
  return props.allItems
    .filter(o => o.parent_id === props.item.id)
    .sort((a, b) => (a.sort_order ?? 0) - (b.sort_order ?? 0))
})

const assignedUserName = computed(() => {
  const uid = props.item.assigned_user_id
  if (!uid) return '-'
  const u = props.users.find(us => us.id === uid)
  return u?.name || u?.username || '已分配'
})

function statusLabel(status) {
  const map = { not_started: '未开始', in_progress: '编纂中', submitted: '已提交' }
  return map[status] || '未开始'
}

watch(() => props.item._editing, (editing) => {
  if (editing) {
    editTitle.value = props.item.title
    nextTick(() => editInputRef.value?.focus())
  }
})

function finishEdit() {
  props.item._editing = false
  if (editTitle.value.trim()) {
    emit('update', props.item, editTitle.value.trim())
  }
}
</script>
