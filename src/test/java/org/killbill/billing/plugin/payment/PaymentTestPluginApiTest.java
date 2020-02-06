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

import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillLogService;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.payment.plugin.api.PaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.api.PluginCallContext;
import org.killbill.billing.plugin.api.payment.PluginPaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.payment.dao.PaymentTestDao;
import org.killbill.clock.Clock;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;

import static org.killbill.billing.plugin.payment.PaymentTestActivator.PLUGIN_NAME;

public class PaymentTestPluginApiTest {

    @Mock
    OSGIKillbillAPI             killbillAPI;
    @Mock
    OSGIConfigPropertiesService configProperties;
    @Mock
    OSGIKillbillLogService      logService;
    @Mock
    Clock                       clock;
    @Mock
    PaymentTestDao              paymentTestDao;


    TestingStates        testingStates;
    PaymentTestPluginApi paymentTestPugin;
    PluginCallContext    pluginCallContext;
    UUID                 accountId;
    UUID                 tenantId;

    @BeforeMethod
    public void setUp() throws SQLException {

        MockitoAnnotations.initMocks(this);
        Mockito.doNothing().when(this.paymentTestDao).addPaymentResponse(Mockito.any(UUID.class),
                                                                         Mockito.any(UUID.class),
                                                                         Mockito.any(
                                                                                 PluginPaymentTransactionInfoPlugin.class));

        this.tenantId = UUID.randomUUID();
        this.accountId = UUID.randomUUID();
        this.pluginCallContext = new PluginCallContext(PLUGIN_NAME, DateTime.now(), this.accountId, this.tenantId);
        this.testingStates = new TestingStates();
        this.paymentTestPugin = new PaymentTestPluginApi(this.killbillAPI,
                                                         this.configProperties,
                                                         this.logService,
                                                         this.clock,
                                                         this.paymentTestDao,
                                                         this.testingStates);
    }

    @AfterMethod
    public void tearDown() {
    }

    @BeforeMethod
    void beforeMethod() {
        this.testingStates = new TestingStates();
    }

    @Test
    public void regularProcess() throws PaymentPluginApiException {
        // no global config
        final PaymentTransactionInfoPlugin ret = this.paymentTestPugin.authorizePayment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                null,
                this.pluginCallContext);

        Assert.assertEquals(ret.getStatus(), PaymentPluginStatus.PROCESSED);
    }

    @Test
    public void returnNull() throws PaymentPluginApiException {
        this.testingStates.add(TestingStates.Actions.RETURN_NIL, null);

        final PaymentTransactionInfoPlugin ret = this.paymentTestPugin.authorizePayment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                null,
                this.pluginCallContext);

        Assert.assertNull(ret);
    }

    @Test(expectedExceptions = PaymentPluginApiException.class)
    public void throwException() throws PaymentPluginApiException {
        this.testingStates.add(TestingStates.Actions.ACTION_THROW_EXCEPTION, null);

        final PaymentTransactionInfoPlugin ret = this.paymentTestPugin.authorizePayment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                null,
                this.pluginCallContext);
    }

    @Test
    public void setInfoPluginStatus() throws PaymentPluginApiException {
        this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_PENDING, "authorizePayment");
        this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED, "capturePayment");
        this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_ERROR, "purchasePayment");

        PaymentTransactionInfoPlugin ret = this.paymentTestPugin.authorizePayment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                null,
                this.pluginCallContext);
        Assert.assertEquals(ret.getStatus(), PaymentPluginStatus.PENDING);

        ret = this.paymentTestPugin.capturePayment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                null,
                this.pluginCallContext);
        Assert.assertEquals(ret.getStatus(), PaymentPluginStatus.CANCELED);

        ret = this.paymentTestPugin.purchasePayment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                null,
                this.pluginCallContext);
        Assert.assertEquals(ret.getStatus(), PaymentPluginStatus.ERROR);
    }

    @Test
    public void methodOverrideWildCard() throws PaymentPluginApiException {
        this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_ERROR, null);
        this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_PENDING, "authorizePayment");

        final PaymentTransactionInfoPlugin ret = this.paymentTestPugin.authorizePayment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                null,
                this.pluginCallContext);
        Assert.assertEquals(ret.getStatus(), PaymentPluginStatus.PENDING);

    }

    @Test

    public void useValidPluginProperty() throws PaymentPluginApiException {
        final ImmutableList<PluginProperty> properties = ImmutableList.of(
                new PluginProperty(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_PENDING.toString(),
                                   "authorizePayment",
                                   false));

        final PaymentTransactionInfoPlugin ret = this.paymentTestPugin.authorizePayment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                properties,
                this.pluginCallContext);
        Assert.assertEquals(ret.getStatus(), PaymentPluginStatus.PENDING);
    }

    @Test
    public void wildcardPluginProperty() throws PaymentPluginApiException {
        ImmutableList<PluginProperty> properties = ImmutableList.of(
                new PluginProperty(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_PENDING.toString(),
                                   null,
                                   false));

        PaymentTransactionInfoPlugin ret = this.paymentTestPugin.authorizePayment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                properties,
                this.pluginCallContext);
        Assert.assertEquals(ret.getStatus(), PaymentPluginStatus.PENDING);

        properties = ImmutableList.of(
                new PluginProperty(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_PENDING.toString(),
                                   "",
                                   false));

        ret = this.paymentTestPugin.authorizePayment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                properties,
                this.pluginCallContext);
        Assert.assertEquals(ret.getStatus(), PaymentPluginStatus.PENDING);

        properties = ImmutableList.of(
                new PluginProperty(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_PENDING.toString(),
                                   "*",
                                   false));

        ret = this.paymentTestPugin.authorizePayment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                properties,
                this.pluginCallContext);
        Assert.assertEquals(ret.getStatus(), PaymentPluginStatus.PENDING);
    }

    @Test
    public void useInvalidPluginProperty() throws PaymentPluginApiException {
        final ImmutableList<PluginProperty> properties = ImmutableList.of(
                new PluginProperty("dummmy",
                                   "authorizePayment",
                                   false));

        final PaymentTransactionInfoPlugin ret = this.paymentTestPugin.authorizePayment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                properties,
                this.pluginCallContext);
        Assert.assertEquals(ret.getStatus(), PaymentPluginStatus.PROCESSED);
    }

    @Test
    public void pluginPropertyOverridesGlobal() throws PaymentPluginApiException {
        this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_PENDING, "authorizePayment");
        this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_CANCELED, "capturePayment");

        final ImmutableList<PluginProperty> properties = ImmutableList.of(
                new PluginProperty(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_ERROR.toString(),
                                   "authorizePayment",
                                   false));

        final PaymentTransactionInfoPlugin ret = this.paymentTestPugin.authorizePayment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                properties,
                this.pluginCallContext);
        Assert.assertEquals(ret.getStatus(), PaymentPluginStatus.ERROR);
    }

    @Test
    public void sleepMethodOverrideWildCard() {
        this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_PENDING, "authorizePayment", 15);
        this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_PENDING, null, 60);
        Assert.assertEquals(this.paymentTestPugin.getSleepValue("authorizePayment", null), 15);

    }

    @Test
    public void sleepFromConfig() {
        ImmutableList<PluginProperty> properties = ImmutableList.of(
                new PluginProperty(TestingStates.Actions.ACTION_SLEEP.toString(), "authorizePayment", false),
                new PluginProperty(TestingStates.SLEEP_PLUGIN_CONFIG_PARAM, "60", false));


        Assert.assertEquals(this.paymentTestPugin.getSleepValue("authorizePayment", properties), 60);

        properties = ImmutableList.of(
                new PluginProperty(TestingStates.Actions.ACTION_SLEEP.toString(), "authorizePayment", false),
                new PluginProperty("missing sleep param", "60", false));


        Assert.assertEquals(this.paymentTestPugin.getSleepValue("authorizePayment", properties), 0);
    }

    @Test
    public void sleepFromGlobalConfig() {
        this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_PENDING, "authorizePayment", 15);
        Assert.assertEquals(this.paymentTestPugin.getSleepValue("authorizePayment", null), 15);
    }

    @Test
    public void wildcardSleepFromGlobalConfig() {
        this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_PENDING, null, 15);
        Assert.assertEquals(this.paymentTestPugin.getSleepValue("authorizePayment", null), 15);
    }

    @Test
    public void sleepFromConfigOverrideGlobal() {
        this.testingStates.add(TestingStates.Actions.ACTION_RETURN_PLUGIN_STATUS_PENDING, "authorizePayment", 15);

        final ImmutableList<PluginProperty> properties = ImmutableList.of(
                new PluginProperty(TestingStates.Actions.ACTION_SLEEP.toString(), "authorizePayment", false),
                new PluginProperty(TestingStates.SLEEP_PLUGIN_CONFIG_PARAM, "60", false));


        Assert.assertEquals(this.paymentTestPugin.getSleepValue("authorizePayment", properties), 60);
    }
}