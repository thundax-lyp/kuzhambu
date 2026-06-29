# Classics Content Refinement Runbook

## Purpose

本文档用于指导 `Classics` 域“摘要 / 标签 / 问答对 / AI 候选确认”业务闭环的分步落地，固定当前阶段的边界、复用策略、数据结构变更、实施顺序和验收口径。

本文档是当前阶段执行手册，不替代：

- `docs/10-requirements/CLASSICS-REQUIREMENTS.md`
- `docs/30-designs/CLASSICS-DESIGN.md`
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`

## Scope

本轮覆盖：

- `Wangqi` 页面摘要、标签、问答对手工维护闭环。
- `MingCustoms` 页面摘要、标签、问答对手工维护闭环。
- `Sancai` 页面标签、问答对聚合编辑闭环补齐。
- `Wangqi / MingCustoms / Sancai` 页面 `AI summary / tags / qa` 候选确认与拒绝闭环。
- `Classics Content` 通用前端服务、组件、刷新语义与契约测试补齐。
- `Classics Facade` 受影响面检查，确认公共读模型不因本轮闭环收口而失配。

本轮不覆盖：

- 新的 AI 触发入口、Prompt 编排、Worker 执行策略。
- 批量公开/私有、批量分享、批量视觉资产处理。
- Discovery 单文档问答、Knowledge 治理规则扩展。
- `summary` 通用写接口抽象。

## Current State

当前已具备的基础：

- `summary` 已进入三类主内容原有保存链路：
  - `SancaiEntry`
  - `WangqiDocument`
  - `MingCustoms`
- 后端已具备通用 `tags / qa / ai candidate apply` 能力：
  - `GET /api/classics/content/tags`
  - `POST /api/classics/content/tags/add`
  - `POST /api/classics/content/tags/update`
  - `POST /api/classics/content/tags/sort`
  - `GET /api/classics/content/qa-pairs`
  - `POST /api/classics/content/qa-pairs/add`
  - `POST /api/classics/content/qa-pairs/update`
  - `POST /api/classics/content/qa-pairs/sort`
  - `POST /api/classics/content/ai-candidates/change`
- `AiCandidatePanel` 和 `ai-candidate-service` 已经接到页面：
  - `Wangqi` 支持 `summary / tags / qa`
  - `MingCustoms` 支持 `summary / tags / qa`
  - `Sancai` 支持 `translate / summary / tags / qa`
- `WangqiPage`、`MingCustomsPage`、`SancaiEntryPanel` 已有 `afterForm` 或详情扩展区可承载通用维护组件。

当前主要缺口：

- 缺少通用 `Classics Content` 前端 `tags / qa` service 与 types。
- `Wangqi`、`MingCustoms` 缺少手工标签和问答对维护 UI。
- `Sancai` 仍未把标签、问答对与现有摘要、版本、AI 候选组合成单一编辑闭环。
- `AI candidate apply / reject` 后与手工维护区之间缺少统一刷新语义和测试约束。

## Target Outcome

完成后，管理员应能在三类 Classics 内容页面内直接完成：

- 编辑并保存 `summary`。
- 手工新增、编辑、删除、排序标签。
- 手工新增、编辑、删除、排序问答对。
- 查看待处理 AI 候选，并对 `summary / tags / qa` 执行确认或拒绝。
- 在手工保存或 AI 候选确认后，当前页面内立即看到刷新后的结果。

其中：

- `summary` 继续复用各内容原有保存接口，不新增通用 `summary` controller。
- `tags / qa / ai candidate apply` 统一复用 `ClassicsContentAdminController`。
- 本轮验收必须同时覆盖“手工维护路径”和“AI 候选确认路径”。

## Key Decisions

### D1. Summary 不抽通用保存接口

原因：

- 三类内容主表和页面模型已经各自承载 `summary`。
- 强行抽通用 `summary` 写接口会打断现有页面保存语义。

本轮策略：

- `summary` 仍由 `Wangqi / MingCustoms / Sancai` 原有保存接口负责。
- RUNBOOK 只要求页面级摘要编辑体验自然，且与 AI 候选应用后的刷新一致。

### D2. Tags / QA 统一走通用 Content Controller

原因：

- 后端 `ClassicsContentAdminController` 已具备 `tags / qa` 的 list/add/update/sort 能力。
- 统一复用可避免按内容类型复制 controller/service。

本轮策略：

- 前端新增通用 `classics-content-service.ts`。
- 页面统一传入：
  - `contentType`
  - `contentId`
  - 刷新回调

### D3. 先 Wangqi 和 MingCustoms，后 Sancai

原因：

- `Wangqi` 和 `MingCustoms` 页面结构更简单，适合作为通用组件第一批接入点。
- `Sancai` 页面状态更多，适合在通用模式稳定后收口。

### D4. AI 候选确认并入本轮主闭环

原因：

- 页面上已有 `AiCandidatePanel`，如果不并入，本轮交付仍然不是完整编辑闭环。
- `summary / tags / qa` 的 AI 候选确认本质上是相同内容面的另一条写路径，应与手工维护共享刷新和结果展示语义。

本轮策略：

- 保持现有 AI 候选列表、载荷编辑、确认、拒绝接口不变。
- 重点补齐“确认后刷新 summary / tags / qa 展示”和“页面测试覆盖”。
- 不新增 AI 触发按钮，不扩展 worker 行为。

## Data Structure Changes

### Backend Data Structures

本轮预期：

- **不新增数据库表**
- **不修改现有 DO / Mapper 结构**
- **不新增 `summary` 通用 DTO**
- **默认不新增 `facade` 协议**

沿用的数据结构：

- `ClassicsContentTag`
- `ClassicsContentQaPair`
- `ClassicsContentResponse`
- `AiCandidateApplyRecord`

仅在以下情况允许补充接口层结构：

- 现有 `tags / qa` 请求或响应字段不足以支撑前端排序、删除、状态刷新。
- 现有 `ai-candidates/change` 响应不足以让前端准确刷新页面局部状态。
- 现有 `facade` DTO 无法反映手工维护或 AI 候选确认后的既有公共展示字段。

如发生补充，应限制在 2-5 个文件的小范围修改内，不扩大到 schema 变更。

### Frontend Data Structures

本轮预期新增：

- `classics-content-types.ts`
  - 统一定义 `contentType`
  - `tag / qa` 列表项结构
  - `add / update / sort` 命令结构
- 通用组件入参结构
  - `contentType`
  - `contentId`
  - `onChanged`
  - `readOnly` 或同类 UI 控制项

本轮预期复用：

- `ai-candidate-types.ts`
- 各页面既有表单值类型：
  - `wangqi-types.ts`
  - `ming-customs-types.ts`
  - `sancai-types.ts`

## Related Files

### Backend

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/ClassicsContentApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`

### Facade

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/ClassicsFacade.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsPublicContentFacadeDto.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsPublicContentFacadeResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/test/java/com/thundax/kuzhambu/classics/facade/ClassicsFacadeArchitectureTest.java`

### Frontend Common

- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service-contract.test.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-payload-editor.tsx`

### Frontend Wangqi

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-document-model.tsx`

### Frontend MingCustoms

- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-model.tsx`

### Frontend Sancai

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`

### Documentation

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`

## Frontend Page Refactor Detail

### Wangqi Page

当前结构：

- `WangqiDocumentModel` 通过 `afterForm` 承载扩展区。
- 扩展区当前已有：
  - `AiCandidatePanel`
  - `WangqiStorageFilePanel`
  - `WangqiVersionHistoryPanel`

本轮改造要求：

- 在 `afterForm` 中把扩展区重组为统一治理面板区。
- 顺序建议：
  1. `AiCandidatePanel`
  2. `ClassicsContentTagPanel`
  3. `ClassicsContentQaPanel`
  4. `WangqiStorageFilePanel`
  5. `WangqiVersionHistoryPanel`
- `summary` 仍保留在 `WangqiDocumentModel` 主表单区域，不下沉到扩展区。

联动要求：

- `summary` 保存成功后，列表摘要和详情摘要同步刷新。
- `AiCandidatePanel` 应用 `summary / tags / qa` 后，要刷新：
  - 当前文档详情
  - 标签面板
  - 问答对面板
  - 候选列表
- 标签或问答对保存成功后，不触发版本历史或附件面板的额外重载。

测试重点：

- 编辑抽屉中同时出现 AI、标签、问答对、附件、版本五块区域。
- AI 应用与手工编辑不会互相覆盖 UI 状态。

### MingCustoms Page

当前结构：

- `MingCustomsModel` 通过 `afterForm` 承载扩展区。
- 扩展区当前只有 `AiCandidatePanel`。

本轮改造要求：

- 在 `afterForm` 中补齐内容治理区。
- 顺序建议：
  1. `AiCandidatePanel`
  2. `ClassicsContentTagPanel`
  3. `ClassicsContentQaPanel`
- `summary` 保持在 `MingCustomsModel` 主表单区域。

联动要求：

- `summary` 保存成功后，列表和详情展示同步刷新。
- AI 应用 `summary / tags / qa` 后，刷新：
  - 当前条目详情
  - 标签面板
  - 问答对面板
- 不破坏现有关键词云与筛选结果展示。

测试重点：

- `afterForm` 从“只有 AI 区”演进为“AI + tags + qa”三块并列。
- 列表页返回后摘要和标签相关展示保持一致。

### Sancai Page

当前结构：

- `SancaiEntryModel` 通过 `afterForm` 承载扩展区。
- 扩展区当前已有：
  - `AiCandidatePanel`
  - `SancaiVersionHistoryPanel`

本轮改造要求：

- 在 `SancaiEntryModel` 的 `afterForm` 中补齐聚合治理区。
- 顺序建议：
  1. `AiCandidatePanel`
  2. `ClassicsContentTagPanel`
  3. `ClassicsContentQaPanel`
  4. `SancaiVersionHistoryPanel`
- `summary`、原文、译文继续保留在主表单区，不迁移到扩展区。

联动要求：

- `summary` 保存成功后，条目列表摘要和详情同步刷新。
- AI 应用 `translate / summary / tags / qa` 后，刷新：
  - 当前条目详情
  - 条目列表
  - 标签面板
  - 问答对面板
  - 候选列表
- 版本恢复后重新打开的详情，标签和问答对要重新拉取，避免使用旧局部状态。

测试重点：

- 聚合区与主表单区并存时不破坏原有版本、分享、导出、静态展示入口。
- `translate` 候选应用后，`summary / tags / qa` 面板状态仍然稳定。

### Common Frontend Refactor Rules

- 页面不直接拼装 `fetch` 或 `postJson`，统一走通用 `classics-content-service.ts`。
- 页面不自己维护 `tags / qa` 原始请求结构，只消费 `classics-content-types.ts`。
- `AiCandidatePanel` 只负责候选提交与候选列表刷新；页面负责决定应用成功后要联动刷新的内容块。
- 每个页面都需要有一个明确的 `onContentChanged` 或同类刷新入口，避免：
  - 标签刷新靠局部 state
  - 摘要刷新靠 detail refetch
  - AI 刷新靠单独 invalidate
  导致三套语义并存。

## Full Flow

### Flow A. 手工摘要维护

1. 管理员进入 `Wangqi / MingCustoms / Sancai` 编辑上下文。
2. 页面通过原有表单模型展示 `summary`。
3. 管理员修改 `summary` 并保存。
4. 页面通过原有内容保存接口提交。
5. 保存成功后刷新当前内容详情与列表摘要展示。

### Flow B. 手工标签维护

1. 页面加载标签面板。
2. 标签面板通过通用 `classics-content-service` 拉取标签列表。
3. 管理员新增、编辑、删除、排序标签。
4. 面板调用通用后端接口保存。
5. 保存成功后刷新：
   - 标签面板自身
   - 页面详情中的标签相关展示
   - 必要时 AI 候选区的上下文依赖

### Flow C. 手工问答对维护

1. 页面加载问答对面板。
2. 问答对面板通过通用 `classics-content-service` 拉取问答对列表。
3. 管理员新增、编辑、删除、排序问答对。
4. 面板调用通用后端接口保存。
5. 保存成功后刷新当前内容详情。

### Flow D. AI 候选确认

1. 页面加载 `AiCandidatePanel` 待处理候选。
2. 管理员查看 `summary / tags / qa` 候选内容。
3. 管理员执行确认或拒绝。
4. `AiCandidatePanel` 调用既有候选接口完成状态变更。
5. 确认成功后刷新：
   - 候选列表
   - 当前页面 `summary`
   - 标签面板
   - 问答对面板

### Flow E. Sancai 聚合收口

1. `SancaiEntryPanel` 同时承载：
   - 原文/译文
   - `summary`
   - 标签
   - 问答对
   - 版本历史
   - AI 候选
2. 任一写路径完成后，页面局部状态必须保持一致，不要求整页重载。

### Flow F. Facade 一致性校验

1. 手工维护或 AI 候选确认完成后，检查相关内容落库字段是否仍能被既有 facade 读模型正确映射。
2. 至少确认公共内容所依赖字段未失配：
   - `summary`
   - `tagNames`
   - `updatedAt`
3. 如 facade 仅复用现有持久化读路径且字段无缺口，本轮不改 facade。
4. 仅在发现公共读模型字段缺失或映射失真时，才启用 facade 补强任务。

## Implementation Plan

### Step 1. 收敛通用前端内容维护类型与服务

范围对象：

- `kuzhambu-apps/admin-web/src/pages/classics/common/`

处理动作：

- 新增统一 `classics-content-types.ts`
- 新增统一 `classics-content-service.ts`
- 锁定契约测试

验收点：

- `tags / qa` 的 list/add/update/delete/sort 调用统一落在一个 service 中。
- URL、请求体、响应结构被契约测试锁定。

### Step 2. 提供通用标签维护组件

范围对象：

- `kuzhambu-apps/admin-web/src/pages/classics/common/components/`

处理动作：

- 新增标签面板
- 支持列表展示、新增、编辑、删除、排序、刷新

验收点：

- 组件只依赖 `contentType + contentId + onChanged`
- 支持空态、保存中、错误提示

### Step 3. 提供通用问答对维护组件

范围对象：

- `kuzhambu-apps/admin-web/src/pages/classics/common/components/`

处理动作：

- 新增问答对面板
- 支持列表展示、新增、编辑、删除、排序、刷新

验收点：

- 组件不耦合具体内容类型
- 支持多项编辑与顺序维护

### Step 4. 收敛 AI 候选确认后的统一刷新语义

范围对象：

- `kuzhambu-apps/admin-web/src/pages/classics/common/`
- `kuzhambu-apps/admin-web/src/pages/classics/**`

处理动作：

- 让 `AiCandidatePanel` 在确认成功后通知页面刷新 `summary / tags / qa`
- 锁定候选确认后的页面联动测试

验收点：

- `summary / tags / qa` 的手工区与 AI 区不出现“候选已应用但页面仍旧值”的情况

### Step 5. 接入 Wangqi 页面

范围对象：

- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/`

处理动作：

- 在 `Wangqi` 编辑上下文并列接入：
  - `summary`
  - 标签面板
  - 问答对面板
  - AI 候选区
- 保留 `WangqiStorageFilePanel` 与 `WangqiVersionHistoryPanel`
- 调整 `afterForm` 扩展区布局与统一刷新回调

验收点：

- 管理员无需离开页面即可完成四类操作
- `summary` 保存与 AI 候选确认均能反映到当前展示
- 附件和版本面板在引入 tags / qa 后仍可独立工作

### Step 6. 接入 MingCustoms 页面

范围对象：

- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/`

处理动作：

- 在 `MingCustoms` 编辑上下文并列接入：
  - `summary`
  - 标签面板
  - 问答对面板
  - AI 候选区
- 调整 `afterForm` 从单块 AI 扩展为多块内容治理区

验收点：

- 不破坏现有关键词云、详情展示和保存链路
- 进入编辑态后，AI / tags / qa 三块可同时操作

### Step 7. 收口 Sancai 页面聚合编辑闭环

范围对象：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/`

处理动作：

- 在 `SancaiEntryPanel` 中补齐标签和问答对入口
- 让其与 `summary`、版本历史、AI 候选共存
- 明确 `refreshSancaiEntryDetail + invalidateEntries` 之外的面板刷新职责

验收点：

- `Sancai` 成为完整内容治理工作台，而不是仅保留单点编辑能力
- 翻译候选、摘要候选、标签编辑、问答编辑可在同一抽屉内连续完成

### Step 8. 补测试与 Coverage

范围对象：

- `kuzhambu-apps/admin-web/src/pages/classics/**`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/**`
- 必要时 `kuzhambu-servers/biz/classics/**`
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`

处理动作：

- 补服务契约测试、页面交互测试
- 校验 facade DTO 是否已具备承载本轮收口后的公共读字段
- 若后端接口补强则同步补 Java 测试
- 更新 Coverage 文档

验收点：

- 至少覆盖 `Wangqi / MingCustoms / Sancai` 的关键路径
- 明确记录 facade 是“无需修改”还是“已补强”
- Coverage 文档能准确反映“手工维护 + AI 候选确认”新状态

## Small Task Split

为避免单次改动过大，按 2-5 个文件一组推进：

### Group A1. 通用类型

- `.../classics/common/classics-content-types.ts`
- `.../classics/common/classics-content-service-contract.test.ts`

### Group A2. 通用服务

- `.../classics/common/classics-content-service.ts`
- `.../classics/common/classics-content-service-contract.test.ts`

### Group B1. 标签面板实现

- `.../classics/common/components/classics-content-tag-panel.tsx`
- `.../classics/common/components/classics-content-tag-panel.css`

### Group B2. 标签面板测试

- `.../classics/common/components/classics-content-tag-panel.test.tsx`
- `.../classics/common/components/classics-content-tag-panel.tsx`

### Group C1. 问答对面板实现

- `.../classics/common/components/classics-content-qa-panel.tsx`
- `.../classics/common/components/classics-content-qa-panel.css`

### Group C2. 问答对面板测试

- `.../classics/common/components/classics-content-qa-panel.test.tsx`
- `.../classics/common/components/classics-content-qa-panel.tsx`

### Group D1. AI 候选刷新语义

- `.../classics/common/components/ai-candidate-panel.tsx`
- `.../classics/common/ai-candidate-service-contract.test.ts`

### Group D2. AI 候选联动测试

- `.../classics/wangqi/wangqi-page.test.tsx`
- `.../classics/ming-customs/ming-customs-page.test.tsx`
- `.../classics/sancai/components/sancai-entry-panel.test.tsx`

### Group E1. Wangqi 接入

- `.../classics/wangqi/wangqi-page.tsx`
- `.../classics/wangqi/components/wangqi-document-model.tsx`
- 必要时 `.../classics/wangqi/wangqi-page.css`

### Group E2. Wangqi 测试

- `.../classics/wangqi/wangqi-page.test.tsx`
- 必要时 `.../classics/wangqi/wangqi-service-contract.test.ts`

### Group F1. MingCustoms 接入

- `.../classics/ming-customs/ming-customs-page.tsx`
- `.../classics/ming-customs/components/ming-customs-model.tsx`
- 必要时 `.../classics/ming-customs/ming-customs-page.css`

### Group F2. MingCustoms 测试

- `.../classics/ming-customs/ming-customs-page.test.tsx`
- 必要时 `.../classics/ming-customs/ming-customs-service-contract.test.ts`

### Group G1. Sancai 接入

- `.../classics/sancai/components/sancai-entry-panel.tsx`
- `.../classics/sancai/components/sancai-entry-model.tsx`
- 必要时 `.../classics/sancai/sancai-page.css`

### Group G2. Sancai 测试

- `.../classics/sancai/components/sancai-entry-panel.test.tsx`
- 必要时 `.../classics/sancai/sancai-service-contract.test.ts`

### Group H. 后端补强

- `.../ClassicsContentAdminController.java`
- `.../ClassicsContentApplicationServiceImpl.java`
- `.../ClassicsContentAdminControllerTest.java`
- `.../ClassicsContentApplicationServiceAiCandidateTest.java`

仅在发现前端所需字段或行为缺失时启用，不预设必改。

### Group I. Facade 校验或补强

- `.../classics/facade/ClassicsFacade.java`
- `.../classics/facade/dto/ClassicsPublicContentFacadeDto.java`
- 必要时对应 facade 测试文件

仅在发现公共读模型字段缺失或映射失真时启用，不预设必改。

### Group J. Coverage 更新

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`

## Verification Checklist

- `summary` 在 `Wangqi / MingCustoms / Sancai` 原有保存链路中保持可编辑。
- 标签与问答对可在三个页面内完成手工维护。
- `AI summary / tags / qa` 候选可确认、可拒绝，且确认后页面展示同步刷新。
- `Sancai` 页面不会因引入新面板破坏已有摘要、版本、正文或 AI 区。
- facade 公共读模型已确认具备，或已按最小范围完成补强。
- 前端契约测试和页面测试覆盖关键路径。
- 若后端接口有补强，Java 侧测试同步通过。

## Closure Rules

完成后需要同步：

- 更新 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- 按实施结果生成 `TODO.md`
- 本 RUNBOOK 在任务完成后删除
