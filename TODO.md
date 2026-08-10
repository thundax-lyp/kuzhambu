# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `classics-wangqi-command-query-records`：04 转换王圻 Command/Query records
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-06-CLASSICS-CONTENT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/command/WangqiDocumentCommand.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/command/WangqiDocumentSourceFileCommand.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/query/WangqiDocumentQuery.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/impl/ClassicsSearchContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/wangqi/WangqiDocumentApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/search/ClassicsSearchContentApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationCommandQueryRecordAllowances.java`
    - 处理动作：将王圻 Command/Query 转为 record 并同步应用和搜索调用。
    - 验收点：三个王圻 `COMMAND_QUERY_RECORD` allowlist key 被删除且相关测试编译通过。
    - 重要度：9/10

- [ ] `classics-interface-command-construction`：05 迁移内容、明俗、王圻 controller 内 Command 构造
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-06-CLASSICS-CONTENT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/MingCustomsAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/assembler/MingCustomsInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/WangqiDocumentAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/assembler/WangqiDocumentInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/MingCustomsAdminControllerTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/WangqiDocumentAdminControllerTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`
    - 处理动作：把三个 admin controller 的 Command 构造迁移到对应 InterfaceAssembler。
    - 验收点：本范围内 `COMMAND_QUERY_CONSTRUCTION` allowlist key 被删除且三个 controller 测试编译通过。
    - 重要度：8/10

- [ ] `classics-assembler-nullness`：06 修复内容、明俗、王圻 assembler nullable contract
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-06-CLASSICS-CONTENT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/assembler/ClassicsContentApplicationAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/assembler/MingCustomsInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/assembler/WangqiDocumentInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`
    - 处理动作：让本范围 assembler public 方法返回非空契约并删除对应 nullness 例外。
    - 验收点：本范围内 assembler nullness allowlist key 被删除且 application/interface 架构测试通过。
    - 重要度：8/10

- [ ] `classics-application-service-boundary`：07 收敛内容和王圻 ApplicationService 边界
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-06-CLASSICS-CONTENT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/ClassicsContentApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/WangqiDocumentApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/support/WangqiDocumentVersionRestorer.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/wangqi/WangqiDocumentApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`
    - 处理动作：为内容和王圻裸参数服务方法建模合适入参并同步实现、调用和测试。
    - 验收点：本范围内 `METHOD_SHAPE` allowlist key 被删除且 application 架构测试通过。
    - 重要度：9/10

- [ ] `classics-content-repository-naming`：08 修复内容仓储方法命名
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-06-CLASSICS-CONTENT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/support/ClassicsContentVersioningSupport.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/mapper/ClassicsContentMapper.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsTagBindingSupport.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/support/WangqiDocumentVersionRestorer.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/test/java/com/thundax/kuzhambu/classics/domain/ClassicsDomainArchitectureTest.java`
    - 处理动作：重命名内容仓储违规方法并同步直接依赖文件。
    - 验收点：`ClassicsContentRepository` 方法命名 allowlist key 被删除且 domain/application/infra 编译通过。
    - 重要度：9/10

- [ ] `classics-mingcustoms-repository-naming`：09 修复明俗仓储方法命名
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-06-CLASSICS-CONTENT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/mingcustoms/repository/MingCustomsRepository.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/mingcustoms/repository/impl/MingCustomsRepositoryImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/mingcustoms/MingCustomsApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/mingcustoms/repository/impl/MingCustomsRepositoryTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/test/java/com/thundax/kuzhambu/classics/domain/ClassicsDomainArchitectureTest.java`
    - 处理动作：重命名 `MingCustomsRepository.deleteKeywordById` 并同步直接依赖文件。
    - 验收点：明俗仓储方法命名 allowlist key 被删除且 domain/application/infra 编译通过。
    - 重要度：8/10

- [ ] `classics-content-api-model-annotations`：10 补齐内容 API 模型注解
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-06-CLASSICS-CONTENT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/response/ClassicsContentResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`
    - 处理动作：补齐内容 Request/Response required annotations 并删除对应 allowlist。
    - 验收点：内容 API 模型注解 allowlist key 被删除且 interface 架构测试通过。
    - 重要度：8/10

- [ ] `classics-mingcustoms-api-model-annotations`：11 补齐明俗 API 模型注解
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-06-CLASSICS-CONTENT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/request/MingCustomsRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/request/MingCustomsVersionRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsCategoriesResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsKeywordCloudItemResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsTagCloudItemResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsVersionResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`
    - 处理动作：补齐明俗 Request/Response required annotations 并删除对应 allowlist。
    - 验收点：明俗 API 模型注解 allowlist key 被删除且 interface 架构测试通过。
    - 重要度：8/10

- [ ] `classics-wangqi-api-model-annotations`：12 补齐王圻 API 模型注解
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-06-CLASSICS-CONTENT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/request/WangqiDocumentRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/request/WangqiDocumentVersionRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentSourceFileResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentVersionResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`
    - 处理动作：补齐王圻 Request/Response required annotations 并删除对应 allowlist。
    - 验收点：王圻 API 模型注解 allowlist key 被删除且 interface 架构测试通过。
    - 重要度：8/10

- [ ] `classics-allowlist-06-cleanup`：13 执行验证并清理现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-06-CLASSICS-CONTENT.md`
    - 范围对象：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-06-CLASSICS-CONTENT.md`、`TODO.md`
    - 处理动作：运行 RUNBOOK 规定的 formatter、测试、静态检查，并清理已完成 RUNBOOK、已完成 TODO 和临时产物。
    - 验收点：RUNBOOK Verification 全部通过，RUNBOOK 文件被删除，`TODO.md` 不保留已完成任务，`git status` 无非任务范围文件。
    - 重要度：10/10

## 待讨论项
