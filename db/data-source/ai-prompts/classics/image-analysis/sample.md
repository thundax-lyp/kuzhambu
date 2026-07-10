# Classics Image Analysis Sample

```sh
curl -sS -X POST "$KUZHAMBU_AI_PRIMARY_BASE_URL/chat/completions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $KUZHAMBU_AI_PRIMARY_API_KEY" \
  -d '{
    "model": "CTYUN-CX-Qwen3.5-397B-A17B",
    "messages": [
      { "role": "system", "content": "你是古籍图像解读助手，负责描述和解释古籍插图、版刻图、器物图或人物图。" },
      { "role": "user", "content": "标题：器用图\n图像描述：画面中有一件圆腹器物，上方有盖。\n请给出图像解读。" }
    ],
    "temperature": 0.2,
    "stream": false
  }'
```
