# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `classics/mingcustoms keyword-cloud domain`：增加明代习俗关键词云领域契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`MingCustomsRepository.java`、`MingCustomsKeywordCloudItem.java`、`MingCustomsApplicationService.java`、`MingCustomsApplicationServiceImpl.java`、`MingCustomsRepositoryImpl.java`
    - 处理动作：将 keyword-cloud 从关键词列表改为按 keyword 聚合并返回 count。
    - 验收点：infra 测试覆盖聚合数量、排序和 visibility 过滤。
    - 重要度：10/10

- [ ] `classics/mingcustoms keyword-cloud interface`：输出明代习俗关键词云接口契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`MingCustomsKeywordCloudItemResponse.java`、`MingCustomsInterfaceAssembler.java`、`MingCustomsAdminController.java`、`MingCustomsAdminControllerTest.java`、`MingCustomsRepositoryTest.java`
    - 处理动作：让 Admin API keyword-cloud 返回 `{ keyword, count }[]`。
    - 验收点：Controller contract test 固定 `keyword` 和 `count` 字段。
    - 重要度：10/10

- [ ] `classics/mingcustoms init-data`：增加明代习俗初始化数据
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`db/data/system.sql`、`db/data/classics.sql`、`db/data-source/system.json`、`scripts/generate-system-data-sql.ts`
    - 处理动作：增加分类字典、明代习俗样例数据，并扩展 system seed 生成脚本支持 `dicts`。
    - 验收点：`node scripts/generate-system-data-sql.ts --check` 通过，dev.env 可查询到字典和样例数据。
    - 重要度：9/10

- [ ] `admin-web ming-customs service`：增加明代习俗前端服务契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`ming-customs-types.ts`、`ming-customs-service.ts`、`ming-customs-service-contract.test.ts`
    - 处理动作：封装 page/get/add/update/delete/keyword-cloud/category-options 服务。
    - 验收点：service contract test 固定 URL、method、request body 和 response 映射。
    - 重要度：9/10

- [ ] `admin-web rich-content viewer`：增加富文本展示控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-rich-content-viewer.tsx`、`kuzhambu-rich-content-viewer.css`、`kuzhambu-rich-content-viewer.test.tsx`、`package.json`、`package-lock.json`
    - 处理动作：用 `marked` 和 `dompurify` 集中处理 Markdown/HTML/TEXT 展示。
    - 验收点：控件测试覆盖 Markdown、HTML、TEXT 和危险输入清理。
    - 重要度：9/10

- [ ] `admin-web ming-customs route`：注册明代习俗页面入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`router/index.tsx`、`ming-customs-page.tsx`、`ming-customs-page.css`、`ming-customs-page.test.tsx`
    - 处理动作：注册 `/classics/ming-customs` 页面骨架和首次加载。
    - 验收点：页面测试确认标题可见并触发 `POST /classics/ming-customs/page`。
    - 重要度：8/10

- [ ] `admin-web ming-customs list`：增加明代习俗列表和关键词云
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`ming-customs-list.tsx`、`ming-customs-keyword-cloud.tsx`、`ming-customs-page.tsx`、`ming-customs-page.css`
    - 处理动作：实现列表、分类/可见性筛选、排序方向和关键词云点击筛选。
    - 验收点：页面测试确认分类筛选、关键词云 count 强弱和点击搜索。
    - 重要度：8/10

- [ ] `admin-web ming-customs editor`：增加明代习俗详情编辑
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`ming-customs-model.tsx`、`ming-customs-form-values.ts`、`ming-customs-page.tsx`、`ming-customs-page.css`
    - 处理动作：实现详情抽屉、新增、编辑、字典分类和富文本正文展示。
    - 验收点：页面测试确认 add/update 请求、textarea 编辑和正文安全渲染。
    - 重要度：9/10

- [ ] `admin-web ming-customs share-delete`：支持明代习俗删除和分享
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`ming-customs-list.tsx`、`ming-customs-page.tsx`、`ming-customs-page.test.tsx`
    - 处理动作：接入删除确认和 `shareService.create`。
    - 验收点：页面测试确认 delete 请求和分享 target `{ contentType: "MING_CUSTOMS", contentId }`。
    - 重要度：8/10

- [ ] `admin-web ming-customs e2e`：固定明代习俗后台冒烟
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-apps/admin-web/e2e/classics/ming-customs/ming-customs.spec.ts`
    - 处理动作：新增明代习俗页面、搜索、编辑、富文本和分享 E2E。
    - 验收点：`npm run e2e -- e2e/classics/ming-customs/ming-customs.spec.ts` 通过。
    - 重要度：8/10

- [ ] `classics/mingcustoms verification`：执行明代习俗闭环验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`kuzhambu-servers`、`kuzhambu-apps`、`dev.env`
    - 处理动作：运行 RUNBOOK 验证计划并完成 dev.env 冒烟。
    - 验收点：后端 Maven、前端 format/lint/test/build、数据生成校验和 dev.env 冒烟均有结果。
    - 重要度：10/10

- [ ] `classics/mingcustoms cleanup`：清理明代习俗闭环调试现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`dev.env`、本地运行进程、本地日志、`git status`
    - 处理动作：清理冒烟测试数据、停止本地服务、移除临时日志并确认工作区只保留本轮应提交文件。
    - 验收点：dev.env 无临时测试脏数据，本地无遗留调试进程，`git status` 无非预期改动。
    - 重要度：9/10

- [ ] `classics/mingcustoms docs-cleanup`：更新覆盖状态并清理手册
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-CLASSICS-MINGCUSTOMS-ADMIN-WEB.md`、`TODO.md`
    - 处理动作：更新明代习俗覆盖状态，收口时删除 RUNBOOK 并清空已完成 TODO。
    - 验收点：PR 前无过期 RUNBOOK，TODO 只保留未完成任务。
    - 重要度：9/10

## 待讨论项
