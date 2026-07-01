## Summary

-

## Scope

-

## Verification

- [ ] PR workflow: governance files
- [ ] PR workflow: servers changed -> `mvn -q clean`
- [ ] PR workflow: servers changed -> `mvn -q spotless:check`
- [ ] PR workflow: servers changed -> `mvn -q checkstyle:check`
- [ ] PR workflow: servers changed -> `mvn -q test`
- [ ] PR workflow: workers changed -> `ruff format --check .`
- [ ] PR workflow: workers changed -> `ruff check .`
- [ ] PR workflow: workers changed -> `pytest`
- [ ] PR workflow: apps changed -> frontend package manifests
- [ ] PR workflow: apps changed -> `npm install`
- [ ] PR workflow: apps changed -> `npm run format:check`
- [ ] PR workflow: apps changed -> `npm run lint`
- [ ] PR workflow: apps changed -> `npm test`
- [ ] PR workflow: db changed -> SQL seed checks

## Documentation And TODO

- [ ] 文档已同步，或本 PR 不涉及文档口径变化。
- [ ] `TODO.md` 已清空或收窄为剩余未完成任务。
- [ ] `RUNBOOK` 已清理，或仍有明确剩余用途。

## Risks

-
