package com.projectronin.interop.tenant.config.model.vendor

import com.projectronin.interop.common.vendor.VendorType
import com.projectronin.interop.tenant.config.model.EpicAuthenticationConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EpicTest {
    @Test
    fun `check getters`() {
        val authenticationConfig = EpicAuthenticationConfig("authEndpoint", "public", "private")
        val epic =
            Epic(
                "clientId",
                authenticationConfig,
                "https://localhost:8080/",
                "Epic Sandbox",
                "21.10",
                "RoninUser",
                "Ronin Message",
                "urn:oid:1.2.840.114350.1.13.0.1.7.2.836982",
                "urn:oid:1.2.840.114350.1.13.0.1.7.2.697780",
                "urn:oid:1.2.840.114350.1.13.0.1.7.5.737384.14",
                "urn:oid:1.2.840.114350.1.13.0.1.7.5.737384.15",
                "urn:oid:1.2.840.114350.1.13.0.1.7.5.737384.8",
                "MRN",
                "hsiValue",
                "urn:oid:1.2.840.114350.1.13.297.3.7.2.686980",
                "E8675309",
                "urn:oid:1.2.840.114350.1.13.0.1.7.2.798268",
                "24.02",
            )

        assertEquals(VendorType.EPIC, epic.type)
        assertEquals("clientId", epic.clientId)
        assertEquals(authenticationConfig, epic.authenticationConfig)
        assertEquals("https://localhost:8080/", epic.serviceEndpoint)
        assertEquals("21.10", epic.release)
        assertEquals("RoninUser", epic.ehrUserId)
        assertEquals("Ronin Message", epic.messageType)
        assertEquals("urn:oid:1.2.840.114350.1.13.0.1.7.2.836982", epic.practitionerProviderSystem)
        assertEquals("urn:oid:1.2.840.114350.1.13.0.1.7.2.697780", epic.practitionerUserSystem)
        assertEquals("urn:oid:1.2.840.114350.1.13.0.1.7.5.737384.14", epic.patientMRNSystem)
        assertEquals("urn:oid:1.2.840.114350.1.13.0.1.7.5.737384.15", epic.patientInternalSystem)
        assertEquals("hsiValue", epic.hsi)
        assertEquals("urn:oid:1.2.840.114350.1.13.297.3.7.2.686980", epic.departmentInternalSystem)
        assertEquals("E8675309", epic.patientOnboardedFlagId)
        assertEquals("urn:oid:1.2.840.114350.1.13.0.1.7.2.798268", epic.orderSystem)
        assertEquals("24.02", epic.appVersion)
    }
}
