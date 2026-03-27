# Modern Preview-Only File Viewer

A clean-room, preview-only file viewer, built for commercial-friendly open source adoption under Apache-2.0.

## Goals

- Preview files online only
- Support `zh` and `en`
- Use LibreOffice as the main Office conversion engine
- Keep the scope narrow and well documented
- Exclude risky or out-of-scope capabilities such as CAD preview, TIFF-to-PDF via iText 5, media transcoding, uploads, deletes, and exports

## Stack

- Backend: Java 21, Spring Boot 3, JODConverter, Caffeine
- Frontend: React, TypeScript, Vite, i18next
- Preview engines: PDF.js-compatible embedding, read-only markdown/text/image rendering
- Deployment: Docker-ready, with external LibreOffice preferred in production

## Current Status

This repository now contains:

- a new backend scaffold under [backend](/Users/orbit-0427/Documents/EasyPreviewer/backend)
- a new frontend scaffold under [frontend](/Users/orbit-0427/Documents/EasyPreviewer/frontend)
- bilingual docs under [docs/en](/Users/orbit-0427/Documents/EasyPreviewer/docs/en) and [docs/zh](/Users/orbit-0427/Documents/EasyPreviewer/docs/zh)

## Development

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

## Documentation

- English overview: [docs/en/overview.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/en/overview.md)
- 中文概览: [docs/zh/overview.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/zh/overview.md)
- Architecture: [docs/en/architecture.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/en/architecture.md)
- 架构说明: [docs/zh/architecture.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/zh/architecture.md)
- Support matrix: [docs/en/support-matrix.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/en/support-matrix.md)
- 支持矩阵: [docs/zh/support-matrix.md](/Users/orbit-0427/Documents/EasyPreviewer/docs/zh/support-matrix.md)

