---
name: kuzhambu-prepare-pr-for-merge
description: 在 Kuzhambu PR 合并前检查 Codex 和 reviewer 反馈产生的修复提交，合理归并低信息 review-fix commits，同时保持最终代码树不变。用于完成 review 修复后、人工 merge PR 前的提交历史整理；不负责合并 PR。
---

# Kuzhambu Prepare PR For Merge

将 PR review 期间形成的工作历史整理为可读的交付历史。重点归并 Codex 或 reviewer 反馈产生的重复 fix commits；不要机械 squash 整个 PR，不要 merge PR。

## 调用边界

- 只用于 slash command 直接调用。
- 只处理当前分支对应的 open PR。
- 不修改 `main`，不创建 merge commit，不执行 PR merge。
- 默认只整理已提交历史；工作区不干净时停止，不要暂存、丢弃或混入未提交改动。
- 历史改写前必须展示计划并获得用户明确确认。
- 只使用 `git push --force-with-lease` 更新已整理的远端分支，禁止使用普通 `--force`。

## 必读上下文

依次完整读取：

1. `docs/AGENTS.md`
2. `docs/00-governance/PR-RULES.md`
3. `docs/00-governance/TODO-RULES.md`
4. `.github/pull_request_template.md`

根据 PR diff 按 `docs/AGENTS.md` 继续读取最小必要治理文档。

## 1. 确认安全前提

执行并阅读：

```sh
git status --short --branch
git branch --show-current
git remote -v
git log --oneline --decorate --max-count=30
```

使用 GitHub connector 或 `gh` 确认：

- 当前分支对应唯一的 open PR。
- PR 目标分支、head 分支和当前本地分支一致。
- PR 尚未 merge 或关闭。
- required checks、review 状态和 unresolved review threads。
- PR 的 base ref、base SHA 和当前远端 head SHA；后续 diff 范围固定使用记录的 base SHA，lease 固定使用记录的远端 head SHA。
- Fetch 当前 PR head 后，确认本地 `HEAD` 必须等于记录的远端 head SHA；只有从远端最新 PR head 开始，才允许制定历史整理方案。

遇到以下任一情况时停止并说明原因：

- 当前分支是 `main`。
- 工作区存在修改或未跟踪文件。
- 没有 open PR，或分支对应多个 PR。
- 仍有未处理的 actionable review feedback。
- 分支含其他作者或协作者在当前 review 周期新增的提交，且用户未确认允许改写。
- 本地 `HEAD` 不等于记录的远端 head SHA；不要从陈旧本地历史规划或执行重写。
- 远端分支在分析后出现新的提交。

## 2. 建立 Review 与 Commit 对照

读取 PR 的 commits、Codex reviews、review comments、issue comments、check annotations 和 thread resolution 状态。必要时使用 `gh` GraphQL 查询 thread-level 状态。

对每个疑似 review-fix commit 检查：

- commit 时间是否位于对应 review 之后。
- commit diff 是否直接处理某条反馈。
- commit message、修改文件和前后提交之间的因果关系。
- 修改是否只是补齐原工程判断，还是形成新的独立判断。

不要只根据作者、`Fix` 前缀或提交时间判定 Codex commit。Codex 修复可能由用户身份提交，普通 `Fix` commit 也可能承载独立工程判断。

## 3. 制定整理方案

逐个分类：

- `Fold into original`：修复只是在补齐或纠正某个原始 commit 的同一工程判断，将其 fixup 到该 commit。
- `Combine review fixes`：多个连续或相关修复共同形成一个清晰判断，将它们合并为一个符合 `Type(scope): 中文说明` 的 commit。
- `Keep independent`：修复引入独立能力、测试边界、治理判断或跨模块变化，继续保留独立 commit。
- `Unclear`：无法可靠判断归属，不自动整理，列为需要用户决定的项目。

遵守以下原则：

- 目标是删除 review/fix/replay/resolve 的协作噪声，不是减少 commit 数量本身。
- 不把整个 PR squash 为一个 commit。
- 不跨越无关工程判断合并提交。
- 保留项目要求的小步 commit 历史和 commit message 格式。
- 少于两个可明确归并的 review-fix commits 时，默认不改写历史。

在执行前输出：

```md
## Commit consolidation plan

* PR:
* Current HEAD:
* Original tree:
* Base ref and SHA:
* Original remote head:
* Fold into original:
* Combine review fixes:
* Keep independent:
* Unclear:
* Proposed commit history:
* Validation after rewrite:
```

等待用户明确确认。用户未确认时不得 rebase、reset、amend 或 force push。

## 4. 执行历史整理

确认后：

1. Fetch 远端并再次确认本地 `HEAD` 等于记录的 remote head SHA，且 base SHA 和 remote head SHA 均未变化；任一条件不满足时停止并重新分析。
2. 记录原始 HEAD、`HEAD^{tree}` 和 `<base-sha>...HEAD` 的完整 patch-id 或等价 diff 证据。
3. 创建明确指向原始 HEAD 的本地备份引用，便于恢复。
4. 使用 interactive rebase、fixup/autosquash 或等价的非交互 Git 操作实现已确认方案。
5. 不修改最终文件内容，不顺手格式化、重构或修复新问题。
6. 对比整理前后的 tree、`<base-sha>...HEAD` diff 和文件列表；任何不一致都必须停止，不得 push。
7. 检查最终 commit 顺序、message、作者信息和每个 commit 的工程判断边界。

发生冲突时停止并报告冲突，不要猜测解决。除非用户另有明确指示，保持本地备份引用直到 PR merge。

## 5. 推送与最终检查

验证最终 tree 与整理前完全一致后：

1. 使用只读远端查询再次读取 base SHA 和 remote head SHA，不要通过更新 remote-tracking ref 改变 lease 依据；任一值与分析时记录值不同都必须停止。
2. 使用分析时记录的远端 head SHA 作为显式 lease 推送：

   ```sh
   git push --force-with-lease=refs/heads/<head-branch>:<original-remote-head-sha> origin HEAD:refs/heads/<head-branch>
   ```

3. 读取 PR commits 和 diff，确认远端历史与计划一致。
4. 基于整理后的远端 commits、diff 和 checks 重新整合 PR 信息：
   - 重新判断 PR 标题是否仍能概括最终交付边界；不准确时更新标题。
   - 按 `.github/pull_request_template.md` 重新生成 `Business Closure`、`Scope`、`Verification Evidence`、`Not Covered`、`Cross-domain Impact`、`Documentation, TODO And RUNBOOK Closure` 和 `Risks`。
   - 删除已失效的 commit SHA、旧提交数量、旧文件范围、旧验证状态和 review/fix/replay/resolve 过程描述。
   - 保留仍然有效的人工背景、工程取舍、风险和未覆盖项，不要覆盖用户需要保留的说明。
   - 在 `Verification Evidence` 或 `Risks` 中记录提交历史已整理、最终 tree 未变化以及使用精确 lease 推送的事实。
5. 回读 PR 标题和描述，确认其内容对应整理后的最终远端状态。
6. 检查 required checks 是否因新 SHA 重新触发。
7. 最多观察 5 分钟；每 20 至 30 秒检查 checks 和新增 review 状态。到期仍在运行时报告当前状态，超时不视为失败。
8. 不因 SHA 改写自动请求新一轮 Codex Review；如果最终 tree 发生实质变化，则必须停止并重新审查。

## 完成标准

- PR 最终 tree 与整理前完全一致。
- 低信息 review-fix commits 已按确认方案归并。
- 独立工程判断仍保留。
- 远端分支通过 `--force-with-lease` 安全更新。
- PR diff 与提交列表符合预期。
- PR 标题和描述已按整理后的远端 commits、diff 和 checks 重新整合。
- checks 已通过，或 5 分钟观察窗口结束后已明确报告仍在运行的状态。
- PR 未被 merge。

## 输出格式

```md
## PR merge preparation summary

* PR:
* Original HEAD:
* Final HEAD:
* Tree unchanged:
* Consolidated commits:
* Preserved commits:
* Push status:
* PR information:
* Checks:
* Local backup ref:
* Remaining risks:
* Next action:
```
