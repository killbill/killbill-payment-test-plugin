/*
 * This file is generated by jOOQ.
 */
package org.killbill.billing.plugin.payment.dao.gen.tables.records;


import java.time.LocalDateTime;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.killbill.billing.plugin.payment.dao.gen.tables.TestpaymentPaymentMethods;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TestpaymentPaymentMethodsRecord extends UpdatableRecordImpl<TestpaymentPaymentMethodsRecord> implements Record8<ULong, String, String, Short, String, LocalDateTime, LocalDateTime, String> {

    private static final long serialVersionUID = -505093445;

    /**
     * Setter for <code>killbill.testpayment_payment_methods.record_id</code>.
     */
    public void setRecordId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>killbill.testpayment_payment_methods.record_id</code>.
     */
    public ULong getRecordId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>killbill.testpayment_payment_methods.kb_account_id</code>.
     */
    public void setKbAccountId(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>killbill.testpayment_payment_methods.kb_account_id</code>.
     */
    public String getKbAccountId() {
        return (String) get(1);
    }

    /**
     * Setter for <code>killbill.testpayment_payment_methods.kb_payment_method_id</code>.
     */
    public void setKbPaymentMethodId(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>killbill.testpayment_payment_methods.kb_payment_method_id</code>.
     */
    public String getKbPaymentMethodId() {
        return (String) get(2);
    }

    /**
     * Setter for <code>killbill.testpayment_payment_methods.is_deleted</code>.
     */
    public void setIsDeleted(Short value) {
        set(3, value);
    }

    /**
     * Getter for <code>killbill.testpayment_payment_methods.is_deleted</code>.
     */
    public Short getIsDeleted() {
        return (Short) get(3);
    }

    /**
     * Setter for <code>killbill.testpayment_payment_methods.additional_data</code>.
     */
    public void setAdditionalData(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>killbill.testpayment_payment_methods.additional_data</code>.
     */
    public String getAdditionalData() {
        return (String) get(4);
    }

    /**
     * Setter for <code>killbill.testpayment_payment_methods.created_date</code>.
     */
    public void setCreatedDate(LocalDateTime value) {
        set(5, value);
    }

    /**
     * Getter for <code>killbill.testpayment_payment_methods.created_date</code>.
     */
    public LocalDateTime getCreatedDate() {
        return (LocalDateTime) get(5);
    }

    /**
     * Setter for <code>killbill.testpayment_payment_methods.updated_date</code>.
     */
    public void setUpdatedDate(LocalDateTime value) {
        set(6, value);
    }

    /**
     * Getter for <code>killbill.testpayment_payment_methods.updated_date</code>.
     */
    public LocalDateTime getUpdatedDate() {
        return (LocalDateTime) get(6);
    }

    /**
     * Setter for <code>killbill.testpayment_payment_methods.kb_tenant_id</code>.
     */
    public void setKbTenantId(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>killbill.testpayment_payment_methods.kb_tenant_id</code>.
     */
    public String getKbTenantId() {
        return (String) get(7);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<ULong> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row8<ULong, String, String, Short, String, LocalDateTime, LocalDateTime, String> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    @Override
    public Row8<ULong, String, String, Short, String, LocalDateTime, LocalDateTime, String> valuesRow() {
        return (Row8) super.valuesRow();
    }

    @Override
    public Field<ULong> field1() {
        return TestpaymentPaymentMethods.TESTPAYMENT_PAYMENT_METHODS.RECORD_ID;
    }

    @Override
    public Field<String> field2() {
        return TestpaymentPaymentMethods.TESTPAYMENT_PAYMENT_METHODS.KB_ACCOUNT_ID;
    }

    @Override
    public Field<String> field3() {
        return TestpaymentPaymentMethods.TESTPAYMENT_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID;
    }

    @Override
    public Field<Short> field4() {
        return TestpaymentPaymentMethods.TESTPAYMENT_PAYMENT_METHODS.IS_DELETED;
    }

    @Override
    public Field<String> field5() {
        return TestpaymentPaymentMethods.TESTPAYMENT_PAYMENT_METHODS.ADDITIONAL_DATA;
    }

    @Override
    public Field<LocalDateTime> field6() {
        return TestpaymentPaymentMethods.TESTPAYMENT_PAYMENT_METHODS.CREATED_DATE;
    }

    @Override
    public Field<LocalDateTime> field7() {
        return TestpaymentPaymentMethods.TESTPAYMENT_PAYMENT_METHODS.UPDATED_DATE;
    }

    @Override
    public Field<String> field8() {
        return TestpaymentPaymentMethods.TESTPAYMENT_PAYMENT_METHODS.KB_TENANT_ID;
    }

    @Override
    public ULong component1() {
        return getRecordId();
    }

    @Override
    public String component2() {
        return getKbAccountId();
    }

    @Override
    public String component3() {
        return getKbPaymentMethodId();
    }

    @Override
    public Short component4() {
        return getIsDeleted();
    }

    @Override
    public String component5() {
        return getAdditionalData();
    }

    @Override
    public LocalDateTime component6() {
        return getCreatedDate();
    }

    @Override
    public LocalDateTime component7() {
        return getUpdatedDate();
    }

    @Override
    public String component8() {
        return getKbTenantId();
    }

    @Override
    public ULong value1() {
        return getRecordId();
    }

    @Override
    public String value2() {
        return getKbAccountId();
    }

    @Override
    public String value3() {
        return getKbPaymentMethodId();
    }

    @Override
    public Short value4() {
        return getIsDeleted();
    }

    @Override
    public String value5() {
        return getAdditionalData();
    }

    @Override
    public LocalDateTime value6() {
        return getCreatedDate();
    }

    @Override
    public LocalDateTime value7() {
        return getUpdatedDate();
    }

    @Override
    public String value8() {
        return getKbTenantId();
    }

    @Override
    public TestpaymentPaymentMethodsRecord value1(ULong value) {
        setRecordId(value);
        return this;
    }

    @Override
    public TestpaymentPaymentMethodsRecord value2(String value) {
        setKbAccountId(value);
        return this;
    }

    @Override
    public TestpaymentPaymentMethodsRecord value3(String value) {
        setKbPaymentMethodId(value);
        return this;
    }

    @Override
    public TestpaymentPaymentMethodsRecord value4(Short value) {
        setIsDeleted(value);
        return this;
    }

    @Override
    public TestpaymentPaymentMethodsRecord value5(String value) {
        setAdditionalData(value);
        return this;
    }

    @Override
    public TestpaymentPaymentMethodsRecord value6(LocalDateTime value) {
        setCreatedDate(value);
        return this;
    }

    @Override
    public TestpaymentPaymentMethodsRecord value7(LocalDateTime value) {
        setUpdatedDate(value);
        return this;
    }

    @Override
    public TestpaymentPaymentMethodsRecord value8(String value) {
        setKbTenantId(value);
        return this;
    }

    @Override
    public TestpaymentPaymentMethodsRecord values(ULong value1, String value2, String value3, Short value4, String value5, LocalDateTime value6, LocalDateTime value7, String value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached TestpaymentPaymentMethodsRecord
     */
    public TestpaymentPaymentMethodsRecord() {
        super(TestpaymentPaymentMethods.TESTPAYMENT_PAYMENT_METHODS);
    }

    /**
     * Create a detached, initialised TestpaymentPaymentMethodsRecord
     */
    public TestpaymentPaymentMethodsRecord(ULong recordId, String kbAccountId, String kbPaymentMethodId, Short isDeleted, String additionalData, LocalDateTime createdDate, LocalDateTime updatedDate, String kbTenantId) {
        super(TestpaymentPaymentMethods.TESTPAYMENT_PAYMENT_METHODS);

        set(0, recordId);
        set(1, kbAccountId);
        set(2, kbPaymentMethodId);
        set(3, isDeleted);
        set(4, additionalData);
        set(5, createdDate);
        set(6, updatedDate);
        set(7, kbTenantId);
    }
}
