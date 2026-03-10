# 地方志智能编修平台 (AI Chronicle System)

## 系统概述

地方志智能编修平台旨在解决地方志编写中的核心问题：资料分散、编写周期长、人工整理成本高、审核流程复杂、数据复用困难。

**核心流程**：资料采集 → 自动整理 → AI辅助编写 → 编辑审核 → 出版发布

## 技术架构

```
数据采集层 → 数据处理层 → 知识库层 → 应用层
```

- **后端**：Java 17 + Spring Boot 3 + PostgreSQL + MinIO + Redis
- **AI 服务**：Python + FastAPI（RAG 架构，可接入大模型）
- **前端**：Vue 3 + Element Plus + TipTap 编辑器

## 功能模块

| 模块 | 说明 |
|------|------|
| 系统管理 | 用户、角色、权限 |
| 资料管理 | 上传 Word/PDF/Excel/图片，OCR、自动分类、摘要、关键词 |
| 志书纲目 | 篇-章-节-条目层级，拖拽调整、模板导入 |
| 条目编写 | 富文本编辑、版本管理、自动保存 |
| AI辅助写作 | 条目生成、资料摘要、数据描述、趋势分析 |
| 大事记系统 | 自动生成、时间排序、分类管理 |
| 统计数据 | Excel 导入、图表、趋势分析 |
| 审核系统 | 多级审核、批注、退回、版本对比 |
| 知识库 | 全文搜索、AI 问答 |
| 出版系统 | 导出 Word/PDF/HTML/Markdown |
| 数据统计 | 编写进度、部门供稿、人员工作量 |
| 权限系统 | 管理员/编辑/供稿人/专家/主编 |

## 快速启动

### 前置要求

- Docker & Docker Compose
- 或：JDK 17、Node 18+、Python 3.11、PostgreSQL、MinIO、Redis

### 使用 Docker Compose 启动

```bash
# 启动所有服务
docker-compose up -d

# 访问
# 前端: http://localhost:3000
# 后端 API: http://localhost:8080
# AI 服务: http://localhost:8000
# 默认账号: admin / admin123
```

### 本地开发

**1. 启动基础设施**

```bash
docker-compose up -d postgres minio redis
```

**2. 后端**

```bash
cd backend
mvn spring-boot:run
```

**3. AI 服务**

```bash
cd ai-service
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

**4. 前端**

```bash
cd frontend
npm install
npm run dev
```

## 数据库

PostgreSQL 数据库表：

- `users` - 用户
- `materials` - 资料
- `catalogs` - 志书纲目
- `entries` - 条目
- `entry_versions` - 条目版本
- `reviews` - 审核记录
- `events` - 大事记
- `statistic_data` - 统计数据

## AI 能力

- 资料摘要（500/1000/2000 字）
- 条目自动生成
- 数据自动描述
- 历史趋势分析
- 大事记自动生成
- 知识库 RAG 问答

## 适用机构

- 地方志办公室
- 政府研究室
- 统计局
- 档案馆
- 高校研究机构

## 许可证

MIT
