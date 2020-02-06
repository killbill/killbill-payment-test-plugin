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

    @Test
    void clearAllStates() {
        Assert.assertEquals(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED,
                                                   "authorizePayment"), true);
        Assert.assertEquals(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED,
                                                   "capturePayment"), true);
        Assert.assertEquals(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED,
                                                   "voidPayment", 60), true);

        Assert.assertEquals(this.testingStates.getSleeps().size(), 1);
        Assert.assertEquals(this.testingStates.getStates().size(), 2);


        Assert.assertEquals(this.testingStates.add(TestingStates.Actions.ACTION_CLEAR, null), true);
        Assert.assertEquals(this.testingStates.getSleeps().size(), 0);
        Assert.assertEquals(this.testingStates.getStates().size(), 0);
    }

    @Test
    void clearOneState() {
        Assert.assertEquals(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED,
                                                   "authorizePayment"), true);
        Assert.assertEquals(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_PENDING,
                                                   "capturePayment"), true);

        Assert.assertEquals(this.testingStates.getStates().size(), 2);


        Assert.assertEquals(this.testingStates.add(TestingStates.Actions.ACTION_CLEAR, "capturePayment"), true);
        Assert.assertEquals(this.testingStates.getStates().size(), 1);
        Assert.assertTrue(this.testingStates.getStates()
                                            .containsValue(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED));

    }
}