# Classics QA Sample

```sh
curl -sS -X POST "$KUZHAMBU_AI_PRIMARY_BASE_URL/chat/completions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $KUZHAMBU_AI_PRIMARY_API_KEY" \
  -d '{
    "model": "CTYUN-bot-DeepSeek-V3.2-pro",
    "messages": [
      { "role": "system", "content": "你是古籍问答生成助手，负责根据给定内容生成可用于知识问答的中文问答对。" },
      { "role": "user", "content": "标题：上古帝王及世系图\n内容：伏羲、神农、黄帝相承之世系。\n请生成 3 到 5 个问答对，返回 JSON。" }
    ],
    "temperature": 0.2,
    "stream": false
  }'
```
