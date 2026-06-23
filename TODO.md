# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditSnapshotAssembler.java,kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditSnapshots.java,kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditExpressionEvaluator.java`：切换 system-application 旧快照工具引用
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditSnapshotAssembler.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditSnapshots.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditExpressionEvaluator.java`
    - 处理动作：让剩余运行时快照工具改用 `common-audit` 快照类型
    - 验收点：`system-application` 旧快照工具不再引用 `system-domain` 审计值对象
    - 重要度：8/10

## 待审阅任务项

- [ ] `kuzhambu-servers/biz/system/kuzhambu-system-infra/pom.xml,kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/persistence/assembler/AuditLogPersistenceAssembler.java,kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/persistence/assembler/AuditMetaPersistenceAssembler.java,kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/repository/impl/AuditLogRepositoryImpl.java`：切换 system-infra 审计持久化引用
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-infra/pom.xml`、`kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/persistence/assembler/AuditLogPersistenceAssembler.java`、`kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/persistence/assembler/AuditMetaPersistenceAssembler.java`、`kuzhambu-servers/biz/system/kuzhambu-system-infra/src/main/java/com/thundax/kuzhambu/system/infra/audit/repository/impl/AuditLogRepositoryImpl.java`
    - 处理动作：切换持久化层到 `common-audit` 审计动作与快照类型
    - 验收点：`system-infra` 不再引用旧审计动作和快照值对象
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
