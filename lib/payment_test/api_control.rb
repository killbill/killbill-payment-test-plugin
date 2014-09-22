require 'payment_test/plugin_property_utils'

module PaymentTest
  class PaymentPluginControl


    class Payment
      attr_reader :kb_account_id, :kb_payment_id, :transactions

      def initialize(kb_account_id, kb_payment_id)
        @kb_account_id = kb_account_id
        @kb_payment_id = kb_payment_id
        @transactions = Array.new
      end

      def add_transaction(transaction)
        @transactions << transaction
      end
    end

    attr_reader :parent, :payments

    def initialize(parent)
      @parent = parent
      @payments = Hash.new
    end

    def authorize_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context)
      add_transaction(kb_account_id, kb_payment_id, kb_transaction_id, :AUTHORIZE, amount, currency, properties)
    end

    def capture_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context)
      add_transaction(kb_account_id, kb_payment_id, kb_transaction_id, :CAPTURE, amount, currency, properties)
    end

    def purchase_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context)
      add_transaction(kb_account_id, kb_payment_id, kb_transaction_id, :PURCHASE, amount, currency, properties)
    end

    def void_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, properties, context)
      add_transaction(kb_account_id, kb_payment_id, kb_transaction_id, :VOID, amount, currency, properties)
    end

    def credit_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context)
      add_transaction(kb_account_id, kb_payment_id, kb_transaction_id, :CREDIT, amount, currency, properties)
    end

    def refund_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context)
      add_transaction(kb_account_id, kb_payment_id, kb_transaction_id, :REFUND, amount, currency, properties)
    end

    def get_payment_info(kb_account_id, kb_payment_id, properties, context)
      @payments[kb_payment_id].transactions
    end

    def search_payments(search_key, offset, limit, properties, context)
      raise NotImplementedError.new("search_payments not implemented")
    end

    def add_payment_method(kb_account_id, kb_payment_method_id, payment_method_props, set_default, properties, context)
      # Noop
    end

    def delete_payment_method(kb_account_id, kb_payment_method_id, properties, context)
      # Noop
    end

    def get_payment_method_detail(kb_account_id, kb_payment_method_id, properties, context)
      payment_method = ::Killbill::Plugin::Model::PaymentMethodInfoPlugin.new
      payment_method.account_id = kb_account_id
      payment_method.payment_method_id = kb_payment_method_id
      payment_method
    end

    def set_default_payment_method(kb_account_id, kb_payment_method_id, properties, context)
      # Noop
    end

    def get_payment_methods(kb_account_id, refresh_from_gateway, properties, context)
      raise NotImplementedError.new("get_payment_methods not implemented")
    end

    def search_payment_methods(search_key, offset, limit, properties, context)
      raise NotImplementedError.new("search_payment_methods not implemented")
    end

    def reset_payment_methods(kb_account_id, payment_methods, properties, context)
      # Noop
    end

    def build_form_descriptor(kb_account_id, customFields, properties, context)
      raise NotImplementedError.new("build_form_descriptor not implemented")
    end

    def process_notification(notification, properties, context)
      # Noop
    end

    def throw_exception_if_required(properties)
      exception = PluginPropertyUtils::get_property_or_nil(properties, 'THROW_EXCEPTION')
      if exception
        raise RuntimeError.new("throwing cause #{exception.value}")
      end
    end

    def should_return_nil(properties)
      PluginPropertyUtils::get_property_or_nil(properties, 'RETURN_NIL')
    end

    def sleep_if_required(properties)
      sleep_prop = PluginPropertyUtils::get_property_or_nil(properties, 'SLEEP_TIME_SEC')
      if sleep_prop
        sleep_time = sleep_prop.value.to_f
        @parent.logger.info "PaymentPluginControl sleeping #{sleep_time}"
        sleep sleep_time
      end
    end

    private

    def get_or_create_payment(kb_account_id, kb_payment_id)
      if !@payments.has_key? kb_payment_id
        new_payment = Payment.new(kb_account_id, kb_payment_id)
        @payments[kb_payment_id] = new_payment
      end
      @payments[kb_payment_id]
    end

    def add_transaction(kb_account_id, kb_payment_id, kb_transaction_id, transaction_type, amount, currency, properties)

      payment = get_or_create_payment(kb_account_id, kb_payment_id)

      transaction = ::Killbill::Plugin::Model::PaymentTransactionInfoPlugin.new
      transaction.kb_payment_id = kb_payment_id
      transaction.kb_transaction_payment_id = kb_transaction_id

      transaction.transaction_type = transaction_type
      transaction.amount = amount
      transaction.currency = currency
      transaction.status = status_from_properties(properties)

      payment.add_transaction(transaction)
      transaction
    end


    def status_from_properties(properties)
      status_prop = PluginPropertyUtils::get_property_or_nil(properties, 'TRANSACTION_STATUS')
      status_prop.nil? ? :PROCESSED : status_prop.value.to_sym
    end
  end
end
