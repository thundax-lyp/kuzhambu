# RUNBOOK Discovery QA 会话删除/导出闭环

## 1. 目标

补齐 Discovery QA 会话删除和 CSV 导出闭环。交付完成后，Portal 用户可以删除和导出自己的未删除会话，Admin 可以删除会话并导出包含删除状态的审计 CSV。

本 RUNBOOK 是临时执行计划。PR 收口时删除本文件，并把稳定结论迁移到 `docs/30-designs/DISCOVERY-DESIGN.md` 和 `docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`。

## 2. 当前事实

- `db/schema/discovery.sql` 已存在 `discovery_qa_session.removed_at datetime(3) DEFAULT NULL`。
- `db/schema/discovery.sql` 已存在 `discovery_qa_session_export`，字段为 `id`、`export_id`、`session_id`、`format`、`storage_object_id`、`export_status`、`failure_reason`、`requester_user_id`、`requested_at`、`completed_at`。
- `QaSession` 和 `QaSessionDO` 已包含 `removedAt` 字段。
- `QaSessionRepositoryImpl.getBySessionId` 当前只按 `session_id` 查询，没有过滤 `removed_at`。
- `QaSessionRepositoryImpl.listByOwnerUserId` 当前只按 `owner_type`、`owner_id` 查询，没有过滤 `removed_at`。
- `KnowledgeQaApplicationServiceImpl.chatCompletion` 当前通过 `QaSessionRepository.getBySessionId` 获取会话，未阻止已删除会话继续追问。
- `DiscoveryQaPortalController` 当前只有 `session/open` 和 `chat/completions` 后端接口。
- `portal-web` 当前已有 `pageQaSessions` 和 `getQaSession` 前端调用，路径为 `/portal/discovery/qa/session/page` 和 `/portal/discovery/qa/session/get`。
- `StorageFacade.upload(UploadStorageFacadeRequest)` 已支持用 `InputStream` 上传文件并返回 `storageObjectId`。
- `StorageOwnerType` 当前没有 Discovery QA 导出 owner type。
- `kuzhambu-discovery-application/pom.xml` 当前未依赖 `kuzhambu-storage-facade`。

## 3. 固定决策

- 删除是软删除：设置 `status=REMOVED` 和 `removed_at=当前时间`，不物理删除 `discovery_qa_message`、`discovery_qa_message_source`、`discovery_qa_retrieval_trace` 或知识内容。
- 删除不是幂等成功：重复删除返回业务错误 `QA_SESSION_ALREADY_REMOVED` 或等价错误码。
- Portal 对已删除会话不可见、不可详情、不可追问、不可导出。
- Admin 可以查看已删除会话详情，也可以导出已删除会话审计 CSV。
- 导出格式固定为 CSV，不交付 JSON、Markdown 或 HTML。
- 导出必须写入 `discovery_qa_session_export`，并把 CSV 上传到 Storage 后写入 `storage_object_id`。
- Storage owner type 使用新增枚举 `DISCOVERY_QA_SESSION_EXPORT`，不复用 `USER`。
- CSV 文件名固定规则：`discovery-qa-session-{sessionId}-{exportId}.csv`。
- CSV content type 固定为 `text/csv; charset=UTF-8`。

## 4. 数据结构变更

### 4.1 不变更 SQL 表结构

- 不新增、删除或修改 `db/schema/discovery.sql` 的表字段。
- 不修改 `db/data/discovery.sql` 的种子数据。

### 4.2 新增 Java 数据结构

新增文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSessionExport.java`

字段：

- `Long id`
- `Long exportId`
- `Long sessionId`
- `String format`
- `Long storageObjectId`
- `String exportStatus`
- `String failureReason`
- `Long requesterUserId`
- `Date requestedAt`
- `Date completedAt`

新增文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionExportDO.java`

字段与 `QaSessionExport` 一一对应，表名为 `discovery_qa_session_export`。

新增文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSessionExportRepository.java`

方法：

- `Long save(QaSessionExport entity)`
- `int update(QaSessionExport entity)`
- `QaSessionExport getByExportId(Long exportId)`

新增文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaSessionExportMapper.java`

类型：

- `BaseMapper<QaSessionExportDO>`

新增文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionExportRepositoryImpl.java`

行为：

- `save` 生成 `id` 和 `export_id`。
- `update` 按主键更新。
- `getByExportId` 按 `export_id` 查询单条记录。

### 4.3 修改既有 Java 数据结构

文件：`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/enums/StorageOwnerType.java`

新增枚举值：

- `DISCOVERY_QA_SESSION_EXPORT`

文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaSessionResult.java`

新增字段：

- `Long removedAt`

文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaSessionDetailResult.java`

继承 `QaSessionResult` 后继续保留：

- `List<QaMessageResult> messages`

新增文件：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaSessionExportResult.java`

字段：

- `Long exportId`
- `Long sessionId`
- `String format`
- `Long storageObjectId`
- `String exportStatus`
- `String failureReason`
- `Long requestedAt`
- `Long completedAt`
- `String filename`
- `String contentType`

## 5. 任务拆分

### 任务 1：后端删除领域与仓储

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSession.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSessionRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaSessionMapper.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImplTest.java`

步骤：

- 在 `QaSession` 增加 `markRemoved(Date removedAt)` 方法，设置 `status` 为 `REMOVED`，设置 `removedAt`。
- 在 `QaSession` 增加 `isRemoved()` 方法，判断 `removedAt != null` 或 `status=REMOVED`。
- 在 `QaSessionRepository` 增加 `int markRemoved(Long sessionId, Date removedAt)`。
- 在 `QaSessionRepositoryImpl.getBySessionId` 保持 Admin 可读语义，不过滤 `removed_at`。
- 在 `QaSessionRepositoryImpl.listByOwnerUserId` 增加 `isNull("removed_at")`，Portal 列表只返回未删除会话。
- 在 `QaSessionRepositoryImpl.markRemoved` 使用条件 `session_id = ? AND removed_at IS NULL` 更新 `status` 和 `removed_at`。
- 在 `QaSessionMapper.selectByOpenedAtRange` 不过滤 `removed_at`，报表继续统计真实会话创建情况。
- 在仓储测试覆盖未删除列表过滤、首次删除成功、重复删除更新行数为 0。

验证：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery/kuzhambu-discovery-domain,biz/discovery/kuzhambu-discovery-infra -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/discovery/kuzhambu-discovery-domain,biz/discovery/kuzhambu-discovery-infra -am -DskipTests compile
```

### 任务 2：后端删除应用服务

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/DeleteQaSessionCommand.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/QaApplicationService.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`

步骤：

- 新增 `DeleteQaSessionCommand` 字段：`Long sessionId`、`String ownerType`、`String ownerId`、`Boolean adminOperation`。
- 在 `QaApplicationService` 增加 `void deleteSession(DeleteQaSessionCommand command)`。
- 在 `QaApplicationService` 增加 `List<QaSessionResult> listPortalSessions(String ownerType, String ownerId, Integer limit)`。
- 在 `QaApplicationService` 增加 `QaSessionDetailResult getPortalSessionDetail(Long sessionId, String ownerType, String ownerId)`。
- 在 `QaApplicationServiceImpl.deleteSession` 校验 `sessionId` 必填。
- Portal 删除时校验 `ownerType` 和 `ownerId` 与会话一致；Admin 删除时跳过 owner 校验。
- 会话不存在返回 `DISCOVERY-30001`。
- 会话已删除返回 `QA_SESSION_ALREADY_REMOVED`。
- 首次删除调用 `qaSessionRepository.markRemoved(sessionId, now)`；返回 0 时重新读取会话区分不存在和重复删除。
- `listPortalSessions` 使用 `qaSessionRepository.listByOwnerUserId` 返回未删除会话。
- `getPortalSessionDetail` 先校验 owner，再拒绝已删除会话。
- 在 `KnowledgeQaApplicationServiceImpl.chatCompletion` 中读取会话后调用 `session.isRemoved()`，已删除会话返回业务错误。
- 应用测试覆盖 owner 校验、Admin 删除、重复删除、已删除会话追问失败。

验证：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery/kuzhambu-discovery-application -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/discovery/kuzhambu-discovery-application -am -DskipTests compile
```

### 任务 3：Portal 删除和会话读取接口

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/request/DiscoveryQaRequests.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/response/DiscoveryQaResponses.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/assembler/DiscoveryQaPortalInterfaceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalControllerTest.java`

步骤：

- 在 `DiscoveryQaPortalController` 增加 `POST session/page`，调用 `listPortalSessions` 按 owner 查询未删除会话。
- 在 `DiscoveryQaPortalController` 增加 `POST session/get`，Portal 获取未删除会话详情；已删除会话返回业务错误。
- 在 `DiscoveryQaPortalController` 增加 `POST session/delete`，传入 `DeleteQaSessionCommand`，`ownerType=USER`，`ownerId=ownerUserId`，`adminOperation=false`。
- 在 `DiscoveryQaRequests` 新增 `QaSessionPageRequest` 字段：`Long ownerUserId`、`Integer pageNo`、`Integer pageSize`、`Integer limit`。
- 在 `DiscoveryQaRequests` 新增 `QaSessionGetRequest` 字段：`Long sessionId`、`Long ownerUserId`。
- 在 `DiscoveryQaRequests` 新增 `QaSessionDeleteRequest` 字段：`Long sessionId`、`Long ownerUserId`。
- 在 `DiscoveryQaResponses` 新增 `QaSessionResponse` 字段：`sessionId`、`ownerUserId`、`title`、`scope`、`contextMode`、`contextContentType`、`contextContentId`、`status`、`openedAt`、`lastMessageAt`。
- 在 Controller 测试覆盖 `session/page`、`session/get`、`session/delete` 路由和请求反序列化。

验证：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery/kuzhambu-discovery-interface,biz/discovery/kuzhambu-discovery-application -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/discovery/kuzhambu-discovery-interface,biz/discovery/kuzhambu-discovery-application -am -DskipTests compile
```

### 任务 4：Admin 删除接口和删除状态展示

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/request/DiscoveryQaAdminRequests.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/response/DiscoveryQaAdminResponses.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/assembler/DiscoveryQaAdminInterfaceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminControllerTest.java`

步骤：

- 在 `DiscoveryQaAdminController` 增加 `POST session/delete`，权限使用 `discovery:qa:edit`。
- 在 `DiscoveryQaAdminRequests` 新增 `QaSessionDeleteRequest` 字段：`Long sessionId`。
- 在 `DiscoveryQaAdminResponses.QaSessionDetailResponse` 新增字段 `Long removedAt`。
- 在 `DiscoveryQaAdminInterfaceAssembler` 映射 `removedAt`。
- 在 Controller 测试覆盖 `session/delete` 路由、权限注解和 `removedAt` 响应字段。

验证：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery/kuzhambu-discovery-interface,biz/discovery/kuzhambu-discovery-application -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/discovery/kuzhambu-discovery-interface,biz/discovery/kuzhambu-discovery-application -am -DskipTests compile
```

### 任务 5：导出领域、仓储和 Storage 接入

文件：

- `kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/model/enums/StorageOwnerType.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/pom.xml`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/model/entity/QaSessionExport.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/qa/repository/QaSessionExportRepository.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/dataobject/QaSessionExportDO.java`

步骤：

- 在 `StorageOwnerType` 增加 `DISCOVERY_QA_SESSION_EXPORT`。
- 在 `kuzhambu-discovery-application/pom.xml` 增加 `kuzhambu-storage-facade` 依赖。
- 新增 `QaSessionExport`，字段按本文 `4.2`。
- 新增 `QaSessionExportRepository`，方法按本文 `4.2`。
- 新增 `QaSessionExportDO`，字段按本文 `4.2`，并声明 `@TableName("discovery_qa_session_export")`。

验证：

```sh
cd kuzhambu-servers
mvn -pl biz/storage/kuzhambu-storage-domain,biz/discovery/kuzhambu-discovery-domain,biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/storage/kuzhambu-storage-domain,biz/discovery/kuzhambu-discovery-domain,biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra -am -DskipTests compile
```

### 任务 6：导出仓储实现

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/mapper/QaSessionExportMapper.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionExportRepositoryImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/qa/persistence/assembler/QaPersistenceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionExportRepositoryImplTest.java`

步骤：

- 新增 `QaSessionExportMapper extends BaseMapper<QaSessionExportDO>`。
- 在 `QaPersistenceAssembler` 增加 `toObject(QaSessionExport)` 和 `toSessionExportDomain(QaSessionExportDO)`。
- 新增 `QaSessionExportRepositoryImpl`，使用 `SnowflakeIdGenerator` 生成 `id` 和 `export_id`。
- 仓储测试覆盖 `save` 自动生成 ID、`update`、`getByExportId`。

验证：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery/kuzhambu-discovery-infra,biz/discovery/kuzhambu-discovery-domain -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/discovery/kuzhambu-discovery-infra,biz/discovery/kuzhambu-discovery-domain -am -DskipTests compile
```

### 任务 7：导出应用服务

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/command/ExportQaSessionCommand.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/result/QaSessionExportResult.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaSessionCsvExporter.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`

步骤：

- 新增 `ExportQaSessionCommand` 字段：`Long sessionId`、`Long requesterUserId`、`String ownerType`、`String ownerId`、`Boolean adminOperation`、`String format`。
- 新增 `QaSessionExportResult`，字段按本文 `4.3`。
- 新增 `QaSessionCsvExporter`，输出 UTF-8 CSV 字符串，所有字段使用标准 CSV 双引号转义。
- CSV 表头固定为：`rowType,sessionId,title,ownerType,ownerId,scope,contextMode,contextContentType,contextContentId,status,openedAt,lastMessageAt,removedAt,messageId,role,content,answerStatus,model,sentAt,answeredAt,sourceId,sourceBusinessId,sourceTitle,sourceStatus,sourceRank,traceId,provider,providerRequestId,finishReason,failureReason`。
- 在 `QaApplicationService` 增加 `QaSessionExportResult exportSession(ExportQaSessionCommand command)`。
- 在 `QaApplicationServiceImpl.exportSession` 创建 `QaSessionExport`，`format=CSV`，`exportStatus=PROCESSING`。
- Portal 导出已删除会话返回业务错误；Admin 导出已删除会话继续生成 CSV。
- 调用 `StorageFacade.upload` 上传 CSV，参数：`ownerType=DISCOVERY_QA_SESSION_EXPORT`、`ownerId=session:{sessionId}:export:{exportId}`、`originalFilename=discovery-qa-session-{sessionId}-{exportId}.csv`、`contentType=text/csv; charset=UTF-8`、`allowedSuffixes=["csv"]`。
- 上传成功后更新 `storageObjectId`、`exportStatus=SUCCEEDED`、`completedAt`。
- 生成或上传失败时更新 `exportStatus=FAILED`、`failureReason`。
- 应用测试覆盖 CSV 转义、Portal 禁止导出删除会话、Admin 允许导出删除会话、Storage 上传参数、失败记录状态。

验证：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-domain,biz/discovery/kuzhambu-discovery-infra,biz/storage/kuzhambu-storage-facade,biz/storage/kuzhambu-storage-domain -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-domain,biz/discovery/kuzhambu-discovery-infra,biz/storage/kuzhambu-storage-facade,biz/storage/kuzhambu-storage-domain -am -DskipTests compile
```

### 任务 8：Portal 导出接口

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/request/DiscoveryQaRequests.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/response/DiscoveryQaResponses.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/assembler/DiscoveryQaPortalInterfaceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalControllerTest.java`

步骤：

- Portal 增加 `POST session/export`。
- Portal `QaSessionExportRequest` 字段：`Long sessionId`、`Long ownerUserId`、`String format`。
- Portal `QaSessionExportResponse` 字段：`exportId`、`sessionId`、`format`、`storageObjectId`、`exportStatus`、`failureReason`、`requestedAt`、`completedAt`、`filename`、`contentType`。
- `format` 只接受 `CSV` 或空值；空值按 `CSV` 处理。
- Assembler 把 `ownerType=USER`、`ownerId=ownerUserId`、`adminOperation=false` 写入 `ExportQaSessionCommand`。
- Controller 测试覆盖 `session/export` 路由、请求反序列化和响应字段。

验证：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery/kuzhambu-discovery-interface,biz/discovery/kuzhambu-discovery-application -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/discovery/kuzhambu-discovery-interface,biz/discovery/kuzhambu-discovery-application -am -DskipTests compile
```

### 任务 9：Admin 导出接口

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminController.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/request/DiscoveryQaAdminRequests.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/response/DiscoveryQaAdminResponses.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/assembler/DiscoveryQaAdminInterfaceAssembler.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminControllerTest.java`

步骤：

- Admin 增加 `POST session/export`，权限使用 `discovery:qa:view`。
- Admin `QaSessionExportRequest` 字段：`Long sessionId`、`Long requesterUserId`、`String format`。
- Admin `QaSessionExportResponse` 字段：`exportId`、`sessionId`、`format`、`storageObjectId`、`exportStatus`、`failureReason`、`requestedAt`、`completedAt`、`filename`、`contentType`。
- `format` 只接受 `CSV` 或空值；空值按 `CSV` 处理。
- Assembler 把 `adminOperation=true` 写入 `ExportQaSessionCommand`。
- Controller 测试覆盖 `session/export` 路由、权限注解、请求反序列化和响应字段。

验证：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery/kuzhambu-discovery-interface,biz/discovery/kuzhambu-discovery-application -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/discovery/kuzhambu-discovery-interface,biz/discovery/kuzhambu-discovery-application -am -DskipTests compile
```

### 任务 10：Portal 前端删除和导出

文件：

- `kuzhambu-apps/portal-web/src/pages/discovery/qa-types.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-service.ts`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/discovery/qa-page.test.tsx`

步骤：

- 在 `qa-types.ts` 新增 `DiscoveryQaDeleteSessionRequest` 字段：`sessionId`、`ownerUserId`。
- 在 `qa-types.ts` 新增 `DiscoveryQaExportSessionRequest` 字段：`sessionId`、`ownerUserId`、`format`。
- 在 `qa-types.ts` 新增 `DiscoveryQaExportSessionResponse` 字段：`exportId`、`sessionId`、`format`、`storageObjectId`、`exportStatus`、`failureReason`、`requestedAt`、`completedAt`、`filename`、`contentType`。
- 在 `qa-service.ts` 新增 `deleteQaSession`，POST `/portal/discovery/qa/session/delete`。
- 在 `qa-service.ts` 新增 `exportQaSession`，POST `/portal/discovery/qa/session/export`。
- 在 `qa-page.tsx` 的会话列表或详情上增加删除按钮和确认提示。
- 删除当前选中会话成功后清空选中会话并刷新列表。
- 在 `qa-page.tsx` 增加导出 CSV 按钮；成功后展示下载信息或 Storage 文件对象入口。
- 在测试中覆盖删除确认、删除后清空当前会话、导出成功、导出失败提示。

验证：

```sh
cd kuzhambu-apps
npm --workspace portal-web run format
npm run format:check
npm run lint
npm run build
```

### 任务 11：Admin 前端删除和导出

文件：

- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-types.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-service.ts`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/discovery-service-contract.test.ts`

步骤：

- 在 `qa-admin-types.ts` 新增 `removedAt` 到会话详情类型。
- 在 `qa-admin-types.ts` 新增删除请求类型：`sessionId`。
- 在 `qa-admin-types.ts` 新增导出请求类型：`sessionId`、`requesterUserId`、`format`。
- 在 `qa-admin-types.ts` 新增导出响应类型：`exportId`、`sessionId`、`format`、`storageObjectId`、`exportStatus`、`failureReason`、`requestedAt`、`completedAt`、`filename`、`contentType`。
- 在 `qa-admin-service.ts` 新增 `deleteQaSession`，POST `/discovery/qa-admin/session/delete`。
- 在 `qa-admin-service.ts` 新增 `exportQaSession`，POST `/discovery/qa-admin/session/export`。
- 在 `qa-admin-page.tsx` 展示 `removedAt` 和 `status=REMOVED`。
- 在 `qa-admin-page.tsx` 增加删除按钮、导出 CSV 按钮和失败提示。
- 在测试中覆盖删除、已删除状态展示、导出已删除会话、服务契约路径。

验证：

```sh
cd kuzhambu-apps
npm --workspace admin-web run format
npm run format:check
npm run lint
npm run build
```

### 任务 12：测试补齐

文件：

- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalControllerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/qa/controller/DiscoveryQaAdminControllerTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionExportRepositoryImplTest.java`

步骤：

- Portal Controller 测试覆盖 `session/page`、`session/get`、`session/delete`。
- Admin Controller 测试覆盖 `session/delete` 和 `removedAt` 映射。
- 应用服务测试覆盖删除状态机、导出 CSV、Storage 上传、导出失败状态。
- 导出仓储测试覆盖 `save`、`update`、`getByExportId`。

验证：

```sh
cd kuzhambu-servers
mvn -pl biz/discovery/kuzhambu-discovery-interface,biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/discovery/kuzhambu-discovery-interface,biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-infra -am test
```

### 任务 13：文档收口

文件：

- `docs/30-designs/DISCOVERY-DESIGN.md`
- `docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-DISCOVERY-QA-SESSION-DELETE-EXPORT.md`

步骤：

- 更新 `DISCOVERY-DESIGN.md`，加入 Portal/Admin `session/delete`、`session/export`，并写明软删除、CSV、Storage、Admin 导出已删除会话审计快照。
- 更新 `DISCOVERY-IMPLEMENTATION-COVERAGE.md`，把会话删除和 CSV 导出从缺口移到已交付。
- 删除本 RUNBOOK。

验证：

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn test

cd ../kuzhambu-apps
npm run format:check
npm run lint
npm run build
npm run test
```

## 6. 提交边界

- 每个任务一个提交。
- 每个任务修改范围控制在任务列出的文件内；如发现必须新增文件，先确认是否仍属于该任务的数据结构或测试文件。
- 提交格式使用 `Type(scope): 中文说明`。
- 不把无关格式化、无关重构或临时调试改动混入提交。

## 7. 风险检查

- 仓储必须保证 Portal 列表过滤 `removed_at`，Admin 详情不被过滤。
- 应用服务必须阻止已删除会话继续追问。
- 重复删除必须返回业务错误，不得成功。
- CSV 生成必须转义逗号、换行和双引号。
- Storage owner type 必须使用 `DISCOVERY_QA_SESSION_EXPORT`。
- 导出失败必须写入 `FAILED` 和 `failure_reason`，不能留下 `PROCESSING`。
