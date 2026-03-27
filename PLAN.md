# Modern Preview-Only File Viewer Plan

## Core Decisions

- Keep the project license commercial-friendly with Apache-2.0.
- Use Java 21, Spring Boot 3, JODConverter, and LibreOffice for office conversion.
- Use React, TypeScript, and Vite for the frontend.
- Support only `zh` and `en`.
- Keep the product preview-only:
  - no editing
  - no save-back
  - no export
  - no upload UI
  - no delete UI/API
  - no directory browsing UI/API

## Supported In V1

- Office documents through LibreOffice conversion
- PDF preview
- Plain text and code preview
- Markdown preview
- Common image formats
- CSV preview through a read-only text/table experience

## Explicitly Unsupported In V1

- CAD preview
- TIFF to PDF conversion based on iText 5
- Server-side media transcoding
- Mutation or file-management features

## Repository Layout

- `backend/`: Spring Boot API and conversion pipeline
- `frontend/`: React preview shell
- `docs/en/`: English documentation
- `docs/zh/`: Chinese documentation

## Delivery Rules

- Every user-facing feature must ship in both Chinese and English.
- Unsupported formats must return localized UI and API messages.
- No new feature merges without bilingual docs.

