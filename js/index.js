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

  var that = this;

  NATIVE.events.registerHandler('gamethriveNotificationOpened', function(v) {
    if (!v.failed) {
      logger.log('{gameThriveIndex} Push notification opened');

      var tags = {};
      tags.last_notification_opened_on =  v.notification_opened_on;

      that.getNotificationOpenedCount();

      pluginSend('sendTags', tags);
    }
  });

  NATIVE.events.registerHandler('gamethriveNotificationRecieved', function(v) {
    if (!v.failed) {
      logger.log('{gameThriveIndex} Push notification recieved');

      var tags = {};
      tags.last_notification_recieved_on =  v.notification_recieved_on;

      that.getNotificationRecievedCount();

      pluginSend('sendTags', tags);
    }
  });

  NATIVE.events.registerHandler('gamethriveGotOpened', function(v) {
    if(!v.failed) {
      logger.log('{gameThriveIndex} retrieved notification opened count : ', v.notification_Open_Count);

      var tags = {};
      tags.notification_opened_count =  parseInt(v.notification_Open_Count,10) + 1;

      pluginSend('sendTags', tags);
    }
  });

  NATIVE.events.registerHandler('gamethriveGotRecievved', function(v) {
    if(!v.failed) {
      logger.log('{gameThriveIndex} retrieved notification recieved count : ', v.notification_Recieve_Count);

      var tags = {};
      tags.notification_recieved_count =  parseInt(v.notification_Recieve_Count,10) + 1;

      pluginSend('sendTags', tags);
    }
  });


  // SendTags
  this.sendTags = function (obj) {
    pluginSend('sendTags', obj);
  };

  //GetTag(NotificationOpenedCount)
  this.getNotificationOpenedCount = function () {
    pluginSend('getNotificationOpenedCount', {});
  };

  //GetTag(NotificationRecievedCount)
  this.getNotificationRecievedCount = function () {
    pluginSend('getNotificationRecievedCount', {});
  };
}))();
