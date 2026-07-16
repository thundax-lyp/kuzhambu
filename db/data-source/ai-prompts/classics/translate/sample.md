# Classics Translate Sample

```sh
curl -sS -X POST "$KUZHAMBU_AI_PRIMARY_BASE_URL/chat/completions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $KUZHAMBU_AI_PRIMARY_API_KEY" \
  -d '{
    "model": "CTYUN-bot-DeepSeek-V3.2-pro",
    "messages": [
      { "role": "system", "content": "你是古籍整理助手，负责把古文或文言文翻译成现代中文。" },
      { "role": "user", "content": "标题：示例\n原文：昔者伏羲氏之王天下也。\n请翻译为现代中文。" }
    ],
    "temperature": 0.2,
    "stream": false
  }'
```
