# Modern Preview-Only File Viewer

A preview-only file viewer, released under Apache-2.0 with additional third-party components kept under their own licenses.

## Goals

- Preview files online only
- Support `zh` and `en`
- Use LibreOffice as the main Office conversion engine
- Keep the scope narrow and well documented
- Exclude risky or out-of-scope capabilities such as CAD preview, TIFF-to-PDF via iText 5, media transcoding, uploads, deletes, and exports

## Stack

- Backend: Java 21, Spring Boot 3, JODConverter, Caffeine
- Frontend: React, TypeScript, Vite, i18next
- Preview engines: LibreOffice conversion, PDF embedding, markdown/text/image rendering, and a vendored spreadsheet viewer stack for `.xlsx` preview
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

### Demo Assets

The clickable demo page uses generated samples under [frontend/public/demo/files](/Users/orbit-0427/Documents/EasyPreviewer/frontend/public/demo/files). To regenerate them:

```bash
python3 scripts/generate_demo_assets.py
```

The generated set includes:

- a complex `docx`
- a complex multi-sheet `xlsx`
- a complex multi-slide `pptx`
- `pdf`, `md`, `txt`, `ts`, `json`, `csv`, `png`, `jpg`, and `svg` samples

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

### Single-Container Docker Build

The repository includes a root [Dockerfile](/Users/orbit-0427/Documents/EasyPreviewer/Dockerfile) that:

- builds the React frontend
- copies the frontend build into the backend static resources during image build
- packages the Spring Boot backend into a single runnable image
- installs LibreOffice in the runtime image so Office preview works out of the box

Build:

```bash
docker build -t modern-preview-demo .
```

Run:

```bash
docker run --rm -p 8080:8080 modern-preview-demo
```

Notes:

- The container serves the frontend and backend together on port `8080`.
- Spreadsheet files are converted to HTML through LibreOffice instead of being flattened into PDF.
- `docker compose up --build -d` starts one named container, `modern-preview-only`, which is easy to find in Docker Desktop.

## Documentation

- English overview: [docs/en/overview.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/en/overview.md)
- 中文概览: [docs/zh/overview.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/zh/overview.md)
- Architecture: [docs/en/architecture.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/en/architecture.md)
- 架构说明: [docs/zh/architecture.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/zh/architecture.md)
- Support matrix: [docs/en/support-matrix.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/en/support-matrix.md)
- 支持矩阵: [docs/zh/support-matrix.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/zh/support-matrix.md)
