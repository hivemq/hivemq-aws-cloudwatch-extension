/*
 * Copyright 2019-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.extensions.aws.cloudwatch;

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import io.github.sgtsilvio.gradle.oci.junit.jupiter.OciImages;
import org.awaitility.Durations;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.utility.MountableFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.MetricStat;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

class EndToEndIT {

    private final @NotNull Network network = org.testcontainers.containers.Network.newNetwork();

    private final @NotNull LocalStackContainer localStack =
            new LocalStackContainer(OciImages.getImageName("localstack/localstack")).withServices(Service.CLOUDWATCH)
                    .withNetwork(network)
                    .withNetworkAliases("localstack");

    private final @NotNull HiveMQContainer hivemq =
            new HiveMQContainer(OciImages.getImageName("hivemq/extensions/hivemq-aws-cloudwatch-extension")
                    .asCompatibleSubstituteFor("hivemq/hivemq4")).withCopyToContainer(MountableFile.forClasspathResource(
                                    "extension-config.xml"),
                            "/opt/hivemq/extensions/hivemq-aws-cloudwatch-extension/extension-config.xml")
                    .withEnv("AWS_REGION", localStack.getRegion())
                    .withEnv("AWS_ACCESS_KEY_ID", localStack.getAccessKey())
                    .withEnv("AWS_SECRET_ACCESS_KEY", localStack.getSecretKey())
                    .withLogConsumer(outputFrame -> System.out.println("HIVEMQ: " +
                            outputFrame.getUtf8StringWithoutLineEnding()))
                    .withNetwork(network);

    @BeforeEach
    void setUp() {
        localStack.start();
        hivemq.start();
    }

    @AfterEach
    void tearDown() {
        hivemq.stop();
        localStack.stop();
        network.close();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void endToEnd() {
        final StaticCredentialsProvider credentialsProvider =
                StaticCredentialsProvider.create(AwsBasicCredentials.create(localStack.getAccessKey(),
                        localStack.getSecretKey()));

        final CloudWatchClient cloudWatchClient = CloudWatchClient.builder()
                .credentialsProvider(credentialsProvider)
                .endpointOverride(localStack.getEndpointOverride(Service.CLOUDWATCH))
                .region(Region.of(localStack.getRegion()))
                .build();

        await().timeout(Durations.FIVE_MINUTES)
                .until(() -> cloudWatchClient.listMetrics()
                        .metrics()
                        .stream()
                        .anyMatch(metric -> "com.hivemq.messages.incoming.publish.count".equals(metric.metricName())));

        final Metric metric = Metric.builder()
                .namespace("hivemq-metrics")
                .metricName("com.hivemq.messages.incoming.publish.count")
                .dimensions(Collections.emptyList())
                .build();

        final MetricStat metricStat =
                MetricStat.builder().stat(Statistic.MAXIMUM.toString()).period(60).metric(metric).build();

        final MetricDataQuery metricDataQuery =
                MetricDataQuery.builder().id("m1").metricStat(metricStat).returnData(true).build();

        await().timeout(Durations.FIVE_MINUTES).until(() -> {
            final GetMetricDataRequest request = GetMetricDataRequest.builder()
                    .startTime(Instant.now().minusSeconds(3600))
                    .endTime(Instant.now())
                    .metricDataQueries(List.of(metricDataQuery))
                    .build();
            final var response = cloudWatchClient.getMetricData(request);
            final OptionalDouble maxValue = response.metricDataResults()
                    .stream()
                    .flatMap(result -> result.values().stream())
                    .mapToDouble(Double::doubleValue)
                    .max();
            return maxValue.isPresent() && maxValue.getAsDouble() == 0.0;
        });

        final Mqtt5BlockingClient mqttClient =
                Mqtt5Client.builder().serverHost(hivemq.getHost()).serverPort(hivemq.getMqttPort()).buildBlocking();
        mqttClient.connect();
        mqttClient.publishWith().topic("wabern").send();

        await().timeout(Durations.FIVE_MINUTES).until(() -> {
            final GetMetricDataRequest request = GetMetricDataRequest.builder()
                    .startTime(Instant.now().minusSeconds(3600))
                    .endTime(Instant.now())
                    .metricDataQueries(List.of(metricDataQuery))
                    .build();
            final var response = cloudWatchClient.getMetricData(request);
            final OptionalDouble maxValue = response.metricDataResults()
                    .stream()
                    .flatMap(result -> result.values().stream())
                    .mapToDouble(Double::doubleValue)
                    .max();
            return maxValue.isPresent() && maxValue.getAsDouble() == 1.0;
        });
        cloudWatchClient.close();
    }
}
