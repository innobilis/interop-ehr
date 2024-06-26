plugins {
    alias(libs.plugins.interop.gradle.spring)
    alias(libs.plugins.interop.gradle.junit)
}

dependencies {
    api(libs.ktorm.core)

    implementation(libs.interop.common)
    implementation(libs.interop.commonKtorm)

    implementation(libs.commons.text)
    implementation(libs.jackson.datatype.jsr310)

    implementation("org.springframework:spring-core")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")

    testImplementation(project(":interop-ehr-liquibase"))
    testImplementation(libs.interop.commonTestDb)

    testImplementation(libs.mockk)
    testImplementation("org.springframework:spring-test")

    testRuntimeOnly(libs.bundles.test.mysql)
}
