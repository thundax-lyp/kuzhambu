---
name: kuzhambu-admin-page-review
description: Use only when explicitly invoked to review a kuzhambu-apps/admin-web src/pages business page by page path or component name. The review evaluates page composition, component responsibility boundaries, data/API ownership, state ownership, sibling coordination, expression structure, layout wrappers, and whether proposed abstractions have a concrete ownership or complexity reason. Defaults to review-only and does not modify code unless the user separately asks to fix.
---

# Kuzhambu Admin Page Review

This skill reviews business pages under `kuzhambu-apps/admin-web/src/pages`.

It is a review workflow, not a refactoring workflow. Do not modify code unless the user separately and explicitly asks to implement fixes.

## Inputs

Require one target argument. It can be either:

- A path under admin-web `src/pages`.
- A page or component name located under admin-web `src/pages`.

Accept examples:

```text
$kuzhambu-admin-page-review classics/sancai/sancai-entry-panel
$kuzhambu-admin-page-review src/pages/classics/sancai/sancai-entry-panel
$kuzhambu-admin-page-review kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-entry-panel
$kuzhambu-admin-page-review SancaiEntryEditDrawer
```

If the input is a component or page name, locate candidates under `kuzhambu-apps/admin-web/src/pages` with `rg --files` and code search. Match file names, directory names, exported component names, and import names. If multiple candidates match, ask the user to choose before reviewing.

## Scope

Default scope:

- The specified page directory or page entry file.
- Directly imported local components, hooks, types, and services used by that page.
- Shared components only at the call-site/API level, unless the suspected issue is caused by the shared component contract itself.

Do not expand into unrelated pages or project-wide refactors without dependency evidence.

## Review workflow

Follow this order. Do not start from local variable details before classifying the page and component relationships.

### 1. Identify the reviewed target

Normalize the user input to an admin-web path:

```text
kuzhambu-apps/admin-web/src/pages/<page>
```

If the input is a component name, first resolve it to its source file under `src/pages`, then identify the nearest owning page or feature directory. Review from that owning boundary, not only the single component file, unless the user explicitly asks for a component-only review.

Inspect the page entry, colocated components, hooks, services, tests, and direct local imports needed to understand the page composition.

### 2. Classify each component

Assign each relevant component one primary role:

- **Composition component**: owns page structure, regions, navigation, tabs, drawers, modals, layout, and necessary coordination between peer capabilities.
- **Capability component**: owns one business capability end to end, including data, state, permissions, validation, interactions, submission, feedback, empty/error states, and internal presentation.
- **Expression component**: owns stable expression patterns such as field display, form item composition, description item, table cell composition, expandable text, or repeated value presentation.
- **Primitive/shared component**: reusable UI or infrastructure component that should not carry page-specific domain behavior.

Flag components that mix composition and capability responsibilities without a clear boundary.

### 3. Review parent-child boundaries

For each important parent-child relationship, determine whether the parent is arranging the child or controlling the child.

Healthy boundaries:

- Parent passes necessary semantic context.
- Child owns its own internal state, data loading, permissions, presentation details, and local workflow.
- Child emits stable semantic events such as changed, selected, submitted, closed, or failed.

Boundary smells:

- Parent passes preassembled JSX for a child business area.
- Parent holds child-specific refs, DOM anchors, class names, focus management, or scroll targets.
- Parent calculates data or permissions only used by one child.
- Parent owns child-local loading, error, selection, draft, candidate, or visibility state.
- Props resemble a remote control made of many booleans, modes, internal handlers, or protocol flags.
- Child cannot be understood as an independent business capability without reading its parent.

### 4. Review prop and callback contracts

Props should express semantic data and stable events.

Prefer:

- Business object or object identifier.
- Required display context.
- Read-only/open/selected state when truly controlled by a parent.
- Stable callbacks for page-level coordination.

Avoid:

- Precomposed UI fragments.
- Internal implementation details.
- Protocol-specific intermediate states that belong inside the capability.
- Large sets of one-off handlers for child internals.
- Data fetched by the parent only because it has convenient access to parameters.

### 5. Review state and variable ownership

For each meaningful state/variable, ask:

- Which business capability does this serve?
- Which component uses it to make decisions?
- What lifecycle owns it?
- Does it affect page-level composition or only a local capability?
- Can it move closer to the decision point?

Rule: state belongs near the lifecycle that owns it; variables belong near the decision point that consumes them.

### 6. Review data/API ownership

Place API requests by business ownership, not parameter convenience.

Prefer local ownership when:

- Data serves one capability or one field.
- Data has local loading/error/empty behavior.
- Data is dynamic option data for a local form item.
- Mutation result only affects one capability.

Prefer higher ownership when:

- Data is true page-level context.
- Multiple peer capabilities share the same source of truth.
- Coordination is genuinely cross-capability.

If transformation is protocol-heavy, identify the owner of that transformation and explain the change reason it should follow. If a separate adapter/service boundary is suggested, state the concrete ownership, reuse, testing, or complexity reason.

### 7. Review peer-component coordination

When peer components interact, classify the interaction:

- Same business process: group them into a higher-level capability component.
- Page-level coordination: keep the minimal coordination in the composition component.
- Incidental state relay: remove the relay by moving ownership to the actual capability or introducing a clear coordinator.

Avoid peer components controlling each other through parent-held intermediate state unless the parent represents a real business coordinator.

### 8. Review expression structure

Check whether display structures use the right semantic composition.

Examples:

- Field-like output should use field/description/form item patterns.
- Editable form values should be represented as form items, not loose labels and controls.
- Repeated tabular information should use table/list structures with consistent cells and actions.
- Repeated text display can use reusable expression components when behavior is stable.

Do not recommend extracting components only because JSX is long. Extract when there is a stable expression pattern or capability boundary.

For page-level business components, similarity is not enough. Recommend extracting a common business component only when both conditions hold:

- Multiple stable repetitions exist.
- The repetitions are governed by the same business rule and should change together when that rule changes.

If there are only one or two similar areas, prefer preserving local page continuity and fixing naming/structure in place. Frontend page readability depends on keeping related UI close together; premature extraction can make the page fragmented.

### 9. Review layout and wrappers

Composition components should prefer semantic layout components such as row, column, flex, grid, tabs, drawer, modal, list, section, or space.

Flag low-semantic wrappers when they:

- Carry business meaning.
- Patch child component internals.
- Manage child-specific focus, scroll, anchors, or internal positioning.
- Add page-level class names around child details that should belong to the child.

Plain HTML wrappers are acceptable when they are minimal, stable layout shells and do not encode child business details.

### 10. Review abstractions

For hooks, helpers, adapters, and extracted components, decide whether they lower complexity.

A valid abstraction should:

- Have a stable responsibility.
- Hide details that callers should not know.
- Have clear inputs and outputs.
- Isolate a change reason or support real reuse under the same business rule.

Flag abstractions that merely move a component’s tangled state into another file.

When recommending or rejecting an abstraction, state the concrete reason. Useful abstraction reasons include:

- Multiple components share the same state model or workflow.
- Multiple page areas repeat the same business rule and should evolve together.
- The data relationship is broad enough that it obscures the component’s expression.
- Protocol adaptation is complex enough to deserve an explicit boundary.
- The logic needs independent unit testing.
- The capability component is already the owner, and extraction would reduce complexity instead of just shortening the file.

## Output format

Use this structure:

```text
结论
- 页面整体角色判断
- 最大边界问题
- 是否建议重构

组件归类
- <component>: <composition/capability/expression/primitive> — <reason>

主要问题
[P1/P2/P3] <title>
- 位置: <file/component>
- 触发: <concrete condition>
- 问题: <why the boundary/ownership is wrong>
- 建议: <where the logic/data/interface should move>
- 抽象判断: <ownership/complexity/reuse reason, if an abstraction is involved>

建议重构顺序
1. <step>
2. <step>

不建议改动
- <stable boundary or intentional exception>

验证建议
- <narrow validation commands or manual checks>
```

Severity guide:

- **P1**: likely user-visible breakage, incorrect business behavior, or hard-to-maintain boundary that blocks safe changes.
- **P2**: concrete responsibility leak, duplicated ownership, or unstable interface likely to cause bugs.
- **P3**: maintainability/readability concern. Include only when useful for planned refactoring.

If no actionable findings exist, say so and list residual risks or validation gaps.

## Fixing after review

If the user asks to fix after the review, implement in small steps:

1. Preserve existing behavior.
2. Move ownership before extracting reuse.
3. Choose the implementation shape from the corrected ownership boundary and the actual complexity.
4. Extract only when the extraction has a concrete ownership, reuse, testing, or complexity reason.
5. Keep page composition working after each step.
6. Run the narrowest relevant frontend validation.
7. Do not stage or commit unless the user asks.
