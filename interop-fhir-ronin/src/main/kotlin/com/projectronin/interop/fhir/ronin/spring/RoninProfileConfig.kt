package com.projectronin.interop.fhir.ronin.spring

import com.projectronin.interop.datalake.spring.DatalakeSpringConfig
import com.projectronin.interop.ehr.spring.EHRSpringConfig
import com.projectronin.interop.validation.client.spring.ValidationClientSpringConfig
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(DatalakeSpringConfig::class, EHRSpringConfig::class, ValidationClientSpringConfig::class)
@ComponentScan(
    *[
        "com.projectronin.interop.fhir.ronin.conceptmap",
        "com.projectronin.interop.fhir.ronin.element",
        "com.projectronin.interop.fhir.ronin.localization",
        "com.projectronin.interop.fhir.ronin.normalization",
        "com.projectronin.interop.fhir.ronin.resource",
        "com.projectronin.interop.fhir.ronin.transform",
        "com.projectronin.interop.fhir.ronin.validation",
    ],
)
class RoninProfileConfig
