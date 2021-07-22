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
    sdkVersion.set("$version")

    resources {
        from("LICENSE")
    }
}

/* ******************** dependencies ******************** */

repositories {
    mavenCentral()
}

dependencies {

    implementation("com.blacklocus:metrics-cloudwatch:${property("cloudwatch.version")}")

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

/* ******************** integration test ******************** */

dependencies {
    integrationTestImplementation("com.hivemq:hivemq-mqtt-client:${property("hivemq-mqtt-client.version")}")
    integrationTestImplementation("com.hivemq:hivemq-testcontainer-junit5:${property("hivemq-testcontainer.version")}")
    integrationTestImplementation("ch.qos.logback:logback-classic:${property("logback-classic.version")}")
}

/* ******************** checks ******************** */

license {
    header = rootDir.resolve("HEADER")
    mapping("java", "SLASHSTAR_STYLE")
}

/* ******************** debugging ******************** */

tasks.prepareHivemqHome {
    hivemqHomeDirectory.set(file("/your/path/to/hivemq-<VERSION>.zip"))
}

tasks.runHivemqWithExtension {
    debugOptions {
        enabled.set(false)
    }
}