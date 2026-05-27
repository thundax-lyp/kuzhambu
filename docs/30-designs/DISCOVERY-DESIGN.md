# Discovery Design

## Purpose

本文档定义 Discovery 域设计，覆盖跨库搜索和智能问答。

## Module

```text
kuzhambu-servers/biz/discovery/
  kuzhambu-discovery-interface/
  kuzhambu-discovery-application/
  kuzhambu-discovery-domain/
  kuzhambu-discovery-infra/
```

## Business Boundary

Discovery 拥有搜索查询、搜索日志、问答会话、问答消息、来源引用和调试信息。Discovery 消费 Classics 内容、Knowledge 同义词和实体、AI 回答生成能力、System 权限上下文。

## DDD Model

- `SearchQuery`
- `SearchResultGroup`
- `SearchLog`
- `SearchClick`
- `QueryUnderstanding`
- `QaSession`
- `QaMessage`
- `QaSource`
- `QaDebugContext`

## Data Model

表名前缀统一使用 `discovery_`。

核心表：

- `discovery_search_log`
- `discovery_search_click`
- `discovery_query_understanding`
- `discovery_qa_session`
- `discovery_qa_message`
- `discovery_qa_source`
- `discovery_qa_debug_context`

搜索索引不是业务真相源，索引结构由 infra 适配维护。

## Application Layer

- `SearchApplicationService`
- `QueryUnderstandingApplicationService`
- `QaApplicationService`
- `QaSessionApplicationService`

Application 层负责权限过滤、查询理解、同义词扩展、实体增强、搜索结果分组、问答上下文组装、来源引用和会话管理。

## Interface Layer

Admin 入口：

- 搜索质量分析。
- 问答上下文调试。

Portal/Admin 通用入口：

- 跨库搜索。
- 智能问答。
- 王圻文档单文档追加式问答。
- 会话列表、删除和导出。

## Infrastructure Layer

- Elasticsearch 或等价检索适配。
- AI 回答生成客户端通过 AI 域 application 能力访问。
- 内容读取必须通过 Classics application 能力，不直接读 Classics 表。

## Data Ownership

Discovery 是 `discovery_*` 表的唯一写入方。搜索索引是派生读模型，不替代 Classics 内容真相源。

## Observability

- 记录查询词、识别意图、结果数量、点击、问答来源和失败原因。
- 管理员可查看问答调试上下文。

## Acceptance

- 搜索和问答不依赖知识图谱作为必需前置。
- 权限过滤发生在结果展示和问答上下文生成前。
