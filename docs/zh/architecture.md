# 架构说明

## 后端

- 基于 Spring Boot 3 的 REST API
- 使用 Caffeine 做内存级会话与缓存索引
- 通过能力注册表管理支持与不支持的文件类型
- 提供远程文件获取与 Office 转换链路
- 提供只读内容流式输出接口

## 前端

- React + TypeScript + Vite
- 使用 i18next 管理 `zh` / `en`
- 单一预览壳层，负责渲染：
  - iframe 中的 PDF
  - 图片直接显示
  - 安全的 Markdown 渲染
  - 只读文本和代码查看
- 明确展示不支持与失败状态

## 转换模型

- Office 文件先下载到临时目录，再通过 LibreOffice 与 JODConverter 转换为 PDF
- 可直接预览的格式通过后端只读透传，不做修改
- 所有转换产物都是临时缓存，不作为可下载导出物管理

