# Storage Facade Isolation Runbook

## 1. Purpose

本文档定义 `Storage` 作为首个试点业务域的 facade 隔离方案。

目标不是立即改完所有跨域依赖，而是先建立一套可复用框架：

- 引入独立 `kuzhambu-storage-facade` 模块。
- 将跨域可见的协议边界固定为 `Facade / FacadeRequest / FacadeResponse / dto / FacadeAssembler`。
- 将实现保留在 `kuzhambu-storage-application`。
- 用 ArchUnit 建立“禁止业务域 application 直接依赖他域 application”的长期门禁。
- 通过迁移白名单保证规则可以渐进落地，而不是一次性击穿全仓。

本文档以当前代码现状为实，以“先收口 Storage，再推广到 AI / Classics / Knowledge / Discovery / Operations”为范围控制原则。

## 2. Scope

本轮覆盖：

- `Storage` 独立 facade 模块设计。
- `Storage` facade 协议对象规范。
- `Storage` facade 与 `StorageApplicationService`、`StorageUploadStreamHelper`、`StoredObjectStore` 的职责重划。
- `kuzhambu-common-test` 中新增 facade / cross-application 相关 ArchUnit 支撑。
- `Storage` 首批 facade 能力拆分方案。
- ArchUnit 白名单迁移策略。

本轮不覆盖：

- 直接完成所有业务域对 `Storage` 的迁移。
- 直接创建 `AI / Classics / Knowledge / Discovery` 的 facade 模块。
- 直接移除当前全部跨 application 依赖。
- 前端、workers 或数据库表结构改造。

## 3. Problem Statement

当前仓库已经出现多条业务域 `application -> application` 横向依赖。

其中 `Storage` 是最危险的一组，因为它已经同时以三种形态向外泄露：

1. `StorageApplicationService`
2. `StorageUploadStreamHelper`
3. `StoredObjectStore`

典型现状：

- `classics-application`
  - 直接依赖 `StorageApplicationService`
  - 直接依赖 `StorageUploadStreamHelper`
- `system-application`
  - 直接依赖 `StorageApplicationService`
  - 直接依赖 `StoredObjectStore`
- `operations-application`
  - 依赖 `ServerArtifactStorageApplicationService`

这会导致几个持续恶化的问题：

- 跨域编译依赖不断扩散。
- helper 变成事实标准 API。
- facade 语义缺位，调用者只能依赖 provider application 的内部形状。
- application 层逐渐退化成跨域编排网。

## 4. Design Decision

### 4.1 模块结构

本轮采用独立 facade 模块方案：

```text
biz/storage/
  kuzhambu-storage-facade/
  kuzhambu-storage-application/
  kuzhambu-storage-domain/
  kuzhambu-storage-infra/
  kuzhambu-storage-interface/
```

依赖方向固定为：

- `consumer-application -> kuzhambu-storage-facade`
- `kuzhambu-storage-application -> kuzhambu-storage-facade`
- `kuzhambu-storage-application -> kuzhambu-storage-domain`
- `kuzhambu-storage-interface -> kuzhambu-storage-application`

禁止：

- `consumer-application -> kuzhambu-storage-application`
- `consumer-application -> com.thundax.kuzhambu.storage.application.helper`
- `consumer-application -> com.thundax.kuzhambu.storage.application.store`

### 4.2 Facade 归位

`kuzhambu-storage-facade` 放：

- `Facade`
- `FacadeRequest`
- `FacadeResponse`
- `dto`

`kuzhambu-storage-application` 放：

- `FacadeImpl`
- `FacadeAssembler`
- 本域 application 内部 `Command / Query / Result`
- 本域编排逻辑

#### Facade 协议归位 Hard Rules

为保证 facade 模块长期可门禁，本轮同步定义以下稳定规则：

1. `XxxFacade` 只能位于 `com.thundax.kuzhambu.<domain>.facade` 包。
2. `com.thundax.kuzhambu.<domain>.facade` 包内对外公开的协议接口必须以 `Facade` 结尾。
3. `XxxFacadeRequest` 只能位于 `com.thundax.kuzhambu.<domain>.facade.request` 包。
4. `com.thundax.kuzhambu.<domain>.facade.request` 包内只能定义 `*FacadeRequest`。
5. `XxxFacadeResponse` 只能位于 `com.thundax.kuzhambu.<domain>.facade.response` 包。
6. `com.thundax.kuzhambu.<domain>.facade.response` 包内只能定义 `*FacadeResponse`。
7. `com.thundax.kuzhambu.<domain>.facade.dto` 包内只能定义 facade 协议 DTO，命名固定为 `*FacadeDto`。

说明：

- 上述规则用于同时约束“某个类型必须放到哪里”与“某个包里只能放什么”。
- `request` / `response` / `dto` 三类协议模型必须严格分包，避免 facade 模块再次演化成混杂目录。
- `FacadeImpl` 与 `FacadeAssembler` 不属于协议模型，不进入 `kuzhambu-storage-facade`。

### 4.3 Facade 对象规范

`FacadeRequest` 与 `FacadeResponse` 统一采用不可变协议风格：

- `@Getter`
- `@Builder`
- `@AllArgsConstructor(access = AccessLevel.PRIVATE)`
- 禁止 `setter`
- 禁止公开直接构造

说明：

- facade 协议对象是跨模块边界，不沿用 application `Command` 的可变对象风格。
- `dto` 优先同口径；如存在框架适配特例，再单独评审。

### 4.4 FacadeAssembler 职责

`FacadeAssembler` 放在 `kuzhambu-storage-application`，负责 provider application 内部适配：

- `FacadeRequest -> application internal params`
- `application/domain internal result -> FacadeResponse`

禁止：

- `FacadeAssembler` 访问 repository / mapper / store
- `FacadeAssembler` 承担业务判断

### 4.5 FacadeImpl 职责

`FacadeImpl` 放在 `kuzhambu-storage-application`，负责：

- 调用本域 application 内部服务
- 编排事务
- 使用 `FacadeAssembler` 做协议转换

`FacadeImpl` 不再把 `StorageApplicationService` 直接暴露给外域。

## 5. Storage Target Facades

本轮先定义 facade 框架，并收敛 Storage 对外能力为以下几类。

### 5.1 `ServerArtifactStorageFacade`

用途：

- 服务端渲染产物落库
- 主要替代 `operations-application -> ServerArtifactStorageApplicationService`

当前对应能力：

- `ServerArtifactStorageApplicationService#storeServerArtifact(...)`

### 5.2 `StorageReadableContentFacade`

用途：

- 查询对象是否可读
- 打开可读内容
- 获取只读内容摘要

当前外域消费点：

- `classics-application`
- `classics-interface`

当前散落能力：

- `StorageApplicationService#existsReadableContent(...)`
- `StorageApplicationService#openReadableContent(...)`
- `StorageApplicationService#get(...)`

### 5.3 `StorageReferenceFacade`

用途：

- 新增引用
- 删除引用
- 变更引用状态
- 查询引用

当前外域消费点：

- `classics-application`

当前散落能力：

- `StorageApplicationService#addReferences(...)`
- `StorageApplicationService#removeReferences(...)`
- `StorageApplicationService#changeReferenceStatus(...)`
- `StorageApplicationService#listReferences(...)`

### 5.4 `StorageObjectLifecycleFacade`

用途：

- 创建对象
- 变更对象元数据
- 删除对象
- 查询对象列表

当前外域消费点：

- `system-application`
- `classics-application`

当前散落能力：

- `StorageApplicationService#create(...)`
- `StorageApplicationService#change(...)`
- `StorageApplicationService#remove(...)`
- `StorageApplicationService#list(...)`

### 5.5 `StorageUploadFacade`

用途：

- 普通上传
- 上传后自动生成对象与引用
- 上传服务端临时产物

当前问题：

- `StorageUploadStreamHelper` 已经泄漏到外域。

本轮原则：

- `StorageUploadStreamHelper` 退回 storage application 内部实现细节。
- 外域只能依赖 facade，不再依赖 helper。

## 6. Current Call Sites To Migrate

### 6.1 `classics-application`

关键文件：

- `ClassicsContentApplicationServiceImpl`
- `SancaiAssetApplicationServiceImpl`
- `WangqiDocumentApplicationServiceImpl`
- `ClassicsSharingApplicationServiceImpl`

当前依赖能力：

- `StorageApplicationService`
- `StorageUploadStreamHelper`

迁移目标：

- 改为依赖 `StorageReadableContentFacade`
- 改为依赖 `StorageReferenceFacade`
- 改为依赖 `StorageObjectLifecycleFacade`
- 改为依赖 `StorageUploadFacade`

### 6.2 `system-application`

关键文件：

- `CurrentUserApplicationServiceImpl`

当前依赖能力：

- `StorageApplicationService`
- `StoredObjectStore`

迁移目标：

- 优先收敛为头像专用 facade，或先使用通用 `StorageObjectLifecycleFacade + StorageReadableContentFacade`
- `StoredObjectStore` 不再对外暴露

### 6.3 `operations-application`

关键文件：

- `DefaultOperationsReportTaskExecutor`

当前依赖能力：

- `ServerArtifactStorageApplicationService`

迁移目标：

- 改为依赖 `ServerArtifactStorageFacade`

## 7. ArchUnit Strategy

### 7.1 最终目标规则

最终硬规则应表达：

1. 业务域 `application` 模块不得依赖其他业务域 `*-application` 模块。
2. 业务域跨域协作只能依赖对端 `*-facade` 模块。
3. 业务域不得依赖对端：
   - `application.helper`
   - `application.support`
   - `application.store`
   - `application.service.impl`
4. `FacadeImpl` 与 `FacadeAssembler` 必须位于 provider 的 `*-application`。
5. `Facade`、`FacadeRequest`、`FacadeResponse`、`dto` 必须位于 `*-facade`，并遵循本 RUNBOOK 第 `4.2` 节定义的包路径与命名规则。

### 7.2 本轮白名单要求

本轮必须引入迁移白名单，否则会击穿现有项目。

白名单原则：

- 只允许记录“已知历史依赖”，不允许给新依赖开口。
- 白名单按“消费模块 -> 提供模块 -> 原因”显式列举。
- 白名单粒度固定为“模块对模块”，不细化到单文件、单类或单 import。
- 每条白名单必须带迁移理由，不接受模糊放行。

建议白名单首批包含：

- `classics-application -> storage-application`
- `system-application -> storage-application`
- `operations-application -> storage-application`
- `discovery-application -> classics-application`
- `discovery-application -> knowledge-application`
- `knowledge-application -> ai-application`
- `operations-application -> classics-application`
- `operations-application -> ai-application`
- `operations-application -> discovery-application`
- `operations-application -> knowledge-application`

### 7.3 白名单收口策略

ArchUnit 应采用“两层规则”：

1. `deny-by-default`
   - 默认禁止 application 横向依赖
2. `allowlist-for-legacy-only`
   - 仅对白名单里的历史依赖暂时放行

每次完成一组 facade 迁移后：

- 从白名单移除对应条目
- 同步补测试

说明：

- 模块级白名单更适合表达“历史遗留跨域依赖仍在迁移中”的现实状态。
- 不采用单文件粒度，避免因为文件移动、职责拆分或局部重构导致白名单频繁抖动。
- 当某个 consumer module 对某个 provider module 的依赖全部迁移完成后，应直接删除整条模块级白名单。

### 7.4 建议新增的测试支撑

建议在 `kuzhambu-common-test` 增加：

- `CrossApplicationIsolationArchitectureRuleSupport`
- `FacadeArchitectureRuleSupport`

建议在规则支撑中维护：

- `LEGACY_CROSS_APPLICATION_ALLOWLIST`
- `FACADE_MODULE_NAME_PATTERN`
- `FACADE_PROTOCOL_CLASS_RULES`

### 7.5 建议首批 ArchUnit 测试文件

- `StorageFacadeArchitectureTest`
- `ClassicsApplicationIsolationArchitectureTest`
- `SystemApplicationIsolationArchitectureTest`
- 后续再推广到：
  - `DiscoveryApplicationIsolationArchitectureTest`
  - `KnowledgeApplicationIsolationArchitectureTest`
  - `OperationsApplicationIsolationArchitectureTest`

## 8. Related Files

### 8.1 Storage 当前相关文件

- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/.../StorageApplicationService.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/.../StorageApplicationServiceImpl.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/.../StorageUploadStreamHelper.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/.../ServerArtifactStorageApplicationService.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/.../ServerArtifactStorageApplicationServiceImpl.java`
- `kuzhambu-servers/biz/storage/kuzhambu-storage-application/.../MultipartUploadApplicationService.java`

### 8.2 外域当前消费点

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/.../ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/.../SancaiAssetApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/.../WangqiDocumentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/.../ClassicsSharingApplicationServiceImpl.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/.../CurrentUserApplicationServiceImpl.java`
- `kuzhambu-servers/biz/operations/kuzhambu-operations-application/.../DefaultOperationsReportTaskExecutor.java`

### 8.3 ArchUnit 当前支撑

- `kuzhambu-servers/common/kuzhambu-common-test/.../ApiAnnotationArchitectureRuleSupport.java`
- `kuzhambu-servers/common/kuzhambu-common-test/.../ApiSurfaceArchitectureRuleSupport.java`
- `kuzhambu-servers/common/kuzhambu-common-test/.../NamingArchitectureRuleSupport.java`

## 9. Task Breakdown

以下任务用于后续生成 TODO；每项控制在 `2-6` 个文件。

### T1 建立 `kuzhambu-storage-facade` 模块骨架

范围对象：

- `biz/storage/pom.xml`
- `kuzhambu-storage-facade/pom.xml`
- `kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/package-info.java`

处理动作：

- 创建模块
- 接入 reactor
- 定义 `facade` 基础 package

### T2 定义 `ServerArtifactStorageFacade` 协议骨架

范围对象：

- `kuzhambu-storage-facade/.../facade/ServerArtifactStorageFacade.java`
- `kuzhambu-storage-facade/.../facade/request/StoreServerArtifactFacadeRequest.java`
- `kuzhambu-storage-facade/.../facade/response/StoreServerArtifactFacadeResponse.java`

处理动作：

- 固定 `Getter + Builder + private constructor`
- 禁止 setter

### T3 定义 `StorageReadableContentFacade` 协议骨架

范围对象：

- `kuzhambu-storage-facade/.../facade/StorageReadableContentFacade.java`
- `kuzhambu-storage-facade/.../facade/request/GetReadableContentFacadeRequest.java`
- `kuzhambu-storage-facade/.../facade/response/GetReadableContentFacadeResponse.java`
- `kuzhambu-storage-facade/.../facade/dto/ReadableStoredObjectFacadeDto.java`

处理动作：

- 固定 `Getter + Builder + private constructor`
- 禁止 setter

### T4 定义 `StorageReferenceFacade` 协议骨架

范围对象：

- `kuzhambu-storage-facade/.../facade/StorageReferenceFacade.java`
- `kuzhambu-storage-facade/.../facade/request/AddStorageReferencesFacadeRequest.java`
- `kuzhambu-storage-facade/.../facade/request/RemoveStorageReferencesFacadeRequest.java`
- `kuzhambu-storage-facade/.../facade/request/ChangeStorageReferenceStatusFacadeRequest.java`
- `kuzhambu-storage-facade/.../facade/response/StorageReferenceFacadeResponse.java`

处理动作：

- 固定 `Getter + Builder + private constructor`
- 禁止 setter

### T5 实现 `ServerArtifactStorageFacadeImpl` 与 `FacadeAssembler`

范围对象：

- `kuzhambu-storage-application/.../facade/impl/ServerArtifactStorageFacadeImpl.java`
- `kuzhambu-storage-application/.../facade/assembler/ServerArtifactStorageFacadeAssembler.java`
- `kuzhambu-storage-application/.../service/impl/ServerArtifactStorageApplicationServiceImpl.java`

处理动作：

- 由 facade impl 调用 `ServerArtifactStorageApplicationServiceImpl`
- 完成 `FacadeRequest -> internal params -> FacadeResponse`

### T6 实现 `StorageReadableContentFacadeImpl` 与 `FacadeAssembler`

范围对象：

- `kuzhambu-storage-application/.../facade/impl/StorageReadableContentFacadeImpl.java`
- `kuzhambu-storage-application/.../facade/assembler/StorageReadableContentFacadeAssembler.java`
- `kuzhambu-storage-application/.../service/StorageApplicationService.java`
- `kuzhambu-storage-application/.../service/content/StoredObjectContent.java`

处理动作：

- 由 facade impl 调用 `StorageApplicationService`
- 收敛只读内容相关跨域出口

### T7 实现 `StorageReferenceFacadeImpl` 与 `FacadeAssembler`

范围对象：

- `kuzhambu-storage-application/.../facade/impl/StorageReferenceFacadeImpl.java`
- `kuzhambu-storage-application/.../facade/assembler/StorageReferenceFacadeAssembler.java`
- `kuzhambu-storage-application/.../service/StorageApplicationService.java`
- `kuzhambu-storage-application/.../service/command/AddStorageReferencesCommand.java`
- `kuzhambu-storage-application/.../service/command/RemoveStorageReferencesCommand.java`
- `kuzhambu-storage-application/.../service/command/ChangeStorageReferenceStatusCommand.java`

处理动作：

- 由 facade impl 调用 `StorageApplicationService`
- 收敛引用关系相关跨域出口

### T8 引入 cross-application isolation rule 与迁移白名单

范围对象：

- `kuzhambu-common-test/.../CrossApplicationIsolationArchitectureRuleSupport.java`
- `kuzhambu-common-test/.../ArchitectureSourceSupport.java`
- `biz/storage/kuzhambu-storage-application/src/test/java/.../StorageApplicationArchitectureTest.java`

处理动作：

- 新增 cross-application isolation rule
- 新增 legacy allowlist

### T9 引入 facade placement rule 与 facade 协议门禁

范围对象：

- `kuzhambu-common-test/.../FacadeArchitectureRuleSupport.java`
- `biz/storage/kuzhambu-storage-facade/src/test/java/.../StorageFacadeArchitectureTest.java`
- `kuzhambu-common-test/.../NamingArchitectureRuleSupport.java`

处理动作：

- 新增 facade placement rule
- 新增 facade request / response / dto 包路径与命名门禁

### T10 首批迁移 `operations-application -> storage-facade`

范围对象：

- `operations/.../DefaultOperationsReportTaskExecutor.java`
- `storage-facade/.../ServerArtifactStorageFacade.java`
- `operations/.../DefaultOperationsReportTaskExecutorTest.java`

处理动作：

- 将 `ServerArtifactStorageApplicationService` 替换为 facade

### T11 首批迁移 `classics-application` 的一个最小上传入口

范围对象：

- `classics/.../SancaiAssetApplicationServiceImpl.java`
- `storage-facade/.../StorageUploadFacade.java`
- `classics/.../SancaiAssetApplicationServiceImplTest.java`

处理动作：

- 将 `SancaiAssetApplicationServiceImpl` 的上传入口改为 facade
- 开始移除 `StorageUploadStreamHelper` 外溢

### T12 首批迁移 `classics-application` 的一个最小只读内容入口

范围对象：

- `classics/.../ClassicsSharingApplicationServiceImpl.java`
- `storage-facade/.../StorageReadableContentFacade.java`
- `classics/.../ClassicsSharingApplicationServiceImplTest.java`

处理动作：

- 将 `ClassicsSharingApplicationServiceImpl` 的只读内容调用改为 facade

### T13 首批迁移 `system-application -> storage-facade` 的一个最小头像入口

范围对象：

- `system/.../CurrentUserApplicationServiceImpl.java`
- `storage-facade/.../StorageReadableContentFacade.java`
- `system/.../CurrentUserApplicationServiceImplTest.java`（新增）

处理动作：

- 将 `CurrentUserApplicationServiceImpl#getAvatarInputStream` 改为 facade

## 10. Success Criteria

RUNBOOK 对应方案被接受后，本轮实现完成时至少要满足：

1. 仓库存在独立 `kuzhambu-storage-facade` 模块。
2. `Storage` 对外跨域能力不再直接以 `StorageApplicationService` 和 `StorageUploadStreamHelper` 作为默认入口。
3. ArchUnit 能识别 facade 模块放置、协议对象风格和 application 横向依赖。
4. ArchUnit 带迁移白名单，能防止新增坏依赖，同时允许旧依赖渐进消减。
5. 至少有一条现有 `application -> storage-application` 依赖被迁移到 `storage-facade`。

## 11. Review Questions

请重点审核以下问题：

1. `FacadeAssembler` 是否确认放在 `xxx-application`，负责 `FacadeRequest -> Command/Query` 与 `Result -> FacadeResponse`？
2. `FacadeRequest / Response` 是否确认统一采用 `Getter + Builder + private constructor`？
3. Storage 首批 facade 分类是否合适，还是需要先收敛成更少的 2-3 个 facade？
4. ArchUnit 白名单的粒度是否接受按“消费模块 -> 提供模块”记录，而不是细到单文件？
5. `operations -> storage` 是否作为首批最小迁移样例先落地？
