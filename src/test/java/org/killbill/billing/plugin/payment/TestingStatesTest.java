package org.killbill.billing.plugin.payment;


import org.testng.Assert;
import org.testng.annotations.Test;

public class TestingStatesTest {

    TestingStates testingStates = new TestingStates();

    @Test
    void testAddKnownMethod() {
        Assert.assertEquals(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED,
                                                   "authorizePayment"), true);
    }

    @Test
    void testAddUnknownMethod() {
        Assert.assertEquals(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED,
                                                   "dummy"), false);
    }
}