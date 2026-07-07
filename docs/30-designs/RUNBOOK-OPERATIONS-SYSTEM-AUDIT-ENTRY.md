# Operations System 日志审计入口闭环 RUNBOOK

## 目标

在 admin Operations「运维入口」补齐 System 日志和业务审计入口，让管理员能从 Operations 控制台直接跳转到 System 提供的系统日志与审计日志页面，并且入口展示、路由跳转、权限可见性和测试验证形成闭环。

## 边界

- Operations 只提供入口和聚合视图，不成为系统日志或业务审计真相源。
- System 继续拥有系统日志、业务审计、权限编码和当前认证上下文。
- 本任务不新增 `operations_*` 表，不复制 `system_*` 日志或审计正文。
- 本任务不改 root `README.md` 作为实现依据。

## 相关文件

修改既有文件：

- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`
- `kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`
- `kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/audit/audit-log/audit-log-page.tsx`
- `kuzhambu-apps/admin-web/e2e/system/logs/logs.spec.ts`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/LogController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemLogContractTest.java`

新增文件：

- `kuzhambu-apps/admin-web/e2e/operations/dashboard/dashboard.spec.ts`

不修改文件：

- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service.ts`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-types.ts`
- `kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-service.ts`
- `kuzhambu-apps/admin-web/src/pages/audit/audit-log/audit-log-service.ts`
- `kuzhambu-apps/admin-web/src/router/index.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.css`，除非新增空状态导致现有样式无法承载。
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditController.java`

## 数据结构变更

### 后端数据结构

无后端表结构、接口响应结构或 Operations 台账结构变更。

不得新增或修改：

- `operations_*` 表。
- `system_*` 日志或审计表。
- `/operations/*` 接口。
- `/sys/log/page` 接口字段。
- `/audit/log/*` 接口字段。

仅允许修改系统日志分页接口权限注解：

| 文件 | 方法 | 当前值 | 目标值 |
| --- | --- | --- | --- |
| `LogController.java` | `page(LogPageRequest request)` | `@HasPermission(value = "super")` | `@HasPermission(value = "system:log:view")` |
| `LogController.java` | `page(LogPageRequest request)` | `@Operation(description = "super")` | `@Operation(description = "system:log:view")` |

审计接口权限保持不变：

| 文件 | 接口权限 |
| --- | --- |
| `AuditController.java` | `@HasPermission(value = "audit:view")` |

### Service 数据结构

无 service 请求或响应结构变更。

不得修改：

| 文件 | 不变字段 |
| --- | --- |
| `system-log-service.ts` | `LogPageQuery.pageNo`、`pageSize`、`title`、`userLoginName`、`userName`、`remoteAddr`、`requestUri`、`beginDate`、`endDate` |
| `audit-log-service.ts` | `AuditLogPageQuery` 既有字段、`getAuditLogDetail(id)` 入参、`getAuditOptions()` 入参 |
| `dashboard-service.ts` | Operations dashboard、health trend、health alert 既有请求和响应字段 |

### 前端入口结构

在 `dashboard-page.tsx` 中将运维入口定义收敛为页面内局部类型，不导出到其他模块：

```ts
interface OperationEntry {
    description: string;
    icon: ReactNode;
    permission: string;
    testId: string;
    title: string;
    to: string;
}
```

字段要求：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `title` | `string` | 是 | 入口卡片主标题，作为可见文本和测试定位文本 |
| `description` | `string` | 是 | 入口卡片说明文案 |
| `to` | `string` | 是 | 目标路由，只允许站内既有路由 |
| `permission` | `string` | 是 | 跳转前必须检查的 `*:view` 权限 |
| `icon` | `ReactNode` | 是 | 入口卡片图标 |
| `testId` | `string` | 是 | E2E 稳定定位属性值，渲染到入口卡片 `data-testid` |

所有入口必须声明 `permission`，不得存在无权限字段的默认可见入口。

入口字段最终值：

| `title` | `description` | `to` | `permission` | `testId` |
| --- | --- | --- | --- | --- |
| `任务台账` | `查看所有长任务、筛选执行状态并打开任务详情` | `/operations/tasks` | `operations:task:view` | `operations-entry-tasks` |
| `备份恢复` | `查看备份、恢复记录并发起手动备份` | `/operations/backup-restore` | `operations:backup:view` | `operations-entry-backup-restore` |
| `清理维护` | `查看清理任务、失败项并触发维护清理` | `/operations/cleanup` | `operations:cleanup:view` | `operations-entry-cleanup` |
| `系统日志` | `查看 System 提供的系统运行与访问日志` | `/system/logs` | `system:log:view` | `operations-entry-system-log` |
| `审计日志` | `查看业务对象变更审计与操作者追踪` | `/audit/logs` | `audit:view` | `operations-entry-audit-log` |

## 权限闭环

最终入口必须按目标页权限控制可见性：

| 入口 | 路由 | 所需权限 | 数据来源 |
| --- | --- | --- | --- |
| 系统日志 | `/system/logs` | `system:log:view` | System 日志页面与接口 |
| 审计日志 | `/audit/logs` | `audit:view` | System 审计页面与接口 |

实现要求：

- `operations:dashboard:view` 只控制 Operations 看板本身可见，不替代日志和审计入口权限。
- `system:log:view` 是系统日志查看权限的最终口径；前端入口、目标页请求门禁、E2E 夹具和后端 `LogController.page` 权限注解必须一致。
- 页面跳转前必须检查目标入口的 `*:view` 权限；检查失败时不得渲染可点击入口，也不得通过点击事件发起 `navigate`。
- 没有 `system:log:view` 时，不展示「系统日志」入口，不允许跳转到 `/system/logs`。
- 没有 `audit:view` 时，不展示「审计日志」入口，不允许跳转到 `/audit/logs`。
- 两项权限都没有时，运维入口区域仍展示当前用户有权限的其他 Operations 入口；如全部入口不可见，展示明确空状态。
- 入口只跳转到既有页面，不直接调用 `/sys/log/*` 或 `/audit/log/*` 服务。
- 目标页自身的数据请求也必须受同一权限约束；入口隐藏不能作为唯一保护。

## 任务拆分

### 任务 1：Operations 入口控件

范围文件：

- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`

前端控件和操作：

- 控件：Operations 看板「运维入口」区域。
- 控件：入口卡片 `Link`。
- 操作：用户点击「系统日志」卡片。
- 操作：用户点击「审计日志」卡片。
- 操作：用户缺少目标 `*:view` 权限时，入口卡片不出现，页面无可点击跳转控件。

实现要求：

1. 在 `dashboard-page.tsx` 将 `operationEntries` 从静态数组调整为带 `permission` 的入口定义，保留现有任务台账、备份恢复和清理维护入口。
2. 为「系统日志」新增入口卡片，标题为 `系统日志`，描述为 `查看 System 提供的系统运行与访问日志`，目标为 `/system/logs`，权限为 `system:log:view`。
3. 为「审计日志」新增入口卡片，标题为 `审计日志`，描述为 `查看业务对象变更审计与操作者追踪`，目标为 `/audit/logs`，权限为 `audit:view`。
4. 现有任务台账、备份恢复、清理维护入口也必须补齐对应 `*:view` 权限：
   - `任务台账`：`operations:task:view`
   - `备份恢复`：`operations:backup:view`
   - `清理维护`：`operations:cleanup:view`
5. 渲染入口前按 `hasPermission(entry.permission)` 过滤。
6. 每张入口卡片使用 `Link`，`to` 取 `entry.to`，卡片根节点带 `data-testid={entry.testId}`。
7. 不新增按钮点击处理器，不调用 `navigate`，不新增 Operations service、接口或聚合接口。
8. 过滤后入口列表为空时，在「运维入口」区域显示空状态文案 `暂无可访问的运维入口`。

测试要求：

- 拥有 `operations:dashboard:view`、`system:log:view`、`audit:view` 时，展示「系统日志」和「审计日志」卡片。
- 缺少 `system:log:view` 时，不展示「系统日志」卡片。
- 缺少 `audit:view` 时，不展示「审计日志」卡片。
- 只拥有 `operations:dashboard:view` 且没有任何入口 `*:view` 权限时，展示 `暂无可访问的运维入口`。
- 点击「系统日志」卡片后，路由进入 `/system/logs`。
- 点击「审计日志」卡片后，路由进入 `/audit/logs`。

### 任务 2：System 目标页权限门禁

范围文件：

- `kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/audit/audit-log/audit-log-page.tsx`
- `kuzhambu-apps/admin-web/e2e/system/logs/logs.spec.ts`

前端控件和操作：

- 控件：系统日志页面根内容。
- 控件：系统日志搜索框。
- 控件：系统日志筛选项。
- 控件：系统日志刷新按钮。
- 控件：系统日志表格。
- 控件：审计日志页面根内容。
- 控件：审计日志搜索框。
- 控件：审计日志筛选项。
- 控件：审计日志刷新按钮。
- 控件：审计日志表格。
- 控件：审计详情 Drawer。
- 操作：无 `system:log:view` 权限访问 `/system/logs`。
- 操作：有 `system:log:view` 权限访问 `/system/logs` 并搜索、筛选、刷新。
- 操作：无 `audit:view` 权限访问 `/audit/logs`。
- 操作：有 `audit:view` 权限访问 `/audit/logs` 并搜索、筛选、刷新、打开审计详情 Drawer。

实现要求：

1. 在 `system-log-page.tsx` 使用 `hasPermission("system:log:view")` 判断页面查看权限。
2. 无权限时展示明确空状态文案 `缺少 system:log:view 权限`。
3. 无权限时 `useQuery` 必须设置 `enabled: false`，不得请求 `/sys/log/page`。
4. 有权限时保持既有搜索、筛选、刷新、分页和表格行为不变。
5. 在 `audit-log-page.tsx` 使用 `hasPermission("audit:view")` 判断页面查看权限。
6. 无权限时展示明确空状态文案 `缺少 audit:view 权限`。
7. 无权限时审计选项、审计分页和审计详情 `useQuery` 必须设置 `enabled: false`，不得请求 `/audit/log/options`、`/audit/log/page` 或 `/audit/log/detail`。
8. 有权限时保持既有搜索、筛选、刷新、分页、查看详情 Drawer 行为不变。

测试要求：

- E2E 权限夹具使用 `system:log:view` 验证 `/system/logs` 可访问。
- 缺少 `system:log:view` 时访问 `/system/logs`，页面展示 `缺少 system:log:view 权限`。
- 缺少 `system:log:view` 时，断言不会发起 `/sys/log/page` 请求。
- 保留现有系统日志搜索、筛选、刷新行为断言。
- E2E 权限夹具使用 `audit:view` 验证 `/audit/logs` 可访问。
- 缺少 `audit:view` 时访问 `/audit/logs`，页面展示 `缺少 audit:view 权限`。
- 缺少 `audit:view` 时，断言不会发起 `/audit/log/options`、`/audit/log/page` 或 `/audit/log/detail` 请求。
- 保留现有审计日志搜索、筛选、刷新、查看详情行为断言。

### 任务 3：菜单图标和 E2E 闭环

范围文件：

- `kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`
- `kuzhambu-apps/admin-web/e2e/operations/dashboard/dashboard.spec.ts`
- `kuzhambu-apps/admin-web/e2e/system/logs/logs.spec.ts`

前端控件和操作：

- 控件：左侧菜单 System 日志菜单项。
- 控件：左侧菜单审计日志菜单项。
- 控件：Operations 看板「运维入口」区域。
- 操作：从 Operations 看板点击入口后到达目标页 heading。
- 操作：菜单 icon key 渲染为真实图标，不出现 `menu-icon-config-error`。

实现要求：

1. 在 `admin-layout.tsx` 覆盖 `system-log`、`audit-log`、`operations`、`operations-dashboard`、`operations-task`、`operations-backup-restore`、`operations-cleanup` 图标 key。
2. 不新增路由；继续使用既有 `/system/logs` 与 `/audit/logs`。
3. 新增 `kuzhambu-apps/admin-web/e2e/operations/dashboard/dashboard.spec.ts`。
4. Operations 看板 E2E 使用包含 `operations:dashboard:view`、`system:log:view`、`audit:view` 的权限和菜单夹具。
5. Operations 看板 E2E 验证「系统日志」「审计日志」入口可见并可跳转到目标页 heading。
6. Operations 看板 E2E 使用只包含 `operations:dashboard:view` 的权限夹具，验证「系统日志」「审计日志」入口不可见。

### 任务 4：系统日志后端权限收敛

范围文件：

- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/LogController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemLogContractTest.java`

接口和操作：

- 接口：`POST /api/sys/log/page`。
- 操作：有 `system:log:view` 权限的管理员请求系统日志分页。
- 操作：无 `system:log:view` 权限的管理员请求系统日志分页。

实现要求：

1. 在 `LogController.page` 将 `@HasPermission(value = "super")` 改为 `@HasPermission(value = "system:log:view")`。
2. 在 `LogController.page` 将 `@Operation(description = "super")` 改为 `@Operation(description = "system:log:view")`。
3. 不修改 `LogPageRequest` 字段。
4. 不修改 `LogResponse`、`LogUserResponse`、`LogDepartmentResponse` 字段。
5. 不修改 `LogApplicationService`、`LogInterfaceAssembler` 或 System 日志持久化实现。

测试要求：

- 在 `SystemLogContractTest` 增加断言，确认 `LogController.page` 的 `@HasPermission` 值为 `system:log:view`。
- 保留既有 `/api/sys/log` 路由、`page` 路径和 `LogPageRequest` JSON 字段断言。
- 保留审计接口 `audit:view` 既有契约断言。

## 验证命令

前端改动后先运行窄范围格式化，再运行门禁：

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run format
npm run format:check
npm run lint
npm run test
```

如只改 admin-web 并需要更窄验证，可先运行相关测试文件：

```sh
cd kuzhambu-apps
npm --workspace kuzhambu-admin-web run test -- dashboard-page.test.tsx
npm --workspace kuzhambu-admin-web run test -- app.test.tsx
```

涉及 `LogController.java` 后运行 Java 窄范围校验：

```sh
cd kuzhambu-servers
mvn -pl biz/system/kuzhambu-system-interface spotless:apply
mvn -pl biz/system/kuzhambu-system-interface -am -Dtest=SystemLogContractTest test
```

## 验收口径

- 管理员从 Operations 看板能看到与自身权限匹配的「系统日志」和「审计日志」入口。
- 点击入口分别进入既有 System 日志页和审计日志页。
- 未授权管理员看不到对应入口，也不会因为入口跳转绕过目标页权限。
- Operations 没有新增日志或审计数据源，没有复制日志或审计正文。
- 测试覆盖入口展示、权限隐藏和跳转路径。
