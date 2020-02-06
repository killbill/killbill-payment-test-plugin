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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class PayloadTest {

    @Test
    public void testSleep() throws IOException {

        final ObjectMapper objectMapper = new ObjectMapper();

        final String json = "{\"CONFIGURE_ACTION\":\"action\", \"SLEEP_TIME_SEC\": 13}";

        final Payload payload = objectMapper.readValue(json, Payload.class);

        Assert.assertEquals("action", payload.getAction());
        Assert.assertEquals(13, payload.getSeepTime());
    }

    @Test
    public void testCreate() throws IOException {

        final ObjectMapper objectMapper = new ObjectMapper();

        final String json = "{\"CONFIGURE_ACTION\":\"action\"}";

        final Payload payload = objectMapper.readValue(json, Payload.class);

        Assert.assertEquals("action", payload.getAction());
    }

}