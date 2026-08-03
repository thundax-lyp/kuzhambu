# Classics Implementation Coverage

## Status

- 当前状态：已完成
- 覆盖范围：三才图会、王圻文档、明代习俗、发布和下线任务、通用内容治理、导出、portal 展示、AI 候选、Storage 协作、Discovery 问答入口。
- 真相源：`docs/10-requirements/CLASSICS-REQUIREMENTS.md`、`docs/20-interfaces/CLASSICS-CONTENT-VERSION-SNAPSHOT-INTERFACE.md`、本文件。

## Completion Summary

- 三才图会已完成门类、卷、条目三级治理，支持条目创建、编辑、跨卷迁移、删除、版本历史、恢复、配图、多图预览、视觉资产和批量操作。
- 王圻文档已完成分页、时间线、详情、新增、编辑、删除、原始文件上传/替换、版本历史、恢复、标签、问答对和单文档 QA 入口。
- 明代习俗已完成分页、分类、详情、新增、编辑、删除、统一标签云筛选、版本历史、恢复、标签和问答对治理。
- 通用内容治理已覆盖标签、问答对、AI 候选应用/拒绝、批量候选治理、批量发布/下线和失败明细。
- 发布和下线已改为异步任务，覆盖任务创建、进度查询、失败回填、自动重试、ES 发布状态、FastGPT enable/disable 和端侧清理入口。
- 导出任务继续通过 Worker 渲染并写入 Storage；三才图会展示改为 portal 在线读取 ES 中 `publicationStatus = READY` 且 `deleted = false` 的内容，不再生成静态展示包。
- Classics 已按 System 权限裁剪三类内容的 view/edit/export 操作，并新增 `classics:publication:view` 任务查询权限。
- 删除 `ERROR/OFFLINE` 内容会保留 publication job 快照和端侧引用，用于 ES 与 FastGPT 清理 Schedule 后续收口。

## Open Items

- 无当前需求阻塞项。
- Knowledge 标签分类、审核、合并等完整 taxonomy 治理属于 Knowledge 独立页面，不作为 Classics 页面内联治理阻塞项。
- Discovery 搜索和 QA 质量继续由 Discovery / AI 演进，Classics 只提供内容、上下文入口和快照。

## Validation Evidence

- 2026-07-09：Classics 后端 `spotless:check`、`checkstyle:check`、相关 Maven test 通过。
- 2026-07-09：Admin Web Classics 相关 Vitest 通过，覆盖 Wangqi、Ming Customs、Sancai 和 common 治理组件。
- 2026-07-09：`cd kuzhambu-apps && pnpm --filter ./admin-web run test` 通过，Admin Web 56 个 test files / 250 tests 全绿。

## Requirement Coverage Matrix

| 子域 | 需求范围 | 状态 | 说明 |
| --- | --- | --- | --- |
| 三才图会 | 门类、卷、条目浏览与治理 | 已完成 | CRUD、排序、跨卷迁移、版本、恢复、状态、标签均已闭环 |
| 三才图会 | 配图与视觉资产 | 已完成 | 多图、当前图、原图上传、AI 生成图、Storage 读取和版本写回已闭环 |
| 三才图会 | AI 视觉任务 | 已完成 | 单条、批量、流式过程、候选应用、失败重试已闭环 |
| 王圻文档 | 文档治理 | 已完成 | 列表、时间线、详情、编辑、删除、原始文件、版本和恢复已闭环 |
| 王圻文档 | 单文档问答入口 | 已完成 | Admin 跳转 Portal QA，固定透传 `SINGLE_DOCUMENT + WANGQI_DOCUMENT` 上下文 |
| 明代习俗 | 内容治理 | 已完成 | 列表、详情、分类、标签云、编辑、删除、版本和恢复已闭环 |
| 通用内容 | 标签和问答对 | 已完成 | 手工治理、AI 候选应用、版本快照和 Knowledge 引用同步已闭环 |
| 通用内容 | 批量操作 | 已完成 | 批量发布/下线、批量候选应用/拒绝均返回成功数和失败明细 |
| 发布任务 | 管理端进度与端侧同步 | 已完成 | 发布/下线任务、任务查询、ES READY/OFFLINE、FastGPT enable/disable 和端侧清理入口已闭环 |
| 导出 | 三类内容导出 | 已完成 | Worker 渲染、Storage 产物、下载、过期、删除和批量管理已闭环 |
| Portal 展示 | 三才图会发布展示 | 已调整 | portal 在线读取 ES READY 且未删除内容，不生成静态展示包 |
| 权限 | 内容和发布管理动作 | 已完成 | 后端过滤/拒绝与 Admin Web 控件禁用已对齐 |
