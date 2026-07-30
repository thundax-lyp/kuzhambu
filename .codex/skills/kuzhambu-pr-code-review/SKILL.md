---
name: kuzhambu-pr-code-review
description: Kuzhambu strict current-branch PR code review workflow for direct slash-command invocation. Use this whenever reviewing committed current-branch changes against `main`, especially when the diff changes runtime contracts, task/status flows, permissions, frontend forms, local-vs-remote state boundaries, async refresh behavior, wrappers around controlled inputs, or other places where behavior can drift across layers even when individual files look reasonable. It reports only actionable issues introduced or exposed by the branch diff and does not modify code.
---

# Kuzhambu PR Code Review

请对当前分支相对于 `main` 分支的代码变更进行一次严格的 Pull Request Review。

这份 skill 不再把 review 当成“逐段阅读 diff 并做通用检查”，而是当成“对本 PR 改变的系统承诺做失效模式分析”。目标不是找风格问题，而是尽量在首轮 review 中识别：

- 契约在链路中途被改坏，但最终消费点或校验点没跟上
- 同一能力在等价路径、多 taskType、多状态、多入口之间不一致
- 权限展示、身份来源、后端校验、seed 数据或资源访问路径脱节
- 前端受控表单、wrapper、局部状态、异步刷新和 effect 依赖边界失稳
- 请求规模被放大成全量扫描、N+1 或并发请求风暴
- 测试只覆盖 happy path，掩盖真实时序、历史数据或刷新路径问题
- 门禁、流程、skill 或自动化脚本自身没有真实约束到它声称约束的系统行为

## 调用方式

本 skill 只用于 slash command 直接调用，不定义额外自然语言触发或适用语义。

## 前置步骤

1. Confirm the current working directory is inside the Kuzhambu repository.
2. Read `docs/AGENTS.md` for document routing.

## 必须先执行并阅读

按顺序执行并阅读以下命令输出：

```sh
git status
git diff --stat main...HEAD
git diff main...HEAD
```

如果 `git status` 显示未提交变更或未跟踪文件，先判断它们是否属于本次待审内容：

- 如果用户明确要求审查工作区变更，再额外读取相关 `git diff` / 文件内容。
- 如果用户只要求审查“本分支”或 PR，默认只审 `main...HEAD`，并在 summary 中简短说明工作区还有未提交/未跟踪内容未纳入审查。

如果 `git diff main...HEAD` 为空，仍输出 `No actionable findings.`，并在 summary 中说明当前分支相对 `main` 无已提交代码差异。

## 上下文加载

遵循 `docs/AGENTS.md` 的最小文档加载原则。根据 diff 类型读取必要治理文档：

- Java servers 变更：读取 `docs/00-governance/ARCHITECTURE.md` 和 `docs/00-governance/SERVERS-ARCHITECTURE.md`；涉及目录、命名、模块归属或依赖方向时，再读 `docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`；涉及数据库、表字段、索引、MyBatis、查询、分页、迁移或缓存真相源时，再读 `docs/00-governance/SERVERS-DATABASE-RULES.md`；涉及业务 ID、ULID、强类型标识或 token 边界时，再读 `docs/00-governance/SERVERS-UNIFIED-ID-DESIGN.md`。
- admin-web 变更：读取 `docs/00-governance/ARCHITECTURE.md` 和 `docs/00-governance/ADMIN-WEB-RULES.md`；涉及 UI、CSS、页面布局、组件、动效、视觉资产、hero、`testId` 或 `data-testid` 时，再读 `docs/00-governance/UI-RULES.md`。
- portal-web 变更：读取 `docs/00-governance/ARCHITECTURE.md` 和 `docs/00-governance/PORTAL-WEB-RULES.md`；涉及 UI、CSS、页面布局、组件、动效、视觉资产、hero、`testId` 或 `data-testid` 时，再读 `docs/00-governance/UI-RULES.md`。
- Python workers 变更：读取 `docs/00-governance/ARCHITECTURE.md` 和 `docs/00-governance/WORKERS-RULES.md`；涉及 worker 能力、接口、流式输出、AI/render 边界或跨服务协议时，再读 `docs/10-requirements/WORKERS-REQUIREMENTS.md`、`docs/30-designs/WORKERS-DESIGN.md` 和相关 `docs/20-interfaces/WORKERS-*-INTERFACE.md`。
- 文档、TODO、PR 或收口流程变更：读取 `docs/00-governance/DOCUMENT-RULES.md`、`docs/00-governance/TODO-RULES.md` 或 `docs/00-governance/PR-RULES.md` 中与 diff 直接相关的文件。

如果 diff 涉及以下任一关键词或相关接口行为，先阅读 `docs/30-designs/SORT-ORDERING-SPECIAL-DESIGN.md`，并以该专项设计作为排序规则依据：

- `Sortable`
- `priority`
- `orderedIds`
- `*SortRequest`
- 排序接口、排序拖拽、排序保存或排序回显

## 审查总流程

本 skill 的主流程固定为 7 步，顺序不要跳：

1. **理解 PR 目标**：明确本 PR 想完成什么，哪些运行时语义可能被改动。
2. **识别系统承诺**：把 diff 转换成被改变的用户能力、接口语义、状态语义、数据语义、权限语义、验证语义或流程语义。
3. **归类失效模式**：不要直接找 bug，先判断这个 PR 最可能落入哪些风险模型。
4. **建立运行时模型**：为命中的风险模型产出最小必要的链路表 / 一致性表 / 状态表 / 治理能力表。
5. **对账正空间和负空间**：既检查新增路径是否成立，也检查被替换、删除、绕过或收窄的旧路径是否仍有明确语义。
6. **推演边界与时间线**：至少检查一组历史数据、异常路径、并发、刷新时序或治理失败路径。
7. **输出 findings**：只报告明确、可操作、由当前分支引入或暴露的问题。

如果第 2 步没有识别出系统承诺，或第 4 步没有形成清晰的运行时模型，不要急着给结论。

## 写边界

- 本任务只输出审查结果。
- 不要修改代码、格式化代码、提交代码、回复评论或生成修复补丁。
- 即使已经定位到明确缺陷，也先把它作为 finding 报告，而不是直接修复。

## 先理解 PR 意图

在报告问题之前，先通过以下信息推断本分支的主要目标：

- `git diff --stat main...HEAD` 的文件分布
- `git diff main...HEAD` 中新增、删除和修改的行为
- commit message、测试文件名、文档变更、接口/路由/配置变更

审查时围绕“本 PR 想完成什么”“diff 实际改变了什么系统承诺”“哪些旧承诺被替换或删除”判断风险，不要把 review 做成逐行风格建议。

如果本 PR 新增或修改共享抽象、公共接口、跨层协议、路由/配置判断、数据访问规则、worker capability、前端表单 wrapper、局部状态 hook 或异步任务流，先提炼它们的运行时契约，再按契约审查调用点和边界情况。

## 系统承诺识别

在选择失效模式之前，先把 PR diff 归纳成 1-5 条系统承诺。系统承诺是当前分支声称让系统保持或改变的稳定语义，不是文件列表。

常见系统承诺包括：

- 用户能力承诺：某个用户入口、操作、批量能力、详情能力、分享/下载/预览/应用能力仍可达或被有意移除。
- 接口响应承诺：同类输入、非法输入、业务拒绝、权限失败和技术失败继续返回既定状态码、错误码和错误消息。
- 状态流转承诺：任务、候选、版本、日志、SSE、轮询和终态结果在所有路径上表达同一状态语义。
- 数据语义承诺：业务 ID、外部 key、资源引用、token、原始文本、partial composite value 和持久化历史值保留各自语义，不被统一转换抹平。
- 权限语义承诺：菜单、路由、按钮、直接访问、后端鉴权、seed 和资源 URL 以同一个权限事实源约束能力。
- 验证语义承诺：新增门禁、测试、CI、脚本或 skill 实际覆盖它声称覆盖的对象，并且不会误放行、误拦截或基于过期事实报告成功。
- 交付流程承诺：push、rebase、force-with-lease、review feedback、CI observation 和 PR 描述同步不会破坏远端状态或让过期信息成为最终结论。

后续 review 必须验证这些承诺在 producer、adapter、validator、consumer、fallback、测试、文档和自动化中一致成立。发现统一封装、统一 fallback、统一 catch、统一 normalize、统一状态映射或统一门禁规则时，优先检查它是否错误合并了不同语义类别。

## 失效模式分类

先从下面几类里选出 1-3 个与本 PR 最相关的主风险模型，后续 review 重点围绕这些模型展开：

### A. 契约链路失效

适用于：

- DTO / command / request / response 字段改动
- 默认值、可空性、枚举值、schema、prompt 变量、配置 key、输出结构变化
- 同一字段从一层生成，另一层校验或消费

常见症状：

- 上游改了字段，但真实消费点没跟上
- 中间层只转发不校验，最终在更下游炸掉
- 测试只验证构造结果，没有验证消费点是否接受

### B. 多路径不一致

适用于：

- 同一能力存在 sync/async、create/update、preview/apply、list/detail、page/dialog、新标签页/页内跳转
- 同一对象有多个 taskType、status、version、candidate、action 路径

常见症状：

- 页面展示 A，按钮操作 B
- 某条路径修好了，等价路径仍旧走旧语义
- preview、download、open-in-new-tab 的认证和数据语义不一致

### C. 权限与身份真相源错位

适用于：

- 菜单、按钮、资源链接、controller、subject、role、permission、seed 数据改动
- 客户端传 actor、tenant、scope、owner、subjectId

常见症状：

- 前端可见但后端 403
- 角色 seed 变更后通过权限展开逻辑获得额外写权限
- 身份字段来自客户端而不是认证主体

### D. 状态机与时序失效

适用于：

- 任务、版本、候选、apply、异步回写、刷新、轮询、fallback、局部状态同步
- 任何“先后顺序变了就可能错”的逻辑

常见症状：

- 旧数据 later apply 覆盖新任务状态
- unmount 后展示卡在旧状态
- 局部状态和远端状态在异步刷新后冲突

### E. 数据范围与性能放大

适用于：

- 搜索、树、列表、分页、聚合查询、每项逐个查询、React Query key 依赖输入值
- 循环内 service / repository / facade 调用

常见症状：

- root 页面打开就全量扫描
- 输入每个字符都触发昂贵请求
- 每项触发单独远程/数据库查询，形成 N+1 或请求风暴

### F. 前端受控语义与局部状态边界失效

适用于：

- `Form.Item` / `KuzhambuFormItem` 包裹结构变化
- 受控组件 wrapper、自定义字段组件、`initialValues`、`setFieldsValue`、`resetFields`
- `useEffect`、`useCallback`、`useMemo`、本地 draft、refetch、mutation success

常见症状：

- wrapper 吞掉 `value/onChange`、`checked/onChange`
- 新对象 refresh 覆盖未保存草稿
- partial patch 留下旧值
- effect 因依赖身份不稳陷入循环或重复覆盖

### G. 测试伪覆盖

适用于：

- 本 PR 只加了 happy path 测试
- 测试只断言构造数据，没断言最终用户可见行为
- 测试没有覆盖刷新、切换、失败、历史数据或交错时序

常见症状：

- 测试全绿，但实际运行在非默认路径失败
- 测试绑定实现细节，没覆盖运行时语义

### H. 治理能力失效

适用于：

- 架构测试、命名门禁、CI workflow、验证脚本、PR 流程、agent、skill、自动化检查或发布规则改动
- 声称新增约束、扩大覆盖、整理历史、同步 PR 信息、观察 CI/review 或改变合并前流程

常见症状：

- 门禁只检查文件形状，没有检查真实声明、运行对象或最终消费结果
- 门禁在当前仓库启用后立即失败，或仍能被等价路径绕过
- 流程基于旧的本地/远端/CI/review 状态报告成功
- 自动化失败后无法恢复到原始安全状态

## 运行时建模要求

命中哪类风险模型，就必须产出哪类最小分析结果。分析结果不必写进最终回复，但必须在内部先做完。

读取跨层上下文是允许的，但上下文代码、旧路径、旧 seed、旧 wrapper、旧消费者本身不自动构成本次 PR finding。只有当当前 diff 满足以下任一条件时，才可以把问题计入 findings：

- 当前 diff 直接引入了缺陷
- 当前 diff 改变了旧契约，导致旧消费者现在失效
- 当前 diff 把原本潜伏的问题连通、放大或变成用户可见问题
- 当前 diff 声称完成了某个闭环，但实际只改了上游或中游，没有改到真实消费点

如果只是借上下文顺手发现既有问题，但无法证明它由当前分支引入或暴露，不要计入 findings；必要时只作为 open question 提及，或忽略。

### 1. 契约链路表

只要 diff 改了字段默认值、可空性、删字段、硬编码、schema、配置来源、状态字段或 request/response 契约，就必须追完整条链路：

- source of truth 是什么
- producer 在哪里生成
- adapter / assembler / wrapper 在哪里转发或改写
- 第一个真实校验点在哪里
- 第一个真实消费点在哪里
- 失败是早失败还是晚失败

**硬规则**：不要停在“字段成功构造”这层。必须找到首个真实校验点和最终真实消费点。

### 2. 一致性对账表

只要同一 feature 同时涉及 taskType、status、version、candidate、permission、preview、download、route、resource URL 或 action，就必须列出：

- 列表/树状态使用什么语义
- 详情状态使用什么语义
- action / apply / submit 使用什么语义
- fallback / preview / dialog / new tab 使用什么语义
- 等价路径之间是否完全一致

### 3. 状态与时间线表

只要有任务、版本、候选、apply、轮询、refetch、fallback、local draft、effect 同步，就必须至少推演 3 条时间线：

1. 旧数据完成后，新数据开始
2. 新数据开始后，旧数据才回写或 apply
3. 不同 taskType / section / tab / refresh 交错发生

如果某个时间线会导致页面状态、按钮动作或持久化结果错位，应作为 finding。

### 4. 前端状态边界表

只要改动 admin-web / portal-web 表单、section、drawer、modal、hook 或 wrapper，就必须先回答：

- 哪个组件拥有真实 draft state
- 哪个组件只是展示或 patch
- 表单 direct child 还是不是真实受控控件
- wrapper 是否透明转发 `value/onChange` / `checked/onChange`
- `initialValues`、`setFieldsValue`、`resetFields` 分别承担什么语义
- refetch / mutation success 是否会覆盖未保存编辑
- child unmount 后 parent 展示是否回退到 live/fallback state
- effect 依赖里的 callback / object identity 是否稳定

### 5. 系统承诺对账表

只要 PR 是替换、删除、迁移、收敛或重新组织能力，就必须列出：

- 被新增或强化的承诺是什么
- 被替换、删除、绕过或收窄的旧承诺是什么
- 旧承诺是否被新路径等价承载
- 如果旧承诺被有意移除，需求、权限、文档、测试和用户入口是否同步表达移除语义
- 是否存在后端能力、权限 seed、前端入口、测试或文档之间的残留不一致

### 6. 治理能力表

只要 PR 修改门禁、CI、脚本、agent、skill、PR 流程或文档治理规则，就必须列出：

- 该治理能力声称约束什么对象和行为
- 实际输入源是什么：diff、文件路径、声明类型、编译产物、远端状态、CI 状态、review 评论或用户确认
- 它会放行什么，拦截什么，跳过什么
- 当前仓库是否已有样本能证明它不会误放行或误拦截
- 失败、中断、远端变更或信息过期时，是否能停止并恢复到可审查状态

## 强制专项检查

### A. 字段 producer-to-sink tracing

触发条件：

- diff 改动 `Command`、`Request`、`Response`、`DTO`、`Result`、schema、prompt variables、配置 helper、seed、权限字段、资源 URL、taskType、status

必须动作：

- 用 `rg` 搜这个字段 / 常量 / key 的所有读取点
- 找到首个真实校验点
- 找到最终真实消费点
- 检查测试是否覆盖到消费点，而不是只覆盖构造点

### B. 前端表单 direct-child / wrapper 透明性

触发条件：

- `Form.Item` / `KuzhambuFormItem` 子树结构变更
- 新增 wrapper 组件包裹输入控件

必须动作：

- 检查 named item 的 direct child 是否仍是实际受控控件，或 wrapper 是否完整转发控件协议
- 对 select、switch、picker、upload、custom field 等非简单 input 特别警惕

### C. partial patch / refresh / draft 覆盖

触发条件：

- `setFieldsValue`、`resetFields`、`initialValues`、`useEffect` 写表单
- refetch、mutation success、切换对象、切 tab、切 section

必须动作：

- 推演“同一对象刷新”和“切换到不同对象”两种路径
- 检查 omitted null fields、partial payload、server NON_NULL 返回是否导致旧值残留
- 检查 refetch 是否覆盖未保存草稿

### D. 权限与认证路径一致性

触发条件：

- 菜单、permission、role、subject、owner、token、download URL、new tab、资源访问路径变更

必须动作：

- 检查 UI 门控与后端鉴权是否一致
- 检查身份字段是否来自服务端认证主体
- 检查 preview、download、open-in-new-tab 是否仍能带上认证语义
- seed 数据必须追权限展开和消费逻辑，不要只看 SQL / JSON 表面

### E. 性能放大器扫描

触发条件：

- 搜索、树、列表、分页、聚合、跨域 facade 调用、React Query key、循环内调用

必须动作：

- 判断是否把 O(1) / O(page) 放大成 O(N) 或 O(N * remote)
- 检查是否在 root-only、空过滤、每次键入、每条记录上触发昂贵查询
- 检查请求是否可 debounce、batch、memoize、skip 或只在必要场景触发

## 审查步骤

1. 推断本 PR 的目标和主要风险区域。
2. 识别本 PR 改变的 1-5 条系统承诺。
3. 归类最相关的 1-3 个失效模式。
4. 为命中的失效模式建立最小运行时模型。
5. 扫描本 PR 中所有调用点、迁移点、测试、配置和文档是否满足这些模型。
6. 对 exact string、Set membership、数组 includes、枚举映射、路径前缀、SQL 条件、状态机分支、协议字段判断以及前端 wrapper/受控组件变更，检查合法边界变体。
7. 只把明确违反契约、造成回归风险、安全风险、架构边界问题、性能退化或真实维护阻塞的问题作为 finding。

## 边界与时序推演清单

遇到以下改动时，至少手工推演一轮反例：

- **状态/任务/版本**：旧任务 later apply、新任务先开始、不同 taskType 交错
- **表单/局部状态**：同一对象刷新、切换到另一对象、child unmount、mutation success 后 refetch
- **权限/身份**：只读角色、无 edit 权限、手工伪造客户端 actor、new tab 访问受保护资源
- **搜索/树/列表**：root-only、空关键字、快速连续输入、命中大量记录
- **协议/字段**：字段缺失、null、省略字段、历史数据、旧枚举、空集合、默认配置未命中

## 重点检查

### 1. 正确性

- 逻辑错误、边界条件遗漏、空值处理错误
- 异常处理不完整，或错误被吞掉导致调用方误判成功
- 并发、状态同步、生命周期、事务边界和资源释放问题
- 是否破坏已有功能、数据兼容性、接口兼容性或迁移路径
- 前后端字段、枚举、路由、DTO、接口契约是否一致

### 2. 架构与结构

- 文件名、文件路径和模块归属是否准确清晰
- 包结构、模块职责和依赖方向是否符合项目边界
- 是否存在职责混乱、循环依赖、不必要的抽象或错误的层级穿透
- 新增代码是否放在正确的工程组、层级和模块中
- 是否让既有大文件继续膨胀，或把可独立的用户流程塞进不合适的组件/服务

### 3. 安全性与稳定性

- 输入校验、权限检查、越权访问、敏感信息泄露
- 注入、路径遍历、不安全反序列化、不可信文件处理等风险
- 运行时配置、默认值和失败降级是否会造成不可预期行为
- 管理端权限、用户边界、文件上传/下载、OSS、导出、AI API key、第三方 base URL 和日志脱敏

### 4. Java servers 契约

如果 diff 涉及 Java servers，必须检查：

- Controller / application / domain / infra 的依赖方向是否符合治理文档
- 应用服务事务边界是否覆盖完整写操作，是否把外部 IO 放进不必要的事务
- Repository 查询条件是否保持权限、业务范围、生命周期状态、分页边界和排序边界
- DTO / command / response 是否与前端、worker 或公开接口协议一致
- 强类型 ID、ULID、token、外部 ID 是否没有退化成裸字符串误用
- 异常、幂等、重复提交、并发更新是否有明确语义

### 5. Frontend 运行时语义

如果 diff 涉及 admin-web / portal-web，必须检查：

- 受控表单 direct child / wrapper 透明性是否仍成立
- `initialValues`、`setFieldsValue`、`resetFields` 的语义是否清晰且互不覆盖
- 本地 draft 是否会被 refetch、mutation success、切换对象或切 tab 覆盖
- callback / object identity 是否会触发 effect 重跑、循环更新或重复写 state
- preview / download / modal / new tab / route 路径是否仍带着正确的权限与认证语义
- 新增测试是否覆盖用户可观察行为，而不是只验证内部 helper

### 6. Python workers 契约

如果 diff 涉及 Python workers，必须检查：

- worker capability 输入/输出 schema 是否与 `docs/20-interfaces/` 协议一致
- 流式输出事件顺序、结束事件、错误事件和 partial result 是否稳定
- 超时、重试、取消、异常回传是否会让 Java 调用方误判成功或挂起
- 大文件、大响应和长任务是否有资源上限和清理逻辑

### 7. 测试

- 新增或变更行为是否有测试覆盖
- 测试是否覆盖失败路径、边界情况、刷新路径、权限差异、历史数据或交错时序
- 测试是否真正验证用户可观察行为，而不是只绑定实现细节
- 如果 diff 修改构建、CI、测试框架、校验脚本或依赖版本，重点审查验证链路是否仍能覆盖目标模块

## 报告规则

- Findings 必须放在最前面，按严重程度从高到低排序。
- 只报告明确、可操作、值得开发者修改的问题。
- 不要报告纯格式问题，除非它明显影响理解或维护。
- 不要为了凑数量提出建议。
- 不报告“可以更优雅”“可以顺手重构”“命名个人偏好”这类没有明确用户可见后果或维护风险的问题。
- 不把缺少重构、抽象不够漂亮、文件还可以继续拆分当作 bug；只有当它造成真实职责错位、回归风险或后续维护阻塞时才报告。
- 不确定的问题不要描述为确定缺陷；可以作为 open question 放在 summary 后。
- 相同根因的问题合并报告，避免对同一缺陷重复计数。
- 优先报告 bug、回归风险、安全问题、权限问题、状态/时序问题和性能放大问题，而不是个人风格偏好。
- 每条 finding 必须包含最小文件位置。行号优先使用 diff 或文件中的具体行；无法精确到单行时，使用最小必要范围。
- Finding 的位置优先指向当前 diff 新增或修改行附近；如果根因在新增调用方，优先报告调用方，不要把旧代码作为主要位置。
- 如果发现的问题依赖推断，明确说明推断依据。

## 每个问题的格式

```md
### [P0/P1/P2/P3] 简短标题

* 文件：`路径`
* 行号：具体行号或最小代码范围
* 问题：说明哪里有问题。
* 影响：说明在什么情况下会发生，以及可能造成什么后果。
* 建议：给出简洁、具体的修复方向。
```

## Open questions

如果存在影响判断但无法从代码和文档确认的问题，在 findings 后、summary 前输出：

```md
## Open questions

* 问题：需要用户或作者确认的具体事项。
```

不要把 open question 计入 P0/P1/P2/P3。

## 优先级定义

- P0：必须立即修复，会导致严重事故、安全漏洞或数据损坏。
- P1：高概率造成明显错误、严重回归、权限失控或重要流程不可用。
- P2：真实存在但影响范围有限的问题。
- P3：低风险的维护性、结构性或长期演进问题。

## 最后输出

在所有 findings 之后输出：

```md
## Review summary

* 本次审查范围：当前分支相对 `main` 的已提交 diff；如有排除项，明确说明
* 我理解的 PR 目标：一句话概述
* 主风险模型：列出本次命中的 1-3 个失效模式
* 是否建议合并：是 / 修复后合并 / 不建议合并
* P0 数量
* P1 数量
* P2 数量
* P3 数量
* 最高风险领域：契约链路 / 多路径一致性 / 权限与身份 / 状态时序 / 性能 / 前端受控语义 / 测试 / 无
* 最主要的风险概述：一到三句话
```

如果没有发现值得报告的问题，明确输出：

```text
No actionable findings.
```

然后仍输出 `## Review summary`，说明建议合并、各优先级数量为 0，以及主要剩余风险或测试缺口。
