# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `SancaiEntryPanel`：补齐详情抽屉标签展示和编辑入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-ENTRY-TAGS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
    - 处理动作：在“条目上下文”展示 Ant Design `Tag` 标签行，并新增 `EditOutlined` 的“编辑标签”按钮定位到同抽屉标签治理面板。
    - 验收点：有标签展示 `tagNameSnapshot`，无标签展示 `未标注标签`，点击 `aria-label="编辑三才图会条目标签"` 的按钮会滚动并 focus 标签治理面板，标签变更后刷新详情和列表 query。
    - 重要度：9/10

- [ ] `SancaiEntryTags`：运行本轮最小验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-ENTRY-TAGS.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application`、`kuzhambu-apps/admin-web`
    - 处理动作：运行 RUNBOOK 中列出的后端 Maven 格式化/测试、Admin Web 格式化、定向 Vitest 和 lint。
    - 验收点：后端相关测试通过，`classics-content-service-contract.test.ts` 和 `sancai-entry-panel.test.tsx` 通过，Admin Web lint 通过。
    - 重要度：8/10

- [ ] `feat/sancai-entry-tags`：同步 main 最新代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-ENTRY-TAGS.md`
    - 范围对象：`main`、`feat/sancai-entry-tags`
    - 处理动作：收口前同步 `main` 最新代码到 `feat/sancai-entry-tags`，处理可能出现的冲突。
    - 验收点：当前分支包含最新 `main`，同步后重新运行本轮最小验证并通过。
    - 重要度：9/10

- [ ] `ClassicsImplementationCoverage`：同步三才标签闭环覆盖状态
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-ENTRY-TAGS.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：更新三才条目详情标签聚合展示、编辑入口、删除闭环和入参治理对应的覆盖描述。
    - 验收点：覆盖矩阵中三才“编辑标题、门类、卷、原文、译文和标签”和“展示原文、译文、标签、配图和状态”的完成状态与代码实现一致。
    - 重要度：8/10

- [ ] `RUNBOOK-SANCAI-ENTRY-TAGS`：清理临时 RUNBOOK 和已完成 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-ENTRY-TAGS.md`
    - 范围对象：`docs/30-designs/RUNBOOK-SANCAI-ENTRY-TAGS.md`、`TODO.md`
    - 处理动作：在功能、验证、main 同步和 Implementation Coverage 均完成后，删除临时 RUNBOOK 并随完成 commit 删除或收窄对应 TODO。
    - 验收点：临时 RUNBOOK 不再保留，`TODO.md` 不记录已完成任务。
    - 重要度：9/10

## 待审阅任务项

## 待讨论项
