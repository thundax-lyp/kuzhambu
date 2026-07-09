# Discovery Quality Smoke Runbook

## Purpose

本 RUNBOOK 用于在进入线上质量验证前，对 Discovery 搜索与问答运行质量做一轮可复现冒烟，并沉淀证据。

目标闭环：

- Elasticsearch 健康和 `discovery-search` 索引可用。
- Admin Search 索引重建可执行且返回有效重建数量。
- Portal Search 能返回分组结果、关键词高亮和可跳转 `targetPath`。
- Portal Search 点击能写入 `discovery_search_click` 并被 Admin Search 分析汇总读取。
- Admin QA 知识库健康、重建、同步状态、来源列表和 trace 可读取。
- Portal QA 能生成带来源回答，来源跳转到当前可访问内容。

## References

- `docs/10-requirements/DISCOVERY-REQUIREMENTS.md`
- `docs/30-designs/DISCOVERY-DESIGN.md`
- `docs/30-designs/DISCOVERY-QA-KNOWLEDGE-SPECIAL-DESIGN.md`
- `docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`
- `docs/00-governance/HOW-TO-ADMIN-LOGIN-SMOKE.md`

## Environment

本轮冒烟固定使用预发或线上质量门禁等价环境，不使用开发者本地环境作为最终证据来源。

必须具备：

- Java 17。
- MySQL、Redis、Elasticsearch、RocketMQ 可访问。
- Admin starter：`http://127.0.0.1:20010/kuzhambu-admin-api`
- Portal starter：`http://127.0.0.1:20011/kuzhambu-api`
- Admin Web：`http://127.0.0.1:5173`
- Portal Web：`http://127.0.0.1:5174`
- 环境变量已加载，且包含 Elasticsearch、DB、Redis、RocketMQ、AI provider、Storage 所需配置。

固定变量：

```sh
export ADMIN_API=http://127.0.0.1:20010/kuzhambu-admin-api/api
export PORTAL_API=http://127.0.0.1:20011/kuzhambu-api/api
export ES_URL=${KUZHAMBU_ELASTICSEARCH_URIS:-https://127.0.0.1:9200}
export ES_USER=${KUZHAMBU_ELASTICSEARCH_USERNAME:-elastic}
export ES_PASS=${KUZHAMBU_ELASTICSEARCH_PASSWORD:-elastic}
export DISCOVERY_INDEX=discovery-search
```

## Runtime Start

启动 Admin starter：

```sh
set -a
source <env-file>
set +a

cd kuzhambu-servers
mvn -pl starter/kuzhambu-admin-starter -am -DskipTests install
cd starter/kuzhambu-admin-starter
mvn spring-boot:run
```

启动 Portal starter：

```sh
set -a
source <env-file>
set +a

cd kuzhambu-servers
mvn -pl starter/kuzhambu-portal-starter -am -DskipTests install
cd starter/kuzhambu-portal-starter
mvn spring-boot:run
```

启动前端：

```sh
cd kuzhambu-apps
pnpm --filter kuzhambu-admin-web dev
pnpm --filter @kuzhambu/portal-web dev -- --host 127.0.0.1 --port 5174
```

通过标准：

- `GET /kuzhambu-admin-api/actuator/health` 返回 `UP`。
- `GET /kuzhambu-api/actuator/health` 返回 `UP`。
- Admin Web 和 Portal Web 页面可打开。

## Data Fixture

执行前固定确认一组公开、可检索、可问答的数据，不使用临时猜测关键词。

本轮建议数据：

- 查询词：`礼制`
- 扩展词：`礼学`
- QA 问题：`礼制和礼学有什么关系？`
- 内容范围：至少一条 `PUBLIC` 的 `SANCAI_ENTRY`、`WANGQI_DOCUMENT` 或 `MING_CUSTOMS`
- 记录字段：`contentType`、`contentId`、`title`、`targetPath`

通过标准：

- 测试内容在预发环境中可由 Portal 匿名访问。
- 测试内容已进入 `discovery-search` 索引。
- 测试内容已进入 QA Knowledge Base，或可通过本 RUNBOOK 的 QA rebuild 同步成功。

## Provider Precheck

执行前确认 Knowledge Base provider：

- provider 网络可达。
- provider 认证、额度和白名单可用。
- 逻辑知识库 `kuzhambu-qa` 可解析。
- provider health 接口或等价检查结果为成功态。

通过标准：

- provider 不存在认证失败、额度不足、网络阻断或知识库不存在问题。
- 若 provider 预检失败，本 RUNBOOK 不进入 Search rebuild 和 QA rebuild。

## Authentication

按 `docs/00-governance/HOW-TO-ADMIN-LOGIN-SMOKE.md` 获取 Admin `Access-Token`。

将结果写入：

```sh
export ADMIN_TOKEN=<admin-access-token>
```

通过标准：

- Admin token 可以访问 `POST $ADMIN_API/discovery/search-admin/logs/page`。
- Portal Search 和 Portal QA 冒烟接口不要求登录态；如环境开启匿名访问限制，本轮冒烟停止并先修正环境访问策略。

## ES Health

执行：

```sh
curl -k -fsS -u "$ES_USER:$ES_PASS" "$ES_URL/_cluster/health?pretty"
curl -k -fsS -u "$ES_USER:$ES_PASS" "$ES_URL/$DISCOVERY_INDEX/_count?pretty"
curl -k -fsS -u "$ES_USER:$ES_PASS" "$ES_URL/_cat/indices/$DISCOVERY_INDEX?v"
```

通过标准：

- 集群 `status` 必须为 `green`。
- `$DISCOVERY_INDEX` 存在。
- `_count.count` 大于 0。
- 若状态为 `yellow` 或 `red`，本轮冒烟不得通过。

## Search Rebuild

执行 Admin 重建：

```sh
curl -fsS -X POST "$ADMIN_API/discovery/search-admin/index/rebuild" \
  -H "Access-Token: $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"confirm":true}'
```

重建后复查 ES：

```sh
curl -k -fsS -u "$ES_USER:$ES_PASS" "$ES_URL/$DISCOVERY_INDEX/_count?pretty"
curl -k -fsS -u "$ES_USER:$ES_PASS" "$ES_URL/$DISCOVERY_INDEX/_search?pretty&size=3" \
  -H "Content-Type: application/json" \
  -d '{"query":{"term":{"deleted":false}}}'
```

通过标准：

- Admin 重建接口返回数值大于 0。
- ES count 大于 0。
- 样例文档包含 `contentType`、`contentId`、`title`、`bodyText`、`deleted:false`。

## Portal Search Highlight

执行：

```sh
SEARCH_JSON=$(curl -fsS -X POST "$PORTAL_API/portal/discovery/search/search" \
  -H "Content-Type: application/json" \
  -d '{
    "queryText":"礼制",
    "knowledgeBases":["SANCAI_ENTRY","WANGQI_DOCUMENT","MING_CUSTOMS"],
    "pageNo":1,
    "pageSize":10
  }')

printf '%s\n' "$SEARCH_JSON" | jq .
export SEARCH_LOG_ID=$(printf '%s' "$SEARCH_JSON" | jq -r '.data.searchLogId')
export FIRST_GROUP=$(printf '%s' "$SEARCH_JSON" | jq -r '.data.groups[0].groupKey')
export FIRST_TYPE=$(printf '%s' "$SEARCH_JSON" | jq -r '.data.groups[0].items[0].contentType')
export FIRST_ID=$(printf '%s' "$SEARCH_JSON" | jq -r '.data.groups[0].items[0].contentId')
export FIRST_PATH=$(printf '%s' "$SEARCH_JSON" | jq -r '.data.groups[0].items[0].targetPath')
```

通过标准：

- `searchLogId` 非空。
- `groups` 至少包含一个分组。
- 第一条结果包含 `title`、`contentType`、`contentId`、`targetPath`。
- 命中结果 `highlightText` 包含白名单标签 `<mark>`，且不包含其他 HTML 标签。
- Portal Web `/discovery/search?q=礼制` 能渲染相同查询结果，刷新后仍保留查询状态。

## Search Click Log

执行：

```sh
curl -fsS -X POST "$PORTAL_API/portal/discovery/search/click" \
  -H "Content-Type: application/json" \
  -d "{
    \"searchLogId\":\"$SEARCH_LOG_ID\",
    \"contentType\":\"$FIRST_TYPE\",
    \"contentId\":\"$FIRST_ID\",
    \"resultGroupKey\":\"$FIRST_GROUP\",
    \"targetPath\":\"$FIRST_PATH\"
  }"
```

复查 Admin 日志与分析：

```sh
curl -fsS -X POST "$ADMIN_API/discovery/search-admin/logs/get" \
  -H "Access-Token: $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"searchLogId\":\"$SEARCH_LOG_ID\"}" | jq .

curl -fsS -X POST "$ADMIN_API/discovery/search-admin/analysis/summary" \
  -H "Access-Token: $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}' | jq .
```

通过标准：

- 点击接口返回成功。
- Admin 日志详情能按 `searchLogId` 读取本次搜索。
- Admin summary 中 `searchCount`、`clickCount` 为非零值。
- Admin Web `discovery/search-admin` 能看到本次查询词和分析摘要。

## QA Knowledge Health

执行：

```sh
curl -fsS -X POST "$ADMIN_API/discovery/qa-admin/knowledge/health" \
  -H "Access-Token: $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}' | jq .
```

通过标准：

- 返回 provider、knowledge base、健康状态和最近检查时间。
- 健康状态为可用；不可用时必须有失败原因，本轮 QA 冒烟不得通过。

## QA Knowledge Rebuild

执行：

```sh
QA_REBUILD_ID=$(curl -fsS -X POST "$ADMIN_API/discovery/qa-admin/knowledge/rebuild" \
  -H "Access-Token: $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"requestId\":\"discovery-quality-smoke-$(date +%Y%m%d%H%M%S)\"}" | jq -r '.data')

curl -fsS -X POST "$ADMIN_API/discovery/qa-admin/knowledge/sync/page" \
  -H "Access-Token: $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"pageNo":1,"pageSize":20}' | jq .
```

通过标准：

- `QA_REBUILD_ID` 非空。
- 同步分页能看到本轮同步记录。
- 同步记录没有 `FAILED`；如存在 `FAILED`，必须能在 `failureReason` 中定位 provider、内容源或权限原因，本轮 QA 冒烟不得通过。

## Portal QA Sources

打开或恢复 Portal QA 会话：

```sh
SESSION_JSON=$(curl -fsS -X POST "$PORTAL_API/portal/discovery/qa/session/open" \
  -H "Content-Type: application/json" \
  -d '{
    "scope":"PORTAL",
    "contextMode":"GENERAL"
  }')

export QA_SESSION_ID=$(printf '%s' "$SESSION_JSON" | jq -r '.data.sessionId')
```

发起问答：

```sh
CHAT_JSON=$(curl -fsS -X POST "$PORTAL_API/portal/discovery/qa/chat/completions" \
  -H "Content-Type: application/json" \
  -d "{
    \"model\":\"kuzhambu-qa\",
    \"stream\":false,
    \"metadata\":{\"sessionId\":$QA_SESSION_ID},
    \"messages\":[{\"role\":\"user\",\"content\":\"礼制和礼学有什么关系？\"}]
  }")

printf '%s\n' "$CHAT_JSON" | jq .
export QA_MESSAGE_ID=$(printf '%s' "$CHAT_JSON" | jq -r '.data.choices[0].message.messageId')
export QA_SOURCE_PATH=$(printf '%s' "$CHAT_JSON" | jq -r '.data.sources[0].sourcePath')
```

通过标准：

- 回答 `choices[0].message.content` 非空。
- `sources` 至少包含一条来源。
- 来源包含 `titleSnapshot`、`contentType`、`contentId`、`sourcePath`、`sourceStatus`。
- `sourceStatus` 为 `AVAILABLE`。
- Portal Web 点击来源能跳转到 `$QA_SOURCE_PATH` 对应内容；无权限来源不得展示正文。

## Admin QA Evidence

执行：

```sh
curl -fsS -X POST "$ADMIN_API/discovery/qa-admin/session/get" \
  -H "Access-Token: $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"sessionId\":$QA_SESSION_ID}" | jq .

curl -fsS -X POST "$ADMIN_API/discovery/qa-admin/source/list" \
  -H "Access-Token: $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"messageId\":$QA_MESSAGE_ID}" | jq .
```

从 Admin Web `discovery/qa-admin` 打开同一会话，记录：

- 会话消息列表。
- 来源列表。
- provider trace。
- AI `callId`、`aiStatus`、`aiErrorType`、`aiErrorMessage`。

通过标准：

- Admin 会话详情能读取本次问答。
- Admin 来源列表与 Portal 返回来源一致。
- provider trace 存在，且失败字段为空。
- 如果 trace 中有 AI 调用字段，`aiStatus` 为成功态。

## Database Evidence

使用同一环境数据库执行：

```sql
SELECT search_log_id, query_text, result_total_count, group_total_count, search_status, search_latency_ms, created_at
FROM discovery_search_log
WHERE search_log_id = '<SEARCH_LOG_ID>';

SELECT search_log_id, content_type, content_id, result_group_key, clicked_at
FROM discovery_search_click
WHERE search_log_id = '<SEARCH_LOG_ID>'
ORDER BY clicked_at DESC
LIMIT 5;

SELECT session_id, title, status, context_mode, context_content_type, context_content_id, last_message_at
FROM discovery_qa_session
WHERE session_id = <QA_SESSION_ID>;

SELECT message_id, session_id, role, answer_status, sent_at
FROM discovery_qa_message
WHERE session_id = <QA_SESSION_ID>
ORDER BY sent_at;

SELECT message_id, source_id, content_type, content_id, title_snapshot, source_path, source_status
FROM discovery_qa_message_source
WHERE message_id = <QA_MESSAGE_ID>;

SELECT trace_id, message_id, provider, latency_ms, failure_reason, retrieved_at
FROM discovery_qa_retrieval_trace
WHERE message_id = <QA_MESSAGE_ID>;
```

通过标准：

- 搜索日志、点击日志、QA 会话、QA 消息、QA 来源、QA trace 均有本轮记录。
- 搜索状态和回答状态均为成功态。
- `failure_reason` 为空。

## Evidence Package

本轮证据固定归档到一个目录，不写入仓库：

```text
discovery-quality-smoke-YYYYMMDD-HHMM/
  01-env.txt
  02-es-health.json
  03-search-rebuild.json
  04-search-response.json
  05-search-click.json
  06-search-admin-log.json
  07-qa-health.json
  08-qa-rebuild.json
  09-qa-chat.json
  10-qa-admin-session.json
  11-db-evidence.txt
  screenshots/
    admin-search-admin.png
    portal-search-highlight.png
    portal-qa-source.png
    admin-qa-trace.png
```

证据包通过标准：

- 每个 JSON 文件保留原始接口响应。
- 截图能看到页面 URL、关键字段和当前时间。
- `01-env.txt` 记录分支、提交、Java/Maven/Node/pnpm/Python 版本、ES URL 主机、DB 主机、starter 端口，不记录密钥。
- 证据包上传到本阶段 PR 描述指定的对象存储或内部文档位置，并在 PR `Verification` 中填写链接。

## Follow-up Outside This Runbook

本 RUNBOOK 不修复 schema 或代码问题。执行中如发现运行态表结构与 `db/schema/discovery.sql` 不一致，单独创建 schema 对齐任务处理。

已知需独立确认：

- 运行时代码使用 `discovery_search_click`。
- `db/schema/discovery.sql` 当前仍保留旧 `discovery_search_click_log`。

## Final Pass Criteria

本 RUNBOOK 只有同时满足以下条件才算通过：

- ES 集群为 `green`，`discovery-search` 索引存在且文档数大于 0。
- Admin Search rebuild 返回有效数量，重建后索引仍可检索。
- Portal Search 返回高亮结果，结果深链可打开。
- Portal click 写入成功，Admin Search 日志和 summary 能读取点击证据。
- Admin QA health 可用，QA rebuild 无失败项。
- Portal QA 返回带来源回答，来源状态可用且跳转成功。
- Admin QA 能读取同一会话、来源和 trace。
- DB 能查到同一轮 `searchLogId`、`sessionId`、`messageId` 对应事实。
- 证据包完整，不包含密钥、token、provider 原始敏感响应。

## Stop Conditions

出现以下任一情况，本轮冒烟停止并判定未通过：

- Elasticsearch cluster health 不是 `green`。
- `discovery-search` 索引不存在或 count 为 0。
- Search rebuild 接口失败或返回 0。
- Search 结果缺少 `searchLogId`、`highlightText`、`targetPath` 任一关键字段。
- 点击接口失败或 Admin 无法按 `searchLogId` 读取日志。
- QA health 不可用。
- QA rebuild 出现失败项且不能定位失败原因。
- Portal QA 无来源，或来源跳转不可用。
- Admin QA trace 缺失，或 trace 存在未解释失败字段。
