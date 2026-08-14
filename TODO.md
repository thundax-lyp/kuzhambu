# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 本清单按 `RUNBOOK-GRAPH-ADMIN-WEB.md` 的固定顺序拆分；每项预计触及 2–10 个文件，并对应一个可独立验证的小步提交。
- 未经本轮人工确认的执行任务均在“待审阅任务项”；确认后才移入“当前任务项”执行。
- 已完成任务必须在其完成 commit 中删除；完成历史保留在 commit 和 PR。

## 当前任务项

## 待审阅任务项

- [ ] `03` `admin-web graph workbench canvas`：实现工作台渐进局部画布
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`src/pages/knowledge/graph-workbench/graph-workbench-page.tsx`、`graph-workbench-page.test.tsx`、`workbench-canvas/`、`workbench-detail-drawer/`（4–6 个文件）
    - 处理动作：使用 `KuzhambuGraph` 实现种子淡化、批次补边、孤立节点移除和详情抽屉。
    - 验收点：测试断言边批次返回前种子淡化、批次追加和完成后移除孤立节点；详情可跳转治理或素材；画布最多 200 个节点。
    - 重要度：10/10

- [ ] `04` `admin-web graph material library`：实现素材库与状态展示
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`src/pages/knowledge/graph-material/graph-material-page.tsx`、`graph-material-types.ts`、`graph-material-page.test.tsx`（3 个文件）
    - 处理动作：实现素材状态、抽取任务入口、空态和失败态的 Mock 列表。
    - 验收点：五种状态有可见标签；`FAILED` 显示 `failureReason`；`PUBLISHING` 与 `WITHDRAWING` 不显示草稿写操作。
    - 重要度：9/10

- [ ] `05` `admin-web graph batch publication`：实现批量发布结果
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`src/pages/knowledge/graph-material/graph-material-page.tsx`、`graph-material-page.test.tsx`、`batch-publication-panel/`（4–5 个文件）
    - 处理动作：按选择顺序展示逐素材的 Mock 预览、确认和结果。
    - 验收点：部分失败不覆盖其他结果且顺序不变；不引入真实发布请求。
    - 重要度：9/10

- [ ] `06` `admin-web graph material draft`：实现单素材草稿画布
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`src/pages/knowledge/graph-material/graph-material-page.tsx`、`graph-material-types.ts`、`graph-material-page.test.tsx`、`material-draft-canvas/`、`material-object-drawer/`（5–7 个文件）
    - 处理动作：实现素材摘要、草稿 CRUD、抽取、导入、画布和对象详情的 Mock 交互。
    - 验收点：`DRAFT` 显示写操作；`PUBLISHED` 仅显示发布结果和撤回；测试覆盖无权限、空态和失败态。
    - 重要度：10/10

- [ ] `07` `admin-web graph publication preview`：实现画布内发布预览
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`src/pages/knowledge/graph-material/graph-material-page.tsx`、`graph-material-page.test.tsx`、`publication-preview/`（4–5 个文件）
    - 处理动作：实现四色预览、冲突定位、发布冻结和撤回状态。
    - 验收点：绿、橙、红、蓝对象均可见；红色冲突未决时发布不可用；不新增发布预览路由。
    - 重要度：10/10

- [ ] `08` `admin-web graph governance browse`：实现整体治理浏览
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`src/pages/knowledge/graph-governance/graph-governance-page.tsx`、`graph-governance-types.ts`、`graph-governance-page.test.tsx`、`governance-table/`、`governance-detail-drawer/`（5–7 个文件）
    - 处理动作：实现节点关系表、局部画布、来源和审计详情的 Mock 浏览。
    - 验收点：节点与关系选择联动画布和详情；测试覆盖标题、空态、无权限态和失败态；不含高风险写操作。
    - 重要度：9/10

- [ ] `09` `admin-web graph governance confirmation`：实现治理影响确认
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`src/pages/knowledge/graph-governance/graph-governance-page.tsx`、`graph-governance-page.test.tsx`、`governance-impact-drawer/`、`governance-mapping-assignment/`（5–7 个文件）
    - 处理动作：实现创建、编辑、删除、合并和拆分的 Mock 影响预览与二次确认。
    - 验收点：确认前显示受影响节点、边、映射和 issue；合并与拆分使用映射分配界面而非表格。
    - 重要度：10/10

- [ ] `10` `admin-web graph deletion changes`：实现删除变更列表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`src/pages/knowledge/graph-deletion-change/graph-deletion-change-page.tsx`、`graph-deletion-change-types.ts`、`graph-deletion-change-page.test.tsx`、`deletion-decision-panel/`（4–5 个文件）
    - 处理动作：实现删除影响及两个不可逆决策入口。
    - 验收点：`PRESERVE_CONTRIBUTION` 与 `WITHDRAW_ASSOCIATIONS` 分别展示影响；测试覆盖空态、无权限态和决策失败态。
    - 重要度：9/10

- [ ] `11` `admin-web graph deletion tasks`：实现删除任务列表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`src/pages/knowledge/graph-deletion-task/graph-deletion-task-page.tsx`、`graph-deletion-task-types.ts`、`graph-deletion-task-page.test.tsx`、`deletion-task-detail-drawer/`（4–5 个文件）
    - 处理动作：实现删除任务、失败原因和重试入口的 Mock 列表。
    - 验收点：失败任务显示原因并可重试；测试覆盖详情、重试后的状态变化和空态。
    - 重要度：9/10

- [ ] `12` `admin-web graph mock e2e`：覆盖图谱 Mock 端到端流程
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/knowledge/graph/graph-mock.spec.ts`、`graph-mock.fixture.ts`（2 个文件）
    - 处理动作：通过 Mock provider 覆盖抽取至撤回、治理合并拆分、删除预检至重试和工作台截断。
    - 验收点：E2E 不访问后端端口；完整流程与发布部分失败、删除任务失败重试分支均通过。
    - 重要度：10/10

- [ ] `13` `graph admin-web closure`：清理图谱 Admin Web 任务现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`（2 个文件）
    - 处理动作：在全部前置任务及完整验证完成后，删除本任务 TODO 和 RUNBOOK。
    - 验收点：不保留已完成任务、完成历史、失效 RUNBOOK 或临时文件；Mock 路由、provider、页面单测与 Mock E2E 保留。
    - 重要度：10/10

## 待讨论项
