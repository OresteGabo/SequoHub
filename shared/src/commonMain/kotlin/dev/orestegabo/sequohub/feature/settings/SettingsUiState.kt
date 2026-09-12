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
    val staffRole: String = "Point de Relai operator",
    val themeMode: ThemeMode = ThemeMode.System,
    val language: AppLanguage = AppLanguage.French,
    val quickScanOnOpen: Boolean = true,
    val soundFeedback: Boolean = true,
    val largeLockerLabels: Boolean = false,
)
