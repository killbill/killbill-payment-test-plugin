require 'singleton'

module PaymentTest
  class State

    include Singleton

    def initialize
      reset_configuration
    end

    def configure_always_return_plugin_status_pending(methods=nil)
      reset_configuration
      configure_methods(methods)
      @always_return_plugin_status_pending = true
      log_current_state
    end

    def configure_always_return_plugin_status_error(methods=nil)
      reset_configuration
      configure_methods(methods)
      @always_return_plugin_status_error = true
      log_current_state
    end

    def configure_always_return_plugin_status_canceled(methods=nil)
      reset_configuration
      configure_methods(methods)
      @always_return_plugin_status_canceled = true
      log_current_state
    end

    def configure_always_throw(methods=nil)
      reset_configuration
      configure_methods(methods)
      @always_throw = true
      log_current_state
    end

    def configure_return_nil(methods=nil)
      reset_configuration
      configure_methods(methods)
      @always_return_nil = true
      log_current_state
    end

    def configure_sleep_time(sleep_time_sec, methods=nil)
      reset_configuration
      configure_methods(methods)
      @sleep_time_sec = sleep_time_sec
      log_current_state
    end

    def reset_configuration
      @always_return_plugin_status_error = false
      @always_return_plugin_status_pending = false
      @always_return_plugin_status_canceled = false
      @always_throw = false
      @always_return_nil = false
      @sleep_time_sec = nil
      @methods = nil
      log_current_state
    end

    def always_return_plugin_status_error(method)
      @always_return_plugin_status_error && is_for_method(method)
    end

    def always_return_plugin_status_pending(method)
      @always_return_plugin_status_pending && is_for_method(method)
    end

    def always_return_plugin_status_canceled(method)
      @always_return_plugin_status_canceled && is_for_method(method)
    end

    def always_throw(method)
      @always_throw && is_for_method(method)
    end

    def always_return_nil(method)
      @always_return_nil && is_for_method(method)
    end

    def sleep_time_sec(method)
      return @sleep_time_sec if @sleep_time_sec && is_for_method(method)
      nil
    end

    def is_clear
      return !@always_return_plugin_status_error &&
          !@always_return_plugin_status_canceled &&
              !@always_return_plugin_status_pending &&
                  !@always_throw &&
                      !@always_return_nil &&
                          @sleep_time_sec.nil?
    end

    def log_current_state
      puts "PaymentTest:State : @always_return_plugin_status_error = #{@always_return_plugin_status_error}, @always_return_plugin_status_canceled = #{@always_return_plugin_status_canceled}, @always_return_plugin_status_pending=#{@always_return_plugin_status_pending}, @always_throw = #{@always_throw}, @always_return_nil = #{@always_return_nil}, @sleep_time_sec = #{@sleep_time_sec}, @methods=#{@methods}"
    end

    private

    def configure_methods(methods=nil)
      @methods = methods.split(",")
    end

    def is_for_method(method)
      # If no methods were configured, this apply to all of them
      return true if @methods.nil?

      @methods.include? method.to_s
    end
  end
end
