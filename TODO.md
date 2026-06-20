# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `wangqi route page`：注册王圻页面和路由
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 范围对象：`wangqi-page.tsx`、`wangqi-page.css`、`kuzhambu-apps/admin-web/src/router/index.tsx`
    - 处理动作：组装 Wangqi 页面 query/mutation/drawer 并注册 `/classics/wangqi` route。
    - 验收点：Admin Web 可从路由进入王圻页面且风格延续古风口径。
    - 重要度：8/10

- [ ] `wangqi frontend tests`：补页面单测和 E2E
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 范围对象：`wangqi-page.test.tsx`、`kuzhambu-apps/admin-web/e2e/classics/wangqi/wangqi.spec.ts`
    - 处理动作：覆盖列表、筛选、详情、保存、删除、文件、版本对比和恢复流程。
    - 验收点：`npm run test` 和 Wangqi E2E 覆盖核心闭环。
    - 重要度：9/10

- [ ] `dev.env smoke`：执行 Wangqi dev.env 冒烟
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 范围对象：`dev.env`、`kuzhambu-servers/starter/kuzhambu-admin-starter`、Wangqi Admin API
    - 处理动作：加载 dev.env 启动 admin starter 并按 RUNBOOK 冒烟 Wangqi API。
    - 验收点：page/timeline/detail/source-file/version/restore API 均满足 RUNBOOK 验收。
    - 重要度：10/10

- [ ] `wangqi closeout`：文档和现场收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`TODO.md`、`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 处理动作：更新覆盖状态、删除完成 TODO、删除 RUNBOOK 并清理本地服务和临时产物。
    - 验收点：`git status --short` 最终为空且无残留 admin/front dev server。
    - 重要度：10/10

## 待讨论项
