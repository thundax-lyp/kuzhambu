# Admin UX/UI Review Reference

Use this reference when reviewing `kuzhambu-apps/admin-web` page layout, tables, actions, Drawer, Modal, task flows, state feedback, permission availability, accessibility, or E2E anchors.

Review concrete management-console behavior, not abstract aesthetics. Findings must identify the trigger, user impact, violated project pattern, and fix direction.

## Page Layout

Check whether the page follows the default admin information order:

```text
PageHeader
SearchAndFilter
Toolbar
Content
Footer
```

Flag:

- Pagination at the top.
- Filters inside table body.
- Random section order.
- Multiple competing primary actions in one view.
- A page bypassing `KuzhambuListPage` or existing page skeletons without a clear reason.

`PageHeader` should have a title and useful description on the left, with at most one primary action and at most two secondary actions on the right. Extra actions should collapse into a menu.

## Toolbar And Batch Actions

For list pages, check:

- Selected state is visible when row selection exists.
- Batch actions are near selected state.
- Batch actions are disabled when no rows are selected.
- Dangerous batch actions require confirmation and appear last.
- Page-level actions do not compete with batch actions.

## Data Table And Action Column

Review table shape against the project table contract:

- Column order should be `Identifier -> BusinessFields -> Status -> Time -> Actions`.
- Table has loading, empty, and error states.
- At least one business data column remains flexible; do not assign fixed width to every column.
- `KuzhambuTable` / `KuzhambuListPage` action columns use `key: "actions"` with structured `options`.
- Do not use custom `render` for the actions column unless the column is not actually an action column.
- If a row has only one operation, render it directly; do not hide it inside a dropdown.
- Row operations are text by default; icons are used only when the project pattern calls for them.
- Delete or dangerous operations are last, danger-styled, and require confirmation.
- Do not use tree table behavior unless row hierarchy is a real business object shown in the table.

Flag action column findings when the page manually decides presentation details that belong to `KuzhambuTable`, such as dropdown thresholds, fixed action column display, dividers, or mobile collapse behavior.

## Drawer And Modal

Use the project container semantics:

- Drawer is for business forms, details, and complex relationships.
- Modal is for confirmation, lightweight feedback, and short focused flows.
- Business forms should use Drawer or Page by default, not Modal.
- Drawer/Modal footer order is `取消 | 确认`, `取消 | 保存`, or `取消 | 删除`.
- A Drawer with sections and a unified footer should prefer `KuzhambuSegmentedDrawer`.
- A short async flow that opens a dialog, starts a task, tracks task status, fetches results, then applies results should prefer `KuzhambuSyncTaskModal`.

For AI or async task modals, verify the timeline:

```text
open modal -> show context -> user explicitly starts task -> create task -> track status -> fetch result -> show result -> user applies/merges/overwrites/appends -> close or refresh
```

Flag flows that:

- Start extraction or mutation automatically on open without explicit user action.
- Close before results are visible.
- Enable apply buttons before a successful result exists.
- Keep creating tasks while waiting for the previous one.
- Mix current persisted state and AI candidate state without clear labels.
- Hide failed task reasons behind generic messages.

## State Feedback

Check all user-visible states:

- Loading state for tables, trees, detail panels, and submit buttons.
- Empty state includes explanation and a recommended next action.
- Error state includes explanation and a recovery action.
- Disabled operations explain whether the cause is permission, business status, selection state, or missing result.
- Toast messages use `动作 + 成功` or `动作 + 失败 + 原因`; avoid generic `操作失败` or `系统错误`.

For tree, table, drawer, and modal combinations, verify that loading is local enough to avoid making the whole page look frozen.

## Expression And Information Hierarchy

Check whether business information uses the right semantic display:

- Basic metadata should use `Descriptions` / project equivalent, not loose text blocks.
- Long text should use an expandable text pattern instead of breaking layout.
- Repeated records should use table/list structures.
- Status should be visible where decisions are made, not duplicated in every navigation label.
- Current persisted state and candidate/unapplied state must be visually separated.

For SPO or graph-like data, ensure the relation expression makes subject, predicate, and object distinct enough for scanning. Prefer stable expression components, tags, arrows, or compact relation rows when they express the business relationship better than raw concatenated text.

## Accessibility And Test Anchors

Check:

- Business controls have accessible names. Prefer visible text; use `aria-label` / `aria-labelledby` or component `ariaLabel` only when visible text is insufficient.
- `testId` / `data-testid` are used for stable automation anchors, but do not replace accessible names.
- Page code using project `Kuzhambu*` interactive components should pass component-level `testId` instead of raw `data-testid`.
- Tests validate user-visible results and key request contracts, not Ant Design internal DOM details.

## Permission And Business Availability

Check that action availability is derived from the right source:

- Permission-controlled actions align with route/menu/button permissions.
- Business-status-controlled actions are disabled or hidden consistently across table, drawer, and modal.
- Disabled actions explain the concrete cause.
- Cross-tab or external permission changes refresh the in-memory permission set when the UI claims to react to them.

## Admin UX Failure Modes

Prioritize findings that match these concrete failure modes:

- Operation is discoverable only after unnecessary clicks.
- Main action, batch action, and row action hierarchy conflicts.
- Status labels disagree between list, detail, modal, and task history.
- Loading gives a stuck or frozen impression.
- Async task lifecycle is not represented end to end.
- Parent composition owns child capability state or protocol details.
- Table complexity exceeds the business hierarchy.
- Candidate, draft, and persisted data can be confused.
- Tests rely on brittle text or Ant Design internals instead of stable business anchors.

Do not report vague aesthetic advice. Omit unsupported preferences such as "make it more modern", "improve hierarchy", or "clean up spacing" unless tied to a specific project rule, concrete user task, or observable failure.
