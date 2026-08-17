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

## S6c Migration Check

- 核对时间：2026-08-17 13:24（Asia/Shanghai）。
- 核对数据源：`dev.env` 指向的开发 MySQL 数据库；本文不记录连接凭据。
- 核对方式：使用 Maven 解析到的 `mysql-connector-j` 9.6.0，通过 JDBC 执行只读统计查询。

| Object | Baseline count | Current count | Result |
| --- | ---: | ---: | --- |
| `knowledge_graph_material` | 0 | 0 | 无数据差异 |
| `knowledge_graph_material_node` | 0 | 0 | 无数据差异 |
| `knowledge_graph_material_edge` | 0 | 0 | 无数据差异 |
| `knowledge_graph_material_stats` | 不适用 | 缺表 | schema 尚未同步 |
| `knowledge_graph_publish_record` | 0 | 0 | 无数据差异 |
| `knowledge_graph_published_node_material` | 不适用 | 0 | 新发布映射无记录 |
| `knowledge_graph_published_edge_material` | 不适用 | 0 | 新发布映射无记录 |
| `knowledge_graph_material_node_mapping` | 0 | 0 | 旧映射无记录 |
| `knowledge_graph_material_edge_mapping` | 0 | 0 | 旧映射无记录 |
| `knowledge_graph_extraction_task` | 0 | 0 | 无数据差异 |

### Difference Conclusion

- 当前开发库业务数据为空，素材、草稿节点、草稿边、发布记录、旧映射和提取任务均保持 `0`，未发现迁移导致的数据数量差异。
- 当前开发库 schema 尚未完成本分支结构同步：`knowledge_graph_material_stats` 表不存在。
- 当前开发库 `knowledge_graph_extraction_task` 仍是旧结构，按新字段 `execution_status` / `disposition` 查询状态分布失败；错误为 `Unknown column 'execution_status' in 'field list'`。
- `scripts/verify-graph-migration.sh` 仍依赖本机 `mysql` 客户端；当前 PATH 未发现 `mysql`，本次改用 JDBC 完成只读统计。

### Executed Validation

| Command | Result |
| --- | --- |
| `mvn -pl common/kuzhambu-common-mybatis -DincludeArtifactIds=mysql-connector-j -Dmdep.outputFile=/tmp/kuzhambu-mysql-classpath.txt dependency:build-classpath` | 通过，解析到 `mysql-connector-j` 9.6.0 |
| JDBC `SELECT COUNT(*)` over graph material, draft graph, publication mapping, legacy mapping, and extraction task tables | 通过；除 `knowledge_graph_material_stats` 缺表外，其余计数见上表 |
| JDBC extraction task status distribution query using `execution_status` / `disposition` | 失败；开发库仍为旧 task schema |
| `mvn -pl biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-infra -am -Dtest=GraphMaterialApplicationServiceImplTest,GraphExtractionApplicationServiceImplTest,GraphPublicationApplicationServiceImplTest,GraphMaterialStatsRepositoryImplTest,GraphExtractionTaskCleanupSchedulerTest,GraphExtractionTaskRepositoryImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，13 tests |
