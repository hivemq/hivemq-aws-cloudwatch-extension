package com.hivemq.extensions.cloudwatch;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClient;
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

    private static @NotNull final String METRIC_NAMESPACE = "hivemq-metrics";
    private static @NotNull final Logger LOG = LoggerFactory.getLogger(CloudWatchReporterService.class);
    private @Nullable CloudWatchReporter cloudWatchReporter = null;

    CloudWatchReporter getCloudWatchReporter() {
        return cloudWatchReporter;
    }


    void startCloudWatchReporter(@NotNull final ExtensionConfiguration configuration,
                                 @NotNull final ManagedExtensionExecutorService executorService,
                                 @NotNull final MetricRegistry metricRegistry) {

        Preconditions.checkNotNull(configuration, "ExtensionConfiguration must not be null");
        Preconditions.checkNotNull(executorService, "ExecutorService must not be null");
        Preconditions.checkNotNull(metricRegistry, "MetricRegistry must not be null");

        @NotNull final Config cloudWatchConfig = configuration.getConfig();

        if (configuration.getEnabledMetrics().isEmpty()) {
            LOG.warn("No hiveMQ metrics enabled, no CloudWatch report started");
        } else {
            cloudWatchReporter = new CloudWatchReporter(
                    metricRegistry, METRIC_NAMESPACE,
                    new ConfiguredMetricsFilter(configuration.getEnabledMetrics()),
                    new AmazonCloudWatchAsyncClient(
                            new DefaultAWSCredentialsProviderChain(),
                            new ClientConfiguration().withConnectionTimeout(cloudWatchConfig.getConnectionTimeout()),
                            executorService)
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

    private class ConfiguredMetricsFilter implements MetricFilter {
        private final Collection<String> metrics;

        ConfiguredMetricsFilter(@NotNull final Collection<String> metrics) {
            this.metrics = metrics;
            Preconditions.checkNotNull(metrics, "Cloud Metrics must not be null");
        }

        @Override
        public boolean matches(final String name, final Metric metric) {
            return metrics.contains(name);
        }
    }

}
