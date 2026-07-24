# Sample

Input variables:

- `document`: `甲，乙之子。`
- `constraints`: `只抽取明确亲属关系。`

Expected output:

```json
{"nodes":[{"name":"甲","evidence":"甲，乙之子"},{"name":"乙","evidence":"甲，乙之子"}],"relations":[{"source":"甲","relation":"父子","target":"乙","evidence":"甲，乙之子","confidence":"HIGH"}]}
```
