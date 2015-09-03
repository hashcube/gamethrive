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
      // Pop off the first two arguments and keep the rest
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

  NATIVE.events.registerHandler('gamethriveNotificationOpened', function(v) {
    if (!v.failed) {
      var tags = {}, date;
      date = new Date(v.notification_opened_on).toUTCString(); 
      tags.last_notification_opened_on = date;
      data.last_notification_opened_on =  v.notification_opened_on;
      data.last_notification_segment_id =  v.segment_id;
      data.last_notification_title =  v.title;
      data.last_notification_message =  v.message;

      that.getNotificationOpenedCount();
      pluginSend('sendTags', tags);
    }
  });

  NATIVE.events.registerHandler('gamethriveNotificationReceived', function(v) {
    if (!v.failed) {
      var tags = {}, date, received_on;
      date = new Date(v.notification_received_on).toUTCString(); 
      tags.last_notification_received_on =  date;
      data.last_notification_received_on = v.notification_received_on;
      that.getNotificationReceivedCount();
      pluginSend('sendTags', tags);
    }
  });

  NATIVE.events.registerHandler('gamethriveGotOpened', function(v) {
    if(!v.failed) {
      var tags = {};
      tags.notification_opened_count =
        parseInt(v.notification_Open_Count,10) + 1;
      data.notification_opened_count = tags.notification_opened_count;
      if(!flag){
        invokeCallbacks(cb, data);
      }
      flag = 0;
      pluginSend('sendTags', tags);
    }
  });

  NATIVE.events.registerHandler('gamethriveGotReceived', function(v) {
    if(!v.failed) {
      var tags = {};
      tags.notification_received_count =
        parseInt(v.notification_Receive_Count,10) + 1;
      data.notification_received_count = tags.notification_received_count;
      invokeCallbacks(cb, data);
      pluginSend('sendTags', tags);
    }
  });


  // SendTags
  this.sendTags = function (obj, next) {
    if(cb.length < 1) {
      cb.push(next); 
    }
    pluginSend('sendTags', obj);
  };

  //GetTag(NotificationOpenedCount)
  this.getNotificationOpenedCount = function () {
    pluginSend('getNotificationOpenedCount', {});
  };

  //GetTag(NotificationReceivedCount)
  this.getNotificationReceivedCount = function () {
    flag =1;
    pluginSend('getNotificationReceivedCount', {});
  };

}))();
