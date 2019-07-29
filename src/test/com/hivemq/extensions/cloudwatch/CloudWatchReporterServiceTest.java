package com.hivemq.extensions.cloudwatch;

import com.codahale.metrics.MetricRegistry;
import com.hivemq.extension.sdk.api.services.ManagedExtensionExecutorService;
import com.hivemq.extensions.cloudwatch.configuration.ExtensionConfiguration;
import com.hivemq.extensions.cloudwatch.configuration.entities.Config;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class CloudWatchReporterServiceTest {

    private ArrayList metrics = new ArrayList();
    private Config config = new Config();
    private CloudWatchReporterService reporterService = new CloudWatchReporterService();

    @Mock
    private ExtensionConfiguration extensionConfiguration;

    @Mock
    private ManagedExtensionExecutorService service;

    @Mock
    private MetricRegistry metricRegistry;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        metrics.add("metric1");
        metrics.add("metric2");
        when(extensionConfiguration.getConfig()).thenReturn(config);
        when(extensionConfiguration.getEnabledMetrics()).thenReturn(metrics);
    }

    @Test
    public void testStartCloudWatchReporter() {

        reporterService.startCloudWatchReporter(extensionConfiguration, service, metricRegistry);
        assertTrue("CloudWatchReporter is started", reporterService.getCloudWatchReporter() != null);
        reporterService.stopCloudWatchReporter();
    }

    @Test
    public void testStartCloudWatchReporterNotWhenEmptyMetrics() {
        when(extensionConfiguration.getEnabledMetrics()).thenReturn(new ArrayList<>());
        reporterService.startCloudWatchReporter(extensionConfiguration, service, metricRegistry);
        assertNull("CloudWatchReporter not started", reporterService.getCloudWatchReporter());
    }
}