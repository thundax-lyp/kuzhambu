# Admin Web Agent

This file extends the repository root `AGENTS.md` for work under `kuzhambu-apps/admin-web/`.

## Scope

- React + TypeScript + Vite admin console.
- UI library: Ant Design.
- Routing: React Router.
- Server state: TanStack Query.
- Tests: Vitest + Testing Library.

## Required Docs

- For admin-web naming, placement, service ownership, frontend layering, UI rules, and default forbidden directories, read:
    - `docs/00-governance/ADMIN-WEB-RULES.md`
- Treat this file as the `kuzhambu-apps/admin-web` TypeScript architecture entry point.
- Do not restate full naming, placement, layer, or UI rules here. Keep the rule source in `ADMIN-WEB-RULES.md`.

## Architecture Gates

- `npm run lint` is the first admin-web architecture gate.
- `eslint-plugin-boundaries` enforces import direction and layer dependency rules that can be expressed as ESLint configuration.
- ESLint core rules enforce simple syntactic gates, including deep relative import bans and direct `fetch` bans outside `src/api/http.ts`.
- Local ESLint rules may enforce narrow project-specific naming checks when an existing package cannot express the rule cleanly.
- ArchUnitTS is the backup architecture gate. Add it only when a rule is important, belongs in `Hard Rules`, and cannot be expressed clearly with ESLint or `eslint-plugin-boundaries`.
- ESLint and ArchUnitTS violation messages should start with the corresponding rule id from `ADMIN-WEB-RULES.md`, for example `ADMIN_WEB_LAYER_NO_DEEP_RELATIVE_IMPORT`.
- Do not create a second admin-web architecture document unless the governance docs are intentionally reorganized first.

## Auth And Permission Rules

- Login session setup must bind token and current-user permissions together.
- After successful `/auth/session/login`, immediately load `/sys/current-user/perms` before navigating into protected pages.
- If permission loading fails during login setup, clear the access token and permissions.
- Clearing the access token must also clear stored permissions.
- Use server-loaded menus for navigation display. Use stored permissions for finer UI capability checks.
- Do not duplicate permission strings in page logic when an existing helper can express the check.

## UI Rules

- The first protected screen should be the admin working shell, not a marketing page.
- Follow the existing Ant Design layout and component style.
- Keep operational pages compact and scannable.
- Use icons for clear toolbar and action affordances when Ant Design provides a suitable icon.
- Avoid adding decorative-only layout sections.

## Testing And Verification

- Admin web tests are Vitest tests. Do not use Jest-only CLI flags or APIs such as `--runInBand`; use `npm test` or Vitest-supported arguments only.
- For auth, permission, routing, request hook, or layout behavior changes, update `src/app.test.tsx` or add a focused test.
- Before committing frontend changes, run the smallest relevant checks:
    - `npm run format:check`
    - `npm run lint`
    - `npm test`
    - `npm run build` when TypeScript, routing, bundle entry, or shared auth/API code changed
- Vite chunk-size warnings are acceptable unless the change intentionally touches bundling or lazy loading.

## Dev Server

- Use `npm run dev -- --port <port>` when a specific port is needed.
- If a dev server is already running for collaboration, leave it running unless the user asks to stop it.
