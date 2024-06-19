package com.hivemq.extensions.aws.cloudwatch

import org.testcontainers.utility.DockerImageName

val LOCALSTACK_DOCKER_IMAGE: DockerImageName = DockerImageName.parse("localstack/localstack:3.2.0")

val HIVEMQ_DOCKER_IMAGE: DockerImageName = DockerImageName.parse("hivemq/hivemq4:4.28.2")

