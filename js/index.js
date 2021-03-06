/* jshint ignore:start */
import util.underscore as _;
/* jshint ignore:end */
function pluginSend(evt, params) {
  NATIVE.plugins.sendEvent('OnesignalPlugin', evt,JSON.stringify(params || {}));
}

function pluginOn(evt, next) {
  NATIVE.events.registerHandler(evt, next);
}

exports = new (Class(function() {

  var cb = [],
    flag = 0,
    data = {},
    invokeCallbacks = function (list) {
      // Pop off the first argument and keep the rest
      var args = Array.prototype.splice.call(arguments, 1),
        len = list.length,
        i, next;

      // For each callback,
      for (i = 0; i < len; ++i) {
        next = list[i];
        // If callback was actually specified,
        if (next) {
          // Run it
          next.apply(null, args);
        }
      }
    };

  NATIVE.events.registerHandler('onesignalNotificationReceived', function(v) {
    var received_data;

    if (!v.failed) {
      received_data = JSON.parse(v.notification_data);
      logger.log("{onesignal} data at js", JSON.stringify(v));
      invokeCallbacks(cb, received_data, "NotificationReceived");
    }
  });

  NATIVE.events.registerHandler('onesignalNotificationOpened', function(v) {
    var received_data;

    received_data = JSON.parse(v.notification_data);
    logger.log("{onesignal} data at js", JSON.stringify(v));
    invokeCallbacks(cb, received_data, "NotificationOpened");
  });

  // SendTags
  this.sendTags = function (obj) {
    pluginSend('sendUserTags', obj);
  };

  this.registerCallback = function (next) {
    if(cb.length < 1) {
      cb.push(next);
    }
  };

  this.getNotificationData = function (cb) {
    NATIVE.plugins.sendRequest("OnesignalPlugin", "getNotificationData", {} , function (err, res) {
        if (!err) {
          cb(res);
        }
    });
  };

}))();
