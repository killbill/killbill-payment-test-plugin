require 'payment_test/api_beatrix'
require 'payment_test/api_control'
require 'payment_test/plugin_property_utils'

module PaymentTest

  #
  # Note we don't inherit Killbill::Plugin::Payment < Killbill::Plugin::PluginBase, but we inherit straight Killbill::Plugin::PluginBase
  # to bypass the definition of all the APIs, which by default raise OperationUnsupportedByGatewayError. That way, any API call
  # goes straight into method_missing, and the correct delegate dispatching can happen.
  #
  class PaymentPlugin < Killbill::Plugin::PluginBase
    attr_reader :api_beatrix, :api_control

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
