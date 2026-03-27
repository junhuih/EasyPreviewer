# Architecture

## Backend

- Spring Boot 3 REST API
- In-memory session/cache registry using Caffeine
- Capability registry for supported and unsupported file types
- Remote source fetch and Office conversion pipeline
- Read-only content streaming endpoint

## Frontend

- React + TypeScript + Vite
- i18next for `zh` / `en`
- One preview shell that renders:
  - PDF in an iframe
  - images directly
  - markdown safely
  - text and code read-only
- Clear unsupported and failure states

## Conversion Model

- Office files are downloaded to a temporary location and converted to PDF through LibreOffice and JODConverter
- Direct preview formats are streamed through the backend without mutation
- All outputs are ephemeral cache artifacts, never user-managed exports

