package dev.orestegabo.sequohub.feature.settings

enum class ThemeMode(val label: String) {
    System(label = "System"),
    Light(label = "Light"),
    Dark(label = "Dark"),
}

enum class AppLanguage(val label: String, val code: String) {
    French(label = "Français", code = "FR"),
    English(label = "English", code = "EN"),
    EweMina(label = "Éwé / Mina", code = "EWE"),
}

data class SettingsUiState(
    val hubName: String = "Lomé Relay 04",
    val staffName: String = "Counter staff",
    val staffRole: String = "Relay point operator",
    val themeMode: ThemeMode = ThemeMode.System,
    val language: AppLanguage = AppLanguage.French,
    val quickScanOnOpen: Boolean = true,
    val soundFeedback: Boolean = true,
    val largeLockerLabels: Boolean = false,
    val openingHours: List<HubOpeningDay> = defaultOpeningHours(),
    val closeToday: Boolean = false,
    val todayClosingTime: String = "17:00",
)

data class HubOpeningDay(
    val day: String,
    val isOpen: Boolean,
    val opensAt: String,
    val closesAt: String,
)

fun defaultOpeningHours(): List<HubOpeningDay> =
    listOf(
        HubOpeningDay(day = "Mon", isOpen = true, opensAt = "08:30", closesAt = "17:00"),
        HubOpeningDay(day = "Tue", isOpen = true, opensAt = "08:30", closesAt = "17:00"),
        HubOpeningDay(day = "Wed", isOpen = true, opensAt = "08:30", closesAt = "17:00"),
        HubOpeningDay(day = "Thu", isOpen = true, opensAt = "08:30", closesAt = "17:00"),
        HubOpeningDay(day = "Fri", isOpen = true, opensAt = "08:30", closesAt = "17:00"),
        HubOpeningDay(day = "Sat", isOpen = true, opensAt = "09:00", closesAt = "14:00"),
        HubOpeningDay(day = "Sun", isOpen = false, opensAt = "09:00", closesAt = "14:00"),
    )
