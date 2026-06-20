# RUNBOOK Classics MingCustoms Admin Web

## 1. 目标

在分支 `feature/classics-mingcustoms-admin-web` 上完成明代习俗知识库 Admin Web 最小闭环。

本轮闭环定义：

- 管理员能从左侧菜单进入 `/classics/ming-customs`。
- 管理员能分页浏览明代习俗条目。
- 管理员能按关键词、分类、可见性筛选。
- 管理员能打开详情抽屉查看标题、概述、正文、分类、章节、节、原文摘录和可见性。
- 管理员能新增、编辑和删除习俗条目。
- 管理员能查看按出现次数体现强弱的关键词云，并点击关键词筛选列表。
- 管理员能从单条习俗创建公开分享链接。

## 2. 非目标

以下能力不进入本轮：

- 批量修改公开或私有状态。
- 摘要、标签、问答对内联维护。
- AI 摘要、标签、问答候选确认。
- 版本历史、版本对比和历史恢复。
- 导出产物生成、下载、删除和过期清理。
- 后端新增数据库表或字段。

注意：本轮会新增系统字典和明代习俗样例初始化数据，但不新增表结构。

## 3. 已有基础

### 后端接口

已有 Controller：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/MingCustomsAdminController.java`

已有接口：

- `POST /api/classics/ming-customs/page`
- `GET /api/classics/ming-customs/{id}`
- `POST /api/classics/ming-customs/add`
- `POST /api/classics/ming-customs/update`
- `POST /api/classics/ming-customs/delete`
- `POST /api/classics/ming-customs/keywords/add`
- `POST /api/classics/ming-customs/keywords/sort`
- `GET /api/classics/ming-customs/keyword-cloud`

已有请求和响应：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/request/MingCustomsRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/request/MingCustomsKeywordSortRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsResponse.java`

可用字段：

- `id`
- `title`
- `category`
- `chapter`
- `section`
- `summary`
- `contentFormat`
- `content`
- `originalExcerpts`
- `visibility`
- `keyword`
- `tagName`
- `sortDirection`

需要调整的后端契约：

- 当前 `keyword-cloud` 返回 `List<String>`，只能支撑关键词列表。
- 需求要求“标签云必须按标签使用频率体现强弱”，因此本轮必须改为 `List<MingCustomsKeywordCloudItemResponse>`。
- 返回字段固定为 `keyword` 和 `count`。
- 前端根据 `count` 计算展示强弱，不让后端绑定字号、颜色等 UI 规则。

### 前端基础

已有可参考页面：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-list.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`

已有分享客户端：

- `kuzhambu-apps/admin-web/src/api/classics/share-service.ts`
- `kuzhambu-apps/admin-web/src/service/classics-share-types.ts`

`ClassicsShareContentType` 已包含 `MING_CUSTOMS`。

已有路由入口：

- `kuzhambu-apps/admin-web/src/router/index.tsx`

已有菜单 seed：

- `db/data/system.sql`
- `db/data-source/system.json`

菜单 URL 已为 `/classics/ming-customs`。

需要新增的系统字典：

- 字典类型：`CLASSICS_MING_CUSTOMS_CATEGORY`
- 用途：明代习俗分类筛选和编辑表单候选项。
- 存储方式：`classics_ming_customs_entry.category` 继续存字典 `value`；前端用字典 `label` 展示。
- 初始化文件：`db/data/system.sql`。
- 数据源文件：同步 `db/data-source/system.json` 中的字典源数据，并扩展 `scripts/generate-system-data-sql.ts` 支持生成 `system_dict`。
- dev.env：实现时必须把新增字典项同步写入 dev.env 指向的 `system_dict` 表。

## 4. 实现步骤

### Step 1：后端 keyword-cloud 契约改造

范围文件：

- 修改 `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/mingcustoms/repository/MingCustomsRepository.java`
- 新增 `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/mingcustoms/model/valueobject/MingCustomsKeywordCloudItem.java`
- 修改 `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/MingCustomsApplicationService.java`
- 修改 `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`
- 修改 `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/mingcustoms/repository/impl/MingCustomsRepositoryImpl.java`
- 新增 `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/response/MingCustomsKeywordCloudItemResponse.java`
- 修改 `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/assembler/MingCustomsInterfaceAssembler.java`
- 修改 `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/controller/MingCustomsAdminController.java`
- 修改 `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/MingCustomsAdminControllerTest.java`
- 新增或修改 infra 测试：`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/mingcustoms/MingCustomsRepositoryTest.java`

处理动作：

- `MingCustomsKeywordCloudItem` 字段固定为 `String keyword`、`Long count`。
- `MingCustomsRepository#listKeywordCloud` 返回 `List<MingCustomsKeywordCloudItem>`。
- infra 查询从 `select keyword` 改为按 `keyword` 聚合：
  - `select k.keyword, count(*) as count`
  - 来源表为 `classics_ming_customs_keyword k`
  - 当传入 `visibility` 时 join `classics_ming_customs_entry e` 并按 `e.visibility` 过滤。
  - 按 `count desc, keyword asc` 排序，避免 UI 抖动。
- `MingCustomsApplicationService#listKeywordCloud` 透传 `List<MingCustomsKeywordCloudItem>`。
- Controller 返回 `List<MingCustomsKeywordCloudItemResponse>`。
- Response JSON 固定为：

```json
[
  {
    "keyword": "礼制",
    "count": 12
  }
]
```

验收点：

- 后端 contract test 断言 `keyword-cloud` 返回 `keyword` 和 `count`。
- infra 测试覆盖聚合数量和可见性过滤。
- 不新增数据库字段。

### Step 2：前端 API 契约和 service

范围文件：

- 新增 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-types.ts`
- 新增 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-service.ts`
- 新增 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-service-contract.test.ts`

处理动作：

- 定义 `MingCustomsRecord`，字段对齐后端 `MingCustomsResponse`。
- 定义 `MingCustomsKeywordCloudItem`，字段为 `keyword` 和 `count`。
- 定义 `MingCustomsQuery`，支持 `pageNo`、`pageSize`、`keyword`、`category`、`visibility`、`tagName`、`sortDirection`。
- 定义 `MingCustomsCommand`，支持新增和更新字段。
- 实现 `page`、`get`、`add`、`update`、`deleteById`、`listKeywordCloud`。
- 实现 `listCategoryOptions`，复用现有系统字典接口 `POST /sys/dict/page`，请求 `type = "CLASSICS_MING_CUSTOMS_CATEGORY"`，并将返回记录映射为 `DictItem[]`。
- service 内部 request/response 类型不向页面导出，符合 admin-web service 规则。

验收点：

- contract test 断言 URL、method 和 request body。
- `deleteById(id)` 请求体为 `{ id }`。
- `listKeywordCloud(visibility)` 使用 query string 传递可见性，并返回 `MingCustomsKeywordCloudItem[]`。
- `listCategoryOptions()` 调用 `/sys/dict/page`，并只向页面暴露 `type`、`value`、`label`。

### Step 3：明代习俗初始化数据补全

范围文件：

- 修改 `db/data/system.sql`
- 修改 `db/data/classics.sql`
- 修改 `db/data-source/system.json`
- 修改 `scripts/generate-system-data-sql.ts`

处理动作：

- 在 `db/data/system.sql` 增加 `system_dict` 初始化数据。
- 字典类型固定为 `CLASSICS_MING_CUSTOMS_CATEGORY`。
- 在 `db/data-source/system.json` 新增顶层 `dicts` 数组，字段为 `type`、`label`、`value`、`priority`、`remarks`。
- 扩展 `scripts/generate-system-data-sql.ts`：
  - `SystemSeed` 增加 `dicts`。
  - 新增 `DictSeed` 类型。
  - 新增 `appendDictSql`，按 `id` 生成 `INSERT INTO system_dict ... ON DUPLICATE KEY UPDATE ...`。
  - 在 `appendAutoIncrementSql` 中加入 `system_dict` 的 next value。
  - 生成后执行 `node scripts/generate-system-data-sql.ts --check`，确保 `db/data/system.sql` 与 JSON 数据源一致。
- 在 `db/data/classics.sql` 补全最小明代习俗样例数据，至少覆盖：
  - 2 条 `classics_ming_customs_entry`，分别使用不同 `category`。
  - 每条 entry 至少 2 个 `classics_ming_customs_keyword`。
  - 至少 1 条 `PUBLIC`、1 条 `PRIVATE`，用于验证列表和关键词云 visibility 过滤。
  - `category` 使用 `CLASSICS_MING_CUSTOMS_CATEGORY` 的 `value`，例如 `RITUAL`、`FESTIVAL`。
- init data 必须能支撑 dev.env 冒烟，不依赖人工通过页面新增基础测试数据。
- 初始分类值先采用稳定、粗粒度分类，避免从现有条目反推：
  - `RITUAL` / `礼制`
  - `FESTIVAL` / `岁时节令`
  - `MARRIAGE` / `婚丧嫁娶`
  - `DAILY_LIFE` / `日用生活`
  - `BELIEF` / `信仰禁忌`
  - `SOCIAL` / `社会交往`
- `priority` 使用稳定间隔，方便后续插入。
- `db/data-source/system.json` 同步追加同一组字典源数据，避免 SQL 与源数据漂移。
- 当前 `system_dict` 没有 `(type, value)` 唯一键，本轮不为字典初始化引入表结构变更；dev.env 手工同步按 `type` 先清理再插入。生成脚本里的 `ON DUPLICATE KEY UPDATE` 只依赖固定主键 `id`。
- dev.env 同步 SQL 示例：

```sql
DELETE FROM `system_dict`
WHERE `type` = 'CLASSICS_MING_CUSTOMS_CATEGORY';

INSERT INTO `system_dict` (`type`, `label`, `value`, `priority`, `remarks`) VALUES
    ('CLASSICS_MING_CUSTOMS_CATEGORY', '礼制', 'RITUAL', 10, '明代习俗分类'),
    ('CLASSICS_MING_CUSTOMS_CATEGORY', '岁时节令', 'FESTIVAL', 20, '明代习俗分类'),
    ('CLASSICS_MING_CUSTOMS_CATEGORY', '婚丧嫁娶', 'MARRIAGE', 30, '明代习俗分类'),
    ('CLASSICS_MING_CUSTOMS_CATEGORY', '日用生活', 'DAILY_LIFE', 40, '明代习俗分类'),
    ('CLASSICS_MING_CUSTOMS_CATEGORY', '信仰禁忌', 'BELIEF', 50, '明代习俗分类'),
    ('CLASSICS_MING_CUSTOMS_CATEGORY', '社会交往', 'SOCIAL', 60, '明代习俗分类');
```

- dev.env 同步明代习俗样例数据时，执行 `db/data/classics.sql` 中本轮新增的 `classics_ming_customs_entry` 和 `classics_ming_customs_keyword` 片段；该片段需要使用固定 `id` 和 `ON DUPLICATE KEY UPDATE`，便于重复同步。
- 样例 keyword 的 `priority` 需要选择稳定且不与同表现有数据冲突的范围。

验收点：

- fresh init data 包含 `CLASSICS_MING_CUSTOMS_CATEGORY` 字典项。
- `node scripts/generate-system-data-sql.ts --check` 通过。
- fresh init data 包含明代习俗 entry 和 keyword 样例，能直接打开列表、筛选分类、渲染关键词云。
- dev.env 数据库 `system_dict` 中可查询到该类型。
- dev.env 数据库 `classics_ming_customs_entry` 和 `classics_ming_customs_keyword` 中可查询到同一组样例数据。
- 前端分类筛选和编辑表单不使用硬编码分类数组。

### Step 4：页面路由和页面骨架

范围文件：

- 修改 `kuzhambu-apps/admin-web/src/router/index.tsx`
- 新增 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
- 新增 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.css`
- 新增 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`

处理动作：

- 注册路由 `classics/ming-customs`。
- 页面使用 `KuzhambuListPage`。
- 页面标题为 `明代习俗`，描述为明代习俗专题条目治理入口。
- 页面提供搜索、筛选、刷新、新增按钮。
- 搜索框 accessible name 为 `搜索明代习俗`。

验收点：

- 访问 `/classics/ming-customs` 能看到页面标题。
- 页面首次加载调用 `POST /classics/ming-customs/page`。
- 搜索关键词会进入 page request 的 `keyword`。

### Step 5：列表、筛选和关键词云

范围文件：

- 新增 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-list.tsx`
- 新增 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-keyword-cloud.tsx`
- 修改 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
- 修改 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.css`

处理动作：

- 列表列固定为：习俗、分类、章节、摘要、可见性、操作。
- 习俗标题可点击打开详情抽屉。
- 筛选字段包含分类、可见性、排序方向。
- 分类 Select 候选来自 `CLASSICS_MING_CUSTOMS_CATEGORY` 字典，不从当前页记录归纳。
- 关键词云显示 `listKeywordCloud` 返回的关键词和强弱。
- 关键词云按 `count` 映射为 3 档或 4 档视觉强度，具体字号/颜色由前端计算。
- 点击关键词云项后，把该关键词应用到列表搜索。

验收点：

- 列表表格 accessible name 为 `明代习俗表格`。
- 点击关键词云 `礼俗` 后，请求体包含 `keyword: "礼俗"`。
- 可见性筛选支持 `PUBLIC`、`PRIVATE` 和全部。
- 关键词云中较高 `count` 的关键词视觉强度更高。

### Step 6：富文本展示控件

范围文件：

- 新增 `kuzhambu-apps/admin-web/src/components/kuzhambu-rich-content/kuzhambu-rich-content-viewer.tsx`
- 新增 `kuzhambu-apps/admin-web/src/components/kuzhambu-rich-content/kuzhambu-rich-content-viewer.css`
- 新增 `kuzhambu-apps/admin-web/src/components/kuzhambu-rich-content/kuzhambu-rich-content-viewer.test.tsx`
- 修改 `kuzhambu-apps/admin-web/package.json`
- 修改 `kuzhambu-apps/package-lock.json`

处理动作：

- 通过 npm workspace 安装 `dompurify` 和 `marked`，让依赖变更同步进入 `kuzhambu-apps/package-lock.json`。
- 新增通用富文本展示控件，入参为 `content?: string | null`、`contentFormat?: "MARKDOWN" | "HTML" | "TEXT" | string | null`。
- 控件内部集中处理 Markdown/HTML/TEXT 展示，业务页面通过控件展示正文内容。
- Markdown 先用 `marked` 转为 HTML，再交给 sanitizer 清理后展示。
- HTML 渲染使用 `dompurify` allowlist sanitizer。
- HTML 安全边界由 sanitizer 承担；正则只用于辅助规整输入，例如去除控制字符或规整空白。
- sanitizer 覆盖危险输入：`script`、事件属性如 `onerror`、`javascript:` 协议、`iframe`、`object`、`embed`、危险 `style`。
- `TEXT` 或未知格式按纯文本展示。

验收点：

- 富文本控件测试覆盖 Markdown、HTML、TEXT 和危险输入清理。
- Markdown/HTML 正文最终渲染结果只保留 allowlist 内的安全节点和属性。

### Step 7：详情抽屉和新增编辑

范围文件：

- 新增 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-model.tsx`
- 新增 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-form-values.ts`
- 修改 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
- 修改 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.css`

处理动作：

- 抽屉支持 `create` 和 `edit` 两种模式。
- 字段包含标题、分类、章节、节、概述、正文、原文摘录、正文格式、可见性。
- `contentFormat` 默认 `MARKDOWN`。
- 详情正文使用富文本展示控件；编辑正文仍使用 textarea。
- 分类字段使用 `CLASSICS_MING_CUSTOMS_CATEGORY` 字典 Select。
- 保存时 `category` 写入字典 `value`，列表和详情展示时用字典 `label` 兜底映射。
- 可见性使用 `Switch`，`PUBLIC` 为公开，`PRIVATE` 为私有。
- 保存成功后关闭抽屉并刷新列表。

验收点：

- 详情抽屉展示 Markdown/HTML 正文时，最终渲染结果只保留 allowlist 内的安全节点和属性。
- 新增时调用 `POST /classics/ming-customs/add`。
- 编辑时调用 `POST /classics/ming-customs/update`，并携带 `id`。
- 表单控件有稳定 accessible name。

### Step 8：删除和分享

范围文件：

- 修改 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-list.tsx`
- 修改 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
- 修改 `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`，补充删除和分享覆盖

处理动作：

- 删除使用 `useKuzhambuConfirm().danger`。
- 删除确认文案说明删除后不再出现在明代习俗列表。
- 分享调用 `shareService.create`。
- 分享 target 固定为 `{ contentType: "MING_CUSTOMS", contentId: entry.id }`。
- 分享标题为 `${title} 分享`，可见性默认 `PUBLIC`。

验收点：

- 删除操作调用 `POST /classics/ming-customs/delete`。
- 分享测试断言 `contentType` 为 `MING_CUSTOMS`。
- 分享成功时复制 `shareUrl`，无 clipboard 时展示链接文案。

### Step 9：后端 Controller 契约测试兜底

范围文件：

- 修改 `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/mingcustoms/MingCustomsAdminControllerTest.java`

处理动作：

- 将当前只断言 controller 类型存在的测试扩展为 controller contract test。
- 固定 `page`、`get`、`add`、`update`、`delete`、`keyword-cloud` 的响应字段和调用路径。
- `keyword-cloud` 断言返回数组元素包含 `keyword` 和 `count`，保持 `{ keyword, count }[]` 契约。
- 确认 `MingCustomsResponse` 已覆盖 Admin Web 依赖字段；当前应包含 `id`、`title`、`category`、`chapter`、`section`、`summary`、`contentFormat`、`content`、`originalExcerpts`、`visibility`。

验收点：

- `MingCustomsAdminControllerTest` 能验证 Admin Web 依赖的字段。
- 后端 response 字段与 `MingCustomsRecord` 一致。

### Step 10：E2E 冒烟

范围文件：

- 新增 `kuzhambu-apps/admin-web/e2e/classics/ming-customs/ming-customs.spec.ts`

处理动作：

- 参考 `kuzhambu-apps/admin-web/e2e/classics/sancai/sancai.spec.ts`。
- mock 当前用户菜单，包含 `/classics/ming-customs`。
- mock 权限，包含 `classics:mingcustoms:view`、`classics:mingcustoms:edit`、`classics:mingcustoms:delete`。
- mock `page`、`keyword-cloud`、`add`、`update`、`delete`、`shares/create`。
- mock 字典接口或 options 来源，包含 `CLASSICS_MING_CUSTOMS_CATEGORY`。
- `keyword-cloud` mock 返回 `{ keyword, count }[]`。

验收点：

- E2E 能打开明代习俗页面。
- 能通过关键词搜索触发正确 request body。
- 能打开编辑抽屉并保存。
- 能在详情抽屉看到 Markdown/HTML 正文展示效果。
- mock 中的危险 HTML 经过控件清理后，只渲染 allowlist 内的安全内容。
- 能创建分享，request body 包含 `MING_CUSTOMS`。

### Step 11：文档和覆盖状态

范围文件：

- 修改 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- 后续实现完成后删除本 RUNBOOK：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`

处理动作：

- 将明代习俗中已完成的 Admin Web 闭环条目更新为已完成或收窄剩余项。
- 保留批量可见性、摘要/标签/问答、版本恢复和导出为剩余未完成。
- PR 收口前清理 RUNBOOK。

验收点：

- 覆盖表不夸大本轮未做能力。
- PR 合并前 RUNBOOK 被删除或明确保留原因。

## 5. 验证计划

### 前端

在 `kuzhambu-apps/` 执行：

```sh
npm run format:check
npm run lint
npm run test
npm run build
```

### 初始化数据生成

在 repo root 执行：

```sh
node scripts/generate-system-data-sql.ts --check
```

针对 admin-web E2E 执行：

```sh
cd kuzhambu-apps/admin-web
npm run e2e -- e2e/classics/ming-customs/ming-customs.spec.ts
```

### 后端

在 `kuzhambu-servers/` 执行：

```sh
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-infra,biz/classics/kuzhambu-classics-interface -am test
```

### dev.env 冒烟

加载 repo-root `dev.env` 后启动 admin starter：

```sh
set -a
source dev.env
set +a
cd kuzhambu-servers
mvn -pl starter/kuzhambu-admin-starter -am -DskipTests install
cd starter/kuzhambu-admin-starter
mvn spring-boot:run
```

启动 admin-web：

```sh
cd kuzhambu-apps
npm run dev:admin
```

冒烟检查：

- 确认 `system_dict` 已有 `CLASSICS_MING_CUSTOMS_CATEGORY`。
- 确认 `classics_ming_customs_entry` 和 `classics_ming_customs_keyword` 已有 init data 样例。
- 使用默认 admin 登录 HOW-TO 登录。
- 打开 `/classics/ming-customs`。
- 搜索关键词。
- 使用分类下拉筛选。
- 新增一条测试习俗。
- 编辑测试习俗标题或摘要。
- 创建分享并确认复制链接。
- 删除测试习俗。
- 清理测试数据和本地日志。

## 6. 小步提交建议

提交粒度以 2-5 个文件为目标；若实现时某一步实际超过 5 个文件，继续按同一边界拆分。

1. `Feat(classics): 增加明代习俗关键词云领域契约`
   - `MingCustomsRepository.java`
   - `MingCustomsKeywordCloudItem.java`
   - `MingCustomsApplicationService.java`
   - `MingCustomsApplicationServiceImpl.java`
   - `MingCustomsRepositoryImpl.java`
2. `Feat(classics): 输出明代习俗关键词云接口契约`
   - `MingCustomsKeywordCloudItemResponse.java`
   - `MingCustomsInterfaceAssembler.java`
   - `MingCustomsAdminController.java`
   - `MingCustomsAdminControllerTest.java`
   - `MingCustomsRepositoryTest.java`
3. `Data(classics): 增加明代习俗初始化数据`
   - `db/data/system.sql`
   - `db/data/classics.sql`
   - `db/data-source/system.json`
   - `scripts/generate-system-data-sql.ts`
4. `Feat(admin-web): 增加明代习俗前端服务`
   - `ming-customs-types.ts`
   - `ming-customs-service.ts`
   - `ming-customs-service-contract.test.ts`
5. `Feat(admin-web): 增加富文本展示控件`
   - `kuzhambu-rich-content-viewer.tsx`
   - `kuzhambu-rich-content-viewer.css`
   - `kuzhambu-rich-content-viewer.test.tsx`
   - `package.json`
   - `package-lock.json`
6. `Feat(admin-web): 注册明代习俗页面入口`
   - `router/index.tsx`
   - `ming-customs-page.tsx`
   - `ming-customs-page.css`
   - `ming-customs-page.test.tsx`
7. `Feat(admin-web): 增加明代习俗列表和关键词云`
   - `ming-customs-list.tsx`
   - `ming-customs-keyword-cloud.tsx`
   - `ming-customs-page.tsx`
   - `ming-customs-page.css`
8. `Feat(admin-web): 增加明代习俗详情编辑`
   - `ming-customs-model.tsx`
   - `ming-customs-form-values.ts`
   - `ming-customs-page.tsx`
   - `ming-customs-page.css`
9. `Feat(admin-web): 支持明代习俗删除分享`
   - `ming-customs-list.tsx`
   - `ming-customs-page.tsx`
   - `ming-customs-page.test.tsx`
10. `Test(classics): 固定明代习俗后台冒烟`
    - `ming-customs.spec.ts`
    - 必要的 e2e fixture 或 mock helper 文件
11. `Docs(classics): 更新明代习俗闭环覆盖状态`
    - `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
    - PR 收口时清理 `docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`

## 7. 执行校验点

- `keyword-cloud` 的后端、前端、E2E 契约均固定为 `{ keyword, count }[]`。
- 分类候选统一来自 `CLASSICS_MING_CUSTOMS_CATEGORY` 字典，页面不硬编码分类数组。
- init data 和 dev.env 同步后，列表、分类筛选、关键词云在无人工造数时可冒烟。
- 正文展示通过通用富文本控件处理 Markdown/HTML/TEXT；编辑正文继续使用 textarea。
- HTML 清理使用 allowlist sanitizer；正则承担辅助规整职责。
