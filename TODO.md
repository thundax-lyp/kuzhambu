# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `knowledge/graph`：暴露图谱版本与正式结果后台接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/**`
    - 处理动作：补齐图谱版本、正式实体、正式关系、正式世系读取接口
    - 验收点：后台已提供以图谱版本列表为主入口的正式结果读取接口
    - 重要度：8/10

- [ ] `admin-web/knowledge`：搭建正式结果读取页骨架和路由
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/**`、`kuzhambu-apps/admin-web/src/router/index.tsx`
    - 处理动作：新增正式结果读取页组骨架和路由入口
    - 验收点：Admin Web 已有独立于 taxonomy 的正式结果读取页入口
    - 重要度：7/10

- [ ] `admin-web/knowledge`：接通图谱版本列表和详情
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/**`
    - 处理动作：接通图谱版本读取 service contract、列表和详情交互
    - 验收点：管理员可从图谱版本列表进入版本详情
    - 重要度：7/10

- [ ] `admin-web/knowledge`：接通正式实体、关系、世系列表与详情
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/**`
    - 处理动作：基于版本入口接通正式实体、关系和世系列表与详情
    - 验收点：管理员可从版本详情下钻查看正式实体、关系和世系结果
    - 重要度：7/10

- [ ] `docs/knowledge`：同步覆盖状态并收口现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`docs/30-designs/*`、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`、`TODO.md`
    - 处理动作：在功能落地后同步设计与 coverage 文档，并清理 RUNBOOK 与 TODO
    - 验收点：文档只反映已落地结果，PR 前 RUNBOOK 删除且 TODO 收空或收窄
    - 重要度：6/10

## 待审阅任务项

## 待讨论项
