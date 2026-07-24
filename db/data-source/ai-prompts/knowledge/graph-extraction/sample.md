# Sample

Input variables:

- `scope`: `三才图会/器用`
- `documents`: `甲器用于祭礼。`

Expected output:

```json
{"nodes":[{"name":"甲器","type":"OBJECT","evidence":"甲器用于祭礼"},{"name":"祭礼","type":"CONCEPT","evidence":"甲器用于祭礼"}],"edges":[{"source":"甲器","relation":"用于","target":"祭礼","evidence":"甲器用于祭礼"}]}
```
