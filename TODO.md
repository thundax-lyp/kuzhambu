# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `repo/final-verification`：执行最终全量格式化与校验收口
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`、`kuzhambu-workers/`
    - 处理动作：按仓库治理要求完成最终全量 `spotless:apply/checkstyle/test`、`format/lint/build/test`、`ruff format/check/pytest` 校验
    - 验收点：后端、前端、workers 的最终全量格式化和验证命令全部通过
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
