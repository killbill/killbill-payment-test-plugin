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
