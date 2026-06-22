# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项



- [ ] `WorkerAiHttpClient`：优先使用 `workerPath`
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKER-USECASE-CLOSURE.md` 第 `4.4` 章节
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClient.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClientTest.java`
    - 处理动作：让 `WorkerAiHttpClient` 按 `AiInvokeCommand.workerPath` 发送请求并保留 generic fallback
    - 验收点：usecase path 请求与 legacy generic 请求都被测试覆盖，签名使用实际发送 path
    - 重要度：10/10

- [ ] `AiWorkerInvocationApplicationServiceTest`：补 invocation 层最小回归
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKER-USECASE-CLOSURE.md` 第 `4.5` 章节
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/invocation/AiWorkerInvocationApplicationServiceTest.java`
    - 处理动作：新增同步调用回归测试，锁定 `workerPath` 与 canonical `operation` 不被 invocation 层覆盖
    - 验收点：同步调用测试断言 `workerPath`、`operation`、`candidateId` 与 `saveCandidate()` 行为正确
    - 重要度：8/10

- [ ] `AI Implementation Coverage`：沉淀 Java AI 接入覆盖事实文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKER-USECASE-CLOSURE.md` 第 `7` 章节
    - 范围对象：`docs/30-designs/AI-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：新增 AI Implementation Coverage 文档并按固定表头登记 Java AI 已接入、明确排除和未接入的 workers usecase
    - 验收点：Coverage 文档只表达 Java AI 接入事实，包含 `implemented`、`not_implemented`、`excluded` 三类记录
    - 重要度：8/10

- [ ] `Workers Implementation Coverage`：沉淀 workers 实现覆盖事实文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-WORKER-USECASE-CLOSURE.md` 第 `8` 章节
    - 范围对象：`docs/30-designs/WORKERS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：新增 Workers Implementation Coverage 文档并按固定表头登记 workers 已实现并注册的 AI usecase
    - 验收点：Coverage 文档只表达 workers 已实现事实，所有记录 `status` 固定为 `implemented`
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
