# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `discovery-qa-admin-session-delete-interface`：补齐 Admin 会话删除接口和删除状态响应
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-SESSION-DELETE-EXPORT.md`
    - 范围对象：`DiscoveryQaAdminController.java`、`DiscoveryQaAdminRequests.java`、`DiscoveryQaAdminResponses.java`、`DiscoveryQaAdminInterfaceAssembler.java`、`DiscoveryQaAdminControllerTest.java`
    - 处理动作：新增 Admin `session/delete` 接口并在会话详情响应中映射 `removedAt`。
    - 验收点：Admin 删除路由、权限注解和 `removedAt` 响应字段测试通过。
    - 重要度：9/10

- [ ] `discovery-qa-export-domain-storage`：建立 QA 会话导出领域结构和 Storage owner 类型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-SESSION-DELETE-EXPORT.md`
    - 范围对象：`StorageOwnerType.java`、`kuzhambu-discovery-application/pom.xml`、`QaSessionExport.java`、`QaSessionExportRepository.java`、`QaSessionExportDO.java`
    - 处理动作：新增 `DISCOVERY_QA_SESSION_EXPORT`、Storage facade 依赖和导出记录领域数据结构。
    - 验收点：导出记录字段与 `discovery_qa_session_export` 表字段一致，Discovery 应用层可依赖 Storage facade。
    - 重要度：10/10

- [ ] `discovery-qa-export-repository`：实现 QA 会话导出仓储
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-SESSION-DELETE-EXPORT.md`
    - 范围对象：`QaSessionExportMapper.java`、`QaSessionExportRepositoryImpl.java`、`QaPersistenceAssembler.java`、`QaSessionExportRepositoryImplTest.java`
    - 处理动作：实现导出记录保存、更新、按 `export_id` 查询和持久化映射。
    - 验收点：`save` 自动生成 ID、`update`、`getByExportId` 测试通过。
    - 重要度：9/10

- [ ] `discovery-qa-export-application`：实现 QA 会话 CSV 导出应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-SESSION-DELETE-EXPORT.md`
    - 范围对象：`ExportQaSessionCommand.java`、`QaSessionExportResult.java`、`QaSessionCsvExporter.java`、`QaApplicationServiceImpl.java`、`QaApplicationServiceImplTest.java`
    - 处理动作：生成标准转义 CSV，写入导出记录，上传 Storage，并维护成功或失败状态。
    - 验收点：CSV 转义、Portal 禁止导出删除会话、Admin 允许导出删除会话、Storage 上传参数和失败状态测试通过。
    - 重要度：10/10

- [ ] `discovery-qa-portal-export-interface`：补齐 Portal 会话导出接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-SESSION-DELETE-EXPORT.md`
    - 范围对象：`DiscoveryQaPortalController.java`、`DiscoveryQaRequests.java`、`DiscoveryQaResponses.java`、`DiscoveryQaPortalInterfaceAssembler.java`、`DiscoveryQaPortalControllerTest.java`
    - 处理动作：新增 Portal `session/export` 接口和 CSV 导出请求响应映射。
    - 验收点：Portal 导出路由、请求反序列化和响应字段测试通过。
    - 重要度：9/10

- [ ] `discovery-qa-admin-export-interface`：补齐 Admin 会话导出接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-SESSION-DELETE-EXPORT.md`
    - 范围对象：`DiscoveryQaAdminController.java`、`DiscoveryQaAdminRequests.java`、`DiscoveryQaAdminResponses.java`、`DiscoveryQaAdminInterfaceAssembler.java`、`DiscoveryQaAdminControllerTest.java`
    - 处理动作：新增 Admin `session/export` 接口和 CSV 导出请求响应映射。
    - 验收点：Admin 导出路由、权限注解、请求反序列化和响应字段测试通过。
    - 重要度：9/10

- [ ] `portal-web-discovery-qa-delete-export`：实现 Portal QA 会话删除和导出前端闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-SESSION-DELETE-EXPORT.md`
    - 范围对象：`qa-types.ts`、`qa-service.ts`、`qa-page.tsx`、`qa-page.test.tsx`
    - 处理动作：新增删除和导出类型、服务调用、页面入口和交互测试。
    - 验收点：删除确认、删除后清空当前会话、导出成功和导出失败提示测试通过。
    - 重要度：8/10

- [ ] `admin-web-discovery-qa-delete-export`：实现 Admin QA 会话删除和导出前端闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-SESSION-DELETE-EXPORT.md`
    - 范围对象：`qa-admin-types.ts`、`qa-admin-service.ts`、`qa-admin-page.tsx`、`qa-admin-page.test.tsx`、`discovery-service-contract.test.ts`
    - 处理动作：新增删除和导出类型、服务调用、页面入口、删除状态展示和契约测试。
    - 验收点：删除、已删除状态展示、导出已删除会话和服务路径契约测试通过。
    - 重要度：8/10

- [ ] `discovery-qa-delete-export-tests`：补齐 Discovery QA 删除和导出测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-SESSION-DELETE-EXPORT.md`
    - 范围对象：`DiscoveryQaPortalControllerTest.java`、`DiscoveryQaAdminControllerTest.java`、`QaApplicationServiceImplTest.java`、`QaSessionExportRepositoryImplTest.java`
    - 处理动作：补齐 Controller、应用服务和导出仓储测试覆盖。
    - 验收点：Portal/Admin 接口、删除状态机、导出 CSV、Storage 上传和导出仓储测试通过。
    - 重要度：9/10

- [ ] `discovery-qa-delete-export-closeout`：更新交付覆盖并清理现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-SESSION-DELETE-EXPORT.md`
    - 范围对象：`DISCOVERY-DESIGN.md`、`DISCOVERY-IMPLEMENTATION-COVERAGE.md`、`RUNBOOK-DISCOVERY-QA-SESSION-DELETE-EXPORT.md`、`TODO.md`
    - 处理动作：同步 Discovery 设计和 Implementation Coverage，删除临时 RUNBOOK，并清空已完成 TODO。
    - 验收点：`DISCOVERY-IMPLEMENTATION-COVERAGE.md` 记录删除和 CSV 导出已交付，工作区只保留本任务相关收口变更。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
