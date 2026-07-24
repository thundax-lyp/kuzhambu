# Sample

Input variables:

- `title`: `器用`
- `categoryPath`: `三才图会/器用`
- `sourceText`: `甲物用于礼，乙物用于乐。`
- `splitHint`: `按器物拆分。`

Expected output:

```json
{"items":[{"title":"甲物","content":"甲物用于礼。","reason":"围绕甲物形成独立用途描述"},{"title":"乙物","content":"乙物用于乐。","reason":"围绕乙物形成独立用途描述"}]}
```
