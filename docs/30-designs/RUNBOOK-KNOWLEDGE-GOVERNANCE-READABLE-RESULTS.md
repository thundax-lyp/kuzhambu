# RUNBOOK: Knowledge 标签治理补完与正式结果可读化

## Purpose

本 RUNBOOK 的目标不是记录讨论过程，而是为下一阶段形成稳定、准确、可执行的 `TODO` 拆解依据。

因此本文档必须固定：

- 本轮明确边界
- 需要的数据结构变更
- 相关代码与文档落点
- 可独立验收的小任务颗粒度

## Scope

本轮只覆盖两条主线：

1. `标签治理补完`
2. `Knowledge 正式结果可读化`

本轮范围：

- `kuzhambu-servers/biz/knowledge`
- `kuzhambu-apps/admin-web`
- `db/schema/knowledge.sql`
- `docs/30-designs/*`
- `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`

本轮包含：

- 标签合并影响预览
- 标签合并动作
- 标签废弃动作
- 标签治理完整统计读取
- 图谱版本读取
- 正式实体读取
- 正式关系读取
- 正式世系结果读取
- 后台接口与 Admin Web 可读入口

本轮不包含：

- Discovery 对同义词或图谱结果的消费
- 数据精修工作台
- 图谱鸟瞰层、门类层、详情层可视化
- 质量报告和低质量重提取入口
- 世系图可视化页面
- 正式结果直接编辑能力

## Current Baseline

当前仓库已具备：

- taxonomy 后端和 Admin Web 基础治理入口
- Classics -> Knowledge 标签绑定协作链路
- Knowledge -> AI -> Workers -> Knowledge 抽取任务闭环
- 候选结果应用到：
  - `knowledge_entity`
  - `knowledge_relation`
  - `knowledge_graph_version`
  - `knowledge_lineage_node`
  - `knowledge_lineage_relation`

当前主要缺口：

- taxonomy 缺 `merge preview`、`merge apply`、`deprecate`、`metrics`
- 正式知识事实已入库，但管理员缺少稳定读取入口

## Fixed Rules

- `Knowledge` 继续保持统一标签、正式知识事实和图谱版本的唯一写入方。
- `ApplicationService`、前端 service 方法和 URL path 必须使用明确业务动作，不回退到 `request` 之类技术词。
- 正式结果可读化固定定位为“后台审阅与治理读取”，不是图谱可视化。
- 本轮正式结果页只读，不承载实体、关系或世系结果编辑。
- 标签合并和废弃必须保留历史引用和治理记录，不做物理删除。
- 源标签合并后仍允许后台查看，但必须明确显示其已并入目标标签。
- 源标签合并后默认不再作为可用标签参与新的治理选择、推荐或人工绑定入口。
- 每个执行单元默认控制在 `2-5` 个文件；如果一个任务同时包含契约、仓储、接口和页面，则必须拆分。
- 标签月度新增趋势固定按“标签首次进入正式可用集合”的时间口径统计，不按最后更新时间统计。
- 正式结果可读化默认以“图谱版本列表”作为主入口，再从版本下钻到实体、关系和世系结果。

## Target Result

完成后必须达到：

1. 管理员可预览标签合并影响，再执行合并动作。
2. 管理员可废弃标签，并让其退出新的可用集合。
3. 管理员可查看标签治理统计，包括使用排行、知识库分布、来源占比和月度新增趋势。
4. 管理员可查看图谱正式版本列表和版本详情。
5. 管理员可查看正式实体、正式关系和正式世系结果。
6. 正式结果查看页能稳定展示来源引用、确认状态、最新版本关联和时间信息。
7. 文档与 coverage 口径只反映已落地结果。

## Data Structure Changes

### 标签治理补完

目标：尽量复用现有 taxonomy 结构，只在确有必要时补最小字段。

优先复用现有表：

- `knowledge_tag`
- `knowledge_tag_alias`
- `knowledge_tag_content_ref`
- `knowledge_tag_review_item`

建议新增或补齐的结构只围绕以下事实展开：

- 标签是否已废弃
- 标签是否已并入目标标签
- 合并影响统计是否可稳定读取
- 治理统计是否可稳定支撑排行、分布、占比和趋势

建议优先采用的字段策略：

- 在 `knowledge_tag` 上补充：
  - `merged_to_tag_id`
  - `deprecated_at`
  - `deprecated_by`
- 如现有状态字段已足够表达“废弃”，则不新增重复状态字段，优先复用状态口径。
- 治理统计优先通过读模型聚合；若月度新增趋势现有结构难以稳定支撑，可补最小时间维度索引或查询条件，但不默认新增统计快照表。

本轮不建议新增：

- 标签治理历史快照表
- 标签统计物化表
- 标签合并回滚表

### 正式结果可读化

目标：复用已有正式表，优先建设读契约和读模型，不把“可读化”误做成“新存储方案”。

直接复用现有正式表：

- `knowledge_graph_version`
- `knowledge_entity`
- `knowledge_relation`
- `knowledge_lineage_node`
- `knowledge_lineage_relation`

读取时必须稳定呈现的字段：

- 图谱版本：
  - `version_id`
  - `task_id`
  - `candidate_id`
  - `task_type`
  - `source_content_type`
  - `source_content_id`
  - `version_no`
  - `status`
  - `applied_at`
- 正式实体：
  - `entity_id`
  - `entity_key`
  - `name`
  - `entity_type`
  - `description`
  - `confirmation_status`
  - `latest_version_id`
  - `source_refs_json`
  - `first_extracted_at`
  - `last_extracted_at`
  - `confirmed_at`
- 正式关系：
  - `relation_id`
  - `relation_key`
  - `source_name`
  - `target_name`
  - `relation_type`
  - `evidence`
  - `confirmation_status`
  - `latest_version_id`
  - `source_refs_json`
  - `first_extracted_at`
  - `last_extracted_at`
  - `confirmed_at`
- 世系节点 / 世系关系：
  - 与实体 / 关系同类可读字段

本轮不建议新增：

- 图谱浏览专用缓存表
- 图可视化节点边快照表
- 正式事实二次投影表

前端默认入口规则：

- 先提供图谱版本列表
- 从版本详情下钻查看正式实体、正式关系和正式世系结果
- 不把“全局正式实体总表”作为第一入口

## Related Files

### 标签治理补完

后端可能涉及：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/**`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/**`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/**`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/**`

前端可能涉及：

- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/**`

SQL / 文档可能涉及：

- `db/schema/knowledge.sql`
- `docs/30-designs/KNOWLEDGE-DESIGN.md`
- `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`

### 正式结果可读化

后端可能涉及：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/**`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/**`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/**`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/**`

前端可能涉及：

- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/**`
- 新增正式结果读取页时的 `kuzhambu-apps/admin-web/src/pages/knowledge/**`
- `kuzhambu-apps/admin-web/src/router/index.tsx`

SQL / 文档可能涉及：

- `db/schema/knowledge.sql`
- `docs/30-designs/KNOWLEDGE-DESIGN.md`
- `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`

## Operation Units

以下单元是本 RUNBOOK 的最小执行颗粒度。每个单元都应该能直接转成单独 TODO。

### A. 标签治理补完

| ID | Result | Data Structure Changes | Related Files | Not Included |
| --- | --- | --- | --- | --- |
| `kgr-01-merge-preview-contract` | 定义标签合并影响预览读契约 | 无；先只定义 result / service contract | `knowledge-application/.../taxonomy/result/*`、`.../service/*`、测试 | 不落写动作 |
| `kgr-02-merge-preview-readmodel` | 落地标签合并影响读取 | 如确有必要补充最小字段；否则只做聚合查询 | `knowledge-domain/.../repository/*`、`knowledge-infra/.../repository/impl/*`、测试 | 不执行真正合并 |
| `kgr-03-merge-apply-domain` | 定义标签合并领域语义 | 只补 merge 所需最小字段 | `knowledge-domain/.../service/*`、`domain/taxonomy/model/*`、测试 | 不改前端 |
| `kgr-04-merge-apply-service` | 落地标签合并 application 编排 | 复用上一单元字段 | `knowledge-application/.../service/impl/*`、测试 | 不改页面 |
| `kgr-05-deprecate-contract-action` | 定义并落地标签废弃动作 | 如现有状态不足，再补废弃字段 | `knowledge-application/...`、`knowledge-domain/...`、`knowledge-infra/...`、测试 | 不做批量废弃 |
| `kgr-06-governance-metrics-contract` | 定义标签治理完整统计读契约 | 无；优先聚合读模型 | `knowledge-application/.../taxonomy/result/*`、`.../service/*`、测试 | 不做页面实现 |
| `kgr-07-governance-metrics-readmodel` | 落地标签治理完整统计读取 | 无新增统计表，优先查询聚合；必要时补最小时间维度索引 | `knowledge-domain/.../repository/*`、`knowledge-infra/.../repository/impl/*`、测试 | 不做缓存化 |
| `kgr-08-taxonomy-admin-api` | 暴露合并预览、合并、废弃、统计接口 | 跟随前述最小字段变化 | `knowledge-interface/.../taxonomy/*` | 不改 admin-web |
| `kgr-09-admin-taxonomy-merge` | Admin Web 补齐合并预览和合并动作 | 无 | `admin-web/src/pages/knowledge/taxonomy/*` | 不做统计 |
| `kgr-10-admin-taxonomy-deprecate-metrics` | Admin Web 补齐废弃动作和统计入口 | 无 | `admin-web/src/pages/knowledge/taxonomy/*` | 不开新独立系统页 |

### B. 正式结果可读化

| ID | Result | Data Structure Changes | Related Files | Not Included |
| --- | --- | --- | --- | --- |
| `kgr-11-version-read-contract` | 定义图谱版本读取契约 | 无；优先复用 `knowledge_graph_version` | `knowledge-application/.../graph/result/*`、`.../service/*`、测试 | 不做实体读取 |
| `kgr-12-version-read-repository` | 落地图谱版本读取 | 无；必要时只补索引或 SQL | `knowledge-domain/.../repository/*`、`knowledge-infra/.../repository/impl/*`、测试 | 不改接口 |
| `kgr-13-entity-read-contract` | 定义正式实体读取契约 | 无；复用 `knowledge_entity` | `knowledge-application/.../graph/result/*`、`.../service/*`、测试 | 不做关系读取 |
| `kgr-14-entity-read-repository` | 落地正式实体读取 | 无；必要时只补查询 SQL | `knowledge-domain/.../repository/*`、`knowledge-infra/.../repository/impl/*`、测试 | 不改接口 |
| `kgr-15-relation-read-contract` | 定义正式关系读取契约 | 无；复用 `knowledge_relation` | `knowledge-application/.../graph/result/*`、`.../service/*`、测试 | 不做世系读取 |
| `kgr-16-relation-read-repository` | 落地正式关系读取 | 无；必要时只补查询 SQL | `knowledge-domain/.../repository/*`、`knowledge-infra/.../repository/impl/*`、测试 | 不改接口 |
| `kgr-17-lineage-read-contract` | 定义正式世系读取契约 | 无；复用 `knowledge_lineage_node` / `knowledge_lineage_relation` | `knowledge-application/.../graph/result/*`、`.../service/*`、测试 | 不做图可视化 |
| `kgr-18-lineage-read-repository` | 落地正式世系读取 | 无；必要时只补查询 SQL | `knowledge-domain/.../repository/*`、`knowledge-infra/.../repository/impl/*`、测试 | 不改接口 |
| `kgr-19-formal-read-admin-api` | 暴露图谱版本、正式实体、正式关系、正式世系读取接口 | 跟随前述最小字段变化 | `knowledge-interface/.../graph/*` | 不改 admin-web |
| `kgr-20-admin-formal-page-shell` | 搭建正式结果读取页骨架和路由 | 无 | `admin-web/src/pages/knowledge/*`、`src/router/index.tsx`、页面测试 | 不做复杂交互 |
| `kgr-21-admin-version-readable` | Admin Web 接通图谱版本列表和详情 | 无 | `admin-web/src/pages/knowledge/*`、service contract、测试 | 不做实体关系页 |
| `kgr-22-admin-formal-facts-readable` | Admin Web 接通正式实体、关系、世系列表与详情 | 无 | `admin-web/src/pages/knowledge/*`、service contract、测试 | 不做编辑能力 |

### C. 文档与收口

| ID | Result | Data Structure Changes | Related Files | Not Included |
| --- | --- | --- | --- | --- |
| `kgr-23-docs-readiness` | 同步设计和 coverage 文档 | 只反映实际已落地结构 | `docs/30-designs/*`、`docs/40-readiness/*` | 不提前写未实现能力 |
| `kgr-24-cleanup` | 收口 TODO 与 RUNBOOK | 无 | `TODO.md`、删除本 RUNBOOK | 不混入功能代码 |

## Suggested Order

建议执行顺序：

1. `kgr-01` 到 `kgr-08`
2. `kgr-09` 到 `kgr-10`
3. `kgr-11` 到 `kgr-19`
4. `kgr-20` 到 `kgr-22`
5. `kgr-23`
6. `kgr-24`

理由：

- taxonomy 治理补完依赖更少，优先收敛业务规则。
- 正式结果可读化必须先稳定后端读契约和读仓储，再接后台接口和前端页面。
- 文档和 cleanup 必须以真实落地结果为准。

## TODO Generation Rules

从本 RUNBOOK 生成 TODO 时固定遵守：

- 一个 TODO 只表达一个主动作。
- 一个 TODO 默认只覆盖一个执行单元。
- 一个 TODO 同时跨越“契约 + 仓储 + 接口 + 页面”时，必须拆开。
- 一个 TODO 超过 `2-5` 个文件时，继续拆。
- TODO 只写执行任务，不把整段 RUNBOOK 原文抄过去。

## Verification

### Knowledge Servers

每个后端单元完成后至少执行相关模块：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-domain,biz/knowledge/kuzhambu-knowledge-infra,biz/knowledge/kuzhambu-knowledge-interface -am spotless:apply test
```

PR 前执行：

```sh
cd kuzhambu-servers
mvn -q clean
mvn -q spotless:check
mvn -q checkstyle:check
mvn -q test
```

### Admin Web

每个前端单元完成后执行：

```sh
cd kuzhambu-apps
npm --workspace admin-web run format
npm run format:check
npm run lint
npm run test
```

### DB / Docs

- `db/schema/knowledge.sql` 变更后，必须核对表结构与 domain / infra 一致。
- 设计或 coverage 文档变更后，只允许描述已落地结果。

## Exit Criteria

同时满足以下条件才算闭环：

1. taxonomy 支持合并影响预览、合并和废弃动作。
2. taxonomy 支持完整治理统计读取，覆盖使用排行、知识库分布、来源占比和月度新增趋势。
3. Knowledge 后台可查看图谱版本和正式知识事实。
4. Admin Web 有可用的治理入口和正式结果查看入口。
5. 文档与 coverage 已同步。
6. `TODO.md` 只保留下一阶段未关闭任务。
7. 本 RUNBOOK 已删除。
