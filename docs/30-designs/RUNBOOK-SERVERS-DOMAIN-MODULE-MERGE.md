# RUNBOOK Servers Domain Module Merge

## Purpose

本文档记录 Java servers 从当前横向分层模块结构迁移为业务域内聚模块结构的执行手册。

本任务属于跨模块重构。任务关闭时，应删除本文档及残留引用。

## Background

当前 `kuzhambu-servers/` 以 `biz/`、`infra/`、`interfaces/` 横向分层组织，并按细粒度需求文件一一拆分业务模块。该结构导致模块数量过多、单模块能力过薄、跨模块协作成本偏高。

目标结构改为 `common`、`biz`、`starter` 三个后端工程组。`biz` 下以业务域为一级组织单元，每个业务域内部使用 `interface`、`application`、`domain`、`infra` 分层。`interface` 内通过 package 区分 `admin` 和 `portal` 入口。原 `interfaces` 工程组调整为和 `biz` 并列的 `starter` 工程组，负责选择启动后台或前台应用。

## Scope

覆盖：

- 调整 Java servers Maven 模块结构。
- 合并现有细粒度 `biz` 子模块。
- 合并现有细粒度 `infra` 子模块。
- 将 admin 和 portal HTTP 入口移动到各业务域 `interface` 层。
- 将原 `interfaces` 工程组调整为 `starter` 工程组。
- 归并需求文档和设计文档的业务域口径。
- 归并 `db/schema` 和 `db/data` SQL 文件的业务域口径。
- 同步 Java servers 架构治理文档。
- 同步构建、验证脚本和必要的 PR 文档入口。

不覆盖：

- 改变业务需求范围。
- 重写业务逻辑。
- 调整前端工程结构。
- 调整 Python workers 工程结构。
- 数据库表字段语义重设计。
- 微服务化拆分。

## Target Domains

### system

合并来源：

- `kuzhambu-biz-core`
- `kuzhambu-biz-auth`
- `kuzhambu-biz-audit`
- `kuzhambu-infra-core`
- `kuzhambu-infra-auth`
- `kuzhambu-infra-audit`

职责：

- 用户主体、角色、菜单和权限资源。
- 登录、验证码、token、refresh token 和认证会话。
- 认证事件。
- 业务数据变更审计。

### storage

合并来源：

- `kuzhambu-biz-storage`
- `kuzhambu-infra-storage`

职责：

- 文件对象。
- 文件引用。
- 文件读取。
- 普通上传和分片上传。
- 本地文件和 S3 兼容存储适配。

### classics

中文业务名：古籍域。

合并来源：

- `kuzhambu-biz-sancai`
- `kuzhambu-biz-wangqi`
- `kuzhambu-biz-mingcustoms`
- `kuzhambu-biz-sharing`
- `kuzhambu-infra-sancai`
- `kuzhambu-infra-wangqi`
- `kuzhambu-infra-mingcustoms`
- `kuzhambu-infra-sharing`

职责：

- 三才图会、王圻文档和明代习俗三类知识内容。
- 内容 CRUD、版本、可见性、批量操作、导出和分享。
- 三才图会门类卷结构、视觉资产和静态展示页面。
- 王圻文档时间线和单文档问答入口上下文。
- 明代习俗标签云和详情阅读。

### ai

合并来源：

- `kuzhambu-biz-ai-config`
- `kuzhambu-biz-ai-refinement`
- `kuzhambu-infra-ai-config`
- `kuzhambu-infra-ai-refinement`

职责：

- AI 服务配置、模型管理和能力映射。
- 提示词管理、变量校验、版本历史和回滚。
- 翻译、标签提取、摘要生成、问答对生成、图片理解和条目拆分。
- AI 候选结果确认、失败反馈、批量取消和调用统计。

### knowledge

合并来源：

- `kuzhambu-biz-taxonomy`
- `kuzhambu-biz-data-refinement`
- `kuzhambu-biz-knowledge-graph`
- `kuzhambu-infra-taxonomy`
- `kuzhambu-infra-data-refinement`
- `kuzhambu-infra-knowledge-graph`

职责：

- 标签治理、标签分类、合并、废弃和别名。
- 同义词词典。
- 实体标注和关系抽取精修。
- 三才图会知识图谱、世系图和质量评估。

### discovery

合并来源：

- `kuzhambu-biz-search`
- `kuzhambu-biz-qa`
- `kuzhambu-infra-search`
- `kuzhambu-infra-qa`

职责：

- 跨库搜索。
- 智能问答。
- 查询理解、同义词扩展、实体识别和实体链接增强。
- 来源引用、会话、搜索日志和质量分析数据。

### operations

`operations` 不作为 `system` 基础域的一部分，也不放入 `starter`。原 `operations` 需求调整为独立运营运维域，后台看板只是该领域的 interface 表现之一。

处理原则：

- 各领域拥有自己的业务事实和生命周期规则。
- `operations` 可以聚合展示统计、健康摘要、日志摘要、任务状态和维护入口。
- `operations` 若存在自有表、报表记录、看板配置、任务台账或维护操作记录，必须拥有独立 `domain` 和 `infra`。
- 纯数据聚合入口可以表现为 admin controller，但一旦需要持久化或 Repository，不得放入 `starter`。
- 过期分享规则归 `classics` 的 sharing 能力。
- 过期导出规则归 `classics` 的 export 能力。
- AI 调用统计来源归 `ai`。
- 搜索和问答统计来源归 `discovery`。
- 备份恢复、健康检查和日志查看属于运行支撑或后台看板，不进入 `system` 基础域规则。

## Target Server Layout

```text
kuzhambu-servers/
  common/
  biz/
    system/
      kuzhambu-system-interface/
      kuzhambu-system-application/
      kuzhambu-system-domain/
      kuzhambu-system-infra/
    storage/
      kuzhambu-storage-interface/
      kuzhambu-storage-application/
      kuzhambu-storage-domain/
      kuzhambu-storage-infra/
    classics/
      kuzhambu-classics-interface/
      kuzhambu-classics-application/
      kuzhambu-classics-domain/
      kuzhambu-classics-infra/
    ai/
      kuzhambu-ai-interface/
      kuzhambu-ai-application/
      kuzhambu-ai-domain/
      kuzhambu-ai-infra/
    knowledge/
      kuzhambu-knowledge-interface/
      kuzhambu-knowledge-application/
      kuzhambu-knowledge-domain/
      kuzhambu-knowledge-infra/
    discovery/
      kuzhambu-discovery-interface/
      kuzhambu-discovery-application/
      kuzhambu-discovery-domain/
      kuzhambu-discovery-infra/
    operations/
      kuzhambu-operations-interface/
      kuzhambu-operations-application/
      kuzhambu-operations-domain/
      kuzhambu-operations-infra/
  starter/
    kuzhambu-admin-starter/
    kuzhambu-portal-starter/
```

## Package Rules

业务域 Java package 固定使用：

```text
com.thundax.kuzhambu.<domain>.interfaces.admin
com.thundax.kuzhambu.<domain>.interfaces.portal
com.thundax.kuzhambu.<domain>.application
com.thundax.kuzhambu.<domain>.domain
com.thundax.kuzhambu.<domain>.infra
```

`<domain>` 取值：

- `system`
- `storage`
- `classics`
- `ai`
- `knowledge`
- `discovery`
- `operations`

`starter` package 固定使用：

```text
com.thundax.kuzhambu.starter.admin
com.thundax.kuzhambu.starter.portal
```

## Document Merge Rules

需求文档从细粒度模块文档归并为业务域文档。归并时只改变组织方式，不新增未经确认的需求。

目标需求文档：

```text
docs/10-requirements/SYSTEM-REQUIREMENTS.md
docs/10-requirements/STORAGE-REQUIREMENTS.md
docs/10-requirements/CLASSICS-REQUIREMENTS.md
docs/10-requirements/AI-REQUIREMENTS.md
docs/10-requirements/KNOWLEDGE-REQUIREMENTS.md
docs/10-requirements/DISCOVERY-REQUIREMENTS.md
docs/10-requirements/OPERATIONS-REQUIREMENTS.md
```

归并来源：

- `SYSTEM-REQUIREMENTS.md` 合并 `CORE-REQUIREMENTS.md`、`AUTH-REQUIREMENTS.md`、`AUDIT-REQUIREMENTS.md`。
- `STORAGE-REQUIREMENTS.md` 保留并按新业务域口径校正引用。
- `CLASSICS-REQUIREMENTS.md` 合并 `SANCAI-KNOWLEDGE-REQUIREMENTS.md`、`WANGQI-DOCUMENT-REQUIREMENTS.md`、`MING-CUSTOMS-REQUIREMENTS.md`、`SHARING-REQUIREMENTS.md` 中的内容和分享规则。
- `AI-REQUIREMENTS.md` 合并 `AI-CONFIG-PROMPT-REQUIREMENTS.md`、`AI-REFINEMENT-REQUIREMENTS.md`。
- `KNOWLEDGE-REQUIREMENTS.md` 合并 `TAXONOMY-REQUIREMENTS.md`、`DATA-REFINEMENT-REQUIREMENTS.md`、`KNOWLEDGE-GRAPH-REQUIREMENTS.md`。
- `DISCOVERY-REQUIREMENTS.md` 合并 `SEARCH-REQUIREMENTS.md`、`QA-REQUIREMENTS.md`。
- `OPERATIONS-REQUIREMENTS.md` 保留并重写为运营运维域需求，覆盖看板、统计、报表、任务状态和维护入口；不承载其他领域的业务规则。

设计文档同步归并到：

```text
docs/30-designs/SYSTEM-DESIGN.md
docs/30-designs/STORAGE-DESIGN.md
docs/30-designs/CLASSICS-DESIGN.md
docs/30-designs/AI-DESIGN.md
docs/30-designs/KNOWLEDGE-DESIGN.md
docs/30-designs/DISCOVERY-DESIGN.md
docs/30-designs/OPERATIONS-DESIGN.md
```

旧需求和旧设计文档处理规则：

- 归并完成前不得删除旧文档。
- 归并完成后，旧文档必须直接删除，不保留短期迁移索引。
- 同一规则只能在新业务域文档中有一个权威表述。
- 旧文档中的 Related Documents 必须更新为新业务域文档引用。

## Database And SQL Rules

数据库迁移遵守 `docs/00-governance/SERVERS-DATABASE-RULES.md`。

SQL 文件从旧细粒度模块文件归并为业务域文件：

```text
db/schema/system.sql
db/schema/storage.sql
db/schema/classics.sql
db/schema/ai.sql
db/schema/knowledge.sql
db/schema/discovery.sql
db/schema/operations.sql

db/data/system.sql
db/data/storage.sql
db/data/classics.sql
db/data/ai.sql
db/data/knowledge.sql
db/data/discovery.sql
db/data/operations.sql
```

归并来源：

- `system.sql` 合并 `sys.sql`、`auth.sql`、`audit.sql`。
- `storage.sql` 保留 `storage.sql`。
- `classics.sql` 合并 `sancai.sql`、`wangqi.sql`、`mingcustoms.sql`、`sharing.sql`。
- `ai.sql` 合并 `aiconfig.sql`、`airefinement.sql`。
- `knowledge.sql` 合并 `taxonomy.sql`、`datarefinement.sql`、`knowledgegraph.sql`。
- `discovery.sql` 合并 `search.sql`、`qa.sql`。
- `operations.sql` 保留并重写为运营运维域 SQL；只保存本领域自有表，不复制其他业务域统计事实。

表名前缀处理：

- 模块文件名按新业务域归并。
- 当前仍处于设计阶段，没有真实生产数据，允许随业务域归并同步重命名表。
- 表名前缀应按新业务域统一收敛。
- `sys_`、`auth_`、`audit_` 表应归并为 `system_` 前缀，必要时通过表名主体表达子能力，例如认证会话、认证事件和审计日志。
- `sancai_`、`wangqi_`、`mingcustoms_`、`sharing_` 表应归并为 `classics_` 前缀，必要时通过表名主体表达内容类型或分享子能力。
- `aiconfig_`、`airefinement_` 表应归并为 `ai_` 前缀。
- `taxonomy_`、`datarefinement_`、`knowledgegraph_` 表应归并为 `knowledge_` 前缀。
- `search_`、`qa_` 表应归并为 `discovery_` 前缀。
- `operations_` 表如确认为看板自有表，应归并为 `operations_` 前缀；非看板自有表应归还对应业务域。
- 表重命名必须同步更新设计文档、DDL、初始化数据、DO、Mapper、Repository 和测试夹具。

SQL 归并验收：

- 新 SQL 文件包含旧文件中的全部 DDL 和初始化数据。
- 旧 SQL 文件不再被构建、初始化或文档引用。
- 表名、字段名、索引名与对应设计文档保持一致。
- 初始化顺序满足外观依赖和数据引用要求。
- 旧业务域表名前缀不再作为新设计权威口径出现，除非文档明确说明为外部兼容字段或历史导入来源。

## Layer Dependency Rules

目标依赖方向：

```text
starter -> interface -> application -> domain
starter -> infra -> application/domain
infra -> domain
```

规则：

- `domain` 不依赖 `application`、`interfaces`、`infra` 或 `starter`。
- `application` 不依赖 `interfaces`、`infra` 或 `starter`。
- `interfaces` 不依赖 `infra`。
- `infra` 不依赖 `interfaces` 或 `starter`。
- `starter` 负责运行时装配，不承载业务规则。
- admin 和 portal 使用同一个业务域 `interface` module，通过 package 和 starter 扫描范围隔离。

## Starter Rules

`kuzhambu-admin-starter` 只启动：

- `*.interfaces.admin`
- 各业务域 `application`
- 各业务域 `infra`
- 必要 `common` 配置

`kuzhambu-portal-starter` 只启动：

- `*.interfaces.portal`
- 各业务域 `application`
- 各业务域 `infra`
- 必要 `common` 配置

优先使用 marker class 配置扫描范围，避免长期维护字符串形式的 `scanBasePackages`。

## Execution Plan

本 RUNBOOK 对应一次完整迁移，不拆分为只包含治理文档或空模块骨架的中间 PR。提交可以小步组织，但 PR 收口必须完成文档、SQL、Maven 结构、代码迁移、验证和清理。

### Phase 0: Freeze And Inventory

- 确认当前分支仅承载模块结构重构。
- 盘点 `kuzhambu-servers/pom.xml`、现有 `biz/pom.xml`、`infra/pom.xml`、`interfaces/pom.xml` 的模块列表。
- 盘点所有 Java package、资源文件、Mapper XML、测试和配置文件位置。
- 盘点 `db/schema` 和 `db/data` 是否按旧模块名存在绑定。

### Phase 1: Governance Update

- 更新 `docs/00-governance/SERVERS-ARCHITECTURE.md`。
- 更新 `docs/00-governance/SERVERS-ARCHITECTURE-RULES.md`。
- 更新 `docs/00-governance/SERVERS-DATABASE-RULES.md` 中业务域前缀和文件归属口径。
- 必要时更新 `docs/AGENTS.md` 文档路由。
- 必要时更新 `docs/40-readiness/PR-WORKFLOW.md` 和 `scripts/verify-all.sh`。

### Phase 2: Requirements And Designs

- 归并 `docs/10-requirements/` 中的需求文档。
- 归并 `docs/30-designs/` 中的设计文档。
- 更新旧文档引用关系。
- 检查需求文档、设计文档、业务域名和 SQL 文件名一致。

### Phase 3: Database SQL

- 归并 `db/schema` 下旧 SQL 文件到目标业务域 SQL 文件。
- 归并 `db/data` 下旧 SQL 文件到目标业务域 SQL 文件。
- 更新初始化脚本、部署配置或文档中对旧 SQL 文件的引用。
- 按新业务域前缀重命名设计阶段表名，并同步更新相关引用。

### Phase 4: Maven Structure

- 在 `biz/` 下新增 7 个业务域目录，并在 `kuzhambu-servers/` 下新增 `starter` 目录。
- 为每个业务域建立 `interface`、`application`、`domain`、`infra` 子模块。
- 将原 `interfaces/kuzhambu-admin-api` 和 `interfaces/kuzhambu-portal-api` 调整为 `starter` 下启动应用。
- 更新 `kuzhambu-servers/pom.xml`、`biz/pom.xml`、各业务域 POM、`starter/pom.xml` 和启动应用 POM 的模块声明与依赖关系。

### Phase 5: Code Migration

- 按目标领域迁移旧 `biz` 代码到对应 `application` 和 `domain`。
- 按目标领域迁移旧 `infra` 代码到对应 `infra`。
- 按目标领域迁移旧 admin/portal controller、request、response 和 assembler 到对应 `interface`。
- 更新 package 声明、import 和资源路径。
- 保持业务行为不变。

### Phase 6: Operations Repositioning

- 将 `operations` 中属于看板展示、报表、任务台账和维护操作记录的能力迁移到独立 `operations` 业务域。
- 将清理规则、统计来源和长任务状态归还对应领域。
- 删除旧横向分层下的 `kuzhambu-biz-operations` 和 `kuzhambu-infra-operations` 模块。

### Phase 7: Validation

- 运行 Maven reactor 编译。
- 运行现有测试。
- 运行架构规则测试或脚本。
- 运行 `scripts/verify-all.sh`。
- 检查 admin starter 和 portal starter 的启动扫描范围。
- 检查 SQL 文件归并后初始化入口可定位目标文件。
- 检查需求文档和设计文档不存在旧模块互相矛盾的重复事实。

### Phase 8: Cleanup

- 删除旧横向分层下的 `infra/`、`interfaces/` 空模块和残留 POM 引用，保留并重建 `biz/` 为业务域工程组。
- 删除或收口旧需求文档、旧设计文档和旧 SQL 文件。
- 删除无用旧 package、资源和配置。
- 更新 TODO，删除或收窄已完成任务。
- PR 收口前删除本文档，除非该 RUNBOOK 仍用于后续阶段。

## TODO Usage

本任务的执行项应写入根目录 `TODO.md`，格式遵循 `docs/00-governance/TODO-RULES.md`。

执行任务模板：

```markdown
- [ ] `范围对象`：任务标题
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：精确文件路径、模块或对象
    - 处理动作：一句话说明要做什么
    - 验收点：可检查的完成结果
    - 重要度：N/10
```

未经人工确认的拆解任务和执行任务必须放入 `待审阅任务项`。确认可以执行后，再移动到 `当前任务项`。

## Validation Checklist

- `mvn -pl ... test` 能通过当前迁移阶段涉及模块。
- `mvn test` 或 Maven reactor 等价验证能通过完整后端工程。
- `scripts/verify-all.sh` 能通过。
- admin starter 不暴露 portal controller。
- portal starter 不暴露 admin controller。
- `domain` 和 `application` 不依赖外层。
- `interfaces` 不依赖 `infra`。
- `infra` 不依赖 `interfaces`。
- 旧模块名不再出现在 Maven module 声明中。
- 新需求文档覆盖旧需求文档的全部有效需求。
- 新设计文档覆盖旧设计文档的全部有效设计。
- 新 SQL 文件覆盖旧 SQL 文件的全部 DDL 和初始化数据。
- 新 SQL 文件、设计文档和 Java 持久化对象使用新业务域表名前缀。
- 旧需求文档、设计文档和 SQL 文件不再作为权威入口被引用。

## Risks

- 一次性移动全部模块会产生大量 package 和 import 变更，容易混入行为变化。
- 旧架构规则测试可能先于新规则更新而失败。
- admin 和 portal controller 如果只靠宽泛 component scan，可能互相暴露入口。
- Mapper XML、资源文件和配置路径容易遗漏。
- 需求文档、设计文档和 SQL 文件如果不同步归并，会让新业务域和旧模块名长期并存。
- SQL 文件归并容易改变初始化顺序，需要单独校验。
- 表重命名虽然当前没有真实数据风险，但容易遗漏 DO、Mapper、初始化数据和文档引用。

## Open Decisions

无。
