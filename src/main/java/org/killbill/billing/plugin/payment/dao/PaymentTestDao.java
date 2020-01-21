package org.killbill.billing.plugin.payment.dao;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.joda.time.DateTime;
import org.jooq.impl.DSL;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.payment.plugin.api.PaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.api.payment.PluginPaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.dao.payment.PluginPaymentDao;
import org.killbill.billing.plugin.payment.dao.gen.tables.TestpaymentPaymentMethods;
import org.killbill.billing.plugin.payment.dao.gen.tables.TestpaymentResponses;
import org.killbill.billing.plugin.payment.dao.gen.tables.records.TestpaymentPaymentMethodsRecord;
import org.killbill.billing.plugin.payment.dao.gen.tables.records.TestpaymentResponsesRecord;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import static org.killbill.billing.plugin.payment.dao.gen.tables.TestpaymentPaymentMethods.TESTPAYMENT_PAYMENT_METHODS;
import static org.killbill.billing.plugin.payment.dao.gen.tables.TestpaymentResponses.TESTPAYMENT_RESPONSES;

public class PaymentTestDao extends PluginPaymentDao<TestpaymentResponsesRecord, TestpaymentResponses, TestpaymentPaymentMethodsRecord, TestpaymentPaymentMethods> {

    public PaymentTestDao(final DataSource dataSource) throws SQLException {
        super(TESTPAYMENT_RESPONSES, TESTPAYMENT_PAYMENT_METHODS, dataSource);
        // Save space in the database
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    }

    public void addPaymentResponse(final UUID accountId,
                                   final UUID tenantId,
                                   final PluginPaymentTransactionInfoPlugin infoPlugin) throws SQLException {
        try (final Connection c = this.dataSource.getConnection()) {
            DSL.using(c, PaymentTestDao.this.dialect, PaymentTestDao.this.settings)
               .insertInto(TESTPAYMENT_RESPONSES,
                           TESTPAYMENT_RESPONSES.STATUS
                       , TESTPAYMENT_RESPONSES.KB_ACCOUNT_ID
                       , TESTPAYMENT_RESPONSES.KB_PAYMENT_ID
                       , TESTPAYMENT_RESPONSES.KB_PAYMENT_TRANSACTION_ID
                       , TESTPAYMENT_RESPONSES.TRANSACTION_TYPE
                       , TESTPAYMENT_RESPONSES.AMOUNT
                       , TESTPAYMENT_RESPONSES.CURRENCY
                       , TESTPAYMENT_RESPONSES.CREATED_DATE
                       , TESTPAYMENT_RESPONSES.KB_TENANT_ID
               )
               .values(infoPlugin.getStatus().name()
                       , accountId.toString()
                       , infoPlugin.getKbPaymentId().toString()
                       , infoPlugin.getKbTransactionPaymentId().toString()
                       , infoPlugin.getTransactionType().name()
                       , infoPlugin.getAmount()
                       , infoPlugin.getCurrency().getSymbol()
                       , toTimestamp(infoPlugin.getCreatedDate())
                       , tenantId.toString()
               )
               .execute();
        }
    }

    public List<PaymentTransactionInfoPlugin> getPaymentResponses(final UUID accountId,
                                                                  final UUID tenantId,
                                                                  final UUID kbPaymentId) throws SQLException {
        try (final Connection c = this.dataSource.getConnection()) {
            return DSL.using(c, PaymentTestDao.this.dialect, PaymentTestDao.this.settings)
                      .selectFrom(TESTPAYMENT_RESPONSES)
                      .where(TESTPAYMENT_RESPONSES.KB_PAYMENT_ID.equal(kbPaymentId.toString()))
                      .and(TESTPAYMENT_RESPONSES.KB_ACCOUNT_ID.equal(accountId.toString()))
                      .and(TESTPAYMENT_RESPONSES.KB_TENANT_ID.equal(tenantId.toString()))
                      .fetch()
                      .stream()
                      .map(record ->
                                   new PluginPaymentTransactionInfoPlugin(
                                           UUID.fromString(record.getKbPaymentId()),
                                           UUID.fromString(record.getKbPaymentTransactionId()),
                                           TransactionType.valueOf(record.getTransactionType()),
                                           record.getAmount(),
                                           Currency.fromCode(record.getCurrency()),
                                           PaymentPluginStatus.valueOf(record.getStatus()),
                                           "", // gatewayError
                                           "", // gateway error code
                                           "", // firstPaymentReferenceId
                                           "", // secondPaymentReferenceId
                                           new DateTime(record.getCreatedDate().getTime()),
                                           null, // effective date
                                           null)) // properties
                      .collect(Collectors.toList());
        }
    }

    public void addPaymentMethod(final UUID kbAccountId,
                                 final UUID kbPaymentMethodId,
                                 final Map<String, Object> additionalDataMap,
                                 final DateTime utcNow,
                                 final UUID kbTenantId) throws SQLException {
        execute(this.dataSource.getConnection(),
                new WithConnectionCallback<Void>() {
                    @Override
                    public Void withConnection(final Connection conn) throws SQLException {
                        DSL.using(conn, PaymentTestDao.this.dialect, PaymentTestDao.this.settings)
                           .insertInto(PaymentTestDao.this.paymentMethodsTable,
                                       DSL.field(KB_ACCOUNT_ID),
                                       DSL.field(KB_PAYMENT_METHOD_ID),
                                       DSL.field(IS_DELETED),
                                       DSL.field(ADDITIONAL_DATA),
                                       DSL.field(CREATED_DATE),
                                       DSL.field(UPDATED_DATE),
                                       DSL.field(KB_TENANT_ID))
                           .values(kbAccountId.toString(),
                                   kbPaymentMethodId.toString(),
                                   (short) FALSE,
                                   asString(additionalDataMap),
                                   toTimestamp(utcNow),
                                   toTimestamp(utcNow),
                                   kbTenantId.toString()
                           )
                           .execute();

                        return null;
                    }
                });
    }

    @Override
    public void deletePaymentMethod(final UUID kbPaymentMethodId,
                                    final DateTime utcNow,
                                    final UUID kbTenantId) throws SQLException {
        execute(this.dataSource.getConnection(),
                new WithConnectionCallback<Void>() {
                    @Override
                    public Void withConnection(final Connection conn) throws SQLException {
                        DSL.using(conn, PaymentTestDao.this.dialect, PaymentTestDao.this.settings)
                           .update(PaymentTestDao.this.paymentMethodsTable)
                           .set(DSL.field(IS_DELETED), TRUE)
                           .set(DSL.field(UPDATED_DATE), toTimestamp(utcNow))
                           .where(DSL.field(KB_PAYMENT_METHOD_ID).equal(kbPaymentMethodId.toString()))
                           .and(DSL.field(KB_TENANT_ID).equal(kbTenantId.toString()))
                           .execute();
                        return null;
                    }
                });
    }
}
