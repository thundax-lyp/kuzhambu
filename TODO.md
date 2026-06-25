# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Portal knowledge entry routing`：接入门户入口与首页跳转
    - 任务类型：设计转执行
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-READONLY.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/app.tsx`、`kuzhambu-apps/portal-web/src/pages/home/home-page.tsx`、`kuzhambu-apps/portal-web/src/pages/home/home-page.test.tsx`
    - 处理动作：接入 `/knowledge` 路由并在现有 Portal 首页增加入口
    - 验收点：Portal 首页能跳到 Knowledge 首页，`/knowledge` 可打开
    - 重要度：7/10

- [ ] `Portal knowledge home shell`：实现首页页面骨架
    - 任务类型：设计转执行
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-READONLY.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-page.tsx`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-page.css`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-page.test.tsx`
    - 处理动作：实现首页首屏布局、概览卡、快捷入口、最近更新区
    - 验收点：首页首屏只有主标题与搜索入口，下方可见统计卡与入口卡
    - 重要度：8/10

- [ ] `Portal knowledge home data adapter`：首页服务与类型
    - 任务类型：设计转执行
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-READONLY.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-service.ts`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-types.ts`
    - 处理动作：抽离首页页面服务与类型定义
    - 验收点：首页不依赖硬编码字符串，后续可平滑接 `/portal/knowledge/home`
    - 重要度：7/10

- [ ] `Portal knowledge atlas shell`：实现浏览页页面骨架
    - 任务类型：设计转执行
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-READONLY.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.tsx`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.css`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-page.test.tsx`
    - 处理动作：实现左中右三栏布局、关系画布区、详情栏
    - 验收点：页面能表达“筛选 - 浏览 - 详情”三层关系
    - 重要度：8/10

- [ ] `Portal knowledge atlas data adapter`：浏览页服务与类型
    - 任务类型：设计转执行
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-READONLY.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-service.ts`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-atlas-types.ts`
    - 处理动作：抽离浏览页页面服务与类型定义
    - 验收点：页面内容可先静态适配，后续可平滑接 `/portal/knowledge/atlas`
    - 重要度：7/10

- [ ] `Portal knowledge quality shell`：实现质量页页面骨架
    - 任务类型：设计转执行
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-READONLY.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-quality-page.tsx`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-quality-page.css`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-quality-page.test.tsx`
    - 处理动作：实现指标区、趋势区、问题卡片、来源表格
    - 验收点：页面能表达覆盖率、置信度、来源总数、待处理项
    - 重要度：8/10

- [ ] `Portal knowledge quality data adapter`：质量页服务与类型
    - 任务类型：设计转执行
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-READONLY.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-quality-service.ts`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-quality-types.ts`
    - 处理动作：抽离质量页页面服务与类型定义
    - 验收点：页面内容可先静态适配，后续可平滑接 `/portal/knowledge/quality`
    - 重要度：7/10

- [ ] `Portal knowledge shared visual baseline`：建立知识门户共享样式基线
    - 任务类型：设计转执行
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-READONLY.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/styles.css`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-page.css`
    - 处理动作：建立知识门户专属背景、卡片、边框、阴影与色彩基线
    - 验收点：视觉风格与 Discovery 明显区分，整体更偏博物馆导览
    - 重要度：8/10

- [ ] `Portal knowledge verify`：完成门户前端验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：本轮新增或修改的 `portal-web` 文件
    - 处理动作：运行 `format:check`、`lint`、`test`、`build`
    - 验收点：相关前端验证全部通过
    - 重要度：8/10

- [ ] `Portal knowledge cleanup`：清理 RUNBOOK 与效果图
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-READONLY.md`
    - 范围对象：`docs/30-designs/RUNBOOK-KNOWLEDGE-PORTAL-READONLY.md`、`docs/30-designs/assets/knowledge-portal-readonly/*`
    - 处理动作：任务结束时同步清理 RUNBOOK 与效果图
    - 验收点：不残留项目外或项目内孤立样稿
    - 重要度：6/10

## 待讨论项
