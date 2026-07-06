# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Discovery Search 结果元数据`：补齐权限裁剪所需的检索结果元数据
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-FILTER-PERMISSION-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/result/SearchResult.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/test/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGatewayTest.java`
    - 处理动作：将 `knowledgeBase/categoryCode/tagNames/contentStatus/visibility/updatedAt` 从索引命中带入 application 结果模型
    - 验收点：ES 命中结果具备权限过滤元数据且 Portal 搜索响应字段不新增
    - 重要度：9/10

- [ ] `Discovery Search 权限过滤`：实现非公开搜索结果的后端权限裁剪
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-FILTER-PERMISSION-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/support/DefaultSearchPermissionFilter.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/search/service/impl/SearchApplicationServiceImplTest.java`
    - 处理动作：按当前主体权限裁剪非公开结果并重算分组数量和搜索总数
    - 验收点：匿名用户不能返回非公开结果，具备对应 Classics 权限的主体可返回对应非公开结果
    - 重要度：10/10

- [ ] `Discovery Search 筛选范围`：锁定高级筛选字段进入查询和日志
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-FILTER-PERMISSION-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/main/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/request/DiscoverySearchRequest.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/search/query/SearchQuery.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-domain/src/main/java/com/thundax/kuzhambu/discovery/domain/search/model/valueobject/SearchScope.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/client/ElasticsearchSearchIndexGateway.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/search/controller/DiscoverySearchPortalControllerTest.java`
    - 处理动作：确认筛选字段完整透传到 `SearchScope`、ES criteria 和 `search_scopes_json`
    - 验收点：知识库、门类、标签、状态、可见性和时间字段均可被后端接收、查询和追溯
    - 重要度：9/10

- [ ] `Portal 搜索高级筛选控件`：补齐 Portal 组合筛选交互
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-FILTER-PERMISSION-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/discovery/search-page.tsx`、`kuzhambu-apps/portal-web/src/pages/discovery/search-types.ts`、`kuzhambu-apps/portal-web/src/pages/discovery/search-page.test.tsx`
    - 处理动作：增加知识库多选、门类多选、标签多选、状态多选、可见性多选、时间范围、搜索和清除筛选控件
    - 验收点：用户可组合筛选后提交搜索，清除筛选和空状态清除操作能同步重置控件与 URL 状态
    - 重要度：9/10

- [ ] `Portal 搜索请求契约`：对齐前端搜索 payload 和响应消费字段
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-FILTER-PERMISSION-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/discovery/search-service.ts`、`kuzhambu-apps/portal-web/src/pages/discovery/search-types.ts`、`kuzhambu-apps/portal-web/src/pages/discovery/search-page.test.tsx`
    - 处理动作：让请求 payload 精确包含后端高级筛选字段并保持响应只消费既有展示字段
    - 验收点：前端请求字段与 `DiscoverySearchRequest` 一致且不依赖后端内部裁剪元数据
    - 重要度：8/10

- [ ] `Discovery 分支同步`：同步 main 分支最新代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-DISCOVERY-FILTER-PERMISSION-CLOSURE.md`
    - 范围对象：`codex/discovery-filter-permission-loop`、`main`、`/Volumes/storage/workspace/kuzhambu-discovery-filter-permission-loop`
    - 处理动作：在验证前同步 `main` 分支最新代码并解决本任务范围内冲突
    - 验收点：工作分支包含 `main` 最新代码，冲突处理不混入无关改动
    - 重要度：9/10

- [ ] `Discovery 筛选权限验证`：运行后端与 Portal 最小验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-FILTER-PERMISSION-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra`、`kuzhambu-apps/portal-web`
    - 处理动作：在同步 `main` 后执行 RUNBOOK 指定的 Java Spotless/Test 与 Portal format/lint/test 验证
    - 验收点：相关后端与前端验证命令通过，若无法运行则记录明确阻塞原因
    - 重要度：9/10

- [ ] `Discovery 收口文档`：更新 coverage 并清理阶段性 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-DISCOVERY-FILTER-PERMISSION-CLOSURE.md`
    - 范围对象：`docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-DISCOVERY-FILTER-PERMISSION-CLOSURE.md`、`TODO.md`
    - 处理动作：将对应能力标记为 `已完成`，删除已无剩余价值的 RUNBOOK，并按完成范围清理 TODO
    - 验收点：coverage 无中间状态，RUNBOOK 已清理，TODO 只保留未完成任务
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
