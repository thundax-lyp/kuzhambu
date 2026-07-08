# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

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
