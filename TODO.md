# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `admin-web graph mock / router / menu`：建立图谱 Mock 基础与入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-*/__mocks__/graph-mock-data.ts`（5 个页面域）、`kuzhambu-apps/admin-web/src/router/index.tsx`、`db/data-source/system.json`（7 个文件）
    - 处理动作：建立五种素材状态、发布冲突、批量部分失败、孤立节点和删除失败任务的无 HTTP Mock 数据，并注册五个新图谱路由与菜单。
    - 验收点：五个路由和菜单均可解析；Mock 数据覆盖 `DRAFT`、`PUBLISHING`、`PUBLISHED`、`WITHDRAWING`、`FAILED` 及 `failureReason`；不新增 service、`postJson` 或网络等待逻辑。
    - 重要度：10/10

- [ ] `admin-web graph workbench overview`：实现工作台指标与筛选
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-workbench/graph-workbench-page.tsx`、`graph-workbench-types.ts`、`graph-workbench-page.test.tsx`（3 个文件）
    - 处理动作：实现工作台指标卡、关键字搜索和门类筛选的 Mock 页面状态。
    - 验收点：测试覆盖标题、指标、搜索筛选、空态、无权限态和失败态；页面不加载全图。
    - 重要度：9/10

- [ ] `admin-web graph workbench canvas`：实现工作台渐进局部画布
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-workbench/graph-workbench-canvas.tsx`、`graph-workbench-page.tsx`、`graph-workbench-page.test.tsx`、`graph-workbench-detail-drawer.tsx`（4 个文件）
    - 处理动作：使用 `KuzhambuGraph` 实现种子淡化、分批补边、孤立节点移除和右侧详情抽屉。
    - 验收点：测试断言首批边到达前种子淡化、每批追加节点和边、结束后孤立节点移除、详情可跳转治理或素材；最终最多渲染 200 个节点。
    - 重要度：10/10

- [ ] `admin-web graph material library`：实现素材库与状态展示
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-material-page.tsx`、`graph-material-types.ts`、`graph-material-page.test.tsx`（3 个文件）
    - 处理动作：实现素材状态、抽取任务入口、空态和失败态的 Mock 列表。
    - 验收点：五种素材状态都有明确可见标签；`FAILED` 显示失败原因；`PUBLISHING` 与 `WITHDRAWING` 不显示草稿写操作。
    - 重要度：9/10

- [ ] `admin-web graph batch publication`：实现批量发布结果面板
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-batch-publication-panel.tsx`、`graph-batch-publication-panel.test.tsx`、`graph-material-page.tsx`（3 个文件）
    - 处理动作：按用户选择顺序展示每份素材的 Mock 预览、确认和成功或失败结果。
    - 验收点：一份素材失败时其余素材结果仍保留且顺序不变；不引入真实批量发布请求。
    - 重要度：9/10

- [ ] `admin-web graph material editor`：实现单素材草稿画布
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-material-detail-page.tsx`、`graph-material-draft-canvas.tsx`、`graph-material-detail-page.test.tsx`、`graph-material-object-drawer.tsx`（4 个文件）
    - 处理动作：实现正文摘要、草稿节点边 CRUD/抽取/导入入口、画布和对象详情的 Mock 交互。
    - 验收点：`DRAFT` 可显示草稿写操作；`PUBLISHED` 仅显示发布结果和撤回入口；测试覆盖无权限、空态和失败态。
    - 重要度：10/10

- [ ] `admin-web graph publication preview`：实现单素材画布内发布预览
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-material/graph-publication-preview.tsx`、`graph-publication-preview.test.tsx`、`graph-material-detail-page.tsx`（3 个文件）
    - 处理动作：在当前草稿画布内实现四色发布预览、冲突定位和确认禁用状态。
    - 验收点：绿/橙/红/蓝四类对象均可见；未决红色冲突时确认发布不可用；不创建独立发布预览路由。
    - 重要度：10/10

- [ ] `admin-web graph governance browse`：实现整体治理浏览
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-governance/graph-governance-page.tsx`、`graph-governance-table.tsx`、`graph-governance-detail-drawer.tsx`、`graph-governance-page.test.tsx`（4 个文件）
    - 处理动作：实现发布节点关系表、局部画布、来源和审计详情的 Mock 浏览。
    - 验收点：节点与关系选择能联动画布和详情；测试覆盖标题、空态、无权限态和失败态；本项不实现高风险写操作。
    - 重要度：9/10

- [ ] `admin-web graph governance confirmation`：实现治理影响预览与二次确认
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-governance/graph-governance-impact-drawer.tsx`、`graph-governance-impact-drawer.test.tsx`、`graph-governance-page.tsx`、`graph-governance-mapping-assignment.tsx`（4 个文件）
    - 处理动作：实现创建、编辑、删除、合并和拆分的 Mock 影响抽屉、映射分配和二次确认。
    - 验收点：高风险动作必须先展示节点、边、映射和 issue；确认前操作不可执行；合并和拆分不以表格替代映射分配。
    - 重要度：10/10

- [ ] `admin-web graph deletion changes`：实现删除变更列表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-deletion-change/graph-deletion-change-page.tsx`、`graph-deletion-change-decision-panel.tsx`、`graph-deletion-change-page.test.tsx`（3 个文件）
    - 处理动作：实现删除影响、`PRESERVE_CONTRIBUTION` 和 `WITHDRAW_ASSOCIATIONS` 两个 Mock 决策入口。
    - 验收点：两个决策按钮分别显示不可逆影响；测试覆盖空态、无权限态和决策失败态。
    - 重要度：9/10

- [ ] `admin-web graph deletion tasks`：实现删除任务列表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/graph-deletion-task/graph-deletion-task-page.tsx`、`graph-deletion-task-detail-drawer.tsx`、`graph-deletion-task-page.test.tsx`（3 个文件）
    - 处理动作：实现删除任务状态、失败原因和重试入口的 Mock 列表。
    - 验收点：失败任务显示原因和重试操作；测试覆盖任务详情、重试后的 Mock 状态变化和空态。
    - 重要度：9/10

- [ ] `admin-web graph mock e2e`：覆盖图谱 Mock 端到端冒烟
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/knowledge/graph/graph-mock.spec.ts`、`graph-mock.fixture.ts`（2 个文件）
    - 处理动作：以 Mock provider 固定覆盖抽取到撤回、治理合并拆分、删除预检到重试和工作台截断流程。
    - 验收点：E2E 不访问后端端口；完整流程通过；失败分支包含发布部分失败和删除任务失败重试。
    - 重要度：10/10

- [ ] `graph admin-web closure`：清理前端任务现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-GRAPH-ADMIN-WEB.md`（2 个文件）
    - 处理动作：在全部前端 TODO 真正完成并记录验证结果后，删除本组 TODO 和已完成 RUNBOOK。
    - 验收点：不保留完成任务、完成历史或失效 RUNBOOK；保留已交付的 Mock 路由、provider、页面单测和 Mock E2E；工作区无本任务临时文件。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
