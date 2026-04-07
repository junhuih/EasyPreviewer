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

## Remote File Server Configuration

At startup, the backend reads `preview.remote.*` settings to control how remote files are fetched. The configuration entry point is [backend/src/main/resources/application.yml](/Users/orbit-0427/Documents/EasyPreviewer/backend/src/main/resources/application.yml).

You can still pass a full remote URL:

```text
http://localhost:5173/?fileUrl=https%3A%2F%2Ffiles.example.com%2Freports%2Fdemo.docx
```

Or configure a shared remote file server base URL and pass only a relative path:

```bash
export PREVIEW_REMOTE_BASE_URL=https://files.example.com/storage/
export PREVIEW_REMOTE_ALLOWED_HOSTS=files.example.com
export PREVIEW_REMOTE_CONNECT_TIMEOUT_MS=5000
export PREVIEW_REMOTE_READ_TIMEOUT_MS=60000
export PREVIEW_REMOTE_MAX_FILE_SIZE_BYTES=104857600
export PREVIEW_REMOTE_AUTHORIZATION="Bearer your-token"
```

Then open:

```text
http://localhost:5173/?fileUrl=reports%2Fdemo.docx
```

Key settings:

- `preview.remote.base-url`: optional base URL used to resolve relative `fileUrl` values
- `preview.remote.allowed-hosts`: optional allowlist of reachable hosts; leave empty to allow any reachable host
- `preview.remote.default-headers`: default request headers sent by the backend when fetching remote files
- `preview.remote.connect-timeout-ms`: connection timeout
- `preview.remote.read-timeout-ms`: read timeout
- `preview.remote.max-file-size-bytes`: maximum accepted remote file size
- `preview.remote.allow-redirects`: whether the backend follows HTTP redirects

## Legacy Entry Point

Older deployments can still call the legacy entry point:

```text
/onlinePreview?url=<BASE64-ENCODED-URL>
```

The backend decodes the Base64 value in `url`, then redirects to the modern `?fileUrl=` flow under the current context path.

This endpoint is only for compatibility with older integrations. New callers should use `?fileUrl=` directly.

Example:

```text
/onlinePreview?url=aHR0cHM6Ly9leGFtcGxlLmNvbS9maWxlcy9yZXBvcnQucGRm
```

That request becomes a redirect to:

```text
/?fileUrl=https%3A%2F%2Fexample.com%2Ffiles%2Freport.pdf
```

### Internal Fetch Rewrite

If the backend can reach an internal file server directly, but the browser must still use a public URL that goes through Cloudflare or Entra, you can rewrite the backend fetch target while keeping the browser-facing URL unchanged.

Example:

```bash
export PREVIEW_REMOTE_REWRITE_HOST=127.0.0.1
export PREVIEW_REMOTE_REWRITE_SCHEME=http
export PREVIEW_REMOTE_REWRITE_PORT=12580
```

With that configuration, a source URL such as `https://files.example.com/reports/demo.docx` is fetched from the rewritten internal target `http://127.0.0.1:12580/reports/demo.docx`.

Notes:

- The rewrite only affects the backend fetch path.
- `preview.remote.allowed-hosts` is still checked against the original `fileUrl` host before the rewrite happens.
- If you use rewrite mode, either leave `allowed-hosts` empty or include the original public host in the allowlist.

## Subpath Deployment

The app can also be mounted behind a path prefix, such as `/fileViewer/`.

Set the backend context path and keep the frontend build using relative asset URLs:

```bash
export SERVER_SERVLET_CONTEXT_PATH=/fileViewer
```

Then open the app under that prefix, for example:

```text
https://example.com/fileViewer/
```

Notes:

- The frontend build is configured to use relative paths, so static assets keep working under a prefix.
- The backend also rewrites preview responses so spreadsheet and HTML assets stay inside the same prefix.

Startup behavior:

- Spring Boot binds these values when the backend starts.
- Startup logs print the loaded remote file configuration without exposing header secrets.
- Restart the backend after changing environment variables or `application.yml`.

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
