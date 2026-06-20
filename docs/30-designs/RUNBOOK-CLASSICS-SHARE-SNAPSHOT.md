# RUNBOOK Classics Share Snapshot

## Purpose

本文档定义古籍分享快照的完整设计和实现步骤。目标是在 Admin 侧创建分享时由后端固化内容快照，并生成 Portal 访问地址；Portal API 只读取固化快照；`portal-web` 只展示 Portal API 返回的数据。

本文是临时执行手册。任务关闭并完成 PR 收口后，按 `TODO-RULES.md` 清理本 RUNBOOK 或沉淀必要稳定规则到治理/设计文档。

本 RUNBOOK 以已合入的 Classics Versionable 设计为前置基础：

- 三类主内容统一实现 `Versionable`。
- 正式版本保存在 `classics_content_version`。
- 主内容表通过 `current_version_id/current_version_no/current_versioned_at/content_updated_at` 标定当前正式版本。
- 分享创建时已经调用 `ensureVersioned(..., SHARE_CREATED, "创建分享")`，并将 `classics_share_target.content_version_id/content_version_no` 绑定到正式内容版本。
- 分享的 `content_snapshot_json` 应来自绑定的 `classics_content_version.snapshot_json`，不是 Admin Web 请求体，也不是独立的分享快照序列化逻辑。

## Scope

包含：

- Admin 创建分享请求契约调整。
- Classics application 基于正式版本绑定分享快照。
- Portal 分享查询契约调整。
- `portal-web` 分享页展示。
- Portal URL 配置和返回。
- 单元测试、契约测试、前端测试和文档同步。

不包含：

- 分享权限体系重做。
- AI/Worker/Storage 产物生成。
- 批量分享结果模型。
- 分享访问统计看板。
- 分享目标删除后的全量联动重算。

## Current Baseline

数据库已经支持分享快照和版本绑定：

- `db/schema/classics.sql`
    - `classics_content_version.snapshot_json`
    - `classics_share_target.title_snapshot`
    - `classics_share_target.content_version_id`
    - `classics_share_target.content_version_no`
    - `classics_share_target.content_snapshot_json`
    - `classics_share_target.content_visibility_snapshot`
    - `classics_share_target.target_status`

后端持久化映射已经支持分享快照：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/model/entity/ClassicsShareTarget.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsShareTargetDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/assembler/ClassicsSharingPersistenceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/repository/impl/ClassicsSharingRepositoryImpl.java`

Versionable 已提供分享创建所需的正式快照来源：

- `ClassicsContentApplicationService#ensureVersioned(...)`
- `ClassicsContentSnapshotAssembler`
- `ClassicsSharingApplicationServiceImpl#createLink`
    - 按 `contentType/contentId` 读取主内容。
    - 调用 `ensureVersioned(..., SHARE_CREATED, "创建分享")`。
    - 将 target 绑定到正式版本。
    - 用正式版本 `snapshotJson` 固化 `content_snapshot_json`。
- `ClassicsSharingAdminController#get`
    - 管理侧详情已返回分享版本和当前内容版本差异字段。

当前缺口：

- `ClassicsSharingAdminController#create` 仅返回分享 ID，没有返回 Portal 分享 URL。
- `ClassicsSharingPortalController` 当前路径使用 `tokenHash`，公开访问不应暴露或要求前端持有 hash。
- Admin request targets 仍直接使用 `ClassicsShareTarget` domain entity，接口层应改为 request DTO。
- Portal/Admin response 仍需要进一步收敛为显式 response DTO，避免公开返回 domain entity 结构。
- `portal-web` 当前没有分享页和 Portal API client。

## Architecture Decision

### Ownership

- Admin 侧提供分享创建入口和目标引用，不提供内容快照真相。
- `classics-application` 在创建分享用例中读取内容，先确保正式内容版本，再固化分享 target。
- `classics_content_version` 是分享快照的内容真相源。
- `classics_share_target` 保存分享绑定版本和展示所需冗余快照，避免 Portal 展示回查原始内容。
- Portal API 根据 shareToken 查询已固化快照，不回查原始内容作为展示真相。
- `portal-web` 展示 Portal API 返回的快照，不调用 Admin API。

### Version Boundary

分享创建属于用户显式确认动作。创建分享时：

- 如果内容未被当前正式版本覆盖，系统创建 `classics_content_version`，`change_type = SHARE_CREATED`。
- 如果内容已经被当前正式版本覆盖，系统复用最新正式版本。
- `classics_share_target.content_version_id/content_version_no` 必须记录分享绑定版本。
- `classics_share_target.content_snapshot_json` 必须等于该绑定版本的 `snapshot_json`。
- 管理侧展示差异时，对比 target 的分享绑定版本和主内容的当前正式版本；差异展示是查询态，不写回 `classics_share_target`。

分享创建不得直接把 Admin Web 提交的 `titleSnapshot/contentSnapshotJson/contentVisibilitySnapshot` 当作真相。新接口应只接收 target 引用。

### URL Boundary

固化给用户访问的链接是 Portal URL：

```text
{KUZHAMBU_PORTAL_WEB_BASE_URL}/share/{shareToken}
```

`classics_share_link` 保存业务真相字段和 `token_hash`，不保存完整 URL。Admin 创建成功后由后端根据配置组装 `shareUrl` 返回。

### ShareToken Boundary

- Admin 请求不得提供自定义 `shareToken`。
- 分享 `shareToken` 只由后端生成，语义是公开分享短码，不是登录认证 token。
- `shareToken` 使用 base64url-safe 小写字符串；生成器不得输出大写字符。
- 数据库只保存 `shareToken` hash，落库字段为 `token_hash`。
- Portal API 接收明文 `shareToken`，后端计算 hash 后查询 `token_hash`。
- 不再让 Portal 路径或前端持有 `tokenHash`。

如果为了兼容现有接口保留 `tokenHash` 字段，只作为过渡字段，不进入新前端口径。

### Visibility Boundary

- 私有内容不允许公开分享。
- 创建分享时如果任一 target 当前可见性为私有，直接拒绝创建并回滚事务。
- `content_visibility_snapshot` 保存创建分享时的可见性快照，用于 Portal 展示和管理侧追溯，不作为绕过创建校验的依据。

## Runtime Flow

### Create Share

1. Admin Web 用户选择内容并提交分享配置。
2. `ClassicsSharingAdminController#create` 接收请求。
3. `ClassicsSharingInterfaceAssembler` 将请求转换为 `ShareLinkCreateCommand`。
4. `ClassicsSharingApplicationServiceImpl#createLink` 生成 shareToken 和 shareToken hash。
5. `ClassicsSharingApplicationServiceImpl#createLink` 按 target 的 `contentType` 和 `contentId` 读取 `Versionable` 主内容。
6. application 调用 `ClassicsContentApplicationService#ensureVersioned(...)`：
    - 需要新版本时生成 `classics_content_version`。
    - 不需要新版本时复用最新正式版本。
7. application 根据返回的 `ClassicsContentVersion` 写入 target：
    - `contentVersionId`
    - `contentVersionNo`
    - `contentSnapshotJson = version.snapshotJson`
    - `titleSnapshot`
    - `contentVisibilitySnapshot`
    - `targetStatus`
8. `ClassicsSharingRepository` 写入 link 和 targets。
9. Admin API 返回：
    - `id`
    - `shareToken`
    - `shareUrl`
    - `title`
    - `visibility`
    - `status`
    - `expiresAt`
    - `targets`

### Portal Display

1. 用户打开 Portal Web 首页。
2. Portal Web 首页展示功能分类入口，例如分享和后续其他公开能力。
3. 用户进入分享入口后打开分享列表。
4. 分享列表支持按分类、时间和标题查询。
5. 用户点击某条分享后打开 `{portalWebBaseUrl}/share/{shareToken}`。
6. `portal-web` 调用 Portal API。
7. `ClassicsSharingPortalController` 接收明文 shareToken。
8. application 计算 shareToken hash，读取 link 和 targets。
9. Portal API 返回 link 元信息和 targets 快照；不得回查主内容重新组装展示内容。
10. `portal-web` 渲染只读分享页。

### Portal Share List

Portal 分享列表是公开发现入口，不替代分享详情访问规则。

- 首页入口：`/` 展示 Portal 能力分类，第一版至少包含分享入口。
- 分享列表入口：`/shares`。
- 分享详情入口：`/share/:shareToken`。
- 列表查询条件：
    - `contentType`：按分享 target 内容分类筛选。
    - `title`：按分享标题或 target 标题快照模糊搜索。
    - `createdFrom/createdTo`：按分享创建时间范围筛选。
    - `page/pageSize`：分页。
- 列表只返回可公开访问、未过期、未撤销、至少包含一个可展示 target 的分享。
- 列表项返回分享元信息和摘要，不返回完整 `content_snapshot_json`。
- 分类、时间、标题查询发生在分享列表；详情页只按 `shareToken` 读取固化快照。

## Backend File Plan

### Configuration

修改：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/...`
    - 新增配置读取或配置对象依赖，提供 Portal Web base URL。
- `kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
    - 新增 `kuzhambu.classics.share.portal-web-base-url` 占位符。
- `kuzhambu-servers/starter/kuzhambu-portal-starter/src/main/resources/application.yml`
    - 保持 Portal API 运行配置；如共享配置类需要，也补同名占位符。
- `.env.example`
    - 新增 `KUZHAMBU_PORTAL_WEB_BASE_URL=http://localhost:5174`。
- `deploy/.env.example`
    - 新增部署环境的 `KUZHAMBU_PORTAL_WEB_BASE_URL`。

候选新增：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/support/ClassicsShareProperties.java`
    - 字段：`portalWebBaseUrl`。

验收点：

- Admin starter 可以启动并绑定 Portal Web base URL 配置。
- 配置缺失时本地默认值明确，不能生成空 URL。

### Domain And Application Contracts

修改：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/ShareLinkCreateCommand.java`
    - 移除新流程对 `tokenHash` 的依赖。
    - targets 使用轻量 target 引用模型，避免接口层直接传 domain entity。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/ClassicsSharingApplicationService.java`
    - `createLink` 返回创建结果，而不是只返回 ID。
    - 新增按 shareToken 查询 Portal 分享详情的方法。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`
    - 在创建分享时生成 shareToken、shareToken hash、Portal URL 和 target 快照。
    - 在 Portal 查询时将 shareToken 转成 shareToken hash。
    - 保留现有 Versionable 绑定逻辑，不再新增独立“分享快照真相”。

候选新增：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/ShareTargetCreateCommand.java`
    - 字段：`contentType`、`contentId`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/result/ShareLinkCreateResult.java`
    - 字段：`id`、`shareToken`、`shareUrl`、`title`、`visibility`、`status`、`expiresAt`、`targets`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/result/SharePortalResult.java`
    - 字段：link 元信息、targets 快照。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/support/ClassicsShareTokenGenerator.java`
    - 生成不可预测 shareToken。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/support/ClassicsShareTokenHasher.java`
    - 计算 shareToken hash。

验收点：

- 创建分享不需要客户端提交内容快照。
- 数据库只保存 `shareToken` hash，落库字段为 `token_hash`。
- 创建结果包含可直接复制的 Portal URL。
- target 已绑定 `contentVersionId/contentVersionNo`。

### Version Snapshot Binding

现状：

- `ClassicsSharingApplicationServiceImpl#createLink` 已读取三类 `Versionable` 主内容。
- `ClassicsContentSnapshotAssembler` 已负责三类正式版本 JSON。
- `ClassicsSharingApplicationServiceImpl#createLink` 已将正式版本 JSON 写入 `content_snapshot_json`。

本 RUNBOOK 后续不得新增独立的 `ClassicsShareSnapshotJsonAssembler` 来重复定义三类内容 JSON。若分享页需要更窄的展示结构，应优先：

- 在 Portal response assembler 中从已固化的 `content_snapshot_json` 投影。
- 或稳定演进 `ClassicsContentSnapshotAssembler` 的版本 JSON schema。

正式版本 `snapshot_json` 是跨版本历史、历史恢复和分享展示复用的稳定契约，必须沉淀到 `docs/20-interfaces/`。实现上新增三类版本快照 DTO，由 `ClassicsContentSnapshotAssembler` 统一映射：

- `SancaiEntryVersionSnapshot`
- `WangqiDocumentVersionSnapshot`
- `MingCustomsVersionSnapshot`

这些 DTO 只表达正式版本快照结构，不表达分享链接、分享 target 或 Portal response。分享侧只能引用绑定版本的 JSON，不能重新定义分享专用快照结构。

正式版本快照字段至少覆盖：

- `SANCAI_ENTRY`
    - `SancaiEntry.title`
    - `SancaiEntry.originalText`
    - `SancaiEntry.translationText`
    - `SancaiEntry.summary`
    - `SancaiEntry.visibility`
    - `SancaiEntry.lifecycleStatus`
- `WANGQI_DOCUMENT`
    - `WangqiDocument.title`
    - `WangqiDocument.content`
    - `WangqiDocument.summary`
    - `WangqiDocument.visibility`
    - 时间字段按现有 entity 命名进入快照。
- `MING_CUSTOMS`
    - `MingCustomsEntry.title`
    - `MingCustomsEntry.summary`
    - `MingCustomsEntry.content`
    - `MingCustomsEntry.category`
    - `MingCustomsEntry.visibility`

目标状态规则：

- 内容存在：`AVAILABLE`。
- 内容不存在：`CONTENT_DELETED`。
- 内容类型不支持：抛出业务异常，不创建半成品 link。

验收点：

- 每个 target 在 insert 前都有非空 `titleSnapshot` 和 `contentVisibilitySnapshot`。
- `contentSnapshotJson` 是稳定 JSON，不包含后端内部类名或 Java 枚举对象结构。
- 三类正式版本快照都有明确 DTO 映射。
- `docs/20-interfaces/` 固定三类正式版本 `snapshot_json` 字段。
- 每个 target 在 insert 前都有非空 `contentVersionId/contentVersionNo`。
- `contentSnapshotJson` 与绑定的 `classics_content_version.snapshot_json` 一致。
- 任一 target 不合法时，事务回滚，不落部分分享数据。

### Admin Interface

修改：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/ClassicsSharingAdminController.java`
    - 创建接口返回 `ShareLinkCreateResult` 对应 response。
    - 不再把 request targets 直接作为 domain entity 传入 application。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/request/ClassicsSharingRequest.java`
    - targets 改为 request DTO 列表。
    - 新流程不要求 `tokenHash`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/response/ClassicsSharingResponse.java`
    - 新增 `shareToken`、`shareUrl`、`targets`。

候选新增：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/assembler/ClassicsSharingInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/request/ClassicsShareTargetRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/response/ClassicsShareTargetResponse.java`

验收点：

- Admin 创建接口请求体只表达分享配置和 target 引用。
- Admin 创建接口响应体包含 Portal URL。
- request/response JSON 字段有契约测试固定。

### Portal Interface

修改：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/ClassicsSharingPortalController.java`
    - 路径从 `/{tokenHash}/targets` 调整为 `/{shareToken}` 或 `/tokens/{shareToken}`。
    - 新增公开分享列表查询入口，支持 `contentType/title/createdFrom/createdTo/page/pageSize`。
    - 返回 link 元信息和 target 快照，不直接返回 domain entity。

候选新增：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/assembler/ClassicsSharingPortalInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/response/ClassicsSharePortalResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/response/ClassicsSharePortalTargetResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/request/ClassicsSharePortalSearchRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/response/ClassicsSharePortalListResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/response/ClassicsSharePortalListItemResponse.java`

验收点：

- Portal API 不暴露 `tokenHash`。
- Portal response 不暴露 domain entity 内部结构。
- Portal 侧过期、撤销、不存在 shareToken 统一返回 404，不区分原因，避免泄露分享链接存在性。
- Admin 管理侧可以保留真实状态展示，用于管理者排查和追溯。
- Portal 分享列表可以按分类、时间和标题查询，但列表 response 不返回完整内容快照。

## Frontend File Plan

### Admin Web

修改：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`
    - 后续接入分享创建入口时，只传 target 引用，不构造内容快照。
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
    - 如三才页面第一版发起分享，新增 share request/response 类型。

候选新增：

- `kuzhambu-apps/admin-web/src/pages/classics/share/share-service.ts`
    - 调用 `/api/classics/shares/create`。
- `kuzhambu-apps/admin-web/src/pages/classics/share/share-types.ts`
    - 定义 Admin 创建分享请求/响应。
- `kuzhambu-apps/admin-web/src/pages/classics/share/share-service-contract.test.ts`
    - 固定创建分享请求体和响应字段。

验收点：

- Admin Web 不提交 `titleSnapshot` 和 `contentSnapshotJson`。
- Admin Web 创建成功后展示或复制 `shareUrl`。

### Portal Web

修改：

- `kuzhambu-apps/portal-web/src/App.tsx`
    - 接入 router，增加 `/`、`/shares`、`/share/:shareToken` 页面。
- `kuzhambu-apps/portal-web/src/styles.css`
    - 增加分享页基础布局样式，或拆分到页面 CSS。
- `kuzhambu-apps/portal-web/src/vite-env.d.ts`
    - 声明 `VITE_PORTAL_API_BASE_URL`。

候选新增：

- `kuzhambu-apps/portal-web/src/api/http.ts`
    - Portal API JSON client。
- `kuzhambu-apps/portal-web/src/features/home/home-page.tsx`
    - Portal 首页，展示分享和后续其他公开能力入口。
- `kuzhambu-apps/portal-web/src/features/classics-share/share-list-page.tsx`
    - 分享列表页，支持分类、时间和标题查询。
- `kuzhambu-apps/portal-web/src/features/classics-share/share-page.tsx`
    - 分享页。
- `kuzhambu-apps/portal-web/src/features/classics-share/share-service.ts`
    - 调用 Portal 分享列表和详情 API。
- `kuzhambu-apps/portal-web/src/features/classics-share/share-types.ts`
    - 定义分享列表、分享详情和 target 快照类型。
- `kuzhambu-apps/portal-web/src/features/classics-share/share-list-page.test.tsx`
    - 固定列表查询条件和结果展示。
- `kuzhambu-apps/portal-web/src/features/classics-share/share-page.test.tsx`
    - 固定 shareToken 路由和快照展示。
- `kuzhambu-apps/portal-web/.env.example`
    - 新增 `VITE_PORTAL_API_BASE_URL=http://localhost:20011/api/portal`。

验收点：

- `/share/:shareToken` 可以展示 title、过期时间、目标列表和目标快照。
- `/shares` 可以按分类、时间和标题查询分享列表。
- 页面不调用 Admin API。
- 页面不需要知道 shareToken hash。

## Test Plan

### Backend Tests

新增或修改：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sharing/ClassicsSharingApplicationServiceImplTest.java`
    - 创建分享时生成 shareToken hash。
    - 创建分享时三类内容均绑定正式版本。
    - 内容未覆盖当前正式版本时，分享创建产生 `SHARE_CREATED` 版本。
    - 内容已覆盖当前正式版本时，分享创建复用既有版本。
    - `classics_share_target.content_snapshot_json` 等于绑定版本的 `snapshot_json`。
    - target 不存在时回滚。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/ClassicsSharingAdminControllerTest.java`
    - 固定 Admin 创建分享路径和 JSON 字段。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/ClassicsSharingPortalControllerTest.java`
    - 从类型存在检查升级为路径和 response contract 检查。
    - 固定过期、撤销、不存在 shareToken 的 404 响应，且 Portal response 不区分失败原因。

最小验证：

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-interface -am test
```

### Smoke Test With dev.env

使用 repo-root `dev.env` 对真实本地环境做最小冒烟：

```sh
set -a
source dev.env
set +a
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn -pl starter/kuzhambu-admin-starter -am -DskipTests install
cd starter/kuzhambu-admin-starter
mvn spring-boot:run
```

冒烟检查：

- 启动前确认 `dev.env` 指向数据库已经包含：
    - 主内容表 `current_version_id/current_version_no/current_versioned_at/content_updated_at`。
    - `classics_content_version.snapshot_json`。
    - `classics_share_target.content_version_id/content_version_no/content_snapshot_json`。
- 使用 `data.sql` 中默认用户登录 Admin Web 或直接调用 Admin API 创建分享。
- 创建分享后在数据库确认：
    - 如内容有未标定变更，新增 `classics_content_version.change_type = SHARE_CREATED`。
    - `classics_share_target.content_version_id/content_version_no` 指向该正式版本。
    - `classics_share_target.content_snapshot_json` 与绑定版本 `snapshot_json` 一致。
    - Admin 创建响应包含可访问的 `shareUrl`。
- 使用返回的 `{portalWebBaseUrl}/share/{shareToken}` 访问 Portal Web，确认页面只展示固化快照。

### Frontend Tests

新增或修改：

- `kuzhambu-apps/admin-web/src/pages/classics/share/share-service-contract.test.ts`
- `kuzhambu-apps/portal-web/src/features/classics-share/share-page.test.tsx`

最小验证：

```sh
cd kuzhambu-apps
npm run format:check
npm run lint
npm test
npm run build
```

## Execution Checklist

### Step 1 Backend Contract Preparation

- [ ] 新增 application result 和 target command。
- [ ] 新增 admin request/response target DTO。
- [ ] 新增 portal response DTO。
- [ ] 补 Admin/Portal controller contract tests。

完成标准：接口层不再直接暴露 `ClassicsShareTarget` domain entity。

### Step 2 ShareToken Support

- [ ] 新增 shareToken generator 和 hasher。
- [ ] 生成 base64url-safe 小写 shareToken，Admin 请求不得自定义 shareToken。
- [ ] 将 shareToken hash 接入 `createLink`。
- [ ] Portal 查询时将明文 shareToken 转换为 shareToken hash。

完成标准：数据库只保存 `token_hash`，Admin 创建结果返回后端生成的 `shareToken`。

### Step 3 Version Snapshot Binding

- [ ] 新增三类正式版本快照 DTO，并让 `ClassicsContentSnapshotAssembler` 使用 DTO 输出 `snapshot_json`。
- [ ] 补充/调整 `createLink` tests，覆盖 `SHARE_CREATED`、版本复用、`content_version_id/content_version_no` 和 `snapshot_json` 一致性。
- [ ] 补 application service tests。

完成标准：创建分享后 `classics_share_target` 快照字段由后端从正式内容版本生成，且 target 绑定正式版本。

### Step 4 Portal Query

- [ ] Portal controller 改为接收明文 shareToken。
- [ ] Portal controller 新增分享列表查询，支持分类、时间和标题条件。
- [ ] Portal response 返回 link 和 target 快照。
- [ ] Portal list response 返回分享摘要，不返回完整内容快照。
- [ ] 补过期、撤销、不存在 shareToken 返回 404 且不区分原因的响应测试。

完成标准：Portal API 不需要 shareToken hash，且不回查原内容作为展示真相。

### Step 5 Portal URL

- [ ] 新增 Portal Web base URL 配置。
- [ ] Admin 创建结果返回 `shareUrl`。
- [ ] 同步 `.env.example` 和 `deploy/.env.example`。
- [ ] 补配置绑定测试或 controller response contract test。

完成标准：Admin 创建成功可以得到 `{portalWebBaseUrl}/share/{shareToken}`。

### Step 6 Portal Web

- [ ] 新增 Portal API client。
- [ ] 新增 `/`、`/shares`、`/share/:shareToken` route。
- [ ] 新增 Portal 首页，展示分享和其他公开能力入口。
- [ ] 新增分享列表页面，支持分类、时间和标题查询。
- [ ] 新增分享详情页面。
- [ ] 补页面测试。

完成标准：`portal-web` 可以从首页进入分享列表，并通过 shareToken 展示固化快照详情。

### Step 7 Documentation And Readiness

- [ ] 更新 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md` 的 B1 状态。
- [ ] 在 `docs/20-interfaces/` 固定三类正式版本 `snapshot_json` schema。
- [ ] 如配置成为稳定运行时要求，更新 `docs/00-governance/SERVERS-ARCHITECTURE.md` 的 Runtime Environment。
- [ ] 用 `dev.env` 完成一次 Admin 创建分享和 Portal 访问冒烟。
- [ ] PR 收口时删除或收窄本 RUNBOOK。

完成标准：文档口径与实现一致。

## Risks

- 直接让 Admin Web 提交快照会引入篡改风险，禁止作为最终设计。
- 将完整 URL 入库会导致 Portal 域名变更后需要迁移历史数据，第一版不采用。
- Portal API 使用 `tokenHash` 会把内部存储身份暴露到公开入口，必须改为明文 shareToken 输入、后端 hash 查询。
- `content_snapshot_json` 必须保持稳定 JSON 结构，不能序列化 Java domain entity 原样结构。
- 分享快照固化后，原内容变更不应影响已创建分享展示；后续如需刷新快照，应单独设计刷新动作。
- 私有内容不允许公开分享，创建入口必须先校验所有 target 可见性。
