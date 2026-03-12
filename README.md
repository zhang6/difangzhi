# 智能年鉴编纂系统

> 基于 Vue 3 + Spring Boot + Supabase 的智能年鉴编纂管理平台

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Tailwind CSS |
| 后端 | Spring Boot 3.2 + Java 17 + Spring Security + JPA |
| 数据库 | Supabase (PostgreSQL 17) |
| 认证 | JWT |
| 部署 | Docker Compose / Vercel + Railway |

## 演示账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | 管理员 |
| editor1 | 123456 | 编辑 |
| editor2 | 123456 | 负责人 |

---

## 快速部署（Docker Compose）

### 前置条件
- Docker 20+
- Docker Compose v2+
- Supabase 项目（数据库密码）

### 步骤

**1. 克隆项目**
```bash
git clone <repo_url>
cd <project>
```

**2. 配置环境变量**
```bash
cp .env.example .env
# 编辑 .env，填写 SPRING_DATASOURCE_PASSWORD
```

> 数据库密码获取位置：Supabase Dashboard → Project Settings → Database → Database password

**3. 一键启动**
```bash
docker-compose up -d --build
```

**4. 访问**
- 前端页面：http://localhost
- 后端 Swagger：http://localhost:8080/swagger-ui.html

**5. 查看日志**
```bash
docker-compose logs -f backend
docker-compose logs -f frontend
```

**6. 停止服务**
```bash
docker-compose down
```

---

## 分平台部署

### 前端 → Vercel

1. Fork/Push 代码到 GitHub
2. 在 [Vercel](https://vercel.com) 导入仓库
3. 配置构建设置：
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
4. 添加环境变量：
   - `VITE_API_BASE_URL` = 后端服务地址（如 `https://your-backend.railway.app`）
5. 部署完成

### 后端 → Railway

1. 在 [Railway](https://railway.app) 创建新项目
2. 选择 "Deploy from GitHub repo"，选择仓库
3. 设置 **Root Directory** 为 `backend`
4. 添加环境变量：

```
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:6543/postgres?prepareThreshold=0
SPRING_DATASOURCE_USERNAME=postgres.tqdosxaesqbwbhngvndj
SPRING_DATASOURCE_PASSWORD=<your_supabase_password>
JWT_SECRET=<your_jwt_secret>
```

5. Railway 会自动检测 Spring Boot 项目并构建

---

## 本地开发

### 前置条件
- Java 17+
- Node.js 20+
- Maven 3.9+（或使用 `./mvnw`）

### 启动后端

```bash
cd backend
export SPRING_DATASOURCE_PASSWORD=your_password
mvn spring-boot:run
# 或：
# SPRING_DATASOURCE_PASSWORD=xxx mvn spring-boot:run
```

后端启动后自动创建演示账号。

### 启动前端

```bash
cd frontend
npm install
npm run dev
# 前端运行在 http://localhost:3000
```

---

## 数据库

项目使用 **Supabase** 云数据库（PostgreSQL 17）：
- Project ID: `tqdosxaesqbwbhngvndj`
- Region: ap-south-1
- 表已通过 `supabase-init.sql` 初始化完毕

如需重置数据库，执行：
```bash
# 在 Supabase Dashboard > SQL Editor 中运行 supabase-init.sql
```

---

## 项目结构

```
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/chronicle/
│   │   ├── config/          # 安全配置、异常处理、数据初始化
│   │   ├── controller/      # REST 控制器
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # JPA 实体
│   │   ├── repository/      # 数据访问层
│   │   ├── security/        # JWT 认证
│   │   └── service/         # 业务逻辑层
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── api/             # HTTP 请求封装
│   │   ├── components/      # 公共组件
│   │   ├── layouts/         # 页面布局
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia 状态管理
│   │   ├── styles/          # 全局样式
│   │   ├── types/           # TypeScript 类型
│   │   └── views/           # 页面视图
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
├── docker-compose.yml       # Docker 一键部署
├── .env.example             # 环境变量模板
└── supabase-init.sql        # 数据库初始化脚本
```

---

## 功能模块

- **年鉴管理**：年鉴 CRUD、进度统计、负责人分配
- **大纲管理**：多级目录树、任务指派、资料上传
- **资料库**：供稿单位文件夹管理、文件上传/下载
- **智能编纂**：AI 生成条目、润色/扩写、版本管理、历史数据检索
- **统稿**：富文本编辑、批注协作、版本历史、导出 Word
- **个人中心**：个人信息管理、我的任务列表
