# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `classics ming-customs version interface models`：补齐明代习俗版本接口模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MING-CUSTOMS-HISTORY-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/request/MingCustomsVersionRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsVersionResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/assembler/MingCustomsInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/MingCustomsAdminControllerTest.java`
    - 处理动作：新增明代习俗版本请求、响应和 assembler 转换并用 controller 测试锁定响应字段。
    - 验收点：版本响应能输出 `id/contentType/contentId/versionNo/versionedAt/snapshotJson/changeType/changeSummary`。
    - 重要度：9/10

- [ ] `classics ming-customs version controller`：补齐明代习俗版本接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MING-CUSTOMS-HISTORY-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/MingCustomsAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/MingCustomsAdminControllerTest.java`
    - 处理动作：新增 `versions/list`、`versions/get`、`versions/reset` 接口和明代习俗版本归属校验。
    - 验收点：接口按 `MING_CUSTOMS + id` 查询、查看和恢复版本，跨内容版本会被拒绝。
    - 重要度：10/10

- [ ] `classics ming-customs restore behavior`：锁定明代习俗历史恢复行为
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MING-CUSTOMS-HISTORY-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/MingCustomsVersionSnapshot.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/mingcustoms/MingCustomsApplicationServiceImplTest.java`
    - 处理动作：校准 `MING_CUSTOMS` 恢复字段、生成 `HISTORY_RESTORED` 新版本并补齐恢复测试。
    - 验收点：恢复后主字段来自快照、当前版本标记指向新版本、搜索同步语义与手动保存一致。
    - 重要度：10/10

- [ ] `admin-web ming-customs version service`：补齐明代习俗版本前端类型和 service
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MING-CUSTOMS-HISTORY-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-service-contract.test.ts`
    - 处理动作：新增版本记录、快照类型和 `listVersions/getVersion/resetVersion` service 方法。
    - 验收点：前端版本 service 请求路径和 `{ id, versionId }` 请求体与后端契约一致。
    - 重要度：9/10

- [ ] `admin-web ming-customs version panel`：新增明代习俗版本历史面板
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MING-CUSTOMS-HISTORY-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-version-history-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.css`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`
    - 处理动作：新增版本列表、版本详情、字段对比、恢复按钮和快照异常态控件。
    - 验收点：面板展示版本历史、字段当前/历史对比，快照异常时禁用恢复。
    - 重要度：9/10

- [ ] `admin-web ming-customs page version flow`：接入明代习俗页面版本查询和恢复
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MING-CUSTOMS-HISTORY-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.css`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`
    - 处理动作：在编辑弹窗中接入版本列表、版本详情、恢复确认、恢复 mutation 和相关 query 刷新。
    - 验收点：用户能在编辑弹窗查看版本对比并确认恢复，恢复后页面数据自动刷新。
    - 重要度：10/10

- [ ] `admin-web ming-customs version e2e`：补齐明代习俗版本历史 E2E 回归
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MING-CUSTOMS-HISTORY-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/classics/ming-customs/ming-customs.spec.ts`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`
    - 处理动作：新增版本列表、版本详情和历史恢复的页面 E2E 与异常快照单测断言。
    - 验收点：E2E 能证明 `versions/list`、`versions/get`、`versions/reset` 三条链路已接通。
    - 重要度：8/10

- [ ] `branch sync main`：同步 main 分支最新代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/ming-customs-history-closure` 分支与 `origin/main`
    - 处理动作：在后端、前端和 E2E 实现完成后同步 `origin/main` 最新代码并处理冲突。
    - 验收点：当前分支包含最新 `origin/main`，同步后相关验证仍通过。
    - 重要度：8/10

- [ ] `ming-customs history final validation`：执行明代习俗版本闭环最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MING-CUSTOMS-HISTORY-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs`、`kuzhambu-apps/admin-web/e2e/classics/ming-customs/ming-customs.spec.ts`
    - 处理动作：在同步 `origin/main` 后运行后端、前端和 E2E 的最小相关验证。
    - 验收点：明代习俗版本接口、恢复行为、页面交互和 E2E 回归验证通过。
    - 重要度：9/10

- [ ] `classics implementation coverage`：更新 Classics Implementation Coverage
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MING-CUSTOMS-HISTORY-CLOSURE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：将明代习俗版本历史、版本对比和历史恢复口径更新为已完成。
    - 验收点：Implementation Coverage 只记录已完成能力，不保留执行过程。
    - 重要度：8/10

- [ ] `classics ming-customs runbook cleanup`：清理明代习俗版本历史 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-CLASSICS-MING-CUSTOMS-HISTORY-CLOSURE.md`、`TODO.md`
    - 处理动作：任务闭环并完成验证后删除 RUNBOOK，按完成情况删除或收窄 TODO 项。
    - 验收点：PR 收口前无已完成且无剩余价值的 RUNBOOK，TODO 不保留已完成任务。
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
