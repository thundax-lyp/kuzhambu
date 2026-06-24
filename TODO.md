# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Starter 装配验证`：确认 Discovery Search 运行时 bean 和扫描路径完整
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-RUNTIME.md`
    - 范围对象：`kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/java/com/thundax/kuzhambu/starter/admin/KuzhambuAdminApplication.java`、`kuzhambu-servers/starter/kuzhambu-portal-starter/src/main/java/com/thundax/kuzhambu/starter/portal/KuzhambuPortalApplication.java`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`、`kuzhambu-servers/starter/kuzhambu-portal-starter/src/test/java/com/thundax/kuzhambu/starter/portal/PortalStarterArchitectureTest.java`
    - 处理动作：确认 starter 扫描包和 mapper 包覆盖 Discovery Search 所需 bean
    - 验收点：不出现 SearchContentProvider、SearchIndexGateway、SearchIndexApplicationService 缺 bean
    - 重要度：8/10

- [ ] `运行时测试收口`：补路径测试并完成最小 Maven 验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-RUNTIME.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/admin/search/controller/DiscoverySearchAdminControllerTest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalControllerTest.java`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`、`kuzhambu-servers/starter/kuzhambu-portal-starter/src/test/java/com/thundax/kuzhambu/starter/portal/PortalStarterArchitectureTest.java`
    - 处理动作：为新增 index rebuild 入口补路径测试，并按 RUNBOOK 执行最小 Maven 验证
    - 验收点：新增接口路径稳定，本轮运行时闭环的最小测试链路可通过
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
