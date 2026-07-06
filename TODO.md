# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `T02 Classics delete entrypoints`：三类内容删除入口接入分享同步
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DELETE-SHARE-RISK-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/wangqi/service/impl/WangqiDocumentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/mingcustoms/service/impl/MingCustomsApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sharing/ClassicsSharingApplicationServiceImplTest.java`
    - 处理动作：在 Sancai、Wangqi、Ming Customs 删除主记录前调用分享同步并补齐应用层测试
    - 验收点：三类内容删除均会触发 `CONTENT_DELETED` 同步，且同步失败会随删除事务回滚
    - 重要度：10/10

- [ ] `T03 Classics sharing contract`：锁定删除目标的接口输出与资源读取拦截
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DELETE-SHARE-RISK-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/response/ClassicsSharingResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/response/ClassicsSharePortalTargetResponse.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/ClassicsSharingAdminControllerTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/ClassicsSharingPortalControllerTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/sharing/ClassicsSharingPersistenceMappingTest.java`
    - 处理动作：确认 Admin/Portal 响应继续输出 `targetStatus`，并用测试锁定 `CONTENT_DELETED` 目标不可读取资源且不进入公开分享列表
    - 验收点：接口测试覆盖 `targetStatus=CONTENT_DELETED`、`titleSnapshot` 和 Portal 资源 404 行为
    - 重要度：9/10

- [ ] `T04 Admin Web sharing detail`：分享详情抽屉展示删除目标占位
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DELETE-SHARE-RISK-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.css`、`kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-types.ts`
    - 处理动作：在“关联内容”表格增加目标状态列，`CONTENT_DELETED` 显示“内容已删除”并隐藏失效操作
    - 验收点：详情抽屉中删除目标保留标题快照和状态标签，且不存在预览、下载或打开已删内容入口
    - 重要度：8/10

- [ ] `T05 Portal Web share detail`：读者侧分享详情隐藏已删除目标正文与资源
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DELETE-SHARE-RISK-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`、`kuzhambu-apps/portal-web/src/pages/share/share-form.test.tsx`、`kuzhambu-apps/portal-web/src/pages/share/share-page.tsx`、`kuzhambu-apps/portal-web/src/pages/share/share-page.test.tsx`、`kuzhambu-apps/portal-web/src/styles.css`
    - 处理动作：对 `CONTENT_DELETED` target 渲染删除占位，过滤公开列表中的删除目标，并补齐 Portal Web 测试
    - 验收点：删除目标只显示标题快照和“内容已删除”说明，不渲染正文、图片、文件元信息、预览或下载控件
    - 重要度：9/10

- [ ] `T06 Branch sync`：同步 main 分支最新代码
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DELETE-SHARE-RISK-CLOSURE.md`
    - 范围对象：`codex/classics-delete-share-risk-runbook` worktree、`main` 分支
    - 处理动作：实现完成后同步 `main` 最新代码并解决冲突
    - 验收点：当前分支包含 `main` 最新代码，且没有未解决冲突
    - 重要度：9/10

- [ ] `T07 Verification`：完成后端和前端验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DELETE-SHARE-RISK-CLOSURE.md`
    - 范围对象：`kuzhambu-servers`、`kuzhambu-apps`
    - 处理动作：同步 `main` 后运行 RUNBOOK 中 Maven 和 npm 格式、静态检查、测试命令
    - 验收点：后端 Maven 检查与测试通过，前端 npm format、lint、test 通过
    - 重要度：9/10

- [ ] `T08 Classics implementation coverage`：更新 Classics 实现覆盖口径
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-DELETE-SHARE-RISK-CLOSURE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：把 Classics 删除后分享闭环标记为已完成，并同步 Admin Web / Portal Web 删除占位口径
    - 验收点：Coverage 中不再保留“删除后分享目标状态同步、风险态重算未完成”的描述
    - 重要度：10/10

- [ ] `T09 Runbook and TODO closure`：清理临时 RUNBOOK 并收窄 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-DELETE-SHARE-RISK-CLOSURE.md`
    - 范围对象：`docs/30-designs/RUNBOOK-CLASSICS-DELETE-SHARE-RISK-CLOSURE.md`、`TODO.md`
    - 处理动作：删除临时 RUNBOOK，并按实际完成情况删除或收窄 TODO
    - 验收点：临时 RUNBOOK 已清理，`TODO.md` 不保留已完成任务
    - 重要度：10/10

## 待讨论项
