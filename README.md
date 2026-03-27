# EasyPreviewer

A preview-only file viewer, released under Apache-2.0 with additional third-party components kept under their own licenses.

## Goals

- Preview files online only
- Support `zh` and `en`
- Use LibreOffice only where conversion is still needed
- Keep the scope narrow and well documented
- Exclude risky or out-of-scope capabilities such as CAD preview, TIFF-to-PDF via iText 5, media transcoding, uploads, deletes, and exports

## Stack

- Backend: Java 21, Spring Boot 3, JODConverter, Caffeine
- Frontend: React, TypeScript, Vite, i18next
- Preview engines: LibreOffice conversion, in-app PDF rendering, markdown/text/image/video rendering, native CSV table preview, and a vendored spreadsheet viewer stack for `.xlsx` and `.xlsm` preview
- Deployment: single-container Docker image with LibreOffice bundled for easy distribution

## Current Status

This repository now contains:

- a new backend scaffold under [backend](/Users/orbit-0427/Documents/EasyPreviewer/backend)
- a new frontend scaffold under [frontend](/Users/orbit-0427/Documents/EasyPreviewer/frontend)
- bilingual docs under [docs/en](/Users/orbit-0427/Documents/EasyPreviewer/docs/en) and [docs/zh](/Users/orbit-0427/Documents/EasyPreviewer/docs/zh)
- a vendored spreadsheet preview bundle under [frontend/public/xlsx](/Users/orbit-0427/Documents/EasyPreviewer/frontend/public/xlsx) and [backend/src/main/resources/static/xlsx](/Users/orbit-0427/Documents/EasyPreviewer/backend/src/main/resources/static/xlsx)

## Licensing Notes

- The outbound license for this repository is Apache-2.0.
- Some shipped runtime assets are third-party works distributed under their own licenses.
- See [THIRD_PARTY_NOTICES.md](/Users/orbit-0427/Documents/EasyPreviewer/THIRD_PARTY_NOTICES.md) for bundled spreadsheet viewer notices and attribution notes.

## Development

### Supported File Types

- PDF: `pdf`
- Office to PDF: `doc`, `docx`, `ppt`, `pptx`, `odt`, `odp`, `wps`
- Spreadsheet viewer: `xls`, `xlsx`, `xlsm`, `xlt`, `xltm`, `ods`
- CSV table preview: `csv`
- Images: `png`, `jpg`, `jpeg`, `gif`, `webp`, `svg`
- Video: `mp4`, `webm`, `mov`, `m4v`, `ogg`
- Markdown: `md`, `markdown`
- Text and code: `txt`, `json`, `xml`, `yaml`, `yml`, `java`, `js`, `ts`, `tsx`, `jsx`, `py`, `css`, `html`

### How To Use It Locally

Start the backend:

```bash
cd /Users/orbit-0427/Documents/EasyPreviewer/backend
mvn spring-boot:run
```

Start the frontend in another terminal:

```bash
cd /Users/orbit-0427/Documents/EasyPreviewer/frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173).

### Embedded Iframe Usage

EasyPreviewer can also run as a simple embedded preview page with a fixed preview frame.

Open:

```text
http://localhost:5173/?fileUrl=<URL-ENCODED-FILE-URL>
```

Example:

```text
http://localhost:5173/?fileUrl=http%3A%2F%2Flocalhost%3A5173%2Fdemo%2Ffiles%2Fsample-complex.xlsx
```

Example iframe:

```html
<iframe
  src="http://localhost:5173/?fileUrl=https%3A%2F%2Fexample.com%2Ffiles%2Freport.xlsx"
  width="1280"
  height="820"
  style="border:0;border-radius:16px;overflow:hidden"
></iframe>
```

Notes:

- The Vite dev server serves the UI on `5173`.
- Vite proxies `/api` to the backend on `8080`.
- If `8080` is already occupied by Docker, stop the container first or change the backend port.
- The left-hand demo list includes office files, spreadsheet samples, images, text/code samples, and the demo MP4.
- In embedded mode, the browser only loads the EasyPreviewer page. The backend fetches the `fileUrl` server-side, so browser-side CORS is not required for normal preview loading.
- The file URL must still be reachable from the backend process or container.

### Demo Assets

The clickable demo page uses generated samples under [frontend/public/demo/files](/Users/orbit-0427/Documents/EasyPreviewer/frontend/public/demo/files). To regenerate them:

```bash
python3 scripts/generate_demo_assets.py
```

The generated set includes:

- a complex `docx`
- a multi-sheet `xlsx`
- a complex multi-slide `pptx`
- `pdf`, `md`, `txt`, `ts`, `json`, `csv`, `png`, `jpg`, and `svg` samples
- optional locally copied reference assets such as the current demo `xlsx` and `mp4`

### Backend

Requirements:

- Java 21
- LibreOffice installed on the host or reachable through `office.home`

Commands:

```bash
cd backend
./mvnw spring-boot:run
```

If Maven Wrapper is not present yet in your environment, use local Maven:

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The Vite development server proxies `/api` to the backend on port `8080`.

### Docker

The repository includes a root [Dockerfile](/Users/orbit-0427/Documents/EasyPreviewer/Dockerfile) that:

- builds the React frontend
- copies the frontend build into the backend static resources during image build
- packages the Spring Boot backend into a single runnable image
- installs LibreOffice in the runtime image so Office document conversion works out of the box

Local refresh flow:

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

Open:

```bash
http://localhost:8080
```

Manual image build:

```bash
docker build -t modern-preview-demo .
```

Manual container run:

```bash
docker run --rm -p 8080:8080 modern-preview-demo
```

Notes:

- The container serves the frontend and backend together on port `8080`.
- The current local compose service is named `modern-preview-only`.
- `docker compose up --build -d` rebuilds the image and replaces the running container with the latest packaged app.
- If the UI does not look updated, rebuild the frontend, resync `frontend/dist` into `backend/src/main/resources/static`, repackage the backend JAR, and rerun `docker compose up --build -d`.
- Docker is not required for local development. The current recommended local link is [http://localhost:5173](http://localhost:5173).

## Documentation

- English overview: [docs/en/overview.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/en/overview.md)
- 中文概览: [docs/zh/overview.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/zh/overview.md)
- Architecture: [docs/en/architecture.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/en/architecture.md)
- 架构说明: [docs/zh/architecture.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/zh/architecture.md)
- Support matrix: [docs/en/support-matrix.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/en/support-matrix.md)
- 支持矩阵: [docs/zh/support-matrix.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/zh/support-matrix.md)
