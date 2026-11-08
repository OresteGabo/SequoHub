package dev.orestegabo.sequohub.core.network
import io.ktor.http.Url
object NetworkConfig {
    const val ProductionBaseUrl = "https://api.sequoservice.com"
    const val BaseUrl = GeneratedSequoBuildConfig.ApiBaseUrl
    const val ReleaseId = GeneratedSequoBuildConfig.ReleaseId
    const val Environment = GeneratedSequoBuildConfig.Environment
    val IsLocalFixture: Boolean get() = Environment == "LOCAL_FIXTURE" && runCatching {
        Url(BaseUrl).host.lowercase().removeSurrounding("[", "]") in setOf("localhost", "127.0.0.1", "10.0.2.2", "::1")
    }.getOrDefault(false)
}
