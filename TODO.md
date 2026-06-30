# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `kuzhambu-ai-application refinement invoke path`：AI-WORKERS T2 去掉 image_analysis legacy 分支
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-ANALYSIS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImplTest.java`
    - 处理动作：移除 `image_analysis` 的 legacy `operation/workerPath` 旁路，统一改为 resolver 分发。
    - 验收点：`analyzeImage(...)` 传给 invocation service 的 `operation` 固定为 `CLASSICS_SANCAI_IMAGE_ANALYSIS`，`workerPath` 固定为 `/internal/ai/classics/sancai/image-analysis`。
    - 重要度：10/10

- [ ] `kuzhambu-ai-application stream command`：AI-WORKERS T3 固定 image_analysis 的 stream 调用语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-ANALYSIS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImpl.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImplTest.java`
    - 处理动作：仅为 `image_analysis` 显式固定 `AiInvokeCommand.stream = true` 并保持 `createCandidate = true`。
    - 验收点：image analysis 调用命令的 `stream = true`、`createCandidate = true`，且不扩大到其他 capability 的 stream 组装重构。
    - 重要度：10/10

- [ ] `kuzhambu-ai final-state markdown model`：AI-WORKERS T4 锁定 image_analysis 最终结果落库语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-ANALYSIS-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiInvokeResult.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCallRecord.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-domain/src/main/java/com/thundax/kuzhambu/ai/domain/invocation/model/entity/AiCandidate.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImplTest.java`
    - 处理动作：通过测试锁定 `image_analysis` 继续复用统一的 `MARKDOWN + resultPayload` 最终结果模型，不新增字段。
    - 验收点：`AiInvokeResult.resultFormat/resultPayload`、`AiCallRecord.streamUsed/resultFormat/resultPayload`、`AiCandidate.resultFormat/resultPayload` 的语义保持统一，且无 schema 变更。
    - 重要度：8/10

- [ ] `docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`：AI-WORKERS T5 更新 image_analysis 完成口径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-ANALYSIS-CLOSURE.md`
    - 范围对象：`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：将 `CLASSICS_SANCAI_IMAGE_ANALYSIS` 从 `部分完成` 收口为 `已完成`，并改写说明为标准 usecase path 与 stream final-state 协议已闭合。
    - 验收点：该项不再保留 legacy Java 入口说明，且 `WORKERS-IMPLEMENTATION-COVERAGE.md` 保持不变。
    - 重要度：7/10

- [ ] `docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-ANALYSIS-CLOSURE.md`：AI-WORKERS T6 清理现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-ANALYSIS-CLOSURE.md`、`TODO.md`
    - 处理动作：在所有代码、测试和覆盖文档完成后删除临时 RUNBOOK，并同步清空本轮 TODO 项。
    - 验收点：该 RUNBOOK 已删除，`TODO.md` 中本轮任务已清空，不保留已完成项。
    - 重要度：6/10

## 待讨论项
