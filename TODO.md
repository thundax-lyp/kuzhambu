# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `01-classics_sancai_showcase`：补齐静态展示任务记录字段和查询条件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHOWCASE-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiShowcase.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiShowcaseDO.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/assembler/SancaiPersistenceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`
    - 处理动作：为 showcase 记录补齐完成时间、范围标题、资产数、文件元数据、失败信息和列表筛选查询。
    - 验收点：Repository 能按 keyword/status/visibilityRiskStatus/requestedAt 区间倒序分页返回完整 `SancaiShowcase` 记录，并能按单条任务更新成功、失败和过期状态。
    - 重要度：9/10

- [ ] `02-SancaiAssetApplicationService`：收口 showcase worker 调用和 Storage 回源闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHOWCASE-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiShowcaseCommand.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/result/SancaiShowcaseJobResult.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`
    - 处理动作：按 `REQUESTED -> PROCESSING -> COMPLETED/FAILED` 编排 showcase worker 调用、artifact 校验、Storage 创建和失败归一。
    - 验收点：成功任务写入 `storageObjectId/filename/contentType/sizeBytes/sha256`，失败任务写入 `failureType/failureMessage` 且不创建可下载产物。
    - 重要度：10/10

- [ ] `03-SancaiAssetAdminController`：补齐 showcase Admin API 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHOWCASE-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
    - 处理动作：扩展 showcase 创建、分页、预览和下载接口的请求字段、响应字段、权限和下载门禁。
    - 验收点：Admin API 可返回完整 showcase 记录，非 `COMPLETED` 或无 `storageObjectId` 的任务不能下载，inline/attachment 响应头正确。
    - 重要度：9/10

- [ ] `04-showcase backend tests`：补齐 Java showcase 回归测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHOWCASE-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/sancai/SancaiRepositoryTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAssetAdminControllerTest.java`
    - 处理动作：为 showcase 查询、回源状态、接口契约和下载门禁补齐 Java 聚焦测试。
    - 验收点：测试覆盖成功回源、失败落库、筛选查询、私有确认、非完成态禁下载和 content disposition。
    - 重要度：8/10

- [ ] `05-sancai-showcase worker`：固化 showcase payload 和离线 HTML 渲染
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHOWCASE-LOOP.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/schemas/render.py`、`kuzhambu-workers/src/kuzhambu_workers/render/sancai_showcase.py`、`kuzhambu-workers/src/kuzhambu_workers/render/templates/sancai_showcase.html`、`docs/20-interfaces/WORKERS-RENDER-INTERFACE.md`
    - 处理动作：按目标 payload 渲染单文件离线 HTML，并同步 render 接口文档。
    - 验收点：worker 输出 `text/html; charset=utf-8` 单文件产物，summary 返回 catalog/volume/asset/risk 元数据，HTML 支持门类、卷、条目、搜索、筛选、浏览模式、打印和资源占位。
    - 重要度：9/10

- [ ] `06-showcase worker tests`：补齐 worker payload 和 HTML 输出测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHOWCASE-LOOP.md`
    - 范围对象：`kuzhambu-workers/tests/test_sancai_showcase.py`、`kuzhambu-workers/tests/test_render_routes.py`、`kuzhambu-workers/tests/test_worker_e2e_render.py`
    - 处理动作：为 `SANCAI_SHOWCASE` payload、stream final artifact 和离线 HTML 输出补齐 worker 回归测试。
    - 验收点：测试覆盖目标 payload、非 HTML 拒绝、summary 元数据、图片资源占位和 stream `completed.artifact`。
    - 重要度：8/10

- [ ] `07-Admin Web Sancai showcase section`：实现静态展示任务列表、筛选、生成、预览和下载
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHOWCASE-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-showcase-job-section.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-showcase-job-section.css`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`
    - 处理动作：在三才图会页面新增 showcase section，接入搜索、状态筛选、风险筛选、生成时间筛选、生成、刷新、预览和下载操作。
    - 验收点：控件可访问名称、placeholder、筛选/重置/刷新行为、私有内容确认文案、成功/失败/处理中行操作和错误提示均符合 RUNBOOK。
    - 重要度：9/10

- [ ] `08-showcase admin-web tests`：补齐前端 service 和页面交互测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHOWCASE-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`
    - 处理动作：为 showcase service 请求体和三才图会页面控件操作补齐前端测试。
    - 验收点：测试覆盖搜索、状态筛选、风险筛选、时间筛选、重置、刷新、生成确认、预览、下载和失败提示。
    - 重要度：8/10

- [ ] `09-main sync`：收口前同步 `main` 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/classics-showcase-loop` 分支
    - 处理动作：在功能实现和聚焦测试完成后同步最新 `origin/main`，解决冲突并复查任务相关 diff。
    - 验收点：当前分支包含最新 `origin/main`，`git status` 只保留本任务相关改动。
    - 重要度：8/10

- [ ] `10-final verification`：运行 showcase 收口验证命令
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHOWCASE-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/classics`、`kuzhambu-workers`、`kuzhambu-apps/admin-web`
    - 处理动作：同步 `main` 后按 RUNBOOK 执行 servers、workers、admin-web 的格式化、静态检查和测试。
    - 验收点：相关验证命令通过，若存在外部波动或环境问题则记录明确失败命令和原因。
    - 重要度：9/10

- [ ] `11-implementation coverage`：更新 Classics 和 Workers 覆盖文档
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-SHOWCASE-LOOP.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：将 showcase 列表搜索筛选、回源闭环和 worker payload 收口状态同步到 Implementation Coverage。
    - 验收点：Coverage 文档不再把静态展示列表搜索、筛选与回源流程边界标为未完成或模糊状态。
    - 重要度：7/10

- [ ] `12-RUNBOOK cleanup`：任务关闭时清理 RUNBOOK 和 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-SHOWCASE-LOOP.md`
    - 范围对象：`docs/30-designs/RUNBOOK-CLASSICS-SHOWCASE-LOOP.md`、`TODO.md`
    - 处理动作：在 showcase 闭环完成且 Coverage 同步后删除临时 RUNBOOK，并删除或收窄已完成 TODO。
    - 验收点：PR 收口时无已完成任务残留在 `TODO.md`，无已完成 RUNBOOK 残留引用。
    - 重要度：8/10

## 待讨论项
