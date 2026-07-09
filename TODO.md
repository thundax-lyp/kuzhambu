# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `admin-web mingcustoms tag cloud controls`：实现明代习俗标签云控件和列表筛选联动
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-TAIL-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/components/ming-customs-keyword-cloud.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
    - 处理动作：将明代习俗主入口改为 `标签云`，点击标签写入 `tagId/tagNameSnapshot` 筛选并提供 `清除标签筛选` 控件。
    - 验收点：标签云 Drawer、标签 item、Badge、当前标签提示和清除按钮均按 RUNBOOK 文案与操作生效。
    - 重要度：10/10

- [ ] `admin-web mingcustoms tag cloud tests`：补齐明代习俗标签云前端契约和页面测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-TAIL-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-service-contract.test.ts`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`
    - 处理动作：覆盖 `listTagCloud` 查询参数、打开标签云、点击标签筛选和清除标签筛选。
    - 验收点：页面请求体包含或清除 `tagId/tagNameSnapshot`，且不再通过旧 `tagName` 承载新标签云筛选。
    - 重要度：9/10

- [ ] `admin-web sancai current-page selection`：明确三才图会当前页多选交互
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-TAIL-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-list.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`
    - 处理动作：把三才图会批量动作和选中数量提示统一为当前页已选口径。
    - 验收点：页面出现 `当前页已选 N 条`，批量动作不暗示跨页勾选，分页或筛选变化不依赖旧页选中项。
    - 重要度：8/10

- [ ] `branch sync main`：同步 main 分支最新代码并处理冲突
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/PR-RULES.md`
    - 范围对象：`feat/classics-tail-closure` 分支、`origin/main`
    - 处理动作：在功能实现和聚焦测试补齐后同步 `origin/main` 到当前分支并处理任务相关冲突。
    - 验收点：当前分支包含最新 `origin/main`，同步后工作树只保留本任务相关改动。
    - 重要度：9/10

- [ ] `classics backend final validation`：运行 Classics 后端最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-TAIL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-domain`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface`
    - 处理动作：同步 `origin/main` 后执行 Classics 后端 Maven formatter、Spotless、Checkstyle 和受影响模块测试。
    - 验收点：后端格式化、静态检查和测试均通过，失败项已修复或明确记录阻塞原因。
    - 重要度：10/10

- [ ] `admin-web classics final validation`：运行 Classics 管理台最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-TAIL-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs`、`kuzhambu-apps/admin-web/src/pages/classics/sancai`
    - 处理动作：同步 `origin/main` 后执行 admin-web format、lint、相关页面测试和 build。
    - 验收点：admin-web 格式检查、lint、聚焦测试和 build 均通过，失败项已修复或明确记录阻塞原因。
    - 重要度：10/10

- [ ] `classics requirements coverage closure`：更新 Classics 需求和 Implementation Coverage
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-TAIL-CLOSURE.md`
    - 范围对象：`docs/10-requirements/CLASSICS-REQUIREMENTS.md`、`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：把三才多选降级、王圻和明代习俗 AI 标签/问答入口、明代习俗统一标签云记录为最终完成口径。
    - 验收点：coverage 不再保留上述尾项的 `部分完成` 或 `未完成` 状态，并记录本轮验证结果。
    - 重要度：10/10

- [ ] `classics runbook cleanup`：清理 Classics 尾项收口 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-CLASSICS-TAIL-CLOSURE.md`、`TODO.md`
    - 处理动作：在 PR 收口前删除已完成且无剩余价值的 RUNBOOK，并按完成情况清理或收窄 TODO。
    - 验收点：RUNBOOK 文件已删除，TODO.md 不保留已完成任务，剩余任务仅表达真实未完成内容。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
