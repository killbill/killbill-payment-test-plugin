require 'payment_test/api_beatrix'
require 'payment_test/api_control'
require 'payment_test/plugin_property_utils'

module PaymentTest

  class PaymentPlugin < Killbill::Plugin::Payment
    attr_reader :api_beatrix, :api_control

    #
    # undef all methods defined in Killbill::Plugin::Payment that throw by default OperationUnsupportedByGatewayError
    # so we get directed straight to the method_missing handler
    #
    undef_method :authorize_payment
    undef_method :capture_payment
    undef_method :purchase_payment
    undef_method :void_payment
    undef_method :credit_payment
    undef_method :refund_payment
    undef_method :get_payment_info
    undef_method :search_payments
    undef_method :add_payment_method
    undef_method :delete_payment_method
    undef_method :get_payment_method_detail
    undef_method :set_default_payment_method
    undef_method :get_payment_methods
    undef_method :search_payment_methods
    undef_method :reset_payment_methods
    undef_method :build_form_descriptor
    undef_method :process_notification

    def initialize
      super
      @api_beatrix = PaymentPluginBeatrix.new(self)
      @api_control = PaymentPluginControl.new(self)
    end


    def method_missing(method, *args, &block)
      # properties is always the second last argument right before context
      properties = args[args.length - 2]

      # Let's be cautious..
      PluginPropertyUtils.validate_properties(properties)

      # Extract TEST_MODE property if it exists
      test_prop = PluginPropertyUtils::get_property_or_nil(properties, 'TEST_MODE')

      # Default to Beatrix (nil properties, no key specified, or explicit key)
      if test_prop.nil? ||
          test_prop.value == 'BEATRIX'
        @api_beatrix.send method, *args
      else
        @api_control.sleep_if_required properties
        @api_control.send method, *args
      end
    end

  end
end
