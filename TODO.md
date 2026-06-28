# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `DiscoveryFacade provider骨架`：新增 provider facade 入口骨架
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/pom.xml`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/facade/impl/DiscoveryFacadeImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/facade/assembler/DiscoveryFacadeAssembler.java`
    - 处理动作：在 discovery provider 侧新增 facade impl 和 assembler 骨架并接入 facade 模块依赖
    - 验收点：`discovery-application` 已具备可编译的 facade provider 入口骨架
    - 重要度：8/10

- [ ] `Discovery summary provider桥接`：桥接 summary provider 能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/facade/impl/DiscoveryFacadeImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/facade/assembler/DiscoveryFacadeAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/service/DiscoveryReportApplicationService.java`
    - 处理动作：将 `DiscoveryReportApplicationService.summary(...)` 桥接为 `DiscoveryFacade.summary(...)`
    - 验收点：provider 侧可返回完整 `DiscoverySummaryFacadeResponse`
    - 重要度：9/10

- [ ] `operations切换discovery facade`：operations 改用 discovery facade
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/pom.xml`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGateway.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/support/DefaultOperationsReportMetricsGatewayTest.java`
    - 处理动作：将 operations 报表摘要读取从 `DiscoveryReportApplicationService` 切换到 `DiscoveryFacade`
    - 验收点：operations 不再直接依赖 `discovery-application`，测试使用 facade 协议
    - 重要度：10/10

- [ ] `Discovery facade测试覆盖`：补齐 facade 协议与 provider 测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-facade/src/test/java/com/thundax/kuzhambu/discovery/facade/DiscoveryFacadeArchitectureTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/facade/impl/DiscoveryFacadeImplTest.java`
    - 处理动作：新增 facade 协议架构测试和 provider 映射测试
    - 验收点：`DiscoveryFacadeArchitectureTest` 与 `DiscoveryFacadeImplTest` 通过
    - 重要度：8/10

- [ ] `discovery跨域白名单收缩`：收缩 discovery 跨域 allowlist
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-FACADE-ISOLATION.md`
    - 范围对象：`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/RepositoryArchitectureRuleSupport.java`、`kuzhambu-servers/common/kuzhambu-common-test/src/main/java/com/thundax/kuzhambu/common/test/architecture/CrossApplicationIsolationArchitectureRuleSupport.java`
    - 处理动作：删除 `operations -> discovery` 的 POM allowlist 和 cross-application allowlist
    - 验收点：architecture 规则中不再保留 `operations -> discovery` 遗留白名单
    - 重要度：9/10

## 待审阅任务项

## 待讨论项
