# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Portal Web 分享详情页`：补齐私有分享控件与操作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PRIVATE-SHARE-ACCESS.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`、`kuzhambu-apps/portal-web/src/pages/share/share-form.test.tsx`
    - 处理动作：详情页展示私有分享登录引导，并在鉴权成功后复用只读详情、资源预览和下载控件。
    - 验收点：页面测试覆盖 `aria-label="私有分享登录引导"`、已有 token 私有分享展示、私有资源预览 URL。
    - 重要度：9/10

- [ ] `Classics 私有分享文档收口`：同步覆盖度并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-PRIVATE-SHARE-ACCESS.md`
    - 范围对象：`docs/20-interfaces/CLASSICS-SHARE-PORTAL-INTERFACE.md`、`docs/30-designs/CLASSICS-DESIGN.md`、`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-CLASSICS-PRIVATE-SHARE-ACCESS.md`
    - 处理动作：同步接口、设计和 Implementation Coverage，并在 PR 收口前删除已无剩余价值的 RUNBOOK。
    - 验收点：Classics Implementation Coverage 不再列私有分享访问缺口，RUNBOOK 文件在闭环完成后被清理。
    - 重要度：10/10

## 待讨论项
