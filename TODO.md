# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `classics admin sharing assembler test`：固定 Admin 分享创建接口契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`ClassicsSharingInterfaceAssembler.java`、`ClassicsSharingAdminControllerTest.java`
    - 处理动作：补接口 assembler 和 controller contract test，固定请求体与响应字段
    - 验收点：测试覆盖 Admin 创建响应包含 `shareUrl` 且请求体不包含内容快照
    - 重要度：8/10

- [ ] `classics portal sharing interface`：调整 Portal 分享查询为明文 shareToken 入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`ClassicsSharingPortalController.java`、`ClassicsSharingPortalInterfaceAssembler.java`、`ClassicsSharePortalResponse.java`、`ClassicsSharePortalTargetResponse.java`
    - 处理动作：将 Portal 查询路径改为接收明文 shareToken，并返回 link 元信息和 target 快照 DTO
    - 验收点：Portal API 不暴露 `tokenHash`，response 不返回 domain entity 内部结构
    - 重要度：9/10

- [ ] `classics portal share list interface`：新增 Portal 分享列表查询接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`ClassicsSharingPortalController.java`、`ClassicsSharePortalSearchRequest.java`、`ClassicsSharePortalListResponse.java`、`ClassicsSharePortalListItemResponse.java`
    - 处理动作：新增公开分享列表查询，支持按分类、时间和标题搜索
    - 验收点：列表只返回可公开访问的分享摘要，不返回完整 `content_snapshot_json`
    - 重要度：8/10

- [ ] `classics portal sharing contract test`：固定 Portal 分享查询响应语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`ClassicsSharingPortalControllerTest.java`、`ClassicsSharingApplicationServiceImplTest.java`
    - 处理动作：补明文 shareToken 查询、分享列表查询、过期、撤销和不存在 shareToken 的测试
    - 验收点：Portal 查询测试覆盖详情和列表成功响应，过期、撤销和不存在 shareToken 均返回 404 且不区分失败原因
    - 重要度：8/10

- [ ] `classics share portal url config`：接入 Portal Web base URL 配置
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`ClassicsShareProperties.java`、`application.yml`、`.env.example`、`deploy/.env.example`
    - 处理动作：新增 `KUZHAMBU_PORTAL_WEB_BASE_URL` 配置并用于组装 Admin 创建结果 `shareUrl`
    - 验收点：配置缺失时本地默认值明确，Admin 创建成功返回 `{portalWebBaseUrl}/share/{shareToken}`
    - 重要度：8/10

- [ ] `admin-web classics share client`：新增 Admin Web 分享创建 client
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`share-service.ts`、`share-types.ts`、`share-service-contract.test.ts`
    - 处理动作：新增 Admin Web 分享创建请求类型、响应类型和 service contract test
    - 验收点：Admin Web client 只提交 target 引用并能读取 `shareUrl`
    - 重要度：7/10

- [ ] `admin-web sancai share entry`：在三才页面接入分享创建入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`sancai-page.tsx`、`sancai-types.ts`、`share-service.ts`
    - 处理动作：让三才页面创建分享时只传 `contentType/contentId` 并展示或复制 `shareUrl`
    - 验收点：页面不会构造 `titleSnapshot/contentSnapshotJson`
    - 重要度：7/10

- [ ] `portal-web home and share route`：新增 Portal 首页和分享路由
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`App.tsx`、`home-page.tsx`、`api/http.ts`、`share-service.ts`、`vite-env.d.ts`
    - 处理动作：新增 `/`、`/shares`、`/share/:shareToken` 路由和 Portal 分享 API client
    - 验收点：Portal 首页展示分享入口，Portal Web 使用明文 shareToken 调用详情 API
    - 重要度：8/10

- [ ] `portal-web classics share list`：实现 Portal 分享列表页
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`share-list-page.tsx`、`share-list-page.test.tsx`、`share-service.ts`、`share-types.ts`
    - 处理动作：实现分享列表，支持按分类、时间和标题查询
    - 验收点：`/shares` 能查询公开分享摘要，列表页不展示完整内容快照
    - 重要度：8/10

- [ ] `portal-web classics share page`：实现 Portal Web 分享快照展示页
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`share-page.tsx`、`share-page.test.tsx`、`styles.css`
    - 处理动作：展示 link 元信息、target 列表和固化快照详情
    - 验收点：`/share/:shareToken` 能展示快照且不调用 Admin API
    - 重要度：8/10

- [ ] `classics share validation`：运行分享链路测试和 dev.env 冒烟
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`kuzhambu-servers`、`kuzhambu-apps`、`dev.env`
    - 处理动作：执行后端、前端最小验证，并用 `dev.env` 完成 Admin 创建分享和 Portal 访问冒烟
    - 验收点：验证命令和冒烟结果可写入 PR 描述
    - 重要度：9/10

- [ ] `classics share docs readiness`：同步分享快照收口文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/20-interfaces/`、`docs/00-governance/SERVERS-ARCHITECTURE.md`、`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 处理动作：同步三类版本快照 schema、接口、配置、覆盖度状态，并在 PR 收口时删除或收窄 RUNBOOK
    - 验收点：`docs/20-interfaces/` 固定三类正式版本 `snapshot_json` 字段，文档口径与实现和验证结果一致
    - 重要度：7/10

- [ ] `classics share cleanup`：清理分享快照任务现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-CLASSICS-SHARE-SNAPSHOT.md`、本地临时调试产物、当前工作区
    - 处理动作：PR 收口前清理已完成 TODO、删除或收窄临时 RUNBOOK、移除临时调试产物并确认工作区不混入无关修改
    - 验收点：`git status` 只剩预期改动，临时 RUNBOOK 已按治理规则处理，TODO 不保留已完成项
    - 重要度：8/10

## 待讨论项
