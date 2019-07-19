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

package com.hivemq.extensions.cloudwatch.configuration.entities;

import com.hivemq.extension.sdk.api.annotations.NotNull;

import javax.xml.bind.annotation.*;

@XmlType(propOrder = {})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "metric")
public class Metric {

    @XmlAttribute(name = "enabled")
    private boolean enabled = true;

    @NotNull
    @XmlValue()
    private String value = "";

    public Metric() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Metric{" +
                "enabled=" + enabled +
                ", value='" + value + '\'' +
                '}';
    }
}
