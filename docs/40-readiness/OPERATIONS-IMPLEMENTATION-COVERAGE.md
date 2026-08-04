# Operations Implementation Coverage

## Status

- 当前状态：已完成
- 覆盖范围：运维仪表盘、跨域 summary、报表、备份恢复、清理任务、健康检查、健康告警、长任务、运维入口。
- 真相源：`docs/10-requirements/OPERATIONS-REQUIREMENTS.md`、本文件。

## Completion Summary

- Dashboard 已接入 Classics、AI、Discovery、Knowledge 的真实 summary，不再使用稳定空值占位。
- Dashboard 已支持周期、趋势、排行、健康摘要、告警、运维入口和按权限裁剪。
- 报表已完成周报/月报生成、Worker 渲染、Storage 产物、分页、详情、下载代理、运行中轮询和失败展示。
- 备份恢复已完成手动备份、启动备份、每日自动备份、恢复前快照、真实恢复、恢复演练和恢复期间 Web 写阻断。
- 清理任务已完成真实执行、调度、台账、明细、失败原因和七类目标：过期备份、导出、分享、草稿、报表、健康检查、长任务。
- 健康检查已完成本地探针、HTTP 外部探针、健康摘要、明细、趋势和诊断 JSON。
- 健康告警已完成生成、确认、恢复、自动恢复动作和失败来源联动。
- 运维入口已完成系统日志和审计日志跳转、权限门禁和页面验证。

## Open Items

- 无当前需求阻塞项。
- 后续如新增调度报表、外部告警系统或更多自动恢复动作，应作为新需求扩展。

## Validation Evidence

- Operations 报表、cleanup、health、backup/restore 和 dashboard 已存在 application / interface / admin-web 定向测试。
- `db/data-source/system.json` 已对齐 Operations 菜单与权限，导入时生成到 `build/seed-sql/system.sql`。
- `.env.example`、`deploy/.env.example` 和 admin-starter 配置已同步 Operations 运行时变量。

## Requirement Coverage Matrix

| 子域 | 需求范围 | 状态 | 说明 |
| --- | --- | --- | --- |
| Dashboard | 跨域聚合展示 | 已完成 | 内容、AI、搜索、问答、标签、趋势、排行已接入真实 summary |
| Dashboard | 权限裁剪 | 已完成 | 后端 summary 入参和前端图表/入口均按权限裁剪 |
| 报表 | 周报/月报 | 已完成 | 生成、分页、详情、状态、失败、下载代理和 Admin 页面已闭环 |
| 备份恢复 | 备份 | 已完成 | 手动、启动、每日自动、台账和脚本结果回写已完成 |
| 备份恢复 | 恢复 | 已完成 | `REAL / DRILL`、恢复前快照、写阻断和长任务追踪已完成 |
| 清理任务 | 真实执行和调度 | 已完成 | 七类清理目标、明细、失败原因和自动/人工来源已完成 |
| 健康检查 | 探针、摘要、趋势 | 已完成 | 本地探针、HTTP 探针、细分页、诊断 JSON 已完成 |
| 健康告警 | 告警和恢复 | 已完成 | 生成、确认、恢复、自动动作和失败来源联动已完成 |
| 长任务 | 台账记录 | 已完成 | 备份、恢复、清理、健康异常来源均可追踪 |
| 运维入口 | 系统日志和审计 | 已完成 | Dashboard 入口、权限门禁和跳转已完成 |
