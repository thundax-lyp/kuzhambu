# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `src/pages/ai/capability-mappings/capability-mappings-page.tsx`：迁移能力映射筛选表单
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-FORM-USEFORM-MIGRATION.md`
    - 范围对象：`src/pages/ai/capability-mappings/capability-mappings-page.tsx`
    - 处理动作：将筛选区改为 `Form.useForm` + `KuzhambuFormItem name` 驱动查询状态。
    - 验收点：scope、capability、enabled 筛选行为保持不变，筛选区 `KuzhambuForm` 不再缺少 `form`。
    - 重要度：6/10

- [ ] `kuzhambu-apps/admin-web`：复扫表单规则并补齐验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-FORM-USEFORM-MIGRATION.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages`
    - 处理动作：运行 RUNBOOK 复扫命令并处理剩余命中项。
    - 验收点：pages direct AntD `Form.Item` 为 0，跨文件 `KuzhambuFormItem` 为 0，业务 `KuzhambuForm` 无 `form=` 的剩余项完成分类。
    - 重要度：9/10

## 待审阅任务项

## 待讨论项
