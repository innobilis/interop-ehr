package com.projectronin.interop.fhir.ronin.resource.observation

import com.projectronin.interop.fhir.r4.datatype.Coding
import com.projectronin.interop.fhir.r4.resource.Observation
import com.projectronin.interop.fhir.r4.validate.resource.R4ObservationValidator
import com.projectronin.interop.fhir.ronin.getRoninIdentifiersForResource
import com.projectronin.interop.fhir.ronin.localization.Localizer
import com.projectronin.interop.fhir.ronin.localization.Normalizer
import com.projectronin.interop.fhir.ronin.normalization.NormalizationRegistryClient
import com.projectronin.interop.fhir.ronin.profile.RoninProfile
import com.projectronin.interop.fhir.ronin.util.isInValueSet
import com.projectronin.interop.fhir.validate.FHIRError
import com.projectronin.interop.fhir.validate.LocationContext
import com.projectronin.interop.fhir.validate.RequiredFieldError
import com.projectronin.interop.fhir.validate.Validation
import com.projectronin.interop.fhir.validate.ValidationIssueSeverity
import com.projectronin.interop.fhir.validate.validation
import com.projectronin.interop.tenant.config.model.Tenant
import org.springframework.stereotype.Component

@Component
class RoninBloodPressure(
    normalizer: Normalizer,
    localizer: Localizer,
    private val registryClient: NormalizationRegistryClient
) :
    BaseRoninVitalSign(
        R4ObservationValidator,
        RoninProfile.OBSERVATION_BLOOD_PRESSURE.value,
        normalizer,
        localizer
    ) {

    // Load the qualifying codes from the value set. These are required to process.
    override val qualifyingCodes: List<Coding> by lazy {
        registryClient.getRequiredValueSet(
            "Observation.code",
            profile
        )
    }

    // Multipart qualifying codes for RoninBloodPressure
    private val validSystolicCodes: List<Coding> by lazy {
        registryClient.getRequiredValueSet(
            "Observation.component:systolic.code",
            profile
        )
    }
    private val validDiastolicCodes: List<Coding> by lazy {
        registryClient.getRequiredValueSet(
            "Observation.component:diastolic.code",
            profile
        )
    }

    // Quantity unit codes - [USCore Blood Pressure Units](http://hl7.org/fhir/us/core/STU5.0.1/StructureDefinition-us-core-blood-pressure.html)
    override val validQuantityCodes = listOf("mm[Hg]")

    // Reference checks - override BaseRoninObservation value lists as needed for RoninBloodPressure
    override val validBasedOnValues = listOf("CarePlan", "MedicationRequest")
    override val validPartOfValues = listOf("MedicationStatement", "Procedure")
    override val validDerivedFromValues = listOf("DocumentReference")
    override val validHasMemberValues = listOf("MolecularSequence", "Observation", "QuestionnaireResponse")

    override fun validateVitalSign(element: Observation, parentContext: LocationContext, validation: Validation) {
        if (element.dataAbsentReason == null) {
            val components = element.component
            val systolic = components.filter { comp ->
                comp.code?.coding?.any { it.isInValueSet(validSystolicCodes) } ?: false
            }
            val diastolic = components.filter { comp ->
                comp.code?.coding?.any { it.isInValueSet(validDiastolicCodes) } ?: false
            }

            if (systolic.size == 1) {
                validateVitalSignValue(
                    systolic.first().value,
                    validQuantityCodes,
                    LocationContext("Observation", "component:systolic"),
                    validation
                )
            }
            if (diastolic.size == 1) {
                validateVitalSignValue(
                    diastolic.first().value,
                    validQuantityCodes,
                    LocationContext("Observation", "component:diastolic"),
                    validation
                )
            }

            validation.apply {
                val componentSystolicCodeContext = LocationContext("Observation", "component:systolic.code")
                checkTrue(
                    systolic.isNotEmpty(),
                    FHIRError(
                        code = "USCORE_BPOBS_001",
                        severity = ValidationIssueSeverity.ERROR,
                        description = "Must match this system|code: ${
                        validSystolicCodes.joinToString(", ") { "${it.system?.value}|${it.code?.value}" }
                        }",
                        location = componentSystolicCodeContext
                    ),
                    parentContext
                )
                checkTrue(
                    systolic.size <= 1,
                    FHIRError(
                        code = "USCORE_BPOBS_004",
                        severity = ValidationIssueSeverity.ERROR,
                        description = "Only 1 entry is allowed for systolic blood pressure",
                        location = componentSystolicCodeContext
                    ),
                    parentContext
                )

                val componentDiastolicCodeContext = LocationContext("Observation", "component:diastolic.code")
                checkTrue(
                    diastolic.isNotEmpty(),
                    FHIRError(
                        code = "USCORE_BPOBS_002",
                        severity = ValidationIssueSeverity.ERROR,
                        description = "Must match this system|code: ${
                        validDiastolicCodes.joinToString(", ") { "${it.system?.value}|${it.code?.value}" }
                        }",
                        location = componentDiastolicCodeContext
                    ),
                    parentContext
                )
                checkTrue(
                    diastolic.size <= 1,
                    FHIRError(
                        code = "USCORE_BPOBS_005",
                        severity = ValidationIssueSeverity.ERROR,
                        description = "Only 1 entry is allowed for diastolic blood pressure",
                        location = componentDiastolicCodeContext
                    ),
                    parentContext
                )
            }
        }
    }

    private val requiredIdError = RequiredFieldError(Observation::id)

    override fun transformInternal(
        normalized: Observation,
        parentContext: LocationContext,
        tenant: Tenant
    ): Pair<Observation?, Validation> {
        val validation = validation {
            checkNotNull(normalized.id, requiredIdError, parentContext)
        }

        val transformed = normalized.copy(
            meta = normalized.meta.transform(),
            identifier = normalized.identifier + normalized.getRoninIdentifiersForResource(tenant)
        )
        return Pair(transformed, validation)
    }
}
