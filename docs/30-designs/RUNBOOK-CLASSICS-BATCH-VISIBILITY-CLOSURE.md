# Classics 批量可见性闭环 RUNBOOK

## 1. 目标

本 RUNBOOK 用于收口 Classics 三类内容的批量公开/私有状态修改闭环。

本轮目标：

- 将 `POST /api/classics/content/visibility/change` 从占位失败结果改为真实分发到三类内容应用服务。
- 在 `admin-web` 的三才图会、王圻文档、明代习俗页面提供批量公开/批量私有入口。
- 批量操作结果必须展示成功数、失败数和失败原因，单条失败不得影响其他内容。
- 更新 `CLASSICS-IMPLEMENTATION-COVERAGE.md`，将批量公开/私有状态入口从未收口调整为已收口或剩余权限过滤项。

## 2. 已确认决策

- 本轮不做 per-content 细粒度权限过滤，只复用 `@HasPermission("classics:content:edit")` 管理端权限边界。
- 本轮不重算历史分享快照；`classics_share_target.content_visibility_snapshot` 继续表示创建分享时的内容可见性快照。
- 本轮不支持跨页选择或按筛选条件全量批量操作，只处理当前页面已选 ID。
- 本轮保留统一入口 `POST /api/classics/content/visibility/change`，不在三类内容 controller 中各自新增批量可见性接口。
- 本轮完成后覆盖状态应表达为：批量公开/私有状态完整入口已完成，细粒度权限过滤仍保留为剩余项。

## 3. 非目标

- 不新增数据库表。
- 不变更 `sancai_entry.visibility`、`wangqi_document.visibility`、`ming_customs.visibility` 字段结构。
- 不实现跨用户细粒度内容权限过滤；本轮只复用现有 `@HasPermission("classics:content:edit")` 管理权限边界。
- 不实现内容删除后的分享目标状态同步、分享风险态重算。
- 不实现跨页选择或全量筛选结果批量操作；本轮只处理当前页面已选记录。

## 4. 数据结构变更

本轮不新增数据库字段，不修改现有 schema。

数据库字段影响精确如下：

| 表 | 字段 | 类型 | 本轮是否改结构 | 本轮运行时行为 |
| --- | --- | --- | --- | --- |
| `sancai_entry` | `visibility` | `varchar(16)` | 否 | 批量更新为 `PUBLIC` 或 `PRIVATE` |
| `wangqi_document` | `visibility` | `varchar(16)` | 否 | 批量更新为 `PUBLIC` 或 `PRIVATE` |
| `ming_customs` | `visibility` | `varchar(16)` | 否 | 批量更新为 `PUBLIC` 或 `PRIVATE` |
| `classics_share_target` | `content_visibility_snapshot` | `varchar(16)` | 否 | 不更新历史分享快照 |

本轮复用并稳定以下 HTTP 数据结构。

### 4.1 后端请求结构

文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsBatchVisibilityRequest.java`

字段：

| 字段 | 类型 | 必填 | 取值 | 说明 |
| --- | --- | --- | --- | --- |
| `contentType` | `String` | 是 | `SANCAI_ENTRY` / `WANGQI_DOCUMENT` / `MING_CUSTOMS` | 目标内容类型 |
| `contentIds` | `List<Long>` | 是 | 非空、去重后处理 | 目标内容数据库主键 |
| `visibility` | `String` | 是 | `PUBLIC` / `PRIVATE` | 目标可见性 |

### 4.2 后端响应结构

文件：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/common/response/ClassicsBatchOperationResponse.java`

字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `successCount` | `Integer` | 成功条数 |
| `failureCount` | `Integer` | 失败条数 |
| `successes` | `List<Item>` | 成功明细 |
| `failures` | `List<Item>` | 失败明细 |

`Item` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `contentType` | `String` | 内容类型 |
| `contentId` | `Long` | 内容 ID |
| `resultId` | `Long` | 成功结果 ID，本轮等于内容 ID |
| `status` | `String` | 成功后的可见性或失败状态 |
| `failureCode` | `String` | 失败代码 |
| `failureReason` | `String` | 可读失败原因 |

### 4.3 前端请求与响应类型

文件：`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-types.ts`

新增或复用类型字段：

| 类型 | 字段 | TypeScript 类型 | 说明 |
| --- | --- | --- | --- |
| `ClassicsBatchVisibilityPayload` | `contentType` | `ClassicsContentType` | 目标内容类型 |
| `ClassicsBatchVisibilityPayload` | `contentIds` | `number[]` | 当前页面已选内容 ID |
| `ClassicsBatchVisibilityPayload` | `visibility` | `"PUBLIC" \| "PRIVATE"` | 目标可见性 |
| `ClassicsBatchOperationRecord` | `successCount` | `number` | 成功条数 |
| `ClassicsBatchOperationRecord` | `failureCount` | `number` | 失败条数 |
| `ClassicsBatchOperationRecord` | `successes` | `ClassicsBatchOperationItemRecord[]` | 成功明细 |
| `ClassicsBatchOperationRecord` | `failures` | `ClassicsBatchOperationItemRecord[]` | 失败明细 |
| `ClassicsBatchOperationItemRecord` | `contentType` | `ClassicsContentType \| string \| null` | 内容类型 |
| `ClassicsBatchOperationItemRecord` | `contentId` | `number \| null` | 内容 ID |
| `ClassicsBatchOperationItemRecord` | `resultId` | `number \| null` | 成功结果 ID |
| `ClassicsBatchOperationItemRecord` | `status` | `string \| null` | 可见性或失败状态 |
| `ClassicsBatchOperationItemRecord` | `failureCode` | `string \| null` | 失败代码 |
| `ClassicsBatchOperationItemRecord` | `failureReason` | `string \| null` | 可读失败原因 |

文件：`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service.ts`

新增 service：

| 方法 | 入参类型 | 返回类型 | 路径 |
| --- | --- | --- | --- |
| `changeVisibilityBatch` | `ClassicsBatchVisibilityCommand` | `Promise<ClassicsBatchOperationRecord>` | `/classics/content/visibility/change` |

### 4.4 运行时字段影响

本轮只修改既有业务字段的值：

- `sancai_entry.visibility`: `PUBLIC` 或 `PRIVATE`
- `wangqi_document.visibility`: `PUBLIC` 或 `PRIVATE`
- `ming_customs.visibility`: `PUBLIC` 或 `PRIVATE`

变更必须复用现有单条可见性修改语义，并继续触发对应搜索索引同步逻辑。

## 5. 相关文件清单

### 5.1 Java servers

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/request/ClassicsBatchVisibilityRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/common/response/ClassicsBatchOperationResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/WangqiDocumentApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/MingCustomsApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`

说明：三类 application service 已存在批量方法；本轮优先不改 application 方法签名，除非验证发现签名无法满足 interface 分发。

### 5.2 Admin Web

- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service-contract.test.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-list.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`

### 5.3 文档

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/RUNBOOK-CLASSICS-BATCH-VISIBILITY-CLOSURE.md`
- `TODO.md`

## 6. 执行任务拆分

### 任务 1：Java interface 分发批量可见性

范围文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`

处理动作：

- 为 `ClassicsContentAdminController#changeBatchVisibility` 注入三类内容 application service。
- 按 `contentType` 分发：
  - `SANCAI_ENTRY` -> `SancaiApplicationService#batchChangeEntryVisibility`
  - `WANGQI_DOCUMENT` -> `WangqiDocumentApplicationService#batchChangeVisibility`
  - `MING_CUSTOMS` -> `MingCustomsApplicationService#batchChangeVisibility`
- 保留 `contentIds` 非空和去重校验。
- 保留 `visibility` 仅允许 `PUBLIC` / `PRIVATE`。
- 删除 `BATCH_VISIBILITY_NOT_IMPLEMENTED` 占位结果。
- 不修改 `ClassicsBatchVisibilityRequest` 和 `ClassicsBatchOperationResponse`，除非测试证明字段契约不满足本 RUNBOOK。

验收点：

- controller test 覆盖三类 content type 的分发。
- unsupported `contentType`、unsupported `visibility`、重复 `contentIds` 仍失败。
- `ClassicsInterfaceArchitectureTest` 仍通过。

### 任务 2：Admin Web 公共批量可见性服务契约

范围文件：

- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service-contract.test.ts`

处理动作：

- 新增 `changeVisibilityBatch` service 方法。
- 请求路径固定为 `/classics/content/visibility/change`。
- 请求字段固定为 `contentType`、`contentIds`、`visibility`。
- 响应复用批量操作结果字段：`successCount`、`failureCount`、`successes`、`failures`。

验收点：

- service contract test 断言请求 method、path、body。
- contract test 断言成功/失败明细字段透传。

### 任务 3：三才图会批量可见性入口

范围文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-list.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`

处理动作：

- 在三才条目多选工具区新增批量公开、批量私有动作。
- 操作 contentType 固定为 `SANCAI_ENTRY`。
- 操作成功后刷新条目列表，并展示成功数、失败数和失败原因。

验收点：

- 页面测试覆盖选中条目后发起批量公开或批量私有。
- 页面测试覆盖失败明细展示。

### 任务 4：王圻文档批量可见性入口

范围文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`

处理动作：

- 在王圻文档多选工具区新增批量公开、批量私有动作。
- 操作 contentType 固定为 `WANGQI_DOCUMENT`。
- 操作成功后刷新文档列表，并展示成功数、失败数和失败原因。

验收点：

- 页面测试覆盖选中文档后发起批量公开或批量私有。
- 页面测试覆盖失败明细展示。

### 任务 5：明代习俗批量可见性入口

范围文件：

- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`

处理动作：

- 在明代习俗多选工具区新增批量公开、批量私有动作。
- 操作 contentType 固定为 `MING_CUSTOMS`。
- 操作成功后刷新习俗列表，并展示成功数、失败数和失败原因。

验收点：

- 页面测试覆盖选中习俗后发起批量公开或批量私有。
- 页面测试覆盖失败明细展示。

### 任务 6：覆盖矩阵与 RUNBOOK 收口

范围文件：

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `TODO.md`
- `docs/30-designs/RUNBOOK-CLASSICS-BATCH-VISIBILITY-CLOSURE.md`

处理动作：

- 更新批量公开/私有状态相关覆盖状态。
- 将已完成 TODO 删除或收窄。
- PR 前删除本 RUNBOOK，除非仍有未完成任务需要保留。

验收点：

- `CLASSICS-IMPLEMENTATION-COVERAGE.md` 不再把“批量公开/私有状态完整入口”描述为未收口。
- `TODO.md` 只保留未完成任务。
- 阶段任务关闭前本 RUNBOOK 已删除。

## 7. 推荐提交顺序

1. `Feat(classics): 接通批量可见性接口分发`
2. `Feat(admin-web): 新增批量可见性契约`
3. `Feat(admin-web): 接入三才批量可见性`
4. `Feat(admin-web): 接入王圻批量可见性`
5. `Feat(admin-web): 接入明代习俗批量可见性`
6. `Docs(classics): 更新批量可见性覆盖状态`
7. `Docs(runbook): 清理批量可见性执行计划`

实际提交可按测试修复需要增加 `Test(...)` 或 `Fix(...)` 小步提交，但每个提交必须表达一个明确工程判断。

## 8. 验证计划

单任务完成后运行最小相关验证，不在每个小任务后跑全量 test。

Java servers 建议：

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-interface -am spotless:apply
mvn -pl biz/classics/kuzhambu-classics-interface -am spotless:check
mvn -pl biz/classics/kuzhambu-classics-interface -am checkstyle:check
mvn -pl biz/classics/kuzhambu-classics-interface -am -DskipTests compile
```

Admin Web 建议：

```sh
cd kuzhambu-apps/admin-web
npx prettier --write <touched-files>
cd ..
npm --workspace admin-web run format:check
npm --workspace admin-web run lint
npm --workspace admin-web run build
```

2-5 个任务完成后统一运行相关测试：

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-interface -am -Dtest=ClassicsContentAdminControllerTest,ClassicsInterfaceArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false test

cd ../kuzhambu-apps
npm --workspace admin-web exec vitest run \
  src/pages/classics/common/classics-content-service-contract.test.ts \
  src/pages/classics/sancai/components/sancai-entry-panel.test.tsx \
  src/pages/classics/wangqi/wangqi-page.test.tsx \
  src/pages/classics/ming-customs/ming-customs-page.test.tsx
```

PR 前全量验证：

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn -DskipTests compile
mvn test

cd ../kuzhambu-apps
npm run format:check
npm run lint
npm run build
npm run test
```

## 9. 风险与决策点

- 当前仅复用 `@HasPermission("classics:content:edit")`，不做 per-content 权限过滤。如果要把“权限过滤”也作为本轮关闭项，需要先设计 System 权限编码与 Classics 内容归属模型。
- 批量设为 `PRIVATE` 时，现有单条可见性修改应继续触发 Discovery 删除/同步语义；本轮必须通过 application service 复用该行为，不直接更新 repository 字段。
- 批量设为 `PUBLIC` 时，现有单条可见性修改应继续触发 Discovery 索引同步语义。
- 分享链接中已存在的 `contentVisibilitySnapshot` 是创建分享时快照；本轮不重算历史分享快照，也不自动撤销分享。
- 如果页面现有工具区空间不足，批量公开/私有应作为次级操作，不应挤占页面唯一主操作。
