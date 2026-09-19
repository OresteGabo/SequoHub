package dev.orestegabo.sequohub.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequohub.core.designsystem.component.SequoHubShapes
import dev.orestegabo.sequohub.core.localization.LocalSequoStrings
import dev.orestegabo.sequohub.core.localization.SequoStrings

@Composable
fun SequoBottomNavigation(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = Color.Transparent,
        shadowElevation = 0.dp,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SequoHubShapes.NavContainer)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.surface.copy(alpha = 0.98f),
                            colorScheme.surfaceContainer.copy(alpha = 0.94f),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    color = colorScheme.outlineVariant.copy(alpha = 0.45f),
                    shape = SequoHubShapes.NavContainer,
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MainTab.entries.forEach { tab ->
                    BottomNavItem(
                        modifier = Modifier.weight(1f),
                        tab = tab,
                        selected = selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val strings = LocalSequoStrings.current
    val containerColor by animateColorAsState(
        targetValue = if (selected) colorScheme.primaryContainer else Color.Transparent,
        label = "navItemContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
        label = "navItemContent",
    )

    Surface(
        onClick = onClick,
        color = containerColor,
        shape = SequoHubShapes.NavItem,
        tonalElevation = if (selected) 3.dp else 0.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .clip(SequoHubShapes.IconCapsule)
                    .background(if (selected) colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                    .padding(if (tab == MainTab.Hub) 10.dp else 8.dp),
            ) {
                BadgedBox(
                    badge = {
                        tab.badgeCount?.let { count ->
                            Badge {
                                Text(count.toString())
                            }
                        }
                    },
                ) {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.localizedLabel(strings),
                        tint = contentColor,
                        modifier = Modifier.size(if (tab == MainTab.Hub) 24.dp else 20.dp),
                    )
                }
            }
            Text(
                text = tab.localizedLabel(strings),
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .size(width = 16.dp, height = 3.dp)
                        .clip(SequoHubShapes.IconCapsule)
                        .background(colorScheme.primary),
                )
            }
        }
    }
}

private fun MainTab.localizedLabel(strings: SequoStrings): String =
    when (this) {
        MainTab.Hub -> strings.navHub
        MainTab.Activity -> strings.navAudit
        MainTab.Settings -> strings.navSettings
    }
