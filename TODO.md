# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 当前任务项按数字编号顺序执行；不得跳过前置契约任务直接做后续调用方或重命名任务。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `9 三才Request与Assembler契约`：9. 清理三才 request 注解与 admin assembler 非空契约 allowlist
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 范围对象：
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiAssetRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiCategoryRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryPageRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiEntryVersionRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/request/SancaiVolumeRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiInterfaceAssembler.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/assembler/SancaiAssetInterfaceAssembler.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`
    - 处理动作：补齐三才 admin request 模型注解并收敛 admin assembler 公共方法非空契约。
    - 验收点：三才 admin request 注解 key 已删除；三才 admin assembler non-null key 已删除或收窄为未处理文件的精确说明。
    - 重要度：7/10

- [ ] `10 三才Response与Portal注解`：10. 清理三才 response 与 Portal API 模型注解 allowlist
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 范围对象：
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiCategoryResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiContentResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiEntryVersionResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiVolumeResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/controller/request/SancaiPortalEntrySearchRequest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/controller/response/SancaiPortalCategoryResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/portal/sancai/controller/response/SancaiPortalVolumeResponse.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`
    - 处理动作：补齐三才 response 与 portal request/response 模型注解。
    - 验收点：Portal 只读 ES READY 且未删除内容契约不变；三才 response 与 portal API 模型注解 key 清零。
    - 重要度：7/10

- [ ] `11 最终收口清理现场`：11. 清理 allowlist、TODO 和 RUNBOOK 现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 范围对象：
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationCommandQueryRecordAllowances.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/ClassicsApplicationArchitectureTest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/test/java/com/thundax/kuzhambu/classics/domain/ClassicsDomainArchitectureTest.java`
        - `kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/ClassicsInterfaceArchitectureTest.java`
        - `docs/30-designs/RUNBOOK-ARCHUNIT-ALLOWLIST-REMOVAL-07-CLASSICS-SANCAI-PUBLICATION.md`
    - 处理动作：确认本切片 allowlist key 清零或仅保留有理由的非本切片 key，并删除已完成 TODO 与本 RUNBOOK。
    - 验收点：本 RUNBOOK 不再留在仓库；`TODO.md` 不保留已完成项；工作区不包含临时执行现场。
    - 重要度：10/10

## 待讨论项
