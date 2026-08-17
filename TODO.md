# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 本清单按图谱素材/任务的 Admin Web RUNBOOK 固定顺序拆分；每项对应一个可独立验证的小步提交。
- 未经本轮人工确认的执行任务均在“待审阅任务项”；确认后才移入“当前任务项”执行。
- 已完成任务必须在其完成 commit 中删除；完成历史保留在 commit 和 PR。

## 当前任务项

## 待审阅任务项

- [ ] `admin-web knowledge routes`：冻结图谱素材与任务页面边界
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/router/index.tsx`、`kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-workbench/`
    - 处理动作：保留素材管理和提取任务入口，并移除新流程到旧 `graph-result`、`refinement` 的跳转依赖。
    - 验收点：路由单测通过，且 `rg` 确认新素材/任务组件不 import 旧 `graph-result` 或 `refinement` service。
    - 重要度：8/10

- [ ] `AW/graph-extraction types`：定义图谱提取任务领域类型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-types.ts`
    - 处理动作：按 RUNBOOK 固定字段定义任务、阶段和候选预览领域类型。
    - 验收点：页面组件只消费 `graph-extraction-types.ts` 导出的领域类型，候选 diff 和 drawer section 使用固定枚举值。
    - 重要度：10/10

- [ ] `AW/graph-material service`：建立图谱素材 HTTP service 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-material-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-material-service-contract.test.ts`
    - 处理动作：实现素材分页、详情、单项提取、批量提取、批量撤回预览和批量撤回 service 方法。
    - 验收点：contract test 断言 URL、body、`idempotencyKey`、`ApiResponse` 解包和正式业务码映射。
    - 重要度：10/10

- [ ] `AW/graph-material mock`：建立图谱素材 Mock adapter 与 fixture
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/__mocks__/graph-material-service-mock.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/__mocks__/graph-mock-data.ts`
    - 处理动作：实现与素材 HTTP service 同签名的 Mock adapter 和素材 fixture。
    - 验收点：Mock 返回正式 `Page<T>` 形状，覆盖未初始化、发布、统计过期、批量部分失败和来源不可见数据。
    - 重要度：9/10

- [ ] `AW/graph-extraction service`：建立图谱提取任务 HTTP service 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service-contract.test.ts`
    - 处理动作：实现任务分页、详情、重试、取消、候选采用、丢弃和重生成 service 方法。
    - 验收点：contract test 断言 URL、body、`idempotencyKey`、任务 `lockVersion`、预期状态和正式业务码映射。
    - 重要度：10/10

- [ ] `AW/graph-extraction mock`：建立图谱提取任务 Mock adapter 与 fixture
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/__mocks__/graph-extraction-service-mock.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/__mocks__/graph-mock-data.ts`
    - 处理动作：实现与提取任务 HTTP service 同签名的 Mock adapter 和任务 fixture。
    - 验收点：Mock 覆盖失败、运行中、成功待审、已处置、候选不可用和版本冲突任务数据。
    - 重要度：9/10

- [ ] `AW/graph-material page query`：素材页接入 service 查询
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-material-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-material-page.test.tsx`
    - 处理动作：用 TanStack Query 调用 `pageMaterials` 替换页面内置素材数组。
    - 验收点：组件测试覆盖加载、空列表、错误恢复和 `material:null` 未初始化素材。
    - 重要度：9/10

- [ ] `AW/graph-material filters`：实现素材筛选与分页控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/material-filters/`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-material-page.tsx`
    - 处理动作：实现关键字、来源类型、分类、卷目、素材状态、任务运行状态、任务采纳状态和分页筛选。
    - 验收点：`material-filters` 测试覆盖筛选变更会生成 `pageMaterials` 查询参数。
    - 重要度：8/10

- [ ] `AW/graph-material table`：实现素材复合表格
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/material-table/`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-material-page.tsx`
    - 处理动作：实现素材标题、来源、统计、状态、任务摘要、候选处置、发布贡献、风险和最近变更列。
    - 验收点：`material-table` 测试覆盖未初始化、统计更新中、权限不足和“查看任务”携带 `contentRefs` 跳转。
    - 重要度：9/10

- [ ] `AW/graph-material batch actions`：实现素材批量动作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/material-batch-actions/`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-material-page.tsx`
    - 处理动作：实现行选择后的批量提取、批量发布、批量撤回和查看任务动作。
    - 验收点：`material-batch-actions` 测试覆盖无选择隐藏、批量部分失败和逐素材结果展示。
    - 重要度：8/10

- [ ] `AW/graph-material drawer shell`：建立素材分段详情抽屉
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/material-detail-drawer/`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-material-page.tsx`
    - 处理动作：用 `KuzhambuSegmentedDrawer` 建立素材详情四段容器并按打开状态调用 `getMaterial`。
    - 验收点：测试覆盖四段可访问、关闭后清除选中素材和详情加载错误可恢复。
    - 重要度：9/10

- [ ] `AW/graph-material overview panel`：实现素材概览段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/material-overview-panel/`
    - 处理动作：实现素材来源、统计、风险和最近活动展示。
    - 验收点：`material-overview-panel` 测试覆盖未初始化素材、统计过期和发布素材。
    - 重要度：8/10

- [ ] `AW/graph-material draft canvas`：改造素材草稿画布段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/material-draft-canvas/material-draft-canvas.tsx`
    - 处理动作：将草稿画布限制为素材 drawer 的 `DRAFT_GRAPH` 段并处理已发布只读状态。
    - 验收点：测试覆盖已发布素材画布只读，且关闭抽屉后不保留旧草稿对象状态。
    - 重要度：8/10

- [ ] `AW/graph-material task summary panel`：实现素材任务摘要段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/material-task-summary-panel/`
    - 处理动作：实现素材详情中的任务摘要和跳转入口。
    - 验收点：测试覆盖任务段无草稿编辑控件，且“查看任务”跳转到提取任务并携带素材引用。
    - 重要度：8/10

- [ ] `AW/graph-material publication panel`：改造发布与变更段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/publication-preview/publication-preview.tsx`
    - 处理动作：将发布、撤回和删除预检入口收敛到素材 drawer 的发布与变更段。
    - 验收点：测试覆盖独立删除变更/删除任务菜单不再作为新流程入口。
    - 重要度：9/10

- [ ] `AW/graph-extraction page query`：提取页接入任务队列查询
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx`
    - 处理动作：用 `pageTasks` 替换旧提取工作台数据入口并默认查询全局 flat 任务队列。
    - 验收点：测试覆盖默认 `groupBy=NONE`、服务端 grouped 模式切换和不在浏览器重组 flat 结果。
    - 重要度：9/10

- [ ] `AW/graph-extraction filters`：实现任务筛选与分页控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/task-filters/`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`
    - 处理动作：实现关键字、来源类型、分类、卷目、`contentRefs`、`batchId`、运行状态、采纳状态和分页筛选。
    - 验收点：`task-filters` 测试覆盖 `batchId` 与 `contentRefs` 查询参数。
    - 重要度：8/10

- [ ] `AW/graph-extraction task table`：实现提取任务表格
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-task-table/`
    - 处理动作：实现任务素材、运行状态、采纳状态、阶段、摘要、失败原因、关联任务和清理时间列。
    - 验收点：`graph-extraction-task-table` 测试覆盖运行任务取消和失败任务重试的可见性。
    - 重要度：9/10

- [ ] `AW/graph-extraction batch create`：实现批量创建面板
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/task-batch-create-panel/`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-page.tsx`
    - 处理动作：实现已选素材或整卷批量创建任务入口。
    - 验收点：测试覆盖 `contentRefs` 和 `volumeCode` 输入互斥，且面板只调用 `createBatchExtraction`。
    - 重要度：8/10

- [ ] `AW/graph-extraction permission cleanup`：移除旧任务权限依赖
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/`、`kuzhambu-apps/admin-web/src/layouts/admin-layout.tsx`
    - 处理动作：删除对 `knowledge:graph:apply` 的依赖并改用既有 `knowledge:graph:edit`。
    - 验收点：`rg` 确认新提取任务页面不再引用 `knowledge:graph:apply`。
    - 重要度：9/10

- [ ] `AW/graph-extraction drawer shell`：建立任务分段详情抽屉
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/task-detail-drawer/`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-task-detail/`
    - 处理动作：用 `KuzhambuSegmentedDrawer` 建立任务详情四段容器并接入 `getTask`。
    - 验收点：测试覆盖 `OVERVIEW`、`EXECUTION`、`CANDIDATE`、`DISPOSITION` 四段可访问。
    - 重要度：9/10

- [ ] `AW/graph-extraction execution panel`：实现任务执行过程段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/task-execution-panel/`
    - 处理动作：按阶段展示进度、摘要、失败原因和时间字段。
    - 验收点：测试覆盖阶段进度、失败原因展示，且不显示完整正文或提示词。
    - 重要度：8/10

- [ ] `AW/graph-extraction candidate panel`：实现候选预览段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/task-candidate-panel/`
    - 处理动作：展示候选节点、边、告警和 diff 预览。
    - 验收点：测试覆盖 `ADD`、`UPDATE`、`REMOVE`、`CONFLICT` 和 `candidate:null` 候选不可用空态。
    - 重要度：9/10

- [ ] `AW/graph-extraction disposition panel`：实现候选处置动作段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/task-disposition-panel/`
    - 处理动作：按任务状态渲染重试、取消、合并、覆盖、丢弃和重新抽取动作。
    - 验收点：测试覆盖失败、运行中、成功待审和已处置状态下的按钮可见性。
    - 重要度：9/10

- [ ] `AW/graph-extraction task mutations`：实现任务 mutation 刷新与冲突处理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/task-detail-drawer/`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/task-disposition-panel/`
    - 处理动作：每次任务写操作发送当前 `lockVersion` 和预期状态，并在成功后失效相关 query。
    - 验收点：测试覆盖版本冲突只刷新不猜测最终状态，且处置成功后按钮消失。
    - 重要度：9/10

- [ ] `AW knowledge graph service switch`：切换真实 Knowledge HTTP service
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-material-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-service.ts`
    - 处理动作：保持领域类型和组件不变，将 adapter 入口从 Mock 切换到 `postJson`。
    - 验收点：contract tests 仍通过，且错误处理通过业务码映射而不是字符串 message。
    - 重要度：8/10

- [ ] `AW knowledge graph e2e`：补充素材任务端到端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/knowledge-graph-material-task.spec.ts`
    - 处理动作：补充素材页、素材 drawer、单项提取、任务 drawer、失败重试、候选采用和批量部分失败 E2E。
    - 验收点：E2E 验证 Network 只请求 `/knowledge/graph/**`，不请求 Classics、AI 或旧图谱 URL。
    - 重要度：8/10

- [ ] `AW knowledge graph readiness evidence`：记录真实联调开发中证据
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`docs/40-readiness/`
    - 处理动作：记录 Knowledge HTTP service 完整可联调前提下的 Admin Web 开发中验证结果。
    - 验收点：readiness 证据覆盖素材页、素材 drawer、任务 drawer、失败重试、候选采用和批量部分失败。
    - 重要度：8/10

- [ ] `AW legacy graph extraction cleanup`：独立清理旧图谱提取组件和旧 service
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-MATERIAL-TASK-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-extraction/graph-extraction-manuscript-tree/`、`graph-extraction-manuscript-detail/`、`graph-workbench-service.ts`、`graph-extraction-candidate-modal.tsx`
    - 处理动作：在 W4/W5 停止引用后，单独删除无引用的旧组件和旧 service。
    - 验收点：`rg` 证明无引用，相关单测、lint 和 build 通过。
    - 重要度：7/10
