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

部分完成：

- Discovery Search 已完成 Search 子域骨架 RUNBOOK 和 TODO 拆解，但运行时代码、数据库脚本、测试和前端入口尚未落地。
- Discovery 设计文档已明确 Elasticsearch 默认适配和复杂能力占位异常策略，但当前仓库还没有 Discovery Java 实现代码。

未完成：

- Discovery Java domain / application / infra / interface 四层代码尚未创建。
- Discovery Search 表结构、Mapper、Repository、ApplicationService、Controller、测试和 starter 接入尚未落地。
- Portal 搜索页面、Admin 搜索分析页面、问答会话、多轮上下文、来源引用和调试信息仍未形成可执行交付物。

## Requirement Coverage Matrix

### 搜索

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 跨库搜索三才图会、王圻文档和明代习俗 | 部分完成 | Search 子域设计已固定内容源范围为 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS` | 运行时代码、索引构建和结果返回未实现 | Discovery, Classics |
| 结果按知识库分组展示 | 部分完成 | 设计文档已固定 `SearchResultGroup` 和 Portal 返回分组结构 | 分组检索与出参实现未完成 | Discovery |
| 组内相关性排序 | 部分完成 | 设计文档已保留相关性排序责任在 Search 检索链路 | 实际排序策略和 ES DSL 未实现 | Discovery |
| 关键词高亮 | 部分完成 | Portal 结果项已固定保留 `highlightText` 字段 | 高亮生成未实现 | Discovery |
| 按知识库、门类、标签、状态、时间筛选 | 部分完成 | `SearchScope` 与搜索请求字段已在设计文档固定 | 实际筛选执行未实现 | Discovery |
| 权限过滤 | 部分完成 | 需求和设计文档都已固定“结果出参前完成权限过滤” | 权限过滤实现未完成 | Discovery, System |
| 搜索日志记录 | 部分完成 | `discovery_search_log` 字段和 Admin 日志接口口径已固定 | 数据库、写入逻辑和查询接口未实现 | Discovery |
| 点击日志记录 | 部分完成 | `discovery_search_click` 字段和 Portal 点击接口口径已固定 | 数据库、写入逻辑和查询接口未实现 | Discovery |
| 搜索深链与状态保留 | 部分完成 | 设计文档已为结果项保留 `targetPath` | Portal 页面和状态恢复未实现 | Discovery, Portal Web |
| 无结果空状态提示 | 未完成 | 需求已沉淀 | 前端页面和接口联调未实现 | Discovery, Portal Web |

### 查询理解与增强

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 查询理解和意图识别 | 部分完成 | `QueryUnderstanding`、`SearchIntentType` 和 `discovery_query_understanding` 字段已在设计文档固定 | 实际理解链路未实现 | Discovery |
| 同义词扩展 | 部分完成 | 需求和设计已固定依赖 Knowledge 同义词词典，数据结构已预留 `expanded_synonyms_json` | 实际扩展逻辑未实现 | Discovery, Knowledge |
| 查询清洗和停用词过滤 | 部分完成 | 数据结构已预留 `normalized_query_text` | 清洗规则未实现 | Discovery |
| 查询改写 | 部分完成 | 数据结构已预留 `rewritten_query_text` | 改写执行未实现 | Discovery, AI |
| 实体识别和实体链接 | 部分完成 | 数据结构已预留 `recognized_entities_json` | 实体增强执行未实现 | Discovery, Knowledge, AI |

### 智能问答

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 多库自然语言问答 | 未完成 | 需求和设计文档已沉淀问答边界 | QA 运行时代码未实现 | Discovery, AI |
| 王圻单文档追加式问答 | 未完成 | 需求和设计文档已定义 | 运行时代码未实现 | Discovery, Classics, AI |
| 多轮会话 | 未完成 | 需求和设计文档已定义 `QaSession`、`QaMessage` | 运行时代码未实现 | Discovery |
| 来源引用 | 未完成 | 需求和设计文档已定义 `QaSource` | 运行时代码未实现 | Discovery |
| 会话列表、删除和导出 | 未完成 | 需求和设计文档已沉淀 | 运行时代码未实现 | Discovery |
| 管理员调试信息 | 未完成 | 需求和设计文档已定义 `QaDebugContext` | 运行时代码未实现 | Discovery |

### 运行时验证

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 当前阶段运行时验证 | 未完成 | 已形成 Discovery Search RUNBOOK 和 TODO 拆解，可直接指导后续实现 | Discovery 模块代码、测试、Maven 验证和 starter 扫描验证均未执行 | Discovery |

## Unfinished Focus

| 能力项 | 状态 | 说明 |
| --- | --- | --- |
| Search 子域骨架代码 | 未完成 | 当前仅完成需求、设计、RUNBOOK 和 TODO 拆解，尚未创建 Java 代码 |
| Elasticsearch 默认适配 | 部分完成 | 设计文档已固定默认适配方向，尚无 infra 实现 |
| Portal 搜索入口 | 未完成 | 仅固定接口协议，Portal 页面未实现 |
| Admin 搜索分析入口 | 未完成 | 仅固定接口协议，Admin 页面未实现 |
| QA 子域 | 未完成 | 当前阶段未进入实现范围 |

## Residual Risks

- Discovery 当前仍无 Java 代码，后续若在实现时调整包结构、命名或接口路径，需要同步更新本覆盖文档和设计文档。
- Search 当前明确只接 Classics 三类内容源；后续若扩大到其他业务域，需要重新确认 SearchScope、索引文档和权限过滤模型。
- Search 当前已固定“数据库主键 + 业务号”双轨；后续若统一 ID 口径变化，需要同步 `SERVERS-UNIFIED-ID-DESIGN.md` 与 Discovery 数据模型。
