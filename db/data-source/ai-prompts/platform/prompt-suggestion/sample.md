# Sample

Input variables:

- `targetCapability`: `CLASSICS_SUMMARY`
- `currentPrompt`: `请摘要。`
- `variables`: `title, document`
- `failureSamples`: `输出经常补充背景常识。`

Expected output:

```json
{"suggestions":[{"problem":"约束过弱，无法限制编造背景","recommendation":"补充只依据输入内容总结的规则","impact":"降低无依据扩展"}]}
```
