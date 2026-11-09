package dev.orestegabo.sequohub

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import dev.orestegabo.sequohub.core.auth.*
import dev.orestegabo.sequohub.core.network.createSequoHttpClient
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.*

class TestAccountLoginClientTest {
    @Test fun authenticatesHubFamilyThenVerifiesAndStoresNormalDeviceSession()=runBlocking {
        val server=HttpServer.create(InetSocketAddress("127.0.0.1",0),0);var payload:JsonObject?=null
        fun respond(x:com.sun.net.httpserver.HttpExchange,text:String) {
            val bytes=text.toByteArray();x.responseHeaders.set("Content-Type","application/json")
            x.sendResponseHeaders(200,bytes.size.toLong());x.responseBody.use{it.write(bytes)}
        }
        server.createContext("/api/auth/test-accounts/session"){x->payload=Json.parseToJsonElement(x.requestBody.readBytes().decodeToString()).jsonObject;respond(x,"""{"accessToken":"test-access","refreshToken":"test-refresh","expiresIn":3600}""")}
        server.createContext("/api/auth/me"){x->assertEquals("Bearer test-access",x.requestHeaders.getFirst("Authorization"));respond(x,"""{"id":"test-operator","active":true,"status":"ACTIVE","roles":["RELAY_PARTNER"]}""")}
        server.start();val origin="http://127.0.0.1:${server.address.port}"
        val store=object:AuthSessionStore {
            var saved:AuthSession?=null
            override suspend fun save(session:AuthSession){saved=session}
            override suspend fun get()=saved
            override suspend fun clear(){saved=null}
        }
        val session=LiveHubSession(store,origin,createSequoHttpClient(origin))
        try {
            assertEquals("test-operator",session.testLogin("hub.test","private-fixture-password").id)
            assertEquals("HUB",payload!!["appFamily"]!!.jsonPrimitive.content)
            assertEquals("SEQUO_HUB",payload!!["device"]!!.jsonObject["appSource"]!!.jsonPrimitive.content)
            assertTrue(store.get()!!.deviceId!!.startsWith("hub-test-"))
        }finally{session.close();server.stop(0)}
    }
}
