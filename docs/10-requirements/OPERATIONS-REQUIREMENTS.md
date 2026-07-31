# Operations Requirements

## Purpose

Operations 域定义 admin 侧运营运维能力需求，负责跨业务域统计聚合展示，以及报表、备份恢复、清理任务、健康检查和运行状态等运维动作的入口与台账记录。

Operations 是独立业务域，不属于 System 基础域，也不承载其他业务域主事实。Operations 面向管理员提供运营运维控制台能力，但控制台页面本身只是 interface 表现，不构成业务边界定义。

## Scope

覆盖：

- 运营运维仪表盘。
- 跨业务域统计结果的聚合展示。
- 平台访问、活跃用户、功能使用频率和热门内容等统计展示。
- 周报和月报生成及导出。
- 备份、恢复和恢复前快照记录。
- 健康检查摘要和运行指标查看。
- 系统日志和业务审计的聚合访问入口。
- 过期备份、过期草稿和过期导出产物清理入口。
- 长任务和批量任务运行状态查看。
- Operations 自有报表记录、备份记录、恢复记录、清理任务记录、健康检查记录和运行状态快照。
- 按权限聚合后的报表快照调用 render workers 生成 HTML 或 PDF 产物。

不覆盖：

- 业务内容编辑。
- 认证、权限、角色、菜单和用户主体管理。
- 通用业务审计真相源。
- 通用系统日志、访问日志和技术日志真相源。
- AI 提示词、模型配置和 AI workers 编排。
- 搜索和问答业务结果生成。
- 文件资源扫描、孤立文件清理和底层存储巡检。
- 其他业务域对象生命周期规则的所有权。

## Functional Requirements

### 仪表盘与聚合展示

- 必须提供运营运维仪表盘。
- 必须展示内容数量、翻译覆盖率、配图覆盖率、视觉资产覆盖率和 AI 调用统计。
- 必须展示内容增长趋势、标签覆盖率变化和热门内容排行。
- 必须展示平台访问量、活跃用户数和功能使用频率。
- 必须支持统计结果图表化展示。
- 必须支持按权限展示聚合结果，不得向未授权管理员暴露运营运维细节。

### 跨域报表统计规格

- Operations 面向周报和月报读取其他业务域统计时，必须使用聚合后的 application result 或 read model，不得直接复用其他业务域 controller response。
- Operations admin 对外接口必须只返回本域独立响应模型，不得透传其他业务域 response 结构。
- 统计结果必须显式返回 `periodStart` 和 `periodEnd`。
- 周报趋势序列必须按日聚合。
- 月报趋势序列必须按周聚合。
- 趋势序列结果必须显式返回 `bucket`，不得由 Operations 在聚合后自行推导。
- Classics 必须提供以下 summary 字段：
  - `contentCount`
  - `translatedContentCount`
  - `imageReadyContentCount`
  - `visualAssetReadyContentCount`
  - `portalVisitCount`
  - `topContents`：`contentId`、`contentType`、`title`、`visitCount`
  - `contentGrowthSeries`：`bucket`、`createdCount`
- AI 必须提供以下 summary 字段：
  - `invocationCount`
  - `succeededInvocationCount`
  - `failedInvocationCount`
  - `avgLatencyMs`
  - `totalCostAmount`
  - `topCapabilities`：`capability`、`invocationCount`
- Discovery 必须提供以下 summary 字段：
  - `searchCount`
  - `qaCount`
  - `avgSearchLatencyMs`
  - `topQueries`：`queryText`、`count`
  - `searchTrendSeries`：`bucket`、`searchCount`
  - `qaTrendSeries`：`bucket`、`qaCount`
- Knowledge 必须提供以下 summary 字段：
  - `tagCoverageRate`
  - `topTags`：`tagName`、`contentRefCount`
  - `categoryDistributions`：`categoryName`、`tagCount`
  - `monthlyNewTags`：`bucket`、`tagCount`

### 报表

- 必须支持生成周报和月报。
- 必须支持将报表导出为 PDF 或 HTML。
- 必须记录报表请求人、统计周期、生成状态、失败原因和导出产物定位信息。
- 必须支持查看报表生成记录和导出结果。

### 备份与恢复

- 必须支持系统启动自动备份。
- 必须支持每 24 小时自动备份。
- 必须支持手动备份。
- 必须支持查看备份列表、备份状态、备份结果和有效期。
- 必须支持从备份发起恢复。
- 必须在恢复前创建恢复前快照记录。
- 必须记录恢复请求人、恢复来源备份、恢复状态、失败原因和完成时间。

### 清理任务

- 必须支持过期备份清理入口。
- 必须支持过期草稿清理入口。
- 必须支持过期导出产物清理入口。
- 必须记录每次清理任务的类型、发起人、开始时间、完成时间、总数、成功数、失败数和失败原因。
- 必须支持查看清理任务明细和单项处理结果。

### 健康检查与运行状态

- 必须支持健康检查摘要查看。
- 必须支持运行指标查看。
- 必须支持查看长任务和批量任务的运行状态、成功数、失败数和失败原因。
- 长任务运行状态必须包含 Classics 发布和下线任务的只读快照；Classics 专门菜单同样只读，不提供任务重试、人工状态推进或其他管理操作。
- 必须支持记录关键组件健康检查结果、检查时间、耗时和说明信息。

### 运维入口

- 必须提供系统日志和业务审计的访问入口。
- 必须支持从 Operations 控制台跳转或聚合查看 System 提供的日志与审计结果。
- 必须支持管理员集中触发和查看报表、备份、恢复、清理和运行状态相关运维动作。

### 台账记录

- 必须记录 Operations 自有报表生成记录。
- 必须记录 Operations 自有备份记录。
- 必须记录 Operations 自有恢复记录。
- 必须记录 Operations 自有清理任务记录和清理单项结果。
- 必须记录 Operations 自有健康检查记录。
- 必须记录 Operations 聚合展示所需的必要运行状态快照或任务台账。

## Business Rules

- Operations 可以聚合展示其他业务域统计，但不拥有其他业务域的业务事实。
- Operations 不得复制其他业务域主表结构或改写其他业务域主事实。
- 内容、翻译、配图、导出、发布任务、Portal 访问和草稿相关事实归 Classics。
- AI 调用统计来源归 AI 域。
- 搜索和问答统计来源归 Discovery 域。
- 标签覆盖率和图谱质量统计来源归 Knowledge 域。
- admin 权限、当前认证上下文和通用业务审计能力由 System 提供。
- Operations 可以提供日志和审计的访问入口，但不得成为日志或审计真相源。
- Operations 自有台账只记录本域发起或管理的运维动作与结果，不记录其他业务域对象的完整生命周期。
- 恢复数据前必须创建恢复前快照。
- 恢复期间应阻止新的写入操作。
- 备份保留期限必须为 30 天，超过期限的备份应自动清理。
- 导出产物是临时产物，不进入数据备份范围。
- 清理操作必须可追溯。
- 统计数据展示必须遵守权限，不得向非管理员开放运营运维细节。
- Operations 能力仅 admin 可用。
- Operations 可以直接调用 render workers 生成周报和月报 HTML 或 PDF 产物。
- Operations 直接调用 render workers 前必须完成 admin 权限校验、统计数据聚合和报表快照准备。
- Operations 不得直接调用 workers 的 AI 接口；如未来需要 AI 报表摘要或异常摘要生成，必须通过 AI 域。
- render workers 返回的报表文件在进入 Storage 前只是临时产物。
- starter 只负责启动装配，不承载 Operations 的 Controller、ApplicationService、Repository 或持久化实现。

## Acceptance Criteria

- 管理员能查看内容、Portal 访问、AI 调用、搜索、问答和热门内容等聚合统计。
- 管理员能查看图表化的运营运维仪表盘。
- 管理员能生成并导出周报或月报。
- 管理员能查看报表生成记录和导出结果。
- 管理员能触发手动备份并看到结果。
- 管理员能查看备份列表并发起恢复。
- 恢复失败时保留恢复前快照记录。
- 管理员能发起过期备份、过期草稿和过期导出产物清理。
- 管理员能查看清理任务结果和单项失败原因。
- 管理员能查看健康检查摘要、关键组件状态和运行指标。
- 管理员能查看长任务和批量任务的运行状态。
- 管理员能通过 Operations 入口访问 System 提供的日志和审计结果。
- Operations 自有报表记录、备份记录、恢复记录、清理任务记录和健康检查记录可追溯。

## Related Documents

- [SYSTEM-REQUIREMENTS.md](./SYSTEM-REQUIREMENTS.md)：提供 admin 权限、当前认证上下文、系统日志和业务审计能力。
- [CLASSICS-REQUIREMENTS.md](./CLASSICS-REQUIREMENTS.md)：提供内容、导出、Portal 访问和草稿相关统计及清理对象。
- [AI-REQUIREMENTS.md](./AI-REQUIREMENTS.md)：提供 AI 调用统计来源。
- [WORKERS-REQUIREMENTS.md](./WORKERS-REQUIREMENTS.md)：提供 render workers 报表生成边界；AI workers 调用必须经由 AI 域。
- [KNOWLEDGE-REQUIREMENTS.md](./KNOWLEDGE-REQUIREMENTS.md)：提供标签覆盖率和图谱质量统计来源。
- [DISCOVERY-REQUIREMENTS.md](./DISCOVERY-REQUIREMENTS.md)：提供搜索和问答行为统计来源。
