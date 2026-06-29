# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `kuzhambu-apps/admin-web/src`：补齐备份恢复管理页面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src`、`kuzhambu-servers/biz/operations/`
    - 处理动作：为备份、恢复、清理和结果查看补齐 `admin-web` 页面、服务调用和状态展示
    - 验收点：管理员可在 `admin-web` 触发备份和恢复，并查看台账、失败原因和快照记录
    - 重要度：9/10

- [ ] `system 菜单与权限种子数据`：补齐 Operations 菜单入口与权限点
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-BACKUP-RESTORE.md`
    - 范围对象：`system` 菜单种子数据、`system` 权限点种子数据、`admin-web` 菜单与路由映射
    - 处理动作：为备份、恢复、清理和健康检查相关页面补齐菜单入口与权限点
    - 验收点：管理员登录后可看到 `Operations` 下的备份恢复相关菜单，菜单权限与后端接口权限点一致，页面可由菜单稳定进入
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
