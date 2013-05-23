require 'spec_helper'
require 'logger'
require 'tempfile'

require 'killbill'

require 'payment_test'

class KillbillApiWithFakeGetAccountById < Killbill::Plugin::KillbillApi
  def initialize(japi_proxy)
    super(japi_proxy)
  end

  # Returns an account where we specify the currency for the report group
  def get_account_by_id(id)
    Killbill::Plugin::Model::Account.new(id, nil, nil, nil, nil, nil, 1, nil, 1, 'USD', nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, false, true)
  end
end

describe PaymentTest::PaymentPlugin do
  before(:each) do
    @plugin = PaymentTest::PaymentPlugin.new
    @plugin.logger = Logger.new(STDOUT)
    @plugin.kb_apis = KillbillApiWithFakeGetAccountById.new(nil)

    @kb_account_id = "a86ed6d4-c0bd-4a44-b49a-5ec29c3b314a"
    @kb_payment_id = "9f73c8e9-188a-4603-a3ba-2ce684411fb9"
    @kb_payment_method_id = "b1396a76-b210-4690-a61e-e94c911a2a09"
    @amount_in_cents = 100
    @currency = 'USD'
    @call_context = nil
  end

  it "should start and stop correctly" do
    @plugin.start_plugin
    @plugin.stop_plugin
  end

  it "should should test charge" do
    output = @plugin.process_payment(@kb_account_id, @kb_payment_id, @kb_payment_method_id, @amount_in_cents, @currency, @call_context)

    output.should be_an_instance_of Killbill::Plugin::Model::PaymentInfoPlugin
    output.amount.should == @amount_in_cents
    output.status.should == Killbill::Plugin::Model::PaymentPluginStatus.new(:PROCESSED)
  end

end
