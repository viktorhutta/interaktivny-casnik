package sk.hutta.server.payment

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.body
import sk.hutta.server.service.badGateway
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

enum class GatewayPaymentStatus { PAID, AUTHORIZED, REJECTED, PENDING }

data class CreatedPayment(val paymentRequestId: Long, val gatewayUrl: String)

// S bránou finby komunikuje len server. ID projektu a tajný kľúč sú v .env.
@Component
class FinbyClient(
    @Value("\${app.finby.url}") url: String,
    @Value("\${app.finby.project-id}") private val projectId: String,
    @Value("\${app.finby.secret-key}") private val secretKey: String,
) {
    private val restClient = RestClient.create(url)
    private var accessToken: String? = null
    private var accessTokenExpiresAt = Instant.MIN

    // Prístupový token platí 30 minút, nový sa pýta až minútu pred jeho vypršaním.
    @Synchronized
    private fun accessToken(): String {
        val token = accessToken
        if (token != null && Instant.now().isBefore(accessTokenExpiresAt)) return token
        val response = restClient.post()
            .uri("/api/oauth2/token")
            .headers { it.setBasicAuth(projectId, secretKey) }
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body("grant_type=client_credentials")
            .retrieve()
            .body<TokenResponse>()!!
        accessToken = response.accessToken
        accessTokenExpiresAt = Instant.now().plusSeconds(response.expiresIn - 60)
        return response.accessToken
    }

    // Údaje karty zadáva hosť až na platobnej stránke brány, server ich nikdy nedostane.
    fun createPayment(amount: BigDecimal, reference: String): CreatedPayment {
        val request = mapOf(
            "PaymentMethod" to "Card",
            "MerchantIdentification" to mapOf("ProjectId" to projectId.toLong()),
            "PaymentInformation" to mapOf(
                "Amount" to mapOf("Amount" to amount.setScale(2, RoundingMode.HALF_UP), "Currency" to "EUR"),
                "Localization" to "SK",
                "IsRedirect" to true,
                "References" to mapOf("MerchantReference" to reference),
                "CardTransaction" to mapOf("PaymentType" to "Purchase"),
            ),
        )
        val response = try {
            restClient.post()
                .uri("/api/Payments/Payment")
                .headers { it.setBearerAuth(accessToken()) }
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body<CreatePaymentResponse>()!!
        } catch (e: RestClientException) {
            throw badGateway("Platobná brána je nedostupná")
        }
        if (response.resultInfo.resultCode != SUCCESS || response.paymentRequestId == null || response.gatewayUrl == null) {
            throw badGateway("Platobná brána platbu nevytvorila")
        }
        return CreatedPayment(response.paymentRequestId, response.gatewayUrl)
    }

    // Vráti null, keď sa stav nepodarilo zistiť (brána je nedostupná alebo dopyt prišiel príliš skoro).
    fun getPaymentStatus(paymentRequestId: Long): GatewayPaymentStatus? = try {
        val response = restClient.get()
            .uri("/api/Payments/Payment/{id}", paymentRequestId)
            .headers { it.setBearerAuth(accessToken()) }
            .retrieve()
            .body<PaymentStateResponse>()
        when (response?.paymentInformation?.status) {
            "Paid" -> GatewayPaymentStatus.PAID
            "Authorized" -> GatewayPaymentStatus.AUTHORIZED
            "Rejected" -> GatewayPaymentStatus.REJECTED
            "Pending" -> GatewayPaymentStatus.PENDING
            else -> null
        }
    } catch (e: RestClientException) {
        null
    }

    private companion object {
        const val SUCCESS = 1001000L
    }
}

// Brána používa v JSON iné názvy polí, preto ich @JsonProperty mapuje na naše.
data class TokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("expires_in") val expiresIn: Long,
)

data class CreatePaymentResponse(
    @JsonProperty("PaymentRequestId") val paymentRequestId: Long?,
    @JsonProperty("GatewayUrl") val gatewayUrl: String?,
    @JsonProperty("ResultInfo") val resultInfo: ResultInfo,
)

data class ResultInfo(@JsonProperty("ResultCode") val resultCode: Long)

data class PaymentStateResponse(@JsonProperty("PaymentInformation") val paymentInformation: PaymentInformation?)

data class PaymentInformation(@JsonProperty("Status") val status: String?)