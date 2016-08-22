package com.tealeaf.plugin.plugins;

import com.tealeaf.EventQueue;
import com.tealeaf.plugin.IPlugin;
import com.tealeaf.logger;

import java.util.Date;

import org.json.JSONObject;
import org.json.JSONException;

import android.content.Intent;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;

import com.onesignal.OneSignal;
import com.onesignal.OneSignal.*;
import com.onesignal.GameBroadcastReceiver;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;

public class OnesignalPlugin implements IPlugin {

  private static final String TAG = "{{OnesignalPlugin}}";

  private static boolean onesignal = false;

  private static JSONObject onesignal_data  = new JSONObject();

  private static JSONObject data_to_send = new JSONObject();

  private static Integer opened_count = 0;

  private static GameBroadcastReceiver gameBroadcastReceiver = new GameBroadcastReceiver();

  public class onesignalNotificationReceived extends com.tealeaf.event.Event {
    boolean failed;
    String notification_data;

    public onesignalNotificationReceived(String notification_data) {
      super("onesignalNotificationReceived");
      this.failed = false;
      this.notification_data = notification_data;
    }
  }

  public class onesignalNotificationOpened extends com.tealeaf.event.Event {
    String notification_data;

    public onesignalNotificationOpened(String notification_data) {
      super("onesignalNotificationOpened");
      this.notification_data = notification_data;
    }
  }

  public void onCreateApplication(Context applicationContext) {
  }

  public void onCreate(Activity activity, Bundle savedInstanceState) {
    String g_Project_Number = null, appID = null;
    PackageManager manager = activity.getPackageManager();

    try {
      if (onesignal == false){
        Bundle meta = manager.getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA).metaData;

        if (meta != null) {
          g_Project_Number = meta.get("googleProjectNo").toString();
          appID = meta.get("onesignalAppID").toString();
        }

        if (appID != null && g_Project_Number != null) {
          OneSignal.init(activity, g_Project_Number, appID, new gameNotificationOpenedHandler());
          onesignal = true;
          logger.log(TAG, "Onesignal instance created");
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onPause() {
  }

  public void onRenderPause() {
  }

  @Override
  public void onResume() {
    checkNotification();
  }

  public void onRenderResume() {
  }

  public void sendUserTags(String jsonData) {
    try {
      JSONObject object = new JSONObject(jsonData);
      sendTags(object);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void checkNotification() {
    Date notificationReceived = null;
    long time_stamp = -1;
    Integer notificationReceivedCount = 0;
    String received_data = null;

    notificationReceived = gameBroadcastReceiver.getReceiveDate();

    if(notificationReceived != null ||
       onesignal_data.has("last_notification_opened_on"))
    {
      if(notificationReceived != null)
      {
        time_stamp = notificationReceived.getTime();
        notificationReceivedCount = gameBroadcastReceiver.getReceiveCount();
        try {
          onesignal_data.put("last_notification_received_on", time_stamp);
          data_to_send.put("last_notification_received_on",
                                      notificationReceived.toString());
          onesignal_data.put("notification_received_count",
                              notificationReceivedCount);

          if(!onesignal_data.has("notification_segment_name")) {
            received_data = gameBroadcastReceiver.getReceiveData("segment_name");
            onesignal_data.put("notification_segment_name", received_data);
          }

          if(!onesignal_data.has("notification_title")) {
            received_data = gameBroadcastReceiver.getReceiveData("title");
            onesignal_data.put("notification_title", received_data);
          }

          if(!onesignal_data.has("notification_message")) {
            received_data = gameBroadcastReceiver.getReceiveData("message");
            onesignal_data.put("notification_message", received_data);
          }

        } catch (Exception e) {
          e.printStackTrace();
        }
        getNotificationReceivedCount(notificationReceivedCount);
      }
      sendTags(data_to_send);
      EventQueue.pushEvent(new onesignalNotificationReceived(
                           onesignal_data.toString()));
      data_to_send = new JSONObject();
      onesignal_data = new JSONObject();
    }
  }

  //Send tags to onesignal
  public void sendTags(JSONObject jsonData) {
    try {
      logger.log(TAG, "Send Tags : " , jsonData.toString());
      OneSignal.sendTags(jsonData);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //Get notification_received_count, Other tags can be gotten in the same way
  public void getNotificationReceivedCount(final Integer receivedCount) {
    try {
      OneSignal.getTags(new GetTagsHandler() {
        @Override
        public void tagsAvailable(JSONObject rTags) {
          Integer  tag_val = 0;
          JSONObject object = new JSONObject();

          logger.log(TAG, "retrieved data for receive : ");
          try {
            tag_val = rTags.getInt("notification_received_count");
          } catch (JSONException eJ){
            logger.log(TAG, "Receiving notification for the first time");
          } catch (Exception e) {
             e.printStackTrace();
          }

          try {
            tag_val += receivedCount ;
            object.put("notification_received_count",
                             tag_val.toString());
            sendTags(object);
          } catch (JSONException eJ){
            logger.log(TAG, "error in json");
          }
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //Get notification_opened_count, Other tags can be gotten in the same way
  public void getNotificationOpenedCount() {
    try {
      OneSignal.getTags(new GetTagsHandler() {
        @Override
        public void tagsAvailable(JSONObject rTags) {
          logger.log(TAG, "retrieved data for open : ");
          Integer tag_val = 0 ;
          JSONObject object = new JSONObject();
          try {
            tag_val = rTags.getInt("notification_opened_count");
          } catch (JSONException eJ){
            logger.log(TAG, "Opening notification for the first time");
          } catch (Exception e) {
             e.printStackTrace();
          }

          try {
            tag_val += opened_count;
            object.put("notification_opened_count", tag_val.toString());
            opened_count = 0;
            sendTags(object);
          } catch (JSONException eJ){
            logger.log(TAG, "Error in json");
          }
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void onStart() {
  }

  public void onFirstRun() {
  }

  public void onStop() {
  }

  public void onDestroy() {
  }

  public void onNewIntent(Intent intent) {
  }

  public void setInstallReferrer(String referrer) {
  }

  public void onActivityResult(Integer request, Integer result, Intent data) {
  }

  public boolean consumeOnBackPressed() {
    return true;
  }

  public void onBackPressed() {
  }

  // NotificationOpenedHandler is implemented in its own class instead of adding implements to MainActivity so we don't hold on to a reference of our first activity if it gets recreated.
  public class gameNotificationOpenedHandler implements NotificationOpenedHandler {
    /**
     * Callback to implement in your app to handle when a notification is opened from the Android status bar
     */
    @Override
    public void notificationOpened (OSNotificationOpenResult openedResult) {
      OSNotification notification = openedResult.notification;
      String title = notification.payload.title;
      String message = notification.payload.body;
      String segment_name = null;
      String action_id = null;
      JSONObject additionalData = notification.payload.additionalData;
      OSNotificationAction.ActionType actionType = openedResult.action.actionType;

      if (actionType == OSNotificationAction.ActionType.ActionTaken)
        action_id = openedResult.action.actionID;
        logger.log(TAG, "Button pressed with id: " + action_id);

      if (additionalData != null) {
        segment_name = additionalData.optString("segment_name", null);
        logger.log(TAG, "Full additionalData:\n" + additionalData.toString());
      }

      Date current_time = new Date();
      long opened_on_time = current_time.getTime();
      boolean is_active = notification.isAppInFocus;
      opened_count += 1;

      try {
        onesignal_data.put("notification_segment_name", segment_name);
        onesignal_data.put("notification_title", title);
        onesignal_data.put("notification_message", message);
        onesignal_data.put("last_notification_opened_on", opened_on_time);
        onesignal_data.put("notification_opened_count", opened_count);
        onesignal_data.put("is_active", is_active);
        onesignal_data.put("action_id", action_id);

        EventQueue.pushEvent(new onesignalNotificationOpened(
                           onesignal_data.toString()));
        data_to_send.put("last_notification_opened_on", current_time.toString());
      } catch (JSONException e) {
        e.printStackTrace();
      }
      getNotificationOpenedCount();
    }
  }
}
