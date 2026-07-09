# Classics Implementation Coverage

## Status

- 当前状态：已完成
- 覆盖范围：三才图会、王圻文档、明代习俗、跨内容分享、通用内容治理、导出/静态展示、AI 候选、Storage 协作、Discovery 问答入口。
- 真相源：`docs/10-requirements/CLASSICS-REQUIREMENTS.md`、`docs/20-interfaces/CLASSICS-CONTENT-VERSION-SNAPSHOT-INTERFACE.md`、本文件。

## Completion Summary

- 三才图会已完成门类、卷、条目三级治理，支持条目创建、编辑、跨卷迁移、删除、版本历史、恢复、配图、多图预览、视觉资产和批量操作。
- 王圻文档已完成分页、时间线、详情、新增、编辑、删除、原始文件上传/替换、版本历史、恢复、标签、问答对和单文档 QA 入口。
- 明代习俗已完成分页、分类、可见性、详情、新增、编辑、删除、统一标签云筛选、版本历史、恢复、标签和问答对治理。
- 通用内容治理已覆盖标签、问答对、AI 候选应用/拒绝、批量候选治理、批量公开/私有、批量分享和失败明细。
- 分享已完成单内容、多内容、批量创建、公开/私有访问、过期、撤销、恢复、快照、资源读取、访问统计和目标删除占位。
- 导出和三才静态展示已完成任务创建、状态展示、Worker 渲染、Storage 产物写入、下载、过期、主动删除和批量管理。
- Classics 已按 System 权限裁剪三类内容的 view/edit/export/share 操作，并在 Admin Web 控件状态中对齐。
- 删除内容会同步分享目标为 `CONTENT_DELETED`，并重算分享风险态。

## Open Items

- 无当前需求阻塞项。
- Knowledge 标签分类、同义词、审核、合并等完整 taxonomy 治理属于 Knowledge 独立页面，不作为 Classics 页面内联治理阻塞项。
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
| 通用内容 | 批量操作 | 已完成 | 批量分享、批量公开/私有、批量候选应用/拒绝均返回成功数和失败明细 |
| 分享 | 管理端和 Portal 访问 | 已完成 | 多目标、批量、公开/私有、撤销/恢复、访问统计、删除占位均已闭环 |
| 导出 | 三类内容导出 | 已完成 | Worker 渲染、Storage 产物、下载、过期、删除和批量管理已闭环 |
| 静态展示 | 三才展示任务 | 已完成 | 生成、筛选、预览、下载、风险确认、删除和批量管理已闭环 |
| 权限 | 私有内容和管理动作 | 已完成 | 后端过滤/拒绝与 Admin Web 控件禁用已对齐 |
