# Sample

Input variables:

- `taxonomyContext`: `已有标签：礼制、器物。`
- `document`: `甲器用于祭礼。`

Expected output:

```json
{"tags":[{"name":"礼制","reason":"文本出现祭礼用途"},{"name":"器物","reason":"文本描述甲器"}]}
```
