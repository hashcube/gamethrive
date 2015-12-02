/* jshint ignore:start */
import util.underscore as _;
/* jshint ignore:end */
function pluginSend(evt, params) {
  NATIVE.plugins.sendEvent('GameThrivePlugin', evt,JSON.stringify(params || {}));
}

function pluginOn(evt, next) {
  NATIVE.events.registerHandler(evt, next);
}

exports = new (Class(function() {

  var that = this,
    cb = [],
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

  NATIVE.events.registerHandler('gamethriveNotificationReceived', function(v) {
    if (!v.failed) {
      var received_data;
      received_data = JSON.parse(v.notification_data);
      logger.log("{gamethrive} data at js", JSON.stringify(v));
      invokeCallbacks(cb, received_data);
    }
  });

  // SendTags
  this.sendTags = function (obj) {
    pluginSend('sendUserTags', obj);
  };

  this.registerCallback = function (next) {
    logger.log("{gamethrive} at callback");
    if(cb.length < 1) {
      cb.push(next); 
    }
  };

}))();
