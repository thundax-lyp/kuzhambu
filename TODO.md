# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Knowledge graph regenerate source`：支持精修来源的图谱重生成请求
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-GRAPH-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/command/RegenerateGraphExtractionCommand.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/KnowledgeGraphExtractionApplicationService.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/request/GraphExtractionRequests.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/assembler/KnowledgeGraphExtractionInterfaceAssembler.java`
    - 处理动作：让重生成任务保留 `REFINEMENT_APPLIED` 触发来源并默认只替换未人工确认结果。
    - 验收点：从精修页发起的重生成任务展示 `triggerSource=REFINEMENT_APPLIED` 且仍经由 `AiFacade`。
    - 重要度：9/10

- [ ] `Knowledge graph version results`：让图谱版本结果暴露精修应用状态
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-GRAPH-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/support/RefinementApplySupport.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/result/GraphVersionResult.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/controller/response/GraphExtractionResponses.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/assembler/KnowledgeGraphExtractionInterfaceAssembler.java`
    - 处理动作：在图谱版本读取结果中增加精修应用标记和最新精修应用时间。
    - 验收点：指定 `graphVersionId` 的正式实体、关系、世系节点和世系关系能读取到人工精修内容。
    - 重要度：9/10

- [ ] `Knowledge quality report stale`：标记精修后过期的质量报告
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-GRAPH-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeQualityReportApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/refinement/result/QualityReportDetailResult.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/response/QualityReportResponses.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/assembler/KnowledgeQualityReportInterfaceAssembler.java`
    - 处理动作：让质量报告详情返回 `stale`、`staleReason` 和最新精修应用时间。
    - 验收点：最新报告早于精修应用时间时返回 `stale=true` 且重新生成后指标基于精修后的正式事实。
    - 重要度：8/10

- [ ] `Knowledge backend verification`：补齐 Knowledge 后端联动测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-GRAPH-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeGraphRefinementApplicationServiceImplTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeGraphRefinementControllerTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/graph/service/impl/KnowledgeGraphExtractionApplicationServiceImplTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/service/impl/KnowledgeQualityReportApplicationServiceImplTest.java`
    - 处理动作：为精修应用响应、精修来源重生成、当前版本事实读取和质量报告过期判断补齐后端测试。
    - 验收点：Knowledge 后端测试覆盖 `RefinementApplyResult`、`REFINEMENT_APPLIED`、`replaceUnconfirmedOnly=true` 和 `stale=true`。
    - 重要度：9/10

- [ ] `Admin refinement follow-up actions`：精修页展示图谱联动操作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-GRAPH-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-types.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.test.tsx`
    - 处理动作：在精修应用成功后展示查看图谱结果、重生成图谱和重新生成质量报告三个操作。
    - 验收点：应用成功后用户可通过按钮进入指定图谱版本、预填重生成表单或指定版本质量报告页。
    - 重要度：10/10

- [ ] `Admin graph extraction regenerate handoff`：图谱抽取页承接精修重生成参数
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-GRAPH-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-create.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-types.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx`
    - 处理动作：从跳转参数预填重生成表单并提交 `REFINEMENT_APPLIED` 来源。
    - 验收点：从精修页点击重生成图谱后表单自动打开，`replaceUnconfirmedOnly` 默认开启且提交 payload 保留精修来源。
    - 重要度：9/10

- [ ] `Admin graph results version focus`：图谱结果页按版本定位正式结果
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-GRAPH-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-version-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/components/graph-version-detail.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-types.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-page.test.tsx`
    - 处理动作：让图谱结果页读取 `graphVersionId` 并联动版本详情和四类结果表。
    - 验收点：从精修页点击查看图谱结果后直接定位对应版本，切换版本时四类结果表同步刷新。
    - 重要度：8/10

- [ ] `Admin quality report regenerate prompt`：质量报告页提示精修后重算
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-GRAPH-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-generate-form.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-summary.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-types.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.test.tsx`
    - 处理动作：让质量报告页按 `graphVersionId` 定位并在报告过期时展示重新生成入口。
    - 验收点：过期报告展示 warning Alert，点击重新生成后刷新报告历史、摘要、问题、来源和标注明细。
    - 重要度：8/10

- [ ] `Admin knowledge frontend verification`：补齐 Admin Knowledge 前端联动测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-GRAPH-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/refinement/refinement-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-results/graph-results-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.test.tsx`
    - 处理动作：为精修成功动作区、重生成预填、版本定位和质量报告重算提示补齐前端测试。
    - 验收点：前端测试能验证三个精修后续按钮、重生成 payload、版本定位和 stale Alert。
    - 重要度：9/10

- [ ] `branch sync main`：收口前同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/knowledge-refinement-graph-loop`、`origin/main`
    - 处理动作：在功能实现和定向测试补齐后同步 `origin/main` 到当前功能分支并解决冲突。
    - 验收点：当前分支包含最新 `origin/main`，且没有无关冲突残留。
    - 重要度：10/10

- [ ] `Knowledge final validation`：同步 main 后运行最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`
    - 处理动作：同步 main 后运行 RUNBOOK 指定的后端格式、静态检查、测试和前端格式、lint、测试。
    - 验收点：后端 `spotless:check`、`checkstyle:check`、Knowledge 测试和前端 `format:check`、`lint`、admin-web 测试通过或记录明确阻塞。
    - 重要度：10/10

- [ ] `Knowledge Implementation Coverage`：更新 Knowledge 实现覆盖状态
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：将 Knowledge 精修与图谱联动闭环、最终验证命令和剩余缺口同步到 Implementation Coverage。
    - 验收点：Coverage 记录精修应用、图谱重生成引导、版本定位和质量报告重算已完成。
    - 重要度：9/10

- [ ] `RUNBOOK cleanup`：清理 Knowledge 精修图谱联动 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-KNOWLEDGE-REFINEMENT-GRAPH-LOOP.md`、`TODO.md`
    - 处理动作：阶段完成并同步 coverage 后删除临时 RUNBOOK 并从 TODO 中删除已完成任务。
    - 验收点：PR 收口前无已完成 RUNBOOK 残留，`TODO.md` 只保留真实未完成事项或为空。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
