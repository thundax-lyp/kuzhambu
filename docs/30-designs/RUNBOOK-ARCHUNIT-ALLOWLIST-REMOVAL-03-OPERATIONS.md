# ArchUnit allowlist 清理 03：Operations

## Purpose

清理 Operations 域的 legacy allowlist。

## Scope

| allowlist 项目 | 所在文件 |
| --- | --- |
| Command/Query record 例外 | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/OperationsApplicationCommandQueryRecordAllowances.java` |
| ApplicationService 边界、构造位置、Assembler 空返回例外 | `kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/OperationsApplicationArchitectureTest.java` |
| Repository 方法命名例外 | `kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/test/java/com/thundax/kuzhambu/operations/domain/OperationsDomainArchitectureTest.java` |
| Response 注解、Controller 动词、Assembler 空返回例外 | `kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/OperationsInterfaceArchitectureTest.java` |

## Non-goals

不修改共享规则，不将批处理调度语义迁移到其他业务域。

## Plan

1. 逐项修复并删除对应 key。
2. Repository 改名同步更新实现与调用方。
3. 保持本批次变更限于 Operations 域。

## Verification

在 `kuzhambu-servers/` 下运行：`mvn -pl biz/operations/kuzhambu-operations-application,biz/operations/kuzhambu-operations-domain,biz/operations/kuzhambu-operations-interface -am test`，再执行 `mvn spotless:check`、`mvn checkstyle:check`。

## Closure

全部 key 清零后删除本文档。
