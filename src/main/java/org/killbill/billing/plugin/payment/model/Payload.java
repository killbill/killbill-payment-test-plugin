/*
 * Copyright 2014-2020 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.payment.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;


@JsonInclude(NON_ABSENT)
public class Payload {

    private final String action;
    private final String methods;
    private final int    sleepTime;

    @JsonCreator
    public Payload(@JsonProperty("CONFIGURE_ACTION") final String action,
                   @JsonProperty("METHODS") final String methods,
                   @JsonProperty("SLEEP_TIME_SEC") final int sleepTime) {
        this.action = action;
        this.methods = methods;
        this.sleepTime = sleepTime;
    }

    public String getAction() {
        return this.action;
    }

    public String getMethods() { return this.methods;}

    public int getSeepTime() {
        return this.sleepTime;
    }
}
