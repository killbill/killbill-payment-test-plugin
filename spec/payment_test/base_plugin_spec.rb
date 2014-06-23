require 'spec_helper'
require 'logger'
require 'tempfile'

require 'killbill'

require 'payment_test'

describe PaymentTest::PaymentPlugin do
  before(:each) do
    kb_apis = Killbill::Plugin::KillbillApi.new("killbill-payment-test", {})
    @plugin = PaymentTest::PaymentPlugin.new
    @plugin.logger = Logger.new(STDOUT)
    @plugin.kb_apis = kb_apis

    @kb_account_id = "a86ed6d4-c0bd-4a44-b49a-5ec29c3b314a"
    @kb_payment_id = "9f73c8e9-188a-4603-a3ba-2ce684411fb9"
    @kb_payment_transaction_id = "46bb26b0-fae2-11e3-a3ac-0800200c9a66"
    @kb_payment_method_id = "b1396a76-b210-4690-a61e-e94c911a2a09"
    @amount_in_cents = 100
    @currency = 'USD'
    @call_context = nil
  end

  it "should start and stop correctly" do
    @plugin.start_plugin
    @plugin.stop_plugin
  end

  it "should test charge" do
    output = @plugin.purchase_payment(@kb_account_id, @kb_payment_id, @kb_payment_transaction_id, @kb_payment_method_id, @amount_in_cents, @currency, [], @call_context)

    output.should be_an_instance_of Killbill::Plugin::Model::PaymentTransactionInfoPlugin
    output.amount.should == @amount_in_cents
    output.status.to_s.should == "PROCESSED"
  end

  it "should test search" do
    @plugin.search_payment_methods("blah", 0, 100, nil, @call_context).size.should == 1
  end
end
