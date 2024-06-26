package com.projectronin.interop.tenant.config.model

import com.projectronin.interop.tenant.config.model.vendor.Vendor
import java.time.ZoneId

/**
 * Configuration associated to a Tenant.
 * @property internalId The ID of the backing data store for this Tenant.
 * @property mnemonic The tenant's mnemonic.
 * @property name The tenant's full name.
 * @property timezone The tenant's timezone.
 * @property batchConfig The batch configuration.
 * @property vendor The vendor-specific configuration.
 * @property monitoredIndicator Flag indicating whether the tenant is monitored or not.
 */
data class Tenant(
    val internalId: Int,
    val mnemonic: String,
    val name: String,
    val timezone: ZoneId,
    val batchConfig: BatchConfig?,
    val vendor: Vendor,
    val monitoredIndicator: Boolean?,
) {
    inline fun <reified T : Vendor> vendorAs(): T {
        if (vendor !is T) throw RuntimeException("Vendor is not a ${T::class.java.simpleName}")
        return vendor
    }
}
