# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `kuzhambu-servers/biz/operations`：迁移运营运维域代码和接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`kuzhambu-servers/biz/kuzhambu-biz-operations/`、`kuzhambu-servers/infra/kuzhambu-infra-operations/`、`kuzhambu-servers/biz/operations/`
    - 处理动作：将 operations 迁移到独立运营运维域四层模块并更新 package/import
    - 验收点：operations 域代码位于 `com.thundax.kuzhambu.operations` 包下，不属于 system 或 starter，且只保存本领域自有规则和表
    - 重要度：9/10

- [ ] `kuzhambu-servers/starter`：创建后台和前台启动应用
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`kuzhambu-servers/starter/kuzhambu-admin-starter/`、`kuzhambu-servers/starter/kuzhambu-portal-starter/`
    - 处理动作：将旧 admin/portal 启动入口调整为 starter，并限制各自扫描 admin 或 portal 接口包
    - 验收点：admin starter 不暴露 portal Controller，portal starter 不暴露 admin Controller
    - 重要度：9/10

- [ ] `scripts verify`：更新验证入口并完成迁移验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`scripts/verify-all.sh`、`docs/40-readiness/PR-WORKFLOW.md`、`kuzhambu-servers/`
    - 处理动作：更新验证入口并运行 Maven reactor、架构规则和统一验证脚本
    - 验收点：相关验证命令通过，PR 工作流记录新的验证口径
    - 重要度：10/10

- [ ] `repo cleanup`：清理旧模块、旧文档和 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`kuzhambu-servers/`、`docs/10-requirements/`、`docs/30-designs/`、`db/`、`TODO.md`
    - 处理动作：删除旧横向模块、旧需求设计文档、旧 SQL 文件，并在 PR 收口前删除本 RUNBOOK
    - 验收点：旧入口不再被引用，已完成 TODO 已删除或收窄
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
