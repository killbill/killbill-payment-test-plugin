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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class TestingStates {

    public static final String SLEEP_PLUGIN_CONFIG_PARAM = "sleepFor";
    public static final String AMOUNT_PLUGIN_CONFIG_PARAM = "amount";
    private final Set<String> allowedMethods;

    public enum Actions {
        ACTION_RETURN_PLUGIN_STATUS_PENDING,
        ACTION_RETURN_PLUGIN_STATUS_ERROR,
        ACTION_RETURN_PLUGIN_STATUS_CANCELED,
        ACTION_THROW_EXCEPTION,
        RETURN_NIL,
        ACTION_SLEEP,
        ACTION_CLEAR
    }

    // key is method, "*" is for any method
    @JsonSerialize
    private final Map<String, Actions> states;

    // key is method, "*" is for any method
    @JsonSerialize
    private final Map<String, Integer> sleeps;
    
    // key is method, "*" is for any method
    @JsonSerialize
    private final Map<String, BigDecimal> amounts;    

    public TestingStates() {
        this.states = new HashMap<>();
        this.sleeps = new HashMap<>();
        this.amounts = new HashMap<>();

        final Class cls = PaymentTestPluginApi.class;
        final Method[] methods = cls.getMethods();
        // TODO: remove method like hashCode, notify, toString ...
        this.allowedMethods = Arrays.stream(methods).map(Method::getName).collect(Collectors.toSet());
    }

    public boolean add(final Actions action, @Nullable final String forMethod) {

        if (forMethod != null) {
            if (this.allowedMethods.contains(forMethod) == false) {
                return false;
            }
        }
        
        if (action == null) {
        	return false;
        }
        
        if (action.compareTo(Actions.ACTION_CLEAR) == 0) {
            if (forMethod == null) {
                this.states.clear();
                this.sleeps.clear();
                this.amounts.clear();
            }
            else {
                this.states.remove(forMethod);
                this.sleeps.remove(forMethod);
                this.amounts.remove(forMethod);
            }
        }
        else {
            final String method = (forMethod != null) ? forMethod : "*";
            this.states.put(method, action);
        }
        return true;
    }
    
    public boolean add(final Actions action, @Nullable final String forMethod, final int sleep, final BigDecimal amount) {

        if (forMethod != null) {
            if (this.allowedMethods.contains(forMethod) == false) {
                return false;
            }
        }
        final String method = (forMethod != null) ? forMethod : "*";
        if(action != null) {
        	this.add(action, forMethod);
        }
        if(sleep != 0) {
        	this.sleeps.put(method, sleep);
        }
        if (amount != null) {
        	this.amounts.put(method, amount);
        }
        
        
        return true;
    }    

    public Map<String, Actions> getStates() {
        return this.states;
    }

    public Map<String, Integer> getSleeps() {
        return this.sleeps;
    }
    
    public Map<String, BigDecimal> getAmounts() {
        return this.amounts;
    }
}
