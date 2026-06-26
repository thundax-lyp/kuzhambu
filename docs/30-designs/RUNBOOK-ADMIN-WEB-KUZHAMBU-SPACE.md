# Admin Web KuzhambuSpace Runbook

## 目的

本文档定义 `kuzhambu-apps/admin-web/` 内 `antd Space` 治理收口方案，目标是消除 `direction` 废弃 warning，并把布局间距组件统一收敛到共享边界 `KuzhambuSpace`。

本轮目标固定为：

1. 新增薄封装共享组件 `KuzhambuSpace`。
2. 在类型层排除 `direction`，只允许使用 `orientation`。
3. 同步覆盖 `Space.Compact` 的共享入口，避免保留半套直连 `antd` 用法。
4. 在 `admin-web` ESLint 中增加 hard rule，禁止直接从 `antd` 导入 `Space`。
5. 完成首批存量替换，并消除当前已知 `direction` warning 来源。

本文档是本轮执行依据。前端治理以 [`docs/00-governance/ADMIN-WEB-RULES.md`](../00-governance/ADMIN-WEB-RULES.md) 为准。

## 分支

- 当前工作分支：`feat/operations-report-runbook`

## 已确认项对应操作

### 统一共享入口

- 页面、布局、共享组件不再直接使用 `antd Space`。
- 统一改为从 `src/components/kuzhambu-space/` 导入：
  - `KuzhambuSpace`
  - `KuzhambuSpaceCompact`

### 废弃属性处理

- `KuzhambuSpace` props 在类型层排除 `direction`。
- 业务代码不得再传 `direction`。
- 竖向布局统一改用 `orientation="vertical"`。

### Lint 门禁

- `admin-web` ESLint 增加 hard rule，禁止从 `antd` 导入：
  - `Space`
  - `SpaceProps`
- `KuzhambuSpace` 实现文件可作为唯一例外，允许接触 `antd Space`。

### 首批替换范围

- 当前直接使用 `direction` 的页面必须在本轮全部替换完成：
  - `src/pages/discovery/search-admin/search-admin-page.tsx`
  - `src/pages/discovery/qa-admin/qa-admin-page.tsx`
- 共享骨架和高复用入口优先切换：
  - `src/components/kuzhambu-list-page/kuzhambu-list-page.tsx`
  - `src/components/placeholder-page.tsx`
  - `src/layouts/admin-layout.tsx`

## 当前基线

已确认事实：

- `admin-web` 已存在多个 `Kuzhambu*` 共享组件目录，新增 `kuzhambu-space` 符合既有共享组件边界。
- `admin-web` ESLint 已承载多条本地 hard rule，适合继续把 UI 组件边界收敛为门禁。
- 当前 `src/` 下仍广泛存在 `import { Space } from "antd"`。
- 当前已确认的废弃 warning 来源至少包括：
  - `src/pages/discovery/search-admin/search-admin-page.tsx`
  - `src/pages/discovery/qa-admin/qa-admin-page.tsx`
- 当前还存在多处 `Space.Compact` 用法，如果只封装 `Space`，会留下新的直连逃生口。

## 本轮范围

在范围内：

- `KuzhambuSpace` / `KuzhambuSpaceCompact` 共享组件。
- ESLint hard rule。
- `ADMIN-WEB-RULES.md` 对应规则落点。
- 首批高复用页面与已知 warning 页面替换。
- 最小相关测试与 lint 验证。

不在范围内：

- 全仓一次性替换所有 `Space` 用法。
- 非 `admin-web` 工程的同类治理。
- 对 `Space` 视觉样式做二次包装或品牌化设计。
- 引入新布局体系替代 `Space`。

## 目标设计

### 组件设计

- 新增目录：`kuzhambu-apps/admin-web/src/components/kuzhambu-space/`
- 文件建议：
  - `index.ts`
  - `kuzhambu-space.tsx`
  - `kuzhambu-space.test.tsx`

建议导出结构：

- `KuzhambuSpace`
- `KuzhambuSpaceCompact`
- 必要的 type export，例如：
  - `KuzhambuSpaceProps`
  - `KuzhambuSpaceCompactProps`

类型约束要求：

- `KuzhambuSpaceProps` 基于 `antd` 的 `SpaceProps` 做 `Omit<..., "direction">`。
- 对外只保留 `orientation`，不做 `direction -> orientation` 的运行时兼容。
- 这样新代码一旦传 `direction`，会直接在 TypeScript 层报错，而不是等到测试 warning。

### Lint 设计

建议新增规则标签：

- `ADMIN_WEB_UI_NO_ANTD_SPACE_DIRECT`

门禁要求：

- 禁止从 `antd` 直接导入 `Space`。
- 禁止从 `antd` 直接导入 `SpaceProps`。
- 报错文案必须明确要求改用 `KuzhambuSpace` / `KuzhambuSpaceCompact`。
- 例外仅限 `src/components/kuzhambu-space/kuzhambu-space.tsx`。

### 替换策略

按风险从低到高分三批：

1. 共享骨架与已知 warning 页面。
2. `Space.Compact` 高频表格操作区。
3. 其余零散页面和组件。

这样可以先建立共享边界和 lint 门禁，再逐步清理存量，避免一次性大改动。

## 关联文件

治理与任务文件：

- `docs/00-governance/ADMIN-WEB-RULES.md`
- `docs/30-designs/RUNBOOK-ADMIN-WEB-KUZHAMBU-SPACE.md`
- `TODO.md`

组件与 lint 文件：

- `kuzhambu-apps/admin-web/eslint.config.js`
- `kuzhambu-apps/admin-web/src/components/kuzhambu-space/index.ts`
- `kuzhambu-apps/admin-web/src/components/kuzhambu-space/kuzhambu-space.tsx`
- `kuzhambu-apps/admin-web/src/components/kuzhambu-space/kuzhambu-space.test.tsx`

首批替换文件：

- `kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`
- `kuzhambu-apps/admin-web/src/components/kuzhambu-list-page/kuzhambu-list-page.tsx`
- `kuzhambu-apps/admin-web/src/components/placeholder-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/search-admin/search-admin-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/discovery/qa-admin/qa-admin-page.tsx`

次批 `Space.Compact` 文件：

- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-workbench-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-entity-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/refinement/components/refinement-relation-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-version-table.tsx`
- `kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-entity-table.tsx`

## 任务拆分

### T1. 新增 `KuzhambuSpace` 共享组件

范围控制在 3 个文件：

- `src/components/kuzhambu-space/index.ts`
- `src/components/kuzhambu-space/kuzhambu-space.tsx`
- `src/components/kuzhambu-space/kuzhambu-space.test.tsx`

完成结果：

- `KuzhambuSpace` 与 `KuzhambuSpaceCompact` 可直接替代 `antd Space` 常见用法。
- `direction` 在类型层不可传入。

### T2. 增加 hard rule 并固化治理规则

范围控制在 2 个文件：

- `kuzhambu-apps/admin-web/eslint.config.js`
- `docs/00-governance/ADMIN-WEB-RULES.md`

完成结果：

- lint 直接阻止新增 `antd Space` 直连用法。
- 治理文档与门禁规则标签一致。

### T3. 替换共享骨架中的 `Space`

范围控制在 3 个文件：

- `src/layouts/admin-layout.tsx`
- `src/components/kuzhambu-list-page/kuzhambu-list-page.tsx`
- `src/components/placeholder-page.tsx`

完成结果：

- 高复用入口不再导入 `antd Space`。
- 后续页面复用默认走共享边界。

### T4. 替换已知 `direction` warning 页面

范围控制在 2 个文件：

- `src/pages/discovery/search-admin/search-admin-page.tsx`
- `src/pages/discovery/qa-admin/qa-admin-page.tsx`

完成结果：

- 当前测试日志中的 `direction` 废弃 warning 被消除。
- 页面统一使用 `orientation`。

### T5. 替换第一批 `Space.Compact` 高频表格

范围控制在 4 个文件：

- `src/pages/knowledge/refinement/components/refinement-workbench-table.tsx`
- `src/pages/knowledge/refinement/components/refinement-entity-table.tsx`
- `src/pages/knowledge/refinement/components/refinement-relation-table.tsx`
- `src/pages/knowledge/graph-extraction/components/graph-extraction-task-table.tsx`

完成结果：

- 第一批表格操作列改用 `KuzhambuSpaceCompact`。
- `Space.Compact` 不再成为 lint 逃生口。

### T6. 替换第二批 `Space.Compact` 图谱结果表格

范围控制在 4 个文件：

- `src/pages/knowledge/graph-results/components/graph-version-table.tsx`
- `src/pages/knowledge/graph-results/components/graph-entity-table.tsx`
- `src/pages/knowledge/graph-results/components/graph-relation-table.tsx`
- `src/pages/knowledge/graph-results/components/graph-lineage-node-table.tsx`

完成结果：

- 图谱结果表格统一切到共享 compact 入口。

### T7. 补齐剩余图谱 lineage 表格与最终验证

范围控制在 4 个文件：

- `src/pages/knowledge/graph-results/components/graph-lineage-relation-table.tsx`
- `src/pages/knowledge/graph-results/graph-results-page.tsx`
- `src/test/setup.ts`
- `kuzhambu-apps/admin-web/package.json`

完成结果：

- 该批量替换后的测试入口稳定可运行。
- 如需在测试环境中把 warning 视为失败，本任务负责落到测试支撑。

## 验证要求

本轮至少执行：

```sh
cd kuzhambu-apps/admin-web
npm run format
npm run format:check
npm run lint
npm run test
```

如新增测试门禁，需要验证：

- 直接 `import { Space } from "antd"` 会被 ESLint 拦截。
- `KuzhambuSpace` 调用 `orientation="vertical"` 不产生 `direction` warning。
- `KuzhambuSpaceCompact` 可覆盖既有按钮紧凑布局场景。

## 风险与处理

### `Space.Compact` 接口不完全等价

- 处理方式：在 T1 先锁定最小兼容面，只覆盖当前仓库真实用法，不预先扩展无需求 API。

### lint 一次性开启后存量过多

- 处理方式：先完成 T1-T4，再开启 hard rule；或在规则内临时只对白名单目录生效，待 T5-T7 收口后再全量开启。
- 最终目标仍然是全量 hard rule，不保留长期灰度状态。

### 测试 warning 来源不止 `direction`

- 处理方式：本轮只把 `Space` 相关 warning 收口；其他噪音单独立题，不在本 RUNBOOK 顺手扩张。

## 建议执行顺序

1. T1 `KuzhambuSpace` 共享组件
2. T2 hard rule 与治理文档
3. T3 共享骨架替换
4. T4 已知 warning 页面替换
5. T5 第一批 `Space.Compact` 表格
6. T6 第二批图谱结果表格
7. T7 剩余 lineage 表格与最终验证
