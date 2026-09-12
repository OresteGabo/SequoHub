# SequoHub UI Specification

This file defines the first mobile interface direction for SequoHub, a Point de Relai app for partner shop
operators. The interface must stay fast, guided, and minimal: scan first, short code fallback second, and no
manual logistics decisions by shop staff.

## Design Principles

- Prioritize one-second counter actions: scan, validate, open locker, receive return, assign free locker.
- Keep screens quiet and operational. Avoid back-office density, marketing layouts, and decorative graphics.
- Use backend state as truth. UI copy should say when a value is resolved from backend rules.
- Every sensitive flow must show the next safe step only. Do not expose raw QR secrets, PINs, full IDs, or private customer data.
- Prefer concise cards, large touch targets, and clear status labels over free-form input.

## Layout System

| Token | Value | Usage |
| --- | --- | --- |
| Screen padding | 0dp outer horizontal, 20dp inner section padding | Full-width mobile sections without a boxed-page feel. |
| Bottom safe area | 108dp | Space for floating navigation. |
| Card radius | 8dp | Cards, sheets, buttons, grid cells, chips. |
| Card padding | 16dp | Standard content container. |
| Control height | 48dp to 56dp | Chips, text fields, primary actions. |
| Locker gap | 8dp | 5x5 grid spacing. |
| Grid cell | 1:1 aspect ratio | Stable A01 to E05 visual grid. |
| Modal sheet margin | 14dp | Bottom sheet edge inset. |

## Navigation

Floating glass-style bottom navigation with five tabs:

| Tab | Purpose |
| --- | --- |
| Scan | Full-screen scanner and package intake flow. |
| Actions | Customer pickup and return handover workflows. |
| Hub | Center/default home dashboard with metrics and 5x5 locker grid. |
| Audit | Timeline of handovers, collections, and system events. |
| Settings | Operator account, hub, appearance, language, workflow, support, and privacy settings. |

The Hub tab must remain visually centered and selected by default.
Bottom navigation items use Material icons, with the centered Hub tab slightly emphasized.

## Theme Source

The app uses Material Theme Builder output as the color foundation. The generated variable names live in:

- `core/designsystem/theme/Color.kt`
- `core/designsystem/theme/Theme.kt`
- `core/designsystem/theme/Type.kt`

When replacing the theme later, preserve the package name and stable variable names, or update only the
theme adapter while leaving feature UI untouched.

## Status Colors

| Status | Intent |
| --- | --- |
| Free | Soft green, indicates locker can be assigned. |
| Reserved | Soft indigo, intake is in progress. |
| Occupied | Soft blue, package is stored. |
| Overdue / Fee due | Soft amber, fee must be collected before release. |
| Blocked / Support hold | Soft red, standard workflow must stop. |
| Maintenance | Neutral gray, locker unavailable. |

## Typography Scale

| Role | Size | Weight |
| --- | --- | --- |
| Screen title | 31sp | Bold |
| Section title | 20sp to 22sp | SemiBold or Bold |
| Card title | 17sp to 18sp | SemiBold |
| Body | 14sp to 15sp | Regular |
| Metadata | 12sp to 13sp | Medium |
| Locker ID | 12sp | Bold |
| Navigation label | 11sp | Medium or SemiBold |

Letter spacing remains `0sp` to keep the UI crisp and readable across Android and iOS.

## Key Components

### Hub Dashboard

- Material top app bar: eyebrow, large screen title, hub name, and live sync badge.
- Material primary tabs for Grid and Attention views.
- Locker search field for direct A01-E05 lookup.
- Metric row: occupied lockers, fees due today, pending Sequo collections.
- Grid tab: 25 stable cells from `A01` to `E05`.
- Attention tab: focused locker action queue for overdue, blocked, maintenance, and reserved lockers.
- Bottom sheet: opens on locker tap, showing locker state, package age, reference, next action, and fee notice when relevant.
- Locker problem action maps to `POST /api/relay/parcels/{parcelId}/problem`.

### Customer Pickup Fee Modal

- Shows package reference and locker ID.
- Shows extra storage fee in integer CFA francs.
- States that the fee source is backend storage rules.
- Primary action: `Collect Fee & Open Locker`.
- Pickup release maps to `POST /api/relay/parcels/{parcelId}/release`.

### Scan And Receive Flow

- Full-screen scanner surface with centered square guide.
- Manual fallback text field for package IDs or QR codes.
- Material single-choice segmented control for condition flags:
  - `sealed_ok`
  - `damaged_outer_packaging`
- Best-free-locker assignment panel for one-tap intake.
- Intake assignment maps to `POST /api/relay/parcels`.
- Camera/manual credential resolution maps to `POST /api/hub/scan/resolve`, currently tracked in `MOBILE_API_TODO.md`.

### Settings

- Accessible from bottom navigation because shop operators need logout, language, and appearance controls quickly.
- Grouped sections:
  - operator and hub identity
  - theme and large-label appearance
  - language: French, English, and Éwé/Mina for Lomé/Togo context
  - counter workflow preferences
  - locker controls for temporarily closing broken or unusable lockers
  - editable opening hours for usual weekly timetable and one-day closure changes
  - notification device controls for `SEQUO_HUB` push registration and revocation
  - notification channel preferences for push, in-app, SMS, and quiet hours
  - support and privacy
  - logout and account deletion
- Account deletion and cross-device preference sync remain API TODO items until backend contracts exist.

### Activity And Notification Inbox

- Activity uses tabs for local audit events and in-app notifications.
- Unread notifications use a subtle highlighted row background, matching familiar email inbox behavior.
- The inbox maps to the documented notification endpoints:
  - read inbox: `GET /api/notifications/inbox`
  - mark read: `PATCH /api/notifications/inbox/{messageId}/read`
  - archive: `POST /api/notifications/inbox/{messageId}/archive`
  - restore: `DELETE /api/notifications/inbox/{messageId}/archive`
- Push controls in Settings map to:
  - register device: `POST /api/notifications/devices/fcm` with `appFamily=SEQUO_HUB`
  - revoke device: `DELETE /api/notifications/devices/{appFamily}/{deviceId}`
- Notification channel preferences map to:
  - read effective preference: `GET /api/notifications/preferences/{appFamily}/effective?eventType=...`
  - save preference: `PUT /api/notifications/preferences/{appFamily}`
- `MOBILE_API_TODO.md` tracks that these preference routes exist in the API repo but are missing from the
  mobile guide.

### Handover API Coverage

- Pickup code validation uses the future unified scan resolver in `MOBILE_API_TODO.md`.
- Fee collection and locker opening map to `POST /api/relay/parcels/{parcelId}/release`.
- Return validation reads `GET /api/returns/{returnId}`.
- Return receipt maps to `POST /api/returns/{returnId}/relay-dropoff`.
- Admin-only storage-fee assessment, return-to-seller closure, and operational monitoring endpoints are
  represented as read-only status/fee states in the UI, not exposed as shop-counter actions.
- Temporary locker closure is tracked in `MOBILE_API_TODO.md` until a locker availability endpoint is documented.
- Opening hours and closure exceptions are tracked in `MOBILE_API_TODO.md` until a hub timetable endpoint is documented.

## Material Components In Use

- App bars / toolbars: shared top header for each main destination.
- Badges: bottom navigation audit count and status labels.
- Chips: theme and language choices.
- Dialogs: logout and account deletion confirmations.
- Dividers: settings sections and hub search separation.
- Search: locker lookup by locker ID.
- Segmented buttons: scan condition and mutually exclusive mode choices.
- Tabs: Hub dashboard modes.
- Sheets: locker detail bottom sheet.

## Implementation Notes

- The composition root lives in `shared/src/commonMain/kotlin/dev/orestegabo/sequohub/App.kt`.
- Feature screens live under `feature/hub`, `feature/scan`, `feature/handover`, and `feature/activity`.
- Shared UI primitives live under `core/designsystem/component`.
- Theme tokens live under `core/designsystem/theme`.
- Missing API contracts are tracked in `MOBILE_API_TODO.md`; do not invent client routes not present in `MOBILE_API_GUIDE.md`.
- Bottom navigation uses Material icons and is inspired by the SchoolBridge rounded gradient navigation bar.
