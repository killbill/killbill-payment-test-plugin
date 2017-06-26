require 'payment_test/api_beatrix'
require 'payment_test/api_control'
require 'payment_test/plugin_property_utils'
require 'payment_test/state'

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

      @state = PaymentTest::State.instance
    end


    def method_missing(method, *args, &block)
      # properties is always the second last argument right before context
      args[args.length - 2] = [] if args[args.length - 2].nil?
      properties = args[args.length - 2]

      # Let's be cautious..
      PluginPropertyUtils.validate_properties(properties)

      if is_beatrix_call(properties)
        @api_beatrix.send method, *args
      else
        # Check if we need to throw
        @api_control.throw_exception_if_required(properties, @state.always_throw(method))

        # Check if we should return nil
        if @api_control.should_return_nil(properties, @state.always_return_nil(method))
          return nil
        end

        # Check if we need to sleep
        @api_control.sleep_if_required(properties, @state.sleep_time_sec(method))

        if @state.always_return_plugin_status_error(method)
          PluginPropertyUtils.add_property_if_not_exist(properties, 'TRANSACTION_STATUS', 'ERROR')
        end

        if @state.always_return_plugin_status_canceled(method)
          PluginPropertyUtils.add_property_if_not_exist(properties, 'TRANSACTION_STATUS', 'CANCELED')
        end        

        # Finally make the call
        @api_control.send method, *args
      end
    end



    private

    def is_beatrix_call(properties)
      test_prop = PluginPropertyUtils::get_property_or_nil(properties, 'TEST_MODE')
      # If a prop has been specified and is not BEATRIX
      return false if test_prop && test_prop.value != 'BEATRIX'
      # Default use case for backward compatible beatrix tests
      return @state.is_clear
    end


  end
end
