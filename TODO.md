# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Knowledge application taxonomy read`：新增同义词方向查询用例
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-SYNONYM-SMOKE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/KnowledgeTaxonomyReadApplicationService.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/KnowledgeTaxonomyReadApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/DiscoverySynonymQueryResult.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/result/DiscoverySynonymMatchResult.java`
    - 处理动作：在 Knowledge application 读协作中新增正向、反向和双向同义词方向查询能力。
    - 验收点：`querySynonyms` 可按方向返回去重后的 `matches`，且旧 `expandSynonyms` 复用双向查询并保持兼容。
    - 重要度：10/10

- [ ] `Knowledge facade synonym query`：暴露外域同义词方向查询契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-SYNONYM-SMOKE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/KnowledgeFacade.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/request/KnowledgeSynonymQueryFacadeRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/response/KnowledgeSynonymQueryFacadeResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-facade/src/main/java/com/thundax/kuzhambu/knowledge/facade/dto/KnowledgeSynonymMatchFacadeDto.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/impl/KnowledgeFacadeImpl.java`
    - 处理动作：在 Knowledge facade 增加 `querySynonyms` 请求、响应、明细 DTO 和实现入口。
    - 验收点：Discovery 可通过 facade 传入 `term`、`direction`、`limit` 并读取 `matches` 与 `expandedTerms`。
    - 重要度：10/10

- [ ] `Knowledge facade assembler tests`：补同义词查询装配测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-SYNONYM-SMOKE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/facade/assembler/KnowledgeFacadeAssembler.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/facade/impl/KnowledgeFacadeImplTest.java`
    - 处理动作：为新同义词查询响应增加 facade 装配逻辑和兼容测试。
    - 验收点：测试断言 `direction`、`limit`、`matches` 和 `expandedTerms` 字段正确输出，旧 `expandSynonyms` 测试继续通过。
    - 重要度：9/10

- [ ] `Discovery Search synonym enhancement`：切换搜索增强到同义词方向查询入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-SYNONYM-SMOKE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/DiscoveryKnowledgeEnhancementProvider.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/QueryUnderstandingApplicationServiceImplTest.java`
    - 处理动作：让 Discovery Search 查询理解消费 `KnowledgeFacade.querySynonyms` 的双向扩展词。
    - 验收点：Search 单测验证新 facade 入口被调用，且 `expanded_synonyms_json` 仍写入扩展词数组。
    - 重要度：10/10

- [ ] `Discovery QA synonym enhancement`：把同义词扩展注入问答请求 metadata
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-SYNONYM-SMOKE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/DiscoveryKnowledgeEnhancementProvider.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImplTest.java`
    - 处理动作：让 Discovery QA 基于最新用户问题获取同义词扩展并写入 `KnowledgeChatRequest` metadata。
    - 验收点：QA 单测断言 provider request metadata 包含 `synonymQueryTerm` 和 `expandedSynonyms`，Portal 响应不透出词典明细。
    - 重要度：10/10

- [ ] `Knowledge synonym direction tests`：补方向查询核心规则单测
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-SYNONYM-SMOKE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/KnowledgeTaxonomyReadApplicationServiceImplTest.java`
    - 处理动作：为 Knowledge 同义词正向、反向、双向、limit 和空白输入规则补单测。
    - 验收点：测试覆盖 `FORWARD`、`REVERSE`、`BIDIRECTIONAL`、limit 收窄、去重和空输入结果。
    - 重要度：9/10

- [ ] `Admin Web taxonomy e2e`：补后台同义词治理 Playwright 冒烟
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-SYNONYM-SMOKE-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/knowledge/taxonomy/taxonomy.spec.ts`
    - 处理动作：新增 Admin `/knowledge/taxonomy` 同义词搜索、新增、编辑、状态切换和删除 Playwright 冒烟。
    - 验收点：Playwright 断言相关控件可操作，并锁定 `/synonym/page|create|update|status|delete` 请求体字段。
    - 重要度：9/10

- [ ] `Portal Web e2e config`：补前台 Playwright 启动配置
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-SYNONYM-SMOKE-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/portal-web/package.json`、`kuzhambu-apps/portal-web/playwright.config.ts`
    - 处理动作：为 Portal Web 增加 Playwright e2e 脚本和本地 Vite webServer 配置。
    - 验收点：`pnpm --filter @kuzhambu/portal-web run e2e` 可启动 Portal e2e。
    - 重要度：8/10

- [ ] `Portal Discovery e2e`：补前台搜索和问答 Playwright 冒烟
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-SYNONYM-SMOKE-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/portal-web/e2e/discovery/search.spec.ts`、`kuzhambu-apps/portal-web/e2e/discovery/qa.spec.ts`
    - 处理动作：新增 Portal Search 查询点击记录和 Portal QA 自动建会话、提问、来源、导出、删除冒烟。
    - 验收点：Playwright 断言 Search 与 QA 页面控件可操作，并锁定 `/search/search|click` 与 `/qa/session/*|chat/completions` 请求体。
    - 重要度：9/10

- [ ] `main branch sync`：收口前同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/knowledge-synonym-smoke-closure` 分支、`origin/main`
    - 处理动作：在最终验证、跨服务证据和覆盖文档收口前把当前分支同步到最新 `origin/main`。
    - 验收点：当前分支包含最新 `origin/main`，且同步冲突已解决，后续验证基于同步后的代码执行。
    - 重要度：9/10

- [ ] `Knowledge Runtime Evidence`：记录最终验证和跨服务冒烟证据
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-SYNONYM-SMOKE-CLOSURE.md`
    - 范围对象：`docs/40-readiness/KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE.md`
    - 处理动作：在同步 main 后记录 Maven、pnpm、Playwright、Knowledge 方向查询、Discovery Search、Discovery QA 和跨服务启动证据。
    - 验收点：证据文档包含同步 main 后的实际命令、数据、结果、trace 或持久化验证方式，不使用“已验证”占位。
    - 重要度：10/10

- [ ] `Implementation Coverage closure`：更新 Knowledge 和 Discovery 覆盖文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-SYNONYM-SMOKE-CLOSURE.md`
    - 范围对象：`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：根据最终验证和跨服务证据收口 Knowledge 与 Discovery 实现覆盖状态。
    - 验收点：覆盖文档不再保留独立同义词正反向查询入口或 Playwright/跨服务冒烟记录缺口。
    - 重要度：10/10

- [ ] `RUNBOOK cleanup and TODO closure`：清理临时 RUNBOOK 并收窄 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-KNOWLEDGE-SYNONYM-SMOKE-CLOSURE.md`、`TODO.md`
    - 处理动作：删除已完成任务的 RUNBOOK，并在真正完成对应任务后从 `TODO.md` 删除或收窄剩余项。
    - 验收点：RUNBOOK 已删除，`TODO.md` 不保留已完成任务清单，仅保留未完成剩余任务或为空。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
