# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `AiInvocationControllerTest`：补 AI 候选管理接口测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-CANDIDATE-CONFIRMATION.md#B3 后端接口测试`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/AiInvocationControllerTest.java`
    - 处理动作：覆盖 reject 和 mark-applied 请求映射到 `AiCandidateDomainService`。
    - 验收点：AI interface 测试证明两个旧管理入口仍可用且未新增 apply 入口。
    - 重要度：7/10

- [ ] `ClassicsContentApplicationServiceAiCandidateTest`：补 Classics 应用候选服务测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-CANDIDATE-CONFIRMATION.md#B3 后端接口测试`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`
    - 处理动作：覆盖 summary、tags、qa 和 REJECTED 候选应用失败。
    - 验收点：测试证明应用会生成 `AI_APPLIED` 版本，且 tags/qa 只替换 AI 来源数据。
    - 重要度：10/10

- [ ] `ai-candidate-service`：新增 Admin Web AI 候选 API
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-CANDIDATE-CONFIRMATION.md#F1 AI 候选 API`
    - 范围对象：`kuzhambu-apps/admin-web/src/api/ai/ai-candidate-service.ts`、`kuzhambu-apps/admin-web/src/api/ai/ai-candidate-types.ts`
    - 处理动作：新增候选列表、应用、拒绝三个 API 方法和对应类型。
    - 验收点：前端不新增 AI refinement API 文件，不调用 `/ai/refinement/*`。
    - 重要度：8/10

- [ ] `ai-candidate-panel`：新增 Admin Web 通用候选面板
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-CANDIDATE-CONFIRMATION.md#F2 通用候选面板`
    - 范围对象：`kuzhambu-apps/admin-web/src/components/ai/ai-candidate-panel.tsx`、`kuzhambu-apps/admin-web/src/components/ai/ai-candidate-payload-editor.tsx`
    - 处理动作：实现 PENDING 候选列表、payload 编辑器、应用按钮和拒绝按钮。
    - 验收点：summary/translate 使用文本框，tags 保存为 `{"tags":[...]}`，qa 保存为 `{"qaPairs":[...]}`，应用成功后刷新列表并调用 `onApplied()`。
    - 重要度：9/10

- [ ] `Classics 页面候选面板接入`：接入三类 Classics 详情页
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-CANDIDATE-CONFIRMATION.md#F3 接入 Classics 页面`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-document-model.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-model.tsx`
    - 处理动作：在三类详情页挂载 `AiCandidatePanel` 并传入 RUNBOOK 指定 capabilities。
    - 验收点：页面只展示、编辑、应用、拒绝已有候选，不新增生成按钮和 AI 触发配置表单。
    - 重要度：8/10

- [ ] `AI 候选确认闭环现场清理`：清理临时 RUNBOOK 和已完成 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-CANDIDATE-CONFIRMATION.md#Validation Commands`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-AI-CANDIDATE-CONFIRMATION.md`
    - 处理动作：在后端和前端验证命令通过后，删除临时 RUNBOOK，并从 `TODO.md` 删除已完成的 AI 候选确认闭环任务。
    - 验收点：`TODO.md` 不保留已完成任务，`docs/30-designs/RUNBOOK-AI-CANDIDATE-CONFIRMATION.md` 已删除。
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
