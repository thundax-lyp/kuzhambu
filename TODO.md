# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `QaSource.java; QaRetrievalTrace.java; QaSourceRepository.java; QaRetrievalTraceRepository.java`：调整 QA 来源和 trace 领域模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSource.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaRetrievalTrace.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSourceRepository.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaRetrievalTraceRepository.java`
    - 处理动作：将来源和 trace 字段调整为 Knowledge Base provider trace 目标结构。
    - 验收点：领域代码无旧理解、扩展词、候选数量和上下文快照字段。
    - 重要度：10/10

- [ ] `QaKnowledgeSyncItem.java; QaKnowledgeSyncBatch.java; QaKnowledgeSyncItemRepository.java; QaKnowledgeSyncBatchRepository.java`：新增知识同步领域模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaKnowledgeSyncItem.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaKnowledgeSyncBatch.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaKnowledgeSyncItemRepository.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaKnowledgeSyncBatchRepository.java`
    - 处理动作：新增同步 item、batch 领域对象和仓储接口。
    - 验收点：领域架构测试通过。
    - 重要度：9/10

- [ ] `QaSessionDO.java; QaMessageDO.java; QaSourceDO.java; QaRetrievalTraceDO.java`：调整 QA 持久化 DO
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionDO.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaMessageDO.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSourceDO.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaRetrievalTraceDO.java`
    - 处理动作：让四个 DO 与最终表字段一致。
    - 验收点：MyBatis 映射编译通过。
    - 重要度：10/10

- [ ] `QaPersistenceAssembler.java; QaSessionRepositoryImpl.java; QaMessageRepositoryImpl.java`：调整会话和消息持久化映射
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/assembler/QaPersistenceAssembler.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImpl.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaMessageRepositoryImpl.java`
    - 处理动作：映射最终会话和消息字段并将 owner 查询改为 `ownerType + ownerId`。
    - 验收点：会话和消息仓储测试通过。
    - 重要度：9/10

- [ ] `QaPersistenceAssembler.java; QaSourceRepositoryImpl.java; QaRetrievalTraceRepositoryImpl.java`：调整来源和 trace 持久化映射
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/assembler/QaPersistenceAssembler.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSourceRepositoryImpl.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaRetrievalTraceRepositoryImpl.java`
    - 处理动作：映射最终来源字段和 provider trace 字段。
    - 验收点：来源和 trace 仓储测试通过。
    - 重要度：9/10

- [ ] `QaKnowledgeSyncItemDO.java; QaKnowledgeSyncBatchDO.java; QaKnowledgeSyncItemMapper.java; QaKnowledgeSyncBatchMapper.java`：新增同步表持久化对象和 Mapper
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncItemDO.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaKnowledgeSyncBatchDO.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaKnowledgeSyncItemMapper.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaKnowledgeSyncBatchMapper.java`
    - 处理动作：新增知识同步 item、batch DO 和 MyBatis Mapper。
    - 验收点：Discovery infra 编译通过。
    - 重要度：9/10

- [ ] `QaKnowledgeSyncItemRepositoryImpl.java; QaKnowledgeSyncBatchRepositoryImpl.java; QaKnowledgeSyncItemRepositoryImplTest.java; QaKnowledgeSyncBatchRepositoryImplTest.java`：实现同步仓储
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncItemRepositoryImpl.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncBatchRepositoryImpl.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncItemRepositoryImplTest.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaKnowledgeSyncBatchRepositoryImplTest.java`
    - 处理动作：实现同步 item 和 batch 仓储并覆盖状态流转测试。
    - 验收点：新增同步仓储测试通过。
    - 重要度：9/10

- [ ] `ClassicsFacade.java; ClassicsQaKnowledgeFacadeDto.java; ClassicsQaKnowledgeFacadeRequest.java; ClassicsQaKnowledgeFacadeResponse.java`：定义 Classics QA 知识读取 Facade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/ClassicsFacade.java`; `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsQaKnowledgeFacadeDto.java`; `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/request/ClassicsQaKnowledgeFacadeRequest.java`; `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsQaKnowledgeFacadeResponse.java`
    - 处理动作：定义只暴露公开、确认、可问答字段的 Classics 知识读取契约。
    - 验收点：Classics facade 架构测试通过。
    - 重要度：9/10

- [ ] `ClassicsFacadeImpl.java; ClassicsFacadeAssembler.java; ClassicsFacadeImplTest.java`：实现 Classics QA 知识读取
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImpl.java`; `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/assembler/ClassicsFacadeAssembler.java`; `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImplTest.java`
    - 处理动作：实现三类 Classics 内容的 QA 知识读取和字段映射。
    - 验收点：测试覆盖 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS`。
    - 重要度：9/10

- [ ] `KnowledgeDocument.java; KnowledgeDocumentAssembler.java; KnowledgeItemTextRenderer.java; KnowledgeDocumentAssemblerTest.java`：组装知识库文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeDocument.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeDocumentAssembler.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeItemTextRenderer.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeDocumentAssemblerTest.java`
    - 处理动作：按设计生成 `metadata` 与 `knowledge`，并仅渲染知识字段。
    - 验收点：测试证明 metadata 不进入知识文本且确认问答对参与渲染。
    - 重要度：10/10

- [ ] `KnowledgeRevisionCalculator.java; KnowledgeSourceResolver.java; KnowledgeRevisionCalculatorTest.java; KnowledgeSourceResolverTest.java`：实现知识版本和来源可见性解析
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeRevisionCalculator.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeSourceResolver.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeRevisionCalculatorTest.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeSourceResolverTest.java`
    - 处理动作：计算知识修订号并在返回来源前重查当前可见性。
    - 验收点：标签、确认问答对会改变修订号，私有来源返回 `UNAVAILABLE`。
    - 重要度：10/10

- [ ] `SyncKnowledgeContentCommand.java; KnowledgeSyncItemPageQuery.java; KnowledgeSyncItemResult.java; KnowledgeSyncApplicationService.java`：定义知识同步应用契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/SyncKnowledgeContentCommand.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/query/KnowledgeSyncItemPageQuery.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/KnowledgeSyncItemResult.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/KnowledgeSyncApplicationService.java`
    - 处理动作：定义 health、rebuild、syncContent、deleteContent、pageSyncItems 应用契约。
    - 验收点：Discovery application 编译通过。
    - 重要度：9/10

- [ ] `KnowledgeSyncApplicationServiceImpl.java; KnowledgeHealthResult.java; KnowledgeSyncBatchResult.java; KnowledgeSyncApplicationServiceImplTest.java`：实现知识同步应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeSyncApplicationServiceImpl.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/KnowledgeHealthResult.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/KnowledgeSyncBatchResult.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeSyncApplicationServiceImplTest.java`
    - 处理动作：通过 `KnowledgeBaseClient` 实现知识库确保、upsert、delete 和同步状态持久化。
    - 验收点：测试覆盖成功同步、失败同步和 provider 删除。
    - 重要度：10/10

- [ ] `ChatCompletionCommand.java; ChatCompletionResult.java; KnowledgeQaApplicationService.java; QaApplicationService.java`：定义 OpenAI-compatible QA 应用契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/ChatCompletionCommand.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/ChatCompletionResult.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/KnowledgeQaApplicationService.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/QaApplicationService.java`
    - 处理动作：新增 chat completion 契约并从正式 QA 契约移除 `askQuestion`。
    - 验收点：Application 编译无正式 QA `AskQuestionCommand` 引用。
    - 重要度：10/10

- [ ] `KnowledgeQaApplicationServiceImpl.java; QaSourceAssembler.java; QaTraceAssembler.java; KnowledgeQaApplicationServiceImplTest.java`：实现 Knowledge Base QA 应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSourceAssembler.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaTraceAssembler.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImplTest.java`
    - 处理动作：调用 `KnowledgeBaseClient.chat()` 并保存消息、来源快照和 provider trace。
    - 验收点：测试覆盖成功回答、provider 失败和不可用来源。
    - 重要度：10/10

- [ ] `QaApplicationServiceImpl.java; QaContextAssembler.java; QaApplicationServiceImplTest.java`：删除旧 QA 回答路径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaContextAssembler.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`
    - 处理动作：删除上下文拼装和 `AiFacade`、查询理解、运行时 Classics 语料依赖。
    - 验收点：Discovery application 正式 QA 路径无旧回答链路引用。
    - 重要度：10/10

- [ ] `DiscoveryQaPortalController.java; DiscoveryQaRequests.java; DiscoveryQaResponses.java; DiscoveryQaPortalInterfaceAssembler.java`：改造 Portal QA 接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalController.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/request/DiscoveryQaRequests.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/response/DiscoveryQaResponses.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/assembler/DiscoveryQaPortalInterfaceAssembler.java`
    - 处理动作：删除 `question/ask` 并新增 OpenAI-compatible `chat/completions` 接口。
    - 验收点：Portal controller 测试覆盖 session open 和 chat completions。
    - 重要度：10/10

- [ ] `DiscoveryQaAdminController.java; DiscoveryQaAdminRequests.java; DiscoveryQaAdminResponses.java; DiscoveryQaAdminInterfaceAssembler.java`：改造 Admin QA 接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminController.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/request/DiscoveryQaAdminRequests.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/response/DiscoveryQaAdminResponses.java`; `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/assembler/DiscoveryQaAdminInterfaceAssembler.java`
    - 处理动作：新增知识库 health、rebuild、sync、sync page 接口并调整 trace 响应。
    - 验收点：Admin controller 测试覆盖全部 QA admin 接口。
    - 重要度：10/10

- [ ] `portal-web qa-service.ts; qa-types.ts; qa-page.tsx; qa-page.test.tsx`：实现 Portal QA 页面闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/discovery/qa-service.ts`; `kuzhambu-apps/portal-web/src/pages/discovery/qa-types.ts`; `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.tsx`; `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.test.tsx`
    - 处理动作：将 Portal QA 页面改为 Discovery `chat/completions`、会话消息流和来源展示。
    - 验收点：Portal QA 测试覆盖首问、追问、答案、来源、失败态和移除 `/question/ask`。
    - 重要度：10/10

- [ ] `admin-web qa-admin-service.ts; qa-admin-types.ts; qa-admin-page.tsx; qa-admin-page.test.tsx; discovery-service-contract.test.ts`：实现 Admin QA 运维页面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-service.ts`; `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-types.ts`; `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.tsx`; `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.test.tsx`; `kuzhambu-apps/admin-web/src/pages/discovery/discovery-service-contract.test.ts`
    - 处理动作：实现 QA 知识库健康、重建、同步、同步列表、来源和 trace 运维视图。
    - 验收点：Admin QA 页面和 service contract 测试覆盖 health、rebuild、sync、trace 和无 provider 直连。
    - 重要度：10/10

- [ ] `kuzhambu-workers README/main/tests`：锁定 Workers 边界
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-workers/README.md`; `kuzhambu-workers/src/kuzhambu_workers/__init__.py`; `kuzhambu-workers/src/kuzhambu_workers/main.py`; `kuzhambu-workers/tests/test_ai_usecase_routes_discovery.py`; `kuzhambu-workers/tests/test_workers_architecture.py`
    - 处理动作：确认并测试 Workers 不承载正式 Discovery QA 问答或知识同步能力。
    - 验收点：Workers 无 Discovery QA runtime endpoint 或知识同步 task。
    - 重要度：8/10

- [ ] `portal qa-service; admin qa-admin-service; discovery-service-contract.test.ts; portal qa-page.test.tsx`：增加跨前端契约检查
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/discovery/qa-service.ts`; `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-service.ts`; `kuzhambu-apps/admin-web/src/pages/discovery/discovery-service-contract.test.ts`; `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.test.tsx`
    - 处理动作：锁定 Portal/Admin 只调用 Discovery API 且不直连 provider。
    - 验收点：前端测试证明无 `/question/ask` 和 provider direct URL。
    - 重要度：9/10

- [ ] `DISCOVERY-DESIGN.md; DISCOVERY-IMPLEMENTATION-COVERAGE.md; RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`：收口设计和 readiness 文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 范围对象：`docs/30-designs/DISCOVERY-DESIGN.md`; `docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`; `docs/30-designs/RUNBOOK-DISCOVERY-QA-KNOWLEDGE-BASE-REWRITE.md`
    - 处理动作：同步最终 QA 知识库设计、覆盖状态，并在实现合并后删除 RUNBOOK。
    - 验收点：目标状态文档不再把旧 QA source、debug context 或 `/question/ask` 当成目标。
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
