package com.projectronin.interop.fhir.ronin.resource.observation

import com.projectronin.interop.fhir.r4.CodeSystem
import com.projectronin.interop.fhir.r4.CodeableConcepts
import com.projectronin.interop.fhir.r4.datatype.Annotation
import com.projectronin.interop.fhir.r4.datatype.CodeableConcept
import com.projectronin.interop.fhir.r4.datatype.Coding
import com.projectronin.interop.fhir.r4.datatype.DynamicValue
import com.projectronin.interop.fhir.r4.datatype.DynamicValueType
import com.projectronin.interop.fhir.r4.datatype.Extension
import com.projectronin.interop.fhir.r4.datatype.Identifier
import com.projectronin.interop.fhir.r4.datatype.Meta
import com.projectronin.interop.fhir.r4.datatype.Narrative
import com.projectronin.interop.fhir.r4.datatype.ObservationComponent
import com.projectronin.interop.fhir.r4.datatype.ObservationReferenceRange
import com.projectronin.interop.fhir.r4.datatype.Quantity
import com.projectronin.interop.fhir.r4.datatype.Reference
import com.projectronin.interop.fhir.r4.datatype.primitive.Canonical
import com.projectronin.interop.fhir.r4.datatype.primitive.Code
import com.projectronin.interop.fhir.r4.datatype.primitive.Decimal
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.r4.datatype.primitive.Instant
import com.projectronin.interop.fhir.r4.datatype.primitive.Markdown
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import com.projectronin.interop.fhir.r4.datatype.primitive.asFHIR
import com.projectronin.interop.fhir.r4.resource.ContainedResource
import com.projectronin.interop.fhir.r4.resource.Observation
import com.projectronin.interop.fhir.r4.valueset.NarrativeStatus
import com.projectronin.interop.fhir.r4.valueset.ObservationStatus
import com.projectronin.interop.fhir.ronin.profile.RoninProfile
import com.projectronin.interop.fhir.ronin.resource.observation.RoninBloodPressure.vitalSignsCode
import com.projectronin.interop.fhir.util.asCode
import com.projectronin.interop.fhir.validate.validation
import com.projectronin.interop.tenant.config.model.Tenant
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RoninBloodPressureTest {
    private val tenant = mockk<Tenant> {
        every { mnemonic } returns "test"
    }

    @Test
    fun `does not qualify when no category`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            category = listOf(),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            code = CodeableConcept(text = "vital sign".asFHIR())
        )

        assertFalse(RoninBloodPressure.qualifies(observation))
    }

    @Test
    fun `does not qualify when no code`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            code = null
        )

        assertFalse(RoninBloodPressure.qualifies(observation))
    }

    @Test
    fun `does not qualify when no coding`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            code = CodeableConcept(text = "code".asFHIR())
        )

        assertFalse(RoninBloodPressure.qualifies(observation))
    }

    @Test
    fun `does not qualify when coding code not for blood pressure`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            code = CodeableConcept(coding = listOf(Coding(system = CodeSystem.LOINC.uri, code = Code("1234"))))
        )

        assertFalse(RoninBloodPressure.qualifies(observation))
    }

    @Test
    fun `does not qualify when coding code is for blood pressure but wrong system`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.UCUM.uri,
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            )
        )

        assertFalse(RoninBloodPressure.qualifies(observation))
    }

    @Test
    fun `qualifies for profile`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            )
        )

        assertTrue(RoninBloodPressure.qualifies(observation))
    }

    @Test
    fun `validate checks ronin identifiers`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR RONIN_TNNT_ID_001: Tenant identifier is required @ Observation.identifier\n" +
                "ERROR RONIN_FHIR_ID_001: FHIR identifier is required @ Observation.identifier",
            exception.message
        )
    }

    @Test
    fun `validate fails if non-blood pressure code`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = Code("random")
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
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
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR USCORE_BPOBS_001: LOINC code 85354-9 required for US Core Blood Pressure profile @ Observation.code",
            exception.message
        )
    }

    @Test
    fun `validate fails if no components and no data absent reason for blood pressure`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR USCORE_BPOBS_002: LOINC code 8480-6 required for systolic blood pressure @ Observation.component.code\n" +
                "ERROR USCORE_BPOBS_003: LOINC code 8462-4 required for diastolic blood pressure @ Observation.component.code",
            exception.message
        )
    }

    @Test
    fun `validate succeeds if no components and data absent reason is provided`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            )
        )

        RoninBloodPressure.validate(observation, null).alertIfErrors()
    }

    @Test
    fun `validate fails if systolic quantity and systolic data absent reason are both provided`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    ),
                    dataAbsentReason = CodeableConcept(coding = listOf(Coding(code = Code("absent reason"))))
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
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
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR R4_OBSCOM_001: dataAbsentReason SHALL only be present if value[x] is not present @ Observation.component[0]",
            exception.message
        )
    }

    @Test
    fun `validate succeeds if systolic quantity has data absent reason instead of value`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    dataAbsentReason = CodeableConcept(coding = listOf(Coding(code = Code("absent reason"))))
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
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
                )
            )
        )

        RoninBloodPressure.validate(observation, null).alertIfErrors()
    }

    @Test
    fun `validate fails if components include conflicting systolic quantity values`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
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
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    dataAbsentReason = CodeableConcept(coding = listOf(Coding(code = Code("absent reason"))))
                ),
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR USCORE_BPOBS_004: Only 1 entry is allowed for systolic blood pressure @ Observation.component.code",
            exception.message
        )
    }

    @Test
    fun `validate fails if no systolic quantity value`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
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
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR REQ_FIELD: valueQuantity.value is a required element @ Observation.valueQuantity.value",
            exception.message
        )
    }

    @Test
    fun `validate fails if no systolic quantity unit`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
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
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR REQ_FIELD: valueQuantity.unit is a required element @ Observation.valueQuantity.unit",
            exception.message
        )
    }

    @Test
    fun `validate fails if systolic quantity system is not UCUM`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = Uri("some-system"),
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
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
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR USCORE_VSOBS_002: Quantity system must be UCUM @ Observation.valueQuantity.system",
            exception.message
        )
    }

    @Test
    fun `validate fails if no systolic quantity code`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
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
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR REQ_FIELD: valueQuantity.code is a required element @ Observation.valueQuantity.code",
            exception.message
        )
    }

    @Test
    fun `validate fails if systolic quantity code is outside the required value set`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("invalid-code")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
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
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR INV_VALUE_SET: 'invalid-code' is outside of required value set @ Observation.valueQuantity.code",
            exception.message
        )
    }

    @Test
    fun `validate fails if diastolic quantity and diastolic data absent reason are both provided`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
                        text = "Diastolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(70.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    ),
                    dataAbsentReason = CodeableConcept(coding = listOf(Coding(code = Code("absent reason"))))
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR R4_OBSCOM_001: dataAbsentReason SHALL only be present if value[x] is not present @ Observation.component[1]",
            exception.message
        )
    }

    @Test
    fun `validate succeeds if diastolic quantity has data absent reason instead of value`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
                        text = "Diastolic".asFHIR()
                    ),
                    dataAbsentReason = CodeableConcept(coding = listOf(Coding(code = Code("absent reason"))))
                )
            )
        )

        RoninBloodPressure.validate(observation, null).alertIfErrors()
    }

    @Test
    fun `validate fails if components include conflicting diastolic quantity values`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
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
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
                        text = "Diastolic".asFHIR()
                    ),
                    dataAbsentReason = CodeableConcept(coding = listOf(Coding(code = Code("absent reason"))))
                ),
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR USCORE_BPOBS_005: Only 1 entry is allowed for diastolic blood pressure @ Observation.component.code",
            exception.message
        )
    }

    @Test
    fun `validate fails if no diastolic quantity value`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
                        text = "Diastolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR REQ_FIELD: valueQuantity.value is a required element @ Observation.valueQuantity.value",
            exception.message
        )
    }

    @Test
    fun `validate fails if no diastolic quantity unit`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
                        text = "Diastolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 70.0),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR REQ_FIELD: valueQuantity.unit is a required element @ Observation.valueQuantity.unit",
            exception.message
        )
    }

    @Test
    fun `validate fails if diastolic quantity system is not UCUM`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
                        text = "Diastolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 70.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = Uri("some-system"),
                            code = Code("mm[Hg]")
                        )
                    )
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR USCORE_VSOBS_002: Quantity system must be UCUM @ Observation.valueQuantity.system",
            exception.message
        )
    }

    @Test
    fun `validate fails if no diastolic quantity code`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
                        text = "Diastolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 70.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri
                        )
                    )
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR REQ_FIELD: valueQuantity.code is a required element @ Observation.valueQuantity.code",
            exception.message
        )
    }

    @Test
    fun `validate fails if diastolic quantity code is outside the required value set`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
                        text = "Diastolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 70.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("invalid-code")
                        )
                    )
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR INV_VALUE_SET: 'invalid-code' is outside of required value set @ Observation.valueQuantity.code",
            exception.message
        )
    }

    @Test
    fun `validate checks US Core vital signs profile`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = Code("bad-code")
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
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
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR USCORE_VSOBS_001: Vital signs must use code \"vital-signs\" in system " +
                "\"http://terminology.hl7.org/CodeSystem/observation-category\" @ Observation.category",
            exception.message
        )
    }

    @Test
    fun `validate succeeds`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
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
                )
            )
        )

        RoninBloodPressure.validate(observation, null).alertIfErrors()
    }

    @Test
    fun `transform fails for observation with no ID`() {
        val observation = Observation(
            status = ObservationStatus.AMENDED.asCode(),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            value = DynamicValue(
                DynamicValueType.QUANTITY,
                Quantity(
                    value = Decimal(182.88),
                    unit = "cm".asFHIR(),
                    system = CodeSystem.UCUM.uri,
                    code = Code("cm")
                )
            )
        )

        val (transformed, _) = RoninBloodPressure.transform(observation, tenant)
        assertNull(transformed)
    }

    @Test
    fun `transforms observation with all attributes`() {
        val observation = Observation(
            id = Id("123"),
            meta = Meta(
                profile = listOf(Canonical("https://www.hl7.org/fhir/observation"))
            ),
            implicitRules = Uri("implicit-rules"),
            language = Code("en-US"),
            text = Narrative(status = NarrativeStatus.GENERATED.asCode(), div = "div".asFHIR()),
            contained = listOf(ContainedResource("""{"resourceType":"Banana","id":"24680"}""")),
            extension = listOf(
                Extension(
                    url = Uri("http://localhost/extension"),
                    value = DynamicValue(DynamicValueType.STRING, "Value")
                )
            ),
            modifierExtension = listOf(
                Extension(
                    url = Uri("http://localhost/modifier-extension"),
                    value = DynamicValue(DynamicValueType.STRING, "Value")
                )
            ),
            identifier = listOf(Identifier(value = "id".asFHIR())),
            basedOn = listOf(Reference(reference = "CarePlan/1234".asFHIR())),
            partOf = listOf(Reference(reference = "MedicationStatement/1234".asFHIR())),
            status = ObservationStatus.AMENDED.asCode(),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                ),
                text = "Blood Pressure".asFHIR()
            ),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            focus = listOf(Reference(display = "focus".asFHIR())),
            encounter = Reference(reference = "Encounter/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            issued = Instant("2022-01-01T00:00:00Z"),
            performer = listOf(Reference(reference = "Organization/1234".asFHIR())),
            value = DynamicValue(
                type = DynamicValueType.STRING,
                "string"
            ),
            interpretation = listOf(CodeableConcept(text = "interpretation".asFHIR())),
            bodySite = CodeableConcept(text = "bodySite".asFHIR()),
            method = CodeableConcept(text = "method".asFHIR()),
            specimen = Reference(reference = "Specimen/1234".asFHIR()),
            device = Reference(reference = "DeviceMetric/1234".asFHIR()),
            referenceRange = listOf(ObservationReferenceRange(text = "referenceRange".asFHIR())),
            hasMember = listOf(Reference(reference = "Observation/5678".asFHIR())),
            derivedFrom = listOf(Reference(reference = "DocumentReference/1234".asFHIR())),
            component = listOf(
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Systolic".asFHIR(),
                                code = RoninBloodPressure.systolicCode
                            )
                        ),
                        text = "Systolic".asFHIR()
                    ),
                    value = DynamicValue(
                        DynamicValueType.QUANTITY,
                        Quantity(
                            value = Decimal(value = 110.0),
                            unit = "mm[Hg]".asFHIR(),
                            system = CodeSystem.UCUM.uri,
                            code = Code("mm[Hg]")
                        )
                    )
                ),
                ObservationComponent(
                    code = CodeableConcept(
                        coding = listOf(
                            Coding(
                                system = CodeSystem.LOINC.uri,
                                display = "Diastolic".asFHIR(),
                                code = RoninBloodPressure.diastolicCode
                            )
                        ),
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
                )
            ),
            note = listOf(
                Annotation(
                    text = Markdown("text"),
                    author = DynamicValue(type = DynamicValueType.REFERENCE, value = "Practitioner/0001")
                )
            )
        )

        val (transformed, validation) = RoninBloodPressure.transform(observation, tenant)
        validation.alertIfErrors()

        transformed!!
        assertEquals("Observation", transformed.resourceType)
        assertEquals(Id("test-123"), transformed.id)
        assertEquals(Meta(profile = listOf(Canonical(RoninProfile.OBSERVATION_BLOOD_PRESSURE.value))), transformed.meta)
        assertEquals(Uri("implicit-rules"), transformed.implicitRules)
        assertEquals(Code("en-US"), transformed.language)
        assertEquals(Narrative(status = NarrativeStatus.GENERATED.asCode(), div = "div".asFHIR()), transformed.text)
        assertEquals(
            listOf(ContainedResource("""{"resourceType":"Banana","id":"24680"}""")),
            transformed.contained
        )
        assertEquals(
            listOf(
                Extension(
                    url = Uri("http://localhost/extension"),
                    value = DynamicValue(DynamicValueType.STRING, "Value")
                )
            ),
            transformed.extension
        )
        assertEquals(
            listOf(
                Extension(
                    url = Uri("http://localhost/modifier-extension"),
                    value = DynamicValue(DynamicValueType.STRING, "Value")
                )
            ),
            transformed.modifierExtension
        )
        assertEquals(
            listOf(
                Identifier(value = "id".asFHIR()),
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            transformed.identifier
        )
        assertEquals(listOf(Reference(reference = "CarePlan/test-1234".asFHIR())), transformed.basedOn)
        assertEquals(listOf(Reference(reference = "MedicationStatement/test-1234".asFHIR())), transformed.partOf)
        assertEquals(ObservationStatus.AMENDED.asCode(), transformed.status)
        assertEquals(
            listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            transformed.category
        )
        assertEquals(
            CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                ),
                text = "Blood Pressure".asFHIR()
            ),
            transformed.code
        )
        assertEquals(Reference(reference = "Patient/test-1234".asFHIR()), transformed.subject)
        assertEquals(listOf(Reference(display = "focus".asFHIR())), transformed.focus)
        assertEquals(Reference(reference = "Encounter/test-1234".asFHIR()), transformed.encounter)
        assertEquals(
            DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            transformed.effective
        )
        assertEquals(Instant("2022-01-01T00:00:00Z"), transformed.issued)
        assertEquals(listOf(Reference(reference = "Organization/test-1234".asFHIR())), transformed.performer)
        assertEquals(
            DynamicValue(
                type = DynamicValueType.STRING,
                "string"
            ),
            transformed.value
        )
        assertNull(transformed.dataAbsentReason)
        assertEquals(listOf(CodeableConcept(text = "interpretation".asFHIR())), transformed.interpretation)
        assertEquals(CodeableConcept(text = "bodySite".asFHIR()), transformed.bodySite)
        assertEquals(CodeableConcept(text = "method".asFHIR()), transformed.method)
        assertEquals(Reference(reference = "Specimen/test-1234".asFHIR()), transformed.specimen)
        assertEquals(Reference(reference = "DeviceMetric/test-1234".asFHIR()), transformed.device)
        assertEquals(listOf(ObservationReferenceRange(text = "referenceRange".asFHIR())), transformed.referenceRange)
        assertEquals(listOf(Reference(reference = "Observation/test-5678".asFHIR())), transformed.hasMember)
        assertEquals(listOf(Reference(reference = "DocumentReference/test-1234".asFHIR())), transformed.derivedFrom)
        assertEquals(
            ObservationComponent(
                code = CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.LOINC.uri,
                            display = "Systolic".asFHIR(),
                            code = RoninBloodPressure.systolicCode
                        )
                    ),
                    text = "Systolic".asFHIR()
                ),
                value = DynamicValue(
                    DynamicValueType.QUANTITY,
                    Quantity(
                        value = Decimal(value = 110.0),
                        unit = "mm[Hg]".asFHIR(),
                        system = CodeSystem.UCUM.uri,
                        code = Code("mm[Hg]")
                    )
                )
            ),
            transformed.component[0]
        )
        assertEquals(
            ObservationComponent(
                code = CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.LOINC.uri,
                            display = "Diastolic".asFHIR(),
                            code = RoninBloodPressure.diastolicCode
                        )
                    ),
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
            transformed.component[1]
        )
        assertEquals(
            listOf(
                Annotation(
                    text = Markdown("text"),
                    author = DynamicValue(type = DynamicValueType.REFERENCE, value = "Practitioner/0001")
                )
            ),
            transformed.note
        )
    }

    @Test
    fun `transforms observation with only required attributes`() {
        val observation = Observation(
            id = Id("123"),
            status = ObservationStatus.AMENDED.asCode(),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                ),
                text = "Blood Pressure".asFHIR()
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            )
        )

        val (transformed, validation) = RoninBloodPressure.transform(observation, tenant)
        validation.alertIfErrors()

        transformed!!
        assertEquals("Observation", transformed.resourceType)
        assertEquals(Id("test-123"), transformed.id)
        assertEquals(Meta(profile = listOf(Canonical(RoninProfile.OBSERVATION_BLOOD_PRESSURE.value))), transformed.meta)
        assertNull(transformed.implicitRules)
        assertNull(transformed.language)
        assertNull(transformed.text)
        assertEquals(listOf<ContainedResource>(), transformed.contained)
        assertEquals(listOf<Extension>(), transformed.extension)
        assertEquals(listOf<Extension>(), transformed.modifierExtension)
        assertEquals(
            listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            transformed.identifier
        )
        assertEquals(listOf<Reference>(), transformed.basedOn)
        assertEquals(listOf<Reference>(), transformed.partOf)
        assertEquals(ObservationStatus.AMENDED.asCode(), transformed.status)
        assertEquals(
            listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            transformed.category
        )
        assertEquals(
            CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                ),
                text = "Blood Pressure".asFHIR()
            ),
            transformed.code
        )
        assertEquals(Reference(reference = "Patient/test-1234".asFHIR()), transformed.subject)
        assertEquals(listOf<Reference>(), transformed.focus)
        assertNull(transformed.encounter)
        assertEquals(
            DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            transformed.effective
        )
        assertNull(transformed.issued)
        assertEquals(listOf<Reference>(), transformed.performer)
        assertNull(transformed.value)
        assertEquals(CodeableConcept(text = "dataAbsent".asFHIR()), transformed.dataAbsentReason)
        assertEquals(listOf<CodeableConcept>(), transformed.interpretation)
        assertNull(transformed.bodySite)
        assertNull(transformed.method)
        assertNull(transformed.specimen)
        assertNull(transformed.device)
        assertEquals(listOf<ObservationReferenceRange>(), transformed.referenceRange)
        assertEquals(listOf<Reference>(), transformed.hasMember)
        assertEquals(listOf<Reference>(), transformed.derivedFrom)
        assertEquals(listOf<ObservationComponent>(), transformed.component)
        assertEquals(listOf<Annotation>(), transformed.note)
    }

    @Test
    fun `transform inherits R4 validation`() {
        val observation = Observation(
            id = Id("123"),
            status = Code("bad-status"),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                )
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            subject = Reference(reference = "Patient/1234".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            )
        )

        val exception = assertThrows<java.lang.IllegalArgumentException> {
            val (transformed, validation) = RoninBloodPressure.transform(observation, tenant)
            assertNull(transformed)
            validation.alertIfErrors()
        }
        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR INV_VALUE_SET: 'bad-status' is outside of required value set @ Observation.status",
            exception.message
        )
    }

    @Test
    fun `validate fails if invalid basedOn reference resource type`() {
        val observation = Observation(
            id = Id("123"),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            status = ObservationStatus.AMENDED.asCode(),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                ),
                text = "Blood Pressure".asFHIR(),
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            subject = Reference(reference = "Patient/123".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            basedOn = listOf(Reference(reference = "".asFHIR()))
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR RONIN_INV_REF_TYPE: The referenced resource type was not one of " +
                "CarePlan, MedicationRequest @ Observation.basedOn[0]",
            exception.message
        )
    }

    @Test
    fun `validate fails if invalid derivedFrom reference resource type`() {
        val observation = Observation(
            id = Id("123"),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            status = ObservationStatus.AMENDED.asCode(),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                ),
                text = "Blood Pressure".asFHIR(),
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            subject = Reference(reference = "Patient/123".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            derivedFrom = listOf(Reference(reference = "".asFHIR()))
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR RONIN_INV_REF_TYPE: The referenced resource type was not " +
                "DocumentReference @ Observation.derivedFrom[0]",
            exception.message
        )
    }

    @Test
    fun `validate fails if invalid hasMember reference resource type`() {
        val observation = Observation(
            id = Id("123"),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            status = ObservationStatus.AMENDED.asCode(),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                ),
                text = "Blood Pressure".asFHIR(),
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            subject = Reference(reference = "Patient/123".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            hasMember = listOf(Reference(reference = "".asFHIR()))
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR RONIN_INV_REF_TYPE: The referenced resource type was not one of " +
                "MolecularSequence, Observation, QuestionnaireResponse @ Observation.hasMember[0]",
            exception.message
        )
    }

    @Test
    fun `validate fails if invalid partOf reference resource type`() {
        val observation = Observation(
            id = Id("123"),
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "123".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            status = ObservationStatus.AMENDED.asCode(),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.LOINC.uri,
                        display = "Blood Pressure".asFHIR(),
                        code = RoninBloodPressure.bloodPressureCode
                    )
                ),
                text = "Blood Pressure".asFHIR(),
            ),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            system = CodeSystem.OBSERVATION_CATEGORY.uri,
                            code = RoninBloodPressure.vitalSignsCode
                        )
                    )
                )
            ),
            dataAbsentReason = CodeableConcept(text = "dataAbsent".asFHIR()),
            subject = Reference(reference = "Patient/123".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                "2022-01-01T00:00:00Z"
            ),
            partOf = listOf(Reference(reference = "".asFHIR()))
        )

        val exception = assertThrows<IllegalArgumentException> {
            RoninBloodPressure.validate(observation, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR RONIN_INV_REF_TYPE: The referenced resource type was not one of " +
                "MedicationStatement, Procedure @ Observation.partOf[0]",
            exception.message
        )
    }
}
