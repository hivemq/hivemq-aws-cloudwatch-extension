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
package com.hivemq.extensions.cloudwatch;

import com.codahale.metrics.MetricRegistry;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.services.ManagedExtensionExecutorService;
import com.hivemq.extensions.cloudwatch.configuration.ExtensionConfiguration;
import com.hivemq.extensions.cloudwatch.configuration.entities.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CloudWatchReporterServiceTest {

    private final @NotNull CloudWatchReporterService reporterService = new CloudWatchReporterService();

    private @NotNull ExtensionConfiguration extensionConfiguration;
    private @NotNull ManagedExtensionExecutorService service;
    private @NotNull MetricRegistry metricRegistry;

    @BeforeEach
    public void setUp() {
        extensionConfiguration = mock(ExtensionConfiguration.class);
        service = mock(ManagedExtensionExecutorService.class);
        metricRegistry = mock(MetricRegistry.class);

        System.setProperty("aws.region", "us-east-1");

        final List<String> metrics = new ArrayList<>();
        final Config config = new Config();

        metrics.add("metric1");
        metrics.add("metric2");
        when(extensionConfiguration.getConfig()).thenReturn(config);
        when(extensionConfiguration.getEnabledMetrics()).thenReturn(metrics);
    }

    @Test
    public void testStartCloudWatchReporter() {
        reporterService.startCloudWatchReporter(extensionConfiguration, service, metricRegistry);
        assertNotNull(reporterService.getCloudWatchReporter(), "CloudWatchReporter is started");
        reporterService.stopCloudWatchReporter();
    }

    @Test
    public void testStartCloudWatchReporterNotWhenEmptyMetrics() {
        when(extensionConfiguration.getEnabledMetrics()).thenReturn(new ArrayList<>());
        reporterService.startCloudWatchReporter(extensionConfiguration, service, metricRegistry);
        assertNull(reporterService.getCloudWatchReporter(), "CloudWatchReporter not started");
    }

    @AfterEach
    public void cleanUp() {
        System.clearProperty("aws.region");
    }
}