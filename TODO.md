# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `classics/mingcustoms cleanup`：清理明代习俗闭环调试现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`dev.env`、本地运行进程、本地日志、`git status`
    - 处理动作：清理冒烟测试数据、停止本地服务、移除临时日志并确认工作区只保留本轮应提交文件。
    - 验收点：dev.env 无临时测试脏数据，本地无遗留调试进程，`git status` 无非预期改动。
    - 重要度：9/10

- [ ] `classics/mingcustoms docs-cleanup`：更新覆盖状态并清理手册
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`、`TODO.md`
    - 处理动作：更新明代习俗覆盖状态，收口时删除 RUNBOOK 并清空已完成 TODO。
    - 验收点：PR 前无过期 RUNBOOK，TODO 只保留未完成任务。
    - 重要度：9/10

## 待审阅任务项

## 待讨论项
