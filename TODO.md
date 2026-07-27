# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `src/pages/classics/sancai/components/sancai-category-panel/sancai-category-edit-modal.tsx`：迁移三才门类编辑弹窗表单
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-FORM-USEFORM-MIGRATION.md`
    - 范围对象：`src/pages/classics/sancai/components/sancai-category-panel/sancai-category-edit-modal.tsx`
    - 处理动作：将门类标题和门类类型改为 `Form.useForm` + `KuzhambuFormItem name` 托管。
    - 验收点：保存读取 `form.validateFields()` 结果，文件内不再用整表 `useState` 管字段值。
    - 重要度：8/10

- [ ] `src/pages/classics/sancai/components/sancai-volume-panel/sancai-volume-edit-modal.tsx`：迁移三才卷目编辑弹窗表单
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-FORM-USEFORM-MIGRATION.md`
    - 范围对象：`src/pages/classics/sancai/components/sancai-volume-panel/sancai-volume-edit-modal.tsx`
    - 处理动作：将卷目标题、所属门类和卷目类型改为 `Form.useForm` + `KuzhambuFormItem name` 托管。
    - 验收点：保存读取 `form.validateFields()` 结果，`fallbackCategoryId` 只参与初始值回填。
    - 重要度：8/10

- [ ] `src/pages/ai/ai-models/components/ai-model-edit-drawer.tsx`：迁移 AI 模型编辑抽屉表单
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-FORM-USEFORM-MIGRATION.md`
    - 范围对象：`src/pages/ai/ai-models/components/ai-model-edit-drawer.tsx`
    - 处理动作：将模型字段改为 `Form.useForm` + `KuzhambuFormItem name` 托管。
    - 验收点：保存仍保留必填校验、JSON 校验、trim、`normalizeJsonText` 和编辑态空 API key 不更新语义。
    - 重要度：8/10

- [ ] `src/pages/system/user/components/user-edit-drawer/user-edit-drawer.tsx`：迁移用户编辑抽屉表单
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-FORM-USEFORM-MIGRATION.md`
    - 范围对象：`src/pages/system/user/components/user-edit-drawer/user-edit-drawer.tsx`
    - 处理动作：将用户字段改为 `Form.useForm` + `KuzhambuFormItem name` 托管。
    - 验收点：创建/编辑/关闭重开的回填语义正确，头像上传、角色查询、部门树和选项派生状态不进入表单。
    - 重要度：8/10

- [ ] `src/pages/classics/wangqi/components/wangqi-document-edit-drawer`：收敛王圻文档基础表单边界
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-FORM-USEFORM-MIGRATION.md`
    - 范围对象：`src/pages/classics/wangqi/components/wangqi-document-edit-drawer/wangqi-document-edit-drawer.tsx`、`src/pages/classics/wangqi/components/wangqi-document-edit-drawer/wangqi-document-basic-section/wangqi-document-basic-section.tsx`、`src/pages/classics/wangqi/components/wangqi-document-edit-drawer/wangqi-document-basic-section/wangqi-document-summary-field/wangqi-document-summary-field.tsx`
    - 处理动作：将 `WangqiDocumentBasicSection` 的 `KuzhambuFormItem` 回收到拥有 `KuzhambuForm` 的文件，并移除 summary field 中的 direct AntD `Form.Item`。
    - 验收点：`wangqi-document-basic-section.tsx` 不再出现 `KuzhambuFormItem`，`wangqi-document-summary-field.tsx` 不再出现 `Form.Item`。
    - 重要度：9/10

- [ ] `src/pages/ai/prompts/components/prompt-edit-drawer.tsx`：清理提示词能力字段 direct Form.Item
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-FORM-USEFORM-MIGRATION.md`
    - 范围对象：`src/pages/ai/prompts/components/prompt-edit-drawer.tsx`
    - 处理动作：将能力字段的 `name` 和校验规则提升到外层 `KuzhambuFormItem`。
    - 验收点：文件中不再出现 direct AntD `Form.Item`，能力选择仍由表单字段 `capability` 托管。
    - 重要度：7/10

- [ ] `src/pages/auth/login/login-page.tsx`：迁移登录页表单组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-FORM-USEFORM-MIGRATION.md`
    - 范围对象：`src/pages/auth/login/login-page.tsx`
    - 处理动作：将登录页 AntD `Form` / `Form.Item` 改为 `Form.useForm` + `KuzhambuForm` / `KuzhambuFormItem`。
    - 验收点：账号、密码、验证码校验和 `onFinish` 登录提交行为保持不变，文件中不再出现 direct AntD `Form.Item`。
    - 重要度：7/10

- [ ] `src/pages/knowledge/taxonomy/components/tag-alias-create-field`：迁移标签别名新增为独立表单
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-FORM-USEFORM-MIGRATION.md`
    - 范围对象：`src/pages/knowledge/taxonomy/components/tag-alias-create-field/tag-alias-create-field.tsx`、`src/pages/knowledge/taxonomy/components/tag-edit-drawer.tsx`
    - 处理动作：将标签别名新增改为独立 `KuzhambuForm`，父级只接收子表单提交值。
    - 验收点：`tag-alias-create-field.tsx` 不再出现 direct AntD `Form.Item`，别名创建成功后只重置子表单字段。
    - 重要度：7/10

- [ ] `src/pages/classics/sancai/components/sancai-entry-edit-drawer/sancai-entry-basic-section`：清理三才 AI 文本弹窗 direct Form.Item
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-FORM-USEFORM-MIGRATION.md`
    - 范围对象：`src/pages/classics/sancai/components/sancai-entry-edit-drawer/sancai-entry-basic-section/sancai-entry-summary-text-field/sancai-entry-summary-modal.tsx`、`src/pages/classics/sancai/components/sancai-entry-edit-drawer/sancai-entry-basic-section/sancai-entry-translation-text-field/sancai-entry-translation-modal.tsx`
    - 处理动作：将摘要和译文弹窗中的 AntD `Form` / `Form.Item` 展示外壳改为普通布局。
    - 验收点：两个弹窗文件中不再出现 direct AntD `Form.Item`，原文、当前文本和 AI 候选文本展示/编辑行为保持不变。
    - 重要度：7/10

- [ ] `src/pages/classics/wangqi/components/wangqi-document-edit-drawer/wangqi-document-edit-drawer.tsx`：清理王圻摘要弹窗 direct Form.Item
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-FORM-USEFORM-MIGRATION.md`
    - 范围对象：`src/pages/classics/wangqi/components/wangqi-document-edit-drawer/wangqi-document-edit-drawer.tsx`
    - 处理动作：将摘要弹窗中的 AntD `Form` / `Form.Item` 展示外壳改为普通布局。
    - 验收点：文件中不再出现 direct AntD `Form.Item`，当前摘要、AI 摘要和正文展示/编辑行为保持不变。
    - 重要度：7/10

- [ ] `src/pages/classics/sancai/components/sancai-entry-edit-drawer/sancai-entry-visual-section/sancai-entry-visual-section.tsx`：迁移三才视觉资产表单
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ADMIN-WEB-FORM-USEFORM-MIGRATION.md`
    - 范围对象：`src/pages/classics/sancai/components/sancai-entry-edit-drawer/sancai-entry-visual-section/sancai-entry-visual-section.tsx`
    - 处理动作：将来源图选择和视觉资产可编辑字段改为 section 内 `Form.useForm` + `KuzhambuFormItem name` 托管。
    - 验收点：视觉资产提交由 `visualAssetForm.validateFields()` 生成 patch，任务流、记录表格和预览态仍不进入表单。
    - 重要度：8/10

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
