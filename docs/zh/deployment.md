# 部署说明

## 环境要求

- 后端需要 Java 21
- 前端开发或构建需要 Node.js
- Office 预览转换需要在后端主机上安装 LibreOffice

## 说明

- 生产环境优先使用外部安装的 LibreOffice
- 默认不在项目中捆绑 LibreOffice 二进制
- 如果系统未安装 LibreOffice，Office 预览请求会返回本地化错误信息

