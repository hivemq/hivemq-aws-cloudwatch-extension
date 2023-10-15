plugins {
    alias(libs.plugins.hivemq.extension)
    alias(libs.plugins.defaults)
    alias(libs.plugins.license)
}

group = "com.hivemq.extensions"
description = "HiveMQ AWS CloudWatch Extension"

hivemqExtension {
    name.set("AWS CloudWatch Extension")
    author.set("HiveMQ")
    priority.set(1000)
    startPriority.set(1000)
    mainClass.set("$group.cloudwatch.CloudWatchMain")
    sdkVersion.set(libs.versions.hivemq.extensionSdk)

    resources {
        from("LICENSE")
    }
}

dependencies {
    implementation(libs.dropwizard.metrics.cloudwatch)
    implementation(libs.aws.sdkv2.cloudwatch)

    // configuration
    implementation(libs.jaxb.api)
    runtimeOnly(libs.jaxb.impl)
}

/* ******************** test ******************** */

dependencies {
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.mockito)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

/* ******************** integration test ******************** */

dependencies {
    integrationTestImplementation(libs.hivemq.mqttClient)

    integrationTestImplementation(platform(libs.testcontainers.bom))
    integrationTestImplementation(libs.testcontainers)
    integrationTestImplementation(libs.testcontainers.junitJupiter)
    integrationTestImplementation(libs.testcontainers.localstack)
    integrationTestImplementation(libs.testcontainers.hivemq)

    //necessary as the localstack s3 service would not start without the old sdk
    integrationTestRuntimeOnly(libs.aws.sdkv1.s3)
    integrationTestRuntimeOnly(libs.logback.classic)
}

/* ******************** checks ******************** */

license {
    header = rootDir.resolve("HEADER")
    mapping("java", "SLASHSTAR_STYLE")
}

/* ******************** debugging ******************** */

tasks.prepareHivemqHome {
    hivemqHomeDirectory.set(file("/your/path/to/hivemq-<VERSION>"))
}

tasks.runHivemqWithExtension {
    debugOptions {
        enabled.set(false)
    }
}
