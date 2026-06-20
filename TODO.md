# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `WangqiDocumentAdminControllerTest`：补强 source-file Controller 冒烟契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/wangqi/WangqiDocumentAdminControllerTest.java`
    - 处理动作：在 source-file API 实现后补齐对应路径、权限注解和 multipart/资源 GET 契约测试。
    - 验收点：Controller 测试固定 source-file POST/GET 路径。
    - 重要度：9/10

- [ ] `wangqi source api`：封装王圻原始文件 Admin API
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 范围对象：`WangqiDocumentAdminController.java`、`WangqiDocumentSourceFileResponse.java`、`WangqiDocumentInterfaceAssembler.java`
    - 处理动作：新增 source-file upload/get/content 端点并区分 JSON 协议响应和资源流响应。
    - 验收点：`POST {id}/source-file/get` 走通用协议，`GET {id}/source-file/content` 走资源 401/404。
    - 重要度：9/10

- [ ] `wangqi frontend service`：新增前端 service 和类型契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 范围对象：`wangqi-types.ts`、`wangqi-service.ts`、`wangqi-service-contract.test.ts`
    - 处理动作：定义 Wangqi record/query/command/source-file/version 类型并固定所有 API path。
    - 验收点：service contract 测试覆盖 POST 协议路径、source-file 资源路径和 version 路径。
    - 重要度：9/10

- [ ] `wangqi list form timeline`：实现王圻列表、表单和时间线组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 范围对象：`wangqi-document-form-values.ts`、`wangqi-document-list.tsx`、`wangqi-document-model.tsx`、`wangqi-timeline-panel.tsx`
    - 处理动作：实现列表筛选、抽屉表单、正文预览和时间线交互。
    - 验收点：页面可按 keyword/visibility/sortDirection 查询并打开详情抽屉。
    - 重要度：8/10

- [ ] `wangqi file version panels`：实现王圻原始文件和版本组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 范围对象：`wangqi-storage-file-panel.tsx`、`wangqi-version-history-panel.tsx`
    - 处理动作：实现原始文件上传/元数据/下载和版本历史/对比/恢复面板。
    - 验收点：恢复成功后刷新详情、列表、时间线、版本列表和文件元数据。
    - 重要度：9/10

- [ ] `wangqi route page`：注册王圻页面和路由
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 范围对象：`wangqi-page.tsx`、`wangqi-page.css`、`kuzhambu-apps/admin-web/src/router/index.tsx`
    - 处理动作：组装 Wangqi 页面 query/mutation/drawer 并注册 `/classics/wangqi` route。
    - 验收点：Admin Web 可从路由进入王圻页面且风格延续古风口径。
    - 重要度：8/10

- [ ] `wangqi frontend tests`：补页面单测和 E2E
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 范围对象：`wangqi-page.test.tsx`、`kuzhambu-apps/admin-web/e2e/classics/wangqi/wangqi.spec.ts`
    - 处理动作：覆盖列表、筛选、详情、保存、删除、文件、版本对比和恢复流程。
    - 验收点：`npm run test` 和 Wangqi E2E 覆盖核心闭环。
    - 重要度：9/10

- [ ] `dev.env smoke`：执行 Wangqi dev.env 冒烟
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 范围对象：`dev.env`、`kuzhambu-servers/starter/kuzhambu-admin-starter`、Wangqi Admin API
    - 处理动作：加载 dev.env 启动 admin starter 并按 RUNBOOK 冒烟 Wangqi API。
    - 验收点：page/timeline/detail/source-file/version/restore API 均满足 RUNBOOK 验收。
    - 重要度：10/10

- [ ] `wangqi closeout`：文档和现场收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`TODO.md`、`docs/30-designs/RUNBOOK-CLASSICS-WANGQI-ADMIN-WEB.md`
    - 处理动作：更新覆盖状态、删除完成 TODO、删除 RUNBOOK 并清理本地服务和临时产物。
    - 验收点：`git status --short` 最终为空且无残留 admin/front dev server。
    - 重要度：10/10

## 待讨论项
