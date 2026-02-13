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

package com.hivemq.extensions.aws.cloudwatch.configuration.entities;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import org.jetbrains.annotations.NotNull;

/**
 * @author David Sondermann
 */
@XmlType(propOrder = {})
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "metric")
@SuppressWarnings("FieldMayBeFinal")
public class Metric {

    @XmlAttribute(name = "enabled")
    private boolean enabled = true;

    @XmlValue()
    private @NotNull String value = "";

    public boolean isEnabled() {
        return enabled;
    }

    public @NotNull String getValue() {
        return value;
    }

    @Override
    public @NotNull String toString() {
        return "Metric{" + "enabled=" + enabled + ", value='" + value + '\'' + '}';
    }
}
