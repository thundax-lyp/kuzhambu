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
    - `searchEventId`
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

## Knowledge 运行时验证收口（2026-07-09）

### 环境

- 日期：2026-07-09（北京时间）
- 分支：`feat/knowledge-validation-closure`
- Java / Maven / Node / pnpm / Python：
  - `openjdk version "17.0.19" 2025-04-15 LTS`
  - `Apache Maven 3.9.11`
  - `Node v22.23.1`
  - `pnpm 10.7.0`
  - `Python 3.10.20`

### Java Servers

- 命令：

```sh
cd kuzhambu-servers
mvn -pl biz/knowledge,biz/ai -am spotless:check checkstyle:check test
```

- 结果：`BUILD SUCCESS`
- Reactor：`root`、`common/common-core`、`common/common-test`、`common/common-web`、`biz/ai`、`biz/knowledge` 均通过。
- Surefire：`common-test` 执行 `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`。

### Python Workers

- 准备：`cd kuzhambu-workers && python3.10 -m venv .venv && .venv/bin/python -m pip install -e '.[dev]'`
- 命令：

```sh
cd kuzhambu-workers
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture
```

- 结果：
  - `79 files already formatted`
  - `All checks passed!`
  - `241 passed, 1 warning in 1.46s`
- 备注：warning 为 FastAPI/Starlette TestClient 依赖的 `httpx` deprecation warning，不影响本次验证结论。

### Admin Web

- 命令：

```sh
cd kuzhambu-apps
pnpm --filter ./admin-web run format:check
pnpm --filter ./admin-web run lint
pnpm --filter ./admin-web run build
pnpm --filter ./admin-web run test
```

- 结果：
  - `format:check`：`All matched files use Prettier code style!`
  - `lint`：`eslint . && node scripts/check-css-boundaries.mjs` 通过。
  - `build`：`tsc --noEmit && vite build` 通过。
  - `test`：`64 passed (64)`、`279 passed (279)`，总耗时 `112.95s`。

### Knowledge Playwright

- 命令：

```sh
cd kuzhambu-apps
pnpm --filter ./admin-web exec playwright test \
  e2e/knowledge/taxonomy/taxonomy.spec.ts \
  e2e/knowledge/graph-extraction/graph-extraction.spec.ts \
  e2e/knowledge/graph-results/graph-results.spec.ts \
  e2e/knowledge/lineage/lineage.spec.ts \
  e2e/knowledge/refinement/refinement.spec.ts \
  e2e/knowledge/quality-report/quality-report.spec.ts
```

- 结果：`6 passed (24.0s)`。
- 覆盖页面与关键断言：
  - `taxonomy.spec.ts`：同义词搜索、新增、编辑、禁用、删除。
  - `graph-extraction.spec.ts`：图谱抽取任务创建、详情查看、候选应用、取消批任务、重生成。
  - `graph-results.spec.ts`：图谱版本、实体、关系、世系节点、世系关系详情。
  - `lineage.spec.ts`：画布筛选、刷新、重置、节点选择、关系选择、画布缩放。
  - `refinement.spec.ts`：精修任务筛选、打开、实体编辑确认、质量标注、应用任务、图谱/质量报告后续入口。
  - `quality-report.spec.ts`：质量报告生成、历史报告打开、问题/来源/标注 tab、低质量门类重提取。

### 收口结论

- Knowledge Java、Workers、Admin Web 单测/构建、Knowledge Playwright 6 个页面冒烟均通过。
- 本轮 evidence 可支撑 `KNOWLEDGE-IMPLEMENTATION-COVERAGE.md` 中“运行时验证部分完成”从 `部分完成` 更新为 `已完成`。

## 结论

- 已完成同一入口 `KnowledgeFacade.querySynonyms(...)` 的方向查询实现与验证闭环。
- Discovery Search/QA 的同义词扩展链路在 Playwright 冒烟里已形成可复现操作与断言。
- Admin 与 Portal 同步冒烟均通过，且 `Knowledge` 运行时验证从“仅有单元测试”升级到“静态检查 + Playwright 端到端样例”。
