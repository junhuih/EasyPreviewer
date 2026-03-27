# Third-Party Notices

This repository is distributed under Apache-2.0, but some bundled runtime assets are third-party works that remain under their own licenses.

This file is an inventory of the vendored spreadsheet preview stack currently shipped in the repository. It is intended to make the repository's compliance posture explicit. It is not a substitute for reviewing each upstream project's full license text when redistributing or modifying those assets.

## Bundled Spreadsheet Viewer Assets

The following directories contain vendored runtime assets used by the spreadsheet preview feature:

- `frontend/public/xlsx`
- `backend/src/main/resources/static/xlsx`

These two directories substantially mirror the same browser-side asset bundle.

## Identified Notices Present In Vendored Files

### Luckysheet

- File: `frontend/public/xlsx/luckysheet.umd.js`
- Notice header references: Luckysheet v2.1.13, https://github.com/mengshukeji/Luckysheet

### LuckyExcel

- File: `frontend/public/xlsx/luckyexcel.umd.js`
- Bundled as part of the spreadsheet import and preview pipeline

### Vue.js

- File: `frontend/public/xlsx/expendPlugins/chart/Vue.js`
- Notice header: Vue.js v2.6.11
- License noted in file header: MIT

### Font Awesome

- File: `frontend/public/xlsx/plugins/plugins.css`
- Notice header: Font Awesome 4.7.0
- License noted in file header: Font assets under SIL OFL 1.1, CSS under MIT

### jQuery UI Related Assets

- File: `frontend/public/xlsx/plugins/plugins.css`
- Notice text in bundled CSS references jQuery Foundation and MIT licensing

### Additional Bundled Fonts, Icons, Images, And Plugins

- Directories include `assets/iconfont`, `assets/iconfont2`, `fonts`, `plugins`, `css`, `expendPlugins`, and related images
- Several of these files include inline license headers, while others do not expose licensing metadata directly in the file body
- Before external redistribution, the upstream source and license for each non-generated asset should be verified and recorded if not already covered by a header in the file

## Compliance Notes

- Inclusion in this repository does not relicense third-party assets to Apache-2.0
- Upstream copyright and license notices embedded in vendored files should be preserved
- New vendored assets should not be added without updating this file
- If the spreadsheet stack is replaced in the future, this notice file should be updated accordingly
