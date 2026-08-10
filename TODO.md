# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `ARCHUNIT-SYSTEM-AUTH-04-08`：收口 auth、captcha、audit controller 动词和 assembler 空返回
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditController.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/AuthController.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/controller/CaptchaController.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/assembler/AuditInterfaceAssembler.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/auth/assembler/AuthInterfaceAssembler.java`；`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/SystemInterfaceArchitectureTest.java`
    - 处理动作：将三个 controller 的 action verb 和两个 assembler 的空返回整改到规则允许形态。
    - 验收点：RUNBOOK 中 `CONTROLLER_ACTION_VERB` 三个 key 和两个 assembler nullness class key 已删除，system interface 架构测试通过。
    - 重要度：8/10

- [ ] `ARCHUNIT-SYSTEM-AUTH-04-09`：清理 System 认证与审计 allowlist 清理现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`；`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 范围对象：`TODO.md`；`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-04-SYSTEM-AUTH-AUDIT.md`
    - 处理动作：在本批 allowlist 清零且验证通过后，删除临时 RUNBOOK 并移除本批已完成 TODO。
    - 验收点：`TODO.md` 不再保留本批已完成任务，临时 RUNBOOK 已删除或按治理要求迁移后删除。
    - 重要度：7/10

## 待讨论项
