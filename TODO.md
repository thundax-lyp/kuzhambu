# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `15 admin-search-frontend`：调整 Admin Search 页面和搜索统计控件 ID 口径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-ID-FIELD-CLEANUP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/discovery/search/search-types.ts`、`kuzhambu-apps/admin-web/src/pages/discovery/search/search-service.ts`、`kuzhambu-apps/admin-web/src/pages/discovery/search/search-page.tsx`、`kuzhambu-apps/admin-web/src/pages/discovery/search/search-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/discovery/search-statistics/search-statistics-types.ts`、`kuzhambu-apps/admin-web/src/pages/discovery/search-statistics/search-statistics-page.tsx`
    - 处理动作：把搜索响应、搜索结果点击、搜索统计表格展开和详情请求中的本体身份改为 `id`。
    - 验收点：搜索输入框、搜索按钮、预览、筛选、日期 RangePicker、重建索引按钮不变；搜索统计“检索编号”列、展开缓存 key 和详情请求使用 `id`。
    - 重要度：9/10

- [ ] `16 frontend-tests-e2e`：更新 Discovery 前端测试和 Portal E2E
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-ID-FIELD-CLEANUP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-console-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/discovery/search/search-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/discovery/search-statistics/search-statistics-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/discovery/common/discovery-service-contract.test.ts`、`kuzhambu-apps/portal-web/e2e/discovery/search/search.spec.ts`、`kuzhambu-apps/portal-web/e2e/discovery/qa/qa.spec.ts`
    - 处理动作：把前端 mock、用户操作断言和 e2e 响应中的本体身份字段改为 `id`。
    - 验收点：前端测试断言会话点击、删除、导出、搜索结果点击、搜索统计展开均按 `id` 工作；点击请求仍包含引用 `searchEventId`。
    - 重要度：9/10

- [ ] `17 backend-tests`：更新 Discovery 后端测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-ID-FIELD-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaMessageRepositoryImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSourceRepositoryImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalControllerTest.java`
    - 处理动作：更新 repository、application 和 interface 测试中的保存回填、`getById` 和 response JSON 字段断言。
    - 验收点：测试不再断言 `setId(nextId)` 或本体旧字段名，后端测试覆盖数据库回填 ID 和 `id` 响应字段。
    - 重要度：9/10

- [ ] `18 verification`：运行 Discovery ID 清理验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-ID-FIELD-CLEANUP.md`
    - 范围对象：`kuzhambu-servers`、`kuzhambu-apps/admin-web`、`kuzhambu-apps/portal-web`
    - 处理动作：运行 RUNBOOK 中 backend 和 frontend 验证命令，并记录失败项或通过结果。
    - 验收点：`mvn -pl biz/discovery -am spotless:check checkstyle:check test`、admin-web format/lint/test/build、portal discovery e2e 按 RUNBOOK 口径完成或留下明确失败原因。
    - 重要度：10/10

- [ ] `19 cleanup-runbook`：完成后清理 ID 清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-DISCOVERY-ID-FIELD-CLEANUP.md`
    - 范围对象：`docs/30-designs/RUNBOOK-DISCOVERY-ID-FIELD-CLEANUP.md`、`TODO.md`
    - 处理动作：在所有实现、测试和文档同步完成后删除临时 RUNBOOK，并从 TODO 中删除已完成任务项。
    - 验收点：项目内不再保留已完成的 ID 清理 RUNBOOK，`TODO.md` 不记录已完成任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
