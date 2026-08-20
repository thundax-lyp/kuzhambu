---
name: kuzhambu-pr-code-review
description: Kuzhambu strict current-branch PR code review workflow for direct slash-command invocation. Review committed changes against `main` through system commitments, failure models, runtime contract surfaces, and closed coverage ledgers. Report only actionable P0-P3 findings introduced or exposed by the branch diff. Do not modify code.
compatibility: Requires Git and the repository Node.js 26 runtime.
---

# Kuzhambu PR Code Review

对当前分支相对于 `main` 的已提交变更执行严格、只读的 Pull Request Review。把 PR 视为一组被改变的系统承诺，沿真实运行链路寻找失效点；不要把 review 做成逐行风格检查。

## 调用与边界

- 本 skill 只用于 slash command 直接调用。
- 只输出审查结果；不要修改或格式化代码、生成补丁、提交、推送、回复评论或委派审查。
- 默认只审 `main...HEAD`。只有用户明确要求时才纳入工作区变更。
- 上下文中的既有问题只有在当前 diff 引入、暴露、连通或放大它时才能成为 finding。

## 1. 建立审查基线

1. 确认当前目录位于 Kuzhambu 仓库内。
2. 阅读 `docs/AGENTS.md`，按其中路由最小化加载治理文档。
3. 运行并完整读取：

   ```sh
   node "$(git rev-parse --show-toplevel)/.codex/skills/kuzhambu-pr-code-review/scripts/collect-review-context.mjs" context --base main
   node "$(git rev-parse --show-toplevel)/.codex/skills/kuzhambu-pr-code-review/scripts/collect-review-context.mjs" diff --base main
   ```

   `context` 已过滤空数据和 Git 展示噪声，并整理基线、工作区变更、提交、模块、changed files 和 diff statistics。`diff` 输出待审 patch。

4. 如果完整 patch 过大，依据 `context` 的模块清单分段读取，直到覆盖全部 changed files：

   ```sh
   node "$(git rev-parse --show-toplevel)/.codex/skills/kuzhambu-pr-code-review/scripts/collect-review-context.mjs" diff --base main --module '<module>'
   ```

   必要时用 `--path '<repo-relative-path>'` 进一步缩小范围。分段读取不能代替 changed-file 全覆盖。

5. 保存 `snapshot` 中的 `head`、`base_sha`、`merge_base`、`diff_hash` 和完整 changed-file 集，作为关闭审查时的比较基线。

如果工作区存在未提交或未跟踪内容：

- 用户只要求审查分支或 PR 时，不纳入 findings，并在 summary 中说明排除范围。
- 用户明确要求审查工作区时，额外读取对应 diff 和文件；不要把它混入已提交 diff 的 ledger。

如果 committed diff 为空，按 0 个 changed file、0 个 contract surface、coverage complete 输出 `No actionable findings.`，并说明当前分支相对 `main` 没有已提交差异。

## 2. 加载必要上下文

始终以 `docs/AGENTS.md` 为文档路由入口。按 changed files 和真实契约影响选择文档：

- Java servers：`ARCHITECTURE.md`、`SERVERS-ARCHITECTURE.md`；按需补充架构细则、数据库规则、统一 ID 设计和相关接口文档。
- admin-web：`ARCHITECTURE.md`、`ADMIN-WEB-RULES.md`；涉及 UI、布局、组件、动效、视觉资产或测试标识时再读 `UI-RULES.md`。
- portal-web：`ARCHITECTURE.md`、`PORTAL-WEB-RULES.md`；涉及 UI 时再读 `UI-RULES.md`。
- Python workers：`ARCHITECTURE.md`、`WORKERS-RULES.md`；跨服务协议或 worker 能力变化时再读相关 requirement、design 和 interface。
- 文档、TODO、PR、skill、CI 或收口流程：读取直接相关的 document、TODO 或 PR 治理文档。
- 排序语义发生变化时，读取 `SORT-ORDERING-SPECIAL-DESIGN.md`。仅出现 `priority`、`status`、`Request` 等词但未改变相关语义，不触发专项文档。

## 3. 识别系统承诺

结合 diff、commit message、测试、文档、接口、路由和配置变更，把 PR 归纳成 1-5 条稳定语义，而不是文件列表。常见承诺包括：

- 用户能力：入口、操作、批量、详情、分享、下载、预览或 apply 能力仍可达，或被明确移除。
- 接口响应：正常、非法、业务拒绝、权限失败和技术失败保持既定状态码、错误码与消息语义。
- 状态流转：任务、候选、版本、日志、SSE、轮询和终态结果在所有路径上一致。
- 数据语义：业务 ID、外部 key、资源引用、token、原始文本、历史值和 partial value 不被错误归一化。
- 权限语义：菜单、路由、按钮、直接访问、后端鉴权、seed 和资源 URL 使用一致的权限事实源。
- 验证或交付：门禁、测试、CI、skill、push、rebase 和 review 流程实际约束其声称覆盖的行为。

替换、删除、迁移、收敛或重新组织能力时，同时记录被替换、绕过或收窄的旧承诺，并验证新路径是否等价承载。

## 4. 选择失效模型并建立运行时模型

完整读取 [`references/failure-models.md`](./references/failure-models.md)，选择 1-3 个最相关的主风险模型。

随后读取 [`references/review-checks.md`](./references/review-checks.md)：先对全部 changed hunks 执行必经基础检查，再执行由系统承诺、主风险模型和 changed hunk 真实触发的运行时模型、专项检查和模块检查。触发字段检查时，必须追到首个真实 validator 和最终 consumer/sink，不能停在 DTO 或 assembler 构造成功处。

## 5. 审查完整 diff

1. 从 producer、adapter、validator、consumer/sink、fallback、历史数据、持久化、迁移、等价路径、测试和文档中建立必要的 contract surfaces。
2. 检查所有 changed files，并读取足够的上下文、调用点和测试来证明问题是否成立。
3. 对账新增路径与被替换、删除、绕过或收窄的旧路径。
4. 至少推演一个与本 PR 有关的反例：历史数据、异常、权限差异、并发、刷新时序、任务交错、性能规模或治理失败。
5. 只报告满足以下条件的问题：
   - 影响正确性、安全、性能、架构或维护性；
   - 离散、可操作，并有具体触发路径和可观察影响；
   - 由本分支引入、暴露或实质加重；
   - 作者知道后大概率会修复。

不要报告纯风格、个人偏好、无后果的“可更优雅”、无法证明的推测或与 diff 无关的既有问题。相同根因合并成一条 finding。

## 6. 关闭 coverage ledger

按 [`references/coverage-and-output.md`](./references/coverage-and-output.md) 建立并关闭：

- changed-file ledger：完整覆盖脚本返回的 changed files；
- contract-surface ledger：覆盖每条系统承诺、每个主风险模型和每个被触发的专项检查。

`mechanical` 和 `not-applicable` 是读完对应 diff 后的审查结论，不是免审标签。任何 `deferred`、缺失条目、数量不守恒或缺失映射都表示 coverage incomplete。

## 7. 重新校验并输出

输出前重新运行：

```sh
node "$(git rev-parse --show-toplevel)/.codex/skills/kuzhambu-pr-code-review/scripts/collect-review-context.mjs" snapshot --base main
```

比较 `head`、`base_sha`、`merge_base`、`diff_hash` 和完整 changed-file 集。任一项变化都使原 ledger 失效；必须按新 diff 重建并重新审查。

基线稳定后，严格使用 [`references/coverage-and-output.md`](./references/coverage-and-output.md) 的 finding、ledger 和 summary 格式。保留 P0、P1、P2、P3 四级：P3 只用于低风险但确实值得作者修改的维护性、结构性或演进问题，不能用来容纳风格偏好。
