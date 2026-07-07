# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `knowledge interface DTO`：新增标签批量治理 interface 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-TAG-BATCH-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagBatchMergeRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagBatchDeprecateRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagBatchReviewRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagBatchMergePreviewResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`
    - 处理动作：新增批量请求响应模型并补齐 interface assembler 映射。
    - 验收点：JSON 字段、validation 注解和 response 组合与 RUNBOOK 接口契约一致。
    - 重要度：9/10

- [ ] `knowledge taxonomy controller tests`：接入批量治理接口并锁定后端测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-TAG-BATCH-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyController.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyControllerTest.java`
    - 处理动作：新增 4 个批量 HTTP 入口并补 application 与 controller 测试。
    - 验收点：权限、审计文案、请求映射、成功路径和整体失败路径均有测试覆盖。
    - 重要度：10/10

- [ ] `admin taxonomy service`：新增 Admin Web 批量治理 service 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-TAG-BATCH-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-types.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service.test.ts`
    - 处理动作：新增批量合并、批量废弃、批量审核前端 service 方法与类型。
    - 验收点：4 个 service 方法 URL 和 payload 字段与 RUNBOOK HTTP API 一致。
    - 重要度：9/10

- [ ] `admin tag batch controls`：实现统一标签批量合并和批量废弃控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-TAG-BATCH-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-batch-merge-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.css`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.test.tsx`
    - 处理动作：在统一标签表格接入多选、批量合并 Drawer、批量废弃确认和页面状态刷新。
    - 验收点：批量合并和批量废弃控件 disabled、确认、提交、清空选择和 query 刷新行为符合 RUNBOOK。
    - 重要度：10/10

- [ ] `admin review batch controls`：实现待审核标签批量通过和批量拒绝控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-TAG-BATCH-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-review-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-batch-review-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.css`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.test.tsx`
    - 处理动作：在待审核标签表格接入多选、批量通过 Drawer、批量拒绝 Drawer 和页面状态刷新。
    - 验收点：批量通过必须选择分类，批量拒绝可直接提交，成功后待审核表格选择数量归零。
    - 重要度：10/10

- [ ] `knowledge backend validation`：运行标签批量治理后端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-TAG-BATCH-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/pom.xml`、`kuzhambu-servers/biz/knowledge/pom.xml`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/pom.xml`
    - 处理动作：运行 Knowledge 后端 formatter、静态检查和 Maven 测试。
    - 验收点：`spotless:check`、`checkstyle:check` 和 Knowledge application/interface 相关 `test` 通过或记录明确阻塞。
    - 重要度：10/10

- [ ] `admin taxonomy validation`：运行标签批量治理 Admin Web 验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-TAG-BATCH-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/package.json`、`kuzhambu-apps/package-lock.json`、`kuzhambu-apps/admin-web/package.json`、`kuzhambu-apps/admin-web/vite.config.ts`
    - 处理动作：运行 Admin Web format、lint、test 和 build。
    - 验收点：`format:check`、`lint`、Admin Web `test` 和 `build` 通过或记录明确阻塞。
    - 重要度：10/10

- [ ] `knowledge final sync and cleanup`：同步 main 后完成 coverage 与 RUNBOOK 收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-TAG-BATCH-GOVERNANCE.md`
    - 范围对象：`main` 分支最新代码、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-TAG-BATCH-GOVERNANCE.md`、`TODO.md`
    - 处理动作：最终收口前同步 `main` 最新代码，更新 Knowledge Implementation Coverage，并删除已完成 RUNBOOK 与对应 TODO。
    - 验收点：分支包含最新 `main` 基线，coverage 中 `标签批量操作` 为 `已完成` 且未完成部分为 `无`，RUNBOOK 已删除，完成项已从 `TODO.md` 清理。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
