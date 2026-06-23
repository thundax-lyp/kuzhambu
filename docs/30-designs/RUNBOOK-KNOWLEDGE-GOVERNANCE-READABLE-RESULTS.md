# RUNBOOK: Knowledge 标签治理补完与正式结果可读化

## Purpose

本 RUNBOOK 用于推进 Knowledge 下一阶段交付，范围固定为两条主线：

- `标签治理补完`
- `Knowledge 正式结果可读化`

目标不是继续扩张 Knowledge 全量需求，而是在当前已完成 taxonomy 治理和抽取闭环的基础上，把管理员真正缺的治理动作和正式结果读能力补齐。

## Scope

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
- 标签治理统计读能力
- 图谱版本可读化
- 正式实体、关系、世系结果可读化
- Admin Web 对应治理入口和结果查看入口

本轮不包含：

- Discovery 对同义词或图谱结果的消费
- 数据精修工作台
- 图谱鸟瞰层、门类层、详情层可视化
- 质量报告和低质量重提取入口
- 世系图可视化页面

## Current Baseline

当前仓库已具备：

- taxonomy 后端和 Admin Web 治理入口
- Classics -> Knowledge 标签绑定协作链路
- Knowledge -> AI -> Workers -> Knowledge 抽取任务闭环
- 候选结果应用到 `knowledge_entity`、`knowledge_relation`、`knowledge_graph_version`、`knowledge_lineage_node`、`knowledge_lineage_relation`

当前缺口集中在：

- taxonomy 治理动作还缺 `merge`、`deprecate` 和统计视图
- 正式知识事实已经入库，但管理员还缺“稳定读取正式结果”的后台入口

## Fixed Rules

- `Knowledge` 继续保持统一标签、正式知识事实和图谱版本的唯一写入方。
- `ApplicationService`、前端 service 方法和 URL path 必须使用明确业务动作，不回退到 `request` 之类技术词。
- 正式结果可读化优先做“列表 / 详情 / 版本”读能力，不误扩成图谱可视化。
- 标签合并和废弃必须保留可追溯性，不破坏历史内容引用和治理记录。
- 每个执行单元默认控制在 `2-5` 个文件；跨层闭环允许按一个明确判断拆成连续小步。

## Target Result

完成后必须达到：

1. 管理员可预览标签合并影响，再执行合并动作。
2. 管理员可废弃标签，并让其退出新的治理和推荐集合。
3. 管理员可查看标签治理统计，包括使用排行、来源占比和基础分布。
4. 管理员可查看图谱正式版本列表和版本详情。
5. 管理员可查看正式实体、正式关系和正式世系结果。
6. 正式结果查看页能稳定展示来源引用、确认状态、最新版本关联和时间信息。
7. 文档与 coverage 口径只反映已落地结果。

## Design Constraints

### 标签治理补完

- 合并影响预览必须至少覆盖：
  - 源标签和目标标签基础信息
  - 别名迁移影响
  - 内容引用数量影响
  - 待审核或已治理记录影响
- 合并动作后：
  - 源标签名称和别名应并入目标标签别名集合或统一解析口径
  - 历史内容应仍可通过目标标签检索
  - 治理记录必须可追溯
- 废弃动作后：
  - 标签退出新的可用集合
  - 历史引用保留
  - 不删除治理历史

### 正式结果可读化

- 优先建设后台读模型，不直接暴露底层表形态。
- 版本读取以 `knowledge_graph_version` 为入口，向下关联正式事实。
- 正式实体、关系、世系结果页至少展示：
  - 业务键或稳定标识
  - 名称 / 类型 / 关系类型
  - `confirmationStatus`
  - `latestVersionId`
  - 来源引用
  - 首次抽取时间 / 最近抽取时间 / 确认时间
- 本轮不要求图形化展示，只要求可治理、可审阅、可追溯。

## Operation Units

以下单元是本 RUNBOOK 的最小执行颗粒度。每个单元控制在 `2-5` 个文件。

| ID | Result | Files | Notes |
| --- | --- | --- | --- |
| `kgr-01-merge-preview-contract` | 定义标签合并影响预览契约 | `knowledge-application/.../tag/result/*`、`knowledge-application/.../tag/service/*`、相关测试 | 只定义读取契约，不落写动作 |
| `kgr-02-merge-preview-readmodel` | 落地标签合并影响读取 | `knowledge-domain/.../repository/*`、`knowledge-infra/.../repository/impl/*`、相关测试 | 覆盖别名、内容引用和治理影响聚合 |
| `kgr-03-merge-apply` | 落地标签合并动作 | `knowledge-domain/.../service/*`、`knowledge-application/.../service/impl/*`、相关测试 | 保持历史引用可追溯 |
| `kgr-04-deprecate-action` | 落地标签废弃动作 | `knowledge-domain/...`、`knowledge-application/...`、相关测试 | 不删除历史，只退出可用集合 |
| `kgr-05-governance-metrics` | 落地标签治理统计读能力 | `knowledge-application/...`、`knowledge-infra/...`、相关测试 | 只做后台统计读模型 |
| `kgr-06-taxonomy-admin-api` | 暴露合并预览、合并、废弃、统计接口 | `knowledge-interface/.../taxonomy/*` | 后台动作统一业务语义 |
| `kgr-07-formal-read-contract` | 定义图谱版本与正式事实读契约 | `knowledge-application/.../graph/result/*`、`.../graph/service/*`、相关测试 | 不混入写动作 |
| `kgr-08-formal-read-repository` | 落地图谱版本、实体、关系、世系正式结果读取 | `knowledge-domain/.../repository/*`、`knowledge-infra/.../repository/impl/*`、相关测试 | 读模型优先 |
| `kgr-09-formal-admin-api` | 暴露正式结果查看接口 | `knowledge-interface/.../graph/*` | 覆盖版本、实体、关系、世系读取入口 |
| `kgr-10-admin-taxonomy-complete` | Admin Web 补齐合并、废弃、统计入口 | `admin-web/src/pages/knowledge/taxonomy/*` | 以现有 taxonomy 页面为主，不新开割裂入口 |
| `kgr-11-admin-formal-readable` | Admin Web 新增正式结果读页面 | `admin-web/src/pages/knowledge/*`、`src/router/*` | 优先列表 + 详情 |
| `kgr-12-docs-readiness` | 同步设计和 coverage 文档 | `docs/30-designs/*`、`docs/40-readiness/*` | 只写已落地结果 |
| `kgr-13-cleanup` | 收口 TODO 与 RUNBOOK | `TODO.md`、删除本 RUNBOOK | PR 前执行 |

## Suggested Order

建议执行顺序：

1. `kgr-01` 到 `kgr-06`
2. `kgr-07` 到 `kgr-09`
3. `kgr-10` 到 `kgr-11`
4. `kgr-12`
5. `kgr-13`

理由：

- 标签治理补完对 taxonomy 主链路最直接，且依赖面更小。
- 正式结果可读化要等读契约和读仓储先稳定，再接前端。
- 文档和 cleanup 必须以实际落地结果为准，放最后。

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
2. taxonomy 支持基础治理统计读取。
3. Knowledge 后台可查看图谱版本和正式知识事实。
4. Admin Web 有可用的治理入口和正式结果查看入口。
5. 文档与 coverage 已同步。
6. `TODO.md` 只保留下一阶段未关闭任务。
7. 本 RUNBOOK 已删除。
