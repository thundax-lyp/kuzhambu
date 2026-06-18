# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Admin Web 三才服务契约`：补齐三才图会前端服务契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-ADMIN-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
    - 处理动作：新增三才图会前端类型、接口方法和请求字段契约测试
    - 验收点：前端 service contract test 固定 categories、volumes、entries/page、entries/{id}、entries/save 路径和请求体
    - 重要度：9/10

- [ ] `Admin Web Classics 路由`：接入三才图会后台页面路由
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-ADMIN-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/router/index.tsx`、`kuzhambu-apps/admin-web/src/app.test.tsx`、`kuzhambu-apps/admin-web/e2e/layout/admin-layout.spec.ts`
    - 处理动作：新增 `/classics/sancai` 路由并把导航测试纳入三才图会菜单
    - 验收点：单测和 Playwright layout 用例能进入三才图会页面且页面非空
    - 重要度：8/10

- [ ] `Admin Web 三才页面骨架`：实现三才图会页面基础结构
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-ADMIN-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
    - 处理动作：实现左侧目录筛选区、顶部检索工具条、主列表区和详情区的页面骨架
    - 验收点：页面在加载中、错误、门类为空、卷为空、筛选无结果状态下不空白且不遮挡
    - 重要度：10/10

- [ ] `Admin Web 三才筛选联动`：实现门类、卷和筛选请求闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-ADMIN-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service.ts`、`kuzhambu-apps/admin-web/e2e/classics/sancai/sancai.spec.ts`
    - 处理动作：实现门类切换、卷切换、关键词搜索、状态筛选和第一页重置
    - 验收点：Playwright 验证 categories、volumes、entries/page 请求体与后端契约一致
    - 重要度：10/10

- [ ] `Admin Web 三才条目表格`：实现条目列表和分页展示
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-ADMIN-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`、`kuzhambu-apps/admin-web/e2e/classics/sancai/sancai.spec.ts`
    - 处理动作：展示条目标题、卷上下文、状态、摘要预览、分页和查看编辑入口
    - 验收点：Playwright 验证列表列、分页切换、pageSize 切换和空结果展示
    - 重要度：10/10

- [ ] `Admin Web 三才编辑页`：实现三才图会条目详情与保存闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-ADMIN-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`、`kuzhambu-apps/admin-web/e2e/classics/sancai/sancai.spec.ts`
    - 处理动作：实现条目详情打开、标题原文译文摘要公开状态编辑和保存刷新
    - 验收点：Playwright 验证打开详情、编辑保存、列表刷新，请求体与 `SancaiEntrySaveRequest` 对齐
    - 重要度：10/10

- [ ] `Classics Coverage 收口`：更新 Classics 覆盖报告并清理临时任务文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-ADMIN-LOOP.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`TODO.md`、`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-ADMIN-LOOP.md`
    - 处理动作：同步三才图会 Admin 最小闭环完成状态并删除已完成 TODO 和临时 RUNBOOK
    - 验收点：Coverage 文档准确保留 AI、Worker、导出、分享、复杂视觉资产、标签治理和批量能力缺口
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
