# 支持矩阵

| 类型 | 状态 | 说明 |
| --- | --- | --- |
| Office 转 PDF（`doc`、`docx`、`ppt`、`pptx`、`odt`、`odp`、`wps`） | 支持 | 通过 LibreOffice 转换后进行只读预览 |
| 表格预览（`xls`、`xlsx`、`xlsm`、`xlt`、`xltm`、`ods`） | 支持 | 表格预览链路；其中 `.xlsx` 与 `.xlsm` 走浏览器工作簿预览 |
| PDF | 支持 | 只读预览 |
| 图片（`png`、`jpg`、`jpeg`、`gif`、`webp`、`svg`） | 支持 | 浏览器原生只读渲染 |
| 视频（`mp4`、`webm`、`mov`、`m4v`、`ogg`） | 支持 | 浏览器原生内联播放 |
| 文本 / 代码（`txt`、`json`、`xml`、`yaml`、`yml`、`java`、`js`、`ts`、`tsx`、`jsx`、`py`、`css`、`html`） | 支持 | 只读文本 / 代码查看 |
| Markdown（`md`、`markdown`） | 支持 | 安全清洗后的只读 Markdown 渲染 |
| CSV | 支持 | 不依赖 LibreOffice 的原生只读表格预览 |
| CAD（`dwg`、`dxf`、`dwf`、`ifc`） | 不支持 | 为避免 Aspose.CAD 依赖而移除 |
| TIFF 高级转换（`tif`、`tiff`） | 不支持 | 有意排除基于 iText 5 的转换链路 |
| 非浏览器原生媒体（`avi`、`rm`、`wmv`、`mkv`、`flv`、`3gp`） | 不支持 | 有意不提供服务端转码 |
| 上传 / 删除 / 导出 / 编辑 | 不支持 | 产品策略：仅预览 |
