# ArchUnit allowlist 清理 06：Classics 内容切片

## Purpose

清理 Classics 的内容、明俗和王圻切片 legacy allowlist。

## Scope

| allowlist 项目 | 所在文件 |
| --- | --- |
| 内容、明俗、王圻 Command/Query record 例外 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationCommandQueryRecordAllowances.java` |
| 对应 ApplicationService 边界、构造位置、Assembler 空返回例外 | `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java` |
| 内容、明俗 Repository 方法命名例外 | `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/test/java/com/thundax/kuzhambu/classics/domain/ClassicsDomainArchitectureTest.java` |
| 内容、明俗、王圻 Request/Response 注解与 Assembler 例外 | `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java` |

## Non-goals

不处理三才、发布、清理、报表和搜索条目。

## Plan

1. 只移除 key 所属 package 为 content、mingcustoms、wangqi 的条目。
2. Repository 改名与实现、调用方一同更新。
3. 逐项删除已修复 key。

## Verification

在 `kuzhambu-servers/` 下运行：`mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-interface -am test`，再执行 `mvn spotless:check`、`mvn checkstyle:check`。

## Closure

本切片 key 清零后删除本文档。
