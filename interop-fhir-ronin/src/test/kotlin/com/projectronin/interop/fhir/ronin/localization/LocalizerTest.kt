package com.projectronin.interop.fhir.ronin.localization

import com.projectronin.interop.fhir.r4.datatype.Address
import com.projectronin.interop.fhir.r4.datatype.Annotation
import com.projectronin.interop.fhir.r4.datatype.Attachment
import com.projectronin.interop.fhir.r4.datatype.AvailableTime
import com.projectronin.interop.fhir.r4.datatype.CodeableConcept
import com.projectronin.interop.fhir.r4.datatype.Coding
import com.projectronin.interop.fhir.r4.datatype.Communication
import com.projectronin.interop.fhir.r4.datatype.ConditionEvidence
import com.projectronin.interop.fhir.r4.datatype.ConditionStage
import com.projectronin.interop.fhir.r4.datatype.Contact
import com.projectronin.interop.fhir.r4.datatype.ContactPoint
import com.projectronin.interop.fhir.r4.datatype.Dosage
import com.projectronin.interop.fhir.r4.datatype.DoseAndRate
import com.projectronin.interop.fhir.r4.datatype.Duration
import com.projectronin.interop.fhir.r4.datatype.DynamicValue
import com.projectronin.interop.fhir.r4.datatype.DynamicValueType
import com.projectronin.interop.fhir.r4.datatype.Extension
import com.projectronin.interop.fhir.r4.datatype.HumanName
import com.projectronin.interop.fhir.r4.datatype.Identifier
import com.projectronin.interop.fhir.r4.datatype.Meta
import com.projectronin.interop.fhir.r4.datatype.Narrative
import com.projectronin.interop.fhir.r4.datatype.NotAvailable
import com.projectronin.interop.fhir.r4.datatype.ObservationComponent
import com.projectronin.interop.fhir.r4.datatype.ObservationReferenceRange
import com.projectronin.interop.fhir.r4.datatype.Participant
import com.projectronin.interop.fhir.r4.datatype.PatientLink
import com.projectronin.interop.fhir.r4.datatype.Period
import com.projectronin.interop.fhir.r4.datatype.Qualification
import com.projectronin.interop.fhir.r4.datatype.Quantity
import com.projectronin.interop.fhir.r4.datatype.Range
import com.projectronin.interop.fhir.r4.datatype.Ratio
import com.projectronin.interop.fhir.r4.datatype.Reference
import com.projectronin.interop.fhir.r4.datatype.SimpleQuantity
import com.projectronin.interop.fhir.r4.datatype.Timing
import com.projectronin.interop.fhir.r4.datatype.TimingRepeat
import com.projectronin.interop.fhir.r4.datatype.primitive.Base64Binary
import com.projectronin.interop.fhir.r4.datatype.primitive.Canonical
import com.projectronin.interop.fhir.r4.datatype.primitive.Code
import com.projectronin.interop.fhir.r4.datatype.primitive.DateTime
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.r4.datatype.primitive.Instant
import com.projectronin.interop.fhir.r4.datatype.primitive.Markdown
import com.projectronin.interop.fhir.r4.datatype.primitive.PositiveInt
import com.projectronin.interop.fhir.r4.datatype.primitive.PrimitiveData
import com.projectronin.interop.fhir.r4.datatype.primitive.Time
import com.projectronin.interop.fhir.r4.datatype.primitive.UnsignedInt
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import com.projectronin.interop.fhir.r4.datatype.primitive.Url
import com.projectronin.interop.fhir.r4.valueset.AddressType
import com.projectronin.interop.fhir.r4.valueset.AddressUse
import com.projectronin.interop.fhir.r4.valueset.ContactPointSystem
import com.projectronin.interop.fhir.r4.valueset.ContactPointUse
import com.projectronin.interop.fhir.r4.valueset.IdentifierUse
import com.projectronin.interop.fhir.r4.valueset.LinkType
import com.projectronin.interop.fhir.r4.valueset.NameUse
import com.projectronin.interop.fhir.r4.valueset.NarrativeStatus
import com.projectronin.interop.fhir.r4.valueset.ParticipantRequired
import com.projectronin.interop.fhir.r4.valueset.ParticipationStatus
import com.projectronin.interop.fhir.util.asCode
import com.projectronin.interop.tenant.config.model.Tenant
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible

// This test does not need all of these tests, however, I want to ensure all current Localizers functions this
// is replacing continue to work.
class LocalizerTest {
    private val tenant = mockk<Tenant> {
        every { mnemonic } returns "test"
    }
    private val nonLocalizableExtensions = listOf(
        Extension(
            id = "9012",
            url = Uri("http://localhost/extension"),
            value = DynamicValue(DynamicValueType.STRING, "Value")
        )
    )
    private val someNonLocalizableExtensions = listOf(
        Extension(
            id = "9012",
            url = Uri("http://localhost/extension"),
            value = DynamicValue(DynamicValueType.STRING, "Value")
        ),
        Extension(
            id = "5678",
            url = Uri("http://localhost/extension"),
            value = DynamicValue(DynamicValueType.REFERENCE, Reference(reference = "Patient/1234"))
        )
    )
    private val localizableExtensions = listOf(
        Extension(
            id = "5678",
            url = Uri("http://localhost/extension"),
            value = DynamicValue(DynamicValueType.REFERENCE, Reference(reference = "Patient/1234"))
        )
    )
    private val localizedExtensions = listOf(
        Extension(
            id = "5678",
            url = Uri("http://localhost/extension"),
            value = DynamicValue(DynamicValueType.REFERENCE, Reference(reference = "Patient/test-1234"))
        )
    )
    private val mixedLocalizedExtensions = listOf(
        Extension(
            id = "9012",
            url = Uri("http://localhost/extension"),
            value = DynamicValue(DynamicValueType.STRING, "Value")
        ),
        Extension(
            id = "5678",
            url = Uri("http://localhost/extension"),
            value = DynamicValue(DynamicValueType.REFERENCE, Reference(reference = "Patient/test-1234"))
        )
    )

    val localizer = Localizer::class.objectInstance!!

    private fun localizeId(id: Id, parameterName: String = "id"): Id? {
        val localizeIdMethod = Localizer::class.functions.find { it.name == "localizeId" }!!
        localizeIdMethod.isAccessible = true
        val localized = localizeIdMethod.call(localizer, id, parameterName, tenant) as? Id
        localizeIdMethod.isAccessible = false
        return localized
    }

    private fun localizeReference(reference: Reference, parameterName: String = "reference"): Reference? {
        val localizeReferenceMethod = Localizer::class.functions.find { it.name == "localizeReference" }!!
        localizeReferenceMethod.isAccessible = true
        val localized = localizeReferenceMethod.call(localizer, reference, parameterName, tenant) as? Reference
        localizeReferenceMethod.isAccessible = false
        return localized
    }

    @Test
    fun `attempt to localize a non-data class results in exception`() {
        class Sample(val id: Id)

        assertThrows<IllegalStateException> { localizer.localize(Sample(id = Id("1234")), tenant) }
    }

    @Test
    fun `prepends tenant mnemonic to id`() {
        assertEquals(Id("test-id-value"), localizeId(Id("id-value")))
    }

    @Test
    fun `returns current address if address has no localizable information`() {
        val address = Address(
            id = "12345",
            extension = nonLocalizableExtensions,
            use = AddressUse.HOME.asCode(),
            type = AddressType.POSTAL.asCode(),
            text = "Address",
            line = listOf("Line"),
            city = "City",
            district = "District",
            state = "State",
            postalCode = "PostalCode",
            country = "Country",
            period = Period(start = DateTime("2021"))
        )
        val localizedAddress = localizer.localize(address, tenant)
        assertTrue(address === localizedAddress)
    }

    @Test
    fun `localizes address with localizable extension`() {
        val address = Address(
            id = "12345",
            extension = localizableExtensions,
            use = AddressUse.HOME.asCode(),
            type = AddressType.POSTAL.asCode(),
            text = "Address",
            line = listOf("Line"),
            city = "City",
            district = "District",
            state = "State",
            postalCode = "PostalCode",
            country = "Country",
            period = Period(start = DateTime("2021"))
        )

        val localizedAddress = localizer.localize(address, tenant)
        assertNotEquals(address, localizedAddress)

        val expectedAddress = Address(
            id = "12345",
            extension = localizedExtensions,
            use = AddressUse.HOME.asCode(),
            type = AddressType.POSTAL.asCode(),
            text = "Address",
            line = listOf("Line"),
            city = "City",
            district = "District",
            state = "State",
            postalCode = "PostalCode",
            country = "Country",
            period = Period(start = DateTime("2021"))
        )
        assertEquals(expectedAddress, localizedAddress)
    }

    @Test
    fun `localizes address with localizable period`() {
        val address = Address(
            id = "12345",
            extension = nonLocalizableExtensions,
            use = AddressUse.HOME.asCode(),
            type = AddressType.POSTAL.asCode(),
            text = "Address",
            line = listOf("Line"),
            city = "City",
            district = "District",
            state = "State",
            postalCode = "PostalCode",
            country = "Country",
            period = Period(extension = localizableExtensions, start = DateTime("2021"))
        )

        val localizedAddress = localizer.localize(address, tenant)
        assertNotEquals(address, localizedAddress)

        val expectedAddress = Address(
            id = "12345",
            extension = nonLocalizableExtensions,
            use = AddressUse.HOME.asCode(),
            type = AddressType.POSTAL.asCode(),
            text = "Address",
            line = listOf("Line"),
            city = "City",
            district = "District",
            state = "State",
            postalCode = "PostalCode",
            country = "Country",
            period = Period(extension = localizedExtensions, start = DateTime("2021"))
        )
        assertEquals(expectedAddress, localizedAddress)
    }

    @Test
    fun `localizes address with localizable extension and period`() {
        val address = Address(
            id = "12345",
            extension = localizableExtensions,
            use = AddressUse.HOME.asCode(),
            type = AddressType.POSTAL.asCode(),
            text = "Address",
            line = listOf("Line"),
            city = "City",
            district = "District",
            state = "State",
            postalCode = "PostalCode",
            country = "Country",
            period = Period(extension = localizableExtensions, start = DateTime("2021"))
        )

        val localizedAddress = localizer.localize(address, tenant)
        assertNotEquals(address, localizedAddress)

        val expectedAddress = Address(
            id = "12345",
            extension = localizedExtensions,
            use = AddressUse.HOME.asCode(),
            type = AddressType.POSTAL.asCode(),
            text = "Address",
            line = listOf("Line"),
            city = "City",
            district = "District",
            state = "State",
            postalCode = "PostalCode",
            country = "Country",
            period = Period(extension = localizedExtensions, start = DateTime("2021"))
        )
        assertEquals(expectedAddress, localizedAddress)
    }

    @Test
    fun `returns current attachment if attachment has no localizable information`() {
        val attachment = Attachment(
            id = "12345",
            extension = nonLocalizableExtensions,
            contentType = Code("contentType"),
            language = Code("language"),
            data = Base64Binary("abcd"),
            url = Url("url"),
            size = UnsignedInt(4),
            hash = Base64Binary("efgh"),
            title = "Title",
            creation = DateTime("2021")
        )
        val localizedAttachment = localizer.localize(attachment, tenant)
        assertTrue(attachment === localizedAttachment)
    }

    @Test
    fun `localizes attachment with localizable extension`() {
        val attachment = Attachment(
            id = "12345",
            extension = localizableExtensions,
            contentType = Code("contentType"),
            language = Code("language"),
            data = Base64Binary("abcd"),
            url = Url("url"),
            size = UnsignedInt(4),
            hash = Base64Binary("efgh"),
            title = "Title",
            creation = DateTime("2021")
        )
        val localizedAttachment = localizer.localize(attachment, tenant)
        assertNotEquals(attachment, localizedAttachment)

        val expectedAttachment = Attachment(
            id = "12345",
            extension = localizedExtensions,
            contentType = Code("contentType"),
            language = Code("language"),
            data = Base64Binary("abcd"),
            url = Url("url"),
            size = UnsignedInt(4),
            hash = Base64Binary("efgh"),
            title = "Title",
            creation = DateTime("2021")
        )
        assertEquals(expectedAttachment, localizedAttachment)
    }

    @Test
    fun `returns current available time if available time has no localizable information`() {
        val availableTime = AvailableTime(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            daysOfWeek = listOf(Code("day")),
            allDay = true,
            availableStartTime = Time("08:00:00"),
            availableEndTime = Time("20:00:00")
        )
        val localizedAvailableTime = localizer.localize(availableTime, tenant)
        assertTrue(availableTime === localizedAvailableTime)
    }

    @Test
    fun `localizes available time with localizable extension`() {
        val availableTime = AvailableTime(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            daysOfWeek = listOf(Code("day")),
            allDay = true,
            availableStartTime = Time("08:00:00"),
            availableEndTime = Time("20:00:00")
        )
        val localizedAvailableTime = localizer.localize(availableTime, tenant)
        assertNotEquals(availableTime, localizedAvailableTime)

        val expectedAvailableTime = AvailableTime(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = nonLocalizableExtensions,
            daysOfWeek = listOf(Code("day")),
            allDay = true,
            availableStartTime = Time("08:00:00"),
            availableEndTime = Time("20:00:00")
        )
        assertEquals(expectedAvailableTime, localizedAvailableTime)
    }

    @Test
    fun `localizes available time with localizable modifier extension`() {
        val availableTime = AvailableTime(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizableExtensions,
            daysOfWeek = listOf(Code("day")),
            allDay = true,
            availableStartTime = Time("08:00:00"),
            availableEndTime = Time("20:00:00")
        )
        val localizedAvailableTime = localizer.localize(availableTime, tenant)
        assertNotEquals(availableTime, localizedAvailableTime)

        val expectedAvailableTime = AvailableTime(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizedExtensions,
            daysOfWeek = listOf(Code("day")),
            allDay = true,
            availableStartTime = Time("08:00:00"),
            availableEndTime = Time("20:00:00")
        )
        assertEquals(expectedAvailableTime, localizedAvailableTime)
    }

    @Test
    fun `localizes available time with localizable extension and modifier extension`() {
        val availableTime = AvailableTime(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = localizableExtensions,
            daysOfWeek = listOf(Code("day")),
            allDay = true,
            availableStartTime = Time("08:00:00"),
            availableEndTime = Time("20:00:00")
        )
        val localizedAvailableTime = localizer.localize(availableTime, tenant)
        assertNotEquals(availableTime, localizedAvailableTime)

        val expectedAvailableTime = AvailableTime(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = localizedExtensions,
            daysOfWeek = listOf(Code("day")),
            allDay = true,
            availableStartTime = Time("08:00:00"),
            availableEndTime = Time("20:00:00")
        )
        assertEquals(expectedAvailableTime, localizedAvailableTime)
    }

    @Test
    fun `returns current codeable concept if codeable concept has no localizable information`() {
        val codeableConcept = CodeableConcept(
            id = "12345",
            extension = nonLocalizableExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val localizedCodeableConcept = localizer.localize(codeableConcept, tenant)
        assertTrue(codeableConcept === localizedCodeableConcept)
    }

    @Test
    fun `localizes codeable concept with localizable extension`() {
        val codeableConcept = CodeableConcept(
            id = "12345",
            extension = localizableExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val localizedCodeableConcept = localizer.localize(codeableConcept, tenant)
        assertNotEquals(codeableConcept, localizedCodeableConcept)

        val expectedCodeableConcept = CodeableConcept(
            id = "12345",
            extension = localizedExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        assertEquals(expectedCodeableConcept, localizedCodeableConcept)
    }

    @Test
    fun `returns current coding as not updated if coding has no localizable information`() {
        val coding = Coding(
            id = "12345",
            extension = nonLocalizableExtensions,
            system = Uri("system"),
            version = "version",
            code = Code("code"),
            display = "Display",
            userSelected = true
        )
        val localizedCoding = localizer.localize(coding, tenant)
        assertTrue(localizedCoding === coding)
    }

    @Test
    fun `localizes coding and sets as updated with localizable extension`() {
        val coding = Coding(
            id = "12345",
            extension = localizableExtensions,
            system = Uri("system"),
            version = "version",
            code = Code("code"),
            display = "Display",
            userSelected = true
        )
        val localizedCoding = localizer.localize(coding, tenant)
        assertNotEquals(coding, localizedCoding)

        val expectedCoding = Coding(
            id = "12345",
            extension = localizedExtensions,
            system = Uri("system"),
            version = "version",
            code = Code("code"),
            display = "Display",
            userSelected = true
        )
        assertEquals(expectedCoding, localizedCoding)
    }

    @Test
    fun `returns current communication if communication has no localizable information`() {
        val communication = Communication(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            language = CodeableConcept(extension = nonLocalizableExtensions)
        )
        val localizedCommunication = localizer.localize(communication, tenant)
        assertTrue(localizedCommunication === communication)
    }

    @Test
    fun `localizes communication with localizable extension`() {
        val communication = Communication(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            language = CodeableConcept(extension = nonLocalizableExtensions)
        )
        val localizedCommunication = localizer.localize(communication, tenant)
        assertNotEquals(localizedCommunication, communication)

        val expectedCommunication = Communication(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = nonLocalizableExtensions,
            language = CodeableConcept(extension = nonLocalizableExtensions)
        )
        assertEquals(expectedCommunication, localizedCommunication)
    }

    @Test
    fun `localizes communication with localizable modifierExtension`() {
        val communication = Communication(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizableExtensions,
            language = CodeableConcept(extension = nonLocalizableExtensions)
        )
        val localizedCommunication = localizer.localize(communication, tenant)
        assertNotEquals(localizedCommunication, communication)

        val expectedCommunication = Communication(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizedExtensions,
            language = CodeableConcept(extension = nonLocalizableExtensions)
        )
        assertEquals(expectedCommunication, localizedCommunication)
    }

    @Test
    fun `localizes communication with localizable language`() {
        val communication = Communication(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            language = CodeableConcept(extension = localizableExtensions)
        )
        val localizedCommunication = localizer.localize(communication, tenant)
        assertNotEquals(localizedCommunication, communication)

        val expectedCommunication = Communication(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            language = CodeableConcept(extension = localizedExtensions)
        )
        assertEquals(expectedCommunication, localizedCommunication)
    }

    @Test
    fun `localizes communication with all localizable values`() {
        val communication = Communication(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = localizableExtensions,
            language = CodeableConcept(extension = localizableExtensions)
        )
        val localizedCommunication = localizer.localize(communication, tenant)
        assertNotEquals(localizedCommunication, communication)

        val expectedCommunication = Communication(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = localizedExtensions,
            language = CodeableConcept(extension = localizedExtensions)
        )
        assertEquals(expectedCommunication, localizedCommunication)
    }

    @Test
    fun `returns current contact if contact has no localizable information`() {
        val contact = Contact(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            relationship = listOf(),
            name = HumanName(extension = nonLocalizableExtensions),
            telecom = listOf(ContactPoint(extension = nonLocalizableExtensions)),
            organization = Reference(extension = nonLocalizableExtensions),
            period = Period(extension = nonLocalizableExtensions)
        )
        val localizedContact = localizer.localize(contact, tenant)
        assertTrue(localizedContact === contact)
    }

    @Test
    fun `localizes contact with localizable extension`() {
        val contact = Contact(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            relationship = listOf(),
            name = HumanName(extension = nonLocalizableExtensions),
            telecom = listOf(ContactPoint(extension = nonLocalizableExtensions)),
            organization = Reference(extension = nonLocalizableExtensions),
            period = Period(extension = nonLocalizableExtensions)
        )
        val localizedContact = localizer.localize(contact, tenant)
        assertNotEquals(localizedContact, contact)

        val expectedContact = Contact(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = nonLocalizableExtensions,
            relationship = listOf(),
            name = HumanName(extension = nonLocalizableExtensions),
            telecom = listOf(ContactPoint(extension = nonLocalizableExtensions)),
            organization = Reference(extension = nonLocalizableExtensions),
            period = Period(extension = nonLocalizableExtensions)
        )
        assertEquals(expectedContact, localizedContact)
    }

    @Test
    fun `localizes contact with localizable modifierExtension`() {
        val contact = Contact(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizableExtensions,
            relationship = listOf(),
            name = HumanName(extension = nonLocalizableExtensions),
            telecom = listOf(ContactPoint(extension = nonLocalizableExtensions)),
            organization = Reference(extension = nonLocalizableExtensions),
            period = Period(extension = nonLocalizableExtensions)
        )
        val localizedContact = localizer.localize(contact, tenant)
        assertNotEquals(localizedContact, contact)

        val expectedContact = Contact(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizedExtensions,
            relationship = listOf(),
            name = HumanName(extension = nonLocalizableExtensions),
            telecom = listOf(ContactPoint(extension = nonLocalizableExtensions)),
            organization = Reference(extension = nonLocalizableExtensions),
            period = Period(extension = nonLocalizableExtensions)
        )
        assertEquals(expectedContact, localizedContact)
    }

    @Test
    fun `localizes contact with localizable name`() {
        val contact = Contact(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            relationship = listOf(),
            name = HumanName(extension = localizableExtensions),
            telecom = listOf(ContactPoint(extension = nonLocalizableExtensions)),
            organization = Reference(extension = nonLocalizableExtensions),
            period = Period(extension = nonLocalizableExtensions)
        )
        val localizedContact = localizer.localize(contact, tenant)
        assertNotEquals(localizedContact, contact)

        val expectedContact = Contact(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            relationship = listOf(),
            name = HumanName(extension = localizedExtensions),
            telecom = listOf(ContactPoint(extension = nonLocalizableExtensions)),
            organization = Reference(extension = nonLocalizableExtensions),
            period = Period(extension = nonLocalizableExtensions)
        )
        assertEquals(expectedContact, localizedContact)
    }

    @Test
    fun `localizes contact with localizable telecom`() {
        val contact = Contact(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            relationship = listOf(),
            name = HumanName(extension = nonLocalizableExtensions),
            telecom = listOf(ContactPoint(extension = localizableExtensions)),
            organization = Reference(extension = nonLocalizableExtensions),
            period = Period(extension = nonLocalizableExtensions)
        )
        val localizedContact = localizer.localize(contact, tenant)
        assertNotEquals(localizedContact, contact)

        val expectedContact = Contact(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            relationship = listOf(),
            name = HumanName(extension = nonLocalizableExtensions),
            telecom = listOf(ContactPoint(extension = localizedExtensions)),
            organization = Reference(extension = nonLocalizableExtensions),
            period = Period(extension = nonLocalizableExtensions)
        )
        assertEquals(expectedContact, localizedContact)
    }

    @Test
    fun `localizes contact with localizable organization`() {
        val contact = Contact(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            relationship = listOf(),
            name = HumanName(extension = nonLocalizableExtensions),
            telecom = listOf(ContactPoint(extension = nonLocalizableExtensions)),
            organization = Reference(extension = localizableExtensions),
            period = Period(extension = nonLocalizableExtensions)
        )
        val localizedContact = localizer.localize(contact, tenant)
        assertNotEquals(localizedContact, contact)

        val expectedContact = Contact(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            relationship = listOf(),
            name = HumanName(extension = nonLocalizableExtensions),
            telecom = listOf(ContactPoint(extension = nonLocalizableExtensions)),
            organization = Reference(extension = localizedExtensions),
            period = Period(extension = nonLocalizableExtensions)
        )
        assertEquals(expectedContact, localizedContact)
    }

    @Test
    fun `localizes contact with localizable period`() {
        val contact = Contact(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            relationship = listOf(),
            name = HumanName(extension = nonLocalizableExtensions),
            telecom = listOf(ContactPoint(extension = nonLocalizableExtensions)),
            organization = Reference(extension = nonLocalizableExtensions),
            period = Period(extension = localizableExtensions)
        )
        val localizedContact = localizer.localize(contact, tenant)
        assertNotEquals(localizedContact, contact)

        val expectedContact = Contact(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            relationship = listOf(),
            name = HumanName(extension = nonLocalizableExtensions),
            telecom = listOf(ContactPoint(extension = nonLocalizableExtensions)),
            organization = Reference(extension = nonLocalizableExtensions),
            period = Period(extension = localizedExtensions)
        )
        assertEquals(expectedContact, localizedContact)
    }

    @Test
    fun `localizes contact with all localizable values`() {
        val contact = Contact(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = localizableExtensions,
            relationship = listOf(),
            name = HumanName(extension = localizableExtensions),
            telecom = listOf(ContactPoint(extension = localizableExtensions)),
            organization = Reference(extension = localizableExtensions),
            period = Period(extension = localizableExtensions)
        )
        val localizedContact = localizer.localize(contact, tenant)
        assertNotEquals(localizedContact, contact)

        val expectedContact = Contact(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = localizedExtensions,
            relationship = listOf(),
            name = HumanName(extension = localizedExtensions),
            telecom = listOf(ContactPoint(extension = localizedExtensions)),
            organization = Reference(extension = localizedExtensions),
            period = Period(extension = localizedExtensions)
        )
        assertEquals(expectedContact, localizedContact)
    }

    @Test
    fun `localizes contact with null name, organization and period`() {
        val contact = Contact(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = localizableExtensions,
            relationship = listOf(),
            telecom = listOf(ContactPoint(extension = localizableExtensions))
        )
        val localizedContact = localizer.localize(contact, tenant)
        assertNotEquals(localizedContact, contact)

        val expectedContact = Contact(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = localizedExtensions,
            relationship = listOf(),
            telecom = listOf(ContactPoint(extension = localizedExtensions))
        )
        assertEquals(expectedContact, localizedContact)
    }

    @Test
    fun `returns current contact point if contact point has no localizable information`() {
        val contactPoint = ContactPoint(
            id = "12345",
            extension = nonLocalizableExtensions,
            system = ContactPointSystem.PHONE.asCode(),
            value = "8675309",
            use = ContactPointUse.OLD.asCode(),
            rank = PositiveInt(3),
            period = Period(start = DateTime("2021"))
        )
        val localizedContactPoint = localizer.localize(contactPoint, tenant)
        assertTrue(contactPoint === localizedContactPoint)
    }

    @Test
    fun `localizes contact point with localizable extension`() {
        val contactPoint = ContactPoint(
            id = "12345",
            extension = localizableExtensions,
            system = ContactPointSystem.PHONE.asCode(),
            value = "8675309",
            use = ContactPointUse.OLD.asCode(),
            rank = PositiveInt(3),
            period = Period(start = DateTime("2021"))
        )
        val localizedContactPoint = localizer.localize(contactPoint, tenant)
        assertNotEquals(contactPoint, localizedContactPoint)

        val expectedContactPoint = ContactPoint(
            id = "12345",
            extension = localizedExtensions,
            system = ContactPointSystem.PHONE.asCode(),
            value = "8675309",
            use = ContactPointUse.OLD.asCode(),
            rank = PositiveInt(3),
            period = Period(start = DateTime("2021"))
        )
        assertEquals(expectedContactPoint, localizedContactPoint)
    }

    @Test
    fun `localizes contact point with localizable period`() {
        val contactPoint = ContactPoint(
            id = "12345",
            extension = nonLocalizableExtensions,
            system = ContactPointSystem.PHONE.asCode(),
            value = "8675309",
            use = ContactPointUse.OLD.asCode(),
            rank = PositiveInt(3),
            period = Period(extension = localizableExtensions, start = DateTime("2021"))
        )
        val localizedContactPoint = localizer.localize(contactPoint, tenant)
        assertNotEquals(contactPoint, localizedContactPoint)

        val expectedContactPoint = ContactPoint(
            id = "12345",
            extension = nonLocalizableExtensions,
            system = ContactPointSystem.PHONE.asCode(),
            value = "8675309",
            use = ContactPointUse.OLD.asCode(),
            rank = PositiveInt(3),
            period = Period(extension = localizedExtensions, start = DateTime("2021"))
        )
        assertEquals(expectedContactPoint, localizedContactPoint)
    }

    @Test
    fun `localizes contact point with localizable extension and period`() {
        val contactPoint = ContactPoint(
            id = "12345",
            extension = localizableExtensions,
            system = ContactPointSystem.PHONE.asCode(),
            value = "8675309",
            use = ContactPointUse.OLD.asCode(),
            rank = PositiveInt(3),
            period = Period(extension = localizableExtensions, start = DateTime("2021"))
        )
        val localizedContactPoint = localizer.localize(contactPoint, tenant)
        assertNotEquals(contactPoint, localizedContactPoint)

        val expectedContactPoint = ContactPoint(
            id = "12345",
            extension = localizedExtensions,
            system = ContactPointSystem.PHONE.asCode(),
            value = "8675309",
            use = ContactPointUse.OLD.asCode(),
            rank = PositiveInt(3),
            period = Period(extension = localizedExtensions, start = DateTime("2021"))
        )
        assertEquals(expectedContactPoint, localizedContactPoint)
    }

    @Test
    fun `returns current dosage if it has no localizable information`() {
        val dosage = Dosage(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions
        )

        val localizedDosage = localizer.localize(dosage, tenant)
        assertSame(localizedDosage, dosage)
    }

    @Test
    fun `localizes dosage with localizable extension`() {
        val dosage = Dosage(
            id = "12345",
            extension = localizableExtensions
        )
        val localizedDosage = localizer.localize(dosage, tenant)

        val expectedDosage = Dosage(
            id = "12345",
            extension = localizedExtensions
        )
        assertEquals(expectedDosage, localizedDosage)
    }

    @Test
    fun `localizes dosage with localizable modifier extension`() {
        val dosage = Dosage(
            id = "12345",
            modifierExtension = localizableExtensions
        )
        val localizedDosage = localizer.localize(dosage, tenant)

        val expectedDosage = Dosage(
            id = "12345",
            modifierExtension = localizedExtensions
        )
        assertEquals(expectedDosage, localizedDosage)
    }

    @Test
    fun `localizes dosage with localizable additionalInstructions`() {
        val dosage = Dosage(
            id = "12345",
            additionalInstruction = listOf(
                CodeableConcept(extension = localizableExtensions)
            )
        )
        val localizedDosage = localizer.localize(dosage, tenant)

        val expectedDosage = Dosage(
            id = "12345",
            additionalInstruction = listOf(
                CodeableConcept(extension = localizedExtensions)
            )
        )
        assertEquals(expectedDosage, localizedDosage)
    }

    @Test
    fun `localizes dosage with localizable timing`() {
        val dosage = Dosage(
            id = "12345",
            timing = Timing(extension = localizableExtensions)
        )
        val localizedDosage = localizer.localize(dosage, tenant)

        val expectedDosage = Dosage(
            id = "12345",
            timing = Timing(extension = localizedExtensions)
        )
        assertEquals(expectedDosage, localizedDosage)
    }

    @Test
    fun `localizes dosage with localizable asNeeded`() {
        val dosage = Dosage(
            id = "12345",
            asNeeded = DynamicValue(
                type = DynamicValueType.CODEABLE_CONCEPT,
                value = CodeableConcept(extension = localizableExtensions)
            )
        )
        val localizedDosage = localizer.localize(dosage, tenant)

        val expectedDosage = Dosage(
            id = "12345",
            asNeeded = DynamicValue(
                type = DynamicValueType.CODEABLE_CONCEPT,
                value = CodeableConcept(extension = localizedExtensions)
            )
        )
        assertEquals(expectedDosage, localizedDosage)
    }

    @Test
    fun `handles dosage with non-CodeableConcept asNeeded`() {
        val dosage = Dosage(
            id = "12345",
            asNeeded = DynamicValue(
                type = DynamicValueType.BOOLEAN,
                value = true
            )
        )
        val localizedDosage = localizer.localize(dosage, tenant)

        val expectedDosage = Dosage(
            id = "12345",
            asNeeded = DynamicValue(
                type = DynamicValueType.BOOLEAN,
                value = true
            )
        )
        assertEquals(expectedDosage, localizedDosage)
    }

    @Test
    fun `localizes dosage with localizable site`() {
        val dosage = Dosage(
            id = "12345",
            site = CodeableConcept(extension = localizableExtensions)
        )
        val localizedDosage = localizer.localize(dosage, tenant)

        val expectedDosage = Dosage(
            id = "12345",
            site = CodeableConcept(extension = localizedExtensions)
        )
        assertEquals(expectedDosage, localizedDosage)
    }

    @Test
    fun `localizes dosage with localizable route`() {
        val dosage = Dosage(
            id = "12345",
            route = CodeableConcept(extension = localizableExtensions)
        )
        val localizedDosage = localizer.localize(dosage, tenant)

        val expectedDosage = Dosage(
            id = "12345",
            route = CodeableConcept(extension = localizedExtensions)
        )
        assertEquals(expectedDosage, localizedDosage)
    }

    @Test
    fun `localizes dosage with localizable method`() {
        val dosage = Dosage(
            id = "12345",
            method = CodeableConcept(extension = localizableExtensions)
        )
        val localizedDosage = localizer.localize(dosage, tenant)

        val expectedDosage = Dosage(
            id = "12345",
            method = CodeableConcept(extension = localizedExtensions)
        )
        assertEquals(expectedDosage, localizedDosage)
    }

    @Test
    fun `localizes dosage with localizable doseAndRate`() {
        val dosage = Dosage(
            id = "12345",
            doseAndRate = listOf(
                DoseAndRate(extension = localizableExtensions)
            )
        )
        val localizedDosage = localizer.localize(dosage, tenant)

        val expectedDosage = Dosage(
            id = "12345",
            doseAndRate = listOf(
                DoseAndRate(extension = localizedExtensions)
            )
        )
        assertEquals(expectedDosage, localizedDosage)
    }

    @Test
    fun `localizes dosage with localizable maxDosePerPeriod`() {
        val dosage = Dosage(
            id = "12345",
            maxDosePerPeriod = Ratio(extension = localizableExtensions)
        )
        val localizedDosage = localizer.localize(dosage, tenant)

        val expectedDosage = Dosage(
            id = "12345",
            maxDosePerPeriod = Ratio(extension = localizedExtensions)
        )
        assertEquals(expectedDosage, localizedDosage)
    }

    @Test
    fun `localizes dosage with localizable maxDosePerAdministration`() {
        val dosage = Dosage(
            id = "12345",
            maxDosePerAdministration = SimpleQuantity(extension = localizableExtensions)
        )
        val localizedDosage = localizer.localize(dosage, tenant)

        val expectedDosage = Dosage(
            id = "12345",
            maxDosePerAdministration = SimpleQuantity(extension = localizedExtensions)
        )
        assertEquals(expectedDosage, localizedDosage)
    }

    @Test
    fun `localizes dosage with localizable maxDosePerLifetime`() {
        val dosage = Dosage(
            id = "12345",
            maxDosePerLifetime = SimpleQuantity(extension = localizableExtensions)
        )
        val localizedDosage = localizer.localize(dosage, tenant)

        val expectedDosage = Dosage(
            id = "12345",
            maxDosePerLifetime = SimpleQuantity(extension = localizedExtensions)
        )
        assertEquals(expectedDosage, localizedDosage)
    }

    @Test
    fun `returns current doseAndRate if it has no localizable information`() {
        val doseAndRate = DoseAndRate(
            id = "12345",
            extension = nonLocalizableExtensions
        )

        val localizedDoseAndRate = localizer.localize(doseAndRate, tenant)
        assertSame(doseAndRate, localizedDoseAndRate)
    }

    @Test
    fun `localizes doseAndRate with localizable extensions`() {
        val doseAndRate = DoseAndRate(
            id = "12345",
            extension = localizableExtensions
        )
        val localizedDoseAndRate = localizer.localize(doseAndRate, tenant)

        val expectedDoseAndRate = DoseAndRate(
            id = "12345",
            extension = localizedExtensions
        )
        assertEquals(expectedDoseAndRate, localizedDoseAndRate)
    }

    @Test
    fun `localizes doseAndRate with localizable type`() {
        val doseAndRate = DoseAndRate(
            id = "12345",
            type = CodeableConcept(extension = localizableExtensions)
        )
        val localizedDoseAndRate = localizer.localize(doseAndRate, tenant)

        val expectedDoseAndRate = DoseAndRate(
            id = "12345",
            type = CodeableConcept(extension = localizedExtensions)
        )
        assertEquals(expectedDoseAndRate, localizedDoseAndRate)
    }

    @Test
    fun `localizes doseAndRate with localizable dose as range`() {
        val doseAndRate = DoseAndRate(
            id = "12345",
            dose = DynamicValue(
                type = DynamicValueType.RANGE,
                value = Range(extension = localizableExtensions)
            )
        )
        val localizedDoseAndRate = localizer.localize(doseAndRate, tenant)

        val expectedDoseAndRate = DoseAndRate(
            id = "12345",
            dose = DynamicValue(
                type = DynamicValueType.RANGE,
                value = Range(extension = localizedExtensions)
            )
        )
        assertEquals(expectedDoseAndRate, localizedDoseAndRate)
    }

    @Test
    fun `localizes doseAndRate with localizable dose as quantity`() {
        val doseAndRate = DoseAndRate(
            id = "12345",
            dose = DynamicValue(
                type = DynamicValueType.QUANTITY,
                value = Quantity(extension = localizableExtensions)
            )
        )
        val localizedDoseAndRate = localizer.localize(doseAndRate, tenant)

        val expectedDoseAndRate = DoseAndRate(
            id = "12345",
            dose = DynamicValue(
                type = DynamicValueType.QUANTITY,
                value = Quantity(extension = localizedExtensions)
            )
        )
        assertEquals(expectedDoseAndRate, localizedDoseAndRate)
    }

    @Test
    fun `handles doseAndRate with unexpected dose type`() {
        val doseAndRate = DoseAndRate(
            id = "12345",
            dose = DynamicValue(
                type = DynamicValueType.BOOLEAN,
                value = true
            )
        )
        val localizedDoseAndRate = localizer.localize(doseAndRate, tenant)

        val expectedDoseAndRate = DoseAndRate(
            id = "12345",
            dose = DynamicValue(
                type = DynamicValueType.BOOLEAN,
                value = true
            )
        )
        assertEquals(expectedDoseAndRate, localizedDoseAndRate)
    }

    @Test
    fun `localizes doseAndRate with localizable rate as ratio`() {
        val doseAndRate = DoseAndRate(
            id = "12345",
            rate = DynamicValue(
                type = DynamicValueType.RATIO,
                value = Ratio(extension = localizableExtensions)
            )
        )
        val localizedDoseAndRate = localizer.localize(doseAndRate, tenant)

        val expectedDoseAndRate = DoseAndRate(
            id = "12345",
            rate = DynamicValue(
                type = DynamicValueType.RATIO,
                value = Ratio(extension = localizedExtensions)
            )
        )
        assertEquals(expectedDoseAndRate, localizedDoseAndRate)
    }

    @Test
    fun `localizes doseAndRate with localizable rate as range`() {
        val doseAndRate = DoseAndRate(
            id = "12345",
            rate = DynamicValue(
                type = DynamicValueType.RANGE,
                value = Range(extension = localizableExtensions)
            )
        )
        val localizedDoseAndRate = localizer.localize(doseAndRate, tenant)

        val expectedDoseAndRate = DoseAndRate(
            id = "12345",
            rate = DynamicValue(
                type = DynamicValueType.RANGE,
                value = Range(extension = localizedExtensions)
            )
        )
        assertEquals(expectedDoseAndRate, localizedDoseAndRate)
    }

    @Test
    fun `localizes doseAndRate with localizable rate as quantity`() {
        val doseAndRate = DoseAndRate(
            id = "12345",
            rate = DynamicValue(
                type = DynamicValueType.QUANTITY,
                value = Quantity(extension = localizableExtensions)
            )
        )
        val localizedDoseAndRate = localizer.localize(doseAndRate, tenant)

        val expectedDoseAndRate = DoseAndRate(
            id = "12345",
            rate = DynamicValue(
                type = DynamicValueType.QUANTITY,
                value = Quantity(extension = localizedExtensions)
            )
        )
        assertEquals(expectedDoseAndRate, localizedDoseAndRate)
    }

    @Test
    fun `localizes doseAndRate with unexpected rate type`() {
        val doseAndRate = DoseAndRate(
            id = "12345",
            rate = DynamicValue(
                type = DynamicValueType.BOOLEAN,
                value = true
            )
        )
        val localizedDoseAndRate = localizer.localize(doseAndRate, tenant)

        val expectedDoseAndRate = DoseAndRate(
            id = "12345",
            rate = DynamicValue(
                type = DynamicValueType.BOOLEAN,
                value = true
            )
        )
        assertEquals(expectedDoseAndRate, localizedDoseAndRate)
    }

    @Test
    fun `returns current duration if it has no localizable information`() {
        val duration = Duration(
            id = "12345",
            extension = nonLocalizableExtensions
        )

        val localizedDuration = localizer.localize(duration, tenant)
        assertSame(duration, localizedDuration)
    }

    @Test
    fun `localizes duration with localizable extensions`() {
        val duration = Duration(
            id = "12345",
            extension = localizableExtensions
        )
        val localizedDuration = localizer.localize(duration, tenant)

        val expectedDuration = Duration(
            id = "12345",
            extension = localizedExtensions
        )
        assertEquals(expectedDuration, localizedDuration)
    }

    @Test
    fun `returns current range if it has no localizable information`() {
        val range = Range(
            id = "12345",
            extension = nonLocalizableExtensions
        )

        val localizedRange = localizer.localize(range, tenant)
        assertSame(range, localizedRange)
    }

    @Test
    fun `localizes range if it has localizable extensions`() {
        val range = Range(
            id = "12345",
            extension = localizableExtensions
        )
        val localizedRange = localizer.localize(range, tenant)

        val expectedRange = Range(
            id = "12345",
            extension = localizedExtensions
        )
        assertEquals(expectedRange, localizedRange)
    }

    @Test
    fun `returns current quantity if it has no localizable information`() {
        val quantity = Quantity(
            id = "12345",
            extension = nonLocalizableExtensions
        )

        val localizedQuantity = localizer.localize(quantity, tenant)
        assertSame(quantity, localizedQuantity)
    }

    @Test
    fun `localizes quantity if it has localizable extensions`() {
        val quantity = Quantity(
            id = "12345",
            extension = localizableExtensions
        )
        val localizedQuantity = localizer.localize(quantity, tenant)

        val expectedQuantity = Quantity(
            id = "12345",
            extension = localizedExtensions
        )
        assertEquals(expectedQuantity, localizedQuantity)
    }

    @Test
    fun `returns current SimpleQuantity if it has no localizable information`() {
        val simpleQuantity = SimpleQuantity(
            id = "12345",
            extension = nonLocalizableExtensions
        )

        val localizedSimpleQuantity = localizer.localize(simpleQuantity, tenant)
        assertSame(simpleQuantity, localizedSimpleQuantity)
    }

    @Test
    fun `localizes SimpleQuantity if it has localizable extensions`() {
        val simpleQuantity = SimpleQuantity(
            id = "12345",
            extension = localizableExtensions
        )
        val localizedSimpleQuantity = localizer.localize(simpleQuantity, tenant)

        val expectedSimpleQuantity = SimpleQuantity(
            id = "12345",
            extension = localizedExtensions
        )
        assertEquals(expectedSimpleQuantity, localizedSimpleQuantity)
    }

    @Test
    fun `returns current Ratio if it has no localizable information`() {
        val ratio = Ratio(
            id = "12345",
            extension = nonLocalizableExtensions
        )

        val localizedRatio = localizer.localize(ratio, tenant)
        assertSame(ratio, localizedRatio)
    }

    @Test
    fun `localizes Ratio if it has localizable extensions`() {
        val ratio = Ratio(
            id = "12345",
            extension = localizableExtensions
        )
        val localizedRatio = localizer.localize(ratio, tenant)

        val expectedRatio = Ratio(
            id = "12345",
            extension = localizedExtensions
        )
        assertEquals(expectedRatio, localizedRatio)
    }

    @Test
    fun `localizes Ratio if it has a localizable numerator`() {
        val ratio = Ratio(
            id = "12345",
            numerator = Quantity(extension = localizableExtensions)
        )
        val localizedRatio = localizer.localize(ratio, tenant)

        val expectedRatio = Ratio(
            id = "12345",
            numerator = Quantity(extension = localizedExtensions)
        )
        assertEquals(expectedRatio, localizedRatio)
    }

    @Test
    fun `localizes Ratio if it has a localizable denominator`() {
        val ratio = Ratio(
            id = "12345",
            denominator = Quantity(extension = localizableExtensions)
        )
        val localizedRatio = localizer.localize(ratio, tenant)

        val expectedRatio = Ratio(
            id = "12345",
            denominator = Quantity(extension = localizedExtensions)
        )
        assertEquals(expectedRatio, localizedRatio)
    }

    @Test
    fun `returns current Timing if it has no localizable information`() {
        val timing = Timing(
            id = "12345",
            extension = nonLocalizableExtensions
        )

        val localizedTiming = localizer.localize(timing, tenant)
        assertSame(timing, localizedTiming)
    }

    @Test
    fun `localizes Timing if it has localizable extensions`() {
        val timing = Timing(
            id = "12345",
            extension = localizableExtensions
        )
        val localizedTiming = localizer.localize(timing, tenant)

        val expectedTiming = Timing(
            id = "12345",
            extension = localizedExtensions
        )
        assertEquals(expectedTiming, localizedTiming)
    }

    @Test
    fun `localizes Timing if it has localizable repeat`() {
        val timing = Timing(
            id = "12345",
            repeat = TimingRepeat(extension = localizableExtensions)
        )
        val localizedTiming = localizer.localize(timing, tenant)

        val expectedTiming = Timing(
            id = "12345",
            repeat = TimingRepeat(extension = localizedExtensions)
        )
        assertEquals(expectedTiming, localizedTiming)
    }

    @Test
    fun `returns current TimingRepeat if it has no localizable information`() {
        val timingRepeat = TimingRepeat(
            id = "12345",
            extension = nonLocalizableExtensions
        )

        val localizedTimingRepeat = localizer.localize(timingRepeat, tenant)
        assertSame(timingRepeat, localizedTimingRepeat)
    }

    @Test
    fun `localizes TimingRepeat if it has localizable extensions`() {
        val timingRepeat = TimingRepeat(
            id = "12345",
            extension = localizableExtensions
        )
        val localizedTimingRepeat = localizer.localize(timingRepeat, tenant)

        val expectedTimingRepeat = TimingRepeat(
            id = "12345",
            extension = localizedExtensions
        )
        assertEquals(expectedTimingRepeat, localizedTimingRepeat)
    }

    @Test
    fun `localizes TimingRepeat if it has a localizable bounds as a duration`() {
        val timingRepeat = TimingRepeat(
            id = "12345",
            bounds = DynamicValue(
                type = DynamicValueType.DURATION,
                value = Duration(extension = localizableExtensions)
            )
        )
        val localizedTimingRepeat = localizer.localize(timingRepeat, tenant)

        val expectedTimingRepeat = TimingRepeat(
            id = "12345",
            bounds = DynamicValue(
                type = DynamicValueType.DURATION,
                value = Duration(extension = localizedExtensions)
            )
        )
        assertEquals(expectedTimingRepeat, localizedTimingRepeat)
    }

    @Test
    fun `localizes TimingRepeat if it has a localizable bounds as a range`() {
        val timingRepeat = TimingRepeat(
            id = "12345",
            bounds = DynamicValue(
                type = DynamicValueType.RANGE,
                value = Range(extension = localizableExtensions)
            )
        )
        val localizedTimingRepeat = localizer.localize(timingRepeat, tenant)

        val expectedTimingRepeat = TimingRepeat(
            id = "12345",
            bounds = DynamicValue(
                type = DynamicValueType.RANGE,
                value = Range(extension = localizedExtensions)
            )
        )
        assertEquals(expectedTimingRepeat, localizedTimingRepeat)
    }

    @Test
    fun `localizes TimingRepeat if it has a localizable bounds as a period`() {
        val timingRepeat = TimingRepeat(
            id = "12345",
            bounds = DynamicValue(
                type = DynamicValueType.PERIOD,
                value = Period(extension = localizableExtensions)
            )
        )
        val localizedTimingRepeat = localizer.localize(timingRepeat, tenant)

        val expectedTimingRepeat = TimingRepeat(
            id = "12345",
            bounds = DynamicValue(
                type = DynamicValueType.PERIOD,
                value = Period(extension = localizedExtensions)
            )
        )
        assertEquals(expectedTimingRepeat, localizedTimingRepeat)
    }

    @Test
    fun `handles TimingRepeat if bounds has an unexpected dynamic value type`() {
        val timingRepeat = TimingRepeat(
            id = "12345",
            bounds = DynamicValue(
                type = DynamicValueType.BOOLEAN,
                value = true
            )
        )
        val localizedTimingRepeat = localizer.localize(timingRepeat, tenant)

        val expectedTimingRepeat = TimingRepeat(
            id = "12345",
            bounds = DynamicValue(
                type = DynamicValueType.BOOLEAN,
                value = true
            )
        )
        assertEquals(expectedTimingRepeat, localizedTimingRepeat)
    }

    @Test
    fun `does not localize an extension with non-localizable extension set`() {
        val extension = Extension(
            id = "12345",
            extension = nonLocalizableExtensions,
            url = Uri("url")
        )
        val localizedExtension = localizer.localize(extension, tenant)
        assertTrue(extension == localizedExtension)
    }

    @Test
    fun `does not localize an extension with non-localizable value`() {
        val extension = Extension(
            id = "12345",
            url = Uri("url"),
            value = DynamicValue(DynamicValueType.STRING, "Value")
        )
        val localizedExtension = localizer.localize(extension, tenant)
        assertTrue(extension == localizedExtension)
    }

    @Test
    fun `localizes extension with all localizable extension set`() {
        val extension = Extension(
            id = "12345",
            extension = localizableExtensions,
            url = Uri("url")
        )
        val localizedExtension = localizer.localize(extension, tenant)
        assertNotEquals(extension, localizedExtension)

        val expectedExtension = Extension(
            id = "12345",
            extension = localizedExtensions,
            url = Uri("url")
        )
        assertEquals(expectedExtension, localizedExtension)
    }

    @Test
    fun `localizes only the extension set members with localizable values in a mixed extension set`() {
        val extension = Extension(
            id = "12345",
            extension = someNonLocalizableExtensions,
            url = Uri("url")
        )
        val localizedExtension = localizer.localize(extension, tenant)
        assertNotEquals(extension, localizedExtension)

        val expectedExtension = Extension(
            id = "12345",
            extension = mixedLocalizedExtensions,
            url = Uri("url")
        )
        assertEquals(expectedExtension, localizedExtension)
    }

    @Test
    fun `localizes an extension with localizable value`() {
        val extension = Extension(
            id = "12345",
            url = Uri("url"),
            value = DynamicValue(DynamicValueType.REFERENCE, Reference(reference = "Patient/123"))
        )
        val localizedExtension = localizer.localize(extension, tenant)
        assertNotEquals(extension, localizedExtension)

        val expectedExtension = Extension(
            id = "12345",
            url = Uri("url"),
            value = DynamicValue(DynamicValueType.REFERENCE, Reference(reference = "Patient/test-123"))
        )
        assertEquals(expectedExtension, localizedExtension)
    }

    @Test
    fun `returns current human name if human name has no localizable information`() {
        val humanName = HumanName(
            id = "12345",
            extension = nonLocalizableExtensions,
            use = NameUse.OFFICIAL.asCode(),
            text = "Text",
            family = "Family",
            given = listOf("Given"),
            prefix = listOf("Prefix"),
            suffix = listOf("Suffix"),
            period = Period(start = DateTime("2021"))
        )
        val localizedHumanName = localizer.localize(humanName, tenant)
        assertTrue(humanName === localizedHumanName)
    }

    @Test
    fun `localizes human name with localizable extension`() {
        val humanName = HumanName(
            id = "12345",
            extension = localizableExtensions,
            use = NameUse.OFFICIAL.asCode(),
            text = "Text",
            family = "Family",
            given = listOf("Given"),
            prefix = listOf("Prefix"),
            suffix = listOf("Suffix"),
            period = Period(start = DateTime("2021"))
        )
        val localizedHumanName = localizer.localize(humanName, tenant)
        assertNotEquals(humanName, localizedHumanName)

        val expectedHumanName = HumanName(
            id = "12345",
            extension = localizedExtensions,
            use = NameUse.OFFICIAL.asCode(),
            text = "Text",
            family = "Family",
            given = listOf("Given"),
            prefix = listOf("Prefix"),
            suffix = listOf("Suffix"),
            period = Period(start = DateTime("2021"))
        )
        assertEquals(expectedHumanName, localizedHumanName)
    }

    @Test
    fun `localizes human name with localizable period`() {
        val humanName = HumanName(
            id = "12345",
            extension = nonLocalizableExtensions,
            use = NameUse.OFFICIAL.asCode(),
            text = "Text",
            family = "Family",
            given = listOf("Given"),
            prefix = listOf("Prefix"),
            suffix = listOf("Suffix"),
            period = Period(extension = localizableExtensions, start = DateTime("2021"))
        )
        val localizedHumanName = localizer.localize(humanName, tenant)
        assertNotEquals(humanName, localizedHumanName)

        val expectedHumanName = HumanName(
            id = "12345",
            extension = nonLocalizableExtensions,
            use = NameUse.OFFICIAL.asCode(),
            text = "Text",
            family = "Family",
            given = listOf("Given"),
            prefix = listOf("Prefix"),
            suffix = listOf("Suffix"),
            period = Period(extension = localizedExtensions, start = DateTime("2021"))
        )
        assertEquals(expectedHumanName, localizedHumanName)
    }

    @Test
    fun `localizes human name with localizable extension and period`() {
        val humanName = HumanName(
            id = "12345",
            extension = localizableExtensions,
            use = NameUse.OFFICIAL.asCode(),
            text = "Text",
            family = "Family",
            given = listOf("Given"),
            prefix = listOf("Prefix"),
            suffix = listOf("Suffix"),
            period = Period(extension = localizableExtensions, start = DateTime("2021"))
        )
        val localizedHumanName = localizer.localize(humanName, tenant)
        assertNotEquals(humanName, localizedHumanName)

        val expectedHumanName = HumanName(
            id = "12345",
            extension = localizedExtensions,
            use = NameUse.OFFICIAL.asCode(),
            text = "Text",
            family = "Family",
            given = listOf("Given"),
            prefix = listOf("Prefix"),
            suffix = listOf("Suffix"),
            period = Period(extension = localizedExtensions, start = DateTime("2021"))
        )
        assertEquals(expectedHumanName, localizedHumanName)
    }

    @Test
    fun `returns current identifier if identifier has no localizable information`() {
        val identifier = Identifier(
            id = "12345",
            extension = nonLocalizableExtensions,
            use = IdentifierUse.OFFICIAL.asCode(),
            type = CodeableConcept(text = "type"),
            system = Uri("system"),
            value = "value",
            period = Period(start = DateTime("2021")),
            assigner = Reference(display = "assigner")
        )
        val localizedIdentifier = localizer.localize(identifier, tenant)
        assertTrue(localizedIdentifier === identifier)
    }

    @Test
    fun `localizes identifier with localizable period`() {
        val identifier = Identifier(
            id = "12345",
            extension = nonLocalizableExtensions,
            use = IdentifierUse.OFFICIAL.asCode(),
            type = CodeableConcept(text = "type"),
            system = Uri("system"),
            value = "value",
            period = Period(extension = localizableExtensions, start = DateTime("2021")),
            assigner = Reference(display = "assigner")
        )
        val localizedIdentifier = localizer.localize(identifier, tenant)
        assertNotEquals(identifier, localizedIdentifier)

        val expectedIdentifier = Identifier(
            id = "12345",
            extension = nonLocalizableExtensions,
            use = IdentifierUse.OFFICIAL.asCode(),
            type = CodeableConcept(text = "type"),
            system = Uri("system"),
            value = "value",
            period = Period(extension = localizedExtensions, start = DateTime("2021")),
            assigner = Reference(display = "assigner")
        )
        assertEquals(expectedIdentifier, localizedIdentifier)
    }

    @Test
    fun `returns current link if link has no localizable information`() {
        val link = PatientLink(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            other = Reference(extension = nonLocalizableExtensions),
            type = LinkType.REPLACES.asCode()
        )
        val localizedLink = localizer.localize(link, tenant)
        assertTrue(localizedLink === link)
    }

    @Test
    fun `localizes link with localizable extension`() {
        val link = PatientLink(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            other = Reference(extension = nonLocalizableExtensions),
            type = LinkType.REPLACES.asCode()
        )
        val localizedLink = localizer.localize(link, tenant)
        assertNotEquals(localizedLink, link)

        val expectedLink = PatientLink(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = nonLocalizableExtensions,
            other = Reference(extension = nonLocalizableExtensions),
            type = LinkType.REPLACES.asCode()
        )
        assertEquals(expectedLink, localizedLink)
    }

    @Test
    fun `localizes link with localizable modifierExtension`() {
        val link = PatientLink(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizableExtensions,
            other = Reference(extension = nonLocalizableExtensions),
            type = LinkType.REPLACES.asCode()
        )
        val localizedLink = localizer.localize(link, tenant)
        assertNotEquals(localizedLink, link)

        val expectedLink = PatientLink(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizedExtensions,
            other = Reference(extension = nonLocalizableExtensions),
            type = LinkType.REPLACES.asCode()
        )
        assertEquals(expectedLink, localizedLink)
    }

    @Test
    fun `localizes link with localizable other`() {
        val link = PatientLink(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            other = Reference(extension = localizableExtensions),
            type = LinkType.REPLACES.asCode()
        )
        val localizedLink = localizer.localize(link, tenant)
        assertNotEquals(localizedLink, link)

        val expectedLink = PatientLink(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            other = Reference(extension = localizedExtensions),
            type = LinkType.REPLACES.asCode()
        )
        assertEquals(expectedLink, localizedLink)
    }

    @Test
    fun `localizes link with all localizable values`() {
        val link = PatientLink(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = localizableExtensions,
            other = Reference(extension = localizableExtensions),
            type = LinkType.REPLACES.asCode()
        )
        val localizedLink = localizer.localize(link, tenant)
        assertNotEquals(localizedLink, link)

        val expectedLink = PatientLink(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = localizedExtensions,
            other = Reference(extension = localizedExtensions),
            type = LinkType.REPLACES.asCode()
        )
        assertEquals(expectedLink, localizedLink)
    }

    @Test
    fun `returns current meta if meta has no localizable information`() {
        val meta = Meta(
            id = "12345",
            extension = nonLocalizableExtensions,
            versionId = Id("versionId"),
            lastUpdated = Instant("2015-02-07T13:28:17.239+02:00"),
            source = Uri("source"),
            profile = listOf(Canonical("profile")),
            security = listOf(Coding(display = "security")),
            tag = listOf(Coding(display = "tag"))
        )
        val localizedMeta = localizer.localize(meta, tenant)
        assertTrue(meta === localizedMeta)
    }

    @Test
    fun `localizes meta with localizable extension`() {
        val meta = Meta(
            id = "12345",
            extension = localizableExtensions,
            versionId = Id("versionId"),
            lastUpdated = Instant("2015-02-07T13:28:17.239+02:00"),
            source = Uri("source"),
            profile = listOf(Canonical("profile")),
            security = listOf(Coding(display = "security")),
            tag = listOf(Coding(display = "tag"))
        )
        val localizedMeta = localizer.localize(meta, tenant)
        assertNotEquals(meta, localizedMeta)

        val expectedMeta = Meta(
            id = "12345",
            extension = localizedExtensions,
            versionId = Id("versionId"),
            lastUpdated = Instant("2015-02-07T13:28:17.239+02:00"),
            source = Uri("source"),
            profile = listOf(Canonical("profile")),
            security = listOf(Coding(display = "security")),
            tag = listOf(Coding(display = "tag"))
        )
        assertEquals(expectedMeta, localizedMeta)
    }

    @Test
    fun `localizes meta with localizable security`() {
        val meta = Meta(
            id = "12345",
            extension = nonLocalizableExtensions,
            versionId = Id("versionId"),
            lastUpdated = Instant("2015-02-07T13:28:17.239+02:00"),
            source = Uri("source"),
            profile = listOf(Canonical("profile")),
            security = listOf(Coding(extension = localizableExtensions, display = "security")),
            tag = listOf(Coding(display = "tag"))
        )
        val localizedMeta = localizer.localize(meta, tenant)
        assertNotEquals(meta, localizedMeta)

        val expectedMeta = Meta(
            id = "12345",
            extension = nonLocalizableExtensions,
            versionId = Id("versionId"),
            lastUpdated = Instant("2015-02-07T13:28:17.239+02:00"),
            source = Uri("source"),
            profile = listOf(Canonical("profile")),
            security = listOf(Coding(extension = localizedExtensions, display = "security")),
            tag = listOf(Coding(display = "tag"))
        )
        assertEquals(expectedMeta, localizedMeta)
    }

    @Test
    fun `localizes meta with localizable tag`() {
        val meta = Meta(
            id = "12345",
            extension = nonLocalizableExtensions,
            versionId = Id("versionId"),
            lastUpdated = Instant("2015-02-07T13:28:17.239+02:00"),
            source = Uri("source"),
            profile = listOf(Canonical("profile")),
            security = listOf(Coding(display = "security")),
            tag = listOf(Coding(extension = localizableExtensions, display = "tag"))
        )
        val localizedMeta = localizer.localize(meta, tenant)
        assertNotEquals(meta, localizedMeta)

        val expectedMeta = Meta(
            id = "12345",
            extension = nonLocalizableExtensions,
            versionId = Id("versionId"),
            lastUpdated = Instant("2015-02-07T13:28:17.239+02:00"),
            source = Uri("source"),
            profile = listOf(Canonical("profile")),
            security = listOf(Coding(display = "security")),
            tag = listOf(Coding(extension = localizedExtensions, display = "tag"))
        )
        assertEquals(expectedMeta, localizedMeta)
    }

    @Test
    fun `localizes meta with localizable extension, security and tag`() {
        val meta = Meta(
            id = "12345",
            extension = localizableExtensions,
            versionId = Id("versionId"),
            lastUpdated = Instant("2015-02-07T13:28:17.239+02:00"),
            source = Uri("source"),
            profile = listOf(Canonical("profile")),
            security = listOf(Coding(extension = localizableExtensions, display = "security")),
            tag = listOf(Coding(extension = localizableExtensions, display = "tag"))
        )
        val localizedMeta = localizer.localize(meta, tenant)
        assertNotEquals(meta, localizedMeta)

        val expectedMeta = Meta(
            id = "12345",
            extension = localizedExtensions,
            versionId = Id("versionId"),
            lastUpdated = Instant("2015-02-07T13:28:17.239+02:00"),
            source = Uri("source"),
            profile = listOf(Canonical("profile")),
            security = listOf(Coding(extension = localizedExtensions, display = "security")),
            tag = listOf(Coding(extension = localizedExtensions, display = "tag"))
        )
        assertEquals(expectedMeta, localizedMeta)
    }

    @Test
    fun `returns current narrative if narrative has no localizable information`() {
        val narrative = Narrative(
            id = "12345",
            extension = nonLocalizableExtensions,
            status = NarrativeStatus.GENERATED.asCode(),
            div = "div"
        )
        val localizedNarrative = localizer.localize(narrative, tenant)
        assertTrue(narrative === localizedNarrative)
    }

    @Test
    fun `localizes narrative with localizable extension`() {
        val narrative = Narrative(
            id = "12345",
            extension = localizableExtensions,
            status = NarrativeStatus.GENERATED.asCode(),
            div = "div"
        )
        val localizedNarrative = localizer.localize(narrative, tenant)
        assertNotEquals(narrative, localizedNarrative)

        val expectedNarrative = Narrative(
            id = "12345",
            extension = localizedExtensions,
            status = NarrativeStatus.GENERATED.asCode(),
            div = "div"
        )
        assertEquals(expectedNarrative, localizedNarrative)
    }

    @Test
    fun `returns current not available if not available has no localizable information`() {
        val notAvailable = NotAvailable(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            description = "description",
            during = Period(start = DateTime("2021"))
        )
        val localizedNotAvailable = localizer.localize(notAvailable, tenant)
        assertTrue(notAvailable === localizedNotAvailable)
    }

    @Test
    fun `localizes not available with localizable extension`() {
        val notAvailable = NotAvailable(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            description = "description",
            during = Period(start = DateTime("2021"))
        )
        val localizedNotAvailable = localizer.localize(notAvailable, tenant)
        assertNotEquals(notAvailable, localizedNotAvailable)

        val expectedNotAvailable = NotAvailable(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = nonLocalizableExtensions,
            description = "description",
            during = Period(start = DateTime("2021"))
        )
        assertEquals(expectedNotAvailable, localizedNotAvailable)
    }

    @Test
    fun `localizes not available with localizable modifier extension`() {
        val notAvailable = NotAvailable(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizableExtensions,
            description = "description",
            during = Period(start = DateTime("2021"))
        )
        val localizedNotAvailable = localizer.localize(notAvailable, tenant)
        assertNotEquals(notAvailable, localizedNotAvailable)

        val expectedNotAvailable = NotAvailable(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizedExtensions,
            description = "description",
            during = Period(start = DateTime("2021"))
        )
        assertEquals(expectedNotAvailable, localizedNotAvailable)
    }

    @Test
    fun `localizes not available with localizable during`() {
        val notAvailable = NotAvailable(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            description = "description",
            during = Period(extension = localizableExtensions, start = DateTime("2021"))
        )
        val localizedNotAvailable = localizer.localize(notAvailable, tenant)
        assertNotEquals(notAvailable, localizedNotAvailable)

        val expectedNotAvailable = NotAvailable(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            description = "description",
            during = Period(extension = localizedExtensions, start = DateTime("2021"))
        )
        assertEquals(expectedNotAvailable, localizedNotAvailable)
    }

    @Test
    fun `localizes not available with localizable extension, modifier extension and during`() {
        val notAvailable = NotAvailable(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = localizableExtensions,
            description = "description",
            during = Period(extension = localizableExtensions, start = DateTime("2021"))
        )
        val localizedNotAvailable = localizer.localize(notAvailable, tenant)
        assertNotEquals(notAvailable, localizedNotAvailable)

        val expectedNotAvailable = NotAvailable(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = localizedExtensions,
            description = "description",
            during = Period(extension = localizedExtensions, start = DateTime("2021"))
        )
        assertEquals(expectedNotAvailable, localizedNotAvailable)
    }

    @Test
    fun `returns current participant if participant has no localizable information`() {
        val participant = Participant(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            type = listOf(CodeableConcept(extension = nonLocalizableExtensions)),
            actor = Reference(extension = nonLocalizableExtensions),
            required = ParticipantRequired.REQUIRED.asCode(),
            status = ParticipationStatus.ACCEPTED.asCode(),
            period = Period(extension = nonLocalizableExtensions)
        )
        val localizedParticipant = localizer.localize(participant, tenant)
        assertTrue(localizedParticipant === participant)
    }

    @Test
    fun `returns current participant if participant has no localizable information and no actor`() {
        val participant = Participant(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            type = listOf(CodeableConcept(extension = nonLocalizableExtensions)),
            required = ParticipantRequired.REQUIRED.asCode(),
            status = ParticipationStatus.ACCEPTED.asCode(),
            period = Period(extension = nonLocalizableExtensions)
        )
        val localizedParticipant = localizer.localize(participant, tenant)
        assertTrue(localizedParticipant === participant)
    }

    @Test
    fun `localizes pariticipant with localizable extension`() {
        val participant = Participant(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            type = listOf(CodeableConcept(extension = nonLocalizableExtensions)),
            actor = Reference(extension = nonLocalizableExtensions),
            required = ParticipantRequired.REQUIRED.asCode(),
            status = ParticipationStatus.ACCEPTED.asCode(),
            period = Period(extension = nonLocalizableExtensions)
        )
        val localizedParticipant = localizer.localize(participant, tenant)
        assertNotEquals(localizedParticipant, participant)

        val expectedParticipant = Participant(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = nonLocalizableExtensions,
            type = listOf(CodeableConcept(extension = nonLocalizableExtensions)),
            actor = Reference(extension = nonLocalizableExtensions),
            required = ParticipantRequired.REQUIRED.asCode(),
            status = ParticipationStatus.ACCEPTED.asCode(),
            period = Period(extension = nonLocalizableExtensions)
        )
        assertEquals(expectedParticipant, localizedParticipant)
    }

    @Test
    fun `localizes pariticipant with localizable extension and no actor`() {
        val participant = Participant(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            type = listOf(CodeableConcept(extension = nonLocalizableExtensions)),
            required = ParticipantRequired.REQUIRED.asCode(),
            status = ParticipationStatus.ACCEPTED.asCode(),
            period = Period(extension = nonLocalizableExtensions)
        )
        val localizedParticipant = localizer.localize(participant, tenant)
        assertNotEquals(localizedParticipant, participant)

        val expectedParticipant = Participant(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = nonLocalizableExtensions,
            type = listOf(CodeableConcept(extension = nonLocalizableExtensions)),
            required = ParticipantRequired.REQUIRED.asCode(),
            status = ParticipationStatus.ACCEPTED.asCode(),
            period = Period(extension = nonLocalizableExtensions)
        )
        assertEquals(expectedParticipant, localizedParticipant)
    }

    @Test
    fun `localizes pariticipant with localizable modifierExtension`() {
        val participant = Participant(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizableExtensions,
            type = listOf(CodeableConcept(extension = nonLocalizableExtensions)),
            actor = Reference(extension = nonLocalizableExtensions),
            required = ParticipantRequired.REQUIRED.asCode(),
            status = ParticipationStatus.ACCEPTED.asCode(),
            period = Period(extension = nonLocalizableExtensions)
        )
        val localizedParticipant = localizer.localize(participant, tenant)
        assertNotEquals(localizedParticipant, participant)

        val expectedParticipant = Participant(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizedExtensions,
            type = listOf(CodeableConcept(extension = nonLocalizableExtensions)),
            actor = Reference(extension = nonLocalizableExtensions),
            required = ParticipantRequired.REQUIRED.asCode(),
            status = ParticipationStatus.ACCEPTED.asCode(),
            period = Period(extension = nonLocalizableExtensions)
        )
        assertEquals(expectedParticipant, localizedParticipant)
    }

    @Test
    fun `localizes pariticipant with localizable type`() {
        val participant = Participant(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            type = listOf(CodeableConcept(extension = localizableExtensions)),
            actor = Reference(extension = nonLocalizableExtensions),
            required = ParticipantRequired.REQUIRED.asCode(),
            status = ParticipationStatus.ACCEPTED.asCode(),
            period = Period(extension = nonLocalizableExtensions)
        )
        val localizedParticipant = localizer.localize(participant, tenant)
        assertNotEquals(localizedParticipant, participant)

        val expectedParticipant = Participant(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            type = listOf(CodeableConcept(extension = localizedExtensions)),
            actor = Reference(extension = nonLocalizableExtensions),
            required = ParticipantRequired.REQUIRED.asCode(),
            status = ParticipationStatus.ACCEPTED.asCode(),
            period = Period(extension = nonLocalizableExtensions)
        )
        assertEquals(expectedParticipant, localizedParticipant)
    }

    @Test
    fun `localizes pariticipant with localizable actor`() {
        val participant = Participant(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            type = listOf(CodeableConcept(extension = nonLocalizableExtensions)),
            actor = Reference(extension = localizableExtensions),
            required = ParticipantRequired.REQUIRED.asCode(),
            status = ParticipationStatus.ACCEPTED.asCode(),
            period = Period(extension = nonLocalizableExtensions)
        )
        val localizedParticipant = localizer.localize(participant, tenant)
        assertNotEquals(localizedParticipant, participant)

        val expectedParticipant = Participant(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            type = listOf(CodeableConcept(extension = nonLocalizableExtensions)),
            actor = Reference(extension = localizedExtensions),
            required = ParticipantRequired.REQUIRED.asCode(),
            status = ParticipationStatus.ACCEPTED.asCode(),
            period = Period(extension = nonLocalizableExtensions)
        )
        assertEquals(expectedParticipant, localizedParticipant)
    }

    @Test
    fun `localizes pariticipant with localizable period`() {
        val participant = Participant(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            type = listOf(CodeableConcept(extension = nonLocalizableExtensions)),
            actor = Reference(extension = nonLocalizableExtensions),
            required = ParticipantRequired.REQUIRED.asCode(),
            status = ParticipationStatus.ACCEPTED.asCode(),
            period = Period(extension = localizableExtensions)
        )
        val localizedParticipant = localizer.localize(participant, tenant)
        assertNotEquals(localizedParticipant, participant)

        val expectedParticipant = Participant(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            type = listOf(CodeableConcept(extension = nonLocalizableExtensions)),
            actor = Reference(extension = nonLocalizableExtensions),
            required = ParticipantRequired.REQUIRED.asCode(),
            status = ParticipationStatus.ACCEPTED.asCode(),
            period = Period(extension = localizedExtensions)
        )
        assertEquals(expectedParticipant, localizedParticipant)
    }

    @Test
    fun `returns current period if period has no localizable information`() {
        val period = Period(
            id = "12345",
            extension = nonLocalizableExtensions,
            start = DateTime("2020"),
            end = DateTime("2023")
        )
        val localizedPeriod = localizer.localize(period, tenant)
        assertTrue(period === localizedPeriod)
    }

    @Test
    fun `localizes period with localizable extension`() {
        val period = Period(
            id = "12345",
            extension = localizableExtensions,
            start = DateTime("2020"),
            end = DateTime("2023")
        )
        val localizedPeriod = localizer.localize(period, tenant)
        assertNotEquals(period, localizedPeriod)

        val expectedPeriod = Period(
            id = "12345",
            extension = localizedExtensions,
            start = DateTime("2020"),
            end = DateTime("2023")
        )
        assertEquals(expectedPeriod, localizedPeriod)
    }

    @Test
    fun `returns current primitive data as not updated if primitive data has no localizable information`() {
        val primitiveData = PrimitiveData(
            id = "12345",
            extension = nonLocalizableExtensions
        )
        val localizedPrimitiveData = localizer.localize(primitiveData, tenant)
        assertTrue(primitiveData === localizedPrimitiveData)
    }

    @Test
    fun `localizes primitive data and sets as updated with localizable extension`() {
        val primitiveData = PrimitiveData(
            id = "12345",
            extension = localizableExtensions
        )
        val localizedPrimitiveData = localizer.localize(primitiveData, tenant)
        assertNotEquals(primitiveData, localizedPrimitiveData)

        val expectedPrimitiveData = PrimitiveData(
            id = "12345",
            extension = localizedExtensions
        )
        assertEquals(expectedPrimitiveData, localizedPrimitiveData)
    }

    @Test
    fun `returns current qualification if qualification has no localizable information`() {
        val qualification = Qualification(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            identifier = listOf(Identifier(value = "id")),
            code = CodeableConcept(text = "code"),
            period = Period(start = DateTime("2021")),
            issuer = Reference(display = "issuer")
        )
        val localizedQualification = localizer.localize(qualification, tenant)
        assertTrue(qualification === localizedQualification)
    }

    @Test
    fun `localizes qualification with localizable extension`() {
        val qualification = Qualification(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            identifier = listOf(Identifier(value = "id")),
            code = CodeableConcept(text = "code"),
            period = Period(start = DateTime("2021")),
            issuer = Reference(display = "issuer")
        )
        val localizedQualification = localizer.localize(qualification, tenant)
        assertNotEquals(qualification, localizedQualification)

        val expectedQualification = Qualification(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = nonLocalizableExtensions,
            identifier = listOf(Identifier(value = "id")),
            code = CodeableConcept(text = "code"),
            period = Period(start = DateTime("2021")),
            issuer = Reference(display = "issuer")
        )
        assertEquals(expectedQualification, localizedQualification)
    }

    @Test
    fun `localizes qualification with localizable modifier extension`() {
        val qualification = Qualification(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizableExtensions,
            identifier = listOf(Identifier(value = "id")),
            code = CodeableConcept(text = "code"),
            period = Period(start = DateTime("2021")),
            issuer = Reference(display = "issuer")
        )
        val localizedQualification = localizer.localize(qualification, tenant)
        assertNotEquals(qualification, localizedQualification)

        val expectedQualification = Qualification(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizedExtensions,
            identifier = listOf(Identifier(value = "id")),
            code = CodeableConcept(text = "code"),
            period = Period(start = DateTime("2021")),
            issuer = Reference(display = "issuer")
        )
        assertEquals(expectedQualification, localizedQualification)
    }

    @Test
    fun `localizes qualification with localizable identifier`() {
        val qualification = Qualification(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            identifier = listOf(Identifier(extension = localizableExtensions, value = "id")),
            code = CodeableConcept(text = "code"),
            period = Period(start = DateTime("2021")),
            issuer = Reference(display = "issuer")
        )
        val localizedQualification = localizer.localize(qualification, tenant)
        assertNotEquals(qualification, localizedQualification)

        val expectedQualification = Qualification(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            identifier = listOf(Identifier(extension = localizedExtensions, value = "id")),
            code = CodeableConcept(text = "code"),
            period = Period(start = DateTime("2021")),
            issuer = Reference(display = "issuer")
        )
        assertEquals(expectedQualification, localizedQualification)
    }

    @Test
    fun `localizes qualification with localizable code`() {
        val qualification = Qualification(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            identifier = listOf(Identifier(value = "id")),
            code = CodeableConcept(extension = localizableExtensions, text = "code"),
            period = Period(start = DateTime("2021")),
            issuer = Reference(display = "issuer")
        )
        val localizedQualification = localizer.localize(qualification, tenant)
        assertNotEquals(qualification, localizedQualification)

        val expectedQualification = Qualification(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            identifier = listOf(Identifier(value = "id")),
            code = CodeableConcept(extension = localizedExtensions, text = "code"),
            period = Period(start = DateTime("2021")),
            issuer = Reference(display = "issuer")
        )
        assertEquals(expectedQualification, localizedQualification)
    }

    @Test
    fun `localizes qualification with localizable period`() {
        val qualification = Qualification(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            identifier = listOf(Identifier(value = "id")),
            code = CodeableConcept(text = "code"),
            period = Period(extension = localizableExtensions, start = DateTime("2021")),
            issuer = Reference(display = "issuer")
        )
        val localizedQualification = localizer.localize(qualification, tenant)
        assertNotEquals(qualification, localizedQualification)

        val expectedQualification = Qualification(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            identifier = listOf(Identifier(value = "id")),
            code = CodeableConcept(text = "code"),
            period = Period(extension = localizedExtensions, start = DateTime("2021")),
            issuer = Reference(display = "issuer")
        )
        assertEquals(expectedQualification, localizedQualification)
    }

    @Test
    fun `localizes qualification with localizable issuer`() {
        val qualification = Qualification(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            identifier = listOf(Identifier(value = "id")),
            code = CodeableConcept(text = "code"),
            period = Period(start = DateTime("2021")),
            issuer = Reference(extension = localizableExtensions, display = "issuer")
        )
        val localizedQualification = localizer.localize(qualification, tenant)
        assertNotEquals(qualification, localizedQualification)

        val expectedQualification = Qualification(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            identifier = listOf(Identifier(value = "id")),
            code = CodeableConcept(text = "code"),
            period = Period(start = DateTime("2021")),
            issuer = Reference(extension = localizedExtensions, display = "issuer")
        )
        assertEquals(expectedQualification, localizedQualification)
    }

    @Test
    fun `localizes qualification with localizable extension, modifier extension, identifier, code, period and issuer`() {
        val qualification = Qualification(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = localizableExtensions,
            identifier = listOf(Identifier(extension = localizableExtensions, value = "id")),
            code = CodeableConcept(extension = localizableExtensions, text = "code"),
            period = Period(extension = localizableExtensions, start = DateTime("2021")),
            issuer = Reference(extension = localizableExtensions, display = "issuer")
        )
        val localizedQualification = localizer.localize(qualification, tenant)
        assertNotEquals(qualification, localizedQualification)

        val expectedQualification = Qualification(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = localizedExtensions,
            identifier = listOf(Identifier(extension = localizedExtensions, value = "id")),
            code = CodeableConcept(extension = localizedExtensions, text = "code"),
            period = Period(extension = localizedExtensions, start = DateTime("2021")),
            issuer = Reference(extension = localizedExtensions, display = "issuer")
        )
        assertEquals(expectedQualification, localizedQualification)
    }

    @Test
    fun `returns current reference if reference has no localizable information`() {
        val reference = Reference(
            id = "12345",
            extension = nonLocalizableExtensions,
            type = Uri("Patient"),
            identifier = Identifier(value = "123"),
            display = "Patient 123"
        )

        val localizedReference = localizeReference(reference)
        assertNull(localizedReference)
    }

    @Test
    fun `localizes reference with localizable reference string`() {
        val reference = Reference(
            id = "12345",
            extension = nonLocalizableExtensions,
            reference = "Patient/123",
            type = Uri("Patient"),
            identifier = Identifier(value = "123"),
            display = "Patient 123"
        )

        val localizedReference = localizeReference(reference)
        assertNotEquals(reference, localizedReference)

        val expectedReference = Reference(
            id = "12345",
            extension = nonLocalizableExtensions,
            reference = "Patient/test-123",
            type = Uri("Patient"),
            identifier = Identifier(value = "123"),
            display = "Patient 123"
        )
        assertEquals(expectedReference, localizedReference)
    }

    @Test
    fun `localize ConditionStage`() {
        val conditionStage = ConditionStage(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            summary = CodeableConcept(
                extension = localizableExtensions,
                coding = listOf(
                    Coding(
                        system = Uri("http://cancerstaging.org"),
                        code = Code("3C"),
                        display = "IIIC"
                    )
                )
            ),
            assessment = listOf(
                Reference(
                    reference = "Condition/ReferenceExample01"
                )
            ),
            type = CodeableConcept(
                extension = localizableExtensions,
                coding = listOf(
                    Coding(
                        system = Uri("http://snomed.info/sct"),
                        code = Code("254360008"),
                        display = "Dukes staging system"
                    )
                )

            )
        )
        val localizedConditionStage = localizer.localize(conditionStage, tenant)
        assertNotEquals(conditionStage, localizedConditionStage)
        val expectedConditionStage = ConditionStage(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = nonLocalizableExtensions,
            summary = CodeableConcept(
                extension = localizedExtensions,
                coding = listOf(
                    Coding(
                        system = Uri("http://cancerstaging.org"),
                        code = Code("3C"),
                        display = "IIIC"
                    )
                )
            ),
            assessment = listOf(
                Reference(
                    reference = "Condition/test-ReferenceExample01"
                )
            ),
            type = CodeableConcept(
                extension = localizedExtensions,
                coding = listOf(
                    Coding(
                        system = Uri("http://snomed.info/sct"),
                        code = Code("254360008"),
                        display = "Dukes staging system"
                    )
                )

            )
        )
        assertEquals(expectedConditionStage, localizedConditionStage)
    }

    @Test
    fun `returns original ConditionStage if no localizable fields`() {
        val conditionStage = ConditionStage(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            summary = CodeableConcept(
                extension = nonLocalizableExtensions
            )
        )
        val localizedConditionStage = localizer.localize(conditionStage, tenant)
        assertEquals(conditionStage, localizedConditionStage)
    }

    @Test
    fun `localize ConditionStage with assessment but no summary`() {
        val conditionStage = ConditionStage(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizableExtensions,
            assessment = listOf(
                Reference(
                    reference = "Condition/ReferenceExample01"
                )
            )
        )
        val localizedConditionStage = localizer.localize(conditionStage, tenant)
        val expectedConditionStage = ConditionStage(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = localizedExtensions,
            assessment = listOf(
                Reference(
                    reference = "Condition/test-ReferenceExample01"
                )
            )
        )

        assertEquals(expectedConditionStage, localizedConditionStage)
    }

    @Test
    fun `localize ConditionEvidence`() {
        val conditionEvidence = ConditionEvidence(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            code = listOf(
                CodeableConcept(
                    text = "Potatoes"
                )
            ),
            detail = listOf(
                Reference(
                    reference = "Condition/ReferenceExample01"
                )
            )
        )
        val localizedConditionEvidence = localizer.localize(conditionEvidence, tenant)
        assertNotEquals(conditionEvidence, localizedConditionEvidence)
        val expectedConditionEvidence = ConditionEvidence(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = nonLocalizableExtensions,
            code = listOf(
                CodeableConcept(
                    text = "Potatoes"
                )
            ),
            detail = listOf(
                Reference(
                    reference = "Condition/test-ReferenceExample01"
                )
            )
        )
        assertEquals(expectedConditionEvidence, localizedConditionEvidence)
    }

    @Test
    fun `returns original ConditionEvidence if no localizable fields`() {
        val conditionEvidence = ConditionEvidence(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            code = listOf(
                CodeableConcept(
                    text = "Potatoes"
                )
            ),
            detail = listOf(
                Reference(
                    display = "Potato01"
                )
            )
        )
        val localizedConditionEvidence = localizer.localize(conditionEvidence, tenant)
        assertEquals(conditionEvidence, localizedConditionEvidence)
    }

    @Test
    fun `localizes ConditionEvidence if contains detail but no code`() {
        val conditionEvidence = ConditionEvidence(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            detail = listOf(
                Reference(
                    reference = "Condition/ReferenceExample01"
                )
            )
        )
        val localizedConditionEvidence = localizer.localize(conditionEvidence, tenant)
        val expectedConditionEvidence = ConditionEvidence(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            detail = listOf(
                Reference(
                    reference = "Condition/test-ReferenceExample01"
                )
            )
        )
        assertEquals(expectedConditionEvidence, localizedConditionEvidence)
    }

    @Test
    fun `localizes ConditionEvidence if contains code but no detail`() {
        val conditionEvidence = ConditionEvidence(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            code = listOf(
                CodeableConcept(
                    extension = localizableExtensions,
                    text = "Potatoes"
                )
            )
        )
        val localizedConditionEvidence = localizer.localize(conditionEvidence, tenant)
        val expectedConditionEvidence = ConditionEvidence(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            code = listOf(
                CodeableConcept(
                    extension = localizedExtensions,
                    text = "Potatoes"
                )
            )
        )
        assertEquals(expectedConditionEvidence, localizedConditionEvidence)
    }

    @Test
    fun `localize Annotations`() {
        val annotation = Annotation(
            id = "12345",
            extension = localizableExtensions,
            author = DynamicValue(
                DynamicValueType.REFERENCE,
                Reference(reference = "Practitioner/roninPractitionerExample01")
            ),
            time = DateTime("2022-02"),
            text = Markdown("Test")
        )
        val localizedAnnotation = localizer.localize(annotation, tenant)
        assertNotEquals(annotation, localizedAnnotation)

        val expectedAnnotation = Annotation(
            id = "12345",
            extension = localizedExtensions,
            author = DynamicValue(
                DynamicValueType.REFERENCE,
                Reference(reference = "Practitioner/test-roninPractitionerExample01")
            ),
            time = DateTime("2022-02"),
            text = Markdown("Test")
        )
        assertEquals(expectedAnnotation, localizedAnnotation)
    }

    @Test
    fun `returns original Annotation if no localizable fields`() {
        val annotation = Annotation(
            id = "12345",
            extension = nonLocalizableExtensions,
            time = DateTime("2022-02"),
            text = Markdown("Test")
        )
        val localizedAnnotation = localizer.localize(annotation, tenant)
        assertEquals(annotation, localizedAnnotation)
    }

    @Test
    fun `ObservationReferenceRange localizes`() {
        val localizableCodeableConcept = CodeableConcept(
            id = "12345",
            extension = localizableExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val expectedCodeableConcept = CodeableConcept(
            id = "12345",
            extension = localizedExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val observationReferenceRange = ObservationReferenceRange(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = localizableExtensions,
            low = SimpleQuantity(),
            high = SimpleQuantity(),
            type = localizableCodeableConcept,
            appliesTo = emptyList(),
            age = Range(),
            text = "Text"
        )
        val expectedObservationReferenceRange = ObservationReferenceRange(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = localizedExtensions,
            low = SimpleQuantity(),
            high = SimpleQuantity(),
            type = expectedCodeableConcept,
            appliesTo = emptyList(),
            age = Range(),
            text = "Text"
        )

        val localizedObservationReferenceRange = localizer.localize(observationReferenceRange, tenant)

        assertNotEquals(observationReferenceRange, localizedObservationReferenceRange)
        assertEquals(expectedObservationReferenceRange, localizedObservationReferenceRange)
    }

    @Test
    fun `ObservationReferenceRange localizes when lots of things are null`() {
        val localizableCodeableConcept = CodeableConcept(
            id = "12345",
            extension = localizableExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val expectedCodeableConcept = CodeableConcept(
            id = "12345",
            extension = localizedExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val observationReferenceRange = ObservationReferenceRange(
            id = "12345",
            extension = emptyList(),
            modifierExtension = emptyList(),
            type = null,
            appliesTo = listOf(localizableCodeableConcept),
            age = Range(),
            text = "Text"
        )
        val expectedObservationReferenceRange = ObservationReferenceRange(
            id = "12345",
            extension = emptyList(),
            modifierExtension = emptyList(),
            type = null,
            appliesTo = listOf(expectedCodeableConcept),
            age = Range(),
            text = "Text"
        )

        val localizedObservationReferenceRange = localizer.localize(observationReferenceRange, tenant)

        assertNotEquals(observationReferenceRange, localizedObservationReferenceRange)
        assertEquals(expectedObservationReferenceRange, localizedObservationReferenceRange)
    }

    @Test
    fun `ObservationReferenceRange doesn't localize when no localization needed`() {
        val nonlocalizableCodeableConcept = CodeableConcept(
            id = "12345",
            extension = nonLocalizableExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val observationReferenceRange = ObservationReferenceRange(
            id = "12345",
            type = nonlocalizableCodeableConcept,
            text = "Text"
        )

        val localizedObservationReferenceRange = localizer.localize(observationReferenceRange, tenant)

        assertEquals(observationReferenceRange, localizedObservationReferenceRange)
    }

    @Test
    fun `ObservationComponent localizes`() {
        val localizableCodeableConcept = CodeableConcept(
            id = "12345",
            extension = localizableExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val expectedCodeableConcept = CodeableConcept(
            id = "12345",
            extension = localizedExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val observationReferenceRange = ObservationReferenceRange(
            id = "12345",
            type = localizableCodeableConcept,
            text = "Text"
        )
        val localizedObservationReferenceRange = ObservationReferenceRange(
            id = "12345",
            type = expectedCodeableConcept,
            text = "Text"
        )
        val observationComponent = ObservationComponent(
            id = "12345",
            extension = localizableExtensions,
            modifierExtension = localizableExtensions,
            code = localizableCodeableConcept,
            value = DynamicValue(
                DynamicValueType.REFERENCE,
                Reference(reference = "Practitioner/123")
            ),
            dataAbsentReason = null,
            interpretation = listOf(localizableCodeableConcept),
            referenceRange = listOf(observationReferenceRange)
        )

        val expectedObservationComponent = ObservationComponent(
            id = "12345",
            extension = localizedExtensions,
            modifierExtension = localizedExtensions,
            code = expectedCodeableConcept,
            value = DynamicValue(
                DynamicValueType.REFERENCE,
                Reference(reference = "Practitioner/test-123")
            ),
            dataAbsentReason = null,
            interpretation = listOf(expectedCodeableConcept),
            referenceRange = listOf(localizedObservationReferenceRange)
        )

        val localizedObservationComponent = localizer.localize(observationComponent, tenant)

        assertNotEquals(observationComponent, localizedObservationComponent)
        assertEquals(expectedObservationComponent, localizedObservationComponent)
    }

    @Test
    fun `ObservationComponent localizes with nulls`() {
        val localizableCodeableConcept = CodeableConcept(
            id = "12345",
            extension = localizableExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val expectedCodeableConcept = CodeableConcept(
            id = "12345",
            extension = localizedExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val observationComponent = ObservationComponent(
            id = "12345",
            code = localizableCodeableConcept,
            value = null
        )

        val expectedObservationComponent = ObservationComponent(
            id = "12345",
            code = expectedCodeableConcept,
            value = null
        )

        val localizedObservationComponent = localizer.localize(observationComponent, tenant)

        assertNotEquals(observationComponent, localizedObservationComponent)
        assertEquals(expectedObservationComponent, localizedObservationComponent)
    }

    @Test
    fun `ObservationComponent localizes with null dynamic value`() {
        val localizableCodeableConcept = CodeableConcept(
            id = "12345",
            extension = localizableExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val nonLocalizableCodeableConcept = CodeableConcept(
            id = "12345",
            extension = nonLocalizableExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val expectedCodeableConcept = CodeableConcept(
            id = "12345",
            extension = localizedExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val observationComponent = ObservationComponent(
            id = "12345",
            extension = emptyList(),
            modifierExtension = emptyList(),
            code = nonLocalizableCodeableConcept,
            value = null,
            dataAbsentReason = localizableCodeableConcept,
            interpretation = emptyList(),
            referenceRange = emptyList()
        )

        val expectedObservationComponent = ObservationComponent(
            id = "12345",
            extension = emptyList(),
            modifierExtension = emptyList(),
            code = nonLocalizableCodeableConcept,
            value = null,
            dataAbsentReason = expectedCodeableConcept,
            interpretation = emptyList(),
            referenceRange = emptyList()
        )

        val localizedObservationComponent = localizer.localize(observationComponent, tenant)

        assertNotEquals(observationComponent, localizedObservationComponent)
        assertEquals(expectedObservationComponent, localizedObservationComponent)
    }

    @Test
    fun `ObservationComponent doesn't localize`() {
        val nonLocalizableCodeableConcept = CodeableConcept(
            id = "12345",
            extension = nonLocalizableExtensions,
            coding = listOf(Coding(display = "coding")),
            text = "Text"
        )
        val observationReferenceRange = ObservationReferenceRange(
            id = "12345",
            type = nonLocalizableCodeableConcept,
            text = "Text"
        )
        val observationComponent = ObservationComponent(
            id = "12345",
            extension = nonLocalizableExtensions,
            modifierExtension = nonLocalizableExtensions,
            code = nonLocalizableCodeableConcept,
            value = DynamicValue(
                DynamicValueType.STRING,
                Reference(reference = "TEST")
            ),
            dataAbsentReason = null,
            interpretation = listOf(nonLocalizableCodeableConcept),
            referenceRange = listOf(observationReferenceRange)
        )

        val localizedObservationComponent = localizer.localize(observationComponent, tenant)

        assertEquals(observationComponent, localizedObservationComponent)
    }
}
