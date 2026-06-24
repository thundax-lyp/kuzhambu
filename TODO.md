# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Discovery application 协议`：创建 Search 查询命令与结果模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-SKELETON.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchQuery.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchLogPageQuery.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/command/SearchClickCreateCommand.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchResult.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchGroupResult.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchLogResult.java`
    - 处理动作：按 RUNBOOK 固定 Portal 搜索、Admin 日志查询和搜索结果的 application 输入输出模型
    - 验收点：application 模型可承载高亮、分组、来源跳转字段且不依赖 interface request/response
    - 重要度：10/10

- [ ] `Discovery application 服务`：创建 Search 用例服务与检索抽象
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-SKELETON.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/SearchApplicationService.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/QueryUnderstandingApplicationService.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/SearchIndexGateway.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/SearchPermissionFilter.java`
    - 处理动作：搭建 Search 用例服务主链路、权限过滤抽象和检索后端占位异常出口
    - 验收点：检索未实现时由 application 层统一抛 `BizException`，不返回伪成功结果
    - 重要度：10/10

- [ ] `Discovery infra 持久化对象`：创建 Search DO 与 Mapper
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-SKELETON.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchLogDO.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchClickDO.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/QueryUnderstandingDO.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/SearchLogMapper.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/SearchClickMapper.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/QueryUnderstandingMapper.java`
    - 处理动作：按 RUNBOOK 表定义创建三张表的 DO 和最小 Mapper 方法签名
    - 验收点：DO 字段与 RUNBOOK 表定义一致，Mapper 只保留持久化访问方法与 `@Mapper`
    - 重要度：10/10

- [ ] `Discovery infra 仓储实现`：创建 Search PersistenceAssembler 与 RepositoryImpl
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-SKELETON.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/SearchLogPersistenceAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/SearchClickPersistenceAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/QueryUnderstandingPersistenceAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchLogRepositoryImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchClickRepositoryImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/QueryUnderstandingRepositoryImpl.java`
    - 处理动作：实现 domain 与 DO 的双向转换，并完成三类 Repository 的最小持久化实现
    - 验收点：RepositoryImpl 只做转换和 Mapper 调用，不混入查询理解、权限过滤和检索逻辑
    - 重要度：9/10

- [ ] `Discovery ES 适配`：创建 Search 索引文档与默认 Gateway
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-SKELETON.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchDocument.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/DiscoverySearchIndexProperties.java`
    - 处理动作：创建 Elasticsearch 文档模型、索引配置和默认 Gateway，占位方法统一抛未实现异常
    - 验收点：ES 代码只位于 `infra.client`，`search/rebuildIndex/upsertDocuments` 都有清晰异常出口
    - 重要度：9/10

- [ ] `Discovery portal 接口`：创建 Search Portal Controller 与协议模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-SKELETON.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalController.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/request/DiscoverySearchRequest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/request/DiscoverySearchClickRequest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/response/DiscoverySearchResponse.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/response/DiscoverySearchGroupResponse.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/assembler/DiscoverySearchPortalInterfaceAssembler.java`
    - 处理动作：落地 Portal 搜索和点击接口的 request/response/assembler 与 controller
    - 验收点：Portal 接口路径、字段、异常出口与 RUNBOOK 接口定义一致
    - 重要度：10/10

- [ ] `Discovery admin 接口`：创建 Search Admin Controller 与日志协议模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-SKELETON.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminController.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/request/DiscoverySearchLogPageRequest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/request/DiscoverySearchLogGetRequest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchLogResponse.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchLogDetailResponse.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/assembler/DiscoverySearchAdminInterfaceAssembler.java`
    - 处理动作：落地 Admin 搜索日志分页和详情接口的 request/response/assembler 与 controller
    - 验收点：Admin 接口使用 `discovery:search:view` 权限码，分页和详情字段与 RUNBOOK 一致
    - 重要度：10/10

- [ ] `Discovery 装配与测试`：接入 starter 扫描、OpenAPI 分组并补骨架测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-SKELETON.md`
    - 范围对象：`kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/java/com/thundax/kuzhambu/starter/admin/KuzhambuAdminApplication.java`、`kuzhambu-servers/starter/kuzhambu-portal-starter/src/main/java/com/thundax/kuzhambu/starter/portal/KuzhambuPortalApplication.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminControllerTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalControllerTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`
    - 处理动作：接入 Discovery 的 admin/portal 扫描与最小结构测试，锁定路径、权限和未实现异常行为
    - 验收点：starter 能扫描到 Discovery 接口，骨架测试覆盖 controller 路径和 Gateway/Application 的异常出口
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
