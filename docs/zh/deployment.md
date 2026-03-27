# 部署说明

## 环境要求

- 后端需要 Java 21
- 前端开发或构建需要 Node.js
- 如果使用容器化本地部署，需要 Docker 与 Docker Compose

## 本地开发运行

启动后端：

```bash
cd /Users/orbit-0427/Documents/EasyPreviewer/backend
mvn spring-boot:run
```

在另一个终端启动前端：

```bash
cd /Users/orbit-0427/Documents/EasyPreviewer/frontend
npm install
npm run dev
```

打开 [http://localhost:5173](http://localhost:5173)。

说明：

- 前端由 Vite 在 `5173` 端口提供服务
- `/api` 会代理到 `8080` 端口的后端
- 如果 Docker 已占用 `8080`，请先停止 `modern-preview-only` 容器，再本地启动后端

## Iframe 嵌入模式

前端支持一个专门的嵌入预览模式，只需要传入一个查询参数：

```text
http://localhost:5173/?fileUrl=<URL 编码后的文件地址>
```

示例：

```text
http://localhost:5173/?fileUrl=http%3A%2F%2Flocalhost%3A5173%2Fdemo%2Ffiles%2Fsample-complex.xlsx
```

iframe 示例：

```html
<iframe
  src="http://localhost:5173/?fileUrl=https%3A%2F%2Fexample.com%2Ffiles%2Freport.xlsx"
  width="1280"
  height="820"
  style="border:0;border-radius:16px;overflow:hidden"
></iframe>
```

说明：

- 这个预览页面会以固定尺寸的预览框布局渲染，适合直接嵌入。
- 浏览器不会直接去请求远程文件，而是由 EasyPreviewer 把 URL 发给后端，再由后端服务端拉取文件。
- 因此正常预览场景下不需要处理浏览器侧的 CORS。
- 目标文件地址仍然必须能被后端进程或容器访问到。

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
- `doc`、`docx`、`ppt`、`pptx`、`odt`、`odp`、`wps` 仍然依赖 LibreOffice 转换
- `xlsx` 与 `xlsm` 走浏览器表格预览链路，不依赖 LibreOffice
- `csv` 与浏览器原生视频预览不依赖 LibreOffice
