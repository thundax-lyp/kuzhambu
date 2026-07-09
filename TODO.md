# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `1 后端下载用例`：新增 Operations 报表下载应用用例
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-REPORTS-ADMIN.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/service/ReportApplicationService.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/service/impl/ReportApplicationServiceImpl.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/query/OperationsReportDetailQuery.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/result/OperationsReportDownloadResult.java`
    - 处理动作：新增 `download(OperationsReportDetailQuery query)` 用例并通过 `StorageFacade.open(OpenStorageFacadeRequest)` 读取报表产物。
    - 验收点：报表不存在、未成功或缺少 `storageObjectId` 时返回 Operations 业务错误，成功时返回 `reportId`、`format`、`artifactFilename`、`contentType`、`contentLength`、`storageOriginalFilename`、`inputStream`。
    - 重要度：10/10

- [ ] `2 后端下载 HTTP 入口`：新增 Operations 报表下载 controller 入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-REPORTS-ADMIN.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/main/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/OperationsReportAdminController.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/OperationsReportAdminControllerTest.java`
    - 处理动作：新增 `GET /api/operations/report/{reportId}/content?download=true` 并把应用层下载结果写回 `HttpServletResponse`。
    - 验收点：接口权限为 `operations:report:view`，controller 不注入 Storage 类型，响应包含 `Content-Type`、`Content-Length`、`Content-Disposition` 和文件流。
    - 重要度：10/10

- [ ] `3 前端报表 service`：新增 admin-web 报表类型与接口封装
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-REPORTS-ADMIN.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/reports/reports-types.ts`、`kuzhambu-apps/admin-web/src/pages/operations/reports/reports-service.ts`、`kuzhambu-apps/admin-web/src/pages/operations/reports/reports-service-contract.test.ts`
    - 处理动作：定义报表 record/detail/command/query 类型并封装 `generateReport`、`pageReports`、`getReportDetail`、`toReportDownloadUrl`。
    - 验收点：service 契约测试锁定 `/operations/report/generate`、`/operations/report/page`、`/operations/report/detail` 和 `/operations/report/{reportId}/content?download=true`。
    - 重要度：9/10

- [ ] `4 前端报表页面`：实现 `/operations/reports` 页面控件和交互
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-REPORTS-ADMIN.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/reports/reports-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/reports/reports-page.css`、`kuzhambu-apps/admin-web/src/pages/operations/reports/reports-page.test.tsx`
    - 处理动作：实现标题区、筛选区、生成区、记录列表、详情抽屉、下载按钮和运行中 5 秒轮询。
    - 验收点：页面测试覆盖筛选控件、生成控件、权限禁用、失败原因、详情抽屉、下载按钮可见性和运行中轮询。
    - 重要度：10/10

- [ ] `5 前端路由菜单 E2E`：接入 Operations 报表管理路由、菜单与端到端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-REPORTS-ADMIN.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/router/index.tsx`、`kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`、`kuzhambu-apps/admin-web/e2e/layout/admin-layout.spec.ts`、`kuzhambu-apps/admin-web/e2e/operations/reports/reports.spec.ts`、`kuzhambu-apps/admin-web/e2e/operations/dashboard/dashboard.spec.ts`
    - 处理动作：注册 `/operations/reports` 路由、`operations-report` 菜单图标和 E2E 菜单 mock。
    - 验收点：E2E 覆盖菜单进入、生成、列表刷新、详情、失败原因和 Operations 下载 URL，且不产生 `.menu-icon-config-error`。
    - 重要度：9/10

- [ ] `6 同步 main 分支`：吸收最新 main 代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：当前分支 `feat/operations-reports-admin`、上游分支 `origin/main`
    - 处理动作：在功能实现和测试更新完成后同步最新 `main` 分支代码。
    - 验收点：当前分支包含最新 `origin/main`，同步后没有未解决冲突或无关回退。
    - 重要度：10/10

- [ ] `7 验证后端前端`：执行 Operations 报表闭环验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-REPORTS-ADMIN.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application`、`kuzhambu-apps/admin-web`
    - 处理动作：执行后端下载代理与 admin-web 报表页面相关格式化、静态检查、单元测试、构建和 E2E 验证。
    - 验收点：验证命令有明确通过结果；如有失败，TODO 收窄为剩余失败项且不删除未完成任务。
    - 重要度：10/10

- [ ] `8 更新覆盖矩阵`：同步 Operations Implementation Coverage
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-OPERATIONS-REPORTS-ADMIN.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：更新 Operations 报表条目，记录 `/operations/reports` 管理页和 Operations 专属下载代理已形成闭环。
    - 验收点：Coverage 文档准确反映 admin-web 报表管理页、生成/列表/详情/失败原因、HTML/PDF 下载代理和权限闭环。
    - 重要度：9/10

- [ ] `9 清理 RUNBOOK 与 TODO`：完成临时文档和任务面板收口
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-OPERATIONS-REPORTS-ADMIN.md`、`TODO.md`
    - 处理动作：删除已完成的临时 RUNBOOK，并从 `TODO.md` 删除已完成任务或收窄为剩余未完成内容。
    - 验收点：已完成任务不在 `TODO.md` 中打勾保留；临时 RUNBOOK 已删除；若仍有未完成范围，TODO 只保留剩余任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
