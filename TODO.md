# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Knowledge portal atlas application query`：冻结 Atlas 三层查询语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalAtlasQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationService.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`
    - 处理动作：新增 application query 并让 `getAtlas()` 显式接收三层浏览参数
    - 验收点：application 层不再依赖无参数 atlas 读取
    - 重要度：9/10

- [ ] `Knowledge portal atlas result model`：改造 Atlas 三层结果结构
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalAtlasResult.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImplTest.java`
    - 处理动作：把 atlas result 调整为 `currentLevel + breadcrumb + overview/category/detail`
    - 验收点：结果模型能无歧义表达三层页面
    - 重要度：9/10

- [ ] `Knowledge graph version category read`：增补门类层版本读取仓储能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/GraphVersionRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/mapper/GraphVersionMapper.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/repository/impl/GraphVersionRepositoryImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/resources/mapper/knowledge/GraphVersionMapper.xml`
    - 处理动作：新增按 `sourceCategoryCode` 读取最近已应用版本和门类版本列表的方法
    - 验收点：overview 和 category 层可稳定读取门类版本信息
    - 重要度：8/10

- [ ] `Knowledge entity detail read`：增补焦点实体详情读取仓储能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/KnowledgeEntityRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/repository/impl/KnowledgeEntityRepositoryImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/resources/mapper/knowledge/KnowledgeEntityMapper.xml`
    - 处理动作：新增按 `entityId/entityKey` 读取 detail 层所需实体详情的只读方法
    - 验收点：detail 层可稳定读取单实体正式事实
    - 重要度：8/10

- [ ] `Knowledge relation adjacency read`：增补焦点实体关系邻接读取仓储能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/repository/KnowledgeRelationRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/repository/impl/KnowledgeRelationRepositoryImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/resources/mapper/knowledge/KnowledgeRelationMapper.xml`
    - 处理动作：新增按焦点实体聚合 detail 层相关关系的只读方法
    - 验收点：detail 层可完整读取单实体关联关系
    - 重要度：8/10

- [ ] `Knowledge atlas overview aggregation`：实现 overview 门类卡片聚合
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImplTest.java`
    - 处理动作：聚合 `overviewView`、门类卡片与 overview 面包屑
    - 验收点：`level=overview` 返回真实门类卡片与跳转 href
    - 重要度：9/10

- [ ] `Knowledge atlas category aggregation`：实现 category 门类层聚合
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImplTest.java`
    - 处理动作：按 `categoryCode` 聚合 category 层版本、实体高亮、关系分组和来源摘要
    - 验收点：`level=category` 返回真实门类层数据块
    - 重要度：9/10

- [ ] `Knowledge atlas detail aggregation`：实现 detail 实体层聚合
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImplTest.java`
    - 处理动作：按 `entityId` 聚合 detail 层实体、关系、来源和时间线
    - 验收点：`level=detail` 返回真实 detail 数据块
    - 重要度：9/10

- [ ] `Knowledge atlas interface contract`：扩展 Portal Atlas request/response 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/request/KnowledgePortalAtlasQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/response/KnowledgePortalAtlasResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/assembler/KnowledgePortalAtlasInterfaceAssembler.java`
    - 处理动作：把 portal atlas HTTP 契约扩展为三层浏览结构
    - 验收点：response 能完整表达 `overview/category/detail`
    - 重要度：9/10

- [ ] `Knowledge atlas controller wiring`：接通三层 query 到 controller
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/KnowledgePortalAtlasController.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/KnowledgePortalAtlasControllerTest.java`
    - 处理动作：把 portal query 装配到 application query 并补齐三种层级接口测试
    - 验收点：controller 测试覆盖 `overview/category/detail` 三种入口
    - 重要度：8/10

- [ ] `Portal atlas service types`：同步前端 Atlas 三层类型与服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-types.ts`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-service.ts`
    - 处理动作：把 atlas types 和 fallback service 改造成三层结构
    - 验收点：前端 service 与后端 atlas response 同构
    - 重要度：8/10

- [ ] `Portal atlas url state`：建立 URL 驱动的 Atlas 页面状态
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.test.tsx`
    - 处理动作：从 URL 解析 `level/categoryCode/entityId` 并纳入 queryKey
    - 验收点：刷新后可恢复当前 atlas 层级
    - 重要度：9/10

- [ ] `Portal atlas overview page`：实现 overview 层页面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.css`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.test.tsx`
    - 处理动作：渲染 overview 标题区、门类卡片区和门类入口跳转
    - 验收点：overview 不再直接复用 detail 骨架布局
    - 重要度：9/10

- [ ] `Portal atlas category page`：实现 category 层页面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.css`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.test.tsx`
    - 处理动作：渲染单门类版本信息、实体高亮、关系分组和来源摘要
    - 验收点：category 层能稳定进入 detail 层
    - 重要度：9/10

- [ ] `Portal atlas detail page`：实现 detail 层页面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.css`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.test.tsx`
    - 处理动作：渲染实体摘要、关联关系、来源条目和时间线
    - 验收点：detail 层具备单实体完整阅读信息
    - 重要度：9/10

- [ ] `Portal atlas breadcrumb`：接入三层面包屑导航
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.css`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.test.tsx`
    - 处理动作：用后端 breadcrumb 驱动 overview/category/detail 返回导航
    - 验收点：点击 breadcrumb 能返回上一层或 overview
    - 重要度：8/10

- [ ] `Knowledge coverage sync`：同步 Knowledge 覆盖文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：把 Portal 页面与图谱浏览分层状态更新为当前实现口径
    - 验收点：覆盖文档不再保留与本轮实现冲突的未完成表述
    - 重要度：7/10

- [ ] `Portal atlas verify`：完成 portal-web 图谱分层验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：本轮新增或修改的 `kuzhambu-apps/portal-web` 文件
    - 处理动作：运行 `format:check`、`lint`、`test`、`build`
    - 验收点：`portal-web` 四项验证全部通过
    - 重要度：8/10

- [ ] `Knowledge atlas hierarchy cleanup`：清理 Atlas 分层 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 处理动作：任务关闭时删除本轮 RUNBOOK
    - 验收点：PR 收口前不残留无剩余用途的 RUNBOOK
    - 重要度：6/10

## 待讨论项
