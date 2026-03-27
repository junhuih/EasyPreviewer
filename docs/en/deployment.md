# Deployment

## Requirements

- Java 21 for the backend
- Node.js for frontend development or build steps
- Docker and Docker Compose for containerized local deployment

## Local Development

Run the backend:

```bash
cd /Users/orbit-0427/Documents/EasyPreviewer/backend
mvn spring-boot:run
```

Run the frontend:

```bash
cd /Users/orbit-0427/Documents/EasyPreviewer/frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173).

Notes:

- The frontend is served by Vite on port `5173`.
- `/api` is proxied to the backend on port `8080`.
- If Docker is already using port `8080`, stop the `modern-preview-only` container before launching the backend locally.

## Embedded Iframe Mode

The frontend supports a dedicated embedded preview mode driven by a single query parameter:

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

- The preview page renders inside a fixed-size frame layout intended for embedding.
- The browser does not fetch the remote file directly. EasyPreviewer sends the URL to the backend, and the backend retrieves the file server-side.
- That means normal browser-side CORS is not needed for preview loading.
- The target file URL must still be reachable from the backend host or container.

## Docker

The local Docker image bundles LibreOffice and serves the frontend and backend from one container on port `8080`.

### Refresh The Running Container With Local Changes

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

Open [http://localhost:8080](http://localhost:8080).

### Running Container Name

- `modern-preview-only`

## Notes

- This repository's Docker flow does bundle LibreOffice inside the container
- The Docker image is built from `backend/target/preview-backend-0.1.0-SNAPSHOT.jar`
- If the backend JAR is not rebuilt after frontend changes, Docker will serve stale frontend assets
- LibreOffice is still needed for `doc`, `docx`, `ppt`, `pptx`, `odt`, `odp`, and `wps`
- `xlsx` and `xlsm` use the browser spreadsheet viewer path, not LibreOffice
- `csv` and browser-native video preview do not require LibreOffice
