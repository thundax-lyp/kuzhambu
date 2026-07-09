# Knowledge Implementation Coverage

## Status

- 当前状态：已完成，存在一个页面集成口径的非阻塞差异。
- 覆盖范围：统一标签、同义词、标签审核、标签合并、标签废弃、数据精修、实体关系、图谱版本、世系图、质量报告、Portal 只读入口、Discovery 读协作。
- 真相源：`docs/10-requirements/KNOWLEDGE-REQUIREMENTS.md`、本文件。

## Completion Summary

- Taxonomy 已完成分类、标签、待审核标签、别名、同义词、标签合并、标签废弃、批量审核和治理统计。
- Knowledge 已为 Classics 提供统一标签解析、自动创建、内容引用同步和内容引用删除能力。
- Knowledge 已为 Discovery 提供同义词扩展、方向查询、标签提示和实体提示。
- 图谱抽取已完成 `RELATION`、`GRAPH`、`LINEAGE` 三类任务，支持批量、取消、重生成、候选应用和正式结果落库。
- 精修工作台已完成实体、关系、世系节点、世系关系的确认、编辑、删除、新增、应用和人工质量标注。
- 图谱正式结果已完成版本列表、版本详情、实体/关系/世系结果浏览和精修状态展示。
- 质量报告已完成快照生成、问题清单、来源明细、人工标注、精修后过期提示和低质量门类重提取。
- Portal 已完成 `/knowledge`、`/knowledge/quality`、`/knowledge/atlas`、`/knowledge/lineage` 四个只读入口。

## Open Items

- 无后端或页面主链路阻塞项。
- Classics 三类内容编辑页已内联标签治理、问答对治理和 AI 候选确认；完整 taxonomy 治理仍在 `/knowledge/taxonomy` 独立页面。该差异是页面边界选择，不阻塞 Knowledge 主需求完成。
- 后续改动权限、字段或接口返回时，应同步更新 Admin Web 契约测试和 Playwright 断言。

## Validation Evidence

- 2026-07-09：`mvn -pl biz/knowledge,biz/ai -am spotless:check checkstyle:check test` 通过。
- 2026-07-09：Workers `ruff format --check`、`ruff check`、`pytest` 通过。
- 2026-07-09：Admin Web `format:check`、`lint`、`build`、`test` 通过。
- 2026-07-09：Knowledge Playwright 6 个页面冒烟通过。
- 运行时证据：`docs/40-readiness/KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md`。

## Requirement Coverage Matrix

| 子域 | 需求范围 | 状态 | 说明 |
| --- | --- | --- | --- |
| Taxonomy | 标签分类、标签、详情、搜索 | 已完成 | Admin Web 和后端接口已完成 |
| Taxonomy | 待审核标签 | 已完成 | 逐条审核、批量审核、通过分类选择和拒绝已完成 |
| Taxonomy | 标签别名、合并、废弃 | 已完成 | 影响预览、执行和历史引用迁移已完成 |
| Taxonomy | 同义词 | 已完成 | 新增、编辑、删除、搜索和方向查询已完成 |
| Taxonomy | 治理统计 | 已完成 | 使用排行、知识库分布、来源占比、月度新增已完成 |
| Classics 协作 | 内容标签绑定 | 已完成 | 手工/AI 标签自动创建、引用同步和删除已完成 |
| Discovery 协作 | 搜索和问答增强 | 已完成 | 同义词、标签提示、实体提示已被 Discovery 消费 |
| 数据精修 | 草稿确认和正式回写 | 已完成 | 实体、关系、世系节点和世系关系均已覆盖 |
| 数据精修 | 人工质量标注 | 已完成 | 写入、删除、分页和质量汇总已完成 |
| 图谱抽取 | AI 任务与候选应用 | 已完成 | 关系、图谱、世系三类任务已完成 |
| 图谱浏览 | Admin 正式结果 | 已完成 | 版本、实体、关系、世系结果和精修状态已完成 |
| Portal | 图谱、世系、质量只读入口 | 已完成 | 四个 Portal 路由已接通 |
| 质量报告 | 报告生成与重提取 | 已完成 | 快照、问题、来源、标注、低质量门类重提取已完成 |
| Classics 页面集成 | 内联完整 taxonomy 治理 | 部分完成 | 标签/问答/候选已内联；完整 taxonomy 保留在独立页面 |
