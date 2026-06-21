# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `admin-web sancai export ui`：接入导出任务前端闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-RENDER-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
    - 处理动作：在三才图会条目面板中增加导出动作、任务列表展示和下载入口。
    - 验收点：管理员能在三才图会条目页发起导出并看到任务状态与下载入口。
    - 重要度：9/10

- [ ] `sancai showcase domain`：补静态展示任务状态回写
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-RENDER-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiShowcase.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/mapper/SancaiAssetMapper.java`
    - 处理动作：为静态展示任务增加成功和失败状态回写及产物字段更新能力。
    - 验收点：静态展示任务能按任务 ID 回写 `status/storageObjectId/entryCount`。
    - 重要度：10/10

- [ ] `sancai showcase app`：打通静态展示任务执行闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-RENDER-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiShowcaseCommand.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/result/SancaiShowcaseJobResult.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`
    - 处理动作：把 `requestShowcase(...)` 改成“建任务 -> 调 render -> 写 Storage -> 回写任务状态”的同步闭环。
    - 验收点：静态展示成功时返回带 `storageObjectId` 的任务结果，失败时任务进入失败态且单测覆盖成功/失败路径。
    - 重要度：10/10

- [ ] `sancai showcase admin api`：补静态展示任务分页与下载接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-RENDER-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
    - 处理动作：新增静态展示任务分页接口和展示产物下载接口，并返回前端所需状态字段。
    - 验收点：后台接口可分页查看静态展示任务并下载成功 HTML 产物。
    - 重要度：9/10

- [ ] `sancai showcase admin controller test`：锁定静态展示任务接口契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-RENDER-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAssetAdminControllerTest.java`
    - 处理动作：补齐静态展示任务分页与下载接口的 controller 契约测试。
    - 验收点：测试覆盖分页返回和成功下载路径。
    - 重要度：8/10

- [ ] `admin-web sancai showcase ui`：接入静态展示任务前端闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-RENDER-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
    - 处理动作：在三才图会条目页接入“生成静态展示”动作、任务列表和下载入口。
    - 验收点：管理员能发起静态展示任务并看到任务状态与下载入口。
    - 重要度：9/10

- [ ] `classics export expiry`：补导出任务过期控制
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-RENDER-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
    - 处理动作：固定导出任务 `7` 天过期规则，并在后端拒绝过期下载、前端展示过期状态。
    - 验收点：过期导出任务不可下载，Admin Web 任务列表能显示已过期状态。
    - 重要度：9/10

- [ ] `render closure verification`：补联调验证与现场清理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-RENDER-CLOSURE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/STORAGE-IMPLEMENTATION-COVERAGE.md`、`TODO.md`、`docs/30-designs/RUNBOOK-CLASSICS-RENDER-CLOSURE.md`
    - 处理动作：完成 workers 与 admin starter 联调验证，更新覆盖文档，删除已完成 TODO，并在任务完成后删除本 RUNBOOK。
    - 验收点：覆盖文档口径与代码一致，`TODO.md` 不保留已完成项，RUNBOOK 在闭环完成后移除。
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
