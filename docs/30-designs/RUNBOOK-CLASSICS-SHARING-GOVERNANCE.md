# RUNBOOK Classics 分享治理闭环

## 目标

把 Classics 分享治理闭环推进到已完成状态：

- 单个分享链接可以稳定承载多个内容目标。
- 分享恢复策略固定为未过期 `REVOKED -> ACTIVE`。
- 分享详情浏览和资源读取都进入访问统计。

## 范围

- 后端：`kuzhambu-servers/biz/classics/` sharing 相关 application、domain、infra、interface。
- Admin Web：`kuzhambu-apps/admin-web/src/pages/classics/sharing/` 和 `kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-service.ts`。
- Portal Web：`kuzhambu-apps/portal-web/src/pages/share/`。
- 文档同步：`docs/20-interfaces/CLASSICS-SHARE-PORTAL-INTERFACE.md`、`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`。

不处理内容编辑、Operations 报表聚合、Storage 底层生命周期、Discovery/AI 能力。

## 已确认决策

- 恢复只允许未过期 `REVOKED -> ACTIVE`，不允许恢复 `EXPIRED`。
- 恢复不改变 `visibility`、`expiresAt`、`shareToken`、`token_hash`、目标快照和目标顺序。
- 访问统计只统计成功访问；不存在、撤销、过期、权限不足、资源不存在均不累加。
- 单链接多内容和批量分享保持两种语义：单链接多内容是一个链接多个 `targets`；批量分享是多个内容各生成独立链接。
- 同一分享链接内重复目标按 `contentType + contentId` 拦截，返回明确错误，不依赖数据库唯一键异常。
- 访问类型先写入 `classics_share_access_record.client_snapshot`，不新增数据库字段。

## 数据结构变更

本任务不新增表，不新增列，不修改索引。

复用字段和写入规则：

- `classics_share_link.status`：恢复策略使用既有 `ACTIVE`、`REVOKED`、`EXPIRED`。
- `classics_share_link.expires_at`：恢复前判断链接是否已经过期。
- `classics_share_link.access_count`：详情浏览和资源读取成功后累加。
- `classics_share_target.share_link_id`、`content_type`、`content_id`：同一链接内重复目标判断口径，对应唯一约束 `UK(share_link_id, content_type, content_id)`。
- `classics_share_target.priority`：Admin 和 Portal 多目标展示顺序。
- `classics_share_access_record.share_link_id`：访问记录归属分享链接。
- `classics_share_access_record.share_target_id`：资源读取记录归属具体目标；详情浏览记录必须写 `null`。
- `classics_share_access_record.access_result`：成功访问固定写 `ALLOWED`。
- `classics_share_access_record.client_snapshot`：写入 JSON 字符串，字段限定为 `accessType`、`privateAccess`、`storageObjectId`、`download`。

详情浏览 `client_snapshot` 精确格式：

```json
{
  "accessType": "DETAIL_VIEW",
  "privateAccess": false
}
```

资源读取 `client_snapshot` 精确格式：

```json
{
  "accessType": "RESOURCE_READ",
  "privateAccess": false,
  "storageObjectId": 300000000001,
  "download": true
}
```

## 任务拆分

### 任务 1：后端分享创建与恢复规则

范围文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/ClassicsSharingApplicationService.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/repository/ClassicsSharingRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/repository/impl/ClassicsSharingRepositoryImpl.java`

处理动作：

- 在单链接创建路径中按 `contentType + contentId` 检测重复目标。
- 重复目标抛出稳定业务错误，错误信息表达“重复分享目标”。
- 在状态变更路径中固定恢复规则：只有未过期 `REVOKED` 可以切到 `ACTIVE`。
- 对 `EXPIRED -> ACTIVE`、已过期 `REVOKED -> ACTIVE`、其他非法状态流转返回明确业务错误。
- 恢复成功只更新 `classics_share_link.status`，不得更新 `expires_at`、`share_token`、`token_hash`、`classics_share_target` 或访问统计字段。

验收点：

- 单链接创建重复 target 不落库，返回明确重复目标错误。
- 未过期 `REVOKED` 分享恢复后同一 `shareToken` 可访问。
- `EXPIRED` 分享恢复失败。
- 已过期 `REVOKED` 分享恢复失败。

### 任务 2：后端访问统计闭环

范围文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sharing/model/entity/ClassicsShareAccessRecord.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/mapper/ClassicsShareLinkMapper.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sharing/persistence/mapper/ClassicsShareTargetMapper.java`

处理动作：

- `getPortalShare` 成功返回公开详情前写访问记录并累加 `access_count`。
- `getPrivatePortalShare` 成功返回私有详情前写访问记录并累加 `access_count`。
- 详情浏览访问记录写 `share_link_id = 当前链接 ID`、`share_target_id = null`、`access_result = ALLOWED`。
- 详情浏览 `client_snapshot` 写 `accessType = DETAIL_VIEW` 和 `privateAccess`。
- 资源读取继续写访问记录并累加 `access_count`。
- 资源读取访问记录写 `share_link_id = 当前链接 ID`、`share_target_id = 命中的目标 ID`、`access_result = ALLOWED`。
- 资源读取 `client_snapshot` 写 `accessType = RESOURCE_READ`、`privateAccess`、`storageObjectId`、`download`。
- 失败访问不调用 `increaseAccessCount`，不写成功访问记录。

验收点：

- 公开详情访问成功后 `access_count + 1`。
- 私有详情访问成功后 `access_count + 1`。
- 资源读取成功后 `access_count + 1`。
- 过期、撤销、无权限和资源不存在访问不增加 `access_count`。
- Admin 访问记录列表可区分详情浏览和资源读取。

### 任务 3：Admin sharing 页面闭环

范围文件：

- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-service.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-types.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/common/classics-share-service-contract.test.ts`

控件和操作要求：

列表表格：

- `累计访问` 列读取 `accessCount`。
- `状态` 列继续展示 `ACTIVE`、`REVOKED`、`EXPIRED`。
- `操作` 列对 `REVOKED` 且未过期记录展示 `恢复` 按钮。
- `EXPIRED` 记录不展示 `恢复` 按钮；如保留禁用按钮，tooltip 必须写“已过期不可恢复”。

恢复操作：

- 点击 `恢复` 按钮后调用 `useKuzhambuConfirm`。
- 确认标题使用“恢复分享”。
- 确认正文表达“恢复后同一分享链接将重新可访问”。
- 确认后调用现有状态更新接口，payload 固定包含 `id` 和 `status: "ACTIVE"`。
- 恢复成功后刷新列表和当前详情。

详情区域：

- `分享目标` 列表按后端返回顺序展示。
- 每个 target 行展示 `contentType`、`contentId`、`titleSnapshot`、`targetStatus`。
- `CONTENT_DELETED` 行展示“内容已删除”状态，不隐藏该 target。

访问记录表格：

- 表格列为 `访问时间`、`访问类型`、`访问结果`、`目标 ID`。
- `访问类型` 从 `clientSnapshot.accessType` 读取。
- `DETAIL_VIEW` 展示为 `详情浏览`。
- `RESOURCE_READ` 展示为 `资源读取`。
- `shareTargetId` 为空时目标 ID 展示为 `-`。

验收点：

- 撤销状态的未过期分享在列表和详情操作中可以恢复。
- 点击恢复后发出 `status: "ACTIVE"` 的状态更新请求。
- 已过期分享没有可用恢复操作。
- 多目标分享详情展示多个 target 行。
- 访问记录能展示详情浏览和资源读取两类记录。

### 任务 4：Portal share 只读访问页闭环

范围文件：

- `kuzhambu-apps/portal-web/src/pages/share/share-service.ts`
- `kuzhambu-apps/portal-web/src/pages/share/share-types.ts`
- `kuzhambu-apps/portal-web/src/pages/share/share-form.tsx`
- `kuzhambu-apps/portal-web/src/pages/share/share-form.test.tsx`
- `kuzhambu-apps/portal-web/src/pages/share/share-service.test.ts`

控件和操作要求：

读取流程：

- 公开分享详情继续通过 `/portal/classics/shares/{shareToken}` 读取。
- 私有分享先通过公开详情得到 `loginRequired`。
- `loginRequired = true` 且没有 access token 时展示登录引导。
- `loginRequired = true` 且有 access token 时调用 `/portal/classics/private-shares/{shareToken}`。
- 撤销、过期、不存在分享统一展示不可访问错误态，不提示具体原因。

内容卡片：

- 页面主体按 `targets` 数组渲染多个内容卡片。
- 内容卡片顺序使用后端返回顺序，不在前端重新排序。
- 每个内容卡片标题使用 `titleSnapshot`；缺失时展示“分享内容 N”。
- 每个内容卡片展示 `contentType`、`contentId`、`contentVersionNo`。
- `CONTENT_DELETED` 目标只展示标题、内容类型、内容 ID 和删除占位提示。
- `CONTENT_DELETED` 目标不得渲染正文、图片、文件预览按钮或下载按钮。

图片与资源控件：

- Sancai 图片缩略图列表只读取当前 target 的 `images`。
- Sancai 主图切换状态必须绑定当前 target，不跨 target 共享。
- Wangqi `预览` 按钮继续生成 `mode=preview` 的分享资源读取 URL。
- Wangqi `下载` 按钮继续生成 `mode=download` 的分享资源读取 URL。
- 私有分享资源按钮必须使用 `/portal/classics/private-shares/{shareToken}/resources/{storageObjectId}/content`。

验收点：

- 一个分享链接包含三个目标时，Portal 展示三个只读内容卡片。
- 已删除目标只显示占位，其余目标仍可阅读。
- 私有分享未登录展示登录引导，登录后展示 targets。
- 恢复后的同一 `shareToken` 在 Portal 可再次访问。
- 资源预览和下载仍可用，并触发后端资源读取统计。

### 任务 5：接口文档、readiness 和测试收口

范围文件：

- `docs/20-interfaces/CLASSICS-SHARE-PORTAL-INTERFACE.md`
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sharing/service/impl/ClassicsSharingApplicationServiceImplTest.java`
- `kuzhambu-apps/admin-web/src/pages/classics/sharing/sharing-page.test.tsx`
- `kuzhambu-apps/portal-web/src/pages/share/share-form.test.tsx`

处理动作：

- 在 Portal interface 文档中补充详情浏览成功会进入访问统计。
- 在 readiness 中把“单链接多个内容”“分享恢复策略”“访问统计”更新为已完成。
- 后端测试覆盖多 target 去重、恢复规则、详情访问统计、失败访问不统计。
- Admin Web 测试覆盖恢复按钮、状态更新 payload、访问类型展示、多目标详情。
- Portal Web 测试覆盖多目标渲染、删除目标占位、私有登录引导和不可访问错误态。

验收点：

- readiness 不再保留本任务三项的未完成描述。
- 文档契约与实际 Portal 访问统计语义一致。
- 相关测试能在本 RUNBOOK 的验证命令中通过。

## 验证命令

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-interface,biz/classics/kuzhambu-classics-infra -am spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -pl biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-interface,biz/classics/kuzhambu-classics-infra -am test
```

Admin Web 与 Portal Web：

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run format
pnpm --filter kuzhambu-portal-web run format
pnpm run format:check
pnpm run lint
pnpm --filter kuzhambu-admin-web run test -- sharing
pnpm --filter kuzhambu-portal-web run test -- share
pnpm --filter kuzhambu-admin-web run build
pnpm --filter kuzhambu-portal-web run build
```

## 收口

- 完成任务 1 到任务 5 后，删除本 RUNBOOK。
- 不提交本 RUNBOOK 作为长期设计文档；最终状态只保留在接口文档、readiness、测试和代码中。
- 不把本任务范围外的 Operations 聚合报表、内容编辑页面或 Storage 生命周期改动混入同一 PR。
