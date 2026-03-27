# 依赖与许可证策略

## 项目对外许可证

- Apache-2.0
- 仓库内捆绑的第三方资源继续沿用其各自原始许可证

## 直接依赖规则

- 优先选择 Apache-2.0、MIT、BSD、MPL-2.0 等可审查的许可证
- 避免让闭源商业使用者默认陷入复杂合规负担的依赖
- 主发布物中避免捆绑专有二进制
- 对运行时直接捆绑的第三方资源，必须在仓库级声明中列出
- 保留上游版权与许可证头信息

## 运行时捆绑资源说明

- 当前表格预览功能会直接发布 `frontend/public/xlsx` 与 `backend/src/main/resources/static/xlsx` 下的静态资源
- 这些资源不会因为被纳入本仓库就自动转为 Apache-2.0
- 当前归档的版权与许可证说明见 `/THIRD_PARTY_NOTICES.md`

## 明确排除

- Aspose.CAD
- 基于 iText 5 的 TIFF 转 PDF 方案
- V1 阶段排除 FFmpeg / JavaCV 转码栈
