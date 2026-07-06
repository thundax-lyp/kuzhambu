# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `classics_share_link`：补齐私有分享创建者数据结构
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PRIVATE-SHARE-ACCESS.md`
    - 范围对象：`db/schema/classics.sql`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/model/entity/ClassicsShareLink.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/dataobject/ClassicsShareLinkDO.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/assembler/ClassicsSharingPersistenceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/sharing/ClassicsSharingPersistenceMappingTest.java`
    - 处理动作：为分享链接新增 `created_by_user_id` 字段、创建者索引和持久化双向映射。
    - 验收点：schema、领域实体、DO、assembler 和持久化映射测试均能读写 `createdByUserId`。
    - 重要度：10/10

- [ ] `Admin 分享创建`：写入当前创建者用户 ID
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PRIVATE-SHARE-ACCESS.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/command/ShareLinkCreateCommand.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/ClassicsSharingAdminController.java`
    - 处理动作：单条分享和批量分享创建时从 `KuzhambuContextHolder.currentSubjectId()` 绑定当前用户 ID。
    - 验收点：创建命令的 `operatorUserId` 最终落到分享链接 `createdByUserId`，无法解析数字用户 ID 时不误写创建者。
    - 重要度：10/10

- [ ] `ClassicsSharingApplicationService`：实现私有分享访问判定
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PRIVATE-SHARE-ACCESS.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/ClassicsSharingApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sharing/ClassicsSharingApplicationServiceImplTest.java`
    - 处理动作：新增私有分享详情和私有资源读取服务，并按创建者或 `classics:sharing:view` 权限放行。
    - 验收点：未登录、创建者、管理员权限、无权限用户、过期或撤销链接的应用服务测试覆盖并通过。
    - 重要度：10/10

- [ ] `Portal 私有分享接口`：补齐私有详情与资源读取入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PRIVATE-SHARE-ACCESS.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/ClassicsSharingPortalController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/ClassicsSharingPrivatePortalController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/assembler/ClassicsSharingPortalInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/response/ClassicsSharePortalResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/ClassicsSharingPortalControllerTest.java`
    - 处理动作：公开接口遇到私有分享返回 `loginRequired=true`，并新增 `/api/portal/classics/private-shares` 私有接口分支。
    - 验收点：接口测试覆盖公开登录引导、私有详情响应、私有资源路径和 `loginRequired` 字段。
    - 重要度：10/10

- [ ] `Portal Web 分享服务`：实现私有分享读取回退
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-PRIVATE-SHARE-ACCESS.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/api/http.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-service.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-types.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-service.test.ts`
    - 处理动作：分享服务先读公开接口，收到 `loginRequired=true` 且本地有 token 时改读私有接口。
    - 验收点：服务测试覆盖无 token 登录引导、有 token 私有详情回退、私有资源 URL 携带 `token` 查询参数。
    - 重要度：9/10

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
