package com.projectronin.interop.fhir.ronin.generators.resource.observation

import com.projectronin.interop.fhir.generators.datatypes.codeableConcept
import com.projectronin.interop.fhir.generators.datatypes.coding
import com.projectronin.interop.fhir.generators.primitives.of
import com.projectronin.interop.fhir.generators.resources.ObservationGenerator
import com.projectronin.interop.fhir.r4.CodeSystem
import com.projectronin.interop.fhir.r4.datatype.CodeableConcept
import com.projectronin.interop.fhir.r4.datatype.Coding
import com.projectronin.interop.fhir.r4.datatype.DynamicValue
import com.projectronin.interop.fhir.r4.datatype.DynamicValueType
import com.projectronin.interop.fhir.r4.datatype.Extension
import com.projectronin.interop.fhir.r4.datatype.Quantity
import com.projectronin.interop.fhir.r4.datatype.primitive.Code
import com.projectronin.interop.fhir.r4.datatype.primitive.Decimal
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import com.projectronin.interop.fhir.r4.datatype.primitive.asFHIR
import com.projectronin.interop.fhir.r4.resource.Observation
import com.projectronin.interop.fhir.r4.resource.ObservationComponent
import com.projectronin.interop.fhir.r4.resource.Patient
import com.projectronin.interop.fhir.ronin.generators.resource.referenceData
import com.projectronin.interop.fhir.ronin.generators.util.generateCodeableConcept
import com.projectronin.interop.fhir.ronin.generators.util.generateReference
import com.projectronin.interop.fhir.ronin.generators.util.generateWithDefault
import com.projectronin.interop.fhir.ronin.generators.util.rcdmMeta
import com.projectronin.interop.fhir.ronin.normalization.ValueSetList
import com.projectronin.interop.fhir.ronin.profile.RoninExtension
import com.projectronin.interop.fhir.ronin.profile.RoninProfile
import com.projectronin.interop.fhir.ronin.validation.ValueSetMetadata

/**
 * Helps generate ronin blood pressure observation profile, applies meta and randomly generates an
 * acceptable code from the [possibleBloodPressureCodes] list, category is generated by base-vital-signs
 */
fun rcdmObservationBloodPressure(tenant: String, block: ObservationGenerator.() -> Unit): Observation {
    return rcdmBaseObservation(tenant) {
        block.invoke(this)
        meta of rcdmMeta(RoninProfile.OBSERVATION_BLOOD_PRESSURE, tenant) {}
        extension of tenantBloodPressureSourceExtension
        category of listOf(codeableConcept { coding of vitalSignsCategory })
        code of generateCodeableConcept(code.generate(), possibleBloodPressureCodes.codes.random())
        subject of generateReference(subject.generate(), subjectReferenceOptions, tenant, "Patient")
        component of generateWithDefault(component, bloodPressureComponent)
    }
}

fun Patient.rcdmObservationBloodPressure(block: ObservationGenerator.() -> Unit): Observation {
    val data = this.referenceData()
    return rcdmObservationBloodPressure(data.tenantId) {
        block.invoke(this)
        subject of generateReference(
            subject.generate(),
            subjectReferenceOptions,
            data.tenantId,
            "Patient",
            data.udpId
        )
    }
}

val possibleBloodPressureCodesList = listOf(
    coding {
        system of "http://loinc.org"
        version of "2.74"
        code of Code("35094-2")
        display of "Blood pressure panel"
    },
    coding {
        system of "http://loinc.org"
        version of "2.74"
        code of Code("55417-0")
        display of "Short blood pressure panel"
    },
    coding {
        system of "http://loinc.org"
        version of "2.74"
        code of Code("9855-8")
        display of "Blood pressure special circumstances"
    },
    coding {
        system of "http://loinc.org"
        version of "2.74"
        code of Code("85354-9")
        display of "Blood pressure panel with all children optional"
    },
    coding {
        system of "http://loinc.org"
        version of "2.74"
        code of Code("34553-8")
        display of "Orthostatic blood pressure panel"
    }
)
val possibleBloodPressureCodes =
    ValueSetList(
        possibleBloodPressureCodesList,
        ValueSetMetadata(
            registryEntryType = "value_set",
            valueSetName = "bloodpressurepanel",
            valueSetUuid = "64baa785-ba8a-448f-8714-93d57fd64db5",
            version = "1"
        )
    )

val bloodPressureComponent = listOf(
    ObservationComponent(
        code = CodeableConcept(
            coding = listOf(Coding(system = CodeSystem.LOINC.uri, code = Code("8462-4"))),
            text = "Diastolic".asFHIR()
        ),
        value = DynamicValue(
            DynamicValueType.QUANTITY,
            Quantity(
                value = Decimal(value = 70.0),
                unit = "mm[Hg]".asFHIR(),
                system = CodeSystem.UCUM.uri,
                code = Code("mm[Hg]")
            )
        )
    ),
    ObservationComponent(
        code = CodeableConcept(
            coding = listOf(Coding(system = CodeSystem.LOINC.uri, code = Code("8480-6"))),
            text = "Systolic".asFHIR()
        ),
        value = DynamicValue(
            DynamicValueType.QUANTITY,
            Quantity(
                value = Decimal(value = 70.0),
                unit = "mm[Hg]".asFHIR(),
                system = CodeSystem.UCUM.uri,
                code = Code("mm[Hg]")
            )
        )
    )
)

private val tenantBloodPressureSourceExtension = listOf(
    Extension(
        url = Uri(RoninExtension.TENANT_SOURCE_OBSERVATION_CODE.value),
        value = DynamicValue(
            DynamicValueType.CODEABLE_CONCEPT,
            CodeableConcept(
                text = "Tenant Blood Pressure".asFHIR(),
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Bad Blood Pressure".asFHIR(),
                        code = Code("bad-blood-pressure")
                    )
                )
            )
        )
    )
)
