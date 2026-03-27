# 部署说明

## 环境要求

- 后端需要 Java 21
- 前端开发或构建需要 Node.js
- 如果使用容器化本地部署，需要 Docker 与 Docker Compose

## Docker

本地 Docker 镜像已经内置 LibreOffice，并通过单个容器在 `8080` 端口同时提供前端与后端服务。

### 将本地修改刷新到正在运行的容器

```bash
cd /Users/orbit-0427/Documents/EasyPreviewer/frontend
npm install
npm run build

cd /Users/orbit-0427/Documents/EasyPreviewer
rsync -a --delete frontend/dist/ backend/src/main/resources/static/

cd /Users/orbit-0427/Documents/EasyPreviewer/backend
mvn clean package

cd /Users/orbit-0427/Documents/EasyPreviewer
docker compose up --build -d
```

浏览器访问 [http://localhost:8080](http://localhost:8080)。

### 当前容器名

- `modern-preview-only`

## 说明

- 当前仓库的 Docker 方案会在容器内捆绑 LibreOffice
- Docker 镜像基于 `backend/target/preview-backend-0.1.0-SNAPSHOT.jar` 构建
- 如果前端改动后没有重新打包后端 JAR，容器里看到的仍然会是旧的前端资源
