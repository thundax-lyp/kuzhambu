# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `12 search-application-http`：调整 Search 应用层和 HTTP 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-ID-FIELD-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchResult.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchEventResult.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/response/DiscoverySearchResponse.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchEventResponse.java`
    - 处理动作：把 Search 本体响应字段 `searchEventId` 改为 `id`，并保留点击请求中的引用 `searchEventId`。
    - 验收点：Search response 本体身份为 `id`，点击事件创建仍能携带 `searchEventId` 引用。
    - 重要度：10/10

- [ ] `13 admin-qa-frontend`：调整 Admin QA 页面控件 ID 口径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-ID-FIELD-CLEANUP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-types.ts`、`kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-service.ts`、`kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-page.tsx`、`kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-session-table.tsx`、`kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-session-detail-drawer.tsx`、`kuzhambu-apps/admin-web/src/pages/discovery/qa/qa-message-panel.tsx`
    - 处理动作：把 QA 会话列表、选择会话、删除会话、导出会话和消息面板的本体身份读取从旧字段改为 `id`。
    - 验收点：会话列表 row key、选择按钮、删除按钮、导出按钮、详情标题和消息 key 均使用本体 `id`。
    - 重要度：9/10

- [ ] `14 admin-qa-console-frontend`：调整 Admin QA Console 表格操作 ID 口径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-ID-FIELD-CLEANUP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-console-types.ts`、`kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-console-service.ts`、`kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-console-page.tsx`、`kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-session-table.tsx`、`kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-session-detail-drawer.tsx`、`kuzhambu-apps/admin-web/src/pages/discovery/qa-console/qa-console-page.test.tsx`
    - 处理动作：把 QA Console 会话表格的查看、导出、删除和 row key 操作改为使用 `record.id`。
    - 验收点：标题筛选、日期 RangePicker、查询按钮不变；会话表格的查看/导出/删除按钮和操作提示文案不再依赖本体 `sessionId`。
    - 重要度：9/10

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
