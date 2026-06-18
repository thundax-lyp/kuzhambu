# RUNBOOK Classics Sancai Admin Loop

## 目标

在 Admin Web 中交付三才图会最小可用后台闭环：后台用户可以进入三才图会页面，按门类和卷查看条目，搜索和分页列表，打开详情，编辑标题、原文、译文、摘要、生命周期状态、公开状态并保存。

本轮只做三才图会后台内容管理的最小闭环，不做 AI 候选、Worker 导出、静态展示、复杂视觉资产生产、标签治理或分享能力。

## 固定边界

- 后端接口定义为真相源，Admin Web 向现有后端接口对齐。
- 若后端 application 已具备能力但 interface 未暴露，补齐接口契约和 controller。
- 前端页面不使用业务占位页；新增页面必须能调用后端契约。
- 本轮不修改 SQL；如后续必须修改 SQL，必须同步 dev 数据库。
- 测试环境使用仓库根目录 `dev.env`。
- Redis 不作为本轮必要验证依赖；如后续启动 Redis，必须关闭持久化。
- PR 收口前必须更新 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`，并删除本 RUNBOOK。

## 执行任务

### 1. 固定三才图会后台接口契约

范围文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiCategoryResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiVolumeResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAdminControllerTest.java`

步骤：

1. 为 `SancaiAdminController` 增加 `POST /api/classics/sancai/categories/list`。
2. 为 `SancaiAdminController` 增加 `POST /api/classics/sancai/volumes/list`，请求体复用只包含 `categoryId` 的请求模型或新建精确请求模型。
3. 为门类和卷增加 response，字段只包含 `id`、`categoryId`、`title`、`categoryType`、`volumeType`、`priority` 中对应可用项。
4. 在 `SancaiInterfaceAssembler` 中补齐 category、volume 到 response 的转换。
5. 在 controller contract test 中固定 categories、volumes、entries/page、entries/{id}、entries/save 的路径、请求字段和响应字段。

验收：

- Classics interface 测试覆盖成功路径和至少一个失败/空请求路径。
- `mvn -pl biz/classics/kuzhambu-classics-interface -am test` 通过。

### 2. 补齐 Admin Web Classics 服务契约

范围文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`

步骤：

1. 新增三才图会前端类型，覆盖 category、volume、entry、page query、save command。
2. 新增 service 方法：`listSancaiCategories`、`listSancaiVolumes`、`pageSancaiEntries`、`getSancaiEntry`、`saveSancaiEntry`。
3. 服务路径必须与后端 controller 契约一致。
4. 增加 contract test，固定请求路径、请求体字段和保存字段。

验收：

- 前端 service contract test 能证明 Admin Web 已向后端接口对齐。
- `npm test` 通过。

### 3. 接入 Classics 路由与菜单导航验证

范围文件：

- `kuzhambu-apps/admin-web/src/router/index.tsx`
- `kuzhambu-apps/admin-web/src/app.test.tsx`
- `kuzhambu-apps/admin-web/e2e/layout/admin-layout.spec.ts`

步骤：

1. 在路由中新增 `/classics/sancai`，绑定真实 `SancaiPage`。
2. 更新已实现菜单导航测试，加入 `classics/sancai` 菜单数据。
3. 更新 Playwright layout 测试，确认点击三才图会菜单后不是空白内容。

验收：

- `npm test` 覆盖 Classics 菜单路由。
- `npm run e2e` 中 layout 导航用例覆盖三才图会入口。

### 4. 实现三才图会列表页

范围文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
- `kuzhambu-apps/admin-web/e2e/classics/sancai/sancai.spec.ts`

步骤：

1. 页面加载时请求门类列表，选中第一个门类后请求卷列表。
2. 支持选择门类、选择卷、关键词搜索、状态筛选和分页请求条目列表。
3. 列表展示标题、门类/卷上下文、生命周期状态、公开状态、翻译状态、配图状态、视觉资产状态和完善状态。
4. Playwright mock 后端 categories、volumes、entries/page，验证筛选和分页请求体。

验收：

- 用户进入 `/classics/sancai` 后能看到门类、卷和条目列表。
- 页面筛选操作能发送后端契约要求的请求体。

### 5. 实现条目详情与编辑保存

范围文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`
- `kuzhambu-apps/admin-web/e2e/classics/sancai/sancai.spec.ts`

步骤：

1. 点击条目行或查看按钮时调用 `entries/{id}` 获取详情。
2. 在抽屉或弹窗中展示并编辑标题、原文、译文、摘要、生命周期状态、公开状态。
3. 保存时调用 `entries/save`，请求体包含 `id`、`volumeId`、`title`、`originalText`、`translationText`、`summary`、`lifecycleStatus`、`visibility`。
4. 保存成功后刷新当前列表并保持当前筛选条件。
5. Playwright 验证打开详情、编辑保存、刷新列表。

验收：

- 页面闭环覆盖列表进入详情、编辑保存、列表刷新。
- 保存请求体与后端 `SancaiEntrySaveRequest` 对齐。

### 6. 更新 Classics 覆盖报告并收口

范围文件：

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `TODO.md`
- `docs/30-designs/RUNBOOK-CLASSICS-SANCAI-ADMIN-LOOP.md`

步骤：

1. 将三才图会“门类和卷浏览”“条目查看、创建、编辑、删除”“分页、筛选、当前卷搜索和多选”等相关行更新到本轮真实状态。
2. 在未完成部分保留未做范围：AI、Worker、导出、分享、复杂视觉资产、标签治理、多选批量。
3. 删除已完成 TODO。
4. 删除本临时 RUNBOOK。

验收：

- Coverage 文档能准确反映三才图会 Admin 最小闭环完成状态。
- `TODO.md` 清空或只保留后续未完成任务。
- PR 中不保留已完成的临时 RUNBOOK。

## 验证命令

后端验证：

```sh
cd kuzhambu-servers
set -a
source ../dev.env
set +a
mvn -pl biz/classics/kuzhambu-classics-interface -am spotless:apply
mvn -pl biz/classics/kuzhambu-classics-interface -am checkstyle:check
mvn -pl biz/classics/kuzhambu-classics-interface -am test
```

前端验证：

```sh
cd kuzhambu-apps/admin-web
npm run format:check
npm run lint
npm test
npm run e2e
```

PR 前验证：

```sh
git diff --check
```
