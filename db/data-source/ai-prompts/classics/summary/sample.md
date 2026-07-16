# Classics Summary Sample

```sh
curl -sS -X POST "$KUZHAMBU_AI_PRIMARY_BASE_URL/chat/completions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $KUZHAMBU_AI_PRIMARY_API_KEY" \
  -d '{
    "model": "CTYUN-bot-DeepSeek-V3.2-pro",
    "messages": [
      { "role": "system", "content": "你是古籍整理助手，负责为古籍条目生成可直接展示的中文摘要。" },
      { "role": "user", "content": "内容类型：SANCAI_ENTRY\n标题：上古帝王及世系图\n分类路径：人物/帝王\n原文：伏羲、神农、黄帝相承之世系。\n请生成一段中文摘要。" }
    ],
    "temperature": 0.2,
    "stream": false
  }'
```
