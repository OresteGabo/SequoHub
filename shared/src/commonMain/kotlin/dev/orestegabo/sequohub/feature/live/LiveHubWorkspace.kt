package dev.orestegabo.sequohub.feature.live

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequohub.core.auth.*
import dev.orestegabo.sequohub.core.hub.*
import dev.orestegabo.sequohub.core.network.*
import dev.orestegabo.sequohub.feature.auth.AuthScreen
import dev.orestegabo.sequohub.feature.legal.LegalScreen
import dev.orestegabo.sequohub.feature.settings.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun HubSessionGate(settings: SettingsUiState, onSettingsChange: (SettingsUiState) -> Unit,
    onGoogleSignIn: suspend () -> GoogleSignInResult, onExploreDemo: () -> Unit) {
    val store = rememberAuthSessionStore()
    val session = remember(store) { LiveHubSession(store) }
    val google = remember { AuthApiClient() }
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<HubCurrentUser?>(null) }
    var restoring by remember { mutableStateOf(true) }; var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }; var legal by remember { mutableStateOf(false) }
    var fixture by remember { mutableStateOf(HubFixtureAccess()) }
    fun t(en: String, fr: String) = if(settings.language == AppLanguage.French) fr else en
    DisposableEffect(session) { onDispose { session.close(); google.close() } }
    LaunchedEffect(session) {
        try { user = session.restore() } catch(e: Exception) { if(e is CancellationException) throw e; error = hubFailure(e, settings.language) }
        finally { restoring = false }
        if (NetworkConfig.IsLocalFixture) try { fixture = session.fixtureAccess() } catch(e: Exception) { if(e is CancellationException) throw e }
    }
    if (restoring) { Box(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) { Text(t("Checking saved session…", "Vérification de la session…")) }; return }
    user?.let { actual ->
        key(actual.id) { LiveHubWorkspace(session, actual, settings, onSettingsChange) {
            scope.launch { try { session.logout() } catch(e: Exception) { if(e is CancellationException) throw e } finally { user = null } }
        } }
        return
    }
    if (legal) { HubBackHandler(true) { legal = false }; LegalScreen(onBack = { legal = false }); return }
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        Box(Modifier.weight(1f)) { AuthScreen(language = settings.language, onLanguageChange = { onSettingsChange(settings.copy(language = it)) },
            onLogin = { error = t("Email/password sign-in is not connected in this app. Use an available verified provider or the labelled demo.", "La connexion par e-mail n'est pas connectée dans cette app. Utilisez un fournisseur vérifié disponible ou la démo indiquée.") },
            googleSignInInProgress = busy,
            onGoogleLogin = {
                if (!busy) scope.launch {
                    busy = true; error = null
                    try { when(val result = onGoogleSignIn()) {
                        is GoogleSignInResult.Success -> user = session.accept(google.loginWithGoogle(result.idToken).session)
                        GoogleSignInResult.Cancelled -> Unit
                        is GoogleSignInResult.Failure -> error = t("Google sign-in could not finish. Check provider configuration and try again.", "La connexion Google a échoué. Vérifiez sa configuration et réessayez.")
                    } } catch(e: Exception) { if(e is CancellationException) throw e; error = hubFailure(e, settings.language) }
                    finally { busy = false }
                }
            }, onAppleLogin = { error = t("Apple sign-in is not connected in this app.", "La connexion Apple n'est pas connectée dans cette app.") }, onPrivacyTermsClick = { legal = true }, appleSignInAvailable = false, emailSignInAvailable = false) }
        Surface(tonalElevation = 2.dp) { Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
            if (NetworkConfig.IsLocalFixture && fixture.enabled && fixture.mode == "LOCAL_FIXTURE") {
                Text("LOCAL FIXTURE · ${NetworkConfig.ReleaseId}", style = MaterialTheme.typography.labelSmall)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { fixture.accounts.forEach { account ->
                    OutlinedButton(modifier = Modifier.fillMaxWidth(), enabled = !busy, onClick = { scope.launch { busy = true; error = null
                        try { user = session.fixtureLogin(account.key) } catch(e: Exception) { if(e is CancellationException) throw e; error = hubFailure(e, settings.language) } finally { busy = false }
                    } }) { Text(account.label) }
                } }
            }
            TextButton(enabled = !busy, onClick = onExploreDemo) { Text(t("Explore demo · sample data only", "Explorer la démo · données fictives")) }
        } }
    }
    error?.let { message -> AlertDialog(onDismissRequest = { error = null }, title = { Text(t("Sign-in unavailable", "Connexion indisponible")) }, text = { Text(message) }, confirmButton = { TextButton(onClick = { error = null }) { Text("OK") } }) }
}

@Composable
private fun LiveHubWorkspace(session: LiveHubSession, user: HubCurrentUser, settings: SettingsUiState,
    onSettingsChange: (SettingsUiState) -> Unit, onLogout: () -> Unit) {
    val repository = remember(session) { LiveScopedHubRepository(session) }
    val scope = rememberCoroutineScope()
    var sites by remember { mutableStateOf<List<AssignedHub>>(emptyList()) }; var site by remember { mutableStateOf<AssignedHub?>(null) }
    var snapshot by remember { mutableStateOf<LiveHubSnapshot?>(null) }; var selected by remember { mutableStateOf<LiveHubParcel?>(null) }
    var inbox by remember { mutableStateOf<List<LiveHubNotification>?>(null) }
    var tab by remember { mutableStateOf(0) }; var busy by remember { mutableStateOf(false) }; var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }; var pending by remember { mutableStateOf<HubWriteIntent?>(null) }
    var confirmation by remember { mutableStateOf<HubWriteIntent?>(null) }; var legal by remember { mutableStateOf(false) }
    var raw by remember { mutableStateOf("") }; var qr by remember { mutableStateOf(false) }; var identity by remember { mutableStateOf(false) }; var note by remember { mutableStateOf("") }
    var returnId by remember { mutableStateOf("") }; var returnPin by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf<HubOpeningHoursResponse?>(null) }
    var savedPreferences by remember { mutableStateOf<LiveHubPreferences?>(null) }
    var preferenceError by remember { mutableStateOf<String?>(null) }
    var exceptionDate by remember { mutableStateOf("") }; var exceptionClosed by remember { mutableStateOf(true) }
    var exceptionOpens by remember { mutableStateOf("") }; var exceptionCloses by remember { mutableStateOf("") }; var exceptionReason by remember { mutableStateOf("") }
    var deletionConfirmation by remember { mutableStateOf("") }; var deletionReason by remember { mutableStateOf("") }

    var generation by remember { mutableStateOf(0) }
    fun t(en: String, fr: String) = if(settings.language == AppLanguage.French) fr else en
    fun applyPreferences(value: LiveHubPreferences) {
        val theme = ThemeMode.entries.firstOrNull { it.name.equals(value.theme, true) }
            ?: error("Saved appearance is not supported by this version.")
        val language = AppLanguage.entries.firstOrNull { it.code.equals(value.language.substringBefore('-'), true) }
            ?: error("Saved language is not supported by this version.")
        savedPreferences = value
        onSettingsChange(settings.copy(themeMode = theme, language = language, quickScanOnOpen = value.quickScanOnOpen,
            soundFeedback = value.soundFeedback, largeLockerLabels = value.largeLockerLabels))
    }
    fun clearCredential() { raw = ""; qr = false; identity = false; returnPin = "" }
    suspend fun load(target: AssignedHub, expected: Int) {
        val actual = repository.snapshot(target)
        if (generation == expected) { snapshot = actual; hours = actual.openingHours; selected = selected?.let { old -> actual.parcels.firstOrNull { it.id == old.id } } }
    }
    fun refresh(target: AssignedHub? = site) {
        if (busy) return
        val expected = generation + 1; generation = expected; snapshot = null; selected = null; sites = emptyList(); site = null; inbox = null; hours = null; clearCredential(); error = null; notice = null; busy = true
        scope.launch { try {
            val allowed = repository.sites()
            if (generation != expected) return@launch
            sites = allowed
            val actual = target?.let { old -> allowed.firstOrNull { it.id == old.id } } ?: allowed.singleOrNull()
            site = actual
            if (actual != null) load(actual, expected)
        } catch(e: Exception) { if(e is CancellationException) throw e; if(generation == expected) error = hubFailure(e, settings.language) }
        finally { if(generation == expected) busy = false } }
    }
    fun runWrite(intent: HubWriteIntent) {
        val target = site ?: return; val expected = generation
        if (busy) return
        pending = intent; busy = true; error = null; notice = null
        scope.launch { try {
            repository.write(intent)
            snapshot = null; selected = null; clearCredential()
            load(target, expected)
            if(generation == expected) { pending = null; notice = if(intent.path == "/api/account/deletion-requests") t("Deletion request recorded for review. Your account has not been erased.", "Demande de suppression enregistrée pour examen. Votre compte n’est pas effacé.") else t("Recorded successfully.", "Enregistré avec succès.") }
        } catch(e: Exception) {
            if(e is CancellationException) throw e
            if(generation == expected) {
                snapshot = null; selected = null; hours = null; clearCredential(); error = hubFailure(e, settings.language)
                if(e is SequoApiException && e.statusCode in setOf(400, 401, 403, 404, 409, 422)) pending = null
            }
        } finally { if(generation == expected) busy = false } }
    }
    LaunchedEffect(repository) {
        refresh()
        try { applyPreferences(repository.preferences()) }
        catch(e: Exception) { if(e is CancellationException) throw e; preferenceError = hubFailure(e, settings.language) }
    }
    HubBackHandler(selected != null || tab != 0 || legal) { when { legal -> legal = false; selected != null -> { selected = null; clearCredential() }; else -> tab = 0 } }
    if (legal) { LegalScreen(onBack = { legal = false }); return }
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) { Text("SEQUO HUB", style = MaterialTheme.typography.titleLarge); Text(site?.name ?: t("Assigned relay", "Relais affecté"), style = MaterialTheme.typography.bodyMedium) }
            TextButton(enabled = !busy, onClick = { refresh() }) { Text(t("Refresh", "Actualiser")) }
        }
        if(NetworkConfig.IsLocalFixture) Text("LOCAL FIXTURE · ${NetworkConfig.ReleaseId}", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelSmall)
        TextButton(enabled = !busy, onClick = { snapshot = null; inbox = null; pending = null; clearCredential(); onLogout() }) { Text(t("Sign out", "Se déconnecter")) }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }; notice?.let { Text(it) }
            pending?.let { intent -> Surface(color = MaterialTheme.colorScheme.tertiaryContainer) { Column(Modifier.padding(14.dp)) {
                Text(t("The result is uncertain. Retry the identical request or refresh before a new action. Leaving this session discards the local retry.", "Résultat incertain. Réessayez la même demande ou actualisez avant une nouvelle action. Quitter la session efface cette reprise locale."))
                TextButton(enabled = !busy, onClick = { runWrite(intent) }) { Text(t("Retry same action", "Réessayer la même action")) }
                TextButton(enabled = !busy, onClick = { pending = null; refresh() }) { Text(t("Refresh and review", "Actualiser et vérifier")) }
            } } }
            if (!busy && error == null && sites.isEmpty()) Text(t("No relay is assigned to this account. Ask SEQUO to review the account and site membership.", "Aucun relais n'est affecté à ce compte. Demandez à SEQUO de vérifier son affectation."))
            if (sites.size > 1) Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { sites.forEach { candidate -> OutlinedButton(modifier = Modifier.fillMaxWidth(), enabled = !busy && pending == null, onClick = { site = candidate; refresh(candidate) }) { Text(candidate.name) } } }
            val actual = snapshot
            if (actual != null) when(tab) {
                0 -> {
                    Text(actual.control.hub.staffMessage ?: hubStatus(actual.control.effectiveMode.name, settings.language), style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(enabled = false, onClick = {}) { Text(t("Camera scanner unavailable", "Lecteur QR caméra indisponible")) }
                    Text(t("Logical slots", "Emplacements logiques"), style = MaterialTheme.typography.titleMedium)
                    if(actual.slots.isEmpty()) Text(t("No storage slots are configured.", "Aucun emplacement n'est configuré."))
                    actual.slots.chunked(3).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { slot -> OutlinedCard(Modifier.weight(1f)) { Column(Modifier.padding(12.dp)) { Text(slot.lockerCode, style = if(settings.largeLockerLabels) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium); Text(hubStatus(slot.status, settings.language), style = MaterialTheme.typography.labelSmall) } } } } }
                    Text(t("Parcels", "Colis"), style = MaterialTheme.typography.titleMedium)
                    if(actual.parcels.isEmpty()) Text(t("No parcel records at this relay.", "Aucun colis enregistré dans ce relais."))
                    actual.parcels.forEach { parcel -> OutlinedCard(onClick = { selected = parcel; clearCredential(); note = "" }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) { Text(parcel.id); Text(hubStatus(parcel.status, settings.language)); parcel.depositedAt?.let { Text(hubHistoryTimestamp(it), style = MaterialTheme.typography.bodySmall) }
                            if(parcel.custody?.reviewDue == true) Text(t("Operator review due", "Revue opérateur requise"), color = MaterialTheme.colorScheme.error)
                        }
                    } }
                    OutlinedTextField(value = raw, onValueChange = { raw = it.take(256) }, label = { Text(t("Manual QR / pickup code", "QR manuel / code de retrait")) }, modifier = Modifier.fillMaxWidth())
                    Button(enabled = !busy && pending == null && raw.isNotBlank(), onClick = { val credential = raw; scope.launch { busy = true; error = null
                        try { val result = repository.scan(actual.site.id, credential); require(result.parcelId != null) { t("This code is not a supported pickup. Use Returns for a return ID and PIN.", "Ce code ne correspond pas à un retrait pris en charge. Utilisez Retours avec l'identifiant et le PIN.") }
                            require(result.blockingReason == null) { result.blockingReason.orEmpty() }; selected = repository.parcel(actual.site.id, result.parcelId); qr = !credential.matches(Regex("[0-9]{6}")); identity = false; notice = t("Assessed storage fee", "Frais de garde évalués") + ": ${result.feeDueCfa} CFA"
                        } catch(e: Exception) { if(e is CancellationException) throw e; selected = null; error = hubFailure(e, settings.language) } finally { busy = false }
                    } }) { Text(t("Resolve with server", "Vérifier sur le serveur")) }
                    Text(t("Enter the customer QR value or PIN manually. New deposits are recorded by assigned riders or SEQUO staff.", "Saisissez la valeur QR ou le PIN du client. Les nouveaux dépôts sont enregistrés par les livreurs affectés ou par SEQUO."), style = MaterialTheme.typography.bodySmall)
                }
                1 -> {
                    Text(t("Customer return intake", "Réception de retour client"), style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(returnId, { returnId = it.take(128) }, label = { Text(t("Return ID", "Identifiant du retour")) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(returnPin, { returnPin = it.filter(Char::isDigit).take(6) }, label = { Text(t("Server return PIN", "PIN de retour du serveur")) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(note, { note = it.take(1000) }, label = { Text(t("Visible package condition (optional)", "État visible du colis (facultatif)")) }, modifier = Modifier.fillMaxWidth())
                    Button(enabled = !busy && pending == null && returnId.isNotBlank() && returnPin.length == 6, onClick = { confirmation = repository.returnIntent(actual.site.id, returnId.trim(), returnPin, note) }) { Text(t("Confirm physical intake", "Confirmer la réception physique")) }
                    Text(t("Eligibility, route and PIN validation are owned by the server. Intake does not approve or execute a refund.", "Le serveur vérifie l'éligibilité, le relais et le PIN. La réception n'autorise ni n'exécute un remboursement."))
                    if(actual.returns.isEmpty()) Text(t("No received return records at this relay.", "Aucun retour reçu dans ce relais."))
                    actual.returns.forEach { returned ->
                        OutlinedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(returned.id, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(hubStatus(returned.status, settings.language))
                                HubRecordHistory(hubHistoryRows(emptyList(), listOf(returned)), settings.language)
                            }
                        }
                    }
                }
                2 -> {
                    Text(t("Persisted custody history", "Historique de garde enregistré"), style = MaterialTheme.typography.titleMedium)
                    val events = hubHistoryRows(actual.parcels, actual.returns)
                    if (events.isEmpty()) Text(t("No history yet.", "Aucun historique.")) else events.forEach { HubHistoryCard(it, settings.language) }
                    Button(enabled = !busy, onClick = { scope.launch { busy = true; inbox = null; error = null; try { inbox = repository.inbox() } catch(e: Exception) { if(e is CancellationException) throw e; error = hubFailure(e, settings.language) } finally { busy = false } } }) { Text(t("Read my notification inbox", "Lire mes notifications")) }
                    inbox?.let { messages -> if(messages.isEmpty()) Text(t("Your inbox is empty.", "Votre boîte est vide.")); messages.forEach { message -> OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text(message.title); Text(message.body); Text(hubHistoryTimestamp(message.createdAt), style = MaterialTheme.typography.labelSmall)
                        Row { listOf(if(message.readAt == null) "read" else null, if(message.archivedAt == null) "archive" else "unarchive").filterNotNull().forEach { action -> TextButton(enabled = !busy, onClick = { scope.launch { busy = true; inbox = null; try { repository.notification(message.id, action); inbox = repository.inbox() } catch(e: Exception) { if(e is CancellationException) throw e; error = hubFailure(e, settings.language) } finally { busy = false } } }) { Text(hubStatus(action, settings.language)) } } }
                    } } } }
                }
                else -> {
                    Text(user.displayName ?: user.id, style = MaterialTheme.typography.titleMedium); Text(actual.site.name)
                    Row { AppLanguage.entries.forEach { language -> TextButton(onClick = { onSettingsChange(settings.copy(language = language)) }) { Text(language.label) } } }
                    Row { ThemeMode.entries.forEach { mode -> TextButton(onClick = { onSettingsChange(settings.copy(themeMode = mode)) }) { Text(mode.label) } } }
                    preferenceError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(t("Large slot labels", "Grands libellés d’emplacement"))
                        Switch(settings.largeLockerLabels, { onSettingsChange(settings.copy(largeLockerLabels = it)) }, enabled = !busy)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(t("Sound preference", "Préférence sonore"))
                        Switch(settings.soundFeedback, { onSettingsChange(settings.copy(soundFeedback = it)) }, enabled = !busy)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(t("Quick-entry preference", "Préférence d’accès rapide"))
                        Switch(settings.quickScanOnOpen, { onSettingsChange(settings.copy(quickScanOnOpen = it)) }, enabled = !busy)
                    }
                    Button(enabled = !busy && savedPreferences != null, onClick = {
                        scope.launch {
                            busy = true; preferenceError = null
                            try { applyPreferences(repository.savePreferences(settings.themeMode.name.uppercase(), settings.language.code.lowercase(), settings.quickScanOnOpen, settings.soundFeedback, settings.largeLockerLabels)); notice = t("Preferences saved.", "Préférences enregistrées.") }
                            catch(e: Exception) { if(e is CancellationException) throw e; savedPreferences = null; preferenceError = hubFailure(e, settings.language) }
                            finally { busy = false }
                        }
                    }) { Text(t("Save preferences", "Enregistrer les préférences")) }
                    TextButton(enabled = !busy, onClick = {
                        scope.launch { busy = true; preferenceError = null
                            try { applyPreferences(repository.preferences()) } catch(e: Exception) { if(e is CancellationException) throw e; savedPreferences = null; preferenceError = hubFailure(e, settings.language) }
                            finally { busy = false }
                        }
                    }) { Text(t("Reload saved preferences", "Relire les préférences")) }
                    Text(t("Opening hours", "Horaires"), style = MaterialTheme.typography.titleMedium)
                    val current = hours
                    if(current != null) {
                        OutlinedTextField(current.timezone.orEmpty(), { hours = current.copy(timezone = it.take(128)) }, label = { Text(t("Time zone (e.g. Africa/Lome)", "Fuseau horaire (ex. Africa/Lome)")) }, modifier = Modifier.fillMaxWidth())
                        if(current.weeklyHours.isEmpty()) Text(t("Not configured: choose hours explicitly. No default opening schedule is assumed.", "Non configuré : choisissez les horaires. Aucun horaire d'ouverture n'est supposé."))
                        val days = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
                        days.forEach { day -> val value = current.weeklyHours.firstOrNull { it.dayOfWeek == day } ?: WeeklyOpeningHourResponse(day, false)
                            fun edit(next: WeeklyOpeningHourResponse) { hours = current.copy(weeklyHours = (current.weeklyHours.filterNot { it.dayOfWeek == day } + next).sortedBy { days.indexOf(it.dayOfWeek) }) }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(hubStatus(day, settings.language)); Switch(value.isOpen, { open -> edit(value.copy(isOpen = open, opensAt = value.opensAt.takeIf { open }, closesAt = value.closesAt.takeIf { open })) }, enabled = !busy) }
                            if(value.isOpen) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(value.opensAt.orEmpty(), { edit(value.copy(opensAt = it.take(8))) }, Modifier.weight(1f), label = { Text(t("Opens HH:mm", "Ouverture HH:mm")) }); OutlinedTextField(value.closesAt.orEmpty(), { edit(value.copy(closesAt = it.take(8))) }, Modifier.weight(1f), label = { Text(t("Closes HH:mm", "Fermeture HH:mm")) }) }
                        }
                        Text(t("Dated opening exception", "Exception d’ouverture datée"), style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(exceptionDate, { exceptionDate = it.take(10) }, label = { Text(t("Date YYYY-MM-DD", "Date AAAA-MM-JJ")) }, modifier = Modifier.fillMaxWidth())
                        Row { Checkbox(exceptionClosed, { exceptionClosed = it }); Text(t("Closed on this date", "Fermé à cette date")) }
                        if(!exceptionClosed) {
                            OutlinedTextField(exceptionOpens, { exceptionOpens = it.take(8) }, label = { Text(t("Opens HH:mm", "Ouverture HH:mm")) }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(exceptionCloses, { exceptionCloses = it.take(8) }, label = { Text(t("Closes HH:mm", "Fermeture HH:mm")) }, modifier = Modifier.fillMaxWidth())
                        }
                        OutlinedTextField(exceptionReason, { exceptionReason = it.take(255) }, label = { Text(t("Exception reason", "Motif de l’exception")) }, modifier = Modifier.fillMaxWidth())
                        TextButton(enabled = !busy && exceptionDate.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")), onClick = {
                            val entry = OpeningHourExceptionResponse(exceptionDate, exceptionClosed, if(exceptionClosed) null else exceptionOpens, if(exceptionClosed) null else exceptionCloses, exceptionReason.takeIf { it.isNotBlank() })
                            hours = current.copy(exceptions = (current.exceptions.filterNot { it.date == entry.date } + entry).sortedBy { it.date })
                            exceptionDate = ""; exceptionReason = ""
                        }) { Text(t("Add or replace dated exception", "Ajouter ou remplacer l’exception")) }
                        Button(enabled = !busy && pending == null && !current.timezone.isNullOrBlank(), onClick = { scope.launch { busy = true; error = null; try { repository.saveHours(current); load(actual.site, generation); notice = t("Opening hours saved.", "Horaires enregistrés.") } catch(e: Exception) { if(e is CancellationException) throw e; snapshot = null; hours = null; error = hubFailure(e, settings.language) } finally { busy = false } } }) { Text(t("Save opening hours", "Enregistrer les horaires")) }
                        current.exceptions.forEach { exception ->
                            Text("${exception.date}: ${if(exception.isClosed) t("Closed", "Fermé") else "${exception.opensAt}–${exception.closesAt}"} ${exception.reason.orEmpty()}")
                            TextButton(enabled = !busy, onClick = { hours = current.copy(exceptions = current.exceptions.filterNot { it.date == exception.date }) }) { Text(t("Remove this exception before saving", "Retirer cette exception avant enregistrement")) }
                        }
                    }
                    Text(t("Slot maintenance", "Maintenance des emplacements"), style = MaterialTheme.typography.titleMedium)
                    actual.slots.forEach { slot -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("${slot.lockerCode}: ${hubStatus(slot.status, settings.language)}"); TextButton(enabled = !busy && slot.status != "OCCUPIED" && pending == null, onClick = { scope.launch { busy = true; error = null
                        try { repository.setLockerMaintenance(actual.site.id, slot.lockerId, slot.status != "MAINTENANCE"); load(actual.site, generation) } catch(e: Exception) { if(e is CancellationException) throw e; snapshot = null; error = hubFailure(e, settings.language) } finally { busy = false }
                    } }) { Text(if(slot.status == "MAINTENANCE") t("Available", "Disponible") else t("Maintenance", "Maintenance")) } } }
                    Text(t("For batch collection, push setup or refund decisions, contact SEQUO operations.", "Pour les lots de collecte, les notifications push et les décisions de remboursement, contactez les opérations SEQUO."))
                    Text(t("Account deletion request", "Demande de suppression du compte"), style = MaterialTheme.typography.titleMedium)
                    Text(t("SEQUO reviews the request. Submission does not immediately erase your account or operational records.", "SEQUO examine la demande. Son envoi n’efface pas immédiatement votre compte ni les enregistrements opérationnels."))
                    OutlinedTextField(deletionReason, { deletionReason = it.take(500) }, label = { Text(t("Reason (optional)", "Motif (facultatif)")) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(deletionConfirmation, { deletionConfirmation = it.take(32) }, label = { Text("DELETE_MY_ACCOUNT") }, modifier = Modifier.fillMaxWidth())
                    OutlinedButton(enabled = !busy && pending == null && deletionConfirmation == "DELETE_MY_ACCOUNT", onClick = { confirmation = repository.deletionIntent(deletionConfirmation, deletionReason) }) { Text(t("Request account deletion", "Demander la suppression du compte")) }
                    TextButton(onClick = { legal = true }) { Text(t("Privacy and terms", "Confidentialité et conditions")) }
                    TextButton(enabled = !busy, onClick = { snapshot = null; inbox = null; pending = null; clearCredential(); onLogout() }) { Text(t("Sign out", "Se déconnecter")) }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf(t("Hub", "Relais"), t("Returns", "Retours"), t("History", "Historique"), t("Settings", "Réglages")).forEachIndexed { index, label -> TextButton(onClick = { if(!busy) { tab = index; selected = null; clearCredential() } }) { Text(label) } }
        }
    }
    selected?.let { parcel -> AlertDialog(onDismissRequest = { if(!busy) { selected = null; clearCredential() } }, title = { Text(t("Parcel detail", "Détail du colis")) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(parcel.id); Text(hubStatus(parcel.status, settings.language)); parcel.depositedAt?.let { Text(t("Deposited", "Déposé") + ": " + hubHistoryTimestamp(it)) }
            parcel.custody?.let { Text(t("Fee eligibility", "Éligibilité aux frais") + ": ${it.storageFeeEligible}"); Text(t("Review due", "Revue requise") + ": ${it.reviewDueAt}") }
            HubRecordHistory(hubHistoryRows(listOf(parcel), emptyList()), settings.language)
            OutlinedTextField(raw, { raw = it.take(256); identity = false }, label = { Text(t("Customer PIN or QR nonce", "PIN client ou contenu QR")) })
            Row { Checkbox(qr, { qr = it; identity = false }); Text(t("QR value", "Valeur QR")) }
            Row { Checkbox(identity, { identity = it }); Text(t("Customer identity checked against presented document", "Identité du client vérifiée sur le document présenté")) }
            OutlinedTextField(note, { note = it.take(1000) }, label = { Text(t("Problem note", "Note du problème")) })
            TextButton(enabled = !busy && pending == null && note.isNotBlank(), onClick = { confirmation = repository.problemIntent(parcel.id, note) }) { Text(t("Report problem", "Signaler un problème")) }
            Text(t("Confirm identity without taking a photograph.", "Vérifiez l'identité sans la photographier."), style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { TextButton(enabled = !busy && pending == null && identity && raw.isNotBlank() && parcel.status in setOf("Deposited", "Delayed", "ReturnToSellerReview"), onClick = {
        try { confirmation = repository.releaseIntent(parcel.relayPointId, parcel.id, raw, qr, identity) } catch(e: Exception) { error = hubFailure(e, settings.language) }
    }) { Text(t("Confirm handoff", "Confirmer la remise")) } }, dismissButton = { TextButton(enabled = !busy, onClick = { selected = null; clearCredential() }) { Text(t("Close", "Fermer")) } }) }
    confirmation?.let { intent -> AlertDialog(onDismissRequest = { confirmation = null }, title = { Text(if(intent.path == "/api/account/deletion-requests") t("Confirm account deletion request", "Confirmer la demande de suppression") else t("Confirm physical operation", "Confirmer l'opération physique")) }, text = { Text(if(intent.path == "/api/account/deletion-requests") t("Submit your account deletion request for SEQUO review? This does not immediately erase your account.", "Envoyer la demande de suppression à SEQUO pour examen ? Le compte n’est pas effacé immédiatement.") else t("Confirm that this operation was physically completed at the selected relay. The server will validate the credential, ownership and current state.", "Confirmez que l'opération est réalisée physiquement dans le relais sélectionné. Le serveur vérifie le code, l'affectation et l'état courant.")) }, confirmButton = { TextButton(onClick = { confirmation = null; runWrite(intent) }) { Text(t("Confirm", "Confirmer")) } }, dismissButton = { TextButton(onClick = { confirmation = null }) { Text(t("Cancel", "Annuler")) } }) }
}

private fun hubFailure(error: Throwable, language: AppLanguage): String {
    val french = language == AppLanguage.French
    if(error is SequoApiException) return when(error.statusCode) {
        401 -> if(french) "Session expirée. Reconnectez-vous." else "Session expired. Sign in again."
        403 -> if(french) "Accès refusé pour ce compte ou ce relais. Actualisez l'affectation." else "This account or relay scope is not authorized. Refresh the assignment."
        400, 404, 409, 422 -> if(french) "Le serveur a refusé la demande. Vérifiez le code, l'identité, le relais et l'état courant, puis actualisez." else "The server refused this request. Check the entered values and refresh the current record."
        else -> if(french) "Service indisponible. Aucune réussite locale n'est supposée." else "Service unavailable. No local success is assumed."
    }
    return if(french) "Impossible de confirmer l'état du serveur. Vérifiez la connexion et réessayez." else "Server state could not be confirmed. Check the connection and retry."
}
