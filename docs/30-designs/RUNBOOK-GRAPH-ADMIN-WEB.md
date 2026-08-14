# RUNBOOK Graph Admin Web

## Purpose

按新 HTTP 契约交付双空间图谱 Admin UX。执行顺序固定为：Mock 页面实现 → Mock E2E 冒烟 → 提交；本分支不接入或等待真实后端 API，不得直接改旧 `graph-extraction` 或 `refinement` 页面承载新模型。

## Scope

本分支只包含 `kuzhambu-apps/admin-web/src/pages/knowledge/graph-*`、Admin 路由、菜单、契约测试和 `e2e/knowledge/graph-*`。Portal Web 的单稿件只读接入不在本分支；它在后端分支合入后另开 Portal 任务。

## Page Structure and UX

```text
知识图谱
├─ 工作台 (/knowledge/graph-workbench)
│  ├─ 指标卡：节点、关系、覆盖素材、孤立节点、核心关系缺失
│  ├─ 搜索与门类筛选
│  ├─ 渐进局部画布：种子淡化 → 分批边/节点 → 完成后移除孤立节点
│  └─ 右侧详情抽屉：来源、质量待办、跳转治理或素材
├─ 整体治理 (/knowledge/graph-governance)
│  ├─ 节点/关系表与局部画布联动
│  ├─ 来源与审计抽屉
│  └─ 创建、编辑、删除、合并、拆分的影响预览和二次确认
└─ 素材空间 (/knowledge/graph-materials)
   ├─ 素材库：状态、抽取任务、批量发布
   ├─ 单素材图谱 (/knowledge/graph-material?contentRefId=...)
   │  ├─ 左：素材正文摘要/证据；中：草稿画布；右：对象详情
   │  └─ 当前画布内发布预览：绿=创建、橙=关联、红=冲突、蓝=已发布
   ├─ 删除变更列表 (/knowledge/graph-deletion-changes)
   └─ 删除任务列表 (/knowledge/graph-deletion-tasks)
```

禁止：空画布加载全图；把发布预览放到新页面；已发布草稿仍允许编辑；只用权限隐藏高风险动作而没有影响预览/二次确认；用表格替代合并/拆分的映射分配界面。

## Mandatory File Plan

1. Mock 阶段新增页面域：`graph-workbench`、`graph-governance`、`graph-material`、`graph-deletion-change`、`graph-deletion-task`；每个域先只创建 `<domain>-page.tsx`、`<domain>-types.ts`、`<domain>-page.test.tsx` 和 `__mocks__/graph-mock-data.ts`。私有组件必须按 `ADMIN-WEB-RULES.md` 目录化。
2. Mock 页面只能从 `__mocks__/graph-mock-data.ts` 读取数据；本分支禁止新增 `<domain>-service.ts`、`postJson` 调用、真实 API contract test 或网络等待逻辑。
3. 在 `src/router/` 新增上述路由；在 `db/data-source/system.json` 更新菜单真相源，再运行 `node scripts/seed/generate-system-sql.mjs`。不得手改 `build/seed-sql/system.sql`。
4. 使用现有 `KuzhambuGraph` 作为画布基础；仅在多个新页面确实共享的业务规则下抽取组件。所有交互控件有可访问名称和稳定 `testId`。

## Exact Execution Steps

1. 建立 `src/pages/knowledge/graph-*/__mocks__/graph-mock-data.ts`。固定提供：DRAFT、PUBLISHING、PUBLISHED、WITHDRAWING、FAILED 五种素材状态（FAILED 含 `failureReason`）、含红色冲突的发布预览、两份素材的批量发布部分失败结果、孤立节点、删除失败任务。Mock 不调用 HTTP。
2. 完成所有页面的 Mock 冒烟测试即为本分支完成条件。每页测试至少断言标题、主操作、无权限状态、空状态和一个失败状态；工作台还必须断言“边批次返回前种子为淡化状态，结束后孤立节点移除”。
3. 完成单素材页：DRAFT 可 CRUD/抽取/导入；PUBLISHED 所有草稿写按钮禁用，仅显示撤回和发布结果；冲突未决时发布按钮禁用。
4. 素材库批量发布固定按所选顺序显示每份素材的预览与结果；一份失败不隐藏或覆盖其他素材的成功/失败结果。完成治理页：删除、合并、拆分一律先请求 preview，抽屉显示受影响节点、边、映射和 issue；用户确认后才 apply，并刷新详情和画布。
5. 完成删除列表：`PRESERVE_CONTRIBUTION` 和 `WITHDRAW_ASSOCIATIONS` 是两个明确按钮，显示不可逆影响；任务失败显示失败原因与重试。
6. Playwright 固定通过 Mock provider 运行；不得替换为真实 service，不得访问后端端口。真实接口接入和跨分支联调不在本 RUNBOOK 范围。

## Verification

每次改动先对触及页面运行 `pnpm --filter kuzhambu-admin-web run format`，再运行：

```sh
cd kuzhambu-apps
pnpm run format:check
pnpm run lint
pnpm --filter kuzhambu-admin-web run test
pnpm --filter kuzhambu-admin-web run build
```

Mock E2E 固定覆盖：抽取→草稿→冲突预览→发布→冻结→撤回；治理合并/拆分；删除预检→决策→失败重试；工作台 200 节点截断。Mock 通过即为本分支验收，不能声明真实接口联调通过。

## Commit Boundaries

每完成下列一项且该组件 Mock 单测通过，立即单独提交；禁止把两个编号的页面、组件或交互放进同一提交，也禁止夹带无关样式重构。

1. `Feat(services/knowledge): 新增图谱 Mock 数据与路由菜单`：唯一可共享的 mock provider、五个固定数据状态、路由和 `system.json` 菜单；不含任何业务页面。
2. `Feat(services/knowledge): 实现图谱工作台指标与筛选`：工作台指标卡、搜索、门类筛选及其页面测试；不含画布。
3. `Feat(services/knowledge): 实现图谱工作台渐进画布`：种子淡化、分批加载、孤立节点移除、详情抽屉及其测试。
4. `Feat(services/knowledge): 实现图谱素材库`：五种素材状态、抽取任务、空态/失败态及其测试；不含单素材页和批量发布结果。
5. `Feat(services/knowledge): 实现图谱素材批量发布结果`：按选择顺序的逐素材预览、确认、成功/失败结果与部分失败测试。
6. `Feat(services/knowledge): 实现图谱单素材草稿画布`：正文摘要、草稿 CRUD/抽取/导入、对象详情和 DRAFT/PUBLISHED 按钮状态测试。
7. `Feat(services/knowledge): 实现图谱素材发布预览`：同画布内四色预览、冲突禁用、发布/冻结/撤回状态及其测试。
8. `Feat(services/knowledge): 实现图谱治理节点关系浏览`：节点/关系表、局部画布、来源与审计抽屉及其测试；不含高风险动作。
9. `Feat(services/knowledge): 实现图谱治理影响确认`：创建、编辑、删除、合并、拆分的影响抽屉、二次确认、映射分配和测试。
10. `Feat(services/knowledge): 实现图谱删除变更列表`：两个决策按钮、不可逆影响展示、空态/失败态和测试。
11. `Feat(services/knowledge): 实现图谱删除任务列表`：任务详情、失败原因、重试交互和测试。
12. `Test(services/knowledge): 覆盖图谱 Mock 端到端流程`：只新增 Mock provider 下的 Playwright 用例，覆盖本手册列出的完整流程；不改业务组件。

## Closure

在 PR 描述记录 Mock E2E、单测、lint 与 build 命令及结果后删除本 RUNBOOK。保留 Mock 路由/provider 和页面单测；它们是本分支交付物，不得删除。
