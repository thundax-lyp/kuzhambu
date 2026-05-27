# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `classics-starter-admin`：装配 Classics 后台能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`kuzhambu-servers/starter/kuzhambu-admin-starter/pom.xml`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/application.yml`
    - 处理动作：将 Classics 后台模块依赖和运行时配置接入 admin starter。
    - 验收点：starter 不新增业务 Controller 或业务规则。
    - 重要度：8/10

- [ ] `classics-starter-portal`：装配 Classics portal 分享能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`kuzhambu-servers/starter/kuzhambu-portal-starter/pom.xml`、`kuzhambu-servers/starter/kuzhambu-portal-starter/src/main/resources/application.yml`
    - 处理动作：将 Classics 分享访问模块依赖和运行时配置接入 portal starter。
    - 验收点：portal starter 只做运行时装配。
    - 重要度：7/10

- [ ] `classics-verification-persistence`：增加 Classics 持久化验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`scripts/verify-classics.sh`、`scripts/verify-all.sh`、`SancaiRepositoryTest.java`
    - 处理动作：验证 schema、data 和 Repository 基础查询。
    - 验收点：可验证 15 个门类、99 卷和 3359 条三才图会条目可查询。
    - 重要度：8/10

- [ ] `classics-verification-api`：增加 Classics API 验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`
    - 范围对象：`SancaiAdminControllerTest.java`、`WangqiDocumentAdminControllerTest.java`、`MingCustomsAdminControllerTest.java`、`ClassicsSharingPortalControllerTest.java`
    - 处理动作：验证后台 API 和分享 portal API 基础契约。
    - 验收点：接口响应结构、权限标记和错误语义符合接口文档。
    - 重要度：8/10
