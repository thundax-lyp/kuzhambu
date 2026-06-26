# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `admin graph extraction page`：扩展批量任务、重生成与取消页面交互
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`graph-extraction-create.tsx`、`graph-extraction-task-table.tsx`、`graph-extraction-task-detail.tsx`、`graph-extraction-page.tsx`、`graph-extraction-page.test.tsx`
    - 处理动作：增加批量范围输入、重生成动作、取消批任务动作与批次字段展示
    - 验收点：页面可创建批量任务、取消未完成批任务并区分普通抽取与重生成
    - 重要度：8/10

- [ ] `ai knowledge backend tests`：补齐 AI × Knowledge 后端测试覆盖
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`KnowledgeAiExtractionApplicationServiceImplTest.java`、`KnowledgeGraphExtractionApplicationServiceTest.java`、`KnowledgeGraphExtractionControllerTest.java`、`GraphExtractionTaskRepositoryImplTest.java`
    - 处理动作：覆盖批量创建、重生成、质量触发、取消批任务与 `batchJobId` 追踪
    - 验收点：application、interface、repository 测试通过
    - 重要度：9/10

- [ ] `ai knowledge closure cleanup`：清理 TODO 与 RUNBOOK 现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-AI-KNOWLEDGE-CLOSURE.md`、相关实现覆盖文档与 PR 描述
    - 处理动作：在闭环完成后删除对应 TODO 项、移除已完成 RUNBOOK，并同步实现覆盖与 PR 收口信息
    - 验收点：TODO 不残留已完成项、已完成 RUNBOOK 被删除且收口文档状态一致
    - 重要度：7/10

## 待讨论项
