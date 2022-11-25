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
package com.hivemq.extensions.cloudwatch.configuration;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extensions.cloudwatch.configuration.entities.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.hivemq.extensions.cloudwatch.configuration.entities.Config.DEF_REPORT_INTERVAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationTest {

    private @NotNull File extensionDir;
    private @NotNull Path configFile;

    @BeforeEach
    void setUp(final @NotNull @TempDir Path tempDir) {
        extensionDir = tempDir.toFile();
        configFile = tempDir.resolve("extension-config.xml");
    }

    @Test
    void defaultConfiguration_ok() throws IOException {
        assertTrue(configFile.toFile().createNewFile());

        final ExtensionConfiguration extensionConfiguration = new ExtensionConfiguration(extensionDir);
        final Config config = extensionConfiguration.getConfig();
        final Config defaultConfig = new Config();

        assertEquals(defaultConfig.getApiTimeout(), config.getApiTimeout());
        assertEquals(defaultConfig.getReportInterval(), config.getReportInterval());
        assertEquals(defaultConfig.getMetrics(), config.getMetrics());
    }

    @Test
    void loadConfiguration_ok() throws IOException {
        Files.writeString(configFile,
                "<cloudwatch-extension-configuration>\n" +
                        "    <report-interval>10</report-interval>\n" +
                        "    <api-timeout>100</api-timeout>\n" +
                        "    <metrics>\n" +
                        "        <metric>com.hivemq.messages.incoming.total.count</metric>\n" +
                        "        <metric>com.hivemq.messages.outgoing.total.count</metric>\n" +
                        "        <metric enabled=\"false\">com.hivemq.messages.incoming.total.rate</metric>\n" +
                        "    </metrics>\n" +
                        "</cloudwatch-extension-configuration>");

        final ExtensionConfiguration extensionConfiguration = new ExtensionConfiguration(extensionDir);
        final Config config = extensionConfiguration.getConfig();

        assertEquals(config.getReportInterval(), 10);
        assertEquals(Optional.of(100), config.getApiTimeout());
        assertEquals(3, config.getMetrics().size());
        assertEquals(List.of("com.hivemq.messages.incoming.total.count", "com.hivemq.messages.outgoing.total.count"),
                extensionConfiguration.getEnabledMetrics());
    }

    @Test
    void intervalConfigurationOK() throws IOException {
        Files.writeString(configFile,
                "<cloudwatch-extension-configuration>\n" +
                        "    <report-interval>30</report-interval>\n" +
                        "</cloudwatch-extension-configuration>");

        final Config config = new ExtensionConfiguration(extensionDir).getConfig();

        assertEquals(30, config.getReportInterval());
        assertEquals(Optional.empty(), config.getApiTimeout());
        assertEquals(List.of(), config.getMetrics());
    }

    @Test
    void intervalConfigurationNOK() throws IOException {
        Files.writeString(configFile,
                "<cloudwatch-extension-configuration>\n" +
                        "    <report-interval>0</report-interval>\n" +
                        "</cloudwatch-extension-configuration>");

        final Config config = new ExtensionConfiguration(extensionDir).getConfig();

        assertEquals(DEF_REPORT_INTERVAL, config.getReportInterval());
        assertEquals(Optional.empty(), config.getApiTimeout());
        assertEquals(List.of(), config.getMetrics());
    }

    @Test
    void timeoutConfigurationOK() throws IOException {
        Files.writeString(configFile,
                "<cloudwatch-extension-configuration>\n" +
                        "    <api-timeout>30</api-timeout>\n" +
                        "</cloudwatch-extension-configuration>");

        final Config config = new ExtensionConfiguration(extensionDir).getConfig();

        assertEquals(DEF_REPORT_INTERVAL, config.getReportInterval());
        assertEquals(Optional.of(30), config.getApiTimeout());
        assertEquals(List.of(), config.getMetrics());
    }

    @Test
    void timeoutConfigurationNOK() throws IOException {
        Files.writeString(configFile,
                "<cloudwatch-extension-configuration>\n" +
                        "    <api-timeout>0</api-timeout>\n" +
                        "</cloudwatch-extension-configuration>");

        final Config config = new ExtensionConfiguration(extensionDir).getConfig();

        assertEquals(DEF_REPORT_INTERVAL, config.getReportInterval());
        assertEquals(Optional.empty(), config.getApiTimeout());
        assertEquals(List.of(), config.getMetrics());
    }
}