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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.hivemq.extensions.aws.cloudwatch.configuration.entities.Config.DEF_REPORT_INTERVAL;
import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(configFile.toFile().createNewFile()).isTrue();

        final var extensionConfiguration = new ExtensionConfiguration(extensionDir);
        final var config = extensionConfiguration.getConfig();
        final var defaultConfig = new Config();
        assertThat(config.getApiTimeout()).isEqualTo(defaultConfig.getApiTimeout());
        assertThat(config.getReportInterval()).isEqualTo(defaultConfig.getReportInterval());
        assertThat(config.getMetrics()).isEqualTo(defaultConfig.getMetrics());
    }

    @Test
    void loadConfiguration_ok() throws IOException {
        Files.writeString(configFile, """
                <cloudwatch-extension-configuration>
                    <report-interval>10</report-interval>
                    <api-timeout>100</api-timeout>
                    <metrics>
                        <metric>com.hivemq.messages.incoming.total.count</metric>
                        <metric>com.hivemq.messages.outgoing.total.count</metric>
                        <metric enabled="false">com.hivemq.messages.incoming.total.rate</metric>
                    </metrics>
                </cloudwatch-extension-configuration>""");

        final var extensionConfiguration = new ExtensionConfiguration(extensionDir);
        final var config = extensionConfiguration.getConfig();
        assertThat(config.getReportInterval()).isEqualTo(10);
        assertThat(config.getApiTimeout()).hasValue(100);
        assertThat(config.getMetrics().size()).isEqualTo(3);
        assertThat(extensionConfiguration.getEnabledMetrics()).containsExactly(
                "com.hivemq.messages.incoming.total.count",
                "com.hivemq.messages.outgoing.total.count");
    }

    @Test
    void intervalConfigurationOK() throws IOException {
        Files.writeString(configFile, """
                <cloudwatch-extension-configuration>
                    <report-interval>30</report-interval>
                </cloudwatch-extension-configuration>""");

        final var config = new ExtensionConfiguration(extensionDir).getConfig();
        assertThat(config.getReportInterval()).isEqualTo(30);
        assertThat(config.getApiTimeout()).isEmpty();
        assertThat(config.getMetrics()).isEmpty();
    }

    @Test
    void intervalConfigurationNOK() throws IOException {
        Files.writeString(configFile, """
                <cloudwatch-extension-configuration>
                    <report-interval>0</report-interval>
                </cloudwatch-extension-configuration>""");

        final var config = new ExtensionConfiguration(extensionDir).getConfig();
        assertThat(config.getReportInterval()).isEqualTo(DEF_REPORT_INTERVAL);
        assertThat(config.getApiTimeout()).isEmpty();
        assertThat(config.getMetrics()).isEmpty();
    }

    @Test
    void timeoutConfigurationOK() throws IOException {
        Files.writeString(configFile, """
                <cloudwatch-extension-configuration>
                    <api-timeout>30</api-timeout>
                </cloudwatch-extension-configuration>""");

        final var config = new ExtensionConfiguration(extensionDir).getConfig();
        assertThat(config.getReportInterval()).isEqualTo(DEF_REPORT_INTERVAL);
        assertThat(config.getApiTimeout()).hasValue(30);
        assertThat(config.getMetrics()).isEmpty();
    }

    @Test
    void timeoutConfigurationNOK() throws IOException {
        Files.writeString(configFile, """
                <cloudwatch-extension-configuration>
                    <api-timeout>0</api-timeout>
                </cloudwatch-extension-configuration>""");

        final var config = new ExtensionConfiguration(extensionDir).getConfig();
        assertThat(config.getReportInterval()).isEqualTo(DEF_REPORT_INTERVAL);
        assertThat(config.getApiTimeout()).isEmpty();
        assertThat(config.getMetrics()).isEmpty();
    }
}
