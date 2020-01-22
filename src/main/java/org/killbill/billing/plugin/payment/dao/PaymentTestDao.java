package org.killbill.billing.plugin.payment.dao;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.joda.time.DateTime;
import org.jooq.impl.DSL;
import org.killbill.billing.plugin.dao.payment.PluginPaymentDao;
import org.killbill.billing.plugin.payment.dao.gen.tables.TestpaymentPaymentMethods;
import org.killbill.billing.plugin.payment.dao.gen.tables.TestpaymentResponses;
import org.killbill.billing.plugin.payment.dao.gen.tables.records.TestpaymentPaymentMethodsRecord;
import org.killbill.billing.plugin.payment.dao.gen.tables.records.TestpaymentResponsesRecord;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import static org.killbill.billing.plugin.payment.dao.gen.tables.TestpaymentPaymentMethods.TESTPAYMENT_PAYMENT_METHODS;
import static org.killbill.billing.plugin.payment.dao.gen.tables.TestpaymentResponses.TESTPAYMENT_RESPONSES;

public class PaymentTestDao extends PluginPaymentDao<TestpaymentResponsesRecord, TestpaymentResponses, TestpaymentPaymentMethodsRecord, TestpaymentPaymentMethods> {

    public PaymentTestDao(final DataSource dataSource) throws SQLException {
        super(TESTPAYMENT_RESPONSES, TESTPAYMENT_PAYMENT_METHODS, dataSource);
        // Save space in the database
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

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
