require 'date'

require 'killbill/payment'

module PaymentTest
  class PaymentPlugin < Killbill::Plugin::Payment


    def start_plugin
      super
    end

    def initialize()
      @raise_exception = false
      super()
    end

    def get_name
    end

    def process_payment(kb_account_id, kb_payment_id, kb_payment_method_id, amount, currency, call_context, options = {})
      # Make an API call from the payment call
      account = @kb_apis.account_user_api.get_account_by_id(kb_account_id, @kb_apis.create_context)
      res = Killbill::Plugin::Model::PaymentInfoPlugin.new
      res.amount= amount
      res.created_date= DateTime.now
      res.effective_date= DateTime.now
      res.status=:PROCESSED
      res.gateway_error="gateway_error"
      res.gateway_error_code="gateway_error_code"
      res
    end

    def get_payment_info(kb_account_id, kb_payment_id, tenant_context, options = {})
        res = Killbill::Plugin::Model::PaymentInfoPlugin.new
        res.amount= 0
        res.created_date= DateTime.now
        res.effective_date= DateTime.now
        res.status=:PROCESSED
        res.gateway_error="gateway_error"
        res.gateway_error_code="gateway_error_code"
        res
    end

    def process_refund(kb_account_id, kb_payment_id, amount, currency, call_context, options = {})
      res = Killbill::Plugin::Model::RefundInfoPlugin.new
      res.amount= amount
      res.created_date= DateTime.now
      res.effective_date=DateTime.now
      res.status=:PROCESSED
      res.gateway_error="gateway_error"
      res.gateway_error_code="gateway_error_code"
      res
    end

    def get_refund_info(kb_account_id, kb_payment_id, tenant_context, options = {})
      res = Killbill::Plugin::Model::RefundInfoPlugin.new
      res.amount= 0
      res.created_date= DateTime.now
      res.effective_date=DateTime.now
      res.status=:PROCESSED
      res.gateway_error="gateway_error"
      res.gateway_error_code="gateway_error_code"
      res
    end

    def add_payment_method(kb_account_id, kb_payment_method_id, payment_method_props, set_default, call_context, options = {})
      nil
    end

    def delete_payment_method(kb_account_id, kb_payment_method_id, call_context, options = {})
    end

    def get_payment_method_detail(kb_account_id, kb_payment_method_id, tenant_context, options = {})
      res = Killbill::Plugin::Model::PaymentMethodPlugin.new
      res.external_payment_method_id="external_payment_method_id"
      res.is_default_payment_method=true
      res.type="Test"
      res.cc_name="cc_name"
      res.cc_expiration_month="cc_expiration_month"
      res.cc_expiration_year="cc_expiration_year"
      res.cc_last4="cc_last4"
      res.address1="address1"
      res.address2="address2"
      res.city="city"
      res.state="state"
      res.zip="zip"
      res.country="country"
      properties = []
      prop1 =  Killbill::Plugin::Model::PaymentMethodKVInfo.new
      prop1.key = "key1"
      prop1.value = "value1"
      properties << prop1
      prop2 =  Killbill::Plugin::Model::PaymentMethodKVInfo.new
      prop2.key = "key2"
      prop2.value = "value2"
      properties << prop2
      res.properties=properties
      res
    end

    def set_default_payment_method(kb_account_id, kb_payment_method_id, call_context, options = {})
    end

    def get_payment_methods(kb_account_id, refresh_from_gateway, call_context, options = {})
      res = Killbill::Plugin::Model::PaymentMethodInfoPlugin.new
      res.account_id=kb_account_id
      res.payment_method_id=kb_account_id
      res.is_default=true
      res.external_payment_method_id="external_payment_method_id"
      [res]
    end

    def reset_payment_methods(kb_account_id, payment_methods)
    end

    def raise_exception_on_next_calls
      @raise_exception = true
    end

    def clear_exception_on_next_calls
      @raise_exception = false
    end

  end
end
