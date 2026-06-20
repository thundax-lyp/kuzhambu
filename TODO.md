# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `classics share target version binding`：分享目标绑定正式内容版本
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-VERSIONABLE.md`
    - 范围对象：`classics_share_target`、分享创建应用服务、分享管理展示
    - 处理动作：为分享目标增加版本绑定字段并在创建分享时调用 `ensureVersioned`
    - 验收点：分享 target 绑定正式版本并冻结快照，管理侧能展示分享版本与当前内容版本差异
    - 重要度：8/10

## 待讨论项
