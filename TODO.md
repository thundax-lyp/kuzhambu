# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 未经本轮人工确认的执行任务均在“待审阅任务项”；确认后才移入“当前任务项”执行。
- 已完成任务必须在其完成 commit 中删除；完成历史保留在 commit 和 PR。

## 当前任务项

- [ ] `知识图谱工作台概览接口`：将 overview 切换为快照读取
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-WORKBENCH-READ-ONLY-ATLAS.md`
    - 范围对象：`GraphWorkbenchApplicationService.java`、`GraphWorkbenchApplicationServiceImpl.java`、`GraphController.java`、`GraphWorkbenchResponses.java`、`GraphControllerTest.java`、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 处理动作：让 overview 只读取 snapshot store，向响应补充 `snapshotAt` 并同步接口文档。
    - 验收点：有旧快照时正常返回；无快照返回 `WORKBENCH_SNAPSHOT_UNAVAILABLE`；Controller 不触发统计 SQL；接口文档字段一致。
    - 重要度：10/10

- [ ] `知识图谱工作台首批关系接口`：以最近关系替换 seeds
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-WORKBENCH-READ-ONLY-ATLAS.md`
    - 范围对象：`GraphPublishedEdgeRepository*`、`GraphWorkbenchApplicationService*`、`GraphController.java`、`GraphWorkbenchRequests.java`、`GraphWorkbenchResponses.java`、`GraphInterfaceAssembler.java`、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 处理动作：以 `recent-edges/list` 返回最近 200 条 ACTIVE 边及其去重端点，删除 `seeds/list` 并同步接口文档。
    - 验收点：结果按 `modified_at DESC, id DESC` 排序；节点恰为边端点；旧 Seeds 路由、请求、响应和方法已删除；接口文档已替换。
    - 重要度：10/10

- [ ] `知识图谱工作台一跳接口`：以固定批次替换 incident edges
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-WORKBENCH-READ-ONLY-ATLAS.md`
    - 范围对象：`GraphPublishedEdgeRepository*`、`GraphWorkbenchApplicationService*`、`GraphController.java`、`GraphWorkbenchRequests.java`、`GraphWorkbenchResponses.java`、`GraphInterfaceAssembler.java`、`GraphRequestsTest.java`、`docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md`
    - 处理动作：以 `one-hop-edges/list` 替换 `incident-edges/list`，固定服务端每批 50 条并同步接口文档。
    - 验收点：请求仅接受 1..400 个 `nodeIds` 和可选 `afterEdgeId`；`truncated` 与 `nextCursor` 一致；旧 Incident 命名与路由已删除；接口文档已替换。
    - 重要度：10/10

- [ ] `admin 图谱工作台数据层`：实现只读态势视图模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-WORKBENCH-READ-ONLY-ATLAS.md`
    - 范围对象：`graph-workbench-types.ts`、`graph-workbench-service.ts`、`hooks/use-graph-workbench-atlas.ts`
    - 处理动作：实现三段读取、独立 overview/graph 状态、游标映射、去重及 600 边封顶。
    - 验收点：首批空边不扩展；后续固定端点集合传递 `afterEdgeId`；节点只随已接纳边出现；取消请求不写入过期 state。
    - 重要度：10/10

- [ ] `admin 图谱工作台画布`：实现无操作的 G6 渲染组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-WORKBENCH-READ-ONLY-ATLAS.md`
    - 范围对象：`graph-workbench-canvas/`、`graph-workbench-types.ts`
    - 处理动作：实现只接收 `{ graph, motion }` 的页面私有 G6 Canvas。
    - 验收点：颜色、形状、标签密度、分批动画和 reduced motion 符合 RUNBOOK；未注册点击、拖拽、缩放或平移行为。
    - 重要度：9/10

- [ ] `admin 图谱工作台页面`：装配只读态势展示并清理旧 UI
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-WORKBENCH-READ-ONLY-ATLAS.md`
    - 范围对象：`graph-workbench-page.tsx`、`graph-workbench-page.css`、`graph-workbench-overview/`、`graph-workbench-legend/`、`graph-workbench-activity-timeline/`、`workbench-canvas/`、`workbench-detail-drawer/`
    - 处理动作：装配四个只读展示组件并删除旧筛选表、原型画布和详情抽屉。
    - 验收点：首屏布局、窄屏、可访问文本和无操作边界符合 RUNBOOK；旧目录已删除。
    - 重要度：10/10

- [ ] `图谱工作台交付验证`：固化跨层验证证据并收口文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-GRAPH-WORKBENCH-READ-ONLY-ATLAS.md`
    - 范围对象：RUNBOOK Test file map、`docs/40-readiness/KNOWLEDGE-GRAPH-WORKBENCH-EVIDENCE.md`、`TODO.md`、RUNBOOK
    - 处理动作：完成契约、页面、E2E 和运行验证，沉淀证据并清理临时任务文档。
    - 验收点：相关 Maven、pnpm 和 Playwright 检查通过；readiness 留存证据；完成项从 TODO 删除，RUNBOOK 与其引用一并删除。
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
