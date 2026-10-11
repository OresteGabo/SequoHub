# Sequo Hub MVP Status

Source contract: `/Users/muhirwagabooreste/AndroidStudioProjects/SequoService/sequo-api/docs/contracts/contrat_mvp_sequo.tex`

This file tracks only contract-listed MVP scope for the relay/hub module.

| Contract item | Status | Progress | Evidence | Remaining MVP work |
| --- | --- | ---: | --- | --- |
| Relay receives package and assigns code/QR/locker | In progress | 70% | `feature/scan`, `feature/hub`, API hub/relay domains | Verify server-connected receive and locker assignment. |
| Relay validates customer pickup | In progress | 70% | `feature/handover`, activity screens | Finalize invalid/expired/already-used code states with API. |
| Relay stores package history | In progress | 70% | `feature/activity`, backend relay domains | Ensure history is backend-persisted. |
| Relay return drop-off | In progress | 60% | `RETURNS_HANDLING.md`, handover/activity surfaces | Complete customer-to-hub return validation and condition capture. |
| Relay excludes food/perishables/sensitive products | Done | 85% | API relay parcel policy | Surface policy failures clearly in hub UI. |
| Storage fee/overdue inventory visibility | In progress | 65% | `STORAGE_PENALTIES.md`, API relay fee domain | Complete dashboard fee/overdue states. |
| Collection manifest handoff | In progress | 55% | Hub docs and relay API domains | Finish collection-agent manifest screen if endpoint supports it. |

Recommended verification:

```bash
./gradlew :androidApp:compileDebugKotlin
```
