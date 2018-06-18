# -- encoding : utf-8 --

#include Killbill::Plugin::ActiveMerchant::Sinatra

require 'sinatra'

configure do
end

helpers do
  def state
    PaymentTest::State.instance
  end
end


get '/plugins/killbill-payment-test/status', :provides => 'json' do
  current_state = state.status
  # Extract non nil value -- basically the one that are set
  result = current_state.select {|key, value| value}
  result.to_json
end


post '/plugins/killbill-payment-test/configure', :provides => 'json' do

  begin
    data = JSON.parse request.body.read
  rescue JSON::ParserError => e
    halt 400, {'Content-Type' => 'text/plain'}, "Invalid payload: #{e}"
  end

  action = data['CONFIGURE_ACTION']
  if action.nil?
    halt 400, {'Content-Type' => 'text/plain'}, "Invalid json, need to specify one CONFIGURE_ACTION={ACTION_RETURN_PLUGIN_STATUS_ERROR|ACTION_RETURN_PLUGIN_STATUS_CANCELED|ACTION_THROW_EXCEPTION|ACTION_RETURN_NIL|ACTION_SLEEP|ACTION_RESET}"
  end


  if action == 'ACTION_RETURN_PLUGIN_STATUS_ERROR'
    state.configure_always_return_plugin_status_error(data['METHODS'])
  elsif action == 'ACTION_RETURN_PLUGIN_STATUS_CANCELED'
    state.configure_always_return_plugin_status_canceled(data['METHODS'])
  elsif action == 'ACTION_RETURN_PLUGIN_STATUS_PENDING'
    state.configure_always_return_plugin_status_pending(data['METHODS'])
  elsif action == 'ACTION_THROW_EXCEPTION'
    state.configure_always_throw(data['METHODS'])
  elsif action == 'ACTION_RETURN_NIL'
    state.configure_return_nil(data['METHODS'])
  elsif action == 'ACTION_SLEEP'
    state.configure_sleep_time(data['SLEEP_TIME_SEC'], data['METHODS'])
  else
    state.reset_configuration
  end
  status 200
end

