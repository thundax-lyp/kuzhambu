---
name: kuzhambu-project-health-review
description: Kuzhambu project health review workflow for direct slash-command invocation with a time range or PR range. Use this skill only when directly invoked as a slash command. It analyzes requirements, design, PRs, commits, code-change scope, review feedback, CI, and verification evidence to judge code health and delivery health over the requested range. Do not modify code.
---

# Kuzhambu Project Health Review

评价指定时间范围或 PR 范围内的项目健康状况。不要复述提交记录；要基于需求、设计、PR、commit、代码改动规模和验证证据判断项目是否健康推进。

## 分析前提

报告必须先声明：这是面向人类决策的工程健康判断，不是自动评分或流水账汇总；被分析项目的代码和文档默认完全由 AI 编写，人类主要负责提出目标、审查方向和作出最终判断。

因此，评审不要把“由 AI 编写”本身当成成功点或风险点。判断重点应放在 AI 产物是否被明确需求、架构规则、PR 边界、review 反馈、测试验证和文档治理有效约束；如果缺少这些约束，应把风险归因到需求、治理、验证或交付闭环缺口，而不是泛泛归因到 AI。

## 调用方式

本 skill 只用于 slash command 直接调用，不定义额外自然语言触发或适用语义。

调用参数必须是时间范围或 PR 范围，例如：

- `最近 3 天`
- `最近 7 天`
- `2026-07-16 到 2026-07-22`
- `PR #120 到 PR #126`

如果调用时没有给出范围，先要求用户补充范围。不要自行假设默认时间范围。

## 判断目标

以资深工程负责人和代码健康审查顾问的视角，同时评价：

- 代码健康：代码结构、模块边界、接口稳定性、测试覆盖、风险收口和工程治理是否健康。
- 推进健康：开发节奏、PR 边界、需求对齐、验证闭环和交付节奏是否健康。

必须回答：

1. 范围内开发是否围绕明确需求推进？
2. 代码结构和模块边界是更清晰，还是出现更多耦合和返工？
3. PR 和 commit 是否形成可审查、可验证的阶段性交付？
4. 主要成功点、主要风险点和下一步收口路线是什么？

不要把提交数量多直接等同于健康。忙碌只是现象，不是结论。

## 证据等级

为关键判断标注证据强度：

- 明确证据：需求文档、设计文档、PR 描述、review 评论、CI 结果、验证命令输出、代码 diff 或测试报告。
- 中等证据：commit 标题、merge commit、改动统计、文件分布和提交时间分布。
- 弱证据：只能从命名、提交密度或缺失信息推断。

如果只能从 commit 标题推断，必须写“从提交标题推断”。不要把推断写成事实。

## 资料采集

拿不到的资料不要阻塞分析，但必须在报告最后说明缺口。

### 文档

按仓库文档路由读取，不要默认全量读取 `docs/`：

1. 读取 `docs/AGENTS.md`。
2. 根据范围涉及的工程组读取最小治理文档：
   - 全局架构：`docs/00-governance/ARCHITECTURE.md`
   - Java servers：`docs/00-governance/SERVERS-ARCHITECTURE.md`
   - admin-web：`docs/00-governance/ADMIN-WEB-RULES.md`
   - portal-web：`docs/00-governance/PORTAL-WEB-RULES.md`
   - workers：`docs/00-governance/WORKERS-RULES.md`
   - TODO、PR 或交付收口：`docs/00-governance/TODO-RULES.md`
3. 根据 PR/commit 主题识别主要模块，再按需读取相关需求、接口和设计文档：
   - `docs/10-requirements/`
   - `docs/20-interfaces/`
   - `docs/30-designs/`

不要为了分析而读取无关需求。

### Git

根据调用范围调整 `--since`、`--until` 或 commit range：

```sh
git status --short
git log --since='<TIME_RANGE>' --date=iso-strict --pretty=format:'%H%x09%ad%x09%an%x09%s'
git log --since='<TIME_RANGE>' --first-parent --merges --date=short --pretty=format:'%h%x09%ad%x09%s'
git log --since='<TIME_RANGE>' --no-merges --numstat --pretty=format:'--COMMIT--%x09%ad%x09%s' --date=short
```

建议补充聚合：

```sh
git log --since='<TIME_RANGE>' --no-merges --date=short --pretty=format:'%ad%x09%s'
git log --since='<TIME_RANGE>' --no-merges --pretty=format:'%s'
```

如果范围是 PR 区间，优先用 first-parent merge commit 确定 PR 边界，再按 PR 对应 merge commit、分支范围或 commit range 分析。

### PR、CI 和验证

如能访问 GitHub 或其他代码托管平台，尽量补充：

- PR 标题、描述、分支名、合并时间和 `git diff --stat`。
- PR review 评论、requested changes 和后续修复提交。
- CI 结果、失败原因、失败重跑记录和最终通过状态。
- 每个 PR 描述中记录的验证命令和结果。

这些资料用于判断阶段目标、审查反馈、验证闭环和 PR 是否过大。不要只依赖 commit 标题。

## 分析步骤

1. 确定调用范围的真实时间边界或 PR 边界。
2. 统计 commit、非合并 commit、merge commit 和 PR 数量。
3. 聚合时间分布、提交类型、模块 scope、顶层目录改动规模。
4. 根据 PR/commit 主题识别主要业务主线和工程治理主线。
5. 读取对应需求、接口、设计和治理文档，建立“需求目标 -> PR/commit -> 验证证据”的映射。
6. 分析需求对齐、代码健康、推进健康、验证闭环和风险区域。
7. 输出成功点、风险点和后续路线。
8. 明确说明哪些判断证据充分，哪些只是推断，哪些需要补充信息。

## 分析维度

- 需求对齐：是否对应需求目标；需求、接口、设计、代码、测试和文档是否闭环；是否存在需求漂移、返工或契约滞后。
- 代码健康：模块边界、分层职责、接口稳定性、命名一致性、重复逻辑、测试覆盖和高风险区域是否变好。
- 推进健康：提交是否小步可审，PR 是否形成阶段边界，是否存在冲刺式修复、主题混杂或持续堆叠未闭环工作。
- 验证闭环：测试、格式、Lint、构建、架构检查、接口契约、CI 和 review 反馈是否支撑交付。

高风险区域包括鉴权、权限、事务、并发、数据一致性、资源限制、流式接口、异步任务、数据库初始化或迁移。

## 输出格式

按以下结构输出：

```md
### 分析前提

本报告是面向人类决策的工程健康判断，不是自动评分或提交流水账。被分析项目的代码和文档默认完全由 AI 编写，人类主要负责目标提出、方向审查和最终判断；因此，以下结论重点评价 AI 产物是否被需求、架构、PR、review、验证和文档治理有效约束。

### 总体判断

3-5 句话说明指定范围内项目是健康推进、偏高压推进、局部失控，还是需要暂停扩张先收口；说明代码健康和推进健康是否一致，以及最关键依据。

### 证据概览

* 已读取文档：
* 已采集 git/PR/CI/review/验证资料：
* 未能获取但影响准确度的资料：

### 推进节奏分析

* commit、非合并 commit、merge/PR 数量：
* 时间分布和高峰：
* 提交类型和主要阶段：
* 冲刺、返工、集中修复或收口迹象：

### 需求对齐分析

* 已明显推进或完成的需求：
* 尚未闭环的需求：
* commit/PR 与需求之间的偏差：
* 证据强度：

### 代码健康分析

* 模块边界：
* 接口稳定性：
* 数据、事务和并发风险：
* 测试覆盖：
* 文档和治理：
* 前端、后端、workers 和部署协同：

### 推进健康分析

* PR 边界：
* commit 粒度：
* review 和 CI 闭环：
* 节奏过载或阶段性收口需求：

### 主要成功点

列出 3-7 条。每条包含成功点、证据和为什么重要。

### 主要风险点

列出 3-7 条，按严重程度排序。每条包含风险、严重程度：高 / 中 / 低、证据、影响和建议。必须区分已经暴露的问题和潜在趋势风险。

### 后续建议路线

* 立即处理：今天到 2 天内
* 短期收口：1 周内
* 中期治理：2-4 周内

说明应该继续推进什么、暂停扩张什么、优先补验证什么、沉淀成规则或自动化门禁什么。

### 需要补充的信息

列出缺失的需求文档、PR 描述、review 评论、CI 结果、验证输出、测试报告、按 PR 拆分的 diff stat 或关键模块目录结构。
```

## 报告约束

- 不要只统计提交数量，不要把忙碌等同于健康。
- 不要只按 commit message 做确定性判断。
- 不要用“感觉”“应该还行”这类无证据判断。
- 必须区分代码健康和推进健康。
- 必须区分事实、推断和缺失信息。
- 成功点和风险点都必须有证据。
- 风险建议必须可执行，不要停留在泛泛提醒。
- 不要修改代码；本 skill 只用于分析和报告。
