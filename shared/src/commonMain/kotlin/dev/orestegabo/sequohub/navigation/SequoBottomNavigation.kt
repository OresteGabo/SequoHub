package dev.orestegabo.sequohub.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.orestegabo.sequo.design.SequoNavDestination
import dev.orestegabo.sequo.design.SequoNavigationBar
import dev.orestegabo.sequohub.core.localization.LocalSequoStrings
import dev.orestegabo.sequohub.core.localization.SequoStrings

@Composable
fun SequoBottomNavigation(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalSequoStrings.current
    SequoNavigationBar(
        destinations = MainTab.entries.map { tab ->
            SequoNavDestination(tab.name, tab.localizedLabel(strings),
                if (tab == selectedTab) tab.selectedIcon else tab.unselectedIcon,
                tab.badgeCount ?: 0)
        },
        selectedKey = selectedTab.name,
        onSelect = { key -> MainTab.entries.firstOrNull { it.name == key }?.let(onTabSelected) },
        modifier = modifier,
    )
}

private fun MainTab.localizedLabel(strings: SequoStrings): String = when (this) {
    MainTab.Hub -> strings.navHub
    MainTab.Activity -> strings.navAudit
    MainTab.Settings -> strings.navSettings
}
