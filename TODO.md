# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `operations alert latest check repository filter`：补齐告警 latestCheckId 持久化筛选
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-PROBE-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-domain/src/main/java/com/thundax/kuzhambu/operations/domain/health/repository/HealthAlertRepository.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/health/service/impl/HealthAlertApplicationServiceImpl.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthAlertRepositoryImpl.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/main/java/com/thundax/kuzhambu/operations/infra/health/persistence/mapper/HealthAlertMapper.java`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/health/repository/impl/HealthAlertRepositoryImplTest.java`
    - 处理动作：为健康告警分页 repository 增加 `latestCheckId` 精确筛选。
    - 验收点：repository 测试覆盖 `latest_check_id = latestCheckId` 查询，健康记录行可精确查询关联告警。
    - 重要度：9/10

- [ ] `admin-web operations health service`：新增健康分页前端服务契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-PROBE-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/health/health-types.ts`、`kuzhambu-apps/admin-web/src/pages/operations/health/health-service.ts`、`kuzhambu-apps/admin-web/src/pages/operations/health/health-service-contract.test.ts`
    - 处理动作：新增健康分页类型和 `getOperationsHealthPage` 服务，并锁定请求体字段。
    - 验收点：service contract 测试证明 `/operations/health/page` 请求包含组件、状态、来源、目标、时间范围和分页字段。
    - 重要度：9/10

- [ ] `admin-web operations health page controls`：实现健康细分页控件和表格
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-PROBE-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/health/health-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/health/health-page.css`、`kuzhambu-apps/admin-web/src/pages/operations/health/health-page.test.tsx`
    - 处理动作：实现组件、状态、来源、目标、时间范围、查询、重置、刷新、分页和健康记录表格。
    - 验收点：页面测试覆盖首次加载、查询、重置、刷新、分页切换、HTTP 来源筛选、目标筛选、状态 tag、空值和错误提示。
    - 重要度：10/10

- [ ] `admin-web operations health detail alerts`：实现健康详情抽屉和关联告警操作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-PROBE-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/health/health-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/health/health-page.css`、`kuzhambu-apps/admin-web/src/pages/operations/health/health-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-service.ts`
    - 处理动作：实现健康详情抽屉、`detailsJson` 格式化展示和 `latestCheckId=checkId` 关联告警查询。
    - 验收点：页面测试覆盖 JSON 格式化、非 JSON 降级、空 details、查看告警、告警空状态和告警请求失败提示。
    - 重要度：10/10

- [ ] `admin-web operations health route entry`：接入健康页路由和 Dashboard 入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-PROBE-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/router/index.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.tsx`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/dashboard-page.test.tsx`
    - 处理动作：新增 `/operations/health` 路由，并在 Dashboard 健康摘要区增加 `查看全部` 跳转入口。
    - 验收点：router 可渲染健康页，Dashboard 测试覆盖入口可见和跳转，Dashboard 不新增完整分页控件。
    - 重要度：9/10

- [ ] `operations health menu seed`：补齐健康检查菜单与权限种子
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-PROBE-LOOP.md`
    - 范围对象：`kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/db/data-source/system.json`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/main/resources/db/data/system.sql`、`kuzhambu-servers/starter/kuzhambu-admin-starter/src/test/java/com/thundax/kuzhambu/starter/admin/AdminStarterArchitectureTest.java`
    - 处理动作：在 Operations 菜单下新增“健康检查”页面菜单并沿用 `operations:health:view` 权限。
    - 验收点：JSON 和 SQL 种子菜单名称、路由 `/operations/health`、权限码一致，architecture 测试覆盖该入口。
    - 重要度：9/10

- [ ] `operations health backend targeted validation`：执行后端定向验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-PROBE-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/operations/`、`kuzhambu-servers/starter/kuzhambu-admin-starter/`
    - 处理动作：运行 RUNBOOK 指定的 Operations application、infra、interface 和 admin starter 定向测试。
    - 验收点：HTTP 探针、健康分页筛选、告警 `latestCheckId`、健康接口契约和菜单配置相关定向测试通过。
    - 重要度：10/10

- [ ] `admin-web operations health targeted validation`：执行前端定向验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-PROBE-LOOP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/operations/health/`、`kuzhambu-apps/admin-web/src/pages/operations/dashboard/`
    - 处理动作：运行 admin-web 健康页和 Dashboard 入口相关 format 与定向测试。
    - 验收点：健康分页 service、页面控件、详情抽屉、关联告警、Dashboard 跳转入口相关前端测试通过。
    - 重要度：10/10

- [ ] `operations health main sync before validation`：最终验证前同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/operations-health-probe-loop` 分支
    - 处理动作：在最终验证前同步最新 `origin/main` 到当前特性分支并解决冲突。
    - 验收点：当前分支包含最新 `origin/main`，同步冲突已解决且未混入无关修改。
    - 重要度：10/10

- [ ] `operations health final validation`：执行同步 main 后的最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-PROBE-LOOP.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/admin-web/`
    - 处理动作：在同步最新 `origin/main` 后运行 RUNBOOK 指定的后端 formatter、静态检查、测试和前端 format、lint、test、build。
    - 验收点：后端 Maven 检查和测试通过，前端 format/lint/test/build 通过，失败时收窄到具体未完成任务。
    - 重要度：10/10

- [ ] `operations implementation coverage closeout`：更新 Operations 完成状态并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-PROBE-LOOP.md`
    - 范围对象：`docs/40-readiness/OPERATIONS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-OPERATIONS-HEALTH-PROBE-LOOP.md`、`TODO.md`
    - 处理动作：在能力完成且验证通过后更新 Implementation Coverage 为已完成，并删除已无继续价值的 RUNBOOK。
    - 验收点：覆盖矩阵中“健康检查与运行状态”改为已完成，本次三项目标不再列为未完成，RUNBOOK 被清理，相关 TODO 删除或收窄。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
