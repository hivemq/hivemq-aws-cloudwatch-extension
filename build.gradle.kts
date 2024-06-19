plugins {
    alias(libs.plugins.hivemq.extension)
    alias(libs.plugins.defaults)
    alias(libs.plugins.license)
    alias(libs.plugins.kotlin)
}

group = "com.hivemq.extensions"
description = "HiveMQ AWS CloudWatch Extension"

hivemqExtension {
    name = "AWS CloudWatch Extension"
    author = "HiveMQ"
    priority = 1000
    startPriority = 1000
    sdkVersion = libs.versions.hivemq.extensionSdk

    resources {
        from("LICENSE")
    }
}

dependencies {
    compileOnly(libs.jetbrains.annotations)

    implementation(libs.dropwizard.metrics.cloudwatch)
    implementation(libs.aws.sdkv2.cloudwatch)

    // configuration
    implementation(libs.jaxb.api)
    runtimeOnly(libs.jaxb.impl)
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        withType<JvmTestSuite> {
            useJUnitJupiter(libs.versions.junit.jupiter)
        }
        "test"(JvmTestSuite::class) {
            dependencies {
                compileOnly(libs.jetbrains.annotations)
                implementation(libs.mockito)
            }
        }
        "integrationTest"(JvmTestSuite::class) {
            dependencies {
                compileOnly(libs.jetbrains.annotations)
                implementation(libs.awaitility)
                implementation(libs.aws.sdkv2.cloudwatch)
                implementation(libs.hivemq.mqttClient)
                implementation(libs.kotlin.stdlib)
                implementation(libs.okhttp)
                implementation(libs.testcontainers.hivemq)
                implementation(libs.testcontainers.junitJupiter)
                implementation(libs.testcontainers.localstack)
                runtimeOnly(libs.logback.classic)
            }
        }
    }
}

license {
    header = rootDir.resolve("HEADER")
    mapping("java", "SLASHSTAR_STYLE")
}
