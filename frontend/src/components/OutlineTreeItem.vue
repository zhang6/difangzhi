<template>
  <div class="outline-tree-item">
    <div
      :class="[
        'flex items-center gap-2 py-2.5 px-3 rounded-lg group hover:bg-slate-50 transition-all duration-200',
        level > 0 && 'border-l-2 border-slate-100 ml-2'
      ]"
      :style="{ paddingLeft: `${level * 20 + 12}px` }"
    >
      <button
        v-if="children.length > 0"
        @click="expanded = !expanded"
        class="p-0.5 rounded-lg hover:bg-slate-200 transition-all shrink-0"
        :class="{ '-rotate-90': !expanded }"
      >
        <span class="material-symbols-outlined text-lg">expand_more</span>
      </button>
      <span v-else class="w-6 shrink-0" />

      <!-- Title + unit name (for child items) -->
      <div class="flex-1 min-w-0">
        <template v-if="item._editing">
          <input
            ref="editInputRef"
            v-model="editTitle"
            type="text"
            class="w-full px-2 py-1 rounded-lg border border-slate-200 focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none text-sm"
            @blur="finishEdit"
            @keydown.enter="finishEdit"
          />
        </template>
        <template v-else>
          <span class="font-medium text-slate-800 block truncate">{{ item.title }}</span>
          <template v-if="level > 0">
            <span v-if="item.unit_name || assignedUserName !== '-'" class="text-xs text-slate-500 truncate block mt-0.5">
              {{ item.unit_name ? `供稿单位：${item.unit_name}` : '' }}{{ item.unit_name && assignedUserName !== '-' ? ' · ' : '' }}{{ assignedUserName !== '-' ? `负责人：${assignedUserName}` : '' }}
            </span>
          </template>
        </template>
      </div>

      <!-- Status badge -->
      <span
        :class="[
          'px-2 py-0.5 rounded text-xs shrink-0 font-medium',
          item.status === 'submitted' && 'bg-emerald-50 text-emerald-600',
          item.status === 'in_progress' && 'bg-primary/10 text-primary',
          item.status === 'not_started' && 'bg-slate-100 text-slate-600'
        ]"
      >
        {{ statusLabel(item.status) }}
      </span>

      <!-- Operations - smooth hover -->
      <div class="opacity-0 group-hover:opacity-100 flex items-center gap-0.5 shrink-0 transition-opacity duration-200">
        <button @click="$emit('move-up', item)" class="p-1.5 rounded-lg hover:bg-slate-200 transition-colors" title="上移">
          <span class="material-symbols-outlined text-lg">arrow_upward</span>
        </button>
        <button @click="$emit('move-down', item)" class="p-1.5 rounded-lg hover:bg-slate-200 transition-colors" title="下移">
          <span class="material-symbols-outlined text-lg">arrow_downward</span>
        </button>
        <template v-if="level > 0">
          <button @click="$emit('assign', item)" class="p-1.5 rounded-lg hover:bg-slate-200 transition-colors" title="分配">
            <span class="material-symbols-outlined text-lg">person_add</span>
          </button>
          <button @click="$emit('smart-compile', item)" class="p-1.5 rounded-lg hover:bg-slate-200 transition-colors" title="智能编纂">
            <span class="material-symbols-outlined text-lg">psychology</span>
          </button>
        </template>
        <button @click="$emit('delete', item)" class="p-1.5 rounded-lg hover:bg-red-100 text-red-500 transition-colors" title="删除">
          <span class="material-symbols-outlined text-lg">delete</span>
        </button>
        <button @click="$emit('edit', item)" class="p-1.5 rounded-lg hover:bg-slate-200 transition-colors" title="编辑">
          <span class="material-symbols-outlined text-lg">edit</span>
        </button>
        <button @click="$emit('add-sibling', item)" class="p-1.5 rounded-lg hover:bg-primary/10 text-primary transition-colors" title="新增同级">
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
