package com.projectronin.interop.fhir.ronin.resource

import com.projectronin.interop.fhir.r4.CodeSystem
import com.projectronin.interop.fhir.r4.CodeableConcepts
import com.projectronin.interop.fhir.r4.datatype.Annotation
import com.projectronin.interop.fhir.r4.datatype.CodeableConcept
import com.projectronin.interop.fhir.r4.datatype.Dosage
import com.projectronin.interop.fhir.r4.datatype.DynamicValue
import com.projectronin.interop.fhir.r4.datatype.DynamicValueType
import com.projectronin.interop.fhir.r4.datatype.Extension
import com.projectronin.interop.fhir.r4.datatype.Identifier
import com.projectronin.interop.fhir.r4.datatype.Meta
import com.projectronin.interop.fhir.r4.datatype.Narrative
import com.projectronin.interop.fhir.r4.datatype.Period
import com.projectronin.interop.fhir.r4.datatype.Reference
import com.projectronin.interop.fhir.r4.datatype.primitive.Canonical
import com.projectronin.interop.fhir.r4.datatype.primitive.Code
import com.projectronin.interop.fhir.r4.datatype.primitive.DateTime
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.r4.datatype.primitive.Markdown
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import com.projectronin.interop.fhir.r4.datatype.primitive.asFHIR
import com.projectronin.interop.fhir.r4.resource.ContainedResource
import com.projectronin.interop.fhir.r4.resource.MedicationStatement
import com.projectronin.interop.fhir.r4.validate.resource.R4MedicationStatementValidator
import com.projectronin.interop.fhir.r4.valueset.MedicationStatementStatus
import com.projectronin.interop.fhir.ronin.profile.RoninProfile
import com.projectronin.interop.fhir.util.asCode
import com.projectronin.interop.fhir.validate.LocationContext
import com.projectronin.interop.fhir.validate.RequiredFieldError
import com.projectronin.interop.fhir.validate.validation
import com.projectronin.interop.tenant.config.model.Tenant
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RoninMedicationStatementTest {
    private val tenant = mockk<Tenant> {
        every { mnemonic } returns "test"
    }

    @Test
    fun `always qualifies`() {
        assertTrue(RoninMedicationStatement.qualifies(MedicationStatement()))
    }

    @Test
    fun `validates Ronin identifiers`() {
        val medicationStatement = MedicationStatement()

        mockkObject(R4MedicationStatementValidator)
        every {
            R4MedicationStatementValidator.validate(medicationStatement, LocationContext(MedicationStatement::class))
        } returns validation { }

        val exception = assertThrows<IllegalArgumentException> {
            RoninMedicationStatement.validate(medicationStatement, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR RONIN_TNNT_ID_001: Tenant identifier is required @ MedicationStatement.identifier\n" +
                "ERROR RONIN_FHIR_ID_001: FHIR identifier is required @ MedicationStatement.identifier",
            exception.message
        )

        unmockkObject(R4MedicationStatementValidator)
    }

    @Test
    fun `validates R4 profile`() {
        val medicationStatement = MedicationStatement(
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "12345".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            )
        )

        mockkObject(R4MedicationStatementValidator)
        every {
            R4MedicationStatementValidator.validate(medicationStatement, LocationContext(MedicationStatement::class))
        } returns validation {
            checkNotNull(
                null,
                RequiredFieldError(MedicationStatement::status),
                LocationContext(MedicationStatement::class)
            )
        }

        val exception = assertThrows<IllegalArgumentException> {
            RoninMedicationStatement.validate(medicationStatement, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR REQ_FIELD: status is a required element @ MedicationStatement.status",
            exception.message
        )

        unmockkObject(R4MedicationStatementValidator)
    }

    @Test
    fun `validate succeeds`() {
        val medicationStatement = MedicationStatement(
            identifier = listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "12345".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            status = MedicationStatementStatus.ACTIVE.asCode(),
            medication = DynamicValue(
                type = DynamicValueType.CODEABLE_CONCEPT,
                value = CodeableConcept()
            ),
            subject = Reference(display = "display".asFHIR())
        )

        RoninMedicationStatement.validate(medicationStatement, null).alertIfErrors()
    }

    @Test
    fun `transform succeeds with all attributes`() {
        val medicationStatement = MedicationStatement(
            id = Id("12345"),
            meta = Meta(
                profile = listOf(Canonical("http://hl7.org/fhir/R4/medicationstatement.html"))
            ),
            implicitRules = Uri("implicit-rules"),
            language = Code("en-US"),
            text = Narrative(
                status = com.projectronin.interop.fhir.r4.valueset.NarrativeStatus.GENERATED.asCode(),
                div = "div".asFHIR()
            ),
            contained = listOf(ContainedResource("""{"resourceType":"Banana","id":"24680"}""")),
            extension = listOf(
                Extension(
                    url = Uri("http://hl7.org/extension-1"),
                    value = DynamicValue(DynamicValueType.STRING, "value")
                )
            ),
            modifierExtension = listOf(
                Extension(
                    url = Uri("http://localhost/modifier-extension"),
                    value = DynamicValue(DynamicValueType.STRING, "Value")
                )
            ),
            identifier = listOf(Identifier(value = "id".asFHIR())),
            basedOn = listOf(Reference(display = "reference".asFHIR())),
            partOf = listOf(Reference(display = "partOf".asFHIR())),
            status = MedicationStatementStatus.ACTIVE.asCode(),
            statusReason = listOf(CodeableConcept(text = "statusReason".asFHIR())),
            category = CodeableConcept(text = "category".asFHIR()),
            medication = DynamicValue(
                type = DynamicValueType.CODEABLE_CONCEPT,
                value = CodeableConcept(text = "medication".asFHIR())
            ),
            subject = Reference(display = "subject".asFHIR()),
            context = Reference(display = "context".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                value = DateTime("1905-08-23")
            ),
            dateAsserted = DateTime("1905-08-23"),
            informationSource = Reference(display = "informationSource".asFHIR()),
            derivedFrom = listOf(Reference(display = "derivedFrom".asFHIR())),
            reasonCode = listOf(CodeableConcept(text = "reasonCode".asFHIR())),
            reasonReference = listOf(Reference(display = "reasonReference".asFHIR())),
            note = listOf(Annotation(text = Markdown("annotation"))),
            dosage = listOf(Dosage(text = "dosage".asFHIR()))
        )

        val (transformed, validation) = RoninMedicationStatement.transform(medicationStatement, tenant)
        validation.alertIfErrors()

        transformed!!
        assertEquals(Id("test-12345"), transformed.id)
        assertEquals(
            RoninProfile.MEDICATION_STATEMENT.value,
            transformed.meta!!.profile[0].value
        )
        assertEquals(medicationStatement.implicitRules, transformed.implicitRules)
        assertEquals(medicationStatement.language, transformed.language)
        assertEquals(medicationStatement.text, transformed.text)
        assertEquals(medicationStatement.contained, transformed.contained)
        assertEquals(medicationStatement.extension, transformed.extension)
        assertEquals(medicationStatement.modifierExtension, transformed.modifierExtension)
        assertEquals(3, transformed.identifier.size)
        assertEquals(
            listOf(
                Identifier(value = "id".asFHIR()),
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "12345".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            transformed.identifier
        )
        assertEquals(medicationStatement.basedOn, transformed.basedOn)
        assertEquals(medicationStatement.partOf, transformed.partOf)
        assertEquals(medicationStatement.status, transformed.status)
        assertEquals(medicationStatement.statusReason, transformed.statusReason)
        assertEquals(medicationStatement.category, transformed.category)
        assertEquals(medicationStatement.medication, transformed.medication)
        assertEquals(medicationStatement.subject, transformed.subject)
        assertEquals(medicationStatement.context, transformed.context)
        assertEquals(medicationStatement.effective, transformed.effective)
        assertEquals(medicationStatement.informationSource, transformed.informationSource)
        assertEquals(medicationStatement.derivedFrom, transformed.derivedFrom)
        assertEquals(medicationStatement.reasonCode, transformed.reasonCode)
        assertEquals(medicationStatement.reasonReference, transformed.reasonReference)
        assertEquals(medicationStatement.note, transformed.note)
        assertEquals(medicationStatement.dosage, transformed.dosage)
    }

    @Test
    fun `transform succeeds with just required attributes`() {
        val medicationStatement = MedicationStatement(
            id = Id("12345"),
            status = MedicationStatementStatus.ACTIVE.asCode(),
            medication = DynamicValue(
                type = DynamicValueType.CODEABLE_CONCEPT,
                value = CodeableConcept(text = "medication".asFHIR())
            ),
            subject = Reference(display = "subject".asFHIR()),
            effective = DynamicValue(
                type = DynamicValueType.DATE_TIME,
                value = DateTime("1905-08-23")
            )
        )

        val (transformed, validation) = RoninMedicationStatement.transform(medicationStatement, tenant)
        validation.alertIfErrors()

        transformed!!
        assertEquals(2, transformed.identifier.size)
        assertEquals(
            listOf(
                Identifier(
                    type = CodeableConcepts.RONIN_FHIR_ID,
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "12345".asFHIR()
                ),
                Identifier(
                    type = CodeableConcepts.RONIN_TENANT,
                    system = CodeSystem.RONIN_TENANT.uri,
                    value = "test".asFHIR()
                )
            ),
            transformed.identifier
        )
        assertEquals(medicationStatement.status, transformed.status)
        assertEquals(medicationStatement.medication, transformed.medication)
        assertEquals(medicationStatement.subject, transformed.subject)
        assertEquals(medicationStatement.effective, transformed.effective)
    }

    @Test
    fun `transform can handle all dynamic types of medication`() {
        fun testMedication(type: DynamicValueType, value: Any) {
            val medicationStatement = MedicationStatement(
                id = Id("12345"),
                status = MedicationStatementStatus.ACTIVE.asCode(),
                medication = DynamicValue(
                    type = type,
                    value = value
                ),
                subject = Reference(display = "subject".asFHIR()),
                effective = DynamicValue(
                    type = DynamicValueType.DATE_TIME,
                    value = DateTime("1905-08-23")
                )
            )
            val (transformed, validation) = RoninMedicationStatement.transform(medicationStatement, tenant)
            validation.alertIfErrors()
            assertEquals(medicationStatement.medication, transformed!!.medication)
        }

        testMedication(DynamicValueType.CODEABLE_CONCEPT, CodeableConcept(text = "medication".asFHIR()))
        testMedication(DynamicValueType.REFERENCE, Reference(display = "reference".asFHIR()))
    }

    @Test
    fun `transform can handle all dynamic types of effective`() {
        fun testMedication(type: DynamicValueType, value: Any) {
            val medicationStatement = MedicationStatement(
                id = Id("12345"),
                status = MedicationStatementStatus.ACTIVE.asCode(),
                medication = DynamicValue(
                    type = DynamicValueType.CODEABLE_CONCEPT,
                    value = CodeableConcept(text = "codeableConcep".asFHIR())
                ),
                subject = Reference(display = "subject".asFHIR()),
                effective = DynamicValue(
                    type = type,
                    value = value
                )
            )
            val (transformed, validation) = RoninMedicationStatement.transform(medicationStatement, tenant)
            validation.alertIfErrors()
            assertEquals(medicationStatement.medication, transformed!!.medication)
        }

        testMedication(DynamicValueType.DATE_TIME, DateTime("2022-10-14"))
        testMedication(DynamicValueType.PERIOD, Period(start = DateTime("2022-10-14")))
    }

    @Test
    fun `transform fails with missing attributes`() {
        val medicationStatement = MedicationStatement()
        val (transformed, _) = RoninMedicationStatement.transform(medicationStatement, tenant)
        assertNull(transformed)
    }
}
