# Sortable 排序专项设计

## 1. 目标
定义统一、稳定、可回放的排序机制，约束所有 `implements Sortable` 的实体只通过服务端交换 `priority` 完成重排。

核心原则：
- 前端只提交有序 ID 列表，不提交 `priority`。
- 后端使用交换式重排写回 `priority`。
- `priority` 由服务端全权管理，并保持全局唯一。
- 排序结果可重复、可回放、可预测。
- 新增对象时由服务端自动分配 `priority`，默认追加到当前最大值之后。

## 2. 适用范围
- 适用于所有手动调整展示顺序的平铺列表实体。
- 不适用于树结构节点的 `lft/rgt` 重排。
- 本仓库当前覆盖的排序实体如下：
  - `system`: `Role`, `Dict`
  - `storage`: `StoredObject`
  - `classics`:
    - `SancaiCategory`
    - `SancaiVolume`
    - `SancaiEntry`
    - `SancaiEntryImage`
    - `MingCustomsKeyword`
    - `ClassicsContentTag`
    - `ClassicsContentQaPair`
    - `ClassicsShareTarget`

## 3. 模型约束
- `priority` 只表示排序权重，不承载业务语义。
- 所有排序都视为 `FlatSort`，不引入 scope 排序参数。
- `priority` 在数据库中保持唯一。
- 重排流程只允许交换参与实体的 `priority`，不允许插值式重算。

## 4. 排序契约
### 4.1 请求入参
- 仅允许：
  - `orderedIds`
  - `sortDirection`
- `sortDirection` 只允许 `ASC` 或 `DESC`，默认 `ASC`。
- 不允许提交 `priority`。

### 4.2 创建契约
- 新建 `Sortable` 对象时，`Controller` 不接收 `priority`。
- `ApplicationService` 在创建实体时读取当前 `maxPriority()`。
- 如果 `maxPriority()` 为空，按 `0` 处理。
- 新实体默认写入 `priority = maxPriority + 1`。
- 创建行为与排序行为共用同一 `priority` 体系。

### 4.3 服务端规则
- 先按当前 `sortDirection` 读取完整排序集合。
- 校验 `orderedIds` 非空、无重复、与当前集合一致。
- 采用交换式序列完成重排。
- 整个过程必须在事务内完成。

### 4.4 更新契约
- 普通 `update` 只允许修改业务字段，不允许写 `priority`。
- `priority` 只能通过专门的 `updatePriority` 接口修改。
- 创建与排序共享同一 `priority` 体系，但语义边界必须分开。

### 4.5 异常语义
- `SORT_EMPTY_INPUT`：`orderedIds` 为空。
- `SORT_MISSING_ID`：排序目标与当前集合不一致。
- `SORT_DB_FAILURE`：持久化失败。

## 5. 实现方式
### 5.1 读取当前排序集合
- `Role` / `Dict` / `StoredObject` 直接按全局 `priority` 读取。
- `classics` 中的各类排序实体也按全局 `priority` 读取。
- 列表查询与排序查询必须使用同一排序键。

### 5.2 交换式重排
- 读取当前集合后，构造 `id -> index` 与 `id -> priority` 映射。
- 逐位比较目标顺序与当前顺序。
- 如有偏差，则先把目标项挪到临时 `priority`，再交换双方 `priority`。
- 每次只改动两个实体的 `priority`。

### 5.3 数据一致性
- 排序操作必须完整回滚。
- 数据库唯一约束作为最终兜底。
- 排序接口不返回中间态。

### 5.4 创建时追加
- 创建流程只负责拿到当前最大 `priority` 并加一。
- `maxPriority()` 为空时，默认从 `0` 起步。
- 新增对象的排序结果应稳定落在列表尾部，随后再由排序接口做人工重排。

## 6. 服务与接口边界
- `application` 层负责排序算法。
- `domain` 层负责仓储能力与实体定义。
- `interface` 层负责接收 `orderedIds` 与 `sortDirection`。
- `infra` 层只负责持久化更新。

## 7. 验收标准
- 调整顺序后刷新列表，展示顺序与 `orderedIds` 一致。
- 重复提交同一 `orderedIds`，结果保持一致。
- 目标集合不完整或含重复 ID 时，整体失败且不写库。
- 排序能力覆盖本仓库内全部 `Sortable` 实体。
