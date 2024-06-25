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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Anja Helmbrecht-Schaar
 */
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
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

    @XmlElementWrapper(name = "metrics")
    @XmlElement(name = "metric")
    private @NotNull List<Metric> metrics = new ArrayList<>();

    @XmlElement(name = "zero-values-submission", defaultValue = "false")
    private boolean zeroValuesSubmission = false;

    @XmlElement(name = "report-raw-count-value", defaultValue = "false")
    private boolean reportRawCountValue = false;

    @XmlElement(name = "cloudwatch-endpoint-override")
    private final @Nullable String cloudWatchEndpointOverride = null;

    public final @NotNull List<Metric> getMetrics() {
        return metrics;
    }

    public final int getReportInterval() {
        return reportInterval;
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

    public boolean getReportRawCountValue() {
        return reportRawCountValue;
    }

    public boolean getZeroValuesSubmission() {
        return zeroValuesSubmission;
    }

    public @Nullable String getCloudWatchEndpointOverride() {
        return cloudWatchEndpointOverride;
    }

    @Override
    public final @NotNull String toString() {
        return "Config{" +
                "reportInterval=" +
                reportInterval +
                ", apiTimeout=" +
                apiTimeout +
                ", metrics=" +
                metrics +
                '}';
    }
}
