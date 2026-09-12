# Mobile API TODO

This file tracks SequoHub mobile needs that are not currently documented as ready API contracts in
`MOBILE_API_GUIDE.md`. Do not build client networking against these until the backend contract exists.

## Hub Scan And Short-Code Validation

The first SequoHub counter UI needs one simple validation flow for shop staff:

- Scan a QR token, or type a short numeric/package/manifest code.
- Backend resolves the credential to the correct hub action:
  - customer pickup
  - package intake
  - customer return drop-off
  - Sequo collection
- Backend confirms the credential belongs to the authenticated hub.
- Backend returns only the next safe action and the minimal display data needed by staff.

Suggested contract shape:

| Method | Path | Body | Purpose |
| --- | --- | --- | --- |
| POST | `/api/hub/scan/resolve` | `hubId`, `credential`, `credentialType`, `idempotencyKey?` | Resolve a QR token or short code to the next hub workflow step. |

Response should include:

- resolved workflow type
- display reference, such as package ID, return ID, or collection batch ID
- locker ID when already assigned
- whether customer or agent ID verification is required
- fee due in integer CFA francs, if any
- blocking reason when the action cannot continue

The response must not expose raw pickup PINs, QR secrets, private customer data, or full identity document
details.

## Hub Home Summary

The first screen will eventually need a lightweight operational summary without loading an admin dashboard.

Suggested contract shape:

| Method | Path | Query | Purpose |
| --- | --- | --- | --- |
| GET | `/api/hub/summary` | `hubId` | Return minimal counter state for a partner shop. |

Useful fields:

- free locker count
- occupied locker count
- overdue package count
- pending Sequo collection count
- open incident count
- last successful sync timestamp

Keep this endpoint scoped to the authenticated hub partner and avoid admin-only settlement or monitoring data.

## Account Deletion

Settings includes a delete-account entry, but `MOBILE_API_GUIDE.md` currently documents logout and
session revocation only. Do not wire delete-account UI to a guessed route.

Suggested contract shape:

| Method | Path | Body | Purpose |
| --- | --- | --- | --- |
| POST | `/api/account/deletion-requests` | `reason?`, `confirmation`, `idempotencyKey` | Request account deletion after explicit user confirmation. |

The backend should define whether deletion is immediate, delayed, or support-reviewed. It should also make
clear what happens to audit records that must be retained for hub operations.

## User Preferences

Settings includes local theme, language, and counter workflow preferences. These can start as device-local
settings, but cross-device persistence needs an API contract.

Suggested contract shape:

| Method | Path | Body/query | Purpose |
| --- | --- | --- | --- |
| GET | `/api/preferences` | none | Read authenticated user's app preferences. |
| PATCH | `/api/preferences` | `theme`, `language`, `quickScanOnOpen`, `soundFeedback`, `largeLockerLabels` | Update safe user preferences. |

Preference values must be scoped to the authenticated user and app family. They must not include secrets,
raw QR tokens, pickup codes, or private customer data.

## Hub Notification Preferences

`MOBILE_API_GUIDE.md` documents the shared notification device and inbox endpoints, including
`SEQUO_HUB` as an `appFamily`. The first SequoHub UI can therefore show a push device registration control
and an in-app inbox.

The API repository also contains notification preference routes, but they are not documented in
`MOBILE_API_GUIDE.md` yet. Before wiring production networking, sync the guide with the implemented
controller contract:

Observed API contract:

| Method | Path | Body/query | Purpose |
| --- | --- | --- | --- |
| GET | `/api/notifications/preferences/{appFamily}/effective` | `eventType` | Read the effective preference for one notification event type. |
| PUT | `/api/notifications/preferences/{appFamily}` | `eventType`, `pushEnabled`, `inAppEnabled`, `smsEnabled`, `quietHoursStart?`, `quietHoursEnd?` | Save a notification preference for the current user and app family. |

Relevant `SEQUO_HUB` event types:

- `RELAY_PARCEL_DEPOSITED`
- `RELAY_PICKUP_CODE_CREATED`
- `RELAY_PARCEL_DELAYED`
- `RETURN_PIN_CREATED`
- `DELIVERY_PROBLEM_REPORTED`

Open product decision: the current preference controller is user-level. For a shared shop phone, decide
whether SequoHub also needs relay-point-level or device-level overrides later.
