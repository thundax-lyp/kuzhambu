# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 未经本轮人工确认的执行任务均在“待审阅任务项”；确认后才移入“当前任务项”执行。
- 已完成任务必须在其完成 commit 中删除；完成历史保留在 commit 和 PR。

## 当前任务项

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
