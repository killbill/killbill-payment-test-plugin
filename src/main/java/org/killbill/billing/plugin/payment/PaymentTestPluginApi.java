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
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;
import org.joda.time.DateTime;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.payment.api.PaymentMethodPlugin;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.GatewayNotification;
import org.killbill.billing.payment.plugin.api.HostedPaymentPageFormDescriptor;
import org.killbill.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.payment.plugin.api.PaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.api.payment.PluginPaymentPluginApi;
import org.killbill.billing.plugin.api.payment.PluginPaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.payment.dao.PaymentTestDao;
import org.killbill.billing.plugin.payment.dao.gen.tables.TestpaymentPaymentMethods;
import org.killbill.billing.plugin.payment.dao.gen.tables.TestpaymentResponses;
import org.killbill.billing.plugin.payment.dao.gen.tables.records.TestpaymentPaymentMethodsRecord;
import org.killbill.billing.plugin.payment.dao.gen.tables.records.TestpaymentResponsesRecord;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.clock.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

public class PaymentTestPluginApi extends PluginPaymentPluginApi<TestpaymentResponsesRecord, TestpaymentResponses, TestpaymentPaymentMethodsRecord, TestpaymentPaymentMethods> {

    private final Logger         LOGGER  = LoggerFactory.getLogger(PaymentTestPluginApi.class);
    private final PaymentTestDao dao;
    private final TestingStates  testingStates;
    private final Integer        noSleep = 0;

    public PaymentTestPluginApi(final OSGIKillbillAPI killbillAPI,
                                final OSGIConfigPropertiesService configProperties,
                                final Clock clock,
                                final PaymentTestDao dao,
                                final TestingStates testingStates) {
        super(killbillAPI, configProperties, clock, dao);
        this.dao = dao;
        this.testingStates = testingStates;
    }

    private TestingStates.Actions getAction(final String methodCalled,
                                            final Iterable<PluginProperty> pluginProperties) {

        // find action from request properties
        if (pluginProperties != null) {
            final Optional<PluginProperty> actionFromProperties =
                    StreamSupport.stream(pluginProperties.spliterator(), false)
                                 // find a plugin property
                                 .filter(p -> {
                                     try {
                                         // where key is a known action
                                         TestingStates.Actions.valueOf(p.getKey());
                                         // and value match all or the method called
                                         if (Strings.isNullOrEmpty((String) p.getValue()) ||
                                                 ((String) p.getValue()).compareTo("*") == 0 ||
                                                 ((String) p.getValue()).compareTo(methodCalled) == 0) {
                                             return true;
                                         }
                                     }
                                     catch (final IllegalArgumentException ignore) {}
                                     return false;
                                 })
                                 .findFirst();
            if (actionFromProperties.isPresent()) {
                return TestingStates.Actions.valueOf(actionFromProperties.get().getKey());
            }
        }
        // find action from global configuration
        TestingStates.Actions action = this.testingStates.getStates().get(methodCalled);
        if (action == null) {
            action = this.testingStates.getStates().get("*");
        }
        return action;
    }

    @VisibleForTesting
    int getSleepValue(final String methodCalled,
                      final Iterable<PluginProperty> pluginProperties) {
        int sleep = 0;
        if (pluginProperties != null) {
            // look for sleep in 'one time config'
            final Optional<PluginProperty> actionFromProperties =
                    StreamSupport.stream(pluginProperties.spliterator(), false)
                                 // find a plugin property
                                 .filter(p -> {
                                     try {
                                         // where key is a known action
                                         TestingStates.Actions a = TestingStates.Actions.valueOf(p.getKey());
                                         // and value match all or the method called
                                         if (a.compareTo(TestingStates.Actions.ACTION_SLEEP) == 0 && (
                                                 Strings.isNullOrEmpty((String) p.getValue()) ||
                                                         ((String) p.getValue()).compareTo("*") == 0 ||
                                                         ((String) p.getValue()).compareTo(methodCalled) == 0)) {
                                             return true;
                                         }
                                     }
                                     catch (final IllegalArgumentException ignore) {}
                                     return false;
                                 })
                                 .findFirst();
            if (actionFromProperties.isPresent()) {
                // find sleep value
                sleep = StreamSupport.stream(pluginProperties.spliterator(), false)
                                     .filter(property -> TestingStates.SLEEP_PLUGIN_CONFIG_PARAM.compareTo(property.getKey()) == 0)
                                     .findFirst().map(property -> Integer.valueOf((String) property.getValue()))
                                     .orElse(0);
            }
        }
        if (sleep == 0) {
            // look for sleep in global config
            final Integer globalSleep = (this.testingStates.getSleeps().get(methodCalled) != null)
                    ? this.testingStates.getSleeps().get(methodCalled) : this.testingStates.getSleeps().get("*");
            if (globalSleep != null && globalSleep.compareTo(this.noSleep) > 0) {
                sleep = globalSleep;
            }
        }
        return sleep;
    }

    private PaymentPluginStatus handleState(final Iterable<PluginProperty> pluginProperties) throws PaymentPluginApiException {
        final String methodCalled = Thread.currentThread().getStackTrace()[2].getMethodName();

        final TestingStates.Actions action = getAction(methodCalled, pluginProperties);
        final int sleep = getSleepValue(methodCalled, pluginProperties);
        if (sleep > 0) {
            try {
                this.LOGGER.info("sleeping in " + methodCalled + " for " + sleep + "(s)");
                Thread.sleep(sleep * 1000000L);
            }
            catch (final InterruptedException ignore) {
            }
        }
        if (action != null) {
            switch (action) {
                case RETURN_NIL:
                    return null;
                case ACTION_RETURN_PLUGIN_STATUS_CANCELED:
                    return PaymentPluginStatus.CANCELED;
                case ACTION_RETURN_PLUGIN_STATUS_PENDING:
                    return PaymentPluginStatus.PENDING;
                case ACTION_RETURN_PLUGIN_STATUS_ERROR:
                    return PaymentPluginStatus.ERROR;
                case ACTION_THROW_EXCEPTION:
                    throw new PaymentPluginApiException("test", action.name() + " for " + methodCalled);
                default:
                    return PaymentPluginStatus.UNDEFINED;
            }
        }
        return PaymentPluginStatus.PROCESSED;
    }

    @Override
    public void addPaymentMethod(final UUID kbAccountId,
                                 final UUID kbPaymentMethodId,
                                 final PaymentMethodPlugin paymentMethodProps,
                                 final boolean setDefault,
                                 final Iterable<PluginProperty> properties,
                                 final CallContext context) throws PaymentPluginApiException {
        try {
            this.dao.addPaymentMethod(kbAccountId,
                                      kbPaymentMethodId,
                                      null,
                                      this.clock.getUTCNow(),
                                      context.getTenantId());
        }
        catch (final SQLException e) {
            throw new PaymentPluginApiException("Unable to add payment method", e);
        }
    }

    @Override
    public void deletePaymentMethod(final UUID kbAccountId,
                                    final UUID kbPaymentMethodId,
                                    final Iterable<PluginProperty> properties,
                                    final CallContext context) throws PaymentPluginApiException {
        final DateTime utcNow = this.clock.getUTCNow();
        try {
            this.dao.deletePaymentMethod(kbPaymentMethodId, utcNow, context.getTenantId());
        }
        catch (final SQLException e) {
            throw new PaymentPluginApiException("Unable to delete payment method for kbPaymentMethodId " + kbPaymentMethodId,
                                                e);
        }
    }

    @Override
    protected PaymentTransactionInfoPlugin buildPaymentTransactionInfoPlugin(final TestpaymentResponsesRecord testpaymentResponsesRecord) {
        return null;
    }

    @Override
    protected PaymentMethodPlugin buildPaymentMethodPlugin(final TestpaymentPaymentMethodsRecord testpaymentPaymentMethodsRecord) {
        return null;
    }

    @Override
    protected PaymentMethodInfoPlugin buildPaymentMethodInfoPlugin(final TestpaymentPaymentMethodsRecord testpaymentPaymentMethodsRecord) {
        return null;
    }

    @Override
    protected String getPaymentMethodId(final TestpaymentPaymentMethodsRecord testpaymentPaymentMethodsRecord) {
        return null;
    }


    private void insertPaymentResponse(final UUID kbAccountId,
                                       final UUID tenantId,
                                       final PluginPaymentTransactionInfoPlugin infoPlugin) throws PaymentPluginApiException {
        try {
            this.dao.addPaymentResponse(kbAccountId, tenantId, infoPlugin);
        }
        catch (final SQLException e) {
            throw new PaymentPluginApiException(e.getMessage(), e);
        }
    }

    @Override
    public PaymentTransactionInfoPlugin authorizePayment(final UUID kbAccountId,
                                                         final UUID kbPaymentId,
                                                         final UUID kbTransactionId,
                                                         final UUID kbPaymentMethodId,
                                                         final BigDecimal amount,
                                                         final Currency currency,
                                                         final Iterable<PluginProperty> properties,
                                                         final CallContext context) throws PaymentPluginApiException {
        final PaymentPluginStatus pluginStatus = handleState(properties);
        if (pluginStatus != null) {
            final PluginPaymentTransactionInfoPlugin infoPlugin = new PluginPaymentTransactionInfoPlugin(kbPaymentId,
            																							 kbTransactionId,
                                                                                                         TransactionType.AUTHORIZE,
                                                                                                         amount,
                                                                                                         currency,
                                                                                                         pluginStatus,
                                                                                                         null,
                                                                                                         null,
                                                                                                         null,
                                                                                                         null,
                                                                                                         DateTime.now(),
                                                                                                         DateTime.now(),
                                                                                                         null);
            insertPaymentResponse(kbAccountId, context.getTenantId(), infoPlugin);
            return infoPlugin;
        }
        return null;
    }

    @Override
    public PaymentTransactionInfoPlugin capturePayment(final UUID kbAccountId,
                                                       final UUID kbPaymentId,
                                                       final UUID kbTransactionId,
                                                       final UUID kbPaymentMethodId,
                                                       final BigDecimal amount,
                                                       final Currency currency,
                                                       final Iterable<PluginProperty> properties,
                                                       final CallContext context) throws PaymentPluginApiException {
        final PaymentPluginStatus pluginStatus = handleState(properties);
        if (pluginStatus != null) {
            final PluginPaymentTransactionInfoPlugin infoPlugin = new PluginPaymentTransactionInfoPlugin(kbPaymentId,
            																							 kbTransactionId,
                                                                                                         TransactionType.CAPTURE,
                                                                                                         amount,
                                                                                                         currency,
                                                                                                         pluginStatus,
                                                                                                         null,
                                                                                                         null,
                                                                                                         null,
                                                                                                         null,
                                                                                                         DateTime.now(),
                                                                                                         DateTime.now(),
                                                                                                         null);
            insertPaymentResponse(kbAccountId, context.getTenantId(), infoPlugin);
            return infoPlugin;
        }
        return null;
    }

    @Override
    public PaymentTransactionInfoPlugin purchasePayment(final UUID kbAccountId,
                                                        final UUID kbPaymentId,
                                                        final UUID kbTransactionId,
                                                        final UUID kbPaymentMethodId,
                                                        final BigDecimal amount,
                                                        final Currency currency,
                                                        final Iterable<PluginProperty> properties,
                                                        final CallContext context) throws PaymentPluginApiException {
        final PaymentPluginStatus pluginStatus = handleState(properties);
        if (pluginStatus != null) {
            final PluginPaymentTransactionInfoPlugin infoPlugin = new PluginPaymentTransactionInfoPlugin(kbPaymentId,
            																							 kbTransactionId,
                                                                                                         TransactionType.PURCHASE,
                                                                                                         amount,
                                                                                                         currency,
                                                                                                         pluginStatus,
                                                                                                         null,
                                                                                                         null,
                                                                                                         null,
                                                                                                         null,
                                                                                                         DateTime.now(),
                                                                                                         DateTime.now(),
                                                                                                         null);
            insertPaymentResponse(kbAccountId, context.getTenantId(), infoPlugin);
            return infoPlugin;
        }
        return null;
    }

    @Override
    public PaymentTransactionInfoPlugin voidPayment(final UUID kbAccountId,
                                                    final UUID kbPaymentId,
                                                    final UUID kbTransactionId,
                                                    final UUID kbPaymentMethodId,
                                                    final Iterable<PluginProperty> properties,
                                                    final CallContext context) throws PaymentPluginApiException {
        final PaymentPluginStatus pluginStatus = handleState(properties);
        if (pluginStatus != null) {
            final PluginPaymentTransactionInfoPlugin infoPlugin = new PluginPaymentTransactionInfoPlugin(kbPaymentId,
            																							 kbTransactionId,
                                                                                                         TransactionType.VOID,
                                                                                                         null,
                                                                                                         null,
                                                                                                         pluginStatus,
                                                                                                         null,
                                                                                                         null,
                                                                                                         null,
                                                                                                         null,
                                                                                                         DateTime.now(),
                                                                                                         DateTime.now(),
                                                                                                         null);
            insertPaymentResponse(kbAccountId, context.getTenantId(), infoPlugin);
            return infoPlugin;
        }
        return null;
    }

    @Override
    public PaymentTransactionInfoPlugin creditPayment(final UUID kbAccountId,
                                                      final UUID kbPaymentId,
                                                      final UUID kbTransactionId,
                                                      final UUID kbPaymentMethodId,
                                                      final BigDecimal amount,
                                                      final Currency currency,
                                                      final Iterable<PluginProperty> properties,
                                                      final CallContext context) throws PaymentPluginApiException {
        final PaymentPluginStatus pluginStatus = handleState(properties);
        if (pluginStatus != null) {
            final PluginPaymentTransactionInfoPlugin infoPlugin = new PluginPaymentTransactionInfoPlugin(kbPaymentId,
            																							 kbTransactionId,
                                                                                                         TransactionType.CREDIT,
                                                                                                         amount,
                                                                                                         currency,
                                                                                                         pluginStatus,
                                                                                                         null,
                                                                                                         null,
                                                                                                         null,
                                                                                                         null,
                                                                                                         DateTime.now(),
                                                                                                         DateTime.now(),
                                                                                                         null);
            insertPaymentResponse(kbAccountId, context.getTenantId(), infoPlugin);
            return infoPlugin;
        }
        return null;
    }

    @Override
    public PaymentTransactionInfoPlugin refundPayment(final UUID kbAccountId,
                                                      final UUID kbPaymentId,
                                                      final UUID kbTransactionId,
                                                      final UUID kbPaymentMethodId,
                                                      final BigDecimal amount,
                                                      final Currency currency,
                                                      final Iterable<PluginProperty> properties,
                                                      final CallContext context) throws PaymentPluginApiException {
        final PaymentPluginStatus pluginStatus = handleState(properties);
        if (pluginStatus != null) {
            final PluginPaymentTransactionInfoPlugin infoPlugin = new PluginPaymentTransactionInfoPlugin(kbPaymentId,
            		     																				 kbTransactionId,
                                                                                                         TransactionType.REFUND,
                                                                                                         null,
                                                                                                         null,
                                                                                                         pluginStatus,
                                                                                                         null,
                                                                                                         null,
                                                                                                         null,
                                                                                                         null,
                                                                                                         DateTime.now(),
                                                                                                         DateTime.now(),
                                                                                                         null);
            insertPaymentResponse(kbAccountId, context.getTenantId(), infoPlugin);
            return infoPlugin;
        }
        return null;
    }

    @Override
    public HostedPaymentPageFormDescriptor buildFormDescriptor(final UUID kbAccountId,
                                                               final Iterable<PluginProperty> customFields,
                                                               final Iterable<PluginProperty> properties,
                                                               final CallContext context) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public GatewayNotification processNotification(final String notification,
                                                   final Iterable<PluginProperty> properties,
                                                   final CallContext context) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public List<PaymentTransactionInfoPlugin> getPaymentInfo(final UUID kbAccountId,
                                                             final UUID kbPaymentId,
                                                             final Iterable<PluginProperty> properties,
                                                             final TenantContext context) throws PaymentPluginApiException {
        try {
            return this.dao.getPaymentResponses(kbAccountId, context.getTenantId(), kbPaymentId);
        }
        catch (final SQLException e) {
            throw new PaymentPluginApiException(e.getMessage(), e);
        }
    }
}
