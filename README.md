# killbill-payment-test-plugin

Plugin to test the Kill Bill PaymentPlugin API.

Release builds are available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.kill-bill.billing.plugin.ruby%22%20AND%20a%3A%22payment-test-plugin%22) with coordinates `org.kill-bill.billing.plugin.ruby:payment-test-plugin`.

Kill Bill compatibility
-----------------------

| Plugin version | Kill Bill version |
| -------------: | ----------------: |
| 4.x.y          | 0.18.z            |
| 5.x.y          | 0.19.z            |
| 6.x.y          | 0.20.z            |
| 7.x.y          | 0.22.z            |


Requirements
-----------------------

The plugin needs a database. The latest version of the schema can be found [here](https://github.com/killbill/killbill-payment-test-plugin/blob/master/src/main/resources/ddl.sql).

Usage
-----

The plugin currently supports 3 different modes

* A mode where the plugin can be configured on a per request basis
* A mode where the plugin can be configured through a private endpoint for all subsequent requests
* A backward compatible mode for existing killbill unit test

## Per Request Level  Configuration

When using payment api, one can provide payment plugin properties to enable failure modes:

In that mode, the property `TEST_MODE` must be set to something different than `BEATRIX` (any other string would work. In addition the following properties are allowed:

* `ACTION_RETURN_PLUGIN_STATUS_ERROR`=true : This will make the plugin return a `PaymentPluginStatus.ERROR` on each payment call (e.g to simulate Insuficient Fund type of errors).
* `ACTION_RETURN_PLUGIN_STATUS_CANCELED`=true : This will make the plugin return a `PaymentPluginStatus.CANCELED` on each payment call (e.g. to simulate Gateway Error type of errors).
* `ACTION_RETURN_PLUGIN_STATUS_PENDING`=true : This will make the plugin return a `PaymentPluginStatus.PENDING` on each payment call
* `THROW_EXCEPTION`=true : This will make the plugin throw RuntimeException exception on each call
* `RETURN_NIL`=true : This will make the plugin return a nil value on each call
* `SLEEP_TIME_SEC`=sleep_time_sec : This will make the plugin sleep `sleep_time_sec` on each call

## Global State Configuration

There are cases where it is not possible to pass plugin properties because the payments are made by the system, and the user does not have the opportunity to set plugin properties. For example, when Kill Bill attempts to pay an invoice, it make a `purchasePayment` call without passing plugin properties.

In order to address this scenario it is convenient to configure the plugin to behave a certain way. The plugin can be configured using a private endpoint `/plugins/killbill-payment-test/configure` to either throw exceptions, return nil values or sleep (similar to previous mode). However we also allow to configure which operations should behave that way (the idea is that we may want to make a `purchasePayment` call fail but still allow the `get_payment_info` call to return.

The json body for the call supports the following:

* `CONFIGURE_ACTION`: {`ACTION_THROW_EXCEPTION`|`RETURN_NIL`|`ACTION_SLEEP`|`ACTION_RESET`}
* `SLEEP_TIME_SEC`: (valid for `CONFIGURE_ACTION` = `ACTION_SLEEP`
* `METHODS`: The payment plugin api methods for which the configuration apply (or all methods if not specified)

Examples:

This will confgure the plugin to return an payment error on each `purchasePayment` call (note that the name of the methods come from the definition of the [payment plugin api](https://github.com/killbill/killbill-plugin-api/blob/master/payment/src/main/java/org/killbill/billing/payment/plugin/api/PaymentPluginApi.java):

```
curl -v \
-u'admin:password' \
-H "X-Killbill-ApiKey: bob" \
-H 'X-Killbill-ApiSecret: lazar' \
-H "Content-Type: application/json" \
-H 'X-Killbill-CreatedBy: stephane' \
-X POST \
--data-binary '{"CONFIGURE_ACTION":"ACTION_RETURN_PLUGIN_STATUS_ERROR", "METHODS":"purchasePayment"}' \
 -v 'http://127.0.0.1:8080/plugins/killbill-payment-test/configure'
```

One can clear the state with the following:

```
curl -v \
-u'admin:password' \
-H "X-Killbill-ApiKey: bob" \
-H 'X-Killbill-ApiSecret: lazar' \
-H "Content-Type: application/json" \
-H 'X-Killbill-CreatedBy: stephane' \
-X POST \
--data-binary '{"CONFIGURE_ACTION":"ACTION_CLEAR"}' \
 -v 'http://127.0.0.1:8080/plugins/killbill-payment-test/configure'
```

## Kill Bill Backward Compatible Configuration

This should be ignored as this will be deprecated.
