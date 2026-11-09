# Storage Penalties

The API owns relay storage assessments. The policy summary below describes current defaults. The remaining workflow design describes proposed capabilities, including reminders, collection, and Support holds.

## Policy Summary

| Rule | Default |
| --- | --- |
| Free storage window | 8 days |
| Penalty starts | After the package has been held for more than 8 days |
| Return-to-seller threshold | 30 days from deposit, configurable |
| Fee amount | Configured by Admin, not hardcoded in the mobile app |
| Fee collection | At customer pickup unless Admin config allows prepayment |
| Clock basis | Parcel deposit timestamp |

The older legacy rule that applied penalties after 5 days is superseded for SequoHub. SequoHub uses the updated 8-day grace period and 30-day return-to-seller rule.

## Key Timestamps

| Field | Meaning |
| --- | --- |
| `stored_at_hub_at` | Package was physically confirmed inside a locker or approved overflow area. |
| `first_pickup_notification_at` | First successful or attempted customer pickup notification. |
| `available_for_pickup_at` | Storage clock anchor, equal to the deposit timestamp. |
| `penalty_start_at` | `available_for_pickup_at + free_storage_duration`. |
| `return_to_seller_at` | `available_for_pickup_at + return_after_duration`. |
| `support_hold_started_at` | Optional hold start when Support pauses penalties. |
| `support_hold_ended_at` | Optional hold end when normal clock resumes. |
| `released_at` | Final pickup or Sequo collection timestamp. |

If notification fails because of SMS/push provider outage, the backend should still record `first_pickup_notification_at` as an attempted notification and flag the package for Support review. Admin policy decides whether the penalty clock pauses for notification failures.

## Default Configuration Keys

These values must come from backend/Admin configuration.

| Config key | Default | Notes |
| --- | --- | --- |
| `free_storage_duration_hours` | `192` | 8 days x 24 hours. |
| `return_after_duration_hours` | `720` | 30 days x 24 hours. |
| `extra_storage_fee_per_day` | Admin-configured | Currency and amount set by market. |
| `penalty_rounding_mode` | `started_day` | Any started penalty day counts as one day. |
| `hub_time_zone` | Market-configured | Use hub local time for customer display. |
| `pause_penalties_during_support_hold` | `true` | Prevents unfair fees during confirmed Sequo issues. |

The mobile app must display configured values from the backend and must not hardcode fee amounts.

## Storage Clock Rules

The API starts the storage clock at deposit.

Clock anchor:

```text
available_for_pickup_at = deposited_at
```

Grace window:

```text
penalty_start_at = available_for_pickup_at + free_storage_duration_hours
```

Return threshold:

```text
return_to_seller_at = available_for_pickup_at + return_after_duration_hours
```

## Fee Calculation

No extra storage fee is due during the free storage window.

After `penalty_start_at`, calculate penalty days using the configured rounding mode.

Default formula:

```text
if now <= penalty_start_at:
    penalty_days = 0
else:
    penalty_days = ceil((now - penalty_start_at - paused_duration) / 24 hours)

extra_storage_fee = penalty_days * extra_storage_fee_per_day
```

`paused_duration` is the total time covered by approved Support holds when penalties are paused.

### Example

Assume:

- `available_for_pickup_at`: January 1 at 09:00
- `free_storage_duration_hours`: 192
- `return_after_duration_hours`: 720
- `extra_storage_fee_per_day`: configured by Admin

Result:

| Time | State |
| --- | --- |
| January 1 at 09:00 | Package becomes available for pickup. |
| January 9 at 09:00 | Free storage window ends. |
| January 9 after 09:00 | First penalty day starts. |
| January 31 at 09:00 | Package reaches 30-day return-to-seller threshold. |

## Package Age Labels

The app should show clear operational labels.

| Label | Condition |
| --- | --- |
| `New` | Stored less than 24 hours. |
| `Active` | Stored within free storage window. |
| `Grace ending soon` | Less than 48 hours before `penalty_start_at`. |
| `Fee due` | `now > penalty_start_at` and package is not on Support hold. |
| `Return pending` | `now >= return_to_seller_at` or Admin manually started return-to-seller. |
| `Support hold` | Penalties or return countdown are paused by Support. |

## Customer Notifications

Default reminders:

| Trigger | Notification |
| --- | --- |
| Package stored | Pickup code/QR, hub address, opening hours, ID requirement. |
| 7 days stored | Reminder to pick up before fees apply. |
| Penalty starts | Notice that extra storage fees now apply. |
| 23 days stored | Warning that package may be returned to seller. |
| Return threshold reached | Notice that the package is due for review, if policy allows notification. |

Notification channels are configured by market and may include push, SMS, WhatsApp, or email. Critical code and deadline notifications should have a fallback channel.

## Fee Collection At Pickup

When a customer presents a valid pickup code after fees have started:

1. App validates QR or numeric code.
2. App calculates fee from backend policy.
3. App shows amount due before locker is opened.
4. Staff collects payment through the configured methods.
5. App records payment method and amount.
6. Package can be released only after payment is confirmed or Support grants an override.

Supported payment methods are market-configured. Cash collection may be allowed for partner shops, but reconciliation must be recorded.

## Return-To-Seller Workflow

When a package reaches `return_to_seller_at`:

1. The scheduler flags the parcel for operator review.
2. An authorized operator decides the disposition. Pickup remains available subject to normal validation.
3. Hub dashboard highlights the package.
4. Admin schedules Sequo collection.
5. Sequo agent collects the package using a collection manifest.
6. Package status becomes `released_to_sequo`.
7. Admin routes the package back to the seller.
8. Seller and customer are notified according to policy.
9. Any unpaid storage fee is attached to the customer account or handled by Support policy.

The 30-day threshold is configurable. For legal, seasonal, or seller-specific policies, Admin may choose a different duration, but the app must always show the active policy for the package.

## Support Holds

Support may pause fees or return-to-seller countdown when the delay is caused by Sequo or hub operations.

Valid examples:

- Hub was closed during published opening hours.
- Package was assigned to the wrong hub.
- Customer pickup code was not delivered.
- Package was blocked by an unresolved Sequo incident.
- Locker access problem prevented pickup.

Invalid examples:

- Customer forgot to pick up.
- Customer did not bring ID.
- Customer refused to pay the fee.
- Customer gave the code to someone not authorized for pickup.

Every hold must include reason, staff/admin actor, start time, end time, and audit note.

## Hub Dashboard Requirements

The hub app must show:

- Total occupied lockers.
- Packages within free window.
- Packages with fees due.
- Packages approaching fee start.
- Packages approaching return-to-seller.
- Packages already pending Sequo collection.
- Fee amount due at pickup.
- Support holds and incidents.

The locker grid should visually distinguish `occupied`, `overdue`, `return_to_seller_pending`, `blocked`, and `maintenance`.

## Edge Cases

| Case | Rule |
| --- | --- |
| Package moved between lockers in same hub | Keep original `available_for_pickup_at`. |
| Package transferred to another hub | Admin decides whether clock resets; default is no reset unless transfer was Sequo fault. |
| Customer arrives exactly at penalty start time | No fee if `now <= penalty_start_at`; fee applies only after that instant. |
| Backend unavailable | Do not release fee-due package unless there is a recent signed validation token or Support override. |
| Package marked perishable after intake | Open incident and schedule urgent Sequo collection. |
| Customer disputes fee | Staff records dispute; Support may override or require payment before release. |
| Locker damaged with package inside | Open incident, move package with two-person confirmation if possible, preserve original clock. |

## Acceptance Criteria

- Penalties do not start before the package has been held for more than 8 days.
- Fee calculation uses backend configuration.
- Fee amount is visible before handover.
- Support holds pause penalties only when policy allows.
- Packages at the 30-day threshold are due for operator review.
- Return-to-seller collection requires an authorized Sequo manifest.
- Old 5-day penalty behavior is not used in SequoHub.
