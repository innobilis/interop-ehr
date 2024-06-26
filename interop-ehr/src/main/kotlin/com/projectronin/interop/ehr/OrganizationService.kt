package com.projectronin.interop.ehr

import com.projectronin.interop.fhir.r4.resource.Organization
import com.projectronin.interop.tenant.config.model.Tenant

/**
 * Defines the functionality of an EHR's Organization service
 */
interface OrganizationService : FHIRService<Organization> {
    /**
     * Finds a list of [Organization]s by FHIRId for a [tenant]
     */
    @Deprecated("Use getByIDs")
    fun findOrganizationsByFHIRId(
        tenant: Tenant,
        organizationFHIRIds: List<String>,
    ): List<Organization>
}
