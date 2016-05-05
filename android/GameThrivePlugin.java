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

  private static JSONObject gameThrive_data  = new JSONObject();

  private static JSONObject data_to_send = new JSONObject();

  private static Integer opened_count = 0;

  private static GameBroadcastReceiver gameBroadcastReceiver = new GameBroadcastReceiver();

  public class gamethriveNotificationReceived extends com.tealeaf.event.Event {
    boolean failed;
    String notification_data;

    public gamethriveNotificationReceived(String notification_data) {
      super("gamethriveNotificationReceived");
      this.failed = false;
      this.notification_data = notification_data;
    }
  }

  public class gamethriveNotificationOpened extends com.tealeaf.event.Event {
    String notification_data;

    public gamethriveNotificationOpened(String notification_data) {
      super("gamethriveNotificationOpened");
      this.notification_data = notification_data;
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

        if (appID != null && g_Project_Number != null) {
          OneSignal.init(activity, g_Project_Number, appID, new gameNotificationOpenedHandler());
          OneSignal.enableNotificationsWhenActive(true);
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

  public void onRenderPause() {
  }

  @Override
  public void onResume() {
    checkNotification();
    OneSignal.onResumed();
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
    // super.onResume();
    Date notificationReceived = null;
    long time_stamp = -1; 
    Integer notificationReceivedCount = 0;
    String received_data = null;
    
    //OneSignal.onResumed();
    notificationReceived = gameBroadcastReceiver.getReceiveDate();
    
    if(notificationReceived != null ||
       gameThrive_data.has("last_notification_opened_on"))
    {
      if(notificationReceived != null) 
      {
        time_stamp = notificationReceived.getTime();
        notificationReceivedCount = gameBroadcastReceiver.getReceiveCount();
        try {
          gameThrive_data.put("last_notification_received_on", time_stamp); 
          data_to_send.put("last_notification_received_on", 
                                      notificationReceived.toString());
          gameThrive_data.put("notification_received_count",
                              notificationReceivedCount);
           
          if(!gameThrive_data.has("notification_segment_name")) {
            received_data = gameBroadcastReceiver.getReceiveData("segment_name");
            gameThrive_data.put("notification_segment_name", received_data); 
          } 
           
          if(!gameThrive_data.has("notification_title")) {
            received_data = gameBroadcastReceiver.getReceiveData("title");
            gameThrive_data.put("notification_title", received_data); 
          } 
           
          if(!gameThrive_data.has("notification_message")) {
            received_data = gameBroadcastReceiver.getReceiveData("message");
            gameThrive_data.put("notification_message", received_data); 
          } 
           
        } catch (Exception e) {
          e.printStackTrace();
        }
        getNotificationReceivedCount(notificationReceivedCount);
      }
      sendTags(data_to_send);
      EventQueue.pushEvent(new gamethriveNotificationReceived(
                           gameThrive_data.toString()));
      data_to_send = new JSONObject();
      gameThrive_data = new JSONObject();
    }
  }

  //Send tags to gameThrive
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
    public void notificationOpened
    (String message, JSONObject additionalData, boolean isActive) {

      Date current_time = new Date();
      long opened_on_time = current_time.getTime();  
      String segment_id = null; 
      String title = null;
      opened_count += 1;  
      try {
        title = additionalData.getString("title");
        segment_id = additionalData.getString("segment_name");
      } catch (JSONException e) {
        logger.log(TAG, "Error in jsondata");
      }

      try {
        gameThrive_data.put("notification_segment_name", segment_id);   
        gameThrive_data.put("notification_title", title);   
        gameThrive_data.put("notification_message", message);   
        gameThrive_data.put("last_notification_opened_on", opened_on_time);   
        gameThrive_data.put("notification_opened_count", opened_count);
        logger.log("{gamethrive}", "notification opened called");
        EventQueue.pushEvent(new gamethriveNotificationOpened(
                           gameThrive_data.toString()));
        data_to_send.put("last_notification_opened_on", current_time.toString()); 
      } catch (JSONException e) {
        e.printStackTrace();
      }  
      getNotificationOpenedCount(); 
    }
  }
}
