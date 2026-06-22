# RUNBOOK-KNOWLEDGE-MVP

## Purpose

本 RUNBOOK 用于完成 Knowledge MVP。目标是交付可运行的后台标签治理与同义词治理闭环，不包含知识图谱、数据精修、Portal 只读和 Discovery 接入。

## Branch

- 当前任务分支固定为 `docs/knowledge-mvp-runbook`。
- 实施任务前如果当前分支不是 `docs/knowledge-mvp-runbook`，先切换到该分支。

## Fixed Scope

本次只交付以下能力：

1. 标签分类管理：分页、创建、更新、启用、禁用。
2. 统一标签管理：分页、详情、创建、更新、启用、禁用、按标签查看关联内容数量。
3. 标签待审核列表：分页、通过、拒绝。
4. 标签别名管理：按标签查看、新增、删除。
5. 同义词管理：分页、创建、更新、启用、禁用、删除。
6. 后台菜单与页面入口。

## Out Of Scope

以下内容本次禁止实现：

1. 数据精修。
2. 知识图谱。
3. 世系图。
4. Portal 页面。
5. Workers 改造。
6. Discovery 搜索或问答接入。
7. 标签合并、标签废弃、批量操作、统计报表。
8. Classics 内容编辑页内联知识治理入口。

## Fixed Decisions

1. Knowledge MVP 只使用 Admin 端。
2. Knowledge MVP 只新建一个子域：`taxonomy`。
3. 所有后端 Java 包路径都使用 `com.thundax.kuzhambu.knowledge`。
4. 所有 Admin API 都挂在 `/api/knowledge/taxonomy`。
5. 所有前端页面都放在 `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/`。
6. 知识菜单由 `db/data-source/system.json` 维护，不在前端硬编码菜单。
7. Knowledge MVP 使用已有 `db/schema/knowledge.sql` 与 `db/data/knowledge.sql`，不新增其他 knowledge SQL 文件。
8. 本次 `db/schema/knowledge.sql` 只保留 MVP 所需表；删除图谱、精修、世系图相关 DDL。

## Data Model

### Tables To Keep

`db/schema/knowledge.sql` 只保留以下 5 张表，表名与字段名必须完全一致。

#### 1. `knowledge_tag_category`

字段：

- `id bigint not null auto_increment`
- `category_id bigint not null`
- `name varchar(128) not null`
- `description varchar(512) default null`
- `priority int not null`
- `status varchar(32) not null`

约束与索引：

- `primary key (id)`
- `unique key uk_knowledge_tag_category_category_id (category_id)`
- `unique key uk_knowledge_tag_category_name (name)`
- `unique key uk_knowledge_tag_category_priority (priority)`
- `key idx_knowledge_tag_category_status (status)`

枚举值：

- `status`: `ENABLED`, `DISABLED`

#### 2. `knowledge_tag`

字段：

- `id bigint not null auto_increment`
- `tag_id bigint not null`
- `name varchar(128) not null`
- `category_id bigint default null`
- `description varchar(1024) default null`
- `status varchar(32) not null`
- `source varchar(32) not null`
- `review_status varchar(32) not null`
- `review_note varchar(512) default null`
- `created_at datetime(3) not null`
- `reviewed_at datetime(3) default null`

约束与索引：

- `primary key (id)`
- `unique key uk_knowledge_tag_tag_id (tag_id)`
- `unique key uk_knowledge_tag_name (name)`
- `key idx_knowledge_tag_category_status (category_id, status)`
- `key idx_knowledge_tag_review_status (review_status, created_at)`
- `key idx_knowledge_tag_source_status (source, status)`

枚举值：

- `status`: `ENABLED`, `DISABLED`
- `source`: `MANUAL`, `AI_EXTRACTED`
- `review_status`: `PENDING`, `APPROVED`, `REJECTED`

#### 3. `knowledge_tag_alias`

字段：

- `id bigint not null auto_increment`
- `alias_id bigint not null`
- `tag_id bigint not null`
- `name varchar(128) not null`
- `source varchar(32) not null`

约束与索引：

- `primary key (id)`
- `unique key uk_knowledge_tag_alias_alias_id (alias_id)`
- `unique key uk_knowledge_tag_alias_name (name)`
- `key idx_knowledge_tag_alias_tag_id (tag_id)`

枚举值：

- `source`: `MANUAL`, `AI_EXTRACTED`

#### 4. `knowledge_tag_content_ref`

字段：

- `id bigint not null auto_increment`
- `ref_id bigint not null`
- `tag_id bigint not null`
- `content_type varchar(32) not null`
- `content_id bigint not null`
- `content_title varchar(255) not null`
- `source varchar(32) not null`

约束与索引：

- `primary key (id)`
- `unique key uk_knowledge_tag_content_ref_ref_id (ref_id)`
- `unique key uk_knowledge_tag_content_ref_unique (tag_id, content_type, content_id)`
- `key idx_knowledge_tag_content_ref_tag_id (tag_id)`
- `key idx_knowledge_tag_content_ref_content (content_type, content_id)`

枚举值：

- `content_type`: `SANCAI_ENTRY`, `WANGQI_DOCUMENT`, `MING_CUSTOM`
- `source`: `MANUAL`, `AI_EXTRACTED`

#### 5. `knowledge_synonym`

字段：

- `id bigint not null auto_increment`
- `synonym_id bigint not null`
- `term varchar(128) not null`
- `synonym varchar(128) not null`
- `status varchar(32) not null`

约束与索引：

- `primary key (id)`
- `unique key uk_knowledge_synonym_synonym_id (synonym_id)`
- `unique key uk_knowledge_synonym_pair (term, synonym)`
- `key idx_knowledge_synonym_term_status (term, status)`
- `key idx_knowledge_synonym_synonym_status (synonym, status)`

枚举值：

- `status`: `ENABLED`, `DISABLED`

### Seed Data

`db/data/knowledge.sql` 只保留以下初始化数据：

1. 4 条标签分类：`人物`、`地点`、`时代`、`主题`。
2. 每条标签分类必须写死 `category_id` 与 `priority`。
3. 不初始化标签。
4. 不初始化别名。
5. 不初始化内容关联。
6. 不初始化同义词。

## Backend File Plan

### Step 1. 收紧 Knowledge SQL

必须修改以下文件：

- `db/schema/knowledge.sql`
- `db/data/knowledge.sql`

执行要求：

1. 删除现有 `knowledge_category`，改为 `knowledge_tag_category`。
2. 删除现有 `knowledge_content_tag_relation`，改为 `knowledge_tag_content_ref`。
3. 删除所有精修、图谱、世系图相关 DDL。
4. 所有排序字段统一改为 `priority`。
5. 所有布尔状态字段统一改为 `status varchar(32)`。
6. 文件结尾不得保留无关注释块。

### Step 2. 创建 Domain 层 taxonomy 子域

必须创建以下文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/entity/TagCategory.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/entity/Tag.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/entity/TagAlias.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/entity/TagContentRef.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/entity/Synonym.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/enums/TagCategoryStatus.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/enums/TagStatus.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/enums/TagSource.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/enums/TagReviewStatus.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/enums/ContentType.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/enums/SynonymStatus.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/valueobject/TagCategoryId.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/valueobject/TagId.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/valueobject/TagAliasId.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/valueobject/TagContentRefId.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/model/valueobject/SynonymId.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/codec/TagCategoryIdCodec.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/codec/TagIdCodec.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/codec/TagAliasIdCodec.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/codec/TagContentRefIdCodec.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/codec/SynonymIdCodec.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/TagCategoryRepository.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/TagRepository.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/TagAliasRepository.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/TagContentRefRepository.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/SynonymRepository.java`

实体字段必须与表字段一一对应，不允许省略：

- `TagCategory`: `id`, `categoryId`, `name`, `description`, `priority`, `status`
- `Tag`: `id`, `tagId`, `name`, `categoryId`, `description`, `status`, `source`, `reviewStatus`, `reviewNote`, `createdAt`, `reviewedAt`
- `TagAlias`: `id`, `aliasId`, `tagId`, `name`, `source`
- `TagContentRef`: `id`, `refId`, `tagId`, `contentType`, `contentId`, `contentTitle`, `source`
- `Synonym`: `id`, `synonymId`, `term`, `synonym`, `status`

### Step 3. 创建 Application 层 taxonomy 服务

必须创建以下文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCategoryCreateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCategoryUpdateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCategoryStatusCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCreateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagUpdateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagStatusCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagReviewCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagAliasCreateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagAliasRemoveCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/SynonymCreateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/SynonymUpdateCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/SynonymStatusCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/SynonymRemoveCommand.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagCategoryPageQuery.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagPageQuery.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/TagReviewPageQuery.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/query/SynonymPageQuery.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/TagCategoryResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/TagResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/TagDetailResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/TagAliasResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/TagContentRefResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/SynonymResult.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/assembler/TaxonomyApplicationAssembler.java`

`TaxonomyApplicationService` 必须提供以下方法：

- `pageCategories`
- `createCategory`
- `updateCategory`
- `changeCategoryStatus`
- `pageTags`
- `getTagDetail`
- `createTag`
- `updateTag`
- `changeTagStatus`
- `pagePendingTags`
- `reviewTag`
- `listTagAliases`
- `createTagAlias`
- `removeTagAlias`
- `pageSynonyms`
- `createSynonym`
- `updateSynonym`
- `changeSynonymStatus`
- `removeSynonym`

固定业务规则：

1. `createTag` 创建的标签默认 `source=MANUAL`、`reviewStatus=APPROVED`。
2. 待审核列表只返回 `source=AI_EXTRACTED` 且 `reviewStatus=PENDING` 的标签。
3. `reviewTag` 的 `decision` 只允许 `APPROVE` 或 `REJECT`。
4. 审核通过时必须要求 `categoryId` 非空。
5. 审核拒绝时必须写入 `reviewNote`。
6. 禁用分类前必须校验该分类下不存在 `status=ENABLED` 的标签。
7. 删除别名只允许物理删除 `knowledge_tag_alias` 记录。
8. 删除同义词只允许物理删除 `knowledge_synonym` 记录。

### Step 4. 创建 Infra 层 taxonomy 持久化

必须创建以下文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/dataobject/TagCategoryDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/dataobject/TagDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/dataobject/TagAliasDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/dataobject/TagContentRefDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/dataobject/SynonymDO.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/assembler/TaxonomyPersistenceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/mapper/TagCategoryMapper.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/mapper/TagMapper.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/mapper/TagAliasMapper.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/mapper/TagContentRefMapper.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/mapper/SynonymMapper.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/TagCategoryRepositoryImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/TagRepositoryImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/TagAliasRepositoryImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/TagContentRefRepositoryImpl.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/SynonymRepositoryImpl.java`

Mapper 最少必须支持以下查询：

- 分类分页查询
- 标签分页查询
- 待审核标签分页查询
- 按 `tagId` 查询标签详情
- 按 `tagId` 查询别名列表
- 按 `tagId` 统计内容关联数量
- 同义词分页查询
- 名称唯一性查询
- 同义词唯一性查询

### Step 5. 创建 Admin Interface 层 taxonomy API

必须创建以下文件：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyController.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCategoryPageRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCategoryCreateRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCategoryUpdateRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCategoryStatusRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagPageRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCreateRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagUpdateRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagStatusRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagDetailRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagReviewPageRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagReviewRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagAliasCreateRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagAliasRemoveRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/SynonymPageRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/SynonymCreateRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/SynonymUpdateRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/SynonymStatusRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/SynonymRemoveRequest.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagCategoryResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagDetailResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagAliasResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagContentRefResponse.java`
- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/SynonymResponse.java`

`KnowledgeTaxonomyController` 必须提供以下 POST 接口：

- `/api/knowledge/taxonomy/category/page`
- `/api/knowledge/taxonomy/category/create`
- `/api/knowledge/taxonomy/category/update`
- `/api/knowledge/taxonomy/category/status`
- `/api/knowledge/taxonomy/tag/page`
- `/api/knowledge/taxonomy/tag/detail`
- `/api/knowledge/taxonomy/tag/create`
- `/api/knowledge/taxonomy/tag/update`
- `/api/knowledge/taxonomy/tag/status`
- `/api/knowledge/taxonomy/tag/review/page`
- `/api/knowledge/taxonomy/tag/review`
- `/api/knowledge/taxonomy/tag/alias/list`
- `/api/knowledge/taxonomy/tag/alias/create`
- `/api/knowledge/taxonomy/tag/alias/remove`
- `/api/knowledge/taxonomy/synonym/page`
- `/api/knowledge/taxonomy/synonym/create`
- `/api/knowledge/taxonomy/synonym/update`
- `/api/knowledge/taxonomy/synonym/status`
- `/api/knowledge/taxonomy/synonym/remove`

权限码固定如下：

- `knowledge:taxonomy:view`
- `knowledge:taxonomy:edit`
- `knowledge:taxonomy:review`

### Step 6. 创建 Admin 前端 taxonomy 页面

必须创建以下文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.css`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-types.ts`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/category-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/category-edit.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-edit.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-review-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-detail-drawer.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-alias-list.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/synonym-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/synonym-edit.tsx`

必须修改以下文件：

- `kuzhambu-apps/admin-web/src/router/index.tsx`

页面固定为单路由：

- 路径：`/knowledge/taxonomy`
- 页面名：`TaxonomyPage`

页面固定布局：

1. 顶部 `Tabs`，固定 4 个标签页：`标签分类`、`统一标签`、`待审核标签`、`同义词`。
2. `标签分类` 页签：左上新增按钮，主体表格。
3. `统一标签` 页签：左上新增按钮，主体表格；点击行打开详情抽屉。
4. `待审核标签` 页签：主体表格；每行有通过、拒绝按钮。
5. `同义词` 页签：左上新增按钮，主体表格。

`taxonomy-service.ts` 必须定义以下方法：

- `pageCategories`
- `createCategory`
- `updateCategory`
- `changeCategoryStatus`
- `pageTags`
- `getTagDetail`
- `createTag`
- `updateTag`
- `changeTagStatus`
- `pagePendingTags`
- `reviewTag`
- `listTagAliases`
- `createTagAlias`
- `removeTagAlias`
- `pageSynonyms`
- `createSynonym`
- `updateSynonym`
- `changeSynonymStatus`
- `removeSynonym`

`taxonomy-types.ts` 必须定义以下类型：

- `TagCategoryRecord`
- `TagRecord`
- `TagDetailRecord`
- `TagAliasRecord`
- `TagContentRefRecord`
- `SynonymRecord`
- `TagCategoryPageQuery`
- `TagPageQuery`
- `TagReviewPageQuery`
- `SynonymPageQuery`

### Step 7. 增加 Admin 菜单与权限种子数据

必须修改以下文件：

- `db/data-source/system.json`
- `db/data/system.sql`

在 `system.json` 中新增一个一级菜单和一个二级菜单：

1. 一级菜单：
- `name`: `知识治理`
- `url`: `/knowledge`
- `icon`: `book`
- `perms`: `["knowledge"]`

2. 二级菜单：
- `name`: `标签与同义词`
- `url`: `/knowledge/taxonomy`
- `icon`: `book`
- `perms`: `["knowledge:taxonomy:view", "knowledge:taxonomy:edit", "knowledge:taxonomy:review"]`

要求：

1. 菜单层级、字段命名和 JSON 结构必须完全复用现有 `classics`、`storage` 段落格式。
2. 更新 `db/data/system.sql` 时，先改 `db/data-source/system.json`，再运行现有生成脚本生成 SQL，不允许手写 `db/data/system.sql`。

## API Response Contract

### TagCategoryResponse

字段：

- `id: string`
- `name: string`
- `description?: string`
- `priority: number`
- `status: string`

### TagResponse

字段：

- `id: string`
- `name: string`
- `categoryId?: string`
- `categoryName?: string`
- `description?: string`
- `status: string`
- `source: string`
- `reviewStatus: string`
- `contentRefCount: number`
- `createdAt: number`
- `reviewedAt?: number`

### TagDetailResponse

字段：

- `tag: TagResponse`
- `aliases: TagAliasResponse[]`
- `contentRefs: TagContentRefResponse[]`

### TagAliasResponse

字段：

- `id: string`
- `name: string`
- `source: string`

### TagContentRefResponse

字段：

- `id: string`
- `contentType: string`
- `contentId: string`
- `contentTitle: string`
- `source: string`

### SynonymResponse

字段：

- `id: string`
- `term: string`
- `synonym: string`
- `status: string`

## Validation Rules

1. 标签分类名唯一。
2. 标签名全局唯一。
3. 标签别名名全局唯一。
4. 同义词 `(term, synonym)` 组合唯一。
5. `term` 不允许等于 `synonym`。
6. 审核通过时，标签分类必须存在且状态为 `ENABLED`。
7. 创建或更新标签时，若填写 `categoryId`，该分类必须存在。
8. 标签详情中的内容引用只读，不提供编辑接口。

## Delivery Check

实现完成后必须满足以下结果：

1. 后台出现 `知识治理 / 标签与同义词` 菜单。
2. 访问 `/knowledge/taxonomy` 能正常渲染四个页签。
3. 标签分类、统一标签、待审核标签、同义词四个列表都能返回分页数据。
4. 标签详情抽屉能展示别名列表和内容引用列表。
5. `db/schema/knowledge.sql` 中不存在图谱、精修、世系图相关表。

## Forbidden Shortcuts

1. 不允许把 taxonomy 代码写进 `knowledge` 根 package；必须放进 `taxonomy` 子域。
2. 不允许把前端所有逻辑堆进 `taxonomy-page.tsx`；必须拆到指定组件文件。
3. 不允许新增 Portal 路由、Portal 页面或 Portal API。
4. 不允许保留旧的 `knowledge_category`、`knowledge_content_tag_relation` 表名。
5. 不允许使用 `sort_order`、`enabled` 作为最终字段名。
