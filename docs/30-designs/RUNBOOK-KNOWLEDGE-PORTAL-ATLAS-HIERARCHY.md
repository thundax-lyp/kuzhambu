# RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY

## 1. 任务目标

本轮目标是把 `Knowledge Portal` 的图谱浏览从“展示型读页面”推进到“分层真闭环”。

本轮必须交付：

- `Portal` 图谱浏览支持 `overview -> category -> detail` 三层真实切换。
- `overview` 层展示门类卡片、门类统计和入口跳转。
- `category` 层展示单门类下的版本、实体、关系和来源摘要。
- `detail` 层展示单实体详情、关联关系、来源条目和时间线。
- 三层之间必须支持面包屑导航和 URL 可恢复状态。
- 后端必须提供稳定只读契约，不允许页面继续依赖“固定 fallback 假数据结构”模拟三层。

本轮明确不做：

- 图谱批量生成和重生成。
- 从质量报告直接触发提取。
- 世系图可视化专页。
- Admin Web 图谱治理页改造。
- 新增临时效果图资产。

## 2. 当前基线

当前仓库已经具备：

- 后端 `KnowledgePortalReadApplicationServiceImpl`，已提供 `getHome()`、`getAtlas()`、`getQuality()`。
- 前端 `portal-web` 已有：
  - `/knowledge`
  - `/knowledge/atlas`
  - `/knowledge/quality`
- 当前 `/knowledge/atlas` 仍是单页展示骨架，未形成真正的三层浏览。
- 当前 `KnowledgePortalAtlasQuery` 已有 `focusId`、`focusType`、`knowledgeBase`、`keyword`、`tag`、`timeRange`，但未明确“浏览层级”语义。
- 当前 `KnowledgePortalAtlasResponse` 仍是以 `focusNode + relationGroups + detail snippets` 为中心，无法稳定表达 `overview / category / detail` 三种读模型。

## 3. 本轮约束

- `Portal` 和 `Admin` 不是同一套组件系统，不共享页面组件实现。
- 本轮优先复用现有 `knowledge graph` 正式事实表与读仓储，不新增数据库 schema。
- 如现有仓储读取能力不足，只允许补充只读 repository 方法，不允许跨域绕过 `repository` 直读 `mapper`。
- 每个执行任务必须控制在 `2-6` 个文件。
- 任务关闭时必须清理本 RUNBOOK。

## 4. 目标浏览模型

### 4.1 URL 语义

本轮统一使用同一路由：

- `/knowledge/atlas?level=overview`
- `/knowledge/atlas?level=category&categoryCode=<code>`
- `/knowledge/atlas?level=detail&entityId=<id>`

补充规则：

- 未传 `level` 时，前端默认跳到 `overview`。
- `categoryCode` 非法或无数据时，后端回退到 `overview`，并返回空 `categoryView`。
- `entityId` 非法或无数据时，后端回退到 `category` 或 `overview`，不返回 500。

### 4.2 后端读模型

`KnowledgePortalAtlasResult` / `KnowledgePortalAtlasResponse` 本轮调整为以下结构：

- `currentLevel`
- `breadcrumbItems`
- `overviewView`
- `categoryView`
- `detailView`
- `availableFilters`

新增嵌套结构：

- `BreadcrumbItem`
  - `level`
  - `label`
  - `href`
- `OverviewView`
  - `summaryTitle`
  - `summarySubtitle`
  - `categoryCards`
- `OverviewCategoryCard`
  - `categoryCode`
  - `categoryName`
  - `entityCount`
  - `relationCount`
  - `appliedVersionCount`
  - `latestVersionNo`
  - `entryHref`
- `CategoryView`
  - `categoryCode`
  - `categoryName`
  - `latestVersionId`
  - `latestVersionNo`
  - `entityHighlights`
  - `relationGroups`
  - `sourceReferences`
- `CategoryEntityHighlight`
  - `entityId`
  - `entityName`
  - `entityType`
  - `confirmationStatus`
  - `entryHref`
- `DetailView`
  - `focusNode`
  - `relationGroups`
  - `sourceReferences`
  - `timelineItems`
  - `relatedTags`

### 4.3 前端页面状态

`knowledge-atlas-types.ts` 本轮同步调整为：

- `KnowledgeAtlasLevel = "overview" | "category" | "detail"`
- `KnowledgeAtlasBreadcrumbItem`
- `KnowledgeAtlasOverviewView`
- `KnowledgeAtlasCategoryView`
- `KnowledgeAtlasDetailView`

页面状态规则：

- 页面启动时从 URL 读取 `level/categoryCode/entityId`
- 页面交互只修改 URL，不单独维护一套脱离 URL 的浏览状态
- `useQuery` 的 `queryKey` 必须包含 `level/categoryCode/entityId`

## 5. 数据来源判断

本轮优先使用：

- `GraphVersionRepository`
- `KnowledgeEntityRepository`
- `KnowledgeRelationRepository`

当前可直接复用字段：

- `GraphVersion.sourceCategoryCode`
- `GraphVersion.sourceCategoryName`
- `GraphVersion.versionId`
- `GraphVersion.versionNo`
- `KnowledgeEntity.entityId`
- `KnowledgeEntity.entityKey`
- `KnowledgeEntity.name`
- `KnowledgeEntity.entityType`
- `KnowledgeEntity.confirmationStatus`
- `KnowledgeEntity.sourceRefsJson`
- `KnowledgeRelation.sourceEntityKey`
- `KnowledgeRelation.targetEntityKey`
- `KnowledgeRelation.relationType`
- `KnowledgeRelation.sourceRefsJson`

本轮允许新增 repository 读口径，但不新增表：

- `GraphVersionRepository`：按 `sourceCategoryCode` 读取最近已应用版本或版本列表。
- `KnowledgeEntityRepository`：按 `entityKey` / `entityId` 读取单实体所需详情辅助数据。
- `KnowledgeRelationRepository`：按 `entityKey` 聚合与焦点实体相关的关系。

## 6. 大任务拆分规则

- 每个小任务只覆盖一个主动作。
- 每个小任务只涉及 `2-6` 个文件。
- 每个小任务都必须能独立提交。
- 一个小任务完成时，必须能删除对应 `TODO` 项。

## 7. 逐文件施工表

### Stage A. 冻结 Atlas 三层契约

#### Task A1. 应用层 Atlas 查询语义定型

- 目标：让 application 层明确接受三层浏览语义，而不是无层级的通用 atlas 读取。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalAtlasQuery.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationService.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`
- 数据结构变更：
  - 新增 application query：`level`、`categoryCode`、`entityId`、`knowledgeBase`、`keyword`、`tag`、`timeRange`
  - `getAtlas()` 改为 `getAtlas(KnowledgePortalAtlasQuery query)`
- 验收条件：
  - application 层不再依赖“无参数 atlas 读取”
  - 后续接口层可以稳定装配三层查询

#### Task A2. 应用层 Atlas 结果模型改为三层结构

- 目标：把 `KnowledgePortalAtlasResult` 从单焦点结构调整为 `overview/category/detail` 三层只读模型。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalAtlasResult.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImplTest.java`
- 数据结构变更：
  - 新增 `currentLevel`
  - 新增 `breadcrumbItems`
  - 新增 `overviewView`
  - 新增 `categoryView`
  - 新增 `detailView`
- 验收条件：
  - 结果模型能无歧义表达三层页面
  - 单测可按三层断言返回结构

### Stage B. 补齐 Knowledge 图谱只读聚合能力

#### Task B1. GraphVersionRepository 增补门类层读能力

- 目标：支持 overview 和 category 层按门类读取版本与门类摘要。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/GraphVersionRepository.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/mapper/GraphVersionMapper.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/repository/impl/GraphVersionRepositoryImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/resources/mapper/knowledge/GraphVersionMapper.xml`
- 数据结构变更：
  - 新增按 `sourceCategoryCode` 读取最近已应用版本的方法
  - 新增读取已应用版本列表的方法，供 overview 做门类聚合
- 验收条件：
  - application 层可稳定拿到门类维度版本信息
  - 不新增 schema

#### Task B2. Entity / Relation 仓储增补 detail 聚合读能力

- 目标：支持按焦点实体聚合 detail 层关系和来源线索。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/KnowledgeEntityRepository.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/KnowledgeRelationRepository.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/repository/impl/KnowledgeEntityRepositoryImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/repository/impl/KnowledgeRelationRepositoryImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/resources/mapper/knowledge/KnowledgeEntityMapper.xml`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/resources/mapper/knowledge/KnowledgeRelationMapper.xml`
- 数据结构变更：
  - 新增按 `entityKey/entityId` 读取关系邻接信息的方法
  - 新增 detail 聚合所需只读查询
- 验收条件：
  - detail 层不再只能显示“第一个 focusNode”
  - 单实体相关关系可以完整读出

### Stage C. Application 聚合三层 atlas 读模型

#### Task C1. 实现 overview 聚合

- 目标：用正式图谱事实聚合出门类卡片，而不是前端自己拼假数据。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImplTest.java`
- 处理动作：
  - 聚合 `overviewView.summaryTitle`
  - 聚合 `overviewView.categoryCards`
  - 生成 `overview` 面包屑
- 验收条件：
  - `level=overview` 时返回 overview 数据块
  - category 卡片具备跳转 href

#### Task C2. 实现 category 聚合

- 目标：生成单门类层的版本、实体高亮、关系分组和来源摘要。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImplTest.java`
- 处理动作：
  - 按 `categoryCode` 找最近已应用版本
  - 生成 `categoryView`
  - 生成 `overview -> category` 面包屑
- 验收条件：
  - `level=category` 返回 category 数据块
  - category 层实体卡片具备进入 detail 的 href

#### Task C3. 实现 detail 聚合

- 目标：按 `entityId` 聚合单实体详情、相关关系、来源和时间线。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImplTest.java`
- 处理动作：
  - 读取 `focusNode`
  - 聚合 `detailView.relationGroups`
  - 聚合 `detailView.sourceReferences`
  - 聚合 `detailView.timelineItems`
  - 生成 `overview -> category -> detail` 面包屑
- 验收条件：
  - `level=detail` 返回 detail 数据块
  - detail 层实体信息、关系和来源条目都可检查

### Stage D. 接口层对外公布三层契约

#### Task D1. Portal atlas request/response 扩展

- 目标：让 HTTP 契约能真实表达三层 atlas 浏览。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/request/KnowledgePortalAtlasQuery.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/response/KnowledgePortalAtlasResponse.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/assembler/KnowledgePortalAtlasInterfaceAssembler.java`
- 接口定义变更：
  - query 新增 `level`、`categoryCode`、`entityId`
  - response 新增 `currentLevel`、`breadcrumbItems`、`overviewView`、`categoryView`、`detailView`
- 验收条件：
  - 前端无须猜测当前层级
  - assembler 能完整转换三层结果

#### Task D2. Portal atlas controller 与接口测试更新

- 目标：把三层 query 正式接到 controller，并锁定接口测试。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/KnowledgePortalAtlasController.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/KnowledgePortalAtlasControllerTest.java`
- 验收条件：
  - controller 把 query 装配到 application query
  - 接口测试至少覆盖 `overview/category/detail` 三种入口

### Stage E. portal-web 更新 atlas 类型与服务

#### Task E1. 前端 atlas 类型与服务同步三层契约

- 目标：让 `portal-web` 类型和 service 与后端三层契约对齐。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-types.ts`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-service.ts`
- 数据结构变更：
  - 新增三层类型
  - fallback 改为三层结构，不再是单 `focusNode` 模型
- 验收条件：
  - service fallback 与后端 response 同构
  - 页面可以只依赖 `types/service` 读三层状态

#### Task E2. 页面级 URL 状态解析

- 目标：把 atlas 页面状态切换建立在 URL 上。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.test.tsx`
- 处理动作：
  - 读取 `level/categoryCode/entityId`
  - `queryKey` 纳入 URL 状态
  - 测试覆盖三种入口 URL
- 验收条件：
  - 刷新后能恢复当前层级
  - 测试可验证 URL 驱动

### Stage F. Portal overview/category/detail 真页面

#### Task F1. overview 层页面实现

- 目标：把当前“关系舞台”页拆出 overview 首屏。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.css`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.test.tsx`
- 页面内容：
  - overview 标题区
  - 门类卡片区
  - 最近版本摘要
  - 进入 category 的导航
- 验收条件：
  - overview 不再直接渲染 detail 结构
  - 门类卡片点击可进入 category

#### Task F2. category 层页面实现

- 目标：把门类层真实渲染出来。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.css`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.test.tsx`
- 页面内容：
  - category 标题和版本信息
  - 实体高亮卡片
  - 关系分组带
  - 来源摘要
- 验收条件：
  - category 层视觉与 overview、detail 明确区分
  - 实体入口点击可进入 detail

#### Task F3. detail 层页面实现

- 目标：让 detail 层展示单实体完整阅读信息。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.css`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.test.tsx`
- 页面内容：
  - focus entity 摘要
  - 关联关系列表
  - 来源条目
  - 时间线
- 验收条件：
  - detail 层不再只是“焦点说明”占位文案
  - detail 层能阅读来源脉络

### Stage G. 面包屑与分层导航收口

#### Task G1. 前端面包屑导航接入

- 目标：用后端返回的 breadcrumb 驱动页面跳转。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.css`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.test.tsx`
- 验收条件：
  - overview/category/detail 均显示正确面包屑
  - 点击 breadcrumb 可返回上一层

### Stage H. 文档、验证与清场

#### Task H1. 覆盖文档同步

- 目标：把 Knowledge 覆盖文档从“Portal 页面未完成”改到当前实现状态。
- 文件范围：
  - `docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
  - `docs/40-readiness/PR-WORKFLOW.md`
- 验收条件：
  - `Portal 页面` 状态与实现一致
  - 若验证口径无变化，不改无关段落

#### Task H2. portal-web 验证

- 目标：完成本轮 `portal-web` 作用域验证。
- 文件范围：
  - 本轮变更的 `kuzhambu-apps/portal-web` 文件
- 验证命令：
  - `npm --workspace portal-web run format:check`
  - `npm --workspace portal-web run lint`
  - `npm --workspace portal-web run test`
  - `npm --workspace portal-web run build`
- 验收条件：
  - 四项全部通过

#### Task H3. RUNBOOK 清场

- 目标：任务完成后删除本 RUNBOOK。
- 文件范围：
  - `docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
- 验收条件：
  - PR 合并前 RUNBOOK 已删除或明确说明仍有剩余用途

## 8. 风险提示

- 当前正式事实表没有单独的“14 大门类统计表”，overview 层大概率需要通过 `GraphVersion + Entity + Relation` 运行时聚合完成，性能上要控制读取范围。
- `sourceRefsJson` 当前是字符串快照，本轮如果要展示来源条目摘要，必须先确认 application 层采用轻量解析还是沿用现有简化文案。
- 门类层如果发现 `sourceCategoryCode` 数据覆盖不足，不在本轮回补历史数据；本轮只做读层退化策略。

## 9. 本轮完成定义

满足以下条件即可视为“Knowledge 图谱浏览分层真闭环”完成：

- `/knowledge/atlas` 能稳定进入 `overview`。
- overview 能进入 category。
- category 能进入 detail。
- detail 能通过 breadcrumb 返回 category 和 overview。
- 三层均由后端正式读模型驱动。
- `portal-web` 验证通过。
- `TODO`、文档和 RUNBOOK 收口完成。
