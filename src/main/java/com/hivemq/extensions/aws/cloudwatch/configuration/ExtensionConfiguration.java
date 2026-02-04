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

package com.hivemq.extensions.aws.cloudwatch.configuration;

import com.hivemq.extensions.aws.cloudwatch.configuration.entities.Config;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author David Sondermann
 */
public class ExtensionConfiguration {

    private static final @NotNull Logger log = LoggerFactory.getLogger(ExtensionConfiguration.class);

    static final @NotNull String CONFIG_PATH = "conf/config.xml";
    static final @NotNull String LEGACY_CONFIG_PATH = "extension-config.xml";

    private final @NotNull ConfigurationXmlParser configurationXmlParser = new ConfigurationXmlParser();
    private final @NotNull ReadWriteLock lock = new ReentrantReadWriteLock();
    private final @NotNull Config config;

    private @NotNull List<String> enabledMetrics = new ArrayList<>();

    public ExtensionConfiguration(final @NotNull File extensionHomeFolder) {
        final var configResolver = new ConfigResolver(extensionHomeFolder.toPath(),
                "AWS CloudWatch Extension",
                CONFIG_PATH,
                LEGACY_CONFIG_PATH);
        config = read(configResolver.get().toFile());
    }

    public @NotNull Config getConfig() {
        return config;
    }

    /**
     * @param file the new config file to read.
     * @return the new config based on the file contents or null if the config is invalid
     */
    private @NotNull Config read(final @NotNull File file) {
        final var defaultConfig = new Config();
        if (file.exists() && file.canRead() && file.length() > 0) {
            return doRead(file, defaultConfig);
        } else {
            log.warn("Unable to read AWS CloudWatch metric extension configuration file {}, using defaults",
                    file.getAbsolutePath());
            return defaultConfig;
        }
    }

    private @NotNull Config doRead(final @NotNull File file, final @NotNull Config defaultConfig) {
        try {
            final var newConfig = configurationXmlParser.unmarshalExtensionConfig(file);
            if (newConfig.getApiTimeout().isPresent() && newConfig.getApiTimeout().get() < 1) {
                log.warn("Connection timeout must be greater than 0, using default timeout");
                newConfig.setApiTimeout(defaultConfig.getApiTimeout().orElse(null));
            }
            if (newConfig.getReportInterval() < 1) {
                log.warn("Report interval must be greater than 0, using default interval {}",
                        defaultConfig.getReportInterval());
                newConfig.setReportInterval(defaultConfig.getReportInterval());
            }
            return newConfig;
        } catch (final IOException e) {
            log.warn("Could not read extension configuration file, reason: {}, using defaults {} ",
                    e.getMessage(),
                    defaultConfig);
            return defaultConfig;
        }
    }

    public @NotNull List<String> getEnabledMetrics() {
        if (enabledMetrics.isEmpty()) {
            final var writeLock = lock.writeLock();
            writeLock.lock();
            try {
                enabledMetrics = readEnabledMetrics();
                log.debug("Enabled metrics loaded.");
            } finally {
                writeLock.unlock();
            }
        }
        return Collections.unmodifiableList(enabledMetrics);
    }

    private @NotNull List<String> readEnabledMetrics() {
        final var readLock = lock.readLock();
        readLock.lock();
        try {
            final var newMetrics = new ArrayList<String>();
            if (config.getMetrics().isEmpty()) {
                log.error("Could not find any enabled HiveMQ metrics in configuration, no metrics were reported. ");
                return List.of();
            }
            for (final var metric : config.getMetrics()) {
                if (metric.isEnabled() && !metric.getValue().isEmpty()) {
                    newMetrics.add(metric.getValue());
                    log.trace("Added HiveMQ metric {} ", metric.getValue());
                }
            }
            return List.copyOf(newMetrics);
        } finally {
            readLock.unlock();
        }
    }
}
