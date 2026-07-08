# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Classics Wangqi/Ming version snapshot`：扩展版本快照与历史恢复
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-QA-VERSION-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/WangqiDocumentVersionSnapshot.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/MingCustomsVersionSnapshot.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentSnapshotAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/support/WangqiDocumentVersionRestorer.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`
    - 处理动作：把 `tags` 和 `qaPairs` 纳入 Wangqi/Ming `snapshot_json` 并在历史恢复时还原。
    - 验收点：历史恢复可恢复主内容、标签快照和已确认问答对，并生成新的 `HISTORY_RESTORED` 版本。
    - 重要度：10/10

- [ ] `Classics AI apply and Discovery sync`：收口 AI 应用与 Discovery 只读同步
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-QA-VERSION-GOVERNANCE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsAiCandidatePayloadParser.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsQaKnowledgeFacadeDto.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/assembler/ClassicsFacadeAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeRevisionCalculator.java`
    - 处理动作：让一次 AI 候选确认应用只生成一个 `AI_APPLIED` 版本，并让 Discovery revision 只消费当前已确认内容。
    - 验收点：AI 应用摘要、标签和问答对时只生成一个版本，候选拒绝不生成版本，私有 Wangqi/Ming 不 upsert 到 QA Knowledge Base。
    - 重要度：9/10

- [ ] `Admin Web QA controls`：补齐 Wangqi/Ming 问答对控件操作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-QA-VERSION-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-content-qa-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
    - 处理动作：补齐问答对新增、编辑、删除、AI 应用和 AI 拒绝后的控件反馈与刷新行为。
    - 验收点：`新增问答对`、`编辑`、`删除问答对 {id}`、`应用`、`拒绝` 操作符合 RUNBOOK 文案和刷新规则。
    - 重要度：9/10

- [ ] `Admin Web version snapshot display`：展示版本快照中的标签与问答对
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-QA-VERSION-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-version-history-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-version-history-panel.tsx`
    - 处理动作：让 Wangqi/Ming 版本历史面板展示 `tags` 和 `qaPairs` 快照摘要。
    - 验收点：选中版本后可看到 `确认标签` 和 `确认问答`，原有查看、恢复按钮可访问名称不变。
    - 重要度：8/10

- [ ] `Classics QA backend validation`：补齐后端与 Discovery 验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-QA-VERSION-GOVERNANCE.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/wangqi/WangqiDocumentApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/mingcustoms/MingCustomsApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/support/KnowledgeRevisionCalculatorTest.java`
    - 处理动作：补齐 QA 版本、AI 应用、历史恢复和 Discovery revision 的后端测试。
    - 验收点：后端测试覆盖 QA 新增/编辑/删除、AI 单版本应用、候选拒绝、历史恢复和 revision 变化规则。
    - 重要度：9/10

- [ ] `Classics QA frontend validation and coverage`：补齐前端验证并更新 Implementation Coverage
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-QA-VERSION-GOVERNANCE.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/classics-content-service-contract.test.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/classics-content-qa-panel.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`、`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：补齐前端控件测试、服务契约测试，并更新 Classics Implementation Coverage。
    - 验收点：前端测试覆盖删除接口、问答对控件、AI 应用/拒绝刷新和版本快照展示，`CLASSICS-IMPLEMENTATION-COVERAGE.md` 标注本闭环覆盖状态。
    - 重要度：9/10

- [ ] `Classics QA governance PR closure`：同步 main 并完成 TODO/RUNBOOK 收口
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/00-governance/PR-RULES.md`
    - 范围对象：`main` 分支、`TODO.md`、`docs/30-designs/RUNBOOK-CLASSICS-QA-VERSION-GOVERNANCE.md`、`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：在实现验证通过后同步 `main` 最新代码，清理已完成 TODO，删除临时 RUNBOOK，并确认 Implementation Coverage 已更新。
    - 验收点：当前分支包含最新 `main`，已完成任务从 `TODO.md` 删除，RUNBOOK 已清理，Coverage 保留最终覆盖口径。
    - 重要度：10/10

## 待讨论项
