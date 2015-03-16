package com.tealeaf.plugin.plugins;

import com.gamethrive.GameBroadcastReceiver;

import com.tealeaf.EventQueue;
import com.tealeaf.event.Event;
import com.tealeaf.plugin.IPlugin;
import com.tealeaf.logger;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;

import com.gamethrive.GameThrive;
import com.gamethrive.GameThrive.*;

import com.gamethrive.NotificationOpenedHandler;

public class GameThrivePlugin implements IPlugin {

  private static final String TAG = "{{GameThrivePlugin}}";

  private static GameThrive gameThrive;

  private GameBroadcastReceiver gameBroadcastReceiver = new GameBroadcastReceiver();

  public class gamethriveNotificationOpened extends com.tealeaf.event.Event {
    boolean failed;
    String notification_opened_on;

    public gamethriveNotificationOpened(String opened_on) {
      super("gamethriveNotificationOpened");
      this.failed = false;
      this.notification_opened_on = opened_on;
    }
  }

  public class gamethriveNotificationReceived extends com.tealeaf.event.Event {
    boolean failed;
    String notification_received_on;

    public gamethriveNotificationReceived(String received_on) {
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
    Date notificationReceived = null;
    PackageManager manager = activity.getPackageManager();

    logger.log("GAMETHRIVE INITIALIZED", TAG);

    try {
      notificationReceived = gameBroadcastReceiver.getReceiveDate();

      if (gameThrive == null){
        Bundle meta = manager.getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA).metaData;

        if (meta != null) {
          g_Project_Number = meta.get("googleProjectNo").toString();
          appID = meta.get("gameThriveAppID").toString();
        }

        logger.log(g_Project_Number, appID, TAG);

        if (appID != null && g_Project_Number != null) {
          gameThrive = new GameThrive(activity, g_Project_Number, appID, new gameNotificationOpenedHandler());
          logger.log("Gamethrive instance created", TAG);
        }
      }

      if(notificationReceived!=null)
      {
        EventQueue.pushEvent(new gamethriveNotificationReceived(notificationReceived.toString()));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onPause() {
    // super.onPause();
    gameThrive.onPaused();
  }

  @Override
  public void onResume() {
    // super.onResume();
    gameThrive.onResumed();
  }

  //Send tags to gameThrive
  public void sendTags(String jsonData) {
    try {
      logger.log(TAG, "Send Tags : " , jsonData);
      JSONObject object = new JSONObject(jsonData);
      gameThrive.sendTags(object);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //Get notification_received_count, Other tags can be gotten in the same way
  public void getNotificationReceivedCount(String params) {
    try {
      gameThrive.getTags(new GetTagsHandler() {
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
      gameThrive.getTags(new GetTagsHandler() {
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

      Date opened_on_time = new Date();

      EventQueue.pushEvent(new gamethriveNotificationOpened
                           (opened_on_time.toString()));
    }
  }
}
