# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `11-storage-strong-typing-closure`：执行全量验证并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`、`TODO.md`、`kuzhambu-servers/biz/storage`、`kuzhambu-apps/admin-web`
    - 处理动作：运行 RUNBOOK 要求的后端和必要前端验证，完成后删除临时 RUNBOOK 并收窄或删除已完成 TODO。
    - 验收点：后端 storage 测试和必要 admin-web 契约测试通过，RUNBOOK 文件已删除或长期结论已迁移到治理/readiness 文档。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
