# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
