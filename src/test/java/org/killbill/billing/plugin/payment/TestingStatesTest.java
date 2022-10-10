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


import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestingStatesTest {

    private final TestingStates testingStates = new TestingStates();
    
    @BeforeMethod
    void clearAll(){
    	Assert.assertTrue(this.testingStates.add(TestingStates.Actions.ACTION_CLEAR, null));
    }

    @Test
    void testAddKnownMethod() {
		Assert.assertTrue(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED,
												 "authorizePayment"));
    }
    
    @Test
    void testAddSleepTimeOnly() {
		Assert.assertTrue(this.testingStates.add(null,"authorizePayment", 60, null));
		
		Assert.assertEquals(this.testingStates.getSleeps().get("authorizePayment").compareTo(60), 0);
		Assert.assertNull(this.testingStates.getStates().get("authorizePayment"));
		Assert.assertNull(this.testingStates.getAmounts().get("authorizePayment"));
		
    }    
    
    @Test
    void testAddAmountOnlyWithoutMethodName() {
		Assert.assertTrue(this.testingStates.add(null, null, 0, BigDecimal.TEN));
		
		Assert.assertNull(this.testingStates.getSleeps().get("authorizePayment"));
		Assert.assertNull(this.testingStates.getStates().get("authorizePayment"));
		Assert.assertNull(this.testingStates.getAmounts().get("authorizePayment"));
		Assert.assertEquals(this.testingStates.getAmounts().get("*").compareTo(BigDecimal.TEN),0);
    }     
    
    @Test
    void testAddSleepTimeAndAmountWithoutState() {
		Assert.assertTrue(this.testingStates.add(null,
												  "authorizePayment", 60, BigDecimal.TEN));
		
		Assert.assertEquals(this.testingStates.getSleeps().get("authorizePayment").compareTo(60), 0);
		Assert.assertEquals(this.testingStates.getAmounts().get("authorizePayment").compareTo(BigDecimal.TEN),0);
		Assert.assertNull(this.testingStates.getStates().get("authorizePayment"));
		
    }     
    
    @Test
    void testAddSleepTimeAndStateWithoutAmount() {
		Assert.assertTrue(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED,
												  "authorizePayment", 60, null));
		
		Assert.assertEquals(this.testingStates.getStates().get("authorizePayment").compareTo(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED), 0);
		Assert.assertEquals(this.testingStates.getSleeps().get("authorizePayment").compareTo(60), 0);
		Assert.assertNull(this.testingStates.getAmounts().get("authorizePayment"));
		
    }   
    
    @Test
    void testAddAmountSleepTimeAndState() {
		Assert.assertTrue(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED, "authorizePayment", 60, BigDecimal.TEN));
		
		Assert.assertEquals(this.testingStates.getStates().get("authorizePayment").compareTo(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED), 0);
		Assert.assertEquals(this.testingStates.getSleeps().get("authorizePayment").compareTo(60), 0);
		Assert.assertEquals(this.testingStates.getAmounts().get("authorizePayment").compareTo(BigDecimal.TEN),0);
		
    }    
 
    @Test
    void testAddUnknownMethod() {
		Assert.assertFalse(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED,
												  "dummy"));
    }
    
  

    @Test
    void clearAllStates() {
		Assert.assertTrue(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED,
												 "authorizePayment"));
		Assert.assertTrue(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED,
												 "capturePayment"));
		Assert.assertTrue(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED,
												 "voidPayment", 60, null));

        Assert.assertEquals(this.testingStates.getSleeps().size(), 1);
        Assert.assertEquals(this.testingStates.getStates().size(), 3);


		Assert.assertTrue(this.testingStates.add(TestingStates.Actions.ACTION_CLEAR, null));
        Assert.assertEquals(this.testingStates.getSleeps().size(), 0);
        Assert.assertEquals(this.testingStates.getStates().size(), 0);
    }

    @Test
    void clearOneState() {
		Assert.assertTrue(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED,
												 "authorizePayment"));
		Assert.assertTrue(this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_PENDING,
												 "capturePayment"));

        Assert.assertEquals(this.testingStates.getStates().size(), 2);


		Assert.assertTrue(this.testingStates.add(TestingStates.Actions.ACTION_CLEAR, "capturePayment"));
        Assert.assertEquals(this.testingStates.getStates().size(), 1);
        Assert.assertTrue(this.testingStates.getStates()
                                            .containsValue(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED));

    }
}