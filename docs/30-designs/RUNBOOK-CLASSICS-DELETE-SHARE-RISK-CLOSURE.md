# Classics 删除后分享风险闭环 RUNBOOK

## 目标

完成 Classics 三类内容删除后的分享安全闭环：当 `SANCAI_ENTRY`、`WANGQI_DOCUMENT` 或 `MING_CUSTOMS` 被删除时，所有仍引用该内容的分享目标同步为 `CONTENT_DELETED`，关联分享链接按剩余可用目标重算风险态，Admin Web 和 Portal Web 按删除状态展示占位，并禁止继续展示或读取已删除目标的正文、图片和文件资源。

完成后，`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md` 中“条目查看、创建、编辑、删除”改为 `已完成`；跨知识库分享中“目标被删除后占位展示”说明删除动作已触发状态同步、风险重算和 Admin Web / Portal Web 占位展示。

## 已确认口径

- 删除内容不改变 `classics_share_link.status`：`ACTIVE` 仍为 `ACTIVE`，`EXPIRED/REVOKED` 仍保持原状态。
- 删除内容不重建、不删除、不改写 `classics_share_target.content_snapshot_json`，但前台不再渲染其中正文、图片或文件。
- 风险态表达“当前仍可访问目标”的风险；`CONTENT_DELETED` 目标不参与风险态计算。
- 所有目标都删除后，`classics_share_link.visibility_risk_status` 写为 `PUBLIC_ONLY`。
- Admin Web 保留管理可追溯性：展示目标行、标题快照、内容类型、删除状态，不提供打开已删内容或预览/下载资源入口。
- Portal Web 保留位置解释：展示标题快照和“内容已删除”占位，不展示正文快照、不展示图片、不展示文件元信息、不展示下载/预览按钮。
- Portal 公开分享列表只展示 `AVAILABLE/ACTIVE` 目标；全部目标删除的分享不进入列表，但 token 直达详情仍返回删除占位。

## 数据结构

本任务不新增数据库表或字段。必须精确使用现有字段：

### `db/schema/classics.sql` / `classics_share_link`

- `status varchar(16)`：不因内容删除变更。
- `visibility_risk_status varchar(16)`：删除同步后重算并更新，取值只使用现有 `PUBLIC_ONLY`、`CONTAINS_PRIVATE`。
- `access_count bigint`：不因内容删除变更。

### `db/schema/classics.sql` / `classics_share_target`

- `content_type varchar(32)` + `content_id bigint`：删除同步定位条件。
- `target_status varchar(16)`：被删内容对应目标从 `AVAILABLE` 或历史兼容值 `ACTIVE` 更新为 `CONTENT_DELETED`。
- `title_snapshot varchar(512)`：Admin Web / Portal Web 删除占位标题来源，必须保留。
- `content_snapshot_json json`：保留历史快照，但 Portal Web 对 `CONTENT_DELETED` 不解析渲染正文、图片或文件。
- `content_visibility_snapshot varchar(16)`：只在 `target_status` 为 `AVAILABLE/ACTIVE` 时参与风险重算。
- `content_version_id bigint`、`content_version_no int`、`priority int`：删除同步不得改写。

### 枚举兼容

- `ClassicsShareTargetStatus` 已有 `AVAILABLE`、`CONTENT_DELETED`，且 `from("ACTIVE")` 会兼容为 `AVAILABLE`。
- 持久化更新时统一写入 `CONTENT_DELETED`，不再写历史 `ACTIVE`。
- Portal 列表 SQL 继续只选 `AVAILABLE/ACTIVE`，不把 `CONTENT_DELETED` 推入公开发现列表。

## 小任务拆解

### 任务 1：后端分享同步能力

目标：提供一个可由三类删除入口调用的同步能力，完成目标状态同步和风险态重算。

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/ClassicsSharingApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/repository/ClassicsSharingRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/repository/impl/ClassicsSharingRepositoryImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/mapper/ClassicsShareTargetMapper.java`

实现要求：

- 在 `ClassicsSharingApplicationService` 新增方法：`void syncContentDeleted(ClassicsContentType contentType, Long contentId)`。
- `ClassicsSharingRepository` 新增方法：
  - `List<ClassicsShareTarget> listTargetsByContent(ClassicsContentType contentType, Long contentId)`
  - `int markTargetsContentDeleted(ClassicsContentType contentType, Long contentId)`
  - `int updateLinkVisibilityRiskStatus(ClassicsShareLinkId id, SancaiVisibilityRiskStatus visibilityRiskStatus)`
- `markTargetsContentDeleted` 的更新条件必须限定：
  - `content_type = contentType.value()`
  - `content_id = contentId`
  - `target_status in ('AVAILABLE', 'ACTIVE')`
  - 更新字段只允许 `target_status = 'CONTENT_DELETED'`
- `syncContentDeleted` 先读取受影响 targets，收集 `shareLinkId`；再批量标记删除；最后逐个重算这些 link 的 `visibility_risk_status`。
- 风险重算只读取 `repository.listTargetsByLinkId(linkId, SortDirection.ASC)` 中 `targetStatus == AVAILABLE` 的 targets。存在任一 `contentVisibilitySnapshot == PRIVATE` 时写 `CONTAINS_PRIVATE`，否则写 `PUBLIC_ONLY`。
- 该方法必须幂等：内容没有分享目标、目标已是 `CONTENT_DELETED`、重复删除同步都不抛错。

### 任务 2：三类内容删除入口接入

目标：所有内容删除入口在同一事务内触发分享同步，避免删除后分享状态悬空。

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sharing/ClassicsSharingApplicationServiceImplTest.java`

实现要求：

- 在 `SancaiApplicationServiceImpl.deleteEntry` 中，确认条目存在并生成删除版本后、物理删除前调用 `sharingApplicationService.syncContentDeleted(ClassicsContentType.SANCAI_ENTRY, id.value())`。
- 在 `WangqiDocumentApplicationServiceImpl.delete` 中，确认文档存在并生成删除版本后、`contentApplicationService.deleteVersions(...)` 前调用 `syncContentDeleted(ClassicsContentType.WANGQI_DOCUMENT, id.value())`。
- 在 `MingCustomsApplicationServiceImpl.delete` 中，确认条目存在并生成删除版本后、物理删除前调用 `syncContentDeleted(ClassicsContentType.MING_CUSTOMS, id.value())`。
- 三个删除入口继续使用现有事务；同步失败必须回滚删除。
- 不为删除同步生成新的 `classics_content_version`；删除版本仍由现有 `ensureVersioned(..., MANUAL_SAVE, "手动删除")` 负责。

测试要求：

- 覆盖 Sancai、Wangqi、Ming Customs 三类 content type 的同步调用。
- 覆盖“公开 + 私有”同链接目标中删除私有目标后，link 风险态从 `CONTAINS_PRIVATE` 重算为 `PUBLIC_ONLY`。
- 覆盖全部目标删除后，link `status` 不变且 `visibility_risk_status = PUBLIC_ONLY`。

### 任务 3：后端接口与持久化契约

目标：保证 Admin / Portal 都能拿到删除状态，Portal 资源读取继续阻断删除目标。

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/response/ClassicsSharingResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/response/ClassicsSharePortalTargetResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/ClassicsSharingAdminControllerTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/ClassicsSharingPortalControllerTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/sharing/ClassicsSharingPersistenceMappingTest.java`

实现要求：

- `ClassicsSharingResponse.Target.targetStatus` 继续输出 `CONTENT_DELETED`，不新增字段。
- `ClassicsSharePortalTargetResponse.targetStatus` 继续输出 `CONTENT_DELETED`，不新增字段。
- `ClassicsSharingApplicationServiceImpl.findReadableResourceTarget` 保持只允许 `ClassicsShareTargetStatus.AVAILABLE`；`CONTENT_DELETED` 目标中的 Wangqi 原始文件和 Sancai 图片必须 404。
- `ClassicsShareTargetMapper.pagePortalShares` 保持 `t.target_status in (#{targetStatus}, #{legacyTargetStatus})`，调用侧只传 `AVAILABLE` 和 `ACTIVE`，确保 `CONTENT_DELETED` 不进入 `/shares` 公开列表。

测试要求：

- Admin 分享详情响应 target 包含 `targetStatus=CONTENT_DELETED` 和 `titleSnapshot`。
- Portal 分享详情响应 target 包含 `targetStatus=CONTENT_DELETED` 和 `titleSnapshot`。
- Portal 资源读取在 target 为 `CONTENT_DELETED` 时失败，不调用 Storage 放行。
- Portal 公开分享列表不返回 `CONTENT_DELETED` target。

### 任务 4：Admin Web 删除占位

目标：分享管理详情抽屉中的目标列表能明确展示删除状态，并移除已删除目标的失效操作。

文件范围：

- `kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.css`
- `kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-types.ts`

控件与操作要求：

- 在 `SharingPage` 的 `targetColumns` 中新增“目标状态”列，展示 Ant Design `Tag` 或既有 `KuzhambuTag`：
  - `AVAILABLE/ACTIVE` 显示“可用”。
  - `CONTENT_DELETED` 显示“内容已删除”。
  - 未知值显示原始状态。
- 在“关联内容”表格中保留 `CONTENT_DELETED` 目标行，继续展示“内容类型”“内容 ID”“标题快照”。
- `CONTENT_DELETED` 目标行不得出现打开内容详情、预览资源、下载资源等操作控件。本页面当前 `targetColumns` 无操作列；后续如果新增操作列，必须在本任务中加条件禁用或隐藏。
- 分享详情抽屉标题仍为“分享详情”；“分享信息”描述区不变；“刷新访问记录”按钮不变。
- 如需样式，限定在 `sharing-page.css`，例如删除占位状态 class 或 Tag 间距，不新增共享样式。

测试要求：

- 在 `sharing-page.test.tsx` 的 mock 详情数据中加入一个 `targetStatus: "CONTENT_DELETED"` 的目标。
- 点击“查看 ...”按钮打开 `KuzhambuDrawer` 后，断言“分享目标列表”中出现“内容已删除”和该目标的 `titleSnapshot`。
- 断言删除目标行不存在“预览”“下载”“打开内容详情”等按钮或链接。

### 任务 5：Portal Web 删除占位

目标：读者侧分享详情对已删除目标只展示占位，不展示任何历史内容正文或资源。

文件范围：

- `kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`
- `kuzhambu-apps/portal-web/src/pages/share/share-form.test.tsx`
- `kuzhambu-apps/portal-web/src/pages/share/share-page.tsx`
- `kuzhambu-apps/portal-web/src/pages/share/share-page.test.tsx`
- `kuzhambu-apps/portal-web/src/styles.css`

分享详情控件要求：

- 在 `share-form.tsx` 增加 helper，例如 `isDeletedTarget(target)`，判断 `target.targetStatus === "CONTENT_DELETED"`。
- `CONTENT_DELETED` target 的 `Card.portal-share-target` 必须保留原顺序位置，header 显示：
  - 内容类型 `formatContentType(target.contentType)`。
  - 标题 `titleSnapshot`，没有标题时使用 `分享内容 N`。
  - 状态 Badge 显示“内容已删除”。
- `CONTENT_DELETED` target 的 `<dl>` 只允许展示：
  - `内容 ID`
  - `目标状态`
  - 可选 `内容类型`
  不展示 `版本 ID`、`内容可见性` 等会暗示快照可消费的字段。
- `CONTENT_DELETED` target 不调用：
  - `renderSnapshotSummary(target)`
  - `renderWangqiResource(...)`
  - `SancaiImageGallery`
  - `resolveResourceUrl`
  - `resolveImageUrl`
- `CONTENT_DELETED` target 不渲染：
  - 正文摘要区 `.portal-share-copy-list`
  - 王圻原始文件区 `.portal-share-resource`
  - 三才图片区 `.portal-share-images`
  - “预览”“下载”“下载原图”“切换图片 ...”控件
- 删除占位文案使用“内容已删除，分享仅保留标题快照。”，放在该 target card 内，class 可命名为 `.portal-share-deleted-placeholder`。

分享列表控件要求：

- `share-page.tsx` 对 `records` 做前端防御过滤：只渲染 `targetStatus !== "CONTENT_DELETED"` 的记录。
- 过滤后无记录时，继续显示现有空状态 `暂无符合条件的公开分享。`
- 不新增筛选项，不改变“标题/分类/开始时间/结束时间/重置/查询”控件。
- 如果后端已过滤，前端过滤仍保留作为防御。

测试要求：

- `share-form.test.tsx` 新增用例：一个 share 同时包含 `AVAILABLE` Sancai target 和 `CONTENT_DELETED` Wangqi target，断言正常 target 仍显示图片区，删除 target 显示占位且不出现“原始文件”“预览”“下载”。
- `share-form.test.tsx` 新增用例：`CONTENT_DELETED` target 的 `contentSnapshotJson` 含 `summary/content/images/storageObject` 时，也不渲染正文、图片或资源链接。
- `share-page.test.tsx` 新增或补齐用例：公开列表 response 同时含 `AVAILABLE` 和 `CONTENT_DELETED` records 时，只渲染 `AVAILABLE` record。

## 验证命令

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-infra,biz/classics/kuzhambu-classics-interface -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-infra,biz/classics/kuzhambu-classics-interface -am test
```

前端：

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm --workspace kuzhambu-portal-web run format
npm run format:check
npm run lint
npm run test
```

## 收口文档

实现和验证通过后更新 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`：

- “条目查看、创建、编辑、删除”改为 `已完成`，说明三类内容删除后会同步分享目标为 `CONTENT_DELETED` 并重算分享风险态。
- “目标被删除后占位展示”说明从“字段可返回状态”收口为“删除动作触发状态同步，Admin Web / Portal Web 按状态展示占位，Portal 不再展示已删除目标正文与资源”。
- `Current Baseline` 增加一条：Classics 删除内容与分享安全闭环已完成，删除后不再把已删除目标计入资源读取、公开列表和风险态。

本 RUNBOOK 在任务 PR 合并前删除；最终状态只保留在 coverage 和必要稳定设计文档中。
