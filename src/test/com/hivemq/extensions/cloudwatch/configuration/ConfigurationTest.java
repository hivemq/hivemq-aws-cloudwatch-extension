package com.hivemq.extensions.cloudwatch.configuration;

import com.hivemq.extensions.cloudwatch.configuration.entities.Config;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.security.sasl.SaslException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.hivemq.extensions.cloudwatch.configuration.entities.Config.DEF_CONNECTION_TIMEOUT;
import static com.hivemq.extensions.cloudwatch.configuration.entities.Config.DEF_REPORT_INTERVAL;
import static org.junit.Assert.assertEquals;

public class ConfigurationTest {

    private static String extensionContent =
            "<extension-configuration>\n" +
                    "    <reportInterval>10</reportInterval>\n" +
                    "    <connectionTimeout>100</connectionTimeout>\n" +
                    "    <metrics>\n" +
                    "        <metric>com.hivemq.messages.incoming.total.count</metric>\n" +
                    "        <metric>com.hivemq.messages.outgoing.total.count</metric>\n" +
                    "        <metric enabled=\"false\">com.hivemq.messages.incoming.total.rate</metric>\n" +
                    "    </metrics>\n" +
                    "</extension-configuration>";
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File root;
    private File file;

    @Before
    public void setUp() throws Exception {
        root = folder.getRoot();
        String fileName = "extension-config.xml";
        file = folder.newFile(fileName);
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
        assertEquals(config.getConnectionTimeout(), 100);
        assertEquals(config.getMetrics().size(), 3);
        assertEquals(extensionConfiguration.getEnabledMetrics().size(), 2);
    }


    @Test
    public void intervalConfigurationOK() throws IOException {
        String intervalConfig =
                "<extension-configuration>\n" +
                        "    <reportInterval>30</reportInterval>\n" + "</extension-configuration>";

        try {
            Files.writeString(file.toPath(), intervalConfig);
        } catch (SaslException e) {
            // expected
            ;
        }
        final Config config = new ExtensionConfiguration(root).getConfig();
        assertEquals(config.getReportInterval(), 30);
        assertEquals(config.getConnectionTimeout(), DEF_CONNECTION_TIMEOUT);
        assertEquals(config.getMetrics().size(), 0);
    }

    @Test
    public void intervalConfigurationNOK() throws IOException {
        String intervalConfig =
                "<extension-configuration>\n" +
                        "    <reportInterval>0</reportInterval>\n" + "</extension-configuration>";

        try {
            Files.writeString(file.toPath(), intervalConfig);
        } catch (SaslException e) {
            // expected
            ;
        }
        final Config config = new ExtensionConfiguration(root).getConfig();
        assertEquals(config.getReportInterval(), DEF_REPORT_INTERVAL);
        assertEquals(config.getConnectionTimeout(), DEF_CONNECTION_TIMEOUT);
        assertEquals(config.getMetrics().size(), 0);
    }


    @Test
    public void timeoutConfigurationOK() throws IOException {
        String intervalConfig =
                "<extension-configuration>\n" +
                        "    <connectionTimeout>30</connectionTimeout>\n" + "</extension-configuration>";

        try {
            Files.writeString(file.toPath(), intervalConfig);
        } catch (SaslException e) {
            // expected
            ;
        }
        final Config config = new ExtensionConfiguration(root).getConfig();
        assertEquals(config.getReportInterval(), DEF_REPORT_INTERVAL);
        assertEquals(config.getConnectionTimeout(), 30);
        assertEquals(config.getMetrics().size(), 0);
    }

    @Test
    public void timeoutConfigurationNOK() throws IOException {
        String intervalConfig =
                "<extension-configuration>\n" +
                        "    <connectionTimeout>0</connectionTimeout>\n" + "</extension-configuration>";

        try {
            Files.writeString(file.toPath(), intervalConfig);
        } catch (SaslException e) {
            // expected
            ;
        }
        final Config config = new ExtensionConfiguration(root).getConfig();
        assertEquals(config.getReportInterval(), DEF_REPORT_INTERVAL);
        assertEquals(config.getConnectionTimeout(), DEF_CONNECTION_TIMEOUT);
        assertEquals(config.getMetrics().size(), 0);
    }
}