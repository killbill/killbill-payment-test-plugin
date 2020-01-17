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