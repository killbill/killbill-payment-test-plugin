require 'date'

require 'killbill/payment'

module PaymentTest
  class PaymentPlugin < Killbill::Plugin::Payment


    def start_plugin
      super
    end

    def initialize(*args)
      @raise_exception = false
      super(*args)
    end

    def get_name
    end

    def process_payment(kb_account_id, kb_payment_id, kb_payment_method_id, amount_in_cents, currency, options = {})
      Killbill::Plugin::Model::PaymentInfoPlugin.new(amount_in_cents, DateTime.now, DateTime.now, Killbill::Plugin::Model::PaymentPluginStatus::PROCESSED, "gateway_error", "gateway_error_code", nil, nil)
    end

    def get_payment_info(kb_account_id, kb_payment_id, options = {})
        Killbill::Plugin::Model::PaymentInfoPlugin.new(0, DateTime.now, DateTime.now, Killbill::Plugin::Model::PaymentPluginStatus::PROCESSED, "gateway_error", "gateway_error_code", nil, nil)
    end

    def process_refund(kb_account_id, kb_payment_id, amount_in_cents, currency, options = {})
      Killbill::Plugin::Model::RefundInfoPlugin.new(amount_in_cents, DateTime.now, DateTime.now, Killbill::Plugin::Model::RefundPluginStatus::PROCESSED, "gateway_error", "gateway_error_code", nil)
    end
    
    def get_refund_info(kb_account_id, kb_payment_id, options = {})
      Killbill::Plugin::Model::RefundInfoPlugin.new(amount_in_cents, DateTime.now, DateTime.now, illbill::Plugin::Model::RefundPluginStatus::PROCESSED, "gateway_error", "gateway_error_code", nil)
    end

    def add_payment_method(kb_account_id, kb_payment_method_id, payment_method_props, set_default, options = {})
      nil
    end

    def delete_payment_method(kb_account_id, kb_payment_method_id, options = {})
    end

    def get_payment_method_detail(kb_account_id, kb_payment_method_id, options = {})
      Killbill::Plugin::Model::PaymentMethodPlugin.new("foo", true, [], nil, "type", "cc_name", "cc_type", "cc_expiration_month", "cc_expiration_year", "cc_last4", "address1", "address2", "city", "state", "zip", "country")
    end

    def set_default_payment_method(kb_account_id, kb_payment_method_id, options = {})
    end

    def get_payment_methods(kb_account_id, refresh_from_gateway, options = {})
      [Killbill::Plugin::Model::PaymentMethodInfoPlugin.new(kb_account_id, kb_account_id, true, "external_payment_method_id")]
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
