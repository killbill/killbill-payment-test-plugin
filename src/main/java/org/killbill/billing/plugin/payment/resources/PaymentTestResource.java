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

package org.killbill.billing.plugin.payment.resources;

import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.Body;
import org.jooby.mvc.GET;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;
import org.killbill.billing.plugin.payment.TestingStates;
import org.killbill.billing.plugin.payment.model.Payload;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Path("/")
public class PaymentTestResource {

    private final TestingStates testingStates;

    @Inject
    public PaymentTestResource(final TestingStates testingStates) {
        this.testingStates = testingStates;
    }

    @Path("status")
    @GET
    public Result status() {
        return Results.with(this.testingStates, Status.OK);
    }

    @Path("configure")
    @POST
    public Result configure(@Body final Payload payload) {

        final boolean added;
        final TestingStates.Actions action = payload.getAction() != null ? TestingStates.Actions.valueOf(payload.getAction()) : null;
        
        if (payload.getSleepTime() == 0 && payload.getAmount() == null) {
        	 added = this.testingStates.add(action,
                     payload.getMethods());
        } else {
        	
        	added = this.testingStates.add(action, 
					   payload.getMethods(), 
					   payload.getSleepTime(),
					   payload.getAmount());
        }

        if (!added) {
            // TODO: add reason in error body
            return Results.with("Error adding").status(Status.BAD_REQUEST);
        }
        return Results.with(Status.OK);
    }
}
