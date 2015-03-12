/* jshint ignore:start */
import util.underscore as _;
/* jshint ignore:end */
function pluginSend(evt, params) {
		NATIVE.plugins.sendEvent('GameThrivePlugin', evt,
			JSON.stringify(params || {}));
}

function pluginOn(evt, next) {
		NATIVE.events.registerHandler(evt, next);
}

exports = new (Class(function() {

  var that = this;

  NATIVE.events.registerHandler('gamethriveNotificationOpened', function(v) {
    logger.log('{gameThriveIndex} Push notification opened');

    if (!v.failed) {
      tags = {};
      tags.last_notification_opened_on =  v.notification_opened_on;

      that.getNotificationOpenedCount();

      pluginSend('sendTags', tags);
    }
  });

  NATIVE.events.registerHandler('gamethriveGotData', function(v) {
    if(!v.failed) {
      logger.log('{gameThriveIndex} retrieved data', v.noOpCount);

      tags = {};
      tags.notification_opened_count =  parseInt(v.noOpCount,10) + 1;

      pluginSend('sendTags', tags);
    }
  });

  // SendTags
  this.sendTags = function (obj) {
    pluginSend('sendTags', obj);
  };

  //GetTag(NotificationOpenedCount)
  this.getNotificationOpenedCount = function (key) {
    pluginSend('getNotificationOpenedCount');
  };

}))();
