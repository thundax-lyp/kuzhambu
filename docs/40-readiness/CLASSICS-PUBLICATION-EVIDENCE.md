# Classics Publication Readiness Evidence

## Stage 1: Foundation

Status: PR_OPEN

### Scope

- Baseline commit: `80f1927d9`
- Functional commit range (inclusive): `722855678^..a885a4fbb`
- Changed files: 138
- Diff: 2,113 insertions, 5,833 deletions

Stage 1 固定发布契约，拆除 server sharing 能力，校准正式版本 snapshot，并建立稿件与
publication job 的状态、租约、条件更新和清理持久化协议。

### Verification

| Check | Result |
| --- | --- |
| `mvn spotless:check checkstyle:check test` | PASS；58/58 reactor modules；2026-07-31；1 min 10 s |
| admin-web format/lint/test | PASS；87 test files；376 tests |
| portal-web format/lint/test | PASS；25 test files；79 tests |
| workers Ruff format/lint/test | PASS；181 tests |
| `scripts/verify-classics.sh` | PASS |
| publication persistence focused tests | PASS；8 tests |
| server sharing residue scan | 0 files |
| admin/portal sharing source scan | 23 files；按计划保留到 Stage 4 |
| legacy search MQ scan | 18 files；按计划保留到 Stage 5 |
| combined publication visibility scan | 183 files；按后续 stage 的语义边界继续收口 |
| `git diff --check` | PASS |

完整门禁首次执行发现 publication content persistence test 位于错误包，违反 Mapper 只能由
`repository.impl` 或 mapper package 依赖的架构规则。测试归位并 amend 同一工程判断后，
聚焦架构测试和第二次完整门禁均通过。

### Delivery State

- Delivery PR: `#193` (`merge`)。
- Stage 1 证据不包含数据库运行时 smoke；运行时收口由 Stage 6 完成。
- Deferred implementation: frontend sharing removal (Stage 4), legacy MQ and remaining visibility removal (Stage 5).

## Stage 5: Visibility And Legacy MQ Removal

Status: DELIVERED

### Scope

- Baseline commit: `d7dea5f6a`
- Functional commit range (inclusive): `932453204^..e2b0c75f6`
- Changed files: 170
- Diff: 433 insertions, 3,419 deletions

Stage 5 删除三类 Classics 稿件 publication visibility、Discovery public search 旧
`visibilityScopes/contentStatuses` 契约，以及 Classics -> RocketMQ -> Discovery 旧搜索同步链路。
Portal/public search 继续只由 ES `publicationStatus = READY` 且 `deleted = false` 决定。

### Verification

| Check | Result |
| --- | --- |
| `cd kuzhambu-servers && mvn spotless:check checkstyle:check test` | PASS；58/58 reactor modules；2026-08-02；3 min 41 s |
| focused Stage 5 Java tests | PASS；Classics/Discovery/starter impacted tests |
| focused admin-web tests | PASS；Ming/Wangqi/Discovery/classics contract tests |
| `cd kuzhambu-apps && pnpm run format:check && pnpm run lint` | PASS |
| `cd kuzhambu-apps && pnpm run build` | PASS；admin-web and portal-web |
| `cd kuzhambu-apps && pnpm --filter ./portal-web run test` | PASS；21 files；57 tests |
| `cd kuzhambu-apps && pnpm --filter ./admin-web exec vitest run <failed files>` | PASS；5 files；52 tests |
| `cd kuzhambu-apps && pnpm run test` | FAIL；admin-web full Vitest had 5 failed files / 8 failed tests; the same failed files passed in focused rerun |
| legacy search sync residue scan | PASS；0 results for searchsync/index-sync/ClassicsSearchIndexSync/RocketMqDiscoverySearchIndexSync |
| public search old filter residue scan | PASS；0 results for `visibilityScopes` / `contentStatuses` |
| `git diff --check` | PASS |

The admin-web full Vitest failure was not reproduced by focused rerun of the exact failed files:
`src/app.test.tsx`, `src/pages/classics/wangqi/wangqi-page.test.tsx`,
`src/pages/discovery/qa-console/qa-console-page.test.tsx`,
`src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx`, and
`src/pages/knowledge/taxonomy/taxonomy-page.test.tsx`.

### Delivery State

- Delivery PR: `#197` (`merge`)。
- Stage 6 completed the runtime smoke and closure evidence in the next section.

## Stage 6: Smoke Closeout Baseline

Status: COMPLETE

### Scope

- Delivery PR: `#198` (`merge`)。
- Functional commits: `fb5e75c57215a92d5781a91e3b23d06fe40cbd65` ..
  `a0ea3797993f50706e5427e2596726ab0bd4bf42`。

Stage 6 已补齐发布运行时恢复自动化覆盖、Portal 无用 RocketMQ 装配关闭、Discovery 中文
搜索 analyzer 配置、部署镜像构建参数、Elasticsearch `8.18.8` + `analysis-ik`
镜像制品准备，以及完整 Docker runtime smoke 收口。

### Verification

| Check | Result |
| --- | --- |
| PR `#198` `Verify Governance` | PASS |
| PR `#198` `Verify Backend` | PASS |
| PR `#198` `Verify Frontend` | PASS |
| PR `#198` `Verify Python Workers` | PASS |
| PR `#198` `Verify Database` | SKIPPED；PR 未改 `db/` |
| `mvn -pl biz/discovery/kuzhambu-discovery-infra -Dtest=ElasticsearchSearchIndexGatewayTest,DiscoverySearchIndexConfigurationTest test` | PASS |
| remote compose config | PASS；ES rendered as `kuzhambu/elasticsearch:8.18.8`, base `kuzhambu/elasticsearch-base:8.18.8`, platform `linux/amd64` |
| ES base image pull | PASS；`container-registry-test.elastic.co/elasticsearch/elasticsearch:8.18.8` pulled by `crane` through local proxy, loaded and retagged as `kuzhambu/elasticsearch-base:8.18.8` |
| final ES image build | PASS；`kuzhambu/elasticsearch:8.18.8` built with local IK zip; `elasticsearch-plugin list` returned `analysis-ik` |
| final ES archive | PASS；saved as ignored local artifact `deploy/image-files/foundation-elasticsearch-8.18.8.tar` |
| `scripts/verify-classics.sh` | PASS；2026-08-03；Classics schema and data files are present |
| remote Docker full smoke | PASS；2026-08-03；server `ser-zj@10.10.10.51`; command `KUZHAMBU_SMOKE_LOAD_IMAGES=false scripts/smoke/full-smoke.sh deploy/.env deploy/fastgpt/.env` |
| remote Classics publication smoke | PASS；2026-08-03；Sancai content `386995904401`; publish job `4`; offline job `5` |
| focused FastGPT client/config tests | PASS；`mvn -pl common/kuzhambu-common-knowledge test`；23 tests |

### Runtime Smoke Coverage

The remote Docker smoke started isolated compose projects with fresh volumes, reused already-loaded
server images, bootstrapped a blank FastGPT cluster, generated the Kuzhambu FastGPT env fragment, and
passed the FastGPT smoke for model records, OpenAPI health, dataset visibility, collection create,
disable, list, `pushData insertLen`, enable and delete.

The same smoke then started Kuzhambu MySQL, Redis, Elasticsearch, RocketMQ, workers, admin/portal
starters, admin-web, portal-web and nginx; loaded all `db/schema/*.sql` and generated `build/seed-sql/*.sql`; verified
admin and portal actuator health through nginx; and verified the portal static route.

The follow-up Classics publication smoke logged in through nginx, selected an `ERROR` Sancai entry,
created a new publish job, waited for `CONTENT_COMMITTED/SUCCEEDED`, verified the portal entry became
visible, created an offline job, waited for `CONTENT_COMMITTED/SUCCEEDED`, and verified the portal
entry was no longer public. The publish path crossed ES prepare/ready and FastGPT
collection create/push/enable; the offline path crossed ES disable and FastGPT disable.

Smoke execution exposed two deployment/runtime gaps and closed both before the final pass:

- FastGPT calls from Spring Boot 3.5 used the default JDK HTTP client path, which produced
  `HTTP/1.1 header parser received no bytes` against the FastGPT Node service during
  collection creation. The FastGPT client now uses `SimpleClientHttpRequestFactory` explicitly and
  sends `Connection: close`.
- Recreating the Kuzhambu starters manually without the full smoke override lost the shared
  FastGPT network and omitted the generated FastGPT env fragment. Deployment documentation now
  requires the generated env as an additional compose env source, and the full smoke continues to
  attach both compose projects to the same explicit smoke network.

FastGPT internal training, vectorization and retrieval visibility are intentionally not runtime smoke
gates. Publication readiness only requires FastGPT API acceptance and collection enable/disable
semantics; FastGPT internal progress remains owned by FastGPT.

### Closure

The temporary publication refactor RUNBOOK has been deleted after the stable requirements, interface,
special design, deployment documentation and this readiness evidence were aligned.
