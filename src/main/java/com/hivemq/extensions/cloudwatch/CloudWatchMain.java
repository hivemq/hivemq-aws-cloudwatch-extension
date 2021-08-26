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
import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartOutput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopOutput;
import com.hivemq.extension.sdk.api.services.ManagedExtensionExecutorService;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extensions.cloudwatch.configuration.ExtensionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Main class for HiveMQ AWS CloudWatch extension
 * <p>
 * After HiveMQ is started, the configured HiveMQ metrics will be exposed to Amazon AWS - CloudWatch
 *
 * @author Anja Helmbrecht-Schaar
 */
public class CloudWatchMain implements ExtensionMain {

    private static final @NotNull Logger log = LoggerFactory.getLogger(CloudWatchMain.class);

    private final @NotNull ManagedExtensionExecutorService service = Services.extensionExecutorService();
    private final @NotNull MetricRegistry metricRegistry = Services.metricRegistry();
    private final @NotNull CloudWatchReporterService reporterService = new CloudWatchReporterService();

    @Override
    public final void extensionStart(
            final @NotNull ExtensionStartInput extensionStartInput,
            final @NotNull ExtensionStartOutput extensionStartOutput) {

        try {
            final File extensionHomeFolder = extensionStartInput.getExtensionInformation().getExtensionHomeFolder();
            final ExtensionConfiguration extensionConfiguration = new ExtensionConfiguration(extensionHomeFolder);
            reporterService.startCloudWatchReporter(extensionConfiguration, service, metricRegistry);
            log.info("Start {}", extensionStartInput.getExtensionInformation().getName());
        } catch (final Exception e) {
            extensionStartOutput.preventExtensionStartup("Extension cannot be started due to errors. ");
            log.error("Exception for {} thrown at start: ", extensionStartInput.getExtensionInformation().getName(), e);
        }
    }

    @Override
    public final void extensionStop(
            final @NotNull ExtensionStopInput extensionStopInput,
            final @NotNull ExtensionStopOutput extensionStopOutput) {

        reporterService.stopCloudWatchReporter();
        log.info("Stop {}", extensionStopInput.getExtensionInformation().getName());
    }
}
