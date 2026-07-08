# RUNBOOK Classics 明代习俗版本历史闭环

## 目标

把 Classics 古籍域中明代习俗的版本历史、版本对比和历史恢复打成可运行、可验证、可收口的已完成状态。

完成后，后台用户在 `Admin Web /classics/ming-customs` 编辑明代习俗条目时，可以查看该条目的正式版本历史，选择任一历史版本查看当前内容与历史快照的字段级对比，并通过二次确认恢复该历史版本。恢复成功后，系统生成新的正式版本，刷新明代习俗页面数据，并提示 `明代习俗版本已恢复`。

## 已确认约束

- 范围只覆盖 `biz/classics` 的 `mingcustoms` 子域和 Admin Web `classics/ming-customs` 页面。
- 版本真相源继续使用 `classics_content_version`，不新增明代习俗专属版本表。
- 版本接口沿用三才图会和王圻文档风格：`versions/list`、`versions/get`、`versions/reset`。
- 版本恢复只恢复明代习俗主字段，不恢复标签、问答、分享、导出任务或访问统计。
- 版本恢复必须生成新的 `HISTORY_RESTORED` 正式版本，不把当前版本指针直接回退到旧版本。
- 版本读取和恢复必须校验 `contentType=MING_CUSTOMS` 且 `contentId` 属于当前明代习俗条目。
- 查看版本历史和版本对比需要 `classics:mingcustoms:view` 权限。
- 恢复历史版本需要 `classics:mingcustoms:edit` 权限。
- 不改变三才图会、王圻文档和通用 Classics 内容版本的既有接口语义。

## 数据结构变更

### 数据库字段

不新增表，不新增数据库字段。

复用现有字段：

文件：`docs/30-designs/CLASSICS-DESIGN.md`

表：`classics_content_version`

| 字段 | 类型 | 本任务口径 |
| --- | --- | --- |
| `id` | `bigint` | 版本 ID，作为 `versionId` 请求字段来源 |
| `content_type` | `varchar(32)` | 明代习俗固定为 `MING_CUSTOMS` |
| `content_id` | `bigint` | 明代习俗条目 ID |
| `version_no` | `int` | 明代习俗版本号 |
| `versioned_at` | `datetime(3)` | 版本生成时间 |
| `snapshot_json` | `json` | 明代习俗版本快照 |
| `change_type` | `varchar(32)` | 恢复生成的新版本固定为 `HISTORY_RESTORED` |
| `change_summary` | `varchar(512)` | 恢复生成的新版本固定为 `恢复历史版本 v{versionNo}` |

表：`classics_ming_customs_entry`

| 字段 | 类型 | 恢复口径 |
| --- | --- | --- |
| `title` | `varchar(255)` | 从历史快照恢复 |
| `category` | `varchar(128)` | 从历史快照恢复 |
| `chapter` | `varchar(128)` | 从历史快照恢复 |
| `section` | `varchar(128)` | 从历史快照恢复 |
| `summary` | `text` | 从历史快照恢复 |
| `content_format` | `varchar(16)` | 从历史快照恢复 |
| `content` | `longtext` | 从历史快照恢复 |
| `original_excerpts` | `longtext` | 从历史快照恢复 |
| `visibility` | `varchar(16)` | 从历史快照恢复 |
| `current_version_id` | `bigint` | 恢复成功后指向新生成版本 |
| `current_version_no` | `int` | 恢复成功后更新为新版本号 |
| `current_versioned_at` | `datetime(3)` | 恢复成功后更新为新版本生成时间 |
| `content_updated_at` | `datetime(3)` | 恢复成功后更新为当前时间，不恢复历史时间 |

### 后端快照字段

现有文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/MingCustomsVersionSnapshot.java`

快照 JSON 字段必须保持如下结构：

| JSON 字段 | Java 类型 | 来源 | 恢复 |
| --- | --- | --- | --- |
| `contentType` | `String` | `ClassicsContentType.MING_CUSTOMS.value()` | 只用于归属校验 |
| `contentId` | `Long` | `entry.contentId()` | 只用于归属校验 |
| `contentUpdatedAt` | `String` | `entry.contentUpdatedAt()` | 不恢复 |
| `title` | `String` | `entry.title` | 恢复 |
| `category` | `String` | `entry.category` | 恢复 |
| `chapter` | `String` | `entry.chapter` | 恢复 |
| `section` | `String` | `entry.section` | 恢复 |
| `summary` | `String` | `entry.summary` | 恢复 |
| `contentFormat` | `String` | `entry.contentFormat.name()` | 恢复 |
| `content` | `String` | `entry.content` | 恢复 |
| `originalExcerpts` | `String` | `entry.originalExcerpts` | 恢复 |
| `visibility` | `String` | `entry.visibility.name()` | 恢复 |

### 后端请求模型

新增文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/request/MingCustomsVersionRequest.java`

字段：

| JSON 字段 | Java 字段 | Java 类型 | 必填场景 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `id` | `Long` | `list/get/reset` | 明代习俗条目 ID |
| `versionId` | `versionId` | `Long` | `get/reset` | `classics_content_version.id` |

类注解：

- `@Getter`
- `@Setter`
- `@JsonInclude(JsonInclude.Include.NON_NULL)`
- `@JsonIgnoreProperties(ignoreUnknown = true)`

### 后端响应模型

新增文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsVersionResponse.java`

字段：

| JSON 字段 | Java 字段 | Java 类型 | 来源 |
| --- | --- | --- | --- |
| `id` | `id` | `Long` | `ClassicsContentVersion.id.value()` |
| `contentType` | `contentType` | `String` | `ClassicsContentVersion.contentType.value()` |
| `contentId` | `contentId` | `Long` | `ClassicsContentVersion.contentId.value()` |
| `versionNo` | `versionNo` | `Integer` | `ClassicsContentVersion.versionNo` |
| `versionedAt` | `versionedAt` | `Date` | `ClassicsContentVersion.versionedAt` |
| `snapshotJson` | `snapshotJson` | `String` | `ClassicsContentVersion.snapshotJson` |
| `changeType` | `changeType` | `String` | `ClassicsContentVersion.changeType.value()` |
| `changeSummary` | `changeSummary` | `String` | `ClassicsContentVersion.changeSummary` |

类注解：

- `@Getter`
- `@Setter`
- `@Builder`
- `@NoArgsConstructor`
- `@AllArgsConstructor`
- `@JsonInclude(JsonInclude.Include.NON_NULL)`
- `@JsonIgnoreProperties(ignoreUnknown = true)`

### 前端类型

修改文件：`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-types.ts`

新增类型：

```ts
export interface MingCustomsContentVersionRecord {
    id: number;
    contentType?: string | null;
    contentId?: number | null;
    versionNo?: number | null;
    versionedAt?: string | null;
    snapshotJson?: string | null;
    changeType?: string | null;
    changeSummary?: string | null;
}

export interface MingCustomsVersionSnapshot {
    contentType?: string | null;
    contentId?: number | null;
    contentUpdatedAt?: string | null;
    title?: string | null;
    category?: string | null;
    chapter?: string | null;
    section?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    originalExcerpts?: string | null;
    visibility?: string | null;
}
```

### 前端组件 Props

新增文件：`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-version-history-panel.tsx`

新增 `MingCustomsVersionHistoryPanelProps`：

| Prop | TypeScript 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `currentEntry` | `MingCustomsRecord \| null \| undefined` | 否 | 当前编辑中的明代习俗条目，用于当前值对比 |
| `detailLoading` | `boolean` | 否 | 版本详情加载状态，映射到详情区域 `aria-busy` |
| `listLoading` | `boolean` | 否 | 版本列表加载状态，传给 `KuzhambuList.loading` |
| `resetting` | `boolean` | 否 | 恢复按钮 loading 状态 |
| `selectedVersion` | `MingCustomsContentVersionRecord \| null` | 否 | 当前选中的版本详情 |
| `versions` | `MingCustomsContentVersionRecord[]` | 是 | 版本历史列表 |
| `onResetVersion` | `(version: MingCustomsContentVersionRecord) => void` | 是 | 点击恢复按钮时触发 |
| `onSelectVersion` | `(version: MingCustomsContentVersionRecord) => void` | 是 | 点击查看按钮时触发 |

## 后端接口契约

修改文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/MingCustomsAdminController.java`

新增接口：

| Method | Path | 权限 | 请求 | 返回 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/classics/ming-customs/versions/list` | `classics:mingcustoms:view` | `MingCustomsVersionRequest` | `List<MingCustomsVersionResponse>` |
| `POST` | `/api/classics/ming-customs/versions/get` | `classics:mingcustoms:view` | `MingCustomsVersionRequest` | `MingCustomsVersionResponse` |
| `POST` | `/api/classics/ming-customs/versions/reset` | `classics:mingcustoms:edit` | `MingCustomsVersionRequest` | `MingCustomsVersionResponse` |

归属校验固定规则：

- `versions/get` 和 `versions/reset` 先调用 `contentService.getVersion(ClassicsContentVersionIdCodec.toDomain(request.getVersionId()))`。
- 如果版本为空，抛出业务异常，文案为 `明代习俗历史版本不存在`。
- 如果 `version.contentType != MING_CUSTOMS` 或 `version.contentId != request.id`，抛出业务异常，文案为 `历史版本不属于当前明代习俗条目`。
- `versions/reset` 归属校验通过后，调用 `contentService.restoreHistoryVersion(ClassicsContentVersionIdCodec.toDomain(request.getVersionId()))`。

## 后端任务拆分

任务拆分规则：每个任务只覆盖 2-5 个文件；任务完成后必须能用该任务自己的测试或相邻测试验证。

### 任务 1：补齐 Ming Customs 版本接口模型

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/request/MingCustomsVersionRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsVersionResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/assembler/MingCustomsInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/MingCustomsAdminControllerTest.java`

动作：

- 新增 `MingCustomsVersionRequest`，字段只包含 `id` 和 `versionId`。
- 新增 `MingCustomsVersionResponse`，字段按“后端响应模型”定义。
- 在 `MingCustomsInterfaceAssembler` 新增 `toVersionResponse(ClassicsContentVersion version)`。
- controller 测试新增响应字段断言，锁定 `id/contentType/contentId/versionNo/changeType/changeSummary/snapshotJson`。

验收：

- 明代习俗版本响应 JSON 字段完整输出。
- 既有明代习俗 CRUD、关键词、关键词云响应不变化。

### 任务 2：补齐 Ming Customs 版本 controller

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/MingCustomsAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/MingCustomsAdminControllerTest.java`

动作：

- `MingCustomsAdminController` 注入 `ClassicsContentApplicationService`。
- 新增 `listVersions`，调用 `contentService.listVersions(ClassicsContentType.MING_CUSTOMS.value(), ClassicsContentIdCodec.toDomain(request.getId()))`。
- 新增 `getVersion`，返回归属校验后的版本响应。
- 新增 `resetVersion`，归属校验后调用 `restoreHistoryVersion` 并返回新版本响应。
- 在 controller 内新增私有方法 `ownedVersion(Long entryId, Long versionId)`，集中执行归属校验。
- controller 测试锁定三条路径、权限注解、service 入参和归属校验失败。

验收：

- `/api/classics/ming-customs/versions/list` 只按 `MING_CUSTOMS + id` 查询版本。
- `/api/classics/ming-customs/versions/get` 不允许读取其他内容类型或其他条目的版本。
- `/api/classics/ming-customs/versions/reset` 恢复成功后返回新的版本记录。

### 任务 3：锁定 Ming Customs 恢复行为

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/MingCustomsVersionSnapshot.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/mingcustoms/MingCustomsApplicationServiceImplTest.java`

动作：

- 复核 `restoreHistoryVersion` 的 `MING_CUSTOMS` 分支字段覆盖，字段必须精确为：`title/category/chapter/section/summary/contentFormat/content/originalExcerpts/visibility`。
- 恢复时保持当前条目 ID，不恢复历史 `contentUpdatedAt`。
- 恢复后生成 `HISTORY_RESTORED` 新版本，`changeSummary` 固定为 `恢复历史版本 v{versionNo}`。
- 恢复后更新 `currentVersionId/currentVersionNo/currentVersionedAt/contentUpdatedAt`。
- 恢复后触发明代习俗搜索同步，公开内容 upsert，非公开内容 delete。
- 测试覆盖快照不可解析、内容类型错误、条目不存在、恢复成功生成新版本。

验收：

- 恢复历史版本后数据库条目主字段与快照一致。
- 新版本号递增，当前版本标记指向新版本。
- 搜索同步发布语义与手动保存一致。

## 前端任务拆分

任务拆分规则：每个任务只覆盖 2-5 个文件；页面交互任务必须写清楚控件、操作、query key 和测试断言。

### 任务 4：补齐 Ming Customs 版本类型和 service

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-service-contract.test.ts`

动作：

- 在 `ming-customs-types.ts` 新增 `MingCustomsContentVersionRecord` 和 `MingCustomsVersionSnapshot`。
- 在 `ming-customs-service.ts` 新增：
  - `listVersions(entryId: number)`
  - `getVersion(entryId: number, versionId: number)`
  - `resetVersion(entryId: number, versionId: number)`
- 三个 service 方法均使用 `postJson`。
- `listVersions` 请求路径为 `/classics/ming-customs/versions/list`，请求体为 `{ id: entryId }`。
- `getVersion` 请求路径为 `/classics/ming-customs/versions/get`，请求体为 `{ id: entryId, versionId }`。
- `resetVersion` 请求路径为 `/classics/ming-customs/versions/reset`，请求体为 `{ id: entryId, versionId }`。
- service contract 测试锁定 method、path 和 request body。

验收：

- 前端版本接口路径和请求体与后端契约一致。
- `MingCustomsVersionSnapshot` 字段覆盖所有前端对比字段。

### 任务 5：新增 Ming Customs 版本历史面板组件

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-version-history-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.css`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`

控件：

- 面板根节点：`section`，`aria-label="明代习俗版本历史面板"`。
- 面板布局容器：`div.ming-customs-version-history-panel-grid`，左侧版本列表，右侧版本详情和字段对比。
- 版本列表：`KuzhambuList`，`aria-label="明代习俗版本历史列表"`。
- 查看按钮：`Button type="link"`，`aria-label="查看明代习俗版本 {versionNo}"`。
- 版本元信息：`Descriptions`，展示 `版本号/变更类型/版本时间/变更说明`。
- 字段对比：`Descriptions column={1}`，展示当前值和历史值。
- 恢复按钮：`Button danger`，`aria-label="恢复明代习俗版本 {versionNo}"`。
- 空状态：`Empty`，文案为 `请选择版本查看对比`。
- 快照异常提示：`Alert type="warning"`，标题为 `版本快照为空或无法解析`。

字段对比顺序：

| key | label | 当前值来源 | 历史值来源 |
| --- | --- | --- | --- |
| `title` | `标题` | `MingCustomsRecord.title` | `MingCustomsVersionSnapshot.title` |
| `category` | `分类` | `MingCustomsRecord.category` | `MingCustomsVersionSnapshot.category` |
| `chapter` | `章节` | `MingCustomsRecord.chapter` | `MingCustomsVersionSnapshot.chapter` |
| `section` | `节` | `MingCustomsRecord.section` | `MingCustomsVersionSnapshot.section` |
| `summary` | `概述` | `MingCustomsRecord.summary` | `MingCustomsVersionSnapshot.summary` |
| `contentFormat` | `正文格式` | `MingCustomsRecord.contentFormat` | `MingCustomsVersionSnapshot.contentFormat` |
| `content` | `正文` | `MingCustomsRecord.content` | `MingCustomsVersionSnapshot.content` |
| `originalExcerpts` | `原文摘录` | `MingCustomsRecord.originalExcerpts` | `MingCustomsVersionSnapshot.originalExcerpts` |
| `visibility` | `可见性` | `MingCustomsRecord.visibility` | `MingCustomsVersionSnapshot.visibility` |

操作：

- 点击 `查看`：调用父组件 `onSelectVersion(version)`。
- 点击 `恢复此版本`：调用父组件 `onResetVersion(version)`。
- 快照解析失败：恢复按钮禁用。
- 当前值与历史值字符串化后不一致：对比行增加 `is-changed` class。
- `versionNo` 为空时，按钮可访问名称回退到 `version.id`。
- 空值、`null`、`undefined` 和空字符串统一显示为 `-`。

验收：

- 打开编辑弹窗后能看到 `明代习俗版本历史面板`。
- 点击版本后能看到版本元信息和字段级当前/历史对比。
- 快照异常时展示 warning 且不能恢复。

### 任务 6：接入 Ming Customs 页面查询、恢复和刷新

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.css`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`

页面状态：

- `selectedVersionId: number | null`
- `versionHistoryOpen` 不单独新增；版本面板随编辑弹窗内明细展示。
- 打开编辑弹窗时把 `selectedVersionId` 置为 `null`。
- 关闭编辑弹窗时把 `selectedVersionId` 置为 `null`。

查询：

- `versionsQuery`
  - `queryKey`: `["ming-customs", "versions", editorEntry?.id]`
  - `enabled`: `editorOpen && editorMode === "edit" && Boolean(editorEntry?.id)`
  - `queryFn`: `service.listVersions(editorEntry.id)`
- `selectedVersionQuery`
  - `queryKey`: `["ming-customs", "versions", "detail", editorEntry?.id, selectedVersionId]`
  - `enabled`: `editorOpen && Boolean(editorEntry?.id) && Boolean(selectedVersionId)`
  - `queryFn`: `service.getVersion(editorEntry.id, selectedVersionId)`

Mutation：

- `resetVersionMutation`：调用 `service.resetVersion(entryId, versionId)`。

控件位置：

- 在明代习俗编辑弹窗内、主表单和标签/问答/AI 候选区域之后展示 `MingCustomsVersionHistoryPanel`。
- 面板不作为列表行外置动作，不新增独立路由。

操作：

- 打开编辑弹窗：加载条目详情和版本列表，清空 `selectedVersionId`。
- 点击 `查看明代习俗版本 {versionNo}`：设置 `selectedVersionId`。
- 点击 `恢复明代习俗版本 {versionNo}`：调用 `useKuzhambuConfirm`。
- 确认弹窗标题：`确认恢复明代习俗历史版本`。
- 确认弹窗内容：`恢复后会生成新的正式版本，当前内容将被历史版本覆盖。`。
- 确认后：执行 `resetVersionMutation`。
- 恢复成功：提示 `明代习俗版本已恢复`。

恢复成功后刷新：

- `["ming-customs", "page"]`
- `["ming-customs", "keyword-cloud"]`
- `["ming-customs", "detail"]`
- `["ming-customs", "versions"]`
- `["classics", "content", "tags", "MING_CUSTOMS"]`
- `["classics", "content", "qa-pairs", "MING_CUSTOMS"]`
- `["ai", "candidates", "MING_CUSTOMS", editorEntry?.id]`

测试：

- 页面测试覆盖版本面板出现。
- 页面测试覆盖点击查看版本后展示历史字段。
- 页面测试覆盖点击恢复后出现确认弹窗。
- 页面测试覆盖确认恢复后调用 `service.resetVersion(entryId, versionId)`。

验收：

- 编辑明代习俗条目时版本历史自动加载。
- 用户可以查看历史版本对比并恢复。
- 恢复后页面展示最新内容，不需要手动刷新浏览器。

### 任务 7：补齐 Ming Customs 版本历史 E2E 回归

文件：

- `kuzhambu-apps/admin-web/e2e/classics/ming-customs/ming-customs.spec.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`

E2E 拦截：

- `**/kuzhambu-admin-api/api/classics/ming-customs/page`
- `**/kuzhambu-admin-api/api/classics/ming-customs/*`
- `**/kuzhambu-admin-api/api/classics/ming-customs/versions/list`
- `**/kuzhambu-admin-api/api/classics/ming-customs/versions/get`
- `**/kuzhambu-admin-api/api/classics/ming-customs/versions/reset`

E2E 操作：

- 进入 `/classics/ming-customs`。
- 点击列表中的编辑按钮打开编辑弹窗。
- 等待 `明代习俗版本历史面板` 可见。
- 点击 `查看明代习俗版本 1`。
- 断言字段对比区出现 `标题`、`当前：`、`历史：`。
- 点击 `恢复明代习俗版本 1`。
- 在确认弹窗中点击确认。
- 断言 `versions/reset` 请求体为 `{ id: 500000000001, versionId: 9001 }`。
- 断言页面出现 `明代习俗版本已恢复`。

单测断言：

- 无版本时显示 `暂无版本历史`。
- 快照 JSON 解析失败时显示 `版本快照为空或无法解析`。
- 确认恢复后调用 `service.resetVersion(500000000001, 9001)`。

验收：

- E2E 能证明页面已接通版本列表、版本详情和历史恢复三条 API。
- 页面单测能证明异常快照不会触发恢复。

## 文档与 readiness 收口

文件：

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-CLASSICS-MING-CUSTOMS-HISTORY-CLOSURE.md`

动作：

- 实现、测试和页面验证全部通过后，将 `CLASSICS-IMPLEMENTATION-COVERAGE.md` 中明代习俗“版本历史/版本对比/历史恢复”改为已完成。
- PR 收口时删除本 RUNBOOK 及残留引用。

验收：

- readiness 文档只记录已完成口径，不保留执行过程。
- RUNBOOK 在任务关闭时被清理。

## 验证命令

后端最小验证：

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-interface,biz/classics/kuzhambu-classics-application spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/classics/kuzhambu-classics-interface,biz/classics/kuzhambu-classics-application test
```

前端最小验证：

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format
pnpm run format:check
pnpm run lint
pnpm --filter kuzhambu-admin-web test -- ming-customs
pnpm --filter kuzhambu-admin-web exec playwright test e2e/classics/ming-customs/ming-customs.spec.ts
```

完整收口验证：

```sh
cd kuzhambu-servers
mvn test

cd ../kuzhambu-apps
pnpm run build
pnpm run test
```
