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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder;
import com.blacklocus.metrics.CloudWatchReporter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.services.ManagedExtensionExecutorService;
import com.hivemq.extensions.cloudwatch.configuration.ExtensionConfiguration;
import com.hivemq.extensions.cloudwatch.configuration.entities.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

class CloudWatchReporterService {

    private static final @NotNull Logger LOG = LoggerFactory.getLogger(CloudWatchReporterService.class);

    private static final @NotNull String METRIC_NAMESPACE = "hivemq-metrics";

    private @Nullable CloudWatchReporter cloudWatchReporter = null;

    public @Nullable CloudWatchReporter getCloudWatchReporter() {
        return cloudWatchReporter;
    }

    void startCloudWatchReporter(final @NotNull ExtensionConfiguration configuration,
                                 final @NotNull ManagedExtensionExecutorService executorService,
                                 final @NotNull MetricRegistry metricRegistry) {

        Preconditions.checkNotNull(configuration, "ExtensionConfiguration must not be null");
        Preconditions.checkNotNull(executorService, "ExecutorService must not be null");
        Preconditions.checkNotNull(metricRegistry, "MetricRegistry must not be null");

        final Config cloudWatchConfig = configuration.getConfig();

        if (configuration.getEnabledMetrics().isEmpty()) {
            LOG.warn("No hiveMQ metrics enabled, no CloudWatch report started");
        } else {
            final AmazonCloudWatchAsync cloudWatchAsync = AmazonCloudWatchAsyncClientBuilder
                    .standard()
                    .withCredentials(new DefaultAWSCredentialsProviderChain())
                    .withClientConfiguration(new ClientConfiguration()
                            .withConnectionTimeout(cloudWatchConfig.getConnectionTimeout()))
                    .withExecutorFactory(() -> executorService)
                    .build();

            cloudWatchReporter = new CloudWatchReporter(
                    metricRegistry, METRIC_NAMESPACE,
                    new ConfiguredMetricsFilter(configuration.getEnabledMetrics()),
                    cloudWatchAsync
            );
            cloudWatchReporter.start(cloudWatchConfig.getReportInterval(), TimeUnit.MINUTES);
            LOG.info("Started CloudWatchReporter for {} HiveMQ metrics", configuration.getEnabledMetrics().size());
        }
    }

    void stopCloudWatchReporter() {
        if (cloudWatchReporter != null) {
            cloudWatchReporter.stop();
            LOG.info("Stopped CloudWatchReporter");
        }
    }

    private static class ConfiguredMetricsFilter implements MetricFilter {
        private final @NotNull Collection<String> metrics;

        ConfiguredMetricsFilter(final @NotNull Collection<String> metrics) {
            this.metrics = metrics;
            Preconditions.checkNotNull(metrics, "Cloud Metrics must not be null");
        }

        @Override
        public boolean matches(final @Nullable String name, final @Nullable Metric metric) {
            return metrics.contains(name);
        }
    }

}
