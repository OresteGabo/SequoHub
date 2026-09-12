package dev.orestegabo.sequohub.feature.hub

enum class LockerState(
    val label: String,
    val shortLabel: String,
) {
    Free(label = "Free", shortLabel = "Free"),
    Reserved(label = "Reserved", shortLabel = "Hold"),
    Occupied(label = "Occupied", shortLabel = "Full"),
    Overdue(label = "Overdue", shortLabel = "Fee"),
    Blocked(label = "Blocked", shortLabel = "Stop"),
    Maintenance(label = "Maintenance", shortLabel = "Maint"),
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
)

val LockerUi.needsAttention: Boolean
    get() = state in setOf(
        LockerState.Reserved,
        LockerState.Overdue,
        LockerState.Blocked,
        LockerState.Maintenance,
    )

fun sampleLockers(): List<LockerUi> {
    val states = listOf(
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Reserved,
        LockerState.Overdue,
        LockerState.Free,
        LockerState.Occupied,
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Maintenance,
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Reserved,
        LockerState.Occupied,
        LockerState.Overdue,
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Blocked,
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Occupied,
        LockerState.Occupied,
        LockerState.Free,
        LockerState.Reserved,
    )

    return ('A'..'E').flatMapIndexed { rowIndex, row ->
        (1..5).map { column ->
            val id = "$row${column.toString().padStart(2, '0')}"
            val state = states[rowIndex * 5 + column - 1]
            LockerUi(
                id = id,
                state = state,
                packageLabel = if (state == LockerState.Free) "No package stored" else "Package stored at hub",
                reference = if (state == LockerState.Free) "Available" else "SQ-${4400 + rowIndex * 5 + column}",
                ageLabel = when (state) {
                    LockerState.Overdue -> "15 days - fee due"
                    LockerState.Occupied -> "Grace ending soon"
                    LockerState.Reserved -> "Intake in progress"
                    LockerState.Blocked -> "Support hold"
                    LockerState.Maintenance -> "Out of service"
                    LockerState.Free -> "Ready now"
                },
                nextAction = when (state) {
                    LockerState.Overdue -> "Collect fee before release"
                    LockerState.Occupied -> "Validate QR or pickup code"
                    LockerState.Reserved -> "Complete package intake"
                    LockerState.Blocked -> "Wait for Support"
                    LockerState.Maintenance -> "Manager action required"
                    LockerState.Free -> "Assign incoming package"
                },
                feeDueCfa = if (state == LockerState.Overdue) 1000 else 0,
                primaryAction = when (state) {
                    LockerState.Free -> "Assign Locker"
                    LockerState.Reserved -> "Resume Intake"
                    LockerState.Blocked -> "Open Support Note"
                    LockerState.Maintenance -> "View Maintenance"
                    else -> "Validate Pickup"
                },
            )
        }
    }
}
