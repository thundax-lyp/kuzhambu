# Full Smoke Requirements

## Purpose

全域冒烟验证一次隔离的全量 seed 导入能经正式发布、图谱提取和图谱发布后，被未认证的
Portal 读取。它不是启动检查、样例检查或生产迁移验证。

## Scope

入口是 `scripts/smoke/full-smoke.sh`。每次运行必须使用隔离的 Docker project、网络、卷和
新建数据库，并执行：

```text
scripts/import-seed-data.sh --rebuild --include-test
```

验收集合是本次导入后 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS` 的全部去重
`(contentType, contentId)`。默认 seed 与 test seed 都在集合内。人工内容、部分发布、既有环境
对账、发布后的编辑和 production 迁移不在范围内。

## Preconditions

- Admin 使用受控账户，并具备三类 Classics 发布权限、`classics:publication:view`、
  `knowledge:graph:view`、`knowledge:graph:edit`。登录必须走正式 pre-auth、SM2 和 session
  login 流程；不得复用浏览器 Cookie。
- Elasticsearch、RocketMQ、workers、admin publication runtime、Knowledge → AI 异步 runtime、
  Sancai 公开 Portal 列表/详情接口及图谱 Portal 接口均已就绪。任一项未就绪，运行必须失败并说明
  缺失项。
- Workers 健康检查必须经 Nginx 的 `/internal/workers/health` 通过。冒烟启动的容器必须在本次
  image load/build 后创建；仅替换 Docker image tag 而复用旧容器不满足本要求。
- Portal 校验请求不得带 `Access-Token`、Cookie 或任何管理员凭据。
- 入口在启动时生成唯一 `smokeRunId`。正式 API 执行器只能在本次运行后创建 evidence JSON，
  并写入相同的 `smokeRunId` 与 `generatedAt`；预先存在的 evidence 文件一律拒绝。证据不得包含
  密码、token、私钥、FastGPT API key 或完整连接串。

## Required Flow

### 1. Import and manifest

导入成功后，脚本读取三类内容的实际 ID，写入 evidence 的 `expected`。后续所有层都必须与
此集合比较；只比较数量不通过。`expected` 中每类至少一条，否则失败。

### 2. Classics publication

每类内容仅通过对应 batch publish API 提交。请求可按固定 `batchSize` 分批，但每个 expected ID
必须恰好提交一次，且响应中 `accepted=true`、有唯一 job ID、无 rejected item。

对每个本次 job 轮询到 deadline。通过条件为：

| Layer | Required state |
| --- | --- |
| Publication job | `jobResultStatus=SUCCEEDED` and `jobStatus=CONTENT_COMMITTED` |
| Main database | expected 集合全部 `lifecycleStatus=PUBLISHED`，无其他状态 |
| Discovery | 每个 expected source ID 有一条 `READY`、`deleted=false` 文档，版本等于 job snapshot |
| Sancai Portal | 未认证列表分页汇总后的集合等于 Sancai expected，且每条详情可读并返回相同 ID 与版本 |

不得直接改 `lifecycle_status`、写 ES、写 FastGPT、伪造 READY 或删除失败 job。

王圻与明代习俗的内容 Portal 列表/详情契约尚未纳入全域冒烟；在其正式公开接口落地前，
不得以 Discovery 或管理员接口替代该验收，也不得把缺失接口误报为通过。三类内容的
Discovery、图谱及图谱 Portal 验收仍为必需项。

FastGPT 不作为本冒烟的直接测试对象：不执行 FastGPT health/API/collection/data 探测，也不把
collection 状态写入 evidence。它仍可作为 publication runtime 的下游依赖启动；任一 publication
job 因该下游失败或超时，仍按 publication job 失败处理。

### 3. Knowledge graph

对每个已发布 `ContentRef` 创建一个图谱提取任务，并以该任务作为此素材唯一的验收任务。任务
达到 `executionStatus=SUCCEEDED` 的前提是 Workers 流式调用完成，且最终 payload 通过该任务的
JSON 输出 schema；`WORKER_STREAM`、`WORKER_RESULT`、`OUTPUT_FORMAT_FAILURE` 或非 JSON 模型输出
均为失败，不得因 HTTP 200、容器健康或存在候选记录而通过。若 `disposition=PENDING`，用正式
candidate apply API 采纳；结果只能是 `ADOPTED_MERGE` 或 `ADOPTED_REPLACE`。重试是同一验收任务；重新生成的
`SUPERSEDED` 历史任务不参与计数，也不得重新采纳。

每份素材的采纳后草稿图必须非空且通过发布预览。预览有 `BLOCKING` issue 或需要人工决策的
冲突时失败。对全部素材执行 preview 和 confirm publish；成功条件为：

- material `status=PUBLISHED`；
- 至少一个有效 `ACTIVE` material mapping；
- 未认证 `POST /portal/knowledge/graph/material/get` 返回 `visible=true`；
- Portal 返回对象恰为该素材有效 mapping 指向的发布对象。共享发布对象允许，草稿、失效映射、
  治理记录和无映射对象不允许出现。

## Completion and Failure

每类 `contentType` 的 expected、accepted、successfulJobs、publishedContents、readyDocuments、
extractionTasks、adoptedTasks、publishedMaterials 和 visibleGraphs 都必须与 expected 是同一集合。
`portalList` 与 `portalDetails` 当前只要求与 `SANCAI_ENTRY` 的 expected 集合相同。任一适用 API
失败、状态失败、超时、缺项、重复项、版本不一致或集合不一致，进程以非零退出。

轮询间隔、deadline 和 batchSize 必须作为运行参数写入 evidence。失败证据至少记录
`contentRef`、job/task ID、最后状态、失败步骤、错误码或原因、已等待时间和 deadline。失败后可以
清理隔离容器、卷和临时文件，但必须先写入脱敏 evidence；不得修改业务状态以让重试显示通过。

只有 `scripts/smoke/verify-full-smoke-evidence.sh` 验证 evidence 成功后，
`full-smoke.sh` 才能输出 `Docker full smoke passed`。

## Related Assets

- 运行命令、配置和故障处置见
  [`HOW-TO-FULL-SMOKE.md`](../40-readiness/HOW-TO-FULL-SMOKE.md)。
- evidence 结构与集合校验由 `scripts/smoke/verify-full-smoke-evidence.sh` 固定。
- 发布、Discovery 和 Portal 契约见
  [`CLASSICS-PUBLICATION-INTERFACE.md`](../20-interfaces/CLASSICS-PUBLICATION-INTERFACE.md)。
- 图谱任务、采纳、发布与 Portal 图谱契约见
  [`KNOWLEDGE-GRAPH-INTERFACE.md`](../20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md)。
