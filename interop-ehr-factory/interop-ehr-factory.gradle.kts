plugins {
    id("com.projectronin.interop.gradle.spring")
    id("com.projectronin.interop.gradle.junit")
    id("com.projectronin.interop.gradle.mockk")
}

dependencies {
    implementation("com.projectronin.interop:interop-common:${project.property("interopCommonVersion")}")
    implementation(project(":interop-ehr"))
    implementation(project(":interop-tenant"))
    implementation(project(":interop-transform"))

    // Spring
    implementation("org.springframework:spring-context")
}
