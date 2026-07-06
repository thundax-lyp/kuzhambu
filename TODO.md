# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `01-ai-knowledge-usecase`：补齐 Knowledge 标签抽取 AI usecase 映射
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/knowledge/service/KnowledgeAiExtractionDomainService.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/knowledge/support/KnowledgeAiWorkerUsecaseResolver.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/knowledge/support/KnowledgeAiWorkerUsecaseResolverTest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/knowledge/service/impl/KnowledgeAiExtractionApplicationServiceImplTest.java`
    - 处理动作：新增 `extractTags` 并把 `TAG` 解析到 `KNOWLEDGE_TAG_EXTRACTION`
    - 验收点：resolver 和 application service 测试断言 `operation / workerPath / capability / createCandidate` 均符合 RUNBOOK
    - 重要度：10/10

- [ ] `02-ai-facade`：暴露 Knowledge 标签抽取 AI Facade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/AiFacade.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/assembler/AiFacadeAssembler.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImplTest.java`
    - 处理动作：新增 `extractKnowledgeTags` facade 方法并保持 `KnowledgeAiExtractionFacadeRequest` 字段透传
    - 验收点：facade 测试断言 `taskType=TAG`、`callId`、`candidateId`、`status` 和 `resultPayload` 不丢失
    - 重要度：10/10

- [ ] `03-knowledge-tag-extraction`：新增 Knowledge 标签同步抽取应用入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagExtractionCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/TagExtractionResult.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`
    - 处理动作：新增 `extractTags` 并组装 `KnowledgeAiExtractionFacadeRequest`
    - 验收点：测试断言 `taskType=TAG`、`scopeType=CONTENT`、`forceJson=true`、`locale=zh-CN` 和 `inputPayloadJson` 字段符合 RUNBOOK
    - 重要度：10/10

- [ ] `04-knowledge-tag-candidate-apply`：新增 Knowledge 标签候选应用入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/TaxonomyApplicationService.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/command/TagCandidateApplyCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImplTest.java`
    - 处理动作：新增 `applyExtractedTags` 并把选中候选接入标签审核治理
    - 验收点：测试断言既有标签复用、新标签为 `source=AI_EXTRACTED` 且 `reviewStatus=PENDING`、应用后调用 `markCandidateApplied`
    - 重要度：10/10

- [ ] `05-knowledge-taxonomy-interface`：暴露 Knowledge 标签抽取 HTTP API
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyController.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagExtractionRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCandidateApplyRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagExtractionResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyControllerTest.java`
    - 处理动作：新增 `tag/extract` 和 `tag/extract/apply` 后台接口
    - 验收点：controller 测试断言 request 校验、command 转换和 response 字段与 RUNBOOK 一致
    - 重要度：9/10

- [ ] `06-admin-web-taxonomy-service`：补齐标签抽取前端 service 与类型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-types.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service.test.ts`
    - 处理动作：新增标签抽取和候选应用 service 契约
    - 验收点：service contract 测试断言 `/knowledge/taxonomy/tag/extract`、`/knowledge/taxonomy/tag/extract/apply` 和请求字段完全匹配 RUNBOOK
    - 重要度：8/10

- [ ] `07-admin-web-taxonomy-ui`：实现标签治理页 AI 抽取标签控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-extraction-drawer.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-extraction-candidate-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.css`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.test.tsx`
    - 处理动作：新增 `AI 抽取标签` 按钮、抽取 drawer、候选表格和应用确认操作
    - 验收点：页面测试覆盖打开 drawer、填写控件、开始抽取、勾选候选、确认应用和刷新标签查询
    - 重要度：8/10

- [ ] `08-main-sync`：收口前同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：当前工作分支与 `main`
    - 处理动作：在实现完成后同步 `main` 最新代码并解决冲突
    - 验收点：工作分支包含 `main` 最新代码且 `git status` 无未解释冲突
    - 重要度：9/10

- [ ] `09-final-validation`：运行 AI Knowledge Tag Extraction 最小闭环验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 范围对象：`kuzhambu-servers`、`kuzhambu-apps/admin-web`、`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 处理动作：运行 RUNBOOK 指定的 Java 与 admin-web 格式化、静态检查和测试
    - 验收点：相关 Maven、Prettier、ESLint 和 taxonomy 测试通过，失败项已定位并修复
    - 重要度：10/10

- [ ] `10-coverage-runbook-cleanup`：更新 AI Implementation Coverage 并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 范围对象：`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-TAG-EXTRACTION.md`
    - 处理动作：把 `KNOWLEDGE_TAG_EXTRACTION` 移入已完成矩阵并删除已完成 RUNBOOK
    - 验收点：`AI-IMPLEMENTATION-COVERAGE.md` 不再保留 `KNOWLEDGE_TAG_EXTRACTION` 未完成项，且 RUNBOOK 文件已清理
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
