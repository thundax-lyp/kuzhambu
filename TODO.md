# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `portal atlas 图谱控件`：实现只读 KnowledgeGraphCanvas
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-14-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-graph-canvas.tsx`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-graph-layout.ts`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-graph-canvas.css`
    - 处理动作：封装 `ReactFlow` 只读画布、overview 径向布局、category/detail dagre 布局和节点点击跳转
    - 验收点：画布支持缩放、平移、fit view 和节点跳转，不支持拖拽编辑、连线或删除
    - 重要度：10/10

- [ ] `portal atlas 页面接入`：接入画布并锁定页面交互
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-14-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.css`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.test.tsx`
    - 处理动作：在 overview、category 和 detail 三层主 stage 接入 `KnowledgeGraphCanvas` 并补齐空门类、实体跳转和详情展示测试
    - 验收点：页面测试覆盖 14 门类展示、空门类 category 空态、实体节点跳转和 detail 焦点关系展示
    - 重要度：10/10

- [ ] `knowledge graph closure 初次验证`：运行实现后的相关验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-14-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge`、`kuzhambu-apps/portal-web`、`/knowledge/atlas`
    - 处理动作：在功能实现完成后运行 RUNBOOK 中的 Maven、npm 和桌面/移动端视觉冒烟验证
    - 验收点：格式、lint、测试、build 和四个 atlas URL 的视觉检查均通过且无抽取类网络调用
    - 重要度：9/10

- [ ] `knowledge graph closure main 同步`：收口前同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-14-CLOSURE.md`
    - 范围对象：`codex/knowledge-graph-14-closure` worktree
    - 处理动作：在初次验证通过后同步最新 `main` 并解决冲突
    - 验收点：当前分支包含最新 `main` 代码，且同步后没有遗留冲突
    - 重要度：9/10

- [ ] `knowledge graph closure 最终复验`：同步 main 后重跑受影响验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-14-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge`、`kuzhambu-apps/portal-web`、`/knowledge/atlas`
    - 处理动作：同步 `main` 后重跑受影响的 Maven、npm、build 和视觉冒烟验证
    - 验收点：最终工作区基于最新 `main` 通过相关验证，且无抽取类网络调用回归
    - 重要度：10/10

- [ ] `knowledge coverage 更新`：同步 Implementation Coverage
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-14-CLOSURE.md`
    - 范围对象：`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：只把实际完成的图谱可视化画布和固定 14 门类空位展示改为已完成，并保留一键重提取缺口
    - 验收点：Implementation Coverage 状态与实际交付一致，未把质量报告低质量门类一键重提取改为已完成
    - 重要度：9/10

- [ ] `knowledge graph closure RUNBOOK 清理`：清理临时 RUNBOOK 和已完成 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-14-CLOSURE.md`
    - 范围对象：`docs/30-designs/RUNBOOK-KNOWLEDGE-GRAPH-14-CLOSURE.md`、`TODO.md`
    - 处理动作：PR 收口前删除临时 RUNBOOK，并按完成状态删除或收窄对应 TODO
    - 验收点：临时 RUNBOOK 已清理，TODO.md 不保留已完成任务
    - 重要度：9/10

## 待审阅任务项

## 待讨论项
