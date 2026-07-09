# Classics Sancai Edit Closure Runbook

## 目标

完成三才图会“编辑标题、门类、卷、原文、译文和标签”的闭环。

已确认采用实现方案：条目编辑抽屉支持修改门类和卷。保存后，条目迁移到目标卷；门类由目标卷反查，不作为条目独立持久化字段。迁移不改变标签、问答对、图片、视觉资产、公开状态、生命周期、分享快照或历史导出产物。

## 现状

- 需求文件 `docs/10-requirements/CLASSICS-REQUIREMENTS.md` 已声明必须支持编辑条目标题、门类、卷、原文、译文和标签。
- 设计文件 `docs/30-designs/CLASSICS-DESIGN.md` 已声明 `classics_sancai_entry.volume_id` 是条目归属卷，支持三级浏览和编辑归属。
- 覆盖矩阵 `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md` 当前将该项标为 `部分完成`，未完成部分是门类/卷迁移未纳入页面闭环。
- 后端保存入参已有 `volumeId`：`SancaiEntryRequest`、`SancaiEntryCommand`、Admin Web `SancaiEntryCommand`。
- 前端表单值 `SancaiEntryFormValues` 当前没有 `categoryId` 和 `volumeId`，编辑抽屉只保存标题、原文、译文、摘要和公开状态。

## 数据结构变更

数据库不新增表、不新增字段、不新增索引；后端 HTTP/API 命令字段复用现有 `volumeId`；前端表单模型新增页面态字段 `categoryId` 和 `volumeId`。

数据库字段语义如下：

| 表 | 字段 | 变更类型 | 目标语义 |
| --- | --- | --- | --- |
| `classics_sancai_entry` | `volume_id bigint` | 复用并写入 | 条目归属卷。编辑迁移时更新为目标卷 ID。 |
| `classics_sancai_entry` | `priority int` | 复用并按规则写入 | 条目全表唯一排序值。跨卷迁移时写为 `max(priority) + 1`，等价移动到目标列表末尾。 |
| `classics_sancai_entry` | `content_updated_at datetime(3)` | 复用并写入 | 条目内容语义更新时间。保存迁移时更新。 |
| `classics_sancai_entry` | `current_version_id bigint` | 复用并由版本化链路写入 | 当前正式版本 ID。保存迁移后由版本化链路更新。 |
| `classics_sancai_entry` | `current_version_no int` | 复用并由版本化链路写入 | 当前正式版本号。保存迁移后递增。 |
| `classics_sancai_entry` | `current_versioned_at datetime(3)` | 复用并由版本化链路写入 | 当前正式版本生成时间。保存迁移后更新。 |
| `classics_sancai_volume` | `id bigint` | 只读校验 | 目标卷 ID。后端必须校验存在。 |
| `classics_sancai_volume` | `category_id bigint` | 只读推导 | 目标卷所属门类。页面用来过滤卷；后端不把门类作为条目字段保存。 |

Java 和 TypeScript 模型字段语义如下：

| 文件 | 字段 | 变更类型 | 目标语义 |
| --- | --- | --- | --- |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryRequest.java` | `volumeId` | 复用 | HTTP 保存请求中的目标卷 ID。 |
| `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/command/SancaiEntryCommand.java` | `volumeId` | 复用 | application 保存命令中的目标卷 ID。 |
| `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts` | `SancaiEntryCommand.volumeId` | 复用 | Admin Web 保存请求体中的目标卷 ID。 |
| `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-form-values.ts` | `SancaiEntryFormValues.categoryId` | 新增前端字段 | 页面门类 Select 当前值；只用于过滤卷，不提交给后端。 |
| `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-form-values.ts` | `SancaiEntryFormValues.volumeId` | 新增前端字段 | 页面卷 Select 当前值；提交到后端 `volumeId`。 |

## 业务规则

- 条目迁移以 `volumeId` 为唯一后端事实；门类归属由 `classics_sancai_volume.category_id` 决定。
- 新增条目和更新条目都必须校验 `volumeId` 非空且目标卷存在。
- 跨卷迁移时，条目 `priority` 写为 `repository.maxEntryPriority() + 1`。
- 同卷编辑时，保留原 `priority`。
- 每次保存迁移都生成 `MANUAL_SAVE` 正式版本，版本快照必须包含迁移后的 `volumeId`。
- 保存迁移后触发搜索同步；公开且已发布条目进入新归属卷对应的搜索内容。
- 迁移不重写 `classics_content_tag`、`classics_content_qa_pair`、`classics_sancai_entry_image`、`classics_sancai_visual_asset`、`classics_share_target` 和历史导出任务。

## 小任务 1：后端保存迁移

文件范围控制在 2-5 个文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiApplicationServiceImpl.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiRepository.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiRepositoryImpl.java`

实现要求：

- 优先复用现有 `repository.getVolumeById(SancaiVolumeId)`；如果端口已存在，不修改 `SancaiRepository.java` 和 `SancaiRepositoryImpl.java`。
- 在 `SancaiApplicationServiceImpl#addEntry` 中校验 `command.volumeId`。
- 在 `SancaiApplicationServiceImpl#updateEntry` 中先读取当前条目，再校验目标卷。
- 如果当前条目 `volumeId` 与目标 `volumeId` 不同：
  - 设置实体 `volumeId = targetVolumeId`。
  - 设置实体 `priority = repository.maxEntryPriority() + 1`。
  - 设置实体 `contentUpdatedAt = new Date()`。
  - 调用现有版本化和搜索同步链路。
- 如果当前条目 `volumeId` 与目标 `volumeId` 相同：
  - 保留当前 `priority`。
  - 继续按现有字段保存标题、原文、译文、摘要、状态和可见性。
- 目标卷不存在时抛出明确业务异常，复用或新增消息：`三才图会卷目不存在`。

## 小任务 2：前端表单和控件

文件范围控制在 2-5 个文件：

- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-form-values.ts`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`

数据流要求：

- `sancai-page.tsx` 持有 `categories` 和 `volumes` 查询结果。
- `sancai-page.tsx` 调用 `SancaiEntryPanel` 时传入：
  - `categories={categories}`
  - `volumes={visibleVolumes}` 或当前页面实际可用的卷集合。
- `sancai-entry-panel.tsx` 接收 `categories` 和 `volumes`，组装并传给 `SancaiEntryModel`：
  - `categoryOptions`: `Array<{ label: string; value: number }>`，来源为 `categories[].title` 和 `categories[].id`。
  - `volumes`: 原 `SancaiVolumeRecord[]`，用于卷 Select 过滤。
- `sancai-entry-model.tsx` 不自行请求门类或卷，只消费 props，避免抽屉内重复拉取目录数据。

控件要求：

- 在 `SancaiEntryModel` 编辑抽屉的基础信息区增加两个控件：
  - 门类选择控件：Ant Design `Select`，字段为 `categoryId`，标签文案为 `门类`。
  - 卷选择控件：Ant Design `Select`，字段为 `volumeId`，标签文案为 `卷`。
- 门类 Select：
  - `options={categoryOptions}`。
  - `value={form.categoryId ?? undefined}`。
  - `placeholder="选择门类"`。
  - `onChange` 写入 `form.categoryId`。
- 卷 Select：
  - `options` 来自 `volumes.filter((volume) => volume.categoryId === form.categoryId)`。
  - `value={form.volumeId ?? undefined}`。
  - `placeholder="选择卷"`。
  - `disabled={!form.categoryId}`。
  - `onChange` 写入 `form.volumeId`。
- 卷选项显示 `volume.title`，值为 `volume.id`。
- 编辑打开时：
  - 根据 `entry.volumeId` 在 `volumes` 中找到当前卷。
  - `form.volumeId = entry.volumeId`。
  - `form.categoryId = currentVolume.categoryId`。
- 新增打开时：
  - `form.volumeId` 默认使用当前树选中的 `volumeId`。
  - `form.categoryId` 默认使用当前树选中的 `categoryId`。
- 用户操作：
  - 选择门类后，如果当前 `form.volumeId` 不属于新门类，立即清空 `form.volumeId`。
  - 清空卷后，保存按钮点击时提示用户选择卷。
  - 选择卷后，`form.volumeId` 更新为目标卷 ID。
- 保存请求：
  - `entryService.add` 请求体必须包含 `volumeId`。
  - `entryService.update` 请求体必须包含 `id` 和 `volumeId`。
  - `categoryId` 不提交给后端。
  - `sancai-entry-service.ts` 不新增 API，只确保现有 `SancaiEntryCommand.volumeId` 从表单传入。
- 迁移成功后的页面行为：
  - 关闭编辑抽屉。
  - 刷新 `["classics", "sancai", "entries"]` 查询。
  - 刷新当前详情、版本历史和版本详情查询。
  - 如果迁移后的条目不在当前卷过滤结果中，当前列表自然移除该条目。
  - 成功提示使用明确文案：`三才图会条目已保存，归属卷已更新`。

## 小任务 3：测试

文件范围控制在 2-5 个文件：

- `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiApplicationServiceImplTest.java`
- `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAdminControllerTest.java`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`
- `kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
- `kuzhambu-apps/admin-web/e2e/classics/sancai/sancai.spec.ts`

后端测试要求：

- `SancaiApplicationServiceImplTest` 覆盖：
  - 更新条目时 `volumeId` 变更，保存后条目使用目标卷。
  - 跨卷迁移后 `priority = maxEntryPriority() + 1`。
  - 同卷编辑保留原 `priority`。
  - 迁移后追加正式版本。
  - 目标卷不存在时报错。
- `SancaiAdminControllerTest` 覆盖：
  - `/api/classics/sancai/entries/update` 请求体中的 `volumeId` 透传到 application command。

前端测试要求：

- `sancai-entry-panel.test.tsx` 覆盖：
  - 编辑抽屉打开后显示门类 Select 和卷 Select。
  - 当前条目的 `volumeId` 能反推出默认门类和卷。
  - 用户切换门类后，卷 Select 清空。
  - 用户选择新卷并保存后，update 请求体包含新 `volumeId`。
- `sancai-service-contract.test.ts` 覆盖：
  - `entryService.update({ id, volumeId, ... })` 发送 `volumeId`。
- `sancai.spec.ts` 如本地 E2E 条件稳定，则覆盖：
  - 从卷 A 打开条目。
  - 在编辑抽屉选择门类和卷 B。
  - 保存后卷 A 列表移除该条目。
  - 切换到卷 B 后能看到该条目。

## 小任务 4：文档和矩阵收口

文件范围控制在 3 个文件：

- `docs/10-requirements/CLASSICS-REQUIREMENTS.md`
- `docs/30-designs/CLASSICS-DESIGN.md`
- `docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`

文档修改要求：

- `CLASSICS-REQUIREMENTS.md`：
  - 保留“必须支持编辑条目标题、门类、卷、原文、译文和标签”。
  - 在三才图会规则中补充：条目门类/卷迁移通过选择目标卷完成，门类由目标卷决定；迁移不改变标签、问答对、图片、视觉资产、分享快照和历史导出产物。
- `CLASSICS-DESIGN.md`：
  - 在 `classics_sancai_entry.volume_id` 字段说明中明确：编辑迁移时更新该字段。
  - 在 `classics_sancai_entry.priority` 字段说明或表后约束中明确：跨卷迁移使用当前全局最大 `priority + 1`，使条目进入目标卷列表末尾。
- `CLASSICS-IMPLEMENTATION-COVERAGE.md`：
  - 将“编辑标题、门类、卷、原文、译文和标签”状态改为 `已完成`。
  - 已完成部分写明：后端保存校验目标卷、跨卷迁移更新 `volume_id` 和 `priority`、生成正式版本、Admin Web 编辑抽屉提供门类和卷 Select、保存后刷新列表和版本。
  - 未完成部分改为 `无`。

## 验收标准

- 编辑抽屉有 `门类` Select 和 `卷` Select。
- 门类切换后，卷 Select 只展示该门类下的卷。
- 门类切换导致当前卷不属于目标门类时，卷 Select 被清空。
- 未选择卷点击保存时，页面提示选择卷，且不发送保存请求。
- 选择目标卷后保存，请求体包含目标 `volumeId`。
- 后端校验目标卷存在；不存在时保存失败。
- 跨卷保存后，`classics_sancai_entry.volume_id` 更新为目标卷 ID。
- 跨卷保存后，`classics_sancai_entry.priority` 更新为保存时 `max(priority) + 1`。
- 同卷保存后，`classics_sancai_entry.priority` 不变。
- 保存后新增正式版本，版本快照里的 `volumeId` 是目标卷。
- 保存后当前卷列表不再显示已迁移条目，目标卷列表显示该条目。
- 标签、问答对、图片、视觉资产、公开状态和生命周期保持不变。
- 覆盖矩阵对应项为 `已完成`，未完成部分为 `无`。

## 验证命令

后端：

```sh
cd kuzhambu-servers
mvn -pl biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-interface,biz/classics/kuzhambu-classics-infra -am spotless:apply
mvn -pl biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-interface,biz/classics/kuzhambu-classics-infra -am spotless:check
mvn checkstyle:check
mvn -pl biz/classics/kuzhambu-classics-domain,biz/classics/kuzhambu-classics-application,biz/classics/kuzhambu-classics-interface,biz/classics/kuzhambu-classics-infra -am test
```

前端：

```sh
cd kuzhambu-apps
pnpm --filter ./admin-web run format
pnpm run format:check
pnpm run lint
pnpm --filter ./admin-web exec vitest run src/pages/classics/sancai
pnpm --filter ./admin-web run build
```

文档检查：

```sh
git diff --check
```

## 关闭条件

- 小任务 1-4 全部完成。
- 验收标准全部满足。
- 验证命令通过，或失败原因已记录为与本任务无关的既有问题。
- 删除本 RUNBOOK，避免任务执行手册长期沉淀为稳定设计文档。
