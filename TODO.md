# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditObjectLoader.java,kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditSnapshotAssembler.java,kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/annotation/AuditLog.java`：收尾 System 审计旧实现
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditObjectLoader.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditSnapshotAssembler.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/annotation/AuditLog.java`
    - 处理动作：切换剩余 Dict 审计实现并删除旧注解定义
    - 验收点：`system-application` 不再保留旧审计注解
    - 重要度：7/10

## 待审阅任务项

- [ ] `kuzhambu-servers/biz/system/kuzhambu-system-interface/pom.xml,kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditController.java,kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/assembler/AuditInterfaceAssembler.java`：切换 system-interface 审计展示引用
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-interface/pom.xml`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditController.java`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/assembler/AuditInterfaceAssembler.java`
    - 处理动作：切换 System 接口层到新的 registry 与快照模型
    - 验收点：审计接口层不再依赖旧运行时包路径
    - 重要度：8/10

- [ ] `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/valueobject/AuditField.java,kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/valueobject/AuditSnapshot.java,kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/enums/AuditAction.java`：删除 system-domain 重复审计模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/valueobject/AuditField.java`、`kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/valueobject/AuditSnapshot.java`、`kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/enums/AuditAction.java`
    - 处理动作：删除已被 `common-audit` 替代的重复类型
    - 验收点：`system-domain` 不再保留重复审计模型定义
    - 重要度：8/10

- [ ] `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml,docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md,TODO.md`：重挂 Knowledge 审计任务依赖
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml`、`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`、`TODO.md`
    - 处理动作：让 Knowledge 精修审计接入改为依赖 `common-audit`
    - 验收点：Knowledge 审计后续任务不再指向 `system-application`
    - 重要度：7/10

## 待讨论项
