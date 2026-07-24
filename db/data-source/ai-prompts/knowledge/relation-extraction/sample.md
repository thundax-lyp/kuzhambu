# Sample

Input variables:

- `document`: `甲器用于祭礼。`
- `context`: `已有实体：甲器、祭礼。`

Expected output:

```json
{"relations":[{"subject":"甲器","predicate":"用于","object":"祭礼","evidence":"甲器用于祭礼"}]}
```
