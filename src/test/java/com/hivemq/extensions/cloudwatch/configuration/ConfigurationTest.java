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

import javax.security.sasl.SaslException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.hivemq.extensions.cloudwatch.configuration.entities.Config.DEF_CONNECTION_TIMEOUT;
import static com.hivemq.extensions.cloudwatch.configuration.entities.Config.DEF_REPORT_INTERVAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurationTest {

    private static final @NotNull String extensionContent =
            "<cloudwatch-extension-configuration>\n" +
                    "    <report-interval>10</report-interval>\n" +
                    "    <connection-timeout>100</connection-timeout>\n" +
                    "    <metrics>\n" +
                    "        <metric>com.hivemq.messages.incoming.total.count</metric>\n" +
                    "        <metric>com.hivemq.messages.outgoing.total.count</metric>\n" +
                    "        <metric enabled=\"false\">com.hivemq.messages.incoming.total.rate</metric>\n" +
                    "    </metrics>\n" +
                    "</cloudwatch-extension-configuration>";
    private File root;
    private File file;

    @BeforeEach
    public void setUp(final @NotNull @TempDir Path tempDir) throws Exception {
        root = tempDir.toFile();
        String fileName = "extension-config.xml";
        file = tempDir.resolve(fileName).toFile();
        file.createNewFile();
    }

    @Test
    public void defaultConfiguration_ok() {
        final ExtensionConfiguration extensionConfiguration = new ExtensionConfiguration(root);
        final Config config = extensionConfiguration.getConfig();
        final Config defaultConfig = new Config();

        assertEquals(config.getConnectionTimeout(), defaultConfig.getConnectionTimeout());
        assertEquals(config.getReportInterval(), defaultConfig.getReportInterval());
        assertEquals(config.getMetrics().size(), 0);
    }

    @Test
    public void loadConfiguration_ok() throws IOException {
        Files.writeString(file.toPath(), extensionContent);
        final ExtensionConfiguration extensionConfiguration = new ExtensionConfiguration(root);
        final Config config = extensionConfiguration.getConfig();

        assertEquals(config.getReportInterval(), 10);
        assertTrue(config.getConnectionTimeout().isPresent());
        assertEquals(config.getConnectionTimeout().get(), 100);
        assertEquals(config.getMetrics().size(), 3);
        assertEquals(extensionConfiguration.getEnabledMetrics().size(), 2);
    }


    @Test
    public void intervalConfigurationOK() throws IOException {
        final String intervalConfig =
                "<cloudwatch-extension-configuration>\n" +
                        "    <report-interval>30</report-interval>\n" +
                        "</cloudwatch-extension-configuration>";

        try {
            Files.writeString(file.toPath(), intervalConfig);
        } catch (final SaslException e) {
            // expected
        }
        final Config config = new ExtensionConfiguration(root).getConfig();
        assertEquals(config.getReportInterval(), 30);
        assertTrue(config.getConnectionTimeout().isEmpty());
        assertEquals(config.getMetrics().size(), 0);
    }

    @Test
    public void intervalConfigurationNOK() throws IOException {
        final String intervalConfig =
                "<cloudwatch-extension-configuration>\n" +
                        "    <report-interval>0</report-interval>\n" +
                        "</cloudwatch-extension-configuration>";

        try {
            Files.writeString(file.toPath(), intervalConfig);
        } catch (final SaslException e) {
            // expected
        }
        final Config config = new ExtensionConfiguration(root).getConfig();
        assertEquals(config.getReportInterval(), DEF_REPORT_INTERVAL);
        assertTrue(config.getConnectionTimeout().isEmpty());
        assertEquals(config.getMetrics().size(), 0);
    }


    @Test
    public void timeoutConfigurationOK() throws IOException {
        final String intervalConfig =
                "<cloudwatch-extension-configuration>\n" +
                        "    <connection-timeout>30</connection-timeout>\n" +
                        "</cloudwatch-extension-configuration>";

        try {
            Files.writeString(file.toPath(), intervalConfig);
        } catch (SaslException e) {
            // expected
        }
        final Config config = new ExtensionConfiguration(root).getConfig();
        assertEquals(config.getReportInterval(), DEF_REPORT_INTERVAL);
        assertTrue(config.getConnectionTimeout().isPresent());
        assertEquals(config.getConnectionTimeout().get(), 30);
        assertEquals(config.getMetrics().size(), 0);
    }

    @Test
    public void timeoutConfigurationNOK() throws IOException {
        final String intervalConfig =
                "<cloudwatch-extension-configuration>\n" +
                        "    <connection-timeout>0</connection-timeout>\n" +
                        "</cloudwatch-extension-configuration>";

        try {
            Files.writeString(file.toPath(), intervalConfig);
        } catch (final SaslException e) {
            // expected
        }
        final Config config = new ExtensionConfiguration(root).getConfig();
        assertEquals(config.getReportInterval(), DEF_REPORT_INTERVAL);
        assertTrue(config.getConnectionTimeout().isEmpty());
        assertEquals(config.getMetrics().size(), 0);
    }
}