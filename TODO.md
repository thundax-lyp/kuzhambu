# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `D-02 discovery-domain-qa-ref-status-valueobjects`：新增 QA 引用、状态和来源值对象
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/valueobject/QaOwnerRef.java`、`QaContextContentRef.java`、`KnowledgeContentRef.java`、`KnowledgeSourceId.java`、`QaSessionStatus.java`、`QaMessageRole.java`、`QaKnowledgeSyncStatus.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/codec/QaStringValueCodec.java`
    - 处理动作：新增 QA owner、content、knowledge source 和核心状态值对象。
    - 验收点：`KnowledgeSourceId` 可表达 `SANCAI_ENTRY:1001` 业务唯一键，状态值对象不向 HTTP/DO 泄漏。
    - 重要度：9/10

- [ ] `D-03 discovery-domain-search-valueobjects`：新增 Search ID、引用、状态值对象和 codec
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/valueobject/SearchEventId.java`、`SearchClickEventId.java`、`QueryUnderstandingId.java`、`SearchOperatorRef.java`、`SearchContentRef.java`、`SearchStatus.java`、`QueryUnderstandingStatus.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/codec/SearchEventIdCodec.java`
    - 处理动作：新增 Search 强类型值对象并把主 ID 固定为 Long-backed。
    - 验收点：Search 三个主 ID 均继承 `BaseLongId`，数字字符串解析只存在于 codec 或边界转换。
    - 重要度：9/10

- [ ] `D-04 discovery-domain-qa-entity-repository-contracts`：强类型化 QA 核心实体和仓储端口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSession.java`、`QaMessage.java`、`QaKnowledgeSyncItem.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSessionRepository.java`、`QaMessageRepository.java`、`QaKnowledgeSyncItemRepository.java`
    - 处理动作：把 QA 核心实体字段和 repository 参数/返回值改为强类型。
    - 验收点：`QaKnowledgeSyncItem.sourceId` 为 `KnowledgeSourceId`，`QaKnowledgeSyncItemRepository.save` 返回 `KnowledgeSourceId` 而不是数据库 `id`。
    - 重要度：10/10

- [ ] `D-05 discovery-infra-qa-persistence-boundary`：适配 QA infra 持久化边界
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/assembler/QaPersistenceAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImpl.java`、`QaMessageRepositoryImpl.java`、`QaKnowledgeSyncItemRepositoryImpl.java`
    - 处理动作：在 infra repository 和 persistence assembler 中使用 codec 完成强类型与 DO 基础类型互转。
    - 验收点：DO 和 mapper 字段仍是基础类型，`QaKnowledgeSyncItemRepositoryImpl.save` 生成数据库 `id` 但返回 `KnowledgeSourceId`。
    - 重要度：10/10

- [ ] `D-06 discovery-application-qa-adapters`：适配 QA application 和 interface 调用链
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`、`KnowledgeQaApplicationServiceImpl.java`、`KnowledgeSyncApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSourceAssembler.java`、`QaTraceAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/assembler/DiscoveryQaAdminInterfaceAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/assembler/DiscoveryQaPortalInterfaceAssembler.java`
    - 处理动作：把 QA application 内部编排改为强类型并在 interface assembler 输出基础类型。
    - 验收点：同步条目 insert/update 仍以 `getBySourceId(KnowledgeSourceId)` 判断，portal/admin response 字段名和基础类型不变。
    - 重要度：10/10

- [ ] `D-07 discovery-search-entity-repository-infra`：强类型化 Search 实体、仓储和 infra 边界
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/entity/SearchEvent.java`、`SearchClickEvent.java`、`QueryUnderstanding.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/repository/SearchEventRepository.java`、`SearchClickEventRepository.java`、`QueryUnderstandingRepository.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/SearchEventPersistenceAssembler.java`、`SearchClickEventPersistenceAssembler.java`
    - 处理动作：把 Search domain 字段、repository 签名和 persistence assembler 改为 Long-backed 强类型。
    - 验收点：entity 内不再直接 `Long.valueOf(String)`，Search DO 保持基础类型，repository save 返回强类型 ID。
    - 重要度：10/10

- [ ] `D-08 discovery-search-application-interface`：适配 Search application 和 HTTP assembler
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`、`QueryUnderstandingApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/assembler/DiscoverySearchPortalInterfaceAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/assembler/DiscoverySearchStatisticsInterfaceAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/request/DiscoverySearchRequest.java`、`DiscoverySearchClickEventRequest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/response/DiscoverySearchResponse.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/response/DiscoverySearchEventResponse.java`
    - 处理动作：把 Search application 内部 ID、requestId、traceId 转换为强类型并保持 HTTP 基础类型契约。
    - 验收点：`searchEventId` 只接受数字字符串，`requestId`/`traceId` 进入 application 后使用 common-core `RequestId`/`TraceId`。
    - 重要度：10/10

- [ ] `D-09 discovery-search-tests`：更新 Search 后端测试 fixture 和断言
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/search/persistence/assembler/SearchEventPersistenceAssemblerTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/search/repository/impl/SearchClickEventRepositoryImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalControllerTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/assembler/DiscoverySearchPortalInterfaceAssemblerTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchStatisticsControllerTest.java`
    - 处理动作：把 Search 测试中的 repository stub、fixture 和断言改为强类型 ID 与数字字符串边界。
    - 验收点：测试中不再把 `"search-1"`、`"s-1"` 作为有效 `searchEventId`，HTTP fixture 仍使用字符串字段。
    - 重要度：9/10

- [ ] `D-10 discovery-qa-tests`：更新 QA 后端测试 fixture 和断言
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplAdminReadTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeSyncApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaMessageRepositoryImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncItemRepositoryImplTest.java`
    - 处理动作：把 QA 测试中的 repository stub、实体构造和断言改为强类型 ID。
    - 验收点：`QaKnowledgeSyncItemRepository.save` 返回 `KnowledgeSourceId` 后，测试证明 application 不再把该返回值写入数据库 `id`。
    - 重要度：9/10

- [ ] `D-11 discovery-portal-search-frontend`：验收并按需适配 portal 搜索页控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/discovery/search/search-page.tsx`、`kuzhambu-apps/portal-web/src/pages/discovery/search/components/search-controls.tsx`、`kuzhambu-apps/portal-web/src/pages/discovery/search/components/search-results.tsx`、`kuzhambu-apps/portal-web/src/pages/discovery/search/search-service.ts`、`kuzhambu-apps/portal-web/src/pages/discovery/search/search-types.ts`、`kuzhambu-apps/portal-web/src/pages/discovery/search/search-page.test.tsx`、`kuzhambu-apps/portal-web/src/pages/discovery/search/search-page-results.test.tsx`、`kuzhambu-apps/portal-web/src/pages/discovery/search/search-service.test.ts`
    - 处理动作：验收搜索输入框、筛选控件、结果点击和分页控件对数字字符串 `searchEventId` 的兼容性并按需修正。
    - 验收点：点击搜索结果会发送数字字符串 `searchEventId`，页面不展示 `{"value":1}` 或 `[object Object]`。
    - 重要度：8/10

- [ ] `D-12 discovery-portal-qa-frontend`：验收并按需适配 portal QA 页控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-page.tsx`、`kuzhambu-apps/portal-web/src/pages/discovery/qa/components/qa-composer.tsx`、`kuzhambu-apps/portal-web/src/pages/discovery/qa/components/qa-timeline.tsx`、`kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-service.ts`、`kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-types.ts`、`kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-page.test.tsx`、`kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-page-session.test.tsx`、`kuzhambu-apps/portal-web/src/pages/discovery/qa/qa-page-context.test.tsx`
    - 处理动作：验收新建会话按钮、会话列表项、提问输入框、流式回答区域和来源列表对基础类型字段的兼容性并按需修正。
    - 验收点：新建会话、选择会话、发送问题、查看来源四个操作字段名不变，来源展示仍为 `SANCAI_ENTRY:1001`。
    - 重要度：8/10

- [ ] `D-13 discovery-validation`：运行 Discovery 与 portal-web 相关验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/discovery`、`kuzhambu-apps/portal-web`
    - 处理动作：运行 RUNBOOK 指定的后端 Maven 验证和按需 portal-web 格式、lint、test。
    - 验收点：`mvn -pl biz/discovery -am spotless:check`、`checkstyle:check`、`test` 通过；如改 portal-web，则 `pnpm run format:check`、`pnpm run lint`、`pnpm --filter portal-web run test` 通过。
    - 重要度：10/10

- [ ] `D-14 discovery-runbook-cleanup`：完成强类型化闭环后清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`docs/30-designs/RUNBOOK-DISCOVERY-DOMAIN-STRONG-TYPING.md`、`TODO.md`
    - 处理动作：验证全部任务完成后删除临时 RUNBOOK，并从 TODO 中删除已完成任务或收窄剩余任务。
    - 验收点：强类型化已通过验证，RUNBOOK 文件已删除，`TODO.md` 不保留已完成任务。
    - 重要度：10/10

## 待讨论项
