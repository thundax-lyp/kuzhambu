# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Knowledge coverage sync`：同步 Knowledge 覆盖文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：把 Portal 页面与图谱浏览分层状态更新为当前实现口径
    - 验收点：覆盖文档不再保留与本轮实现冲突的未完成表述
    - 重要度：7/10

- [ ] `Portal atlas verify`：完成 portal-web 图谱分层验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：本轮新增或修改的 `kuzhambu-apps/portal-web` 文件
    - 处理动作：运行 `format:check`、`lint`、`test`、`build`
    - 验收点：`portal-web` 四项验证全部通过
    - 重要度：8/10

- [ ] `Knowledge atlas hierarchy cleanup`：清理 Atlas 分层 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 范围对象：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-ATLAS-HIERARCHY.md`
    - 处理动作：任务关闭时删除本轮 RUNBOOK
    - 验收点：PR 收口前不残留无剩余用途的 RUNBOOK
    - 重要度：6/10

## 待讨论项
