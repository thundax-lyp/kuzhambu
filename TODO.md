# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 本清单按 `RUNBOOK-GRAPH-ADMIN-WEB.md` 的固定顺序拆分；每项预计触及 2–10 个文件，并对应一个可独立验证的小步提交。
- 未经本轮人工确认的执行任务均在“待审阅任务项”；确认后才移入“当前任务项”执行。
- 已完成任务必须在其完成 commit 中删除；完成历史保留在 commit 和 PR。

## 当前任务项

## 待审阅任务项

- [ ] `24 graph backend 现场清理`：清理已完成任务、RUNBOOK 与临时执行痕迹
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`、`docs/00-governance/TODO-RULES.md`、`docs/00-governance/DOCUMENT-RULES.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-GRAPH-BACKEND.md`，以及本任务创建的临时迁移验证文件（2–10 个文件）
    - 处理动作：在全部交付、迁移核对和 PR 验证记录完成后删除本组 TODO、已完成 RUNBOOK 与临时文件。
    - 验收点：`TODO.md` 不保留完成任务或历史；RUNBOOK 及残留引用已删除；PR 描述已记录接口、迁移和验证结果；工作区无本任务临时文件。
    - 重要度：10/10

## 待讨论项
