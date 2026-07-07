# Classics 生命周期编辑闭环 RUNBOOK

## 目标

补齐三才图会条目 `SANCAI_ENTRY` 的草稿、发布、归档、恢复发布管理闭环：后台用户在 Admin Web 条目列表中按当前生命周期状态执行明确动作，后端通过专用管理接口校验状态流转、写入主表字段、追加正式版本，并刷新列表、详情、版本历史和搜索同步。

本轮不覆盖王圻文档和明代习俗。需求已明确王圻文档、明代习俗不使用草稿、发布、归档生命周期。

## 最终行为

| 当前状态 | 允许动作 | 目标状态 | Admin 文案 |
| --- | --- | --- | --- |
| `DRAFT` | 发布 | `PUBLISHED` | 发布 |
| `PUBLISHED` | 归档 | `ARCHIVED` | 归档 |
| `ARCHIVED` | 恢复发布 | `PUBLISHED` | 恢复发布 |

禁止流转：

- `PUBLISHED -> DRAFT`
- `ARCHIVED -> DRAFT`
- `DRAFT -> ARCHIVED`

边界规则：

- “恢复发布”只表示 `ARCHIVED -> PUBLISHED`，不等同于历史版本恢复。
- 历史版本恢复继续使用现有 `POST /api/classics/sancai/entries/versions/reset`。
- 归档不是删除，不触发分享目标 `CONTENT_DELETED`。
- 不做批量生命周期动作。
- 不新增权限资源，统一使用 `classics:sancai:edit`。

## 数据结构

本轮不新增表，不新增字段，不新增枚举值，不写数据库 migration。

必须精确读写的既有字段：

| 表 | 字段 | 写入时机 | 要求 |
| --- | --- | --- | --- |
| `classics_sancai_entry` | `lifecycle_status` | 生命周期变更成功时 | 写入目标值：`PUBLISHED` 或 `ARCHIVED` |
| `classics_sancai_entry` | `content_updated_at` | 生命周期变更成功时 | 更新为当前时间 |
| `classics_sancai_entry` | `current_version_id` | 正式版本追加成功后 | 指向新生成的 `classics_content_version.id` |
| `classics_sancai_entry` | `current_version_no` | 正式版本追加成功后 | 写入新版本号 |
| `classics_sancai_entry` | `current_versioned_at` | 正式版本追加成功后 | 写入新版本生成时间 |
| `classics_content_version` | `content_type` | 正式版本追加时 | 固定为 `SANCAI_ENTRY` |
| `classics_content_version` | `content_id` | 正式版本追加时 | 写入三才条目 ID |
| `classics_content_version` | `version_no` | 正式版本追加时 | 按现有版本服务递增 |
| `classics_content_version` | `snapshot_json` | 正式版本追加时 | 必须包含变更后的 `lifecycleStatus` |
| `classics_content_version` | `change_type` | 正式版本追加时 | 继续使用手动确认语义 |
| `classics_content_version` | `change_summary` | 正式版本追加时 | 写入可读摘要，例如 `发布条目`、`归档条目`、`恢复发布条目` |

实现时需要确认 `SancaiApplicationServiceImpl.changeEntryStatus` 不只是更新内存实体和生成版本，还要通过现有 repository 更新路径把 `classics_sancai_entry.lifecycle_status` 落库。

## 接口契约

新增专用 Admin API：

```text
POST /api/classics/sancai/entries/lifecycle/change
```

请求体：

```json
{
  "id": 123,
  "lifecycleStatus": "PUBLISHED"
}
```

响应：

```json
true
```

接口规则：

- Controller：`SancaiAdminController`
- 权限：`@HasPermission("classics:sancai:edit")`
- 日志：`@SysLogger(value = "生命周期变更")`
- 入参只允许使用 `id` 和 `lifecycleStatus`
- 不复用 `entries/update` 做生命周期变更
- 不把可见性、删除、版本恢复、批量操作混入该接口

## 小任务拆分

### 任务 1：后端状态流转和落库

目标：应用层只允许三条生命周期流转，并确保主表字段和正式版本一致。

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiEntryStatusCommand.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/model/enums/SancaiEntryLifecycleStatus.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiRepositoryImpl.java`

要求：

- `changeEntryStatus` 读取当前条目后校验当前状态与目标状态。
- 只允许 `DRAFT -> PUBLISHED`、`PUBLISHED -> ARCHIVED`、`ARCHIVED -> PUBLISHED`。
- 目标状态为空、当前状态为空、无效流转都抛业务异常。
- 成功时写入 `lifecycle_status`、`content_updated_at`。
- 成功时追加正式版本并刷新 `current_version_id/current_version_no/current_versioned_at`。
- 成功时触发现有搜索同步。
- 不改 `SancaiEntryLifecycleStatus` 枚举值；只在需要时补辅助判断方法。

### 任务 2：后端 Admin 接口和接口测试

目标：暴露单条生命周期变更接口，并覆盖路径、请求体和权限。

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAdminControllerTest.java`

要求：

- 新增 `POST entries/lifecycle/change`。
- 请求体继续使用或精简复用 `SancaiEntryRequest`，但接口只读取 `id` 和 `lifecycleStatus`。
- Controller 将 `lifecycleStatus` 转成 `SancaiEntryLifecycleStatus`。
- Controller 传入 `KuzhambuContextHolder.currentAuthorities()`。
- 测试断言请求路径为 `/api/classics/sancai/entries/lifecycle/change`。
- 测试断言请求体字段为 `id` 和 `lifecycleStatus`。
- 测试断言权限仍是 `classics:sancai:edit`。

### 任务 3：后端应用测试

目标：锁定生命周期规则、版本追加和权限失败。

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/support/FakeSancaiRepositorySupport.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`

要求：

- 覆盖 `DRAFT -> PUBLISHED` 成功。
- 覆盖 `PUBLISHED -> ARCHIVED` 成功。
- 覆盖 `ARCHIVED -> PUBLISHED` 成功。
- 覆盖 `PUBLISHED -> DRAFT` 失败。
- 覆盖 `ARCHIVED -> DRAFT` 失败。
- 覆盖无 `classics:sancai:edit` 权限失败。
- 成功用例断言实体最终 `lifecycleStatus`、`contentUpdatedAt`、`currentVersionId/currentVersionNo/currentVersionedAt` 被更新。
- 成功用例断言新增版本的 `snapshotJson` 包含目标 `lifecycleStatus`。

### 任务 4：前端 service 和类型契约

目标：Admin Web 通过专用 service 调生命周期接口，不复用完整编辑保存。

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`

要求：

- 新增类型 `SancaiEntryLifecycleStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED"`。
- `SancaiEntryRecord.lifecycleStatus` 使用该类型或兼容字符串。
- 新增 service 入参类型：

```ts
export interface SancaiEntryLifecycleCommand {
    id: number;
    lifecycleStatus: SancaiEntryLifecycleStatus;
}
```

- 新增方法 `changeLifecycleStatus(command: SancaiEntryLifecycleCommand)`。
- 方法请求 `POST /classics/sancai/entries/lifecycle/change`。
- 请求体只包含 `id` 和 `lifecycleStatus`。
- contract test 精确断言路径和 body。

### 任务 5：前端列表控件和交互测试

目标：三才条目列表操作列提供发布、归档、恢复发布三种单条动作。

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-list.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`

控件要求：

- 在 `SancaiEntryList` 的 `actions` 操作列新增生命周期菜单项。
- 菜单项位置：`查看` 后、分隔线前、`删除` 前。
- 当前 `DRAFT` 显示按钮文本 `发布`，`ariaLabel` 为 `发布 ${条目标题}`。
- 当前 `PUBLISHED` 显示按钮文本 `归档`，`ariaLabel` 为 `归档 ${条目标题}`。
- 当前 `ARCHIVED` 显示按钮文本 `恢复发布`，`ariaLabel` 为 `恢复发布 ${条目标题}`。
- 无 `classics:sancai:edit` 权限时按钮禁用。
- 未知状态不显示生命周期按钮。

确认弹窗要求：

- 使用 `useKuzhambuConfirm`。
- 发布弹窗：
  - `title`: `发布三才图会条目`
  - `message`: `确认发布 ${条目标题}？`
  - `description`: `发布后条目进入已发布治理范围，公开或私有仍由可见性字段决定。`
  - `okText`: `发布`
- 归档弹窗：
  - `title`: `归档三才图会条目`
  - `message`: `确认归档 ${条目标题}？`
  - `description`: `归档后条目不进入默认列表、搜索、问答、导出和静态展示。`
  - `okText`: `归档`
- 恢复发布弹窗：
  - `title`: `恢复发布三才图会条目`
  - `message`: `确认恢复发布 ${条目标题}？`
  - `description`: `恢复后条目重新进入已发布治理范围。`
  - `okText`: `恢复发布`

操作要求：

- 点击菜单项后只打开确认弹窗，不立即调用接口。
- 确认后调用 `entryService.changeLifecycleStatus`。
- 成功后刷新 `["classics", "sancai", "entries"]`。
- 若详情抽屉当前打开，成功后刷新详情、版本历史和列表。
- 成功提示：
  - 发布：`三才图会条目已发布`
  - 归档：`三才图会条目已归档`
  - 恢复发布：`三才图会条目已恢复发布`
- 不把 `lifecycleStatus` 放进编辑表单控件。
- 不新增批量发布、批量归档、批量恢复发布按钮。

测试要求：

- 草稿条目渲染 `发布`。
- 已发布条目渲染 `归档`。
- 已归档条目渲染 `恢复发布`。
- 无编辑权限时生命周期按钮禁用。
- 点击 `发布` 后弹窗标题为 `发布三才图会条目`。
- 确认 `发布` 后请求 body 为 `{ id, lifecycleStatus: "PUBLISHED" }`。
- 成功后出现 `三才图会条目已发布`。

### 任务 6：覆盖文档和收口

目标：实现完成后更新 coverage，并关闭临时 RUNBOOK。

文件：

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-CLASSICS-LIFECYCLE-EDIT-LOOP.md`

要求：

- 将 `生命周期：草稿、发布、归档、恢复` 从部分完成更新为已完成。
- 说明管理接口、Admin Web 单条操作、状态流转校验、正式版本追加均已闭环。
- 本 RUNBOOK 在任务关闭前删除，不长期保留。

## 验证命令

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/classics -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/classics -am test
```

前端：

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm run format:check
npm run lint
npm --workspace kuzhambu-admin-web run test
```

## 审核清单

- 数据库没有新增表或字段。
- 主表 `classics_sancai_entry.lifecycle_status` 与版本快照 `snapshot_json.lifecycleStatus` 一致。
- 生命周期接口只读取 `id` 和 `lifecycleStatus`。
- 状态流转只允许三条最终行为路径。
- `PUBLISHED` 和 `ARCHIVED` 都不能退回 `DRAFT`。
- Admin Web 生命周期动作只出现在单条操作列，不出现在批量工具栏。
- 编辑表单没有生命周期下拉框。
- 归档不触发删除和分享目标 `CONTENT_DELETED`。
- 王圻文档和明代习俗没有生命周期改动。
- 权限仍使用 `classics:sancai:edit`。
