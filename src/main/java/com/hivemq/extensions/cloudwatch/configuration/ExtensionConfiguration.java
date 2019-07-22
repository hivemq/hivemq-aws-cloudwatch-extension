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

package com.hivemq.extensions.cloudwatch.configuration;

import com.google.common.collect.ImmutableList;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extensions.cloudwatch.configuration.entities.Config;
import com.hivemq.extensions.cloudwatch.configuration.entities.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ExtensionConfiguration {

    private static final @NotNull String EXTENSION_CONFIG_FILE_NAME = "extension-config.xml";
    private static final @NotNull Logger LOG = LoggerFactory.getLogger(ExtensionConfiguration.class);

    private final @NotNull ConfigurationXmlParser configurationXmlParser = new ConfigurationXmlParser();
    private final @NotNull ReadWriteLock lock = new ReentrantReadWriteLock();
    private final @NotNull Config config;
    private @NotNull List<String> enabledMetrics = new ArrayList<>();


    public ExtensionConfiguration(@NotNull final File extensionHomeFolder) {
        this.config = read(new File(extensionHomeFolder, EXTENSION_CONFIG_FILE_NAME));
    }

    @NotNull
    public Config getConfig() {
        return this.config;
    }

    /**
     * @param file the new config file to read.
     * @return the new config based on the file contents or null if the config is invalid
     */
    @NotNull
    private Config read(@NotNull final File file) {

        final @NotNull Config defaultConfig = new Config();

        if (file.exists() && file.canRead() && file.length() >0 ) {
            return doRead(file, defaultConfig);
        } else {
            LOG.warn("Unable to read CloudWatch metric extension configuration file {}, using defaults", file.getAbsolutePath());
            return defaultConfig;
        }
    }

    private Config doRead(@NotNull final File file, @NotNull final Config defaultConfig) {
        try {

            final @NotNull Config newConfig = configurationXmlParser.unmarshalExtensionConfig(file);
            if (newConfig.getConnectionTimeout() < 1) {
                LOG.warn("Connection timeout must be greater than 0, using default timeout " + defaultConfig.getConnectionTimeout());
                newConfig.setConnectionTimeout(defaultConfig.getConnectionTimeout());
            }

            if (newConfig.getReportInterval() < 1) {
                LOG.warn("Report interval must be greater than 0, using default interval " + defaultConfig.getReportInterval());
                newConfig.setReportInterval(defaultConfig.getReportInterval());
            }

            return newConfig;

        } catch (IOException e) {
            LOG.warn("Could not read extension configuration file, reason: {}, using defaults {} ", e.getMessage(), defaultConfig.toString());
            return defaultConfig;
        }
    }

    public List<String> getEnabledMetrics() {
        if (this.enabledMetrics == null || this.enabledMetrics.isEmpty()) {
            final Lock writeLock = lock.writeLock();
            try {
                writeLock.lock();
                this.enabledMetrics = readEnabledMetrics();
                LOG.debug("Enabled metrics loaded.");
            } finally {
                writeLock.unlock();
            }
        }
        return this.enabledMetrics;
    }

    private List<String> readEnabledMetrics() {
        final Lock readLock = this.lock.readLock();
        try {
            readLock.lock();
            final List<String> newMetrics = new ArrayList<>();

            if (this.config.getMetrics() == null || this.config.getMetrics().isEmpty()) {
                LOG.error("Could not find any enabled HiveMQ metrics in configuration, no metrics were reported. ");
                return ImmutableList.of();
            }
            for (Metric metric : this.config.getMetrics()) {
                if (metric.isEnabled() && !metric.getValue().isEmpty()) {
                    newMetrics.add(metric.getValue());
                    LOG.trace("Added HiveMQ metric {} ", metric.getValue());
                }
            }
            return ImmutableList.copyOf(newMetrics);
        } finally {
            readLock.unlock();
        }

    }

}
