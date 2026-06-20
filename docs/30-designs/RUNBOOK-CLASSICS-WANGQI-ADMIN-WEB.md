# RUNBOOK Classics Wangqi Admin Web

## 目标

本分支目标是完成王圻文档 Admin Web 最小管理闭环，让后台用户能从菜单进入 `/classics/wangqi` 页面，并完成文档列表、筛选、时间线视图、详情阅读、新增、编辑、删除和可见性维护。

本 RUNBOOK 是执行手册，不是长期设计文档。任务完成并合并前应删除本文件，并把稳定完成状态沉淀到 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`。

## 当前基线

已存在后端表：

- `db/schema/classics.sql`
    - `classics_wangqi_document`
        - `id bigint`
        - `title varchar(255)`
        - `summary text`
        - `content_format varchar(16)`
        - `content longtext`
        - `document_time datetime(3)`
        - `storage_object_id bigint`
        - `visibility varchar(16)`
        - `current_version_id bigint`
        - `current_version_no int`
        - `current_versioned_at datetime(3)`
        - `content_updated_at datetime(3)`

已存在后端 Admin API：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/controller/WangqiDocumentAdminController.java`
    - `POST /api/classics/wangqi/documents/page`
    - `GET /api/classics/wangqi/documents/{id}`
    - `POST /api/classics/wangqi/documents/timeline/list`
    - `POST /api/classics/wangqi/documents/add`
    - `POST /api/classics/wangqi/documents/update`
    - `POST /api/classics/wangqi/documents/delete`

已存在菜单和权限 seed：

- `db/data/system.sql`
    - 菜单 `王圻文档`，URL `/classics/wangqi`
    - 权限 `classics:wangqi:view`
    - 权限 `classics:wangqi:edit`
    - 权限 `classics:wangqi:delete`
- `db/data-source/system.json`
    - 同步维护菜单和权限来源数据。

当前缺口：

- `kuzhambu-apps/admin-web/src/router/index.tsx` 尚未注册 `/classics/wangqi` 页面组件。
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/` 尚不存在。
- `db/data/classics.sql` 没有王圻文档初始化数据，dev.env 页面冒烟可能没有可浏览记录。
- `scripts/verify-classics.sh` 只校验三才图会初始化数据，没有校验王圻文档初始化数据。
- `WangqiDocumentAdminControllerTest` 仅校验 controller 类型存在，缺少请求契约烟测。

## 本轮范围

### 后端范围

本轮优先不新增 schema 字段，不修改 `classics_wangqi_document` 表结构。

需要发生的数据变更：

- 在 `db/data/classics.sql` 增加最少 2 条 `classics_wangqi_document` 初始化数据：
    - 1 条 `PUBLIC`
    - 1 条 `PRIVATE`
    - 覆盖 `MARKDOWN` 和 `HTML` 两类 `content_format`
    - `content_updated_at` 必须显式赋值
    - `storage_object_id` 可以为 `NULL`

需要发生的测试/校验变更：

- 在 `scripts/verify-classics.sh` 增加 `classics_wangqi_document` 初始化数据存在性校验。
- 补强 `WangqiDocumentAdminControllerTest`，覆盖 page/get/timeline/add/update/delete 的请求路径和基础请求体。

不计划发生的后端变更：

- 不新增 Wangqi 分类字典。王圻文档当前没有分类字段。
- 不新增标签、问答对、AI 候选确认接口。
- 不新增 Storage 文件上传或替换接口。
- 不新增版本历史、版本对比、历史恢复接口。
- 不新增批量公开/私有修改接口。

### 前端范围

新增页面目录：

```text
kuzhambu-apps/admin-web/src/pages/classics/wangqi/
  wangqi-page.tsx
  wangqi-page.css
  wangqi-page.test.tsx
  wangqi-service.ts
  wangqi-service-contract.test.ts
  wangqi-types.ts
  components/
    wangqi-document-form-values.ts
    wangqi-document-list.tsx
    wangqi-document-model.tsx
    wangqi-timeline-panel.tsx
```

修改现有文件：

- `kuzhambu-apps/admin-web/src/router/index.tsx`
    - import `WangqiPage`
    - 注册 route：`path: "classics/wangqi"`

可复用现有共享组件：

- `kuzhambu-apps/admin-web/src/components/kuzhambu-list-page/`
- `kuzhambu-apps/admin-web/src/components/kuzhambu-drawer/`
- `kuzhambu-apps/admin-web/src/components/kuzhambu-confirm-modal/`
- `kuzhambu-apps/admin-web/src/components/kuzhambu-rich-content-viewer/`
- `kuzhambu-apps/admin-web/src/components/kuzhambu-table/`
- `kuzhambu-apps/admin-web/src/components/kuzhambu-tag/`

不计划新增共享组件。若实现时发现 Wangqi 和 MingCustoms 有明显可抽取代码，只记录为后续重构，不在本轮抽象。

## 数据结构设计

### 后端 API 请求字段

对应 `WangqiDocumentRequest`：

```ts
interface WangqiDocumentRequest {
    id?: number | null;
    title?: string | null;
    summary?: string | null;
    contentFormat?: "MARKDOWN" | "HTML" | string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: number | null;
    visibility?: "PUBLIC" | "PRIVATE" | string | null;
    keyword?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
    pageNo?: number;
    pageSize?: number;
}
```

### 后端 API 响应字段

对应 `WangqiDocumentResponse`：

```ts
interface WangqiDocumentResponse {
    id: number;
    title?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: number | null;
    visibility?: string | null;
}
```

本轮不要求响应返回 `currentVersionId`、`currentVersionNo`、`currentVersionedAt` 或 `contentUpdatedAt`。如果实现页面时需要展示“正式版本/污染状态”，必须先补充后端 response、接口测试和本文档，再实现前端展示。

### 前端领域类型

新增 `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-types.ts`：

```ts
export type WangqiContentFormat = "MARKDOWN" | "HTML";
export type WangqiDocumentVisibility = "PUBLIC" | "PRIVATE";

export interface WangqiDocumentRecord {
    id: number;
    title?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: number | null;
    visibility?: string | null;
}
```

新增 `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-service.ts`：

```ts
export type WangqiDocumentQuery = PageQuery<{
    keyword?: string | null;
    visibility?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
}>;

export interface WangqiDocumentCommand {
    id?: number | null;
    title?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: number | null;
    visibility?: string | null;
}
```

Service 方法固定为：

- `page(request?: WangqiDocumentQuery)`
- `get(id: number)`
- `listTimeline(request?: WangqiDocumentQuery)`
- `add(request: WangqiDocumentCommand)`
- `update(request: WangqiDocumentCommand)`
- `deleteById(id: number)`

### 前端表单字段

`wangqi-document-form-values.ts` 固定转换以下字段：

- `id`
- `title`
- `summary`
- `contentFormat`
- `content`
- `documentTime`
- `storageObjectId`
- `visibility`

时间字段规则：

- UI 使用 Ant Design `DatePicker`。
- 表单内部可以使用 `dayjs`。
- 提交给后端时转换为 ISO 字符串或后端可解析的日期字符串。
- 从后端加载时按 `documentTime` 反填。

Storage 字段规则：

- 本轮不接 Storage 文件选择器。
- `storageObjectId` 作为可选数字输入或只读展示字段实现。
- 文案需明确这是“原始文件对象 ID”，不暗示已完成上传/替换闭环。

## 页面行为设计

### 路由和菜单

- 浏览器地址：`/classics/wangqi`
- 后台菜单 seed 已存在；前端只补 route。
- 权限由后端菜单和 current-user perms 控制；页面内部仍按按钮语义展示，不新增前端权限系统。

### 列表页

列表筛选：

- `keyword`
    - 搜索标题、摘要和正文。
    - placeholder：`搜索王圻文档标题、摘要或正文`
- `visibility`
    - `全部`
    - `公开`
    - `私有`
- `sortDirection`
    - 默认 `DESC`
    - 可切换 `DESC` / `ASC`，按 `documentTime` 时间线顺序理解。

列表列：

- 标题
- 摘要预览
- 文档时间
- 原始文件对象 ID
- 可见性
- 操作

操作：

- `查看/编辑`
- `删除`

### 时间线面板

`wangqi-timeline-panel.tsx` 读取 `listTimeline`：

- 使用当前 `keyword`、`visibility`、`sortDirection`。
- 展示标题、文档时间、摘要预览。
- 点击时间线项打开详情抽屉。
- 时间线不是独立 route。

### 详情和编辑抽屉

`wangqi-document-model.tsx` 使用 `KuzhambuDrawer`。

表单字段：

- 标题：必填。
- 摘要：多行文本。
- 正文格式：`MARKDOWN` / `HTML`。
- 正文：多行文本。
- 文档时间：日期时间选择。
- 原始文件对象 ID：可选数字。
- 可见性：公开/私有。

预览区域：

- 使用 `KuzhambuRichContentViewer` 展示正文。
- `MARKDOWN` 和 `HTML` 都必须走同一个清洗展示控件。

保存规则：

- 无 `id` 调用 `add`。
- 有 `id` 调用 `update`。
- 成功后刷新 page 和 timeline query。

删除规则：

- 使用 `useKuzhambuConfirm` 二次确认。
- 调用 `deleteById(id)`。
- 成功后刷新 page 和 timeline query。

## 分步实施计划

每次提交控制在 2-5 个文件。若某一步实现中出现必须跨越更多文件的情况，应先拆分为更小的提交或更新本 RUNBOOK。

### Step 1：补齐王圻初始化数据

文件：

- `db/data/classics.sql`
- `scripts/verify-classics.sh`

动作：

- 在 `classics_wangqi_document` 增加 2 条 seed。
- 在 `verify-classics.sh` 增加 Wangqi seed 校验。
- 若 dev.env 已存在 schema，但没有 seed，需要同步执行对应 insert。不得依赖页面手工新增作为长期数据来源。

验证：

- `scripts/verify-classics.sh`

### Step 2：补强后端 Controller 冒烟测试

文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/WangqiDocumentAdminControllerTest.java`
- 如测试需要 fixture，可只在同文件内定义。

动作：

- 覆盖 page/get/timeline/add/update/delete 基础调用。
- 固定 API path 和 request body 关键字段。

验证：

- `cd kuzhambu-servers`
- `mvn -pl biz/classics/kuzhambu-classics-interface -am spotless:check checkstyle:check test`

### Step 3：新增前端 service 和类型契约

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-service-contract.test.ts`

动作：

- 定义 `WangqiDocumentRecord`。
- 定义 `WangqiDocumentQuery` 和 `WangqiDocumentCommand`。
- 实现 page/get/listTimeline/add/update/deleteById。
- 测试固定所有 API path 和 request body。

验证：

- `cd kuzhambu-apps`
- `npm run format:check`
- `npm run lint`
- `npm run test -- --run wangqi-service-contract`

### Step 4：实现表单值转换和私有组件

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-document-form-values.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-document-list.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-document-model.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-timeline-panel.tsx`

动作：

- 实现列表、抽屉表单、正文预览和时间线面板。
- 使用 `KuzhambuRichContentViewer` 展示正文。
- 所有业务交互控件必须有稳定可访问名称。

验证：

- `cd kuzhambu-apps`
- `npm run format:check`
- `npm run lint`
- `npm run test -- --run wangqi`

### Step 5：注册页面和路由

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.css`
- `kuzhambu-apps/admin-web/src/router/index.tsx`

动作：

- 组装 query、mutations、page list、timeline 和 drawer。
- 注册 `/classics/wangqi` route。
- 页面根 class 使用 `wangqi-page` 前缀。
- 风格延续 Admin Web 古风口径，避免新增独立视觉体系。

验证：

- `cd kuzhambu-apps`
- `npm run format:check`
- `npm run lint`
- `npm run test`
- `npm run build`

### Step 6：补页面单测和 E2E

文件：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`
- `kuzhambu-apps/admin-web/e2e/classics/wangqi/wangqi.spec.ts`

动作：

- 单测覆盖列表加载、筛选请求、打开详情、保存新增、保存更新、删除确认。
- E2E mock current-user menus/perms，并覆盖菜单进入、筛选、时间线、富文本清洗预览、保存和删除请求。

验证：

- `cd kuzhambu-apps`
- `npm run test`
- `npm --workspace admin-web run e2e -- e2e/classics/wangqi/wangqi.spec.ts`

### Step 7：dev.env 冒烟

前置：

- 使用 Java 17。
- 启动前加载 repo root `dev.env`。
- 若 Step 1 增加了 seed，需要同步到 dev.env 数据库。

动作：

1. 安装 starter 依赖：

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn -pl starter/kuzhambu-admin-starter -am -DskipTests install
```

2. 启动 admin starter：

```sh
set -a
source ../../../dev.env
set +a
cd kuzhambu-servers/starter/kuzhambu-admin-starter
mvn spring-boot:run
```

3. 使用 `docs/00-governance/HOW-TO-ADMIN-LOGIN-SMOKE.md` 登录。

4. 冒烟 API：

- `POST /admin-api/api/classics/wangqi/documents/page`
- `POST /admin-api/api/classics/wangqi/documents/timeline/list`
- `GET /admin-api/api/classics/wangqi/documents/{id}`

验收：

- page 返回 `COMMON-00000`。
- records 至少包含 seed 数据。
- timeline 返回 `COMMON-00000`。
- detail 返回正文和可见性字段。

### Step 8：文档和现场收口

文件：

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `TODO.md`
- `docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`

动作：

- 更新 Wangqi 覆盖状态。
- 删除完成 TODO。
- 删除本 RUNBOOK。
- 清理本地服务、临时日志、Playwright 产物和 dev.env 临时测试数据。

验证：

- `git status --short` 只显示预期收口文件，最终应为空。
- `lsof -iTCP:20010 -sTCP:LISTEN -n -P || true` 无残留 admin starter。
- `lsof -iTCP:5173 -sTCP:LISTEN -n -P || true` 无残留前端 dev server。

## 完整验证清单

后端：

- `cd kuzhambu-servers && mvn spotless:check`
- `cd kuzhambu-servers && mvn checkstyle:check`
- `cd kuzhambu-servers && mvn test`

前端：

- `cd kuzhambu-apps && npm run format:check`
- `cd kuzhambu-apps && npm run lint`
- `cd kuzhambu-apps && npm run test`
- `cd kuzhambu-apps && npm run build`
- `cd kuzhambu-apps && npm --workspace admin-web run e2e -- e2e/classics/wangqi/wangqi.spec.ts`

数据：

- `scripts/verify-classics.sh`
- dev.env 数据库存在 Wangqi seed。

冒烟：

- dev.env 登录成功。
- Wangqi page/timeline/detail API 返回成功。

## 风险和边界

- 王圻原始文件关联本轮只处理 `storageObjectId` 字段，不实现文件选择、上传、替换和下载。
- 王圻标签、问答对和 AI 候选确认仍属于后续 Classics/Knowledge/AI 协作任务。
- 王圻版本历史、版本对比和历史恢复仍属于后续 Versionable 展示和恢复任务。
- 私有文档权限过滤依赖 System 权限接入，本轮只按现有接口和 seed 权限完成后台管理闭环。
- 如果实现过程中发现后端 response 缺少页面必须展示的字段，应先补充接口契约、测试和本 RUNBOOK，再继续前端实现。
