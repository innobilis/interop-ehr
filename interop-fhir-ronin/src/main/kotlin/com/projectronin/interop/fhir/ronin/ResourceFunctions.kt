package com.projectronin.interop.fhir.ronin

import com.projectronin.interop.fhir.r4.CodeSystem
import com.projectronin.interop.fhir.r4.CodeableConcepts
import com.projectronin.interop.fhir.r4.datatype.Identifier
import com.projectronin.interop.fhir.r4.datatype.primitive.FHIRString
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.r4.resource.Resource
import com.projectronin.interop.fhir.ronin.util.dataAuthorityIdentifier
import com.projectronin.interop.fhir.ronin.util.toFhirIdentifier
import com.projectronin.interop.tenant.config.model.Tenant

fun <T : Resource<T>> T.getFhirIdentifiers(): List<Identifier> =
    id?.toFhirIdentifier()?.let { listOf(it) } ?: emptyList()

fun Id?.toFhirIdentifier(): Identifier? = this?.let {
    Identifier(
        value = value?.let { FHIRString(it) },
        system = CodeSystem.RONIN_FHIR_ID.uri,
        type = CodeableConcepts.RONIN_FHIR_ID
    )
}

fun <T : Resource<T>> T.getRoninIdentifiersForResource(tenant: Tenant): List<Identifier> =
    this.getFhirIdentifiers() + tenant.toFhirIdentifier() + dataAuthorityIdentifier
