# ArchUnit allowlist 清理 07：Classics 三才与发布

## Purpose

清理 Classics 的三才、发布、清理、报表和搜索切片 legacy allowlist。

## Scope

| allowlist 项目 | 所在文件 |
| --- | --- |
| 三才与发布 Command/Query record 例外 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationCommandQueryRecordAllowances.java` |
| 三才、发布、清理、报表、搜索 ApplicationService 与 Assembler 例外 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java` |
| 三才、发布 Repository 方法命名例外 | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/test/java/com/thundax/kuzhambu/classics/domain/ClassicsDomainArchitectureTest.java` |
| 三才、发布 Request/Response 注解与 Assembler 例外 | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java` |

## Non-goals

不处理内容、明俗和王圻条目。

## Plan

1. 只处理 package 属于 sancai、publication、cleanup、report、search 的 key。
2. 保持发布状态机和跨域 facade 契约不变。
3. 修复后立刻删除精确 allowlist key。

## Verification

在 `kuzhambu-servers/` 下运行：`mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-interface -am test`，再执行 `mvn spotless:check`、`mvn checkstyle:check`。

## Closure

本切片 key 清零后删除本文档。
