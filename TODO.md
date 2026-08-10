# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
