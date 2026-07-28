---
name: kuzhambu-push-pr
description: Kuzhambu branch-to-PR closeout workflow for direct slash-command invocation. This skill reads PR-RULES, validates scope, pushes a non-main branch, creates or updates the GitHub PR, watches checks and comments, fixes actionable feedback, replies, and leaves the PR ready for human review. Do not merge unless the user explicitly asks to merge.
---

# Kuzhambu Push And PR

将当前已完成的小步工作收口为 GitHub Pull Request，并持续处理 PR 上的 Codex 或 reviewer 评论，直到 PR 处于可审状态。

## 调用方式

本 skill 只用于 slash command 直接调用，不定义额外自然语言触发或适用语义。

## 核心边界

- 必须通过 `branch -> PR -> review -> merge`，不得把开发改动直接 push 到 `main`。
- 本 skill 可以 push、创建 PR、更新 PR、检查 CI、读取和回复 PR 评论、提交修复。
- 不要 merge PR，除非用户明确要求“合并 PR”。
- 不要 squash merge，除非用户明确要求压缩历史。
- 如果当前工作区有未提交改动，先判断是否属于本 PR；属于则按项目提交规则小步提交，不属于则不要混入。

## 必读上下文

1. Read `docs/AGENTS.md` for document routing.
2. Read `docs/00-governance/PR-RULES.md` completely.
3. Read `docs/00-governance/TODO-RULES.md` for commit and closeout rules.
4. Read `.github/pull_request_template.md`.
5. Inspect `.github/workflows/pr-verify.yml` enough to know which checks this PR should trigger.

按 diff 类型继续读取最小必要治理文档：

- Java servers 变更：`docs/00-governance/ARCHITECTURE.md`、`docs/00-governance/SERVERS-ARCHITECTURE.md`。
- admin-web 变更：`docs/00-governance/ARCHITECTURE.md`、`docs/00-governance/ADMIN-WEB-RULES.md`。
- portal-web 变更：`docs/00-governance/ARCHITECTURE.md`、`docs/00-governance/PORTAL-WEB-RULES.md`。
- Python workers 变更：`docs/00-governance/ARCHITECTURE.md`、`docs/00-governance/WORKERS-RULES.md`。
- 文档、TODO、PR、workflow 或收口流程变更：`docs/00-governance/DOCUMENT-RULES.md`、`docs/00-governance/PR-RULES.md`、`docs/00-governance/TODO-RULES.md` 中与 diff 相关的文件。

## 工作流

### 1. 盘点当前状态

先执行并阅读：

```sh
git status
git branch --show-current
git log --oneline --decorate --max-count=12
git diff --stat
git diff --cached --stat
git diff --stat main...HEAD
```

判断：

- 当前分支是否是 `main`。
- 是否有未提交改动。
- 当前分支相对 `main` 的提交和文件范围。
- 是否存在 TODO、RUNBOOK、文档或验证证据未收口。

### 2. 分支安全

- 如果当前分支不是 `main`，在该分支上继续 PR 收口。
- 如果当前分支是 `main` 且存在本地领先 `origin/main` 的工作提交，不要 push `main`；创建一个语义清楚的新分支承载当前 HEAD，例如 `docs/codex-skills-review-workflow`、`feat/<scope>` 或 `fix/<scope>`。
- 如果当前分支是 `main` 且没有可 PR 的提交，停止并说明没有可创建 PR 的分支差异。
- 不要执行破坏性命令恢复 `main`，除非用户明确要求。

### 3. 本地收口

按 `TODO-RULES.md` 检查：

- commit 是否小步且有明确工程判断。
- 是否混入无关改动。
- TODO 是否已清理或收窄。
- 临时 RUNBOOK 是否已删除，或证据是否已迁移到 readiness/evidence 文档。
- 行为、配置、流程、验证或文档口径变化是否已同步相应文档。

根据 diff 类型运行最小相关验证。优先使用项目规定命令：

- Java servers：先运行 touched files 的 formatter，再运行相关 `spotless:check`、`checkstyle:check`、`test`。
- Frontend apps：先运行相关 formatter，再运行 `format:check`、`lint`、`test`。
- Python workers：先运行相关 formatter，再运行 `ruff format --check`、`ruff check`、`pytest -p no:capture`。
- Docs/skills/prompt-only changes：至少运行 `git diff --check`，并检查旧路径引用、断链或治理入口是否需要更新。

如果无法运行某项验证，必须在 PR 描述的 `Verification Evidence` 或 `Risks` 中明确说明原因。

### 4. Push

- Push 当前非 main 分支到 origin。
- 如果 upstream 不存在，使用 `git push -u origin <branch>`。
- Push 后读取远端结果，确认分支已发布。

### 5. 创建或更新 PR

使用可用的 GitHub 工具或 `gh`：

- 如果当前分支已有 open PR，更新该 PR。
- 如果没有 open PR，创建指向 `main` 的 PR。
- PR 标题使用 `Type(scope): 中文说明`。
- PR 描述必须使用 `.github/pull_request_template.md` 的结构，并完整填写：
  - `Business Closure`
  - `Scope`
  - `Verification Evidence`
  - `Not Covered`
  - `Cross-domain Impact`
  - `Documentation, TODO And RUNBOOK Closure`
  - `Risks`
- 不要把未运行的验证伪装为已通过；未覆盖项明确写在 `Not Covered` 或 `Risks`。

创建或更新 PR 时，必须基于当前远端 HEAD 的最终 commits 和 diff 重新整合 PR 信息：

- 重新判断 PR 标题是否仍能概括完整交付边界；不准确时更新标题。
- 重新生成 `Business Closure`、`Scope`、`Verification Evidence`、`Not Covered`、`Cross-domain Impact`、`Documentation, TODO And RUNBOOK Closure` 和 `Risks`。
- 删除已被后续 commit、review fix 或历史整理淘汰的旧 SHA、旧文件范围、旧验证状态和过程性描述。
- 保留仍然有效的人工背景、取舍、风险说明和未覆盖项；不要用自动生成内容覆盖用户仍需保留的信息。
- 以远端 PR 的 commits、diff 和 checks 为准，不要仅根据本地 commit message 推断完成状态。
- 每轮 review fix push 后，如果交付范围、验证证据、未覆盖项或风险发生变化，再次同步 PR 信息。

### 6. 等待 PR 完成

创建或更新 PR 后，开启最长 5 分钟的观察窗口；步骤 7 中每轮修复 push 后重新开启一个最长 5 分钟的观察窗口。观察期间每 20 至 30 秒检查：

- PR URL 和编号。
- GitHub Actions / required checks 状态。
- PR review 状态。
- Codex comments、review comments、issue comments 和 check annotations。

checks 和 review 已完成且状态稳定时提前结束观察；发现明确失败或 actionable feedback 时立即进入处理，不必等待当前窗口结束。5 分钟到期后仍有 checks 或 review 正在运行时，停止本轮等待，报告当前状态和下一步检查方式；等待超时本身不视为失败。

优先使用 GitHub connector 获取 PR 信息；如果需要 thread-level review comment 状态或 connector 不足，使用 `gh` 查询。

### 7. 处理 Codex 和 reviewer 评论

对每条 Codex/reviewer 评论分类：

- `Actionable`: 指出明确 bug、风险、缺失验证、文档或可执行修复。
- `Question`: 需要作者解释或确认。
- `Non-actionable`: 风格偏好、误报、已被现有代码/文档覆盖、或不属于本 PR。

处理规则：

- 对 `Actionable` 评论，读取相关代码和上下文，修复后运行最小相关验证，提交并 push。
- 对 `Question` 评论，回复清楚依据、取舍和下一步。
- 对 `Non-actionable` 评论，礼貌回复为什么不改，并给出代码或规则依据。
- 不要只回复“done”；回复必须说明改了什么、验证了什么，或为什么不改。
- 如果评论无法处理，明确记录 blocker 和需要用户决策的问题。

每轮 push 后重新检查：

- PR diff 是否符合预期。
- CI 是否重新触发并最终通过或明确失败原因。
- Codex/reviewer 是否还有未处理 actionable 评论。

### 8. 完成标准

只有同时满足以下条件，才报告 PR 已完成：

- 当前工作区干净。
- 非 main 分支已 push。
- PR 已创建或更新，并给出 URL。
- PR 标题和描述已按远端最终 commits、diff 和 checks 重新整合，符合 PR 模板和 `PR-RULES.md`。
- 本地最小相关验证已运行，或未运行原因已明确记录。
- GitHub checks 通过；如果 5 分钟观察窗口结束时仍在运行，报告当前状态和下一步检查方式。
- Codex/reviewer actionable comments 已处理并回复；如果仍有未处理项，明确列出。
- 没有未清理的 TODO/RUNBOOK 收口问题，或 PR 描述已说明剩余风险。

## 输出格式

完成后输出：

```md
## PR closeout summary

* Branch:
* PR:
* Push status:
* Checks:
* Codex/reviewer comments:
* Local verification:
* Documentation/TODO/RUNBOOK closure:
* Remaining risks:
* Next action:
```

如果创建了 PR，必须给出 PR URL。
