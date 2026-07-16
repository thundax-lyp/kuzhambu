# Portal Web Rules

## Purpose

本文件固定 `kuzhambu-apps/portal-web` 的前端治理规则。

本文件覆盖：

- 前端目录和页面域边界
- TypeScript、React 和 ESLint 门禁
- Service、API 和共享 UI 组件边界

## Scope

当前范围：

- `kuzhambu-apps/portal-web/src`
- React、TypeScript、Vite、shadcn/ui、Tailwind CSS 和 TanStack Query 代码

不在范围内：

- `admin-web` 的 Ant Design 管理台规则
- Java 后端命名、路径与分层规则
- HTTP API 后端契约

## Principles

- portal-web 使用读者侧体验栈，允许 shadcn/ui 和 Tailwind CSS。
- 可机器门禁的规则优先沉淀到 ESLint。
- 页面域内 service 和 types 优先跟随页面目录放置。
- 共享基础设施保留在 `src/api/`、`src/components/`、`src/lib/` 等明确边界中。

## Hard Rules

### Architecture

- `PORTAL_WEB_LAYER_FETCH_ONLY_HTTP`：只有 `src/api/http.ts` 可直接调用 `fetch`。
- `PORTAL_WEB_LAYER_SHARED_COMPONENT_NO_PAGE`：`src/components/` 不导入页面、页面 service 或页面 types。
- `PORTAL_WEB_LAYER_API_NO_PAGE`：`src/api/` 不导入页面、页面 service、页面 types 或组件。
- `PORTAL_WEB_LAYER_NO_DEEP_RELATIVE_IMPORT`：`src` 下禁止 `../../` 及更深相对 import。
- `PORTAL_WEB_LAYER_PAGE_NO_PARENT_RELATIVE_IMPORT`：页面文件禁止 `../` import。
- `PORTAL_WEB_LAYER_PAGE_NO_EXTERNAL_SERVICE`：页面域代码不导入其他页面域的 service。
- `PORTAL_WEB_LAYER_COMPONENT_INDEX_EXPORT_ONLY`：`src/components/**/index.ts` 只包含带 `from` 的 re-export 声明。

### Placement

- 页面放在 `src/pages/<domain>/<domain>-page.tsx`。
- 页面表单、详情或同域入口放在同页面域，例如 `src/pages/share/share-form.tsx`。
- 页面 service 放在同页面域 `<domain>-service.ts`。
- 页面类型放在同页面域 `<domain>-types.ts`。
- 跨页面 HTTP 基础设施放在 `src/api/`。
- shadcn/ui 生成组件放在 `src/components/ui/`。

### Naming

- `PORTAL_WEB_NAME_FILE_KEBAB_CASE`：`src` 文件名使用 kebab-case。
- `PORTAL_WEB_NAME_FUNCTION_ARROW`：前端业务方法默认使用箭头函数；`src/components/ui/` 下 shadcn/ui 生成组件不受此规则约束。
- `PORTAL_WEB_NAME_NO_NESTED_TERNARY`：禁止嵌套三元表达式。
- `PORTAL_WEB_NAME_SERVICE_NAMESPACE_IMPORT`：页面和组件运行时引用同域 service 固定使用 `import * as service from "./xx-service"`；`import type` 不受限制。
- TypeScript 变量和方法使用 camelCase，类型使用 PascalCase，常量允许 UPPER_CASE。

### Code Quality

- 禁止 `console.log`；允许 `console.warn` 和 `console.error`。
- 禁止显式 `any`。
- React Hooks 使用 `eslint-plugin-react-hooks` 推荐规则。
- React Refresh 使用 `react-refresh/only-export-components`。

## Review Rules

- 页面域逻辑明显变复杂时，优先拆到同目录 service、types 或组件中。
- shared UI 组件不得引入页面语义。
- portal-web 不复用 admin-web 的 Ant Design 专属门禁，也不禁止 Tailwind CSS。

### UI

- 业务控件应具备业务可访问名称。优先使用可见文本；无稳定可见文本时，DOM 元素使用 `aria-label` 或 `aria-labelledby`，封装组件使用 `ariaLabel`。可访问名称只表达用户和辅助技术需要理解的业务语义，不承担自动化测试稳定锚点职责。
- 需要稳定自动化定位的业务控件应显式提供 `data-testid` 或组件封装层的 `testId`；`testId` 命名应描述模块、对象和动作，避免复用纯展示文案。
- `data-testid` / `testId` 遵循 [`UI-RULES.md`](./UI-RULES.md) 的统一测试锚点规则：生产发布构建擦除，开发、单测和 E2E 构建保留。
- 不得为了测试覆盖原生控件语义 role。`button`、`input`、`select`、`table` 等原生或组件库已提供语义的控件保留其默认 role，只补充稳定名称。

### Testing

- Playwright locator 优先级固定为：

```text
getByTestId
getByRole(..., { name })
getByRole
getByLabel
getByText
CSS Selector
```

- Playwright 测试业务控件时优先使用 `getByTestId` 作为明确技术锚点；`getByRole(..., { name })` 用于验证用户可感知语义、可访问名称或没有必要新增技术锚点的简单控件。
- `data-testid` / `testId` 不替代可访问名称。图标按钮、无文本按钮、表格、搜索框等仍需通过可见文本、DOM `aria-label` / `aria-labelledby` 或组件封装层的 `ariaLabel` 提供业务语义。
- 若 E2E 使用生产式构建产物，构建时必须设置 `VITE_EXPOSE_TEST_ID=true` 保留 `data-testid`；生产发布构建不得设置该变量。
- Playwright 测试禁止使用 `waitForTimeout`。
- Playwright 测试禁止使用复杂 CSS selector。
- E2E 测试之间不得依赖共享状态。
- 测试应验证用户可见结果和关键请求契约，不验证组件库内部 DOM 细节。
