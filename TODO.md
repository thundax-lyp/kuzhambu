# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Classics 三才接口契约`：固定三才图会后台接口契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-ADMIN-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiCategoryResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiVolumeResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAdminControllerTest.java`
    - 处理动作：补齐门类和卷查询接口，并固定 entries 查询、详情、保存契约
    - 验收点：Classics interface 测试覆盖三才图会门类、卷、条目分页、详情和保存契约
    - 重要度：10/10

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

- [ ] `Admin Web 三才列表页`：实现三才图会列表筛选闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-ADMIN-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`、`kuzhambu-apps/admin-web/e2e/classics/sancai/sancai.spec.ts`
    - 处理动作：实现门类、卷、关键词、状态筛选、分页和条目列表展示
    - 验收点：Playwright 验证页面能加载门类、卷、条目列表并发送正确筛选请求体
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
