# Admin Web Rules

## Purpose

本文件固定 `kuzhambu-apps/admin-web` 的前端治理规则。

本文件覆盖：

- 前端架构与分层
- 命名与目录归属
- UI 与交互规范
- Service、API、状态与权限边界
- 测试与验证规则

## Scope

当前范围：

- `kuzhambu-apps/admin-web/src`
- `kuzhambu-apps/admin-web/e2e`
- React、TypeScript、Vite、Ant Design 管理台代码

不在范围内：

- Java 后端命名、路径与分层规则
- HTTP API 后端契约
- 完整品牌手册
- 非 admin-web 前端工程

## Principles

- 一致性优先于灵活性。
- 约定优先于配置。
- 能机器门禁的规则优先机器门禁。
- 相同交互必须使用相同实现。
- 不依赖开发者记忆规则；规则应尽量沉淀到共享组件、TypeScript、ESLint、Playwright 和 Code Review。

## Rule Structure

规则分为两个层级：

- `Hard Rules`：必须可由 ESLint、TypeScript、测试或架构脚本稳定门禁。
- `Review Rules`：由 AI 或人工审阅执行，暂不强制门禁。

同一条规则只归入一个层级。已由 `Hard Rules` 稳定门禁的内容不得在 `Review Rules` 中重复表述；当 `Review Rules` 被沉淀为门禁后，必须从 `Review Rules` 删除或改写为未被门禁覆盖的语义审阅点。

新增规则应先归入以下主题之一：

- `Architecture`
- `Placement`
- `Naming`
- `Service`
- `Code Quality`
- `UI`
- `State`
- `Permission`
- `Testing`
- `Forbidden Defaults`

`Hard Rules` 必须能够通过 ESLint、TypeScript、测试或架构脚本稳定门禁。
暂时没有门禁支撑的语义判断固定放入 `Review Rules`。
门禁报错信息必须包含本文件中的规则标签，例如 `ADMIN_WEB_LAYER_NO_DEEP_RELATIVE_IMPORT`。

## Hard Rules

### Architecture

- `ADMIN_WEB_LAYER_FETCH_ONLY_HTTP`：只有 `src/api/http.ts` 可直接调用 `fetch`。
- `ADMIN_WEB_LAYER_POST_HELPER_SERVICE_ONLY`：`postJson` / `postFormData` 只在 `*-service.ts` 使用。
- `ADMIN_WEB_LAYER_QUERY_FN_FROM_SERVICE`：`queryFn` / `mutationFn` 只调用 service 方法。
- `ADMIN_WEB_LAYER_SHARED_COMPONENT_NO_PAGE`：`src/components/` 不导入 `src/pages/`。
- `ADMIN_WEB_LAYER_SHARED_SERVICE_TYPES_ONLY`：`src/service/*-service.ts` 不导入页面目录；共享业务类型只从 `*-types.ts` 引用。
- `ADMIN_WEB_LAYER_COMPONENT_INDEX_EXPORT_ONLY`：`src/components/**/index.ts` 只包含带 `from` 的 re-export 声明。
- `ADMIN_WEB_LAYER_API_NO_PAGE`：`src/api/` 不导入页面、布局或组件。
- `ADMIN_WEB_LAYER_AUTH_NO_PAGE`：`src/auth/` 不导入页面、布局或页面 service。
- `ADMIN_WEB_LAYER_NO_DEEP_RELATIVE_IMPORT`：`src` 下禁止 `../../` 及更深相对 import。
- `ADMIN_WEB_LAYER_PAGE_NO_PARENT_RELATIVE_IMPORT`：页面文件禁止 `../` import。
- `ADMIN_WEB_LAYER_PAGE_COMPONENT_NO_EXTERNAL_PAGE`：页面私有组件不引用其他页面域；同模块 `common` 组件例外。
- `ADMIN_WEB_LAYER_PAGE_NO_EXTERNAL_SERVICE`：页面域代码不导入其他页面域的 service；同模块 `common` service 例外。
- `ADMIN_WEB_LAYER_SHARED_COMPONENT_CSS_LOCAL`：共享组件禁止 `../*.css` import。

### Placement

- `ADMIN_WEB_PATH_PAGE_SHAPE`：页面放在 `src/pages/<module>/<domain>/<domain>-page.tsx`。
- `ADMIN_WEB_PATH_PAGE_COMPONENTS`：页面私有组件放在同页面域 `components/`。
- `ADMIN_WEB_PATH_PAGE_SERVICE`：页面 service 放在同页面域 `<domain>-service.ts`；模块内复用的共享 service 放在 `src/pages/<module>/common/*-service.ts`。
- `ADMIN_WEB_PATH_AUTH`：认证、token、权限持久化放在 `src/auth/`。
- `ADMIN_WEB_PATH_ROUTER`：路由放在 `src/router/`。
- `ADMIN_WEB_PATH_QUERY`：TanStack Query 基线放在 `src/query/`。
- `ADMIN_WEB_PATH_HOOK_FILE`：hook 文件放在 `*/hooks/use-<name>.ts`。
- `ADMIN_WEB_PATH_GLOBAL_TYPES`：跨页面通用类型放在 `src/types/`。
- `ADMIN_WEB_PATH_TEST_SUPPORT`：测试支撑放在 `src/test/`。
- `ADMIN_WEB_PATH_E2E_PAGE_SPEC`：页面 E2E 放在 `e2e/<module>/<domain>/<domain>.spec.ts`。
- `ADMIN_WEB_PATH_E2E_LAYOUT_SPEC`：布局 E2E 放在 `e2e/layout/*.spec.ts`。

### Naming

- `ADMIN_WEB_NAME_FILE_KEBAB_CASE`：`src` 文件名使用 kebab-case。
- `ADMIN_WEB_NAME_PAGE_FILE`：页面文件命名为 `<domain>-page.tsx`。
- `ADMIN_WEB_NAME_PAGE_STYLE_FILE`：页面样式命名为 `<domain>-page.css`。
- `ADMIN_WEB_NAME_PAGE_SERVICE_FILE`：页面 service 命名为 `<domain>-service.ts`。
- `ADMIN_WEB_NAME_PAGE_TYPES_FILE`：页面类型命名为 `<domain>-types.ts`。
- `ADMIN_WEB_NAME_COMPONENT_EXPORT`：React 组件使用 PascalCase named export。
- `ADMIN_WEB_COMPONENT_SINGLE_EXPORT`：页面私有组件文件最多导出一个 PascalCase 组件。
- `ADMIN_WEB_NAME_PAGE_EXPORT`：页面组件使用 `export const XxxPage = () => {}`。
- `ADMIN_WEB_NAME_FUNCTION_ARROW`：前端方法默认使用箭头函数。
- `ADMIN_WEB_NAME_NO_NESTED_TERNARY`：禁止嵌套三元表达式。
- `ADMIN_WEB_NAME_CAMEL_CASE`：变量和方法使用 camelCase。
- `ADMIN_WEB_NAME_API_CONTRACT_TYPE_LOCATION`：`XxxRequest` / `XxxResponse` 只定义在 `*-service.ts` 或 `src/api/`。
- `ADMIN_WEB_NAME_API_CONTRACT_TYPE_EXPOSURE`：`XxxRequest` / `XxxResponse` 是 service 内部 API 契约，不从 service 导出。
- `ADMIN_WEB_NAME_SERVICE_INPUT_TYPE_LOCATION`：`XxxQuery` / `XxxCommand` 只定义在 `*-service.ts`；`PageQuery<T>` 只定义在 `src/types/page.ts`。
- `ADMIN_WEB_NAME_OPTION_RECORD_LOCATION`：`*OptionRecord` / `*OptionsRecord` 只定义在 `src/types/options.ts`；领域选项接口使用 `OptionsRecord<"xxxOptions">` 表达。
- `ADMIN_WEB_NAME_BUSINESS_DATA_TYPE_LOCATION`：`XxxRecord` / `XxxNode` 只定义在明确边界的 `*-types.ts`。
- `ADMIN_WEB_NAME_BOOLEAN`：布尔变量使用 `is`、`has`、`can` 前缀。
- `ADMIN_WEB_NAME_CONSTANT`：常量使用 `UPPER_SNAKE_CASE`。
- `ADMIN_WEB_NAME_PROPS_CAMEL_CASE`：`*Props` 属性必须使用 camelCase；原生 DOM attribute 只在渲染边界转换。
- `ADMIN_WEB_NAME_KUZHAMBU_COMPONENT`：`Kuzhambu*` 只在 `src/components/` 定义。
- `ADMIN_WEB_NAME_PAGE_CLASS_PREFIX`：页面根节点 class 使用页面域前缀；共享组件 class 使用 `kuzhambu-`。
- `ADMIN_WEB_STYLE_COMPONENT_CLASS_LOCATION`：组件域 CSS class 只定义在对应组件 CSS 文件中。
- `ADMIN_WEB_STYLE_PAGE_CLASS_LOCATION`：页面域 CSS class 只定义在对应页面 CSS 文件中。

### Service

- `ADMIN_WEB_NAME_SERVICE_METHOD`：service 方法必须使用固定动词前缀。当前共享动作白名单已包含 `regenerate`，因此重生成类前端 service 方法必须显式使用 `regenerate*`，不得退回 `update`、`change` 或其他别名。
- `ADMIN_WEB_NAME_SERVICE_METHOD_INPUT`：service 方法入参固定为无入参、单个 `XxxQuery`、单个 `XxxCommand` 或最多 3 个 plain parameters。
- `ADMIN_WEB_NAME_SERVICE_HELPER_TYPE`：service helper 泛型固定为 `XxxQuery`、`XxxCommand`、inline payload、plain value、`XxxRecord`、`XxxNode`、`OptionsRecord<...>`、`Page<XxxRecord/XxxNode>` 或数组。
- `ADMIN_WEB_NAME_SERVICE_TYPE_EXPOSURE`：页面和组件只从 service 引用 `XxxQuery` / `XxxCommand`；`XxxRecord` / `XxxNode` 从 `*-types.ts` 引用。
- `ADMIN_WEB_NAME_SERVICE_NAMESPACE_IMPORT`：页面和组件运行时引用同域 service 固定使用 `import * as service from "./xx-service"`；`import type` 不受限制。

### Code Quality

- `ADMIN_WEB_CODE_NO_CONSOLE_LOG`：禁止 `console.log`；临时诊断必须用带原因的 ESLint 单行豁免。
- `ADMIN_WEB_CODE_NO_EXPLICIT_ANY`：禁止显式 `any`；无法建模的边界必须用带原因的 ESLint 单行豁免。

### UI

- `ADMIN_WEB_UI_CONFIRM_HOOK`：确认操作固定使用 `useKuzhambuConfirm`，页面不直接调用 `Modal.confirm`。
- `ADMIN_WEB_UI_NO_ANTD_SPACE_DIRECT`：页面、布局和共享组件禁止直接从 `antd` 导入 `Space` 或 `SpaceProps`；统一使用 `src/components/kuzhambu-space/` 暴露的 `KuzhambuSpace` 与 `KuzhambuSpaceCompact`。`KuzhambuSpace` 实现文件自身作为唯一例外。
- `ADMIN_WEB_UI_NO_ANTD_DRAWER_DIRECT`：页面和共享组件禁止直接从 `antd` 导入 `Drawer`；统一使用 `src/components/kuzhambu-drawer/` 暴露的 `KuzhambuDrawer`。`KuzhambuDrawer` 实现文件自身作为唯一例外。
- `ADMIN_WEB_UI_TABLE_ACTION_COLUMN`：表格操作列使用 `key: "actions"`，优先传 `options`；`render` 只作为复杂逃生口。
- `ADMIN_WEB_UI_INTERACTIVE_ACCESSIBLE_NAME`：可机器判断的业务交互控件必须有稳定可访问名称。当前门禁覆盖无可见文本的 `Button`、`Input.Search`、`Table` 和 `KuzhambuTable`；名称来自可见文本、`aria-label` 或 `aria-labelledby`。

### Forbidden Defaults

- `ADMIN_WEB_FORBID_BOUNDARYLESS_DIR`：`src` 下禁止无边界 `common`、`base`、`shared` 子目录；`src/pages/<module>/common` 作为模块内共享边界例外。
- `ADMIN_WEB_FORBID_EXTRA_SYSTEM`：禁止新增第二套路由、请求、权限、状态或样式体系目录。
- `ADMIN_WEB_FORBID_BACKEND_LAYER_DIR`：前端目录禁止 `controller`、`dao`、`mapper`、`repository`。
- `ADMIN_WEB_FORBID_STYLE_SYSTEM`：禁止 CSS module、styled-components、Tailwind。
- `ADMIN_WEB_FORBID_BUCKET_DIR`：`src` 下禁止 `utils`、`models`、`stores` 兜底目录。

## Review Rules

### Architecture

- 复杂业务逻辑不得直接写在 JSX 中。
- 页面应优先复用项目已有共享组件和页面骨架。

### Naming

- 前端自有按钮、菜单项和确认弹窗文案应表达具体动作，例如 `重置密码`、`移除头像`、`刷新密钥`；避免只写 `操作`、`变更状态`、`处理`。
- 搜索框 placeholder 应表达可搜索对象，避免只写 `搜索` 或 `请输入`。
- 页面状态变量命名贴近 UI 含义，例如 `query`、`selectedRowKeys`、`editingDictionary`。

### Placement

- 路由、登录态、权限、请求 hook、布局行为和关键页面加载行为优先覆盖在 `src/app.test.tsx`。
- 页面交互复杂度明显上升时，可以新增同目录或测试目录下的聚焦测试。

### UI

- Hard Rule 暂未覆盖的业务控件也应具备稳定的可访问名称。优先使用可见文本；无稳定可见文本时使用 `aria-label` 或 `aria-labelledby`，确保 Playwright 可以通过 `getByRole(..., { name })` 定位。
- 不得为了测试覆盖原生控件语义 role。`button`、`input`、`select`、`table` 等原生或 Ant Design 已提供语义的控件保留其默认 role，只补充稳定名称。
- `KuzhambuListPage` 这类共享业务组件必须尽量用 `subjectName` 生成搜索框和表格的默认可访问名称，页面只在默认文案不准确时覆盖。

#### Page Layout

后台业务页面默认遵循以下信息顺序：

```text
Page
  PageHeader
  SearchAndFilter
  Toolbar
  Content
  Footer
```

禁止：

- 分页放在页面顶部。
- 筛选控件放在表格主体中。
- 页面区块顺序随机。
- 同一页面出现多个主操作。

#### PageHeader

`PageHeader` 默认结构：

```text
Left:
  Title
  Description

Right:
  SecondaryActions
  PrimaryAction
```

规则：

- 最多 1 个主按钮。
- 最多 2 个次级按钮。
- 更多操作收敛到下拉菜单。
- 列表页优先使用 `KuzhambuListPage` 组织右侧操作；页面不要绕过骨架塞入多个主操作。

#### Toolbar

`Toolbar` 默认结构：

```text
Left:
  SelectedState
  BatchActions

Right:
  PageActions
```

批量操作规则：

- 未选中数据时禁用。
- 危险操作必须二次确认。
- 批量删除必须放在最后。

#### DataTable

表格列默认顺序：

```text
Identifier
BusinessFields
Status
Time
Actions
```

示例：

```text
名称 | 类型 | 状态 | 创建人 | 更新时间 | 操作
```

#### Action Column

操作默认顺序：

```text
查看
编辑
复制
启用 / 禁用
删除
```

删除操作必须：

- 放在最后。
- 使用危险样式。
- 要求二次确认。

页面只判断操作语义、顺序、危险级别和是否需要复杂 `render`；展示数量、分隔线、列宽、固定列、移动端下拉和排序拖动入口交给 `KuzhambuTable`。

#### Form

业务表单默认使用 Drawer 或 Page，避免使用 Modal 承载业务录入。

表单按钮默认位于右下角，顺序固定为：

```text
取消 | 保存
```

删除按钮必须：

- 与保存按钮分离。
- 使用危险样式。
- 要求二次确认。

禁止：

```text
删除 | 保存
```

表单 label 必须使用用户可读业务文案。

禁止：

```text
userName
phone
effectiveAt
```

推荐：

```text
用户名称
手机号
生效时间
```

校验消息必须说明具体字段或失败原因。

禁止：

```text
参数错误
必填
```

推荐：

```text
请输入用户名称
请选择角色
结束时间不能早于开始时间
```

校验行为：

- 失焦或提交时校验。
- 提交失败时滚动到第一个错误字段。
- 异步校验必须 debounce `500ms`。

#### Drawer And Modal

默认用途：

| Type | Usage |
| --- | --- |
| Drawer | 业务表单、详情、复杂关系 |
| Modal | 确认、轻量反馈 |

底部按钮顺序：

```text
取消 | 确认
取消 | 保存
取消 | 删除
```

#### Empty State

空状态必须包含：

- 空状态说明。
- 推荐下一步操作。

示例：

```text
暂无文章
新建第一篇文章
```

#### Error State

页面错误状态必须包含：

- 错误说明。
- 恢复操作。

示例：

```text
页面加载失败
重试
```

禁止：

- 白屏。
- 静默失败。

#### Loading

表格必须使用 loading 状态：

```tsx
<Table loading={loading} />
```

提交按钮必须使用 loading 状态：

```tsx
<Button loading={submitting}>保存</Button>
```

禁止：

- 重复提交。
- 无加载反馈。

#### Upload

上传入口必须展示：

- 支持格式。
- 文件大小限制。
- 文件数量限制。

示例：

```text
JPG / PNG
最大 5MB
最多 9 个文件
```

上传过程必须表达以下状态：

```text
上传中
上传成功
上传失败
重试
删除
```

#### Toast And Message

成功提示格式：

```text
动作 + 成功
```

示例：

```text
保存成功
删除成功
```

失败提示格式：

```text
动作 + 失败 + 原因
```

示例：

```text
删除失败，存在关联订单
```

禁止：

```text
操作失败
系统错误
```

### Permission

- 因当前业务状态不可执行的操作默认禁用，并说明禁用原因。
- 权限字符串优先集中在页面或专门 helper 中，不在多个无关组件中重复散落。

### Testing

- Playwright locator 优先级固定为：

```text
getByRole(..., { name })
getByRole
getByLabel
getByText
getByTestId
CSS Selector
```

- Playwright 测试业务控件时优先使用 `getByRole(..., { name })`，控件名称来自稳定可见文本、`aria-label` 或 `aria-labelledby`。
- Playwright 测试禁止使用 `waitForTimeout`。
- Playwright 测试禁止使用复杂 CSS selector。
- E2E 测试之间不得依赖共享状态。
- 避免过长 E2E 流程。
- 测试应验证用户可见结果和关键请求契约，不验证 Ant Design 内部 DOM 细节。

默认覆盖重点：

- 登录。
- 权限。
- CRUD。
- 搜索。
- 分页。

示例：

```ts
test("delete requires confirmation", async ({ page }) => {
    await page.goto("/users");

    await page.getByRole("button", { name: "删除" }).click();

    await expect(page.getByText("确认删除")).toBeVisible();
});
```

## Code Review Checklist

### Table

- [ ] 操作列在需要时固定右侧。
- [ ] 删除操作放在最后。
- [ ] 操作数量符合收敛规则。
- [ ] 存在 loading 状态。
- [ ] 存在空状态。
- [ ] 存在错误状态。

### Form

- [ ] 校验消息明确。
- [ ] 提交失败后滚动到错误字段。
- [ ] 防止重复提交。
- [ ] 按钮顺序正确。

### General

- [ ] 使用已有共享组件。
- [ ] 无复杂 JSX。
- [ ] 目录结构正确。

## Open Items

- 是否将 Drawer footer 继续沉淀为共享组件默认行为或测试门禁。
- 是否为列表页操作顺序、Table actions、Confirm hook 补充测试门禁。
- 是否固定详情页结构为 `PageHeader`、`Summary`、`BasicInfo`、`BusinessInfo`、`Timeline`、`Logs`。
- Playwright 默认覆盖重点是否包含上传和核心业务流程。
