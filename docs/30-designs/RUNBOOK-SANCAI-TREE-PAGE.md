# RUNBOOK Sancai Tree Page

## 目标

将 `/classics/sancai` 改造成树形目录页面：

- 左侧为 `Category -> Volume -> Entry` 树。
- 右侧列表随树节点类型切换。
- `Content` 不进入树，只在选中 `Entry` 后按 `entryId` 查询。
- 顶部新增按钮、搜索和筛选表单随当前右侧列表类型同步变化。
- CRUD 操作发生在对应 `Panel`，不下沉到 `List` 或 `Model`。

本文档是临时执行手册。任务关闭时删除。

## 核心职责

### SancaiPage

`SancaiPage` 只负责页面级编排：

- 并发查询 `Category`、`Volume`、`Entry` 三个列表。
- 按 `categoryId`、`volumeId` 装配左侧树数据。
- 维护统一选中节点：`category`、`volume`、`entry`。
- 维护树展开节点。
- 根据当前选中节点决定右侧展示哪个 `Panel`。
- 把当前上下文传给 `KuzhambuListPage` 的 `addText`、`filterFields`、`searchPlaceholder`、`pageActions`。
- 协调树点击和右侧列表项点击的联动。

`SancaiPage` 不执行 CRUD mutation。

### SancaiCatalogTreePanel

`SancaiCatalogTreePanel` 只负责树展示和选择事件：

- 输入树节点数据、选中 key、展开 key。
- 渲染 `Category -> Volume -> Entry`。
- 点击节点时触发 `onSelectNode(node)`。
- 展开折叠时触发 `onExpandedKeysChange(keys)`。

`SancaiCatalogTreePanel` 不 import service，不执行 CRUD。

### Sancai*Panel

`SancaiCategoryPanel`、`SancaiVolumePanel`、`SancaiEntryPanel`、`SancaiContentPanel` 是各自领域的业务边界：

- 拥有本域 query/mutation。
- 打开和关闭本域 `Model`。
- 接收 `Model` 的 submit values。
- 调用本域 service 的 `add/update/deleteById/sort/list`。
- 负责 invalidate/refetch 本域相关 query。
- 负责删除当前选中项后的回落策略。
- 把列表项选择事件回传给 `SancaiPage`。

CRUD 操作发生在 `Panel`。

### Sancai*List

`SancaiCategoryList`、`SancaiVolumeList`、`SancaiEntryList`、`SancaiContentList` 只负责展示：

- 输入 records、selectedId、loading。
- 渲染表格或列表。
- 触发 `onSelect(record)`。
- 触发 `onEdit(record)`。
- 触发 `onDelete(record)`。
- 触发 `onSort(...)` 或排序 UI 事件。

`List` 不 import service，不执行 mutation，不 invalidate query。

### Sancai*Model

`SancaiCategoryModel`、`SancaiVolumeModel`、`SancaiEntryModel`、`SancaiContentModel` 只负责表单：

- 输入 open 状态、initialValues、提交中状态、选项数据。
- 管理表单本地状态。
- 做基础表单校验。
- 点击提交时触发 `onSubmit(values)`。
- 点击取消时触发 `onCancel()`。

`Model` 不 import service，不执行 mutation，不 invalidate query。

## Service 拆分

Service 按功能域拆文件。函数名不重复领域名。

### sancai-category-service.ts

- `list(request?)`
- `add(command)`
- `update(command)`
- `deleteById(id)`
- `sort(command)`
- `listTypes()`

### sancai-volume-service.ts

- `list(request?)`
- `add(command)`
- `update(command)`
- `deleteById(id)`
- `sort(command)`
- `listTypes()`

### sancai-entry-service.ts

- `list(request?)`
- `add(command)`
- `update(command)`
- `deleteById(id)`
- `sort(command)`

### sancai-content-service.ts

- `listByEntry(entryId, request?)`
- `add(command)`
- `update(command)`
- `deleteById(id)`
- `sort(command)`

## 后端接口

### 已有接口继续保留

- `POST /api/classics/sancai/categories/list`
- `POST /api/classics/sancai/categories/add`
- `POST /api/classics/sancai/categories/update`
- `POST /api/classics/sancai/categories/delete`
- `POST /api/classics/sancai/categories/sort`
- `GET /api/classics/sancai/categories/types`
- `POST /api/classics/sancai/volumes/list`
- `POST /api/classics/sancai/volumes/add`
- `POST /api/classics/sancai/volumes/update`
- `POST /api/classics/sancai/volumes/delete`
- `POST /api/classics/sancai/volumes/sort`
- `GET /api/classics/sancai/volumes/types`
- `POST /api/classics/sancai/entries/page`
- `POST /api/classics/sancai/entries/add`
- `POST /api/classics/sancai/entries/update`

### 需要补齐的接口

- `POST /api/classics/sancai/entries/list`
- `POST /api/classics/sancai/entries/delete`
- `POST /api/classics/sancai/entries/sort`
- `POST /api/classics/sancai/contents/list`
- `POST /api/classics/sancai/contents/add`
- `POST /api/classics/sancai/contents/update`
- `POST /api/classics/sancai/contents/delete`
- `POST /api/classics/sancai/contents/sort`

接口命名必须明确，不新增或使用 `save`。

## 树装配

前端分别请求三个列表：

- `categoryService.list()`
- `volumeService.list()`
- `entryService.list()`

装配规则：

- `Category` 节点 key：`category:{id}`
- `Volume` 节点 key：`volume:{id}`
- `Entry` 节点 key：`entry:{id}`
- `Volume` 通过 `categoryId` 归属到 `Category`。
- `Entry` 通过 `volumeId` 归属到 `Volume`。
- 找不到有效父节点的数据不进入树，并在开发期保留可调试的过滤逻辑。

## 右侧列表切换

当前选中节点决定右侧 `Panel`：

- 无节点或 root：`SancaiCategoryPanel`
- `category:{id}`：`SancaiVolumePanel`
- `volume:{id}`：`SancaiEntryPanel`
- `entry:{id}`：`SancaiContentPanel`

右侧列表项点击必须调用 `SancaiPage` 的统一选择函数，使树同步展开和选中。

## 顶部工具栏

三才页面应使用 `KuzhambuListPage`：

- `tableAside`：`SancaiCatalogTreePanel`
- `enableSearch`：由当前 `Panel` 配置决定。
- `enableFilter`：由当前 `Panel` 配置决定。
- `enableAdd`：由当前 `Panel` 配置决定。
- `addText`：由当前 `Panel` 配置决定。
- `searchPlaceholder`：由当前 `Panel` 配置决定。
- `filterFields`：由当前 `Panel` 配置决定。
- `pageActions`：刷新、排序等非新增操作。

上下文切换时，必须清理不适用于当前列表类型的筛选条件。

## Panel 配置

每个 `Panel` 输出页面工具栏配置：

```ts
interface SancaiPanelToolbarConfig {
    addText?: string;
    enableAdd: boolean;
    enableFilter: boolean;
    enableSearch: boolean;
    filterFields: KuzhambuListPageFilterField[];
    searchPlaceholder?: string;
}
```

`SancaiPage` 读取当前 `Panel` 对应配置并传给 `KuzhambuListPage`。

## 分步执行

### 1. 后端 Entry 列表接口

- 补齐 `entries/list`。
- Controller 方法名使用白名单允许的明确动作。
- 测试覆盖请求路径、返回结构和无 `save` 命名。

### 2. Service 按域拆分

- 拆出 `sancai-category-service.ts`。
- 拆出 `sancai-volume-service.ts`。
- 拆出 `sancai-entry-service.ts`。
- 保持函数名为 `list/add/update/deleteById/sort/listTypes`。
- 更新 service contract 测试。

### 3. List/Model 纯化

- `SancaiCategoryList`、`SancaiVolumeList`、`SancaiEntryList` 只保留展示和事件。
- `SancaiCategoryModel`、`SancaiVolumeModel`、`SancaiEntryModel` 只保留表单和 `onSubmit`。
- 移除 `Model` 中的 service、mutation 和 invalidate。

### 4. Panel 接管 CRUD

- `SancaiCategoryPanel` 接管 category query/mutation。
- `SancaiVolumePanel` 接管 volume query/mutation。
- `SancaiEntryPanel` 接管 entry query/mutation。
- 删除和排序 mutation 放在对应 `Panel`。
- 新增和修改由 `Panel` 接收 `Model` submit 后调用 service。

### 5. Tree 导航

- 新增 `SancaiCatalogTreePanel`。
- `SancaiPage` 并发加载三列表并装配树。
- 实现树点击更新当前选中节点。
- 实现默认选中第一个有效 `Category`。

### 6. 右侧 Panel 切换

- 根据选中节点类型渲染对应 `Panel`。
- 右侧列表项点击反选树节点。
- 删除当前选中节点后回落到最近有效节点。

### 7. 工具栏同步

- 三才页面接入 `KuzhambuListPage`。
- 新增按钮、搜索、筛选随当前 `Panel` 配置变化。
- 切换上下文时清理不适用筛选条件。

### 8. Content 接入

- 补齐 content 后端接口和前端 service。
- 新增 `SancaiContentPanel/List/Model`。
- 仅选中 `Entry` 时请求 Content。

### 9. 清理和收口

- 删除旧双列目录样式。
- 删除旧 service 聚合函数。
- 删除完成的 TODO。
- 删除本 RUNBOOK。

## 验证

前端每步至少运行：

```sh
cd kuzhambu-apps
npm --workspace admin-web run lint
npm --workspace admin-web run test -- src/pages/classics/sancai/sancai-service-contract.test.ts src/pages/classics/sancai/sancai-page.test.tsx
```

涉及构建或页面装配收口时运行：

```sh
cd kuzhambu-apps
npm --workspace admin-web run build
```

后端接口变更运行：

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-interface -am spotless:check checkstyle:check test
```
