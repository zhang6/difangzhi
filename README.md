# 智能年鉴编纂系统

基于 Vue 3 + Tailwind CSS + Supabase 构建的智能年鉴编纂系统，用于编写地方志年鉴。

## 技术栈

- **前端**: Vue 3 + Vite 7 + Vue Router 4 + Pinia
- **样式**: Tailwind CSS v4 + Material Symbols Outlined
- **富文本**: TipTap
- **数据库**: Supabase (PostgreSQL)
- **认证**: bcryptjs + localStorage

## 功能模块

| 模块 | 功能 |
|------|------|
| **年鉴管理** | 年鉴的新建、编辑、删除、卡片展示、进度跟踪 |
| **大纲管理** | 多级大纲目录、任务指派、资料收集、导入大纲 |
| **资料库** | 供稿单位管理、文件上传/下载、按标签搜索 |
| **智能编纂** | AI 条目生成、资料预览、往年数据参考 |
| **统稿** | 富文本编辑、批注协作、版本历史、导出 Word |
| **个人中心** | 个人信息、任务管理 |

## 快速开始

```bash
cd frontend
npm install
npm run dev
```

## 环境变量

```env
VITE_SUPABASE_URL=your_supabase_url
VITE_SUPABASE_ANON_KEY=your_supabase_anon_key
```

## 数据库表

| 表名 | 说明 |
|------|------|
| yb_users | 用户表 |
| yb_yearbooks | 年鉴表 |
| yb_yearbook_managers | 年鉴负责人关联 |
| yb_outlines | 大纲目录（多级） |
| yb_material_folders | 资料库文件夹 |
| yb_material_files | 资料文件 |
| yb_entries | AI 生成条目 |
| yb_entry_versions | 条目版本历史 |
| yb_annotations | 批注 |
| yb_drafts | 草稿记录 |
| yb_history_data | 往年历史数据 |

## 默认登录

- 用户名: `admin`
- 密码: `admin123`

## 设计原型

原型截图和 HTML 代码位于 `stitch-designs/` 目录。
