# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `admin-web/classics/common`：补齐通用内容类型定义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-CONTENT-REFINEMENT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service-contract.test.ts`
    - 处理动作：新增 `Classics Content` 通用 `tags / qa` 类型定义并先锁定契约边界
    - 验收点：前端存在稳定的 `contentType`、`tag / qa` 项和命令结构定义，契约测试能表达对应请求响应形状
    - 重要度：8/10

- [ ] `admin-web/classics/common`：补齐通用内容服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-CONTENT-REFINEMENT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service-contract.test.ts`
    - 处理动作：新增统一 `tags / qa` 查询与写入服务并复用通用契约测试
    - 验收点：页面不再各自拼装 `tags / qa` 请求，URL、请求体和响应结构由契约测试锁定
    - 重要度：9/10

- [ ] `admin-web/classics/common/components`：补齐标签维护面板
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-CONTENT-REFINEMENT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-content-tag-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-content-tag-panel.css`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-content-tag-panel.test.tsx`
    - 处理动作：实现可复用的标签列表、增改删、排序与刷新面板
    - 验收点：标签面板只依赖 `contentType + contentId + onChanged`，并具备空态、保存中、错误提示和测试覆盖
    - 重要度：9/10

- [ ] `admin-web/classics/common/components`：补齐问答对维护面板
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-CONTENT-REFINEMENT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-content-qa-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-content-qa-panel.css`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-content-qa-panel.test.tsx`
    - 处理动作：实现可复用的问答对列表、增改删、排序与刷新面板
    - 验收点：问答对面板不耦合具体内容类型，支持多项编辑和顺序维护，并具备测试覆盖
    - 重要度：9/10

- [ ] `admin-web/classics/common`：收敛 AI 候选确认后的统一刷新语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-CONTENT-REFINEMENT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/components/ai-candidate-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/common/ai-candidate-service-contract.test.ts`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
    - 处理动作：让 AI 候选确认后统一触发页面级 `summary / tags / qa` 刷新，并补齐联动测试
    - 验收点：不会出现“候选已应用但页面仍显示旧摘要、旧标签或旧问答”的状态错位
    - 重要度：10/10

- [ ] `admin-web/classics/wangqi`：接入 Wangqi 内容治理面板
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-CONTENT-REFINEMENT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-document-model.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.css`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`
    - 处理动作：在 `Wangqi` 编辑抽屉中接入 `AI + tags + qa` 治理区并保留附件与版本面板
    - 验收点：管理员可在同一抽屉内完成摘要、标签、问答、AI 候选、附件和版本相关操作且互不冲突
    - 重要度：9/10

- [ ] `admin-web/classics/ming-customs`：接入 MingCustoms 内容治理面板
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-CONTENT-REFINEMENT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-model.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.css`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`
    - 处理动作：把 `MingCustoms` 的 `afterForm` 从单块 AI 扩展为 `AI + tags + qa` 内容治理区
    - 验收点：进入编辑态后可连续完成摘要、标签、问答和 AI 候选操作，且不破坏关键词云和列表展示
    - 重要度：9/10

- [ ] `admin-web/classics/sancai`：收口 Sancai 聚合内容治理面板
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-CONTENT-REFINEMENT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
    - 处理动作：在 `Sancai` 条目抽屉中补齐标签和问答对入口，并与摘要、翻译、版本和 AI 候选形成统一闭环
    - 验收点：`Sancai` 成为完整内容治理工作台，翻译候选、摘要候选、标签维护和问答维护可在同一抽屉连续完成
    - 重要度：10/10

- [ ] `classics/facade`：校验公共读模型是否具备
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-CONTENT-REFINEMENT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/ClassicsFacade.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsPublicContentFacadeDto.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/response/ClassicsPublicContentFacadeResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/test/java/com/thundax/kuzhambu/classics/facade/ClassicsFacadeArchitectureTest.java`
    - 处理动作：检查 facade 公共读模型是否已能稳定承载 `summary / tagNames / updatedAt` 等本轮闭环依赖字段
    - 验收点：明确记录 facade 为“无需修改”或“已完成最小补强”，且不引入无关协议扩张
    - 重要度：7/10

- [ ] `docs/classics`：同步实现覆盖状态
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-CONTENT-REFINEMENT.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：在闭环实现完成后同步更新 `Classics` 实现覆盖文档
    - 验收点：Coverage 文档准确反映 `summary / tags / qa / AI 候选确认` 的交付状态
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
