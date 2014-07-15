module PaymentTest
  class PaymentPluginBeatrix

    attr_reader :parent

    def initialize(parent)
      @parent = parent
    end


    def authorize_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context)
    end

    def capture_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context)
    end

    def purchase_payment(kb_account_id, kb_payment_id, kb_payment_transaction_id, kb_payment_method_id, amount, currency, properties, context)
      build_transaction_info :PURCHASE, kb_payment_id, kb_payment_transaction_id, amount, currency
    end

    def void_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, properties, context)
    end

    def credit_payment(kb_account_id, kb_payment_id, kb_transaction_id, kb_payment_method_id, amount, currency, properties, context)
    end

    def refund_payment(kb_account_id, kb_payment_id, kb_payment_transaction_id, kb_payment_method_id, amount, currency, properties, context)
      build_transaction_info :REFUND, kb_payment_id, kb_payment_transaction_id, amount, currency
    end

    def get_payment_info(kb_account_id, kb_payment_id, properties, context)
      [build_transaction_info(:PURCHASE, kb_payment_id)]
    end

    def search_payments(searchKey, offset, limit, properties, context)
      [get_payment_method_detail(nil, nil, properties, context)]
    end

    def add_payment_method(kb_account_id, kb_payment_method_id, paymentMethodProps, setDefault, properties, context)
    end

    def delete_payment_method(kb_account_id, kb_payment_method_id, properties, context)
    end

    def get_payment_method_detail(kb_account_id, kb_payment_method_id, properties, context)
      res = Killbill::Plugin::Model::PaymentMethodPlugin.new
      res.kb_payment_method_id = "9e3ff858-809d-4d12-a1fa-da789e0841d"
      res.external_payment_method_id = "external_payment_method_id"
      res.is_default_payment_method = true
      properties = []
      prop1 = Killbill::Plugin::Model::PluginProperty.new
      prop1.key = "key1"
      prop1.value = "value1"
      properties << prop1
      prop2 = Killbill::Plugin::Model::PluginProperty.new
      prop2.key = "key2"
      prop2.value = "value2"
      properties << prop2
      res.properties=properties
      res
    end

    def set_default_payment_method(kb_account_id, kb_payment_method_id, properties, context)
    end

    def get_payment_methods(kb_account_id, refreshFromGateway, properties, context)
      res = Killbill::Plugin::Model::PaymentMethodInfoPlugin.new
      res.account_id = kb_account_id
      res.payment_method_id = kb_account_id
      res.is_default = true
      res.external_payment_method_id = "external_payment_method_id"
      [res]
    end

    def search_payment_methods(searchKey, offset, limit, properties, context)
      [Killbill::Plugin::Model::PaymentMethodInfoPlugin.new]
    end

    def reset_payment_methods(kb_account_id, paymentMethods, properties, context)
    end

    def build_form_descriptor(kb_account_id, customFields, properties, context)
    end

    def process_notification(notification, properties, context)
    end


    private

    def build_transaction_info(transaction_type, kb_payment_id, kb_payment_transaction_id=nil, amount=0, currency=:USD)
      res = Killbill::Plugin::Model::PaymentTransactionInfoPlugin.new
      res.kb_payment_id = kb_payment_id
      res.kb_transaction_payment_id = kb_payment_transaction_id
      res.transaction_type = transaction_type
      res.amount = amount
      res.currency = currency
      res.created_date = DateTime.now
      res.effective_date = DateTime.now
      res.status = :PROCESSED
      res.gateway_error = "gateway_error"
      res.gateway_error_code = "gateway_error_code"
      res.first_payment_reference_id = "first_payment_reference_id"
      res.second_payment_reference_id = "second_payment_reference_id"
      res
    end
  end
end
