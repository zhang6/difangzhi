# 地方志智能编修平台 - 部署指南

## 方式一：Vercel 部署（推荐，免费）

1. 访问 https://vercel.com 并登录（支持 GitHub）
2. 点击 **Import Project**，选择 `zhang6/difangzhi` 仓库
3. **Root Directory** 设置为 `frontend`
4. 添加环境变量：
   - `VITE_SUPABASE_URL` = `https://tqdosxaesqbwbhngvndj.supabase.co`
   - `VITE_SUPABASE_ANON_KEY` = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRxZG9zeGFlc3Fid2Jobmd2bmRqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyODAxMzIsImV4cCI6MjA3ODg1NjEzMn0.YArQlOsSoHZZRO0Gwm4zyzvDGIRl1GimlosqSPUshz8`
5. 点击 **Deploy**，等待完成
6. 获得 URL：`https://xxx.vercel.app`

**默认账号**：admin / admin123

---

## 方式二：Tiiny.host 快速部署（无需注册）

1. 打开 https://tiiny.host
2. 上传 `chronicle-deploy.zip`（位于项目根目录）
3. 输入子域名（如 `chronicle-demo`）
4. 点击发布，获得 `https://chronicle-demo.tiiny.site`

**注意**：免费版限制 3MB，若超出请使用 Vercel。

---

## 方式三：Netlify 部署

1. 访问 https://app.netlify.com 并登录
2. 拖拽 `frontend/dist` 文件夹到页面，或连接 GitHub 仓库
3. Build 命令：`cd frontend && npm run build`
4. Publish 目录：`frontend/dist`
5. 添加环境变量（同 Vercel）

---

## 方式四：本地运行

```bash
cd frontend
npm install
npm run build
npx serve -s dist -l 3000
```

访问 http://localhost:3000

---

## 数据库说明

系统已使用 Supabase 云数据库，表结构已创建，默认管理员已初始化。
无需额外配置数据库即可使用。
