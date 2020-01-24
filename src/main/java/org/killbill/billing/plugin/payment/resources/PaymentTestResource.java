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

        if (payload.getSeepTime() != 0) {
            added = this.testingStates.add(TestingStates.Actions.valueOf(payload.getAction()),
                                           payload.getMethods(),
                                           payload.getSeepTime());
        }
        else {
            added = this.testingStates.add(TestingStates.Actions.valueOf(payload.getAction()),
                                           payload.getMethods());
        }

        if (!added) {
            // TODO: add reason in error body
            return Results.with("Error adding").status(Status.BAD_REQUEST);
        }
        return Results.with(Status.OK);
    }
}
