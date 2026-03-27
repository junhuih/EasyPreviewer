# Deployment

## Requirements

- Java 21 for the backend
- Node.js for frontend development or build steps
- Docker and Docker Compose for containerized local deployment

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
