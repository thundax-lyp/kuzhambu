# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `17 backend-tests`：更新 Discovery 后端测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-ID-FIELD-CLEANUP.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSessionRepositoryImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaMessageRepositoryImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/qa/repository/impl/QaSourceRepositoryImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalControllerTest.java`
    - 处理动作：更新 repository、application 和 interface 测试中的保存回填、`getById` 和 response JSON 字段断言。
    - 验收点：测试不再断言 `setId(nextId)` 或本体旧字段名，后端测试覆盖数据库回填 ID 和 `id` 响应字段。
    - 重要度：9/10

- [ ] `18 verification`：运行 Discovery ID 清理验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-ID-FIELD-CLEANUP.md`
    - 范围对象：`kuzhambu-servers`、`kuzhambu-apps/admin-web`、`kuzhambu-apps/portal-web`
    - 处理动作：运行 RUNBOOK 中 backend 和 frontend 验证命令，并记录失败项或通过结果。
    - 验收点：`mvn -pl biz/discovery -am spotless:check checkstyle:check test`、admin-web format/lint/test/build、portal discovery e2e 按 RUNBOOK 口径完成或留下明确失败原因。
    - 重要度：10/10

- [ ] `19 cleanup-runbook`：完成后清理 ID 清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-DISCOVERY-ID-FIELD-CLEANUP.md`
    - 范围对象：`docs/30-designs/RUNBOOK-DISCOVERY-ID-FIELD-CLEANUP.md`、`TODO.md`
    - 处理动作：在所有实现、测试和文档同步完成后删除临时 RUNBOOK，并从 TODO 中删除已完成任务项。
    - 验收点：项目内不再保留已完成的 ID 清理 RUNBOOK，`TODO.md` 不记录已完成任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
