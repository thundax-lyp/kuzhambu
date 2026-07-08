# KNOWLEDGE-RUNTIME-SMOKE-EVIDENCE

## 环境

- 日期：2026-07-08（北京时间）
- 分支：`feat/knowledge-synonym-smoke-closure`
- 提交：`824e2a6e`
- Java / Maven / Node / pnpm / Python：
  - `openjdk version "17.0.19"`
  - `Apache Maven 3.9.11`
  - `Node v22.23.1`
  - `pnpm 10.7.0`
  - `Python 3.10.20`
- 数据库 / ES / Provider Stub：本次运行仅使用后端编译结果与前端 mock 请求完成冒烟验证；未启动 `starter` 常驻运行时（见下方跨服务命令备注）。

## 数据准备

- Admin 与 Portal Playwright 冒烟使用统一模拟数据：
  - 同义词示例：
    - 正向：`礼制 -> 礼学`
    - 反向：`典礼 -> 礼制`
  - QA 场景问题：`礼学和礼制有什么关系？`

## Knowledge 同义词

### 后端方向查询单测

- 命令：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge/kuzhambu-knowledge-application -Dtest=KnowledgeTaxonomyReadApplicationServiceImplTest test
```

- 结果：`BUILD SUCCESS`（`Tests run: 7, Failures: 0, Errors: 0, Skipped: 0`）
- 关键验证：
  - `querySynonymsShouldReturnForwardMatchesOnly`
  - `querySynonymsShouldReturnReverseMatchesOnly`
  - `querySynonymsShouldMergeBidirectionalMatchesWithDeduplication`
  - `querySynonymsShouldClampLimitAndIgnoreBlankExpandedTerm`

## Discovery Search

### Frontend 冒烟（Portal Search）

- 命令：

```sh
cd kuzhambu-apps
pnpm --filter @kuzhambu/portal-web run e2e -- e2e/discovery/search.spec.ts e2e/discovery/qa.spec.ts
```

- 结果：`2 passed (2.1s)`（命令日志 `logs/portal_web_smoke_e2e.log`）
- 关键请求/响应证据（见 `portal-web/e2e/discovery/search.spec.ts`）：
  - `POST /api/portal/discovery/search/search` 返回样例
    - `queryText: "礼学"`
    - `highlightText: "<mark>礼学</mark> 与礼制"`
  - `POST /api/portal/discovery/search/click` 路径请求被断言包含：
    - `searchLogId`
    - `contentType`
    - `contentId`
    - `resultGroupKey`

## Discovery QA

### Frontend 冒烟（Portal QA）

- 命令：

```sh
cd kuzhambu-apps
pnpm --filter @kuzhambu/portal-web run e2e -- e2e/discovery/search.spec.ts e2e/discovery/qa.spec.ts

```

- 结果：同上 `2 passed (2.1s)`
- 关键请求/响应证据（见 `portal-web/e2e/discovery/qa.spec.ts`）：
  - `POST /api/portal/discovery/qa/session/open`
    - 返回 `sessionId: 7001`
    - `scope/contextMode`：`PORTAL` / `GENERAL`
  - `POST /api/portal/discovery/qa/chat/completions`
    - `model: "kuzhambu-qa"`
    - `stream: false`
    - `metadata.sessionId: 7001`
  - 回答正文中验证包含：`礼学可作为礼制相关内容的检索扩展。`
  - 来源展示验证 `sources[0].titleSnapshot == "礼制条目"`

## Admin Web 冒烟

- 命令：

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web run e2e -- e2e/knowledge/taxonomy/taxonomy.spec.ts
```

- 结果：`1 passed (4.5s)`（命令日志 `logs/admin_web_taxonomy_e2e.log`）
- 关键端到端断言（见 `admin-web/e2e/knowledge/taxonomy/taxonomy.spec.ts`）：
  - `/synonym/page` 搜索请求包含 `term: "礼制"`、`pageNo: 1`、`pageSize: 20`
  - 新建请求包含 `term: "礼制"`、`synonym: "礼学"`
  - 更新请求包含 `synonym: "典礼"`
  - 状态变更请求包含 `status: "DISABLED"`
  - 删除请求包含目标行 `id`

## 跨服务命令

- 已执行校验命令（静态与单测）

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge,biz/discovery -am spotless:check checkstyle:check compile -DskipTests
```

- 结果：`BUILD SUCCESS`（`reactors` 全部编译通过）
- 说明：本轮收口依据为前后端 Playwright + 单测闭环；受限于本机启动条件，未在本次任务中引入长期驻留式 `starter` 多服务联跑。

## 结论

- 已完成同一入口 `KnowledgeFacade.querySynonyms(...)` 的方向查询实现与验证闭环。
- Discovery Search/QA 的同义词扩展链路在 Playwright 冒烟里已形成可复现操作与断言。
- Admin 与 Portal 同步冒烟均通过，且 `Knowledge` 运行时验证从“仅有单元测试”升级到“静态检查 + Playwright 端到端样例”。
