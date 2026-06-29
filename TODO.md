# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Operations long task repository query`：扩展 long task 查询能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-RUNTIME-CLOSURE.md`
    - 范围对象：`LongTaskSnapshotRepository.java`、`LongTaskSnapshotRepositoryImpl.java`、`LongTaskSnapshotMapper.java`
    - 处理动作：补齐 long task 分页和详情查询
    - 验收点：repository 可支撑 long task `page / detail`
    - 重要度：8/10

- [ ] `Operations long task app-interface`：新增 long task 应用与接口读闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-RUNTIME-CLOSURE.md`
    - 范围对象：`kuzhambu-operations-application` 与 `kuzhambu-operations-interface` 下 task query、result、service、controller 文件
    - 处理动作：新增 long task `page / detail` 应用服务与 admin API
    - 验收点：暴露 `/api/operations/task/page` 和 `/api/operations/task/detail`
    - 重要度：9/10

- [ ] `Admin Web operations tasks page`：落地统一运维入口页面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-RUNTIME-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/tasks` 与 `src/router/index.tsx`
    - 处理动作：新增 `/operations/tasks` 页面并接入 health summary、long task table 和运维入口跳转
    - 验收点：页面可展示 health summary、long task 列表，并提供 report / backup-restore / cleanup 入口
    - 重要度：9/10

- [ ] `Admin Web operations cleanup page`：落地 cleanup 管理页面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-RUNTIME-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/cleanup` 与 `src/router/index.tsx`
    - 处理动作：新增 `/operations/cleanup` 页面并接入 cleanup 触发、列表、详情与失败项查看
    - 验收点：页面支持 cleanup 执行、job 列表展示和 detail / failure item 查看
    - 重要度：9/10

- [ ] `Operations readiness coverage`：更新 Operations 实现覆盖文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-RUNTIME-CLOSURE.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：根据 cleanup、health、long task 和统一运维入口的实际落地结果同步 Implementation Coverage
    - 验收点：`OPERATIONS-IMPLEMENTATION-COVERAGE.md` 与代码事实一致，能准确反映已完成、部分完成和未完成项
    - 重要度：8/10

- [ ] `Operations runtime runbook cleanup`：删除已完成 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-RUNTIME-CLOSURE.md`
    - 范围对象：`docs/30-designs/RUNBOOK-OPERATIONS-RUNTIME-CLOSURE.md`
    - 处理动作：在 Operations runtime closure 全部完成后删除本次专项 RUNBOOK
    - 验收点：RUNBOOK 已删除，仓库中不保留已失效的专项执行文档
    - 重要度：7/10

- [ ] `Operations runtime closure cleanup`：清理收口现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`TODO.md`、Operations runtime closure 相关文档与残留临时文件
    - 处理动作：删除已完成 TODO 项，清理本轮遗留的临时说明、无用占位和收口残留
    - 验收点：TODO 只保留未完成项，已完成任务不残留，工作区与文档现场整洁
    - 重要度：8/10

## 待讨论项
