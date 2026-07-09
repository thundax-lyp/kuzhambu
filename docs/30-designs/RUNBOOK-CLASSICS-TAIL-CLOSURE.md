# Classics Tail Closure Runbook

## 目标

收口 Classics coverage 中仍影响“需求已完成”的尾项：

- 王圻文档和明代习俗详情页补齐 AI 标签提取和问答对生成入口，并与现有摘要任务、候选确认和版本治理闭环一致。
- 明代习俗标签云改为基于 `classics_content_tag` 统一内容标签统计，点击标签后按统一标签筛选列表，并遵守后端权限过滤。
- 三才图会多选需求明确降级为当前页多选；跨页范围动作统一使用“筛选结果”作用域。

本 RUNBOOK 只定义最终交付，不记录中间状态。实现完成、coverage 更新后删除本文件。

## 数据结构变更

### 明代习俗查询入参

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/query/MingCustomsPageQuery.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/request/MingCustomsRequest.java`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-service.ts`

字段口径：

| 字段 | 类型 | 处理 |
| --- | --- | --- |
| `category` | `String` / `string` | 保留，分类筛选 |
| `keyword` | `String` / `string` | 保留，文本搜索，不承载标签筛选 |
| `visibility` | `MingCustomsVisibility` / `string` | 保留，可见性筛选 |
| `sortDirection` | `SortDirection` / `"ASC"` 或 `"DESC"` | 保留，排序方向 |
| `tagName` | `String` / `string` | 保留为历史兼容字段，新页面不再写入 |
| `tagId` | `Long` / `number` | 新增，统一标签 ID 筛选，优先级高于 `tagNameSnapshot` |
| `tagNameSnapshot` | `String` / `string` | 新增，无 `tagId` 的历史标签兜底筛选 |

筛选规则：

- `tagId` 非空时，列表必须命中 `classics_content_tag.content_type = 'MING_CUSTOMS'`、`content_id = classics_ming_customs_entry.id`、`tag_id = tagId`、`status = 'ACTIVE'`。
- `tagId` 为空且 `tagNameSnapshot` 非空时，列表必须命中 `classics_content_tag.content_type = 'MING_CUSTOMS'`、`content_id = classics_ming_customs_entry.id`、`tag_name_snapshot = tagNameSnapshot`、`status = 'ACTIVE'`。
- `tagId`、`tagNameSnapshot` 必须与 `category`、`keyword`、`visibility`、权限过滤组合生效。

### 明代习俗标签云数据结构

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/mingcustoms/model/valueobject/MingCustomsTagCloudItem.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsTagCloudItemResponse.java`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-types.ts`

新增字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `tagId` | `Long` / `number` / `null` | Knowledge 统一标签 ID；历史标签允许为空 |
| `tagNameSnapshot` | `String` / `string` | Classics 保存的标签展示名快照 |
| `count` | `Long` / `number` | 当前筛选和权限口径下绑定该标签的明代习俗条目数 |

接口：

- 新增 `GET /api/classics/ming-customs/tag-cloud`。
- 查询参数：`visibility`、`category`、`keyword`，均可为空。
- 响应：`Array<{ tagId, tagNameSnapshot, count }>`。
- 排序：`count DESC`，同 count 时按 `tagNameSnapshot ASC`。
- 旧 `GET /api/classics/ming-customs/keyword-cloud` 可保留为关键词云，不再用于标签云筛选。

### 权限过滤口径

涉及文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentPermissionSupport.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`

规则：

- 明代习俗列表和标签云必须使用同一个可见性过滤决策。
- 没有私有内容查看能力时，后端强制 `visibility = PUBLIC`，即使请求未传 `visibility` 也不得统计或返回私有习俗。
- 有私有内容查看能力时，`visibility` 为空表示全部，`PUBLIC` 表示公开，`PRIVATE` 表示私有。
- 如 `ClassicsContentPermissionSupport` 已有等价方法，直接复用；如没有，新增一个明确方法，例如 `canViewPrivate(ClassicsContentType contentType, Set<String> permissions)`，不得把权限判断散落到 mapper、controller 或前端。

### AI 精修能力字段

涉及文件：

- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-types.ts`

字段口径：

| 字段 | 取值 | 说明 |
| --- | --- | --- |
| `capability` | `summary` | 摘要生成，已有能力 |
| `capability` | `tags` | 标签提取，本轮补齐入口和文案 |
| `capability` | `qa` | 问答对生成，本轮补齐入口和文案 |

前端能力文案：

- `summary` -> `摘要`
- `tags` -> `标签`
- `qa` -> `问答对`

## 小任务拆解

### 任务 1：明代习俗查询模型和权限决策

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentPermissionSupport.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/query/MingCustomsPageQuery.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/MingCustomsApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`

实施要求：

- `MingCustomsPageQuery` 新增 `tagId`、`tagNameSnapshot` 字段。
- `MingCustomsApplicationService` 新增 `listTagCloud(MingCustomsPageQuery query)`。
- `MingCustomsApplicationServiceImpl.page(...)` 对 `query.operatorPermissions` 做统一可见性收敛。
- `MingCustomsApplicationServiceImpl.listTagCloud(...)` 复用同一可见性收敛逻辑。
- 私有查看能力判断集中在 `ClassicsContentPermissionSupport`。

验收点：

- 无私有查看能力时，列表和标签云都不会读取私有习俗。
- 有私有查看能力时，`visibility` 参数按请求生效。

### 任务 2：明代习俗标签云领域对象和仓储接口

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/mingcustoms/model/valueobject/MingCustomsTagCloudItem.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/mingcustoms/repository/MingCustomsRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/mingcustoms/repository/impl/MingCustomsRepositoryImpl.java`

实施要求：

- 新增 `MingCustomsTagCloudItem`，只包含 `tagId`、`tagNameSnapshot`、`count` 三个字段。
- `MingCustomsRepository.page(...)` 和 `list(...)` 支持 `tagId`、`tagNameSnapshot`。
- `MingCustomsRepository` 新增 `listTagCloud(...)`，入参必须覆盖 `category`、`keyword`、`visibility`。
- `MingCustomsRepositoryImpl` 查询 `classics_content_tag`，不得继续从 `classics_ming_customs_keyword` 统计标签云。

SQL 口径：

- 列表筛选关联条件：
  - `classics_content_tag.content_type = 'MING_CUSTOMS'`
  - `classics_content_tag.content_id = classics_ming_customs_entry.id`
  - `classics_content_tag.status = 'ACTIVE'`
  - `classics_content_tag.tag_id = :tagId` 或 `classics_content_tag.tag_name_snapshot = :tagNameSnapshot`
- 标签云聚合字段：
  - `classics_content_tag.tag_id`
  - `classics_content_tag.tag_name_snapshot`
  - `COUNT(DISTINCT classics_content_tag.content_id) AS count`

验收点：

- 标签云统计只来自 `classics_content_tag`。
- 标签云统计受 `category`、`keyword`、`visibility` 影响。

### 任务 3：明代习俗 Admin API 契约

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/request/MingCustomsRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsTagCloudItemResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/assembler/MingCustomsInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/MingCustomsAdminController.java`

实施要求：

- `MingCustomsRequest` 新增 JSON 字段 `tagId`、`tagNameSnapshot`。
- 新增 `MingCustomsTagCloudItemResponse`，字段为 `tagId`、`tagNameSnapshot`、`count`。
- `MingCustomsInterfaceAssembler.toQuery(...)` 映射 `tagId`、`tagNameSnapshot`。
- `MingCustomsInterfaceAssembler` 新增 `toTagCloudResponse(...)`。
- `MingCustomsAdminController` 新增 `GET /api/classics/ming-customs/tag-cloud`。
- `tag-cloud` controller 从 `KuzhambuContextHolder.currentAuthorities()` 写入 query。

验收点：

- `POST /api/classics/ming-customs/page` 可接收 `tagId` 和 `tagNameSnapshot`。
- `GET /api/classics/ming-customs/tag-cloud` 返回 `tagId/tagNameSnapshot/count`。

### 任务 4：明代习俗后端测试

文件范围：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/mingcustoms/MingCustomsApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/mingcustoms/repository/impl/MingCustomsRepositoryTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/MingCustomsAdminControllerTest.java`

测试要求：

- application 测试覆盖无私有权限时 page 和 tag cloud 都强制排除私有内容。
- infra 测试覆盖 `tagId` 筛选、`tagNameSnapshot` 兜底筛选、`COUNT(DISTINCT content_id)` 聚合。
- interface 测试覆盖 page 请求字段映射和 `tag-cloud` 响应字段。

### 任务 5：王圻和明代习俗 AI 入口控件

文件范围：

- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`

控件要求：

- 王圻详情抽屉 `AI 精修任务` 卡片保留 `单文档问答` 按钮。
- 王圻详情抽屉 `AI 精修任务` 卡片新增三个按钮：
  - `创建摘要任务`
  - `创建标签任务`
  - `创建问答任务`
- 明代习俗详情抽屉 `AI 精修任务` 卡片新增三个按钮：
  - `创建摘要任务`
  - `创建标签任务`
  - `创建问答任务`
- 三个按钮必须有稳定可访问名称，直接使用可见文案即可。

操作要求：

- 点击 `创建摘要任务` 时，`capability = "summary"`。
- 点击 `创建标签任务` 时，`capability = "tags"`。
- 点击 `创建问答任务` 时，`capability = "qa"`。
- loading 只作用于被点击的按钮。
- 创建成功后刷新当前内容的任务列表。
- 创建失败后使用页面现有 message 错误提示。
- 当前用户信息未加载完成时，不发请求，提示“当前用户信息未加载完成，请稍后重试”。

空内容提示：

- 摘要任务：`正文为空，无法创建摘要精修任务`
- 标签任务：`正文为空，无法创建标签精修任务`
- 问答任务：`正文为空，无法创建问答精修任务`

payload 要求：

- 王圻 payload 包含 `title`、`summary`、`content`、`tags`、`qaPairs`、`documentTime`、`storageObjectId`。
- 明代习俗 payload 包含 `title`、`summary`、`content`、`tags`、`qaPairs`、`category`、`chapter`、`section`、`originalExcerpts`。
- `requestId` 前缀按页面和能力区分：`wangqi-summary-request`、`wangqi-tags-request`、`wangqi-qa-request`、`ming-customs-summary-request`、`ming-customs-tags-request`、`ming-customs-qa-request`。
- `traceId` 前缀同理使用 `*-trace`。

### 任务 6：王圻和明代习俗 AI 前端测试

文件范围：

- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-refinement-task-service.test.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`

测试要求：

- service 测试覆盖 `tags`、`qa` 的能力文案和 retryable 判断。
- 王圻页面测试覆盖三个按钮分别发起 `summary`、`tags`、`qa`。
- 明代习俗页面测试覆盖三个按钮分别发起 `summary`、`tags`、`qa`。
- 王圻和明代习俗测试都要断言 `contentType`、`contentId`、`requestId` 前缀。
- 正文为空时，测试断言不会调用创建任务接口。

### 任务 7：明代习俗标签云前端控件

文件范围：

- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-keyword-cloud.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`

控件要求：

- 页面主入口按钮文案改为 `标签云`，图标可继续使用 `TagsOutlined`。
- Drawer `aria-label` 改为 `明代习俗标签云`。
- Drawer 标题改为 `标签云`。
- 空状态文案改为 `暂无标签`。
- 标签 item 文案展示 `tagNameSnapshot`。
- 标签 item 右侧 Badge 展示 `count`。
- 标签 item 可访问名称为 `筛选标签 {tagNameSnapshot}，{count} 条`。
- 当前标签筛选展示为只读 Tag 或等价轻量控件，文本为 `标签：{tagNameSnapshot}`。
- 当前标签筛选旁提供 `清除标签筛选` 按钮。

操作要求：

- `listTagCloud` 请求 `GET /classics/ming-customs/tag-cloud`。
- `listTagCloud` 查询参数包含当前 `visibility`、`category`、`keyword`。
- 点击标签 item 后关闭 Drawer。
- 点击标签 item 后写入页面查询状态 `tagId`、`tagNameSnapshot`。
- 点击标签 item 后清空旧 `tagName`。
- 点击标签 item 后页码重置为第 1 页并刷新列表。
- 点击 `清除标签筛选` 后移除 `tagId`、`tagNameSnapshot`，页码重置为第 1 页并刷新列表。
- 搜索框仍只写入 `keyword`，不得被标签云点击写入。

### 任务 8：明代习俗标签云前端测试

文件范围：

- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-service-contract.test.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`

测试要求：

- service contract 覆盖 `listTagCloud({ visibility, category, keyword })` 的 URL 查询参数。
- 页面测试覆盖点击 `标签云` 打开 Drawer。
- 页面测试覆盖点击标签 item 后 page 请求体包含 `tagId`、`tagNameSnapshot`。
- 页面测试覆盖点击标签 item 后 page 请求体不包含旧 `tagName`。
- 页面测试覆盖点击 `清除标签筛选` 后 page 请求体不包含 `tagId`、`tagNameSnapshot`。

### 任务 9：三才图会多选降级前端

文件范围：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-list.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`

控件和操作要求：

- 表格选择仍只使用当前页 row selection。
- 选中数量提示统一为 `当前页已选 N 条`。
- 批量分享按钮或确认文案必须表达 `当前页已选`。
- 批量公开按钮或确认文案必须表达 `当前页已选`。
- 批量私有按钮或确认文案必须表达 `当前页已选`。
- 批量候选治理按钮或确认文案必须表达 `当前页已选`。
- 批量视觉资产处理按钮或确认文案必须表达 `当前页已选`。
- 导出和静态展示的跨页范围入口只能使用 `筛选结果` 文案，不得写成跨页选中。
- 切换门类、卷、筛选条件、搜索词或分页时允许清空当前页选择，不需要保存跨页选择。

测试要求：

- 测试覆盖页面出现 `当前页已选`。
- 测试覆盖分页或筛选变化后不依赖旧页选中项。

### 任务 10：需求和 coverage 收口

文件范围：

- `docs/10-requirements/CLASSICS-REQUIREMENTS.md`
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-CLASSICS-TAIL-CLOSURE.md`

文档要求：

- `CLASSICS-REQUIREMENTS.md` 将三才图会多选明确为当前页多选。
- `CLASSICS-REQUIREMENTS.md` 写明跨页范围处理由筛选结果导出、筛选结果静态展示或等价范围动作承载。
- `CLASSICS-IMPLEMENTATION-COVERAGE.md` 将王圻 AI 标签/问答入口、明代习俗 AI 标签/问答入口、明代习俗统一标签云和三才多选降级记为已完成。
- `CLASSICS-IMPLEMENTATION-COVERAGE.md` 不保留上述尾项的 `部分完成` 或 `未完成`。
- 最终 PR 收口前删除本 RUNBOOK。

## 统一验证

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-infra,biz/classics/kuzhambu-classics-interface -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-infra,biz/classics/kuzhambu-classics-interface -am test
```

前端：

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format
pnpm run format:check
pnpm run lint
pnpm --filter kuzhambu-admin-web run test -- src/pages/classics/common/ai-refinement-task-service.test.ts src/pages/classics/wangqi/wangqi-page.test.tsx src/pages/classics/ming-customs/ming-customs-page.test.tsx src/pages/classics/ming-customs/ming-customs-service-contract.test.ts src/pages/classics/sancai/sancai-page.test.tsx
pnpm --filter kuzhambu-admin-web run build
```

人工冒烟：

- 无私有查看能力账号打开明代习俗页，点击 `标签云`，确认私有习俗标签不出现；点击标签后列表不出现私有习俗。
- 具备 Classics 管理权限账号打开王圻详情，分别点击 `创建摘要任务`、`创建标签任务`、`创建问答任务`，确认任务列表刷新且候选区可继续应用。
- 具备 Classics 管理权限账号打开明代习俗详情，分别点击 `创建摘要任务`、`创建标签任务`、`创建问答任务`，确认任务列表刷新且候选区可继续应用。
- 打开三才图会列表，选择当前页条目，确认选中数量、批量按钮或确认弹窗只表达 `当前页已选`。
