# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `索引重建应用服务`：新增 SearchIndexApplicationService
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-RUNTIME.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchIndexApplicationService.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/SearchContentProvider.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchIndexApplicationServiceImplTest.java`
    - 处理动作：新增 application 层索引重建入口，串联内容读取和索引文档生成，不引入增量同步或后台调度
    - 验收点：Application 层存在明确索引重建用例入口，且单元测试不依赖真实 ES
    - 重要度：10/10

- [ ] `关键词检索实现`：实现 ES search 的真实结果项返回
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-RUNTIME.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchDocument.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`
    - 处理动作：实现关键词检索、分页和 SearchResult 映射，保证返回真实内容项
    - 验收点：search 不再抛未实现异常，结果项包含 contentType、contentId、title、summary、targetPath
    - 重要度：10/10

- [ ] `过滤与分组出参`：补齐基础过滤和 SearchGroupResult
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-RUNTIME.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchGroupResult.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`
    - 处理动作：在真实检索基础上增加 knowledgeBases、categoryCodes、contentStatuses、visibilityScopes 过滤，并按 contentType 分组
    - 验收点：Search 接口返回真实分组结果，过滤条件能影响命中结果
    - 重要度：10/10

- [ ] `真实搜索日志`：在 SearchApplicationServiceImpl 中落成功与失败日志
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-RUNTIME.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchLog.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchLogRepositoryImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`
    - 处理动作：实现搜索成功与失败路径的真实日志写入，并写入 searchScopesJson
    - 验收点：search 返回的 searchLogId 来自真实落库对象，成功/失败日志都有测试覆盖
    - 重要度：10/10

- [ ] `点击日志串联`：校准 click 接口与真实 searchLogId
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-RUNTIME.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/assembler/DiscoverySearchPortalInterfaceAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalControllerTest.java`
    - 处理动作：确保点击日志与真实 searchLogId 串联，必要时补点击前校验
    - 验收点：Search 和 Click 能通过 searchLogId 稳定串联
    - 重要度：8/10

- [ ] `索引重建管理入口`：新增 Admin index rebuild 接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-RUNTIME.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminController.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/request/DiscoverySearchIndexRebuildRequest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminControllerTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchIndexApplicationService.java`
    - 处理动作：新增 Admin 手动重建入口，权限码固定为 discovery:search:edit
    - 验收点：可通过后端接口手动触发一次全量重建，接口定位为运维入口
    - 重要度：9/10

- [ ] `Admin 日志详情真实化`：返回真实范围和失败信息
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-RUNTIME.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/assembler/DiscoverySearchAdminInterfaceAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchLogDetailResponse.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminControllerTest.java`
    - 处理动作：确保详情接口直接返回真实落库的 searchScopesJson、failureCode、failureMessage、requestId、traceId
    - 验收点：管理端详情响应与落库内容一致，不在接口层拼装伪 JSON
    - 重要度：8/10

- [ ] `Starter 装配验证`：确认 Discovery Search 运行时 bean 和扫描路径完整
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-RUNTIME.md`
    - 范围对象：`kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/java/com/thundax/kuzhambu/starter/admin/KuzhambuAdminApplication.java`、`kuzhambu-servers/starter/kuzhambu-portal-starter/src/main/java/com/thundax/kuzhambu/starter/portal/KuzhambuPortalApplication.java`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`、`kuzhambu-servers/starter/kuzhambu-portal-starter/src/test/java/com/thundax/kuzhambu/starter/portal/PortalStarterArchitectureTest.java`
    - 处理动作：确认 starter 扫描包和 mapper 包覆盖 Discovery Search 所需 bean
    - 验收点：不出现 SearchContentProvider、SearchIndexGateway、SearchIndexApplicationService 缺 bean
    - 重要度：8/10

- [ ] `运行时测试收口`：补路径测试并完成最小 Maven 验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-RUNTIME.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminControllerTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalControllerTest.java`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`、`kuzhambu-servers/starter/kuzhambu-portal-starter/src/test/java/com/thundax/kuzhambu/starter/portal/PortalStarterArchitectureTest.java`
    - 处理动作：为新增 index rebuild 入口补路径测试，并按 RUNBOOK 执行最小 Maven 验证
    - 验收点：新增接口路径稳定，本轮运行时闭环的最小测试链路可通过
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
