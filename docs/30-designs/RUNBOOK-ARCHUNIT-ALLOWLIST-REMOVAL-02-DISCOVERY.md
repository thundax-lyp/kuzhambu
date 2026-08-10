# ArchUnit allowlist 清理 02：Discovery

## Purpose

清理 Discovery 域的 legacy allowlist。

## Scope

| allowlist 项目 | 所在文件 |
| --- | --- |
| Command/Query record 例外 | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/DiscoveryApplicationCommandQueryRecordAllowances.java` |
| ApplicationService 边界、构造位置、Assembler 空返回例外 | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/DiscoveryApplicationArchitectureTest.java` |
| Repository 方法命名例外 | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/test/java/com/thundax/kuzhambu/discovery/domain/DiscoveryDomainArchitectureTest.java` |
| Response 注解、Controller 动词、Assembler 空返回例外 | `kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/DiscoveryInterfaceArchitectureTest.java` |

## Non-goals

不修改共享 ArchUnit support，不改动非 Discovery 域的业务契约。

## Plan

1. 以 key 为单位整改生产代码并删除对应例外。
2. 对跨 interface/application 的变更在同一提交中闭环。
3. 保持 allowlist 只收缩。

## Verification

在 `kuzhambu-servers/` 下运行：`mvn -pl biz/discovery/kuzhambu-discovery-application,biz/discovery/kuzhambu-discovery-domain,biz/discovery/kuzhambu-discovery-interface -am test`，再执行 `mvn spotless:check`、`mvn checkstyle:check`。

## Closure

全部 key 删除且验证通过后删除本文档。
