# PR Workflow

## 1. Purpose

本文档定义 kuzhambu 的 Pull Request 合并前验证流程。

目标是保证 PR 合并前固定执行统一 verify 入口，并让新增模块验证接入同一 workflow。

## 2. Scope

当前范围：

- GitHub Pull Request workflow
- 统一 verify 入口
- PR 标题和描述要求
- 文档、TODO 和 RUNBOOK 收口要求

不在范围内：

- 不定义发布流程
- 不定义分支保护配置的 GitHub UI 操作
- 不伪造没有构建系统模块的验证命令

## 3. Bounded Context

Commit 是工程判断记录，可以表示阶段任务中的中间判断。

PR 是阶段性交付边界。PR 合并前必须完整、可编译、可测试，并完成文档、TODO 和 RUNBOOK 收口。

PR 合并前固定执行 `.github/workflows/pr-verify.yml`。workflow 固定调用 `scripts/verify-all.sh`。新增或调整验证命令时，必须接入该脚本，避免 GitHub Actions 与本地验证入口分叉。

## 4. Module Mapping

- `.github/workflows/pr-verify.yml`: GitHub PR 触发入口。
- `scripts/verify-all.sh`: 仓库统一 verify 编排入口。
- `.github/pull_request_template.md`: PR 描述模板。
- `docs/00-governance/TODO-RULES.md`: TODO、commit、PR 和 verify protocol 的主规则。

## 5. Global Constraints

- 代码变更必须先落到独立分支，再通过 Pull Request 进入 `main`；禁止将开发中的改动直接 push 到 `main` 或绕过 PR 直接合并。
- PR 合并前必须通过统一 verify workflow。
- PR 标题固定使用 `Type(scope): 中文说明`。
- PR 描述固定使用 `.github/pull_request_template.md`。
- PR 必须完成阶段任务对应的文档、TODO 和 RUNBOOK 收口。
- workflow 不直接散落项目验证细节，项目验证细节固定收敛到 `scripts/verify-all.sh`。
- 新增项目验证能力时必须同步接入 `scripts/verify-all.sh`。
- PR 自动验证只包含已自动化 testcase；未自动化 testcase 不得伪装为 PR 必过项。
- 没有构建系统或验证命令的模块不得在 workflow 中伪造空验证。
- Java servers 验证要求本地或 CI 使用 Java 11+；当前 Checkstyle 版本不支持 Java 8 运行时。
- Java servers 验证必须检查 `common`、`biz`、`starter` 三段式布局，并拒绝继续保留旧 `kuzhambu-servers/interfaces` 入口。
- PR 合并默认使用普通 merge commit，保留分支中的小步 commit 历史；不得默认 squash。

## 6. PR Description

PR 描述固定包含：

- `Summary`: 说明本 PR 完成的阶段性交付。
- `Scope`: 说明主要改动范围。
- `Verification`: 记录已运行验证命令和结果。
- `Documentation And TODO`: 确认文档、TODO 和 RUNBOOK 收口状态。
- `Risks`: 说明剩余风险、未自动化验证或未完成任务。

## 7. Key Flow

1. 开发者打开或更新 Pull Request。
2. GitHub 触发 `PR Verify` workflow。
3. workflow 执行 `scripts/verify-all.sh`。
4. 所有当前自动化验证通过后，PR 才允许进入合并判断。
5. PR 审核通过后，才允许合并到 `main`；合并时默认执行普通 merge，例如 `gh pr merge <number> --merge --delete-branch`。
