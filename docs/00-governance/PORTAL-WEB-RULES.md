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
