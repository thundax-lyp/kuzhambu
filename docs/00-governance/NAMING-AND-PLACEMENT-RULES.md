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

- Java 后端模块使用 `kuzhambu-*` 前缀。
- Java 代码使用 `src/main/java/`、`src/test/java/`、`src/main/resources/`。
- Java 后端工程组放在 `kuzhambu-servers/`。
- 后端通用能力放在 `kuzhambu-servers/common/`，并按 `core`、`web`、`security`、`openapi`、`mybatis`、`mysql`、`redis`、`cache`、`mq`、`elasticsearch`、`oss`、`test` 拆分。
- 业务域规则和业务服务放在 `kuzhambu-servers/biz/`。
- 技术适配放在 `kuzhambu-servers/infra/`。
- 框架业务域使用 `kuzhambu-biz-core` 和 `kuzhambu-infra-core`，包含 menu、user、role 等基础管理对象。
- 知识图谱业务域使用 `kuzhambu-biz-knowledge-graph` 和 `kuzhambu-infra-knowledge-graph`，Java 包名使用 `knowledgegraph`。
- `biz` 和 `infra` 内部按业务域分包，不使用 `infra/shared`。
- `biz/kuzhambu-biz-<domain>/` 内部使用 `application/` 和 `domain/` 分层包，不建立独立 application Maven 模块。
- `infra/kuzhambu-infra-<domain>/` 内部按需使用 `persistence/`、`mapper/`、`repository/`、`assembler/`、`client/` 包。
- `repository/` 放业务域持久化端口的实现，命名为 `*Repository`，面向聚合读写，不按数据库表裸访问命名。
- `mapper/` 放 MyBatis 数据库访问对象，命名为 `*Mapper`，只表达 DB 表读写实现细节。
- 持久化转换类命名为 `*PersistenceAssembler`，放在对应业务域 infra 子工程的 `assembler/` 包。
- 跨业务域技术能力使用 `common-*` 模块承载。
- 后台接口入口服务放在 `kuzhambu-servers/interfaces/kuzhambu-admin-api/`。
- 前台接口入口服务放在 `kuzhambu-servers/interfaces/kuzhambu-portal-api/`。
- 接口服务内部按业务域分包，不使用额外 `adminapi` 或 `portal` 包层级。
- 接口服务配置文件固定为各自 `src/main/resources/application.yml`，不得新增 `application-local.yml`、`application-test.yml` 或 `application-docker.yml`。
- Flyway 初始化脚本放在 `kuzhambu-servers/interfaces/kuzhambu-admin-api/src/main/resources/db/migration/`。
- ULID 生成和业务 ID 基础类型放在 `kuzhambu-servers/common/kuzhambu-common-core/src/main/java/com/github/thundax/kuzhambu/common/core/id/`。
- 产出 jar 的 Maven 子工程 leaf 目录名必须与 `artifactId` 保持一致。
- 分组聚合 POM 可以放在分组目录根部，例如 `common/pom.xml`、`biz/pom.xml`、`infra/pom.xml`。
- 前端应用统一放在 `kuzhambu-apps/`。
- 后台前端固定放在 `kuzhambu-apps/admin-web/`，技术栈为 React、Vite 和 Ant Design。
- 前台前端固定放在 `kuzhambu-apps/portal-web/`，技术栈为 React、Vite、shadcn/ui、Tailwind CSS、Zustand、TanStack Query、TypeScript、ESLint 和 Prettier。
- Python worker 能力支撑代码统一放在 `kuzhambu-workers/`。
- Python worker 本地虚拟环境固定为 `kuzhambu-workers/.venv/`。
- 新增模块时，必须先说明模块职责、入口、测试位置和构建命令。
- 模块拥有自己的规则时，在模块根目录新增 `AGENTS.md`。

## Naming Rules

- Java 类使用 `PascalCase`。
- 方法和字段使用 `camelCase`。
- 常量使用 `UPPER_SNAKE_CASE`。
- 包名使用小写英文，例如 `com.github.thundax.kuzhambu`。
- 文档文件名使用大写英文和 `-`。
- `docs/60-human/` 的临时参考材料可保留原始文件名；抽取为正式工程文档时必须改为大写英文命名。
