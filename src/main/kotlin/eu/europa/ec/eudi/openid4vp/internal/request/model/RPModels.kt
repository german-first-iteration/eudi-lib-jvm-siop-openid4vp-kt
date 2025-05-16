package eu.europa.ec.eudi.openid4vp.internal.request.model

import kotlinx.serialization.Serializable

@Serializable
data class VerifierAttestationPayload(
    val privacy_policy: String,
    val purpose: List<LocalizedText>,
    val contact: ContactInfo,
    val credentials: List<CredentialInfo>,
    val credential_sets: List<CredentialSetInfo>? = null,
    val sub: String,
    val jti: String,
    val status: StatusWrapper,
    val public_body: Boolean,
    val entitlements: List<String>,
    val services: List<String>
)

@Serializable
data class LocalizedText(
    val locale: String,
    val name: String
)

@Serializable
data class ContactInfo(
    val website: String,
    val `e-mail`: String,
    val phone: String
)

@Serializable
data class CredentialInfo(
    val id: String,
    val format: String,
    val meta: MetaInfo,
    val claims: List<ClaimPath>
)

@Serializable
data class MetaInfo(
    val vct_values: List<String>
)

@Serializable
data class ClaimPath(
    val path: List<String>
)

@Serializable
data class CredentialSetInfo(
    val options: List<List<String>>,
    val required: Boolean,
    val purpose: List<LocalizedText>
)

@Serializable
data class StatusWrapper(
    val status_list: StatusList
)

@Serializable
data class StatusList(
    val idx: Int,
    val uri: String
)


@Serializable
data class RelyingPartyInfo(
    val id: String,
    val name: String,
    val EORI: String? = null,
    val NTR: String? = null,
    val LEI: String? = null,
    val VAT: String? = null,
    val EX: String? = null,
    val TAX: String? = null,
    val EUID: String? = null,
    val distinguishedName: String? = null,
    val user: String? = null
)


@Serializable
data class AttestationMetadata(
    val id: String,
    val jwt: String,
    val intendedUse: IntendedUse,
    val revoked: Boolean? = null
)

@Serializable
data class IntendedUse(
    val purpose: List<LocalizedText>,
    val credentials: List<Credential>,
    val credentialSet: List<CredentialSet>
)

@Serializable
data class Credential(
    val id: String,
    val format: String,
    val meta: CredentialMeta,
    val claims: List<ClaimPath>
)

@Serializable
data class CredentialMeta(
    val vct_values: List<String>
)

@Serializable
data class CredentialSet(
    val options: List<List<String>>,
    val required: Boolean,
    val purpose: List<LocalizedText>
)
