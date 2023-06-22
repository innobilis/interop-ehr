package com.projectronin.interop.fhir.ronin.generators.resource

import com.projectronin.interop.common.jackson.JacksonManager
import com.projectronin.interop.fhir.r4.datatype.ContactPoint
import com.projectronin.interop.fhir.r4.datatype.Identifier
import com.projectronin.interop.fhir.r4.datatype.primitive.Code
import com.projectronin.interop.fhir.r4.datatype.primitive.asFHIR
import com.projectronin.interop.fhir.r4.resource.Location
import com.projectronin.interop.fhir.ronin.element.RoninContactPoint
import com.projectronin.interop.fhir.ronin.localization.Localizer
import com.projectronin.interop.fhir.ronin.localization.Normalizer
import com.projectronin.interop.fhir.ronin.profile.RoninProfile
import com.projectronin.interop.fhir.ronin.resource.RoninLocation
import com.projectronin.interop.fhir.validate.LocationContext
import com.projectronin.interop.tenant.config.model.Tenant
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoninLocationTest {
    private lateinit var roninContactPoint: RoninContactPoint
    private lateinit var roninLocation: RoninLocation
    private val tenant = mockk<Tenant> {
        every { mnemonic } returns "test"
    }

    @BeforeEach
    fun setup() {
        roninContactPoint = mockk {
            every { validateRonin(any(), LocationContext(Location::class), any()) } answers { thirdArg() }
            every { validateUSCore(any(), LocationContext(Location::class), any()) } answers { thirdArg() }
        }
        val normalizer: Normalizer = mockk {
            every { normalize(any(), tenant) } answers { firstArg() }
        }
        val localizer: Localizer = mockk {
            every { localize(any(), tenant) } answers { firstArg() }
        }
        roninLocation = RoninLocation(normalizer, localizer, roninContactPoint)
    }

    @Test
    fun `example use for roninLocation`() {
        // create location resource with attributes you need, provide the tenant(mda), here "fake-tenant"
        val roninLocation = rcdmLocation("fake-tenant") {
            // to test an attribute like status - provide the value
            status of Code("testing-this-status")
            // same with other attributes
            // be sure attributes are provided correctly, below contact-point is a list
            telecom of listOf(
                ContactPoint(
                    value = "123-456-7890".asFHIR()
                )
            )
        }

        // This object can be serialized to JSON to be injected into your workflow, all required R4 attributes wil be generated
        val roninLocationJSON = JacksonManager.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(roninLocation)

        // Uncomment to take a peek at the JSON
        // println(roninLocationJSON)
        assertNotNull(roninLocationJSON)
    }

    @Test
    fun `example use for roninLocation - missing required fields generated`() {
        // create location resource with attributes you need, provide the tenant(mda), here "fake-tenant"
        val roninLocation = rcdmLocation("fake-tenant") {
            // meta, identifiers, name, telecom will all be generated
        }
        // This object can be serialized to JSON to be injected into your workflow, all required R4 attributes wil be generated
        val roninLocationJSON = JacksonManager.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(roninLocation)

        // Uncomment to take a peek at the JSON
        // println(roninLocationJSON)
        assertNotNull(roninLocationJSON)
        assertNotNull(roninLocation.meta)
        assertEquals(
            roninLocation.meta!!.profile[0].value,
            RoninProfile.LOCATION.value
        )
        assertEquals(3, roninLocation.identifier.size)
        assertNotNull(roninLocation.status)
        assertNotNull(roninLocation.name)
        assertNotNull(roninLocation.telecom)
    }

    @Test
    fun `validates with identifier added`() {
        val location = rcdmLocation("test") {
            identifier of listOf(Identifier(id = "ID-Id".asFHIR()))
            status of Code("active")
            name of "this is a name"
        }
        val validation = roninLocation.validate(location, null).hasErrors()
        assertEquals(validation, false)
        assertEquals(4, location.identifier.size)
        val ids = location.identifier.map { it.id }.toSet()
        assertTrue(ids.contains("ID-Id".asFHIR()))
    }

    @Test
    fun `generates valid rcdm location`() {
        val location = rcdmLocation("test") {}
        val validation = roninLocation.validate(location, null).hasErrors()
        assertEquals(validation, false)
        assertNotNull(location.meta)
        assertNotNull(location.identifier)
        assertEquals(3, location.identifier.size)
        assertNotNull(location.name)
        assertNotNull(location.telecom)
    }

    @Test
    fun `generates rcdm location with given status but fails validation`() {
        val location = rcdmLocation("test") {
            status of Code("this is a bad status")
        }
        assertEquals(location.status, Code("this is a bad status"))

        // validate location should fail
        val validation = roninLocation.validate(location, null)
        assertTrue(validation.hasErrors())

        val issueCodes = validation.issues().map { it.code }.toSet()
        assertEquals(setOf("INV_VALUE_SET"), issueCodes)
    }

    @Test
    fun `generates rcdm location and validates with contact-point and status`() {
        val contactPoint = ContactPoint(
            value = "123-456-7890".asFHIR(),
            system = Code("something")
        )
        val location = rcdmLocation("test") {
            status of Code("active")
            telecom of listOf(contactPoint)
            name of "this is a name"
        }
        val validation = roninLocation.validate(location, null).hasErrors()
        assertEquals(validation, false)
        assertNotNull(location.meta)
        assertNotNull(location.identifier)
        assertEquals(3, location.identifier.size)
        assertNotNull(location.telecom)
    }

    @Test
    fun `fails validation with mode being incorrect`() {
        val location = rcdmLocation("test") {
            status of Code("active")
            mode of Code("mode")
            name of "this is a name"
        }
        val validation = roninLocation.validate(location, null)
        assertTrue(validation.hasErrors())

        val issueCodes = validation.issues().map { it.code }.toSet()
        assertEquals(setOf("INV_VALUE_SET"), issueCodes)
    }

    @Test
    fun `validates if empty name provided, data-absent-reason is returned`() {
        val location = rcdmLocation("test") {
            name of ""
        }
        val validation = roninLocation.validate(location, null)
        assertFalse(validation.hasErrors())
        assertNotNull(location.name)
        assertTrue(location.name?.value!!.contains("http://hl7.org/fhir/StructureDefinition/data-absent-reason"))
    }
}