# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。
- 当前任务项按 `T01` 到 `T13` 的编号顺序执行。

## 当前任务项

- [ ] `T12 Frontend verification`：运行 admin-web 前端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/system/role`；`kuzhambu-apps/admin-web/src/pages/system/user`；`kuzhambu-apps/admin-web/src/pages/system/menu`；`kuzhambu-apps/admin-web/src/pages/system/department`；`kuzhambu-apps/admin-web/src/pages/audit/audit-log`
    - 处理动作：运行 RUNBOOK 中列出的 admin-web 格式化检查、lint、测试和前端协议字段扫描。
    - 验收点：前端验证通过；若存在失败，TODO 收窄为失败页面、控件或协议字段对应的剩余任务。
    - 重要度：9/10

- [ ] `T13 Runbook cleanup`：清理临时 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-SYSTEM-DOMAIN-STRONG-TYPING.md`
    - 处理动作：在实现、验证和必要文档沉淀完成后删除临时 RUNBOOK。
    - 验收点：强类型化闭环完成后仓库不再保留该 RUNBOOK；如发现长期规则，已先迁移到对应治理文档。
    - 重要度：9/10

## 待审阅任务项

## 待讨论项
