package dev.orestegabo.sequohub.feature.hub

import dev.orestegabo.sequohub.core.localization.SequoStrings

enum class LockerState(
    val label: String,
    val shortLabel: String,
) {
    Free(label = "Free", shortLabel = "Free"),
    Occupied(label = "Occupied", shortLabel = "Full"),
    Maintenance(label = "Maintenance", shortLabel = "Closed"),
}

enum class LockerSyncState(
    val label: String,
) {
    Synced(label = "Synced"),
    Pending(label = "Pending sync"),
    Failed(label = "Sync failed"),
}

data class LockerUi(
    val id: String,
    val state: LockerState,
    val packageLabel: String,
    val reference: String,
    val ageLabel: String,
    val nextAction: String,
    val feeDueCfa: Int,
    val primaryAction: String,
    val syncState: LockerSyncState,
)

val LockerUi.needsAttention: Boolean
    get() = state == LockerState.Maintenance || feeDueCfa > 0 || syncState != LockerSyncState.Synced

fun sampleLockers(strings: SequoStrings): List<LockerUi> {
    val states = listOf(
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Occupied,
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Occupied,
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Maintenance,
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Occupied,
        LockerState.Occupied,
        LockerState.Occupied,
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Maintenance,
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Occupied,
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Occupied,
    )

    return ('A'..'E').flatMapIndexed { rowIndex, row ->
        (1..5).map { column ->
            val id = "$row${column.toString().padStart(2, '0')}"
            val state = states[rowIndex * 5 + column - 1]
            val hasFeeDue = id in setOf("A04", "D01", "E03")
            val syncState = when (id) {
                "B03", "D01" -> LockerSyncState.Pending
                "C04" -> LockerSyncState.Failed
                else -> LockerSyncState.Synced
            }
            LockerUi(
                id = id,
                state = state,
                packageLabel = if (state == LockerState.Free) strings.clear else strings.packageStored,
                reference = if (state == LockerState.Free) strings.clear else "SQ-${4400 + rowIndex * 5 + column}",
                ageLabel = when (state) {
                    LockerState.Occupied -> if (hasFeeDue) "15 days - ${strings.feeDue}" else strings.pickupReady
                    LockerState.Maintenance -> strings.underMaintenance
                    LockerState.Free -> strings.clear
                },
                nextAction = when (state) {
                    LockerState.Occupied -> if (hasFeeDue) strings.collectBeforeRelease else strings.validate
                    LockerState.Maintenance -> strings.needsAttention
                    LockerState.Free -> strings.assign
                },
                feeDueCfa = if (hasFeeDue) 1000 else 0,
                primaryAction = when (state) {
                    LockerState.Free -> strings.assign
                    LockerState.Maintenance -> strings.maintenance
                    LockerState.Occupied -> strings.validate
                },
                syncState = syncState,
            )
        }
    }
}
