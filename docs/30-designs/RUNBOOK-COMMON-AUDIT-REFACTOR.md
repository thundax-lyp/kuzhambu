# Common Audit Refactor Runbook

## Purpose

本文档定义审计基础设施重构执行手册。

本轮目标：

- 新增 `kuzhambu-common-audit` 模块。
- 将通用审计注解、快照模型、运行时 SPI 和注册器迁移到 `common-audit`。
- 消除业务 `application -> application` 审计依赖。
- 保持 System 审计记录、查询和切面编排能力不变。
- 为 Knowledge 审计接入提供合法依赖边界。

本轮不做：

- 重写 `AuditLogAspect` 业务逻辑。
- 重写 System 审计日志存储模型。
- 一次性接入所有业务模块审计。

## Branch

- 当前工作分支：`feat/knowledge-refinement-workbench`

## Delivery Rule

- 审计运行时通用契约只能位于 `common-audit`。
- 业务模块不得新增 `application -> application` 依赖以使用审计能力。
- 单个执行任务关联文件不得超过 `5` 个。
- 单个执行任务只表达一个主动作。
- 迁移顺序固定为：`common-audit` 建模块、System 引用切换、Knowledge 接入恢复。

## Target Structure

### 1. `kuzhambu-common-audit`

模块路径：

- `kuzhambu-servers/common/kuzhambu-common-audit`

固定承载内容：

- `com.thundax.kuzhambu.common.audit.annotation.AuditLog`
- `com.thundax.kuzhambu.common.audit.model.enums.AuditAction`
- `com.thundax.kuzhambu.common.audit.model.valueobject.AuditField`
- `com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot`
- `com.thundax.kuzhambu.common.audit.runtime.AuditObjectLoader`
- `com.thundax.kuzhambu.common.audit.runtime.AuditSnapshotAssembler`
- `com.thundax.kuzhambu.common.audit.runtime.AuditObjectLoaderRegistry`
- `com.thundax.kuzhambu.common.audit.runtime.AuditSnapshotAssemblerRegistry`
- `com.thundax.kuzhambu.common.audit.runtime.AuditSnapshots`

### 2. `kuzhambu-system-application`

保留内容：

- `AuditLogAspect`
- `AuditOperatorResolver`
- `AuditExpressionEvaluator`
- `AuditApplicationService`
- `CreateAuditLogCommand`
- 审计日志写入、查询、详情编排
- System 自身 `sys/*AuditSnapshotAssembler`
- System 自身 `sys/*AuditObjectLoader`

变更要求：

- 所有对注解、快照模型、SPI、注册器的引用切换到 `common-audit`

### 3. 业务 `application`

业务模块只允许：

- 在 service 方法使用 `@AuditLog`
- 实现自身 `XxxAuditSnapshotAssembler`
- 实现自身 `XxxAuditObjectLoader`

业务模块禁止：

- 依赖其他业务 `application` 获取审计注解或 SPI

## Migration Decisions

### 1. 注解下沉口径

`@AuditLog` 下沉到 `common-audit`，保留字段：

- `type`
- `id`
- `action`
- `summary`
- `condition`
- `recordWhenUnchanged`

`action` 类型保留为 `AuditAction`，同时将 `AuditAction` 一并下沉到 `common-audit`。

### 2. 快照模型下沉口径

下沉到 `common-audit` 的快照模型固定为：

- `AuditField`
- `AuditSnapshot`

System 审计日志领域实体继续引用这两个类型，不再在 `system-domain` 内保留重复定义。

### 3. 运行时 SPI 下沉口径

下沉到 `common-audit` 的运行时 SPI 固定为：

- `AuditObjectLoader`
- `AuditSnapshotAssembler`
- `AuditObjectLoaderRegistry`
- `AuditSnapshotAssemblerRegistry`
- `AuditSnapshots`

保持现有自动装配方式不变：

- `XxxAuditObjectLoader` 定义为 `@Component`
- `XxxAuditSnapshotAssembler` 定义为 `@Component`
- registry 通过 `List<AuditObjectLoader>` 和 `List<AuditSnapshotAssembler>` 自动加载

### 4. Aspect 落点口径

`AuditLogAspect` 不下沉到 `common-audit`。

原因：

- 它依赖 `AuditApplicationService`
- 它依赖 `CreateAuditLogCommand`
- 它属于 System 审计写入编排

## Module Dependency Rule

重构完成后依赖关系固定为：

- `system-domain -> common-audit`
- `system-application -> common-audit`
- `knowledge-application -> common-audit`
- `knowledge-application` 不依赖 `system-application`

## File-Level Execution Plan

### Task 1. 新增 `common-audit` 模块骨架

数据结构变更：

- 无数据库变更

文件变更：

- `kuzhambu-servers/common/pom.xml`
- `kuzhambu-servers/common/kuzhambu-common-audit/pom.xml`
- `kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/annotation/AuditLog.java`
- `kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/model/enums/AuditAction.java`
- `kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/model/valueobject/AuditField.java`

处理动作：

- 建模块并迁移注解、动作枚举和字段模型首批文件

验收点：

- `common` reactor 可识别 `kuzhambu-common-audit`

### Task 2. 迁移快照模型与运行时 SPI

数据结构变更：

- 无数据库变更

文件变更：

- `kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/model/valueobject/AuditSnapshot.java`
- `kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditObjectLoader.java`
- `kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditSnapshotAssembler.java`
- `kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditObjectLoaderRegistry.java`
- `kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditSnapshotAssemblerRegistry.java`

处理动作：

- 迁移审计快照和运行时接口

验收点：

- `common-audit` 可独立编译

### Task 3. 迁移 `AuditSnapshots`

数据结构变更：

- 无数据库变更

文件变更：

- `kuzhambu-servers/common/kuzhambu-common-audit/src/main/java/com/thundax/kuzhambu/common/audit/runtime/AuditSnapshots.java`
- `kuzhambu-servers/common/kuzhambu-common-audit/pom.xml`

处理动作：

- 迁移快照工具类并补齐依赖

验收点：

- `AuditSnapshots` 编译通过且无额外业务依赖

### Task 4. 切换 `system-domain` 到 `common-audit`

数据结构变更：

- 无数据库变更

文件变更：

- `kuzhambu-servers/biz/system/kuzhambu-system-domain/pom.xml`
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/entity/AuditLog.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/valueobject/AuditField.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/valueobject/AuditSnapshot.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-domain/src/main/java/com/thundax/kuzhambu/system/domain/audit/model/enums/AuditAction.java`

处理动作：

- System domain 切换到 `common-audit` 类型并删除重复定义

验收点：

- `system-domain` 不再保留重复审计模型

### Task 5. 切换 `system-application` 注解与 SPI 引用

数据结构变更：

- 无数据库变更

文件变更：

- `kuzhambu-servers/biz/system/kuzhambu-system-application/pom.xml`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/AuditLogAspect.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/MenuAuditObjectLoader.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/MenuAuditSnapshotAssembler.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/service/impl/AuditApplicationServiceImpl.java`

处理动作：

- 切换首批 System 审计运行时引用到 `common-audit`

验收点：

- `system-application` 可引用 `common-audit` 并保持切面编排不变

### Task 6. 切换剩余 `system-application` 审计实现

数据结构变更：

- 无数据库变更

文件变更：

- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DepartmentAuditObjectLoader.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DepartmentAuditSnapshotAssembler.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/RoleAuditObjectLoader.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/RoleAuditSnapshotAssembler.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/UserAuditObjectLoader.java`

处理动作：

- 继续切换剩余 System 审计实现引用

验收点：

- 所有 System loader / assembler 统一依赖 `common-audit`

### Task 7. 收尾剩余 `system-application` 审计实现

数据结构变更：

- 无数据库变更

文件变更：

- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/UserAuditSnapshotAssembler.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditObjectLoader.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/runtime/sys/DictAuditSnapshotAssembler.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/audit/annotation/AuditLog.java`

处理动作：

- 切换剩余引用并删除旧注解定义

验收点：

- `system-application` 不再保留旧注解与旧 SPI 定义

### Task 8. 切换 `system-interface` 审计展示引用

数据结构变更：

- 无数据库变更

文件变更：

- `kuzhambu-servers/biz/system/kuzhambu-system-interface/pom.xml`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/controller/AuditController.java`
- `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/kuzhambu/system/interfaces/admin/audit/assembler/AuditInterfaceAssembler.java`

处理动作：

- System interface 引用新的 registry 和快照模型

验收点：

- 审计接口层引用不再依赖旧包路径

### Task 9. 为 Knowledge 审计接入解锁依赖

数据结构变更：

- 无数据库变更

文件变更：

- `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/pom.xml`
- `docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-WORKBENCH.md`
- `TODO.md`

处理动作：

- 让 Knowledge 精修审计 TODO 重新指向 `common-audit`

验收点：

- Knowledge 后续审计实现不再依赖 `system-application`

## Validation Rule

每个执行任务只做最小验证：

- Java formatter：`mvn -pl ... spotless:check`
- Java style：`mvn -pl ... checkstyle:check`

统一测试放到所有审计迁移任务消费完成后执行。
