# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `classics/portal/resource-read`：接入 Portal 分享资源读取应用能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/ClassicsSharingApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sharing/ClassicsSharingApplicationServiceImplTest.java`
    - 处理动作：按分享 token 和快照资源 ID 校验后读取 Storage 内容并记录成功访问。
    - 验收点：测试覆盖不在快照内、过期撤销、跨内容类型误读、非 Wangqi 下载和失败统一 404。
    - 重要度：10/10
- [ ] `classics/portal/resource-api`：接入 Portal 分享资源读取接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/controller/ClassicsSharingPortalController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/portal/sharing/ClassicsSharingPortalControllerTest.java`
    - 处理动作：暴露 `/resources/{storageObjectId}/content` 并设置 inline/download 响应头。
    - 验收点：接口测试覆盖成功读取、失败统一 404 和非 Wangqi 下载限制。
    - 重要度：10/10
- [ ] `admin-web/storage/preview`：接入 Admin Storage 页面预览和下载
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-types.ts`
    - 处理动作：增加 Storage 内容 URL helper，并用 `toAuthenticatedResourceUrl` 驱动预览和下载动作。
    - 验收点：Storage 页面预览和下载按钮分别使用业务 URL、token 拼接和 `download` 参数。
    - 重要度：8/10
- [ ] `admin-web/wangqi/source-preview`：接入 Admin Wangqi 原始文件预览下载
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/wangqi/components/wangqi-storage-file-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-service-contract.test.ts`
    - 处理动作：让 Wangqi 文件面板通过业务域接口和鉴权 URL 完成预览下载。
    - 验收点：Wangqi 不再直连 Storage 通用读取，服务测试覆盖 `download` URL 参数。
    - 重要度：8/10
- [ ] `admin-web/sancai/image-upload-preview`：接入 Admin Sancai 图片上传预览
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
    - 处理动作：让 Sancai 页面通过业务上传接口和鉴权资源 URL 完成上传、预览和下载。
    - 验收点：Sancai 上传不使用 Storage 通用上传入口，图片预览和下载都带 token。
    - 重要度：9/10
- [ ] `portal-web/share/resource-types`：补齐 Portal 分享资源类型和服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/share/share-types.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-service.ts`
    - 处理动作：定义 Portal 分享资源类型和分享资源 URL helper。
    - 验收点：Portal Web 能表达 `target.storageObject` 和 `target.images[].storageObject`，资源 URL 指向分享读取接口。
    - 重要度：8/10
- [ ] `portal-web/share/resource-preview`：接入 Portal 分享页资源预览
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`、`kuzhambu-apps/portal-web/src/pages/share/share-types.ts`
    - 处理动作：渲染 Portal 已装配的 Wangqi 原始文件和 Sancai 图片资源。
    - 验收点：Portal 分享页不以裸 JSON 作为主要内容，只有 Wangqi 原始文件显示下载按钮。
    - 重要度：9/10
- [ ] `readiness/storage-preview`：完成 Storage 预览闭环验证记录
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`
    - 范围对象：`docs/40-readiness/PR-WORKFLOW.md`、`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`、`TODO.md`
    - 处理动作：运行 RUNBOOK 要求的后端、前端和人工冒烟验证并更新 readiness 覆盖记录。
    - 验收点：PR 收口材料记录 Maven、npm 和人工冒烟结果，验证失败项已修复或明确剩余风险。
    - 重要度：8/10
- [ ] `cleanup/storage-preview-runbook`：清理 Storage 预览闭环现场任务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-STORAGE-PREVIEW-CLOSURE.md`、`docs/40-readiness/PR-WORKFLOW.md`
    - 处理动作：在功能、验证和文档同步完成后删除临时 RUNBOOK 并清空或收窄已完成 TODO。
    - 验收点：PR 合并前没有已完成任务残留，临时 RUNBOOK 已删除，工作区只保留交付相关改动。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
