# PR Workflow

## 1. Purpose

本文档定义 kuzhambu 的 Pull Request 合并前验证流程。

目标是保证 PR 合并前固定执行明确、可读的 workflow 步骤，并让新增模块验证接入同一 workflow。

## 2. Scope

当前范围：

- GitHub Pull Request workflow
- PR workflow 显式验证步骤
- PR 标题和描述要求
- 文档、TODO 和 RUNBOOK 收口要求

不在范围内：

- 不定义发布流程
- 不定义分支保护配置的 GitHub UI 操作
- 不伪造没有构建系统模块的验证命令

## 3. Bounded Context

Commit 是工程判断记录，可以表示阶段任务中的中间判断。

PR 是阶段性交付边界。PR 合并前必须完整、可编译、可测试，并完成文档、TODO 和 RUNBOOK 收口。

PR 合并前固定执行 `.github/workflows/pr-verify.yml`。workflow 必须显式声明治理文件检查，并按变更目录触发后端 Maven 验证、前端 package manifest 校验、前端 Prettier 检查、前端 ESLint 检查、前端 Vitest、worker manifest 校验、workers ruff 检查和 workers pytest no-capture 验证；不得用一个 shell 脚本隐藏 PR 必过项。

## 4. Module Mapping

- `.github/workflows/pr-verify.yml`: GitHub PR 触发入口。
- `scripts/verify-all.sh`: 本地辅助 verify 编排入口，不作为 PR workflow 的必过入口。
- `.github/pull_request_template.md`: PR 描述模板。
- `docs/00-governance/TODO-RULES.md`: TODO、commit、PR 和 verify protocol 的主规则。

## 5. Global Constraints

- 代码变更必须先落到独立分支，再通过 Pull Request 进入 `main`；禁止将开发中的改动直接 push 到 `main` 或绕过 PR 直接合并。
- PR 合并前必须通过统一 verify workflow。
- PR 标题固定使用 `Type(scope): 中文说明`。
- PR 描述固定使用 `.github/pull_request_template.md`。
- PR 必须完成阶段任务对应的文档、TODO 和 RUNBOOK 收口。
- workflow 必须直接展示 PR 必过验证步骤；新增项目验证能力时必须同步接入 `.github/workflows/pr-verify.yml`。
- 本地辅助脚本可以复用同等验证能力，但不得成为 PR workflow 唯一可见入口。
- PR 自动验证只包含已自动化 testcase；未自动化 testcase 不得伪装为 PR 必过项。
- 没有构建系统或验证命令的模块不得在 workflow 中伪造空验证。
- Java servers 验证要求本地或 CI 使用 Java 17；不得使用 Java 8 或 Java 11 运行 Maven 验证。
- Java servers 目录发生变更时，PR 验证必须显式执行 `mvn -q clean`、`mvn -q spotless:check`、`mvn -q checkstyle:check` 和 `mvn -q test`。
- Java servers 验证必须检查 `common`、`biz`、`starter` 三段式布局，并拒绝继续保留旧 `kuzhambu-servers/interfaces` 入口。
- Apps 目录发生变更时，PR 验证必须使用 Node 20，并显式执行 `npm ci`、`npm run format:check`、`npm run lint` 和 `npm test`。
- Python workers 目录发生变更时，PR 验证必须使用 Python 3.10，并显式执行 `ruff format --check .`、`ruff check .` 和 `python -m pytest -p no:capture`。
- PR workflow 或 PR 模板发生变更时，PR 验证必须触发 servers、workers、apps 和 db 的显式检查，以验证验证规则本身。
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
3. workflow 显式执行治理文件检查，并按变更目录执行后端布局检查、Classics SQL seed 检查、servers `mvn -q clean`、`mvn -q spotless:check`、`mvn -q checkstyle:check`、`mvn -q test`、前端 package manifest 校验、apps `npm ci`、`npm run format:check`、`npm run lint`、`npm test`、worker manifest 校验、workers `ruff format --check .`、`ruff check .` 和 `python -m pytest -p no:capture`。
4. 所有当前自动化验证通过后，PR 才允许进入合并判断。
5. PR 审核通过后，才允许合并到 `main`；合并时默认执行普通 merge，例如 `gh pr merge <number> --merge --delete-branch`。
