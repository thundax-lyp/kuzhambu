# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `graph-extraction-page.tsx`：改造图谱抽取页面编排
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-FRONTEND-FLOW.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-create.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-task-table.tsx`
    - 处理动作：用稿件树和稿件详情替换手填 JSON 创建入口并保留任务台账视角。
    - 验收点：页面主流程为选择稿件、抽取图谱、查看候选、应用候选和进入精修。
    - 重要度：10/10

- [ ] `KnowledgeGraphWorkbenchControllerTest`：补齐新增接口冒烟和前端契约测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-FRONTEND-FLOW.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/workbench/controller/KnowledgeGraphWorkbenchControllerTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/workbench/service/impl/KnowledgeGraphWorkbenchApplicationServiceImplTest.java`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-workbench-service-contract.test.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx`
    - 处理动作：覆盖新增接口、三类来源、自动 payload、权限和前端闭环。
    - 验收点：Maven 指定测试和前端 vitest 均通过，且服务契约断言前端不发送 JSON 手填字段。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
