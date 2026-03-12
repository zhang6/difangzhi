# 阶段一：构建 Vue 应用
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --quiet
COPY . .
ARG VITE_API_BASE_URL=""
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# 阶段二：nginx 服务
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
