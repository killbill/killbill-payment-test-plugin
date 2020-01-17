package org.killbill.billing.plugin.payment;

import com.google.common.annotations.VisibleForTesting;

import org.joda.time.DateTime;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillLogService;
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
import org.killbill.clock.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

public class PaymentTestPluginApi extends PluginPaymentPluginApi<TestpaymentResponsesRecord, TestpaymentResponses, TestpaymentPaymentMethodsRecord, TestpaymentPaymentMethods> {

    private final Logger         LOGGER  = LoggerFactory.getLogger(PaymentTestPluginApi.class);
    private final PaymentTestDao dao;
    private final TestingStates  testingStates;
    private final Integer        noSleep = new Integer(0);

    public PaymentTestPluginApi(final OSGIKillbillAPI killbillAPI,
                                final OSGIConfigPropertiesService configProperties,
                                final OSGIKillbillLogService logService,
                                final Clock clock,
                                final PaymentTestDao dao,
                                final TestingStates testingStates) {
        super(killbillAPI, configProperties, logService, clock, dao);
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
                                         if (p.getValue() == null || ((String) p.getValue()).length() == 0 ||
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
        TestingStates.Actions action = this.testingStates.getStates().get("*");

        final String calledMethod = "All";
        if (action == null) {
            action = this.testingStates.getStates().get(methodCalled);
        }
        return action;
    }

    @VisibleForTesting
    int getSleepFromProperty(final String methodCalled,
                             final Iterable<PluginProperty> pluginProperties) {
        int sleep = 0;
        if (pluginProperties != null) {
            final Optional<PluginProperty> actionFromProperties =
                    StreamSupport.stream(pluginProperties.spliterator(), false)
                                 // find a plugin property
                                 .filter(p -> {
                                     try {
                                         // where key is a known action
                                         TestingStates.Actions a = TestingStates.Actions.valueOf(p.getKey());
                                         // and value match all or the method called
                                         if (a.compareTo(TestingStates.Actions.ACTION_SLEEP) == 0 && (
                                                 p.getValue() == null || ((String) p.getValue()).length() == 0 ||
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
        return sleep;
        //        final Integer sleep = this.testingStates.getSleeps().get(methodCalled);
        //        if (sleep != null && sleep.compareTo(this.noSleep) > 0) {
        //            try {
        //                this.LOGGER.info("sleeping in " + methodCalled + " for " + sleep.intValue() + "(s)");
        //                Thread.sleep(sleep.intValue() * 1000000);
        //            }
        //            catch (final InterruptedException ignore) {
        //            }
        //        }
        //        return sleep;
    }

    private PaymentPluginStatus handleState(final Iterable<PluginProperty> pluginProperties) throws PaymentPluginApiException {
        final String methodCalled = Thread.currentThread().getStackTrace()[2].getMethodName();
        final TestingStates.Actions action = getAction(methodCalled, pluginProperties);

        final Integer sleep = this.testingStates.getSleeps().get(methodCalled);
        if (sleep != null && sleep.compareTo(this.noSleep) > 0) {
            try {
                this.LOGGER.info("sleeping in " + methodCalled + " for " + sleep.intValue() + "(s)");
                Thread.sleep(sleep.intValue() * 1000000);
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
            return new PluginPaymentTransactionInfoPlugin(kbPaymentId,
                                                          kbPaymentMethodId,
                                                          TransactionType.AUTHORIZE,
                                                          amount,
                                                          currency,
                                                          pluginStatus,
                                                          null, null, null, null,
                                                          DateTime.now(),
                                                          DateTime.now(),
                                                          null);
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
            return new PluginPaymentTransactionInfoPlugin(kbPaymentId,
                                                          kbPaymentMethodId,
                                                          TransactionType.CAPTURE,
                                                          amount,
                                                          currency,
                                                          pluginStatus,
                                                          null, null, null, null,
                                                          DateTime.now(),
                                                          DateTime.now(),
                                                          null);
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
            return new PluginPaymentTransactionInfoPlugin(kbPaymentId,
                                                          kbPaymentMethodId,
                                                          TransactionType.PURCHASE,
                                                          amount,
                                                          currency,
                                                          pluginStatus,
                                                          null, null, null, null,
                                                          DateTime.now(),
                                                          DateTime.now(),
                                                          null);
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
            return new PluginPaymentTransactionInfoPlugin(kbPaymentId,
                                                          kbPaymentMethodId,
                                                          TransactionType.VOID,
                                                          null,
                                                          null,
                                                          pluginStatus,
                                                          null, null, null, null,
                                                          DateTime.now(),
                                                          DateTime.now(),
                                                          null);
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
            return new PluginPaymentTransactionInfoPlugin(kbPaymentId,
                                                          kbPaymentMethodId,
                                                          TransactionType.CREDIT,
                                                          amount,
                                                          currency,
                                                          pluginStatus,
                                                          null, null, null, null,
                                                          DateTime.now(),
                                                          DateTime.now(),
                                                          null);
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
            return new PluginPaymentTransactionInfoPlugin(kbPaymentId,
                                                          kbPaymentMethodId,
                                                          TransactionType.REFUND,
                                                          null,
                                                          null,
                                                          pluginStatus,
                                                          null, null, null, null,
                                                          DateTime.now(),
                                                          DateTime.now(),
                                                          null);
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
}
