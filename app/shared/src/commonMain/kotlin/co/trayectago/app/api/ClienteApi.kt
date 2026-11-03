package co.trayectago.app.api

import co.trayectago.app.auth.Autenticador
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

/** Cliente de la API de Trayecta Go escrito a mano desde el contrato OpenAPI. */
class ClienteApi(private val urlBase: String, private val autenticador: Autenticador, motor: HttpClientEngine) {
    private val formato = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val http = HttpClient(motor) {
        install(ContentNegotiation) {
            json(formato)
            json(formato, contentType = ContentType("application", "problem+json"))
        }
    }

    suspend fun salud(): Salud = pedir(autenticado = false) { http.get("$urlBase/v1/salud") }.body()

    suspend fun yo(): Usuario = pedir { token -> http.get("$urlBase/v1/me") { bearerAuth(token!!) } }.body()

    /** Ejecuta la petición; ante un 401 renueva el token una sola vez y reintenta. */
    private suspend fun pedir(autenticado: Boolean = true, peticion: suspend (token: String?) -> HttpResponse): HttpResponse {
        try {
            val token = if (autenticado) autenticador.tokenDeAcceso() ?: throw ErrorApi.NoAutenticado else null
            var respuesta = peticion(token)
            if (autenticado && respuesta.status == HttpStatusCode.Unauthorized) {
                val renovado = autenticador.tokenDeAcceso(forzarRenovacion = true) ?: throw ErrorApi.NoAutenticado
                respuesta = peticion(renovado)
                if (respuesta.status == HttpStatusCode.Unauthorized) throw ErrorApi.NoAutenticado
            }
            if (!respuesta.status.isSuccess()) {
                throw ErrorApi.Servidor(respuesta.status.value, runCatching { respuesta.body<Problema>() }.getOrNull())
            }
            return respuesta
        } catch (e: IOException) {
            throw ErrorApi.SinConexion
        }
    }
}
