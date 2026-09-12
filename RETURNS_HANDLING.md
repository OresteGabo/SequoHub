# Returns Handling

This document defines how SequoHub handles customer return drop-offs at Points de Relai.

## Policy Summary

| Rule | SequoHub requirement |
| --- | --- |
| Return window | Customer can drop off a return within 72 hours of delivery. |
| Drop-off location | Point de Relai only, unless Support/Admin creates an exception. |
| Verification | Return QR or numeric return code plus customer ID verification. |
| Hub responsibility | Receive, identify, inspect external condition, store, and hand over to Sequo. |
| Refund authority | Sequo performs final validation before refund processing. |
| Perishable items | Food and perishable items are not accepted for standard hub returns. |

The older legacy 14-day return window is superseded for SequoHub. The active hub rule is 72 hours from confirmed delivery.

## Return Eligibility

A return is eligible for hub drop-off only when all conditions are true:

- The order belongs to Sequo.
- The customer has an authorized `return_id`.
- The return is within 72 hours of `delivered_at`.
- The item category is return-eligible.
- The item is not food, hot meal, refrigerated, frozen, hazardous, or blocked by policy.
- The return has not already been dropped off or closed.
- The return QR or numeric return code is valid.
- The customer identity check passes or Support grants an override.

Eligibility is determined by the backend. The hub app should not let staff manually decide eligibility.

## Return Actors

| Actor | Responsibility |
| --- | --- |
| Customer | Requests return, brings package to hub, presents return QR/code and ID. |
| Hub staff | Validates QR/code, verifies ID, records visible condition, stores package. |
| Sequo collection agent | Collects return packages from hub using a manifest. |
| Sequo quality team | Performs final validation before refund processing. |
| Seller | Receives return outcome and may be charged or consulted depending on policy. |
| Support/Admin | Handles exceptions, overrides, disputes, and refund decisions. |

## Return Status Model

| Status | Meaning |
| --- | --- |
| `delivered` | Customer received the original package. |
| `return_requested` | Customer initiated a return request. |
| `return_authorized` | Backend issued `return_id`, QR token, and numeric code. |
| `return_dropoff_pending` | Customer must bring package to a Point de Relai within 72 hours. |
| `return_received_at_hub` | Hub accepted and stored the return. |
| `awaiting_sequo_collection` | Return is waiting for Sequo pickup. |
| `collected_by_sequo` | Sequo agent collected the return from the hub. |
| `under_final_validation` | Sequo quality team is checking the return. |
| `refund_approved` | Refund can be processed. |
| `refund_rejected` | Return failed final validation. |
| `closed` | Return process is complete. |
| `incident_open` | Return is blocked by an exception. |

## Customer Return Request

The customer-facing app should create the return before the hub sees it.

Required customer steps:

1. Open delivered order.
2. Select eligible item or order return.
3. Choose return reason.
4. Confirm drop-off at Point de Relai.
5. Select preferred refund method if required.
6. Receive:
   - `return_id`
   - return QR
   - numeric return code
   - deadline timestamp
   - hub drop-off instructions

The return deadline is:

```text
return_dropoff_deadline_at = delivered_at + 72 hours
```

## Hub Drop-Off Workflow

1. Customer presents return QR or numeric return code.
2. Hub staff opens `Receive Return`.
3. Staff scans QR or enters code.
4. App verifies:
   - `return_id` exists.
   - Return is assigned or allowed for this hub.
   - Current time is before `return_dropoff_deadline_at`.
   - Return has not already been received.
   - Category is eligible.
5. Staff verifies customer ID.
6. Staff checks external package condition.
7. Staff records condition and photos only if policy allows.
8. Staff assigns a free locker.
9. App marks status `return_received_at_hub`.
10. App marks package `awaiting_sequo_collection`.
11. Customer receives confirmation that the return was dropped off.
12. Hub dashboard shows the return in the next Sequo collection list.

Hub staff must not promise refund approval. The customer-facing confirmation should say that Sequo will collect and validate the item before refund processing.

## External Condition Check

Hub staff performs a counter-level check only.

Allowed observations:

- Package sealed.
- Package opened but repacked.
- Outer packaging damaged.
- Item visible.
- Liquid/leak detected.
- Label missing or unreadable.
- Wrong return reference on package.
- Customer says item is missing.

Hub staff does not determine whether the seller or Sequo is responsible. That decision belongs to Sequo final validation.

## Return Reasons

Default reason codes:

| Code | Reason |
| --- | --- |
| `wrong_item` | Wrong item received. |
| `damaged_item` | Item arrived damaged. |
| `missing_part` | Item or accessory missing. |
| `size_or_model_issue` | Size, fit, model, or variant issue. |
| `not_as_described` | Item does not match description. |
| `delivery_damage` | Damage appears related to delivery. |
| `other` | Requires customer text and Support review. |

Reason codes are customer inputs and do not automatically approve refunds.

## Sequo Collection Workflow

1. Sequo agent arrives with collection QR or `collection_batch_id`.
2. Hub staff scans the manifest.
3. App lists all return packages to release.
4. Staff retrieves packages from lockers.
5. Staff and agent confirm count and visible condition.
6. Agent accepts custody in the app.
7. Backend records `collected_by_sequo`.
8. Lockers become `free`.
9. Return enters `under_final_validation`.

If any listed return is missing, damaged, or not in the expected locker, the app must require an incident before completing the collection batch.

## Final Validation And Refund Boundary

Sequo performs final validation after collection.

Final validation may check:

- Returned item matches original order.
- Serial number, variant, size, or SKU matches.
- Item condition matches return reason.
- Package contains all expected parts.
- Return was dropped off within the 72-hour window.
- No fraud, substitution, or abuse indicators are present.

Only after final validation can Sequo approve refund processing.

Hub staff cannot:

- Approve a refund.
- Reject a refund for product reasons.
- Change return amount.
- Negotiate with the customer or seller.
- Open sealed product packaging without Support authorization.

## Late Returns

If the customer arrives after the 72-hour deadline:

1. App blocks standard return intake.
2. Staff explains that the return window has expired.
3. Staff can open a Support request if the customer disputes the deadline.
4. Staff must not store the package unless Support creates an override.

Support override must record reason, actor, time, and whether refund eligibility is still under review.

## Return Storage Rules

Return packages are stored in lockers until Sequo collection.

- Return packages should be collected on the next scheduled Sequo route.
- Customer storage penalties do not apply to return packages after successful hub drop-off.
- Delayed Sequo pickup becomes an internal operations issue, not a customer penalty.
- Return lockers should be prioritized for collection to avoid capacity pressure.

## Incidents

| Incident | Hub action |
| --- | --- |
| Invalid return code | Do not receive. Direct customer to Support. |
| Expired return window | Block intake unless Support override exists. |
| ID mismatch | Do not receive unless delegated/override policy allows it. |
| Perishable item | Reject standard return and escalate. |
| Package unsafe or leaking | Do not store in locker. Escalate immediately. |
| Locker full | Ask Admin for alternate hub, collection, or overflow approval. |
| Customer leaves package without validation | Create incident and isolate package if safe. |
| Return already received | Do not accept duplicate. Direct customer to Support. |

## Data Required For Each Return

| Field | Required |
| --- | --- |
| `return_id` | Yes |
| `order_id` | Yes |
| `package_id` | Yes when available |
| `hub_id` | Yes |
| `locker_id` | Yes after intake |
| `delivered_at` | Yes |
| `return_dropoff_deadline_at` | Yes |
| `received_at_hub_at` | Yes after intake |
| `customer_verification_result` | Yes |
| `verification_method` | QR or numeric code |
| `condition_notes` | Yes |
| `incident_id` | Required only when exception exists |
| `collection_batch_id` | Required after Sequo collection |

## Acceptance Criteria

- Hub cannot receive a return without valid QR/code.
- Hub cannot receive a standard return after 72 hours without Support override.
- Hub must verify customer ID before accepting a return.
- Hub receives and stores the return but does not approve the refund.
- Sequo collection is required before final validation.
- Return packages do not accrue customer storage penalties after successful drop-off.
- Every return status transition is auditable.
