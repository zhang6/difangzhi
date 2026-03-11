# 智能年鉴编纂系统

结合大模型与NLP技术，实现年鉴编纂工作线上化管理。适用于编写地方志年鉴（如《江苏年鉴2024》《南京年鉴2025》）。

## 技术栈

- **前端**: Vue 3 + TypeScript + Element Plus
- **数据库**: Supabase (PostgreSQL)
- **文件存储**: Supabase Storage
- **认证**: Supabase Auth (JWT)

## 模块导航

| 模块 | 说明 |
|------|------|
| 年鉴管理 | 年鉴卡片列表，新建/编辑/删除，分配负责人，展示进度 |
| 大纲管理 | 多级目录树，导入大纲，任务指派，资料收集关联 |
| 资料库 | 供稿单位文件夹管理，Word/PDF文件上传/预览/下载/删除 |
| 智能编纂 | 资料选择 + 原始数据填充 + AI生成条目 + 版本管理 + 数据检测 |
| 统稿 | 大纲条目统一管理，内容编辑，批注协作，版本历史，导出Word |
| 个人中心 | 个人信息编辑，我的任务列表 |

## 权限角色

- **管理员(admin)**: 年鉴新增/编辑、分配负责人、大纲全操作
- **负责人(manager)**: 仅查看被分配的年鉴
- **编辑(editor)**: 仅对指定大纲进行智能编纂

## 快速开始

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:3000

### 演示账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 编辑 | editor1 | 123456 |

## 数据库表

| 表名 | 说明 |
|------|------|
| yb_users | 用户信息 |
| yb_yearbooks | 年鉴 |
| yb_yearbook_managers | 年鉴负责人关联 |
| yb_outlines | 大纲节点（多级树形） |
| yb_material_folders | 供稿单位文件夹 |
| yb_material_files | 资料文件 |
| yb_entries | 条目 |
| yb_entry_versions | 条目版本 |
| yb_annotations | 批注 |
| yb_drafts | 草稿 |
| yb_history_data | 历史数据 |

## AI 功能（Mock）

当前阶段所有 AI 功能均为 Mock 实现：
- 条目AI生成、AI润色/扩写
- 历史数据检索
- 数据智能检测
- 机器人问答

## 构建部署

```bash
cd frontend
npm run build
```

产出目录为 `frontend/dist`，可部署至任意静态服务或 Vercel / Netlify。
