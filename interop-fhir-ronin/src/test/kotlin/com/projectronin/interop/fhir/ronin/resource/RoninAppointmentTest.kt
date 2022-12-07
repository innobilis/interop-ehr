package com.projectronin.interop.fhir.ronin.resource

import com.projectronin.interop.fhir.r4.CodeSystem
import com.projectronin.interop.fhir.r4.CodeableConcepts
import com.projectronin.interop.fhir.r4.datatype.CodeableConcept
import com.projectronin.interop.fhir.r4.datatype.Coding
import com.projectronin.interop.fhir.r4.datatype.DynamicValue
import com.projectronin.interop.fhir.r4.datatype.DynamicValueType
import com.projectronin.interop.fhir.r4.datatype.Extension
import com.projectronin.interop.fhir.r4.datatype.Identifier
import com.projectronin.interop.fhir.r4.datatype.Meta
import com.projectronin.interop.fhir.r4.datatype.Narrative
import com.projectronin.interop.fhir.r4.datatype.Participant
import com.projectronin.interop.fhir.r4.datatype.Period
import com.projectronin.interop.fhir.r4.datatype.Reference
import com.projectronin.interop.fhir.r4.datatype.primitive.Canonical
import com.projectronin.interop.fhir.r4.datatype.primitive.Code
import com.projectronin.interop.fhir.r4.datatype.primitive.DateTime
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.r4.datatype.primitive.Instant
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import com.projectronin.interop.fhir.r4.datatype.primitive.asFHIR
import com.projectronin.interop.fhir.r4.resource.Appointment
import com.projectronin.interop.fhir.r4.resource.ContainedResource
import com.projectronin.interop.fhir.r4.validate.resource.R4AppointmentValidator
import com.projectronin.interop.fhir.r4.valueset.AppointmentStatus
import com.projectronin.interop.fhir.r4.valueset.NarrativeStatus
import com.projectronin.interop.fhir.r4.valueset.ParticipationStatus
import com.projectronin.interop.fhir.ronin.conceptmap.ConceptMapClient
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RoninAppointmentTest {
    private lateinit var conceptMapClient: ConceptMapClient
    private lateinit var roninAppointment: RoninAppointment

    private val tenant = mockk<Tenant> {
        every { mnemonic } returns "test"
    }

    @BeforeEach
    fun setup() {
        conceptMapClient = mockk()
        roninAppointment = RoninAppointment.create(conceptMapClient)
    }

    @Test
    fun `always qualifies`() {
        assertTrue(
            roninAppointment.qualifies(
                Appointment(
                    status = AppointmentStatus.CANCELLED.asCode(),
                    participant = listOf(
                        Participant(
                            actor = Reference(display = "actor".asFHIR()),
                            status = ParticipationStatus.ACCEPTED.asCode()
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `validate checks ronin identifiers`() {
        val appointment = Appointment(
            id = Id("12345"),
            extension = listOf(statusExtension("cancelled")),
            status = AppointmentStatus.CANCELLED.asCode(),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            roninAppointment.validate(appointment, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR RONIN_TNNT_ID_001: Tenant identifier is required @ Appointment.identifier\n" +
                "ERROR RONIN_FHIR_ID_001: FHIR identifier is required @ Appointment.identifier",
            exception.message
        )
    }

    @Test
    fun `validate checks R4 profile`() {
        val appointment = Appointment(
            id = Id("12345"),
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
            extension = listOf(statusExtension("cancelled")),
            status = AppointmentStatus.CANCELLED.asCode(),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        mockkObject(R4AppointmentValidator)
        every { R4AppointmentValidator.validate(appointment, LocationContext(Appointment::class)) } returns validation {
            checkNotNull(
                null,
                RequiredFieldError(Appointment::basedOn),
                LocationContext(Appointment::class)
            )
        }

        val exception = assertThrows<IllegalArgumentException> {
            roninAppointment.validate(appointment, null).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR REQ_FIELD: basedOn is a required element @ Appointment.basedOn",
            exception.message
        )

        unmockkObject(R4AppointmentValidator)
    }

    @Test
    fun `validate succeeds`() {
        val appointment = Appointment(
            id = Id("12345"),
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
            extension = listOf(statusExtension("cancelled")),
            status = AppointmentStatus.CANCELLED.asCode(),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        roninAppointment.validate(appointment, null).alertIfErrors()
    }

    @Test
    fun `transforms appointment with all attributes`() {
        val appointment = Appointment(
            id = Id("12345"),
            meta = Meta(
                profile = listOf(Canonical("http://hl7.org/fhir/R4/appointment.html"))
            ),
            implicitRules = Uri("implicit-rules"),
            language = Code("en-US"),
            text = Narrative(status = NarrativeStatus.GENERATED.asCode(), div = "div".asFHIR()),
            contained = listOf(ContainedResource("""{"resourceType":"Banana","id":"24680"}""")),
            extension = listOf(
                Extension(
                    url = Uri("http://hl7.org/extension-1"),
                    value = DynamicValue(DynamicValueType.STRING, "value")
                ),
                Extension(
                    url = Uri("http://hl7.org/extension-2"),
                    value = DynamicValue(DynamicValueType.BOOLEAN, false)
                )
            ),
            modifierExtension = listOf(
                Extension(
                    url = Uri("http://localhost/modifier-extension"),
                    value = DynamicValue(DynamicValueType.STRING, "Value")
                )
            ),
            identifier = listOf(Identifier(value = "id".asFHIR())),
            status = AppointmentStatus.CANCELLED.asCode(),
            cancelationReason = CodeableConcept(text = "cancel reason".asFHIR()),
            serviceCategory = listOf(CodeableConcept(text = "service category".asFHIR())),
            serviceType = listOf(CodeableConcept(text = "service type".asFHIR())),
            specialty = listOf(CodeableConcept(text = "specialty".asFHIR())),
            appointmentType = CodeableConcept(text = "appointment type".asFHIR()),
            reasonCode = listOf(CodeableConcept(text = "reason code".asFHIR())),
            reasonReference = listOf(Reference(display = "reason reference".asFHIR())),
            priority = 1.asFHIR(),
            description = "appointment test".asFHIR(),
            supportingInformation = listOf(Reference(display = "supporting info".asFHIR())),
            start = Instant("2017-01-01T00:00:00Z"),
            end = Instant("2017-01-01T01:00:00Z"),
            minutesDuration = 15.asFHIR(),
            slot = listOf(Reference(display = "slot".asFHIR())),
            created = DateTime("2021-11-16"),
            comment = "comment".asFHIR(),
            patientInstruction = "patient instruction".asFHIR(),
            basedOn = listOf(Reference(display = "based on".asFHIR())),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            ),
            requestedPeriod = listOf(Period(start = DateTime("2021-11-16")))
        )

        conceptMapClient = mockk {
            every {
                getConceptMappingForEnum(
                    tenant,
                    "Appointment",
                    "Appointment.status",
                    Coding(
                        system = Uri("http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus"),
                        code = Code(value = "cancelled")
                    ),
                    AppointmentStatus::class
                )
            } returns Pair(statusCoding("cancelled"), statusExtension("cancelled"))
        }

        roninAppointment = RoninAppointment.create(conceptMapClient)
        val transformed = roninAppointment.transform(appointment, tenant)

        transformed!!
        assertEquals("Appointment", transformed.resourceType)
        assertEquals(Id(value = "test-12345"), transformed.id)
        assertEquals(
            Meta(profile = listOf(Canonical(RoninProfile.APPOINTMENT.value))),
            transformed.meta
        )
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
                    url = Uri("http://hl7.org/extension-1"),
                    value = DynamicValue(DynamicValueType.STRING, "value")
                ),
                Extension(
                    url = Uri("http://hl7.org/extension-2"),
                    value = DynamicValue(DynamicValueType.BOOLEAN, false)
                ),
                Extension(
                    url = Uri("http://projectronin.io/fhir/StructureDefinition/Extension/tenant-sourceAppointmentStatus"),
                    value = DynamicValue(
                        type = DynamicValueType.CODING,
                        value = Coding(
                            system = Uri("http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus"),
                            code = Code(value = "cancelled")
                        )
                    )
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
        assertEquals(AppointmentStatus.CANCELLED.asCode(), transformed.status)
        assertEquals(CodeableConcept(text = "cancel reason".asFHIR()), transformed.cancelationReason)
        assertEquals((listOf(CodeableConcept(text = "service category".asFHIR()))), transformed.serviceCategory)
        assertEquals((listOf(CodeableConcept(text = "service type".asFHIR()))), transformed.serviceType)
        assertEquals((listOf(CodeableConcept(text = "specialty".asFHIR()))), transformed.specialty)
        assertEquals(CodeableConcept(text = "appointment type".asFHIR()), transformed.appointmentType)
        assertEquals(listOf(CodeableConcept(text = "reason code".asFHIR())), transformed.reasonCode)
        assertEquals(listOf(Reference(display = "reason reference".asFHIR())), transformed.reasonReference)
        assertEquals(1.asFHIR(), transformed.priority)
        assertEquals("appointment test".asFHIR(), transformed.description)
        assertEquals(listOf(Reference(display = "supporting info".asFHIR())), transformed.supportingInformation)
        assertEquals(Instant(value = "2017-01-01T00:00:00Z"), transformed.start)
        assertEquals(Instant(value = "2017-01-01T01:00:00Z"), transformed.end)
        assertEquals(15.asFHIR(), transformed.minutesDuration)
        assertEquals(listOf(Reference(display = "slot".asFHIR())), transformed.slot)
        assertEquals(DateTime(value = "2021-11-16"), transformed.created)
        assertEquals("patient instruction".asFHIR(), transformed.patientInstruction)
        assertEquals(listOf(Reference(display = "based on".asFHIR())), transformed.basedOn)
        assertEquals(
            listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            ),
            transformed.participant
        )
        assertEquals(listOf(Period(start = DateTime(value = "2021-11-16"))), transformed.requestedPeriod)
    }

    @Test
    fun `transform appointment with only required attributes`() {
        val appointment = Appointment(
            id = Id("12345"),
            status = AppointmentStatus.CANCELLED.asCode(),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        conceptMapClient = mockk {
            every {
                getConceptMappingForEnum(
                    tenant,
                    "Appointment",
                    "Appointment.status",
                    Coding(
                        system = Uri("http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus"),
                        code = Code(value = "cancelled")
                    ),
                    AppointmentStatus::class
                )
            } returns Pair(statusCoding("cancelled"), statusExtension("cancelled"))
        }

        roninAppointment = RoninAppointment.create(conceptMapClient)
        val transformed = roninAppointment.transform(appointment, tenant)

        transformed!!
        assertEquals("Appointment", transformed.resourceType)
        assertEquals(Id(value = "test-12345"), transformed.id)
        assertEquals(
            Meta(profile = listOf(Canonical(RoninProfile.APPOINTMENT.value))),
            transformed.meta
        )
        assertNull(transformed.implicitRules)
        assertNull(transformed.language)
        assertNull(transformed.text)
        assertEquals(listOf<ContainedResource>(), transformed.contained)
        assertEquals(
            listOf(
                Extension(
                    url = Uri("http://projectronin.io/fhir/StructureDefinition/Extension/tenant-sourceAppointmentStatus"),
                    value = DynamicValue(
                        type = DynamicValueType.CODING,
                        value = Coding(
                            system = Uri("http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus"),
                            code = Code(value = "cancelled")
                        )
                    )
                )
            ),
            transformed.extension
        )
        assertEquals(listOf<Extension>(), transformed.modifierExtension)
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
        assertEquals(AppointmentStatus.CANCELLED.asCode(), transformed.status)
        assertNull(transformed.cancelationReason)
        assertEquals(listOf<CodeableConcept>(), transformed.serviceCategory)
        assertEquals(listOf<CodeableConcept>(), transformed.serviceType)
        assertEquals(listOf<CodeableConcept>(), transformed.specialty)
        assertNull(transformed.appointmentType)
        assertEquals(listOf<CodeableConcept>(), transformed.reasonCode)
        assertEquals(listOf<Reference>(), transformed.reasonReference)
        assertNull(transformed.priority)
        assertNull(transformed.description)
        assertEquals(listOf<Reference>(), transformed.supportingInformation)
        assertNull(transformed.start)
        assertNull(transformed.end)
        assertNull(transformed.minutesDuration)
        assertEquals(listOf<Reference>(), transformed.slot)
        assertNull(transformed.created)
        assertNull(transformed.patientInstruction)
        assertEquals(listOf<Reference>(), transformed.basedOn)
        assertEquals(
            listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            ),
            transformed.participant
        )
        assertEquals(listOf<Period>(), transformed.requestedPeriod)
    }

    @Test
    fun `transform fails for appointment with missing id`() {
        val appointment = Appointment(
            identifier = listOf(Identifier(value = "id".asFHIR())),
            extension = listOf(statusExtension("cancelled")),
            status = AppointmentStatus.CANCELLED.asCode(),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        conceptMapClient = mockk {
            every {
                getConceptMappingForEnum(
                    tenant,
                    "Appointment",
                    "Appointment.status",
                    Coding(
                        system = Uri("http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus"),
                        code = Code(value = "cancelled")
                    ),
                    AppointmentStatus::class
                )
            } returns Pair(statusCoding("cancelled"), statusExtension("cancelled"))
        }

        roninAppointment = RoninAppointment.create(conceptMapClient)

        val transformed = roninAppointment.transform(appointment, tenant)
        assertNull(transformed)
    }

    @Test
    fun `validate fails for appointment with missing identifiers`() {
        val appointment = Appointment(
            identifier = listOf(Identifier(value = "id".asFHIR())),
            extension = listOf(statusExtension("cancelled")),
            status = AppointmentStatus.CANCELLED.asCode(),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            roninAppointment.validate(appointment, LocationContext(Appointment::class)).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR RONIN_TNNT_ID_001: Tenant identifier is required @ Appointment.identifier\n" +
                "ERROR RONIN_FHIR_ID_001: FHIR identifier is required @ Appointment.identifier",
            exception.message
        )
    }

    @Test
    fun `validate fails for appointment with wrong status for missing start and end for participant`() {
        val appointment = Appointment(
            id = Id("12345"),
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
            extension = listOf(statusExtension("booked")),
            status = AppointmentStatus.BOOKED.asCode(),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            roninAppointment.validate(appointment, LocationContext(Appointment::class)).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR R4_APPT_002: Start and end can only be missing for appointments with the following statuses: proposed, cancelled, waitlist @ Appointment",
            exception.message
        )
    }

    @Test
    fun `validate fails for appointment with wrong status for cancelationReason`() {
        val appointment = Appointment(
            id = Id("12345"),
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
            extension = listOf(statusExtension("proposed")),
            status = AppointmentStatus.PROPOSED.asCode(),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            ),
            cancelationReason = CodeableConcept(
                text = "No Show".asFHIR(),
                coding = listOf(Coding(code = AppointmentStatus.NOSHOW.asCode()))
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            roninAppointment.validate(appointment, LocationContext(Appointment::class)).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR R4_APPT_003: cancelationReason is only used for appointments that have the following statuses: cancelled, noshow @ Appointment",
            exception.message
        )
    }

    @Test
    fun `validate fails for appointment missing status source extension`() {
        val appointment = Appointment(
            id = Id("12345"),
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
            status = AppointmentStatus.CANCELLED.asCode(),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            roninAppointment.validate(appointment, LocationContext(Appointment::class)).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR RONIN_APPT_001: Appointment extension list may not be empty @ Appointment.status",
            exception.message
        )
    }

    @Test
    fun `validate fails for appointment with wrong URL in status source extension`() {
        val appointment = Appointment(
            id = Id("12345"),
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
            extension = listOf(
                Extension(
                    url = Uri("emailSystemExtension"),
                    value = DynamicValue(
                        type = DynamicValueType.CODING,
                        value = statusCoding("cancelled")
                    )
                )
            ),
            status = AppointmentStatus.CANCELLED.asCode(),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            roninAppointment.validate(appointment, LocationContext(Appointment::class)).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR RONIN_APPT_002: Tenant source appointment status extension is missing or invalid @ Appointment.status",
            exception.message
        )
    }

    @Test
    fun `validate fails for appointment with missing URL in status source extension`() {
        val appointment = Appointment(
            id = Id("12345"),
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
            extension = listOf(
                Extension(
                    value = DynamicValue(
                        type = DynamicValueType.CODING,
                        value = statusCoding("cancelled")
                    )
                )
            ),
            status = AppointmentStatus.CANCELLED.asCode(),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            roninAppointment.validate(appointment, LocationContext(Appointment::class)).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR RONIN_APPT_002: Tenant source appointment status extension is missing or invalid @ Appointment.status\n" +
                "ERROR REQ_FIELD: url is a required element @ Appointment.extension[0].url",
            exception.message
        )
    }

    @Test
    fun `validate fails for appointment with right URL and wrong data type in status source extension`() {
        val appointment = Appointment(
            id = Id("12345"),
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
            extension = listOf(
                Extension(
                    url = Uri("http://projectronin.io/fhir/StructureDefinition/Extension/tenant-sourceAppointmentStatus"),
                    value = DynamicValue(
                        type = DynamicValueType.BOOLEAN,
                        value = true
                    )
                )
            ),
            status = AppointmentStatus.CANCELLED.asCode(),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            roninAppointment.validate(appointment, LocationContext(Appointment::class)).alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR RONIN_APPT_002: Tenant source appointment status extension is missing or invalid @ Appointment.status",
            exception.message
        )
    }

    @Test
    fun `transform succeeds for appointment status - when concept map returns a good value`() {
        val appointment = Appointment(
            id = Id("12345"),
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
            status = Code(value = "abc"),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        conceptMapClient = mockk {
            every {
                getConceptMappingForEnum(
                    tenant,
                    "Appointment",
                    "Appointment.status",
                    Coding(
                        system = Uri("http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus"),
                        code = Code(value = "abc")
                    ),
                    AppointmentStatus::class
                )
            } returns Pair(statusCoding("cancelled"), statusExtension("abc"))
        }

        roninAppointment = RoninAppointment.create(conceptMapClient)

        val transformed = roninAppointment.transform(appointment, tenant)
        transformed!!
        assertEquals(
            listOf(
                Extension(
                    url = Uri("http://projectronin.io/fhir/StructureDefinition/Extension/tenant-sourceAppointmentStatus"),
                    value = DynamicValue(
                        type = DynamicValueType.CODING,
                        value = Coding(
                            system = Uri("http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus"),
                            code = Code(value = "abc")
                        )
                    )
                )
            ),
            transformed.extension
        )
        assertEquals(
            Code(value = "cancelled"),
            transformed.status
        )
    }

    @Test
    fun `transform fails for appointment status - when concept map has no match`() {
        val appointment = Appointment(
            id = Id("12345"),
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
            status = Code(value = "xyz"),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        conceptMapClient = mockk {
            every {
                getConceptMappingForEnum(
                    tenant,
                    "Appointment",
                    "Appointment.status",
                    Coding(
                        system = Uri("http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus"),
                        code = Code(value = "xyz")
                    ),
                    AppointmentStatus::class
                )
            } returns null
        }

        roninAppointment = RoninAppointment.create(conceptMapClient)

        val pair = roninAppointment.transformInternal(appointment, LocationContext(Appointment::class), tenant)
        val exception = assertThrows<IllegalArgumentException> {
            pair.second.alertIfErrors()
        }
        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR NOV_CONMAP_LOOKUP: Tenant source value 'xyz' has no target defined in " +
                "http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus @ Appointment.status",
            exception.message
        )
    }

    @Test
    fun `transform fails for appointment status - when concept map gives a result not in required value set`() {
        val appointment = Appointment(
            id = Id("12345"),
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
            status = Code(value = "xyz"),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )
        conceptMapClient = mockk {
            every {
                getConceptMappingForEnum(
                    tenant,
                    "Appointment",
                    "Appointment.status",
                    Coding(
                        system = Uri("http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus"),
                        code = Code(value = "xyz")
                    ),
                    AppointmentStatus::class
                )
            } returns Pair(statusCoding("waiting"), statusExtension("xyz"))
        }
        roninAppointment = RoninAppointment.create(conceptMapClient)

        val transformResult =
            roninAppointment.transformInternal(appointment, LocationContext(Appointment::class), tenant)
        val exception = assertThrows<IllegalArgumentException> {
            transformResult.second.alertIfErrors()
        }

        assertEquals(
            "Encountered validation error(s):\n" +
                "ERROR INV_CONMAP_VALUE_SET: http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus " +
                "mapped 'xyz' to 'waiting' which is outside of required value set @ Appointment.status",
            exception.message
        )
    }

    @Test
    fun `transform succeeds for appointment status with empty source value - if empty source value is in concept map`() {
        val appointment = Appointment(
            id = Id("12345"),
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
            status = Code(value = ""),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        conceptMapClient = mockk {
            every {
                getConceptMappingForEnum(
                    tenant,
                    "Appointment",
                    "Appointment.status",
                    Coding(
                        system = Uri("http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus"),
                        code = Code(value = "")
                    ),
                    AppointmentStatus::class
                )
            } returns Pair(statusCoding("booked"), statusExtension(""))
        }

        roninAppointment = RoninAppointment.create(conceptMapClient)

        val transformedBad = roninAppointment.transform(appointment, tenant)
        assertNull(transformedBad)
    }

    @Test
    fun `transform fails if the concept map result for status invalidates an invariant for Appointment - start and end`() {
        val appointment = Appointment(
            id = Id("12345"),
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
            extension = listOf(statusExtension("cancelled")),
            status = AppointmentStatus.CANCELLED.asCode(),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            )
        )

        conceptMapClient = mockk {
            every {
                getConceptMappingForEnum(
                    tenant,
                    "Appointment",
                    "Appointment.status",
                    Coding(
                        system = Uri("http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus"),
                        code = Code(value = "cancelled")
                    ),
                    AppointmentStatus::class
                )
            } returns Pair(statusCoding("booked"), statusExtension("cancelled"))
        }

        roninAppointment = RoninAppointment.create(conceptMapClient)

        val transformedBad = roninAppointment.transform(appointment, tenant)
        assertNull(transformedBad)
    }

    @Test
    fun `transform fails if the concept map result for status invalidates an invariant for Appointment - cancelation reason`() {
        val appointment = Appointment(
            id = Id("12345"),
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
            status = AppointmentStatus.PROPOSED.asCode(),
            participant = listOf(
                Participant(
                    actor = Reference(display = "actor".asFHIR()),
                    status = ParticipationStatus.ACCEPTED.asCode()
                )
            ),
            cancelationReason = CodeableConcept(
                text = "No Show".asFHIR(),
                coding = listOf(Coding(code = AppointmentStatus.NOSHOW.asCode()))
            )
        )

        conceptMapClient = mockk {
            every {
                getConceptMappingForEnum(
                    tenant,
                    "Appointment",
                    "Appointment.status",
                    Coding(
                        system = Uri("http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus"),
                        code = Code(value = "proposed")
                    ),
                    AppointmentStatus::class
                )
            } returns Pair(statusCoding("waitlist"), statusExtension("proposed"))
        }

        roninAppointment = RoninAppointment.create(conceptMapClient)

        val transformedBad = roninAppointment.transform(appointment, tenant)
        assertNull(transformedBad)
    }

    private fun statusCoding(value: String) = Coding(
        system = Uri("http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus"),
        code = Code(value = value)
    )

    private fun statusExtension(value: String) = Extension(
        url = Uri("http://projectronin.io/fhir/StructureDefinition/Extension/tenant-sourceAppointmentStatus"),
        value = DynamicValue(
            type = DynamicValueType.CODING,
            value = Coding(
                system = Uri("http://projectronin.io/fhir/CodeSystem/test/AppointmentStatus"),
                code = Code(value = value)
            )
        )
    )
}
