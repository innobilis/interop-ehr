package com.projectronin.interop.fhir.ronin.localization

import com.projectronin.interop.fhir.r4.datatype.CodeableConcept
import com.projectronin.interop.fhir.r4.datatype.Coding
import com.projectronin.interop.fhir.r4.datatype.Extension
import com.projectronin.interop.fhir.r4.datatype.Identifier
import com.projectronin.interop.fhir.ronin.code.normalizeCoding
import com.projectronin.interop.fhir.ronin.code.normalizeIdentifier
import com.projectronin.interop.fhir.validate.Validatable
import com.projectronin.interop.tenant.config.model.Tenant
import org.springframework.stereotype.Component

@Component
class Normalizer : BaseGenericTransformer() {
    /**
     * Normalizes the [element] for the [tenant]
     */
    fun <T : Any> normalize(element: T, tenant: Tenant): T {
        val normalizedValues = getTransformedValues(element, tenant)
        return copy(element, normalizedValues)
    }

    override fun transformType(element: Any, parameterName: String, tenant: Tenant): TransformResult {
        return when (element) {
            is Coding -> TransformResult(normalizeCoding(element, parameterName, tenant))
            is Identifier -> TransformResult(normalizeIdentifier(element, parameterName, tenant))
            is CodeableConcept -> TransformResult(normalizeCodeableConcept(element, parameterName, tenant))
            is Extension -> normalizeExtension(element, parameterName, tenant)
            is Validatable<*> -> TransformResult(transformOrNull(element, parameterName, tenant))
            else -> TransformResult(null)
        }
    }

    /**
     * Normalizes the [coding] for the [tenant].
     */
    private fun normalizeCoding(coding: Coding, parameterName: String, tenant: Tenant): Coding? {
        val nonNormalizedCoding = transformOrNull(coding, parameterName, tenant)
        val normalizedSystem = coding.system?.normalizeCoding()
        return if (normalizedSystem == coding.system) {
            nonNormalizedCoding
        } else {
            (nonNormalizedCoding ?: coding).copy(system = normalizedSystem)
        }
    }

    /**
     * Normalizes the [identifier] for the [tenant].
     */
    private fun normalizeIdentifier(identifier: Identifier, parameterName: String, tenant: Tenant): Identifier? {
        val nonNormalizedIdentifier = transformOrNull(identifier, parameterName, tenant)
        val normalizedSystem = identifier.system?.normalizeIdentifier()
        return if (normalizedSystem == identifier.system) {
            nonNormalizedIdentifier
        } else {
            (nonNormalizedIdentifier ?: identifier).copy(system = normalizedSystem)
        }
    }

    /**
     * Normalizes the [codeableConcept] for the [tenant].
     */
    private fun normalizeCodeableConcept(
        codeableConcept: CodeableConcept,
        parameterName: String,
        tenant: Tenant
    ): CodeableConcept {
        val nonNormalizedCodeableConcept = transformOrNull(codeableConcept, parameterName, tenant) ?: codeableConcept

        // If text is populated on the codeable concept already, return as is.
        if (codeableConcept.text?.value?.isNotEmpty() == true) {
            return nonNormalizedCodeableConcept
        }

        // When text isn't populated, pull from the single coding, or the single user selected coding
        val selectedCoding =
            codeableConcept.coding.singleOrNull { it.userSelected?.value == true }
                ?: codeableConcept.coding.singleOrNull()
        if (selectedCoding != null && selectedCoding.display?.value?.isNotEmpty() == true) {
            return nonNormalizedCodeableConcept.copy(text = selectedCoding.display)
        }

        // Otherwise make no changes
        return nonNormalizedCodeableConcept
    }

    private fun normalizeExtension(extension: Extension, parameterName: String, tenant: Tenant): TransformResult {
        return if (extension.value == null) {
            TransformResult(extension, true)
        } else {
            return TransformResult(extension)
        }
    }
}
