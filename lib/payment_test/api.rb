require 'payment_test/api_beatrix'
require 'payment_test/api_control'

module PaymentTest
  class PaymentPlugin < Killbill::Plugin::Payment

    attr_reader :api_beatrix, :api_control

    def initialize
      super
      @api_beatrix = PaymentPluginBeatrix.new(self)
      @api_control = PaymentPluginControl.new(self)
    end

    def authorize_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context)
      self.send "missed_authorize_payment".to_sym, kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context
    end

    def capture_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context)
      self.send "missed_capture_payment".to_sym, kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context
    end

    def purchase_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context)
      self.send "missed_purchase_payment".to_sym, kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context
    end

    def void_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, properties, context)
      self.send "missed_void_payment".to_sym, kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, properties, context
    end

    def credit_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context)
      self.send "missed_credit_payment".to_sym, kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context
    end

    def refund_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context)
      self.send "missed_refund_payment".to_sym, kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context
    end

    def get_payment_info(kb_account_id, kb_payment_id, properties, context)
      self.send "missed_get_payment_info".to_sym, kb_account_id, kb_payment_id, properties, context
    end

    def search_payments(search_key, offset, limit, properties, context)
      self.send "missed_search_payments".to_sym, search_key, offset, limit, properties, context
    end

    def add_payment_method(kb_account_id, kb_payment_method_id, payment_method_props, set_default, properties, context)
      self.send "missed_add_payment_method".to_sym, kb_account_id, kb_payment_method_id, payment_method_props, set_default, properties, context
    end

    def delete_payment_method(kb_account_id, kb_payment_method_id, properties, context)
      self.send "missed_delete_payment_method".to_sym, kb_account_id, kb_payment_method_id, properties, context
    end

    def get_payment_method_detail(kb_account_id, kb_payment_method_id, properties, context)
      self.send "missed_get_payment_method_detail".to_sym, kb_account_id, kb_payment_method_id, properties, context
    end

    def set_default_payment_method(kb_account_id, kb_payment_method_id, properties, context)
      self.send "missed_set_default_payment_method".to_sym, kb_account_id, kb_payment_method_id, properties, context
    end

    def get_payment_methods(kb_account_id, refresh_from_gateway, properties, context)
      self.send "missed_get_payment_methods".to_sym, kb_account_id, refresh_from_gateway, properties, context
    end

    def search_payment_methods(search_key, offset, limit, properties, context)
      self.send "missed_search_payment_methods".to_sym, search_key, offset, limit, properties, context
    end

    def reset_payment_methods(kb_account_id, payment_methods, properties, context)
      self.send "missed_reset_payment_methods".to_sym, kb_account_id, payment_methods, properties, context
    end

    def build_form_descriptor(kb_account_id, customFields, properties, context)
      self.send "missed_build_form_descriptor".to_sym, kb_account_id, customFields, properties, context
    end

    def process_notification(notification, properties, context)
      self.send "missed_process_notification".to_sym, notification, properties, context
    end


    def method_missing(method, *args, &block)
      # properties is always the second last argument right before context
      properties = args[args.length - 2]

      if  properties && (! properties.is_a? Hash)
        raise ArgumentError.new "properties should be a Hash"
      end

      # remove the 'missed_'
      real_method = method.to_s.gsub('missed_', '').to_sym

      # Default to Beatrix (nil properties, no key specified, or explicit key)
      if properties.nil? ||
          (! properties.has_key? 'TEST_MODE') ||
          properties['TEST_MODE'] == 'BEATRIX'
        @api_beatrix.send real_method, *args
      else
        @api_control.sleep_if_required properties
        @api_control.send real_method, *args
      end
    end
  end
end
