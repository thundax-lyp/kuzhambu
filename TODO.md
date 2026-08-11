# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Discovery Portal QA API`：迁移 Portal QA endpoint 与调用方
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-02-DISCOVERY.md` 任务 12B
    - 范围对象：`DiscoveryQaPortalController.java`、`DiscoveryQaPortalStreamController.java`、`qa-service.ts`、`qa.spec.ts`、`qa-page-context.test.tsx`、`DiscoveryQaPortalControllerTest.java`、`DISCOVERY-QA-KNOWLEDGE-SPECIAL-DESIGN.md`
    - 处理动作：按 RUNBOOK 固定映射替换 Portal QA 的 open/export/chat/stream endpoint。
    - 验收点：列出文件中不存在旧 Portal QA URL，接口测试与 portal-web 相关测试通过。
    - 重要度：9/10

- [ ] `Discovery Admin QA API`：迁移 Admin QA 与 search statistics endpoint 及调用方
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-02-DISCOVERY.md` 任务 12C
    - 范围对象：`DiscoveryQaConversationController.java`、`DiscoveryQaConversationStreamController.java`、`DiscoveryQaAdminController.java`、`DiscoverySearchStatisticsController.java`、`qa-service.ts`、`qa-service-contract.test.ts`、`qa-console-service.ts`、`qa-console-service-contract.test.ts`、`search-statistic-service.ts`、`search-statistic-service-contract.test.ts`
    - 处理动作：按 RUNBOOK 固定映射替换 Admin QA、QA-admin 和 statistics endpoint。
    - 验收点：列出文件中不存在旧 Admin Discovery URL，interface 与 admin-web 相关测试通过。
    - 重要度：9/10

- [ ] `Discovery interface contract closure`：清除 Controller 动词 allowlist 并同步 API 文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-02-DISCOVERY.md` 任务 12D
    - 范围对象：`DiscoveryInterfaceArchitectureTest.java`、`DISCOVERY-QA-KNOWLEDGE-SPECIAL-DESIGN.md`、`DISCOVERY-DESIGN.md`
    - 处理动作：删除 Controller 动词 allowlist 并将两个设计文档更新为新 endpoint。
    - 验收点：interface architecture test 不引用 allowlist helper，两个设计文档不含旧 endpoint。
    - 重要度：9/10

- [ ] `Discovery allowlist 清理收口`：执行全量验证并清理任务现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-02-DISCOVERY.md` Verification、Closure；`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-02-DISCOVERY.md`、`TODO.md`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/`
    - 处理动作：运行最终 Maven、Spotless、Checkstyle 和静态扫描，确认变更范围后删除 RUNBOOK 与已完成 TODO。
    - 验收点：全部验证通过、工作区无关变更已排除、RUNBOOK 已删除且 `TODO.md` 不保留已完成项。
    - 重要度：10/10

## 待讨论项
