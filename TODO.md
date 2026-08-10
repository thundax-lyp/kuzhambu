# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `09-10 interface taxonomy assembler`：taxonomy interface assembler nullness 收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/assembler/TaxonomyApplicationAssembler.java`
    - 处理动作：补齐 taxonomy interface assembler 非空契约并删除对应 ArchUnit allowlist key。
    - 验收点：`KnowledgeInterfaceArchitectureTest` 不再包含 `KnowledgeTaxonomyInterfaceAssembler` nullness allowlist。
    - 重要度：8/10

- [ ] `09-11 interface portal atlas home quality models`：Portal atlas/home/quality 模型注解收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/request/KnowledgePortalAtlasRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/controller/response/KnowledgePortalAtlasResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/controller/request/KnowledgePortalHomeRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/controller/response/KnowledgePortalHomeResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/controller/request/KnowledgePortalQualityRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/controller/response/KnowledgePortalQualityResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`、`kuzhambu-apps/portal-web/src/pages/knowledge/atlas/atlas-service.ts`、`kuzhambu-apps/portal-web/src/pages/knowledge/quality/quality-service.ts`
    - 处理动作：为 Portal atlas/home/quality request/response 补齐模型注解并同步 portal-web 调用契约。
    - 验收点：`KnowledgeInterfaceArchitectureTest` 不再包含 atlas/home/quality request/response annotation allowlist。
    - 重要度：8/10

- [ ] `09-12 interface portal lineage and assemblers`：Portal lineage response 与 assembler nullness 收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/atlas/assembler/KnowledgePortalAtlasInterfaceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/assembler/KnowledgePortalHomeInterfaceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/lineage/assembler/KnowledgePortalLineageInterfaceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/lineage/controller/response/KnowledgePortalLineageResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/assembler/KnowledgePortalQualityInterfaceAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`
    - 处理动作：补齐 Portal assembler 非空契约与 lineage response 模型注解并删除对应 ArchUnit allowlist key。
    - 验收点：`KnowledgeInterfaceArchitectureTest` 不再包含 Portal assembler nullness key 与 `KnowledgePortalLineageResponse` annotation key。
    - 重要度：8/10

- [ ] `09-13 cleanup`：收口验证与现场清理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationCommandQueryRecordAllowances.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/KnowledgeApplicationArchitectureTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/KnowledgeDomainArchitectureTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/KnowledgeInterfaceArchitectureTest.java`、`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-09-KNOWLEDGE-TAXONOMY-PORTAL.md`、`TODO.md`
    - 处理动作：确认本切片 allowlist 清零后删除 RUNBOOK 并清空对应 TODO。
    - 验收点：本 RUNBOOK 被删除，`TODO.md` 不再保留本切片任务，工作区无无关修改。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
