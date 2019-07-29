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

import com.amazonaws.ClientConfiguration;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "cloudwatch-extension-configuration")
@XmlType(propOrder = {})
@XmlAccessorType(XmlAccessType.NONE)
public class Config {

    public static final int DEF_REPORT_INTERVAL = 1;
    public static final int DEF_CONNECTION_TIMEOUT = ClientConfiguration.DEFAULT_CONNECTION_TIMEOUT;

    @XmlElement(name = "report-interval", required = true, defaultValue = "" + DEF_REPORT_INTERVAL)
    private int reportInterval = DEF_REPORT_INTERVAL;

    @XmlElement(name = "connection-timeout", required = true, defaultValue = "" + DEF_CONNECTION_TIMEOUT)
    private int connectionTimeout = DEF_CONNECTION_TIMEOUT;

    @XmlElementWrapper(name = "metrics")
    @XmlElement(name = "metric")
    private List<Metric> metrics = new ArrayList<>();

    public Config() {
    }

    public final List<Metric> getMetrics() {
        return metrics;
    }

    public final int getReportInterval() {
        return this.reportInterval;
    }

    public final void setReportInterval(int reportInterval) {
        this.reportInterval = reportInterval;
    }

    public final int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public final void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public final String toString() {
        return "Config{" +
                "reportInterval=" + reportInterval +
                ", connectionTimeout=" + connectionTimeout +
                ", metrics=" + metrics +
                '}';
    }
}
