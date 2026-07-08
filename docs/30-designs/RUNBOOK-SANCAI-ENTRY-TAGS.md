# RUNBOOK Sancai 条目标签闭环

## 目标

补齐 Sancai 条目详情里的标签聚合展示、前端标签编辑入口和通用内容标签入参治理。完成后，Sancai 条目详情抽屉可以直接展示当前标签，并能从详情上下文进入同抽屉内的标签治理面板完成新增、编辑、排序和移除；标签写入和删除继续维护 Classics 主事实与 Knowledge 内容标签引用投影。

## 范围

- 只覆盖 `SANCAI_ENTRY` 的详情展示和前端入口。
- 通用内容标签接口只补必要字段、删除路由和入参治理。
- 不改 Wangqi 页面。
- 不改 Ming 页面。
- 不新增或修改数据库表结构。
- 不改 Knowledge 标签治理页面、Knowledge 表结构和 Knowledge facade 契约。

## 数据结构变更

### Sancai 条目详情响应

文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryResponse.java`

新增字段：

| Java 字段 | JSON 字段 | 类型 | 来源 | 说明 |
| --- | --- | --- | --- | --- |
| `tags` | `tags` | `List<ClassicsContentResponse>` | `classics_content_tag` | 当前 Sancai 条目的标签绑定列表。 |

### 通用内容标签响应

文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/response/ClassicsContentResponse.java`

新增字段：

| Java 字段 | JSON 字段 | 类型 | 来源字段 | 说明 |
| --- | --- | --- | --- | --- |
| `tagId` | `tagId` | `Long` | `classics_content_tag.tag_id` | Knowledge 统一标签 ID。 |
| `source` | `source` | `String` | `classics_content_tag.source` | 标签来源，例如 `MANUAL`、`AI`。 |
| `priority` | `priority` | `Integer` | `classics_content_tag.priority` | 当前内容内标签展示排序。 |

标签响应中继续使用的既有字段：

| Java 字段 | JSON 字段 | 类型 | 来源字段 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `id` | `Long` | `classics_content_tag.id` | Classics 内容标签绑定 ID。 |
| `contentType` | `contentType` | `String` | `classics_content_tag.content_type` | 内容类型，本任务为 `SANCAI_ENTRY`。 |
| `contentId` | `contentId` | `Long` | `classics_content_tag.content_id` | Sancai 条目 ID。 |
| `tagNameSnapshot` | `tagNameSnapshot` | `String` | `classics_content_tag.tag_name_snapshot` | 标签展示快照。 |
| `status` | `status` | `String` | `classics_content_tag.status` | 标签绑定状态。 |

### 前端类型

文件：`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-types.ts`

新增字段和类型：

| TypeScript 定义 | 类型 | 说明 |
| --- | --- | --- |
| `ClassicsContentTagRecord.priority` | `number \| null \| undefined` | 接收后端标签排序字段。 |
| `ClassicsContentTagDeletePayload.id` | `number` | 删除标签绑定的请求入参。 |

文件：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`

新增字段：

| TypeScript 字段 | 类型 | 说明 |
| --- | --- | --- |
| `SancaiEntryRecord.tags` | `ClassicsContentTagRecord[] \| undefined` | Sancai 详情聚合标签。 |

## 小任务拆分

### 任务 1：Sancai 详情聚合标签

相关文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAdminControllerTest.java`

实现要求：

- `GET /api/classics/sancai/entries/{id}` 在读取 `SancaiEntry` 后，同步读取 `ClassicsContentApplicationService.listTags("SANCAI_ENTRY", ClassicsContentId.of(id))`。
- `SancaiInterfaceAssembler.toResponse(SancaiEntry, List<ClassicsContentTag>)` 输出 `SancaiEntryResponse.tags`。
- `SancaiInterfaceAssembler.toResponse(SancaiEntry)` 保留，默认传空标签列表，避免影响列表页响应。
- 测试断言详情响应中存在 `tags[0].tagNameSnapshot`。

验收点：

- Sancai 列表页仍只依赖原条目字段。
- Sancai 详情页响应包含 `tags` 数组。
- 空标签条目返回空数组，不返回 `null`。

### 任务 2：通用内容标签接口治理

相关文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/response/ClassicsContentResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`

接口要求：

- `GET /api/classics/content/tags`
  - 必填：`contentType`、`contentId`
  - `contentType` 白名单：`SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS`
  - 不允许空条件查询标签表。
- `POST /api/classics/content/tags/add`
  - 必填：`contentType`、`contentId`、`tagNameSnapshot`
  - `tagNameSnapshot` 必须 trim。
- `POST /api/classics/content/tags/update`
  - 必填：`id`、`contentType`、`contentId`、`tagNameSnapshot`
  - 必须确认 existing tag 归属于同一个 `contentType + contentId`。
- `POST /api/classics/content/tags/sort`
  - 必填：`contentType`、`contentId`、`orderedIds`
  - `orderedIds` 必须非空且去重。
- `POST /api/classics/content/tags/delete`
  - 必填：`id`
  - 删除 Classics 标签绑定，并移除 Knowledge 内容标签引用投影。

Application 要求：

- `listTags` 校验 `contentType` 和 `contentId`，防止绕过 Controller 产生空范围查询。
- `addTag`、`updateTag` 校验必要字段；`source` 缺失时补 `MANUAL`，`status` 缺失时补 `ACTIVE`。
- `updateTag` 不允许把标签绑定从一个内容迁移到另一个内容。
- `deleteTag` 在标签不存在时直接返回；存在时调用 `ClassicsTagBindingSupport.removeTagRef(existing)`。

验收点：

- 标签响应包含 `id`、`contentType`、`contentId`、`tagId`、`tagNameSnapshot`、`source`、`status`、`priority`。
- 缺少必要参数时返回参数错误，不进入 repository 查询或写入。
- 删除标签会清理 Knowledge 内容引用投影。

### 任务 3：前端通用标签面板删除闭环

相关文件：

- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-content-tag-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service-contract.test.ts`

控件和操作要求：

- `classics-content-service.ts` 新增 `deleteTag({ id })`。
- `deleteTag` 请求路径固定为 `POST /classics/content/tags/delete`。
- `ClassicsContentTagPanel` 标签行中的“移除”按钮：
  - 仍使用 `Button` 控件。
  - 保留 `danger`。
  - 图标使用 `DeleteOutlined`。
  - 可访问名称为 `aria-label="移除标签 {id}"`。
  - 点击后调用 `deleteTag({ id })`。
  - 删除请求 pending 时显示 loading。
- “移除”不再调用 `updateTag`，不再仅把 `status` 改成 `REMOVED`。
- 删除成功后刷新当前标签 query，并调用 `onChanged`。

验收点：

- 点击“移除”后调用 `/classics/content/tags/delete`。
- 标签列表刷新。
- Sancai 详情聚合标签刷新。
- Knowledge 内容引用投影被后端删除。

### 任务 4：Sancai 详情抽屉标签展示和编辑入口

相关文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`

控件和操作要求：

- `SancaiEntryModel` 的“条目上下文”区保留：
  - 翻译状态文本
  - 图片状态文本
  - 视觉状态文本
  - 精修状态文本
  - 原文文本
  - 译文文本
- “条目上下文”区新增标签展示行：
  - 固定文本：`标签：`
  - 有标签时展示 Ant Design `Tag` 控件，内容为 `tagNameSnapshot`。
  - 无标签时展示文本 `未标注标签`。
- 标签展示数据来源：
  - 优先使用 `SancaiEntryRecord.tags[].tagNameSnapshot`。
  - 当详情未返回 `tags` 时，兜底使用现有通用标签 query 的标签名。
- 标签展示行新增“编辑标签”按钮：
  - 控件：Ant Design `Button`
  - 文案：`编辑标签`
  - 图标：`EditOutlined`
  - 尺寸：`size="small"`
  - 可访问名称：`aria-label="编辑三才图会条目标签"`
  - 操作：点击后滚动到同一抽屉内的“三才图会标签治理”面板，并 focus 面板锚点。
- `SancaiEntryPanel` 在“三才图会标签治理”面板外层增加锚点容器：
  - 使用 `ref`
  - 设置 `tabIndex={-1}`
  - 点击“编辑标签”时执行 `scrollIntoView({ block: "start", behavior: "smooth" })`
- 标签新增、编辑、排序、移除后刷新：
  - `["classics", "content", "tags", "SANCAI_ENTRY", selectedEntryId]`
  - `["classics", "sancai", "entries", "detail", selectedEntryId]`
  - `["classics", "sancai", "entries"]`

验收点：

- 打开 Sancai 条目详情抽屉即可看到标签。
- 点击“编辑标签”会定位到“三才图会标签治理”面板。
- 在标签治理面板新增、编辑、排序、移除后，详情上下文标签同步更新。
- Sancai 测试 mock 包含 `deleteTag`，标签面板渲染不报 mock 缺失。

## 前端人工验收步骤

1. 打开后台 Sancai 页面。
2. 选择一个门类和卷目。
3. 在条目列表点击“查看”或等价查看操作，打开 Sancai 条目详情抽屉。
4. 在“条目上下文”区域确认标签展示：
   - 有标签：显示一个或多个 `Tag`。
   - 无标签：显示 `未标注标签`。
5. 点击“编辑标签”按钮。
6. 确认抽屉滚动到“三才图会标签治理”面板。
7. 点击“三才图会标签治理”面板的“新增标签”按钮。
8. 在弹窗的“标签名称”输入框输入标签名，点击确认。
9. 确认新标签出现在标签列表和详情“条目上下文”标签行。
10. 点击标签行的“编辑”按钮，修改“标签名称”，点击确认。
11. 确认详情“条目上下文”标签名更新。
12. 对标签列表执行排序操作。
13. 确认排序请求成功，标签列表保持新顺序。
14. 点击标签行的“移除”按钮。
15. 确认标签从标签列表和详情“条目上下文”中消失。

## 验证命令

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-interface,biz/classics/kuzhambu-classics-application -am spotless:apply
mvn -pl biz/classics/kuzhambu-classics-interface,biz/classics/kuzhambu-classics-application -am test
```

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format
pnpm --filter kuzhambu-admin-web exec vitest run src/pages/classics/common/classics-content-service-contract.test.ts src/pages/classics/sancai/components/sancai-entry-panel.test.tsx
pnpm --filter kuzhambu-admin-web run lint
```

## 回滚口径

- 回滚任务 1 时，同步移除 `SancaiEntryRecord.tags` 的前端优先读取。
- 回滚任务 2 的 `tags/delete` 时，同步回滚任务 3 的 `deleteTag` service 和“移除”按钮行为。
- 回滚任务 2 的 Application 入参防线时，必须确认 Controller 层参数治理仍保留。
- 回滚任务 4 时，保留原有通用标签治理面板，不影响已有标签管理能力。
