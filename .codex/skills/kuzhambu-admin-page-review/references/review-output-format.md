# Review Output Format

Use this structure for `kuzhambu-admin-page-review` results:

```text
结论
- 页面整体角色判断
- UX/UI 结论: Pass / Needs work / Blocked
- 最大边界问题
- 最大用户体验问题
- 是否建议重构

组件归类
- <component>: <composition/capability/expression/primitive> — <reason>

主要问题
[P1/P2/P3] <title>
- 位置: <file/component>
- 触发: <concrete condition>
- 用户影响: <observable impact to task completion, clarity, correctness, or trust>
- 项目规则: <ADMIN-WEB-RULES/UI-RULES pattern or local component contract>
- 问题: <why the boundary/ownership/UX pattern is wrong>
- 建议: <where the logic/data/interface/UI structure should move or how it should render>
- 抽象判断: <ownership/complexity/reuse reason, if an abstraction is involved>

建议重构顺序
1. <step>
2. <step>

不建议改动
- <stable boundary or intentional exception>

验证建议
- <narrow validation commands or manual checks>
```

## Severity Guide

- **P1**: likely user-visible breakage, incorrect business behavior, or hard-to-maintain boundary that blocks safe changes.
- **P2**: concrete responsibility leak, duplicated ownership, unstable UX contract, or unstable interface likely to cause bugs.
- **P3**: maintainability/readability concern. Include only when useful for planned refactoring.

If no actionable findings exist, say so and list residual risks or validation gaps.

Findings must be actionable. Omit unsupported preferences unless tied to a specific project rule, concrete user task, or observable failure.
