# Knowledge Graph Material Task Baseline

## Purpose

记录图谱素材与提取任务改造前的开发环境基线，供 S6c 核对迁移结果。本文不记录数据库连接凭据。

## Collection

- 采集时间：2026-08-17（Asia/Shanghai）。
- 数据源：`dev.env` 配置的开发 MySQL 数据库。
- 查询方式：通过仓库 Maven 缓存中的 MySQL JDBC 驱动执行只读 `SELECT COUNT(*)` 与按状态分组查询。
- 统计口径：素材表、草稿节点表、草稿边表、发布记录表、节点映射表、边映射表，以及提取任务的 `status` 分布。

## Data Baseline

| Object | Count | Status distribution |
| --- | ---: | --- |
| `knowledge_graph_material` | 0 | 无记录 |
| `knowledge_graph_material_node` | 0 | 不适用 |
| `knowledge_graph_material_edge` | 0 | 不适用 |
| `knowledge_graph_publish_record` | 0 | 不适用 |
| `knowledge_graph_material_node_mapping` | 0 | 不适用 |
| `knowledge_graph_material_edge_mapping` | 0 | 不适用 |
| `knowledge_graph_extraction_task` | 0 | 无记录 |

## Legacy Surface Baseline

- `db/schema/knowledge.sql` 的 `knowledge_graph_extraction_task` 仍包含旧 `status` 字段和 `retry_from_task_id` 兼容字段。
- `GraphExtractionApplicationService` 与其实现仍以 AI batch job 作为提取任务事实来源；实现包含启动、重试、当前任务、历史查询和候选应用入口。
- `GraphLegacyWriteEntryShutdownTest` 已断言不存在 `/knowledge/graph-extraction`、`/knowledge/graph-result`、`/knowledge/refinement` HTTP 写入口；本次检索未发现这些 URL 的 Java/XML 调用方。

## S6c Update Contract

S6c 必须在本文件追加迁移后相同口径的统计、差异结论和已执行验证命令结果。S6d 开始前必须确认该更新已提交。
