# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `system avatar to storage-facade`：首批迁移 `system-application` 的最小头像入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/system/kuzhambu-system-application/src/main/java/com/thundax/kuzhambu/system/application/core/service/impl/CurrentUserApplicationServiceImpl.java`、`kuzhambu-servers/biz/storage/kuzhambu-storage-facade/src/main/java/com/thundax/kuzhambu/storage/facade/StorageReadableContentFacade.java`、`kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/core/service/impl/CurrentUserApplicationServiceImplTest.java`
    - 处理动作：将头像读取入口改为依赖 `StorageReadableContentFacade` 并新增对应应用层测试。
    - 验收点：`CurrentUserApplicationServiceImpl#getAvatarInputStream` 不再直接依赖 storage application 或 `StoredObjectStore` 的跨域只读形状。
    - 重要度：8/10

- [ ] `storage facade closure cleanup`：清理 Storage facade 试点现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-STORAGE-FACADE-ISOLATION.md`、相关实现覆盖文档与 PR 描述
    - 处理动作：在试点闭环完成后删除已完成 TODO、移除已完成 RUNBOOK 并同步实现覆盖与 PR 收口信息。
    - 验收点：`TODO.md` 不保留已完成项、已完成 RUNBOOK 被删除且文档与 PR 对试点收口状态口径一致。
    - 重要度：7/10

## 待讨论项
