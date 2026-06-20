# RUNBOOK Classics Sancai Version Restore

## Goal

交付“三才图会条目版本历史 / 对比 / 恢复”闭环：

- 后台可查看某个三才图会条目的正式版本列表。
- 可查看单个历史版本快照，并和当前条目字段做对比。
- 可将历史版本恢复为当前条目内容。
- 恢复动作必须写入新的正式版本，`changeType=HISTORY_RESTORED`，并刷新条目当前版本指针。

本文件是执行期 RUNBOOK。任务关闭前应删除本文件，并把完成状态同步到 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`。

## Baseline

需求来源：

- `docs/10-requirements/CLASSICS-REQUIREMENTS.md`
  - 三才图会要求支持版本历史、版本对比、历史恢复。
  - 自动保存只做防丢失，不是正式版本。
  - 手动保存、AI 结果应用、历史恢复应生成正式版本。
- `docs/30-designs/CLASSICS-DESIGN.md`
  - `classics_sancai_entry` 是三才图会主内容表。
  - `classics_content_version` 是通用内容版本表。
  - 主内容通过 `current_version_id`、`current_version_no`、`current_versioned_at`、`content_updated_at` 标记当前版本状态。
- `docs/20-interfaces/CLASSICS-CONTENT-VERSION-SNAPSHOT-INTERFACE.md`
  - 已定义 `SANCAI_ENTRY` 快照结构。
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
  - 当前缺口是“三才图会版本历史、版本对比和历史恢复”：通用版本模型已到位，但条目版本查询、对比、恢复写入规则尚未对外服务。

现有后端基础：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/entity/SancaiEntry.java`
  - 已实现 `Versionable`。
  - 字段包括：
    - `id`
    - `volumeId`
    - `title`
    - `originalText`
    - `translationText`
    - `summary`
    - `lifecycleStatus`
    - `visibility`
    - `translationStatus`
    - `imageStatus`
    - `visualAssetStatus`
    - `refinementStatus`
    - `priority`
    - `currentVersionId`
    - `currentVersionNo`
    - `currentVersionedAt`
    - `contentUpdatedAt`
  - `contentType()` 返回 `ClassicsContentType.SANCAI_ENTRY`。
  - `contentId()` 返回条目 id 对应的 `ClassicsContentId`。
  - `markVersioned(...)` 会刷新当前版本指针字段。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`
  - `addEntry` 和 `updateEntry` 已调用 `contentApplicationService.ensureVersioned(entry, MANUAL_SAVE, "手动保存")`。
  - `changeEntryStatus`、`changeEntryVisibility`、排序、删除不应生成正式版本。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/SancaiEntryVersionSnapshot.java`
  - 已定义并可从 `SancaiEntry` 生成快照。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentSnapshotAssembler.java`
  - 已支持 `ClassicsContentType.SANCAI_ENTRY`。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
  - `listVersions(...)`、`getVersion(...)`、`ensureVersioned(...)` 已可复用。
  - `restoreHistoryVersion(...)` 当前只支持 `WANGQI_DOCUMENT`，三才图会会命中“不支持恢复该类型历史版本”。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiRepositoryImpl.java`
  - `updateEntry(...)` 已更新内容字段、状态字段、版本指针字段、`contentUpdatedAt`。
  - `updateEntry(...)` 当前没有更新 `priority`。本次恢复策略不恢复历史 `priority`，而是在当前目标卷内分配新的末尾排序值。

现有前端基础：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
  - `SancaiEntryRecord` 还没有版本指针字段。
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
  - 只包含条目列表、新增、更新、删除、排序。
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
  - 管理条目列表、抽屉打开、保存、删除、分享、排序。
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
  - 编辑抽屉当前只展示表单。
- 可参考王圻已完成实现：
  - `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-version-history-panel.tsx`
  - `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-service.ts`
  - `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-types.ts`

## Data Contract

### Main Entry

三才图会条目响应需要暴露版本状态。后端响应和前端 `SancaiEntryRecord` 应补齐：

```ts
interface SancaiEntryRecord {
    id: number;
    volumeId?: number | null;
    title?: string | null;
    originalText?: string | null;
    translationText?: string | null;
    summary?: string | null;
    lifecycleStatus?: string | null;
    visibility?: string | null;
    translationStatus?: string | null;
    imageStatus?: string | null;
    visualAssetStatus?: string | null;
    refinementStatus?: string | null;
    priority?: number | null;
    currentVersionId?: number | null;
    currentVersionNo?: number | null;
    currentVersionedAt?: string | null;
    contentUpdatedAt?: string | null;
    versionDirty?: boolean;
}
```

`versionDirty` 规则必须和 `ClassicsContentVersioningService.needsVersion(...)` 一致：

- `currentVersionId` 或 `currentVersionedAt` 为空时返回 `true`。
- `contentUpdatedAt.after(currentVersionedAt)` 时返回 `true`。
- 其他情况返回 `false`。
- 当前三才图会的手动保存会立即生成正式版本，因此正常保存后应为 `false`。

### Version Record

新增三才图会版本响应结构，字段对齐通用 `ClassicsContentVersion`：

```ts
interface SancaiContentVersionRecord {
    id: number;
    contentType?: string | null;
    contentId?: number | null;
    versionNo?: number | null;
    versionedAt?: string | null;
    snapshotJson?: string | null;
    changeType?: string | null;
    changeSummary?: string | null;
}
```

### Snapshot

`SANCAI_ENTRY` 的 `snapshotJson` 必须保持以下结构。恢复、对比、测试都以这个结构为准：

```json
{
  "contentType": "SANCAI_ENTRY",
  "contentId": 300000003360,
  "contentUpdatedAt": "2026-06-20T10:00:00Z",
  "volumeId": 1,
  "title": "天地",
  "originalText": "原文",
  "translationText": "译文",
  "summary": "摘要",
  "lifecycleStatus": "PUBLISHED",
  "visibility": "PUBLIC",
  "translationStatus": "PENDING",
  "imageStatus": "PENDING",
  "visualAssetStatus": "PENDING",
  "refinementStatus": "PENDING",
  "priority": 1
}
```

恢复时必须恢复这些字段：

- `volumeId`
- `title`
- `originalText`
- `translationText`
- `summary`
- `lifecycleStatus`
- `visibility`
- `translationStatus`
- `imageStatus`
- `visualAssetStatus`
- `refinementStatus`

恢复时不得从历史快照恢复这些字段：

- `id`：使用当前被恢复的条目 id。
- `priority`：恢复时直接赋予新排序值，规则为当前目标卷 `max(priority) + 1`。
- `contentUpdatedAt`：恢复动作发生时写入当前时间。
- `currentVersionId`、`currentVersionNo`、`currentVersionedAt`：由新生成的 `HISTORY_RESTORED` 版本回填。

恢复事务顺序必须保持：

1. 从历史版本快照构造 `SancaiEntry restored`。
2. 将 `restored.contentUpdatedAt` 设置为当前时间。
3. 按历史快照中的 `volumeId` 计算当前目标卷 `max(priority) + 1`，并写入 `restored.priority`。
4. 写入主表内容字段、状态字段、新 `priority`、`contentUpdatedAt`。
5. 调用 `createRestoredVersion(restored, restoredFrom)`，生成 `HISTORY_RESTORED` 版本。
6. `createRestoredVersion(...)` 回填 `restored.currentVersionId/currentVersionNo/currentVersionedAt`。
7. 再写入主表版本指针字段。

这样新版本的 `snapshotJson` 记录的是恢复后的当前内容，而不是被恢复版本原始快照的逐字复制。

## Backend Work

### Application Restore Dispatcher

修改：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`

新增依赖：

- `SancaiEntryVersionRestorer sancaiEntryVersionRestorer`

在 `restoreHistoryVersion(ClassicsContentVersionId versionId)` 增加 `SANCAI_ENTRY` 分支：

```java
if (version.getContentType() == ClassicsContentType.SANCAI_ENTRY) {
    Versionable restored = sancaiEntryVersionRestorer.restoreSnapshot(version);
    ClassicsContentVersion restoredVersion = createRestoredVersion(restored, version);
    sancaiEntryVersionRestorer.markVersioned((SancaiEntry) restored);
    return restoredVersion;
}
```

边界要求：

- 不从 `ClassicsContentApplicationServiceImpl` 反向调用 `SancaiApplicationService`。
- 保持和 `WangqiDocumentVersionRestorer` 相同的模式：专用 Restorer 负责读写三才图会主表，Content service 负责版本分发和生成 `HISTORY_RESTORED` 版本。

### Sancai Restorer

新增：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/support/SancaiEntryVersionRestorer.java`

职责：

- 校验 `version.getContentType() == ClassicsContentType.SANCAI_ENTRY`。
- 使用 `ObjectMapper.readTree(version.getSnapshotJson())` 解析快照。
- 校验：
  - `snapshot.contentType == "SANCAI_ENTRY"`
  - `snapshot.contentId == version.contentId`
- 读取当前条目：
  - `repository.getEntryById(SancaiEntryIdCodec.toDomain(version.getContentId().value()))`
  - 当前条目不存在时抛出 `BizException("三才图会条目不存在，无法恢复历史版本")`。
- 将快照映射为新的 `SancaiEntry`：
  - id 使用当前条目 id。
  - 字段按本 RUNBOOK 的 Snapshot 恢复字段列表赋值。
  - `priority` 不使用历史快照值；按恢复后的 `volumeId` 分配 `max(priority) + 1`。
  - `contentUpdatedAt` 设置为 `new Date()`。
- 调用 `repository.updateEntry(restored)` 写入主表，并断言返回值为 `1`；否则抛出当前条目不存在错误。
- `markVersioned(SancaiEntry entry)` 再次调用 `repository.updateEntry(entry)`，用于在 `createRestoredVersion(...)` 回填版本指针后持久化 `current_version_*` 字段，并断言返回值为 `1`。

快照字段解析要求：

- `volumeId` 使用 `SancaiVolumeIdCodec.toDomain(longValue(snapshot, "volumeId"))`。
- `contentId` 使用 `ClassicsContentIdCodec.toDomain(longValue(snapshot, "contentId"))`。
- 状态字段统一使用对应 enum 的 `from(...)` 方法：
  - `SancaiEntryLifecycleStatus.from(...)`
  - `SancaiEntryVisibility.from(...)`
  - `SancaiEntryTranslationStatus.from(...)`
  - `SancaiEntryImageStatus.from(...)`
  - `SancaiEntryVisualAssetStatus.from(...)`
  - `SancaiEntryRefinementStatus.from(...)`
- `priority` 只用于历史对比展示，不用于恢复写入。
- 恢复写入的 `priority` 使用目标卷当前最大值加 1。目标卷来自历史快照 `volumeId`。

建议错误消息：

- 版本类型错误：`历史版本不是三才图会条目版本`
- 快照 JSON 不可解析：`历史版本快照不可解析`
- 快照归属错误：`历史版本快照不属于当前三才图会条目`
- 当前条目不存在：`三才图会条目不存在，无法恢复历史版本`

### Repository

优先复用：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiRepository.java`
  - `getEntryById(...)`
  - `updateEntry(...)`

当前真实基础设施落点：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/dataobject/SancaiEntryDO.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/assembler/SancaiPersistenceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/mapper/SancaiMapper.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiRepositoryImpl.java`

`SancaiEntryDO` 和 `SancaiPersistenceAssembler` 已包含版本字段和 `priority` 映射。必须补充一个恢复专用写入边界：

- 推荐新增 `updateRestoredEntry(SancaiEntry entry)` 并只给 Restorer 使用。
- `updateRestoredEntry(...)` 必须覆盖 `priority`、内容字段、状态字段、`contentUpdatedAt`、版本指针字段。
- 普通 `updateEntry(...)` 继续不更新 `priority`，避免普通编辑改变排序。
- Restorer 需要在写入前根据恢复后的 `volumeId` 分配 `priority=max(priority)+1`；可复用或新增 repository 方法获取目标卷最大排序值。

恢复写入必须覆盖普通内容字段、状态字段、新排序字段、版本指针字段。

### Admin API

修改：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAdminController.java`

新增注入：

- `ClassicsContentApplicationService contentService`

新增请求：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryVersionRequest.java`

字段：

```java
@NotNull
private Long id;

private Long versionId;
```

`list` 只要求 `id`；`get` 和 `reset` 同时要求 `id`、`versionId`。如果使用同一个 Request 类，不要在 `versionId` 字段上加全局 `@NotNull`，controller 需要在 `get/reset` 显式校验 `versionId`，避免空值进入 `ClassicsContentVersionIdCodec.toDomain(...)`。

新增响应：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryVersionResponse.java`

字段：

```java
private Long id;
private String contentType;
private Long contentId;
private Integer versionNo;
private Date versionedAt;
private String snapshotJson;
private String changeType;
private String changeSummary;
```

同时修改：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryResponse.java`

补齐字段：

```java
private Long currentVersionId;
private Integer currentVersionNo;
private Date currentVersionedAt;
private Date contentUpdatedAt;
private Boolean versionDirty;
```

新增接口：

```text
POST /api/classics/sancai/entries/versions/list
permission: classics:sancai:view
body: { "id": 300000003360 }
response: SancaiEntryVersionResponse[]

POST /api/classics/sancai/entries/versions/get
permission: classics:sancai:view
body: { "id": 300000003360, "versionId": 900000001 }
response: SancaiEntryVersionResponse

POST /api/classics/sancai/entries/versions/reset
permission: classics:sancai:edit
body: { "id": 300000003360, "versionId": 900000001 }
response: SancaiEntryVersionResponse
```

归属校验：

- `get` 和 `reset` 必须先读取版本并校验：
  - version 非空
  - `version.contentType == ClassicsContentType.SANCAI_ENTRY`
  - `version.contentId == ClassicsContentIdCodec.toDomain(request.id)`
- 校验失败抛出：`BizException("三才图会版本不属于当前条目")`
- `reset` 只在归属校验通过后调用 `contentService.restoreHistoryVersion(...)`。
- `list` 不需要逐条归属校验，但必须使用 `ClassicsContentType.SANCAI_ENTRY.value()` 和 request id 调用 `contentService.listVersions(...)`，不能返回其他 content type 的版本。

### Interface Assembler

修改：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java`

补齐 `toResponse(SancaiEntry entity)`：

- `currentVersionId`
- `currentVersionNo`
- `currentVersionedAt`
- `contentUpdatedAt`
- `versionDirty`

`versionDirty` 建议抽私有方法：

```java
private static boolean versionDirty(SancaiEntry entity) {
    if (entity == null) {
        return false;
    }
    if (entity.getCurrentVersionId() == null || entity.getCurrentVersionedAt() == null) {
        return true;
    }
    return entity.getContentUpdatedAt() != null
            && entity.getContentUpdatedAt().after(entity.getCurrentVersionedAt());
}
```

新增：

```java
public static SancaiEntryVersionResponse toVersionResponse(ClassicsContentVersion version)
```

字段映射对齐王圻 `WangqiDocumentInterfaceAssembler.toVersionResponse(...)`，使用：

- `ClassicsContentVersionIdCodec.toValue(version.getId())`
- `ClassicsContentIdCodec.toValue(version.getContentId())`
- `version.getContentType().value()`
- `version.getChangeType().value()`

## Frontend Work

### Types

修改：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`

给 `SancaiEntryRecord` 添加版本字段，新增：

```ts
export interface SancaiContentVersionRecord {
    id: number;
    contentType?: string | null;
    contentId?: number | null;
    versionNo?: number | null;
    versionedAt?: string | null;
    snapshotJson?: string | null;
    changeType?: string | null;
    changeSummary?: string | null;
}

export interface SancaiVersionSnapshot {
    contentType?: string | null;
    contentId?: number | null;
    contentUpdatedAt?: string | null;
    volumeId?: number | null;
    title?: string | null;
    originalText?: string | null;
    translationText?: string | null;
    summary?: string | null;
    lifecycleStatus?: string | null;
    visibility?: string | null;
    translationStatus?: string | null;
    imageStatus?: string | null;
    visualAssetStatus?: string | null;
    refinementStatus?: string | null;
    priority?: number | null;
}
```

### Service

修改：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`

同时把 import 改为：

```ts
import { getJson, postJson } from "@/api/http";
```

新增：

```ts
export const get = (id: number) =>
    getJson<SancaiEntryRecord>(`/classics/sancai/entries/${id}`);

interface SancaiEntryVersionCommand {
    id: number;
    versionId?: number | null;
}

export const listVersions = (entryId: number) =>
    postJson<SancaiContentVersionRecord[], SancaiEntryVersionCommand>(
        "/classics/sancai/entries/versions/list",
        { body: { id: entryId } }
    );

export const getVersion = (entryId: number, versionId: number) =>
    postJson<SancaiContentVersionRecord, SancaiEntryVersionCommand>(
        "/classics/sancai/entries/versions/get",
        { body: { id: entryId, versionId } }
    );

export const resetVersion = (entryId: number, versionId: number) =>
    postJson<SancaiContentVersionRecord, SancaiEntryVersionCommand>(
        "/classics/sancai/entries/versions/reset",
        { body: { id: entryId, versionId } }
    );
```

### Version Panel

新增：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-version-history-panel.tsx`

对齐王圻版本面板，但比较字段使用三才图会快照字段：

- 标题：`title`
- 原文：`originalText`
- 译文：`translationText`
- 摘要：`summary`
- 生命周期：`lifecycleStatus`
- 可见性：`visibility`
- 译文状态：`translationStatus`
- 图像状态：`imageStatus`
- 视觉资产状态：`visualAssetStatus`
- 精修状态：`refinementStatus`
- 排序值：`priority`

交互：

- 左侧列表展示 `versionNo`、`changeType`、`changeSummary`、`versionedAt`。
- 点击“查看”后展示快照详情。
- 对比当前条目值和历史快照值，变化字段加醒目样式。
- 点击“恢复此版本”触发父组件回调。
- `snapshotJson` 为空或 JSON 解析失败时展示警告，不允许无感恢复。
- 恢复按钮在以下场景禁用：
  - 未选中版本。
  - 版本快照无法解析。
  - `resetting=true`。
- `formatValue(...)` 必须把 `null`、`undefined`、空字符串统一展示为 `-`，否则对比结果会因为空值形态不同产生误判。

### Entry Panel Integration

修改：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`

新增状态：

- `selectedVersion: SancaiContentVersionRecord | null`

新增查询：

- query key：`["classics", "sancai", "entries", selectedEntry?.id, "versions"]`
- enabled：`isModelOpen && !isCreating && Boolean(selectedEntry?.id)`
- queryFn：`entryService.listVersions(selectedEntry.id)`
- 当 `selectedEntry?.id` 变化时必须把 `selectedVersion` 重置为 `null`，避免不同条目的历史版本串台。
- 版本列表加载成功后：
  - 如果当前没有 `selectedVersion`，默认选中列表第一条。
  - 如果已有 `selectedVersion`，但新列表不包含该版本 id，则重置为第一条或 `null`。

新增 mutation：

- `resetVersionMutation`
- 成功后失效：
  - `["classics", "sancai", "entries"]`
  - 当前条目版本 query key
- 成功后必须调用 `entryService.get(entryId)` 并 `setEditingEntry(updatedEntry)`，保持抽屉打开。
- 成功后必须弹出明确 message box，不能只用轻量 toast。文案建议：
  - 标题：`历史版本已恢复`
  - 内容：`当前条目内容已恢复为版本 {versionNo} 的快照，并已生成新的正式版本。条目已移动到当前卷末尾。`
  - 确认按钮：`知道了`

恢复确认：

- 使用 `useKuzhambuConfirm().danger(...)`
- 标题：`恢复三才图会历史版本`
- 描述必须提示“恢复会覆盖当前条目内容，并生成新的正式版本。”

恢复完成提示：

- 使用明确的 modal/message box，例如 Ant Design `Modal.success(...)` 或项目等价确认弹窗。
- 不要只用 `messageApi.success(...)`，否则用户容易错过恢复结果。
- 提示内容必须同时说明：
  - 已恢复到所选历史版本。
  - 已生成新的正式版本。
  - 条目已移动到当前卷末尾。

传入抽屉：

- 给 `SancaiEntryModel` 增加 `afterForm?: ReactNode`，或参照 `WangqiDocumentModel` 的 `afterForm` 方案。
- 编辑模式下把 `SancaiVersionHistoryPanel` 放在表单后。
- 新增模式下不展示版本面板。

强制修正：

- `SancaiEntryModel` 当前用 `useState(() => toEntryFormValues(entry))` 初始化表单。实现时必须在 `entry/open/mode` 变化时重置表单状态，否则恢复成功后抽屉里的表单仍显示旧内容。
- 保存成功后如果接口只返回 `{id}`，列表失效后仍要关闭抽屉；不要把局部旧 `editingEntry` 当作最新数据继续展示。
- 如果保持抽屉打开，保存成功也应调用 `entryService.get(id)` 刷新当前条目。

### Styles

修改：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`

新增样式类建议：

- `.sancai-version-history-panel`
- `.sancai-version-history-panel-grid`
- `.sancai-version-history-list`
- `.sancai-version-history-detail`
- `.sancai-version-history-detail-stack`
- `.sancai-version-compare`
- `.sancai-version-compare .is-changed`

保持后台工具界面风格：紧凑、可扫描，不做营销式卡片布局。

### API Test Fixtures

前端测试 mock 需要同时覆盖：

- 初始条目：`title="恢复前标题"`，`summary="恢复前摘要"`。
- 历史快照：`title="历史标题"`，`summary="历史摘要"`，其余状态字段和当前条目保持一致。
- reset 响应：`changeType="HISTORY_RESTORED"`。
- reset 后 `entryService.get(id)` 响应：当前条目 title/summary 已变成历史快照值，`priority` 为当前卷末尾新排序值，`currentVersionNo` 为新版本号，`versionDirty=false`。
- reset 后断言弹出 message box，且文案说明已恢复、已生成新正式版本、条目已移动到当前卷末尾。

## Execution Order

按以下顺序小步实施，避免前后端契约漂移：

1. 后端数据契约：
   - 补 `SancaiEntryResponse` 版本字段。
   - 补 `SancaiEntryVersionRequest`、`SancaiEntryVersionResponse`。
   - 补 `SancaiInterfaceAssembler.toVersionResponse(...)` 和 `versionDirty(...)`。
2. 后端恢复能力：
   - 补 `SancaiEntryVersionRestorer`。
   - 新增 Restorer 专用写入方法，恢复时将 `priority` 分配为目标卷 `max(priority)+1`。
   - 在 `ClassicsContentApplicationServiceImpl.restoreHistoryVersion(...)` 增加 `SANCAI_ENTRY` 分支。
3. 后端接口：
   - 在 `SancaiAdminController` 注入 `ClassicsContentApplicationService`。
   - 增加 `entries/versions/list|get|reset`。
   - 补归属校验和空 `versionId` 校验。
4. 后端测试：
   - 先补 Restorer 单测。
   - 再补 Content service 分发测试。
   - 最后补 Controller contract 测试。
5. 前端服务和类型：
   - 补 `SancaiEntryRecord` 版本字段。
   - 补 version record、snapshot 类型。
   - 补 `get/listVersions/getVersion/resetVersion` 服务方法和 contract 测试。
6. 前端 UI：
   - 新增 `SancaiVersionHistoryPanel`。
   - 接入 `SancaiEntryPanel` 查询、选择、恢复、刷新当前条目。
   - 修正 `SancaiEntryModel` 表单随 entry 变化同步。
7. 前端测试和 E2E：
   - 补组件测试。
   - 补 `sancai.spec.ts` 版本闭环。
8. 收尾：
   - 跑本 RUNBOOK 的 Local Validation。
   - 更新 coverage 文档。
   - 删除本 RUNBOOK。

## Tests

### Backend

补充或新增：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/.../content/service/impl/ClassicsContentApplicationServiceImplTest.java`
  - `restoreHistoryVersion` 遇到 `SANCAI_ENTRY` 时调用 `SancaiEntryVersionRestorer`。
  - 恢复后创建新版本，`changeType=HISTORY_RESTORED`。
  - 恢复后调用 `markVersioned(...)` 持久化当前版本指针。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/.../sancai/support/SancaiEntryVersionRestorerTest.java`
  - 正常快照恢复内容字段和状态字段。
  - 正常恢复不使用历史 `priority`，而是分配目标卷 `max(priority)+1`。
  - 快照 content type 错误时报错。
  - 快照 content id 不一致时报错。
  - 当前条目不存在时报错。
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/.../sancai/controller/SancaiAdminControllerTest.java`
  - `entries/versions/list` 路由、权限、body 映射。
  - `entries/versions/get` 归属校验失败。
  - `entries/versions/reset` 归属校验通过后调用恢复。

### Frontend

补充或新增：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
  - `listVersions` 路径和 body。
  - `getVersion` 路径和 body。
  - `resetVersion` 路径和 body。
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
  - 编辑条目时加载版本列表。
  - 点击版本查看后展示当前 / 历史对比。
  - 恢复时弹确认，确认后调用 reset 接口、刷新条目列表、刷新当前抽屉条目。
  - 恢复成功后弹出 message box，说明已恢复、已生成新正式版本、条目已移动到当前卷末尾。
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`
  - 若页面级集成已有覆盖，补版本入口可见性即可。
- `kuzhambu-apps/admin-web/e2e/classics/sancai/sancai.spec.ts`
  - 增加版本闭环 Playwright 场景。

## Local Validation

后端最小验证：

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/classics/kuzhambu-classics-application -Dtest=ClassicsContentApplicationServiceImplTest,SancaiEntryVersionRestorerTest test
mvn -pl biz/classics/kuzhambu-classics-interface -Dtest=SancaiAdminControllerTest test
```

前端最小验证：

```sh
cd kuzhambu-apps
npm run format:check
npm run lint
npm --workspace admin-web run test -- --run sancai
cd admin-web
npx playwright test e2e/classics/sancai/sancai.spec.ts
```

全量验证按 `AGENTS.md`：

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn test

cd ../kuzhambu-apps
npm run format:check
npm run lint
npm run test
npm run build
```

## Manual Smoke

本地服务：

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

手工验证路径：

1. 打开 admin-web，进入 `/classics/sancai`。
2. 选择一个卷目和条目。
3. 记录当前标题、原文、译文、摘要、可见性。
4. 保存一次条目，确认版本列表增加 `MANUAL_SAVE` 版本。
5. 修改标题或摘要，再保存一次。
6. 打开版本历史，选择上一个版本，确认当前 / 历史对比有变化字段标记。
7. 点击恢复，确认弹窗文案说明会覆盖当前内容并生成新正式版本。
8. 恢复后重新打开条目，确认内容回到历史版本。
9. 版本列表新增一条 `HISTORY_RESTORED`，`changeSummary` 为 `恢复历史版本 v{被恢复版本号}`。
10. 当前条目响应里的 `currentVersionNo` 指向新版本，`versionDirty=false`。
11. 当前条目 `priority` 是目标卷恢复后的最大排序值，列表中移动到当前卷末尾。
12. 恢复成功后出现 message box，文案说明已恢复、已生成新正式版本、条目已移动到当前卷末尾。

如果 smoke 使用临时条目，清理规则：

- 通过 UI 或 API 删除临时 `classics_sancai_entry`。
- 开发库中同步删除该临时条目的 `classics_content_version` 记录：

```sql
delete from classics_content_version
where content_type = 'SANCAI_ENTRY'
  and content_id = :temporary_entry_id;
```

## Out Of Scope

本次不处理：

- 三才图会分类、卷目的版本历史。
- 自动保存草稿版本。
- AI 候选应用链路。
- 分享版本快照固定化。
- QA、标签、导出任务的版本恢复。
- 历史版本删除能力。
- 历史版本跨条目复制。

## Closeout

完成后必须：

- 更新 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`，把三才图会“版本历史、版本对比和历史恢复”状态改为完成或说明剩余缺口。
- 若有任务 TODO 文件，按 `docs/00-governance/TODO-RULES.md` 清理已完成项。
- 删除本 RUNBOOK。
- 小步提交，每个提交只覆盖一个清晰边界，例如：
  - `Feat(classics): 支持三才图会版本恢复后端`
  - `Feat(admin-web): 接入三才图会版本历史面板`
  - `Test(classics): 覆盖三才图会版本恢复闭环`
  - `Docs(classics): 更新三才图会完成度`
