package dev.orestegabo.sequohub.feature.scan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequohub.core.designsystem.component.AppScroll
import dev.orestegabo.sequohub.core.designsystem.component.CodeEntryRow
import dev.orestegabo.sequohub.core.designsystem.component.MinimalCard
import dev.orestegabo.sequohub.core.designsystem.component.PrimaryActionButton
import dev.orestegabo.sequohub.core.designsystem.component.SequoHubShapes
import dev.orestegabo.sequohub.core.designsystem.component.TopHeader
import dev.orestegabo.sequohub.core.designsystem.component.cleanCode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanReceiveScreen(
    onStartCameraScan: () -> Unit,
    onResolveManualCode: (String) -> Unit,
    onAssignLocker: () -> Unit,
) {
    var manualCode by rememberSaveable { mutableStateOf("") }
    var selectedCondition by rememberSaveable { mutableStateOf(ConditionFlag.SealedOk) }

    AppScroll {
        TopHeader(
            eyebrow = "Intake",
            title = "Scan package",
            subtitle = "Incoming parcel or Plan B delivery",
            status = "Fast path",
        )

        ScannerLaunchPanel(onStartCameraScan = onStartCameraScan)

        MinimalCard {
            Text(
                text = "Manual fallback",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            CodeEntryRow(
                value = manualCode,
                placeholder = "Package ID or QR code",
                onValueChange = { manualCode = cleanCode(it, max = 22) },
                buttonLabel = "Resolve",
                onSubmit = { onResolveManualCode(manualCode) },
            )
        }

        MinimalCard {
            Text(
                text = "Condition",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                ConditionFlag.entries.forEachIndexed { index, flag ->
                    SegmentedButton(
                        selected = selectedCondition == flag,
                        onClick = { selectedCondition = flag },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ConditionFlag.entries.size,
                        ),
                        label = { Text(flag.label) },
                    )
                }
            }
            IntakeAssignment(onAssignLocker = onAssignLocker)
        }
    }
}

@Composable
private fun ScannerLaunchPanel(
    onStartCameraScan: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = SequoHubShapes.Card,
        color = colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.72f)),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier.size(118.dp),
                shape = SequoHubShapes.Card,
                color = colorScheme.primaryContainer,
                border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.28f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(58.dp),
                    )
                }
            }

            Text(
                text = "Scan package QR",
                color = colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 18.dp),
            )
            Text(
                text = "Open the camera only when staff starts intake.",
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp, bottom = 18.dp),
            )
            PrimaryActionButton(
                modifier = Modifier.width(220.dp),
                label = "Start camera scan",
                onClick = onStartCameraScan,
            )
        }
    }
}

@Composable
private fun IntakeAssignment(
    onAssignLocker: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SequoHubShapes.Small,
        color = colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Best free locker",
                        color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = "C05",
                        color = colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            PrimaryActionButton(
                modifier = Modifier.width(116.dp),
                label = "Assign",
                onClick = onAssignLocker,
            )
        }
    }
}

private enum class ConditionFlag(val label: String, val apiValue: String) {
    SealedOk(label = "Sealed OK", apiValue = "sealed_ok"),
    DamagedOuterPackaging(label = "Damaged", apiValue = "damaged_outer_packaging"),
}
