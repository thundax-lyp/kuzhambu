# Classics Image Generation Sample

```sh
curl -sS -X POST "$KUZHAMBU_AI_TEXT2IMAGE_BASE_URL/images/generations" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $KUZHAMBU_AI_TEXT2IMAGE_API_KEY" \
  -d '{
    "model": "doubao-seedream-5-0-pro-260628",
    "prompt": "古籍插图风格，博物馆图录质感，描绘一件圆腹带盖器物，米白宣纸背景，细线刻画，克制配色",
    "response_format": "url",
    "size": "2K",
    "stream": false,
    "watermark": true
  }'
```
