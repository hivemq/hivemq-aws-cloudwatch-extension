
/*
 * Copyright 2019 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
 * Main class for HiveMQ CloudWatch extension
 * <p>
 * After HiveMQ is started, the configured HiveMQ metrics will be exposed to amazon aws -  cloudwatch
 *
 * @Author Anja Helmbrecht-Schaar
 */

public class CloudWatchMain implements ExtensionMain {

    private static @NotNull
    final Logger LOG = LoggerFactory.getLogger(CloudWatchMain.class);
    private final ManagedExtensionExecutorService service = Services.extensionExecutorService();
    private final MetricRegistry metricRegistry = Services.metricRegistry();
    private @NotNull
    final CloudWatchReporterService reporterService = new CloudWatchReporterService();

    @Override
    public final void extensionStart(@NotNull final ExtensionStartInput extensionStartInput,
                                     @NotNull final ExtensionStartOutput extensionStartOutput) {
        try {
            final File extensionHomeFolder = extensionStartInput.getExtensionInformation().getExtensionHomeFolder();
            final ExtensionConfiguration extensionConfiguration = new ExtensionConfiguration(extensionHomeFolder);
            reporterService.startCloudWatchReporter(extensionConfiguration, service, metricRegistry);

        } catch (Exception e) {
            LOG.error("Exception for {} thrown at start: ", extensionStartInput.getExtensionInformation().getName(), e);
        }
    }

    @Override
    public final void extensionStop(@NotNull final ExtensionStopInput extensionStopInput,
                                    @NotNull final ExtensionStopOutput extensionStopOutput) {
        reporterService.stopCloudWatchReporter();
        LOG.info("Stop {}", extensionStopInput.getExtensionInformation().getName());
    }

}
