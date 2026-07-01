# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Sancai Final Verify`：执行全量格式化、静态检查、构建与测试
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`、`kuzhambu-workers/`
    - 处理动作：按仓库规则执行本轮涉及模块的最终格式化、静态检查、构建与测试
    - 验收点：至少完成 `cd kuzhambu-servers && mvn -q spotless:check && mvn -q checkstyle:check && mvn -q test`、`cd kuzhambu-apps && npm run format:check && npm run lint && npm run build && npm test`，若 workers 改动则完成 `cd kuzhambu-workers && .venv/bin/python -m ruff format --check . && .venv/bin/python -m ruff check . && .venv/bin/python -m pytest -p no:capture`
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
