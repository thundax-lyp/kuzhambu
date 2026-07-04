# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `classics-sancai-image-worker-export`：同步 worker Classics 导出图片元数据输出
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/render/classics_export.py`、`kuzhambu-workers/src/kuzhambu_workers/render/templates/classics_export.html`、`kuzhambu-workers/tests/test_classics_export.py`
    - 处理动作：JSON 保留 `items[].images[]`，HTML 渲染图片元数据，CSV 不展开多图为多行。
    - 验收点：导出不丢失图片元数据且 CSV 行数仍等于内容条目数。
    - 重要度：7/10

- [ ] `classics-sancai-image-interface-doc`：更新三才内容版本快照接口文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`docs/20-interfaces/CLASSICS-CONTENT-VERSION-SNAPSHOT-INTERFACE.md`
    - 处理动作：将 `SANCAI_ENTRY.images` 明确为按 `priority ASC` 输出的多图列表，并说明 `currentUsed` 标识当前图。
    - 验收点：接口文档与 Java snapshot 输出字段一致，且不承诺新增数据库字段。
    - 重要度：9/10

- [ ] `classics-sancai-image-coverage-runbook-closeout`：更新覆盖状态并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GOVERNANCE-CLOSEOUT.md`、`TODO.md`
    - 处理动作：将三才多图、缩略预览、放大浏览、原图删除和图片列表管理标记为已完成，并在 PR 收口前删除已完成 RUNBOOK 和清空对应 TODO。
    - 验收点：Implementation Coverage 只记录完成事实和剩余缺口，RUNBOOK 已删除，TODO.md 不保留已完成任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
