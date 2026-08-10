# ArchUnit allowlist 清理 01：AI 与 Storage

## Purpose

清理 AI 与 Storage 域的 ArchUnit legacy allowlist，并在每项生产代码整改后同步删除对应 key。

## Scope

| allowlist 项目 | 所在文件 |
| --- | --- |
| AI Repository 方法命名例外 | `kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/test/java/com/thundax/kuzhambu/ai/domain/AiDomainArchitectureTest.java` |
| AI Controller 动词例外 | `kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/AiInterfaceArchitectureTest.java` |
| Storage Command/Query record 例外 | `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/StorageApplicationCommandQueryRecordAllowances.java` |
| Storage Command/Query 构造位置例外 | `kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/test/java/com/thundax/kuzhambu/storage/application/StorageApplicationArchitectureTest.java` |
| Storage Controller 动词例外 | `kuzhambu-servers/biz/storage/kuzhambu-storage-interface/src/test/java/com/thundax/kuzhambu/storage/interfaces/StorageInterfaceArchitectureTest.java` |

## Non-goals

不修改 `kuzhambu-common-test` 的共享规则；不借由扩大 allowlist 保持测试通过。

## Plan

1. 按 allowlist key 定位生产代码并完成对应整改。
2. 删除已命中的 key；ArchUnit 不得报告新增违规或 stale allowance。
3. 运行受影响 Maven 模块的架构测试。

## Verification

在 `kuzhambu-servers/` 下运行：`mvn -pl biz/ai/kuzhambu-ai-domain,biz/ai/kuzhambu-ai-interface,biz/storage/kuzhambu-storage-application,biz/storage/kuzhambu-storage-interface -am test`；提交前运行 `mvn spotless:check` 与 `mvn checkstyle:check`。

## Closure

本批次全部 key 清零并验证通过后删除本文档。
