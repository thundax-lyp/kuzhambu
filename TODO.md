# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `09-storage-multipart-repository-contract`：强类型化分片仓储契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-servers/biz/storage/kuzhambu-storage-domain/src/main/java/com/thundax/kuzhambu/storage/domain/object/repository/MultipartUploadRepository.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-infra/src/main/java/com/thundax/kuzhambu/storage/infra/object/repository/impl/MultipartUploadRepositoryImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-application/src/main/java/com/thundax/kuzhambu/storage/application/service/impl/MultipartUploadApplicationServiceImpl.java`
    - 处理动作：将 `getMultipartPart(MultipartUploadId, Integer)` 改为接收 `MultipartPartNumber`。
    - 验收点：multipart HTTP `partNumber` 仍为 number，进入 repository 前转换为强类型。
    - 重要度：9/10

- [ ] `10-storage-admin-web-contract-regression`：回归 Storage 管理页前端协议和控件操作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-types.ts`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-service.ts`、`kuzhambu-apps/admin-web/src/pages/storage/storage-object/storage-object-page.tsx`、`kuzhambu-apps/admin-web/e2e/storage/storage-object/storage-object.spec.ts`
    - 处理动作：确认 Storage 管理页 request/response 字段保持基础类型，并回归搜索、筛选、上传、取消、预览、下载、删除、批量删除、拖拽排序和分页。
    - 验收点：前端 `id`、`contentType`、`size`、`referenceOwnerType`、`partNumber` 等协议字段保持现有类型，E2E 覆盖的控件操作通过。
    - 重要度：8/10

- [ ] `11-storage-strong-typing-closure`：执行全量验证并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`
    - 范围对象：`docs/30-designs/RUNBOOK-STORAGE-DOMAIN-STRONG-TYPING.md`、`TODO.md`、`kuzhambu-servers/biz/storage`、`kuzhambu-apps/admin-web`
    - 处理动作：运行 RUNBOOK 要求的后端和必要前端验证，完成后删除临时 RUNBOOK 并收窄或删除已完成 TODO。
    - 验收点：后端 storage 测试和必要 admin-web 契约测试通过，RUNBOOK 文件已删除或长期结论已迁移到治理/readiness 文档。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
