# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/model/valueobject/AuditSnapshot.java,kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditObjectLoader.java,kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditSnapshotAssembler.java,kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditObjectLoaderRegistry.java,kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditSnapshotAssemblerRegistry.java`：迁移审计快照与运行时 SPI
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/model/valueobject/AuditSnapshot.java`、`kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditObjectLoader.java`、`kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditSnapshotAssembler.java`、`kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditObjectLoaderRegistry.java`、`kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditSnapshotAssemblerRegistry.java`
    - 处理动作：迁移审计快照对象与自动加载 SPI
    - 验收点：`common-audit` 可独立编译
    - 重要度：7/10

## 待审阅任务项

- [ ] `kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditSnapshots.java,kuzhambu-servers/common/kuzhambu-common-audit/pom.xml`：迁移审计快照工具类
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditSnapshots.java`、`kuzhambu-servers/common/kuzhambu-common-audit/pom.xml`
    - 处理动作：迁移快照工具类并补齐 `common-audit` 依赖
    - 验收点：`AuditSnapshots` 编译通过且不依赖业务模块
    - 重要度：6/10

- [ ] `kuzhambu-servers/biz/system/kuzhambu-system-domain/pom.xml,kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/entity/AuditLog.java,kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/valueobject/AuditField.java,kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/valueobject/AuditSnapshot.java,kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/enums/AuditAction.java`：切换 system-domain 审计模型引用
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-domain/pom.xml`、`kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/entity/AuditLog.java`、`kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/valueobject/AuditField.java`、`kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/valueobject/AuditSnapshot.java`、`kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/enums/AuditAction.java`
    - 处理动作：让 System 审计领域改用 `common-audit` 类型并删除重复定义
    - 验收点：`system-domain` 不再保留重复审计模型
    - 重要度：9/10

- [ ] `kuzhambu-servers/biz/system/kuzhambu-system-application/pom.xml,kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditLogAspect.java,kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/service/impl/AuditApplicationServiceImpl.java`：切换 system-application 核心审计引用
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/pom.xml`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditLogAspect.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/service/impl/AuditApplicationServiceImpl.java`
    - 处理动作：切换 System 审计核心编排引用到 `common-audit`
    - 验收点：`system-application` 核心审计编排继续可编译
    - 重要度：9/10

- [ ] `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/MenuAuditObjectLoader.java,kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/MenuAuditSnapshotAssembler.java,kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DepartmentAuditObjectLoader.java,kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DepartmentAuditSnapshotAssembler.java`：切换首批 System loader 与 assembler
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/MenuAuditObjectLoader.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/MenuAuditSnapshotAssembler.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DepartmentAuditObjectLoader.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DepartmentAuditSnapshotAssembler.java`
    - 处理动作：切换 Menu 与 Department 审计实现引用到 `common-audit`
    - 验收点：首批 System loader / assembler 不再引用旧包路径
    - 重要度：7/10

- [ ] `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/RoleAuditObjectLoader.java,kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/RoleAuditSnapshotAssembler.java,kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/UserAuditObjectLoader.java,kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/UserAuditSnapshotAssembler.java`：切换第二批 System loader 与 assembler
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/RoleAuditObjectLoader.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/RoleAuditSnapshotAssembler.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/UserAuditObjectLoader.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/UserAuditSnapshotAssembler.java`
    - 处理动作：切换 Role 与 User 审计实现引用到 `common-audit`
    - 验收点：第二批 System loader / assembler 不再引用旧包路径
    - 重要度：7/10

- [ ] `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditObjectLoader.java,kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditSnapshotAssembler.java,kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/annotation/AuditLog.java`：收尾 System 审计旧实现
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditObjectLoader.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditSnapshotAssembler.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/annotation/AuditLog.java`
    - 处理动作：切换剩余 Dict 审计实现并删除旧注解定义
    - 验收点：`system-application` 不再保留旧审计注解
    - 重要度：7/10

- [ ] `kuzhambu-servers/biz/system/kuzhambu-system-interface/pom.xml,kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditController.java,kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/assembler/AuditInterfaceAssembler.java`：切换 system-interface 审计展示引用
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-interface/pom.xml`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditController.java`、`kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/assembler/AuditInterfaceAssembler.java`
    - 处理动作：切换 System 接口层到新的 registry 与快照模型
    - 验收点：审计接口层不再依赖旧运行时包路径
    - 重要度：8/10

- [ ] `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml,docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md,TODO.md`：重挂 Knowledge 审计任务依赖
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-COMMON-AUDIT-REFACTOR.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml`、`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`、`TODO.md`
    - 处理动作：让 Knowledge 精修审计接入改为依赖 `common-audit`
    - 验收点：Knowledge 审计后续任务不再指向 `system-application`
    - 重要度：7/10

## 待讨论项
