package eu.europa.ec.eudi.openid4vp.internal.request

import eu.europa.ec.eudi.openid4vp.internal.request.model.AttestationMetadata
import eu.europa.ec.eudi.openid4vp.internal.request.model.RelyingPartyInfo
import eu.europa.ec.eudi.openid4vp.internal.request.model.VerifierAttestationPayload
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Base64

internal class VerifierAttestationResolver(
    private val httpClient: HttpClient
) {
    suspend fun resolve(presentationPayload: String?): AttestationMetadata? {
        if (presentationPayload == null) return null
        val attestationJwt = extractVerifierAttestation(presentationPayload) ?: return null
        val verifierAttestationPayload = decodeSubFromJwt(attestationJwt) ?: return null // sub is needed to get the RP certificates
        val rpInfo = fetchRpId(verifierAttestationPayload.sub) ?: return null
        val relyingPartyCertificateMetadata = fetchRpCertificates(rpInfo.id)
        // TODO match claims in presentation payload with relying party certificate metadata
        return relyingPartyCertificateMetadata
    }

    private fun decodeSubFromJwt(jwt: String): VerifierAttestationPayload? = runCatching {
        val parts = jwt.split(".")
        val payload = String(Base64.getUrlDecoder().decode(parts[1]))
        val result = Json.decodeFromString<VerifierAttestationPayload>(payload)
        result
    }.getOrNull()

    /**
     * This method gets the RP ID using the SUB value of the relying party.
     */
    private suspend fun fetchRpId(sub: String): RelyingPartyInfo? {
        val encodedSub = java.net.URLEncoder.encode(sub.removePrefix("CN="), "UTF-8")
        val url = "https://funke-wallet.de/relying-parties?name=$encodedSub"
        val response = httpClient.get(url).bodyAsText()
        val json = Json.parseToJsonElement(response).jsonArray
        return Json.decodeFromString<RelyingPartyInfo>(json[0].toString().trimIndent())
    }

    /**
     * This method gets the RP certificates using the RP ID.
     */
    private suspend fun fetchRpCertificates(rpId: String): AttestationMetadata? {
        val url = "https://funke-wallet.de/relying-parties/$rpId/registration-certificates"
        val response = httpClient.get(url).bodyAsText()
        val cert = Json.parseToJsonElement(response).jsonArray
        val metadataList = Json.decodeFromString<AttestationMetadata>(cert[0].toString())
        return metadataList
    }

    private fun extractVerifierAttestation(input: String): String? = runCatching {
        val json: JsonObject = if (input.count { it == '.' } == 2) {
            // It's a JWT ➝ decode payload
            val payload = String(Base64.getUrlDecoder().decode(input.split(".")[1]))
            Json.parseToJsonElement(payload).jsonObject
        } else {
            // It's raw JSON
            Json.parseToJsonElement(input).jsonObject
        }
        val attestation = json["verifier_attestations"]?.jsonArray?.firstOrNull()?.jsonObject
        attestation?.get("data")?.jsonPrimitive?.content
    }.getOrNull()
}
