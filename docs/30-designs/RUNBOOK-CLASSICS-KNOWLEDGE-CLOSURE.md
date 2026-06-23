# Classics <-> Knowledge 闭环 RUNBOOK

## Purpose

定义 Classics 与 Knowledge 标签协作闭环的目标结果、操作范围、数据结构调整、接口调整、文件落点、实施顺序和验证要求。

## Scope

覆盖：

- Classics 内容标签与 Knowledge taxonomy 的协作闭环。
- 手工标签、AI 标签确认、删除标签的同步规则。
- Knowledge 内容引用模型的同步与重建。
- 标签排序在 Classics 内的作用域与责任边界。

不覆盖：

- Knowledge 图谱、世系图、数据精修。
- Discovery 搜索与问答接入。
- 权限体系补齐、批量操作、运营统计扩展。

## Target Result

闭环完成后，必须满足：

- `classics_content_tag` 为内容标签主事实。
- `knowledge_tag_content_ref` 为 Knowledge 侧派生引用模型。
- 手工标签、AI 标签确认、删除标签都通过统一协作语义同步到 Knowledge。
- AI 标签确认不再绕过 Knowledge taxonomy。
- 标签排序仍属于 Classics 内容主事实，作用域为 `contentType + contentId`。
- Knowledge taxonomy 后台 CRUD 不作为 Classics 跨域协作入口。
- 两域枚举口径一致，不因内容类型或来源不一致产生脏数据。

## Confirmed Decisions

### 主事实与模型定位

- 保留 `classics_content_tag` 为内容标签绑定主事实。
- 保留 `Knowledge` 为统一标签治理主域。
- `knowledge_tag_content_ref` 只作为 Knowledge 内容引用派生模型，用于治理展示、引用统计和对账修复。
- 不做双主事实。
- 不做 `classics_content_tag` 与 `knowledge_tag_content_ref` 的全字段镜像。

### 协作原则

- 所有手工标签、AI 标签确认、删除标签，都必须通过统一协作语义同步到 Knowledge。
- 不允许仅落 Classics 本地而不回写 Knowledge 引用模型。
- 现有 Knowledge taxonomy 后台 CRUD 不作为 Classics 跨域协作入口。
- 后续需要单独补充稳定的跨域协作语义。

### 手工标签策略

- 手工新增标签时，先通过 Knowledge 协作语义解析已有统一标签或别名。
- 无法解析到既有统一标签时，允许自动创建 Knowledge 标签。
- `tagNameSnapshot` 保留为 Classics 内容侧展示快照，不因后续 Knowledge 治理动作回写历史快照。
- 手工标签绑定后，必须同步更新 Knowledge 内容引用派生模型。

### AI 标签策略

- AI 标签确认路径不得继续绕过 Knowledge 语义。
- AI 标签确认必须先经过 Knowledge 协作语义完成统一标签解析，再回写 Classics 主事实并同步 Knowledge 引用模型。
- AI 新标签默认进入待审核状态，不直接进入正式标签池。

### 排序策略

- 标签排序属于 Classics 内容标签主事实的一部分，不下放为前端事实。
- 前端只负责拖拽交互和提交某条内容内的有序标签列表。
- 后端负责持久化排序真相。
- 排序作用域必须收敛为 `contentType + contentId`。

### 同步字段范围

同步最小公共语义：

- `tagId`
- `contentType`
- `contentId`
- `source`
- `contentTitle`

不直接同步：

- `tagNameSnapshot`
- `priority`
- 标签绑定 `status`

## Data Structure Changes

### D1. `classics_content_tag`

当前结构：

- 表：`db/schema/classics.sql`
- DO：`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/dataobject/ClassicsContentTagDO.java`
- Entity：`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/entity/ClassicsContentTag.java`

确认保留字段：

- `id`
- `content_type`
- `content_id`
- `tag_id`
- `tag_name_snapshot`
- `source`
- `status`
- `priority`

必须变更：

- 将 `priority` 从“全局唯一排序”调整为“单内容内排序”。
- 删除全局唯一约束：
  - `uk_classics_content_tag_priority`
- 新增单内容内排序唯一约束：
  - 建议名：`uk_classics_content_tag_content_priority`
  - 字段：`content_type`, `content_id`, `priority`

建议保持不变：

- `uk_classics_content_tag_name (content_type, content_id, tag_name_snapshot)`
- `idx_classics_content_tag_content (content_type, content_id)`
- `idx_classics_content_tag_tag (tag_id, content_type)`
- `idx_classics_content_tag_name (tag_name_snapshot, content_type)`

影响文件：

- `db/schema/classics.sql`
- `docs/30-designs/CLASSICS-DESIGN.md`
- `kuzhambu-servers/biz/classics/**/ClassicsContentTag*.java`

### D2. `knowledge_tag_content_ref`

当前结构：

- 表：`db/schema/knowledge.sql`
- DO：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/dataobject/TagContentRefDO.java`
- Entity：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/entity/TagContentRef.java`

确认保留字段：

- `id`
- `ref_id`
- `tag_id`
- `content_type`
- `content_id`
- `content_title`
- `source`

确认不新增字段：

- 不新增 `tag_name_snapshot`
- 不新增 `priority`
- 不新增内容标签绑定 `status`

建议保持不变：

- `uk_knowledge_tag_content_ref_unique (tag_id, content_type, content_id)`
- `idx_knowledge_tag_content_ref_tag_id (tag_id)`
- `idx_knowledge_tag_content_ref_content (content_type, content_id)`

影响文件：

- `db/schema/knowledge.sql`
- `docs/30-designs/KNOWLEDGE-DESIGN.md`
- `kuzhambu-servers/biz/knowledge/**/TagContentRef*.java`

### D3. 枚举结构

必须变更：

- Knowledge `ContentType` 改为：
  - `SANCAI_ENTRY`
  - `WANGQI_DOCUMENT`
  - `MING_CUSTOMS`
- Knowledge `TagSource` 改为：
  - `MANUAL`
  - `AI`

涉及文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/enums/ContentType.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/enums/TagSource.java`
- 所有依赖这两个枚举的 assembler / repository / tests

## API Changes

### A1. Classics 标签接口

现有接口：

- `GET /api/classics/content/tags`
- `POST /api/classics/content/tags/add`
- `POST /api/classics/content/tags/update`
- `POST /api/classics/content/tags/sort`

必须变更：

- `POST /api/classics/content/tags/sort`
  - 请求体新增：
    - `contentType`
    - `contentId`
- 新增删除标签接口：
  - 建议：`POST /api/classics/content/tags/delete`
  - 最少入参：
    - `id`
    - `contentType`
    - `contentId`

建议增强：

- `GET /api/classics/content/tags`
  - 响应补充 `tagId`
  - 响应补充 `source`
  - 响应保留 `tagNameSnapshot`
- `POST /api/classics/content/tags/add`
  - 继续允许前端传 `tagNameSnapshot`
  - 不要求前端直接理解 Knowledge 侧创建细节
- `POST /api/classics/content/tags/update`
  - 保持以内容标签绑定更新为中心，不直接暴露 Knowledge taxonomy 写入细节

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentTagSortRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/response/ClassicsContentResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java`

### A2. Classics AI 标签确认接口

现有接口：

- `POST /api/classics/content/ai-candidates/change`

确认保留：

- 不新增新入口，沿用现有接口。

必须变更：

- `capability=tags` 时，后端逻辑改为走 Knowledge 协作语义。
- 不允许再直接按标签名快照重建本地标签。

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`

### A3. Knowledge taxonomy 后台接口

确认不变：

- `KnowledgeTaxonomyController` 现有后台治理接口保持服务 Admin 页面用途。

确认不新增：

- 不新增给 Classics 直接调用的 `interfaces.admin` 或 HTTP 协作接口。

## Module and File Changes

### M1. Classics 模块

必须修改文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
  - 新增标签删除入口
  - 调整排序入口入参
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java`
  - 补足删除所需入参复用
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentTagSortRequest.java`
  - 新增 `contentType`
  - 新增 `contentId`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/response/ClassicsContentResponse.java`
  - 新增 `tagId`
  - 新增 `source`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java`
  - 调整请求/响应装配
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentTagSortCommand.java`
  - 新增 `contentType`
  - 新增 `contentId`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/ClassicsContentApplicationService.java`
  - 增加删除标签用例签名
  - 调整排序命令签名
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
  - 接入 Knowledge 协作语义
  - 改写手工标签新增/更新/删除逻辑
  - 改写 AI 标签确认逻辑
  - 修复排序作用域
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java`
  - 增加按内容范围排序查询与删除支持
  - 废弃或删除无作用域的 `listTags(SortDirection)`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java`
  - 按内容范围实现排序
  - 删除全表排序依赖
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/dataobject/ClassicsContentTagDO.java`
  - 仅随表结构和返回字段同步调整

建议新增文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsTagBindingSupport.java`
  - 封装手工/AI 标签绑定公共流程

### M2. Knowledge 模块

必须修改文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/enums/ContentType.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/enums/TagSource.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/TagContentRefRepository.java`
  - 补按内容删除、按内容查询、按内容重建所需签名
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/TagContentRefRepositoryImpl.java`
  - 实现新增 repository 能力
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/assembler/TaxonomyPersistenceAssembler.java`
  - 同步枚举和引用模型装配
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/TagRepositoryImpl.java`
  - 同步 `TagSource` 口径
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`
  - 保持后台治理用例不变，但同步枚举兼容

建议新增文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/service/KnowledgeTagBindingDomainService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/service/impl/KnowledgeTagBindingDomainServiceImpl.java`

建议新增方法：

- `resolveTagByNameOrAlias(String name)`
- `resolveOrCreateManualTag(String name)`
- `syncContentTagRef(TagId tagId, ContentType contentType, Long contentId, String contentTitle, TagSource source)`
- `removeContentTagRef(TagId tagId, ContentType contentType, Long contentId)`
- `rebuildContentTagRefs(ContentType contentType, Long contentId)`

### M3. 数据与文档模块

必须修改文件：

- `db/schema/classics.sql`
- `db/schema/knowledge.sql`
- `docs/30-designs/CLASSICS-DESIGN.md`
- `docs/30-designs/KNOWLEDGE-DESIGN.md`
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`

建议视需要修改：

- `db/data/classics.sql`
- `db/data/knowledge.sql`

### M4. 测试模块

必须修改文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`

建议新增文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/KnowledgeTagBindingDomainServiceTest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/KnowledgeTaxonomyCompatibilityTest.java`

## Operation Plan

### P1. 统一枚举口径

目标：

- 统一 `contentType`
- 统一 `source`

操作：

- 将 Knowledge taxonomy 中内容类型枚举改为与 Classics 一致：
  - `SANCAI_ENTRY`
  - `WANGQI_DOCUMENT`
  - `MING_CUSTOMS`
- 将 Knowledge taxonomy 中标签来源枚举改为与 Classics 协作口径一致：
  - `MANUAL`
  - `AI`
- 全仓搜索并修正依赖旧值 `MING_CUSTOM`、`AI_EXTRACTED` 的代码、测试和种子数据。

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/enums/ClassicsContentType.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/model/enums/ClassicsContentSource.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/enums/ContentType.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/enums/TagSource.java`
- `kuzhambu-servers/biz/knowledge/**/TaxonomyPersistenceAssembler.java`
- `kuzhambu-servers/biz/knowledge/**/TagContentRef*.java`
- 相关测试文件

### P2. 明确数据结构职责

目标：

- 固化主事实与派生模型边界。

操作：

- 保持 `classics_content_tag` 不变为内容标签主事实。
- 保持 `knowledge_tag_content_ref` 为派生引用模型。
- 不新增与 `classics_content_tag` 镜像重复的字段到 `knowledge_tag_content_ref`。
- 仅确认 `knowledge_tag_content_ref` 足以承载以下字段：
  - `tag_id`
  - `content_type`
  - `content_id`
  - `content_title`
  - `source`

涉及表：

- `classics_content_tag`
- `knowledge_tag_content_ref`

涉及文件：

- `docs/30-designs/CLASSICS-DESIGN.md`
- `docs/30-designs/KNOWLEDGE-DESIGN.md`
- `kuzhambu-servers/biz/classics/**/ClassicsContentTag*.java`
- `kuzhambu-servers/biz/knowledge/**/TagContentRef*.java`

### P3. 新增稳定跨域协作语义

目标：

- 不直接复用 Knowledge taxonomy 后台 CRUD。
- 为 Classics 提供稳定领域协作入口。

操作：

- 在 Knowledge 侧补充稳定协作语义，至少覆盖：
  - `resolveTagByNameOrAlias`
  - `resolveOrCreateManualTag`
  - `syncContentTagRef`
  - `removeContentTagRef`
  - `rebuildContentTagRefs`
- 该语义不通过 `interfaces.admin` 暴露页面接口。
- 该语义供单体内 Classics application 编排调用。

推荐落点：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/service/`
- 必要时增加 application 内部 support / helper，但不以后台 controller 充当跨域入口。

涉及文件：

- `docs/00-governance/SERVERS-ARCHITECTURE.md`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/`

### P4. 接通手工标签闭环

目标：

- 手工标签新增、更新、删除形成闭环。

操作：

- Classics 手工新增标签时：
  - 先按标签名走 Knowledge 协作语义解析统一标签或别名。
  - 若不存在则自动创建 Knowledge 标签。
  - 再落 `classics_content_tag` 主事实。
  - 再同步 `knowledge_tag_content_ref`。
- Classics 更新标签时：
  - 重新解析目标统一标签。
  - 更新 `classics_content_tag`。
  - 同步更新 `knowledge_tag_content_ref`。
- Classics 删除标签时：
  - 删除 `classics_content_tag`。
  - 同步删除对应 `knowledge_tag_content_ref`。
- `tagNameSnapshot` 继续由 Classics 保存，不回写到 Knowledge 引用表。

涉及接口：

- `GET /api/classics/content/tags`
- `POST /api/classics/content/tags/add`
- `POST /api/classics/content/tags/update`
- 若无删除接口，需补充删除入口或确认现有调用点

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/ClassicsContentApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentTagCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java`

### P5. 接通 AI 标签确认闭环

目标：

- AI 标签确认必须进入 Knowledge taxonomy 语义。

操作：

- 保留 Classics AI 候选确认入口。
- 替换当前“直接按标签名重建本地 AI 标签”的逻辑。
- 新逻辑应为：
  - 解析 AI 返回标签列表。
  - 对每个标签先走 Knowledge 协作语义解析已有统一标签。
  - 未命中的标签按 AI 新标签策略进入 Knowledge。
  - 回写 `classics_content_tag`。
  - 同步 `knowledge_tag_content_ref`。
- 不允许 AI 标签确认只写 Classics 本地数据。

涉及接口：

- `POST /api/classics/content/ai-candidates/change`

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsAiCandidatePayloadParser.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`

### P6. 修复标签排序作用域

目标：

- 排序仅作用于单内容内标签。

操作：

- 前端继续负责拖拽交互和提交有序列表。
- 后端排序接口补齐内容作用域：
  - `contentType`
  - `contentId`
- repository 层排序查询改为限定单内容范围。
- 禁止使用全表 `listTags(sortDirection)` 作为排序输入。

涉及接口：

- `POST /api/classics/content/tags/sort`

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentTagSortRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentTagSortCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java`

### P7. 补充重建与对账能力

目标：

- 修复历史数据漂移。
- 为后续联调和上线提供兜底。

操作：

- 增加按单内容重建 `knowledge_tag_content_ref` 的能力。
- 增加按内容类型批量重建的能力。
- 增加按标签或按内容对账的能力。
- 重建逻辑一律以 `classics_content_tag` 为输入真相源。

推荐落点：

- Knowledge domain/service 或 application/support
- 如需管理入口，再评估是否补充 admin 端专用工具接口

涉及文件：

- `kuzhambu-servers/biz/knowledge/**/TagContentRefRepository*.java`
- `kuzhambu-servers/biz/classics/**/ClassicsContentRepository*.java`
- 相关批处理或管理接口文件

### P8. 文档与测试收口

目标：

- 保证闭环方案可追溯、可验证。

操作：

- 更新稳定设计文档与覆盖清单。
- 补充后端单测、集成测试、前端联调用例。
- 记录必要的重建与冒烟步骤。

涉及文档：

- `docs/30-designs/CLASSICS-DESIGN.md`
- `docs/30-designs/KNOWLEDGE-DESIGN.md`
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`

## Required Alignment

在进入实现前，先统一以下口径：

- `contentType`
- `source`

要求：

- Classics 与 Knowledge 使用一致的枚举值。
- 不允许依赖运行时临时映射作为长期方案。

## Required Collaboration Semantics

后续需补充稳定协作语义，至少覆盖：

- `resolveTagByNameOrAlias`
- `resolveOrCreateManualTag`
- `syncContentTagRef`
- `removeContentTagRef`
- `rebuildContentTagRefs`

## Pending Decisions

- 无。

## Confirmed Default Rules

- AI 新标签默认进入待审核状态，不直接进入正式标签池。
- 自动创建 Knowledge 标签时，默认分类为“未分类”。
- Knowledge 禁用标签后：
  - 存量引用保留，不回写、不篡改历史内容标签事实。
  - 增量新增和编辑禁止继续绑定禁用标签。
- 删除标签引用时：
  - 先保证 `classics_content_tag` 主事实删除成功。
  - `knowledge_tag_content_ref` 同步失败时走补偿或重建，不回滚 Classics 主事实。

## Delivery Order

1. 统一 `contentType` 和 `source` 枚举口径。
2. 定义稳定跨域协作语义。
3. 打通手工标签新增、更新、删除的同步闭环。
4. 打通 AI 标签确认到 Knowledge 的闭环。
5. 增加引用重建与对账能力。
6. 修复 Classics 标签排序作用域为 `contentType + contentId`。
7. 补自动化验证与联调冒烟。

## Validation

至少验证：

- Sancai、Wangqi、MingCustoms 三类内容的手工标签新增、更新、删除都能同步到 Knowledge。
- 标签别名解析正确，无法解析时能按规则自动创建标签。
- AI 标签确认后，统一标签绑定与 Knowledge 引用同步正确。
- Knowledge 标签详情中的内容引用数量和明细与 Classics 主事实一致。
- 标签排序只影响单内容内标签顺序，不影响其他内容。
- 重建后 Knowledge 引用模型与 Classics 主事实一致。

验证范围：

- 后端：
  - Classics content service / repository 单测
  - Knowledge taxonomy / content ref 单测
  - 跨域协作语义测试
- 前端：
  - 标签新增、编辑、删除、排序请求体与回显
- 联调：
  - Classics 页面操作后，Knowledge 标签详情引用数量与明细同步正确

## Exit Criteria

满足以下条件后，删除本 RUNBOOK：

- 稳定跨域协作语义已落到正式设计或接口文档。
- 闭环实现与重建能力已完成。
- 自动化验证和联调记录已补齐。
- 本文中的待定事项已全部决策或明确放弃。
