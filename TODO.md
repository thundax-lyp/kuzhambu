# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `13-validation`：13 运行格式、静态搜索和 storage 模块测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-application`、`kuzhambu-servers/biz/storage/kuzhambu-storage-interface`
    - 处理动作：运行 RUNBOOK 指定的 formatter、测试和静态搜索验证
    - 验收点：storage application/interface 相关验证通过，或明确记录不可运行原因和剩余风险
    - 重要度：10/10

- [ ] `14-runbook-closure`：14 清理 RUNBOOK 并收窄 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-STORAGE-APPLICATION-STRONG-CONTRACT.md`、`TODO.md`
    - 处理动作：任务完成后删除临时 RUNBOOK，并删除或收窄已完成 TODO 项
    - 验收点：PR 收口时无已完成 TODO 残留，临时 RUNBOOK 已删除或结论已迁移到稳定文档
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
