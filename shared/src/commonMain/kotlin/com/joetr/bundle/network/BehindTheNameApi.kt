package com.joetr.bundle.network

import com.joetr.bundle.network.data.Either
import com.joetr.bundle.network.data.behind.BehindTheNameApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

private const val BASE_URL =
    "https://www.behindthename.com/api/related.json?usage=eng&key=jo483589395"

class BehindTheNameApi {

    @OptIn(ExperimentalSerializationApi::class)
    private val client by lazy {
        HttpClient {
            install(Logging)
            defaultRequest {
                url(BASE_URL)
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        ignoreUnknownKeys = true
                        explicitNulls = false
                        encodeDefaults = false
                    },
                )
            }
        }
    }

    suspend fun getRelatedNames(name: String): Either<BehindTheNameApiResponse, Throwable> {
        return try {
            val response: BehindTheNameApiResponse = client.get {
                parameter("name", name)
            }.body()
            Either.success(response)
        } catch (throwable: Throwable) {
            Either.failure(throwable)
        }
    }
}
