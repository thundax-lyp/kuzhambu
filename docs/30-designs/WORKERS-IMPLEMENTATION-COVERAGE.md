## Classics usecase

| domain | contentType | capability | operation | workerPath | stream | workerEntry | status | note |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| classics | SANCAI_ENTRY | translate | CLASSICS_SANCAI_TRANSLATE | /internal/ai/classics/sancai/translate | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | translate | CLASSICS_SANCAI_TRANSLATE_BATCH_ITEM | /internal/ai/classics/sancai/translate-batch-item | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | summary | CLASSICS_SANCAI_SUMMARY | /internal/ai/classics/sancai/summary | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | tags | CLASSICS_SANCAI_TAGS | /internal/ai/classics/sancai/tags | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | qa | CLASSICS_SANCAI_QA | /internal/ai/classics/sancai/qa | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | image_analysis | CLASSICS_SANCAI_IMAGE_ANALYSIS | /internal/ai/classics/sancai/image-analysis | true | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | fusion | CLASSICS_SANCAI_FUSION | /internal/ai/classics/sancai/fusion | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | visual | CLASSICS_SANCAI_VISUAL_DESCRIPTION | /internal/ai/classics/sancai/visual-description | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | image_gen | CLASSICS_SANCAI_IMAGE_GEN | /internal/ai/classics/sancai/image-gen | true | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | SANCAI_ENTRY | split | CLASSICS_SANCAI_SPLIT | /internal/ai/classics/sancai/split | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | WANGQI_DOCUMENT | summary | CLASSICS_WANGQI_SUMMARY | /internal/ai/classics/wangqi/summary | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | WANGQI_DOCUMENT | tags | CLASSICS_WANGQI_TAGS | /internal/ai/classics/wangqi/tags | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | WANGQI_DOCUMENT | qa | CLASSICS_WANGQI_QA | /internal/ai/classics/wangqi/qa | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | MING_CUSTOMS | summary | CLASSICS_MING_CUSTOMS_SUMMARY | /internal/ai/classics/ming-customs/summary | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | MING_CUSTOMS | tags | CLASSICS_MING_CUSTOMS_TAGS | /internal/ai/classics/ming-customs/tags | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |
| classics | MING_CUSTOMS | qa | CLASSICS_MING_CUSTOMS_QA | /internal/ai/classics/ming-customs/qa | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | 路由与 graph 注册器协同提供 usecase 调用 |

## Discovery usecase

| domain | contentType | capability | operation | workerPath | stream | workerEntry | status | note |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| discovery | - | query_understanding | DISCOVERY_QUERY_UNDERSTANDING | /internal/ai/discovery/query-understanding | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | usecase 已在 registry 预期列表中注册 |
| discovery | - | query_understanding | DISCOVERY_QUERY_REWRITE | /internal/ai/discovery/query-rewrite | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | usecase 已在 registry 预期列表中注册 |
| discovery | - | answer_generation | DISCOVERY_ANSWER_GENERATION | /internal/ai/discovery/answer-generation | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | usecase 已在 registry 预期列表中注册 |
| discovery | - | answer_generation | DISCOVERY_ANSWER_GENERATION_STREAM | /internal/ai/discovery/answer-generation/stream | true | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | usecase 已在 registry 预期列表中注册 |

## Knowledge usecase

| domain | contentType | capability | operation | workerPath | stream | workerEntry | status | note |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| knowledge | - | relation_extraction | KNOWLEDGE_RELATION_EXTRACTION | /internal/ai/knowledge/relation-extraction | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | usecase 已在 registry 预期列表中注册 |
| knowledge | - | knowledge_graph | KNOWLEDGE_GRAPH_EXTRACTION | /internal/ai/knowledge/graph-extraction | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | usecase 已在 registry 预期列表中注册 |
| knowledge | - | lineage_extraction | KNOWLEDGE_LINEAGE_EXTRACTION | /internal/ai/knowledge/lineage-extraction | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | usecase 已在 registry 预期列表中注册 |
| knowledge | - | tags | KNOWLEDGE_TAG_EXTRACTION | /internal/ai/knowledge/tag-extraction | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | usecase 已在 registry 预期列表中注册 |

## Platform usecase

| domain | contentType | capability | operation | workerPath | stream | workerEntry | status | note |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| platform | - | prompt_suggestion | PLATFORM_PROMPT_SUGGESTION | /internal/ai/platform/prompt-suggestion | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | usecase 已在 registry 预期列表中注册 |
| platform | - | version_summary | PLATFORM_VERSION_SUMMARY | /internal/ai/platform/version-summary | false | kuzhambu_workers.ai.graph_registry:GraphRegistry.invoke | implemented | usecase 已在 registry 预期列表中注册 |
