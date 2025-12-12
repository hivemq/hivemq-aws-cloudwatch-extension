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

import com.codahale.metrics.MetricRegistry;
import com.hivemq.extension.sdk.api.services.ManagedExtensionExecutorService;
import com.hivemq.extensions.aws.cloudwatch.configuration.ExtensionConfiguration;
import com.hivemq.extensions.aws.cloudwatch.configuration.entities.Config;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CloudWatchReporterServiceTest {

    private final @NotNull ExtensionConfiguration extensionConfiguration = mock();
    private final @NotNull ManagedExtensionExecutorService extensionExecutorService = mock();
    private final @NotNull MetricRegistry metricRegistry = mock();

    private final @NotNull CloudWatchReporterService reporterService = new CloudWatchReporterService();

    @BeforeEach
    void setUp() {
        final var config = new Config();
        when(extensionConfiguration.getConfig()).thenReturn(config);

        System.setProperty("aws.region", "us-east-1");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("aws.region");
    }

    @Test
    void startCloudWatchReporter_whenConfiguredMetrics_thenStarted() {
        when(extensionConfiguration.getEnabledMetrics()).thenReturn(List.of("metric1", "metric2"));
        reporterService.startCloudWatchReporter(extensionConfiguration, extensionExecutorService, metricRegistry);
        assertNotNull(reporterService.getCloudWatchReporter(), "CloudWatchReporter is started");
        reporterService.stopCloudWatchReporter();
    }

    @Test
    void startCloudWatchReporter_whenEmptyMetrics_thenNotStarted() {
        when(extensionConfiguration.getEnabledMetrics()).thenReturn(List.of());
        reporterService.startCloudWatchReporter(extensionConfiguration, extensionExecutorService, metricRegistry);
        assertNull(reporterService.getCloudWatchReporter(), "CloudWatchReporter not started");
    }
}
