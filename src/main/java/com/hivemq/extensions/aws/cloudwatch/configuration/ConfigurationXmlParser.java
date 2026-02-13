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

import com.hivemq.extension.sdk.api.annotations.ThreadSafe;
import com.hivemq.extensions.aws.cloudwatch.configuration.entities.Config;
import com.hivemq.extensions.aws.cloudwatch.configuration.entities.Metric;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author David Sondermann
 */
@ThreadSafe
class ConfigurationXmlParser {

    private static final @NotNull Logger log = LoggerFactory.getLogger(ConfigurationXmlParser.class);

    // JAXB context is thread safe
    private final @NotNull JAXBContext jaxb;

    ConfigurationXmlParser() {
        try {
            jaxb = JAXBContext.newInstance(Config.class, Metric.class);
        } catch (final JAXBException e) {
            log.error("Error in the AWS CloudWatch Extension. Could not initialize XML parser", e);
            throw new RuntimeException("Initialize XML parser Error", e);
        }
    }

    final @NotNull Config unmarshalExtensionConfig(final @NotNull File file) throws IOException {
        try {
            final var unmarshaller = jaxb.createUnmarshaller();
            return (Config) unmarshaller.unmarshal(file);
        } catch (final JAXBException e) {
            log.error("Error in the AWS CloudWatch Extension. Could not unmarshal XML configuration", e);
            throw new IOException("Could not unmarshal XML configuration Error", e);
        }
    }
}
