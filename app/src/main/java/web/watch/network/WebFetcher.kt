package web.watch.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import java.security.MessageDigest

data class CompatibilityResult(
    val isCompatible: Boolean,
    val message: String,
    val htmlContent: String? = null
)

object WebFetcher {
    private val client by lazy {
        HttpClient(CIO) {
            engine {
                requestTimeout = 15_000
            }
        }
    }

    suspend fun fetchUrl(url: String): Pair<HttpResponse, String> {
        val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else url

        val response: HttpResponse = client.get(formattedUrl) {
            header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
            header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            header("Accept-Language", "en-US,en;q=0.5")
        }
        val body = response.bodyAsText()
        return Pair(response, body)
    }

    suspend fun checkCompatibility(url: String): CompatibilityResult {
        return try {
            val (response, body) = fetchUrl(url)
            val statusCode = response.status.value

            val lowerBody = body.lowercase()
            val isCloudflareOrBlocked = statusCode == 403 || statusCode == 503 ||
                    lowerBody.contains("cloudflare") ||
                    lowerBody.contains("just a moment...") ||
                    lowerBody.contains("attention required! | cloudflare") ||
                    lowerBody.contains("enable javascript and cookies to continue") ||
                    lowerBody.contains("access denied") ||
                    lowerBody.contains("ddos-guard")

            if (isCloudflareOrBlocked) {
                CompatibilityResult(
                    isCompatible = false,
                    message = "The URL is not compatible. Security/Cloudflare protection or bot blocking was detected."
                )
            } else if (statusCode in 200..299) {
                // Strip HTML tags / scripts for text representation if needed, or keep HTML
                CompatibilityResult(
                    isCompatible = true,
                    message = "Congratulations! Webpage fetched successfully and compatible with WebWatch.",
                    htmlContent = body
                )
            } else {
                CompatibilityResult(
                    isCompatible = false,
                    message = "The URL returned HTTP status code $statusCode."
                )
            }
        } catch (e: Exception) {
            CompatibilityResult(
                isCompatible = false,
                message = "Failed to fetch webpage: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    fun hashContent(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(content.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
