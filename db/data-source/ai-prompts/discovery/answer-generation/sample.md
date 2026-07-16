# Discovery Answer Generation Sample

```sh
curl -sS -X POST "$KUZHAMBU_AI_PRIMARY_BASE_URL/chat/completions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $KUZHAMBU_AI_PRIMARY_API_KEY" \
  -d '{
    "model": "CTYUN-bot-DeepSeek-V3.2-pro",
    "messages": [
      { "role": "system", "content": "你是古籍知识发现的回答助手，负责根据检索来源生成可信中文回答。" },
      { "role": "user", "content": "用户问题：世系图是什么？\n检索来源：[{\"title\":\"上古帝王及世系图\",\"summary\":\"记录上古帝王相承关系。\"}]\n请返回 JSON。" }
    ],
    "temperature": 0.2,
    "stream": false
  }'
```
