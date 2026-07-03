# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Batch result model`：新增 Classics 共享批量操作结果模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/result/ClassicsBatchOperationResult.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/result/ClassicsBatchOperationItemResult.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/common/response/ClassicsBatchOperationResponse.java`
    - 处理动作：新增 success/failure 计数和 item 级成功/失败明细模型。
    - 验收点：应用层和接口层都能表达 `successCount`、`failureCount`、`successes[]`、`failures[]`。
    - 重要度：9/10

- [ ] `Batch visibility API`：新增批量公开/私有状态接口入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsBatchVisibilityRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`
    - 处理动作：新增批量 visibility request、endpoint 和接口测试。
    - 验收点：接口校验 `contentType`、`contentIds`、`visibility`，并返回 `ClassicsBatchOperationResponse`。
    - 重要度：8/10

- [ ] `Sancai batch visibility`：实现 Sancai 批量公开/私有状态修改
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiApplicationServiceImplTest.java`
    - 处理动作：批量调用现有 `changeEntryVisibility` 语义并记录 item 级失败。
    - 验收点：批量修改保留 version、content updated time、search sync，并支持部分失败。
    - 重要度：8/10

- [ ] `Wangqi batch visibility`：实现 Wangqi 批量公开/私有状态修改
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/WangqiDocumentApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/wangqi/WangqiDocumentApplicationServiceImplTest.java`
    - 处理动作：批量调用现有 `changeVisibility` 语义并记录 item 级失败。
    - 验收点：批量修改保留 version、search sync，并支持部分失败。
    - 重要度：8/10

- [ ] `Ming Customs batch visibility`：实现 Ming Customs 批量公开/私有状态修改
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/MingCustomsApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/mingcustoms/MingCustomsApplicationServiceImplTest.java`
    - 处理动作：批量调用现有 `changeVisibility` 语义并记录 item 级失败。
    - 验收点：批量修改保留 version、search sync，并支持部分失败。
    - 重要度：8/10

- [ ] `Batch share backend`：实现批量分享应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/BatchShareCreateCommand.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/ClassicsSharingApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sharing/ClassicsSharingApplicationServiceImplTest.java`
    - 处理动作：新增 `batchCreateLinks`，逐 target 复用现有分享创建和 snapshot 绑定逻辑。
    - 验收点：批量分享返回成功/失败明细，私有内容未确认时请求级拒绝，重复 target 行为确定。
    - 重要度：9/10

- [ ] `Batch share interface`：新增批量分享 Admin API
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/request/ClassicsBatchShareCreateRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/assembler/ClassicsSharingInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/ClassicsSharingAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/ClassicsSharingAdminControllerTest.java`
    - 处理动作：新增 `POST /api/classics/shares/batch-create` 和 request/assembler/controller 测试。
    - 验收点：接口返回 `ClassicsBatchOperationResponse`，且不新增 Portal controller 或 Portal endpoint。
    - 重要度：9/10

- [ ] `Admin Web batch share contract`：新增批量分享前端 service contract
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-service-contract.test.ts`
    - 处理动作：新增 batch request/result TS 类型和 `batchCreate` service 方法。
    - 验收点：contract test 固定 `/classics/shares/batch-create` 请求和批量结果字段。
    - 重要度：8/10

- [ ] `Sancai Admin batch share`：接入 Sancai 页面批量分享入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`
    - 处理动作：在已有选择态基础上调用 `classics-share-service.batchCreate`。
    - 验收点：页面展示批量分享成功数、失败数和失败原因。
    - 重要度：7/10

- [ ] `Wangqi Admin batch share`：接入 Wangqi 页面批量分享入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`
    - 处理动作：在已有选择态基础上调用 `classics-share-service.batchCreate`。
    - 验收点：页面展示批量分享成功数、失败数和失败原因。
    - 重要度：7/10

- [ ] `Ming Customs Admin batch share`：接入 Ming Customs 页面批量分享入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`
    - 处理动作：在已有选择态基础上调用 `classics-share-service.batchCreate`。
    - 验收点：页面展示批量分享成功数、失败数和失败原因。
    - 重要度：7/10

- [ ] `Sharing Admin regression`：验证批量分享记录仍可在分享管理页维护
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.test.tsx`
    - 处理动作：验证批量创建出的 share link 可列表展示并继续使用现有 status update。
    - 验收点：`ACTIVE`、`EXPIRED`、`REVOKED` 管理行为不回退。
    - 重要度：7/10

- [ ] `Portal Web share regression`：验证 Portal 分享读取状态语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/share/share-service.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-types.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-service.test.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-page.tsx`、`kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`
    - 处理动作：补充 Portal Web 分享读取回归，不新增 Portal 批量入口。
    - 验收点：`ACTIVE` 可读，`EXPIRED` / `REVOKED` 按现有错误语义处理，response 字段不变时不新增 Portal 类型字段。
    - 重要度：8/10

- [ ] `Implementation Coverage`：更新已关闭能力的 implementation coverage
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：在代码和测试通过后，将已关闭项更新为完成并保留剩余未完成项。
    - 验收点：coverage 状态与实际交付和测试结果一致。
    - 重要度：9/10

- [ ] `RUNBOOK closure`：清理阶段性 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`、`TODO.md`
    - 处理动作：阶段目标关闭后删除 RUNBOOK，并从 TODO 中删除或收窄已完成任务。
    - 验收点：PR 收口前不保留已完成的临时 RUNBOOK 和已完成 TODO。
    - 重要度：8/10

- [ ] `Servers full verification`：执行 Java servers 全量 format -> checkstyle -> compile -> test
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/`
    - 处理动作：按顺序执行 Java servers 全量格式检查、静态检查、编译和测试。
    - 验收点：按 `format -> checkstyle -> compile -> test` 顺序通过 `mvn spotless:check`、`mvn checkstyle:check`、`mvn -DskipTests compile`、`mvn test`。
    - 重要度：10/10

- [ ] `Apps full verification`：执行 frontend apps 全量 format -> lint -> build -> test
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-apps/`
    - 处理动作：按顺序执行 frontend apps 全量格式检查、lint、构建和测试。
    - 验收点：按 `format -> lint -> build -> test` 顺序通过 `npm run format:check`、`npm run lint`、`npm run build`、`npm run test`。
    - 重要度：10/10

- [ ] `Workers full verification`：执行 Python workers 全量 format -> lint -> compile -> test
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-workers/`
    - 处理动作：按顺序执行 Python workers 全量格式检查、lint、编译检查和测试。
    - 验收点：按 `format -> lint -> compile -> test` 顺序通过 `ruff format --check`、`ruff check`、`python -m compileall src`、`pytest -p no:capture`。
    - 重要度：10/10

## 待讨论项
