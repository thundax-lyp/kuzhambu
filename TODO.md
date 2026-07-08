# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Portal share 服务契约`：补齐多目标和私有分享读取类型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARING-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/share/share-service.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-types.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-service.test.ts`
    - 处理动作：补齐 Portal 分享服务对公开详情、私有详情、多 target 响应和资源 URL 的契约。
    - 验收点：公开详情、私有详情和资源读取 URL 均保持正确 endpoint，多 target 响应类型能被页面消费。
    - 重要度：9/10

- [ ] `Portal share 页面`：实现多目标只读内容卡片和删除占位
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARING-GOVERNANCE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`、`kuzhambu-apps/portal-web/src/pages/share/share-form.test.tsx`
    - 处理动作：按 targets 渲染多个内容卡片并保持私有登录引导、删除占位和资源按钮语义一致。
    - 验收点：一个链接可展示多个内容卡片，已删除目标只显示占位，私有分享未登录引导登录且登录后展示内容。
    - 重要度：9/10

- [ ] `classics sharing 接口文档`：同步 Portal 分享访问统计契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARING-GOVERNANCE.md`
    - 范围对象：`docs/20-interfaces/CLASSICS-SHARE-PORTAL-INTERFACE.md`、`docs/30-designs/RUNBOOK-CLASSICS-SHARING-GOVERNANCE.md`
    - 处理动作：补充 Portal 分享详情浏览成功会进入访问统计的接口契约。
    - 验收点：Portal 接口文档与详情浏览、私有详情浏览、资源读取三类成功访问统计语义一致。
    - 重要度：8/10

- [ ] `classics sharing 交付状态`：更新 Classics Implementation Coverage
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARING-GOVERNANCE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-CLASSICS-SHARING-GOVERNANCE.md`
    - 处理动作：将单链接多个内容、分享恢复策略和访问统计更新为已完成状态。
    - 验收点：Implementation Coverage 不再保留本轮三项的未完成或部分完成描述。
    - 重要度：10/10

- [ ] `classics sharing 最终收口`：同步 main 分支代码并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-SHARING-GOVERNANCE.md`
    - 范围对象：`origin/main`、`docs/30-designs/RUNBOOK-CLASSICS-SHARING-GOVERNANCE.md`、`TODO.md`
    - 处理动作：最终验证前同步最新 `main`，解决冲突后删除 RUNBOOK 并按完成情况清理 TODO。
    - 验收点：分支包含最新 `main`，RUNBOOK 已删除，已完成 TODO 项已清理。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
