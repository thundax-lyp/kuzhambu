# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `readiness/storage-preview`：完成 Storage 预览闭环验证记录
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`
    - 范围对象：`docs/40-readiness/PR-WORKFLOW.md`、`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`、`TODO.md`
    - 处理动作：运行 RUNBOOK 要求的后端、前端和人工冒烟验证并更新 readiness 覆盖记录。
    - 验收点：PR 收口材料记录 Maven、npm 和人工冒烟结果，验证失败项已修复或明确剩余风险。
    - 重要度：8/10
- [ ] `cleanup/storage-preview-runbook`：清理 Storage 预览闭环现场任务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`、`docs/40-readiness/PR-WORKFLOW.md`
    - 处理动作：在功能、验证和文档同步完成后删除临时 RUNBOOK 并清空或收窄已完成 TODO。
    - 验收点：PR 合并前没有已完成任务残留，临时 RUNBOOK 已删除，工作区只保留交付相关改动。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
