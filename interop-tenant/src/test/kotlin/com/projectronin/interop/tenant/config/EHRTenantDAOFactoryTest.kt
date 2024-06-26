package com.projectronin.interop.tenant.config

import com.projectronin.interop.common.vendor.VendorType
import com.projectronin.interop.tenant.config.data.CernerTenantDAO
import com.projectronin.interop.tenant.config.data.EpicTenantDAO
import com.projectronin.interop.tenant.config.data.model.TenantDO
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EHRTenantDAOFactoryTest {
    private val epicTenantDAO = mockk<EpicTenantDAO>()
    private val cernerTenantDAO = mockk<CernerTenantDAO>()

    @Test
    fun `returns epic correctly`() {
        val tenantDO =
            mockk<TenantDO> {
                every { ehr.vendorType } returns VendorType.EPIC
            }
        val dao = EHRTenantDAOFactory(epicTenantDAO, cernerTenantDAO).getEHRTenantDAO(tenantDO)
        assertEquals(epicTenantDAO, dao)
    }

    @Test
    fun `returns cerner correctly`() {
        val tenantDO =
            mockk<TenantDO> {
                every { ehr.vendorType } returns VendorType.CERNER
            }
        val dao = EHRTenantDAOFactory(epicTenantDAO, cernerTenantDAO).getEHRTenantDAO(tenantDO)
        assertEquals(cernerTenantDAO, dao)
    }

    @Test
    fun `returns all correctly`() {
        val dao1 = EHRTenantDAOFactory(epicTenantDAO, cernerTenantDAO).getEHRTenantDAOByVendorType(VendorType.EPIC)
        val dao2 = EHRTenantDAOFactory(epicTenantDAO, cernerTenantDAO).getEHRTenantDAOByVendorType(VendorType.CERNER)
        assertEquals(epicTenantDAO, dao1)
        assertEquals(cernerTenantDAO, dao2)
    }
}
