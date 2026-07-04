# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `classics-sancai-image-backend-state`：补齐三才图片删除、当前图切换和排序归属语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/SancaiAssetApplicationService.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/persistence/mapper/SancaiAssetMapper.java`
    - 处理动作：实现 `useImage`、删除当前图自动补位、上传当前图自动清空旧当前图、按 owner 精确解绑 Storage 和按 `entryId` 限定排序。
    - 验收点：同一条目最多一张图片 `currentUsed=true`，删除当前图自动补位，上传当前图清空旧当前图，排序只接受同一 `entryId` 的图片。
    - 重要度：10/10

- [ ] `classics-sancai-image-admin-api`：暴露三才图片删除和当前图切换 admin API
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryImageSortRequest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
    - 处理动作：新增 `images/delete`、`images/current/change`，并让 `images/sort` 请求携带和校验 `entryId`。
    - 验收点：删除、当前图切换和排序接口路径、权限、请求体和响应字段稳定可测。
    - 重要度：10/10

- [ ] `classics-sancai-image-snapshot-payload`：将三才内容快照图片改为多图列表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/SancaiEntryVersionSnapshot.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentSnapshotAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsContentSnapshotAssemblerTest.java`
    - 处理动作：移除 snapshot `images` 只保留当前图的过滤逻辑，保留全部图片并按 `priority ASC` 输出。
    - 验收点：snapshot `images` 同时包含 `currentUsed=true` 和 `currentUsed=false` 图片，字段列表不变且顺序稳定。
    - 重要度：10/10

- [ ] `classics-sancai-image-showcase-payload`：补齐三才静态展示多图 payload
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiShowcaseCommand.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`
    - 处理动作：让 showcase render payload 输出 `entries[].images[]` 多图列表及 `src/alt/caption/currentUsed/priority` 字段。
    - 验收点：worker 接收的 showcase payload 不依赖回调 Java API 即可渲染多图和当前图。
    - 重要度：9/10

- [ ] `classics-sancai-image-export-payload`：补齐 Classics 导出多图 payload
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/result/ClassicsExportJobResult.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`
    - 处理动作：让 Sancai 导出数据源输出 `items[].images[]` 多图元数据。
    - 验收点：JSON/HTML 导出数据保留多图元数据，CSV 行数仍等于内容条目数。
    - 重要度：8/10

- [ ] `classics-sancai-image-backend-tests`：补齐三才图片后端回归测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAssetAdminControllerTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/test/java/com/thundax/kuzhambu/classics/infra/sancai/SancaiRepositoryTest.java`
    - 处理动作：覆盖当前图切换、删除补位、Storage 解绑、接口映射和按条目排序。
    - 验收点：新增测试能锁定 delete/current/sort/snapshot/payload 的关键行为。
    - 重要度：10/10

- [ ] `classics-sancai-image-admin-service`：补齐 admin-web 三才图片 service 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
    - 处理动作：新增 `deleteImage`、`changeCurrentImage`、`sortImages` service 契约并锁定 URL、method、body。
    - 验收点：页面只通过同域 service 调用图片删除、当前图切换和排序 API。
    - 重要度：9/10

- [ ] `classics-sancai-image-admin-controls`：实现 admin-web 配图管理控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`
    - 处理动作：新增 `配图管理` 区块、图片卡片、`预览图片`、`下载图片`、`设为当前使用图片`、`删除图片` 和空状态。
    - 验收点：测试能通过可访问名称定位配图管理区和四类图片操作控件。
    - 重要度：10/10

- [ ] `classics-sancai-image-admin-upload-sort`：实现 admin-web 配图上传和排序控件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`
    - 处理动作：新增 `上传配图`、`图片标题`、`图片类型`、`上传后设为当前使用`、`上移图片`、`下移图片` 控件。
    - 验收点：上传调用带 `currentUsed`，排序调用带 `entryId` 和完整 `orderedIds`。
    - 重要度：9/10

- [ ] `classics-sancai-image-admin-preview`：实现 admin-web 多图放大浏览
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-image-preview.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.css`
    - 处理动作：新增 `配图预览` 抽屉，支持大图、上一张、下一张和下载当前图片。
    - 验收点：点击 `预览图片` 打开抽屉，多图可切换，单图禁用切换按钮。
    - 重要度：8/10

- [ ] `classics-sancai-image-portal-display`：同步 portal 分享三才多图展示
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/share/share-page.tsx`、`kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`、`kuzhambu-apps/portal-web/src/pages/share/share-types.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-form.test.tsx`
    - 处理动作：三才分享详情按 `currentUsed` 选择主图，按 `priority ASC` 展示缩略图并支持点击切换主图。
    - 验收点：Portal 不生成或暴露 admin 图片 API，主图和下载链接均来自分享资源。
    - 重要度：8/10

- [ ] `classics-sancai-image-portal-contract`：补齐 portal 分享图片 service 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/share/share-service.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-service.test.ts`、`kuzhambu-apps/portal-web/src/pages/share/share-types.ts`
    - 处理动作：锁定 `images[].storageObject.previewUrl/downloadUrl` 和多图字段解析。
    - 验收点：portal service 不丢弃 `currentUsed=false` 图片且不拼接 admin URL。
    - 重要度：7/10

- [ ] `classics-sancai-image-worker-showcase`：同步 worker 三才静态展示多图渲染
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/render/sancai_showcase.py`、`kuzhambu-workers/src/kuzhambu_workers/render/templates/sancai_showcase.html`、`kuzhambu-workers/tests/test_sancai_showcase.py`
    - 处理动作：按 `priority ASC` 渲染多图、当前图标记和缺图占位。
    - 验收点：HTML 多图顺序稳定，当前图有 `data-current="true"`，缺图不阻断正文。
    - 重要度：8/10

- [ ] `classics-sancai-image-worker-export`：同步 worker Classics 导出图片元数据输出
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/render/classics_export.py`、`kuzhambu-workers/src/kuzhambu_workers/render/templates/classics_export.html`、`kuzhambu-workers/tests/test_classics_export.py`
    - 处理动作：JSON 保留 `items[].images[]`，HTML 渲染图片元数据，CSV 不展开多图为多行。
    - 验收点：导出不丢失图片元数据且 CSV 行数仍等于内容条目数。
    - 重要度：7/10

- [ ] `classics-sancai-image-interface-doc`：更新三才内容版本快照接口文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`docs/20-interfaces/CLASSICS-CONTENT-VERSION-SNAPSHOT-INTERFACE.md`
    - 处理动作：将 `SANCAI_ENTRY.images` 明确为按 `priority ASC` 输出的多图列表，并说明 `currentUsed` 标识当前图。
    - 验收点：接口文档与 Java snapshot 输出字段一致，且不承诺新增数据库字段。
    - 重要度：9/10

- [ ] `classics-sancai-image-coverage-runbook-closeout`：更新覆盖状态并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`、`TODO.md`
    - 处理动作：将三才多图、缩略预览、放大浏览、原图删除和图片列表管理标记为已完成，并在 PR 收口前删除已完成 RUNBOOK 和清空对应 TODO。
    - 验收点：Implementation Coverage 只记录完成事实和剩余缺口，RUNBOOK 已删除，TODO.md 不保留已完成任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
