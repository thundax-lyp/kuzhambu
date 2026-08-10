# ArchUnit allowlist 清理 06：Classics 内容切片

## Purpose

清理 Classics 内容、明俗、王圻相关 ArchUnit legacy allowlist，让对应 Command/Query、ApplicationService 边界、Command/Query 构造位置、Assembler 空返回、Repository 命名、Request/Response 注解规则回到默认架构约束。

本 RUNBOOK 的执行目标是：每个移除项都有明确代码修复，不通过改宽 ArchUnit 规则、迁移到新 allowlist 或删除测试规避。

## Scope

本切片只处理下列 allowlist 所在文件中的指定条目。

| 文件 | 本次处理条目 |
| --- | --- |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationCommandQueryRecordAllowances.java` | `content`、`mingcustoms`、`wangqi` package 下的 `COMMAND_QUERY_RECORD` 条目 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java` | `content`、`mingcustoms`、`wangqi` 相关 `METHOD_SHAPE`、`COMMAND_QUERY_CONSTRUCTION`、`COMMAND_QUERY_ASSEMBLER_NULL_RETURN` 条目；以及本切片修复后的 `ClassicsContentApplicationAssembler` nullness 例外 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/test/java/com/thundax/kuzhambu/classics/domain/ClassicsDomainArchitectureTest.java` | `ClassicsContentRepository`、`MingCustomsRepository` 的 Repository 方法命名例外 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java` | `admin.content`、`admin.mingcustoms`、`admin.wangqi` 的 Request/Response 注解例外；以及本切片修复后的对应 InterfaceAssembler nullness 例外 |

### Command/Query record 文件

将下列文件从 Lombok class 改为 Java record，并同步更新构造、访问器、测试断言和空值处理。

| 子域 | 文件 |
| --- | --- |
| 内容 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/AiCandidateApplyContentCommand.java` |
| 内容 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/AiCandidateBatchApplyContentCommand.java` |
| 内容 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/AiCandidateBatchRejectContentCommand.java` |
| 内容 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentExportCommand.java` |
| 内容 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentQaPairCommand.java` |
| 内容 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentQaPairSortCommand.java` |
| 内容 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentTagCommand.java` |
| 内容 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentTagSortCommand.java` |
| 明俗 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/command/MingCustomsCommand.java` |
| 明俗 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/command/MingCustomsKeywordCommand.java` |
| 明俗 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/command/MingCustomsKeywordSortCommand.java` |
| 明俗 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/query/MingCustomsQuery.java` |
| 王圻 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/command/WangqiDocumentCommand.java` |
| 王圻 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/command/WangqiDocumentSourceFileCommand.java` |
| 王圻 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/query/WangqiDocumentQuery.java` |

### Application 文件

| 文件 | 本次处理内容 |
| --- | --- |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/ClassicsContentApplicationService.java` | 修复 `sortQaPairs`、`deleteVersions`、`pageExportJobs`、`listTags`、`ensureVersioned`、`listVersions`、`listQaPairs`、`applyAiResult` 的 ApplicationService 边界例外 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java` | 同步接口签名、record 访问器、Repository 方法改名和 Command/Query 入参调整 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/assembler/ClassicsContentApplicationAssembler.java` | 修复 public assembler 方法的 nullable contract，完成后移除 application 架构测试中的该类 nullness 例外 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsTagBindingSupport.java` | 同步 `ContentTagCommand` record 访问器和构造方式 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/MingCustomsApplicationService.java` | 同步明俗 Command/Query record 后的服务签名或调用约束 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java` | 同步明俗 record 访问器、Repository 方法改名和分页查询入参 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/WangqiDocumentApplicationService.java` | 修复 `changeStorageObject` ApplicationService 边界例外 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java` | 同步王圻 record 访问器、`changeStorageObject` 入参建模和版本恢复调用 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/support/WangqiDocumentVersionRestorer.java` | 移除 support 类内直接构造 `ContentTagCommand`、`ContentQaPairCommand` 的例外 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/search/service/impl/ClassicsSearchContentApplicationServiceImpl.java` | 同步 `MingCustomsQuery`、`WangqiDocumentQuery` record 访问器 |

### Interface 文件

| 文件 | 本次处理内容 |
| --- | --- |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java` | 移除 controller 内直接构造 `ContentTagSortCommand`、`ContentQaPairSortCommand`，改由 InterfaceAssembler 负责 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java` | 修复 AI apply/batch apply/batch reject 空返回；承担内容请求到 Command/Query 的转换 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentRequest.java` | 补齐 Request 模型必需注解 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentQaPairSortRequest.java` | 如 controller 构造迁移需要，补充或调整 assembler 输入模型 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsContentTagSortRequest.java` | 如 controller 构造迁移需要，补充或调整 assembler 输入模型 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/response/ClassicsContentResponse.java` | 补齐 Response 模型必需注解 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/MingCustomsAdminController.java` | 移除 controller 内直接构造 `MingCustomsKeywordSortCommand`，改由 InterfaceAssembler 负责 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/assembler/MingCustomsInterfaceAssembler.java` | 承担明俗请求到 Command/Query 的转换，并修复 public 方法 nullable contract |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/request/MingCustomsRequest.java` | 补齐 Request 模型必需注解 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/request/MingCustomsVersionRequest.java` | 补齐 Request 模型必需注解 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/request/MingCustomsKeywordSortRequest.java` | 如 controller 构造迁移需要，补充或调整 assembler 输入模型 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsCategoriesResponse.java` | 补齐 Response 模型必需注解 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsKeywordCloudItemResponse.java` | 补齐 Response 模型必需注解 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsResponse.java` | 补齐 Response 模型必需注解 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsTagCloudItemResponse.java` | 补齐 Response 模型必需注解 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsVersionResponse.java` | 补齐 Response 模型必需注解 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/WangqiDocumentAdminController.java` | 移除 controller 内直接构造 `WangqiDocumentSourceFileCommand`，改由 InterfaceAssembler 负责 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/assembler/WangqiDocumentInterfaceAssembler.java` | 承担王圻请求到 Command/Query 的转换，并修复 public 方法 nullable contract |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/request/WangqiDocumentRequest.java` | 补齐 Request 模型必需注解 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/request/WangqiDocumentVersionRequest.java` | 补齐 Request 模型必需注解 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentResponse.java` | 补齐 Response 模型必需注解 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentSourceFileResponse.java` | 补齐 Response 模型必需注解 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/response/WangqiDocumentVersionResponse.java` | 补齐 Response 模型必需注解 |

### Domain 与 Infra 文件

| 文件 | 本次处理内容 |
| --- | --- |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java` | 修复本文件中所有 Repository 方法命名例外；`getSancaiEntryForAiApply` 虽指向三才实体，但本次按内容仓储命名例外处理 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/support/ClassicsContentVersioningSupport.java` | 同步内容仓储方法改名 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/mingcustoms/repository/MingCustomsRepository.java` | 修复 `deleteKeywordById` Repository 方法命名例外 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java` | 同步内容仓储接口方法改名 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/persistence/mapper/ClassicsContentMapper.java` | 同步内容仓储实现依赖的 mapper 方法改名 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/mingcustoms/repository/impl/MingCustomsRepositoryImpl.java` | 同步明俗仓储接口方法改名 |

### 受影响测试文件

| 文件 | 本次处理内容 |
| --- | --- |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java` | 同步内容 Command record、Service 签名和 Repository 方法改名 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java` | 同步 AI candidate Command record 和 Repository 方法改名 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/mingcustoms/MingCustomsApplicationServiceImplTest.java` | 同步明俗 Command/Query record 和 Repository 方法改名 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/wangqi/WangqiDocumentApplicationServiceImplTest.java` | 同步王圻 Command/Query record、`changeStorageObject` 和 Repository 方法改名 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/search/ClassicsSearchContentApplicationServiceImplTest.java` | 同步 `MingCustomsQuery`、`WangqiDocumentQuery` record 访问器 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java` | 同步内容 InterfaceAssembler 转换和 Command record 断言 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/MingCustomsAdminControllerTest.java` | 同步明俗 InterfaceAssembler 转换和 Query record 断言 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/WangqiDocumentAdminControllerTest.java` | 同步王圻 InterfaceAssembler 转换和 Command/Query record 断言 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/mingcustoms/repository/impl/MingCustomsRepositoryTest.java` | 同步明俗仓储方法改名 |

如果 Repository 方法改名导致发布、清理或三才测试编译失败，只允许对直接调用同一仓储方法的文件做机械同步；不得删除或修改这些子域的 allowlist 条目。

## Non-goals

不处理下列内容：

- `sancai` package 自有 Command/Query、Controller、Request/Response、Repository allowlist。
- `publication`、`cleanup`、`report`、`search` 的 ApplicationService 边界例外。
- `portal.sancai` 相关 allowlist。
- 数据库结构、接口协议语义或前端页面行为变更。

## Plan

1. 先处理 application Command/Query record：逐个文件转换为 record，保留既有构造语义；同步所有编译调用点后删除对应 `COMMAND_QUERY_RECORD` key。
2. 处理 InterfaceAssembler 和 controller 构造位置：把内容、明俗、王圻 controller 内的 Command/Query 构造迁移到对应 InterfaceAssembler；修复 assembler 空返回；删除对应 `COMMAND_QUERY_CONSTRUCTION` 和 `COMMAND_QUERY_ASSEMBLER_NULL_RETURN` key。
3. 处理 ApplicationService 边界：为内容、王圻的裸参数方法引入合适 Command/Query 或强类型入参；同步实现、调用方和测试；删除对应 `METHOD_SHAPE` key。
4. 处理 Repository 命名：重命名 `ClassicsContentRepository` 和 `MingCustomsRepository` 的违规方法；同步 impl、mapper、application 调用方和测试；删除对应 Repository 方法命名 key。
5. 处理 API 模型注解：补齐内容、明俗、王圻 Request/Response 类及内部类的 required annotations；删除对应 request/response annotation key。
6. 每完成一组删除对应 allowlist key，不保留已修复项的临时豁免。

## Verification

所有命令在 `kuzhambu-servers/` 下运行。

1. 先运行 narrowest formatter：

```sh
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-infra,biz/classics/kuzhambu-classics-interface -am spotless:apply
```

2. 再运行相关模块测试：

```sh
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-infra,biz/classics/kuzhambu-classics-interface -am test
```

3. 最后运行全 Java formatting 和静态检查：

```sh
mvn spotless:check
mvn checkstyle:check
```

验证通过前必须检查 `git diff`，确认 diff 只包含本 RUNBOOK 范围内文件，或仅包含 Repository 方法改名引起的直接机械调用方同步。

## Closure

当本 RUNBOOK Scope 中列出的 allowlist key 全部清零，且 Verification 全部通过后：

1. 删除 `docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-06-CLASSICS-CONTENT.md`。
2. 确认没有其他文档继续引用本 RUNBOOK。
3. 提交中不要保留“临时执行手册已完成”的长期文档痕迹；若执行中产生稳定架构规则，只迁移到 `docs/00-governance/` 后再删除本 RUNBOOK。
