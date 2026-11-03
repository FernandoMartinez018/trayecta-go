package co.trayectago.app.api

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun motorHttp(): HttpClientEngine = OkHttp.create()
