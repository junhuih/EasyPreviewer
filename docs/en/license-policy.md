# Dependency and License Policy

## Outbound Project License

- Apache-2.0
- Bundled third-party assets keep their original licenses and notices

## Direct Dependency Rules

- Prefer Apache-2.0, MIT, BSD, MPL-2.0, or similarly reviewable licenses
- Avoid dependencies that force closed-source adopters into difficult compliance by default
- Avoid bundled proprietary binaries in the main distribution
- Document vendored runtime assets in a repository-level notice file
- Preserve upstream copyright and license headers for bundled files

## Bundled Runtime Assets

- The spreadsheet preview feature currently ships vendored static assets under `frontend/public/xlsx` and `backend/src/main/resources/static/xlsx`
- Those assets are not relicensed to Apache-2.0 merely by being included in this repository
- See `/THIRD_PARTY_NOTICES.md` for the current attribution inventory

## Explicitly Excluded

- Aspose.CAD
- iText 5 based TIFF to PDF chain
- FFmpeg / JavaCV transcoding stack for v1
