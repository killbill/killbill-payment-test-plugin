module PaymentTest

  class PluginPropertyUtils

    def self.get_property_or_nil(properties, key_name)
      test_props = (properties || []).select { |e| e.key == key_name }
      if test_props.size > 1
        raise ArgumentError.new "multiple property with key #{key_name} is not allowed"
      end
      test_props.size == 1 ? test_props[0] : nil
    end

    def self.add_property_if_not_exist(properties, key_name, key_value)
      if get_property_or_nil(properties, key_name).nil?
        prop = Killbill::Plugin::Model::PluginProperty.new
        prop.key = key_name
        prop.value = key_value
        properties << prop
      end
    end

    def self.validate_properties(properties)
      if properties.nil?
        return
      end

      if !properties.is_a? Array
        raise ArgumentError.new "properties should be an Array"
      end

      properties.each do |p|
        if !p.is_a? Killbill::Plugin::Model::PluginProperty
          raise ArgumentError.new "Each property should be of type Killbill::Plugin::Model::PluginProperty"
        end
      end
    end

  end
end
