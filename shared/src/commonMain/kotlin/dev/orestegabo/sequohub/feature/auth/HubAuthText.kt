package dev.orestegabo.sequohub.feature.auth

import dev.orestegabo.sequohub.feature.settings.AppLanguage

internal data class HubAuthText(
    val counter: String, val google: String, val signingIn: String, val apple: String, val email: String,
    val termsIntro: String, val termsLink: String,
    val findTitle: String, val findDetail: String, val handoffTitle: String, val handoffDetail: String,
    val historyTitle: String, val historyDetail: String,
)

internal fun hubAuthText(language: AppLanguage): HubAuthText =
    if (language == AppLanguage.French) HubAuthText(
        "Comptoir relais", "Continuer avec Google", "Connexion…", "Continuer avec Apple", "Utiliser un e-mail",
        "En continuant, vous acceptez", "Confidentialité et conditions",
        "Retrouver un colis", "Saisissez le code de retrait ou la valeur QR du client pour vérifier le colis sur le serveur.",
        "Confirmer la remise", "Vérifiez l’identité du client et confirmez le retrait physique dans votre relais.",
        "Garder un historique clair", "Consultez les dépôts, retraits et retours enregistrés dans votre relais.",
    ) else HubAuthText(
        "Relay counter", "Continue with Google", "Signing in…", "Continue with Apple", "Use email instead",
        "By continuing you accept", "Privacy & Terms",
        "Find a parcel", "Enter the customer’s pickup code or QR value to check the parcel with the server.",
        "Confirm the handoff", "Check the customer’s identity and confirm the physical pickup at your assigned relay.",
        "Keep a clear history", "Review recorded deposits, pickups and returns for your relay.",
    )
