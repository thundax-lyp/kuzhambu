# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `08-user`：清理 User record 与构造边界
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-05-SYSTEM-CORE.md`
    - 范围对象：
        - `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeUserInfoCommand.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/ChangeUserStatusCommand.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateUserCommand.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/RemoveUserCommand.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetUserQuery.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/UserQuery.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/UserController.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/UserInterfaceAssembler.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemAdminManagementContractTest.java`
        - `kuzhambu-apps/admin-web/src/pages/system/user/user-service.ts`
        - `kuzhambu-apps/admin-web/src/pages/system/user/user-service-contract.test.ts`
    - 处理动作：转换 User Command/Query 为 record，并将 controller 构造迁移到 assembler 后同步前后端契约。
    - 验收点：User 相关 allowlist 可删除，后端契约和 admin-web service contract 一致。
    - 重要度：9/10

- [ ] `09-log`：清理 Log record 与构造边界
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-05-SYSTEM-CORE.md`
    - 范围对象：
        - `kuzhambu-apps/admin-web/e2e/system/management/management.spec.ts`
        - `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/CreateLogCommand.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/command/DeleteLogCommand.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/GetLogQuery.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/query/LogQuery.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/controller/LogController.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/service/impl/SysLogMessageServiceImpl.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/core/assembler/LogInterfaceAssembler.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/SystemLogContractTest.java`
        - `kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-service.ts`
        - `kuzhambu-apps/admin-web/src/pages/system/system-log/system-log-service-contract.test.ts`
        - `kuzhambu-apps/admin-web/e2e/system/logs/logs.spec.ts`
    - 处理动作：转换 Log Command/Query 为 record，并将 controller/service 构造迁移到 assembler 后同步前后端契约。
    - 验收点：Log 相关 allowlist 可删除，后端契约、admin-web service contract 和 logs E2E 路径一致。
    - 重要度：9/10

- [ ] `10-allowlist-closure`：统一删除 allowlist 并完成现场清理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-05-SYSTEM-CORE.md`
    - 范围对象：
        - `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationCommandQueryRecordAllowances.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/SystemInterfaceArchitectureTest.java`
        - `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/admin/core/aop/SysLogMethodInterceptorTest.java`
        - `kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/integration/IntegrationAuthClient.java`
        - `kuzhambu-apps/admin-web/e2e/auth/login/login.spec.ts`
        - `TODO.md`
        - `docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-05-SYSTEM-CORE.md`
        - GitHub PR #232 `discussion_r3746603229`
        - GitHub PR #232 `discussion_r3746633916`
    - 处理动作：删除已清理的 System Core allowlist，完成最终验证后清空 TODO、删除 RUNBOOK，并到 PR #232 对两个遗留 comment 逐条回复处理结果。
    - 验收点：System Core allowlist 清零；TODO 和 RUNBOOK 完成现场清理；PR #232 两条遗留 comment 均有针对性结果回复。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
