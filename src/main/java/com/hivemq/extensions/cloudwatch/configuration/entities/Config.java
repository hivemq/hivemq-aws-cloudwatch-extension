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
package com.hivemq.extensions.cloudwatch.configuration.entities;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Anja Helmbrecht-Schaar
 */
@XmlRootElement(name = "cloudwatch-extension-configuration")
@XmlType(propOrder = {})
@XmlAccessorType(XmlAccessType.NONE)
public class Config {

    public static final int DEF_REPORT_INTERVAL = 1;
    public static final @Nullable Integer DEF_API_TIMEOUT = null;

    @XmlElement(name = "report-interval", required = true, defaultValue = "" + DEF_REPORT_INTERVAL)
    private int reportInterval = DEF_REPORT_INTERVAL;

    @XmlElement(name = "api-timeout")
    private @Nullable Integer apiTimeout = DEF_API_TIMEOUT;

    @SuppressWarnings("FieldMayBeFinal")
    @XmlElementWrapper(name = "metrics")
    @XmlElement(name = "metric")
    private @Nullable List<Metric> metrics = new ArrayList<>();


    public final @Nullable List<Metric> getMetrics() {
        return metrics;
    }

    public final int getReportInterval() {
        return this.reportInterval;
    }

    public final void setReportInterval(final int reportInterval) {
        this.reportInterval = reportInterval;
    }

    public final @NotNull Optional<Integer> getApiTimeout() {
        return Optional.ofNullable(apiTimeout);
    }

    public final void setApiTimeout(final @Nullable Integer apiTimeout) {
        this.apiTimeout = apiTimeout;
    }

    @Override
    public final @NotNull String toString() {
        return "Config{" +
                "reportInterval=" + reportInterval +
                ", connectionTimeout=" + apiTimeout +
                ", metrics=" + metrics +
                '}';
    }
}
