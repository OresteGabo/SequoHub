@file:OptIn(kotlin.time.ExperimentalTime::class)

package dev.orestegabo.sequohub.feature.live

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.design.SequoDesign
import dev.orestegabo.sequo.design.SequoPanel
import dev.orestegabo.sequohub.core.hub.LiveHubParcel
import dev.orestegabo.sequohub.core.hub.LiveHubReturn
import dev.orestegabo.sequohub.feature.settings.AppLanguage
import kotlin.time.Instant

internal data class HubHistoryEntry(
    val action: String, val createdAt: String, val reference: String,
    val actorUserId: String?, val details: String?,
)

internal fun hubHistoryRows(parcels: List<LiveHubParcel>, returns: List<LiveHubReturn>): List<HubHistoryEntry> =
    (parcels.flatMap { parcel -> parcel.history.map { HubHistoryEntry(it.type, it.createdAt, parcel.id, it.actorUserId, it.message) } } +
        returns.flatMap { returned -> returned.history.map { HubHistoryEntry(it.action, it.createdAt, returned.id, it.actorUserId, it.metadata) } })
        .sortedWith(compareByDescending<HubHistoryEntry> { runCatching { Instant.parse(it.createdAt) }.getOrNull() })

internal fun hubHistoryTimestamp(raw: String): String = runCatching {
    Instant.parse(raw).toString().removeSuffix("Z").substringBefore('.').replace('T', ' ') + " UTC"
}.getOrElse { raw }

@Composable
internal fun HubHistoryCard(entry: HubHistoryEntry, language: AppLanguage) {
    val french = language == AppLanguage.French
    var expanded by remember(entry) { mutableStateOf(false) }
    SequoPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(SequoDesign.CardPadding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(hubStatus(entry.action, language), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(hubHistoryTimestamp(entry.createdAt), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(entry.reference, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            entry.actorUserId?.takeIf(String::isNotBlank)?.let { actor ->
                Text((if (french) "Enregistré par : " else "Recorded by: ") + actor,
                    style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp)) {
                Text(if (expanded) { if (french) "Masquer les détails" else "Hide details" }
                    else { if (french) "Voir les détails" else "Show details" })
            }
            if (expanded) SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(entry.reference, style = MaterialTheme.typography.bodySmall)
                    Text(entry.createdAt, style = MaterialTheme.typography.bodySmall)
                    entry.actorUserId?.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    entry.details?.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}

@Composable
internal fun HubRecordHistory(entries: List<HubHistoryEntry>, language: AppLanguage) {
    if (entries.isEmpty()) return
    var expanded by remember(entries) { mutableStateOf(false) }
    TextButton(onClick = { expanded = !expanded }) {
        Text((if (language == AppLanguage.French) "Historique" else "History") + " (" + entries.size + ")")
    }
    if (expanded) entries.forEach { HubHistoryCard(it, language) }
}
