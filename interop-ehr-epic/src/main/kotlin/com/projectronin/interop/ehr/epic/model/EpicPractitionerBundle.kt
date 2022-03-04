package com.projectronin.interop.ehr.epic.model

import com.projectronin.interop.ehr.epic.util.convertResources
import com.projectronin.interop.ehr.model.enums.DataSource
import com.projectronin.interop.fhir.r4.resource.Bundle

class EpicPractitionerBundle(override val resource: Bundle) : EpicFHIRBundle<EpicPractitioner>(resource) {
    override val dataSource: DataSource
        get() = DataSource.FHIR_R4

    override val resources: List<EpicPractitioner> by lazy {
        resource.convertResources(::EpicPractitioner).toSet().toList() // de-duplicates
    }
}
