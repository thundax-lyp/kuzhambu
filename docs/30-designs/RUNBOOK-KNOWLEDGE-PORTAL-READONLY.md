# Knowledge Portal Read-Only Runbook

## Purpose

本文档用于实现 `Knowledge` 的 `Portal` 只读消费入口，目标是把现有 Knowledge 后端能力转译成面向用户浏览的内容展示闭环。

本轮只做 `Portal`，不做 `Admin`。两边只共享业务语义与数据契约，不共享同一套页面组件系统。

## Execution Rules

- RUNBOOK 必须清晰、准确、无歧义。
- 所有大任务必须拆成单次只涉及 `2-6` 个文件的小任务。
- 每个小任务都必须写清：
  - 目标
  - 文件范围
  - 数据结构或接口定义
  - 验收标准
- Portal 页面不得复用 Admin 页面组件。
- 若设计方向变化，应先改 RUNBOOK，再动代码。

## Scope

覆盖：

- `kuzhambu-servers/biz/knowledge/` 的 Portal 只读 application / interface 契约。
- `kuzhambu-apps/portal-web/` 的知识门户首页、浏览页、质量页。
- 首页对 `Knowledge` 入口的导航接入。
- 效果图归档与最终清理。

不覆盖：

- Admin Knowledge 页面重构。
- 后端大规模读模型重做。
- 图谱复杂交互引擎引入。
- 搜索 / 问答主链路再次改造。

## Visual Direction

本轮视觉方向是“古籍数字图谱 / 博物馆档案”：

- 不是普通 SaaS 仪表盘。
- 不是治理台，也不是后台编辑器。
- 关键词是 `阅读`、`浏览`、`追溯`、`沉浸`、`可视化`。

建议的设计语言：

- 底色：象牙纸、米白纸张、轻微纤维纹理。
- 主色：沉稳墨绿、青玉绿、朱砂红。
- 视觉元素：印章、卷轴、卡片边框、浅阴影、纸面层次。
- 字体气质：标题更像展陈说明，正文更像导览读物。

## Reference Mockups

效果图必须放在仓库内，和 RUNBOOK 一起管理。本轮统一放在：

- `docs/30-designs/assets/knowledge-portal-readonly/`

当前三张效果图：

- 首页总览：`docs/30-designs/assets/knowledge-portal-readonly/ig_055afcfc5cab9cd9016a3cc892c92c8190941bd9f2c4b60b2f.png`
- 浏览详情页：`docs/30-designs/assets/knowledge-portal-readonly/ig_055afcfc5cab9cd9016a3cc8e15b2c819095a3ef27452ff074.png`
- 质量总览页：`docs/30-designs/assets/knowledge-portal-readonly/ig_055afcfc5cab9cd9016a3cc935ec0c8190862fdf05abde15a2.png`

## Target Experience

### Portal 首页

首页承担“入口分发”职责。

页面目标：

- 让用户一眼知道这里能看什么。
- 提供统一搜索入口。
- 提供三个主入口：图谱浏览、质量总览、来源追溯。
- 提供当前知识资产概览与最近更新。

建议组件组合：

- 顶部 `Breadcrumb + Header`
- 中央 `Hero`
- `Input.Search`
- `Button`
- `Card` 统计卡
- `Card` 快捷入口
- `Card` 最近更新列表
- 首页导览栏

### Portal 浏览页

浏览页承担“查看单个知识对象及其关系网络”的职责。

页面目标：

- 用户能从实体进入关联关系。
- 用户能看见来源、标签、时间线、关联内容。
- 用户能在不离开页面的情况下切换不同视图。

建议组件组合：

- 左侧筛选栏：知识库、类型、标签、时间、来源
- 中央主画布：图谱关系视图
- 右侧信息栏：实体详情、来源引用、相关标签、关联时间线
- 顶部操作条：收藏、分享、跳转来源、切换视图
- 视图切换：`Tabs` 或 `Segmented`

### Portal 质量页

质量页承担“理解当前知识状态”的职责。

页面目标：

- 把覆盖率、置信度、来源分布、待关注项讲清楚。
- 提供趋势、分布、问题清单、来源明细。
- 让用户从“结果”进入“问题来源”。

建议组件组合：

- KPI 指标条
- 趋势折线图区域
- 分布条形图区域
- 质量关注事项列表
- 来源明细表格
- 右上角时间筛选与导出按钮

## Component Boundary

### Portal

Portal 只使用 `kuzhambu-apps/portal-web` 内部组件系统：

- `src/components/ui/*`
- 页面级自定义组件
- 页面级 CSS

Portal 不复用 Admin 的页面组件，不直接引入 Admin 的表单、表格或抽屉实现。

### Admin

Admin 保持原有治理风格，不参与本次 Portal 只读页面实现。

## Data Contracts

本轮 Portal 不是纯前端 mock，最终需要由 `Knowledge` 后端提供只读契约。Portal 只能消费这些契约，不得自行拼接 Admin 数据。

### Home Contract

- `GET /portal/knowledge/home`
- 目标：返回首页展示所需的概览与入口信息。
- 字段：
  - `heroTitle`
  - `heroSubtitle`
  - `searchPlaceholder`
  - `stats[]`
  - `quickLinks[]`
  - `recentUpdates[]`
  - `featureCollections[]`

`stats[]`：

- `key`
- `label`
- `value`
- `deltaText`
- `trend`
- `icon`

`quickLinks[]`：

- `key`
- `label`
- `description`
- `href`
- `type`

`recentUpdates[]`：

- `title`
- `subtitle`
- `summary`
- `updatedAt`
- `href`
- `coverImageUrl`

### Atlas Contract

- `GET /portal/knowledge/atlas`
- 目标：返回图谱浏览页的初始数据与过滤条件。
- 查询参数：
  - `focusId`
  - `focusType`
  - `knowledgeBase`
  - `keyword`
  - `tag`
  - `timeRange`
- 字段：
  - `focusNode`
  - `relationGroups[]`
  - `sourceReferences[]`
  - `relatedTags[]`
  - `timelineItems[]`
  - `availableFilters`

`focusNode`：

- `id`
- `title`
- `type`
- `summary`
- `status`
- `confidence`
- `coverImageUrl`

`relationGroups[]`：

- `groupKey`
- `groupLabel`
- `relations[]`

`relations[]`：

- `sourceId`
- `sourceLabel`
- `relationLabel`
- `targetId`
- `targetLabel`
- `relationType`
- `weight`

`sourceReferences[]`：

- `sourceId`
- `sourceTitle`
- `sourceType`
- `snippet`
- `updatedAt`
- `href`

`relatedTags[]`：

- `tagId`
- `tagName`
- `tagCategory`
- `score`

`timelineItems[]`：

- `timeLabel`
- `title`
- `description`
- `href`

### Quality Contract

- `GET /portal/knowledge/quality`
- 目标：返回质量总览页的指标、趋势、分布和关注事项。
- 查询参数：
  - `date`
  - `range`
  - `knowledgeBase`
- 字段：
  - `qualityStats[]`
  - `trendSeries[]`
  - `distributionRows[]`
  - `attentionItems[]`
  - `sourceRows[]`
  - `updatedAt`

`qualityStats[]`：

- `key`
- `label`
- `value`
- `deltaText`
- `trend`
- `severity`

`trendSeries[]`：

- `metricKey`
- `metricLabel`
- `points[]`

`distributionRows[]`：

- `label`
- `value`
- `percentage`
- `count`

`attentionItems[]`：

- `severity`
- `title`
- `description`
- `updatedAt`
- `href`

`sourceRows[]`：

- `sourceName`
- `sourceType`
- `confidence`
- `coverage`
- `issueCount`
- `updatedAt`
- `href`

## File-by-File Workboard

### Stage A. 后端只读契约

#### Task A1. 首页 read model

- 目标：定义首页只读结果与服务骨架。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/model/result/KnowledgePortalHomeResult.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/service/KnowledgePortalReadApplicationService.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/service/impl/KnowledgePortalReadApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/service/impl/KnowledgePortalReadApplicationServiceImplTest.java`
- 数据结构：
  - `KnowledgePortalHomeResult`
- 验收：
  - Service 能返回首页只读结果。
  - 首页结果不依赖 Admin 写模型。

#### Task A2. 首页 API

- 目标：把首页只读结果暴露成稳定 Portal API。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/controller/KnowledgePortalHomeController.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/assembler/KnowledgePortalHomeInterfaceAssembler.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/controller/request/KnowledgePortalHomeQuery.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/controller/response/KnowledgePortalHomeResponse.java`
- 接口：
  - `GET /portal/knowledge/home`
- 验收：
  - 路径稳定。
  - 返回值只包含 Portal 需要的 read DTO。

#### Task A3. 浏览页 read model

- 目标：定义浏览页只读结果与服务骨架。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/model/result/KnowledgePortalAtlasResult.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/service/KnowledgePortalReadApplicationService.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/service/impl/KnowledgePortalReadApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/service/impl/KnowledgePortalReadApplicationServiceImplTest.java`
- 数据结构：
  - `KnowledgePortalAtlasResult`
- 验收：
  - Service 能返回浏览页只读结果。
  - 浏览页结果可由单一入口查询。

#### Task A4. 浏览页 API

- 目标：把浏览页只读结果暴露成稳定 Portal API。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/KnowledgePortalAtlasController.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/assembler/KnowledgePortalAtlasInterfaceAssembler.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/request/KnowledgePortalAtlasQuery.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/response/KnowledgePortalAtlasResponse.java`
- 接口：
  - `GET /portal/knowledge/atlas`
- 验收：
  - 路径稳定。
  - 查询参数与 Portal 页面筛选一致。

#### Task A5. 质量页 read model

- 目标：定义质量页只读结果与服务骨架。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/model/result/KnowledgePortalQualityResult.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/service/KnowledgePortalReadApplicationService.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/service/impl/KnowledgePortalReadApplicationServiceImpl.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/service/impl/KnowledgePortalReadApplicationServiceImplTest.java`
- 数据结构：
  - `KnowledgePortalQualityResult`
- 验收：
  - Service 能返回质量页只读结果。
  - 质量页结果可被页面直接消费。

#### Task A6. 质量页 API

- 目标：把质量页只读结果暴露成稳定 Portal API。
- 文件范围：
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/controller/KnowledgePortalQualityController.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/assembler/KnowledgePortalQualityInterfaceAssembler.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/controller/request/KnowledgePortalQualityQuery.java`
  - `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/controller/response/KnowledgePortalQualityResponse.java`
- 接口：
  - `GET /portal/knowledge/quality`
- 验收：
  - 路径稳定。
  - 查询参数与 Portal 页面筛选一致。

### Stage B. Portal 页面骨架

#### Task B1. 门户入口与首页跳转

- 目标：把 Knowledge 入口挂到现有 Portal 首页，并接入 `/knowledge` 路由。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/app.tsx`
  - `kuzhambu-apps/portal-web/src/pages/home/home-page.tsx`
  - `kuzhambu-apps/portal-web/src/pages/home/home-page.test.tsx`
- 验收：
  - Portal 首页能跳到 Knowledge 首页。
  - `/knowledge` 路由可打开知识门户首页。

#### Task B2. 首页页面骨架

- 目标：实现首页页面骨架和首屏布局。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-page.tsx`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-page.css`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-page.test.tsx`
- 数据结构：
  - `headline`
  - `summary`
  - `searchTerm`
  - `statCards`
  - `quickLinks`
  - `recentUpdates`
- 验收：
  - 首页首屏只保留一条主标题与一个搜索入口。
  - 下方可见统计卡与入口卡。

#### Task B3. 首页数据适配

- 目标：把首页内容抽成页面级服务与类型。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-service.ts`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-types.ts`
- 验收：
  - 首页样式不依赖硬编码字符串。
  - 后续可平滑接入 `/portal/knowledge/home`。

#### Task B4. 浏览页页面骨架

- 目标：完成浏览页主框架与关系画布区域。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.css`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.test.tsx`
- 数据结构：
  - `focusNode`
  - `relationGroups`
  - `sourceReferences`
  - `relatedTags`
  - `timelineItems`
- 验收：
  - 左中右三栏布局成立。
  - 页面能表达“筛选 - 浏览 - 详情”三层关系。

#### Task B5. 浏览页数据适配

- 目标：把浏览页内容抽成页面级服务与类型。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-service.ts`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-types.ts`
- 验收：
  - 页面内容可先由静态数据适配。
  - 后续可平滑接入 `/portal/knowledge/atlas`。

#### Task B6. 质量页页面骨架

- 目标：完成质量页主框架与指标展示区域。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-quality-page.tsx`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-quality-page.css`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-quality-page.test.tsx`
- 数据结构：
  - `qualityStats`
  - `trendSeries`
  - `distributionRows`
  - `attentionItems`
  - `sourceRows`
- 验收：
  - 页面能表达覆盖率、置信度、来源总数、待处理项。
  - 质量趋势和来源列表都有明确视觉层次。

#### Task B7. 质量页数据适配

- 目标：把质量页内容抽成页面级服务与类型。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-quality-service.ts`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-quality-types.ts`
- 验收：
  - 页面内容可先由静态数据适配。
  - 后续可平滑接入 `/portal/knowledge/quality`。

#### Task B8. 共享样式基线

- 目标：建立知识门户专属视觉基线，避免和 Discovery 页面混用。
- 文件范围：
  - `kuzhambu-apps/portal-web/src/styles.css`
  - `kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-page.css`
- 验收：
  - 页面背景、卡片、边框、阴影与 Discovery 保持明显区分。
  - 知识门户整体气质更偏博物馆导览。

### Stage C. 验证与清理

#### Task C1. Portal 验证

- 目标：完成 Portal 前端验证。
- 文件范围：
  - 相关页面测试文件
  - `kuzhambu-apps/portal-web/src/styles.css`
  - 其余本轮新增的知识门户文件
- 验收：
  - `npm run format:check`
  - `npm run lint`
  - `npm run test`
  - `npm run build`

#### Task C2. 清理现场

- 目标：任务结束时清理阶段性文档和效果图。
- 文件范围：
  - `docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-READONLY.md`
  - `docs/30-designs/assets/knowledge-portal-readonly/*`
- 验收：
  - RUNBOOK 删除或归档。
  - 效果图和 RUNBOOK 一起清除，不残留项目外或项目内孤立样稿。

## Acceptance Criteria

- Portal 与 Admin 的组件系统边界清晰，不混用。
- 首页、浏览页、质量页都能形成独立可识别的视觉层次。
- 页面布局与三张效果图一致，至少在信息结构上对齐。
- 每个阶段都能独立验证，不要求一次性铺完整站。

## Cleanup

- RUNBOOK 仅作为本轮执行手册，完成后应删除或归档。
- 效果图与 RUNBOOK 同生命周期管理。
- 不保留项目外样稿引用。
