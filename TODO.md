# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Sharing Admin regression`：验证批量分享记录仍可在分享管理页维护
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.test.tsx`
    - 处理动作：验证批量创建出的 share link 可列表展示并继续使用现有 status update。
    - 验收点：`ACTIVE`、`EXPIRED`、`REVOKED` 管理行为不回退。
    - 重要度：7/10

- [ ] `Portal Web share regression`：验证 Portal 分享读取状态语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/share/share-service.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-types.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-service.test.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-page.tsx`、`kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`
    - 处理动作：补充 Portal Web 分享读取回归，不新增 Portal 批量入口。
    - 验收点：`ACTIVE` 可读，`EXPIRED` / `REVOKED` 按现有错误语义处理，response 字段不变时不新增 Portal 类型字段。
    - 重要度：8/10

- [ ] `Implementation Coverage`：更新已关闭能力的 implementation coverage
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：在代码和测试通过后，将已关闭项更新为完成并保留剩余未完成项。
    - 验收点：coverage 状态与实际交付和测试结果一致。
    - 重要度：9/10

- [ ] `RUNBOOK closure`：清理阶段性 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-CLASSICS-AI-WORKERS-CLOSURE.md`、`TODO.md`
    - 处理动作：阶段目标关闭后删除 RUNBOOK，并从 TODO 中删除或收窄已完成任务。
    - 验收点：PR 收口前不保留已完成的临时 RUNBOOK 和已完成 TODO。
    - 重要度：8/10

- [ ] `Servers full verification`：执行 Java servers 全量 format -> checkstyle -> compile -> test
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/`
    - 处理动作：按顺序执行 Java servers 全量格式检查、静态检查、编译和测试。
    - 验收点：按 `format -> checkstyle -> compile -> test` 顺序通过 `mvn spotless:check`、`mvn checkstyle:check`、`mvn -DskipTests compile`、`mvn test`。
    - 重要度：10/10

- [ ] `Apps full verification`：执行 frontend apps 全量 format -> lint -> build -> test
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-apps/`
    - 处理动作：按顺序执行 frontend apps 全量格式检查、lint、构建和测试。
    - 验收点：按 `format -> lint -> build -> test` 顺序通过 `npm run format:check`、`npm run lint`、`npm run build`、`npm run test`。
    - 重要度：10/10

- [ ] `Workers full verification`：执行 Python workers 全量 format -> lint -> compile -> test
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-workers/`
    - 处理动作：按顺序执行 Python workers 全量格式检查、lint、编译检查和测试。
    - 验收点：按 `format -> lint -> compile -> test` 顺序通过 `ruff format --check`、`ruff check`、`python -m compileall src`、`pytest -p no:capture`。
    - 重要度：10/10

## 待讨论项
