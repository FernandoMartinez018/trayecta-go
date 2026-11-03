package co.trayectago.app.api

import io.ktor.client.engine.HttpClientEngine

/** Motor HTTP de cada plataforma (OkHttp en Android). */
internal expect fun motorHttp(): HttpClientEngine
