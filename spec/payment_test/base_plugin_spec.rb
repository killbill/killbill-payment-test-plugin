require 'spec_helper'
require 'logger'
require 'tempfile'

describe PaymentTest::PaymentPlugin do
  before(:each) do
    @plugin = PaymentTest::PaymentPlugin.new
    @plugin.logger = Logger.new(STDOUT)
  end

  it "should start and stop correctly" do
    @plugin.start_plugin
    @plugin.stop_plugin
  end
end
