---
name: kuzhambu-module-code-review
description: Run this skill whenever the user asks to review a module, perform a module code review, audit a package or directory, review all code under a module path, uses slash-command style input like "/module-review <moduleName>" or "/模块审查 <moduleName>", or says phrases like "阅读 module code review prompt，对这个模块做 review". This skill performs Kuzhambu's systematic full-module code review, not a PR diff review. Use it for repository subdirectories, Java Maven modules, frontend app areas, Python worker packages, and named business modules.
---

# Kuzhambu Module Code Review

请对用户指定模块的全部代码进行一次系统性 Code Review。这个 skill 审查模块当前状态，不以最近 Git diff 为边界。

## 模块路径

- 支持快捷命令形态：`/<command> <moduleName>`。将 slash command 后面的全部内容视为模块名或模块路径；例如 `ai`、`admin-web`、`kuzhambu-servers/biz/ai`、`kuzhambu-apps/admin-web/src/features/system`。
- 如果用户给出明确路径，将该路径作为 `<MODULE_PATH>`。
- 如果用户只给出模块名，先在仓库内定位最匹配的目录或 Maven/package 模块；无法唯一确定时，先提出一个简短澄清问题。
- 如果用户没有指定模块，不能自行扩大到全仓审查；请要求用户提供模块路径或模块名。
- 文件路径使用仓库相对路径。

## 前置步骤

1. Confirm the current working directory is inside the Kuzhambu repository.
2. Read `docs/AGENTS.md` for document routing.
3. Resolve and state `<MODULE_PATH>`.
4. List the module directory structure, excluding generated files, build outputs, third-party dependencies, compressed files, and binary artifacts.
5. Identify module entry points, public APIs, core classes/components/functions, configuration, tests, major data flows, and dependencies.

## 上下文加载

遵循 `docs/AGENTS.md` 的最小文档加载原则。根据 `<MODULE_PATH>` 和模块内容读取必要治理文档：

- Java servers 模块：读取 `docs/00-governance/ARCHITECTURE.md`、`docs/00-governance/SERVERS-ARCHITECTURE.md`；涉及目录、命名、模块归属或依赖方向时，再读 `docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`。
- admin-web 模块：读取 `docs/00-governance/ARCHITECTURE.md` 和 `docs/00-governance/ADMIN-WEB-RULES.md`。
- portal-web 模块：读取 `docs/00-governance/ARCHITECTURE.md` 和 `docs/00-governance/PORTAL-WEB-RULES.md`。
- Python workers 模块：读取 `docs/00-governance/ARCHITECTURE.md` 和 `docs/00-governance/WORKERS-RULES.md`。
- 文档模块：读取 `docs/00-governance/DOCUMENT-RULES.md`。
- 涉及 `Sortable`、`priority`、`orderedIds`、`*SortRequest` 或排序接口/拖拽/保存/回显时，读取 `docs/30-designs/SORT-ORDERING-SPECIAL-DESIGN.md`。

可以读取模块外部代码来理解接口、调用关系和数据结构；外部代码只作为上下文，不对其做全面审查。

## 审查边界

- 审查 `<MODULE_PATH>` 当前全部源代码、配置和测试代码。
- 不要只抽样阅读核心文件；应先建立结构和调用关系，再按包、目录或功能切片逐步审查。
- 不要修改代码、格式化代码、提交代码或生成修复补丁；本任务只输出审查报告。
- 不报告模块外部既有问题，除非当前模块依赖方式使该问题成为模块风险；这种情况要把 finding 定位到模块内调用点。

## 重点检查

### 1. 正确性

- 逻辑错误、边界条件遗漏、空值/空集合/非法状态处理。
- 异常处理不完整、异常被吞掉、错误转换导致调用方误判。
- 状态不一致、生命周期和资源释放问题。
- 并发、线程安全、竞态条件、重复执行和幂等性问题。
- 数据丢失、覆盖、污染、精度损失、编码/序列化错误。
- 时间、时区、排序、分页和权限上下文导致的错误。
- 前后端字段、枚举、DTO、接口契约、worker 协议是否一致。

只报告能够说明触发条件和实际影响的问题。

### 2. 模块职责与架构

- 模块职责是否清晰，是否承担了不属于自己的职责。
- 包结构、目录结构、依赖方向是否符合项目边界。
- 是否存在循环依赖、跨层调用、错误的层级穿透。
- 领域逻辑、应用逻辑、基础设施逻辑、UI 状态或适配逻辑是否混杂。
- 公共接口是否稳定、最小且清晰；内部实现是否被不必要地暴露。
- 抽象层级是否过多或不足；是否存在为了抽象而抽象的接口、工厂、包装类或中间层。

### 3. 文件与包结构

- 文件名、类名、组件名、包名和路径是否准确表达职责。
- 类、组件或文件是否包含多个无关职责。
- 是否存在内容过少、没有独立价值的文件。
- 是否存在应该拆分但过度膨胀的文件。
- 包名、目录名和实际职责是否一致。

### 4. 命名

- 模块名、包名、文件名、类名、方法名、属性名和变量名是否准确表达真实语义。
- 是否存在模糊名称，例如 `data`、`info`、`manager`、`handler`、`util`、`process`。
- 是否存在名称与实际行为不一致、同一概念多个名称、不必要缩写或过长名称。
- 布尔值、集合、命令和查询命名是否清楚。

命名问题必须说明它造成的误解或维护风险，并给出更合适的建议名称。

### 5. 清晰性与简洁性

- 重复代码、重复条件判断、重复数据转换。
- 不必要的包装方法、无价值接口或抽象类。
- 过深调用链、过深嵌套、不必要中间变量和临时对象。
- 可以合并的相似实现、死代码、废弃代码和未使用代码。
- 为通过类型检查、lint 或编译而牺牲业务语义的兜底值、转换逻辑或命名。

不要为了减少代码行数而牺牲语义清晰度；只有当简化能降低真实维护成本或回归风险时才报告。

### 6. API 与封装

- 公共 API 是否过多，参数/返回值是否难以理解。
- 是否用不明确的 `Map`、`Object`、数组或字符串传递结构化数据。
- 可变状态是否直接暴露，是否缺少必要不可变约束。
- 调用方是否必须了解过多内部实现。
- 接口是否容易误用；默认值、失败语义和异常约定是否明确。

### 7. 性能与资源

- 明显重复计算、不必要 I/O、循环中的数据库/网络/文件操作。
- 无界集合、缓存、队列、不合理全量加载、大对象复制。
- 阻塞操作、线程池/连接/流/文件句柄未正确释放。
- N+1 调用、会随数据规模显著恶化的算法。

不要报告没有实际依据的微优化。

### 8. 安全性与稳定性

- 输入校验、权限校验、越权访问和用户边界。
- 路径遍历、命令/SQL/模板/表达式注入、不安全反序列化。
- 敏感数据日志输出，密钥、token、密码硬编码。
- 不安全随机数或加密方式。
- 用户输入直接控制文件、网络、第三方 base URL 或执行行为。
- 文件上传/下载、OSS、导出、AI API key、worker 调用超时/重试/错误回传。

### 9. 测试

- 核心行为、失败路径、边界条件和回归风险是否有测试。
- 测试是否只验证实现细节，是否重复、脆弱或不可读。
- 测试名称是否准确，mock 是否过多导致测试失去实际价值。
- 是否缺少模块级集成测试。
- 是否存在实现已经变化但测试未同步的问题。

## 报告门槛

- 只报告明确、可操作、值得修改的问题。
- 不要为了凑数量提出建议。
- 不报告单纯格式问题。
- 不把个人风格偏好描述为缺陷。
- 不报告“可以更优雅”“可以顺手重构”这类没有明确用户可见后果或维护风险的问题。
- 不确定的问题不要描述为确定缺陷；可以放入 `Open questions`。
- 相同根因的问题合并。
- 每个 finding 都必须给出具体文件和代码位置。
- 优先报告正确性、回归风险、安全、架构和接口问题。
- 对结构性问题，说明它影响了哪些文件、调用链或后续变更能力。
- 不要只说“代码复杂”；必须说明复杂在哪里、造成什么风险、如何降低复杂度。

## 输出格式

先输出：

```md
# Module overview

* 模块路径：`<MODULE_PATH>`
* 模块主要职责：
* 目录和包结构概述：
* 核心入口：
* 核心类、组件或接口：
* 主要数据流：
* 外部依赖：
* 对外暴露的 API：
* 当前测试覆盖概况：
```

然后逐条输出问题，按 P0、P1、P2、P3 顺序排列：

```md
## [P0/P1/P2/P3] 问题标题

* 类型：正确性 / 架构 / 结构 / 命名 / 简化 / API / 性能 / 安全 / 测试
* 文件：`文件路径`
* 行号：具体行号或最小代码范围
* 问题：问题的具体内容。
* 触发条件：在什么情况下出现。
* 影响：可能造成的实际后果。
* 原因：问题产生的根本原因。
* 建议：具体、可执行的修改方向。
* 关联范围：受影响的类、接口或调用方。
```

如果存在影响判断但无法从代码和文档确认的问题，在 findings 后输出：

```md
# Open questions

* 问题：需要用户或作者确认的具体事项。
```

不要把 open question 计入 P0/P1/P2/P3。

## 优先级

- P0：可能造成严重安全事故、数据损坏或系统不可用。
- P1：高概率导致明确错误、严重回归或核心架构失控。
- P2：真实存在的局部缺陷或重要维护问题。
- P3：低风险但值得处理的结构、命名或简化问题。

## Refactoring priorities

最后输出：

```md
# Refactoring priorities

1. 修改目标：
   涉及文件：
   预期收益：
   风险：
   建议拆分步骤：
```

按照投入产出比排序，列出最值得优先处理的 3-8 项。仅列出与 findings 或明确维护风险相关的事项；不要为了填满数量而创造重构项。

## Review summary

最后输出：

```md
# Review summary

* 是否存在阻断性问题：是 / 否
* P0 数量：
* P1 数量：
* P2 数量：
* P3 数量：
* 模块整体评价：
* 最大正确性风险：
* 最大架构风险：
* 最值得优先简化的部分：
* 测试覆盖的主要缺口：
```

如果没有发现值得报告的问题，明确输出：

```text
No actionable findings.
```

然后仍输出 `# Module overview`、`# Refactoring priorities` 和 `# Review summary`，说明没有发现 actionable finding，并记录剩余风险或测试缺口。
