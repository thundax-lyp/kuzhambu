---
name: kuzhambu-pr-code-review
description: Run this skill whenever the user asks to read the PR code review prompt, review the current branch, review this branch against main, perform a PR review, audit the branch diff, or says phrases like "阅读 PR code review prompt，对本分支进行review". This skill performs the project's strict current-branch code review directly from the skill instructions. Use it even when the user does not explicitly mention a skill.
---

# Kuzhambu PR Code Review

请对当前分支相对于 `main` 分支的代码变更进行一次严格的 Pull Request Review。

## 前置步骤

1. Confirm the current working directory is inside the Kuzhambu repository.
2. Read `docs/AGENTS.md` for document routing.

## 先理解 PR 意图

在报告问题之前，先通过以下信息推断本分支的主要目标：

- `git diff --stat main...HEAD` 的文件分布。
- `git diff main...HEAD` 中新增、删除和修改的行为。
- commit message、测试文件名、文档变更、接口/路由/配置变更。

审查时围绕“本 PR 想完成什么”和“diff 实际改变了什么”判断风险，不要把 review 做成逐行风格建议。

## 审查目标

- 使用 `git diff main...HEAD` 作为本次 PR 的主要审查范围。
- 只报告由当前分支相对 `main` 引入或暴露的问题。
- 可以阅读必要的上下文代码、测试、配置和治理文档，但不要把上下文中的既有问题当成本次 PR 问题报告。
- 不要修改代码、格式化代码、提交代码或生成修复补丁；本任务只输出审查结果。
- 文件路径使用仓库相对路径。

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

- Java servers 变更：读取 `docs/00-governance/ARCHITECTURE.md` 和 `docs/00-governance/SERVERS-ARCHITECTURE.md`；涉及目录、命名或模块归属时，再读 `docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`。
- admin-web 变更：读取 `docs/00-governance/ARCHITECTURE.md` 和 `docs/00-governance/ADMIN-WEB-RULES.md`。
- portal-web 变更：读取 `docs/00-governance/ARCHITECTURE.md` 和 `docs/00-governance/PORTAL-WEB-RULES.md`。
- Python workers 变更：读取 `docs/00-governance/ARCHITECTURE.md` 和 `docs/00-governance/WORKERS-RULES.md`。
- 文档、TODO、PR 或收口流程变更：读取 `docs/00-governance/DOCUMENT-RULES.md`、`docs/00-governance/TODO-RULES.md` 或 `docs/00-governance/PR-RULES.md` 中与 diff 直接相关的文件。

如果 diff 涉及以下任一关键词或相关接口行为，先阅读 `docs/30-designs/SORT-ORDERING-SPECIAL-DESIGN.md`，并以该专项设计作为排序规则依据：

- `Sortable`
- `priority`
- `orderedIds`
- `*SortRequest`
- 排序接口、排序拖拽、排序保存或排序回显

## 重点检查

### 1. 正确性

- 逻辑错误、边界条件遗漏、空值处理错误。
- 异常处理不完整，或错误被吞掉导致调用方误判成功。
- 并发、状态同步、生命周期、事务边界和资源释放问题。
- 是否破坏已有功能、数据兼容性、接口兼容性或迁移路径。
- 前后端字段、枚举、路由、DTO、接口契约是否一致。

### 2. 架构与结构

- 文件名、文件路径和模块归属是否准确清晰。
- 包结构、模块职责和依赖方向是否符合项目边界。
- 是否存在职责混乱、循环依赖、不必要的抽象或错误的层级穿透。
- 新增代码是否放在正确的工程组、层级和模块中。
- 是否让既有大文件继续膨胀，或把可独立的用户流程塞进不合适的组件/服务。

### 3. 命名与可读性

- 类名、方法名、属性名、变量名是否准确表达语义。
- 是否存在模糊、冗余、误导或与既有命名体系不一致的名称。
- 代码是否可以在不损害行为清晰度的前提下进一步简化。
- 是否存在重复逻辑、无效封装或不必要的中间层。
- 是否存在为了通过类型检查、lint 或编译而牺牲业务语义的命名、兜底值或转换逻辑。

### 4. 安全性与稳定性

- 输入校验、权限检查、越权访问、敏感信息泄露。
- 注入、路径遍历、不安全反序列化、不可信文件处理等风险。
- 性能退化、内存泄漏、阻塞操作、无限重试或异常放大。
- 运行时配置、默认值和失败降级是否会造成不可预期行为。
- 管理端权限、用户边界、文件上传/下载、OSS、导出、AI API key、第三方 base URL 和日志脱敏。
- MyBatis/SQL 查询条件、分页边界、排序边界和批量操作的数据范围。
- Python worker 调用的超时、重试、错误回传和跨服务协议兼容性。

### 5. 测试

- 新增或变更行为是否有测试覆盖。
- 测试是否覆盖失败路径、边界情况和回归风险。
- 测试是否真正验证用户可观察行为，而不是只绑定实现细节。
- 如果缺少测试会掩盖真实回归风险，应作为发现报告；不要只因为“没有测试”泛泛报问题。
- 如果 diff 修改构建、CI、测试框架、校验脚本或依赖版本，重点审查验证链路是否仍能覆盖目标模块。

## 报告规则

- Findings 必须放在最前面，按严重程度从高到低排序。
- 只报告明确、可操作、值得开发者修改的问题。
- 不要报告纯格式问题，除非它明显影响理解或维护。
- 不要为了凑数量提出建议。
- 不报告“可以更优雅”“可以顺手重构”“命名个人偏好”这类没有明确用户可见后果或维护风险的问题。
- 不把缺少重构、抽象不够漂亮、文件还可以继续拆分当作 bug；只有当它造成真实职责错位、回归风险或后续维护阻塞时才报告。
- 不确定的问题不要描述为确定缺陷；可以作为 open question 放在 summary 后。
- 相同根因的问题合并报告，避免对同一缺陷重复计数。
- 优先报告 bug、回归风险、安全问题和架构边界问题，而不是个人风格偏好。
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
- P1：高概率造成明显错误、严重回归或重要流程不可用。
- P2：真实存在但影响范围有限的问题。
- P3：低风险的维护性、结构性或长期演进问题。

## 最后输出

在所有 findings 之后输出：

```md
## Review summary

* 本次审查范围：当前分支相对 `main` 的已提交 diff；如有排除项，明确说明
* 我理解的 PR 目标：一句话概述
* 是否建议合并：是 / 修复后合并 / 不建议合并
* P0 数量
* P1 数量
* P2 数量
* P3 数量
* 最高风险领域：正确性 / 架构 / 安全 / 测试 / 无
* 最主要的风险概述：一到三句话
```

如果没有发现值得报告的问题，明确输出：

```text
No actionable findings.
```

然后仍输出 `## Review summary`，说明建议合并、各优先级数量为 0，以及主要剩余风险或测试缺口。
