# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `kuzhambu-logo`：迁移 `sandwich-logo`
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/UI-RULES.md`
    - 范围对象：`src/components/sandwich-logo.tsx`、`public/sandwich-logo.svg`、`src/layouts/admin-layout.tsx`
    - 处理动作：将 logo 组件、静态资产和直接调用方改为 `KuzhambuLogo` / `kuzhambu-logo`。
    - 验收点：logo 组件和静态资源无旧品牌命名残留，admin-web lint 通过。
    - 重要度：8/10

- [ ] `admin-web-brand-copy`：收口剩余品牌文案
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/UI-RULES.md`
    - 范围对象：`src/layouts/admin-layout.tsx`、`src/pages/auth/login/login-page.tsx`、`src/app.test.tsx`、`e2e/layout/admin-layout.spec.ts`、`README.md`、`index.html`
    - 处理动作：将剩余展示文案从 sandwich 语义改为 kuzhambu 目标品牌文案。
    - 验收点：展示层无旧品牌文案残留，admin-web lint 和 test 通过。
    - 重要度：8/10

- [ ] `admin-web-validation`：执行 admin-web 迁移验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-apps/admin-web/`
    - 处理动作：运行 workspace 下 admin-web 的 format、lint、test 和 build 验证。
    - 验收点：`format:check`、`lint`、`test`、`build` 通过，或记录明确阻塞原因。
    - 重要度：10/10

## 待讨论项

- [ ] kuzhambu admin API 契约是否沿用 `/admin-api/api`
    - 任务类型：待讨论项
    - 关联任务：`kuzhambu-apps/admin-web`
    - 决策要求：确认 `ADMIN_API_BASE_URL`、`ADMIN_CLIENT_ID`、token header 和响应 code 是否保持兼容。
    - 重要度：9/10
