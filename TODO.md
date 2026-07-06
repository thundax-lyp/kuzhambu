# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Discovery 分支同步`：同步 main 分支最新代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-DISCOVERY-FILTER-PERMISSION-CLOSURE.md`
    - 范围对象：`codex/discovery-filter-permission-loop`、`main`、`/Volumes/storage/workspace/kuzhambu-discovery-filter-permission-loop`
    - 处理动作：在验证前同步 `main` 分支最新代码并解决本任务范围内冲突
    - 验收点：工作分支包含 `main` 最新代码，冲突处理不混入无关改动
    - 重要度：9/10

- [ ] `Discovery 筛选权限验证`：运行后端与 Portal 最小验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-FILTER-PERMISSION-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra`、`kuzhambu-apps/portal-web`
    - 处理动作：在同步 `main` 后执行 RUNBOOK 指定的 Java Spotless/Test 与 Portal format/lint/test 验证
    - 验收点：相关后端与前端验证命令通过，若无法运行则记录明确阻塞原因
    - 重要度：9/10

- [ ] `Discovery 收口文档`：更新 coverage 并清理阶段性 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-DISCOVERY-FILTER-PERMISSION-CLOSURE.md`
    - 范围对象：`docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-DISCOVERY-FILTER-PERMISSION-CLOSURE.md`、`TODO.md`
    - 处理动作：将对应能力标记为 `已完成`，删除已无剩余价值的 RUNBOOK，并按完成范围清理 TODO
    - 验收点：coverage 无中间状态，RUNBOOK 已清理，TODO 只保留未完成任务
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
