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

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.services.ManagedExtensionExecutorService;
import com.hivemq.extensions.cloudwatch.configuration.ExtensionConfiguration;
import com.hivemq.extensions.cloudwatch.configuration.entities.Config;
import io.github.azagniotov.metrics.reporter.cloudwatch.CloudWatchReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientAsyncConfiguration;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.client.config.SdkAdvancedAsyncClientOption;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @author Anja Helmbrecht-Schaar
 */
class CloudWatchReporterService {

    private static final @NotNull Logger log = LoggerFactory.getLogger(CloudWatchReporterService.class);

    private static final @NotNull String METRIC_NAMESPACE = "hivemq-metrics";

    private @Nullable CloudWatchReporter cloudWatchReporter;

    public @Nullable CloudWatchReporter getCloudWatchReporter() {
        return cloudWatchReporter;
    }

    void startCloudWatchReporter(
            final @NotNull ExtensionConfiguration configuration,
            final @NotNull ManagedExtensionExecutorService executorService,
            final @NotNull MetricRegistry metricRegistry) {

        final Config cloudWatchConfig = configuration.getConfig();

        if (configuration.getEnabledMetrics().isEmpty()) {
            log.warn("No HiveMQ metrics enabled, no AWS CloudWatch report started");
        } else {
            final Duration apiTimeout = cloudWatchConfig.getApiTimeout().map(Duration::ofMillis).orElse(null);

            final CloudWatchAsyncClient cloudWatchAsync = CloudWatchAsyncClient.builder()
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .asyncConfiguration(ClientAsyncConfiguration.builder()
                            .advancedOption(SdkAdvancedAsyncClientOption.FUTURE_COMPLETION_EXECUTOR, executorService)
                            .build())
                    .overrideConfiguration(ClientOverrideConfiguration.builder()
                            .apiCallTimeout(apiTimeout)
                            .apiCallAttemptTimeout(apiTimeout)
                            .build())
                    .build();

            cloudWatchReporter = CloudWatchReporter.forRegistry(metricRegistry, cloudWatchAsync, METRIC_NAMESPACE)
                    .filter(new ConfiguredMetricsFilter(configuration.getEnabledMetrics()))
                    .build();
            cloudWatchReporter.start(cloudWatchConfig.getReportInterval(), TimeUnit.MINUTES);
            log.info("Started CloudWatchReporter for {} HiveMQ metrics", configuration.getEnabledMetrics().size());
        }
    }

    void stopCloudWatchReporter() {
        if (cloudWatchReporter != null) {
            cloudWatchReporter.stop();
            log.info("Stopped CloudWatchReporter");
        }
    }

    private static class ConfiguredMetricsFilter implements MetricFilter {

        private final @NotNull Collection<String> metrics;

        ConfiguredMetricsFilter(final @NotNull Collection<String> metrics) {
            this.metrics = metrics;
        }

        @Override
        public boolean matches(final @Nullable String name, final @Nullable Metric metric) {
            return metrics.contains(name);
        }
    }
}
