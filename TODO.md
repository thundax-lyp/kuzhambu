# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `portal-web classics share page`：实现 Portal Web 分享快照展示页
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`share-page.tsx`、`share-page.test.tsx`、`styles.css`
    - 处理动作：展示 link 元信息、target 列表和固化快照详情
    - 验收点：`/share/:shareToken` 能展示快照且不调用 Admin API
    - 重要度：8/10

- [ ] `classics share validation`：运行分享链路测试和 dev.env 冒烟
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`kuzhambu-servers`、`kuzhambu-apps`、`dev.env`
    - 处理动作：执行后端、前端最小验证，并用 `dev.env` 完成 Admin 创建分享和 Portal 访问冒烟
    - 验收点：验证命令和冒烟结果可写入 PR 描述
    - 重要度：9/10

- [ ] `classics share docs readiness`：同步分享快照收口文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/20-interfaces/`、`docs/00-governance/SERVERS-ARCHITECTURE.md`、`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 处理动作：同步三类版本快照 schema、接口、配置、覆盖度状态，并在 PR 收口时删除或收窄 RUNBOOK
    - 验收点：`docs/20-interfaces/` 固定三类正式版本 `snapshot_json` 字段，文档口径与实现和验证结果一致
    - 重要度：7/10

- [ ] `classics share cleanup`：清理分享快照任务现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`、本地临时调试产物、当前工作区
    - 处理动作：PR 收口前清理已完成 TODO、删除或收窄临时 RUNBOOK、移除临时调试产物并确认工作区不混入无关修改
    - 验收点：`git status` 只剩预期改动，临时 RUNBOOK 已按治理规则处理，TODO 不保留已完成项
    - 重要度：8/10

## 待讨论项
