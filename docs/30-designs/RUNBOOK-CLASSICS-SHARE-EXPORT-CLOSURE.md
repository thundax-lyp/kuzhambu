# Classics Share Export Closure Runbook

## Purpose

本 RUNBOOK 用于执行 `Classics` 本轮两项闭环：

- 分享管理闭环
- 导出 / 静态展示任务治理闭环

本文档只定义当前阶段的执行边界、数据结构变更、相关文件和任务拆分，不替代需求与正式设计文档。

## Scope

本轮必须完成：

- `Wangqi / MingCustoms / Sancai` 单内容分享入口闭环。
- 分享后台管理闭环：
  - 分页列表
  - 详情
  - targets 查看
  - 状态变更
  - 访问记录分页
- `Wangqi / MingCustoms` 导出任务创建与任务列表闭环。
- `Sancai` 导出任务列表与静态展示任务列表治理收口。
- `Classics Facade` 受影响面显式校验。
- 覆盖状态文档同步。

本轮不做：

- 批量分享
- 批量公开 / 私有
- 权限接入
- 导出 / 展示任务物理删除与清理策略
- 复杂视觉资产生产
- Portal 私有分享登录分支

## Current Decision

- 分享管理先不新开一级菜单。
- 分享管理本轮做完整：`列表 / 详情 / 状态 / 访问记录`。
- 导出治理先放在各内容页面内，不做全局任务中心。
- `Facade` 先校验，默认不改；只有确认缺字段时才最小补充。

## Data Structure Changes

### Backend

本轮默认：

- 不新增数据库表
- 不修改现有 schema
- 不修改分享、导出、静态展示领域实体主结构

本轮任务持久化表固定为：

- 分享链接：`classics_share_link`
- 分享目标：`classics_share_target`
- 分享访问记录：`classics_share_access_record`
- 三类内容导出任务：`classics_content_export_job`
- 三才静态展示任务：`classics_sancai_showcase`

本轮允许的数据结构变更仅限：

- `ClassicsSharingRequest`
  - 可补分页筛选字段：`issuedAfter`、`issuedBefore`、`shareLinkId`、`contentType`
- `ClassicsSharingResponse`
  - 可补列表、详情、访问记录展示所需字段
- 如访问记录需要独立响应结构，可新增 sharing interface 专用 response DTO

### Frontend

本轮允许新增或调整：

- `classics-share-types.ts`
  - 分享列表项
  - 分享详情
  - 访问记录
- `classics-share-service.ts`
  - `page`
  - `get`
  - `updateStatus`
  - `pageAccessRecords`
- 导出任务前端结构只做最小补充，不新起全局任务域

### Facade

本轮默认不修改：

- `ClassicsFacade`
- `ClassicsPublicContentFacadeDto`
- `ClassicsPublicContentFacadeResponse`

只有在执行过程中确认公共读口径缺字段时，才允许最小补充 `dto / response / assembler`。

## Related Files

### Sharing Backend

- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/ClassicsSharingAdminController.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/request/ClassicsSharingRequest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/controller/response/ClassicsSharingResponse.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/assembler/ClassicsSharingInterfaceAssembler.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sharing/ClassicsSharingAdminControllerTest.java`

### Sharing Frontend

- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-service-contract.test.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/wangqi/wangqi-page.test.tsx`

### Export / Showcase

- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/ming-customs/ming-customs-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-export-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-export-service-contract.test.ts`

### Facade Check

- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/ClassicsFacade.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java/com/thundax/kuzhambu/classics/facade/dto/ClassicsPublicContentFacadeDto.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/impl/ClassicsFacadeImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/facade/assembler/ClassicsFacadeAssembler.java`

### Documentation

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `TODO.md`

## Task Split

### Task A1. 分享后台接口补齐

范围：

- `.../ClassicsSharingAdminController.java`
- `.../ClassicsSharingRequest.java`
- `.../ClassicsSharingResponse.java`
- `.../ClassicsSharingInterfaceAssembler.java`
- `.../ClassicsSharingAdminControllerTest.java`

动作：

- 暴露分享分页列表接口
- 暴露访问记录分页接口
- 保持现有创建 / 详情 / 状态更新语义不变

### Task A2. 前端通用分享服务补齐

范围：

- `.../classics-share-service.ts`
- `.../classics-share-types.ts`
- `.../classics-share-service-contract.test.ts`

动作：

- 补齐分享管理所需 service 和 types
- 补齐契约测试

### Task A3. Wangqi 分享入口补齐

范围：

- `.../wangqi-page.tsx`
- `.../wangqi-page.test.tsx`
- 必要时 `.../wangqi-document-list.tsx`

动作：

- 接入单文档分享入口
- 与现有 MingCustoms / Sancai 反馈语义保持一致

### Task A4. 分享管理页落地

范围：

- `kuzhambu-apps/admin-web/src/pages/classics/common/components/` 下 2-5 个分享管理组件文件
- 必要时 `kuzhambu-apps/admin-web/src/pages/classics/sharing/` 下 1-2 个页面文件

动作：

- 完成列表 / 详情 / 状态 / 访问记录管理闭环

### Task B1. MingCustoms 导出任务治理

范围：

- `.../ming-customs-page.tsx`
- `.../ming-customs-page.test.tsx`
- 必要时新增 1-2 个导出任务复用组件文件

动作：

- 接入导出任务创建与任务列表

### Task B2. Sancai 导出 / 静态展示任务治理收口

范围：

- `.../sancai-entry-panel.tsx`
- `.../sancai-entry-panel.test.tsx`
- 必要时 `.../sancai-entry-service.ts`

动作：

- 收口导出任务区和静态展示任务区刷新与状态展示

### Task B3. 导出任务复用收口

范围：

- `.../classics-export-service.ts`
- `.../classics-export-service-contract.test.ts`
- 复用组件相关 2-3 个文件

动作：

- 把 `Wangqi / MingCustoms / Sancai` 的导出任务展示语义收敛到同一模式

### Task C1. Facade 校验

范围：

- `.../ClassicsFacade.java`
- `.../ClassicsPublicContentFacadeDto.java`
- `.../ClassicsFacadeImpl.java`
- `.../ClassicsFacadeAssembler.java`
- 必要时 facade 测试文件

动作：

- 校验本轮改动是否影响公共读口径
- 如无缺口，记录“已校验，无需修改”
- 如有缺口，最小补字段并补测试

### Task C2. 文档收口

范围：

- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `TODO.md`

动作：

- 同步覆盖状态
- 删除或收窄已完成 TODO

## Verification

本轮至少覆盖：

- 分享后台 controller 测试
- 分享前端 service contract 测试
- `Wangqi / MingCustoms / Sancai` 页面单测更新
- `Facade` 若改动则补测试；若未改动则明确记录“已校验，无需修改”
- 前端 `format:check`
- 前端 `lint`
- 前端 `test`
- 如后端接口调整，执行最小相关范围：
  - `mvn spotless:check`
  - `mvn checkstyle:check`
  - `mvn test`

## Done Criteria

- `Wangqi / MingCustoms / Sancai` 都能单内容创建分享
- 后台能查看分享列表、详情、targets、访问记录，并能变更状态
- `Wangqi / MingCustoms / Sancai` 都能查看各自导出任务状态与下载入口
- `Sancai` 能稳定查看静态展示任务状态与下载入口
- `Facade` 已完成显式校验，并记录结果
- 不新增数据库 schema 改动
- `TODO.md` 与覆盖文档同步收口
