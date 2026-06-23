# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `knowledge/taxonomy`：补齐标签合并影响预览与合并动作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/*taxonomy*`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/*`
    - 处理动作：按 RUNBOOK 先落地标签合并影响预览，再闭合标签合并动作和后台入口
    - 验收点：管理员可预览合并影响并完成标签合并，历史引用仍可追溯
    - 重要度：8/10

- [ ] `knowledge/taxonomy`：补齐标签废弃与治理统计读能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/*taxonomy*`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/*`
    - 处理动作：补齐标签废弃动作和基础治理统计读模型及后台展示入口
    - 验收点：管理员可废弃标签并查看基础治理统计，废弃标签退出新的可用集合
    - 重要度：8/10

- [ ] `knowledge/graph`：定义正式结果可读化读契约与读仓储
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/*graph*`
    - 处理动作：为图谱版本、正式实体、正式关系和正式世系结果补齐读取契约与读仓储
    - 验收点：后端可稳定返回版本列表、详情和正式事实读取结果
    - 重要度：8/10

- [ ] `knowledge/graph`：补齐正式结果后台接口与 Admin Web 可读页面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/*graph*`、`kuzhambu-apps/admin-web/src/pages/knowledge/*`
    - 处理动作：基于读契约补齐正式结果后台接口、路由和列表详情页面
    - 验收点：管理员可查看图谱版本和正式实体、关系、世系结果详情
    - 重要度：8/10

- [ ] `docs/knowledge`：同步覆盖状态并收口现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`docs/30-designs/*`、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`、`TODO.md`
    - 处理动作：在功能落地后同步设计与 coverage 文档，并清理 RUNBOOK 与 TODO
    - 验收点：文档只反映已落地结果，PR 前 RUNBOOK 删除且 TODO 收空或收窄
    - 重要度：6/10

## 待审阅任务项

## 待讨论项
