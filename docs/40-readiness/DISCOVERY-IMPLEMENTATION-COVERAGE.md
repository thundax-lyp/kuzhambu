# Discovery Implementation Coverage

## Purpose

本文档记录 Discovery 域当前实现对需求文档的覆盖状态，用于后续补充开发、联调验收和范围控制。

本文档不替代 `docs/10-requirements/DISCOVERY-REQUIREMENTS.md`、`docs/30-designs/DISCOVERY-DESIGN.md` 或阶段性 RUNBOOK。

本文档要求：

- 覆盖 `DISCOVERY-REQUIREMENTS.md` 的全部需求项。
- 对已形成运行时代码、页面入口或稳定文档边界的能力明确标记 `已完成`。
- 对已有模型、接口骨架或设计闭环，但仍缺运行时代码的能力明确标记 `部分完成`。
- 对当前仓库尚未形成可执行交付物的能力统一标记 `未完成`。

## Status Definition

- `已完成`：当前仓库已有可追溯交付物，并已形成运行时代码、页面入口或稳定文档闭环。
- `部分完成`：已有设计、协议、数据结构或阶段性骨架，但仍缺关键运行时代码、联调或验证闭环。
- `未完成`：当前仓库尚未形成可执行交付物。

## Current Baseline

已完成：

- Discovery 已具备独立需求文档，明确搜索、问答、权限过滤、同义词增强和会话边界。
- Discovery 已具备独立设计文档，并明确 Search 子域当前优先级、内容源范围、数据结构、接口路径和 Elasticsearch 默认适配方向。
- Discovery Search 当前固定只接 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS` 三类内容源。
- Discovery Search 已固定采用“数据库主键 + 业务号”双轨数据模型。
- Discovery Search 已固定 Portal 搜索、Portal 点击、Admin 搜索日志分页和详情接口路径与字段口径。
- Discovery Search 已落地 Java `application / infra / interface` 运行时代码，并完成 starter 装配。
- Discovery Search 已通过 Classics application facade 读取三类“当前可公开消费内容”，不直接依赖 Classics mapper 或 DO。
- Discovery Search 已完成 `DiscoverySearchDocument` 映射、Elasticsearch 默认 Gateway 实现、全量索引重建应用入口和真实检索链路。
- Discovery Search 已完成成功 / 失败搜索日志写入、点击日志写入、Admin 手动重建索引入口，以及 Portal 搜索结果真实分组返回。
- Discovery Search 已完成 `afterCommit + RocketMQ` 增量同步链路，覆盖三类 Classics 内容的新增、更新、可见性变化、删除和内容治理写路径。
- Discovery Search 已完成 `currentVersionNo` 幂等控制、删除态写入、删除态定时物理清理和 `rebuild` 全量兜底闭环。
- Discovery Search 已补齐 `search / click / rebuild` 共享动作白名单，并完成最小 Maven 运行时测试闭环。
- Discovery Query Understanding 已接通 Knowledge 同义词、标签和实体提示读协作，并通过 AI 域完成 query-understanding / query-rewrite 调度。
- Discovery QA 已接通 Portal `chat/completions`、Admin 知识库运维、会话持久化、来源引用、知识同步状态和 provider trace 读取，形成问答闭环。
- Discovery QA 已完成会话软删除、Portal owner 删除保护、Portal 删除后不可见/不可详情/不可追问/不可导出、Admin 删除和已删除会话审计读取。
- Discovery QA 已完成会话 CSV 导出，导出记录写入 `discovery_qa_session_export`，文件上传 Storage 并返回 `exportId`、`storageObjectId`、`filename` 和 `contentType`。
- Portal Web 已完成 QA 会话删除确认、删除后清空当前会话、CSV 导出成功/失败提示；Admin Web 已完成 QA 会话删除、`REMOVED` 状态展示和已删除会话 CSV 导出入口。

部分完成：

- Discovery Search 已完成后端运行时闭环，Portal 搜索页面和 Admin 搜索分析页面已落地，但高亮、复杂排序与分析深化仍需继续完善。
- Discovery 设计文档已明确 Elasticsearch 默认适配、增量同步和删除态清理规则；高级增强能力仍保留未实现状态。

未完成：

- 更细粒度的搜索分析和高级 QA 生命周期能力仍未形成完整交付物。

## Requirement Coverage Matrix

### 搜索

| 需求项                               | 状态     | 已完成部分                                                                                                                                                                     | 未完成部分                                     | 责任域                |
| ------------------------------------ | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------- | --------------------- |
| 跨库搜索三才图会、王圻文档和明代习俗 | 已完成   | Search 已通过 Classics application facade 读取 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS` 三类内容，完成索引文档生成、Elasticsearch 检索、Portal 搜索页面展示与点击回写 | 无                                             | Discovery, Classics   |
| 结果按知识库分组展示                 | 已完成   | Search 返回固定按 `contentType` 分组，并映射为 Portal `groups/items` 结构；Portal 搜索页已按分组渲染结果                                                                       | 无                                             | Discovery             |
| 组内相关性排序                       | 部分完成 | Elasticsearch 检索已形成真实查询链路并返回结果顺序                                                                                                                             | 高级相关性调优、复杂排序策略仍未实现           | Discovery             |
| 关键词高亮                           | 部分完成 | Portal 结果项继续保留 `highlightText` 字段                                                                                                                                     | 本轮允许返回 `null`，高亮文本生成仍未实现      | Discovery             |
| 按知识库、门类、标签、状态、时间筛选 | 部分完成 | `SearchScope`、请求字段和 Search Gateway 真实检索入口已形成                                                                                                                    | 当前筛选能力仍偏基础，复杂条件与标签增强未完成 | Discovery             |
| 权限过滤                             | 部分完成 | 本轮固定只返回当前可公开消费内容，避免把不可前台消费内容暴露到结果中                                                                                                           | 通用权限过滤策略和与 System 的深度整合仍未完成 | Discovery, System     |
| 搜索日志记录                         | 已完成   | 搜索成功 / 失败都会写入 `discovery_search_log`，Admin 已可分页和查看详情                                                                                                       | 搜索分析报表、热词与失败率统计未实现           | Discovery             |
| 点击日志记录                         | 已完成   | Portal 点击接口已真实写入 `discovery_search_click`，并校验 `searchLogId` 存在                                                                                                  | 点击分析、聚合统计未实现                       | Discovery             |
| 搜索深链与状态保留                   | 部分完成 | 结果项已返回稳定占位 `targetPath`                                                                                                                                              | Portal 页面深链消费和搜索状态恢复未实现        | Discovery, Portal Web |
| 无结果空状态提示                     | 未完成   | 需求已沉淀                                                                                                                                                                     | 前端页面和接口联调未实现                       | Discovery, Portal Web |
| 索引增量同步                         | 已完成   | Classics 写路径已在事务提交后发送 RocketMQ 索引同步消息，Discovery 消费端按 `currentVersionNo` 幂等更新 ES 文档                                                                | Outbox、复杂重试编排未实现                     | Discovery, Classics   |
| 索引删除态清理                       | 已完成   | `DELETE` 先写删除态，定时任务按保留期物理清理，Admin `rebuild` 继续兜底                                                                                                        | 删除态保留期策略仍需线上观测优化               | Discovery             |

### 查询理解与增强

| 需求项               | 状态   | 已完成部分                                                                                                       | 未完成部分 | 责任域                   |
| -------------------- | ------ | ---------------------------------------------------------------------------------------------------------------- | ---------- | ------------------------ |
| 查询理解和意图识别   | 已完成 | `QueryUnderstanding`、`SearchIntentType`、`discovery_query_understanding` 数据结构、应用服务和 AI 调度链路已接通 | 无         | Discovery                |
| 同义词扩展           | 已完成 | 需求和设计已固定依赖 Knowledge 同义词词典，Discovery 已通过知识治理读协作消费扩展结果                            | 无         | Discovery, Knowledge     |
| 查询清洗和停用词过滤 | 已完成 | `normalized_query_text`、清洗规则和查询标准化结果已落地                                                          | 无         | Discovery                |
| 查询改写             | 已完成 | `rewritten_query_text`、AI 调度和改写结果落库已落地                                                              | 无         | Discovery, AI            |
| 实体识别和实体链接   | 已完成 | `recognized_entities_json`、实体提示和知识读协作链路已落地                                                       | 无         | Discovery, Knowledge, AI |

### 智能问答

| 需求项               | 状态     | 已完成部分                                                                                                                   | 未完成部分                       | 责任域                  |
| -------------------- | -------- | ---------------------------------------------------------------------------------------------------------------------------- | -------------------------------- | ----------------------- |
| 多库自然语言问答     | 已完成   | Portal QA 页面已改为 `chat/completions`，`QaApplicationService`、知识库回答生成、会话持久化和来源回显已落地                  | 无                               | Discovery               |
| 王圻单文档追加式问答 | 部分完成 | 通用 QA 已接通 `knowledgeBase` 维度和内容源上下文                                                                            | 专用单文档追加式入口尚未单独拆分 | Discovery, Classics, AI |
| 多轮会话             | 已完成   | `QaSession`、`QaMessage`、`openSession`、`chat/completions`、会话分页和会话详情读取已落地                                    | 无                               | Discovery               |
| 来源引用             | 已完成   | `QaSource`、`discovery_qa_message_source`、来源写入、可见性重检与按消息查询接口已落地                                        | 无                               | Discovery               |
| 会话删除和导出       | 已完成   | Portal/Admin 会话删除接口、软删除状态机、Portal 已删除会话访问拦截、Admin 已删除会话审计导出、CSV 生成、Storage 上传和前端入口均已落地 | 无                               | Discovery               |
| 管理员调试信息       | 已完成   | Admin QA 运维页已覆盖知识库健康、重建、同步分页、来源列表和 provider trace                                                   | 无                               | Discovery               |
| 知识库同步状态       | 已完成   | Admin QA 已提供 `knowledge/health`、`knowledge/rebuild`、`knowledge/sync` 和 `knowledge/sync/page`，可查看同步状态和失败原因 | 无                               | Discovery               |

### 运行时验证

| 需求项             | 状态   | 已完成部分                                                                                                                                                                                  | 未完成部分                                                   | 责任域    |
| ------------------ | ------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------ | --------- |
| 当前阶段运行时验证 | 已完成 | 已完成 Search / QA runtime 与索引同步执行闭环，starter 扫描、Admin / Portal controller 测试、application 测试和最小 Maven 验证均已跑通；本地 Elasticsearch HTTPS 连通和认证已用 `curl` 验证 | 全量 PR workflow、真实前端页面联调和 ES 集群健康治理仍未完成 | Discovery |

## Unfinished Focus

| 能力项                 | 状态     | 说明                                                                                                                 |
| ---------------------- | -------- | -------------------------------------------------------------------------------------------------------------------- |
| Search 子域骨架代码    | 已完成   | Search 相关 application、infra、interface 代码、测试和 starter 装配已落地                                            |
| Elasticsearch 默认适配 | 已完成   | 已在 infra 内实现 `ElasticsearchSearchIndexGateway`，并接入 starter 运行时配置                                       |
| 索引增量同步           | 已完成   | 已实现 `afterCommit + RocketMQ` 发送、Discovery 消费、`currentVersionNo` 幂等和删除态清理                            |
| Portal 搜索入口        | 已完成   | Portal 搜索和点击接口已可运行，Portal 搜索页面已落地                                                                 |
| Admin 搜索分析入口     | 已完成   | Admin 搜索日志分页、详情和索引重建入口已落地，搜索分析页面已落地                                                     |
| QA 子域                | 已完成   | QA 核心运行时、Portal `chat/completions` 页面、Admin 运维页、知识同步、provider trace、会话删除和 CSV 导出已落地 |

## Residual Risks

- Search 当前明确只接 Classics 三类内容源；后续若扩大到其他业务域，需要重新确认 `SearchScope`、索引文档和权限过滤模型。
- Search 当前已形成基础闭环；高亮、复杂排序和搜索分析深度优化仍需后续迭代。
- Search 当前增量同步采用 `afterCommit` 直接发 MQ，不采用 outbox；当数据库提交成功但 MQ 发送失败时，仍需依赖重试或 Admin `rebuild` 做恢复。
- 本地 Elasticsearch 虽已验证 HTTPS 地址和认证可达，但当前集群健康状态不是绿态；若后续索引重建或检索异常，需要继续排查未分配分片与证书信任配置。
- Discovery QA 知识同步依赖外部 Knowledge Base provider；provider 不可用时需要依靠同步失败原因、provider trace 和 Admin 重建入口排查。
- Discovery QA CSV 导出依赖 Storage 上传链路；Storage 不可用时导出记录会标记 `FAILED` 并返回失败原因，需要由 Admin 侧排查 Storage 和对象权限配置。
