# Admin Web UI 规范候选版

## 1. 定位

本文定义 `kuzhambu-apps/admin-web` 的 UI 生成规范。未来稳定后，本文可替代 `docs/00-governance/UI-RULES.md`。

本文面向 AI、工程实现和设计评审。表达以规则为主，避免解释性散文。

## 2. 核心原则

- MUST 内容优先：页面先服务阅读、检索、筛选、比较、编辑、审核、追踪和批量操作。
- MUST 工具优先：admin-web 是工作台，不是官网、展示页或营销页。
- MUST 学术克制：使用古籍、纸面、墨色、印章等弱符号，但不得干扰业务内容。
- MUST 响应式重组：不同屏幕使用不同信息结构，不得简单缩放桌面页。
- MUST 数据一致：同一页面族的列表、详情、编辑、活动记录必须使用同一业务对象语义。
- MUST 可访问：交互控件必须有稳定可访问名称、可见焦点、键盘路径和足够对比。
- SHOULD 复用模式：同类页面使用同一布局、筛选、表格、表单、详情和反馈模式。
- NEVER 使用营销式 hero、装饰性卡片堆叠、无业务意义大图、强渐变、玻璃拟态、霓虹、暗黑科技风。

## 3. 设计依据

本文借鉴但不照搬：

- WCAG 2.2：对比度、焦点、目标尺寸、键盘可达。
- Material Design：断点、导航抽屉、数据表格。
- Apple HIG：触控目标、平台适配、导航层级。
- GOV.UK / MOJ：表单、过滤器、已选筛选项。
- IBM Carbon：数据表格、批量操作、过滤状态。
- Nielsen Norman Group：数据表格任务和企业系统可用性。
- Microsoft Fluent：布局、层级、一致性。

仓库内旧 `UI-RULES.md` 只保留核心精神：内容优先、学术克制、工具型体验。

## 4. Viewport 模型

MUST 使用四档 Viewport 模型。

| 档位 | 宽度 | 设备 | 布局策略 |
|---|---:|---|---|
| `phone` | `< 768px` | 手机 | 单列、卡片、抽屉、底部操作 |
| `pad` | `768px - 1023px` | PAD | 顶栏、抽屉菜单、轻表格/卡片、筛选摘要 |
| `laptop` | `1024px - 1439px` | 笔记本 | icon rail、优先级表格、滑出详情 |
| `pc` | `>= 1440px` | PC | 完整侧栏、宽表格、持久详情、多栏工作台 |

### 4.1 Viewport 行为

- `pc`: 完整表格 + 持久详情。
- `laptop`: 优先级表格 + 滑出详情。
- `pad`: 轻表格或卡片列表 + 抽屉详情。
- `phone`: 卡片列表 + 全屏详情或底部详情。

### 4.2 规则

- MUST 设计稿、实现和验收都标明所属 Viewport。
- MUST 同一页面族覆盖四档 Viewport。
- MUST 不同 Viewport 允许改变信息结构。
- SHOULD 使用统一断点口径。
- NEVER 将桌面布局等比缩小为手机布局。

## 5. 全局布局

### 5.1 PC

```text
Sidebar | Topbar
Sidebar | PageHeader
Sidebar | FilterBar
Sidebar | Toolbar
Sidebar | Content + Inspector
```

- MUST 使用完整侧栏。
- SHOULD 侧栏宽 `240px - 280px`。
- SHOULD 顶栏高 `56px - 64px`。
- SHOULD 页面外边距 `24px`。
- MAY 使用右侧持久详情面板，宽 `320px - 420px`。
- NEVER 将表格页限制在过窄最大宽度。

### 5.2 Laptop

```text
Rail | Topbar
Rail | PageHeader + Toolbar
Rail | Compact Content
Rail | Slide-over Detail
```

- MUST 优先保留主内容宽度。
- SHOULD 使用 `64px - 88px` icon rail。
- SHOULD 二级菜单通过 popover、flyout 或页面分组承载。
- MUST 使用滑出详情，不使用常驻宽详情栏。

### 5.3 PAD

```text
Topbar
PageHeader
Search
FilterSummary
Content
SheetDetail
```

- MUST 不常驻完整侧栏。
- SHOULD 使用抽屉菜单。
- SHOULD 内容为 1-2 列。
- SHOULD 复杂选择器进入 sheet。
- MUST 保证触控目标可用。

### 5.4 Phone

```text
AppBar
Search
FilterChips
PrimaryAction
CardList
Bottom/Sheet Detail
```

- MUST 单列。
- MUST 表格转业务卡片。
- MUST 详情/编辑使用全屏页、全屏抽屉或底部 sheet。
- SHOULD 主操作首屏可见。
- NEVER 在手机端硬塞桌面表格。

## 6. 菜单

### 6.1 一级菜单

默认一级菜单 SHOULD 按任务组织：

```text
仪表盘
用户中心
古籍管理
知识治理
搜索问答
AI 管理
运营运维
审计中心
系统设置
```

- MUST 使用业务词，不使用接口名、权限码、数据库表名。
- MUST 同一业务对象在菜单、页面标题、按钮中保持同名。
- SHOULD 将 `用户中心` 作为一等入口。
- SHOULD 将 `审计中心` 与 `运营运维` 分开。

### 6.2 菜单响应式

- `pc`: 完整侧栏，一级和二级菜单可展开。
- `laptop`: icon rail，二级菜单通过 flyout/popover。
- `pad`: 抽屉菜单，点击菜单项后关闭。
- `phone`: 抽屉菜单，宽度 `min(86vw, 340px)`。

## 7. 页面骨架

默认业务页面 MUST 使用以下顺序：

```text
PageHeader
SearchAndFilter
Toolbar
Content
Pagination/Footer
```

NEVER:

- 分页放顶部。
- 筛选控件放入表格主体。
- 同一页面出现多个主操作。
- 页面区块顺序随机。

### 7.1 PageHeader

```text
Left:  Title + Description
Right: SecondaryActions + PrimaryAction
```

- MUST 有明确页面标题。
- SHOULD 有一句业务描述。
- MUST 最多 1 个主按钮。
- SHOULD 最多 2 个次级按钮。
- MUST 更多操作收敛到菜单。

### 7.2 Toolbar

```text
Left:  SelectionState + BatchActions
Right: ViewOptions + Export + Refresh
```

- MUST 批量操作依赖选中状态。
- MUST 危险批量操作二次确认。
- SHOULD 导出、刷新、视图设置作为工具操作，不抢主按钮视觉。

## 8. 搜索与筛选

### 8.1 搜索

- MUST 搜索框说明可搜索对象。
- SHOULD 放在筛选区最左或移动端首个控件。
- SHOULD 支持清除。
- SHOULD 搜索词作为已选筛选条件的一部分。

示例：

```text
搜索用户名 / 姓名 / 邮箱 / 手机号
搜索操作内容、资源、结果
搜索古籍标题、卷册、标签
```

### 8.2 筛选分类

| 类型 | 示例 | 展示 |
|---|---|---|
| 高频 | 部门、状态、角色、时间 | 常驻 |
| 中频 | 来源、标签、创建人 | 高级筛选 |
| 低频 | 内部 ID、请求 ID、IP 段 | 高级筛选或专用搜索 |

### 8.3 已选筛选

- MUST 显示已选条件。
- MUST 支持单项移除。
- MUST 支持重置全部。
- SHOULD 显示结果数量。
- SHOULD 区分“无数据”和“筛选无结果”。

### 8.4 高级筛选

- `pc`: inline panel 或右侧 filter panel。
- `laptop`: popover 或 slide-over。
- `pad`: sheet。
- `phone`: sheet，底部固定 `重置` 和 `应用筛选`。

## 9. 数据表格与数据视图

### 9.1 任务

数据表格 MUST 支持：

- 找到记录。
- 比较记录。
- 查看、编辑或新增记录。
- 对单条或多条记录执行操作。

### 9.2 列顺序

默认列顺序：

```text
Selection
Identifier
BusinessFields
Status
Time
Actions
```

用户中心示例：

```text
选择 | 用户信息 | 所属机构 | 角色 | 状态 | 最近登录 | 操作
```

### 9.3 列优先级

每列 MUST 定义优先级。

| 优先级 | 含义 | 屏幕行为 |
|---|---|---|
| `primary` | 识别对象必需 | 所有屏幕保留 |
| `secondary` | 高频判断 | PC、laptop、pad 保留 |
| `tertiary` | 辅助判断 | PC、laptop 保留 |
| `detail` | 详情字段 | 详情/展开/卡片详情 |
| `hidden` | 低频字段 | 默认隐藏 |

### 9.4 PC 表格

- MUST 支持完整列。
- MUST 支持选择、分页、排序、批量操作。
- SHOULD 操作列固定右侧。
- MAY 第一业务列固定左侧。
- MAY 右侧持久详情面板。

### 9.5 Laptop 表格

- MUST 裁剪低优先级列。
- SHOULD 操作列收敛为更多菜单。
- MUST 详情进入 slide-over。

### 9.6 PAD 数据视图

- SHOULD 使用轻表格或卡片列表。
- MUST 只保留核心字段。
- SHOULD 批量操作进入选择模式。

### 9.7 Phone 数据视图

- MUST 使用卡片列表。
- MUST 卡片点击进入详情。
- SHOULD 更多按钮承载次要操作。
- NEVER 暴露危险操作为卡片主按钮。

用户卡片结构：

```text
ArchiveMarker
Avatar
Name + Status
LoginName
Department
RoleBadges
LastActivity
MoreAction
```

### 9.8 空状态

空状态 MUST 说明：

- 当前没有什么。
- 为什么可能没有。
- 用户可以做什么。

示例：

```text
暂无用户
当前部门没有符合筛选条件的用户。
[重置筛选] [新增用户]
```

NEVER 只显示 `暂无数据`。

## 10. 表单

### 10.1 分组

复杂表单 MUST 按业务意义分组。

用户编辑页示例：

```text
基本信息
部门选择
角色分配
安全设置
备注
```

- SHOULD 每组 `3 - 7` 个字段。
- MUST 高危字段单独分组。
- SHOULD 超过 7 个字段时拆组或分步。

### 10.2 布局

- `pc`: 2-3 列；树、权限、长文本占整行。
- `laptop`: 1-2 列；复杂选择器使用面板。
- `pad`: 1-2 列；触控目标加大。
- `phone`: 单列；分组卡片；底部固定保存。

### 10.3 字段

- MUST 有 label。
- MUST 必填项可见。
- MUST 错误靠近字段。
- MUST placeholder 不替代 label。
- SHOULD 禁用状态说明原因。

### 10.4 保存

- MUST 主保存按钮文案为具体动作，例如 `保存更改`。
- MUST 取消未保存改动时确认。
- MUST 保存成功后更新列表/详情数据。
- SHOULD 保存区在表单底部或 sticky footer，避免双处保存。

## 11. 部门选择

部门选择是用户中心的一等控件。

### 11.1 列表页

- `pc`: 左侧部门树或部门筛选面板。
- `laptop`: 部门面板可折叠。
- `pad`: 部门 chip + sheet 触发器。
- `phone`: 部门选择按钮位于搜索下方。

### 11.2 编辑页

- `pc`: 树面板或组合选择器。
- `laptop`: 搜索 + 树列表。
- `pad`: 搜索 + 树列表或 sheet。
- `phone`: 全屏 sheet 或底部 sheet。

### 11.3 数据规则

- MUST 列表筛选部门与详情部门字段使用同一部门路径模型。
- MUST 单主部门时控件表达单选。
- MUST 多部门时区分主部门和附属部门。
- SHOULD 部门路径显示为 `一级 / 二级 / 三级`。

## 12. 详情、编辑、活动记录

### 12.1 详情

详情用于解释列表选中对象。

- `pc`: 可持久显示右侧详情。
- `laptop`: slide-over。
- `pad`: drawer/sheet。
- `phone`: 独立页、全屏抽屉或底部 sheet。

详情 MUST 保留对象身份：

```text
Name
Identifier
Status
KeyMetadata
PrimaryActions
```

### 12.2 编辑

编辑页 MUST 保留对象身份。

用户编辑页 MUST 包含：

- 基本信息。
- 部门选择。
- 角色分配。
- 安全设置。
- 保存 / 取消。

### 12.3 活动记录

活动记录 MUST 回答：

- 什么时间。
- 谁操作。
- 对谁操作。
- 发生了什么。
- 结果是什么。
- 来源 IP / 设备 / 会话。

事件结构：

```text
TimelineMarker
ActionName
ResultBadge
Timestamp
Actor
Target
IP / Device
ExpandableDetail
```

- `pc/laptop`: 可使用表格 + 详情面板。
- `pad/phone`: 使用 timeline card。

## 13. 按钮与操作

### 13.1 操作层级

| 层级 | 用途 | 示例 |
|---|---|---|
| Primary | 页面主动作 | 新增用户、保存更改 |
| Secondary | 支持动作 | 刷新、导出、重置 |
| Tertiary | 行内低频动作 | 查看日志、复制 ID |
| Danger | 危险动作 | 删除、禁用、重置密码 |

- MUST 每页最多一个 primary。
- MUST 危险操作二次确认。
- SHOULD 行内操作顺序为 `查看 -> 编辑 -> 复制 -> 更多 -> 危险操作`。
- SHOULD 手机端行内操作收敛到更多菜单。

### 13.2 图标

- SHOULD 图标增强识别，不替代文本。
- MUST 无文本图标按钮有 `aria-label`。
- MUST 同一动作使用同一图标。
- SHOULD 状态图标配文字或 badge。

## 14. 状态与反馈

### 14.1 Loading

- SHOULD 首次加载使用 skeleton。
- SHOULD 局部刷新保留旧数据。
- MUST 长任务显示状态、进度或日志入口。

### 14.2 Success

- MUST 完成后反馈。
- MUST 文案表达具体结果。

示例：

```text
用户已更新
抽取任务已创建
筛选条件已重置
```

NEVER 使用泛化文案 `操作成功` 作为唯一反馈。

### 14.3 Error

错误 MUST 说明：

- 发生了什么。
- 用户能否重试。
- 下一步怎么做。

示例：

```text
保存失败：登录名已存在。请更换登录名后重试。
```

### 14.4 Empty

空状态 MUST 区分：

- 无权限。
- 无数据。
- 筛选无结果。
- 服务不可用。
- 功能未配置。

## 15. 视觉规范

### 15.1 色彩

基础色板：

| 角色 | 色值 |
|---|---|
| 纸面背景 | `#F7F3EA` |
| 主内容面 | `#FFFCF6` |
| 墨色文字 | `#20231F` |
| 次级文字 | `#70695E` |
| 边框 | `#D9D0C1` |
| 主色 / 玉色 | `#2F6B57` |
| 印章 / 危险 | `#B84A3A` |
| 辅助蓝灰 | `#526E7E` |
| 警示琥珀 | `#B9873A` |

- MUST 主色用于主操作、选中状态、关键进度。
- MUST 印章红用于品牌、危险、重点标记。
- SHOULD 状态使用 badge，不用整行大面积染色。

### 15.2 字体

- SHOULD 页面标题使用书卷气中文字体。
- MUST 表格、表单、控件、正文使用清晰 UI 字体。
- SHOULD ID、时间、IP、数值使用等宽数字或 monospace。
- MUST 手机正文不小于 `14px`。
- NEVER 使用负 letter spacing。

### 15.3 间距

基础间距：

```text
4 / 8 / 12 / 16 / 20 / 24 / 32
```

- SHOULD PC 页面边距 `24px`。
- SHOULD laptop 页面边距 `20px`。
- SHOULD pad/phone 页面边距 `16px`。
- SHOULD 卡片内边距 `12px - 20px`。

### 15.4 圆角与阴影

- SHOULD 工作台卡片圆角不超过 `8px`。
- MAY 移动端容器圆角到 `12px`。
- SHOULD 阴影只用于浮层、详情面板、活动卡片。
- NEVER 层层嵌套卡片。

## 16. 可访问性

- MUST 交互控件有稳定可访问名称。
- MUST 支持键盘导航。
- MUST 焦点可见。
- MUST 弹窗/抽屉打开后焦点进入容器，关闭后回到触发控件。
- MUST 状态不只依赖颜色表达。
- SHOULD 手机/PAD 主要点击目标不小于 `44px` 高。
- SHOULD 紧凑图标按钮保留足够点击区域。

## 17. 页面类型

### 17.1 CRUD 管理页

适用：用户、角色、部门、菜单、字典、AI 模型、AI 服务。

结构：

```text
Header
Search + Filters
BatchToolbar
DataView
Detail / Edit
```

MUST 支持：

- 搜索。
- 筛选。
- 批量操作。
- 详情/编辑。
- 空状态。
- 四档响应式。

### 17.2 日志 / 审计页

适用：审计日志、系统日志、用户活动、AI 调用记录。

结构：

```text
Header
TimeRange + EventType + Result + Actor
Timeline/Table
EventDetail
```

- MUST 时间作为首要筛选。
- MUST 事件结果可见。
- SHOULD 事件详情可复制 ID / IP / 会话。

### 17.3 知识治理工作台

适用：图谱抽取、图谱结果、来源谱系、质量报告、知识精修。

PC 结构：

```text
Source / Version Tree
Main Workspace
Inspector / Review Panel
```

- SHOULD 表达来源、实体、关系、审核结果之间的关联。
- MUST AI 候选先进入候选区，用户确认后写入正式数据。
- NEVER 全部退化为普通表格。

### 17.4 运维看板

适用：运维看板、健康检查、任务管理、备份恢复、清理任务。

结构：

```text
MetricSummary
AlertTimeline
ServiceHealth
TaskQueue
ActionPanel
```

- MUST 关键异常首屏可见。
- MUST 操作入口靠近状态反馈。
- MUST 长任务提供进度、重试、取消或日志入口。

## 18. UI Kit 候选组件

后续 admin-web UI kit SHOULD 包含：

```text
AdminShell
AdminSidebar
AdminTopbar
AdminPage
PageHeader
PageToolbar
FilterBar
FilterSheet
DataView
DataTable
DataCardList
InspectorPanel
EntityDrawer
ConfirmDialog
EmptyState
StatusBadge
ActionMenu
ActivityTimeline
DepartmentPicker
```

职责：

- `AdminShell`: 全局布局与菜单。
- `AdminPage`: 页面骨架。
- `DataView`: 表格/卡片响应式切换。
- `DepartmentPicker`: 部门路径和选择。
- `ActivityTimeline`: 活动记录。
- `FilterSheet`: 移动端筛选。

## 19. 验收清单

每个页面 MUST 验收：

- PC / laptop / pad / phone 四档截图。
- 页面任务首屏可理解。
- 主操作唯一。
- 搜索和筛选清晰。
- 已选筛选项可见、可移除。
- 表格列按优先级降级。
- 手机端没有桌面表格。
- 详情和编辑保留对象身份。
- 活动记录解释状态变化。
- 空状态有可执行动作。
- 错误状态说明原因和下一步。
- 键盘焦点清晰。
- 控件有稳定可访问名称。
- 控制台无相关错误。

## 20. 外部参考

- [WCAG 2.2, W3C](https://www.w3.org/TR/WCAG22/)
- [Material Design 3 Breakpoints](https://m3.material.io/foundations/layout/breakpoints)
- [Material Design Navigation Drawer](https://m2.material.io/components/navigation-drawer)
- [Material Design Data Tables](https://m2.material.io/components/data-tables)
- [Apple Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines)
- [GOV.UK Design System Components](https://design-system.service.gov.uk/components/)
- [MOJ Design System Filter](https://design-patterns.service.justice.gov.uk/components/filter/)
- [Carbon Design System Data Table](https://carbondesignsystem.com/components/data-table/usage/)
- [Carbon Design System Filtering](https://carbondesignsystem.com/patterns/filtering/)
- [NN/g Data Tables](https://www.nngroup.com/articles/data-tables/)
- [Fluent 2 Layout](https://fluent2.microsoft.design/layout)
