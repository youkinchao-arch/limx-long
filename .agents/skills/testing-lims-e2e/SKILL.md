---
name: testing-lims-e2e
description: Bring up the Hongqiao LIMS stack locally and run end-to-end UI tests. Use when verifying any LIMS feature (document/report workflows, personnel, equipment, dashboard) through the browser.
---

# LIMS End-to-End Testing

Full-stack: React + TS + Vite frontend → Java 17 + Spring Boot 3.3 backend → PostgreSQL (Flyway migrations).

## Local stack startup

1. **PostgreSQL** (docker container `lims-pg`, db `lims_test`, user/pass `lims`/`lims`):
   ```
   docker start lims-pg   # if it exists but is stopped
   docker exec lims-pg psql -U lims -d lims_test -c "SELECT 1"
   ```
   If the container doesn't exist, create one exposing 5432 with those credentials.

2. **Backend** (port 8000). Build then run with explicit Spring env vars (env var naming matters — use SPRING_DATASOURCE_*):
   ```
   cd backend && mvn -q -DskipTests package
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/lims_test \
   SPRING_DATASOURCE_USERNAME=lims SPRING_DATASOURCE_PASSWORD=lims \
   JWT_SECRET=local-e2e-test-secret-key-1234567890abcd \
   CORS_ORIGINS=http://localhost:5173,http://localhost:5174,http://localhost:3000 \
   SERVER_PORT=8000 java -jar target/lims-backend-0.1.0.jar
   ```
   Flyway applies migrations on boot. Running a newer branch against an existing `lims_test` applies only the new migration version on top — no need to wipe the DB.

3. **Frontend** (Vite, port 5173; proxies `/api` → http://localhost:8000):
   ```
   cd frontend && npm run dev
   ```

## CORS gotcha
Backend only allows origins in `CORS_ORIGINS`. If Vite falls back to 5174 (5173 busy), login fails with a 403 the UI shows as a generic "操作失败/failed". Include both 5173 and 5174 in `CORS_ORIGINS`. Production uses nginx same-origin, so set `CORS_ORIGINS` to the real domain there.

## Login
- Default admin: `admin` / `admin123` (dev only). Admin has `*` permissions (so both `:write` and `:approve`).
- Token login: `POST /api/v1/auth/login` form-encoded `username` + `password` → `{access_token}`.

## Verifying writes server-side (avoid frontend-only illusions)
- Audit trail: `GET /api/v1/audit/logs?page=1&page_size=50` with `Authorization: Bearer <token>` records CREATE/SUBMIT/APPROVE/REJECT/LOGIN etc. with actor + IP.
- Direct DB checks: `docker exec lims-pg psql -U lims -d lims_test -c "..."`.

## Document approval workflow (P2 module #1)
Page: sidebar **Documents / 文件管理** (`/documents`). State machine: `draft → under_review → approved → obsolete`, plus `under_review → rejected → (resubmit) under_review`.

UI behavior driven by status + permissions (`frontend/src/pages/DocumentsPage.tsx`):
- draft/rejected + `document:write` → **Submit** (Popconfirm)
- under_review + `document:approve` → **Approve** (modal: optional effective date + comment) / **Reject** (modal: required reason)
- approved + `document:approve` → **Retire/Obsolete**
- all statuses → **History** drawer (append-only `document_reviews`)

Status tag colors (`CrudTable.tsx`): draft=grey, under_review=orange, approved=green, rejected=red, obsolete=grey.

Golden-path test: create DOC → Submit (→under_review/orange) → Approve w/ effective date (→approved/green) → check History shows Submitted+Approved; then a second doc → Submit → Reject (empty reason must be blocked with "Please enter a rejection reason") → (rejected/red) → Submit again (→under_review). History is append-only (Submit→Reject→Submit all retained). Toggle 中文 to confirm i18n (批准/退回/作废/流转记录, 审核中/已批准).

## Test environment limits
- No Chinese IME: typing CJK into inputs is dropped. Use ASCII values when creating records; seeded Chinese data displays fine.
- antd deprecation warnings in console (`destroyOnClose`, static `message`, detached `useForm`) are library noise, not defects.

## Devin Secrets Needed
None for local testing — admin/admin123 and a local JWT secret suffice. Real deployments inject `JWT_SECRET` and `CORS_ORIGINS` via environment.
