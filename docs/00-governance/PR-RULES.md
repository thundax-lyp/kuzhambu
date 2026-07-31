# PR Rules

## 1. Purpose

本文档定义 kuzhambu 的 Pull Request 合并前验证规则。

目标是保证 PR 合并前固定执行明确、可读的 workflow 步骤，并让新增模块验证接入同一 workflow。

## 2. Scope

当前范围：

- GitHub Pull Request workflow
- PR workflow 显式验证步骤
- PR 标题和描述要求
- 文档、TODO 和 RUNBOOK 收口要求
- 阶段性交付的业务完成点、验证证据、未覆盖项和跨域影响说明

不在范围内：

- 不定义发布流程
- 不定义分支保护配置的 GitHub UI 操作
- 不伪造没有构建系统模块的验证命令
- 不定义本地并发开发方式；开发者可以使用多个 clone、worktree 或其他本地组织方式，但 PR 仍必须满足本文档的交付边界

## 3. Bounded Context

Commit 是工程判断记录，可以表示阶段任务中的中间判断。

PR 是阶段性交付边界。PR 合并前必须完整、可编译、可测试，并完成文档、TODO 和 RUNBOOK 收口。

PR 合并前固定执行 `.github/workflows/pr-verify.yml`。workflow 必须显式声明治理文件检查，并按变更目录触发后端 Maven 验证、前端 package manifest 校验、前端 Prettier 检查、前端 ESLint 检查、前端 Vitest、worker manifest 校验、workers ruff 检查和 workers pytest no-capture 验证；不得用一个 shell 脚本隐藏 PR 必过项。

## 4. Module Mapping

- `.github/workflows/pr-verify.yml`: GitHub PR 触发入口。
- `scripts/verify-all.sh`: 本地辅助 verify 编排入口，不作为 PR workflow 的必过入口。
- `.github/pull_request_template.md`: PR 描述模板。
- `docs/00-governance/TODO-RULES.md`: TODO 清理、收窄和 PR 收口规则。

## 5. Global Constraints

- 代码变更必须先落到独立分支，再通过 Pull Request 进入 `main`；禁止将开发中的改动直接 push 到 `main` 或绕过 PR 直接合并。
- PR 合并前必须通过统一 verify workflow。
- PR 标题固定使用 `Type(scope): 中文说明`。
- PR 描述固定使用 `.github/pull_request_template.md`。
- PR 必须完成阶段任务对应的文档、TODO 和 RUNBOOK 收口；TODO 清理或收窄的具体规则以 `TODO-RULES.md` 为准。
- PR 应尽量只承载一个可验收业务闭环；如果同一 PR 跨多个业务域，必须在 PR 描述中说明不可拆分原因、跨域影响和额外验证范围。
- PR 必须明确记录业务完成点、验证证据、未覆盖项、跨域影响、文档、TODO 与 RUNBOOK 收口状态。
- `PR-RULES.md` 增加 PR 描述必填信息时，必须同步更新 `.github/pull_request_template.md`。
- workflow 必须直接展示 PR 必过验证步骤；新增项目验证能力时必须同步接入 `.github/workflows/pr-verify.yml`。
- 本地辅助脚本可以复用同等验证能力，但不得成为 PR workflow 唯一可见入口。
- PR 自动验证只包含已自动化 testcase；未自动化 testcase 不得伪装为 PR 必过项。
- 没有构建系统或验证命令的模块不得在 workflow 中伪造空验证。
- Java servers 验证要求本地或 CI 使用 Java 17；不得使用 Java 8 或 Java 11 运行 Maven 验证。
- Java servers 目录发生变更时，PR 验证必须显式执行 `mvn -q spotless:check`、`mvn -q checkstyle:check` 和 `mvn -q test`；CI runner 使用干净 checkout，不要求单独执行 `mvn -q clean`。
- Java servers PR workflow、根级构建配置、`common` 模块组或 Maven 聚合 POM 发生变更时，PR 验证必须执行全量 Maven 验证；其他 leaf module 变更可以使用 Maven reactor `-pl ... -am -amd` 裁剪到受影响模块、其依赖和依赖它的模块。
- Java servers 验证必须检查 `common`、`biz`、`starter` 三段式布局，并拒绝继续保留旧 `kuzhambu-servers/interfaces` 入口。
- Apps 目录发生变更时，PR 验证必须使用 Node 20，并显式执行锁文件安装、`pnpm run format:check`、`pnpm run lint` 和全量或受影响范围的 Vitest；GitHub Actions 中使用 `pnpm install --frozen-lockfile` 和 pnpm 缓存。
- Apps 子 workspace 目录发生变更时，PR 验证按 workspace 裁剪执行对应 `format:check`、`lint` 和 `test`。`admin-web` 仅变更 `src/pages/<module>/` 时，Vitest 可以裁剪为 `src/app.test.tsx`、全部受影响 page module 和依赖变更文件的模块外测试；`admin-web` 共享代码、配置或 page module 之外的文件发生变更时必须执行该 workspace 全量测试。`kuzhambu-apps/` 根级文件或 PR workflow 发生变更时必须验证全部 frontend workspace。
- Python workers 目录发生变更时，PR 验证必须使用 Python 3.10，并显式执行 `ruff format --check .`、`ruff check .` 和 `python -m pytest -p no:capture`。
- PR workflow、PR 规则文档或 PR 模板发生变更时，PR 验证必须触发 servers、workers、apps 和 db 的显式检查，以验证验证规则本身。
- PR 合并默认使用普通 merge commit，保留分支中的小步 commit 历史；不得默认 squash。
- Storage 文件读取、预览、下载或分享资源访问发生变更时，PR 验证记录必须说明资源归属校验、下载权限边界、后端契约测试、前端资源 URL 拼接验证，以及未自动化人工冒烟项。

## 6. PR Description

PR 描述固定包含：

- `Business Closure`: 说明本 PR 形成的业务完成点；如果只是治理、验证或文档收口，也必须说明完成的交付边界。
- `Scope`: 说明主要改动范围，并说明是否跨业务域。
- `Verification Evidence`: 记录已运行验证命令、结果和人工或运行时冒烟证据；未自动化验证不得伪装为 workflow 必过项。
- `Not Covered`: 说明本 PR 未覆盖、未自动化或刻意不纳入的事项；没有未覆盖项时明确写 `无`。
- `Cross-domain Impact`: 说明是否影响其他业务域、facade、接口、权限、菜单、配置、Storage owner、AI final-state、Workers usecase、Operations summary 等跨域契约。
- `Documentation, TODO And RUNBOOK Closure`: 确认需求、接口、readiness、治理文档是否同步；确认相关 TODO 已按 `TODO-RULES.md` 清理或收窄；确认临时 RUNBOOK 已删除，或证据已沉淀到指定 readiness/evidence 文档。
- `Risks`: 说明剩余风险、运行时依赖或上线前仍需关注的事项。

## 7. Key Flow

1. 开发者打开或更新 Pull Request。
2. GitHub 触发 `PR Verify` workflow。
3. workflow 显式执行治理文件检查，并按变更目录执行后端布局检查、Classics SQL seed 检查、servers 全量或受影响 Maven reactor 模块的 `mvn -q spotless:check`、`mvn -q checkstyle:check`、`mvn -q test`、前端 package manifest 校验、apps 锁文件安装、按受影响 workspace 执行 `format:check`、`lint`、全量或受影响 admin page module 及其模块外依赖测试的 `test`、worker manifest 校验、workers `ruff format --check .`、`ruff check .` 和 `python -m pytest -p no:capture`。
4. 所有当前自动化验证通过后，PR 才允许进入合并判断。
5. PR 审核通过后，才允许合并到 `main`；合并时默认执行普通 merge，例如 `gh pr merge <number> --merge --delete-branch`。
