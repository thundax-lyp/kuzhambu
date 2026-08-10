# ArchUnit allowlist 清理 09：Knowledge Taxonomy 与 Portal

## Purpose

清理 Knowledge 标签体系与 Portal 切片 legacy allowlist。

## Scope

| allowlist 项目 | 所在文件 |
| --- | --- |
| taxonomy Command/Query record 例外 | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java` |
| taxonomy ApplicationService 边界与 Assembler 例外 | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationArchitectureTest.java` |
| taxonomy Repository 方法命名例外 | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/KnowledgeDomainArchitectureTest.java` |
| taxonomy 与 portal Request/Response 注解、Controller 动词和 Assembler 例外 | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java` |

## Non-goals

不处理 graph、refinement、lineage 条目；不改动 Portal 对外协议而不同步调用方。

## Plan

1. 只移除 taxonomy 与 portal package 所属 key。
2. 对接口模型注解或 Controller 路径整改同步更新调用端。
3. 逐项删 key 并以 stale allowance 检查确认没有残留。

## Verification

在 `kuzhambu-servers/` 下运行：`mvn -pl biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-domain,biz/knowledge/kuzhambu-knowledge-interface -am test`，再执行 `mvn spotless:check`、`mvn checkstyle:check`。

## Closure

本切片 key 清零后删除本文档。
