# ArchUnit allowlist 清理 08：Knowledge 图谱与精炼

## Purpose

清理 Knowledge 图谱抽取、精炼与血缘切片 legacy allowlist。

## Scope

| allowlist 项目 | 所在文件 |
| --- | --- |
| 图谱/精炼 Command/Query record 例外 | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java` |
| 图谱/精炼 ApplicationService 边界与 Assembler 例外 | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationArchitectureTest.java` |
| 图谱/精炼 Repository 方法命名例外 | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/KnowledgeDomainArchitectureTest.java` |
| graph、refinement、lineage Request/Response 注解、Controller 动词与 Assembler 例外 | `kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java` |

## Non-goals

不处理 taxonomy 与 portal 条目。

## Plan

1. 只处理 graph、refinement、lineage package 所属 key。
2. 跨 interface/application 的对象迁移在同一提交完成。
3. 不扩大现有 allowlist。

## Verification

在 `kuzhambu-servers/` 下运行：`mvn -pl biz/knowledge/kuzhambu-knowledge-application,biz/knowledge/kuzhambu-knowledge-domain,biz/knowledge/kuzhambu-knowledge-interface -am test`，再执行 `mvn spotless:check`、`mvn checkstyle:check`。

## Closure

本切片 key 清零后删除本文档。
