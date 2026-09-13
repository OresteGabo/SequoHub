package dev.orestegabo.sequohub.feature.hub

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

fun sampleLockers(): List<LockerUi> {
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
                packageLabel = if (state == LockerState.Free) "No package stored" else "Package stored at hub",
                reference = if (state == LockerState.Free) "Available" else "SQ-${4400 + rowIndex * 5 + column}",
                ageLabel = when (state) {
                    LockerState.Occupied -> if (hasFeeDue) "15 days - fee due" else "Grace ending soon"
                    LockerState.Maintenance -> "Out of service"
                    LockerState.Free -> "Ready now"
                },
                nextAction = when (state) {
                    LockerState.Occupied -> if (hasFeeDue) "Collect fee before release" else "Validate QR or pickup code"
                    LockerState.Maintenance -> "Manager action required"
                    LockerState.Free -> "Assign incoming package"
                },
                feeDueCfa = if (hasFeeDue) 1000 else 0,
                primaryAction = when (state) {
                    LockerState.Free -> "Assign Locker"
                    LockerState.Maintenance -> "View Maintenance"
                    LockerState.Occupied -> "Validate Pickup"
                },
                syncState = syncState,
            )
        }
    }
}
