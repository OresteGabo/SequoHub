# SequoHub

SequoHub is the mobile operating app for Sequo relay point partners: local shops that act as pickup points, missed-delivery fallback locations, return drop-off counters, and short-term package storage hubs.

This repository is focused on the hub/relay workflow. It is not the full Sequo marketplace, rider app, merchant app, or admin back office, although it must integrate cleanly with those systems.

## Current Source Of Truth

The legacy Sequo documentation described a broader Lome marketplace with merchants, riders, programmed deliveries, relay points, delivery PINs, returns, and storage penalties. Those documents are useful background, but SequoHub follows the updated business rules below wherever there is a conflict:

| Area | SequoHub rule |
| --- | --- |
| Hub purpose | Local shop partners operate as relay points and package storage hubs. |
| Physical storage | Default hub layout is a 5x5 wooden locker box with 25 numbered lockers. |
| Handover verification | Package release requires a generated numeric code or QR scan plus customer ID verification. |
| Missed home delivery | Non-perishable packages can be rerouted to the nearest eligible relay point. Food and perishable items are excluded. |
| Storage penalties | Packages held longer than 2 weeks accrue extra storage fees. |
| Uncollected packages | After 1 month by default, configurable by Admin, uncollected packages are returned to the seller. |
| Customer returns | Customers can drop off returns at a relay point within 72 hours of delivery. |
| Refund control | Hubs receive returns only. Sequo collects returned items and performs final validation before refund processing. |

## Product Scope

SequoHub lets a partner shop do a small set of operational tasks reliably:

- Receive packages from Sequo riders, merchants, or authorized collectors.
- Assign packages to physical locker slots.
- Validate customer pickup using QR or numeric code.
- Verify customer identity before handover.
- Receive eligible customer returns.
- Track package age, storage fees, overdue inventory, and return-to-seller deadlines.
- Release packages or returns to Sequo collection agents.
- Report incidents such as damaged packages, wrong hub, full lockers, invalid code, suspicious pickup, or missing parcel.

## Primary Users

| User | Role in SequoHub |
| --- | --- |
| Hub partner staff | Operates the shop counter and daily locker workflows. |
| Hub manager | Reviews locker occupancy, incidents, partner earnings, and staff activity. |
| Customer | Presents a QR code or numeric code and ID to pick up a package or drop off a return. |
| Sequo rider | Drops missed-delivery packages or collects routed packages when authorized. |
| Sequo collection agent | Collects returns and uncollected packages for final processing. |
| Support/Admin | Resolves exceptions, configures rules, audits records, and overrides blocked flows. |

## Core Operational Objects

| Object | Description |
| --- | --- |
| `hub_id` | Unique ID for the relay point partner location. |
| `locker_id` | Physical locker position, using `A01` through `E05` by default for a 5x5 box. |
| `package_id` | Unique package reference from Sequo backend. |
| `pickup_code` | Generated numeric code used for pickup validation. |
| `qr_token` | Scannable validation token linked to a package, pickup, return, or collection action. |
| `return_id` | Unique return authorization reference. |
| `collection_batch_id` | Sequo collection manifest for pickups from a hub. |
| `incident_id` | Audit reference for any exception or manual support action. |

## Documentation Map

The hub operating rules are split into focused documents:

- [LOCKER_WORKFLOWS.md](LOCKER_WORKFLOWS.md): locker layout, intake, QR/code validation, Plan B missed delivery routing, customer pickup, Sequo collection, and exception handling.
- [STORAGE_PENALTIES.md](STORAGE_PENALTIES.md): two-week grace period, extra storage fee calculation, overdue alerts, uncollected package return logic, and configurable policy keys.
- [RETURNS_HANDLING.md](RETURNS_HANDLING.md): 72-hour return drop-off workflow, QR/code validation, hub responsibilities, Sequo collection, final validation, and refund boundaries.

## Required Hub Workflows

### 1. Package Intake

The hub scans the package QR code or enters the package ID, confirms the package is expected at that hub, checks that it is not food or perishable when coming from a missed home delivery, assigns a free locker, and confirms storage.

### 2. Customer Pickup

The customer presents a QR code or numeric pickup code. Hub staff verifies the code, confirms the package and locker, checks customer ID, collects any storage fees due, releases the package, invalidates the code, and frees the locker.

### 3. Plan B Missed Delivery

When a customer is not home, Sequo can reroute non-perishable packages to the nearest eligible relay point with available locker capacity. Food, hot meals, refrigerated products, fragile perishables, or other blocked categories must not be rerouted to a hub.

### 4. Storage Penalty Processing

The free storage window is 14 days. Once a package has been held for more than 2 weeks, extra storage fees accrue according to Admin configuration. After 1 month by default, the package is marked for return to seller unless Support applies a documented hold.

### 5. Customer Return Drop-Off

The customer must drop off an eligible return within 72 hours of delivery. The hub validates the return QR/code and customer ID, records package condition, stores the item, and waits for Sequo collection. The hub never approves refunds.

### 6. Sequo Collection

An authorized Sequo agent scans a collection QR or enters a collection manifest code. The hub releases only the listed packages or returns, records the handoff, and frees the corresponding lockers.

## Mobile Implementation Notes

SequoHub is currently a Kotlin Multiplatform project targeting Android and iOS.

Main folders:

- [androidApp](androidApp): Android application entry point.
- [iosApp](iosApp): iOS application entry point.
- [shared](shared/src): shared Kotlin and Compose Multiplatform code.

The hub app should be Android-first for partner shop operations, but shared business rules should stay in `shared` so iOS can remain compatible.

## Engineering Requirements

- Store all operational truth on the backend, not only on the phone.
- Treat QR and numeric codes as single-use validation credentials.
- Hash validation codes server-side and avoid storing clear codes after verification.
- Keep customer identity data minimal. Record ID type and verification result; avoid storing full ID images unless the compliance policy explicitly requires it.
- Support low-connectivity operation with a local pending-action queue, then sync with idempotency keys.
- Never release a package from an offline-only validation if the code has not been recently verified or pre-authorized by the backend.
- Audit every locker state change, manual override, failed pickup attempt, fee collection, and Sequo collection handoff.
- Display operational text in simple language suitable for shop staff.

## Useful Commands

Build the Android app:

```bash
./gradlew :androidApp:assembleDebug
```

Run shared Android host tests:

```bash
./gradlew :shared:testAndroidHostTest
```

Run iOS simulator tests:

```bash
./gradlew :shared:iosSimulatorArm64Test
```

## Definition Of Done For Hub MVP

The first SequoHub MVP is ready for pilot when:

- A hub can receive a package, assign a locker, and notify the customer.
- A customer pickup cannot complete without QR/code validation and ID verification.
- Non-perishable missed deliveries can be rerouted to eligible hubs.
- Food and perishable packages are blocked from Plan B hub storage.
- Storage age, extra fees, overdue alerts, and return-to-seller deadlines are calculated consistently.
- A customer can drop off an eligible return within 72 hours.
- Sequo can collect returns and uncollected packages using a collection manifest.
- Every sensitive action is visible to Support/Admin through audit records.
