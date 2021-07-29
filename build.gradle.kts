plugins {
    id("com.hivemq.extension")
    id("com.github.hierynomus.license")
    id("com.github.sgtsilvio.gradle.utf8")
}

/* ******************** metadata ******************** */

group = "com.hivemq.extensions"
description = "HiveMQ Cloudwatch Extension"

hivemqExtension {
    name.set("Cloudwatch Extension")
    author.set("HiveMQ")
    priority.set(1000)
    startPriority.set(1000)
    mainClass.set("$group.cloudwatch.CloudWatchMain")
    sdkVersion.set("${property("hivemq-extension-sdk.version")}")

    resources {
        from("LICENSE")
    }
}

/* ******************** dependencies ******************** */

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.azagniotov:dropwizard-metrics-cloudwatch:${property("cloudwatch-metric.version")}")
    implementation("software.amazon.awssdk:cloudwatch:${property("cloudwatch-sdk.version")}")

    // configuration
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:${property("jakarta-xml-bind.version")}")
    runtimeOnly("com.sun.xml.bind:jaxb-impl:${property("jaxb.version")}")
}

/* ******************** test ******************** */

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junit-jupiter.version")}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${property("junit-jupiter.version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.mockito:mockito-core:${property("mockito.version")}")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
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