package dev.orestegabo.sequohub.feature.live

import dev.orestegabo.sequohub.feature.settings.AppLanguage

internal fun hubStatus(raw: String, language: AppLanguage): String {
    val labels = mapOf(
        "AVAILABLE" to ("Available" to "Disponible"), "OCCUPIED" to ("Occupied" to "Occupé"), "MAINTENANCE" to ("Maintenance" to "Maintenance"),
        "NORMAL" to ("Operational" to "En service"), "HUB_PAUSED" to ("Relay paused" to "Relais en pause"), "HUB_DISABLED" to ("Relay disabled" to "Relais désactivé"),
        "INTAKE_PAUSED_PICKUP_ALLOWED" to ("Intake paused; pickup available" to "Dépôts en pause ; retraits disponibles"), "SERVICE_RESTRICTED" to ("Service restricted" to "Service limité"),
        "Deposited" to ("Ready for pickup" to "Prêt au retrait"), "Delayed" to ("Pickup overdue" to "Retrait en retard"), "ReturnToSellerReview" to ("Storage review due" to "Revue de garde requise"),
        "PickedUp" to ("Picked up" to "Retiré"), "ReturnedToSeller" to ("Returned to seller" to "Rendu au vendeur"), "CollectedBySequo" to ("Collected by SEQUO" to "Collecté par SEQUO"),
        "Requested" to ("Return requested" to "Retour demandé"), "AwaitingRelayDropoff" to ("Awaiting return dropoff" to "Dépôt du retour attendu"), "DroppedAtRelay" to ("Return received at relay" to "Retour reçu au relais"),
        "ReceivedBySequo" to ("Received by SEQUO" to "Reçu par SEQUO"), "RefundPending" to ("Refund instruction pending" to "Instruction de remboursement en attente"), "Rejected" to ("Rejected" to "Refusé"),
        "Deposit" to ("Deposit recorded" to "Dépôt enregistré"), "Release" to ("Pickup recorded" to "Retrait enregistré"), "Delay" to ("Delay recorded" to "Retard enregistré"), "Review" to ("Storage review" to "Revue de garde"), "Problem" to ("Problem reported" to "Problème signalé"),
        "RELAY_DROPOFF" to ("Return dropoff" to "Dépôt du retour"), "SEQUO_COLLECTION" to ("SEQUO collection" to "Collecte SEQUO"),
        "MONDAY" to ("Monday" to "Lundi"), "TUESDAY" to ("Tuesday" to "Mardi"), "WEDNESDAY" to ("Wednesday" to "Mercredi"), "THURSDAY" to ("Thursday" to "Jeudi"), "FRIDAY" to ("Friday" to "Vendredi"), "SATURDAY" to ("Saturday" to "Samedi"), "SUNDAY" to ("Sunday" to "Dimanche"),
        "read" to ("Mark read" to "Marquer comme lu"), "archive" to ("Archive" to "Archiver"), "unarchive" to ("Restore" to "Restaurer"),
    )
    return labels[raw]?.let { if(language == AppLanguage.French) it.second else it.first } ?: raw.replace('_', ' ')
}
