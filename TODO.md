# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。
- 当前任务项按 `T01` 到 `T13` 的编号顺序执行。

## 当前任务项

- [ ] `T08 Audit repository ref contract`：用审计 Ref 收敛审计仓储查询契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/repository/AuditLogRepository.java`；`kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/repository/impl/AuditLogRepositoryImpl.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/service/impl/AuditApplicationServiceImpl.java`；`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/audit/service/impl/AuditApplicationServiceImplTest.java`
    - 处理动作：把 `AuditLogRepository` 的对象和操作者查询参数收敛为 `AuditObjectRef` 与 `AuditOperatorRef`。
    - 验收点：`AuditLogRepository` 不再暴露分散的对象 ID 和操作者 ID 字符串参数，且 Ref 内部 ID 字段仍为 `String`。
    - 重要度：9/10

- [ ] `T09 Frontend role and user regression`：回归角色与用户管理控件协议
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/system/role/role-types.ts`；`kuzhambu-apps/admin-web/src/pages/system/role/role-service.ts`；`kuzhambu-apps/admin-web/src/pages/system/role/components/role-edit-drawer/role-edit-drawer.tsx`；`kuzhambu-apps/admin-web/src/pages/system/role/components/menu-tree-field/menu-tree-field.tsx`；`kuzhambu-apps/admin-web/src/pages/system/user/components/user-edit-drawer/user-edit-drawer.tsx`
    - 处理动作：回归角色 drawer 的名称输入、权限开关、状态开关、菜单权限 Tree，以及用户 drawer 的部门 TreeSelect 和角色多选。
    - 验收点：角色保存请求 `menus` 仍是 `[{ id: string }]`，用户保存请求 `departmentId` 仍是 string 或 null 且 `roleIds` 仍是 `string[]`。
    - 重要度：7/10

- [ ] `T10 Frontend menu department audit regression`：回归菜单、部门和审计筛选控件协议
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/system/menu/menu-page.tsx`；`kuzhambu-apps/admin-web/src/pages/system/menu/components/menu-edit-drawer.tsx`；`kuzhambu-apps/admin-web/src/pages/system/department/department-page.tsx`；`kuzhambu-apps/admin-web/src/pages/system/department/components/department-edit-drawer.tsx`；`kuzhambu-apps/admin-web/src/pages/audit/audit-log/components/audit-log-filter.tsx`
    - 处理动作：回归菜单新增/编辑/显示开关/移动、部门新增/编辑/移动，以及审计对象和操作者筛选。
    - 验收点：菜单和部门请求 ID 仍为 string 或 null，审计筛选请求 `objectId` 与 `operatorId` 仍为 string。
    - 重要度：7/10

- [ ] `T11 Backend verification`：运行 system 后端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-domain`；`kuzhambu-servers/biz/system/kuzhambu-system-application`；`kuzhambu-servers/biz/system/kuzhambu-system-infra`；`kuzhambu-servers/biz/system/kuzhambu-system-interface`
    - 处理动作：运行 RUNBOOK 中列出的后端 Maven 格式化、静态检查、测试和 domain 残留基础 ID 扫描。
    - 验收点：后端验证通过；若存在失败，TODO 收窄为失败模块或失败文件对应的剩余任务。
    - 重要度：10/10

- [ ] `T12 Frontend verification`：运行 admin-web 前端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SYSTEM-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/system/role`；`kuzhambu-apps/admin-web/src/pages/system/user`；`kuzhambu-apps/admin-web/src/pages/system/menu`；`kuzhambu-apps/admin-web/src/pages/system/department`；`kuzhambu-apps/admin-web/src/pages/audit/audit-log`
    - 处理动作：运行 RUNBOOK 中列出的 admin-web 格式化检查、lint、测试和前端协议字段扫描。
    - 验收点：前端验证通过；若存在失败，TODO 收窄为失败页面、控件或协议字段对应的剩余任务。
    - 重要度：9/10

- [ ] `T13 Runbook cleanup`：清理临时 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-SYSTEM-DOMAIN-STRONG-TYPING.md`
    - 处理动作：在实现、验证和必要文档沉淀完成后删除临时 RUNBOOK。
    - 验收点：强类型化闭环完成后仓库不再保留该 RUNBOOK；如发现长期规则，已先迁移到对应治理文档。
    - 重要度：9/10

## 待审阅任务项

## 待讨论项
