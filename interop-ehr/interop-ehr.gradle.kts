plugins {
    id("com.projectronin.interop.gradle.spring")
    id("com.projectronin.interop.gradle.junit")
}

dependencies {
    implementation(project(":interop-tenant"))
    implementation(libs.interop.common)
    implementation(libs.interop.commonHttp)
    implementation(libs.interop.commonJackson)
    implementation(libs.interop.fhir)
    implementation(libs.ktor.client.core)
    implementation(libs.jackson.databind)
    implementation("org.springframework:spring-context")

    testImplementation(libs.mockk)
}
