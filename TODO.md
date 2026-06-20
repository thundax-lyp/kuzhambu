# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `classics versioning service`：实现统一版本判断和版本生成服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-VERSIONABLE.md`
    - 范围对象：`ClassicsContentVersioningService.java`、`ClassicsContentSnapshotAssembler.java`、`ClassicsContentApplicationServiceImpl.java`
    - 处理动作：实现 `needsVersion`、版本号递增、稳定快照生成和 `ensureVersioned` 能力
    - 验收点：任意 `Versionable` 内容可判断是否需要版本，并能生成版本后回填主表版本标定字段
    - 重要度：10/10

- [ ] `classics manual save flows`：三类内容手动保存触发正式版本
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-VERSIONABLE.md`
    - 范围对象：`SancaiApplicationServiceImpl.java`、`WangqiDocumentApplicationServiceImpl.java`、`MingCustomsApplicationServiceImpl.java`
    - 处理动作：在三类内容手动保存成功后生成 `MANUAL_SAVE` 正式版本
    - 验收点：手动保存会写入 `classics_content_version`，自动保存草稿不会写入正式版本
    - 重要度：10/10

- [ ] `classics AI restore flows`：AI 应用和历史恢复触发正式版本
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-VERSIONABLE.md`
    - 范围对象：AI 应用结果写入入口、历史恢复入口、`ClassicsContentApplicationServiceImpl.java`
    - 处理动作：在用户确认 AI 应用和历史恢复后分别生成 `AI_APPLIED` 与 `HISTORY_RESTORED` 正式版本
    - 验收点：AI 应用和历史恢复都有可追溯版本，且单次实施文件范围控制在 2-5 个文件
    - 重要度：9/10

- [ ] `classics versionable tests`：补齐 Versionable 自动化测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-VERSIONABLE.md`
    - 范围对象：`kuzhambu-classics-domain/src/test`、`kuzhambu-classics-application/src/test`、`kuzhambu-classics-infra/src/test`
    - 处理动作：新增覆盖版本判断、版本生成、主表回填和非用户确认 update 不生成版本的测试
    - 验收点：测试明确断言自动保存、排序、状态刷新或访问统计不会写入 `classics_content_version`
    - 重要度：10/10

- [ ] `classics versionable validation`：运行 Versionable 验证和 dev.env 冒烟测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-VERSIONABLE.md`
    - 范围对象：`kuzhambu-servers`、`dev.env`、`kuzhambu-admin-starter`
    - 处理动作：运行 Maven 格式检查、静态检查、相关测试、本地数据库同步、starter 启动和业务冒烟检查
    - 验收点：相关验证通过，至少一类内容手动保存生成正式版本并回填主表，至少一个非正式版本动作不生成版本
    - 重要度：10/10

- [ ] `classics share target version binding`：分享目标绑定正式内容版本
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-VERSIONABLE.md`
    - 范围对象：`classics_share_target`、分享创建应用服务、分享管理展示
    - 处理动作：为分享目标增加版本绑定字段并在创建分享时调用 `ensureVersioned`
    - 验收点：分享 target 绑定正式版本并冻结快照，管理侧能展示分享版本与当前内容版本差异
    - 重要度：8/10

## 待讨论项
