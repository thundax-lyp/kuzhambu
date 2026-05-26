# Naming And Placement Rules

## Purpose

本文档固定 kuzhambu 的命名和文件归属规则，避免新增代码或文档时产生多套口径。

## Document Placement

- 仓库级入口放在根目录，例如 `README.md`、`AGENTS.md`、`LICENSE`。
- AI 文档路由放在 `docs/AGENTS.md`。
- 稳定治理规则放在 `docs/00-governance/`。
- 需求文档放在 `docs/10-requirements/`，命名为 `*-REQUIREMENTS.md`。
- 接口、协议和配置契约放在 `docs/20-interfaces/`。
- 专项设计和一次性 `RUNBOOK-*` 放在 `docs/30-designs/`。
- 上线、发布和运维准备放在 `docs/40-readiness/`。
- 人类阅读材料、项目叙事和临时参考材料放在 `docs/60-human/`。

## Code Placement

- 当前尚未建立代码目录。
- Java 代码落地时优先使用 `src/main/java/`、`src/test/java/`、`src/main/resources/`。
- 新增模块时，必须先说明模块职责、入口、测试位置和构建命令。
- 模块拥有自己的规则时，在模块根目录新增 `AGENTS.md`。

## Naming Rules

- Java 类使用 `PascalCase`。
- 方法和字段使用 `camelCase`。
- 常量使用 `UPPER_SNAKE_CASE`。
- 包名使用小写英文，例如 `com.github.thundax.kuzhambu`。
- 文档文件名使用大写英文和 `-`。
- `docs/60-human/` 的临时参考材料可保留原始文件名；抽取为正式工程文档时必须改为大写英文命名。

## Open Items

无
