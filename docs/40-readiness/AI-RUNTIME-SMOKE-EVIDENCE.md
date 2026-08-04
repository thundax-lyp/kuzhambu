# AI Runtime Smoke Evidence

## Scope

- Date: 2026-07-09
- Branch: `feat/ai-runtime-acceptance-runbook`
- Runtime: remote Docker stack from `../../servers.md`, compose directory `/tmp/kz-ai-acceptance`
- Admin API: `http://10.10.10.51:23010/kuzhambu-admin-api/api`
- Admin Web: local `kuzhambu-admin-web` dev server at `http://localhost:5173`
- Worker API: Docker service `workers`, internal base URL `http://workers:8000`
- Secrets policy: API keys, complete tokens, complete prompt templates, prompt payloads, and full business input are not recorded here.

## Runtime Health

Docker services were running during acceptance:

| Service | Container | Evidence |
| --- | --- | --- |
| Admin API | `kz-ai-admin-starter` | `Up`, exposed `23010 -> 20010` |
| Workers | `kz-ai-workers` | `Up`, exposed `28000 -> 8000` |
| MySQL | `kz-ai-mysql` | `Up (healthy)`, exposed `23306 -> 3306` |
| Redis | `kz-ai-redis` | `Up`, exposed `26379 -> 6379` |
| RocketMQ namesrv/broker | `kz-ai-rocketmq-*` | `Up` |
| Elasticsearch | `kz-ai-elasticsearch` | `Up`, exposed `29200 -> 9200` |

Worker health evidence:

| Endpoint | Result |
| --- | --- |
| `GET /internal/health` | `200`, `status=UP`, `service=kuzhambu-workers`, `version=0.0.1-dev` |
| `GET /internal/openapi.json` | `200`, contains `/internal/ai/invoke` and `/internal/ai/stream` |

Admin login smoke used `POST /auth/session/pre-auth-session` and `POST /auth/session/login`; both returned `COMMON-00000`. The access token was used only as a runtime header and is not persisted in this document.

## Service Configuration

Evidence files: `/tmp/kz-ai-acceptance-local/01-primary-before.json`, `/tmp/kz-ai-acceptance-local/01-service-save.json`.

| Field | Evidence |
| --- | --- |
| `serviceId` | `legacy-high-service-id` |
| `serviceRole` | `PRIMARY` |
| `apiSource` | `ctyun` |
| `baseUrl` | `https://worker-ai.local/mock` |
| `enabled` | `true` |
| `status` | `AVAILABLE` |
| `configuredAt` | `2026-07-09T05:36:07.838Z` after save |

`encryptedApiKey` was intentionally not recorded.

## Model Detection

Evidence files: `/tmp/kz-ai-acceptance-local/01-model-before.json`, `/tmp/kz-ai-acceptance-local/01-model-check.json`, `/tmp/kz-ai-acceptance-local/01-model-check-records.json`.

| Field | Evidence |
| --- | --- |
| `modelId` | `legacy-high-model-id` |
| `serviceId` | `legacy-high-service-id` |
| `modelName` | `CTYUN-CX-DeepSeek-V3.1` |
| `displayName` | `CTYUN DeepSeek V3.1` |
| `capabilityTagsJson` | includes `text`, `structured_output`, `streaming_text` |
| `defaultParamsJson` | `max_tokens=4096`, `temperature=0.2` |
| `enabled` | `true` |
| `checkId` | `863050895510732800` |
| `status` | `SUCCEEDED` |
| `latencyMs` | `2` |
| `checkedAt` | `2026-07-09T05:36:08.189Z` |

## Business Configuration

Evidence files: `/tmp/kz-ai-acceptance-local/02-business-config-get.json`, `/tmp/kz-ai-acceptance-local/02-business-config-negative.txt`.

| Field | Evidence |
| --- | --- |
| `businessConfigId` | `legacy-high-business-config-id` |
| `scope` | `classics` |
| `capability` | `summary` |
| `modelId` | `legacy-high-model-id` |
| `enabled` | `true` |
| `configuredAt` | `2026-07-09T05:44:06.776Z` |

Negative model-capability evidence was captured by attempting to bind a model that lacks the required target capability tag. The request failed as expected; the failure text is stored only in local evidence and does not contain secrets.

## Prompt Variable Validation

Evidence files: `/tmp/kz-ai-acceptance-local/02-prompt-current.json`, `/tmp/kz-ai-acceptance-local/02-variable-valid-ok.json`, `/tmp/kz-ai-acceptance-local/02-variable-valid-fail.txt`.

| Field | Evidence |
| --- | --- |
| `templateId` | `legacy-high-template-id` |
| `promptVersionId` | `863052904448131072` |
| `versionNo` | `2` |
| `variablesSnapshotJson` | required `contentType`; optional `title`, `bodyText` |
| `outputSchemaJson` | `type=text` |
| `current` | `true` |
| `changeSummary` | `Runtime acceptance prompt variable validation.` |
| `registeredAt` | `2026-07-09T05:44:07.161Z` |

Variable validation evidence:

- Complete variable set returned `true`.
- Missing required variable returned a validation failure as expected.
- Complete `messageTemplatesJson` and business input are not recorded.

## Refinement Task And Candidate

Evidence files: `/tmp/kz-ai-acceptance-local/05-create-task.response.json`, `/tmp/kz-ai-acceptance-local/05-task-get.latest.json`, `/tmp/kz-ai-acceptance-local/05-candidate-list.raw.json`.

The accepted `classics + summary` task used a current `SANCAI_ENTRY` item and completed with a real candidate.

| Field | Evidence |
| --- | --- |
| `taskId` | `863100086391930880` |
| `scope` | `classics` |
| `capability` | `summary` |
| `contentType` | `SANCAI_ENTRY` |
| `contentId` | `legacy-high-sancai-entry-id` |
| `requestId` / `traceId` | `ai-runtime-todo05-20260709165135` |
| `status` | `SUCCEEDED` |
| `serviceRole` | `PRIMARY` |
| `modelId` | `legacy-high-model-id` |
| `modelName` | `CTYUN-CX-DeepSeek-V3.1` |
| `callId` | `863100086496788480` |
| `candidateId` | `863100086928801792` |
| `streamEnabled` | `false` |
| `resultFormat` | `TEXT` |
| `requestedAt` | `2026-07-09T08:51:36.221Z` |
| `completedAt` | `2026-07-09T08:51:36.351Z` |

Candidate evidence:

| Field | Evidence |
| --- | --- |
| `candidateId` | `863100086928801792` |
| `callId` | `863100086496788480` |
| `capability` | `summary` |
| `contentType` | `SANCAI_ENTRY` |
| `contentId` | `legacy-high-sancai-entry-id` |
| `status` | `PENDING` |

## Invocation Statistics

Evidence files: `/tmp/kz-ai-acceptance-local/05-invocation-summary-before.raw.json`, `/tmp/kz-ai-acceptance-local/05-invocation-summary-after.raw.json`, `/tmp/kz-ai-acceptance-local/05-call-page-after-fix.raw.json`.

After the Sancai summary task, invocation statistics showed the summary count increase from `2` to `3`.

| Field | Evidence |
| --- | --- |
| `invocationCount` | `3` |
| `succeededInvocationCount` | `2` |
| `failedInvocationCount` | `0` |
| `topCapabilities[summary]` | `3` |
| `callId` | `863100086496788480` |
| `callIdText` | `863100086496788480` |
| `status` | `SUCCEEDED` |
| `streamUsed` | `false` |
| `streamCompleted` | `false` |
| `fallbackUsed` | `false` |
| `resultFormat` | `TEXT` |
| `warningsJson` | `[]` |

The Admin invocation table displayed `callIdText=863100086496788480` and did not display the JavaScript-rounded value `863100086496788500`.

## Admin UI Evidence

Evidence files:

- `/tmp/kz-ai-acceptance-local/04-services-edit-primary.png`
- `/tmp/kz-ai-acceptance-local/04-models-check-history.png`
- `/tmp/kz-ai-acceptance-local/04-models-check-row-scoped.png`
- `/tmp/kz-ai-acceptance-local/04-business-configs-select-summary.png`
- `/tmp/kz-ai-acceptance-local/04-prompts-summary-query-validate.png`
- `/tmp/kz-ai-acceptance-local/05-sancai-summary-task-drawer.png`
- `/tmp/kz-ai-acceptance-local/05-invocations-page-inspect.png`

UI acceptance covered:

| Page | Controls and operation verified |
| --- | --- |
| `/ai/services` | service role tabs, PRIMARY edit drawer, save/readback, status display |
| `/ai/models` | model list filter, model row check action, check history drawer |
| `/ai/business-configs` | capability/model/prompt selects, config readback, invalid model capability failure |
| `/ai/prompts` | template query, version display, variable validation success and required-variable failure |
| `/classics/sancai` | tree selection for volume, entry panel, summary refinement action, task drawer, `SUCCEEDED` status |
| `/ai/invocations` | summary metrics, capability ranking, invocation log table, exact `callIdText`, detail action |

Known UI observation during the Sancai page check: `GET /classics/sancai/entries/300000000001` returned `500`, while the entry list, refinement task page, and versions list returned `200`; the summary task drawer and task status still rendered and completed. This is recorded as observation only and was not expanded beyond the AI runtime acceptance scope.

## Result

The AI governance runtime acceptance loop is evidenced end to end:

- Service config is readable and saveable.
- PRIMARY model detection writes a successful check record.
- `classics + summary` business configuration is enabled.
- Prompt variables pass complete validation and fail missing-required validation.
- A real Sancai summary refinement task completes through Java `WorkerAiClient` to workers.
- Task, invocation log, candidate, invocation summary, and Admin UI can be correlated by `requestId`, `traceId`, `taskId`, `callId`, and `candidateId`.

## Verification

Narrow validation executed on 2026-07-09:

| Area | Command | Result |
| --- | --- | --- |
| Java AI interface reactor | `cd kuzhambu-servers && mvn -pl biz/ai/kuzhambu-ai-interface -am spotless:check checkstyle:check -DskipTests package` | Passed, reactor `BUILD SUCCESS` |
| Frontend apps | `cd kuzhambu-apps && pnpm run format:check && pnpm run lint && pnpm run build` | Passed; Vite emitted existing chunk-size advisory only |
| Workers | `cd kuzhambu-workers && .venv/bin/python -m ruff format --check . && .venv/bin/python -m ruff check .` | Passed |

Tests were intentionally not run in this step; final test execution is reserved for the closing verification task.

Final validation executed on 2026-07-09 after syncing `origin/main`:

| Area | Command | Result |
| --- | --- | --- |
| Java servers | `cd kuzhambu-servers && mvn spotless:check checkstyle:check test` | Passed, 58 reactor modules `SUCCESS` |
| Frontend apps | `cd kuzhambu-apps && pnpm run format:check && pnpm run lint && pnpm run build && pnpm run test` | Passed; `admin-web` 64 test files / 279 tests, `portal-web` 13 test files / 50 tests |
| Workers | `cd kuzhambu-workers && .venv/bin/python -m ruff format --check . && .venv/bin/python -m ruff check . && .venv/bin/python -m pytest -p no:capture` | Passed; 242 tests passed, 1 upstream `StarletteDeprecationWarning` |
