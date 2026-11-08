package dev.orestegabo.sequohub.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

@Composable
actual fun rememberAuthSessionStore(): AuthSessionStore = remember { IosAuthSessionStore() }

/** Device-only Keychain storage; no silent plaintext fallback on a Security error. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosAuthSessionStore(private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
    private val json: Json = Json) : AuthSessionStore {
    override suspend fun save(session: AuthSession) {
        write(json.encodeToString(session))
        defaults.removeObjectForKey(Account)
    }
    override suspend fun get(): AuthSession? {
        read()?.let { return decode(it) }
        val legacy = defaults.stringForKey(Account) ?: return null
        val supported = decode(legacy)
        write(json.encodeToString(supported))
        // Remove the old value only after successful write and verified Keychain readback.
        check(read() == json.encodeToString(supported)) { "Saved sign-in could not be protected. Sign in again." }
        defaults.removeObjectForKey(Account)
        return supported
    }
    override suspend fun clear() {
        val status = query().dictionary { SecItemDelete(it) }
        check(status == errSecSuccess || status == errSecItemNotFound) { "Saved sign-in could not be cleared. Try again." }
        defaults.removeObjectForKey(Account)
    }
    private fun decode(raw: String): AuthSession = json.decodeFromString<AuthSession>(raw).also {
        require(it.accessToken.isNotBlank() && it.refreshToken.isNotBlank() && it.expiresIn > 0) { "Saved sign-in is invalid. Sign in again." }
    }
    private fun query(): Map<Any?, Any?> = mapOf(kSecClass to kSecClassGenericPassword, kSecAttrService to "dev.orestegabo.sequohub.auth", kSecAttrAccount to Account)
    private fun write(value: String) {
        val data = NSString.create(string = value).dataUsingEncoding(NSUTF8StringEncoding) ?: error("Saved sign-in could not be protected.")
        val attributes = mapOf<Any?, Any?>(kSecValueData to data, kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
        var status = query().dictionary { key -> attributes.dictionary { values -> SecItemUpdate(key, values) } }
        if(status == errSecItemNotFound) status = (query() + attributes).dictionary { SecItemAdd(it, null) }
        check(status == errSecSuccess) { "Saved sign-in could not be protected. Try again." }
    }
    private fun read(): String? = memScoped {
        val result = alloc<CFTypeRefVar>(); result.value = null
        val status = (query() + mapOf<Any?, Any?>(kSecReturnData to true, kSecMatchLimit to kSecMatchLimitOne)).dictionary { SecItemCopyMatching(it, result.ptr) }
        try {
            when(status) {
                errSecItemNotFound -> null
                errSecSuccess -> {
                    val raw = result.value
                    result.value = null // Ownership passes to ARC through CFBridgingRelease.
                    val data = raw?.let { CFBridgingRelease(it) as? NSData }
                        ?: error("Saved sign-in could not be read. Sign in again.")
                    NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
                        ?: error("Saved sign-in could not be decoded. Sign in again.")
                }
                else -> error("Protected sign-in is unavailable. Unlock this device and retry.")
            }
        } finally { result.value?.let { CFRelease(it) } }
    }
    private fun <T> Map<Any?, Any?>.dictionary(block: (CFDictionaryRef) -> T): T {
        val retained = toRetainedCFDictionary()
        return try { block(retained) } finally { CFRelease(retained) }
    }
    private companion object { const val Account = "auth_session" }
}

/** Preserve native CF constants; boxing their pointers into NSDictionary produces errSecParam. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun Map<Any?, Any?>.toRetainedCFDictionary(): CFDictionaryRef {
    val dictionary = checkNotNull(CFDictionaryCreateMutable(null, 0,
        kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr))
    try {
        forEach { (key, value) ->
            if (key != null && value != null) {
                val (nativeKey, releaseKey) = key.asCFValue()
                val (nativeValue, releaseValue) = value.asCFValue()
                try { CFDictionarySetValue(dictionary, nativeKey, nativeValue) }
                finally {
                    if (releaseKey) CFRelease(nativeKey)
                    if (releaseValue) CFRelease(nativeValue)
                }
            }
        }
        return dictionary
    } catch (error: Throwable) { CFRelease(dictionary); throw error }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun Any.asCFValue(): Pair<CFTypeRef?, Boolean> = when (this) {
    is CPointer<*> -> this to false
    is Boolean -> (if (this) kCFBooleanTrue else kCFBooleanFalse) to false
    else -> CFBridgingRetain(if (this is String) NSString.create(string = this) else this) to true
}
