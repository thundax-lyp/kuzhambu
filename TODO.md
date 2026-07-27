# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `KnowledgeGraphWorkbenchController`：新增 knowledge 图谱工作台接口入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-FRONTEND-FLOW.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/workbench/controller/KnowledgeGraphWorkbenchController.java`
    - 处理动作：新增 `manuscript-tree`、`manuscript/get`、`manuscript/extract`、`candidate/get`、`candidate/apply` 接口。
    - 验收点：所有接口挂载 knowledge 权限组并调用 application service。
    - 重要度：10/10

- [ ] `KnowledgeGraphManuscriptTreeAssembler`：统一三类稿件树节点转换
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-FRONTEND-FLOW.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/workbench/support/KnowledgeGraphManuscriptTreeAssembler.java`
    - 处理动作：把 `SANCAI_ENTRY`、`WANGQI_DOCUMENT`、`MING_CUSTOMS` 转成统一 `SOURCE_ROOT/CATEGORY/VOLUME/MANUSCRIPT` 节点。
    - 验收点：三类来源返回相同节点结构并支持按 `parentKey` 懒加载。
    - 重要度：9/10

- [ ] `KnowledgeGraphWorkbenchApplicationServiceImpl`：聚合稿件图谱详情
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-FRONTEND-FLOW.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/workbench/service/impl/KnowledgeGraphWorkbenchApplicationServiceImpl.java`
    - 处理动作：按稿件聚合基础信息、最近抽取任务、最新图谱版本和质量摘要。
    - 验收点：`manuscript/get` 能返回前端详情页所需的完整稿件图谱状态。
    - 重要度：10/10

- [ ] `KnowledgeGraphManuscriptPayloadBuilder`：后台自动组装抽取 payload
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-FRONTEND-FLOW.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/workbench/support/KnowledgeGraphManuscriptPayloadBuilder.java`
    - 处理动作：根据稿件来源和 ID 读取正文并生成内部 `inputPayloadJson`、`scopeJson` 和 prompt 参数。
    - 验收点：`manuscript/extract` 请求无需前端传 `inputPayloadJson` 或 `promptMessagesJson`。
    - 重要度：10/10

- [ ] `KnowledgeGraphWorkbenchApplicationServiceImpl`：按稿件串联候选查询和应用
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-FRONTEND-FLOW.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/workbench/service/impl/KnowledgeGraphWorkbenchApplicationServiceImpl.java`
    - 处理动作：实现最新候选查询和候选应用后返回 `graphVersionId`、`graphStatus`。
    - 验收点：前端可从稿件详情直接完成候选查看、应用和进入精修。
    - 重要度：10/10

- [ ] `graph-workbench-service.ts`：新增前端稿件工作台 service/types
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-FRONTEND-FLOW.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-workbench-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-workbench-types.ts`
    - 处理动作：对接 `graph-workbench` 接口并定义统一稿件树、详情、候选和应用类型。
    - 验收点：前端 service 请求不包含手填 JSON、prompt 或模型字段。
    - 重要度：9/10

- [ ] `GraphExtractionManuscriptTree`：新增统一稿件树组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-FRONTEND-FLOW.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-manuscript-tree.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.css`
    - 处理动作：渲染三类来源统一稿件树、懒加载子节点并展示图谱状态。
    - 验收点：点击 `MANUSCRIPT` 节点能向页面容器提交稿件上下文。
    - 重要度：9/10

- [ ] `GraphExtractionManuscriptDetail`：新增稿件图谱详情组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-FRONTEND-FLOW.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-manuscript-detail.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/components/graph-extraction-candidate-preview.tsx`
    - 处理动作：展示稿件图谱状态、最近任务、最新版本、质量摘要和候选预览。
    - 验收点：详情区提供抽取、应用候选、查看结果和进入精修动作。
    - 重要度：10/10

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
