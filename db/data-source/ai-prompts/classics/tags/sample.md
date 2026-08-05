# Classics Tags Sample

```sh
curl -sS -X POST "$KUZHAMBU_AI_PRIMARY_BASE_URL/chat/completions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $KUZHAMBU_AI_PRIMARY_API_KEY" \
  -d '{
    "model": "CTYUN-bot-DeepSeek-V3.2-pro",
    "messages": [
      { "role": "system", "content": "你是古籍知识组织助手，负责从古籍条目中抽取稳定、可复用的中文标签。只输出可被 JSON.parse 直接解析的原始 JSON 对象，不要输出 Markdown、代码块或解释。" },
      { "role": "user", "content": "标题：上古帝王及世系图\n内容：伏羲、神农、黄帝相承之世系。\n请抽取 3 到 8 个中文标签，只返回 {\"tags\":[\"标签1\",\"标签2\",\"标签3\"]} 格式的原始 JSON。" }
    ],
    "temperature": 0.1,
    "stream": false
  }'
```
