package com.projectronin.interop.ehr.epic

import com.projectronin.interop.common.vendor.VendorType
import com.projectronin.interop.datalake.oci.client.OCIClient
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.ktorm.database.Database
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

class EpicVendorFactoryTest {
    private val appointmentService = mockk<EpicAppointmentService>()
    private val messageService = mockk<EpicMessageService>()
    private val patientService = mockk<EpicPatientService>()
    private val practitionerService = mockk<EpicPractitionerService>()
    private val practitionerRoleService = mockk<EpicPractitionerRoleService>()
    private val conditionService = mockk<EpicConditionService>()
    private val identifierService = mockk<EpicIdentifierService>()
    private val observationService = mockk<EpicObservationService>()
    private val locationService = mockk<EpicLocationService>()
    private val medicationService = mockk<EpicMedicationService>()
    private val medicationStatementService = mockk<EpicMedicationStatementService>()

    private val vendorFactory =
        EpicVendorFactory(
            patientService,
            appointmentService,
            messageService,
            practitionerService,
            practitionerRoleService,
            conditionService,
            identifierService,
            observationService,
            locationService,
            medicationService,
            medicationStatementService,
        )

    @Test
    fun `is all wired for spring`() {
        @Configuration
        @ComponentScan("com.projectronin.interop")
        class TestConfig() {
            @Bean
            @Qualifier("ehr")
            fun ehrDatabase(): Database = mockk()

            @Bean
            @Qualifier("ConceptMap")
            fun ociClient(): OCIClient = mockk()

            @Bean
            fun taskExecutor(): ThreadPoolTaskExecutor = mockk(relaxed = true)
        }

        val applicationContext = AnnotationConfigApplicationContext(TestConfig::class.java)

        // If the Vendor Factory and its dependencies are not wired, this will throw an UnsatisfiedDependencyException.
        applicationContext.getBean(EpicVendorFactory::class.java)
    }

    @Test
    fun `vendor type is epic`() {
        assertEquals(VendorType.EPIC, vendorFactory.vendorType)
    }

    @Test
    fun `returns AppointmentService`() {
        assertEquals(appointmentService, vendorFactory.appointmentService)
    }

    @Test
    fun `returns MessageService`() {
        assertEquals(messageService, vendorFactory.messageService)
    }

    @Test
    fun `returns PatientService`() {
        assertEquals(patientService, vendorFactory.patientService)
    }

    @Test
    fun `returns PractitionerService`() {
        assertEquals(practitionerService, vendorFactory.practitionerService)
    }

    @Test
    fun `returns ConditionService`() {
        assertEquals(conditionService, vendorFactory.conditionService)
    }

    @Test
    fun `returns IdentifierService`() {
        assertEquals(identifierService, vendorFactory.identifierService)
    }

    @Test
    fun `returns ObservationService`() {
        assertEquals(observationService, vendorFactory.observationService)
    }

    @Test
    fun `returns LocationService`() {
        assertEquals(locationService, vendorFactory.locationService)
    }

    @Test
    fun `returns MedicationService`() {
        assertEquals(medicationService, vendorFactory.medicationService)
    }

    @Test
    fun `returns MedicationStatementService`() {
        assertEquals(medicationStatementService, vendorFactory.medicationStatementService)
    }
}
