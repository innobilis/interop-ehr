package com.projectronin.interop.fhir.ronin.resource.diagnosticReport

import com.projectronin.interop.fhir.r4.CodeSystem
import com.projectronin.interop.fhir.r4.datatype.primitive.Code
import com.projectronin.interop.fhir.r4.resource.DiagnosticReport
import com.projectronin.interop.fhir.r4.validate.resource.R4DiagnosticReportValidator
import com.projectronin.interop.fhir.ronin.getFhirIdentifiers
import com.projectronin.interop.fhir.ronin.localization.Localizer
import com.projectronin.interop.fhir.ronin.localization.Normalizer
import com.projectronin.interop.fhir.ronin.profile.RoninProfile
import com.projectronin.interop.fhir.ronin.resource.base.USCoreBasedProfile
import com.projectronin.interop.fhir.ronin.util.toFhirIdentifier
import com.projectronin.interop.fhir.validate.FHIRError
import com.projectronin.interop.fhir.validate.LocationContext
import com.projectronin.interop.fhir.validate.RequiredFieldError
import com.projectronin.interop.fhir.validate.Validation
import com.projectronin.interop.fhir.validate.ValidationIssueSeverity
import com.projectronin.interop.fhir.validate.validation
import com.projectronin.interop.tenant.config.model.Tenant
import org.springframework.stereotype.Component

/**
 * Validator and Transformer for the Ronin Diagnostic Report Laboratory profile.
 */
@Component
class RoninDiagnosticReportLaboratory(normalizer: Normalizer, localizer: Localizer) :
    USCoreBasedProfile<DiagnosticReport>(
        R4DiagnosticReportValidator,
        RoninProfile.DIAGNOSTIC_REPORT_LABORATORY.value,
        normalizer,
        localizer
    ) {

    companion object {
        internal val qualifyingCode = Code("LAB")
    }

    private val requiredIdError = RequiredFieldError(DiagnosticReport::id)
    private val requiredSubjectFieldError = RequiredFieldError(DiagnosticReport::subject)
    private val noDiagnosticReportCategoryError = FHIRError(
        code = "USCORE_DX_RPT_LAB_001",
        severity = ValidationIssueSeverity.ERROR,
        description = "Diagnostic Report Laboratory category required for US Core Diagnostic Report Laboratory profile",
        location = LocationContext(DiagnosticReport::category)
    )

    override fun qualifies(resource: DiagnosticReport): Boolean {
        return resource.category.any { codeableConcept ->
            codeableConcept.coding.any { coding ->
                coding.system == CodeSystem.DIAGNOSTIC_REPORT_LABORATORY.uri && coding.code == qualifyingCode
            }
        }
    }

    override fun validateRonin(element: DiagnosticReport, parentContext: LocationContext, validation: Validation) {
        validation.apply {
            requireRoninIdentifiers(element.identifier, parentContext, this)
        }
    }

    override fun validateUSCore(element: DiagnosticReport, parentContext: LocationContext, validation: Validation) {
        validation.apply {
            checkTrue(
                element.category.any { codeableConcept ->
                    codeableConcept.coding.any { coding ->
                        coding.system == CodeSystem.DIAGNOSTIC_REPORT_LABORATORY.uri && coding.code == qualifyingCode
                    }
                },
                noDiagnosticReportCategoryError,
                parentContext
            )

            checkNotNull(element.subject, requiredSubjectFieldError, parentContext)

            // code and status field checks are done by R4DiagnosticReportValidator
        }
    }

    override fun transformInternal(
        normalized: DiagnosticReport,
        parentContext: LocationContext,
        tenant: Tenant
    ): Pair<DiagnosticReport?, Validation> {
        val validation = validation {
            checkNotNull(normalized.id, requiredIdError, parentContext)
        }

        val transformed = normalized.copy(
            meta = normalized.meta.transform(),
            identifier = normalized.identifier + normalized.getFhirIdentifiers() + tenant.toFhirIdentifier()
        )

        return Pair(transformed, validation)
    }
}