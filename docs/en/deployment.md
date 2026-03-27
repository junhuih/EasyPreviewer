# Deployment

## Requirements

- Java 21 for the backend
- Node.js for frontend development or build steps
- LibreOffice installed on the backend host for Office conversion

## Notes

- Production should prefer an external LibreOffice installation
- This project does not bundle LibreOffice binaries by default
- If LibreOffice is missing, Office preview requests fail with a localized message

