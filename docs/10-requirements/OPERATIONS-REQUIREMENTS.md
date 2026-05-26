# Operations Requirements

## Purpose

运维管理模块定义平台运行状态、统计分析、备份恢复、日志、健康检查和清理任务需求。

## Scope

覆盖：
- 运维仪表盘。
- 内容、翻译、配图、AI 调用等统计。
- 平台访问量、活跃用户、功能使用频率和热门内容统计。
- 周报和月报生成及导出。
- 备份和恢复。
- 日志查看和筛选。
- 健康检查和运行指标。
- 过期备份、过期分享、过期草稿和过期导出产物清理。
- 长任务和批量操作运行状态查看。

不覆盖：
- 业务内容编辑。
- AI 提示词和模型配置的具体管理。
- 搜索和问答业务结果生成。
- 文件资源扫描、孤立文件清理和底层存储巡检。


## Functional Requirements

- 必须提供运维仪表盘。
- 必须展示内容数量、翻译覆盖率、配图覆盖率和 AI 调用统计。
- 必须展示内容增长趋势、标签覆盖率变化和热门内容排行。
- 必须展示平台访问量、活跃用户数和功能使用频率。
- 必须支持统计结果图表化展示。
- 必须支持生成周报和月报，并导出为 PDF 或 HTML。
- 必须支持系统启动自动备份。
- 必须支持每 24 小时自动备份。
- 必须支持手动备份、备份列表查看和备份恢复。
- 必须支持日志查看、筛选和检索。
- 必须支持健康检查和运行指标查看。
- 必须支持过期备份、过期分享、过期草稿和过期导出产物清理。
- 必须支持查看长任务和批量操作的运行状态、成功数、失败数和失败原因。

## Business Rules

- 恢复数据前必须创建恢复前快照。
- 恢复期间应阻止新的写入操作。
- 备份保留期限必须为 30 天，超过期限的备份应自动清理。
- 导出产物是临时产物，不进入数据备份范围。
- 清理操作必须可追溯。
- 日志筛选必须支持按级别、模块、时间和关键词查询。
- 运行日志保留期限必须为 30 天，超过期限的日志应自动清理或归档。
- 统计数据展示必须遵守权限，不得向非管理员开放运维细节。
- 运维能力仅 admin 可用。

## Acceptance Criteria

- 管理员能触发手动备份并看到结果。
- 管理员能查看备份列表并发起恢复。
- 管理员能查看内容、访问、AI 调用和热门内容统计。
- 管理员能生成并导出周报或月报。
- 系统关键异常可在日志中追溯。
- 恢复失败时保留恢复前快照。
- 管理员能查看长任务和批量操作的运行状态。

## Related Documents

- [AUTH-REQUIREMENTS.md](./AUTH-REQUIREMENTS.md)：提供 admin 权限、用户状态和关键操作日志。
- [AI-CONFIG-PROMPT-REQUIREMENTS.md](./AI-CONFIG-PROMPT-REQUIREMENTS.md)：提供 AI 服务状态、模型检测和调用统计来源；本模块只做运维查看和报表。
- [SEARCH-REQUIREMENTS.md](./SEARCH-REQUIREMENTS.md)：提供搜索行为日志和点击数据用于质量分析。
- [SHARING-REQUIREMENTS.md](./SHARING-REQUIREMENTS.md)：提供过期分享清理对象和分享访问统计来源。
- [SANCAI-KNOWLEDGE-REQUIREMENTS.md](./SANCAI-KNOWLEDGE-REQUIREMENTS.md)、[WANGQI-DOCUMENT-REQUIREMENTS.md](./WANGQI-DOCUMENT-REQUIREMENTS.md)、[MING-CUSTOMS-REQUIREMENTS.md](./MING-CUSTOMS-REQUIREMENTS.md)：提供内容统计、导出产物清理对象和长任务状态来源。

## Open Items

无
