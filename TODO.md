# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `db/schema/knowledge.sql`：新增 Knowledge 质量报告表结构
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`db/schema/knowledge.sql`
    - 处理动作：新增 `knowledge_quality_report`、`knowledge_quality_report_issue`、`knowledge_quality_report_source_detail` 表和索引，并扩展 `knowledge_quality_annotation.object_type` 说明口径。
    - 验收点：三张报告表字段、类型、索引与 RUNBOOK 数据结构一致，标注对象支持 `ENTITY`、`RELATION`、`LINEAGE_NODE`、`LINEAGE_RELATION`。
    - 重要度：10/10

- [ ] `knowledge-domain`：新增质量报告领域对象和仓储边界
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/model/entity/QualityReport.java`、`QualityReportIssue.java`、`QualityReportSourceDetail.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/QualityReportRepository.java`
    - 处理动作：新增报告主表、问题快照、来源明细领域实体和报告仓储接口。
    - 验收点：领域对象字段与 RUNBOOK 表字段一致，仓储接口覆盖保存、详情、分页和最新 `PUBLISHED` 报告读取。
    - 重要度：10/10

- [ ] `knowledge-infra dataobject`：新增质量报告持久化对象和 Mapper
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/dataobject/QualityReportDO.java`、`QualityReportIssueDO.java`、`QualityReportSourceDetailDO.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/mapper/QualityReportMapper.java`、`QualityReportIssueMapper.java`
    - 处理动作：新增三张报告表的 MyBatis DO 和主表/问题 Mapper。
    - 验收点：DO 字段与数据库字段一一对应，Mapper 能被 Spring 扫描并支持基础 CRUD。
    - 重要度：9/10

- [ ] `knowledge-infra repository`：新增质量报告持久化转换和仓储实现
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/mapper/QualityReportSourceDetailMapper.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/persistence/assembler/QualityReportPersistenceAssembler.java`、`QualityReportIssuePersistenceAssembler.java`、`QualityReportSourceDetailPersistenceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/QualityReportRepositoryImpl.java`
    - 处理动作：实现报告快照的领域/DO 转换、来源明细 Mapper 和报告仓储。
    - 验收点：保存报告时主表、问题和来源明细整体写入，最新报告按 `report_status=PUBLISHED` 与 `generated_at` 倒序读取。
    - 重要度：10/10

- [ ] `QualityAnnotationRepository`：扩展质量标注查询能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/refinement/repository/QualityAnnotationRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/QualityAnnotationRepositoryImpl.java`
    - 处理动作：新增按 `graphVersionId` 查询全部质量标注的方法，供报告生成和详情展示复用。
    - 验收点：报告服务可一次读取图谱版本范围内 `ENTITY`、`RELATION`、`LINEAGE_NODE`、`LINEAGE_RELATION` 标注。
    - 重要度：9/10

- [ ] `KnowledgeQualityReportApplicationService`：新增质量报告生成和读取用例
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/KnowledgeQualityReportApplicationService.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeQualityReportApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/command/GenerateQualityReportCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/query/QualityReportPageQuery.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/QualityReportDetailResult.java`
    - 处理动作：实现按 `graphVersionId` 生成 `PUBLISHED` 报告、分页、详情和最新报告读取。
    - 验收点：报告生成只消费 Knowledge 正式事实、refinement 状态和人工标注，不依赖 AI facade、图谱抽取接口或 worker client。
    - 重要度：10/10

- [ ] `KnowledgeQualityReportController`：新增后台质量报告接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeQualityReportController.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/request/QualityReportRequests.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/QualityReportResponses.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeQualityReportInterfaceAssembler.java`
    - 处理动作：新增 `/api/knowledge/quality/report/generate|page|detail|latest` 接口和协议转换。
    - 验收点：查看接口使用 `knowledge:quality-report:view`，生成接口使用 `knowledge:quality-report:generate`，请求响应字段与 RUNBOOK 一致。
    - 重要度：10/10

- [ ] `KnowledgePortalReadApplicationServiceImpl`：Portal 质量页改读报告快照
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`
    - 处理动作：将 `getQuality()` 改为读取最新 `PUBLISHED` 质量报告并映射为现有 Portal 响应模型。
    - 验收点：Portal 有报告时展示报告快照，无报告时返回明确空态和 `尚未生成质量报告` 问题。
    - 重要度：10/10

- [ ] `admin-web refinement`：补齐精修页人工质量标注控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-types.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-quality-annotation-drawer.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-quality-annotation-table.tsx`
    - 处理动作：在实体、关系、世系节点、世系关系行内增加 `标注` 操作并接入 Drawer 表单。
    - 验收点：Drawer 包含 `annotationStatus` Select、`annotationLabel` Select、`comment` TextArea 和 `保存`、`删除标注`、`取消` 按钮，保存/删除后刷新详情、摘要和标注表。
    - 重要度：10/10

- [ ] `admin-web quality-report service`：新增质量报告前端 API 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-types.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-service.test.ts`
    - 处理动作：新增质量报告类型、`generateReport`、`pageReports`、`getReportDetail`、`getLatestReport` 服务和契约测试。
    - 验收点：服务请求路径分别命中 `/knowledge/quality/report/generate|page|detail|latest`，请求体字段与 RUNBOOK 一致。
    - 重要度：9/10

- [ ] `admin-web quality-report page`：新增质量报告生成和历史页面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.tsx`、`quality-report-page.css`、`components/quality-report-generate-form.tsx`、`components/quality-report-summary.tsx`、`components/quality-report-history-table.tsx`
    - 处理动作：新增独立 `质量报告` 页面，支持输入 `graphVersionId` 生成报告、展示四个指标和历史列表。
    - 验收点：页面包含 `InputNumber`、`生成报告` 主按钮、四个 `Statistic` 和历史表，生成成功后刷新最新报告和历史列表。
    - 重要度：10/10

- [ ] `admin-web quality-report detail`：新增质量报告详情控件和路由
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-issue-table.tsx`、`quality-report-source-table.tsx`、`quality-report-annotation-table.tsx`、`kuzhambu-apps/admin-web/src/router/index.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.test.tsx`
    - 处理动作：新增 `问题清单`、`来源明细`、`人工标注` Tabs 详情表并挂载 `/knowledge/quality-report` 路由。
    - 验收点：三个 Tab 的表格列和 `打开` 操作符合 RUNBOOK，路由能进入质量报告页。
    - 重要度：10/10

- [ ] `system menu seed`：新增后台质量报告菜单和权限种子
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`db/data-source/system.json`、`db/data/system.sql`
    - 处理动作：新增 `质量报告` 菜单和 `knowledge:quality-report:view|generate` 权限种子，并重新生成系统数据 SQL。
    - 验收点：后台菜单可导航到 `/knowledge/quality-report`，权限点与后端 `@HasPermission` 一致。
    - 重要度：9/10

- [ ] `knowledge tests`：补齐质量报告后端测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeQualityReportApplicationServiceTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeQualityReportControllerTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/controller/KnowledgePortalQualityControllerTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/QualityReportRepositoryTest.java`
    - 处理动作：新增或更新报告生成、后台接口、Portal 空态和仓储读取测试。
    - 验收点：测试覆盖报告主表/问题/来源明细保存、最新 `PUBLISHED` 排序、Portal 空态和后台接口权限路径。
    - 重要度：10/10

- [ ] `quality loop validation`：运行 Knowledge 质量闭环验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`
    - 处理动作：运行 RUNBOOK 中 Java servers 与 admin-web 的格式化、静态检查和测试命令。
    - 验收点：`mvn spotless:check`、`mvn checkstyle:check`、Knowledge 相关 Maven test、`npm run format:check`、`npm run lint`、admin-web test 均通过，或记录明确阻塞。
    - 重要度：10/10

- [ ] `Knowledge documentation closure`：更新覆盖矩阵并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`docs/30-designs/KNOWLEDGE-DESIGN.md`、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`、`TODO.md`
    - 处理动作：将质量标注报告闭环写入 Knowledge 设计和 Implementation Coverage，删除已完成 RUNBOOK，并按完成情况清理或收窄 TODO。
    - 验收点：Coverage 不再把质量报告闭环标为未完成，RUNBOOK 文件已删除，`TODO.md` 不保留已完成任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
