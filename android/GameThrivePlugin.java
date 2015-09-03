package com.tealeaf.plugin.plugins;

import com.onesignal.GameBroadcastReceiver;

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

import com.onesignal.OneSignal.NotificationOpenedHandler;

public class GameThrivePlugin implements IPlugin {

  private static final String TAG = "{{GameThrivePlugin}}";

  private static boolean gameThrive = false;

  private static GameBroadcastReceiver gameBroadcastReceiver = new GameBroadcastReceiver();

  public class gamethriveNotificationOpened extends com.tealeaf.event.Event {
    boolean failed;
    long notification_opened_on;
    String segment_id, title, message;

    public gamethriveNotificationOpened(long opened_on, String segment_id, String title, String message) {
      super("gamethriveNotificationOpened");
      this.failed = false;
      this.notification_opened_on = opened_on;
      this.segment_id = segment_id;
      this.title = title;
      this.message = message;
    }
  }

  public class gamethriveNotificationReceived extends com.tealeaf.event.Event {
    boolean failed;
    long notification_received_on;

    public gamethriveNotificationReceived(long received_on) {
      super("gamethriveNotificationReceived");
      this.failed = false;
      this.notification_received_on = received_on;
    }
  }

  public class gamethriveGotOpened extends com.tealeaf.event.Event {
    boolean failed;
    String notification_Open_Count;

    public gamethriveGotOpened(String opCount) {
      super("gamethriveGotOpened");
      this.failed = false;
      this.notification_Open_Count = opCount;
    }
  }

  public class gamethriveGotReceived extends com.tealeaf.event.Event {
    boolean failed;
    String notification_Receive_Count;

    public gamethriveGotReceived(String recCount) {
      super("gamethriveGotReceived");
      this.failed = false;
      this.notification_Receive_Count = recCount;
    }
  }

  public void onCreateApplication(Context applicationContext) {
  }

  public void onCreate(Activity activity, Bundle savedInstanceState) {
    String g_Project_Number = null, appID = null;
    PackageManager manager = activity.getPackageManager();

    logger.log("GAMETHRIVE INITIALIZED", TAG);

    try {

      if (gameThrive == false){
        Bundle meta = manager.getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA).metaData;

        if (meta != null) {
          g_Project_Number = meta.get("googleProjectNo").toString();
          appID = meta.get("gameThriveAppID").toString();
        }

        logger.log(g_Project_Number, appID, TAG);

        if (appID != null && g_Project_Number != null) {
          OneSignal.init(activity, g_Project_Number, appID, new gameNotificationOpenedHandler());
          gameThrive = true;
          logger.log("Gamethrive instance created", TAG);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onPause() {
    // super.onPause();
    OneSignal.onPaused();
  }

  @Override
  public void onResume() {
    checkNotification();
  }

  private void checkNotification() {
    // super.onResume();
    long notificationReceived = -1;

    OneSignal.onResumed();
    notificationReceived = gameBroadcastReceiver.getReceiveDate().getTime();
    if(notificationReceived!=-1)
    {
      EventQueue.pushEvent(new gamethriveNotificationReceived(notificationReceived));
    }
  }

  //Send tags to gameThrive
  public void sendTags(String jsonData) {
    try {
      logger.log(TAG, "Send Tags : " , jsonData);
      JSONObject object = new JSONObject(jsonData);
      OneSignal.sendTags(object);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //Get notification_received_count, Other tags can be gotten in the same way
  public void getNotificationReceivedCount(String params) {
    try {
      OneSignal.getTags(new GetTagsHandler() {
        @Override
        public void tagsAvailable(JSONObject rTags) {
          Integer notificationReceivedCount = 0;
          notificationReceivedCount = gameBroadcastReceiver.getReceiveCount() - 1;

          logger.log(TAG, "retrieved data for receive : ");
          try {

            EventQueue.pushEvent
              (new gamethriveGotReceived
               (""+(rTags.getInt("notification_received_count") +
                 notificationReceivedCount)));

          } catch (org.json.JSONException eJ){
            logger.log(TAG, "Receiving notification for the first time");
            EventQueue.pushEvent(new gamethriveGotReceived(
                                   notificationReceivedCount.toString()));
          } catch (Exception e) {
             e.printStackTrace();
          }
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //Get notification_opened_count, Other tags can be gotten in the same way
  public void getNotificationOpenedCount(String params) {
    try {
      OneSignal.getTags(new GetTagsHandler() {
        @Override
        public void tagsAvailable(JSONObject rTags) {
          logger.log(TAG, "retrieved data for open : ");
          try {
            EventQueue.pushEvent(new gamethriveGotOpened
                                 (rTags.get("notification_opened_count").toString()));
          } catch (org.json.JSONException eJ){
            EventQueue.pushEvent(new gamethriveGotOpened("0"));
            logger.log(TAG, "Opening notification for the first time");
          } catch (Exception e) {
             e.printStackTrace();
          }
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void onStart() {
  }

  public void onStop() {
  }

  public void onDestroy() {
  }

  public void onNewIntent(Intent intent) {
    checkNotification();
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
    public void notificationOpened
    (String message, JSONObject additionalData, boolean isActive) {

      Date current_on_time = new Date();
      long opened_on_time = current_on_time.getTime();  
      String segment_id = null; 
      String title = null; 
      try {
        segment_id = additionalData.getString("segment_id");
        title = additionalData.getString("title");
      } catch (JSONException e) {
        logger.log(TAG, "Error in jsondata");
      }
        
      EventQueue.pushEvent(new gamethriveNotificationOpened
                           (opened_on_time, segment_id, title, message));
    }
  }
}
