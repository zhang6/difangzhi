<template>
  <div class="flex flex-col h-full overflow-hidden bg-slate-50">
    <!-- 顶部工具栏 -->
    <div class="bg-white border-b border-slate-200 px-5 py-3 flex items-center gap-4 flex-shrink-0">
      <div class="flex items-center gap-2 text-xs text-slate-400">
        <button class="hover:text-primary" @click="$router.push('/yearbooks')">年鉴管理</button>
        <span class="material-symbols-outlined" style="font-size:12px;">chevron_right</span>
        <span>{{ yearbookStore.currentYearbookName || '请先选择年鉴' }}</span>
        <span class="material-symbols-outlined" style="font-size:12px;">chevron_right</span>
        <span class="text-slate-600 font-medium">智能编纂</span>
      </div>

      <div class="ml-auto flex items-center gap-3">
        <!-- 年鉴选择 -->
        <select v-model="selectedYearbookId" class="text-xs border border-slate-300 rounded-lg px-2 py-1.5 focus:outline-none" @change="onYearbookChange">
          <option value="">选择年鉴</option>
          <option v-for="yb in yearbookList" :key="yb.id" :value="yb.id">{{ yb.name }}</option>
        </select>
        <!-- 大纲选择 -->
        <select v-model="selectedOutlineId" class="text-xs border border-slate-300 rounded-lg px-2 py-1.5 focus:outline-none" @change="onOutlineChange">
          <option value="">选择大纲节点</option>
          <optgroup v-for="node in flatOutlines" :key="node.id">
            <option :value="node.id">{{ '  '.repeat(node.level - 1) + node.title }}</option>
          </optgroup>
        </select>
      </div>

      <!-- AI助手入口 -->
      <button
        class="flex items-center gap-1.5 px-3 py-1.5 text-xs rounded-lg text-purple-600 border border-purple-300 hover:bg-purple-50 transition-colors"
        @click="botVisible=true"
      >
        <span class="material-symbols-outlined" style="font-size:14px;">smart_toy</span>
        AI助手
      </button>
    </div>

    <!-- 无选择状态 -->
    <div v-if="!selectedOutlineId" class="flex-1 flex items-center justify-center">
      <div class="text-center text-slate-400">
        <span class="material-symbols-outlined mb-3" style="font-size:56px;color:#cbd5e1;">auto_fix_high</span>
        <p class="text-sm">请从顶部选择年鉴和大纲节点开始编纂</p>
        <button class="mt-4 text-xs text-primary hover:underline" @click="$router.push('/outlines')">前往大纲管理 →</button>
      </div>
    </div>

    <!-- 三栏布局 -->
    <div v-else class="flex flex-1 overflow-hidden">

      <!-- 左栏：大纲目录 -->
      <div class="flex flex-col border-r border-slate-200 bg-white overflow-hidden flex-shrink-0" style="width:200px;">
        <div class="px-3 py-3 border-b border-slate-100 flex-shrink-0">
          <p class="text-xs font-semibold text-slate-500 mb-2">大纲目录</p>
          <div class="progress-track">
            <div class="progress-fill" :style="{ width: outlineProgress + '%' }"></div>
          </div>
          <div class="flex justify-between text-xs text-slate-400 mt-1">
            <span>整体进度</span>
            <span class="font-medium text-blue-600">{{ outlineProgress }}%</span>
          </div>
        </div>
        <div class="flex-1 overflow-y-auto py-2 px-1.5 text-xs">
          <div
            v-for="node in flatOutlines"
            :key="node.id"
            class="py-1.5 px-2 rounded cursor-pointer transition-colors flex items-center justify-between"
            :class="[
              node.level === 1 ? 'font-semibold text-slate-700 mt-1' : 'text-slate-500 pl-4',
              selectedOutlineId === node.id ? 'bg-blue-50 text-blue-700 border border-blue-200' : 'hover:bg-slate-50'
            ]"
            @click="selectOutline(node)"
          >
            <span class="truncate">{{ node.title }}</span>
            <span v-if="node.status === 'submitted'" class="material-symbols-outlined text-green-500 flex-shrink-0" style="font-size:12px;">check_circle</span>
          </div>
        </div>
      </div>

      <!-- 中栏：编辑区 -->
      <div class="flex-1 flex flex-col overflow-hidden">
        <!-- 节标题 -->
        <div class="bg-white border-b border-slate-200 px-5 py-3 flex items-center gap-3 flex-shrink-0">
          <div class="flex-1">
            <h2 class="text-sm font-bold text-slate-900">{{ currentOutlineTitle }}</h2>
            <p class="text-xs text-slate-400">{{ yearbookStore.currentYearbookName }}</p>
          </div>
          <div class="flex items-center gap-2">
            <span class="badge" :class="statusBadge(activeEntry?.status)">{{ statusLabel(activeEntry?.status) }}</span>
            <button class="flex items-center gap-1 px-2.5 py-1.5 text-xs border border-slate-300 rounded-lg text-slate-600 hover:bg-slate-50" @click="showDetect">
              <span class="material-symbols-outlined" style="font-size:13px;">fact_check</span>检测
            </button>
            <button class="flex items-center gap-1 px-2.5 py-1.5 text-xs border border-slate-300 rounded-lg text-slate-600 hover:bg-slate-50" @click="showDiff">
              <span class="material-symbols-outlined" style="font-size:13px;">difference</span>对比
            </button>
          </div>
        </div>

        <!-- AI工具栏 -->
        <div class="bg-white border-b border-slate-100 px-3 py-1.5 flex items-center gap-0.5 flex-shrink-0">
          <button class="toolbar-btn flex items-center gap-1" @click="handleAIGenerate">
            <span class="material-symbols-outlined text-purple-600" style="font-size:14px;">auto_fix_high</span>
            <span class="text-purple-600 font-medium text-xs">AI生成</span>
          </button>
          <button class="toolbar-btn flex items-center gap-1" @click="handleAIExpand">
            <span class="material-symbols-outlined text-blue-500" style="font-size:14px;">expand</span>
            <span class="text-xs">扩写</span>
          </button>
          <button class="toolbar-btn flex items-center gap-1" @click="handleAIRewrite">
            <span class="material-symbols-outlined text-green-500" style="font-size:14px;">auto_awesome</span>
            <span class="text-xs">润色</span>
          </button>
          <div class="w-px h-5 bg-slate-200 mx-1"></div>
          <button class="toolbar-btn font-bold text-sm" @click="execCmd('bold')">B</button>
          <button class="toolbar-btn italic text-sm" @click="execCmd('italic')">I</button>
          <button class="toolbar-btn underline text-xs" @click="execCmd('underline')">U</button>
          <div class="w-px h-5 bg-slate-200 mx-1"></div>
          <div class="ml-auto flex items-center gap-2">
            <span class="text-xs text-slate-400">字数：{{ wordCount }}</span>
            <button class="flex items-center gap-1 px-2.5 py-1 text-xs border border-slate-300 rounded-lg text-slate-600 hover:bg-slate-50" @click="saveVersion">
              <span class="material-symbols-outlined" style="font-size:13px;">save</span>保存版本
            </button>
            <button
              class="flex items-center gap-1 px-2.5 py-1 text-xs text-white rounded-lg hover:shadow"
              style="background:linear-gradient(135deg,#10b981,#059669);"
              @click="handleSubmit"
            >
              <span class="material-symbols-outlined" style="font-size:13px;">send</span>提交
            </button>
          </div>
        </div>

        <!-- AI生成进度条 -->
        <div v-if="aiLoading" class="bg-gradient-to-r from-blue-50 to-purple-50 px-5 py-2.5 flex items-center gap-3 border-b border-blue-100 flex-shrink-0">
          <div class="flex gap-1">
            <span class="ai-dot"></span>
            <span class="ai-dot"></span>
            <span class="ai-dot"></span>
          </div>
          <span class="text-xs text-blue-700 font-medium">AI 正在生成内容...</span>
          <div class="flex-1 progress-track">
            <div class="progress-fill transition-all duration-300" :style="{ width: aiProgress + '%' }"></div>
          </div>
          <button class="text-xs text-slate-400 hover:text-red-500" @click="cancelAI">取消</button>
        </div>

        <!-- 编辑器区域 -->
        <div class="flex-1 overflow-y-auto p-5">
          <!-- 条目列表 -->
          <div class="flex gap-2 mb-4 flex-wrap">
            <button
              v-for="entry in entries"
              :key="entry.id"
              class="px-3 py-1.5 text-xs rounded-lg border transition-colors"
              :class="activeEntry?.id === entry.id
                ? 'bg-blue-600 text-white border-blue-600'
                : 'bg-white text-slate-600 border-slate-300 hover:border-blue-400'"
              @click="selectEntry(entry)"
            >{{ entry.title }}</button>
            <button class="px-3 py-1.5 text-xs rounded-lg border border-dashed border-blue-300 text-blue-600 hover:bg-blue-50" @click="newEntryVisible=true">
              + 新建条目
            </button>
          </div>

          <!-- 编辑器 -->
          <div v-if="activeEntry" class="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
            <h3 class="text-sm font-bold text-slate-900 mb-4">【{{ activeEntry.title }}】</h3>
            <div
              ref="editorRef"
              class="rich-editor"
              contenteditable="true"
              :data-placeholder="editorPlaceholder"
              @input="onEditorInput"
              @blur="autoSave"
            ></div>
          </div>

          <div v-else class="bg-white rounded-xl border border-slate-200 p-6 text-center text-slate-400 text-sm">
            从上方选择或新建一个条目开始编辑
          </div>

          <!-- AI生成结果 -->
          <div v-if="aiResult" class="mt-4 bg-gradient-to-br from-blue-50 to-purple-50 rounded-xl border border-blue-200 p-5 fade-in">
            <div class="flex items-center gap-2 mb-3">
              <span class="material-symbols-outlined text-purple-600" style="font-size:16px;">auto_fix_high</span>
              <span class="text-sm font-semibold text-slate-800">AI 生成结果</span>
              <span class="badge badge-purple text-[10px] ml-auto">草稿</span>
            </div>
            <div class="bg-white rounded-lg p-4 text-sm text-slate-700 leading-7 border border-blue-100 whitespace-pre-wrap">{{ aiResult }}</div>
            <div class="flex justify-end gap-2 mt-3">
              <button class="px-4 py-1.5 text-xs border border-slate-300 rounded-lg text-slate-600 hover:bg-slate-50" @click="aiResult=''">放弃</button>
              <button class="px-4 py-1.5 text-xs text-white rounded-lg hover:shadow" style="background:linear-gradient(135deg,#1a90ff,#0059b3);" @click="acceptAI">采用此版本</button>
            </div>
          </div>
        </div>

        <!-- 底部状态栏 -->
        <div class="bg-white border-t border-slate-200 px-5 py-2 flex items-center justify-between text-xs text-slate-400 flex-shrink-0">
          <div class="flex items-center gap-4">
            <span class="flex items-center gap-1">
              <span class="material-symbols-outlined" style="font-size:13px;">schedule</span>
              最后保存：{{ lastSavedTime || '未保存' }}
            </span>
            <span v-if="versions.length > 0" class="flex items-center gap-1">
              <span class="material-symbols-outlined" style="font-size:13px;">history</span>
              版本：v{{ versions[0]?.version || 1 }}
            </span>
          </div>
          <div class="flex items-center gap-3">
            <button class="hover:text-primary flex items-center gap-1" @click="versionsVisible=true">
              <span class="material-symbols-outlined" style="font-size:13px;">history</span>草稿记录
            </button>
            <button class="hover:text-primary flex items-center gap-1" @click="historyVisible=true">
              <span class="material-symbols-outlined" style="font-size:13px;">library_books</span>历史数据
            </button>
          </div>
        </div>
      </div>

      <!-- 右栏：参考资料 -->
      <div class="flex flex-col border-l border-slate-200 bg-white overflow-hidden flex-shrink-0" style="width:260px;">
        <div class="flex border-b border-slate-200 flex-shrink-0">
          <button
            v-for="t in rightTabs"
            :key="t.key"
            class="flex-1 py-2.5 text-xs font-medium transition-colors border-b-2"
            :class="rightTab === t.key ? 'text-blue-600 border-blue-600' : 'text-slate-500 border-transparent hover:text-slate-700'"
            @click="rightTab=t.key"
          >{{ t.label }}</button>
        </div>

        <!-- 参考资料 Tab -->
        <div v-if="rightTab === 'materials'" class="flex-1 overflow-y-auto p-3">
          <p class="text-xs font-medium text-slate-600 mb-2">已关联资料 ({{ nodeFiles.length }})</p>
          <div v-if="nodeFiles.length === 0" class="text-center text-slate-400 text-xs py-4">暂无关联资料</div>
          <div v-else class="space-y-1.5">
            <div
              v-for="f in nodeFiles"
              :key="f.id"
              class="flex items-center gap-2 p-2 rounded-lg cursor-pointer hover:bg-blue-50 transition-colors"
              :class="selectedFiles.has(f.id) ? 'bg-blue-50' : 'bg-slate-50'"
            >
              <input type="checkbox" :checked="selectedFiles.has(f.id)" class="rounded w-3.5 h-3.5" @change="toggleFile(f.id)"/>
              <span class="material-symbols-outlined text-blue-500" style="font-size:14px;">description</span>
              <span class="text-xs text-slate-700 truncate flex-1">{{ f.file_name }}</span>
            </div>
          </div>
        </div>

        <!-- 历史数据 Tab -->
        <div v-if="rightTab === 'history'" class="flex-1 overflow-y-auto">
          <div class="px-3 pt-3 pb-2">
            <input
              v-model="historyKeyword"
              type="text"
              placeholder="搜索历史数据..."
              class="w-full px-2 py-1.5 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-blue-400"
              @keyup.enter="searchHistory"
            />
          </div>
          <div class="px-3 space-y-2 pb-3">
            <div v-if="historyData.length === 0" class="text-center text-slate-400 text-xs py-4">暂无历史数据</div>
            <div v-for="h in historyData" :key="h.id" class="border border-slate-100 rounded-lg p-3">
              <div class="text-xs font-medium text-blue-600 mb-1">{{ h.year }}年 · {{ h.yearbook_name }}</div>
              <p class="text-xs text-slate-600 leading-5 line-clamp-3">{{ h.content }}</p>
              <button class="mt-1.5 text-[11px] text-primary hover:underline" @click="useHistory(h)">引用此数据</button>
            </div>
          </div>
        </div>

        <!-- AI提示 Tab -->
        <div v-if="rightTab === 'tips'" class="flex-1 overflow-y-auto p-3">
          <div class="space-y-3">
            <div v-for="tip in aiTips" :key="tip.title" class="bg-amber-50 border-l-2 border-amber-400 p-3 rounded-r-lg">
              <p class="text-xs font-medium text-amber-800 mb-1">{{ tip.title }}</p>
              <p class="text-xs text-amber-700 leading-5">{{ tip.content }}</p>
            </div>
          </div>
        </div>

        <!-- AI生成按钮 -->
        <div class="p-3 border-t border-slate-200 flex-shrink-0">
          <button
            class="w-full py-2.5 text-xs text-white rounded-xl font-medium flex items-center justify-center gap-2 hover:opacity-90 transition-opacity"
            style="background:linear-gradient(135deg,#7c3aed,#1a90ff);"
            :disabled="aiLoading"
            @click="handleAIGenerate"
          >
            <span class="material-symbols-outlined" style="font-size:16px;">auto_fix_high</span>
            {{ aiLoading ? 'AI生成中...' : '基于资料生成AI条目' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 新建条目弹窗 -->
    <div v-if="newEntryVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="newEntryVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-sm mx-4 fade-in">
        <div class="flex items-center justify-between px-6 py-4 border-b">
          <h2 class="text-base font-semibold">新建条目</h2>
          <button @click="newEntryVisible=false" class="p-1 rounded text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="px-6 py-4">
          <label class="block text-sm font-medium text-slate-700 mb-1.5">条目标题 *</label>
          <input v-model="newEntryTitle" type="text" placeholder="请输入条目标题"
            class="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"/>
        </div>
        <div class="flex justify-end gap-3 px-6 py-4 border-t">
          <button @click="newEntryVisible=false" class="px-4 py-2 text-sm border border-slate-300 rounded-xl text-slate-600">取消</button>
          <button class="px-4 py-2 text-sm text-white rounded-xl" style="background:linear-gradient(135deg,#1a90ff,#0059b3);" @click="createNewEntry">确定</button>
        </div>
      </div>
    </div>

    <!-- 版本历史弹窗 -->
    <div v-if="versionsVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="versionsVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg mx-4 fade-in">
        <div class="flex items-center justify-between px-6 py-4 border-b">
          <h2 class="text-base font-semibold">草稿记录</h2>
          <button @click="versionsVisible=false" class="p-1 rounded text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="px-6 py-4 max-h-96 overflow-y-auto">
          <div v-if="versions.length === 0" class="text-center text-slate-400 text-sm py-8">暂无版本记录</div>
          <div v-else class="space-y-3">
            <div v-for="(v, i) in versions" :key="v.id" class="flex items-start gap-3 p-3 rounded-xl" :class="i === 0 ? 'bg-blue-50 border border-blue-200' : 'bg-slate-50'">
              <div class="w-8 h-8 rounded-full flex items-center justify-center text-white text-xs font-bold flex-shrink-0" style="background:#1a90ff;">v{{ v.version }}</div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-700">{{ v.editor_name || '系统' }}</p>
                <p class="text-xs text-slate-400 mt-0.5">{{ v.created_at?.substring(0, 16).replace('T', ' ') }}</p>
                <p v-if="v.revision_note" class="text-xs text-slate-500 mt-1">{{ v.revision_note }}</p>
              </div>
              <div class="flex gap-2">
                <span v-if="i === 0" class="text-xs text-blue-600 font-medium">当前版本</span>
                <button v-else class="text-xs text-primary hover:underline" @click="restoreVersion(v)">回滚</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 历史数据弹窗 -->
    <div v-if="historyVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="historyVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg mx-4 fade-in">
        <div class="flex items-center justify-between px-6 py-4 border-b">
          <h2 class="text-base font-semibold">历史数据检索</h2>
          <button @click="historyVisible=false" class="p-1 rounded text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="px-6 py-4">
          <div class="flex gap-2 mb-4">
            <input v-model="historyKeyword" type="text" placeholder="输入关键词检索历史数据..."
              class="flex-1 px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
              @keyup.enter="searchHistory"/>
            <button class="px-4 py-2 text-sm text-white rounded-lg" style="background:#1a90ff;" @click="searchHistory">检索</button>
          </div>
          <div class="max-h-72 overflow-y-auto space-y-3">
            <div v-if="historyData.length === 0" class="text-center text-slate-400 text-sm py-8">暂无历史数据</div>
            <div v-for="h in historyData" :key="h.id" class="border border-slate-200 rounded-xl p-4">
              <div class="flex items-center justify-between mb-2">
                <span class="text-xs font-medium text-blue-600">{{ h.year }}年 · {{ h.yearbook_name }}</span>
                <button class="text-xs text-primary hover:underline" @click="useHistory(h); historyVisible=false">引用</button>
              </div>
              <p class="text-xs text-slate-500 leading-5 line-clamp-4">{{ h.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据检测弹窗 -->
    <div v-if="detectVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="detectVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg mx-4 fade-in">
        <div class="flex items-center justify-between px-6 py-4 border-b">
          <h2 class="text-base font-semibold">数据智能检测</h2>
          <button @click="detectVisible=false" class="p-1 rounded text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="px-6 py-4 max-h-96 overflow-y-auto">
          <div v-if="conflicts.length === 0" class="text-center text-slate-400 text-sm py-8">
            <span class="material-symbols-outlined text-green-500 mb-2" style="font-size:36px;">check_circle</span>
            <p>未发现数据问题</p>
          </div>
          <div v-else class="space-y-3">
            <div v-for="c in conflicts" :key="c.description" class="p-3 rounded-xl border" :class="c.severity === 'high' ? 'bg-red-50 border-red-200' : 'bg-amber-50 border-amber-200'">
              <div class="flex items-center gap-2 mb-1">
                <span class="badge" :class="c.severity === 'high' ? 'badge-red' : c.severity === 'medium' ? 'badge-yellow' : 'badge-blue'">{{ c.type }}</span>
              </div>
              <p class="text-xs text-slate-700">{{ c.description }}</p>
              <p class="text-xs text-slate-400 mt-1">位置：{{ c.location }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 对比审阅弹窗 -->
    <div v-if="diffVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="diffVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-4xl mx-4 fade-in">
        <div class="flex items-center justify-between px-6 py-4 border-b">
          <h2 class="text-base font-semibold">对比审阅</h2>
          <button @click="diffVisible=false" class="p-1 rounded text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="p-6 grid grid-cols-2 gap-4">
          <div>
            <h3 class="text-xs font-semibold text-slate-500 uppercase mb-2">原始内容</h3>
            <div class="border border-slate-200 rounded-xl p-4 text-sm text-slate-600 leading-6 min-h-48 bg-slate-50 whitespace-pre-wrap">{{ activeEntry?.original_content || '暂无原始内容' }}</div>
          </div>
          <div>
            <h3 class="text-xs font-semibold text-blue-500 uppercase mb-2">AI生成内容</h3>
            <div class="border border-blue-200 rounded-xl p-4 text-sm text-slate-700 leading-6 min-h-48 bg-blue-50 whitespace-pre-wrap">{{ currentEditorContent || '暂无AI内容' }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- AI助手弹窗 -->
    <div v-if="botVisible" class="fixed inset-0 z-50 flex items-center justify-center" style="background:rgba(0,0,0,0.4);" @click.self="botVisible=false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 fade-in flex flex-col" style="height:500px;">
        <div class="flex items-center justify-between px-5 py-4 border-b flex-shrink-0">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-purple-600" style="font-size:20px;">smart_toy</span>
            <span class="font-semibold">AI助手</span>
          </div>
          <button @click="botVisible=false" class="p-1 rounded text-slate-400 hover:bg-slate-100">
            <span class="material-symbols-outlined" style="font-size:20px;">close</span>
          </button>
        </div>
        <div class="flex-1 overflow-y-auto p-4 space-y-3">
          <div v-for="(msg, i) in botMessages" :key="i" class="flex" :class="msg.role === 'user' ? 'justify-end' : 'justify-start'">
            <div class="max-w-[80%] px-3 py-2 rounded-xl text-sm leading-6"
              :class="msg.role === 'user' ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-700'">
              {{ msg.content }}
            </div>
          </div>
        </div>
        <div class="px-4 py-3 border-t flex-shrink-0">
          <div class="flex gap-2">
            <input
              v-model="botQuestion"
              type="text"
              placeholder="输入问题，如：年鉴条目格式要求..."
              class="flex-1 px-3 py-2 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-purple-400"
              @keyup.enter="askBot"
            />
            <button
              class="px-4 py-2 text-sm text-white rounded-xl"
              style="background:linear-gradient(135deg,#7c3aed,#a78bfa);"
              :disabled="botLoading"
              @click="askBot"
            >发送</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useYearbookStore } from '@/stores/yearbook'
import { fetchYearbooks } from '@/api/yearbook'
import { fetchOutlineFlat } from '@/api/outline'
import { fetchEntries, createEntry, updateEntry, fetchVersions, createVersion, fetchHistoryData } from '@/api/entry'
import { aiGenerateEntry, aiRewrite, aiExpand, aiDetectConflicts, aiBotAnswer } from '@/api/ai'
import type { Yearbook, OutlineNode, Entry, EntryVersion, HistoryData } from '@/types'

const router = useRouter()
const auth = useAuthStore()
const yearbookStore = useYearbookStore()

const yearbookList = ref<Yearbook[]>([])
const selectedYearbookId = ref(yearbookStore.currentYearbookId || '')
const selectedOutlineId = ref(yearbookStore.currentOutlineId || '')
const flatOutlines = ref<OutlineNode[]>([])
const currentOutlineTitle = ref(yearbookStore.currentOutlineTitle || '')
const outlineProgress = ref(0)

const entries = ref<Entry[]>([])
const activeEntry = ref<Entry | null>(null)
const editorRef = ref<HTMLElement>()
const wordCount = computed(() => editorRef.value?.innerText?.length || 0)
const currentEditorContent = ref('')
const lastSavedTime = ref('')
const editorPlaceholder = '填入原始数据或开始编写内容...'

const versions = ref<EntryVersion[]>([])
const historyData = ref<HistoryData[]>([])
const nodeFiles = ref<any[]>([])
const selectedFiles = ref(new Set<string>())
const conflicts = ref<any[]>([])

const aiLoading = ref(false)
const aiProgress = ref(0)
const aiResult = ref('')
let aiTimer: any

const rightTab = ref('materials')
const rightTabs = [
  { key: 'materials', label: '参考资料' },
  { key: 'history', label: '历史数据' },
  { key: 'tips', label: 'AI提示' },
]

const aiTips = [
  { title: '年鉴文体规范', content: '采用客观、简洁的叙述风格，避免主观评价性语言，多用完成时态。' },
  { title: '数据引用格式', content: '数字统一使用阿拉伯数字，大数字用「万」「亿」为单位，须注明来源。' },
  { title: '时间表述规范', content: '统一使用「年内」「截至年底」「同比增长」等规范表述。' },
]

const historyKeyword = ref('')
const newEntryVisible = ref(false)
const newEntryTitle = ref('')
const versionsVisible = ref(false)
const historyVisible = ref(false)
const detectVisible = ref(false)
const diffVisible = ref(false)
const botVisible = ref(false)
const botMessages = ref([{ role: 'assistant', content: '您好！我是AI助手，可以回答年鉴编纂相关问题。' }])
const botQuestion = ref('')
const botLoading = ref(false)

async function loadYearbooks() {
  const result = await fetchYearbooks({ page: 1, pageSize: 100 })
  yearbookList.value = result.data
}

async function onYearbookChange() {
  selectedOutlineId.value = ''
  currentOutlineTitle.value = ''
  entries.value = []
  activeEntry.value = null
  if (selectedYearbookId.value) {
    yearbookStore.setCurrentYearbook(
      selectedYearbookId.value,
      yearbookList.value.find(y => y.id === selectedYearbookId.value)?.name || ''
    )
    flatOutlines.value = await fetchOutlineFlat(selectedYearbookId.value)
  }
}

function selectOutline(node: OutlineNode) {
  selectedOutlineId.value = node.id
  currentOutlineTitle.value = node.title
  yearbookStore.setCurrentOutline(node.id, node.title)
  onOutlineChange()
}

async function onOutlineChange() {
  if (!selectedOutlineId.value) return
  const node = flatOutlines.value.find(n => n.id === selectedOutlineId.value)
  if (node) {
    currentOutlineTitle.value = node.title
    yearbookStore.setCurrentOutline(node.id, node.title)
  }
  entries.value = []
  activeEntry.value = null
  const result = await fetchEntries({ outlineId: selectedOutlineId.value, page: 1, pageSize: 50 })
  entries.value = result.data
  if (entries.value.length > 0) selectEntry(entries.value[0])
  try { historyData.value = await fetchHistoryData(currentOutlineTitle.value) } catch {}
}

async function selectEntry(entry: Entry) {
  activeEntry.value = { ...entry }
  await nextTick()
  if (editorRef.value) {
    editorRef.value.innerHTML = entry.ai_content || entry.original_content || ''
    currentEditorContent.value = editorRef.value.innerHTML
  }
  try { versions.value = await fetchVersions(entry.id) } catch { versions.value = [] }
}

function onEditorInput() {
  if (editorRef.value) currentEditorContent.value = editorRef.value.innerHTML
}

async function autoSave() {
  if (!activeEntry.value || !editorRef.value) return
  const content = editorRef.value.innerHTML
  try {
    await updateEntry(activeEntry.value.id, { ai_content: content })
    lastSavedTime.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } catch {}
}

function execCmd(cmd: string) {
  document.execCommand(cmd, false)
  editorRef.value?.focus()
}

function showAIGenerating() {
  aiLoading.value = true
  aiProgress.value = 0
  aiTimer = setInterval(() => {
    aiProgress.value = Math.min(aiProgress.value + Math.random() * 15, 95)
  }, 200)
}

async function handleAIGenerate() {
  if (!activeEntry.value) { ElMessage.warning('请先选择一个条目'); return }
  showAIGenerating()
  try {
    const rawData = activeEntry.value.original_content || (editorRef.value?.innerText || '')
    const histContent = historyData.value.map(h => h.content).join('\n')
    const result = await aiGenerateEntry(activeEntry.value.id, rawData, histContent)
    clearInterval(aiTimer)
    aiProgress.value = 100
    aiLoading.value = false
    aiResult.value = result
  } catch (e: any) {
    clearInterval(aiTimer)
    aiLoading.value = false
    ElMessage.error(e.message || 'AI生成失败')
  }
}

async function handleAIRewrite() {
  if (!editorRef.value?.innerText) { ElMessage.warning('请先输入内容'); return }
  aiLoading.value = true
  try {
    const result = await aiRewrite(editorRef.value.innerHTML)
    aiResult.value = result
  } catch { ElMessage.error('润色失败') }
  finally { aiLoading.value = false }
}

async function handleAIExpand() {
  if (!editorRef.value?.innerText) { ElMessage.warning('请先输入内容'); return }
  aiLoading.value = true
  try {
    const result = await aiExpand(editorRef.value.innerHTML)
    aiResult.value = result
  } catch { ElMessage.error('扩写失败') }
  finally { aiLoading.value = false }
}

function cancelAI() {
  clearInterval(aiTimer)
  aiLoading.value = false
  aiProgress.value = 0
}

async function acceptAI() {
  if (!activeEntry.value || !editorRef.value) return
  editorRef.value.innerText = aiResult.value
  currentEditorContent.value = editorRef.value.innerHTML
  await updateEntry(activeEntry.value.id, { ai_content: editorRef.value.innerHTML, status: 'editing' })
  aiResult.value = ''
  ElMessage.success('已采用AI生成内容')
}

async function saveVersion() {
  if (!activeEntry.value || !editorRef.value?.innerHTML) { ElMessage.warning('内容为空，无法保存版本'); return }
  try {
    const nextVer = (versions.value[0]?.version || 0) + 1
    await createVersion({
      entry_id: activeEntry.value.id, version: nextVer,
      content: editorRef.value.innerHTML,
      revision_note: `版本 v${nextVer}`,
      editor_id: auth.user?.id,
    })
    versions.value = await fetchVersions(activeEntry.value.id)
    ElMessage.success(`版本 v${nextVer} 已保存`)
  } catch (e: any) { ElMessage.error(e.message || '保存失败') }
}

async function handleSubmit() {
  if (!activeEntry.value) return
  try {
    await autoSave()
    await updateEntry(activeEntry.value.id, { status: 'submitted' })
    activeEntry.value.status = 'submitted'
    ElMessage.success('已提交')
  } catch (e: any) { ElMessage.error(e.message || '提交失败') }
}

function restoreVersion(ver: EntryVersion) {
  if (!editorRef.value) return
  editorRef.value.innerHTML = ver.content
  currentEditorContent.value = ver.content
  autoSave()
  ElMessage.success(`已回滚到 v${ver.version}`)
}

async function showDetect() {
  if (!editorRef.value?.innerText) { ElMessage.warning('内容为空'); return }
  try {
    conflicts.value = await aiDetectConflicts(editorRef.value.innerHTML)
  } catch { conflicts.value = [] }
  detectVisible.value = true
}

function showDiff() { diffVisible.value = true }

async function searchHistory() {
  if (!historyKeyword.value) return
  try {
    historyData.value = await fetchHistoryData(historyKeyword.value)
    rightTab.value = 'history'
  } catch {}
}

function useHistory(h: HistoryData) {
  if (!editorRef.value) return
  editorRef.value.innerHTML += `\n\n<p><strong>【历史参考 ${h.year}年】</strong></p><p>${h.content}</p>`
  currentEditorContent.value = editorRef.value.innerHTML
  ElMessage.success('已引用历史数据')
}

function toggleFile(fileId: string) {
  if (selectedFiles.value.has(fileId)) selectedFiles.value.delete(fileId)
  else selectedFiles.value.add(fileId)
}

async function createNewEntry() {
  if (!newEntryTitle.value.trim() || !selectedOutlineId.value) return
  try {
    await createEntry({
      outline_id: selectedOutlineId.value, title: newEntryTitle.value,
      sort_order: entries.value.length, status: 'draft', created_by: auth.user?.id,
    })
    newEntryTitle.value = ''
    newEntryVisible.value = false
    const result = await fetchEntries({ outlineId: selectedOutlineId.value, page: 1, pageSize: 50 })
    entries.value = result.data
    ElMessage.success('创建成功')
  } catch (e: any) { ElMessage.error(e.message || '创建失败') }
}

async function askBot() {
  if (!botQuestion.value.trim()) return
  const q = botQuestion.value
  botMessages.value.push({ role: 'user', content: q })
  botQuestion.value = ''
  botLoading.value = true
  try {
    const answer = await aiBotAnswer(q)
    botMessages.value.push({ role: 'assistant', content: answer })
  } catch {
    botMessages.value.push({ role: 'assistant', content: '抱歉，暂时无法回答。' })
  } finally { botLoading.value = false }
}

function statusLabel(s?: string) {
  return { not_started: '未开始', in_progress: '编纂中', submitted: '已提交', editing: '编辑中', draft: '草稿' }[s || ''] || '草稿'
}
function statusBadge(s?: string) {
  return { not_started: 'badge-gray', in_progress: 'badge-yellow', submitted: 'badge-green', editing: 'badge-blue', draft: 'badge-gray' }[s || ''] || 'badge-gray'
}

onMounted(async () => {
  await loadYearbooks()
  if (selectedYearbookId.value) {
    flatOutlines.value = await fetchOutlineFlat(selectedYearbookId.value)
    if (selectedOutlineId.value) await onOutlineChange()
  }
})
</script>

<style scoped>
.rich-editor:empty:before {
  content: attr(data-placeholder);
  color: #9ca3af;
  pointer-events: none;
}
</style>
