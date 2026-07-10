# Discovery Query Understanding Sample

```sh
curl -sS -X POST "$KUZHAMBU_AI_PRIMARY_BASE_URL/chat/completions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $KUZHAMBU_AI_PRIMARY_API_KEY" \
  -d '{
    "model": "CTYUN-bot-DeepSeek-V3.2-pro",
    "messages": [
      { "role": "system", "content": "你是古籍知识发现的查询理解助手，负责把用户查询转换成稳定的检索意图。" },
      { "role": "user", "content": "用户查询：世系图\n请返回 JSON。" }
    ],
    "temperature": 0.1,
    "stream": false
  }'
```
