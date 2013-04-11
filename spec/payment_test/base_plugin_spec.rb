require 'spec_helper'
require 'logger'
require 'tempfile'

require 'payment_test'

describe PaymentTest::PaymentPlugin do
  before(:each) do
    @plugin = PaymentTest::PaymentPlugin.new
    @plugin.logger = Logger.new(STDOUT)
    @kb_account_id = "a86ed6d4-c0bd-4a44-b49a-5ec29c3b314a"
    @kb_payment_id = "9f73c8e9-188a-4603-a3ba-2ce684411fb9"
    @kb_payment_method_id = "b1396a76-b210-4690-a61e-e94c911a2a09"
    @amount_in_cents = 100
    @currency = 'USD'
  end

  it "should start and stop correctly" do
    @plugin.start_plugin
    @plugin.stop_plugin
  end

  it "should should test charge" do
    output = @plugin.process_payment(@kb_account_id, @kb_payment_id, @kb_payment_method_id, @amount_in_cents, @currency)

    output.should be_an_instance_of Killbill::Plugin::PaymentResponse
    output.amount_in_cents.should == @amount_in_cents
    output.status.should == Killbill::Plugin::PaymentStatus::SUCCESS
  end

end
