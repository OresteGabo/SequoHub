package dev.orestegabo.sequohub

import dev.orestegabo.sequohub.core.hub.*
import dev.orestegabo.sequohub.feature.live.*
import dev.orestegabo.sequohub.feature.settings.AppLanguage
import kotlin.test.*

class HubHistoryPresentationTest {
    @Test fun timestampIsNormalizedToExplicitUtcWithoutInventingLegacyTime() {
        assertEquals("2026-11-08 10:03:04 UTC", hubHistoryTimestamp("2026-11-08T12:03:04.987+02:00"))
        assertEquals("2026-11-08 10:03:04 UTC", hubHistoryTimestamp("2026-11-08T10:03:04Z"))
        assertEquals("legacy recorded time", hubHistoryTimestamp("legacy recorded time"))
    }

    @Test fun combinedHistoryKeepsRealActorReferenceDetailsAndChronologicalOrder() {
        val parcel = LiveHubParcel("parcel-one", "site-one", status = "PickedUp", updatedAt = "2026-11-08T10:00:00Z",
            history = listOf(LiveCustodyEvent("Deposit", "2026-11-08T09:00:00Z", "received", "rider-one"),
                LiveCustodyEvent("Pickup", "2026-11-08T10:00:00Z", actorUserId = "operator-one")))
        val returned = LiveHubReturn("return-one", "order-one", "CollectedBySequo", requestedAt = "2026-11-08T08:00:00Z",
            history = listOf(LiveReturnEvent("SEQUO_COLLECTION", "2026-11-08T12:30:00+02:00", "operations-one", "two packages")))
        val rows = hubHistoryRows(listOf(parcel), listOf(returned))
        assertEquals(listOf("SEQUO_COLLECTION", "Pickup", "Deposit"), rows.map { it.action })
        assertEquals(listOf("return-one", "parcel-one", "parcel-one"), rows.map { it.reference })
        assertEquals(listOf("operations-one", "operator-one", "rider-one"), rows.map { it.actorUserId })
        assertEquals("two packages", rows.first().details)
        assertEquals("received", rows.last().details)
        assertEquals("Collecte SEQUO", hubStatus(rows.first().action, AppLanguage.French))
    }

    @Test fun simultaneousLegacyEventsAreRetainedWithoutFabricatedActor() {
        val event = LiveCustodyEvent("Deposit", "2026-11-08T09:00:00Z")
        val parcel = LiveHubParcel("parcel", "site", status = "Deposited", updatedAt = event.createdAt,
            history = listOf(event, event.copy(message = "second recorded note")))
        val rows = hubHistoryRows(listOf(parcel), emptyList())
        assertEquals(2, rows.size)
        assertTrue(rows.all { it.actorUserId == null })
        assertEquals("second recorded note", rows.last().details)
        assertEquals("Collecte SEQUO", hubStatus("SequoCollection", AppLanguage.French))
    }
}
