package codes.reactor.plugin.discordhook.webhook

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object DiscordWebHook {
    @Volatile
    private var httpClient: HttpClient? = null

    @Synchronized
    fun init() {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build()
        }
    }

    @Synchronized
    fun shutdown() {
        httpClient?.shutdown()
        httpClient = null
    }

    fun sendWebhook(message: DiscordMessage) {
        if (message.url.isBlank()) return

        val client = httpClient ?: return

        try {
            val jsonPayload = """
                {
                  "embeds": [
                    {
                      "title": ${escapeJson(message.title)},
                      "description": ${escapeJson(message.description)}
                    }
                  ]
                }
            """.trimIndent()

            val request = HttpRequest.newBuilder()
                .uri(URI.create(message.url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build()

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept { response ->
                    val statusCode = response.statusCode()
                    if (statusCode !in 200..299) {
                        System.err.println("Error on send webhook. Payload: $jsonPayload. Response: ${response.body()} (Error code: $statusCode)")
                    }
                }
                .exceptionally { e ->
                    System.err.println("Error on send webhook. Payload: $jsonPayload")
                    e.printStackTrace()
                    null
                }
        } catch (e: Exception) {
            System.err.println("Error on send webhook")
            e.printStackTrace()
        }
    }

    private fun escapeJson(value: String?): String {
        if (value == null) return "null"
        val escaped = value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\u000C", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return "\"$escaped\""
    }
}
