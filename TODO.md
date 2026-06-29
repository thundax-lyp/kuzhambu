# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `admin-web/classics/share-common`：补齐通用分享服务与类型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-EXPORT-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-service-contract.test.ts`
    - 处理动作：补齐分享管理所需的前端 service、types 和契约测试
    - 验收点：前端可统一使用 `page / get / updateStatus / pageAccessRecords`，且分享 service contract 测试覆盖新请求契约
    - 重要度：9/10

- [ ] `admin-web/classics/wangqi`：补齐 Wangqi 单文档分享入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-EXPORT-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-document-list.tsx`
    - 处理动作：为 Wangqi 页面接入单文档分享入口并与现有分享反馈语义保持一致
    - 验收点：Wangqi 列表或详情可以创建分享链接并给出成功反馈，且页面单测覆盖该入口
    - 重要度：8/10

- [ ] `admin-web/classics/sharing`：落地分享管理页
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-EXPORT-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/components/`、`kuzhambu-apps/admin-web/src/pages/classics/sharing/`
    - 处理动作：新增分享管理页面或入口并完成分享列表、详情、状态变更和访问记录查看闭环
    - 验收点：后台可查看分享标题、状态、可见性、过期时间、访问次数、targets 和访问记录，并可执行状态变更
    - 重要度：10/10

- [ ] `admin-web/classics/ming-customs`：补齐 MingCustoms 导出任务治理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-EXPORT-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`
    - 处理动作：为 MingCustoms 页面接入导出任务创建与任务列表闭环
    - 验收点：MingCustoms 页面可提交导出任务并查看任务状态、过期状态和下载入口，且页面单测同步更新
    - 重要度：8/10

- [ ] `admin-web/classics/export-common`：收口导出任务复用语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-EXPORT-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/common/classics-export-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/classics-export-service-contract.test.ts`、`kuzhambu-apps/admin-web/src/pages/classics/common/components/`
    - 处理动作：把 Wangqi、MingCustoms、Sancai 的导出任务展示语义收敛到同一模式
    - 验收点：三类内容页面的导出任务状态、下载入口和刷新语义一致，且导出 service contract 与复用组件同步成立
    - 重要度：8/10

- [ ] `admin-web/classics/sancai`：收口 Sancai 导出与静态展示任务治理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-EXPORT-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
    - 处理动作：统一 Sancai 导出任务区和静态展示任务区的状态展示与刷新语义
    - 验收点：Sancai 页面可稳定查看导出任务和静态展示任务的状态与下载入口，且相关单测更新通过
    - 重要度：8/10

- [ ] `classics/facade`：校验 Facade 公共读口径是否齐备
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-EXPORT-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/ClassicsFacade.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsPublicContentFacadeDto.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/assembler/ClassicsFacadeAssembler.java`
    - 处理动作：检查本轮分享与导出治理是否影响 Facade 公共读口径，并仅在缺字段时做最小补充
    - 验收点：收口记录能明确说明 Facade 已校验且是否需要改动，若发生改动则对应测试同步补齐
    - 重要度：7/10

- [ ] `docs/classics`：同步覆盖状态并收口任务面板
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SHARE-EXPORT-CLOSURE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-CLASSICS-SHARE-EXPORT-CLOSURE.md`、`TODO.md`
    - 处理动作：在本轮闭环完成后同步更新 Classics 覆盖文档、删除临时 RUNBOOK 并删除已完成 TODO
    - 验收点：Coverage 文档准确反映分享管理和导出治理新状态，临时 RUNBOOK 已删除，`TODO.md` 只保留剩余未完成任务
    - 重要度：7/10

## 待审阅任务项

## 待讨论项
