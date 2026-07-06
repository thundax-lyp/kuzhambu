# Classics 私有分享访问闭环 RUNBOOK

## 目标

Classics 分享访问支持公开与私有两条稳定分支：

- 公开分享继续通过 `GET /api/portal/classics/shares/{shareToken}` 和资源读取接口免登录访问。
- 私有分享只允许创建者和具备 `classics:sharing:view` 权限的管理员访问。
- 私有分享未登录时给出登录引导，不返回内容快照或资源内容；已登录但无权限时按不可访问处理。
- 过期、撤销、不存在的分享继续按不可访问处理。
- 分享访问与资源读取成功后写入访问记录并递增访问次数。

## 数据结构变更

- 表：`classics_share_link`
- 新增字段：`created_by_user_id bigint DEFAULT NULL`
- 新增索引：`idx_classics_share_link_creator (created_by_user_id, visibility)`
- 字段语义：记录分享链接创建者的用户 ID，用于私有分享创建者访问判定；历史数据允许为空，空值私有分享只能由具备 `classics:sharing:view` 的管理员访问。
- Schema 文件：`db/schema/classics.sql`
- 设计文档：`docs/30-designs/CLASSICS-DESIGN.md`
- 发布确认：若生产发布依赖迁移脚本，需要补充等价 `ALTER TABLE classics_share_link ADD COLUMN created_by_user_id bigint DEFAULT NULL` 与索引创建脚本；若当前阶段以 `db/schema/classics.sql` 为权威 schema，则不新增迁移文件。

## 后端任务拆解

### 任务 1：分享创建者入库

相关文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/model/entity/ClassicsShareLink.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsShareLinkDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/assembler/ClassicsSharingPersistenceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/ShareLinkCreateCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/ClassicsSharingAdminController.java`

验收口径：

- `ClassicsShareLink`、`ClassicsShareLinkDO` 均包含 `createdByUserId`。
- 持久化 assembler 双向映射 `createdByUserId`。
- 单条创建和批量创建分享时，从 `KuzhambuContextHolder.currentSubjectId()` 读取当前用户 ID，写入 `ShareLinkCreateCommand.operatorUserId`，最终落到 `createdByUserId`。
- 当前 subject ID 必须能解析为数字用户 ID；无法解析时不写创建者，私有分享仅允许管理员权限访问。

### 任务 2：私有分享应用服务闭环

相关文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/ClassicsSharingApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sharing/ClassicsSharingApplicationServiceImplTest.java`

验收口径：

- 公开详情接口遇到有效私有分享时返回认证要求错误码 `CLASSICS-14002`，不返回内容快照。
- 新增 `getPrivatePortalShare(String shareToken, Long currentUserId, Set<String> currentPermissions)`。
- 新增 `getPrivatePortalShareResourceContent(String shareToken, Long storageObjectId, boolean download, Long currentUserId, Set<String> currentPermissions)`。
- 私有分享访问通过条件为：`currentUserId` 等于 `createdByUserId`，或 `currentPermissions` 包含 `classics:sharing:view`。
- 私有分享无权限、过期、撤销、不存在时均按不可访问处理。
- 私有详情和私有资源读取成功后记录访问并递增访问次数。

### 任务 3：Portal 接口契约

相关文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/ClassicsSharingPortalController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/ClassicsSharingPrivatePortalController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/assembler/ClassicsSharingPortalInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/response/ClassicsSharePortalResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/ClassicsSharingPortalControllerTest.java`

验收口径：

- 公开详情：`GET /api/portal/classics/shares/{shareToken}`。
- 公开资源：`GET /api/portal/classics/shares/{shareToken}/resources/{storageObjectId}/content`。
- 私有详情：`GET /api/portal/classics/private-shares/{shareToken}`。
- 私有资源：`GET /api/portal/classics/private-shares/{shareToken}/resources/{storageObjectId}/content`。
- `ClassicsSharePortalResponse` 新增 `loginRequired: Boolean`。
- 公开详情遇到私有分享时返回 `visibility=PRIVATE`、`loginRequired=true`、空 targets，不返回资源 URL。
- 私有详情返回 `loginRequired=false`，资源 URL 使用 `/api/portal/classics/private-shares/.../content`。
- 私有资源接口从 `KuzhambuContextHolder` 读取当前用户和权限；图片、链接、下载类访问可通过既有 `token` 查询参数完成认证。

### 任务 4：文档与覆盖度收口

相关文件：

- `docs/20-interfaces/CLASSICS-SHARE-PORTAL-INTERFACE.md`
- `docs/30-designs/CLASSICS-DESIGN.md`
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-CLASSICS-PRIVATE-SHARE-ACCESS.md`

验收口径：

- 接口文档明确公开与私有 Portal 路径、`loginRequired` 响应、私有资源访问规则。
- 设计文档明确 `created_by_user_id` 字段。
- 覆盖度文档不再把私有分享访问列为未完成主干缺口。
- RUNBOOK 只描述目标态交付和验证，不保留执行中的临时状态。

## Portal Web 任务拆解

### 任务 1：HTTP 与服务层

相关文件：

- `kuzhambu-apps/portal-web/src/api/http.ts`
- `kuzhambu-apps/portal-web/src/pages/share/share-service.ts`
- `kuzhambu-apps/portal-web/src/pages/share/share-types.ts`
- `kuzhambu-apps/portal-web/src/pages/share/share-service.test.ts`

验收口径：

- `http.ts` 提供带 `Access-Token` 请求头的 JSON GET 方法。
- `share-types.ts` 中 `ClassicsSharePortalResponse` 包含 `loginRequired?: boolean | null`。
- `share-types.ts` 中资源 URL 命令包含 `privateAccess?: boolean`。
- `share-service.ts` 先调用公开详情接口。
- 公开详情返回 `loginRequired=true` 且本地存在 token 时，调用私有详情接口。
- 本地 token 读取顺序为 `kuzhambu.portal.accessToken`，再回退到 `kuzhambu.admin.accessToken`。
- 私有资源 URL 使用 `/api/portal/classics/private-shares/{shareToken}/resources/{storageObjectId}/content`，并追加 `token` 查询参数。
- service 测试覆盖：无 token 登录引导、有 token 私有详情回退、私有资源 URL、公开资源 URL。

### 任务 2：分享详情页控件与操作

相关文件：

- `kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`
- `kuzhambu-apps/portal-web/src/pages/share/share-form.test.tsx`

控件与操作：

- 页面加载操作：读取路由中的 `shareToken`，调用 `shareService.getAccessibleShare(shareToken)`。
- 加载态控件：保持既有分享详情加载占位。
- 登录引导控件：当响应 `loginRequired=true` 时展示 `aria-label="私有分享登录引导"` 的提示卡片。
- 登录引导文案：说明该链接为私有分享，需要登录后访问。
- 内容详情控件：私有分享鉴权成功后复用公开分享详情的标题、摘要、目标列表、资源列表只读展示。
- 资源预览操作：私有分享资源预览必须调用 `shareService.getShareResourceContentUrl(..., privateAccess=true)`。
- 资源下载操作：私有分享下载链接必须使用私有资源路径并携带 token 查询参数。
- 无权限错误操作：私有接口返回不可访问时，页面按既有不可访问错误态展示，不显示登录引导。
- page 测试覆盖：私有分享登录引导、已有 token 私有分享展示、私有资源预览 URL。

## 安全与产品确认

- 私有分享当前不是 ACL 分享；访问对象限定为创建者和具备 `classics:sharing:view` 的管理员。
- 未登录用户可以知道链接是私有分享，但不能看到标题、目标、资源或内容快照。
- 已登录但无权限用户不区分“链接存在但无权限”和“链接不存在”。
- 私有资源 URL 通过 `token` 查询参数支持图片预览和下载；后续如需降低日志和 Referer 风险，可扩展为短期资源票据。
- Portal Web 若后续接入统一登录页，登录引导控件可增加明确登录按钮；当前闭环只要求提示与已有 token 自动读取。

## 验证

后端验证：

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-interface -am spotless:apply
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-interface -am spotless:check
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-interface -am test
```

Portal Web 验证：

```sh
cd ../kuzhambu-apps
npm --workspace @kuzhambu/portal-web run format
npm run format:check
npm --workspace @kuzhambu/portal-web run test -- share
```
